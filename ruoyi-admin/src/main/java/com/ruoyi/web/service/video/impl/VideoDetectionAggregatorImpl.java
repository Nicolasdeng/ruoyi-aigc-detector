package com.ruoyi.web.service.video.impl;

import com.ruoyi.web.domain.VideoDetectionDetail;
import com.ruoyi.web.domain.VideoDetectionDetail.ApiResult;
import com.ruoyi.web.domain.VideoDetectionDetail.AnalysisReport;
import com.ruoyi.web.service.video.IVideoDetectionAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 视频检测结果聚合器实现
 * 
 * @author ruoyi
 */
@Service
public class VideoDetectionAggregatorImpl implements IVideoDetectionAggregator {
    
    private static final Logger log = LoggerFactory.getLogger(VideoDetectionAggregatorImpl.class);
    
    @Override
    public VideoDetectionDetail aggregate(List<ApiResult> apiResults) {
        VideoDetectionDetail detail = new VideoDetectionDetail();
        
        // 设置API结果列表
        detail.setApiResults(apiResults);
        
        // 计算加权平均得分
        double weightedScore = calculateWeightedScore(apiResults);
        
        // 转换为百分比
        BigDecimal aiProbability = BigDecimal.valueOf(weightedScore * 100)
            .setScale(2, RoundingMode.HALF_UP);
        detail.setAiProbability(aiProbability);
        
        // 判定最终结果
        String result = determineResult(weightedScore);
        detail.setDetectionResult(result);
        
        // 评估置信度级别
        String confidenceLevel = assessConfidenceLevel(weightedScore, apiResults);
        detail.setConfidenceLevel(confidenceLevel);
        
        // 生成分析报告
        AnalysisReport report = new AnalysisReport();
        detail.setAnalysisReport(report);
        
        log.info("视频检测结果聚合完成: result={}, probability={}%, confidence={}", 
            result, aiProbability, confidenceLevel);
        
        return detail;
    }
    
    @Override
    public double calculateWeightedScore(List<ApiResult> apiResults) {
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        
        for (ApiResult result : apiResults) {
            double weight = result.getWeight().doubleValue();
            double score = result.getScore().doubleValue();
            
            totalWeightedScore += score * weight;
            totalWeight += weight;
        }
        
        return totalWeight > 0 ? totalWeightedScore / totalWeight : 0.5;
    }
    
    @Override
    public String determineResult(double weightedScore) {
        if (weightedScore >= 0.70) {
            return "AI生成";
        } else if (weightedScore >= 0.45) {
            return "可能AI生成";
        } else {
            return "真实内容";
        }
    }
    
    @Override
    public String assessConfidenceLevel(double weightedScore, List<ApiResult> apiResults) {
        // 统计判定为AI的检测器数量
        long aiCount = apiResults.stream()
            .filter(ApiResult::getIsAI)
            .count();
        double aiRatio = (double) aiCount / apiResults.size();
        
        // 综合评估置信度
        if (weightedScore >= 0.80 && aiRatio >= 0.8) {
            return "高";
        } else if (weightedScore >= 0.60 && aiRatio >= 0.6) {
            return "中";
        } else if (weightedScore <= 0.30 && aiRatio <= 0.2) {
            return "高";  // 真实内容的高置信度
        } else if (weightedScore <= 0.40) {
            return "中";  // 真实内容的中置信度
        } else {
            return "低";
        }
    }
}
