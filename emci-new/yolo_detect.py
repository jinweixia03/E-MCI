#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YOLOv8 井盖检测脚本 - Java后端调用版
输出JSON格式结果便于Java解析
"""

import argparse
import json
import os
import sys
import warnings
from pathlib import Path
from typing import List, Dict, Any

# 忽略警告
warnings.filterwarnings('ignore')

try:
    from ultralytics import YOLO
    import cv2
    import numpy as np
except ImportError as e:
    print(json.dumps({
        "success": False,
        "error": f"缺少必要的依赖包: {e}",
        "message": "请安装: pip install ultralytics opencv-python numpy"
    }, ensure_ascii=False))
    sys.exit(1)


# 缺陷类型映射（与训练时的类别顺序一致）
# 模型类别: {0: 'manhole', 1: 'damage', 2: 'open', 3: 'missing'}
CLASS_NAMES = {
    0: "井盖",
    1: "损坏",
    2: "开启",
    3: "缺失"
}


def detect_image(model, image_path: str, conf_threshold: float, iou_threshold: float, output_dir: str) -> Dict[str, Any]:
    """
    检测单张图片

    Returns:
        {
            "success": True/False,
            "original_url": "原始图片路径",
            "result_url": "结果图片路径",
            "defect_count": 缺陷数量,
            "defects": [
                {
                    "type": "缺陷类型",
                    "confidence": 置信度,
                    "bbox": [x, y, width, height]
                }
            ],
            "process_time_ms": 处理时间毫秒
        }
    """
    import time
    start_time = time.time()

    try:
        # 执行检测
        results = model(image_path, conf=conf_threshold, iou=iou_threshold, verbose=False)
        result = results[0]
        boxes = result.boxes

        # 解析检测结果
        defects = []
        if boxes is not None and len(boxes) > 0:
            for box in boxes:
                cls_id = int(box.cls.item())
                conf = float(box.conf.item())
                xyxy = box.xyxy[0].cpu().numpy().tolist()  # [x1, y1, x2, y2]

                # 转换为 [x, y, width, height]
                x, y = xyxy[0], xyxy[1]
                width = xyxy[2] - xyxy[0]
                height = xyxy[3] - xyxy[1]

                class_name = CLASS_NAMES.get(cls_id, f"类别{cls_id}")

                defects.append({
                    "type": class_name,
                    "confidence": round(conf, 4),
                    "bbox": [round(x, 2), round(y, 2), round(width, 2), round(height, 2)],
                    "class_id": cls_id
                })

        # 保存标注后的图片
        output_path = None
        if output_dir:
            os.makedirs(output_dir, exist_ok=True)
            filename = Path(image_path).name
            output_path = os.path.join(output_dir, filename)

            # 绘制检测框
            annotated_frame = result.plot()
            cv2.imwrite(output_path, annotated_frame)

        process_time = int((time.time() - start_time) * 1000)

        # 构建JSON结果
        detection_result = {
            "success": True,
            "original_url": image_path,
            "result_url": output_path if output_path else image_path,
            "defect_count": len(defects),
            "defects": defects,
            "process_time_ms": process_time
        }

        return detection_result

    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "original_url": image_path,
            "defect_count": 0,
            "defects": [],
            "process_time_ms": 0
        }


def detect_video(model, video_path: str, conf_threshold: float, iou_threshold: float, output_dir: str) -> Dict[str, Any]:
    """检测视频"""
    import time
    start_time = time.time()

    try:
        cap = cv2.VideoCapture(video_path)
        fps = int(cap.get(cv2.CAP_PROP_FPS))
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        os.makedirs(output_dir, exist_ok=True)
        filename = Path(video_path).name
        output_path = os.path.join(output_dir, filename)

        fourcc = cv2.VideoWriter_fourcc(*'mp4v')
        out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))

        all_defects = []
        frame_count = 0

        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break

            frame_count += 1

            # 每5帧检测一次
            if frame_count % 5 == 0:
                results = model(frame, conf=conf_threshold, iou=iou_threshold, verbose=False)
                result = results[0]

                if result.boxes is not None and len(result.boxes) > 0:
                    annotated_frame = result.plot()
                    out.write(annotated_frame)

                    for box in result.boxes:
                        cls_id = int(box.cls.item())
                        conf = float(box.conf.item())
                        class_name = CLASS_NAMES.get(cls_id, f"类别{cls_id}")

                        all_defects.append({
                            "type": class_name,
                            "confidence": round(conf, 4),
                            "frame": frame_count
                        })
                else:
                    out.write(frame)
            else:
                out.write(frame)

        cap.release()
        out.release()

        # 统计缺陷
        defect_types = {}
        for d in all_defects:
            t = d["type"]
            defect_types[t] = defect_types.get(t, 0) + 1

        unique_defects = [{"type": k, "count": v} for k, v in defect_types.items()]

        process_time = int((time.time() - start_time) * 1000)

        return {
            "success": True,
            "original_url": video_path,
            "result_url": output_path,
            "defect_count": len(unique_defects),
            "defects": unique_defects[:10],
            "total_frames": frame_count,
            "process_time_ms": process_time
        }

    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "original_url": video_path,
            "defect_count": 0,
            "defects": []
        }


def main():
    parser = argparse.ArgumentParser(description='YOLOv8 井盖检测')
    parser.add_argument('--source', type=str, required=True, help='输入图片或视频路径')
    parser.add_argument('--weights', type=str, default='models/best.pt', help='模型权重路径')
    parser.add_argument('--conf', type=float, default=0.7, help='置信度阈值')
    parser.add_argument('--iou', type=float, default=0.5, help='IOU阈值')
    parser.add_argument('--output', type=str, default='output', help='输出目录')
    parser.add_argument('--save-txt', action='store_true', help='保存结果为txt')
    parser.add_argument('--save-conf', action='store_true', help='保存置信度')

    args = parser.parse_args()

    # 检查输入文件
    if not os.path.exists(args.source):
        print(json.dumps({
            "success": False,
            "error": f"输入文件不存在: {args.source}"
        }, ensure_ascii=False))
        sys.exit(1)

    # 检查模型文件
    if not os.path.exists(args.weights):
        alt_paths = ['best.pt', 'yolov8s.pt', 'yolov8n.pt', 'yolov8m.pt']
        found = False
        for alt in alt_paths:
            if os.path.exists(alt):
                args.weights = alt
                found = True
                break

        if not found:
            print(json.dumps({
                "success": False,
                "error": f"模型文件不存在: {args.weights}"
            }, ensure_ascii=False))
            sys.exit(1)

    # 加载模型
    try:
        model = YOLO(args.weights)
    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": f"加载模型失败: {e}"
        }, ensure_ascii=False))
        sys.exit(1)

    # 判断是图片还是视频
    ext = Path(args.source).suffix.lower()
    image_exts = ['.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.webp']
    video_exts = ['.mp4', '.avi', '.mov', '.mkv', '.flv', '.wmv']

    try:
        if ext in image_exts:
            result = detect_image(model, args.source, args.conf, args.iou, args.output)
        elif ext in video_exts:
            result = detect_video(model, args.source, args.conf, args.iou, args.output)
        else:
            print(json.dumps({
                "success": False,
                "error": f"不支持的文件格式: {ext}"
            }, ensure_ascii=False))
            sys.exit(1)

        # 输出JSON结果（Java后端会读取这个）
        print(json.dumps(result, ensure_ascii=False))

    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": f"检测失败: {e}"
        }, ensure_ascii=False))
        sys.exit(1)


if __name__ == '__main__':
    main()
