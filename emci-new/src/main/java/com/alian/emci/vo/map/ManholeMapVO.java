package com.alian.emci.vo.map;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 地图用井盖信息VO（精简版，适合地图展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地图用井盖信息")
public class ManholeMapVO {

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

    @Schema(description = "状态：0-损坏, 1-正常, 2-维修中")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "井盖类型：1-雨水，2-污水，3-电力，4-通信，5-燃气")
    private Integer manholeType;

    @Schema(description = "井盖类型文本")
    private String manholeTypeText;

    @Schema(description = "检测次数")
    private Integer detectionCount;

    @Schema(description = "缺陷数量")
    private Integer defectCount;

    @Schema(description = "安全评分")
    private Double safetyScore;

    @Schema(description = "最后检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastDetTime;

    @Schema(description = "是否存在最新检测")
    private Boolean hasLatestDetection;

    @Schema(description = "最新检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestDetectionTime;

    @Schema(description = "最新检测缺陷数")
    private Integer latestDefectCount;

    @Schema(description = "最新检测是否已修复")
    private Integer latestIsRepaired;
}
