package com.alian.emci.service.impl;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.common.ResultCode;
import com.alian.emci.dto.manhole.ManholeCreateRequest;
import com.alian.emci.dto.manhole.ManholeMapQueryRequest;
import com.alian.emci.dto.manhole.ManholeQueryRequest;
import com.alian.emci.dto.manhole.ManholeUpdateRequest;
import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Manhole;
import com.alian.emci.exception.BusinessException;
import com.alian.emci.mapper.DetectionMapper;
import com.alian.emci.mapper.ManholeMapper;
import com.alian.emci.service.ManholeService;
import com.alian.emci.vo.manhole.ManholeVO;
import com.alian.emci.vo.map.ManholeClusterVO;
import com.alian.emci.vo.map.ManholeMapStatsVO;
import com.alian.emci.vo.map.ManholeMapVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.beans.FeatureDescriptor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 井盖服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManholeServiceImpl implements ManholeService {

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

    // 井盖状态映射 (与H2 Schema保持一致: 0-正常, 1-异常/损坏, 2-维修中)
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>() {{
        put(0, "正常");
        put(1, "损坏");
        put(2, "维修中");
    }};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ManholeVO> create(ManholeCreateRequest request) {
        // 检查编号是否已存在
        if (manholeMapper.exists(new LambdaQueryWrapper<Manhole>()
                .eq(Manhole::getManholeId, request.getManholeId()))) {
            throw new BusinessException(ResultCode.CONFLICT, "井盖编号已存在");
        }

        Manhole manhole = new Manhole();
        BeanUtils.copyProperties(request, manhole);
        manholeMapper.insert(manhole);

        log.info("创建井盖: {}", manhole.getManholeId());
        return Result.success("创建成功", convertToVO(manhole));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ManholeVO> update(Long id, ManholeUpdateRequest request) {
        Manhole manhole = manholeMapper.selectById(id);
        if (manhole == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "井盖不存在");
        }

        // 只复制非null属性
        copyNonNullProperties(request, manhole);
        manholeMapper.updateById(manhole);

        log.info("更新井盖: {}", manhole.getManholeId());
        return Result.success("更新成功", convertToVO(manhole));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        Manhole manhole = manholeMapper.selectById(id);
        if (manhole == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "井盖不存在");
        }

        manholeMapper.deleteById(id);
        log.info("删除井盖: {}", manhole.getManholeId());
        return Result.success();
    }

    @Override
    public Result<ManholeVO> getById(Long id) {
        Manhole manhole = manholeMapper.selectById(id);
        if (manhole == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "井盖不存在");
        }
        return Result.success(convertToVO(manhole));
    }

    @Override
    public Result<PageResult<ManholeVO>> pageQuery(ManholeQueryRequest request) {
        LambdaQueryWrapper<Manhole> wrapper = buildQueryWrapper(request);

        // 按创建时间倒序
        wrapper.orderByDesc(Manhole::getCreateTime);

        Page<Manhole> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Manhole> resultPage = manholeMapper.selectPage(page, wrapper);

        List<ManholeVO> list = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                list
        ));
    }

    @Override
    public Result<List<ManholeVO>> getAll() {
        List<Manhole> list = manholeMapper.selectList(
                new LambdaQueryWrapper<Manhole>()
                        .orderByDesc(Manhole::getCreateTime)
        );
        return Result.success(list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    @Override
    public Result<ManholeVO> getByManholeId(String manholeId) {
        Manhole manhole = manholeMapper.selectOne(
                new LambdaQueryWrapper<Manhole>()
                        .eq(Manhole::getManholeId, manholeId)
        );
        if (manhole == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "井盖不存在");
        }
        return Result.success(convertToVO(manhole));
    }

    // ==================== 地图相关接口实现 ====================

    @Override
    public Result<List<ManholeMapVO>> queryForMap(ManholeMapQueryRequest request) {
        LambdaQueryWrapper<Manhole> wrapper = buildMapQueryWrapper(request);

        // 限制返回数量
        if (request.getMaxResults() != null && request.getMaxResults() > 0) {
            wrapper.last("LIMIT " + request.getMaxResults());
        }

        List<Manhole> list = manholeMapper.selectList(wrapper);

        List<ManholeMapVO> voList = list.stream()
                .map(this::convertToMapVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<List<ManholeClusterVO>> getClusterData(ManholeMapQueryRequest request) {
        // 获取范围内的所有井盖
        LambdaQueryWrapper<Manhole> wrapper = buildMapQueryWrapper(request);
        List<Manhole> allManholes = manholeMapper.selectList(wrapper);

        if (allManholes.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        // 计算聚合
        int gridSize = request.getGridSize() != null ? request.getGridSize() : 60;
        List<ManholeClusterVO> clusters = calculateClusters(allManholes, gridSize, request);

        return Result.success(clusters);
    }

    @Override
    public Result<ManholeMapStatsVO> getMapStats(ManholeMapQueryRequest request) {
        LambdaQueryWrapper<Manhole> wrapper = buildMapQueryWrapper(request);
        List<Manhole> list = manholeMapper.selectList(wrapper);

        long totalCount = list.size();
        // 状态统计与H2 Schema保持一致: 0-正常, 1-损坏, 2-维修中
        long normalCount = list.stream().filter(m -> m.getStatus() != null && m.getStatus() == 0).count();
        long damagedCount = list.stream().filter(m -> m.getStatus() != null && m.getStatus() == 1).count();
        long repairingCount = list.stream().filter(m -> m.getStatus() != null && m.getStatus() == 2).count();
        long defectCount = list.stream().filter(m -> m.getDefectCount() != null && m.getDefectCount() > 0).count();

        // 类型统计
        Map<Integer, Long> typeCountMap = list.stream()
                .filter(m -> m.getManholeType() != null)
                .collect(Collectors.groupingBy(Manhole::getManholeType, Collectors.counting()));

        List<ManholeMapStatsVO.TypeStat> typeDistribution = typeCountMap.entrySet().stream()
                .map(e -> ManholeMapStatsVO.TypeStat.builder()
                        .type(e.getKey())
                        .name(MANHOLE_TYPE_MAP.getOrDefault(e.getKey(), "未知"))
                        .count(e.getValue())
                        .percentage(totalCount > 0 ? round((double) e.getValue() / totalCount * 100, 2) : 0.0)
                        .build())
                .sorted(Comparator.comparing(ManholeMapStatsVO.TypeStat::getCount).reversed())
                .collect(Collectors.toList());

        Map<String, Long> typeStats = typeCountMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> MANHOLE_TYPE_MAP.getOrDefault(e.getKey(), "未知"),
                        Map.Entry::getValue
                ));

        // 状态统计 (与H2 Schema保持一致: 0-正常, 1-损坏, 2-维修中)
        List<ManholeMapStatsVO.StatusStat> statusDistribution = Arrays.asList(
                ManholeMapStatsVO.StatusStat.builder()
                        .status(0).name("正常").count(normalCount)
                        .percentage(totalCount > 0 ? round((double) normalCount / totalCount * 100, 2) : 0.0)
                        .build(),
                ManholeMapStatsVO.StatusStat.builder()
                        .status(1).name("损坏").count(damagedCount)
                        .percentage(totalCount > 0 ? round((double) damagedCount / totalCount * 100, 2) : 0.0)
                        .build(),
                ManholeMapStatsVO.StatusStat.builder()
                        .status(2).name("维修中").count(repairingCount)
                        .percentage(totalCount > 0 ? round((double) repairingCount / totalCount * 100, 2) : 0.0)
                        .build()
        );

        // 城市统计
        Map<String, Long> cityStats = list.stream()
                .filter(m -> StringUtils.hasText(m.getCity()))
                .collect(Collectors.groupingBy(Manhole::getCity, Collectors.counting()));

        // 区县统计
        Map<String, Long> districtStats = list.stream()
                .filter(m -> StringUtils.hasText(m.getDistrict()))
                .collect(Collectors.groupingBy(Manhole::getDistrict, Collectors.counting()));

        ManholeMapStatsVO stats = ManholeMapStatsVO.builder()
                .totalCount(totalCount)
                .normalCount(normalCount)
                .damagedCount(damagedCount)
                .repairingCount(repairingCount)
                .defectCount(defectCount)
                .typeStats(typeStats)
                .cityStats(cityStats)
                .districtStats(districtStats)
                .typeDistribution(typeDistribution)
                .statusDistribution(statusDistribution)
                .build();

        return Result.success(stats);
    }

    @Override
    public Result<List<ManholeMapVO>> getNearbyManholes(Double longitude, Double latitude, Integer radius) {
        if (longitude == null || latitude == null || radius == null) {
            return Result.success(Collections.emptyList());
        }

        // 简化的附近查询：使用范围查询
        double deltaLng = radius / 111320.0 / Math.cos(Math.toRadians(latitude));
        double deltaLat = radius / 110540.0;

        LambdaQueryWrapper<Manhole> wrapper = new LambdaQueryWrapper<Manhole>()
                .ge(Manhole::getLongitude, longitude - deltaLng)
                .le(Manhole::getLongitude, longitude + deltaLng)
                .ge(Manhole::getLatitude, latitude - deltaLat)
                .le(Manhole::getLatitude, latitude + deltaLat);

        List<Manhole> list = manholeMapper.selectList(wrapper);

        // 计算实际距离并排序
        List<ManholeMapVO> result = list.stream()
                .map(m -> {
                    ManholeMapVO vo = convertToMapVO(m);
                    double distance = calculateDistance(longitude, latitude, m.getLongitude(), m.getLatitude());
                    vo.setSafetyScore(distance); // 临时借用字段存储距离
                    return vo;
                })
                .filter(vo -> vo.getSafetyScore() <= radius)
                .sorted(Comparator.comparing(ManholeMapVO::getSafetyScore))
                .collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<List<String>> getAllCities() {
        // 查询所有城市（不分组，查询后自己处理）
        List<Manhole> list = manholeMapper.selectList(
                new LambdaQueryWrapper<Manhole>()
                        .isNotNull(Manhole::getCity)
                        .ne(Manhole::getCity, "")
        );

        List<String> cities = list.stream()
                .map(Manhole::getCity)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return Result.success(cities);
    }

    @Override
    public Result<List<String>> getDistrictsByCity(String city) {
        if (!StringUtils.hasText(city)) {
            return Result.success(Collections.emptyList());
        }

        List<Manhole> list = manholeMapper.selectList(
                new LambdaQueryWrapper<Manhole>()
                        .eq(Manhole::getCity, city)
                        .isNotNull(Manhole::getDistrict)
                        .ne(Manhole::getDistrict, "")
        );

        List<String> districts = list.stream()
                .map(Manhole::getDistrict)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return Result.success(districts);
    }

    // ==================== 私有方法 ====================

    /**
     * 复制非null属性
     */
    private void copyNonNullProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    /**
     * 获取对象中为null的属性名数组
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName -> wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Manhole> buildQueryWrapper(ManholeQueryRequest request) {
        LambdaQueryWrapper<Manhole> wrapper = new LambdaQueryWrapper<>();

        if (request.getManholeId() != null && !request.getManholeId().isEmpty()) {
            wrapper.eq(Manhole::getManholeId, request.getManholeId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Manhole::getManholeId, request.getKeyword())
                    .or()
                    .like(Manhole::getAddress, request.getKeyword()));
        }
        if (request.getStatus() != null) {
            wrapper.eq(Manhole::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getCity())) {
            wrapper.eq(Manhole::getCity, request.getCity());
        }
        if (StringUtils.hasText(request.getDistrict())) {
            wrapper.eq(Manhole::getDistrict, request.getDistrict());
        }
        if (request.getManholeType() != null) {
            wrapper.eq(Manhole::getManholeType, request.getManholeType());
        }
        if (request.getMinLongitude() != null) {
            wrapper.ge(Manhole::getLongitude, request.getMinLongitude());
        }
        if (request.getMaxLongitude() != null) {
            wrapper.le(Manhole::getLongitude, request.getMaxLongitude());
        }
        if (request.getMinLatitude() != null) {
            wrapper.ge(Manhole::getLatitude, request.getMinLatitude());
        }
        if (request.getMaxLatitude() != null) {
            wrapper.le(Manhole::getLatitude, request.getMaxLatitude());
        }
        if (request.getHasDefect() != null) {
            if (request.getHasDefect() == 1) {
                wrapper.gt(Manhole::getDefectCount, 0);
            } else {
                wrapper.eq(Manhole::getDefectCount, 0).or().isNull(Manhole::getDefectCount);
            }
        }

        return wrapper;
    }

    /**
     * 构建地图查询条件
     */
    private LambdaQueryWrapper<Manhole> buildMapQueryWrapper(ManholeMapQueryRequest request) {
        LambdaQueryWrapper<Manhole> wrapper = new LambdaQueryWrapper<>();

        // 地图范围查询
        if (request.getMinLongitude() != null) {
            wrapper.ge(Manhole::getLongitude, request.getMinLongitude());
        }
        if (request.getMaxLongitude() != null) {
            wrapper.le(Manhole::getLongitude, request.getMaxLongitude());
        }
        if (request.getMinLatitude() != null) {
            wrapper.ge(Manhole::getLatitude, request.getMinLatitude());
        }
        if (request.getMaxLatitude() != null) {
            wrapper.le(Manhole::getLatitude, request.getMaxLatitude());
        }

        // 其他条件
        if (request.getStatus() != null) {
            wrapper.eq(Manhole::getStatus, request.getStatus());
        }
        if (StringUtils.hasText(request.getCity())) {
            wrapper.eq(Manhole::getCity, request.getCity());
        }
        if (StringUtils.hasText(request.getDistrict())) {
            wrapper.eq(Manhole::getDistrict, request.getDistrict());
        }
        if (request.getManholeType() != null) {
            wrapper.eq(Manhole::getManholeType, request.getManholeType());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Manhole::getManholeId, request.getKeyword())
                    .or()
                    .like(Manhole::getAddress, request.getKeyword()));
        }
        if (request.getManholeId() != null) {
            wrapper.eq(Manhole::getManholeId, request.getManholeId());
        }
        if (Boolean.TRUE.equals(request.getOnlyDefect()) || (request.getHasDefect() != null && request.getHasDefect() == 1)) {
            wrapper.gt(Manhole::getDefectCount, 0);
        }

        return wrapper;
    }

    /**
     * 计算聚合
     */
    private List<ManholeClusterVO> calculateClusters(List<Manhole> manholes, int gridSize, ManholeMapQueryRequest request) {
        // 计算网格大小（经纬度）
        double centerLat = (request.getMinLatitude() + request.getMaxLatitude()) / 2;
        double gridLat = gridSize / 110540.0; // 米转纬度
        double gridLng = gridSize / (111320.0 * Math.cos(Math.toRadians(centerLat))); // 米转经度

        // 按网格分组
        Map<String, List<Manhole>> gridMap = new HashMap<>();

        for (Manhole m : manholes) {
            if (m.getLatitude() == null || m.getLongitude() == null) continue;

            int gridX = (int) Math.floor(m.getLongitude() / gridLng);
            int gridY = (int) Math.floor(m.getLatitude() / gridLat);
            String gridKey = gridX + "_" + gridY;

            gridMap.computeIfAbsent(gridKey, k -> new ArrayList<>()).add(m);
        }

        // 转换为聚合VO
        List<ManholeClusterVO> clusters = new ArrayList<>();
        for (Map.Entry<String, List<Manhole>> entry : gridMap.entrySet()) {
            List<Manhole> gridManholes = entry.getValue();
            if (gridManholes.isEmpty()) continue;

            // 计算中心点和边界
            double avgLng = gridManholes.stream().mapToDouble(Manhole::getLongitude).average().orElse(0);
            double avgLat = gridManholes.stream().mapToDouble(Manhole::getLatitude).average().orElse(0);
            double minLng = gridManholes.stream().mapToDouble(Manhole::getLongitude).min().orElse(0);
            double maxLng = gridManholes.stream().mapToDouble(Manhole::getLongitude).max().orElse(0);
            double minLat = gridManholes.stream().mapToDouble(Manhole::getLatitude).min().orElse(0);
            double maxLat = gridManholes.stream().mapToDouble(Manhole::getLatitude).max().orElse(0);

            ManholeClusterVO cluster = ManholeClusterVO.builder()
                    .clusterId(entry.getKey())
                    .longitude(avgLng)
                    .latitude(avgLat)
                    .count(gridManholes.size())
                    .swLng(minLng)
                    .swLat(minLat)
                    .neLng(maxLng)
                    .neLat(maxLat)
                    .manholes(gridManholes.stream().map(this::convertToMapVO).collect(Collectors.toList()))
                    .build();

            clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378137; // 地球半径
        return s;
    }

    /**
     * 四舍五入
     */
    private double round(double value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * 转换为VO
     */
    private ManholeVO convertToVO(Manhole manhole) {
        ManholeVO vo = new ManholeVO();
        BeanUtils.copyProperties(manhole, vo);

        // 设置状态文本
        if (manhole.getStatus() != null) {
            vo.setStatusText(STATUS_MAP.get(manhole.getStatus()));
        }

        // 设置类型文本
        if (manhole.getManholeType() != null) {
            vo.setManholeTypeText(MANHOLE_TYPE_MAP.get(manhole.getManholeType()));
        }

        // 查询最新检测记录，获取检测图片
        if (manhole.getManholeId() != null) {
            Detection latestDetection = detectionMapper.selectOne(
                    new LambdaQueryWrapper<Detection>()
                            .eq(Detection::getManholeId, manhole.getManholeId())
                            .eq(Detection::getDeleted, 0)
                            .orderByDesc(Detection::getDetectionTime)
                            .last("LIMIT 1")
            );
            if (latestDetection != null) {
                vo.setDetectionId(latestDetection.getId());
                vo.setDetectionOriginalImgUrl(latestDetection.getOriginalImgUrl());
                vo.setDetectionResultImgUrl(latestDetection.getResultImgUrl());
                vo.setHasDefect(latestDetection.getHasDefect());
                vo.setDefectTypes(latestDetection.getDefectTypes());
                vo.setPrimaryDefectType(latestDetection.getPrimaryDefectType());
                vo.setPrimaryConfidence(latestDetection.getPrimaryConfidence());
            }
        }

        return vo;
    }

    /**
     * 转换为地图VO
     */
    private ManholeMapVO convertToMapVO(Manhole manhole) {
        ManholeMapVO vo = new ManholeMapVO();
        BeanUtils.copyProperties(manhole, vo);

        // 设置状态文本
        if (manhole.getStatus() != null) {
            vo.setStatusText(STATUS_MAP.get(manhole.getStatus()));
        }

        // 设置类型文本
        if (manhole.getManholeType() != null) {
            vo.setManholeTypeText(MANHOLE_TYPE_MAP.get(manhole.getManholeType()));
        }

        // 设置检测相关信息
        vo.setHasLatestDetection(manhole.getLastDetTime() != null);

        return vo;
    }
}
