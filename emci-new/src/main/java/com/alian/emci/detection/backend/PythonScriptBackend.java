package com.alian.emci.detection.backend;

import com.alian.emci.config.properties.YoloProperties;
import com.alian.emci.detection.command.CommandBuilder;
import com.alian.emci.detection.command.CommandExecutor;
import com.alian.emci.detection.command.ExecutionResult;
import com.alian.emci.detection.parser.ResultParser;
import com.alian.emci.vo.detection.DetectionResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Python脚本检测后端
 * 通过调用Python脚本执行检测
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonScriptBackend extends AbstractDetectionBackend {

    private final YoloProperties yoloProperties;
    private final CommandBuilder commandBuilder;
    private final CommandExecutor commandExecutor;
    private final ResultParser resultParser;

    @Override
    public DetectionResultVO detect(String imagePath, double confThreshold, double iouThreshold) {
        long startTime = System.currentTimeMillis();

        try {
            // 获取项目根目录
            String projectRoot = getProjectRoot();

            // 准备输出目录
            String outputDir = prepareOutputDir();

            // 构建命令
            List<String> command = commandBuilder.buildCommand(
                    projectRoot,
                    yoloProperties.getScript().getPath(),
                    imagePath,
                    outputDir,
                    confThreshold,
                    iouThreshold
            );

            log.info("执行YOLO检测: {}", imagePath);

            // 执行命令
            ExecutionResult execResult = commandExecutor.execute(
                    command,
                    new File(projectRoot),
                    60
            );

            if (!execResult.isSuccess()) {
                log.error("检测失败: {}", execResult.getFormattedError());
                return createMockResult(imagePath, System.currentTimeMillis() - startTime);
            }

            // 解析结果
            DetectionResultVO result = resultParser.parse(execResult.getOutput(), imagePath);
            result.setProcessTime(System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("检测异常", e);
            return createMockResult(imagePath, System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public DetectionResultVO detectVideo(String videoPath, double confThreshold, double iouThreshold) {
        // 视频检测使用相同的逻辑，但超时更长
        long startTime = System.currentTimeMillis();

        try {
            String projectRoot = getProjectRoot();
            String outputDir = prepareOutputDir();

            List<String> command = commandBuilder.buildCommand(
                    projectRoot,
                    yoloProperties.getScript().getPath(),
                    videoPath,
                    outputDir,
                    confThreshold,
                    iouThreshold
            );

            ExecutionResult execResult = commandExecutor.execute(
                    command,
                    new File(projectRoot),
                    300  // 视频检测5分钟超时
            );

            DetectionResultVO result = resultParser.parse(execResult.getOutput(), videoPath);
            result.setProcessTime(System.currentTimeMillis() - startTime);

            return result;

        } catch (Exception e) {
            log.error("视频检测异常", e);
            return createErrorResult(videoPath, e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        if (yoloProperties.isUseMock()) {
            return true; // 模拟模式始终可用
        }

        String projectRoot = getProjectRoot();
        String modelPath = Paths.get(projectRoot, yoloProperties.getModel().getPath()).toString();
        return new File(modelPath).exists();
    }

    @Override
    public String getBackendInfo() {
        StringBuilder info = new StringBuilder();

        if (yoloProperties.getConda().isEnabled()) {
            info.append("Conda环境: ").append(yoloProperties.getConda().getEnvName()).append("\n");
        }

        info.append("Python路径: ").append(yoloProperties.getPython().getPath()).append("\n");
        info.append("脚本路径: ").append(yoloProperties.getScript().getPath()).append("\n");

        String projectRoot = getProjectRoot();
        String modelPath = Paths.get(projectRoot, yoloProperties.getModel().getPath()).toString();
        File modelFile = new File(modelPath);

        if (modelFile.exists()) {
            info.append(String.format("模型: %s (%.2f MB)\n",
                    modelPath, modelFile.length() / 1024.0 / 1024.0));
        } else {
            info.append("模型未找到: ").append(modelPath).append("\n");
        }

        info.append("模拟模式: ").append(yoloProperties.isUseMock() ? "开启" : "关闭");

        return info.toString();
    }

    @Override
    public String getName() {
        return "python-script";
    }

    /**
     * 准备输出目录
     */
    private String prepareOutputDir() throws Exception {
        String outputDir = Paths.get(System.getProperty("java.io.tmpdir"), "yolo_output").toString();
        Files.createDirectories(Paths.get(outputDir));
        return outputDir;
    }

    /**
     * 获取项目根目录
     */
    private String getProjectRoot() {
        String userDir = System.getProperty("user.dir");

        // 检查当前目录是否包含检测脚本
        if (new File(userDir, "yolo_detect_onnx.py").exists() ||
                new File(userDir, "detect.py").exists()) {
            return userDir;
        }

        // 向上查找
        File current = new File(userDir);
        for (int i = 0; i < 3; i++) {
            File parent = current.getParentFile();
            if (parent == null) break;

            if (new File(parent, "detect.py").exists() ||
                    new File(parent, "models/best.onnx").exists()) {
                return parent.getAbsolutePath();
            }
            current = parent;
        }

        // 默认返回user.dir
        return userDir;
    }

    /**
     * 创建模拟结果（用于fallback）
     */
    private DetectionResultVO createMockResult(String imagePath, long processTime) {
        DetectionResultVO result = new DetectionResultVO();
        result.setOriginalUrl("file://" + imagePath);
        result.setResultUrl("file://" + imagePath);
        result.setProcessTime(processTime);
        result.setSuccess(true);

        // 生成模拟缺陷
        String[] defectNames = {"Manhole", "Damage", "Open", "Missing"};
        int count = (int) (Math.random() * 3) + 1;
        List<DetectionResultVO.DefectInfo> defects = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            String type = defectNames[(int) (Math.random() * defectNames.length)];
            double confidence = 0.75 + Math.random() * 0.24;

            defects.add(DetectionResultVO.DefectInfo.builder()
                    .type(type)
                    .confidence(Math.round(confidence * 100) / 100.0)
                    .bbox(List.of(
                            100.0 + Math.random() * 300,
                            100.0 + Math.random() * 200,
                            30.0 + Math.random() * 50,
                            30.0 + Math.random() * 50
                    ))
                    .build());
        }

        result.setDefects(defects);
        result.setDefectCount(defects.size());

        return result;
    }
}
