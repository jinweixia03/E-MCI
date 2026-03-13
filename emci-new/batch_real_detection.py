#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量真实YOLO检测脚本
使用真实YOLOv8模型检测所有井盖图片，结果保存到 uploads/detections
每张图片随机检测 2-3 次，模拟多次检测场景

使用方法:
    python batch_real_detection.py
"""

import os
import random
import json
import sys
import shutil
from pathlib import Path
from datetime import datetime, timedelta
import warnings

warnings.filterwarnings('ignore')

# 配置路径
BASE_DIR = Path("D:/1_Source/XFCode/emic/emci-new")
MANHOLES_DIR = BASE_DIR / "uploads" / "manholes"
DETECTIONS_DIR = BASE_DIR / "uploads" / "detections"
MODEL_PATH = BASE_DIR / "models" / "best.onnx"

# 导入YOLO检测器
sys.path.insert(0, str(BASE_DIR))
from yolo_detect_onnx import YOLOv8ONNX, detect_image

# 缺陷类型中文映射
CLASS_NAMES_CN = {
    0: "井盖",
    1: "损坏",
    2: "开启",
    3: "缺失"
}


def generate_detection_no(index):
    """生成检测编号"""
    date_str = datetime.now().strftime('%Y%m%d')
    return f"DET{date_str}{str(index).zfill(4)}"


def generate_detection_sql(manhole_id, detection_no, img_url, result, detection_time):
    """生成检测记录的SQL插入语句"""
    has_defect = 1 if result["defect_count"] > 0 else 0
    defect_count = result["defect_count"]

    # 收集缺陷类型
    defect_types = None
    if result["defects"]:
        types = [d["type"] for d in result["defects"]]
        defect_types = ",".join(set(types))

    sql = f"""INSERT INTO detection (manhole_id, detection_no, detection_time, img_url, has_defect, defect_count, defect_types) VALUES ('{manhole_id}', '{detection_no}', '{detection_time.strftime('%Y-%m-%d %H:%M:%S')}', '{img_url}', {has_defect}, {defect_count}, {f"'{defect_types}'" if defect_types else 'NULL'});"""

    return sql, has_defect


def generate_repair_sql(detection_id, manhole_id, detection_time, has_defect):
    """生成维修记录的SQL插入语句（每个detection都对应一个repair）"""

    if has_defect:
        # 有缺陷的情况：随机分配维修人员，随机状态
        repair_user_id = random.randint(2, 11)
        # 随机状态：0-待维修(40%), 1-维修中(30%), 2-已完成(30%)
        status_weights = [0.4, 0.3, 0.3]
        status = random.choices([0, 1, 2], weights=status_weights)[0]

        # 分配时间（检测后1-3天）
        assigned_time = detection_time + timedelta(days=random.randint(1, 3))

        # 完成时间（如果是已完成状态）
        complete_time = "NULL"
        if status == 2:
            complete_time_obj = assigned_time + timedelta(days=random.randint(1, 7))
            complete_time = f"'{complete_time_obj.strftime('%Y-%m-%d %H:%M:%S')}'"
    else:
        # 无缺陷的情况：状态标记为已完成（无需处理）
        repair_user_id = "NULL"  # 无需分配维修人员
        status = 2  # 已完成（无需处理）
        assigned_time = detection_time  # 分配时间即为检测时间
        complete_time = f"'{detection_time.strftime('%Y-%m-%d %H:%M:%S')}'"  # 完成时间同检测时间

    sql = f"""INSERT INTO repair (detection_id, manhole_id, repair_user_id, status, assigned_time, complete_time) VALUES ({detection_id}, '{manhole_id}', {repair_user_id}, {status}, '{assigned_time.strftime('%Y-%m-%d %H:%M:%S')}', {complete_time});"""

    return sql


def main():
    """主函数"""
    print("=" * 60)
    print("井盖图片批量真实YOLO检测")
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
        detector = YOLOv8ONNX(str(MODEL_PATH), conf_threshold=0.25, iou_threshold=0.5)
    except Exception as e:
        print(f"加载模型失败: {e}")
        return

    # 获取所有图片文件
    image_extensions = {'.jpg', '.jpeg', '.png'}
    image_files = [
        f for f in MANHOLES_DIR.iterdir()
        if f.is_file() and f.suffix.lower() in image_extensions
    ]

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
            try:
                # 使用真实YOLO模型检测
                result = detect_image(detector, str(image_path), str(DETECTIONS_DIR))

                if not result["success"]:
                    print(f"       检测 {detection_count} 失败: {result.get('error', '未知错误')}")
                    continue

                # 生成文件名: {manhole_id}_{n}.jpg
                output_filename = f"{manhole_id}_{detection_count}.jpg"
                output_path = DETECTIONS_DIR / output_filename

                # 重命名结果文件（从默认文件名改为带编号）
                default_output = DETECTIONS_DIR / image_path.name
                if default_output.exists():
                    shutil.move(str(default_output), str(output_path))

                # 生成检测编号
                detection_no = generate_detection_no(detection_id)

                # 图片URL（用于数据库）
                img_url = f"/uploads/detections/{output_filename}"

                # 随机生成检测时间（最近3个月内）
                days_ago = random.randint(1, 90)
                detection_time = datetime.now() - timedelta(days=days_ago)

                # 生成SQL
                det_sql, has_defect = generate_detection_sql(
                    manhole_id, detection_no, img_url, result, detection_time
                )
                detection_sqls.append(det_sql)

                # 生成维修SQL（每个detection都对应一个repair）
                repair_sql = generate_repair_sql(detection_id, manhole_id, detection_time, has_defect)
                repair_sqls.append(repair_sql)

                detection_id += 1

            except Exception as e:
                print(f"       检测 {detection_count} 异常: {e}")
                continue

        print(f"       完成 {num_detections} 次检测")

    print()
    print("=" * 60)
    print("检测完成！")
    print(f"生成检测记录: {len(detection_sqls)} 条")
    print(f"生成维修记录: {len(repair_sqls)} 条")
    print()

    # 保存SQL文件
    sql_file = BASE_DIR / "src" / "main" / "resources" / "db" / "insert_real_detections.sql"
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write("-- ============================================\n")
        f.write("-- 批量真实YOLO检测数据\n")
        f.write(f"-- 生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- 使用模型: {MODEL_PATH.name}\n")
        f.write("-- ============================================\n\n")
        f.write("SET NAMES utf8mb4;\n\n")

        f.write("-- 检测记录\n")
        for sql in detection_sqls:
            f.write(sql + "\n")

        f.write("\n-- 维修记录（每个检测都对应一个维修记录）\n")
        for sql in repair_sqls:
            f.write(sql + "\n")

        f.write("\n-- 完成\n")
        f.write(f"SELECT '插入完成: {len(detection_sqls)} 条检测, {len(repair_sqls)} 条维修' AS message;\n")

    print(f"SQL文件已保存: {sql_file}")
    print()
    print("使用方式:")
    print("1. 检查生成的图片在 uploads/detections/ 目录")
    print("2. 执行SQL文件插入数据到数据库")
    print("3. 重启后端服务查看结果")
    print("=" * 60)


if __name__ == "__main__":
    main()
