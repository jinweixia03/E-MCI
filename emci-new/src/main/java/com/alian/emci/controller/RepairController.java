package com.alian.emci.controller;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.repair.RepairCreateRequest;
import com.alian.emci.dto.repair.RepairUpdateRequest;
import com.alian.emci.service.RepairService;
import com.alian.emci.vo.repair.RepairVO;
import com.alian.emci.entity.User;
import com.alian.emci.mapper.UserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 维修记录控制器
 */
@Slf4j
@Tag(name = "维修记录", description = "维修记录管理相关接口")
@RestController
@RequestMapping("/repair")
@RequiredArgsConstructor
public class RepairController {

    private final RepairService repairService;
    private final UserMapper userMapper;

    @Operation(summary = "创建维修记录", description = "创建新的维修任务")
    @PostMapping
    public Result<RepairVO> create(@RequestBody RepairCreateRequest request) {
        return repairService.create(request);
    }

    @Operation(summary = "更新维修记录", description = "更新维修记录信息")
    @PutMapping("/{id}")
    public Result<RepairVO> update(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @RequestBody RepairUpdateRequest request) {
        return repairService.update(id, request);
    }

    @Operation(summary = "删除维修记录", description = "根据ID删除维修记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairService.delete(id);
    }

    @Operation(summary = "获取维修记录详情", description = "根据ID获取维修记录详情（包含检测图片）")
    @GetMapping("/{id}")
    public Result<RepairVO> getById(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairService.getById(id);
    }

    @Operation(summary = "分页查询维修记录", description = "分页查询维修记录列表")
    @GetMapping("/page")
    public Result<PageResult<RepairVO>> pageQuery(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "井盖编号") @RequestParam(required = false) String manholeId,
            @Parameter(description = "状态：0-待维修,1-维修中,2-已完成") @RequestParam(required = false) Integer status,
            @Parameter(description = "关联检测记录ID") @RequestParam(required = false) Long detectionId) {
        return repairService.pageQuery(pageNum, pageSize, manholeId, status, detectionId);
    }

    @Operation(summary = "获取井盖的维修记录", description = "根据井盖编号获取该井盖的所有维修记录（包含检测图片）")
    @GetMapping("/manhole/{manholeId}")
    public Result<List<RepairVO>> getByManholeId(
            @Parameter(description = "井盖编号") @PathVariable String manholeId) {
        return repairService.getByManholeId(manholeId);
    }

    @Operation(summary = "根据检测记录获取维修记录", description = "根据检测记录ID获取关联的维修记录")
    @GetMapping("/detection/{detectionId}")
    public Result<RepairVO> getByDetectionId(
            @Parameter(description = "检测记录ID") @PathVariable Long detectionId) {
        return repairService.getByDetectionId(detectionId);
    }

    @Operation(summary = "开始维修", description = "标记维修任务为进行中")
    @PostMapping("/{id}/start")
    public Result<RepairVO> startRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairService.startRepair(id);
    }

    @Operation(summary = "完成维修", description = "维修人员提交完成，等待确认")
    @PostMapping("/{id}/complete")
    public Result<RepairVO> completeRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修后图片URL") @RequestParam(required = false) String afterImg,
            @Parameter(description = "备注") @RequestParam(required = false) String remark) {
        return repairService.completeRepair(id, afterImg, remark);
    }

    @Operation(summary = "确认维修", description = "管理员确认维修任务已完成")
    @PostMapping("/{id}/confirm")
    public Result<RepairVO> confirmRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id) {
        return repairService.confirmRepair(id);
    }

    @Operation(summary = "分配维修任务", description = "将维修任务分配给指定人员")
    @PostMapping("/{id}/assign")
    public Result<RepairVO> assignRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "维修人员ID") @RequestParam Long repairUserId) {
        return repairService.assignRepair(id, repairUserId);
    }

    @Operation(summary = "根据状态查询维修记录", description = "根据状态获取维修记录列表")
    @GetMapping("/by-status/{status}")
    public Result<List<RepairVO>> getByStatus(
            @Parameter(description = "状态：0-待维修,1-维修中,2-已完成") @PathVariable Integer status) {
        return repairService.getByStatus(status);
    }

    @Operation(summary = "获取维修统计", description = "获取各状态维修任务数量统计")
    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        return repairService.getStats();
    }

    @Operation(summary = "重新分配维修任务", description = "将维修任务重新分配给指定人员")
    @PostMapping("/{id}/reassign")
    public Result<RepairVO> reassignRepair(
            @Parameter(description = "维修记录ID") @PathVariable Long id,
            @Parameter(description = "新的维修人员ID") @RequestParam Long repairUserId) {
        return repairService.reassignRepair(id, repairUserId);
    }

    @Operation(summary = "获取可分配的维修人员列表", description = "获取所有type=2的维修人员")
    @GetMapping("/users")
    public Result<List<User>> getRepairUsers() {
        List<User> users = userMapper.selectList(Wrappers.<User>lambdaQuery()
                .eq(User::getType, 2)
                .eq(User::getDeleted, 0)
                .select(User::getId, User::getUsername));
        return Result.success(users);
    }

    // ==================== 维修员专用接口 ====================

    @Operation(summary = "维修员分页查询自己的维修记录", description = "维修员只能查看分配给自己的维修任务")
    @GetMapping("/my/page")
    public Result<PageResult<RepairVO>> pageQueryByRepairUser(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态：0-待维修,1-维修中,2-待确认,3-已完成") @RequestParam(required = false) Integer status,
            @Parameter(description = "关联检测记录ID") @RequestParam(required = false) Long detectionId,
            @Parameter(description = "当前维修员ID") @RequestParam Long repairUserId) {
        return repairService.pageQueryByRepairUser(repairUserId, pageNum, pageSize, status, detectionId);
    }

    @Operation(summary = "获取维修员的维修统计", description = "获取当前维修员各状态任务数量")
    @GetMapping("/my/stats")
    public Result<Map<String, Long>> getStatsByRepairUser(
            @Parameter(description = "当前维修员ID") @RequestParam Long repairUserId) {
        return repairService.getStatsByRepairUser(repairUserId);
    }
}
