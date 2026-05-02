-- ----------------------------
-- 微信小程序用户表
-- ----------------------------
DROP TABLE IF EXISTS `wechat_user`;
CREATE TABLE `wechat_user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `openid` varchar(100) NOT NULL COMMENT '微信openid',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信unionid',
  `session_key` varchar(100) DEFAULT NULL COMMENT '微信会话密钥',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '用户头像URL',
  `gender` tinyint(1) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `country` varchar(50) DEFAULT NULL COMMENT '国家',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` char(1) DEFAULT '0' COMMENT '账号状态 0-正常 1-停用',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志 0-存在 2-删除',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_unionid` (`unionid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='微信小程序用户表';
