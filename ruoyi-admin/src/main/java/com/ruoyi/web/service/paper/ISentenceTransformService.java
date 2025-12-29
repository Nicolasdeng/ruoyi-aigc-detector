package com.ruoyi.web.service.paper;

import java.util.List;
import java.util.Map;

/**
 * 句式变换服务接口
 * 提供学术论文句式多样化变换建议
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface ISentenceTransformService
{
    /**
     * 分析文本并提供句式变换建议
     * 
     * @param text 原始文本
     * @return 句式变换建议列表
     */
    List<Map<String, String>> analyzeSentencePatterns(String text);
    
    /**
     * 变换指定句子的句式
     * 
     * @param sentence 原始句子
     * @return 变换后的句子列表（多种变换方式）
     */
    List<String> transformSentence(String sentence);
    
    /**
     * 应用句式变换规则
     * 
     * @param text 原始文本
     * @param transformations Map<原句, 变换后的句子>
     * @return 变换后的文本
     */
    String applyTransformations(String text, Map<String, String> transformations);
    
    /**
     * 自动优化句式（智能变换）
     * 
     * @param text 原始文本
     * @param transformRatio 变换比例（0.0-1.0）
     * @return 优化后的文本
     */
    String autoTransform(String text, double transformRatio);
    
    /**
     * 检测过于规范的句式模式
     * 
     * @param text 原始文本
     * @return 需要调整的句子列表
     */
    List<String> detectUniformPatterns(String text);
    
    /**
     * 获取所有支持的句式变换规则
     * 
     * @return 规则描述列表
     */
    List<String> getAllTransformRules();
}
