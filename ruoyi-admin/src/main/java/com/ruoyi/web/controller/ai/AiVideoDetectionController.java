package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiVideoDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI视频检测Controller
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/ai/detection/video")
public class AiVideoDetectionController extends BaseController {
    
    @Autowired
    private IAiVideoDetectionService aiVideoDetectionService;

    /**
     * 上传视频并检测
     */
    @Anonymous
    @Log(title = "AI视频检测", businessType = BusinessType.OTHER)
    @PostMapping("/upload")
    public AjaxResult uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) String userIdStr) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return AjaxResult.error("请选择要上传的视频");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                return AjaxResult.error("只支持视频文件");
            }
            
            // 验证文件大小 (限制100MB)
            if (file.getSize() > 100 * 1024 * 1024) {
                return AjaxResult.error("视频大小不能超过100MB");
            }
            
            // 执行检测
            AiDetectionRecord record = aiVideoDetectionService.detectVideo(file);
            
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
            logger.error("视频检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 通过URL检测视频
     */
    @Anonymous
    @Log(title = "AI视频URL检测", businessType = BusinessType.OTHER)
    @PostMapping("/url")
    public AjaxResult detectVideoByUrl(@RequestParam("url") String videoUrl) {
        try {
            if (videoUrl == null || videoUrl.trim().isEmpty()) {
                return AjaxResult.error("视频URL不能为空");
            }
            
            // 简单的URL格式验证
            if (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://")) {
                return AjaxResult.error("请提供有效的视频URL（需以http://或https://开头）");
            }
            
            AiDetectionRecord record = aiVideoDetectionService.detectVideoByUrl(videoUrl);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("视频URL检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }
}
