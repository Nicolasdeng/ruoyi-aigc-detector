package com.ruoyi.web.service.text;

import java.util.List;
import java.util.Map;

/**
 * 文本检测结果聚合服务接口
 * 负责汇总多个检测器的结果
 * 
 * @author ruoyi
 */
public interface ITextDetectionAggregator {
    
    /**
     * 聚合多个检测器的结果
     * 
     * @param detectorResults 各检测器的检测结果列表
     * @param textLength 文本长度
     * @return 聚合后的最终结果
     */
    Map<String, Object> aggregate(List<Map<String, Object>> detectorResults, int textLength);
    
    /**
     * 计算加权平均分数
     * 
     * @param detectorResults 各检测器的检测结果列表
     * @return 加权平均分数
     */
    double calculateWeightedScore(List<Map<String, Object>> detectorResults);
    
    /**
     * 根据分数和投票结果确定最终判定
     * 
     * @param weightedScore 加权平均分数
     * @param aiVotes AI判定票数
     * @param humanVotes 人类判定票数
     * @return 最终判定结果
     */
    String determineResult(double weightedScore, int aiVotes, int humanVotes);
    
    /**
     * 确定风险等级
     * 
     * @param score 综合分数
     * @param result 检测结果
     * @return 风险等级
     */
    String determineRiskLevel(double score, String result);
}
