package com.alian.emci.service;

import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Repair;

/**
 * 维修任务分配服务
 * 实现维修人员的自动分配和负载均衡
 */
public interface RepairDispatchService {

    /**
     * 为检测记录自动分配维修任务
     * @param detection 检测记录
     * @return 创建的维修记录
     */
    Repair autoDispatch(Detection detection);

    /**
     * 重新分配维修任务
     * @param repairId 维修记录ID
     * @param newRepairUserId 新的维修人员ID
     * @return 更新后的维修记录
     */
    Repair reDispatch(Long repairId, Long newRepairUserId);

    /**
     * 获取当前负载最轻的维修人员ID
     * @return 维修人员ID
     */
    Long getLeastLoadedRepairUserId();

    /**
     * 批量分配未分配的维修任务
     */
    void batchDispatchUnassigned();
}
