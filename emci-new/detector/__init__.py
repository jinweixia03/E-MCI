#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YOLO 井盖检测模块
提供统一的检测接口和多种后端支持
"""

from .core.detector import ManholeDetector
from .core.result import DetectionResult, DefectInfo
from .backends.onnx_backend import ONNXBackend
from .backends.ultralytics_backend import UltralyticsBackend

__all__ = [
    'ManholeDetector',
    'DetectionResult',
    'DefectInfo',
    'ONNXBackend',
    'UltralyticsBackend',
]

__version__ = '2.0.0'
