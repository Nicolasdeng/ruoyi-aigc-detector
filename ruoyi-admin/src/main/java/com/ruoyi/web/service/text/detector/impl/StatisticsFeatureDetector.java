package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 统计特征检测器
 * 基于文本的统计特征进行AI检测
 * 
 * @author ruoyi
 */
@Component
public class StatisticsFeatureDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsFeatureDetector.class);
    
    private static final String DETECTOR_NAME = "统计特征分析";
    private static final double WEIGHT = 0.30;
    
    @Override
    public Map<String, Object> detect(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 文本基础统计
            int charCount = text.length();
            String[] sentences = text.split("[。！？.!?]+");
            int sentenceCount = Math.max(sentences.length, 1);
            double avgSentenceLength = (double) charCount / sentenceCount;
            
            // 词汇统计
            String[] words = text.split("[\\s\\p{P}]+");
            Set<String> uniqueWords = new HashSet<>(Arrays.asList(words));
            double uniqueRatio = (double) uniqueWords.size() / Math.max(words.length, 1);
            
            // 计算AI概率分数
            double score = calculateAiScore(avgSentenceLength, uniqueRatio, charCount);
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", DETECTOR_NAME);
            result.put("score", Math.round(score * 10.0) / 10.0);
            result.put("result", getResult(score));
            result.put("confidence", score / 100.0);
            result.put("responseTime", System.currentTimeMillis() - startTime);
            
            // 详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("charCount", charCount);
            details.put("sentenceCount", sentenceCount);
            details.put("avgSentenceLength", Math.round(avgSentenceLength * 10.0) / 10.0);
            details.put("uniqueWordRatio", Math.round(uniqueRatio * 100.0) / 100.0);
            result.put("details", details);
            
            log.debug("统计特征检测完成 - 分数: {}, 平均句长: {}, 词汇唯一度: {}", 
                score, avgSentenceLength, uniqueRatio);
            
            return result;
            
        } catch (Exception e) {
            log.error("统计特征检测失败", e);
            return createErrorResult(System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 计算AI生成分数
     */
    private double calculateAiScore(double avgSentenceLength, double uniqueRatio, int charCount) {
        double score = 50.0; // 基础分数
        
        // 1. 句子长度分析（AI生成的文本句长通常较为均匀和适中）
        if (avgSentenceLength >= 15 && avgSentenceLength <= 35) {
            score += 15.0; // 句长适中，符合AI特征
        } else if (avgSentenceLength < 10 || avgSentenceLength > 50) {
            score -= 10.0; // 句长过短或过长，更像人类写作
        }
        
        // 2. 词汇多样性分析（AI倾向于使用更多样的词汇）
        if (uniqueRatio >= 0.6 && uniqueRatio <= 0.85) {
            score += 15.0; // 词汇多样性适中
        } else if (uniqueRatio < 0.4) {
            score += 10.0; // 重复度高，可能是AI
        } else if (uniqueRatio > 0.9) {
            score -= 10.0; // 词汇过于多样，更像人类
        }
        
        // 3. 文本长度影响（较长的文本更容易被识别）
        if (charCount > 500) {
            score += 5.0;
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
