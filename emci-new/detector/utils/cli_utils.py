#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""命令行工具函数"""

import argparse
import logging
import sys
from pathlib import Path
from typing import Optional


def parse_args(description: str = "YOLO检测") -> argparse.Namespace:
    """
    解析命令行参数

    Args:
        description: 程序描述

    Returns:
        解析后的参数
    """
    parser = argparse.ArgumentParser(description=description)

    parser.add_argument(
        '--source', '-s',
        type=str,
        required=True,
        help='输入图片或视频路径'
    )

    parser.add_argument(
        '--weights', '-w',
        type=str,
        default='models/best.onnx',
        help='模型权重路径 (默认: models/best.onnx)'
    )

    parser.add_argument(
        '--conf', '-c',
        type=float,
        default=0.25,
        help='置信度阈值 (默认: 0.25)'
    )

    parser.add_argument(
        '--iou', '-i',
        type=float,
        default=0.5,
        help='IOU阈值 (默认: 0.5)'
    )

    parser.add_argument(
        '--output', '-o',
        type=str,
        default='output',
        help='输出目录 (默认: output)'
    )

    parser.add_argument(
        '--backend', '-b',
        type=str,
        choices=['onnx', 'ultralytics'],
        default='onnx',
        help='检测后端: onnx 或 ultralytics (默认: onnx)'
    )

    parser.add_argument(
        '--no-save',
        action='store_true',
        help='不保存结果图片'
    )

    parser.add_argument(
        '--quiet', '-q',
        action='store_true',
        help='静默模式，只输出JSON结果'
    )

    return parser.parse_args()


def setup_logging(quiet: bool = False, level: Optional[int] = None) -> logging.Logger:
    """
    配置日志

    Args:
        quiet: 是否静默模式
        level: 日志级别

    Returns:
        日志记录器
    """
    if level is None:
        level = logging.WARNING if quiet else logging.INFO

    # 配置根日志记录器
    logging.basicConfig(
        level=level,
        format='%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%H:%M:%S',
        stream=sys.stderr
    )

    return logging.getLogger('detector')


def validate_paths(source: str, weights: str) -> tuple[bool, Optional[str]]:
    """
    验证输入路径

    Args:
        source: 输入文件路径
        weights: 模型权重路径

    Returns:
        (是否有效, 错误信息)
    """
    # 检查输入文件
    if not Path(source).exists():
        return False, f"输入文件不存在: {source}"

    # 检查模型文件
    if not Path(weights).exists():
        # 尝试其他路径
        alt_paths = ['best.onnx', 'best.pt', 'models/best.onnx', 'models/best.pt']
        for alt in alt_paths:
            if Path(alt).exists():
                return True, None
        return False, f"模型文件不存在: {weights}"

    return True, None
