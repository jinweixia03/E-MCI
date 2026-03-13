package com.alian.emci.service.impl;

import com.alian.emci.common.Result;
import com.alian.emci.drone.PathPlanner;
import com.alian.emci.entity.Drone;
import com.alian.emci.entity.DroneTask;
import com.alian.emci.entity.Manhole;
import com.alian.emci.mapper.DroneMapper;
import com.alian.emci.mapper.DroneTaskMapper;
import com.alian.emci.mapper.ManholeMapper;
import com.alian.emci.service.DroneService;
import com.alian.emci.vo.drone.*;
import com.alian.emci.dto.drone.DroneUpdateRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 无人机服务实现类（极简版）
 * 一个无人机对应一个固定检测任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DroneServiceImpl implements DroneService {

    private final DroneMapper droneMapper;
    private final DroneTaskMapper droneTaskMapper;
    private final ManholeMapper manholeMapper;
    private final PathPlanner pathPlanner;
    private final ObjectMapper objectMapper;

    @Override
    public Result<List<DroneVO>> getDroneList() {
        List<Drone> drones = droneMapper.selectList(
                new LambdaQueryWrapper<Drone>()
                        .eq(Drone::getDeleted, 0)
                        .orderByDesc(Drone::getCreateTime)
        );
        return Result.success(drones.stream()
                .map(this::convertToDroneVO)
                .collect(Collectors.toList()));
    }

    @Override
    public Result<List<DroneVO>> getAvailableDrones() {
        List<Drone> drones = droneMapper.selectList(
                new LambdaQueryWrapper<Drone>()
                        .eq(Drone::getDeleted, 0)
                        .eq(Drone::getStatus, 0)
                        .ge(Drone::getBattery, 30)
                        .orderByDesc(Drone::getBattery)
        );
        return Result.success(drones.stream()
                .map(this::convertToDroneVO)
                .collect(Collectors.toList()));
    }

    @Override
    public Result<DroneVO> getDroneDetail(Long id) {
        Drone drone = droneMapper.selectById(id);
        if (drone == null || drone.getDeleted() == 1) {
            return Result.error("无人机不存在");
        }
        return Result.success(convertToDroneVO(drone));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DroneVO> deployDrone(DroneDeployRequest request) {
        // 验证无人机
        Drone drone = droneMapper.selectById(request.getDroneId());
        if (drone == null || drone.getDeleted() == 1) {
            return Result.error("无人机不存在");
        }

        // 查询区域内井盖
        List<Manhole> manholes = findManholesInRadius(
                request.getCenterLat(), request.getCenterLng(), request.getRadius());
        if (manholes.isEmpty()) {
            return Result.error("该区域未找到井盖");
        }

        // 路径规划：优先使用前端传入的预览路径，否则重新计算
        List<PathPointVO> pathVOs;
        if (request.getPathPoints() != null && !request.getPathPoints().isEmpty()) {
            // 使用预览时缓存的路径
            pathVOs = request.getPathPoints();
            log.info("使用预览缓存的路径，无人机ID: {}", request.getDroneId());
        } else {
            // 重新计算路径（A*算法）
            List<PathPlanner.PathPoint> pathPoints = pathPlanner.planOptimalPath(
                    request.getCenterLat(), request.getCenterLng(), manholes);
            pathVOs = pathPoints.stream().map(p -> {
                PathPointVO vo = new PathPointVO();
                vo.setManholeId(p.getManholeId());
                vo.setManholeNo(p.getManholeNo());
                vo.setLat(p.getLat());
                vo.setLng(p.getLng());
                vo.setSequence(p.getSequence());
                vo.setDistanceFromStart(p.getDistanceFromStart());
                return vo;
            }).collect(Collectors.toList());
            log.info("重新计算路径，无人机ID: {}", request.getDroneId());
        }

        // 更新无人机位置
        drone.setLatitude(request.getCenterLat());
        drone.setLongitude(request.getCenterLng());
        drone.setRadius(request.getRadius());
        droneMapper.updateById(drone);

        // 查询是否已有任务（一对一）
        DroneTask task = droneTaskMapper.selectOne(
                new LambdaQueryWrapper<DroneTask>()
                        .eq(DroneTask::getDroneId, request.getDroneId())
                        .last("LIMIT 1")
        );

        if (task == null) {
            // 创建新任务
            task = new DroneTask();
            task.setDroneId(request.getDroneId());
        }

        // 更新任务信息
        task.setCenterLat(request.getCenterLat());
        task.setCenterLng(request.getCenterLng());
        task.setRadius(request.getRadius());
        task.setManholeCount(manholes.size());
        task.setEstimatedTime(pathVOs.size() * 2);

        try {
            task.setPathPoints(objectMapper.writeValueAsString(pathVOs));
        } catch (JsonProcessingException e) {
            log.error("路径点序列化失败", e);
            return Result.error("保存路径失败");
        }

        if (task.getId() == null) {
            droneTaskMapper.insert(task);
        } else {
            droneTaskMapper.updateById(task);
        }

        return Result.success(convertToDroneVO(drone));
    }

    @Override
    public Result<PathPlanResultVO> previewPathPlan(DroneDeployRequest request) {
        log.info("预览路径规划请求: droneId={}, centerLat={}, centerLng={}, radius={}",
                request.getDroneId(), request.getCenterLat(), request.getCenterLng(), request.getRadius());

        List<Manhole> manholes = findManholesInRadius(
                request.getCenterLat(), request.getCenterLng(), request.getRadius());

        log.info("找到 {} 个井盖在半径 {} 米内", manholes.size(), request.getRadius());

        if (manholes.isEmpty()) {
            return Result.error("该区域未找到井盖");
        }

        List<PathPlanner.PathPoint> pathPoints = pathPlanner.planOptimalPath(
                request.getCenterLat(), request.getCenterLng(), manholes);

        log.info("A*算法规划完成，路径点数量: {}", pathPoints.size());

        PathPlanResultVO result = new PathPlanResultVO();
        result.setCenterLat(request.getCenterLat());
        result.setCenterLng(request.getCenterLng());
        result.setRadius(request.getRadius());
        result.setManholeCount(manholes.size());
        result.setPathPoints(convertToPathPointVOs(pathPoints));
        result.setEstimatedDistance(pathPoints.isEmpty() ? 0 :
                pathPoints.get(pathPoints.size() - 1).getDistanceFromStart());
        result.setEstimatedTime(pathPoints.size() * 2);
        result.setAlgorithmType("A_STAR");

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> startInspection(Long droneId) {
        Drone drone = droneMapper.selectById(droneId);
        if (drone == null || drone.getDeleted() == 1) {
            return Result.error("无人机不存在");
        }
        if (drone.getStatus() != 0) {
            return Result.error("无人机当前状态不可用");
        }

        // 检查是否有任务
        DroneTask task = droneTaskMapper.selectOne(
                new LambdaQueryWrapper<DroneTask>()
                        .eq(DroneTask::getDroneId, droneId)
                        .last("LIMIT 1")
        );
        if (task == null) {
            return Result.error("请先部署无人机设置检测区域");
        }

        drone.setStatus(1); // 巡检中
        droneMapper.updateById(drone);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> stopInspection(Long droneId) {
        Drone drone = droneMapper.selectById(droneId);
        if (drone == null || drone.getDeleted() == 1) {
            return Result.error("无人机不存在");
        }

        drone.setStatus(0); // 闲置
        droneMapper.updateById(drone);

        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DroneVO> updateDrone(Long id, DroneUpdateRequest request) {
        Drone drone = droneMapper.selectById(id);
        if (drone == null || drone.getDeleted() == 1) {
            return Result.error("无人机不存在");
        }

        if (request.getLatitude() != null) {
            drone.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        }
        if (request.getLongitude() != null) {
            drone.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        }
        if (request.getRadius() != null) {
            drone.setRadius(request.getRadius());
        }
        if (request.getStatus() != null) {
            drone.setStatus(request.getStatus());
        }

        droneMapper.updateById(drone);
        return Result.success(convertToDroneVO(drone));
    }

    @Override
    public Result<List<DroneTaskVO>> getAllTasks() {
        // 直接查询drone_task表获取所有任务
        List<DroneTask> tasks = droneTaskMapper.selectList(
                new LambdaQueryWrapper<DroneTask>()
                        .orderByDesc(DroneTask::getCreateTime)
        );

        List<DroneTaskVO> voList = tasks.stream()
                .map(this::convertToDroneTaskVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    // ==================== 私有方法 ====================

    private List<Manhole> findManholesInRadius(BigDecimal centerLat, BigDecimal centerLng, Integer radius) {
        double latDelta = (double) radius / 111000.0;
        double lngDelta = (double) radius / (111000.0 * Math.cos(Math.toRadians(centerLat.doubleValue())));

        BigDecimal minLat = centerLat.subtract(BigDecimal.valueOf(latDelta));
        BigDecimal maxLat = centerLat.add(BigDecimal.valueOf(latDelta));
        BigDecimal minLng = centerLng.subtract(BigDecimal.valueOf(lngDelta));
        BigDecimal maxLng = centerLng.add(BigDecimal.valueOf(lngDelta));

        log.info("查询范围: lat[{}, {}], lng[{}, {}]", minLat, maxLat, minLng, maxLng);

        List<Manhole> candidates = manholeMapper.selectList(
                new LambdaQueryWrapper<Manhole>()
                        .between(Manhole::getLatitude, minLat, maxLat)
                        .between(Manhole::getLongitude, minLng, maxLng)
                        .eq(Manhole::getDeleted, 0)
        );

        log.info("初步查询到 {} 个候选井盖", candidates.size());

        return candidates.stream()
                .filter(m -> pathPlanner.calculateDistance(
                        centerLat, centerLng,
                        BigDecimal.valueOf(m.getLatitude()),
                        BigDecimal.valueOf(m.getLongitude())) <= radius)
                .collect(Collectors.toList());
    }

    private DroneVO convertToDroneVO(Drone drone) {
        DroneVO vo = new DroneVO();
        BeanUtils.copyProperties(drone, vo);
        vo.setStatusText(getDroneStatusText(drone.getStatus()));

        // 查询关联的任务（一对一）
        DroneTask task = droneTaskMapper.selectOne(
                new LambdaQueryWrapper<DroneTask>()
                        .eq(DroneTask::getDroneId, drone.getId())
                        .last("LIMIT 1")
        );

        if (task != null) {
            vo.setCenterLat(task.getCenterLat());
            vo.setCenterLng(task.getCenterLng());
            vo.setTaskRadius(task.getRadius());
            vo.setManholeCount(task.getManholeCount());
            vo.setEstimatedTime(task.getEstimatedTime());

            if (task.getPathPoints() != null) {
                try {
                    List<PathPointVO> pathPoints = objectMapper.readValue(
                            task.getPathPoints(), new TypeReference<List<PathPointVO>>() {});
                    vo.setPathPoints(pathPoints);
                } catch (JsonProcessingException e) {
                    log.error("路径点解析失败", e);
                }
            }
        }

        return vo;
    }

    private List<PathPointVO> convertToPathPointVOs(List<PathPlanner.PathPoint> pathPoints) {
        return pathPoints.stream().map(p -> {
            PathPointVO vo = new PathPointVO();
            vo.setManholeId(p.getManholeId());
            vo.setManholeNo(p.getManholeNo());
            vo.setLat(p.getLat());
            vo.setLng(p.getLng());
            vo.setSequence(p.getSequence());
            vo.setDistanceFromStart(p.getDistanceFromStart());
            return vo;
        }).collect(Collectors.toList());
    }

    private String getDroneStatusText(Integer status) {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "闲置");
        map.put(1, "巡检中");
        map.put(2, "充电中");
        map.put(3, "维修中");
        return map.getOrDefault(status, "未知");
    }

    private DroneTaskVO convertToDroneTaskVO(DroneTask task) {
        DroneTaskVO vo = new DroneTaskVO();
        vo.setId(task.getId());
        vo.setDroneId(task.getDroneId());
        vo.setCenterLat(task.getCenterLat());
        vo.setCenterLng(task.getCenterLng());
        vo.setRadius(task.getRadius());
        vo.setManholeCount(task.getManholeCount());
        vo.setEstimatedTime(task.getEstimatedTime());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        // 查询无人机名称
        Drone drone = droneMapper.selectById(task.getDroneId());
        if (drone != null) {
            vo.setDroneName(drone.getName());
        }

        // 解析路径点
        if (task.getPathPoints() != null) {
            try {
                List<PathPointVO> pathPoints = objectMapper.readValue(
                        task.getPathPoints(), new TypeReference<List<PathPointVO>>() {});
                vo.setPathPoints(pathPoints);
            } catch (JsonProcessingException e) {
                log.error("路径点解析失败", e);
            }
        }

        return vo;
    }
}
