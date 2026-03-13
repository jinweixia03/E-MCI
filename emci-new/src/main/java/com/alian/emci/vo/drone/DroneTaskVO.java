package com.alian.emci.vo.drone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 无人机任务VO（对应drone_task表）
 */
@Data
@Schema(description = "无人机检测任务")
public class DroneTaskVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "无人机ID")
    private Long droneId;

    @Schema(description = "无人机名称")
    private String droneName;

    @Schema(description = "检测中心纬度")
    private BigDecimal centerLat;

    @Schema(description = "检测中心经度")
    private BigDecimal centerLng;

    @Schema(description = "检测半径（米）")
    private Integer radius;

    @Schema(description = "路径点列表")
    private List<PathPointVO> pathPoints;

    @Schema(description = "覆盖井盖数量")
    private Integer manholeCount;

    @Schema(description = "预计检测时间（分钟）")
    private Integer estimatedTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
