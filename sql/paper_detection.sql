-- 论文检测相关表

-- 1. 论文检测记录表
CREATE TABLE `paper_detection_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '检测ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '论文标题',
  `content` longtext NOT NULL COMMENT '论文内容',
  `file_url` varchar(500) DEFAULT NULL COMMENT '上传文件URL',
  `word_count` int(11) DEFAULT 0 COMMENT '字数',
  `ai_risk_level` varchar(20) DEFAULT NULL COMMENT 'AI风险等级(low/medium/high)',
  `ai_score` decimal(5,2) DEFAULT 0.00 COMMENT 'AI概率评分(0-100)',
  `duplicate_level` varchar(20) DEFAULT NULL COMMENT '重复度等级(low/medium/high)',
  `duplicate_score` decimal(5,2) DEFAULT 0.00 COMMENT '重复度评分(0-100)',
  `style_score` decimal(5,2) DEFAULT 0.00 COMMENT '风格异常评分(0-100)',
  `total_paragraphs` int(11) DEFAULT 0 COMMENT '总段落数',
  `high_risk_paragraphs` int(11) DEFAULT 0 COMMENT '高风险段落数',
  `status` varchar(20) DEFAULT 'processing' COMMENT '检测状态(processing/completed/failed)',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `detect_duration` int(11) DEFAULT 0 COMMENT '检测耗时(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论文检测记录表';

-- 2. 段落检测详情表
CREATE TABLE `paper_paragraph_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '详情ID',
  `detection_id` bigint(20) NOT NULL COMMENT '检测记录ID',
  `paragraph_index` int(11) NOT NULL COMMENT '段落索引(从1开始)',
  `paragraph_content` text NOT NULL COMMENT '段落内容',
  `word_count` int(11) DEFAULT 0 COMMENT '段落字数',
  `ai_risk` decimal(5,2) DEFAULT 0.00 COMMENT 'AI风险评分(0-100)',
  `risk_level` varchar(20) DEFAULT NULL COMMENT '风险等级(low/medium/high)',
  `risk_types` varchar(500) DEFAULT NULL COMMENT '风险类型JSON数组',
  `risk_reasons` text DEFAULT NULL COMMENT '风险原因JSON',
  `suggestions` text DEFAULT NULL COMMENT '修改建议JSON',
  `api_results` text DEFAULT NULL COMMENT 'API检测结果JSON',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_detection_id` (`detection_id`),
  KEY `idx_risk_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='段落检测详情表';

-- 3. 优化历史表
CREATE TABLE `paper_optimization_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `detection_id` bigint(20) NOT NULL COMMENT '检测记录ID',
  `paragraph_id` bigint(20) NOT NULL COMMENT '段落ID',
  `original_content` text NOT NULL COMMENT '原始内容',
  `optimized_content` text NOT NULL COMMENT '优化后内容',
  `optimization_level` varchar(20) DEFAULT 'medium' COMMENT '优化强度(light/medium/strong)',
  `optimization_params` text DEFAULT NULL COMMENT '优化参数JSON',
  `ai_risk_before` decimal(5,2) DEFAULT 0.00 COMMENT '优化前AI风险',
  `ai_risk_after` decimal(5,2) DEFAULT 0.00 COMMENT '优化后AI风险',
  `improvement_rate` decimal(5,2) DEFAULT 0.00 COMMENT '改善率(%)',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_detection_id` (`detection_id`),
  KEY `idx_paragraph_id` (`paragraph_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优化历史表';

-- 5. 检测统计表
CREATE TABLE `paper_detection_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_detections` int(11) DEFAULT 0 COMMENT '总检测次数',
  `completed_detections` int(11) DEFAULT 0 COMMENT '完成检测次数',
  `failed_detections` int(11) DEFAULT 0 COMMENT '失败检测次数',
  `total_optimizations` int(11) DEFAULT 0 COMMENT '总优化次数',
  `suggestion_clicks` int(11) DEFAULT 0 COMMENT '建议点击次数',
  `new_users` int(11) DEFAULT 0 COMMENT '新增用户数',
  `active_users` int(11) DEFAULT 0 COMMENT '活跃用户数',
  `avg_ai_score` decimal(5,2) DEFAULT 0.00 COMMENT '平均AI风险分',
  `avg_detect_duration` int(11) DEFAULT 0 COMMENT '平均检测耗时(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测统计表';

-- 插入默认统计记录
INSERT INTO `paper_detection_statistics` (`stat_date`) VALUES (CURDATE());
