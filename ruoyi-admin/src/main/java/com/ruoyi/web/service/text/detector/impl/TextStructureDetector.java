package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 文本结构检测器
 * 基于文本的结构特征进行AI检测
 * 
 * @author ruoyi
 */
@Component
public class TextStructureDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(TextStructureDetector.class);
    
    private static final String DETECTOR_NAME = "文本结构分析";
    private static final double WEIGHT = 0.25;
    
    @Override
    public Map<String, Object> detect(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 段落分析
            String[] paragraphs = text.split("\n\n+");
            int paragraphCount = paragraphs.length;
            
            // 句子分析
            String[] sentences = text.split("[。！？.!?]+");
            int sentenceCount = sentences.length;
            
            // 标点符号分析
            long punctuationCount = text.chars().filter(ch -> 
                ch == ',' || ch == '。' || ch == '，' || ch == '！' || 
                ch == '？' || ch == '.' || ch == '!' || ch == '?'
            ).count();
            double punctuationRatio = (double) punctuationCount / text.length();
            
            // 段落长度一致性
            double paragraphConsistency = calculateParagraphConsistency(paragraphs);
            
            // 标题标记检测
            boolean hasHeaders = detectHeaders(text);
            
            // 计算AI概率分数
            double score = calculateAiScore(paragraphCount, paragraphConsistency, 
                punctuationRatio, hasHeaders, text.length());
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", DETECTOR_NAME);
            result.put("score", Math.round(score * 10.0) / 10.0);
            result.put("result", getResult(score));
            result.put("confidence", score / 100.0);
            result.put("responseTime", System.currentTimeMillis() - startTime);
            
            // 详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("paragraphCount", paragraphCount);
            details.put("sentenceCount", sentenceCount);
            details.put("punctuationRatio", Math.round(punctuationRatio * 1000.0) / 1000.0);
            details.put("paragraphConsistency", Math.round(paragraphConsistency * 100.0) / 100.0);
            details.put("hasHeaders", hasHeaders);
            result.put("details", details);
            
            log.debug("文本结构检测完成 - 分数: {}, 段落数: {}, 一致性: {}", 
                score, paragraphCount, paragraphConsistency);
            
            return result;
            
        } catch (Exception e) {
            log.error("文本结构检测失败", e);
            return createErrorResult(System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 计算段落长度一致性
     * 返回值越接近1，说明段落长度越一致
     */
    private double calculateParagraphConsistency(String[] paragraphs) {
        if (paragraphs.length <= 1) {
            return 0.5;
        }
        
        // 计算段落长度
        int[] lengths = new int[paragraphs.length];
        int totalLength = 0;
        for (int i = 0; i < paragraphs.length; i++) {
            lengths[i] = paragraphs[i].length();
            totalLength += lengths[i];
        }
        
        // 计算平均长度
        double avgLength = (double) totalLength / paragraphs.length;
        
        // 计算标准差
        double variance = 0;
        for (int length : lengths) {
            variance += Math.pow(length - avgLength, 2);
        }
        variance /= paragraphs.length;
        double stdDev = Math.sqrt(variance);
        
        // 计算变异系数（标准差/平均值）
        double cv = avgLength > 0 ? stdDev / avgLength : 0;
        
        // 变异系数越小，一致性越高
        // 将变异系数转换为一致性分数（0-1）
        return Math.max(0, 1 - cv);
    }
    
    /**
     * 检测是否有标题标记
     */
    private boolean detectHeaders(String text) {
        // Markdown标题
        if (text.contains("# ") || text.contains("## ") || text.contains("### ")) {
            return true;
        }
        
        // 数字标题
        if (text.matches(".*[一二三四五六七八九十][、.].*") || 
            text.matches(".*\\d+[、.].*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 计算AI生成分数
     */
    private double calculateAiScore(int paragraphCount, double paragraphConsistency, 
                                    double punctuationRatio, boolean hasHeaders, 
                                    int textLength) {
        double score = 50.0; // 基础分数
        
        // 1. 段落结构（AI生成的文本通常有清晰的段落结构）
        if (paragraphCount >= 3 && paragraphCount <= 10) {
            score += 15.0; // 段落数量适中
        } else if (paragraphCount > 10) {
            score += 10.0; // 段落较多，也可能是AI
        }
        
        // 2. 段落一致性（AI生成的段落长度通常较为一致）
        if (paragraphConsistency >= 0.7) {
            score += 15.0; // 高一致性
        } else if (paragraphConsistency >= 0.5) {
            score += 8.0; // 中等一致性
        }
        
        // 3. 标点符号使用（AI倾向于标准使用标点）
        if (punctuationRatio >= 0.05 && punctuationRatio <= 0.15) {
            score += 10.0; // 标点使用规范
        }
        
        // 4. 标题结构（AI喜欢使用标题结构）
        if (hasHeaders && textLength > 200) {
            score += 10.0;
        }
        
        // 5. 文本长度调整
        if (textLength < 100) {
            score -= 10.0; // 太短不易判断
        } else if (textLength > 1000) {
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
