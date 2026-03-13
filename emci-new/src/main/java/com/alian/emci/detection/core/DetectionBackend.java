package com.alian.emci.detection.core;

import com.alian.emci.vo.detection.DetectionResultVO;

import java.io.IOException;
import java.util.List;

/**
 * 检测后端接口
 * 定义所有检测后端必须实现的功能
 */
public interface DetectionBackend {

    /**
     * 检测本地图片文件
     *
     * @param imagePath     图片路径
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果
     */
    DetectionResultVO detect(String imagePath, double confThreshold, double iouThreshold);

    /**
     * 批量检测
     *
     * @param imagePaths    图片路径列表
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果列表
     */
    List<DetectionResultVO> detectBatch(List<String> imagePaths, double confThreshold, double iouThreshold);

    /**
     * 检测视频
     *
     * @param videoPath     视频路径
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果
     */
    DetectionResultVO detectVideo(String videoPath, double confThreshold, double iouThreshold);

    /**
     * 检查后端是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取后端信息
     *
     * @return 后端信息描述
     */
    String getBackendInfo();

    /**
     * 获取后端名称
     *
     * @return 后端名称
     */
    String getName();
}
