package com.ruoyi.web.service;

import com.ruoyi.web.domain.AiDetectionRecord;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI图片检测Service接口
 * 
 * @author ruoyi
 */
public interface IAiImageDetectionService {
    
    /**
     * 检测上传的图片
     * 
     * @param file 上传的图片文件
     * @return 检测记录
     */
    AiDetectionRecord detectImage(MultipartFile file) throws Exception;

    /**
     * 通过URL检测图片
     * 
     * @param imageUrl 图片URL
     * @return 检测记录
     */
    AiDetectionRecord detectImageByUrl(String imageUrl) throws Exception;
}
