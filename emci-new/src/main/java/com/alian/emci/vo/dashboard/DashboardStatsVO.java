package com.alian.emci.vo.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 数据大屏统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "数据大屏统计信息")
public class DashboardStatsVO {

    @Schema(description = "井盖统计")
    private ManholeStats manholeStats;

    @Schema(description = "检测统计")
    private DetectionStats detectionStats;

    @Schema(description = "维修统计")
    private RepairStats repairStats;

    @Schema(description = "无人机统计")
    private DroneStats droneStats;

    @Schema(description = "缺陷类型分布")
    private List<DefectTypeStat> defectTypeDistribution;

    @Schema(description = "井盖类型分布")
    private List<TypeStat> manholeTypeDistribution;

    @Schema(description = "状态分布")
    private List<StatusStat> statusDistribution;

    @Schema(description = "最近7天检测趋势")
    private List<TrendData> detectionTrend;

    @Schema(description = "城市分布")
    private List<CityStat> cityDistribution;

    @Schema(description = "最近检测记录")
    private List<RecentDetection> recentDetections;

    @Schema(description = "安全评分排行")
    private List<SafetyRank> safetyRanking;

    // ============= 子类定义 =============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ManholeStats {
        private Long totalCount;
        private Long normalCount;
        private Long damagedCount;
        private Long repairingCount;
        private Double normalRate;
        private Long defectCount;
        private Double defectRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetectionStats {
        private Long todayCount;
        private Long weekCount;
        private Long monthCount;
        private Long totalCount;
        private Long todayDefectCount;
        private Long weekDefectCount;
        private Long monthDefectCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RepairStats {
        private Long pendingCount;
        private Long inProgressCount;
        private Long completedCount;
        private Long totalCount;
        private Double completionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DroneStats {
        private Long totalCount;
        private Long onlineCount;
        private Long offlineCount;
        private Long chargingCount;
        private Long faultCount;
        private Double onlineRate;
        private Long totalFlightHours;
        private Long totalInspectionCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefectTypeStat {
        private String type;
        private String name;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStat {
        private Integer type;
        private String name;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusStat {
        private Integer status;
        private String name;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String date;
        private Long detectionCount;
        private Long defectCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CityStat {
        private String city;
        private Long count;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentDetection {
        private String manholeId;
        private String address;
        private String detectionTime;
        private Integer defectCount;
        private String defectTypes;
        private Double confidence;
        private Integer status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyRank {
        private String manholeId;
        private String address;
        private Double score;
        private Integer level;
        private String levelName;
    }
}
