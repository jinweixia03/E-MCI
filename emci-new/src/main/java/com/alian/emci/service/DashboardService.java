package com.alian.emci.service;

import com.alian.emci.common.Result;
import com.alian.emci.vo.dashboard.DashboardStatsVO;

/**
 * 仪表盘服务接口
 */
public interface DashboardService {

    /**
     * 获取统计数据
     */
    Result<DashboardStatsVO> getStats();
}
