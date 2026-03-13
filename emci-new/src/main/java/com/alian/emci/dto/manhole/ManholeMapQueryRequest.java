package com.alian.emci.dto.manhole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 井盖地图查询请求
 */
@Data
@Schema(description = "井盖地图查询请求")
public class ManholeMapQueryRequest {

    @Schema(description = "最小经度（地图范围查询）")
    private Double minLongitude;

    @Schema(description = "最大经度（地图范围查询）")
    private Double maxLongitude;

    @Schema(description = "最小纬度（地图范围查询）")
    private Double minLatitude;

    @Schema(description = "最大纬度（地图范围查询）")
    private Double maxLatitude;

    @Schema(description = "中心经度")
    private Double centerLongitude;

    @Schema(description = "中心纬度")
    private Double centerLatitude;

    @Schema(description = "搜索半径（米）")
    private Integer radius;

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

    @Schema(description = "是否有缺陷：0-无，1-有")
    private Integer hasDefect;

    @Schema(description = "是否只显示有缺陷的")
    private Boolean onlyDefect;

    @Schema(description = "地图缩放级别")
    private Integer zoom;

    @Schema(description = "是否启用聚合")
    private Boolean enableCluster;

    @Schema(description = "聚合网格大小（像素）")
    private Integer gridSize;

    @Schema(description = "最大返回数量，默认500")
    private Integer maxResults = 500;
}
