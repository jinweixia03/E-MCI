package com.alian.emci.dto.manhole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 井盖更新请求
 */
@Data
@Schema(description = "井盖更新请求")
public class ManholeUpdateRequest {

    @Schema(description = "井盖图片URL")
    private String imgUrl;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
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
