#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ONNX Runtime 环境验证脚本
无需conda，仅需 pip install -r requirements_onnx.txt
"""

import os
import sys


def print_section(title):
    print(f"\n{'='*50}")
    print(f"  {title}")
    print(f"{'='*50}")


def check_python_version():
    """检查Python版本"""
    print_section("Python版本检查")
    version = sys.version_info
    print(f"Python版本: {version.major}.{version.minor}.{version.micro}")
    if version.major >= 3 and version.minor >= 8:
        print("✓ Python版本符合要求 (>=3.8)")
        return True
    else:
        print("✗ Python版本过低，需要 >=3.8")
        return False


def check_packages():
    """检查Python包"""
    print_section("依赖包检查")

    checks = {
        'onnxruntime': None,
        'cv2': None,
        'numpy': None,
    }

    try:
        import onnxruntime as ort
        print(f"✓ onnxruntime: {ort.__version__}")
        print(f"  可用Providers: {ort.get_available_providers()}")
        checks['onnxruntime'] = True
    except ImportError:
        print("✗ onnxruntime 未安装")
        print("  安装命令: pip install onnxruntime")
        checks['onnxruntime'] = False

    try:
        import cv2
        print(f"✓ opencv-python: {cv2.__version__}")
        checks['cv2'] = True
    except ImportError:
        print("✗ opencv-python 未安装")
        print("  安装命令: pip install opencv-python")
        checks['cv2'] = False

    try:
        import numpy as np
        print(f"✓ numpy: {np.__version__}")
        checks['numpy'] = True
    except ImportError:
        print("✗ numpy 未安装")
        print("  安装命令: pip install numpy")
        checks['numpy'] = False

    return all(checks.values())


def check_model():
    """检查模型文件"""
    print_section("ONNX模型文件检查")

    model_paths = [
        'models/best.onnx',
        'best.onnx',
        '../models/best.onnx',
    ]

    for path in model_paths:
        if os.path.exists(path):
            size_mb = os.path.getsize(path) / (1024 * 1024)
            print(f"✓ ONNX模型文件存在: {path}")
            print(f"  大小: {size_mb:.2f} MB")
            return path

    print("✗ 未找到 models/best.onnx 模型文件")
    print("  请运行: yolo export model=best.pt format=onnx")
    return None


def test_onnx_inference(model_path):
    """测试ONNX推理"""
    print_section("ONNX推理测试")

    try:
        import onnxruntime as ort
        import numpy as np

        # 创建会话
        session = ort.InferenceSession(model_path, providers=['CPUExecutionProvider'])
        print(f"✓ 模型加载成功")
        print(f"  输入: {session.get_inputs()[0].name} {session.get_inputs()[0].shape}")
        print(f"  输出: {session.get_outputs()[0].name} {session.get_outputs()[0].shape}")

        # 测试推理
        input_shape = (1, 3, 640, 640)
        x = np.random.randn(*input_shape).astype(np.float32)
        outputs = session.run(None, {session.get_inputs()[0].name: x})

        print(f"✓ 推理测试成功")
        print(f"  输出形状: {outputs[0].shape}")
        return True
    except Exception as e:
        print(f"✗ 推理测试失败: {e}")
        return False


def test_detection_script():
    """测试检测脚本"""
    print_section("检测脚本测试")

    script_path = 'yolo_detect_onnx.py'
    if not os.path.exists(script_path):
        print(f"✗ 检测脚本不存在: {script_path}")
        return False

    print(f"✓ 检测脚本存在: {script_path}")

    # 创建测试图片
    try:
        import cv2
        import numpy as np

        img = np.ones((480, 640, 3), dtype=np.uint8) * 200
        cv2.circle(img, (320, 240), 100, (50, 50, 50), -1)
        cv2.circle(img, (320, 240), 100, (30, 30, 30), 3)
        cv2.imwrite('test_onnx.jpg', img)

        # 运行检测
        import subprocess
        result = subprocess.run(
            ['python', script_path, '--source', 'test_onnx.jpg', '--conf', '0.25', '--output', 'output'],
            capture_output=True, text=True, timeout=30
        )

        # 清理
        if os.path.exists('test_onnx.jpg'):
            os.remove('test_onnx.jpg')

        if result.returncode == 0:
            print("✓ 检测脚本执行成功")
            # 解析输出
            try:
                import json
                output = json.loads(result.stdout.strip().split('\n')[-1])
                print(f"  检测结果: success={output.get('success')}, defects={output.get('defect_count', 0)}")
            except:
                pass
            return True
        else:
            print(f"✗ 检测脚本执行失败: {result.stderr}")
            return False
    except Exception as e:
        print(f"✗ 检测测试失败: {e}")
        return False


def main():
    print("=" * 50)
    print("ONNX Runtime 环境验证")
    print("=" * 50)

    checks = {
        'Python版本': check_python_version(),
        '依赖包': check_packages(),
    }

    model_path = check_model()
    checks['模型文件'] = model_path is not None

    if model_path:
        checks['ONNX推理'] = test_onnx_inference(model_path)
        checks['检测脚本'] = test_detection_script()

    print_section("验证结果汇总")
    for name, result in checks.items():
        status = "✓ 通过" if result else "✗ 失败"
        print(f"  {name}: {status}")

    all_passed = all(checks.values())
    if all_passed:
        print("\n✓ 所有检查通过！ONNX环境配置正确。")
        print("\n使用方法:")
        print("  python yolo_detect_onnx.py --source 图片.jpg --conf 0.25")
        return 0
    else:
        print("\n✗ 部分检查未通过，请根据上述信息修复。")
        print("\n安装依赖:")
        print("  pip install -r requirements_onnx.txt")
        return 1


if __name__ == '__main__':
    sys.exit(main())
