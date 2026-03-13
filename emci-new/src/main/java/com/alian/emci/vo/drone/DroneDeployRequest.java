package com.alian.emci.vo.drone;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 无人机部署请求
 */
@Data
@Schema(description = "无人机部署请求")
public class DroneDeployRequest {

    @Schema(description = "无人机ID")
    @NotNull(message = "请选择无人机")
    private Long droneId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "部署中心纬度")
    @NotNull(message = "请选择部署位置")
    private BigDecimal centerLat;

    @Schema(description = "部署中心经度")
    @NotNull(message = "请选择部署位置")
    private BigDecimal centerLng;

    @Schema(description = "巡检半径（米），默认500米")
    private Integer radius = 500;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "预览时计算的路径点（可选，传入则直接使用）")
    private List<PathPointVO> pathPoints;
}
