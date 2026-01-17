package com.ruoyi.web.service.video;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * 视频AI模型检测器统一接口
 * 
 * @author ruoyi
 */
public interface IVideoAiModelDetector {
    
    /**
     * 获取模型名称
     * 
     * @return 模型名称(如: "Runway Gen-2", "Pika Labs"等)
     */
    String getModelName();
    
    /**
     * 检测视频是否由该AI模型生成
     * 
     * @param videoFrames 视频关键帧列表
     * @param videoMetadata 视频元数据(如:分辨率、帧率、时长等)
     * @return 检测结果
     */
    VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> videoMetadata);

    VideoModelDetectionResult detectModel(List<BufferedImage> frames);

    /**
     * 获取特征详情
     * 
     * @param videoFrames 视频关键帧列表
     * @return 特征详情Map,包含各项特征的具体分析
     */
    Map<String, Object> getFeatureDetails(List<BufferedImage> videoFrames);
    
    /**
     * 生成优化建议
     * 
     * @param result 检测结果
     * @return 优化建议列表
     */
    List<String> generateSuggestions(VideoModelDetectionResult result);

    Map<String, Object> getFeatureDetails(Map<String, Double> scores);

    List<String> generateSuggestions(double confidence, Map<String, Double> scores);
}
