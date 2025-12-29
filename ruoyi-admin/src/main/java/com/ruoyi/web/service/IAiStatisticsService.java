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
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取今日统计信息
     * 
     * @return 今日统计信息
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取趋势统计信息
     * 
     * @param days 统计天数
     * @return 趋势统计信息
     */
    Map<String, Object> getTrendStatistics(int days);
}
