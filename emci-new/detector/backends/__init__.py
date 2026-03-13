#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""检测后端实现模块"""

from .onnx_backend import ONNXBackend
from .ultralytics_backend import UltralyticsBackend

__all__ = ['ONNXBackend', 'UltralyticsBackend']
