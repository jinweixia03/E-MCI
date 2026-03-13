package com.alian.emci.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 检测缺陷详情实体
 */
@Data
@TableName("detection_defect_detail")
@Schema(description = "检测缺陷详情实体")
public class DetectionDefectDetail {

    private static final long serialVersionUID = 1L;

    @Schema(description = "缺陷详情ID")
    private Long id;

    @Schema(description = "关联检测记录ID")
    private Long detectionId;

    @Schema(description = "缺陷类型")
    private String defectType;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "左上角X坐标（相对比例）")
    private Double bboxX;

    @Schema(description = "左上角Y坐标（相对比例）")
    private Double bboxY;

    @Schema(description = "边界框宽度（相对比例）")
    private Double bboxWidth;

    @Schema(description = "边界框高度（相对比例）")
    private Double bboxHeight;

    @Schema(description = "左上角X像素坐标")
    private Integer pixelX;

    @Schema(description = "左上角Y像素坐标")
    private Integer pixelY;

    @Schema(description = "边界框宽度（像素）")
    private Integer pixelWidth;

    @Schema(description = "边界框高度（像素）")
    private Integer pixelHeight;
}
