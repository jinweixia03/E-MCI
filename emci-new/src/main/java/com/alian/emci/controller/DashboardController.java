package com.alian.emci.controller;

import com.alian.emci.common.Result;
import com.alian.emci.service.DashboardService;
import com.alian.emci.vo.dashboard.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 */
@Tag(name = "仪表盘", description = "数据统计相关接口")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取统计数据", description = "获取首页仪表盘统计数据")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return dashboardService.getStats();
    }
}
