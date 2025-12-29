-- AI检测工具数据库表
-- ----------------------------
-- 检测记录表
-- ----------------------------
DROP TABLE IF EXISTS `ai_detection_record`;
CREATE TABLE `ai_detection_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `file_url` varchar(500) NOT NULL COMMENT '文件URL',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型：image/video/audio/text',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `detection_result` varchar(20) DEFAULT NULL COMMENT '检测结果：AI_GENERATED/HUMAN_CREATED/UNCERTAIN',
  `confidence_score` decimal(5,2) DEFAULT NULL COMMENT '置信度分数(0-100)',
  `detection_details` text COMMENT '检测详情(JSON格式)',
  `api_results` text COMMENT 'API检测结果汇总(JSON格式)',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/COMPLETED/FAILED',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_file_type` (`file_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI检测记录表';

-- ----------------------------
-- API配置表
-- ----------------------------
DROP TABLE IF EXISTS `ai_detection_api_config`;
CREATE TABLE `ai_detection_api_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `api_name` varchar(100) NOT NULL COMMENT 'API名称',
  `api_url` varchar(500) NOT NULL COMMENT 'API地址',
  `api_key` varchar(200) DEFAULT NULL COMMENT 'API密钥',
  `api_type` varchar(20) NOT NULL COMMENT 'API类型：image/video/audio/text',
  `weight` decimal(3,2) DEFAULT 1.00 COMMENT '权重(0-1)',
  `timeout` int(11) DEFAULT 30000 COMMENT '超时时间(毫秒)',
  `status` char(1) DEFAULT '0' COMMENT '状态：0启用 1禁用',
  `priority` int(11) DEFAULT 0 COMMENT '优先级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_api_type` (`api_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI检测API配置表';

-- 插入默认API配置示例
INSERT INTO `ai_detection_api_config` (`api_name`, `api_url`, `api_type`, `weight`, `status`, `priority`, `remark`) 
VALUES 
('Hugging Face API', 'https://api-inference.huggingface.co/models/umm-maybe/AI-image-detector', 'image', 0.40, '0', 1, 'Hugging Face AI图片检测模型'),
('Illuminarty API', 'https://api.illuminarty.ai/v1/analyze', 'image', 0.30, '0', 2, 'Illuminarty AI检测服务'),
('OpenAI Detect API', 'https://api.openai.com/v1/moderations', 'image', 0.30, '0', 3, 'OpenAI内容审核API');
