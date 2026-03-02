-- ============================================================
-- Spring AI Alibaba Multi-Agent Demo 数据库表结构
-- ============================================================
-- 数据库: coffee_shop
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `coffee_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `coffee_shop`;

-- ============================================================
-- 0. 字典参考表 - 统一管理所有枚举值
-- ============================================================
DROP TABLE IF EXISTS `reference`;
CREATE TABLE `reference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `ref_type` VARCHAR(50) NOT NULL COMMENT '字典类型: user_status, sweetness, ice_level, drink_type, strength, product_status, feedback_type',
    `ref_key` VARCHAR(50) NOT NULL COMMENT '字典键',
    `ref_value` VARCHAR(100) NOT NULL COMMENT '字典值',
    `ref_code` TINYINT NOT NULL COMMENT '字典代码',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用: 0-否, 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ref_type_key` (`ref_type`, `ref_key`),
    KEY `idx_ref_type` (`ref_type`),
    KEY `idx_ref_code` (`ref_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典参考表';

-- ============================================================
-- 1. 用户表 (order-mcp-server)
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 1.1. 用户偏好表 (新增)
-- ============================================================
DROP TABLE IF EXISTS `user_preference`;
CREATE TABLE `user_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `preferred_sweetness` TINYINT COMMENT '偏好甜度: 1-无糖, 2-微糖, 3-半糖, 4-少糖, 5-标准糖',
    `preferred_ice_level` TINYINT COMMENT '偏好冰度/温度: 1-热饮, 2-温饮, 3-去冰, 4-少冰, 5-正常冰',
    `preferred_drink_type` VARCHAR(50) COMMENT '偏好饮品类型: 拿铁, 美式, 卡布奇诺, 摩卡等',
    `preferred_strength` TINYINT COMMENT '偏好浓度: 1-轻度, 2-中度, 3-重度',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    CONSTRAINT `fk_user_preference_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好表';

-- ============================================================
-- 1.2. 用户偏好记忆表 (新增)
-- ============================================================
DROP TABLE IF EXISTS `user_preference_memory`;
CREATE TABLE `user_preference_memory` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记忆ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `preference_content` TEXT NOT NULL COMMENT '偏好内容(JSON格式存储)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_user_preference_memory_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户偏好记忆表';

-- ============================================================
-- 2. 产品表 (order-mcp-server / consult-sub-agent) - 咖啡店
-- ============================================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `description` TEXT COMMENT '产品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    `shelf_time` INT NOT NULL DEFAULT 90 COMMENT '保质期(天)',
    `preparation_time` INT NOT NULL DEFAULT 5 COMMENT '制作时间(分钟)',
    `is_seasonal` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否季节性产品: 0-否, 1-是',
    `season_start` DATE COMMENT '季节开始日期',
    `season_end` DATE COMMENT '季节结束日期',
    `is_regional` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否区域限定: 0-否, 1-是',
    `available_regions` VARCHAR(500) COMMENT '可用区域(JSON格式或逗号分隔)',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_price` (`price`),
    KEY `idx_status` (`status`),
    KEY `idx_seasonal` (`is_seasonal`, `season_start`, `season_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- ============================================================
-- 3. 订单表 (order-mcp-server) - 咖啡订单
-- ============================================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_id` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '产品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `sweetness` TINYINT COMMENT '甜度: 1-无糖, 2-微糖, 3-半糖, 4-少糖, 5-标准糖',
    `ice_level` TINYINT COMMENT '冰度/温度: 1-热饮, 2-温饮, 3-去冰, 4-少冰, 5-正常冰',
    `quantity` INT NOT NULL COMMENT '数量',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_order_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 4. 反馈表 (feedback-mcp-server)
-- ============================================================
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
    `order_id` VARCHAR(64) COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `feedback_type` TINYINT NOT NULL COMMENT '反馈类型: 1-产品反馈, 2-服务反馈, 3-投诉, 4-建议',
    `rating` TINYINT COMMENT '评分: 1-5星',
    `content` TEXT NOT NULL COMMENT '反馈内容',
    `solution` TEXT COMMENT '处理方案',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_feedback_type` (`feedback_type`),
    KEY `idx_rating` (`rating`),
    KEY `idx_created_at` (`created_at`),
    CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';

-- ============================================================
-- 索引说明
-- ============================================================
-- uk_*: 唯一索引
-- idx_*: 普通索引
-- fk_*: 外键约束
-- ============================================================
