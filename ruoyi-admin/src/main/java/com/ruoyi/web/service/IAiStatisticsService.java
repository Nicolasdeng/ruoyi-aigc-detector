package com.ruoyi.web.service;

import java.util.Map;

/**
 * AI检测统计信息Service接口
 * 
 * @author ruoyi
 */
public interface IAiStatisticsService {
    
    /**
     * 获取统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getStatistics(Long userId);

    /**
     * 获取今日统计信息
     * 
     * @param userId 用户ID
     * @return 今日统计信息
     */
    Map<String, Object> getTodayStatistics(Long userId);

    /**
     * 获取趋势统计信息
     * 
     * @param userId 用户ID
     * @param days 统计天数
     * @return 趋势统计信息
     */
    Map<String, Object> getTrendStatistics(Long userId, int days);
}
