package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 语义连贯性检测器
 * 基于文本的语义连贯性特征进行AI检测
 * 
 * @author ruoyi
 */
@Component
public class SemanticCoherenceDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(SemanticCoherenceDetector.class);
    
    private static final String DETECTOR_NAME = "语义连贯性分析";
    private static final double WEIGHT = 0.20;
    
    // 连接词（AI更倾向于使用连接词）
    private static final String[] CONNECTORS = {
        "因此", "所以", "但是", "然而", "而且", "并且", "此外",
        "另外", "同时", "首先", "其次", "最后", "总之",
        "however", "therefore", "moreover", "furthermore", "thus"
    };
    
    @Override
    public Map<String, Object> detect(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            String lowerText = text.toLowerCase();
            
            // 连接词使用频率
            int connectorCount = countConnectors(lowerText);
            double connectorRatio = (double) connectorCount / Math.max(text.split("[。！？.!?]+").length, 1);
            
            // 重复短语检测
            double repetitionScore = calculateRepetitionScore(text);
            
            // 句子长度变化
            double sentenceLengthVariance = calculateSentenceLengthVariance(text);
            
            // 计算AI概率分数
            double score = calculateAiScore(connectorRatio, repetitionScore, 
                sentenceLengthVariance, text.length());
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", DETECTOR_NAME);
            result.put("score", Math.round(score * 10.0) / 10.0);
            result.put("result", getResult(score));
            result.put("confidence", score / 100.0);
            result.put("responseTime", System.currentTimeMillis() - startTime);
            
            // 详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("connectorCount", connectorCount);
            details.put("connectorRatio", Math.round(connectorRatio * 100.0) / 100.0);
            details.put("repetitionScore", Math.round(repetitionScore * 100.0) / 100.0);
            details.put("sentenceLengthVariance", Math.round(sentenceLengthVariance * 100.0) / 100.0);
            result.put("details", details);
            
            log.debug("语义连贯性检测完成 - 分数: {}, 连接词比率: {}, 重复度: {}", 
                score, connectorRatio, repetitionScore);
            
            return result;
            
        } catch (Exception e) {
            log.error("语义连贯性检测失败", e);
            return createErrorResult(System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 统计连接词数量
     */
    private int countConnectors(String text) {
        int count = 0;
        for (String connector : CONNECTORS) {
            if (text.contains(connector.toLowerCase())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 计算重复短语分数
     * AI生成的文本可能包含更多重复的短语结构
     */
    private double calculateRepetitionScore(String text) {
        String[] sentences = text.split("[。！？.!?]+");
        if (sentences.length < 2) {
            return 0.5;
        }
        
        // 提取每句话的前几个字
        Map<String, Integer> prefixCount = new HashMap<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.length() >= 3) {
                String prefix = trimmed.substring(0, Math.min(3, trimmed.length()));
                prefixCount.put(prefix, prefixCount.getOrDefault(prefix, 0) + 1);
            }
        }
        
        // 计算重复度
        int repetitions = 0;
        for (int count : prefixCount.values()) {
            if (count > 1) {
                repetitions += count - 1;
            }
        }
        
        return (double) repetitions / sentences.length;
    }
    
    /**
     * 计算句子长度方差
     * AI生成的句子长度通常变化较小
     */
    private double calculateSentenceLengthVariance(String text) {
        String[] sentences = text.split("[。！？.!?]+");
        if (sentences.length < 2) {
            return 0.5;
        }
        
        // 计算每句长度
        int[] lengths = new int[sentences.length];
        int totalLength = 0;
        for (int i = 0; i < sentences.length; i++) {
            lengths[i] = sentences[i].trim().length();
            totalLength += lengths[i];
        }
        
        // 计算平均长度
        double avgLength = (double) totalLength / sentences.length;
        
        // 计算标准差
        double variance = 0;
        for (int length : lengths) {
            variance += Math.pow(length - avgLength, 2);
        }
        variance /= sentences.length;
        double stdDev = Math.sqrt(variance);
        
        // 计算变异系数
        return avgLength > 0 ? stdDev / avgLength : 0;
    }
    
    /**
     * 计算AI生成分数
     */
    private double calculateAiScore(double connectorRatio, double repetitionScore, 
                                    double sentenceLengthVariance, int textLength) {
        double score = 50.0; // 基础分数
        
        // 1. 连接词使用（AI倾向于使用更多连接词）
        if (connectorRatio >= 0.3 && connectorRatio <= 0.6) {
            score += 15.0; // 连接词使用频率适中
        } else if (connectorRatio > 0.6) {
            score += 20.0; // 连接词使用频率高，更可能是AI
        }
        
        // 2. 重复度（过高的重复可能是AI）
        if (repetitionScore >= 0.2) {
            score += 10.0;
        }
        
        // 3. 句子长度变化（AI的句子长度变化较小）
        if (sentenceLengthVariance < 0.3) {
            score += 15.0; // 低变化率
        } else if (sentenceLengthVariance < 0.5) {
            score += 8.0; // 中等变化率
        }
        
        // 4. 文本长度调整
        if (textLength < 100) {
            score -= 10.0; // 太短不易判断
        } else if (textLength > 500) {
            score += 5.0; // 长文本更容易识别
        }
        
        // 确保分数在合理范围内
        return Math.max(0.0, Math.min(95.0, score));
    }
    
    /**
     * 根据分数确定结果
     */
    private String getResult(double score) {
        if (score >= 70) {
            return "AI_GENERATED";
        } else if (score <= 30) {
            return "HUMAN_CREATED";
        } else {
            return "UNCERTAIN";
        }
    }
    
    /**
     * 创建错误结果
     */
    private Map<String, Object> createErrorResult(long responseTime) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", DETECTOR_NAME);
        result.put("score", 50.0);
        result.put("result", "UNCERTAIN");
        result.put("confidence", 0.0);
        result.put("responseTime", responseTime);
        result.put("error", true);
        return result;
    }
    
    @Override
    public String getName() {
        return DETECTOR_NAME;
    }
    
    @Override
    public double getWeight() {
        return WEIGHT;
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 本地检测器始终可用
    }
}
