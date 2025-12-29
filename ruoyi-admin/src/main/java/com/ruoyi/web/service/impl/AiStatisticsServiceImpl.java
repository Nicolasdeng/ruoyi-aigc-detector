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
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总检测数
        stats.put("totalCount", aiDetectionMapper.selectTotalCount());
        
        // 各类型数量统计
        stats.put("aiGeneratedCount", aiDetectionMapper.selectCountByResult("AI_GENERATED"));
        stats.put("humanCreatedCount", aiDetectionMapper.selectCountByResult("HUMAN_CREATED"));
        stats.put("uncertainCount", aiDetectionMapper.selectCountByResult("UNCERTAIN"));
        
        // 今日检测数
        stats.put("todayCount", aiDetectionMapper.selectTodayCount());
        
        return stats;
    }

    /**
     * 获取今日统计信息
     * 
     * @return 今日统计信息
     */
    @Override
    public Map<String, Object> getTodayStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 今日总检测数
        stats.put("todayTotal", aiDetectionMapper.selectTodayCount());
        
        // 今日各类型统计（简化实现，实际应该添加对应的Mapper方法）
        stats.put("todayAiGenerated", 0);
        stats.put("todayHumanCreated", 0);
        stats.put("todayUncertain", 0);
        
        return stats;
    }

    /**
     * 获取趋势统计信息
     * 
     * @param days 统计天数
     * @return 趋势统计信息
     */
    @Override
    public Map<String, Object> getTrendStatistics(int days) {
        Map<String, Object> stats = new HashMap<>();
        
        // 简化实现，实际应该根据天数查询历史数据
        stats.put("days", days);
        stats.put("trend", "暂未实现详细趋势统计");
        
        return stats;
    }
}
