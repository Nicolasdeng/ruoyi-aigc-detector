package com.ruoyi.web.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 论文文本分析器接口
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface IPaperTextAnalyzer 
{
    /**
     * 分析段落AI风险
     * 
     * @param paragraph 段落文本
     * @return 风险评分(0-100)
     */
    BigDecimal analyzeParagraphRisk(String paragraph);
    
    /**
     * 识别风险类型
     * 
     * @param paragraph 段落文本
     * @param score 风险评分
     * @return 风险类型列表
     */
    List<String> identifyRiskTypes(String paragraph, BigDecimal score);
    
    /**
     * 生成修改建议
     * 
     * @param paragraph 段落文本
     * @param riskTypes 风险类型列表
     * @return 修改建议
     */
    Map<String, Object> generateSuggestions(String paragraph, List<String> riskTypes);
    
    /**
     * 计算段落字数
     * 
     * @param paragraph 段落文本
     * @return 字数
     */
    int countWords(String paragraph);
    
    /**
     * 将段落文本分句
     * 
     * @param paragraph 段落文本
     * @return 句子列表
     */
    List<String> splitSentences(String paragraph);
    
    /**
     * 计算文本重复度
     * 
     * @param content 文本内容
     * @return 重复度评分(0-100)
     */
    BigDecimal calculateDuplicateScore(String content);
    
    /**
     * 计算风格异常评分
     * 
     * @param content 文本内容
     * @return 风格异常评分(0-100)
     */
    BigDecimal calculateStyleScore(String content);
    
    /**
     * 判断风险等级
     * 
     * @param score 风险评分
     * @return 风险等级(low/medium/high)
     */
    String getRiskLevel(BigDecimal score);
}
