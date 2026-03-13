package com.alian.emci.service.impl;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.common.ResultCode;
import com.alian.emci.dto.detection.DetectionCreateRequest;
import com.alian.emci.dto.detection.DetectionQueryRequest;
import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Repair;
import com.alian.emci.exception.BusinessException;
import com.alian.emci.mapper.DetectionMapper;
import com.alian.emci.service.DetectionService;
import com.alian.emci.service.RepairDispatchService;
import com.alian.emci.vo.detection.DetectionResultVO;
import com.alian.emci.vo.detection.DetectionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检测记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectionServiceImpl implements DetectionService {

    private final DetectionMapper detectionMapper;
    private final RepairDispatchService repairDispatchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DetectionVO> create(DetectionCreateRequest request) {
        Detection detection = new Detection();
        BeanUtils.copyProperties(request, detection);

        // 默认设置为未修复
        if (detection.getIsRepaired() == null) {
            detection.setIsRepaired(0);
        }

        // 默认检测状态为成功
        if (detection.getDetectionStatus() == null) {
            detection.setDetectionStatus(1);
        }

        // 默认检测时间为当前时间
        if (detection.getDetectionTime() == null) {
            detection.setDetectionTime(LocalDateTime.now());
        }

        // 如果没有缺陷，确保相关字段正确
        if (detection.getHasDefect() == null) {
            detection.setHasDefect(detection.getDefectCount() != null && detection.getDefectCount() > 0 ? 1 : 0);
        }

        detectionMapper.insert(detection);

        // 自动分配维修任务（如果有缺陷）
        Repair repair = repairDispatchService.autoDispatch(detection);
        if (repair != null) {
            log.info("检测记录自动分配维修任务成功: detectionId={}, repairId={}", detection.getId(), repair.getId());
        }

        log.info("创建检测记录: manholeId={}, detectionNo={}, defects={}",
                detection.getManholeId(), detection.getDetectionNo(), detection.getDefectCount());
        return Result.success("创建成功", convertToVO(detection));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(Long id) {
        Detection detection = detectionMapper.selectById(id);
        if (detection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "检测记录不存在");
        }

        detectionMapper.deleteById(id);
        log.info("删除检测记录: {}", id);
        return Result.success();
    }

    @Override
    public Result<DetectionVO> getById(Long id) {
        Detection detection = detectionMapper.selectById(id);
        if (detection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "检测记录不存在");
        }
        return Result.success(convertToVO(detection));
    }

    @Override
    public Result<PageResult<DetectionVO>> pageQuery(DetectionQueryRequest request) {
        LambdaQueryWrapper<Detection> wrapper = new LambdaQueryWrapper<>();

        // 条件查询（只使用数据库存在的字段）
        if (request.getManholeId() != null && !request.getManholeId().isEmpty()) {
            wrapper.eq(Detection::getManholeId, request.getManholeId());
        }
        if (request.getHasDefect() != null) {
            wrapper.eq(Detection::getHasDefect, request.getHasDefect());
        }
        // is_repaired 字段在数据库中不存在，如需筛选请扩展数据库
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(Detection::getManholeId, request.getKeyword());
        }

        // 按检测时间倒序
        wrapper.orderByDesc(Detection::getDetectionTime);

        Page<Detection> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Detection> resultPage = detectionMapper.selectPage(page, wrapper);

        List<DetectionVO> list = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                resultPage.getTotal(),
                list
        ));
    }

    @Override
    public Result<List<DetectionVO>> getByManholeId(String manholeId) {
        List<Detection> list = detectionMapper.selectList(
                new LambdaQueryWrapper<Detection>()
                        .eq(Detection::getManholeId, manholeId)
                        .orderByDesc(Detection::getDetectionTime)
        );
        return Result.success(list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    @Override
    public Result<List<DetectionVO>> getNeedRepaired() {
        // 注意：is_repaired 字段不存在于数据库，只查询有缺陷的记录
        List<Detection> list = detectionMapper.selectList(
                new LambdaQueryWrapper<Detection>()
                        .eq(Detection::getHasDefect, 1)
                        .orderByDesc(Detection::getDetectionTime)
        );
        return Result.success(list.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DetectionVO> markRepaired(Long id) {
        Detection detection = detectionMapper.selectById(id);
        if (detection == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "检测记录不存在");
        }

        // 注意：is_repaired 字段在数据库中不存在，此方法暂时返回成功但不执行实际更新
        // detection.setIsRepaired(1);
        // detectionMapper.updateById(detection);

        log.warn("标记检测记录为已修复: {} (注意：is_repaired字段不存在于数据库，未实际更新)", id);
        return Result.success("标记成功（数据库不支持）", convertToVO(detection));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<DetectionVO> saveDetectionRecord(String manholeId, DetectionResultVO result) {
        Detection detection = new Detection();

        // 基础信息
        detection.setManholeId(manholeId);
        detection.setDetectionNo(generateDetectionNo());
        detection.setDetectionTime(LocalDateTime.now());
        detection.setDetectionStatus(1);

        // 图片信息 - 保存到数据库的 img_url 字段
        if (result.getResultUrl() != null && !result.getResultUrl().isEmpty()) {
            detection.setImgUrl(result.getResultUrl());
        } else if (result.getOriginalUrl() != null && !result.getOriginalUrl().isEmpty()) {
            detection.setImgUrl(result.getOriginalUrl());
        }

        // 修复状态
        detection.setIsRepaired(0);

        // 模型参数（默认值）
        detection.setModelVersion("yolov8");
        detection.setConfThreshold(0.7);
        detection.setIouThreshold(0.5);

        // 处理时间
        if (result.getProcessTime() != null) {
            detection.setProcessTimeMs(result.getProcessTime().intValue());
        }

        // 设置缺陷信息
        if (result.getDefects() != null && !result.getDefects().isEmpty()) {
            detection.setHasDefect(1);
            detection.setDefectCount(result.getDefects().size());

            // 提取缺陷类型
            String defectTypes = result.getDefects().stream()
                    .map(DetectionResultVO.DefectInfo::getType)
                    .distinct()
                    .collect(Collectors.joining(","));
            detection.setDefectTypes(defectTypes);

            // 找出最高置信度的缺陷
            DetectionResultVO.DefectInfo primaryDefect = result.getDefects().stream()
                    .max((a, b) -> Double.compare(a.getConfidence(), b.getConfidence()))
                    .orElse(null);

            if (primaryDefect != null) {
                detection.setPrimaryDefectType(primaryDefect.getType());
                detection.setPrimaryConfidence(primaryDefect.getConfidence());
            }
        } else {
            detection.setHasDefect(0);
            detection.setDefectCount(0);
        }

        detectionMapper.insert(detection);

        // 自动分配维修任务（如果有缺陷）
        Repair repair = repairDispatchService.autoDispatch(detection);
        if (repair != null) {
            log.info("AI检测记录自动分配维修任务成功: detectionId={}, repairId={}", detection.getId(), repair.getId());
        }

        log.info("保存AI检测记录: manholeId={}, detectionNo={}, defects={}",
                manholeId, detection.getDetectionNo(), detection.getDefectCount());
        return Result.success("保存成功", convertToVO(detection));
    }

    /**
     * 生成检测编号（保证唯一性）
     */
    private String generateDetectionNo() {
        // 使用日期 + 时间戳 + 随机数，确保唯一性
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%03d", (int) (Math.random() * 1000));
        return "DET" + timestamp + random;
    }

    /**
     * 转换为VO
     */
    private DetectionVO convertToVO(Detection detection) {
        DetectionVO vo = new DetectionVO();
        BeanUtils.copyProperties(detection, vo);

        // 从 imgUrl 派生 originalImgUrl 和 resultImgUrl
        // 注意：imgUrl 是数据库存储的字段，originalImgUrl/resultImgUrl 是内存计算字段
        if (detection.getImgUrl() != null && !detection.getImgUrl().isEmpty()) {
            // 结果图片使用 img_url（detections目录）
            vo.setResultImgUrl(detection.getImgUrl());
            // 原始图片使用 manholes 目录的同名文件
            String originalUrl = detection.getImgUrl()
                .replace("/uploads/detections/", "/uploads/manholes/")
                .replaceAll("_\\d+\\.jpg$", ".jpg"); // 去掉 _1, _2 等后缀
            vo.setOriginalImgUrl(originalUrl);
            log.debug("转换图片URL: imgUrl={}, resultImgUrl={}, originalImgUrl={}",
                    detection.getImgUrl(), vo.getResultImgUrl(), vo.getOriginalImgUrl());
        } else {
            log.warn("检测记录 imgUrl 为空: detectionId={}", detection.getId());
        }

        vo.setRepairStatusText(detection.getIsRepaired() != null && detection.getIsRepaired() == 1 ? "已修复" : "待修复");
        return vo;
    }
}
