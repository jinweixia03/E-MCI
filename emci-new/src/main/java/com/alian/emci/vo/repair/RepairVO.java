package com.alian.emci.vo.repair;

import com.alian.emci.vo.BaseVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维修记录VO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维修记录信息")
public class RepairVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联检测记录ID")
    @JsonProperty("repairId")
    private Long detectionId;

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "维修人员ID")
    private Long repairUserId;

    @Schema(description = "维修人员姓名")
    @JsonProperty("principal")
    private String repairUserName;

    @Schema(description = "状态：0-待维修, 1-维修中, 2-待确认, 3-已完成")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "缺陷类型（逗号分隔）")
    private String defectTypes;

    @Schema(description = "维修内容")
    private String repairContent;

    @Schema(description = "分配时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assignedTime;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("startTime")
    private LocalDate startTimeDate;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("endTime")
    private LocalDate endTimeDate;

    @Schema(description = "维修前图片")
    private String beforeImg;

    @Schema(description = "维修后图片")
    private String afterImg;

    @Schema(description = "备注")
    private String remark;

    // ========== 检测记录相关信息 ==========

    @Schema(description = "检测编号")
    private String detectionNo;

    @Schema(description = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectionTime;

    @Schema(description = "检测原始图片URL")
    private String detectionOriginalImgUrl;

    @Schema(description = "检测结果图片URL（带标注框）")
    private String detectionResultImgUrl;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "缺陷数量")
    private Integer defectCount;

    @Schema(description = "主要缺陷类型")
    private String primaryDefectType;

    @Schema(description = "最高置信度")
    private Double primaryConfidence;
}
