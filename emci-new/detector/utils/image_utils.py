#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""图片处理工具函数"""

from pathlib import Path
from typing import Set, Optional, Tuple
import numpy as np
import cv2


# 支持的图片和视频格式
SUPPORTED_IMAGE_FORMATS = {'.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.webp'}
SUPPORTED_VIDEO_FORMATS = {'.mp4', '.avi', '.mov', '.mkv', '.flv', '.wmv'}


def load_image(image_path: str) -> Optional[np.ndarray]:
    """
    加载图片

    Args:
        image_path: 图片路径

    Returns:
        OpenCV格式的图片，加载失败返回None
    """
    image = cv2.imread(image_path)
    return image


def save_image(image: np.ndarray, output_path: str, quality: int = 95) -> bool:
    """
    保存图片

    Args:
        image: OpenCV格式的图片
        output_path: 输出路径
        quality: JPEG质量 (0-100)

    Returns:
        是否保存成功
    """
    try:
        ext = Path(output_path).suffix.lower()

        if ext in ['.jpg', '.jpeg']:
            # JPEG质量设置
            cv2.imwrite(output_path, image, [cv2.IMWRITE_JPEG_QUALITY, quality])
        else:
            cv2.imwrite(output_path, image)
        return True
    except Exception:
        return False


def is_image_file(file_path: str) -> bool:
    """检查文件是否为图片"""
    ext = Path(file_path).suffix.lower()
    return ext in SUPPORTED_IMAGE_FORMATS


def is_video_file(file_path: str) -> bool:
    """检查文件是否为视频"""
    ext = Path(file_path).suffix.lower()
    return ext in SUPPORTED_VIDEO_FORMATS


def get_supported_formats() -> Tuple[Set[str], Set[str]]:
    """
    获取支持的文件格式

    Returns:
        (图片格式集合, 视频格式集合)
    """
    return SUPPORTED_IMAGE_FORMATS.copy(), SUPPORTED_VIDEO_FORMATS.copy()


def resize_image(image: np.ndarray, max_size: int = 1920) -> np.ndarray:
    """
    等比例缩放图片（如果超过最大尺寸）

    Args:
        image: 原始图片
        max_size: 最大边长

    Returns:
        缩放后的图片
    """
    h, w = image.shape[:2]
    max_dim = max(h, w)

    if max_dim <= max_size:
        return image

    scale = max_size / max_dim
    new_w = int(w * scale)
    new_h = int(h * scale)

    return cv2.resize(image, (new_w, new_h), interpolation=cv2.INTER_AREA)
