package com.alian.emci.detection.backend;

import com.alian.emci.vo.detection.DetectionResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 模拟检测后端
 * 用于测试和演示，无需实际模型
 */
@Slf4j
@Component
public class MockDetectionBackend extends AbstractDetectionBackend {

    private static final String[] DEFECT_NAMES = {"Manhole", "Damage", "Open", "Missing"};

    @Override
    public DetectionResultVO detect(String imagePath, double confThreshold, double iouThreshold) {
        log.info("[模拟检测] 图片: {}, conf: {}, iou: {}", imagePath, confThreshold, iouThreshold);

        long startTime = System.currentTimeMillis();

        // 模拟处理时间（50-200ms）
        try {
            Thread.sleep((long) (Math.random() * 150 + 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        DetectionResultVO result = new DetectionResultVO();
        result.setOriginalUrl("file://" + imagePath);
        result.setResultUrl("file://" + imagePath);
        result.setSuccess(true);
        result.setProcessTime(System.currentTimeMillis() - startTime);

        // 随机生成1-3个缺陷
        List<DetectionResultVO.DefectInfo> defects = generateMockDefects();
        result.setDefects(defects);
        result.setDefectCount(defects.size());

        log.info("[模拟检测] 完成，发现 {} 个缺陷", defects.size());

        return result;
    }

    @Override
    public DetectionResultVO detectVideo(String videoPath, double confThreshold, double iouThreshold) {
        log.info("[模拟检测] 视频: {}", videoPath);

        long startTime = System.currentTimeMillis();

        // 模拟更长的处理时间
        try {
            Thread.sleep((long) (Math.random() * 1000 + 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        DetectionResultVO result = new DetectionResultVO();
        result.setOriginalUrl("file://" + videoPath);
        result.setResultUrl("file://" + videoPath);
        result.setSuccess(true);
        result.setProcessTime(System.currentTimeMillis() - startTime);

        // 视频检测生成更多缺陷
        List<DetectionResultVO.DefectInfo> defects = generateMockDefects(2, 5);
        result.setDefects(defects);
        result.setDefectCount(defects.size());

        log.info("[模拟检测] 视频完成，发现 {} 个缺陷", defects.size());

        return result;
    }

    @Override
    public boolean isAvailable() {
        return true; // 模拟后端始终可用
    }

    @Override
    public String getBackendInfo() {
        return "模拟检测后端 (用于测试)\n" +
               "特点:\n" +
               "- 无需模型文件\n" +
               "- 随机生成检测结果\n" +
               "- 适合开发和演示";
    }

    @Override
    public String getName() {
        return "mock";
    }

    /**
     * 生成模拟缺陷
     */
    private List<DetectionResultVO.DefectInfo> generateMockDefects() {
        return generateMockDefects(1, 3);
    }

    private List<DetectionResultVO.DefectInfo> generateMockDefects(int minCount, int maxCount) {
        List<DetectionResultVO.DefectInfo> defects = new ArrayList<>();
        int count = (int) (Math.random() * (maxCount - minCount + 1)) + minCount;

        for (int i = 0; i < count; i++) {
            String type = DEFECT_NAMES[(int) (Math.random() * DEFECT_NAMES.length)];
            double confidence = 0.7 + Math.random() * 0.29; // 0.7-0.99

            defects.add(DetectionResultVO.DefectInfo.builder()
                    .type(type)
                    .confidence(Math.round(confidence * 100) / 100.0)
                    .bbox(Arrays.asList(
                            50.0 + Math.random() * 400,
                            50.0 + Math.random() * 300,
                            20.0 + Math.random() * 100,
                            20.0 + Math.random() * 100
                    ))
                    .build());
        }

        return defects;
    }
}
