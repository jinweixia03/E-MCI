package com.alian.emci.service;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.repair.RepairCreateRequest;
import com.alian.emci.dto.repair.RepairUpdateRequest;
import com.alian.emci.vo.repair.RepairVO;

import java.util.List;
import java.util.Map;

/**
 * 维修记录服务接口
 */
public interface RepairService {

    /**
     * 创建维修记录
     */
    Result<RepairVO> create(RepairCreateRequest request);

    /**
     * 更新维修记录
     */
    Result<RepairVO> update(Long id, RepairUpdateRequest request);

    /**
     * 删除维修记录
     */
    Result<Void> delete(Long id);

    /**
     * 根据ID查询
     */
    Result<RepairVO> getById(Long id);

    /**
     * 分页查询
     */
    Result<PageResult<RepairVO>> pageQuery(Integer pageNum, Integer pageSize, String manholeId, Integer status, Long detectionId);

    /**
     * 根据井盖编号查询维修记录（包含检测图片信息）
     */
    Result<List<RepairVO>> getByManholeId(String manholeId);

    /**
     * 根据检测记录ID查询维修记录
     */
    Result<RepairVO> getByDetectionId(Long detectionId);

    /**
     * 开始维修
     */
    Result<RepairVO> startRepair(Long id);

    /**
     * 完成维修
     */
    Result<RepairVO> completeRepair(Long id, String afterImg, String remark);

    /**
     * 分配维修任务
     */
    Result<RepairVO> assignRepair(Long id, Long repairUserId);

    /**
     * 根据状态查询维修记录
     */
    Result<List<RepairVO>> getByStatus(Integer status);

    /**
     * 重新分配维修任务
     */
    Result<RepairVO> reassignRepair(Long id, Long repairUserId);

    /**
     * 确认维修完成
     */
    Result<RepairVO> confirmRepair(Long id);

    /**
     * 获取维修统计
     */
    Result<Map<String, Long>> getStats();

    /**
     * 维修员分页查询自己的维修记录
     */
    Result<PageResult<RepairVO>> pageQueryByRepairUser(Long repairUserId, Integer pageNum, Integer pageSize, Integer status, Long detectionId);

    /**
     * 获取维修员的维修统计
     */
    Result<Map<String, Long>> getStatsByRepairUser(Long repairUserId);
}
