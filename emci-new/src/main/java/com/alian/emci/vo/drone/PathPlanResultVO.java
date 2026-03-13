package com.alian.emci.vo.drone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路径规划结果VO
 */
@Data
@Schema(description = "路径规划结果")
public class PathPlanResultVO {

    @Schema(description = "部署中心纬度")
    private BigDecimal centerLat;

    @Schema(description = "部署中心经度")
    private BigDecimal centerLng;

    @Schema(description = "巡检半径（米）")
    private Integer radius;

    @Schema(description = "覆盖井盖数量")
    private Integer manholeCount;

    @Schema(description = "最优路径点列表（已排序）")
    private List<PathPointVO> pathPoints;

    @Schema(description = "预计飞行距离（米）")
    private Integer estimatedDistance;

    @Schema(description = "预计飞行时间（分钟）")
    private Integer estimatedTime;

    @Schema(description = "算法类型：A_STAR-启发式搜索，GREEDY-贪心算法")
    private String algorithmType;
}
