#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检测结果数据模型
定义统一的结果格式
"""

from dataclasses import dataclass, field, asdict
from typing import List, Optional, Dict, Any
from datetime import datetime
import json


@dataclass
class DefectInfo:
    """缺陷信息"""
    type: str                    # 缺陷类型
    confidence: float           # 置信度 (0-1)
    bbox: List[float]          # 边界框 [x, y, width, height]
    class_id: int = 0          # 类别ID

    def to_dict(self) -> Dict[str, Any]:
        """转换为字典"""
        return {
            "type": self.type,
            "confidence": round(self.confidence, 4),
            "bbox": [round(x, 2) for x in self.bbox],
            "class_id": self.class_id
        }


@dataclass
class DetectionResult:
    """检测结果"""
    success: bool = True
    original_url: str = ""
    result_url: Optional[str] = None
    defect_count: int = 0
    defects: List[DefectInfo] = field(default_factory=list)
    process_time_ms: int = 0
    error: Optional[str] = None
    timestamp: datetime = field(default_factory=datetime.now)

    def to_dict(self) -> Dict[str, Any]:
        """转换为字典（用于JSON序列化）"""
        result = {
            "success": self.success,
            "original_url": self.original_url,
            "result_url": self.result_url,
            "defect_count": len(self.defects),
            "defects": [d.to_dict() for d in self.defects],
            "process_time_ms": self.process_time_ms,
            "timestamp": self.timestamp.isoformat()
        }
        if self.error:
            result["error"] = self.error
        return result

    def to_json(self, ensure_ascii: bool = False) -> str:
        """转换为JSON字符串"""
        return json.dumps(self.to_dict(), ensure_ascii=ensure_ascii)

    @classmethod
    def error_result(cls, error_msg: str, original_url: str = "") -> "DetectionResult":
        """创建错误结果"""
        return cls(
            success=False,
            error=error_msg,
            original_url=original_url,
            process_time_ms=0
        )

    @classmethod
    def success_result(cls, defects: List[DefectInfo], original_url: str = "",
                       result_url: Optional[str] = None,
                       process_time_ms: int = 0) -> "DetectionResult":
        """创建成功结果"""
        return cls(
            success=True,
            original_url=original_url,
            result_url=result_url,
            defects=defects,
            defect_count=len(defects),
            process_time_ms=process_time_ms
        )
