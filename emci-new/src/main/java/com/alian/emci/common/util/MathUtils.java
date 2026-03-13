package com.alian.emci.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数学计算工具类
 */
public final class MathUtils {

    private MathUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 四舍五入
     *
     * @param value  原始值
     * @param places 保留小数位数
     * @return 四舍五入后的值
     */
    public static double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 计算百分比
     *
     * @param part  部分值
     * @param total 总值
     * @param scale 保留小数位数
     * @return 百分比值 (0-100)
     */
    public static double calculatePercentage(long part, long total, int scale) {
        if (total <= 0) {
            return 0.0;
        }
        return round((double) part / total * 100, scale);
    }

    /**
     * 计算两点间距离（米）- Haversine公式
     *
     * @param lng1 经度1
     * @param lat1 纬度1
     * @param lng2 经度2
     * @param lat2 纬度2
     * @return 距离（米）
     */
    public static double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        return s * 6378137; // 地球半径
    }

    /**
     * 计算安全等级
     *
     * @param score 安全评分
     * @return 等级对象
     */
    public static SafetyLevel calculateSafetyLevel(double score) {
        if (score >= 90) {
            return new SafetyLevel(1, "优秀");
        } else if (score >= 80) {
            return new SafetyLevel(2, "良好");
        } else if (score >= 60) {
            return new SafetyLevel(3, "一般");
        } else {
            return new SafetyLevel(4, "差");
        }
    }

    /**
     * 安全等级记录
     */
    public record SafetyLevel(int level, String name) {
    }
}
