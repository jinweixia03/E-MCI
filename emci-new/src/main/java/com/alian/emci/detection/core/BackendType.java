package com.alian.emci.detection.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 检测后端类型
 */
@Getter
@RequiredArgsConstructor
public enum BackendType {

    /**
     * Python脚本后端（ONNX）
     */
    PYTHON_ONNX("python-onnx", "Python ONNX Runtime"),

    /**
     * Python脚本后端（Ultralytics）
     */
    PYTHON_ULTRALYTICS("python-ultralytics", "Python Ultralytics"),

    /**
     * 模拟后端（用于测试）
     */
    MOCK("mock", "模拟检测"),

    /**
     * 自动选择
     */
    AUTO("auto", "自动选择");

    private final String code;
    private final String description;

    /**
     * 根据代码获取类型
     */
    public static BackendType fromCode(String code) {
        for (BackendType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return AUTO;
    }
}
