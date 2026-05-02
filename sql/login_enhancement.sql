-- ========================================
-- 登录功能增强SQL脚本
-- ========================================

-- 1. 登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
  `login_ip` varchar(128) DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) DEFAULT '' COMMENT '登录地点',
  `device_type` varchar(50) DEFAULT '' COMMENT '设备类型(miniprogram/app/web)',
  `device_model` varchar(100) DEFAULT '' COMMENT '设备型号',
  `system_version` varchar(50) DEFAULT '' COMMENT '系统版本',
  `app_version` varchar(50) DEFAULT '' COMMENT '小程序/应用版本',
  `login_result` varchar(50) DEFAULT 'success' COMMENT '登录结果(success/fail)',
  `fail_reason` varchar(255) DEFAULT '' COMMENT '失败原因',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_openid` (`openid`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='系统登录日志表';

-- 2. Token黑名单表（作为Redis的备份）
DROP TABLE IF EXISTS `sys_token_blacklist`;
CREATE TABLE `sys_token_blacklist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `token` varchar(500) NOT NULL COMMENT 'Token值',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `blacklist_time` datetime DEFAULT NULL COMMENT '加入黑名单时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `reason` varchar(255) DEFAULT '' COMMENT '加入黑名单原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`(255)),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- 3. 给wechat_user表添加一些增强字段
ALTER TABLE `wechat_user` ADD COLUMN `device_id` varchar(100) DEFAULT NULL COMMENT '设备ID' AFTER `avatar_url`;
ALTER TABLE `wechat_user` ADD COLUMN `last_login_ip` varchar(128) DEFAULT NULL COMMENT '最后登录IP' AFTER `device_id`;
ALTER TABLE `wechat_user` ADD COLUMN `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间' AFTER `last_login_ip`;
ALTER TABLE `wechat_user` ADD COLUMN `login_count` int(11) DEFAULT 0 COMMENT '登录次数' AFTER `last_login_time`;
ALTER TABLE `wechat_user` ADD COLUMN `status` tinyint(1) DEFAULT 1 COMMENT '账号状态(0禁用 1正常)' AFTER `login_count`;
ALTER TABLE `wechat_user` ADD COLUMN `agree_protocol` tinyint(1) DEFAULT 0 COMMENT '是否同意协议(0未同意 1已同意)' AFTER `status`;
ALTER TABLE `wechat_user` ADD COLUMN `agree_time` datetime DEFAULT NULL COMMENT '同意协议时间' AFTER `agree_protocol`;

-- 4. 创建用户协议表
DROP TABLE IF EXISTS `sys_user_agreement`;
CREATE TABLE `sys_user_agreement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `agreement_type` varchar(50) NOT NULL COMMENT '协议类型(user_agreement/privacy_policy)',
  `title` varchar(200) NOT NULL COMMENT '协议标题',
  `content` text COMMENT '协议内容',
  `version` varchar(20) DEFAULT '1.0' COMMENT '协议版本',
  `status` tinyint(1) DEFAULT 1 COMMENT '状态(0停用 1启用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`agreement_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户协议表';

-- 5. 插入默认协议数据
INSERT INTO `sys_user_agreement` (`agreement_type`, `title`, `content`, `version`, `status`, `create_time`) VALUES
('user_agreement', 'AI检测小程序用户服务协议', 
'## 一、服务条款的确认和接受

欢迎使用AI检测小程序！在您使用本小程序服务前，请务必仔细阅读并理解本协议。

## 二、服务说明

1. AI检测小程序为用户提供文本、图片、音频、视频等内容的AI生成检测服务
2. 本服务仅供参考，检测结果不作为最终判断依据
3. 我们将持续优化检测算法，提高检测准确率

## 三、用户账号

1. 您需通过微信授权登录使用本服务
2. 您应妥善保管账号信息，账号仅供本人使用
3. 如发现账号被盗用，请立即联系我们

## 四、用户行为规范

1. 不得上传违法、违规内容进行检测
2. 不得恶意攻击、破坏系统正常运行
3. 不得利用本服务从事任何违法活动

## 五、隐私保护

我们重视您的隐私保护，具体内容请查看《隐私政策》

## 六、免责声明

1. 检测结果仅供参考，不承担因使用检测结果产生的任何责任
2. 因不可抗力导致的服务中断，我们不承担责任

## 七、协议修改

我们保留随时修改本协议的权利，修改后的协议将在小程序内公布

如有疑问，请联系客服。',
'1.0', 1, NOW()),

('privacy_policy', 'AI检测小程序隐私政策', 
'## 一、信息收集

我们会收集以下信息以提供更好的服务：

### 1.1 用户信息
- 微信昵称和头像（需您授权）
- 微信OpenID（用于识别用户身份）

### 1.2 使用信息
- 检测历史记录
- 设备信息（型号、系统版本等）
- 日志信息

## 二、信息使用

我们收集的信息将用于：
1. 提供AI检测服务
2. 改进服务质量
3. 统计分析
4. 账号安全保护

## 三、信息保护

### 3.1 安全措施
- 采用行业标准的加密技术
- 严格的访问控制
- 定期安全审计

### 3.2 数据存储
- 数据存储在安全的服务器中
- 采取防火墙、加密等技术措施

## 四、信息共享

我们不会向第三方出售、出租或分享您的个人信息，除非：
1. 获得您的明确同意
2. 法律法规要求
3. 保护用户或公众安全所必需

## 五、您的权利

您有权：
1. 访问您的个人信息
2. 更正不准确的信息
3. 删除您的账号和数据
4. 拒绝特定的信息处理

## 六、未成年人保护

如您为未成年人，请在监护人陪同下阅读本政策，并在监护人同意后使用本服务。

## 七、政策更新

我们可能会不定期更新本隐私政策，更新后将在小程序内公告。

## 八、联系我们

如有任何疑问，请通过小程序内联系方式与我们联系。

最后更新日期：2026年1月11日',
'1.0', 1, NOW());

-- 6. 创建用户协议签署记录表
DROP TABLE IF EXISTS `sys_user_agreement_record`;
CREATE TABLE `sys_user_agreement_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `agreement_id` bigint(20) NOT NULL COMMENT '协议ID',
  `agreement_type` varchar(50) NOT NULL COMMENT '协议类型',
  `agreement_version` varchar(20) DEFAULT NULL COMMENT '协议版本',
  `agree_time` datetime DEFAULT NULL COMMENT '同意时间',
  `ip_address` varchar(128) DEFAULT NULL COMMENT 'IP地址',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_agreement_id` (`agreement_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户协议签署记录表';
