package com.ruoyi.web.service.paper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AI模型检测器接口
 * 用于识别特定AI模型生成的文本特征
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public interface IAiModelDetector
{
    /**
     * 检测文本是否符合该AI模型的特征
     * 
     * @param content 待检测文本内容
     * @return 匹配度分数 (0-100)
     */
    BigDecimal detectModel(String content);
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getDetectorName();
    
    /**
     * 获取AI模型名称
     * 
     * @return AI模型名称
     */
    String getModelName();
    
    /**
     * 获取详细的特征匹配信息
     * 
     * @param content 待检测文本内容
     * @return 特征匹配详情 Map<特征名, 特征值描述>
     */
    Map<String, String> getFeatureDetails(String content);
    
    /**
     * 生成针对该模型的优化建议
     * 
     * @param content 待检测文本内容
     * @return 优化建议列表
     */
    List<String> generateSuggestions(String content);

    List<String> generateSuggestions(String text, double score);

    /**
     * 生成针对该模型的优化建议（带匹配分数）
     * 
     * @param content 待检测文本内容
     * @param matchScore 匹配分数
     * @return 优化建议列表
     */
    List<String> generateSuggestions(String content, BigDecimal matchScore);
}
