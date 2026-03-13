#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YOLOv8 井盖检测脚本 - 重构版
统一的检测入口，支持多种后端

用法:
    python detect.py --source image.jpg
    python detect.py --source image.jpg --backend onnx
    python detect.py --source image.jpg --backend ultralytics --weights best.pt
"""

import json
import sys
import warnings
from pathlib import Path

# 忽略警告
warnings.filterwarnings('ignore')

# 添加项目根目录到路径
project_root = Path(__file__).parent
sys.path.insert(0, str(project_root))

from detector import ManholeDetector, ONNXBackend, UltralyticsBackend
from detector.utils import parse_args, setup_logging, validate_paths, is_image_file, is_video_file


def create_backend(args) -> ONNXBackend | UltralyticsBackend:
    """根据参数创建检测后端"""
    weights = args.weights

    # 如果指定的权重不存在，尝试查找
    if not Path(weights).exists():
        alt_paths = ['best.onnx', 'best.pt', 'models/best.onnx', 'models/best.pt']
        for alt in alt_paths:
            if Path(alt).exists():
                weights = alt
                break

    # 根据文件后缀和参数选择后端
    if args.backend == 'ultralytics' or weights.endswith('.pt'):
        return UltralyticsBackend(
            model_path=weights,
            conf_threshold=args.conf,
            iou_threshold=args.iou
        )
    else:
        return ONNXBackend(
            model_path=weights,
            conf_threshold=args.conf,
            iou_threshold=args.iou
        )


def main():
    args = parse_args("YOLOv8 井盖检测 - 支持ONNX和Ultralytics后端")
    logger = setup_logging(args.quiet)

    # 验证路径
    valid, error = validate_paths(args.source, args.weights)
    if not valid:
        result = {
            "success": False,
            "error": error,
            "message": "请检查路径是否正确"
        }
        print(json.dumps(result, ensure_ascii=False))
        sys.exit(1)

    try:
        # 创建后端
        backend = create_backend(args)
        detector = ManholeDetector(backend)

        # 执行检测
        if is_image_file(args.source):
            result = detector.detect_image(
                image_path=args.source,
                save_result=not args.no_save,
                output_dir=args.output
            )
        elif is_video_file(args.source):
            output_path = Path(args.output) / Path(args.source).name
            result = detector.detect_video(
                video_path=args.source,
                output_path=str(output_path) if not args.no_save else None
            )
        else:
            result = {
                "success": False,
                "error": f"不支持的文件格式: {Path(args.source).suffix}",
                "supported_formats": "图片: jpg, jpeg, png, bmp | 视频: mp4, avi, mov"
            }
            print(json.dumps(result, ensure_ascii=False))
            sys.exit(1)

        # 输出JSON结果
        print(json.dumps(result.to_dict(), ensure_ascii=False))

    except ImportError as e:
        error_result = {
            "success": False,
            "error": f"缺少依赖包: {e}",
            "message": "请安装: pip install onnxruntime opencv-python numpy"
        }
        print(json.dumps(error_result, ensure_ascii=False))
        sys.exit(1)

    except Exception as e:
        error_result = {
            "success": False,
            "error": str(e)
        }
        print(json.dumps(error_result, ensure_ascii=False))
        sys.exit(1)


if __name__ == '__main__':
    main()
