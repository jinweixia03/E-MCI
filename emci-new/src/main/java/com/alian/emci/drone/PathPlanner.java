package com.alian.emci.drone;

import com.alian.emci.entity.Manhole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 无人机路径规划器
 * 使用贪心算法 + 2-opt优化巡检路径
 */
@Component
public class PathPlanner {

    // 地球半径（米）
    private static final double EARTH_RADIUS = 6371000;

    /**
     * 路径点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PathPoint {
        private Long manholeId;
        private String manholeNo;
        private BigDecimal lat;
        private BigDecimal lng;
        private int sequence;
        // 距离起点的累计距离（米）
        private int distanceFromStart;
    }

    /**
     * 规划最优巡检路径（贪心算法 + 2-opt优化）
     *
     * @param startLat 起点纬度
     * @param startLng 起点经度
     * @param manholes 待巡检的井盖列表
     * @return 按最优顺序排列的路径点列表
     */
    public List<PathPoint> planOptimalPath(BigDecimal startLat, BigDecimal startLng, List<Manhole> manholes) {
        if (manholes == null || manholes.isEmpty()) {
            return new ArrayList<>();
        }

        // 构建点列表（起点 + 所有井盖）
        List<Point> points = new ArrayList<>();
        points.add(new Point(null, startLat, startLng)); // 起点

        for (Manhole manhole : manholes) {
            points.add(new Point(manhole.getId(), manhole.getManholeId(),
                    BigDecimal.valueOf(manhole.getLatitude()),
                    BigDecimal.valueOf(manhole.getLongitude())));
        }

        // 使用贪心算法获取初始路径
        List<PathPoint> path = greedyPath(points, manholes);

        // 如果点数不多，使用2-opt优化
        if (points.size() <= 50) {
            path = twoOptOptimize(path, points);
        }

        return path;
    }

    /**
     * 2-opt优化：通过交换路径中的两条边来缩短总距离
     */
    private List<PathPoint> twoOptOptimize(List<PathPoint> initialPath, List<Point> points) {
        int n = initialPath.size();
        if (n <= 3) return initialPath;

        // 构建距离矩阵
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    dist[i][j] = haversineDistance(
                        new Point(null, initialPath.get(i).getLat(), initialPath.get(i).getLng()),
                        new Point(null, initialPath.get(j).getLat(), initialPath.get(j).getLng())
                    );
                }
            }
        }

        int[] path = new int[n];
        for (int i = 0; i < n; i++) {
            path[i] = i;
        }

        boolean improved = true;
        int maxIterations = 100;
        int iteration = 0;

        while (improved && iteration < maxIterations) {
            improved = false;
            iteration++;

            for (int i = 1; i < n - 1; i++) {
                for (int j = i + 1; j < n; j++) {
                    // 计算交换前后的距离变化
                    double before = dist[path[i-1]][path[i]] + dist[path[j]][path[(j+1)%n]];
                    double after = dist[path[i-1]][path[j]] + dist[path[i]][path[(j+1)%n]];

                    if (after < before - 0.001) {
                        // 反转 i 到 j 之间的路径
                        reverse(path, i, j);
                        improved = true;
                    }
                }
            }
        }

        // 构建优化后的路径
        List<PathPoint> result = new ArrayList<>();
        double totalDistance = 0;

        for (int i = 0; i < path.length; i++) {
            PathPoint pp = initialPath.get(path[i]);
            pp.setSequence(i);

            if (i > 0) {
                totalDistance += dist[path[i-1]][path[i]];
            }
            pp.setDistanceFromStart((int) totalDistance);

            result.add(pp);
        }

        return result;
    }

    private void reverse(int[] path, int i, int j) {
        while (i < j) {
            int temp = path[i];
            path[i] = path[j];
            path[j] = temp;
            i++;
            j--;
        }
    }

    /**
     * 贪心算法：每次选择最近的未访问点
     */
    private List<PathPoint> greedyPath(List<Point> points, List<Manhole> manholes) {
        int n = points.size();
        boolean[] visited = new boolean[n];
        List<Integer> path = new ArrayList<>();

        // 从起点开始
        int current = 0;
        path.add(current);
        visited[current] = true;

        // 贪心选择最近的点
        while (path.size() < n) {
            double minDist = Double.MAX_VALUE;
            int next = -1;

            for (int i = 1; i < n; i++) {
                if (!visited[i]) {
                    double dist = haversineDistance(points.get(current), points.get(i));
                    if (dist < minDist) {
                        minDist = dist;
                        next = i;
                    }
                }
            }

            if (next == -1) break;

            visited[next] = true;
            path.add(next);
            current = next;
        }

        return buildResult(path, points, manholes);
    }

    /**
     * 构建结果列表
     */
    private List<PathPoint> buildResult(List<Integer> path, List<Point> points, List<Manhole> manholes) {
        List<PathPoint> result = new ArrayList<>();
        double totalDistance = 0;

        for (int i = 0; i < path.size(); i++) {
            int index = path.get(i);
            Point point = points.get(index);

            PathPoint pp = new PathPoint();
            pp.setManholeId(point.id);
            pp.setManholeNo(point.manholeNo);
            pp.setLat(point.lat);
            pp.setLng(point.lng);
            pp.setSequence(i); // 0是起点，1开始是巡检点

            // 计算累计距离
            if (i > 0) {
                Point prevPoint = points.get(path.get(i - 1));
                totalDistance += haversineDistance(prevPoint, point);
            }
            pp.setDistanceFromStart((int) totalDistance);

            result.add(pp);
        }

        return result;
    }

    /**
     * 计算两点之间的球面距离（Haversine公式）
     */
    private double haversineDistance(Point p1, Point p2) {
        double lat1 = Math.toRadians(p1.lat.doubleValue());
        double lat2 = Math.toRadians(p2.lat.doubleValue());
        double deltaLat = Math.toRadians(p2.lat.doubleValue() - p1.lat.doubleValue());
        double deltaLng = Math.toRadians(p2.lng.doubleValue() - p1.lng.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * 计算两点之间的距离（对外接口）
     */
    public double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        return haversineDistance(
                new Point(null, lat1, lng1),
                new Point(null, lat2, lng2)
        );
    }

    /**
     * 内部点类
     */
    @Data
    @AllArgsConstructor
    private static class Point {
        private Long id;
        private String manholeNo;
        private BigDecimal lat;
        private BigDecimal lng;

        public Point(Long id, BigDecimal lat, BigDecimal lng) {
            this.id = id;
            this.lat = lat;
            this.lng = lng;
        }
    }
}
