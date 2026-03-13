package com.alian.emci.dto.manhole;

import com.alian.emci.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 井盖查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "井盖查询请求")
public class ManholeQueryRequest extends PageRequest {

    @Schema(description = "井盖编号")
    private String manholeId;

    @Schema(description = "关键词（编号/位置模糊查询）")
    private String keyword;

    @Schema(description = "状态：0-损坏, 1-正常, 2-维修中")
    private Integer status;

    @Schema(description = "所在城市")
    private String city;

    @Schema(description = "所在区县")
    private String district;

    @Schema(description = "井盖类型：1-雨水，2-污水，3-电力，4-通信，5-燃气")
    private Integer manholeType;

    @Schema(description = "最小经度（地图范围查询）")
    private Double minLongitude;

    @Schema(description = "最大经度（地图范围查询）")
    private Double maxLongitude;

    @Schema(description = "最小纬度（地图范围查询）")
    private Double minLatitude;

    @Schema(description = "最大纬度（地图范围查询）")
    private Double maxLatitude;

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;
}
