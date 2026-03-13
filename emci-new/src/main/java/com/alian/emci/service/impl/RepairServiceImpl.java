package com.alian.emci.service.impl;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.common.ResultCode;
import com.alian.emci.common.constant.ManholeConstant;
import com.alian.emci.common.util.StatsUtils;
import com.alian.emci.dto.repair.RepairCreateRequest;
import com.alian.emci.dto.repair.RepairUpdateRequest;
import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Repair;
import com.alian.emci.entity.User;
import com.alian.emci.exception.BusinessException;
import com.alian.emci.mapper.DetectionMapper;
import com.alian.emci.mapper.RepairMapper;
import com.alian.emci.mapper.UserMapper;
import com.alian.emci.service.RepairService;
import com.alian.emci.vo.repair.RepairVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 维修记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairServiceImpl implements RepairService {

    private final RepairMapper repairMapper;
    private final DetectionMapper detectionMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> create(RepairCreateRequest request) {
        Repair repair = new Repair();
        BeanUtils.copyProperties(request, repair);

        // 设置初始状态
        repair.setStatus(0);
        repair.setAssignedTime(LocalDateTime.now());

        repairMapper.insert(repair);

        log.info("创建维修记录: detectionId={}, manholeId={}", repair.getDetectionId(), repair.getManholeId());
        return Result.success("创建成功", convertToVO(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> update(Long id, RepairUpdateRequest request) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        BeanUtils.copyProperties(request, repair);
        repairMapper.updateById(repair);

        log.info("更新维修记录: {}", id);
        return Result.success("更新成功", convertToVO(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        repairMapper.deleteById(id);
        log.info("删除维修记录: {}", id);
        return Result.success();
    }

    @Override
    public Result<RepairVO> getById(Long id) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }
        log.info("getById - repair id: {}, afterImg from DB: {}", id, repair.getAfterImg());
        RepairVO vo = convertToVOWithDetection(repair);
        log.info("getById - vo afterImg: {}", vo.getAfterImg());
        return Result.success(vo);
    }

    @Override
    public Result<PageResult<RepairVO>> pageQuery(Integer pageNum, Integer pageSize, String manholeId, Integer status, Long detectionId) {
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();

        if (manholeId != null && !manholeId.isEmpty()) {
            wrapper.eq(Repair::getManholeId, manholeId);
        }
        if (status != null) {
            wrapper.eq(Repair::getStatus, status);
        }
        if (detectionId != null) {
            wrapper.eq(Repair::getDetectionId, detectionId);
        }

        wrapper.orderByDesc(Repair::getCreateTime);

        Page<Repair> page = new Page<>(pageNum, pageSize);
        Page<Repair> resultPage = repairMapper.selectPage(page, wrapper);

        List<RepairVO> list = resultPage.getRecords().stream()
                .map(this::convertToVOWithDetection)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                list
        ));
    }

    @Override
    public Result<List<RepairVO>> getByManholeId(String manholeId) {
        List<Repair> list = repairMapper.selectList(
                new LambdaQueryWrapper<Repair>()
                        .eq(Repair::getManholeId, manholeId)
                        .orderByDesc(Repair::getCreateTime)
        );

        List<RepairVO> voList = list.stream()
                .map(this::convertToVOWithDetection)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    public Result<RepairVO> getByDetectionId(Long detectionId) {
        Repair repair = repairMapper.selectOne(
                new LambdaQueryWrapper<Repair>()
                        .eq(Repair::getDetectionId, detectionId)
        );
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }
        return Result.success(convertToVOWithDetection(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> startRepair(Long id) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        repair.setStatus(1);
        repair.setAssignedTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        log.info("开始维修: {}", id);
        return Result.success("开始维修", convertToVO(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> completeRepair(Long id, String afterImg, String remark) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        // 维修完成提交，等待确认
        repair.setStatus(2);
        repair.setCompleteTime(LocalDateTime.now());
        // 只更新非空字段，避免覆盖已有数据
        if (afterImg != null && !afterImg.isEmpty()) {
            repair.setAfterImg(afterImg);
        }
        if (remark != null && !remark.isEmpty()) {
            repair.setRemark(remark);
        }
        repairMapper.updateById(repair);

        log.info("维修完成提交，等待确认: {}", id);
        return Result.success("维修完成，等待确认", convertToVO(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> confirmRepair(Long id) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        // 确认维修完成
        repair.setStatus(3);
        repairMapper.updateById(repair);

        log.info("确认维修完成: {}", id);
        return Result.success("确认维修完成", convertToVO(repair));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> assignRepair(Long id, Long repairUserId) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        repair.setRepairUserId(repairUserId);
        repair.setAssignedTime(LocalDateTime.now());
        repairMapper.updateById(repair);

        log.info("分配维修任务: {} -> 用户{}", id, repairUserId);
        return Result.success("分配成功", convertToVO(repair));
    }

    @Override
    public Result<List<RepairVO>> getByStatus(Integer status) {
        List<Repair> list = repairMapper.selectList(
                new LambdaQueryWrapper<Repair>()
                        .eq(status != null, Repair::getStatus, status)
                        .orderByDesc(Repair::getCreateTime)
        );

        List<RepairVO> voList = list.stream()
                .map(this::convertToVOWithDetection)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<RepairVO> reassignRepair(Long id, Long repairUserId) {
        Repair repair = repairMapper.selectById(id);
        if (repair == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "维修记录不存在");
        }

        // 检查新分配的用户是否是 type=2 的维修人员
        User newUser = userMapper.selectById(repairUserId);
        if (newUser == null || newUser.getType() != 2) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "只能选择维修人员(type=2)进行分配");
        }

        Long oldUserId = repair.getRepairUserId();

        // 使用 UpdateWrapper 强制更新 null 值（updateById 会忽略 null）
        UpdateWrapper<Repair> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("repair_user_id", repairUserId)
                .set("status", 0)
                .set("assigned_time", null)
                .set("complete_time", null);
        repairMapper.update(null, updateWrapper);

        // 更新内存对象用于返回
        repair.setRepairUserId(repairUserId);
        repair.setStatus(0);
        repair.setAssignedTime(null);
        repair.setCompleteTime(null);

        log.info("重新分配维修任务: {} -> 从用户{}到用户{}，状态重置为待处理", id, oldUserId, repairUserId);
        return Result.success("重新分配成功，任务重置为待处理", convertToVOWithDetection(repair));
    }

    @Override
    public Result<Map<String, Long>> getStats() {
        // 使用SQL分组查询统计各状态数量
        List<Map<String, Object>> resultList = repairMapper.selectMaps(
            new QueryWrapper<Repair>()
                .select("status", "count(*) as count")
                .groupBy("status")
        );

        Map<String, Long> stats = StatsUtils.createStatusStats(repairMapper.selectCount(null));
        StatsUtils.fillStatusStats(stats, resultList, "status", "count");

        return Result.success(stats);
    }

    @Override
    public Result<PageResult<RepairVO>> pageQueryByRepairUser(Long repairUserId, Integer pageNum, Integer pageSize, Integer status, Long detectionId) {
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();

        // 只查询分配给该维修员的记录
        wrapper.eq(Repair::getRepairUserId, repairUserId);

        if (status != null) {
            wrapper.eq(Repair::getStatus, status);
        }
        if (detectionId != null) {
            wrapper.eq(Repair::getDetectionId, detectionId);
        }

        wrapper.orderByDesc(Repair::getCreateTime);

        Page<Repair> page = new Page<>(pageNum, pageSize);
        Page<Repair> resultPage = repairMapper.selectPage(page, wrapper);

        List<RepairVO> list = resultPage.getRecords().stream()
                .map(this::convertToVOWithDetection)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                list
        ));
    }

    @Override
    public Result<Map<String, Long>> getStatsByRepairUser(Long repairUserId) {
        // 查询该维修员各状态数量
        List<Map<String, Object>> resultList = repairMapper.selectMaps(
            new QueryWrapper<Repair>()
                .eq("repair_user_id", repairUserId)
                .select("status", "count(*) as count")
                .groupBy("status")
        );

        Map<String, Long> stats = StatsUtils.createStatusStats(
            repairMapper.selectCount(new LambdaQueryWrapper<Repair>().eq(Repair::getRepairUserId, repairUserId))
        );
        StatsUtils.fillStatusStats(stats, resultList, "status", "count");

        return Result.success(stats);
    }

    /**
     * 转换为VO（基础转换）
     */
    private RepairVO convertToVO(Repair repair) {
        RepairVO vo = new RepairVO();
        BeanUtils.copyProperties(repair, vo);

        // 手动复制可能遗漏的字段
        vo.setBeforeImg(repair.getBeforeImg());
        vo.setAfterImg(repair.getAfterImg());
        vo.setRemark(repair.getRemark());

        // 调试日志
        log.info("convertToVO - repair.afterImg: {}, vo.afterImg: {}", repair.getAfterImg(), vo.getAfterImg());

        // 转换日期格式（LocalDateTime -> LocalDate）
        // startTime 从 assignedTime 获取（assignedTime 为 null 则 startTime 也为 null）
        if (repair.getAssignedTime() != null) {
            vo.setStartTimeDate(repair.getAssignedTime().toLocalDate());
        }
        // endTime 从 completeTime 获取
        if (repair.getCompleteTime() != null) {
            vo.setEndTimeDate(repair.getCompleteTime().toLocalDate());
        }

        // 设置状态文本
        vo.setStatusText(ManholeConstant.getRepairStatusName(repair.getStatus()));

        // 设置维修人员姓名
        if (repair.getRepairUserId() != null) {
            User user = userMapper.selectById(repair.getRepairUserId());
            if (user != null) {
                vo.setRepairUserName(user.getUsername());
            }
        }

        return vo;
    }

    /**
     * 转换为VO（包含检测记录信息，包括图片URL）
     */
    private RepairVO convertToVOWithDetection(Repair repair) {
        RepairVO vo = convertToVO(repair);

        // 关联查询检测记录，获取图片URL
        if (repair.getDetectionId() != null) {
            Detection detection = detectionMapper.selectById(repair.getDetectionId());
            if (detection != null) {
                vo.setDetectionNo(detection.getDetectionNo());
                vo.setDetectionTime(detection.getDetectionTime());
                vo.setDetectionOriginalImgUrl(detection.getOriginalImgUrl());
                vo.setDetectionResultImgUrl(detection.getResultImgUrl());
                vo.setHasDefect(detection.getHasDefect());
                vo.setDefectCount(detection.getDefectCount());
                vo.setPrimaryDefectType(detection.getPrimaryDefectType());
                vo.setPrimaryConfidence(detection.getPrimaryConfidence());
                // 从 detection 复制缺陷类型
                vo.setDefectTypes(detection.getDefectTypes());
                // 如果 repair 表中没有 before_img，则从 detection 复制
                if (vo.getBeforeImg() == null || vo.getBeforeImg().isEmpty()) {
                    vo.setBeforeImg(detection.getResultImgUrl());
                }
            }
        }

        return vo;
    }
}
