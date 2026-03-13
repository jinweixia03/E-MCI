package com.alian.emci.dto.drone;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 无人机更新请求
 */
@Data
@Schema(description = "无人机更新请求")
public class DroneUpdateRequest {

    @Schema(description = "注册时间")
    private LocalDate registerTime;

    @Schema(description = "最大续航时间(小时)")
    private Double maxHour;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "覆盖半径(米)")
    private Integer radius;

    @Schema(description = "状态：0-停用，1-运行中")
    private Integer status;
}
