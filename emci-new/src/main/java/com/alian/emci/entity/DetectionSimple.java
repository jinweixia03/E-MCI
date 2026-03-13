package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 检测记录实体（简化版）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("detection")
@Schema(description = "检测记录")
public class DetectionSimple extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "关联井盖编号")
    private String manholeId;

    @Schema(description = "检测编号")
    private String detectionNo;

    @Schema(description = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectionTime;

    @Schema(description = "检测图片URL")
    private String imgUrl;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "缺陷总数")
    private Integer defectCount;

    @Schema(description = "缺陷类型列表")
    private String defectTypes;
}
