package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 无人机实体类（简化版）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drone")
public class Drone extends BaseEntity {

    private String name;

    /** 状态：0-闲置，1-巡检中，2-充电中，3-维修中 */
    private Integer status;

    private Integer battery;

    private BigDecimal latitude;

    private BigDecimal longitude;

    /** 最大飞行半径（米） */
    private Integer radius;
}
