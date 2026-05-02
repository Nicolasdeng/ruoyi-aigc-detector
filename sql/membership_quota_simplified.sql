-- =============================================
-- 简化版会员与配额管理系统数据库表
-- 创建时间: 2026-01-18
-- 说明: 单一黄金会员体系，每日免费配额，保留广告功能
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
  `image_quota` int(11) NOT NULL DEFAULT 5 COMMENT '图片检测配额',
  `image_used` int(11) NOT NULL DEFAULT 0 COMMENT '图片检测已使用',
  `video_quota` int(11) NOT NULL DEFAULT 5 COMMENT '视频检测配额',
  `video_used` int(11) NOT NULL DEFAULT 0 COMMENT '视频检测已使用',
  `audio_quota` int(11) NOT NULL DEFAULT 5 COMMENT '音频检测配额',
  `audio_used` int(11) NOT NULL DEFAULT 0 COMMENT '音频检测已使用',
  `paper_quota` int(11) NOT NULL DEFAULT 5 COMMENT '论文检测配额',
  `paper_used` int(11) NOT NULL DEFAULT 0 COMMENT '论文检测已使用',
  `ad_bonus_quota` int(11) NOT NULL DEFAULT 0 COMMENT '看广告获得的额外配额',
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
  `membership_type` varchar(20) NOT NULL DEFAULT 'FREE' COMMENT '会员类型: FREE-免费用户, GOLD-黄金会员',
  `expire_time` datetime DEFAULT NULL COMMENT '会员到期时间',
  `is_active` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否有效: 0-无效, 1-有效',
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
  `package_type` varchar(20) NOT NULL COMMENT '套餐类型: WEEK-周卡, MONTH-月卡',
  `duration_days` int(11) NOT NULL COMMENT '购买时长(天): 7-周卡, 30-月卡',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `final_price` decimal(10,2) NOT NULL COMMENT '实付金额',
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
-- 删除优惠券表(不再使用)
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;

-- ----------------------------
-- 初始化说明
-- ----------------------------
-- 1. 普通用户(FREE)每天各功能免费5次
-- 2. 黄金会员(GOLD)无限次使用
-- 3. 套餐价格:
--    - 周卡: 原价5.9元, 现价2.9元/周
--    - 月卡: 原价19.9元, 现价9.9元/月
-- 4. 广告功能保留,用于后续增加额外配额
-- 5. 每日配额会在用户首次使用时自动创建
-- 6. 用户注册时会自动创建FREE会员记录
