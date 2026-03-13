package com.alian.emci.detection.backend;

import com.alian.emci.config.properties.YoloProperties;
import com.alian.emci.detection.core.BackendType;
import com.alian.emci.detection.core.DetectionBackend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 检测后端工厂
 * 管理检测后端的创建和选择
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackendFactory {

    private final PythonScriptBackend pythonScriptBackend;
    private final MockDetectionBackend mockDetectionBackend;
    private final YoloProperties yoloProperties;

    private final Map<BackendType, DetectionBackend> backendCache = new HashMap<>();

    /**
     * 获取检测后端
     *
     * @param type 后端类型
     * @return 检测后端实例
     */
    public DetectionBackend getBackend(BackendType type) {
        // 检查缓存
        if (backendCache.containsKey(type)) {
            return backendCache.get(type);
        }

        DetectionBackend backend = createBackend(type);
        backendCache.put(type, backend);
        return backend;
    }

    /**
     * 获取默认后端（自动选择）
     *
     * @return 默认后端
     */
    public DetectionBackend getDefaultBackend() {
        // 如果强制使用模拟模式
        if (yoloProperties.isUseMock()) {
            log.info("使用模拟检测后端（配置强制使用模拟模式）");
            return mockDetectionBackend;
        }

        // 尝试使用Python后端
        if (pythonScriptBackend.isAvailable()) {
            log.info("使用Python脚本检测后端");
            return pythonScriptBackend;
        }

        // 回退到模拟后端
        log.warn("Python后端不可用，回退到模拟后端");
        return mockDetectionBackend;
    }

    /**
     * 创建检测后端
     */
    private DetectionBackend createBackend(BackendType type) {
        return switch (type) {
            case PYTHON_ONNX, PYTHON_ULTRALYTICS -> pythonScriptBackend;
            case MOCK -> mockDetectionBackend;
            case AUTO -> getDefaultBackend();
        };
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        backendCache.clear();
        log.info("检测后端缓存已清除");
    }
}
