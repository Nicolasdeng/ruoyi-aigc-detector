package com.ruoyi.web.service.image.impl;

import com.ruoyi.web.service.image.IDetectionAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 检测结果聚合服务实现
 * 
 * @author ruoyi
 */
@Service
public class DetectionAggregatorImpl implements IDetectionAggregator {
    
    private static final Logger log = LoggerFactory.getLogger(DetectionAggregatorImpl.class);
    
    @Override
    public Map<String, Object> aggregateResults(List<Map<String, Object>> detectionResults) {
        if (detectionResults == null || detectionResults.isEmpty()) {
            throw new RuntimeException("没有检测结果可供聚合");
        }
        
        // 过滤出可用的API结果
        List<Map<String, Object>> availableResults = detectionResults.stream()
                .filter(r -> (Boolean) r.getOrDefault("available", true))
                .collect(Collectors.toList());
        
        if (availableResults.isEmpty()) {
            throw new RuntimeException("没有可用的检测引擎返回结果");
        }
        
        // 加权平均计算
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        int aiCount = 0;
        int humanCount = 0;
        int uncertainCount = 0;
        double maxScore = 0.0;
        double minScore = 1.0;
        List<String> aiFeatures = new ArrayList<>();
        
        for (Map<String, Object> apiResult : availableResults) {
            double weight = ((Number) apiResult.getOrDefault("weight", 1.0)).doubleValue();
            double score = ((Number) apiResult.getOrDefault("score", 0.5)).doubleValue();
            boolean isAI = (Boolean) apiResult.getOrDefault("isAI", false);
            String apiName = (String) apiResult.get("apiName");
            
            totalWeightedScore += score * weight;
            totalWeight += weight;
            maxScore = Math.max(maxScore, score);
            minScore = Math.min(minScore, score);
            
            if (isAI) {
                aiCount++;
                aiFeatures.add(apiName + " 判定为AI生成");
            } else if (score > 0.4 && score < 0.6) {
                uncertainCount++;
            } else {
                humanCount++;
            }
        }
        
        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.5;
        
        // 计算风险评估
        Map<String, Object> riskAssessment = calculateRiskAssessment(finalScore, availableResults);
        String result = (String) riskAssessment.get("result");
        String riskLevel = (String) riskAssessment.get("riskLevel");
        BigDecimal confidence = (BigDecimal) riskAssessment.get("confidence");
        String explanation = (String) riskAssessment.get("explanation");
        
        // 构建详细信息
        Map<String, Object> details = new HashMap<>();
        details.put("finalScore", Math.round(finalScore * 10000) / 100.0); // 百分比
        details.put("riskLevel", riskLevel);
        details.put("explanation", explanation);
        details.put("apiCount", availableResults.size());
        details.put("totalApiCalled", detectionResults.size());
        details.put("aiVotes", aiCount);
        details.put("humanVotes", humanCount);
        details.put("uncertainVotes", uncertainCount);
        details.put("maxScore", Math.round(maxScore * 10000) / 100.0);
        details.put("minScore", Math.round(minScore * 10000) / 100.0);
        details.put("scoreRange", Math.round((maxScore - minScore) * 10000) / 100.0);
        details.put("aiFeatures", aiFeatures);
        
        // 生成使用建议
        List<String> suggestions = generateSuggestions(riskLevel, finalScore);
        details.put("suggestions", suggestions);
        
        // 构建返回结果
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("result", result);
        aggregated.put("confidence", confidence);
        aggregated.put("details", details);
        aggregated.put("apiResults", availableResults);
        
        log.info("检测结果聚合完成: {} - 置信度: {} - 风险等级: {}", result, confidence, riskLevel);
        
        return aggregated;
    }
    
    @Override
    public Map<String, Object> calculateRiskAssessment(double finalScore, List<Map<String, Object>> apiResults) {
        Map<String, Object> assessment = new HashMap<>();
        
        String result;
        String riskLevel;
        BigDecimal confidence;
        String explanation;
        
        int aiCount = (int) apiResults.stream()
                .filter(r -> (Boolean) r.getOrDefault("isAI", false))
                .count();
        int totalCount = apiResults.size();
        
        // 高风险：明显AI生成（红色）
        if (finalScore > 0.70) {
            result = "AI_GENERATED";
            riskLevel = "HIGH";
            confidence = BigDecimal.valueOf(Math.min(finalScore * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
            explanation = "图片具有明显的AI生成特征";
            if (aiCount >= totalCount * 0.8) {
                explanation += "，多数检测引擎一致判定为AI生成";
            }
        }
        // 中风险：存在AI特征（黄色）
        else if (finalScore >= 0.30 && finalScore <= 0.70) {
            result = "UNCERTAIN";
            riskLevel = "MEDIUM";
            if (finalScore > 0.5) {
                confidence = BigDecimal.valueOf((finalScore - 0.5) * 200)
                        .setScale(2, RoundingMode.HALF_UP);
                explanation = "图片存在一定AI生成特征，但无法完全确定";
            } else {
                confidence = BigDecimal.valueOf((0.5 - finalScore) * 200)
                        .setScale(2, RoundingMode.HALF_UP);
                explanation = "图片特征不明显，建议结合使用场景判断";
            }
        }
        // 低风险：更像真实图片（绿色）
        else {
            result = "HUMAN_CREATED";
            riskLevel = "LOW";
            confidence = BigDecimal.valueOf(Math.min((1 - finalScore) * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
            explanation = "图片更符合真实拍摄特征";
            int humanCount = (int) apiResults.stream()
                    .filter(r -> !(Boolean) r.getOrDefault("isAI", false))
                    .count();
            if (humanCount >= totalCount * 0.8) {
                explanation += "，多数检测引擎判定为人类创作";
            }
        }
        
        assessment.put("result", result);
        assessment.put("riskLevel", riskLevel);
        assessment.put("confidence", confidence);
        assessment.put("explanation", explanation);
        
        return assessment;
    }
    
    @Override
    public List<String> generateSuggestions(String riskLevel, double finalScore) {
        List<String> suggestions = new ArrayList<>();
        
        switch (riskLevel) {
            case "HIGH":
                suggestions.add("此图片具有明显AI生成特征，不建议用于需要真实性的场合");
                suggestions.add("如需用作课程作业或正式材料，请谨慎使用");
                if (finalScore > 0.85) {
                    suggestions.add("检测置信度极高，强烈建议核实图片来源");
                }
                break;
                
            case "MEDIUM":
                suggestions.add("检测结果存在不确定性，建议人工进一步核实");
                suggestions.add("可结合图片来源、使用场景等信息综合判断");
                suggestions.add("建议咨询相关领域专家进行二次确认");
                break;
                
            case "LOW":
                suggestions.add("图片特征符合真实拍摄，但仍建议核实来源");
                suggestions.add("AI技术在不断进化，建议保持警惕");
                if (finalScore < 0.15) {
                    suggestions.add("检测置信度较高，图片真实性较有保障");
                }
                break;
                
            default:
                suggestions.add("请参考详细检测报告进行综合判断");
                break;
        }
        
        return suggestions;
    }
}
