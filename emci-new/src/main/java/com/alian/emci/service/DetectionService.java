package com.alian.emci.service;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.detection.DetectionCreateRequest;
import com.alian.emci.dto.detection.DetectionQueryRequest;
import com.alian.emci.vo.detection.DetectionResultVO;
import com.alian.emci.vo.detection.DetectionVO;

import java.util.List;

/**
 * 检测记录服务接口
 */
public interface DetectionService {

    /**
     * 创建检测记录
     */
    Result<DetectionVO> create(DetectionCreateRequest request);

    /**
     * 删除检测记录
     */
    Result<Void> delete(Long id);

    /**
     * 根据ID查询
     */
    Result<DetectionVO> getById(Long id);

    /**
     * 分页查询
     */
    Result<PageResult<DetectionVO>> pageQuery(DetectionQueryRequest request);

    /**
     * 根据井盖编号查询检测记录
     */
    Result<List<DetectionVO>> getByManholeId(String manholeId);

    /**
     * 获取需要维修的检测记录
     */
    Result<List<DetectionVO>> getNeedRepaired();

    /**
     * 标记为已修复
     */
    Result<DetectionVO> markRepaired(Long id);

    /**
     * 保存AI检测结果
     */
    Result<DetectionVO> saveDetectionRecord(String manholeId, DetectionResultVO result);
}
