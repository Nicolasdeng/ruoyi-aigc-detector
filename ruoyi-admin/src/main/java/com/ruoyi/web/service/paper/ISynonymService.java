package com.ruoyi.web.service.paper;

import java.util.List;
import java.util.Map;

/**
 * 同义词服务接口
 * 提供学术论文常用词汇的同义词替换建议
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface ISynonymService
{
    /**
     * 获取指定词汇的同义词列表
     * 
     * @param word 原始词汇
     * @return 同义词列表，如果没有同义词则返回空列表
     */
    List<String> getSynonyms(String word);
    
    /**
     * 为文本中的词汇提供同义词替换建议
     * 
     * @param text 原始文本
     * @return Map<原词, List<同义词>>
     */
    Map<String, List<String>> getSynonymSuggestions(String text);
    
    /**
     * 智能替换文本中的词汇为同义词
     * 
     * @param text 原始文本
     * @param replacements Map<原词, 替换词>
     * @return 替换后的文本
     */
    String replaceWithSynonyms(String text, Map<String, String> replacements);
    
    /**
     * 自动优化文本（随机替换部分词汇）
     * 
     * @param text 原始文本
     * @param replaceRatio 替换比例（0.0-1.0）
     * @return 优化后的文本
     */
    String autoOptimize(String text, double replaceRatio);
    
    /**
     * 检查词汇是否有同义词
     * 
     * @param word 词汇
     * @return true-有同义词，false-无同义词
     */
    boolean hasSynonyms(String word);
    
    /**
     * 获取所有支持的词汇列表
     * 
     * @return 词汇列表
     */
    List<String> getAllSupportedWords();
}
