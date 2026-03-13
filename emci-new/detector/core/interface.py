#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检测后端接口定义
定义所有检测后端必须实现的接口
"""

from abc import ABC, abstractmethod
from typing import List, Tuple, Any
import numpy as np

from .result import DefectInfo


class DetectionBackend(ABC):
    """检测后端抽象基类"""

    def __init__(self, model_path: str, conf_threshold: float = 0.25, iou_threshold: float = 0.5):
        self.model_path = model_path
        self.conf_threshold = conf_threshold
        self.iou_threshold = iou_threshold
        self._model = None

    @abstractmethod
    def load_model(self) -> None:
        """加载模型"""
        pass

    @abstractmethod
    def detect(self, image: np.ndarray) -> List[DefectInfo]:
        """
        执行检测

        Args:
            image: OpenCV格式的图片 (BGR)

        Returns:
            缺陷信息列表
        """
        pass

    @abstractmethod
    def preprocess(self, image: np.ndarray) -> Tuple[Any, ...]:
        """预处理图片"""
        pass

    @abstractmethod
    def postprocess(self, outputs: Any, original_shape: Tuple[int, int],
                    metadata: Any) -> List[DefectInfo]:
        """后处理检测结果"""
        pass

    @property
    def is_loaded(self) -> bool:
        """检查模型是否已加载"""
        return self._model is not None

    def draw_results(self, image: np.ndarray, defects: List[DefectInfo],
                     color: Tuple[int, int, int] = (0, 255, 0)) -> np.ndarray:
        """
        在图片上绘制检测结果

        Args:
            image: 原始图片
            defects: 缺陷列表
            color: 框的颜色 (B, G, R)

        Returns:
            绘制后的图片
        """
        result = image.copy()

        for defect in defects:
            x, y, w, h = map(int, defect.bbox)
            label = f"{defect.type} {defect.confidence:.2%}"

            # 绘制边界框
            cv2.rectangle(result, (x, y), (x + w, y + h), color, 2)

            # 绘制标签背景
            (text_w, text_h), _ = cv2.getTextSize(
                label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 1
            )
            cv2.rectangle(
                result, (x, y - text_h - 4), (x + text_w, y), color, -1
            )

            # 绘制文字
            cv2.putText(
                result, label, (x, y - 2),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 1
            )

        return result
