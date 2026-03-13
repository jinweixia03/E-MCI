package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维修记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair")
@Schema(description = "维修记录实体")
public class Repair extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联检测记录ID")
    private Long detectionId;

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "维修人员ID")
    private Long repairUserId;

    @Schema(description = "状态：0-待维修, 1-维修中, 2-待确认, 3-已完成")
    private Integer status;

    @Schema(description = "分配时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    // ========== 扩展字段（极简版） ==========

    @Schema(description = "维修前图片URL（来自detection）")
    @TableField("before_img")
    private String beforeImg;

    @Schema(description = "维修后图片URL（存在uploads/repair/）")
    @TableField("after_img")
    private String afterImg;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

}
