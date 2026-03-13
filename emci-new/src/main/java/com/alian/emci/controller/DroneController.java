package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.dto.drone.DroneUpdateRequest;
import com.alian.emci.service.DroneService;
import com.alian.emci.vo.drone.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 无人机管理控制器（极简版）
 * 一个无人机对应一个固定检测任务
 */
@RestController
@RequestMapping("/drone")
@RequiredArgsConstructor
@Tag(name = "无人机管理", description = "无人机部署、路径规划、巡检控制")
public class DroneController {

    private final DroneService droneService;

    @GetMapping("/list")
    @Operation(summary = "获取无人机列表（含任务信息）")
    public Result<List<DroneVO>> getDroneList() {
        return droneService.getDroneList();
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用无人机列表")
    public Result<List<DroneVO>> getAvailableDrones() {
        return droneService.getAvailableDrones();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取无人机详情")
    public Result<DroneVO> getDroneDetail(
            @Parameter(description = "无人机ID") @PathVariable Long id) {
        return droneService.getDroneDetail(id);
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署无人机：设置检测区域并规划路径")
    public Result<DroneVO> deployDrone(@Valid @RequestBody DroneDeployRequest request) {
        return droneService.deployDrone(request);
    }

    @PostMapping("/preview")
    @Operation(summary = "预览路径规划")
    public Result<PathPlanResultVO> previewPathPlan(@Valid @RequestBody DroneDeployRequest request) {
        return droneService.previewPathPlan(request);
    }

    @PostMapping("/{droneId}/start")
    @Operation(summary = "开始巡检")
    public Result<Void> startInspection(
            @Parameter(description = "无人机ID") @PathVariable Long droneId) {
        return droneService.startInspection(droneId);
    }

    @PostMapping("/{droneId}/stop")
    @Operation(summary = "结束巡检")
    public Result<Void> stopInspection(
            @Parameter(description = "无人机ID") @PathVariable Long droneId) {
        return droneService.stopInspection(droneId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新无人机信息")
    public Result<DroneVO> updateDrone(
            @Parameter(description = "无人机ID") @PathVariable Long id,
            @RequestBody DroneUpdateRequest request) {
        return droneService.updateDrone(id, request);
    }

    @GetMapping("/tasks")
    @Operation(summary = "获取所有检测任务列表（直接查询drone_task表）")
    public Result<List<DroneTaskVO>> getAllTasks() {
        return droneService.getAllTasks();
    }
}
