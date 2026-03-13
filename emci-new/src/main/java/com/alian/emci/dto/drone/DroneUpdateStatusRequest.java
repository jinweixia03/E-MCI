package com.alian.emci.dto.drone;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 无人机状态更新请求
 */
@Data
@Schema(description = "无人机状态更新请求")
public class DroneUpdateStatusRequest {

    @Schema(description = "状态：0-停用，1-运行中", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
