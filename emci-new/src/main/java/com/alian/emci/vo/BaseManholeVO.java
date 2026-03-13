package com.alian.emci.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 井盖VO基础类
 * 包含井盖相关VO共有的字段
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class BaseManholeVO extends BaseVO {

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
}
