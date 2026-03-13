package com.alian.emci.service.impl;

import com.alian.emci.common.Result;
import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Manhole;
import com.alian.emci.mapper.DetectionMapper;
import com.alian.emci.mapper.ManholeMapper;
import com.alian.emci.service.DashboardService;
import com.alian.emci.vo.dashboard.DashboardStatsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现 - 数据大屏
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ManholeMapper manholeMapper;
    private final DetectionMapper detectionMapper;

    // 井盖类型映射
    private static final Map<Integer, String> MANHOLE_TYPE_MAP = new HashMap<>() {{
        put(1, "雨水");
        put(2, "污水");
        put(3, "电力");
        put(4, "通信");
        put(5, "燃气");
    }};

    // 状态映射 (0-正常, 1-损坏, 2-维修中)
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>() {{
        put(0, "正常");
        put(1, "损坏");
        put(2, "维修中");
    }};

    // 缺陷类型映射
    private static final Map<String, String> DEFECT_TYPE_MAP = new HashMap<>() {{
        put("crack", "裂缝");
        put("wear", "磨损");
        put("deformation", "变形");
        put("missing", "缺失");
        put("displacement", "位移");
        put("corrosion", "腐蚀");
        put("破损", "破损");
        put("下沉", "下沉");
        put("缺失", "缺失");
        put("松动", "松动");
    }};

    @Override
    public Result<DashboardStatsVO> getStats() {
        DashboardStatsVO stats = DashboardStatsVO.builder()
                .manholeStats(getManholeStats())
                .detectionStats(getDetectionStats())
                .repairStats(getRepairStats())
                .droneStats(getDroneStats())
                .defectTypeDistribution(getDefectTypeDistribution())
                .manholeTypeDistribution(getManholeTypeDistribution())
                .statusDistribution(getStatusDistribution())
                .detectionTrend(getDetectionTrend())
                .cityDistribution(getCityDistribution())
                .recentDetections(getRecentDetections())
                .safetyRanking(getSafetyRanking())
                .build();

        return Result.success(stats);
    }

    /**
     * 获取井盖统计
     */
    private DashboardStatsVO.ManholeStats getManholeStats() {
        List<Manhole> manholes = manholeMapper.selectList(null);
        long totalCount = manholes.size();
        long normalCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 0).count();
        long damagedCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long repairingCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 2).count();
        long defectCount = manholes.stream().filter(m -> m.getDefectCount() != null && m.getDefectCount() > 0).count();

        return DashboardStatsVO.ManholeStats.builder()
                .totalCount(totalCount)
                .normalCount(normalCount)
                .damagedCount(damagedCount)
                .repairingCount(repairingCount)
                .normalRate(totalCount > 0 ? round((double) normalCount / totalCount * 100, 2) : 0.0)
                .defectCount(defectCount)
                .defectRate(totalCount > 0 ? round((double) defectCount / totalCount * 100, 2) : 0.0)
                .build();
    }

    /**
     * 获取检测统计
     */
    private DashboardStatsVO.DetectionStats getDetectionStats() {
        // 今日
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        long todayCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, todayStart)
                        .lt(Detection::getDetectionTime, todayEnd)
        );
        long todayDefectCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, todayStart)
                        .lt(Detection::getDetectionTime, todayEnd)
                        .gt(Detection::getDefectCount, 0)
        );

        // 本周
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        long weekCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, weekStart)
        );
        long weekDefectCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, weekStart)
                        .gt(Detection::getDefectCount, 0)
        );

        // 本月
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, monthStart)
        );
        long monthDefectCount = detectionMapper.selectCount(
                new LambdaQueryWrapper<Detection>()
                        .ge(Detection::getDetectionTime, monthStart)
                        .gt(Detection::getDefectCount, 0)
        );

        long totalCount = detectionMapper.selectCount(null);

        return DashboardStatsVO.DetectionStats.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .todayDefectCount(todayDefectCount)
                .weekDefectCount(weekDefectCount)
                .monthDefectCount(monthDefectCount)
                .build();
    }

    /**
     * 获取维修统计（基于井盖状态计算）
     */
    private DashboardStatsVO.RepairStats getRepairStats() {
        List<Manhole> manholes = manholeMapper.selectList(null);
        long damagedCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long repairingCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 2).count();
        long normalCount = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 0).count();

        // 已完成的维修 = 正常状态且有过缺陷记录的
        long completedCount = Math.max(0, normalCount - damagedCount - repairingCount);
        long totalCount = damagedCount + repairingCount + completedCount;

        return DashboardStatsVO.RepairStats.builder()
                .pendingCount(damagedCount)
                .inProgressCount(repairingCount)
                .completedCount(completedCount)
                .totalCount(totalCount)
                .completionRate(totalCount > 0 ? round((double) completedCount / totalCount * 100, 2) : 0.0)
                .build();
    }

    /**
     * 获取无人机统计（模拟数据）
     */
    private DashboardStatsVO.DroneStats getDroneStats() {
        // 返回模拟数据，因为无人机功能尚未实现
        return DashboardStatsVO.DroneStats.builder()
                .totalCount(0L)
                .onlineCount(0L)
                .offlineCount(0L)
                .chargingCount(0L)
                .faultCount(0L)
                .onlineRate(0.0)
                .totalFlightHours(0L)
                .totalInspectionCount(0L)
                .build();
    }

    /**
     * 获取缺陷类型分布
     */
    private List<DashboardStatsVO.DefectTypeStat> getDefectTypeDistribution() {
        List<Detection> detections = detectionMapper.selectList(
                new LambdaQueryWrapper<Detection>().gt(Detection::getDefectCount, 0)
        );

        // 统计缺陷类型
        Map<String, Long> defectCounts = new HashMap<>();
        for (Detection d : detections) {
            if (d.getDefectTypes() != null) {
                String[] types = d.getDefectTypes().split(",");
                for (String type : types) {
                    String trimmed = type.trim();
                    if (!trimmed.isEmpty()) {
                        defectCounts.merge(trimmed, 1L, Long::sum);
                    }
                }
            }
        }

        long total = defectCounts.values().stream().mapToLong(Long::longValue).sum();

        return defectCounts.entrySet().stream()
                .map(e -> DashboardStatsVO.DefectTypeStat.builder()
                        .type(e.getKey())
                        .name(DEFECT_TYPE_MAP.getOrDefault(e.getKey(), e.getKey()))
                        .count(e.getValue())
                        .percentage(total > 0 ? round((double) e.getValue() / total * 100, 2) : 0.0)
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(6)
                .collect(Collectors.toList());
    }

    /**
     * 获取井盖类型分布
     */
    private List<DashboardStatsVO.TypeStat> getManholeTypeDistribution() {
        List<Manhole> manholes = manholeMapper.selectList(null);
        Map<Integer, Long> typeCount = manholes.stream()
                .filter(m -> m.getManholeType() != null)
                .collect(Collectors.groupingBy(Manhole::getManholeType, Collectors.counting()));

        long total = manholes.size();

        return typeCount.entrySet().stream()
                .map(e -> DashboardStatsVO.TypeStat.builder()
                        .type(e.getKey())
                        .name(MANHOLE_TYPE_MAP.getOrDefault(e.getKey(), "未知"))
                        .count(e.getValue())
                        .percentage(total > 0 ? round((double) e.getValue() / total * 100, 2) : 0.0)
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }

    /**
     * 获取状态分布
     */
    private List<DashboardStatsVO.StatusStat> getStatusDistribution() {
        List<Manhole> manholes = manholeMapper.selectList(null);
        long total = manholes.size();
        long normal = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 0).count();
        long damaged = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long repairing = manholes.stream().filter(m -> m.getStatus() != null && m.getStatus() == 2).count();

        return Arrays.asList(
                DashboardStatsVO.StatusStat.builder()
                        .status(0).name("正常").count(normal)
                        .percentage(total > 0 ? round((double) normal / total * 100, 2) : 0.0)
                        .build(),
                DashboardStatsVO.StatusStat.builder()
                        .status(1).name("损坏").count(damaged)
                        .percentage(total > 0 ? round((double) damaged / total * 100, 2) : 0.0)
                        .build(),
                DashboardStatsVO.StatusStat.builder()
                        .status(2).name("维修中").count(repairing)
                        .percentage(total > 0 ? round((double) repairing / total * 100, 2) : 0.0)
                        .build()
        );
    }

    /**
     * 获取检测趋势（最近7天）
     */
    private List<DashboardStatsVO.TrendData> getDetectionTrend() {
        List<DashboardStatsVO.TrendData> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            long detectionCount = detectionMapper.selectCount(
                    new LambdaQueryWrapper<Detection>()
                            .ge(Detection::getDetectionTime, startOfDay)
                            .lt(Detection::getDetectionTime, endOfDay)
            );
            long defectCount = detectionMapper.selectCount(
                    new LambdaQueryWrapper<Detection>()
                            .ge(Detection::getDetectionTime, startOfDay)
                            .lt(Detection::getDetectionTime, endOfDay)
                            .gt(Detection::getDefectCount, 0)
            );

            trend.add(DashboardStatsVO.TrendData.builder()
                    .date(date.format(formatter))
                    .detectionCount(detectionCount)
                    .defectCount(defectCount)
                    .build());
        }

        return trend;
    }

    /**
     * 获取城市分布
     */
    private List<DashboardStatsVO.CityStat> getCityDistribution() {
        List<Manhole> manholes = manholeMapper.selectList(null);
        Map<String, Long> cityCount = manholes.stream()
                .filter(m -> m.getCity() != null && !m.getCity().isEmpty())
                .collect(Collectors.groupingBy(Manhole::getCity, Collectors.counting()));

        long total = manholes.size();

        return cityCount.entrySet().stream()
                .map(e -> DashboardStatsVO.CityStat.builder()
                        .city(e.getKey())
                        .count(e.getValue())
                        .percentage(total > 0 ? round((double) e.getValue() / total * 100, 2) : 0.0)
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 获取最近检测记录
     */
    private List<DashboardStatsVO.RecentDetection> getRecentDetections() {
        List<Detection> detections = detectionMapper.selectList(
                new LambdaQueryWrapper<Detection>()
                        .orderByDesc(Detection::getDetectionTime)
                        .last("LIMIT 10")
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        return detections.stream().map(d -> {
            String manholeId = d.getManholeId();
            // 获取井盖地址
            List<Manhole> manholes = manholeMapper.selectList(
                    new LambdaQueryWrapper<Manhole>().eq(Manhole::getManholeId, manholeId)
            );
            String address = manholes.isEmpty() ? "" : manholes.get(0).getAddress();

            return DashboardStatsVO.RecentDetection.builder()
                    .manholeId(manholeId)
                    .address(address)
                    .detectionTime(d.getDetectionTime() != null ? d.getDetectionTime().format(formatter) : "")
                    .defectCount(d.getDefectCount() != null ? d.getDefectCount() : 0)
                    .defectTypes(d.getDefectTypes())
                    .confidence(d.getPrimaryConfidence())
                    .status(d.getDefectCount() != null && d.getDefectCount() > 0 ? 1 : 0)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 获取安全评分排行（最低分的前10个）
     */
    private List<DashboardStatsVO.SafetyRank> getSafetyRanking() {
        List<Manhole> manholes = manholeMapper.selectList(
                new LambdaQueryWrapper<Manhole>()
                        .isNotNull(Manhole::getSafetyScore)
                        .orderByAsc(Manhole::getSafetyScore)
                        .last("LIMIT 10")
        );

        return manholes.stream().map(m -> {
            int level;
            String levelName;
            double score = m.getSafetyScore() != null ? m.getSafetyScore() : 100;

            if (score >= 90) {
                level = 1;
                levelName = "优秀";
            } else if (score >= 80) {
                level = 2;
                levelName = "良好";
            } else if (score >= 60) {
                level = 3;
                levelName = "一般";
            } else {
                level = 4;
                levelName = "差";
            }

            return DashboardStatsVO.SafetyRank.builder()
                    .manholeId(m.getManholeId())
                    .address(m.getAddress())
                    .score(score)
                    .level(level)
                    .levelName(levelName)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 四舍五入
     */
    private double round(double value, int places) {
        return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
    }
}
