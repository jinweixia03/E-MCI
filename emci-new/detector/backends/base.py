#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
后端基础类
提供通用功能和常量定义
"""

from typing import Dict


class ClassNames:
    """类别名称定义 - 统一使用英文，界面层进行本地化"""

    MANHOLE = "Manhole"
    DAMAGE = "Damage"
    OPEN = "Open"
    MISSING = "Missing"

    # 类别ID映射
    ID_TO_NAME: Dict[int, str] = {
        0: MANHOLE,
        1: DAMAGE,
        2: OPEN,
        3: MISSING,
    }

    NAME_TO_ID: Dict[str, int] = {
        MANHOLE: 0,
        DAMAGE: 1,
        OPEN: 2,
        MISSING: 3,
    }

    # 中文映射（用于显示）
    CN_NAMES: Dict[str, str] = {
        MANHOLE: "井盖",
        DAMAGE: "损坏",
        OPEN: "开启",
        MISSING: "缺失",
    }

    @classmethod
    def get_name(cls, class_id: int) -> str:
        """获取类别名称"""
        return cls.ID_TO_NAME.get(class_id, f"Class{class_id}")

    @classmethod
    def get_cn_name(cls, class_id: int) -> str:
        """获取中文名称"""
        name = cls.get_name(class_id)
        return cls.CN_NAMES.get(name, name)

    @classmethod
    def get_class_id(cls, name: str) -> int:
        """获取类别ID"""
        return cls.NAME_TO_ID.get(name, -1)
