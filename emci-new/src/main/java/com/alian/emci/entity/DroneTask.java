package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 无人机巡检任务实体类（简化版）
 * 不继承BaseEntity，因为数据库表没有deleted字段
 */
@Data
@TableName("drone_task")
public class DroneTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long droneId;

    // 部署区域
    private BigDecimal centerLat;

    private BigDecimal centerLng;

    private Integer radius;

    /**
     * 路径点JSON数组
     * 格式：[{"manholeId": "M001", "sequence": 1}, ...]
     */
    private String pathPoints;

    private Integer manholeCount;

    private Integer estimatedTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
