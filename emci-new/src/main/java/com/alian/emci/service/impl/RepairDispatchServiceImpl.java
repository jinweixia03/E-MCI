package com.alian.emci.service.impl;

import com.alian.emci.entity.Detection;
import com.alian.emci.entity.Repair;
import com.alian.emci.entity.User;
import com.alian.emci.mapper.RepairMapper;
import com.alian.emci.mapper.UserMapper;
import com.alian.emci.service.RepairDispatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 维修任务分配服务实现
 * 采用负载均衡算法分配维修任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairDispatchServiceImpl implements RepairDispatchService {

    private final RepairMapper repairMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Repair autoDispatch(Detection detection) {
        // 如果检测记录没有缺陷，不创建维修任务
        if (detection.getHasDefect() == null || detection.getHasDefect() == 0) {
            log.info("检测记录无缺陷，跳过维修任务分配: detectionId={}", detection.getId());
            return null;
        }

        // 检查是否已存在维修记录
        Repair existingRepair = repairMapper.selectOne(
                new LambdaQueryWrapper<Repair>()
                        .eq(Repair::getDetectionId, detection.getId())
                        .eq(Repair::getDeleted, 0)
        );

        if (existingRepair != null) {
            log.info("检测记录已有维修任务，跳过分配: detectionId={}", detection.getId());
            return existingRepair;
        }

        // 创建维修记录
        Repair repair = new Repair();
        repair.setDetectionId(detection.getId());
        repair.setManholeId(detection.getManholeId());
        repair.setStatus(0); // 待维修

        // 自动分配维修人员（不设置开始时间，由维修人员接单时设置）
        Long repairUserId = getLeastLoadedRepairUserId();
        if (repairUserId != null) {
            repair.setRepairUserId(repairUserId);
            // 不设置 assignedTime，等维修人员开始处理时再设置
        }

        // 从检测记录复制图片
        repair.setBeforeImg(detection.getImgUrl()); // 使用检测记录的图片作为维修前图片
        repair.setRemark("系统自动分配");

        repairMapper.insert(repair);

        log.info("自动分配维修任务: repairId={}, detectionId={}, manholeId={}, assignedUser={}",
                repair.getId(), detection.getId(), detection.getManholeId(), repairUserId);

        return repair;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Repair reDispatch(Long repairId, Long newRepairUserId) {
        Repair repair = repairMapper.selectById(repairId);
        if (repair == null) {
            log.warn("维修记录不存在: {}", repairId);
            return null;
        }

        // 检查新分配的用户是否是 type=2 的维修人员
        User newUser = userMapper.selectById(newRepairUserId);
        if (newUser == null || newUser.getType() != 2) {
            log.warn("只能选择维修人员(type=2)进行分配");
            return null;
        }

        Long oldUserId = repair.getRepairUserId();

        // 使用 UpdateWrapper 强制更新 null 值
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Repair> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", repairId)
                .set("repair_user_id", newRepairUserId)
                .set("status", 0)
                .set("assigned_time", null)
                .set("complete_time", null);
        repairMapper.update(null, updateWrapper);

        // 更新内存对象
        repair.setRepairUserId(newRepairUserId);
        repair.setStatus(0);
        repair.setAssignedTime(null);
        repair.setCompleteTime(null);

        log.info("重新分配维修任务: repairId={}, oldUser={}, newUser={}",
                repairId, oldUserId, newRepairUserId);

        return repair;
    }

    @Override
    public Long getLeastLoadedRepairUserId() {
        // 获取所有维修人员（type=2 或 type=1 的管理员也可以）
        List<User> repairUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getType, 2)
                        .eq(User::getDeleted, 0)
        );

        if (repairUsers.isEmpty()) {
            // 如果没有专门的维修人员，使用管理员
            repairUsers = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getType, 1)
                            .eq(User::getDeleted, 0)
            );
        }

        if (repairUsers.isEmpty()) {
            log.warn("系统中没有可用的维修人员");
            return null;
        }

        // 统计每个维修人员的当前负载
        Map<Long, Integer> userLoadMap = new HashMap<>();

        for (User user : repairUsers) {
            // 统计待处理和进行中的任务数
            Long pendingCount = repairMapper.selectCount(
                    new LambdaQueryWrapper<Repair>()
                            .eq(Repair::getRepairUserId, user.getId())
                            .eq(Repair::getStatus, 0)
                            .eq(Repair::getDeleted, 0)
            );

            Long inProgressCount = repairMapper.selectCount(
                    new LambdaQueryWrapper<Repair>()
                            .eq(Repair::getRepairUserId, user.getId())
                            .eq(Repair::getStatus, 1)
                            .eq(Repair::getDeleted, 0)
            );

            // 计算负载：待处理*2 + 进行中*1（待处理权重更高，因为需要优先处理）
            int load = pendingCount.intValue() * 2 + inProgressCount.intValue();
            userLoadMap.put(user.getId(), load);
        }

        // 找到负载最轻的人员
        return userLoadMap.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(repairUsers.get(0).getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDispatchUnassigned() {
        // 查询所有未分配维修人员的待处理任务
        List<Repair> unassignedRepairs = repairMapper.selectList(
                new LambdaQueryWrapper<Repair>()
                        .isNull(Repair::getRepairUserId)
                        .eq(Repair::getStatus, 0)
                        .eq(Repair::getDeleted, 0)
        );

        if (unassignedRepairs.isEmpty()) {
            log.info("没有未分配的维修任务");
            return;
        }

        log.info("开始批量分配维修任务，共 {} 条", unassignedRepairs.size());

        for (Repair repair : unassignedRepairs) {
            Long userId = getLeastLoadedRepairUserId();
            if (userId != null) {
                repair.setRepairUserId(userId);
                repair.setAssignedTime(LocalDateTime.now());
                repairMapper.updateById(repair);

                log.info("批量分配维修任务: repairId={}, assignedUser={}", repair.getId(), userId);
            }
        }
    }
}
