#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
YOLOv8 井盖检测脚本 - ONNX Runtime版本
无需conda，仅需 pip install onnxruntime opencv-python numpy
输出JSON格式结果便于Java解析
"""

import argparse
import json
import os
import sys
import time
import warnings
from pathlib import Path
from typing import List, Dict, Any, Tuple

import numpy as np
import cv2

warnings.filterwarnings('ignore')

# 缺陷类型映射（与训练时的类别顺序一致）
# 模型类别: {0: 'manhole', 1: 'damage', 2: 'open', 3: 'missing'}
CLASS_NAMES = {
    0: "Manhole",
    1: "Damage",
    2: "Open",
    3: "Missing"
}


class YOLOv8ONNX:
    """YOLOv8 ONNX Runtime 检测器"""

    def __init__(self, model_path: str, conf_threshold: float = 0.25, iou_threshold: float = 0.5):
        self.conf_threshold = conf_threshold
        self.iou_threshold = iou_threshold
        self.input_shape = (640, 640)  # 默认输入尺寸

        # 导入onnxruntime
        try:
            import onnxruntime as ort
        except ImportError:
            raise ImportError("请先安装 onnxruntime: pip install onnxruntime")

        # 创建推理会话
        # 尝试使用GPU，否则使用CPU
        try:
            self.session = ort.InferenceSession(model_path, providers=['CUDAExecutionProvider', 'CPUExecutionProvider'])
            # 检查是否真正使用了GPU
            if 'CUDAExecutionProvider' in self.session.get_providers():
                print(f"使用GPU加速推理", file=sys.stderr)
            else:
                print(f"使用CPU推理", file=sys.stderr)
        except Exception as e:
            self.session = ort.InferenceSession(model_path, providers=['CPUExecutionProvider'])
            print(f"使用CPU推理", file=sys.stderr)

        # 获取模型输入输出信息
        self.input_name = self.session.get_inputs()[0].name
        self.output_name = self.session.get_outputs()[0].name
        input_shape = self.session.get_inputs()[0].shape
        # 处理动态维度 (None, 字符串等) -> 使用默认值640
        self.input_shape = (640, 640)
        if len(input_shape) >= 4:
            try:
                h = int(input_shape[2]) if input_shape[2] is not None else 640
                w = int(input_shape[3]) if input_shape[3] is not None else 640
                self.input_shape = (h, w)
            except (ValueError, TypeError):
                # 动态维度如 'height', 'width'，使用默认值
                self.input_shape = (640, 640)

        # 类别数量固定为4 (井盖, 损坏, 开启, 缺失)
        self.num_classes = 4

    def preprocess(self, image: np.ndarray) -> Tuple[np.ndarray, Tuple[float, float], Tuple[float, float]]:
        """预处理图片"""
        # 获取原始尺寸
        orig_h, orig_w = image.shape[:2]

        # 计算缩放比例（保持长宽比）
        input_h, input_w = self.input_shape
        scale = min(input_w / orig_w, input_h / orig_h)
        new_w = int(orig_w * scale)
        new_h = int(orig_h * scale)

        # 缩放图片
        resized = cv2.resize(image, (new_w, new_h), interpolation=cv2.INTER_LINEAR)

        # 创建填充画布
        padded = np.full((input_h, input_w, 3), 114, dtype=np.uint8)

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
                   scale: Tuple[float, float], pad: Tuple[float, float]) -> List[Dict[str, Any]]:
        """后处理检测结果
        ONNX导出格式: (batch, 300, 6) -> [x1, y1, x2, y2, conf, class_id]
        已经包含NMS，直接解析即可
        """
        orig_h, orig_w = orig_shape
        scale_x, scale_y = scale
        pad_x, pad_y = pad
        input_h, input_w = self.input_shape

        results = []
        predictions = outputs[0]  # (300, 6)

        for pred in predictions:
            x1, y1, x2, y2, conf, cls = pred

            # 过滤低置信度和无效框 (x1==0且x2==0表示无效)
            if conf < self.conf_threshold or (x1 == 0 and x2 == 0):
                continue

            class_id = int(cls)

            # 去除填充并缩放到原始尺寸
            x1 = (x1 - pad_x) / scale_x
            y1 = (y1 - pad_y) / scale_y
            x2 = (x2 - pad_x) / scale_x
            y2 = (y2 - pad_y) / scale_y

            # 限制在图片范围内
            x1 = max(0, min(x1, orig_w))
            y1 = max(0, min(y1, orig_h))
            x2 = max(0, min(x2, orig_w))
            y2 = max(0, min(y2, orig_h))

            width = x2 - x1
            height = y2 - y1

            # 过滤无效尺寸
            if width <= 0 or height <= 0:
                continue

            class_name = CLASS_NAMES.get(class_id, f"类别{class_id}")

            results.append({
                "type": class_name,
                "confidence": round(float(conf), 4),
                "bbox": [round(float(x1), 2), round(float(y1), 2), round(float(width), 2), round(float(height), 2)],
                "class_id": class_id
            })

        return results

    def detect(self, image_path: str) -> Tuple[List[Dict[str, Any]], np.ndarray]:
        """检测单张图片"""
        # 读取图片
        image = cv2.imread(image_path)
        if image is None:
            raise ValueError(f"无法读取图片: {image_path}")

        orig_shape = image.shape[:2]

        # 预处理
        input_tensor, scale, pad = self.preprocess(image)

        # 推理
        outputs = self.session.run([self.output_name], {self.input_name: input_tensor})[0]

        # 后处理
        results = self.postprocess(outputs, orig_shape, scale, pad)

        return results, image

    def draw_results(self, image: np.ndarray, results: List[Dict[str, Any]]) -> np.ndarray:
        """绘制检测结果"""
        for det in results:
            x, y, w, h = det["bbox"]
            x, y, w, h = int(x), int(y), int(w), int(h)
            conf = det["confidence"]
            label = f"{det['type']} {conf:.2%}"

            # 绘制框
            color = (0, 255, 0)  # 绿色
            cv2.rectangle(image, (x, y), (x + w, y + h), color, 2)

            # 绘制标签背景
            (text_w, text_h), _ = cv2.getTextSize(label, cv2.FONT_HERSHEY_SIMPLEX, 0.6, 1)
            cv2.rectangle(image, (x, y - text_h - 4), (x + text_w, y), color, -1)

            # 绘制文字
            cv2.putText(image, label, (x, y - 2), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 1)

        return image


def detect_image(detector: YOLOv8ONNX, image_path: str, output_dir: str) -> Dict[str, Any]:
    """检测单张图片"""
    start_time = time.time()

    try:
        # 执行检测
        defects, image = detector.detect(image_path)

        # 保存标注后的图片
        output_path = None
        if output_dir:
            os.makedirs(output_dir, exist_ok=True)
            filename = Path(image_path).name
            output_path = os.path.join(output_dir, filename)

            # 绘制检测框
            annotated = detector.draw_results(image.copy(), defects)
            cv2.imwrite(output_path, annotated)

        process_time = int((time.time() - start_time) * 1000)

        return {
            "success": True,
            "original_url": image_path,
            "result_url": output_path if output_path else image_path,
            "defect_count": len(defects),
            "defects": defects,
            "process_time_ms": process_time
        }

    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "original_url": image_path,
            "defect_count": 0,
            "defects": [],
            "process_time_ms": 0
        }


def main():
    parser = argparse.ArgumentParser(description='YOLOv8 ONNX Runtime 井盖检测')
    parser.add_argument('--source', type=str, required=True, help='输入图片路径')
    parser.add_argument('--weights', type=str, default='models/best.onnx', help='ONNX模型路径')
    parser.add_argument('--conf', type=float, default=0.25, help='置信度阈值')
    parser.add_argument('--iou', type=float, default=0.5, help='IOU阈值')
    parser.add_argument('--output', type=str, default='output', help='输出目录')

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
        # 尝试其他路径
        alt_paths = ['best.onnx', 'models/best.onnx', '../models/best.onnx']
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
        detector = YOLOv8ONNX(args.weights, args.conf, args.iou)
    except ImportError as e:
        print(json.dumps({
            "success": False,
            "error": f"缺少依赖包: {e}",
            "message": "请安装: pip install onnxruntime opencv-python numpy"
        }, ensure_ascii=False))
        sys.exit(1)
    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": f"加载模型失败: {e}"
        }, ensure_ascii=False))
        sys.exit(1)

    # 执行检测
    try:
        result = detect_image(detector, args.source, args.output)
        print(json.dumps(result, ensure_ascii=False))
    except Exception as e:
        print(json.dumps({
            "success": False,
            "error": f"检测失败: {e}"
        }, ensure_ascii=False))
        sys.exit(1)


if __name__ == '__main__':
    main()
