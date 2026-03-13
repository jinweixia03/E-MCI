#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Ultralytics YOLO 检测后端
使用 ultralytics 库进行推理
需要: pip install ultralytics opencv-python numpy
"""

from typing import List, Tuple, Any
import numpy as np

from ..core.interface import DetectionBackend
from ..core.result import DefectInfo
from .base import ClassNames


class UltralyticsBackend(DetectionBackend):
    """
    Ultralytics YOLOv8 检测器

    支持原生YOLOv8模型（.pt格式）
    提供最佳性能和易用性，但需要安装ultralytics
    """

    def __init__(self, model_path: str, conf_threshold: float = 0.25, iou_threshold: float = 0.5):
        super().__init__(model_path, conf_threshold, iou_threshold)
        self._device = None

    def load_model(self) -> None:
        """加载YOLO模型"""
        try:
            from ultralytics import YOLO
        except ImportError:
            raise ImportError("请先安装 ultralytics: pip install ultralytics")

        self._model = YOLO(self.model_path)
        self._device = self._model.device

    def detect(self, image: np.ndarray) -> List[DefectInfo]:
        """
        执行检测

        Args:
            image: OpenCV格式的图片 (BGR)

        Returns:
            缺陷信息列表
        """
        if not self.is_loaded:
            self.load_model()

        # 执行推理
        results = self._model(
            image,
            conf=self.conf_threshold,
            iou=self.iou_threshold,
            verbose=False
        )

        return self._parse_results(results[0])

    def _parse_results(self, result) -> List[DefectInfo]:
        """解析YOLOv8结果"""
        defects = []
        boxes = result.boxes

        if boxes is None or len(boxes) == 0:
            return defects

        for box in boxes:
            cls_id = int(box.cls.item())
            conf = float(box.conf.item())
            xyxy = box.xyxy[0].cpu().numpy().tolist()

            # 转换为 [x, y, width, height]
            x, y = xyxy[0], xyxy[1]
            width = xyxy[2] - xyxy[0]
            height = xyxy[3] - xyxy[1]

            defects.append(DefectInfo(
                type=ClassNames.get_name(cls_id),
                confidence=conf,
                bbox=[x, y, width, height],
                class_id=cls_id
            ))

        return defects

    def preprocess(self, image: np.ndarray) -> Tuple[Any, ...]:
        """
        预处理图片

        Ultralytics会自动处理预处理，此方法仅用于兼容性
        """
        # Ultralytics内部处理预处理
        return (image,)

    def postprocess(self, outputs: Any, orig_shape: Tuple[int, int],
                    metadata: Any) -> List[DefectInfo]:
        """
        后处理检测结果

        Ultralytics已经返回处理后的结果
        """
        # 结果已经在detect方法中解析
        return outputs if isinstance(outputs, list) else []

    @property
    def device(self) -> str:
        """获取推理设备"""
        return str(self._device) if self._device else "unknown"
