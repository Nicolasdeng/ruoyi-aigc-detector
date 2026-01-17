package com.ruoyi.web.service.audio.detector;

import java.util.Map;

/**
 * 音频检测器接口
 * 所有音频检测器都需要实现此接口
 * 
 * @author ruoyi
 */
public interface IAudioDetector {
    
    /**
     * 检测音频文件
     * 
     * @param audioFilePath 音频文件路径
     * @return 检测结果，包含：
     *         - score: AI分数 (0-1之间，越高越可能是AI生成)
     *         - isAI: 是否判定为AI生成
     *         - confidence: 置信度
     *         - features: 特征详情
     *         - detectorName: 检测器名称
     */
    Map<String, Object> detect(String audioFilePath);
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getDetectorName();
    
    /**
     * 获取检测器权重（用于结果聚合）
     * 
     * @param mode 检测模式：fast/standard/deep
     * @return 权重值 (0-1之间)
     */
    double getWeight(String mode);
    
    /**
     * 检测器是否在指定模式下启用
     * 
     * @param mode 检测模式：fast/standard/deep
     * @return 是否启用
     */
    boolean isEnabledForMode(String mode);
}
