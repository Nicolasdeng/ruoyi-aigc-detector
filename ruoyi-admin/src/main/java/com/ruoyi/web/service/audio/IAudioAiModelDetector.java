package com.ruoyi.web.service.audio;

import java.util.Map;

/**
 * 音频AI模型检测器接口
 * 用于检测音频是否由特定AI模型生成
 * 
 * @author ruoyi
 */
public interface IAudioAiModelDetector {
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getDetectorName();
    
    /**
     * 获取检测器权重
     * 用于加权计算最终检测分数
     * 
     * @return 权重值（0-1之间）
     */
    double getWeight();
    
    /**
     * 执行AI模型检测
     * 
     * @param audioPath 音频文件路径
     * @param audioFeatures 音频特征数据（由AudioFeatureAnalyzer提供）
     * @return 检测结果
     */
    AudioModelDetectionResult detect(String audioPath, Map<String, Object> audioFeatures);
    
    /**
     * 检测器是否可用
     * 
     * @return true-可用，false-不可用
     */
    default boolean isAvailable() {
        return true;
    }
}
