#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ONNX Runtime 检测后端
无需conda，仅需 pip install onnxruntime opencv-python numpy
"""

import sys
from typing import List, Tuple, Any
import numpy as np
import cv2

from ..core.interface import DetectionBackend
from ..core.result import DefectInfo
from .base import ClassNames


class ONNXBackend(DetectionBackend):
    """
    YOLOv8 ONNX Runtime 检测器

    支持GPU加速（如果可用），否则自动回退到CPU
    """

    # 默认输入尺寸
    DEFAULT_INPUT_SIZE = (640, 640)
    # 填充颜色（灰色）
    PAD_COLOR = 114

    def __init__(self, model_path: str, conf_threshold: float = 0.25, iou_threshold: float = 0.5):
        super().__init__(model_path, conf_threshold, iou_threshold)
        self.input_shape = self.DEFAULT_INPUT_SIZE
        self._providers = None
        self._input_name = None
        self._output_name = None
        self._num_classes = 4

    def load_model(self) -> None:
        """加载ONNX模型"""
        try:
            import onnxruntime as ort
        except ImportError:
            raise ImportError("请先安装 onnxruntime: pip install onnxruntime")

        # 创建推理会话，自动选择可用Provider
        self._model = self._create_session(ort)

        # 获取模型输入输出信息
        self._input_name = self._model.get_inputs()[0].name
        self._output_name = self._model.get_outputs()[0].name

        # 解析输入形状
        self._parse_input_shape(self._model)

    def _create_session(self, ort) -> Any:
        """创建ONNX推理会话"""
        # 按优先级尝试Provider
        providers = ['CUDAExecutionProvider', 'CPUExecutionProvider']
        available_providers = ort.get_available_providers()

        # 过滤实际可用的providers
        effective_providers = [p for p in providers if p in available_providers]

        try:
            session = ort.InferenceSession(
                self.model_path,
                providers=effective_providers
            )
            self._providers = session.get_providers()

            # 记录使用的provider
            provider = self._providers[0] if self._providers else "Unknown"
            self._log(f"使用 {provider} 进行推理")

            return session
        except Exception as e:
            self._log(f"创建会话失败，回退到CPU: {e}")
            return ort.InferenceSession(
                self.model_path,
                providers=['CPUExecutionProvider']
            )

    def _parse_input_shape(self, session) -> None:
        """解析模型输入形状"""
        input_shape = session.get_inputs()[0].shape

        if len(input_shape) >= 4:
            try:
                h = int(input_shape[2]) if input_shape[2] is not None else 640
                w = int(input_shape[3]) if input_shape[3] is not None else 640
                self.input_shape = (h, w)
            except (ValueError, TypeError):
                self.input_shape = self.DEFAULT_INPUT_SIZE

    def _log(self, message: str) -> None:
        """输出日志到stderr"""
        print(message, file=sys.stderr)

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

        orig_shape = image.shape[:2]

        # 预处理
        input_tensor, scale, pad = self.preprocess(image)

        # 推理
        outputs = self._model.run(
            [self._output_name],
            {self._input_name: input_tensor}
        )[0]

        # 后处理
        return self.postprocess(outputs, orig_shape, (scale, pad))

    def preprocess(self, image: np.ndarray) -> Tuple[np.ndarray, Tuple[float, float], Tuple[float, float]]:
        """
        预处理图片

        Args:
            image: 原始图片 (H, W, C)

        Returns:
            (input_tensor, scale, padding)
        """
        orig_h, orig_w = image.shape[:2]
        input_h, input_w = self.input_shape

        # 计算缩放比例（保持长宽比）
        scale = min(input_w / orig_w, input_h / orig_h)
        new_w = int(orig_w * scale)
        new_h = int(orig_h * scale)

        # 缩放图片
        resized = cv2.resize(image, (new_w, new_h), interpolation=cv2.INTER_LINEAR)

        # 创建填充画布
        padded = np.full((input_h, input_w, 3), self.PAD_COLOR, dtype=np.uint8)

        # 将缩放后的图片放在画布中心
        dw = (input_w - new_w) // 2
        dh = (input_h - new_h) // 2
        padded[dh:dh+new_h, dw:dw+new_w] = resized

        # 归一化 (0-255 -> 0-1)
        normalized = padded.astype(np.float32) / 255.0

        # HWC -> CHW
        transposed = normalized.transpose(2, 0, 1)

        # 添加batch维度
        input_tensor = np.expand_dims(transposed, axis=0)

        return input_tensor, (scale, scale), (dw, dh)

    def postprocess(self, outputs: np.ndarray, orig_shape: Tuple[int, int],
                    metadata: Tuple[Tuple[float, float], Tuple[float, float]]) -> List[DefectInfo]:
        """
        后处理检测结果

        ONNX导出格式: (batch, 300, 6) -> [x1, y1, x2, y2, conf, class_id]
        已经包含NMS，直接解析即可

        Args:
            outputs: 模型输出
            orig_shape: 原始图片尺寸 (H, W)
            metadata: (scale, padding)

        Returns:
            缺陷信息列表
        """
        orig_h, orig_w = orig_shape
        (scale_x, scale_y), (pad_x, pad_y) = metadata
        input_h, input_w = self.input_shape

        results = []
        predictions = outputs[0]  # (300, 6)

        for pred in predictions:
            x1, y1, x2, y2, conf, cls = pred

            # 过滤低置信度和无效框
            if conf < self.conf_threshold or (x1 == 0 and x2 == 0):
                continue

            class_id = int(cls)

            # 去除填充并缩放到原始尺寸
            x1 = max(0, min((x1 - pad_x) / scale_x, orig_w))
            y1 = max(0, min((y1 - pad_y) / scale_y, orig_h))
            x2 = max(0, min((x2 - pad_x) / scale_x, orig_w))
            y2 = max(0, min((y2 - pad_y) / scale_y, orig_h))

            width = x2 - x1
            height = y2 - y1

            # 过滤无效尺寸
            if width <= 0 or height <= 0:
                continue

            results.append(DefectInfo(
                type=ClassNames.get_name(class_id),
                confidence=float(conf),
                bbox=[float(x1), float(y1), float(width), float(height)],
                class_id=class_id
            ))

        return results
