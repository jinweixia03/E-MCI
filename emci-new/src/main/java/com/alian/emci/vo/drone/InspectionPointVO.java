package com.alian.emci.vo.drone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 巡检点VO
 */
@Data
@Schema(description = "巡检点信息")
public class InspectionPointVO {

    @Schema(description = "巡检点ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "井盖ID")
    private Long manholeId;

    @Schema(description = "井盖编号")
    private String manholeNo;

    @Schema(description = "巡检顺序")
    private Integer sequence;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "状态：0-待检测，1-检测中，2-已检测，3-跳过")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "检测结果：0-正常，1-异常，2-无法检测")
    private Integer result;

    @Schema(description = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inspectTime;

    @Schema(description = "检测图片URL")
    private String imageUrl;

    @Schema(description = "缺陷类型")
    private String defectTypes;

    @Schema(description = "置信度")
    private BigDecimal confidence;

    @Schema(description = "备注")
    private String remark;
}
