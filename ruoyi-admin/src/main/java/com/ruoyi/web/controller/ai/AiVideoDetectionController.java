package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiVideoDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * AI视频检测Controller
 * 
 * @author ruoyi
 */
@RequiresAuth
@RestController
@RequestMapping("/ai/detection/video")
public class AiVideoDetectionController extends BaseController {
    
    @Autowired
    private IAiVideoDetectionService aiVideoDetectionService;

    /**
     * 上传视频并检测
     */
    @RequiresAuth
    @Log(title = "AI视频检测", businessType = BusinessType.OTHER)
    @PostMapping("/upload")
    public AjaxResult uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
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
            AiDetectionRecord record = aiVideoDetectionService.detectVideo(file, userId);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("视频检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 通过URL检测视频
     */
    @RequiresAuth
    @Log(title = "AI视频URL检测", businessType = BusinessType.OTHER)
    @PostMapping("/url")
    public AjaxResult detectVideoByUrl(@RequestParam("url") String videoUrl,
                                       HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            if (videoUrl == null || videoUrl.trim().isEmpty()) {
                return AjaxResult.error("视频URL不能为空");
            }
            
            // 简单的URL格式验证
            if (!videoUrl.startsWith("http://") && !videoUrl.startsWith("https://")) {
                return AjaxResult.error("请提供有效的视频URL（需以http://或https://开头）");
            }
            
            AiDetectionRecord record = aiVideoDetectionService.detectVideoByUrl(videoUrl, userId);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("视频URL检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }
}
