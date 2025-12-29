package com.ruoyi.web.service.image.detector;

import java.util.Map;

/**
 * 图片检测器接口
 * 各种AI检测引擎的统一接口
 * 
 * @author ruoyi
 */
public interface IImageDetector {
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getName();
    
    /**
     * 获取检测器权重
     * 
     * @return 权重值（0-1之间）
     */
    double getWeight();
    
    /**
     * 检测本地图片文件
     * 
     * @param filePath 本地文件路径
     * @return 检测结果，包含score、isAI、details等信息
     */
    Map<String, Object> detect(String filePath);
    
    /**
     * 检测URL图片
     * 
     * @param imageUrl 图片URL
     * @return 检测结果
     */
    Map<String, Object> detectByUrl(String imageUrl);
    
    /**
     * 检测器是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
    
    /**
     * 获取超时时间（秒）
     * 
     * @return 超时时间
     */
    default int getTimeout() {
        return 10;
    }
}
