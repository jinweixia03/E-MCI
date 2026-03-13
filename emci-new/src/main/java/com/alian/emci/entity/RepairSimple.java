package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维修记录实体（简化版）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("repair")
@Schema(description = "维修记录")
public class RepairSimple extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联检测记录ID")
    private Long detectionId;

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "维修人员ID")
    private Long repairUserId;

    @Schema(description = "状态：0-待维修, 1-维修中, 2-已完成")
    private Integer status;

    @Schema(description = "分配时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
}
