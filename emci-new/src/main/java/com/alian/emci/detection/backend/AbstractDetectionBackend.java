package com.alian.emci.detection.backend;

import com.alian.emci.detection.core.DetectionBackend;
import com.alian.emci.vo.detection.DetectionResultVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 检测后端抽象基类
 * 提供通用的批量检测实现
 */
@Slf4j
public abstract class AbstractDetectionBackend implements DetectionBackend {

    @Override
    public List<DetectionResultVO> detectBatch(List<String> imagePaths, double confThreshold, double iouThreshold) {
        return imagePaths.stream()
                .map(path -> {
                    try {
                        return detect(path, confThreshold, iouThreshold);
                    } catch (Exception e) {
                        log.error("批量检测失败: {}", path, e);
                        return createErrorResult(path, e.getMessage());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 创建错误结果
     */
    protected DetectionResultVO createErrorResult(String inputPath, String error) {
        DetectionResultVO result = new DetectionResultVO();
        result.setSuccess(false);
        result.setOriginalUrl(inputPath);
        result.setError(error);
        result.setDefectCount(0);
        return result;
    }

    /**
     * 创建成功结果
     */
    protected DetectionResultVO createSuccessResult(String inputPath, List<DetectionResultVO.DefectInfo> defects) {
        DetectionResultVO result = new DetectionResultVO();
        result.setSuccess(true);
        result.setOriginalUrl(inputPath);
        result.setResultUrl(inputPath);
        result.setDefects(defects);
        result.setDefectCount(defects != null ? defects.size() : 0);
        return result;
    }
}
