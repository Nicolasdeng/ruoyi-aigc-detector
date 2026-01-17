package com.ruoyi.web.service;

import com.ruoyi.web.domain.AiDetectionRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI视频检测Service接口
 * 
 * @author ruoyi
 */
public interface IAiVideoDetectionService {
    
    /**
     * 检测上传的视频
     * 
     * @param file 视频文件
     * @param userId 用户ID（可选，用于记录检测历史）
     * @return 检测记录
     * @throws Exception 检测异常
     */
    AiDetectionRecord detectVideo(MultipartFile file, Long userId) throws Exception;
    
    /**
     * 通过URL检测视频
     * 
     * @param videoUrl 视频URL
     * @param userId 用户ID（可选，用于记录检测历史）
     * @return 检测记录
     * @throws Exception 检测异常
     */
    AiDetectionRecord detectVideoByUrl(String videoUrl, Long userId) throws Exception;
}
