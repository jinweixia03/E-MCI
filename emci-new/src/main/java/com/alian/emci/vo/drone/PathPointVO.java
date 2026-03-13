package com.alian.emci.vo.drone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 路径点VO
 */
@Data
@Schema(description = "路径规划点")
public class PathPointVO {

    @Schema(description = "井盖ID")
    private Long manholeId;

    @Schema(description = "井盖编号")
    private String manholeNo;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "巡检顺序（0是起点）")
    private Integer sequence;

    @Schema(description = "距离起点的累计距离（米）")
    private Integer distanceFromStart;
}
