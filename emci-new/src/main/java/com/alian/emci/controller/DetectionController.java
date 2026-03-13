package com.alian.emci.controller;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.detection.DetectionCreateRequest;
import com.alian.emci.dto.detection.DetectionQueryRequest;
import com.alian.emci.service.DetectionService;
import com.alian.emci.vo.detection.DetectionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检测记录控制器
 */
@Tag(name = "检测管理", description = "井盖检测记录相关接口")
@RestController
@RequestMapping("/detection")
@RequiredArgsConstructor
public class DetectionController {

    private final DetectionService detectionService;

    @Operation(summary = "创建检测记录", description = "创建新的检测记录")
    @PostMapping
    public Result<DetectionVO> create(@Valid @RequestBody DetectionCreateRequest request) {
        return detectionService.create(request);
    }

    @Operation(summary = "删除检测记录", description = "根据ID删除检测记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "检测记录ID") @PathVariable Long id) {
        return detectionService.delete(id);
    }

    @Operation(summary = "获取检测记录详情", description = "根据ID获取检测记录详细信息")
    @GetMapping("/{id}")
    public Result<DetectionVO> getById(@Parameter(description = "检测记录ID") @PathVariable Long id) {
        return detectionService.getById(id);
    }

    @Operation(summary = "分页查询检测记录", description = "分页查询检测记录列表")
    @GetMapping("/page")
    public Result<PageResult<DetectionVO>> pageQuery(@Valid DetectionQueryRequest request) {
        return detectionService.pageQuery(request);
    }

    @Operation(summary = "根据井盖编号查询", description = "根据井盖编号查询检测记录")
    @GetMapping("/by-manhole/{manholeId}")
    public Result<List<DetectionVO>> getByManholeId(
            @Parameter(description = "井盖编号") @PathVariable String manholeId) {
        return detectionService.getByManholeId(manholeId);
    }

    @Operation(summary = "获取待维修列表", description = "获取所有需要维修的检测记录")
    @GetMapping("/need-repair")
    public Result<List<DetectionVO>> getNeedRepaired() {
        return detectionService.getNeedRepaired();
    }

    @Operation(summary = "标记为已修复", description = "将检测记录标记为已修复")
    @PatchMapping("/{id}/repair")
    public Result<DetectionVO> markRepaired(@Parameter(description = "检测记录ID") @PathVariable Long id) {
        return detectionService.markRepaired(id);
    }
}
