/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : localhost:3306
 Source Schema         : emci

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 14/03/2026 03:13:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for detection
-- ----------------------------
DROP TABLE IF EXISTS `detection`;
CREATE TABLE `detection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '检测记录ID',
  `manhole_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联井盖编号',
  `detection_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '检测编号（格式：DET202403130001）',
  `detection_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
  `img_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '检测图片URL（带标注结果）',
  `has_defect` tinyint NULL DEFAULT 0 COMMENT '是否有缺陷：0-无，1-有',
  `defect_count` int NULL DEFAULT 0 COMMENT '缺陷总数',
  `defect_types` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '缺陷类型（逗号分隔）',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_detection_no`(`detection_no` ASC) USING BTREE,
  INDEX `idx_manhole_id`(`manhole_id` ASC) USING BTREE,
  INDEX `idx_has_defect`(`has_defect` ASC) USING BTREE,
  CONSTRAINT `fk_detection_manhole` FOREIGN KEY (`manhole_id`) REFERENCES `manhole` (`manhole_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22275 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '检测记录表（精简版）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for drone
-- ----------------------------
DROP TABLE IF EXISTS `drone`;
CREATE TABLE `drone`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '无人机名称',
  `status` tinyint NULL DEFAULT 0 COMMENT '0-闲置 1-巡检中 2-充电中 3-维修中',
  `battery` tinyint NULL DEFAULT 100 COMMENT '电量',
  `latitude` decimal(10, 8) NULL DEFAULT NULL COMMENT '当前纬度',
  `longitude` decimal(11, 8) NULL DEFAULT NULL COMMENT '当前经度',
  `radius` int NULL DEFAULT 1000 COMMENT '检测半径（米）',
  `deleted` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '无人机表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for drone_task
-- ----------------------------
DROP TABLE IF EXISTS `drone_task`;
CREATE TABLE `drone_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `drone_id` bigint NOT NULL COMMENT '无人机ID（唯一，一对一）',
  `center_lat` decimal(10, 8) NOT NULL COMMENT '检测中心纬度',
  `center_lng` decimal(11, 8) NOT NULL COMMENT '检测中心经度',
  `radius` int NOT NULL COMMENT '检测半径（米）',
  `path_points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '检测路径（JSON: [{manholeId, sequence, lat, lng}, ...]）',
  `manhole_count` int NULL DEFAULT 0 COMMENT '覆盖井盖数',
  `estimated_time` int NULL DEFAULT 0 COMMENT '预计时间（分钟）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `drone_id`(`drone_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_drone_id`(`drone_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_drone` FOREIGN KEY (`drone_id`) REFERENCES `drone` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '无人机检测任务（一对一）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for manhole
-- ----------------------------
DROP TABLE IF EXISTS `manhole`;
CREATE TABLE `manhole`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `manhole_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '井盖编号',
  `img_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '井盖图片URL',
  `latitude` decimal(10, 8) NULL DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(11, 8) NULL DEFAULT NULL COMMENT '经度',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `next_det_time` date NULL DEFAULT NULL COMMENT '下次检测时间',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-正常，1-异常，2-维修中，3-已废弃',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在区县',
  `manhole_type` tinyint NULL DEFAULT 1 COMMENT '井盖类型：1-雨水，2-污水，3-电力，4-通信，5-燃气',
  `last_det_time` timestamp NULL DEFAULT NULL COMMENT '最后检测时间',
  `detection_count` int NULL DEFAULT 0 COMMENT '检测次数',
  `defect_count` int NULL DEFAULT 0 COMMENT '缺陷数量',
  `safety_score` decimal(5, 2) NULL DEFAULT 100.00 COMMENT '安全评分',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_manhole_id`(`manhole_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE,
  INDEX `idx_district`(`district` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8886 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '井盖信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for repair
-- ----------------------------
DROP TABLE IF EXISTS `repair`;
CREATE TABLE `repair`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '维修记录ID',
  `detection_id` bigint NOT NULL COMMENT '关联检测记录ID',
  `manhole_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联井盖编号',
  `repair_user_id` bigint NULL DEFAULT NULL COMMENT '维修人员ID（关联sys_user）',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待维修, 1-维修中, 2-已完成',
  `assigned_time` datetime NULL DEFAULT NULL COMMENT '分配时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0-正常，1-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `before_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修前图片URL',
  `after_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维修后图片URL（存在uploads/repair/）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_detection_id`(`detection_id` ASC) USING BTREE,
  INDEX `idx_manhole_id`(`manhole_id` ASC) USING BTREE,
  INDEX `idx_repair_user_id`(`repair_user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_repair_detection` FOREIGN KEY (`detection_id`) REFERENCES `detection` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_repair_manhole` FOREIGN KEY (`manhole_id`) REFERENCES `manhole` (`manhole_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_repair_user` FOREIGN KEY (`repair_user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22273 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '维修记录表（精简版）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `passwd` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `head_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `type` tinyint NULL DEFAULT 0 COMMENT '用户类型：0-普通用户，1-管理员',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
