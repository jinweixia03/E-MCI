package com.alian.emci.vo.manhole;

import com.alian.emci.vo.BaseManholeVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 井盖信息VO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "井盖信息")
public class ManholeVO extends BaseManholeVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    // ========== 最新检测记录信息 ==========

    @Schema(description = "检测记录ID")
    private Long detectionId;

    @Schema(description = "检测原始图片URL")
    private String detectionOriginalImgUrl;

    @Schema(description = "检测结果图片URL（带标注框）")
    private String detectionResultImgUrl;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "缺陷类型列表")
    private String defectTypes;

    @Schema(description = "主要缺陷类型")
    private String primaryDefectType;

    @Schema(description = "最高置信度")
    private Double primaryConfidence;
}
