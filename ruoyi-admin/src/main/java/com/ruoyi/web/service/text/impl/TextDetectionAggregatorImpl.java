package com.ruoyi.web.service.text.impl;

import com.ruoyi.web.service.text.ITextDetectionAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文本检测结果聚合服务实现
 * 
 * @author ruoyi
 */
@Service
public class TextDetectionAggregatorImpl implements ITextDetectionAggregator {
    
    private static final Logger log = LoggerFactory.getLogger(TextDetectionAggregatorImpl.class);
    
    @Override
    public Map<String, Object> aggregate(List<Map<String, Object>> detectorResults, int textLength) {
        if (detectorResults == null || detectorResults.isEmpty()) {
            return createDefaultResult();
        }
        
        try {
            // 计算加权平均分数
            double weightedScore = calculateWeightedScore(detectorResults);
            
            // 统计投票
            int aiVotes = 0;
            int humanVotes = 0;
            int uncertainVotes = 0;
            
            for (Map<String, Object> result : detectorResults) {
                String detectorResult = (String) result.get("result");
                if ("AI_GENERATED".equals(detectorResult)) {
                    aiVotes++;
                } else if ("HUMAN_CREATED".equals(detectorResult)) {
                    humanVotes++;
                } else {
                    uncertainVotes++;
                }
            }
            
            // 确定最终结果
            String finalResult = determineResult(weightedScore, aiVotes, humanVotes);
            
            // 确定风险等级
            String riskLevel = determineRiskLevel(weightedScore, finalResult);
            
            // 构建聚合结果
            Map<String, Object> aggregated = new HashMap<>();
            aggregated.put("score", Math.round(weightedScore * 10.0) / 10.0);
            aggregated.put("isAI", "AI_GENERATED".equals(finalResult));
            aggregated.put("result", finalResult);
            aggregated.put("riskLevel", riskLevel);
            aggregated.put("confidence", weightedScore / 100.0);
            
            // 投票统计
            Map<String, Integer> votes = new HashMap<>();
            votes.put("aiVotes", aiVotes);
            votes.put("humanVotes", humanVotes);
            votes.put("uncertainVotes", uncertainVotes);
            votes.put("totalVotes", detectorResults.size());
            aggregated.put("votes", votes);
            
            // 检测器结果
            aggregated.put("models", detectorResults);
            
            // 统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("textLength", textLength);
            statistics.put("detectorCount", detectorResults.size());
            statistics.put("avgResponseTime", calculateAvgResponseTime(detectorResults));
            aggregated.put("statistics", statistics);
            
            // 生成建议
            aggregated.put("suggestions", generateSuggestions(finalResult, weightedScore));
            
            log.info("文本检测结果聚合完成 - 综合评分: {}, 判定: {}, 风险等级: {}", 
                weightedScore, finalResult, riskLevel);
            
            return aggregated;
            
        } catch (Exception e) {
            log.error("结果聚合失败", e);
            return createDefaultResult();
        }
    }
    
    @Override
    public double calculateWeightedScore(List<Map<String, Object>> detectorResults) {
        double totalScore = 0.0;
        double totalWeight = 0.0;
        
        for (Map<String, Object> result : detectorResults) {
            if (result.containsKey("error") && (Boolean) result.get("error")) {
                continue; // 跳过错误结果
            }
            
            double score = ((Number) result.get("score")).doubleValue();
            double weight = result.containsKey("weight") ? 
                ((Number) result.get("weight")).doubleValue() : 1.0;
            
            totalScore += score * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalScore / totalWeight : 50.0;
    }
    
    @Override
    public String determineResult(double weightedScore, int aiVotes, int humanVotes) {
        // 综合考虑加权分数和投票结果
        
        // 规则1：分数主导（分数差异明显时）
        if (weightedScore >= 75) {
            return "AI_GENERATED";
        } else if (weightedScore <= 25) {
            return "HUMAN_CREATED";
        }
        
        // 规则2：投票主导（当分数在中间区域时）
        if (weightedScore >= 60 && weightedScore < 75) {
            // 分数偏高，如果AI票数也多，则判定为AI
            if (aiVotes > humanVotes) {
                return "AI_GENERATED";
            }
        } else if (weightedScore > 25 && weightedScore <= 40) {
            // 分数偏低，如果人类票数也多，则判定为人类
            if (humanVotes > aiVotes) {
                return "HUMAN_CREATED";
            }
        }
        
        // 规则3：票数差异大时
        if (aiVotes >= humanVotes + 2) {
            return "AI_GENERATED";
        } else if (humanVotes >= aiVotes + 2) {
            return "HUMAN_CREATED";
        }
        
        // 默认：不确定
        return "UNCERTAIN";
    }
    
    @Override
    public String determineRiskLevel(double score, String result) {
        if ("AI_GENERATED".equals(result)) {
            if (score >= 85) {
                return "HIGH"; // 高风险：很可能是AI生成
            } else if (score >= 70) {
                return "MEDIUM"; // 中风险：可能是AI生成
            } else {
                return "LOW"; // 低风险：不太确定
            }
        } else if ("HUMAN_CREATED".equals(result)) {
            if (score <= 15) {
                return "LOW"; // 低风险：很可能是人类创作
            } else if (score <= 30) {
                return "MEDIUM"; // 中风险：可能是人类创作
            } else {
                return "HIGH"; // 高风险：判断可能不准确
            }
        } else {
            return "MEDIUM"; // 不确定时默认中风险
        }
    }
    
    /**
     * 计算平均响应时间
     */
    private long calculateAvgResponseTime(List<Map<String, Object>> detectorResults) {
        long totalTime = 0;
        int count = 0;
        
        for (Map<String, Object> result : detectorResults) {
            if (result.containsKey("responseTime")) {
                totalTime += ((Number) result.get("responseTime")).longValue();
                count++;
            }
        }
        
        return count > 0 ? totalTime / count : 0;
    }
    
    /**
     * 生成建议
     */
    private List<String> generateSuggestions(String result, double score) {
        List<String> suggestions = new ArrayList<>();
        
        if ("AI_GENERATED".equals(result)) {
            if (score >= 85) {
                suggestions.add("该文本很可能由AI生成，建议人工核实");
                suggestions.add("如用于学术或商业场合，建议注明来源");
                suggestions.add("建议检查是否符合平台使用规范");
            } else {
                suggestions.add("该文本可能包含AI生成内容");
                suggestions.add("建议进行更详细的人工审核");
            }
        } else if ("HUMAN_CREATED".equals(result)) {
            if (score <= 15) {
                suggestions.add("该文本很可能由人类创作");
                suggestions.add("未检测到明显的AI生成特征");
            } else {
                suggestions.add("该文本可能由人类创作");
                suggestions.add("但存在一些AI生成特征，建议谨慎判断");
            }
        } else {
            suggestions.add("无法确定文本来源");
            suggestions.add("建议增加文本长度或提供更多上下文");
            suggestions.add("可尝试使用其他检测工具进行交叉验证");
        }
        
        return suggestions;
    }
    
    /**
     * 创建默认结果
     */
    private Map<String, Object> createDefaultResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("score", 50.0);
        result.put("isAI", false);
        result.put("result", "UNCERTAIN");
        result.put("riskLevel", "MEDIUM");
        result.put("confidence", 0.5);
        result.put("models", new ArrayList<>());
        result.put("error", true);
        result.put("errorMessage", "检测失败或无可用检测器");
        return result;
    }
}
