package com.alian.emci.service.impl;

import com.alian.emci.detection.backend.BackendFactory;
import com.alian.emci.detection.core.DetectionBackend;
import com.alian.emci.service.FileStorageService;
import com.alian.emci.service.YoloDetectionService;
import com.alian.emci.vo.detection.DetectionResultVO;
import com.alian.emci.vo.file.FileUploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * YOLO检测服务实现 - 重构版
 * 使用检测后端工厂进行解耦合
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class YoloDetectionServiceImplV2 implements YoloDetectionService {

    private final BackendFactory backendFactory;
    private final FileStorageService fileStorageService;

    @Override
    public DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold) throws IOException {
        return detectImage(imageFile, confThreshold, iouThreshold, null);
    }

    @Override
    public DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold, String manholeId) throws IOException {
        // 保存上传的文件到临时目录
        Path inputPath = saveTempFile(imageFile);

        try {
            // 获取后端并执行检测
            DetectionBackend backend = backendFactory.getDefaultBackend();
            DetectionResultVO result = backend.detect(
                    inputPath.toString(),
                    confThreshold != null ? confThreshold : 0.7,
                    iouThreshold != null ? iouThreshold : 0.5
            );

            // 处理结果保存
            if (manholeId != null && !manholeId.isEmpty()) {
                processAndSaveResult(result, manholeId, getExtension(imageFile.getOriginalFilename()));
            }

            return result;

        } finally {
            // 清理临时文件
            cleanupTempFile(inputPath);
        }
    }

    @Override
    public DetectionResultVO detectImageFile(String imagePath, Double confThreshold, Double iouThreshold) {
        DetectionBackend backend = backendFactory.getDefaultBackend();
        return backend.detect(
                imagePath,
                confThreshold != null ? confThreshold : 0.7,
                iouThreshold != null ? iouThreshold : 0.5
        );
    }

    @Override
    public List<DetectionResultVO> detectBatch(List<MultipartFile> imageFiles, Double confThreshold, Double iouThreshold) {
        return imageFiles.stream()
                .map(file -> {
                    try {
                        return detectImage(file, confThreshold, iouThreshold);
                    } catch (IOException e) {
                        log.error("批量检测失败: {}", file.getOriginalFilename(), e);
                        return createErrorResult(file.getOriginalFilename(), e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public DetectionResultVO detectVideo(MultipartFile videoFile, Double confThreshold, Double iouThreshold) throws IOException {
        Path inputPath = saveTempFile(videoFile);

        try {
            DetectionBackend backend = backendFactory.getDefaultBackend();
            return backend.detectVideo(
                    inputPath.toString(),
                    confThreshold != null ? confThreshold : 0.7,
                    iouThreshold != null ? iouThreshold : 0.5
            );
        } finally {
            cleanupTempFile(inputPath);
        }
    }

    @Override
    public String getModelInfo() {
        DetectionBackend backend = backendFactory.getDefaultBackend();
        return backend.getBackendInfo();
    }

    @Override
    public boolean isModelAvailable() {
        return backendFactory.getDefaultBackend().isAvailable();
    }

    // ========== 私有辅助方法 ==========

    /**
     * 保存临时文件
     */
    private Path saveTempFile(MultipartFile file) throws IOException {
        String tempDir = System.getProperty("java.io.tmpdir");
        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + extension;
        Path path = Paths.get(tempDir, "yolo_input", fileName);

        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        return path;
    }

    /**
     * 清理临时文件
     */
    private void cleanupTempFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("清理临时文件失败: {}", path);
        }
    }

    /**
     * 处理并保存结果
     */
    private void processAndSaveResult(DetectionResultVO result, String manholeId, String extension) {
        String resultUrl = result.getResultUrl();
        if (resultUrl == null || !(resultUrl.startsWith("file://") || resultUrl.contains(":\\"))) {
            return;
        }

        File resultFile = new File(resultUrl.replace("file://", ""));
        if (!resultFile.exists()) {
            return;
        }

        try {
            // 上传到存储
            String fileName = manholeId + "_1";
            FileUploadVO upload = fileStorageService.uploadBytesWithName(
                    Files.readAllBytes(resultFile.toPath()),
                    fileName,
                    extension,
                    "image/" + extension,
                    "detections"
            );

            result.setResultUrl(upload.getUrl());
            result.setOriginalUrl("/uploads/manholes/" + manholeId + "." + extension);

        } catch (Exception e) {
            log.warn("上传结果图片失败: {}", e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 创建错误结果
     */
    private DetectionResultVO createErrorResult(String filename, String error) {
        DetectionResultVO result = new DetectionResultVO();
        result.setSuccess(false);
        result.setOriginalUrl(filename);
        result.setError(error);
        result.setDefectCount(0);
        return result;
    }
}
