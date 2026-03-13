package com.alian.emci.vo.map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 井盖地图统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "井盖地图统计信息")
public class ManholeMapStatsVO {

    @Schema(description = "井盖总数")
    private Long totalCount;

    @Schema(description = "正常数量")
    private Long normalCount;

    @Schema(description = "损坏数量")
    private Long damagedCount;

    @Schema(description = "维修中数量")
    private Long repairingCount;

    @Schema(description = "有缺陷数量")
    private Long defectCount;

    @Schema(description = "按类型统计")
    private Map<String, Long> typeStats;

    @Schema(description = "按城市统计")
    private Map<String, Long> cityStats;

    @Schema(description = "按区县统计")
    private Map<String, Long> districtStats;

    @Schema(description = "类型分布列表")
    private List<TypeStat> typeDistribution;

    @Schema(description = "状态分布列表")
    private List<StatusStat> statusDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "类型统计项")
    public static class TypeStat {
        @Schema(description = "类型值")
        private Integer type;

        @Schema(description = "类型名称")
        private String name;

        @Schema(description = "数量")
        private Long count;

        @Schema(description = "占比")
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "状态统计项")
    public static class StatusStat {
        @Schema(description = "状态值")
        private Integer status;

        @Schema(description = "状态名称")
        private String name;

        @Schema(description = "数量")
        private Long count;

        @Schema(description = "占比")
        private Double percentage;
    }
}
