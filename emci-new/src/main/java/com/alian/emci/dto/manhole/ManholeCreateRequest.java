package com.alian.emci.dto.manhole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 井盖创建请求
 */
@Data
@Schema(description = "井盖创建请求")
public class ManholeCreateRequest {

    @Schema(description = "井盖编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "井盖编号不能为空")
    private String manholeId;

    @Schema(description = "井盖图片URL")
    private String imgUrl;

    @Schema(description = "纬度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "纬度不能为空")
    private Double latitude;

    @Schema(description = "经度", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "经度不能为空")
    private Double longitude;

    @Schema(description = "下次检测时间")
    private LocalDate nextDetTime;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "状态：0-损坏, 1-正常, 2-维修中")
    private Integer status;

    @Schema(description = "所在省份")
    private String province;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "井盖类型：1-雨水，2-污水，3-电力，4-通信，5-燃气")
    private Integer manholeType;
}
