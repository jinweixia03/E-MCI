package com.alian.emci.controller;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.manhole.ManholeCreateRequest;
import com.alian.emci.dto.manhole.ManholeMapQueryRequest;
import com.alian.emci.dto.manhole.ManholeQueryRequest;
import com.alian.emci.dto.manhole.ManholeUpdateRequest;
import com.alian.emci.service.ManholeService;
import com.alian.emci.vo.manhole.ManholeVO;
import com.alian.emci.vo.map.ManholeClusterVO;
import com.alian.emci.vo.map.ManholeMapStatsVO;
import com.alian.emci.vo.map.ManholeMapVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 井盖控制器
 */
@Tag(name = "井盖管理", description = "井盖信息管理相关接口")
@RestController
@RequestMapping("/manhole")
@RequiredArgsConstructor
public class ManholeController {

    private final ManholeService manholeService;

    @Operation(summary = "创建井盖", description = "创建新的井盖信息")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ManholeVO> create(@Valid @RequestBody ManholeCreateRequest request) {
        return manholeService.create(request);
    }

    @Operation(summary = "更新井盖", description = "根据ID更新井盖信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ManholeVO> update(
            @Parameter(description = "井盖ID") @PathVariable Long id,
            @Valid @RequestBody ManholeUpdateRequest request) {
        return manholeService.update(id, request);
    }

    @Operation(summary = "删除井盖", description = "根据ID删除井盖")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@Parameter(description = "井盖ID") @PathVariable Long id) {
        return manholeService.delete(id);
    }

    @Operation(summary = "获取井盖详情", description = "根据ID获取井盖详细信息")
    @GetMapping("/{id}")
    public Result<ManholeVO> getById(@Parameter(description = "井盖ID") @PathVariable Long id) {
        return manholeService.getById(id);
    }

    @Operation(summary = "分页查询井盖", description = "分页查询井盖列表")
    @GetMapping("/page")
    public Result<PageResult<ManholeVO>> pageQuery(@Valid ManholeQueryRequest request) {
        return manholeService.pageQuery(request);
    }

    @Operation(summary = "获取所有井盖", description = "获取所有井盖列表（不分页）")
    @GetMapping("/all")
    public Result<List<ManholeVO>> getAll() {
        return manholeService.getAll();
    }

    @Operation(summary = "根据编号查询", description = "根据井盖编号查询")
    @GetMapping("/by-code/{manholeId}")
    public Result<ManholeVO> getByManholeId(
            @Parameter(description = "井盖编号") @PathVariable String manholeId) {
        return manholeService.getByManholeId(manholeId);
    }

    // ==================== 地图相关接口 ====================

    @Operation(summary = "地图查询井盖", description = "根据地图范围查询井盖（用于地图展示）")
    @GetMapping("/map/query")
    public Result<List<ManholeMapVO>> queryForMap(ManholeMapQueryRequest request) {
        return manholeService.queryForMap(request);
    }

    @Operation(summary = "获取地图聚合数据", description = "获取地图聚合点数据")
    @GetMapping("/map/cluster")
    public Result<List<ManholeClusterVO>> getClusterData(ManholeMapQueryRequest request) {
        return manholeService.getClusterData(request);
    }

    @Operation(summary = "获取地图统计", description = "获取地图范围内的井盖统计信息")
    @GetMapping("/map/stats")
    public Result<ManholeMapStatsVO> getMapStats(ManholeMapQueryRequest request) {
        return manholeService.getMapStats(request);
    }

    @Operation(summary = "获取附近井盖", description = "获取指定位置附近的井盖")
    @GetMapping("/map/nearby")
    public Result<List<ManholeMapVO>> getNearbyManholes(
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "半径（米）") @RequestParam(defaultValue = "1000") Integer radius) {
        return manholeService.getNearbyManholes(longitude, latitude, radius);
    }

    @Operation(summary = "获取城市列表", description = "获取所有有井盖的城市列表")
    @GetMapping("/cities")
    public Result<List<String>> getAllCities() {
        return manholeService.getAllCities();
    }

    @Operation(summary = "获取区县列表", description = "获取指定城市下的区县列表")
    @GetMapping("/cities/{city}/districts")
    public Result<List<String>> getDistrictsByCity(
            @Parameter(description = "城市名称") @PathVariable String city) {
        return manholeService.getDistrictsByCity(city);
    }
}
