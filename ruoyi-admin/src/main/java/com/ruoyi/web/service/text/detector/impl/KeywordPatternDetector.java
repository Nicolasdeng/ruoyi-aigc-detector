package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 关键词模式检测器
 * 基于AI生成文本常见的关键词和表达模式进行检测
 * 
 * @author ruoyi
 */
@Component
public class KeywordPatternDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(KeywordPatternDetector.class);
    
    private static final String DETECTOR_NAME = "关键词模式检测";
    private static final double WEIGHT = 0.25;
    
    // AI生成文本常见模式（中文）
    private static final String[] AI_PATTERNS_CN = {
        "作为一个", "作为一名", "根据我的理解", "基于以上", "综上所述",
        "需要注意的是", "值得一提的是", "总的来说", "换句话说",
        "首先", "其次", "再次", "最后", "总之", "因此",
        "这意味着", "这表明", "这说明", "由此可见"
    };
    
    // AI生成文本常见模式（英文）
    private static final String[] AI_PATTERNS_EN = {
        "as an ai", "i'm an ai", "i am an ai", "as a language model",
        "i don't have", "i cannot", "i can't", "however", "furthermore",
        "additionally", "moreover", "in conclusion", "to summarize",
        "it's worth noting", "it should be noted", "on the other hand"
    };
    
    // 过于正式的表达
    private static final String[] FORMAL_PATTERNS = {
        "您好", "尊敬的", "亲爱的", "感谢您", "衷心感谢",
        "谨此", "特此", "兹有", "hereby", "therefore"
    };
    
    @Override
    public Map<String, Object> detect(String text) {
        long startTime = System.currentTimeMillis();
        
        try {
            String lowerText = text.toLowerCase();
            
            // 检测AI常见模式
            int cnPatternCount = countPatterns(lowerText, AI_PATTERNS_CN);
            int enPatternCount = countPatterns(lowerText, AI_PATTERNS_EN);
            int formalPatternCount = countPatterns(lowerText, FORMAL_PATTERNS);
            
            // 检测列表结构
            int listMarkerCount = countListMarkers(text);
            
            // 计算AI概率分数
            double score = calculateAiScore(cnPatternCount, enPatternCount, 
                formalPatternCount, listMarkerCount, text.length());
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("name", DETECTOR_NAME);
            result.put("score", Math.round(score * 10.0) / 10.0);
            result.put("result", getResult(score));
            result.put("confidence", score / 100.0);
            result.put("responseTime", System.currentTimeMillis() - startTime);
            
            // 详细信息
            Map<String, Object> details = new HashMap<>();
            details.put("cnPatternCount", cnPatternCount);
            details.put("enPatternCount", enPatternCount);
            details.put("formalPatternCount", formalPatternCount);
            details.put("listMarkerCount", listMarkerCount);
            result.put("details", details);
            
            log.debug("关键词模式检测完成 - 分数: {}, 中文模式: {}, 英文模式: {}", 
                score, cnPatternCount, enPatternCount);
            
            return result;
            
        } catch (Exception e) {
            log.error("关键词模式检测失败", e);
            return createErrorResult(System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 统计模式出现次数
     */
    private int countPatterns(String text, String[] patterns) {
        int count = 0;
        for (String pattern : patterns) {
            if (text.contains(pattern.toLowerCase())) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 统计列表标记
     */
    private int countListMarkers(String text) {
        int count = 0;
        
        // 数字列表：1. 2. 3.
        count += countOccurrences(text, "\\d+\\.");
        
        // 项目符号：- * •
        count += countOccurrences(text, "^[\\-\\*•]\\s", true);
        
        return count;
    }
    
    /**
     * 统计正则表达式匹配次数
     */
    private int countOccurrences(String text, String regex) {
        return countOccurrences(text, regex, false);
    }
    
    /**
     * 统计正则表达式匹配次数
     */
    private int countOccurrences(String text, String regex, boolean multiline) {
        try {
            java.util.regex.Pattern pattern;
            if (multiline) {
                pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.MULTILINE);
            } else {
                pattern = java.util.regex.Pattern.compile(regex);
            }
            java.util.regex.Matcher matcher = pattern.matcher(text);
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 计算AI生成分数
     */
    private double calculateAiScore(int cnPatternCount, int enPatternCount, 
                                    int formalPatternCount, int listMarkerCount, 
                                    int textLength) {
        double score = 50.0; // 基础分数
        
        // 1. AI常见模式匹配
        score += Math.min(cnPatternCount * 8.0, 30.0);
        score += Math.min(enPatternCount * 8.0, 30.0);
        
        // 2. 正式表达（AI倾向于使用正式表达）
        if (textLength > 100) {
            score += Math.min(formalPatternCount * 3.0, 15.0);
        }
        
        // 3. 列表结构（AI喜欢使用结构化列表）
        if (listMarkerCount >= 3) {
            score += 10.0;
        } else if (listMarkerCount >= 1) {
            score += 5.0;
        }
        
        // 4. 如果没有检测到任何AI特征，降低分数
        if (cnPatternCount == 0 && enPatternCount == 0 && listMarkerCount == 0) {
            score -= 15.0;
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
