package com.ruoyi.web.service.paper;

import java.util.List;
import java.util.Map;

/**
 * 段落优化服务接口
 * 提供学术论文段落结构优化建议
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface IParagraphOptimizerService
{
    /**
     * 分析段落并生成优化建议
     * 
     * @param paragraph 段落文本
     * @return 优化建议列表
     */
    List<String> analyzeAndSuggest(String paragraph);
    
    /**
     * 检测段落长度问题
     * 
     * @param paragraph 段落文本
     * @return 长度问题描述及建议
     */
    Map<String, Object> checkLength(String paragraph);
    
    /**
     * 检测句子长度分布
     * 
     * @param paragraph 段落文本
     * @return 句子长度分析结果
     */
    Map<String, Object> analyzeSentenceLength(String paragraph);
    
    /**
     * 检测逻辑连贯性
     * 
     * @param paragraph 段落文本
     * @return 连贯性分析结果
     */
    Map<String, Object> checkCoherence(String paragraph);
    
    /**
     * 检测词汇重复问题
     * 
     * @param paragraph 段落文本
     * @return 重复词汇列表及建议
     */
    Map<String, Object> detectRepetitiveWords(String paragraph);
    
    /**
     * 生成段落重组建议
     * 
     * @param paragraph 段落文本
     * @return 重组建议
     */
    List<String> suggestReorganization(String paragraph);
    
    /**
     * 优化段落结构
     * 
     * @param paragraph 原始段落
     * @return 优化后的段落
     */
    String optimizeStructure(String paragraph);
    
    /**
     * 综合评分
     * 
     * @param paragraph 段落文本
     * @return 评分（0-100）
     */
    int calculateScore(String paragraph);
}
