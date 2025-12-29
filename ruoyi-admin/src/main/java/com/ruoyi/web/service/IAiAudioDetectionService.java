package com.ruoyi.web.service;

import com.ruoyi.web.domain.AiDetectionRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI音频检测Service接口
 * 支持三种检测模式：fast(快速)、standard(标准)、deep(深度)
 * 
 * @author ruoyi
 */
public interface IAiAudioDetectionService {
    
    /**
     * 检测上传的音频（支持检测模式选择）
     * 
     * @param file 音频文件
     * @param mode 检测模式：fast(快速)、standard(标准)、deep(深度)
     * @param userId 用户ID（可选）
     * @return 检测记录
     * @throws Exception 检测异常
     */
    AiDetectionRecord detectAudio(MultipartFile file, String mode, Long userId) throws Exception;
    
    /**
     * 检测上传的音频（使用默认标准模式）
     * 
     * @param file 音频文件
     * @return 检测记录
     * @throws Exception 检测异常
     */
    default AiDetectionRecord detectAudio(MultipartFile file) throws Exception {
        return detectAudio(file, "standard", null);
    }
    
    /**
     * 通过URL检测音频（支持检测模式选择）
     * 
     * @param audioUrl 音频URL
     * @param mode 检测模式：fast(快速)、standard(标准)、deep(深度)
     * @param userId 用户ID（可选）
     * @return 检测记录
     * @throws Exception 检测异常
     */
    AiDetectionRecord detectAudioByUrl(String audioUrl, String mode, Long userId) throws Exception;
    
    /**
     * 通过URL检测音频（使用默认标准模式）
     * 
     * @param audioUrl 音频URL
     * @return 检测记录
     * @throws Exception 检测异常
     */
    default AiDetectionRecord detectAudioByUrl(String audioUrl) throws Exception {
        return detectAudioByUrl(audioUrl, "standard", null);
    }
}
