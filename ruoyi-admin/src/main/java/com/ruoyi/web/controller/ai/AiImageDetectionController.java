package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiImageDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI图片检测Controller
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/ai/detection/image")
public class AiImageDetectionController extends BaseController {
    
    @Autowired
    private IAiImageDetectionService aiImageDetectionService;

    /**
     * 上传图片并检测
     */
    @Anonymous
    @Log(title = "AI图片检测", businessType = BusinessType.OTHER)
    @PostMapping("/upload")
    public AjaxResult uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userIdStr) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return AjaxResult.error("请选择要上传的图片");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return AjaxResult.error("只支持图片文件");
            }
            
            // 验证文件大小 (限制10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return AjaxResult.error("图片大小不能超过10MB");
            }
            
            // 执行检测
            AiDetectionRecord record = aiImageDetectionService.detectImage(file);
            
            // 设置用户ID - 安全转换
            if (userIdStr != null && !userIdStr.trim().isEmpty() 
                && !"null".equalsIgnoreCase(userIdStr) 
                && !"[object Null]".equalsIgnoreCase(userIdStr)
                && !"undefined".equalsIgnoreCase(userIdStr)) {
                try {
                    Long userId = Long.parseLong(userIdStr.trim());
                    record.setUserId(userId);
                } catch (NumberFormatException e) {
                    logger.warn("无效的userId格式: {}", userIdStr);
                }
            }
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("图片检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 通过URL检测图片
     */
    @Anonymous
    @Log(title = "AI图片URL检测", businessType = BusinessType.OTHER)
    @PostMapping("/url")
    public AjaxResult detectImageByUrl(@RequestParam("url") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return AjaxResult.error("图片URL不能为空");
            }
            
            // 简单的URL格式验证
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return AjaxResult.error("请提供有效的图片URL（需以http://或https://开头）");
            }
            
            AiDetectionRecord record = aiImageDetectionService.detectImageByUrl(imageUrl);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("图片URL检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }
}
