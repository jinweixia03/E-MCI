package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 巡检点实体类
 * 记录任务中每个井盖的检测状态和结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inspection_point")
public class InspectionPoint extends BaseEntity {

    /**
     * 巡检点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 井盖ID
     */
    private Long manholeId;

    /**
     * 井盖编号（冗余存储）
     */
    private String manholeNo;

    /**
     * 巡检顺序
     */
    private Integer sequence;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 状态：0-待检测，1-检测中，2-已检测，3-跳过
     */
    private Integer status;

    /**
     * 检测结果：0-正常，1-异常，2-无法检测
     */
    private Integer result;

    /**
     * 检测时间
     */
    private LocalDateTime inspectTime;

    /**
     * 检测图片URL
     */
    private String imageUrl;

    /**
     * 缺陷类型（多个用逗号分隔）
     */
    private String defectTypes;

    /**
     * 置信度
     */
    private BigDecimal confidence;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否已删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
