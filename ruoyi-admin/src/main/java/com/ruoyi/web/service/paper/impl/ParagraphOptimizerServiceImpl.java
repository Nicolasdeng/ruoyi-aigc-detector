package com.ruoyi.web.service.paper.impl;

import com.ruoyi.web.service.paper.IParagraphOptimizerService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 段落优化服务实现
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class ParagraphOptimizerServiceImpl implements IParagraphOptimizerService
{
    @Override
    public List<String> analyzeAndSuggest(String paragraph)
    {
        List<String> suggestions = new ArrayList<>();
        
        // 长度检查
        Map<String, Object> lengthResult = checkLength(paragraph);
        if ((boolean) lengthResult.get("hasIssue")) {
            suggestions.add((String) lengthResult.get("suggestion"));
        }
        
        // 句子长度分析
        Map<String, Object> sentenceResult = analyzeSentenceLength(paragraph);
        if ((boolean) sentenceResult.get("hasIssue")) {
            suggestions.add((String) sentenceResult.get("suggestion"));
        }
        
        // 逻辑连贯性
        Map<String, Object> coherenceResult = checkCoherence(paragraph);
        if ((boolean) coherenceResult.get("hasIssue")) {
            suggestions.add((String) coherenceResult.get("suggestion"));
        }
        
        // 词汇重复
        Map<String, Object> repetitiveResult = detectRepetitiveWords(paragraph);
        if ((boolean) repetitiveResult.get("hasIssue")) {
            suggestions.add((String) repetitiveResult.get("suggestion"));
        }
        
        return suggestions;
    }
    
    @Override
    public Map<String, Object> checkLength(String paragraph)
    {
        Map<String, Object> result = new HashMap<>();
        int length = paragraph.length();
        
        if (length > 250) {
            result.put("hasIssue", true);
            result.put("length", length);
            result.put("suggestion", "段落过长（" + length + "字），建议拆分为2-3个较短段落，提高可读性");
        } else if (length < 50) {
            result.put("hasIssue", true);
            result.put("length", length);
            result.put("suggestion", "段落过短（" + length + "字），建议扩充内容或与其他段落合并");
        } else {
            result.put("hasIssue", false);
            result.put("length", length);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> analyzeSentenceLength(String paragraph)
    {
        Map<String, Object> result = new HashMap<>();
        String[] sentences = paragraph.split("[。！？]");
        
        List<Integer> lengths = new ArrayList<>();
        int totalLength = 0;
        int longSentences = 0;
        
        for (String sentence : sentences) {
            int length = sentence.trim().length();
            if (length > 0) {
                lengths.add(length);
                totalLength += length;
                if (length > 40) {
                    longSentences++;
                }
            }
        }
        
        if (lengths.isEmpty()) {
            result.put("hasIssue", false);
            return result;
        }
        
        double avgLength = (double) totalLength / lengths.size();
        
        result.put("sentenceCount", lengths.size());
        result.put("avgLength", avgLength);
        result.put("longSentences", longSentences);
        
        if (longSentences > lengths.size() / 2) {
            result.put("hasIssue", true);
            result.put("suggestion", "存在" + longSentences + "个长句（>40字），建议拆分为短句，增加句式多样性");
        } else if (avgLength > 35) {
            result.put("hasIssue", true);
            result.put("suggestion", "平均句长偏长（" + String.format("%.1f", avgLength) + "字），建议适当缩短句子");
        } else {
            result.put("hasIssue", false);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> checkCoherence(String paragraph)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 检查逻辑连接词
        String[] connectors = {
            "因此", "所以", "然而", "但是", "而且", "并且",
            "首先", "其次", "最后", "综上所述", "总之"
        };
        
        int connectorCount = 0;
        List<String> foundConnectors = new ArrayList<>();
        
        for (String connector : connectors) {
            if (paragraph.contains(connector)) {
                connectorCount++;
                foundConnectors.add(connector);
            }
        }
        
        result.put("connectorCount", connectorCount);
        result.put("connectors", foundConnectors);
        
        // 句子数量
        String[] sentences = paragraph.split("[。！？]");
        int sentenceCount = 0;
        for (String s : sentences) {
            if (s.trim().length() > 0) sentenceCount++;
        }
        
        if (sentenceCount >= 3 && connectorCount == 0) {
            result.put("hasIssue", true);
            result.put("suggestion", "缺少逻辑连接词，建议添加'因此'、'然而'等词汇增强逻辑性");
        } else if (connectorCount > sentenceCount / 2) {
            result.put("hasIssue", true);
            result.put("suggestion", "逻辑连接词使用过多，显得过于规范，建议减少使用");
        } else {
            result.put("hasIssue", false);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> detectRepetitiveWords(String paragraph)
    {
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> wordCount = new HashMap<>();
        
        // 简单分词（按2-4字）
        for (int len = 2; len <= 4; len++) {
            for (int i = 0; i <= paragraph.length() - len; i++) {
                String word = paragraph.substring(i, i + len);
                // 过滤标点符号
                if (word.matches(".*[a-zA-Z0-9。，！？、；：''（）\\[\\]【】].*")) {
                    continue;
                }
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        
        // 找出重复次数>=3的词
        List<Map<String, Object>> repetitiveWords = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() >= 3) {
                Map<String, Object> item = new HashMap<>();
                item.put("word", entry.getKey());
                item.put("count", entry.getValue());
                repetitiveWords.add(item);
            }
        }
        
        result.put("repetitiveWords", repetitiveWords);
        
        if (!repetitiveWords.isEmpty()) {
            result.put("hasIssue", true);
            StringBuilder sb = new StringBuilder("发现重复词汇：");
            for (int i = 0; i < Math.min(3, repetitiveWords.size()); i++) {
                Map<String, Object> item = repetitiveWords.get(i);
                sb.append("\"").append(item.get("word")).append("\"(").append(item.get("count")).append("次) ");
            }
            sb.append("，建议使用同义词替换");
            result.put("suggestion", sb.toString());
        } else {
            result.put("hasIssue", false);
        }
        
        return result;
    }
    
    @Override
    public List<String> suggestReorganization(String paragraph)
    {
        List<String> suggestions = new ArrayList<>();
        String[] sentences = paragraph.split("[。！？]");
        
        if (sentences.length >= 5) {
            suggestions.add("段落包含较多句子，可考虑按主题拆分");
        }
        
        // 检查是否有明显的主题转换
        boolean hasTransition = paragraph.contains("另外") || 
                               paragraph.contains("此外") || 
                               paragraph.contains("同时");
        
        if (hasTransition && sentences.length >= 4) {
            suggestions.add("检测到主题转换词，建议在此处拆分段落");
        }
        
        return suggestions;
    }
    
    @Override
    public String optimizeStructure(String paragraph)
    {
        // 基础优化：调整格式
        String optimized = paragraph.trim();
        
        // 移除多余空格
        optimized = optimized.replaceAll("\\s+", "");
        
        // 确保句号后有适当间隔（对于拆分）
        optimized = optimized.replace("。", "。 ");
        
        return optimized.trim();
    }
    
    @Override
    public int calculateScore(String paragraph)
    {
        int score = 100;
        
        // 长度检查 (-10分)
        Map<String, Object> lengthResult = checkLength(paragraph);
        if ((boolean) lengthResult.get("hasIssue")) {
            score -= 10;
        }
        
        // 句子长度 (-15分)
        Map<String, Object> sentenceResult = analyzeSentenceLength(paragraph);
        if ((boolean) sentenceResult.get("hasIssue")) {
            score -= 15;
        }
        
        // 逻辑连贯性 (-20分)
        Map<String, Object> coherenceResult = checkCoherence(paragraph);
        if ((boolean) coherenceResult.get("hasIssue")) {
            score -= 20;
        }
        
        // 词汇重复 (-15分)
        Map<String, Object> repetitiveResult = detectRepetitiveWords(paragraph);
        if ((boolean) repetitiveResult.get("hasIssue")) {
            score -= 15;
        }
        
        return Math.max(0, score);
    }
}
