package com.alian.emci.detection.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 命令执行结果
 */
@Getter
@RequiredArgsConstructor
public class ExecutionResult {

    private final boolean success;
    private final int exitCode;
    private final String output;
    private final long durationMs;

    /**
     * 创建成功结果
     */
    public static ExecutionResult success(String output, long durationMs) {
        return new ExecutionResult(true, 0, output, durationMs);
    }

    /**
     * 创建失败结果
     */
    public static ExecutionResult error(String message) {
        return new ExecutionResult(false, -1, message, 0);
    }

    /**
     * 创建超时结果
     */
    public static ExecutionResult timeout(int timeoutSec) {
        return new ExecutionResult(false, -2,
                "执行超时 (" + timeoutSec + "s)", timeoutSec * 1000L);
    }

    /**
     * 获取格式化后的错误信息
     */
    public String getFormattedError() {
        if (success) {
            return null;
        }
        if (exitCode == -2) {
            return "检测超时";
        }
        return "检测失败 (exitCode=" + exitCode + "): " + output;
    }
}
