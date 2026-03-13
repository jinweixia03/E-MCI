package com.alian.emci.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计工具类
 * 提供通用的统计计算方法
 */
public final class StatsUtils {

    private StatsUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 创建状态统计Map（初始化所有状态为0）
     *
     * @param totalSupplier 获取总数的函数
     * @return 状态统计Map
     */
    public static Map<String, Long> createStatusStats(Long totalSupplier) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", 0L);
        stats.put("inProgress", 0L);
        stats.put("toConfirm", 0L);
        stats.put("completed", 0L);
        stats.put("total", totalSupplier != null ? totalSupplier : 0L);
        return stats;
    }

    /**
     * 填充状态统计结果
     *
     * @param stats       统计Map
     * @param resultList  查询结果列表
     * @param statusKey   状态字段名
     * @param countKey    数量字段名
     */
    public static void fillStatusStats(Map<String, Long> stats, List<Map<String, Object>> resultList,
                                       String statusKey, String countKey) {
        for (Map<String, Object> map : resultList) {
            Integer status = (Integer) map.get(statusKey);
            Long count = ((Number) map.get(countKey)).longValue();

            switch (status != null ? status : -1) {
                case 0 -> stats.put("pending", count);
                case 1 -> stats.put("inProgress", count);
                case 2 -> stats.put("toConfirm", count);
                case 3 -> stats.put("completed", count);
            }
        }
    }

    /**
     * 计算百分比并格式化为字符串
     *
     * @param part  部分值
     * @param total 总值
     * @return 百分比字符串
     */
    public static String formatPercentage(long part, long total) {
        if (total <= 0) {
            return "0.00%";
        }
        return String.format("%.2f%%", (double) part / total * 100);
    }

    /**
     * 计算频率（每单位时间的数量）
     *
     * @param count    数量
     * @param days     天数
     * @param scale    保留小数位数
     * @return 频率值
     */
    public static double calculateFrequency(long count, int days, int scale) {
        if (days <= 0) {
            return 0.0;
        }
        return MathUtils.round((double) count / days, scale);
    }
}
