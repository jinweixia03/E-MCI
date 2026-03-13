package com.alian.emci.vo.manhole;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 井盖信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "井盖信息")
public class ManholeVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "井盖编号")
    private String manholeId;

    @Schema(description = "井盖图片URL")
    private String imgUrl;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "下次检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate nextDetTime;

    @Schema(description = "状态：0-正常，1-异常，2-维修中，3-已废弃")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "井盖类型：1-雨水，2-污水，3-电力，4-通信，5-燃气")
    private Integer manholeType;

    @Schema(description = "井盖类型文本")
    private String manholeTypeText;

    @Schema(description = "最后检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDetTime;

    @Schema(description = "检测次数")
    private Integer detectionCount;

    @Schema(description = "缺陷数量")
    private Integer defectCount;

    @Schema(description = "安全评分")
    private Double safetyScore;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

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
