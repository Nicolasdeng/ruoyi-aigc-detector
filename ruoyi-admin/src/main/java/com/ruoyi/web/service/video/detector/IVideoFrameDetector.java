package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.domain.VideoDetectionDetail.ApiResult;

/**
 * 视频帧检测器接口
 * 
 * @author ruoyi
 */
public interface IVideoFrameDetector {
    
    /**
     * 检测视频帧
     * 
     * @param filePath 视频文件路径
     * @return 检测结果
     */
    ApiResult detect(String filePath);
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getDetectorName();
    
    /**
     * 获取检测器权重
     * 
     * @return 权重值（0.0-1.0）
     */
    double getWeight();
}
