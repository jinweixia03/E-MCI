#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量YOLO检测脚本 - 重构版
使用新的检测模块架构

使用方法:
    python batch_detect.py
"""

import os
import random
import sys
import shutil
from pathlib import Path
from datetime import datetime, timedelta
import warnings

warnings.filterwarnings('ignore')

# 添加项目根目录到路径
BASE_DIR = Path(__file__).parent
sys.path.insert(0, str(BASE_DIR))

from detector import ManholeDetector, ONNXBackend
from detector.utils import get_supported_formats


# 配置
MANHOLES_DIR = BASE_DIR / "uploads" / "manholes"
DETECTIONS_DIR = BASE_DIR / "uploads" / "detections"
MODEL_PATH = BASE_DIR / "models" / "best.onnx"


def generate_detection_no(index: int) -> str:
    """生成检测编号"""
    date_str = datetime.now().strftime('%Y%m%d')
    return f"DET{date_str}{str(index).zfill(4)}"


def generate_sql_statements(manhole_id: str, detection_no: str, img_url: str,
                            result, detection_time: datetime, detection_id: int) -> tuple:
    """
    生成SQL语句（检测记录和维修记录）

    Returns:
        (detection_sql, repair_sql)
    """
    # 检测记录SQL
    has_defect = 1 if result.defect_count > 0 else 0
    defect_types = None
    if result.defects:
        types = [d.type for d in result.defects]
        defect_types = ",".join(set(types))

    detection_sql = f"""INSERT INTO detection (manhole_id, detection_no, detection_time, img_url, has_defect, defect_count, defect_types) VALUES ('{manhole_id}', '{detection_no}', '{detection_time.strftime('%Y-%m-%d %H:%M:%S')}', '{img_url}', {has_defect}, {result.defect_count}, {f"'{defect_types}'" if defect_types else 'NULL'});"""

    # 维修记录SQL
    if has_defect:
        repair_user_id = random.randint(2, 11)
        status_weights = [0.4, 0.3, 0.3]
        status = random.choices([0, 1, 2], weights=status_weights)[0]
        assigned_time = detection_time + timedelta(days=random.randint(1, 3))
        complete_time = "NULL"
        if status == 2:
            complete_time_obj = assigned_time + timedelta(days=random.randint(1, 7))
            complete_time = f"'{complete_time_obj.strftime('%Y-%m-%d %H:%M:%S')}'"
    else:
        repair_user_id = "NULL"
        status = 2
        assigned_time = detection_time
        complete_time = f"'{detection_time.strftime('%Y-%m-%d %H:%M:%S')}'"

    repair_sql = f"""INSERT INTO repair (detection_id, manhole_id, repair_user_id, status, assigned_time, complete_time) VALUES ({detection_id}, '{manhole_id}', {repair_user_id}, {status}, '{assigned_time.strftime('%Y-%m-%d %H:%M:%S')}', {complete_time});"""

    return detection_sql, repair_sql


def save_sql_file(detection_sqls: list, repair_sqls: list, output_path: Path) -> None:
    """保存SQL文件"""
    output_path.parent.mkdir(parents=True, exist_ok=True)

    with open(output_path, 'w', encoding='utf-8') as f:
        f.write("-- ============================================\n")
        f.write("-- 批量YOLO检测数据\n")
        f.write(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- 使用模型: {MODEL_PATH.name}\n")
        f.write("-- ============================================\n\n")
        f.write("SET NAMES utf8mb4;\n\n")

        f.write("-- 检测记录\n")
        for sql in detection_sqls:
            f.write(sql + "\n")

        f.write("\n-- 维修记录\n")
        for sql in repair_sqls:
            f.write(sql + "\n")

        f.write(f"\n-- 完成\n")
        f.write(f"SELECT '插入完成: {len(detection_sqls)} 条检测, {len(repair_sqls)} 条维修' AS message;\n")


def get_image_files(directory: Path) -> list:
    """获取目录中的所有图片文件"""
    image_exts, _ = get_supported_formats()
    return [
        f for f in directory.iterdir()
        if f.is_file() and f.suffix.lower() in image_exts
    ]


def process_image(detector, image_path: Path, manhole_id: str,
                  detection_count: int, detection_id: int) -> tuple:
    """
    处理单张图片的检测

    Returns:
        (detection_sql, repair_sql) 或 (None, None) 如果失败
    """
    try:
        result = detector.detect_image(
            str(image_path),
            save_result=True,
            output_dir=str(DETECTIONS_DIR)
        )

        if not result.success:
            print(f"       检测 {detection_count} 失败: {result.error}")
            return None, None

        # 重命名结果文件
        output_filename = f"{manhole_id}_{detection_count}.jpg"
        output_path = DETECTIONS_DIR / output_filename
        default_output = DETECTIONS_DIR / image_path.name
        if default_output.exists():
            shutil.move(str(default_output), str(output_path))

        # 生成SQL
        detection_no = generate_detection_no(detection_id)
        img_url = f"/uploads/detections/{output_filename}"
        days_ago = random.randint(1, 90)
        detection_time = datetime.now() - timedelta(days=days_ago)

        det_sql, repair_sql = generate_sql_statements(
            manhole_id, detection_no, img_url, result, detection_time, detection_id
        )

        return det_sql, repair_sql

    except Exception as e:
        print(f"       检测 {detection_count} 异常: {e}")
        return None, None


def main():
    """主函数"""
    print("=" * 60)
    print("井盖图片批量YOLO检测")
    print("=" * 60)

    # 检查目录和模型
    if not MANHOLES_DIR.exists():
        print(f"错误: 源目录不存在: {MANHOLES_DIR}")
        return

    if not MODEL_PATH.exists():
        print(f"错误: 模型文件不存在: {MODEL_PATH}")
        return

    # 清空并创建输出目录
    if DETECTIONS_DIR.exists():
        shutil.rmtree(DETECTIONS_DIR)
    DETECTIONS_DIR.mkdir(parents=True, exist_ok=True)

    # 加载模型
    print(f"加载模型: {MODEL_PATH}")
    try:
        backend = ONNXBackend(str(MODEL_PATH), conf_threshold=0.25, iou_threshold=0.5)
        detector = ManholeDetector(backend)
    except Exception as e:
        print(f"加载模型失败: {e}")
        return

    # 获取所有图片文件
    image_files = get_image_files(MANHOLES_DIR)

    print(f"找到 {len(image_files)} 张图片")
    print(f"结果将保存到: {DETECTIONS_DIR}")
    print(f"每张图片将随机检测 2-3 次")
    print()

    # SQL语句列表
    detection_sqls = []
    repair_sqls = []
    detection_id = 1

    # 处理每张图片
    for idx, image_path in enumerate(image_files, 1):
        manhole_id = image_path.stem
        print(f"[{idx}/{len(image_files)}] 处理: {manhole_id}")

        # 随机决定检测次数（2-3次）
        num_detections = random.randint(2, 3)

        for detection_count in range(1, num_detections + 1):
            det_sql, repair_sql = process_image(
                detector, image_path, manhole_id,
                detection_count, detection_id
            )

            if det_sql and repair_sql:
                detection_sqls.append(det_sql)
                repair_sqls.append(repair_sql)
                detection_id += 1

        print(f"       完成 {num_detections} 次检测")

    print()
    print("=" * 60)
    print("检测完成！")
    print(f"生成检测记录: {len(detection_sqls)} 条")
    print(f"生成维修记录: {len(repair_sqls)} 条")
    print()

    # 保存SQL文件
    sql_file = BASE_DIR / "src" / "main" / "resources" / "db" / "insert_detections.sql"
    save_sql_file(detection_sqls, repair_sqls, sql_file)

    print(f"SQL文件已保存: {sql_file}")
    print()
    print("使用方式:")
    print("1. 检查生成的图片在 uploads/detections/ 目录")
    print("2. 执行SQL文件插入数据到数据库")
    print("3. 重启后端服务查看结果")
    print("=" * 60)


if __name__ == "__main__":
    main()
