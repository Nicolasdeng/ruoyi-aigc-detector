package com.ruoyi.web.service.image;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 图片上传服务接口
 * 
 * @author ruoyi
 */
public interface IImageUploadService {
    
    /**
     * 上传图片文件
     * 
     * @param file 上传的文件
     * @return 上传结果，包含文件路径、文件大小等信息
     * @throws Exception 上传失败时抛出异常
     */
    Map<String, Object> uploadImage(MultipartFile file) throws Exception;
    
    /**
     * 验证图片文件
     * 
     * @param file 待验证的文件
     * @return 验证是否通过
     */
    boolean validateImage(MultipartFile file);
    
    /**
     * 获取图片基本信息
     * 
     * @param filePath 文件路径
     * @return 图片信息（宽度、高度、格式等）
     */
    Map<String, Object> getImageInfo(String filePath);
    
    /**
     * 下载URL图片到本地
     * 
     * @param imageUrl 图片URL
     * @return 本地文件路径
     * @throws Exception 下载失败时抛出异常
     */
    String downloadImageFromUrl(String imageUrl) throws Exception;
}
