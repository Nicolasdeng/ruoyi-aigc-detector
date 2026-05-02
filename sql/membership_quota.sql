-- =============================================
-- 会员与配额管理系统数据库表
-- 创建时间: 2026-01-17
-- 说明: 支持每日配额、会员体系、订单管理、优惠券功能
-- =============================================

-- ----------------------------
-- 1. 用户每日配额表
-- ----------------------------
DROP TABLE IF EXISTS `user_daily_quota`;
CREATE TABLE `user_daily_quota` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `quota_date` date NOT NULL COMMENT '配额日期',
  `text_quota` int(11) NOT NULL DEFAULT 5 COMMENT '文本检测配额',
  `text_used` int(11) NOT NULL DEFAULT 0 COMMENT '文本检测已使用',
  `image_quota` int(11) NOT NULL DEFAULT 3 COMMENT '图片检测配额',
  `image_used` int(11) NOT NULL DEFAULT 0 COMMENT '图片检测已使用',
  `video_quota` int(11) NOT NULL DEFAULT 2 COMMENT '视频检测配额',
  `video_used` int(11) NOT NULL DEFAULT 0 COMMENT '视频检测已使用',
  `audio_quota` int(11) NOT NULL DEFAULT 2 COMMENT '音频检测配额',
  `audio_used` int(11) NOT NULL DEFAULT 0 COMMENT '音频检测已使用',
  `paper_quota` int(11) NOT NULL DEFAULT 2 COMMENT '论文检测配额',
  `paper_used` int(11) NOT NULL DEFAULT 0 COMMENT '论文检测已使用',
  `ad_bonus_text` int(11) NOT NULL DEFAULT 0 COMMENT '看广告获得的文本配额',
  `ad_bonus_video` int(11) NOT NULL DEFAULT 0 COMMENT '看广告获得的激励视频配额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `quota_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_quota_date` (`quota_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日配额表';

-- ----------------------------
-- 2. 用户会员信息表
-- ----------------------------
DROP TABLE IF EXISTS `user_membership`;
CREATE TABLE `user_membership` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `membership_type` varchar(20) NOT NULL DEFAULT 'FREE' COMMENT '会员类型: FREE-免费, GOLD-黄金, PLATINUM-铂金',
  `expire_time` datetime DEFAULT NULL COMMENT '会员到期时间',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有效: 0-无效, 1-有效',
  `auto_renew` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动续费: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_membership_type` (`membership_type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员信息表';

-- ----------------------------
-- 3. 会员订单表
-- ----------------------------
DROP TABLE IF EXISTS `membership_order`;
CREATE TABLE `membership_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `membership_type` varchar(20) NOT NULL COMMENT '会员类型: GOLD-黄金, PLATINUM-铂金',
  `duration_months` int(11) NOT NULL COMMENT '购买时长(月)',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `discount_price` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `final_price` decimal(10,2) NOT NULL COMMENT '实付金额',
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券ID',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式: WECHAT-微信支付',
  `transaction_id` varchar(128) DEFAULT NULL COMMENT '第三方交易流水号',
  `order_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING-待支付, PAID-已支付, CANCELLED-已取消, REFUNDED-已退款',
  `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单表';

-- ----------------------------
-- 4. 用户优惠券表
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `coupon_code` varchar(32) NOT NULL COMMENT '优惠券代码',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID(NULL表示公开券)',
  `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `discount_type` varchar(20) NOT NULL COMMENT '优惠类型: PERCENT-折扣, AMOUNT-减免',
  `discount_value` decimal(10,2) NOT NULL COMMENT '优惠值(折扣为百分比,减免为金额)',
  `min_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `applicable_membership` varchar(100) DEFAULT NULL COMMENT '适用会员类型(逗号分隔,NULL表示全部)',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `is_used` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已使用: 0-未使用, 1-已使用',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint(20) DEFAULT NULL COMMENT '使用的订单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_code` (`coupon_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_is_used` (`is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ----------------------------
-- 初始化数据
-- ----------------------------

-- 插入首月特惠优惠券模板(公开券)
INSERT INTO `user_coupon` (`coupon_code`, `user_id`, `coupon_name`, `discount_type`, `discount_value`, `min_amount`, `applicable_membership`, `expire_time`) 
VALUES 
('FIRST_GOLD_50', NULL, '黄金会员首月5折', 'PERCENT', 50.00, 0.00, 'GOLD', DATE_ADD(NOW(), INTERVAL 30 DAY)),
('FIRST_PLATINUM_50', NULL, '铂金会员首月5折', 'PERCENT', 50.00, 0.00, 'PLATINUM', DATE_ADD(NOW(), INTERVAL 30 DAY));

-- 说明: 
-- 1. 每日配额会在用户首次使用时自动创建
-- 2. 用户注册时会自动创建FREE会员记录
-- 3. 优惠券可以通过管理后台或API动态创建
