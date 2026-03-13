package com.alian.emci.service;

import com.alian.emci.common.Result;
import com.alian.emci.dto.drone.DroneUpdateRequest;
import com.alian.emci.vo.drone.*;

import java.util.List;

/**
 * 无人机服务接口（极简版）
 * 一个无人机对应一个固定检测任务（一对一）
 */
public interface DroneService {

    /**
     * 获取所有无人机列表
     */
    Result<List<DroneVO>> getDroneList();

    /**
     * 获取可用的无人机列表
     */
    Result<List<DroneVO>> getAvailableDrones();

    /**
     * 获取无人机详情（含任务/路径信息）
     */
    Result<DroneVO> getDroneDetail(Long id);

    /**
     * 部署无人机：设置固定检测区域，规划路径（有则更新，无则创建）
     */
    Result<DroneVO> deployDrone(DroneDeployRequest request);

    /**
     * 预览路径规划
     */
    Result<PathPlanResultVO> previewPathPlan(DroneDeployRequest request);

    /**
     * 开始巡检（修改状态为巡检中）
     */
    Result<Void> startInspection(Long droneId);

    /**
     * 结束巡检（修改状态为闲置）
     */
    Result<Void> stopInspection(Long droneId);

    /**
     * 更新无人机信息
     */
    Result<DroneVO> updateDrone(Long id, DroneUpdateRequest request);

    /**
     * 获取所有检测任务列表（直接查询drone_task表）
     */
    Result<List<DroneTaskVO>> getAllTasks();
}
