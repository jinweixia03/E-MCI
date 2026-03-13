package com.alian.emci.vo.drone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 无人机VO（简化版）
 */
@Data
@Schema(description = "无人机信息")
public class DroneVO {

    @Schema(description = "无人机ID")
    private Long id;

    @Schema(description = "无人机名称")
    private String name;

    /** 状态：0-闲置，1-巡检中，2-充电中，3-维修中 */
    @Schema(description = "状态：0-闲置，1-巡检中，2-充电中，3-维修中")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "当前纬度")
    private BigDecimal latitude;

    @Schema(description = "当前经度")
    private BigDecimal longitude;

    @Schema(description = "电池电量（百分比）")
    private Integer battery;

    @Schema(description = "最大飞行半径（米）")
    private Integer radius;

    // ========== 任务信息（一对一）==========
    @Schema(description = "检测中心纬度")
    private BigDecimal centerLat;

    @Schema(description = "检测中心经度")
    private BigDecimal centerLng;

    @Schema(description = "检测半径（米）")
    private Integer taskRadius;

    @Schema(description = "检测路径点列表")
    private java.util.List<PathPointVO> pathPoints;

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
