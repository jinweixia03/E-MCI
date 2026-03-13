#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
井盖检测器主类
提供统一的检测接口
"""

import time
import os
from pathlib import Path
from typing import Optional, Union, List
import numpy as np
import cv2

from .result import DetectionResult, DefectInfo
from .interface import DetectionBackend


class ManholeDetector:
    """
    井盖检测器

    使用示例:
        detector = ManholeDetector(backend)
        result = detector.detect_image("path/to/image.jpg")
        print(result.to_json())
    """

    def __init__(self, backend: DetectionBackend):
        """
        初始化检测器

        Args:
            backend: 检测后端实例（ONNXBackend 或 UltralyticsBackend）
        """
        self.backend = backend
        self._ensure_model_loaded()

    def _ensure_model_loaded(self) -> None:
        """确保模型已加载"""
        if not self.backend.is_loaded:
            self.backend.load_model()

    def detect_image(self, image_path: str,
                     save_result: bool = False,
                     output_dir: Optional[str] = None) -> DetectionResult:
        """
        检测单张图片

        Args:
            image_path: 图片路径
            save_result: 是否保存结果图片
            output_dir: 结果图片输出目录

        Returns:
            检测结果
        """
        start_time = time.time()

        try:
            # 读取图片
            image = self._load_image(image_path)
            if image is None:
                return DetectionResult.error_result(
                    f"无法读取图片: {image_path}",
                    original_url=image_path
                )

            # 执行检测
            defects = self.backend.detect(image)

            # 保存结果图片
            result_url = None
            if save_result and output_dir:
                result_url = self._save_result_image(
                    image, defects, image_path, output_dir
                )

            process_time = int((time.time() - start_time) * 1000)

            return DetectionResult.success_result(
                defects=defects,
                original_url=image_path,
                result_url=result_url,
                process_time_ms=process_time
            )

        except Exception as e:
            return DetectionResult.error_result(
                str(e),
                original_url=image_path
            )

    def detect_batch(self, image_paths: List[str],
                     save_results: bool = False,
                     output_dir: Optional[str] = None) -> List[DetectionResult]:
        """
        批量检测图片

        Args:
            image_paths: 图片路径列表
            save_results: 是否保存结果图片
            output_dir: 结果图片输出目录

        Returns:
            检测结果列表
        """
        results = []
        for path in image_paths:
            result = self.detect_image(path, save_results, output_dir)
            results.append(result)
        return results

    def detect_video(self, video_path: str,
                     output_path: Optional[str] = None,
                     frame_interval: int = 5) -> DetectionResult:
        """
        检测视频

        Args:
            video_path: 视频路径
            output_path: 输出视频路径
            frame_interval: 检测间隔帧数

        Returns:
            检测结果（汇总）
        """
        start_time = time.time()

        try:
            cap = cv2.VideoCapture(video_path)
            if not cap.isOpened():
                return DetectionResult.error_result(
                    f"无法打开视频: {video_path}",
                    original_url=video_path
                )

            fps = int(cap.get(cv2.CAP_PROP_FPS))
            width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
            height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

            # 初始化视频写入器
            writer = None
            if output_path:
                fourcc = cv2.VideoWriter_fourcc(*'mp4v')
                writer = cv2.VideoWriter(output_path, fourcc, fps, (width, height))

            all_defects = []
            frame_count = 0
            processed_count = 0

            while True:
                ret, frame = cap.read()
                if not ret:
                    break

                frame_count += 1

                # 按间隔检测
                if frame_count % frame_interval == 0:
                    defects = self.backend.detect(frame)
                    processed_count += 1

                    if defects:
                        all_defects.extend(defects)
                        if writer:
                            annotated = self.backend.draw_results(frame, defects)
                            writer.write(annotated)
                    elif writer:
                        writer.write(frame)
                elif writer:
                    writer.write(frame)

            cap.release()
            if writer:
                writer.release()

            # 统计缺陷类型
            defect_types = {}
            for d in all_defects:
                t = d.type
                defect_types[t] = defect_types.get(t, 0) + 1

            # 去重统计
            unique_defects = [
                DefectInfo(type=k, confidence=1.0, bbox=[0, 0, 0, 0])
                for k in defect_types.keys()
            ]

            process_time = int((time.time() - start_time) * 1000)

            return DetectionResult.success_result(
                defects=unique_defects,
                original_url=video_path,
                result_url=output_path,
                process_time_ms=process_time
            )

        except Exception as e:
            return DetectionResult.error_result(
                str(e),
                original_url=video_path
            )

    def _load_image(self, image_path: str) -> Optional[np.ndarray]:
        """加载图片"""
        return cv2.imread(image_path)

    def _save_result_image(self, image: np.ndarray,
                           defects: List[DefectInfo],
                           original_path: str,
                           output_dir: str) -> str:
        """保存结果图片"""
        os.makedirs(output_dir, exist_ok=True)

        filename = Path(original_path).name
        output_path = os.path.join(output_dir, filename)

        # 绘制检测结果
        annotated = self.backend.draw_results(image, defects)
        cv2.imwrite(output_path, annotated)

        return output_path

    @property
    def conf_threshold(self) -> float:
        """获取置信度阈值"""
        return self.backend.conf_threshold

    @conf_threshold.setter
    def conf_threshold(self, value: float) -> None:
        """设置置信度阈值"""
        self.backend.conf_threshold = value

    @property
    def iou_threshold(self) -> float:
        """获取IOU阈值"""
        return self.backend.iou_threshold

    @iou_threshold.setter
    def iou_threshold(self, value: float) -> None:
        """设置IOU阈值"""
        self.backend.iou_threshold = value
