package com.alian.emci.detection.core;

import lombok.Builder;
import lombok.Getter;

import java.nio.file.Path;

/**
 * 检测上下文
 * 封装检测过程中的共享数据
 */
@Getter
@Builder
public class DetectionContext {

    /**
     * 输入文件路径
     */
    private final Path inputPath;

    /**
     * 输出目录
     */
    private final Path outputDir;

    /**
     * 置信度阈值
     */
    private final double confThreshold;

    /**
     * IOU阈值
     */
    private final double iouThreshold;

    /**
     * 井盖ID（可选，用于保存结果）
     */
    private final String manholeId;

    /**
     * 处理超时时间（秒）
     */
    @Builder.Default
    private final int timeoutSeconds = 60;

    /**
     * 创建临时上下文
     */
    public static DetectionContext createTempContext(String inputPath, double conf, double iou) {
        return DetectionContext.builder()
                .inputPath(Path.of(inputPath))
                .outputDir(Path.of(System.getProperty("java.io.tmpdir"), "yolo_output"))
                .confThreshold(conf)
                .iouThreshold(iou)
                .build();
    }
}
