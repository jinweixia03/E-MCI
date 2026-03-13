package com.alian.emci.service;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.manhole.ManholeCreateRequest;
import com.alian.emci.dto.manhole.ManholeMapQueryRequest;
import com.alian.emci.dto.manhole.ManholeQueryRequest;
import com.alian.emci.dto.manhole.ManholeUpdateRequest;
import com.alian.emci.vo.manhole.ManholeVO;
import com.alian.emci.vo.map.ManholeClusterVO;
import com.alian.emci.vo.map.ManholeMapStatsVO;
import com.alian.emci.vo.map.ManholeMapVO;

import java.util.List;

/**
 * 井盖服务接口
 */
public interface ManholeService {

    /**
     * 创建井盖
     */
    Result<ManholeVO> create(ManholeCreateRequest request);

    /**
     * 更新井盖
     */
    Result<ManholeVO> update(Long id, ManholeUpdateRequest request);

    /**
     * 删除井盖
     */
    Result<Void> delete(Long id);

    /**
     * 根据ID查询
     */
    Result<ManholeVO> getById(Long id);

    /**
     * 分页查询
     */
    Result<PageResult<ManholeVO>> pageQuery(ManholeQueryRequest request);

    /**
     * 获取所有井盖
     */
    Result<List<ManholeVO>> getAll();

    /**
     * 根据编号查询
     */
    Result<ManholeVO> getByManholeId(String manholeId);

    // ==================== 地图相关接口 ====================

    /**
     * 地图范围查询井盖
     */
    Result<List<ManholeMapVO>> queryForMap(ManholeMapQueryRequest request);

    /**
     * 获取地图聚合数据
     */
    Result<List<ManholeClusterVO>> getClusterData(ManholeMapQueryRequest request);

    /**
     * 获取地图统计信息
     */
    Result<ManholeMapStatsVO> getMapStats(ManholeMapQueryRequest request);

    /**
     * 获取附近井盖
     */
    Result<List<ManholeMapVO>> getNearbyManholes(Double longitude, Double latitude, Integer radius);

    /**
     * 获取所有城市列表
     */
    Result<List<String>> getAllCities();

    /**
     * 获取城市下的区县列表
     */
    Result<List<String>> getDistrictsByCity(String city);
}
