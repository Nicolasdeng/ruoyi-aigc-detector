package com.ruoyi.web.service.image;

import java.util.List;
import java.util.Map;

/**
 * 检测结果聚合服务接口
 * 负责汇总多个检测器的结果并生成最终判定
 * 
 * @author ruoyi
 */
public interface IDetectionAggregator {
    
    /**
     * 聚合多个检测器的结果
     * 
     * @param detectionResults 各检测器的结果列表
     * @return 聚合后的最终结果，包含result、confidence、details等
     */
    Map<String, Object> aggregateResults(List<Map<String, Object>> detectionResults);
    
    /**
     * 计算三级风险评估
     * 
     * @param finalScore 最终得分（0-1）
     * @param apiResults 各API结果
     * @return 风险评估信息（HIGH/MEDIUM/LOW）
     */
    Map<String, Object> calculateRiskAssessment(double finalScore, List<Map<String, Object>> apiResults);
    
    /**
     * 生成使用建议
     * 
     * @param riskLevel 风险等级
     * @param finalScore 最终得分
     * @return 建议列表
     */
    List<String> generateSuggestions(String riskLevel, double finalScore);
}
