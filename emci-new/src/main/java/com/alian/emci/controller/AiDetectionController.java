package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.service.DetectionService;
import com.alian.emci.service.YoloDetectionService;
import com.alian.emci.vo.detection.DetectionResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * AI检测控制器
 */
@Slf4j
@Tag(name = "AI检测", description = "AI井盖检测相关接口")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiDetectionController {

    private final YoloDetectionService yoloDetectionService;
    private final DetectionService detectionService;

    @Operation(summary = "图片检测", description = "上传图片进行AI检测")
    @PostMapping("/detect/image")
    public Result<DetectionResultVO> detectImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0.7") Double confThreshold,
            @RequestParam(defaultValue = "0.5") Double iouThreshold,
            @RequestParam(required = false) String manholeId) {

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        try {
            log.info("开始图片检测: file={}, manholeId={}, conf={}, iou={}",
                    file.getOriginalFilename(), manholeId, confThreshold, iouThreshold);

            // 执行检测（只检测，不保存）
            DetectionResultVO result = yoloDetectionService.detectImage(file, confThreshold, iouThreshold, manholeId);

            log.info("图片检测完成: {}，井盖ID: {}，发现 {} 个缺陷",
                    file.getOriginalFilename(), manholeId, result.getDefectCount());
            return Result.success("检测成功", result);

        } catch (IOException e) {
            log.error("图片检测失败", e);
            return Result.error("检测失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量图片检测", description = "上传多张图片进行AI检测")
    @PostMapping("/detect/batch")
    public Result<List<DetectionResultVO>> detectBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "0.7") Double confThreshold,
            @RequestParam(defaultValue = "0.5") Double iouThreshold) {

        if (files.isEmpty()) {
            return Result.error("文件不能为空");
        }

        List<DetectionResultVO> results = yoloDetectionService.detectBatch(files, confThreshold, iouThreshold);
        return Result.success("批量检测完成", results);
    }

    @Operation(summary = "视频检测", description = "上传视频进行AI检测")
    @PostMapping("/detect/video")
    public Result<DetectionResultVO> detectVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0.7") Double confThreshold,
            @RequestParam(defaultValue = "0.5") Double iouThreshold) {

        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return Result.error("只能上传视频文件");
        }

        try {
            DetectionResultVO result = yoloDetectionService.detectVideo(file, confThreshold, iouThreshold);
            log.info("视频检测完成: {}，发现 {} 个缺陷", file.getOriginalFilename(), result.getDefectCount());
            return Result.success("检测成功", result);

        } catch (IOException e) {
            log.error("视频检测失败", e);
            return Result.error("检测失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取支持的模型列表", description = "获取所有可用的AI检测模型")
    @GetMapping("/models")
    public Result<List<String>> getAvailableModels() {
        return Result.success(Arrays.asList("yolov8s", "yolov8m", "yolov8l", "best.pt"));
    }

    @Operation(summary = "获取模型信息", description = "获取当前使用的模型信息")
    @GetMapping("/model-info")
    public Result<String> getModelInfo() {
        return Result.success(yoloDetectionService.getModelInfo());
    }

    @Operation(summary = "检查模型状态", description = "检查AI模型是否可用")
    @GetMapping("/model-status")
    public Result<Boolean> checkModelStatus() {
        return Result.success(yoloDetectionService.isModelAvailable());
    }

    @Operation(summary = "保存检测结果", description = "将AI检测结果保存到数据库")
    @PostMapping("/save")
    public Result<DetectionResultVO> saveDetectionResult(
            @RequestParam String manholeId,
            @RequestBody DetectionResultVO result) {

        if (manholeId == null || manholeId.isEmpty()) {
            return Result.error("井盖ID不能为空");
        }

        if (result == null) {
            return Result.error("检测结果不能为空");
        }

        try {
            log.info("手动保存检测记录: manholeId={}, defects={}", manholeId, result.getDefectCount());
            var saveResult = detectionService.saveDetectionRecord(manholeId, result);

            if (saveResult.getCode() == 200) {
                log.info("检测记录保存成功: detectionNo={}",
                        saveResult.getData() != null ? saveResult.getData().getDetectionNo() : "unknown");
                return Result.success("保存成功", result);
            } else {
                log.warn("检测记录保存失败: {}", saveResult.getMessage());
                return Result.error("保存失败: " + saveResult.getMessage());
            }
        } catch (Exception e) {
            log.error("保存检测记录异常: manholeId={}", manholeId, e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }
}
