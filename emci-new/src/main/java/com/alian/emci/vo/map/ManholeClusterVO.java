package com.alian.emci.vo.map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 井盖聚合VO（用于地图聚合展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "井盖聚合信息")
public class ManholeClusterVO {

    @Schema(description = "聚合中心经度")
    private Double longitude;

    @Schema(description = "聚合中心纬度")
    private Double latitude;

    @Schema(description = "聚合数量")
    private Integer count;

    @Schema(description = "聚合ID（网格ID）")
    private String clusterId;

    @Schema(description = "包含的井盖列表")
    private List<ManholeMapVO> manholes;

    @Schema(description = "聚合边界（西南角经度）")
    private Double swLng;

    @Schema(description = "聚合边界（西南角纬度）")
    private Double swLat;

    @Schema(description = "聚合边界（东北角经度）")
    private Double neLng;

    @Schema(description = "聚合边界（东北角纬度）")
    private Double neLat;
}
