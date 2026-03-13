/**
 * AI检测模块
 *
 * 提供井盖检测的核心功能，包括：
 * - 检测后端接口（DetectionBackend）
 * - 多种后端实现（Python脚本、模拟）
 * - 命令执行和结果解析
 *
 * 使用示例:
 * <pre>
 * DetectionBackend backend = backendFactory.getDefaultBackend();
 * DetectionResultVO result = backend.detect(imagePath, 0.7, 0.5);
 * </pre>
 */
package com.alian.emci.detection;
