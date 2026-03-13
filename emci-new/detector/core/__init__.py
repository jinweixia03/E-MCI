#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""核心检测模块"""

from .detector import ManholeDetector
from .result import DetectionResult, DefectInfo
from .interface import DetectionBackend

__all__ = ['ManholeDetector', 'DetectionResult', 'DefectInfo', 'DetectionBackend']
