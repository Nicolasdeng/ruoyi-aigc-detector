package com.ruoyi.web.service.video;

import com.ruoyi.web.domain.VideoDetectionDetail;
import com.ruoyi.web.domain.VideoDetectionDetail.ApiResult;

import java.util.List;

/**
 * 视频检测结果聚合器接口
 * 
 * @author ruoyi
 */
public interface IVideoDetectionAggregator {
    
    /**
     * 聚合多个检测器的结果
     * 
     * @param apiResults 各检测器的结果列表
     * @return 聚合后的检测详情
     */
    VideoDetectionDetail aggregate(List<ApiResult> apiResults);
    
    /**
     * 计算加权平均得分
     * 
     * @param apiResults 各检测器的结果列表
     * @return 加权平均得分（0.0-1.0）
     */
    double calculateWeightedScore(List<ApiResult> apiResults);
    
    /**
     * 判定最终结果
     * 
     * @param weightedScore 加权平均得分
     * @return 检测结果文本
     */
    String determineResult(double weightedScore);
    
    /**
     * 评估置信度级别
     * 
     * @param weightedScore 加权平均得分
     * @param apiResults 各检测器的结果列表
     * @return 置信度级别（高/中/低）
     */
    String assessConfidenceLevel(double weightedScore, List<ApiResult> apiResults);
}
