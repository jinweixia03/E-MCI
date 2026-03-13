package com.alian.emci.service;

import com.alian.emci.vo.detection.DetectionResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * YOLO检测服务接口
 */
public interface YoloDetectionService {

    /**
     * 检测单张图片
     *
     * @param imageFile     图片文件
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果
     */
    DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold) throws IOException;

    /**
     * 检测单张图片（指定井盖ID，用于保存结果）
     *
     * @param imageFile     图片文件
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @param manholeId     井盖ID（用于命名保存的检测图片）
     * @return 检测结果
     */
    DetectionResultVO detectImage(MultipartFile imageFile, Double confThreshold, Double iouThreshold, String manholeId) throws IOException;

    /**
     * 检测本地图片文件
     *
     * @param imagePath     图片路径
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果
     */
    DetectionResultVO detectImageFile(String imagePath, Double confThreshold, Double iouThreshold);

    /**
     * 批量检测图片
     *
     * @param imageFiles    图片文件列表
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果列表
     */
    List<DetectionResultVO> detectBatch(List<MultipartFile> imageFiles, Double confThreshold, Double iouThreshold);

    /**
     * 检测视频
     *
     * @param videoFile     视频文件
     * @param confThreshold 置信度阈值
     * @param iouThreshold  IOU阈值
     * @return 检测结果（包含输出视频路径）
     */
    DetectionResultVO detectVideo(MultipartFile videoFile, Double confThreshold, Double iouThreshold) throws IOException;

    /**
     * 获取模型信息
     *
     * @return 模型信息
     */
    String getModelInfo();

    /**
     * 检查模型是否可用
     *
     * @return 是否可用
     */
    boolean isModelAvailable();
}
