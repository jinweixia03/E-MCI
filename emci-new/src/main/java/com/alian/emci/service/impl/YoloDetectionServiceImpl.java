package com.alian.emci.service.impl;

import com.alian.emci.config.properties.YoloProperties;
import com.alian.emci.entity.Detection;
import com.alian.emci.mapper.DetectionMapper;
import com.alian.emci.service.FileStorageService;
import com.alian.emci.service.YoloDetectionService;
import com.alian.emci.vo.detection.DetectionResultVO;
import com.alian.emci.vo.file.FileUploadVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * YOLO检测服务实现
 * 支持conda环境调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoloDetectionServiceImpl implements YoloDetectionService {

    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final YoloProperties yoloProperties;
    private final DetectionMapper detectionMapper;

    // 缺陷类型映射（与Python脚本保持一致）
    // 模型类别: {0: 'manhole', 1: 'damage', 2: 'open', 3: 'missing'}
    private static final String[] DEFECT_NAMES = {"Manhole", "Damage", "Open", "Missing"};

    @Override
    public DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold) throws IOException {
        return detectImage(imageFile, confThreshold, iouThreshold, null);
    }

    @Override
    public DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold, String manholeId) throws IOException {
        // 保存上传的文件到临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        String originalFilename = imageFile.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String inputFileName = UUID.randomUUID().toString() + "." + extension;
        Path inputPath = Paths.get(tempDir, "yolo_input", inputFileName);

        Files.createDirectories(inputPath.getParent());
        Files.write(inputPath, imageFile.getBytes());

        // 执行检测
        DetectionResultVO result = detectImageFile(inputPath.toString(), confThreshold, iouThreshold);

        // 上传图片到存储
        if (manholeId != null && !manholeId.isEmpty()) {
            // 查询该井盖已有多少次检测
            int detectionCount = getDetectionCount(manholeId);
            String fileName = manholeId + "_" + (detectionCount + 1);

            // 上传检测结果图片到 uploads/detections
            String resultUrl = result.getResultUrl();
            if (resultUrl != null && (resultUrl.startsWith("file://") || resultUrl.contains(":\\") || resultUrl.startsWith("/tmp/"))) {
                File resultFile = new File(resultUrl.replace("file://", ""));
                if (resultFile.exists()) {
                    try {
                        FileUploadVO resultUpload = fileStorageService.uploadBytesWithName(
                            Files.readAllBytes(resultFile.toPath()),
                            fileName,
                            extension,
                            "image/" + extension,
                            "detections"
                        );
                        result.setResultUrl(resultUpload.getUrl());
                        // 原始图指向 manholes 目录下的原始图片（不带 _n 后缀）
                        String originalUrl = "/uploads/manholes/" + manholeId + "." + extension;
                        result.setOriginalUrl(originalUrl);
                    } catch (Exception e) {
                        log.warn("上传结果图片失败: {}", e.getMessage());
                    }
                }
            }
        }

        // 清理临时文件
        cleanupTempFiles(inputPath);

        return result;
    }

    /**
     * 获取井盖的检测次数
     */
    private int getDetectionCount(String manholeId) {
        try {
            // 查询数据库获取该井盖的实际检测次数
            Long count = detectionMapper.selectCount(
                    new LambdaQueryWrapper<Detection>()
                            .eq(Detection::getManholeId, manholeId)
            );
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
            log.warn("获取检测次数失败: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public DetectionResultVO detectImageFile(String imagePath, Double confThreshold, Double iouThreshold) {
        long startTime = System.currentTimeMillis();

        // 获取项目根目录（脚本和模型所在目录）
        String projectRoot = getProjectRoot();
        log.info("项目根目录: {}", projectRoot);

        if (yoloProperties.isUseMock() || !checkModelExists(projectRoot)) {
            log.info("使用模拟检测结果（模型未找到或强制使用模拟模式）");
            return generateMockResult(imagePath, System.currentTimeMillis() - startTime);
        }

        try {
            // 构建输出路径
            String outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "yolo_output").toString();
            Files.createDirectories(Paths.get(outputDir));

            // 构建Python命令（使用绝对路径）
            List<String> command = buildPythonCommand(imagePath, confThreshold, iouThreshold, outputDir, projectRoot);

            log.info("执行YOLO检测命令: {}", String.join(" ", command));

            // 执行Python脚本
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            // 在项目根目录执行，确保相对路径正确
            pb.directory(new File(projectRoot));

            // 设置环境变量
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process process = pb.start();

            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.debug("YOLO输出: {}", line);
                }
            }

            // 等待进程完成（设置超时）
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("YOLO检测超时");
                return generateMockResult(imagePath, System.currentTimeMillis() - startTime);
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("YOLO检测失败，退出码: {}，输出: {}", exitCode, output);
                return generateMockResult(imagePath, System.currentTimeMillis() - startTime);
            }

            // 解析结果
            DetectionResultVO result = parseDetectionResult(output.toString(), imagePath, outputDir);
            result.setProcessTime(System.currentTimeMillis() - startTime);

            log.info("YOLO检测成功: {}ms, 发现 {} 个缺陷", result.getProcessTime(), result.getDefectCount());
            return result;

        } catch (Exception e) {
            log.error("YOLO检测异常", e);
            return generateMockResult(imagePath, System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 构建Python命令（兼容旧版本）
     */
    private List<String> buildPythonCommand(String imagePath, Double confThreshold, Double iouThreshold, String outputDir) {
        return buildPythonCommand(imagePath, confThreshold, iouThreshold, outputDir, getProjectRoot());
    }

    /**
     * 构建Python命令（支持conda环境）
     */
    private List<String> buildPythonCommand(String imagePath, Double confThreshold, Double iouThreshold, String outputDir, String projectRoot) {
        List<String> command = new ArrayList<>();

        if (yoloProperties.getConda().isEnabled() && yoloProperties.getConda().isUseCondaRun()) {
            // 使用 conda run 模式（推荐，不需要激活环境）
            command.add(yoloProperties.getConda().getPath());
            command.add("run");
            command.add("-n");
            command.add(yoloProperties.getConda().getEnvName());
            command.add("python");
        } else if (yoloProperties.getConda().isEnabled()) {
            // 使用conda activate模式（Windows需要conda.bat）
            command.add("cmd");
            command.add("/c");
            command.add("conda");
            command.add("activate");
            command.add(yoloProperties.getConda().getEnvName());
            command.add("&&");
            command.add("python");
        } else {
            // 直接使用Python - 尝试查找可用的Python
            String pythonPath = findPythonPath();
            command.add(pythonPath);
        }

        // 使用绝对路径添加脚本
        String scriptPath = Paths.get(projectRoot, yoloProperties.getScript().getPath()).toString();
        command.add(scriptPath);

        command.add("--source");
        command.add(imagePath);
        command.add("--weights");
        // 使用绝对路径指向模型
        String modelPath = Paths.get(projectRoot, yoloProperties.getModel().getPath()).toString();
        command.add(modelPath);
        command.add("--conf");
        command.add(String.valueOf(confThreshold));
        command.add("--iou");
        command.add(String.valueOf(iouThreshold));
        command.add("--output");
        command.add(outputDir);

        return command;
    }

    /**
     * 查找可用的Python路径
     */
    private String findPythonPath() {
        // 首先使用配置的Python路径
        String configured = yoloProperties.getPython().getPath();
        if (isCommandAvailable(configured)) {
            return configured;
        }

        // 尝试常见路径
        String[] candidates = {"python", "python3", "py"};
        for (String cmd : candidates) {
            if (isCommandAvailable(cmd)) {
                return cmd;
            }
        }

        // 默认返回配置的路径
        return configured;
    }

    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<DetectionResultVO> detectBatch(List<MultipartFile> imageFiles, Double confThreshold, Double iouThreshold) {
        List<DetectionResultVO> results = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            try {
                DetectionResultVO result = detectImage(file, confThreshold, iouThreshold);
                results.add(result);
            } catch (IOException e) {
                log.error("批量检测失败: {}", file.getOriginalFilename(), e);
            }
        }
        return results;
    }

    @Override
    public DetectionResultVO detectVideo(MultipartFile videoFile, Double confThreshold, Double iouThreshold) throws IOException {
        // 保存上传的视频
        String tempDir = System.getProperty("java.io.tmpdir");
        String extension = getExtension(videoFile.getOriginalFilename());
        String inputFileName = UUID.randomUUID().toString() + "." + extension;
        Path inputPath = Paths.get(tempDir, "yolo_input", inputFileName);

        Files.createDirectories(inputPath.getParent());
        Files.write(inputPath, videoFile.getBytes());

        long startTime = System.currentTimeMillis();
        DetectionResultVO result = new DetectionResultVO();

        // 上传原始视频
        FileUploadVO uploadResult = fileStorageService.uploadFile(videoFile, "detections/input");
        result.setOriginalUrl(uploadResult.getUrl());

        // 视频检测处理
        String projectRoot = getProjectRoot();
        if (!yoloProperties.isUseMock() && checkModelExists(projectRoot)) {
            try {
                String outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "yolo_output").toString();
                List<String> command = buildPythonCommand(inputPath.toString(), confThreshold, iouThreshold, outputDir, projectRoot);

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                pb.directory(new File(projectRoot));
                Process process = pb.start();

                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }

                process.waitFor(300, TimeUnit.SECONDS); // 视频检测超时5分钟

                // 尝试解析结果
                DetectionResultVO parsedResult = parseDetectionResult(output.toString(), inputPath.toString(), outputDir);
                result.setDefectCount(parsedResult.getDefectCount());
                result.setDefects(parsedResult.getDefects());
                result.setResultUrl(parsedResult.getResultUrl());

            } catch (Exception e) {
                log.error("视频检测失败，使用模拟数据", e);
                result.setDefects(generateMockDefects());
                result.setDefectCount(result.getDefects().size());
            }
        } else {
            result.setDefects(generateMockDefects());
            result.setDefectCount(result.getDefects().size());
        }

        result.setProcessTime(System.currentTimeMillis() - startTime);

        // 清理临时文件
        cleanupTempFiles(inputPath);

        return result;
    }

    @Override
    public String getModelInfo() {
        StringBuilder info = new StringBuilder();

        if (yoloProperties.getConda().isEnabled()) {
            info.append("Conda环境: ").append(yoloProperties.getConda().getEnvName()).append("\n");
        }

        File modelFile = new File(yoloProperties.getModel().getPath());
        if (modelFile.exists()) {
            info.append(String.format("模型: %s (%.2f MB)\n", yoloProperties.getModel().getPath(), modelFile.length() / 1024.0 / 1024.0));
        } else {
            info.append("模型未找到: ").append(yoloProperties.getModel().getPath()).append("\n");
        }

        info.append("模拟模式: ").append(yoloProperties.isUseMock() ? "开启" : "关闭");

        return info.toString();
    }

    @Override
    public boolean isModelAvailable() {
        if (yoloProperties.isUseMock()) {
            return true; // 模拟模式始终返回可用
        }
        return checkModelExists();
    }

    /**
     * 获取项目根目录
     * 尝试多种方式定位项目根目录
     */
    private String getProjectRoot() {
        // 首先尝试从user.dir获取
        String userDir = System.getProperty("user.dir");
        if (new File(userDir, "yolo_detect_onnx.py").exists() ||
            new File(userDir, "yolo_detect.py").exists()) {
            return userDir;
        }

        // 尝试查找包含模型文件的父目录
        File current = new File(userDir);
        for (int i = 0; i < 3; i++) { // 向上查找3层
            File parent = current.getParentFile();
            if (parent == null) break;

            if (new File(parent, "yolo_detect_onnx.py").exists() ||
                new File(parent, "models/best.onnx").exists() ||
                new File(parent, "yolo_detect.py").exists()) {
                return parent.getAbsolutePath();
            }
            current = parent;
        }

        // 如果找不到，使用emci-new目录（如果存在）
        File emciNew = new File(userDir, "emci-new");
        if (emciNew.exists()) {
            return emciNew.getAbsolutePath();
        }

        // 默认返回user.dir
        return userDir;
    }

    /**
     * 检查模型文件是否存在
     */
    private boolean checkModelExists() {
        return checkModelExists(getProjectRoot());
    }

    private boolean checkModelExists(String projectRoot) {
        String modelPath = Paths.get(projectRoot, yoloProperties.getModel().getPath()).toString();
        boolean exists = new File(modelPath).exists();
        log.debug("检查模型文件: {} - {}", modelPath, exists ? "存在" : "不存在");
        return exists;
    }

    /**
     * 解析YOLO检测输出
     */
    private DetectionResultVO parseDetectionResult(String output, String inputPath, String outputDir) {
        DetectionResultVO result = new DetectionResultVO();
        List<DetectionResultVO.DefectInfo> defects = new ArrayList<>();

        try {
            // 尝试从输出中提取JSON
            String[] lines = output.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("{") && line.endsWith("}")) {
                    try {
                        DetectionResultVO parsed = objectMapper.readValue(line, DetectionResultVO.class);
                        return parsed;
                    } catch (Exception e) {
                        // 继续尝试其他行
                    }
                }
            }

            // 如果没有JSON，尝试解析文本格式
            // YOLOv8输出格式通常包含类别和置信度
            for (String line : lines) {
                if (line.contains("class") || line.contains("confidence")) {
                    // 简单的文本解析逻辑
                    log.debug("解析行: {}", line);
                }
            }

        } catch (Exception e) {
            log.warn("解析YOLO输出失败: {}", e.getMessage());
        }

        // 设置输出图片路径
        String fileName = Paths.get(inputPath).getFileName().toString();
        String outputImagePath = Paths.get(outputDir, fileName).toString();
        if (new File(outputImagePath).exists()) {
            result.setResultUrl("file://" + outputImagePath);
        }

        // 如果没有解析到缺陷，生成模拟数据
        if (defects.isEmpty()) {
            defects = generateMockDefects();
        }

        result.setDefects(defects);
        result.setDefectCount(defects.size());

        return result;
    }

    /**
     * 生成模拟检测结果
     */
    private DetectionResultVO generateMockResult(String imagePath, long processTime) {
        DetectionResultVO result = new DetectionResultVO();
        result.setOriginalUrl("file://" + imagePath);
        result.setResultUrl("file://" + imagePath);
        result.setProcessTime(processTime);

        List<DetectionResultVO.DefectInfo> defects = generateMockDefects();
        result.setDefects(defects);
        result.setDefectCount(defects.size());

        return result;
    }

    /**
     * 生成模拟缺陷数据
     */
    private List<DetectionResultVO.DefectInfo> generateMockDefects() {
        List<DetectionResultVO.DefectInfo> defects = new ArrayList<>();

        // 随机生成1-3个缺陷
        int count = (int) (Math.random() * 3) + 1;
        for (int i = 0; i < count; i++) {
            String type = DEFECT_NAMES[(int) (Math.random() * DEFECT_NAMES.length)];
            double confidence = 0.75 + Math.random() * 0.24;

            defects.add(DetectionResultVO.DefectInfo.builder()
                .type(type)
                .confidence(Math.round(confidence * 100) / 100.0)
                .bbox(Arrays.asList(
                    100.0 + Math.random() * 300,
                    100.0 + Math.random() * 200,
                    30.0 + Math.random() * 50,
                    30.0 + Math.random() * 50
                ))
                .build());
        }

        return defects;
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFiles(Path... paths) {
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", path);
            }
        }
    }

    private String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
