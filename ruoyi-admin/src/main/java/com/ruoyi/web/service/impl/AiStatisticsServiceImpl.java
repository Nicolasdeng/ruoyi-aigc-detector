package com.ruoyi.web.service.impl;

import com.ruoyi.web.mapper.AiDetectionMapper;
import com.ruoyi.web.service.IAiStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI检测统计信息Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class AiStatisticsServiceImpl implements IAiStatisticsService {
    
    @Autowired
    private AiDetectionMapper aiDetectionMapper;

    /**
     * 获取统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总检测数（按用户ID过滤）
        stats.put("totalCount", aiDetectionMapper.selectTotalCountByUserId(userId));
        
        // 各类型数量统计（按用户ID过滤）
        stats.put("aiGeneratedCount", aiDetectionMapper.selectCountByResultAndUserId("AI_GENERATED", userId));
        stats.put("humanCreatedCount", aiDetectionMapper.selectCountByResultAndUserId("HUMAN_CREATED", userId));
        stats.put("uncertainCount", aiDetectionMapper.selectCountByResultAndUserId("UNCERTAIN", userId));
        
        // 今日检测数（按用户ID过滤）
        stats.put("todayCount", aiDetectionMapper.selectTodayCountByUserId(userId));
        
        return stats;
    }

    /**
     * 获取今日统计信息
     * 
     * @param userId 用户ID
     * @return 今日统计信息
     */
    @Override
    public Map<String, Object> getTodayStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 今日总检测数（按用户ID过滤）
        stats.put("todayTotal", aiDetectionMapper.selectTodayCountByUserId(userId));
        
        // 今日各类型统计（简化实现，实际应该添加对应的Mapper方法）
        // TODO: 后续可以添加按用户ID和日期过滤的各类型统计方法
        stats.put("todayAiGenerated", 0);
        stats.put("todayHumanCreated", 0);
        stats.put("todayUncertain", 0);
        
        return stats;
    }

    /**
     * 获取趋势统计信息
     * 
     * @param userId 用户ID
     * @param days 统计天数
     * @return 趋势统计信息
     */
    @Override
    public Map<String, Object> getTrendStatistics(Long userId, int days) {
        Map<String, Object> stats = new HashMap<>();
        
        // 简化实现，实际应该根据天数和用户ID查询历史数据
        // TODO: 后续可以添加按用户ID和日期范围查询趋势数据的Mapper方法
        stats.put("userId", userId);
        stats.put("days", days);
        stats.put("trend", "暂未实现详细趋势统计");
        
        return stats;
    }
}
