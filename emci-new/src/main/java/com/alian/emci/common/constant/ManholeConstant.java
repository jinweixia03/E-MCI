package com.alian.emci.common.constant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 井盖相关常量
 * 集中管理井盖类型、状态、缺陷类型等映射关系
 */
public final class ManholeConstant {

    private ManholeConstant() {
        // 工具类，禁止实例化
    }

    // ==================== 井盖类型 ====================
    public static final Map<Integer, String> MANHOLE_TYPE_MAP;
    public static final int TYPE_RAIN = 1;      // 雨水
    public static final int TYPE_SEWAGE = 2;    // 污水
    public static final int TYPE_POWER = 3;     // 电力
    public static final int TYPE_COMM = 4;      // 通信
    public static final int TYPE_GAS = 5;       // 燃气

    // ==================== 井盖状态 ====================
    public static final Map<Integer, String> STATUS_MAP;
    public static final int STATUS_NORMAL = 0;      // 正常
    public static final int STATUS_DAMAGED = 1;     // 损坏
    public static final int STATUS_REPAIRING = 2;   // 维修中

    // ==================== 维修状态 ====================
    public static final Map<Integer, String> REPAIR_STATUS_MAP;
    public static final int REPAIR_PENDING = 0;     // 待维修
    public static final int REPAIR_IN_PROGRESS = 1; // 维修中
    public static final int REPAIR_TO_CONFIRM = 2;  // 待确认
    public static final int REPAIR_COMPLETED = 3;   // 已完成

    // ==================== 缺陷类型 ====================
    public static final Map<String, String> DEFECT_TYPE_MAP;

    // ==================== 用户类型 ====================
    public static final int USER_TYPE_NORMAL = 0;   // 普通用户
    public static final int USER_TYPE_ADMIN = 1;    // 管理员
    public static final int USER_TYPE_REPAIR = 2;   // 维修人员

    static {
        // 井盖类型
        Map<Integer, String> typeMap = new HashMap<>();
        typeMap.put(TYPE_RAIN, "雨水");
        typeMap.put(TYPE_SEWAGE, "污水");
        typeMap.put(TYPE_POWER, "电力");
        typeMap.put(TYPE_COMM, "通信");
        typeMap.put(TYPE_GAS, "燃气");
        MANHOLE_TYPE_MAP = Collections.unmodifiableMap(typeMap);

        // 井盖状态
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(STATUS_NORMAL, "正常");
        statusMap.put(STATUS_DAMAGED, "损坏");
        statusMap.put(STATUS_REPAIRING, "维修中");
        STATUS_MAP = Collections.unmodifiableMap(statusMap);

        // 维修状态
        Map<Integer, String> repairMap = new HashMap<>();
        repairMap.put(REPAIR_PENDING, "待维修");
        repairMap.put(REPAIR_IN_PROGRESS, "维修中");
        repairMap.put(REPAIR_TO_CONFIRM, "待确认");
        repairMap.put(REPAIR_COMPLETED, "已完成");
        REPAIR_STATUS_MAP = Collections.unmodifiableMap(repairMap);

        // 缺陷类型
        Map<String, String> defectMap = new HashMap<>();
        defectMap.put("crack", "裂缝");
        defectMap.put("wear", "磨损");
        defectMap.put("deformation", "变形");
        defectMap.put("missing", "缺失");
        defectMap.put("displacement", "位移");
        defectMap.put("corrosion", "腐蚀");
        defectMap.put("破损", "破损");
        defectMap.put("下沉", "下沉");
        defectMap.put("松动", "松动");
        defectMap.put("Manhole", "井盖");
        defectMap.put("Damage", "损坏");
        defectMap.put("Open", "开启");
        DEFECT_TYPE_MAP = Collections.unmodifiableMap(defectMap);
    }

    /**
     * 获取井盖类型名称
     */
    public static String getManholeTypeName(Integer type) {
        if (type == null) {
            return null;
        }
        return MANHOLE_TYPE_MAP.getOrDefault(type, "未知");
    }

    /**
     * 获取井盖状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return STATUS_MAP.getOrDefault(status, "未知");
    }

    /**
     * 获取维修状态名称
     */
    public static String getRepairStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return REPAIR_STATUS_MAP.getOrDefault(status, "未知");
    }

    /**
     * 获取缺陷类型名称
     */
    public static String getDefectTypeName(String type) {
        if (type == null) {
            return null;
        }
        return DEFECT_TYPE_MAP.getOrDefault(type, type);
    }
}
