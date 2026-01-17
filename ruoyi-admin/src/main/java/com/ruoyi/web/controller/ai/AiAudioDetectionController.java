package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.annotation.RequiresAuth;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.service.IAiAudioDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * AI音频检测Controller
 * 支持三种检测模式：fast(快速)、standard(标准)、deep(深度)
 * 
 * @author ruoyi
 */
@RequiresAuth
@RestController
@RequestMapping("/ai/detection/audio")
public class AiAudioDetectionController extends BaseController {
    
    @Autowired
    private IAiAudioDetectionService aiAudioDetectionService;

    // 支持的音频格式
    private static final Set<String> SUPPORTED_AUDIO_TYPES = new HashSet<>(Arrays.asList(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/wave", "audio/x-wav",
        "audio/mp4", "audio/m4a", "audio/x-m4a", "audio/ogg", "audio/flac",
        "audio/aac", "audio/x-aac", "audio/wma", "audio/x-ms-wma"
    ));

    /**
     * 上传音频并检测（支持检测模式选择）
     */
    @RequiresAuth
    @Log(title = "AI音频检测", businessType = BusinessType.OTHER)
    @PostMapping("/upload")
    public AjaxResult uploadAndDetect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mode", defaultValue = "standard") String mode,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            // 验证文件
            if (file == null || file.isEmpty()) {
                return AjaxResult.error("请选择要上传的音频文件");
            }
            
            // 验证文件类型
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
                // 如果Content-Type不准确，尝试通过文件扩展名判断
                if (fileName != null && !isSupportedAudioFile(fileName)) {
                    return AjaxResult.error("不支持的音频格式，仅支持：MP3、WAV、M4A、OGG、FLAC、AAC、WMA");
                }
            }
            
            // 验证文件大小 (限制50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return AjaxResult.error("音频文件大小不能超过50MB");
            }
            
            // 验证检测模式
            if (!isValidMode(mode)) {
                return AjaxResult.error("不支持的检测模式，请选择：fast(快速)、standard(标准)、deep(深度)");
            }
            
            // 执行检测
            AiDetectionRecord record = aiAudioDetectionService.detectAudio(file, mode, userId);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("音频检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 通过URL检测音频（支持检测模式选择）
     */
    @RequiresAuth
    @Log(title = "AI音频URL检测", businessType = BusinessType.OTHER)
    @PostMapping("/url")
    public AjaxResult detectAudioByUrl(
            @RequestParam("url") String audioUrl,
            @RequestParam(value = "mode", defaultValue = "standard") String mode,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            if (audioUrl == null || audioUrl.trim().isEmpty()) {
                return AjaxResult.error("音频URL不能为空");
            }
            
            // URL格式验证
            if (!audioUrl.startsWith("http://") && !audioUrl.startsWith("https://")) {
                return AjaxResult.error("请提供有效的音频URL（需以http://或https://开头）");
            }
            
            // 验证检测模式
            if (!isValidMode(mode)) {
                return AjaxResult.error("不支持的检测模式，请选择：fast(快速)、standard(标准)、deep(深度)");
            }
            
            AiDetectionRecord record = aiAudioDetectionService.detectAudioByUrl(audioUrl, mode, userId);
            
            return AjaxResult.success("检测完成", record);
        } catch (Exception e) {
            logger.error("音频URL检测失败", e);
            return AjaxResult.error("检测失败：" + e.getMessage());
        }
    }

    /**
     * 批量检测音频（最多10个文件）
     */
    @RequiresAuth
    @Log(title = "AI音频批量检测", businessType = BusinessType.OTHER)
    @PostMapping("/batch")
    public AjaxResult batchDetect(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "mode", defaultValue = "standard") String mode,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            
            // 验证文件数量
            if (files == null || files.length == 0) {
                return AjaxResult.error("请选择要上传的音频文件");
            }
            
            if (files.length > 10) {
                return AjaxResult.error("批量检测最多支持10个文件");
            }
            
            // 验证检测模式
            if (!isValidMode(mode)) {
                return AjaxResult.error("不支持的检测模式，请选择：fast(快速)、standard(标准)、deep(深度)");
            }
            
            List<AiDetectionRecord> successResults = new ArrayList<>();
            List<Map<String, Object>> failedResults = new ArrayList<>();
            
            // 逐个检测
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                try {
                    // 验证文件
                    if (file == null || file.isEmpty()) {
                        failedResults.add(createFailedResult(i, "文件为空"));
                        continue;
                    }
                    
                    // 验证文件类型
                    String contentType = file.getContentType();
                    String fileName = file.getOriginalFilename();
                    
                    if (contentType == null || !SUPPORTED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
                        if (fileName == null || !isSupportedAudioFile(fileName)) {
                            failedResults.add(createFailedResult(i, "不支持的音频格式"));
                            continue;
                        }
                    }
                    
                    // 验证文件大小
                    if (file.getSize() > 50 * 1024 * 1024) {
                        failedResults.add(createFailedResult(i, "文件大小超过50MB"));
                        continue;
                    }
                    
                    // 执行检测
                    AiDetectionRecord record = aiAudioDetectionService.detectAudio(file, mode, userId);
                    successResults.add(record);
                    
                } catch (Exception e) {
                    logger.error("批量检测第{}个文件失败", i + 1, e);
                    failedResults.add(createFailedResult(i, e.getMessage()));
                }
            }
            
            // 返回批量检测结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", files.length);
            result.put("success", successResults.size());
            result.put("failed", failedResults.size());
            result.put("successResults", successResults);
            result.put("failedResults", failedResults);
            
            return AjaxResult.success("批量检测完成", result);
            
        } catch (Exception e) {
            logger.error("批量检测失败", e);
            return AjaxResult.error("批量检测失败：" + e.getMessage());
        }
    }

    /**
     * 获取检测模式说明
     */
    @RequiresAuth
    @GetMapping("/modes")
    public AjaxResult getDetectionModes() {
        List<Map<String, Object>> modes = new ArrayList<>();
        
        Map<String, Object> fast = new HashMap<>();
        fast.put("code", "fast");
        fast.put("name", "快速检测");
        fast.put("description", "使用3个核心检测器，10秒内完成");
        fast.put("detectors", Arrays.asList("声纹特征分析", "音频元数据分析", "启发式规则"));
        fast.put("timeEstimate", "10秒");
        fast.put("accuracy", "良好");
        modes.add(fast);
        
        Map<String, Object> standard = new HashMap<>();
        standard.put("code", "standard");
        standard.put("name", "标准检测");
        standard.put("description", "使用5个检测器，平衡准确性与速度");
        standard.put("detectors", Arrays.asList("声纹特征分析", "音频元数据分析", "频谱分析", "启发式规则", "特征匹配"));
        standard.put("timeEstimate", "30秒");
        standard.put("accuracy", "优秀");
        modes.add(standard);
        
        Map<String, Object> deep = new HashMap<>();
        deep.put("code", "deep");
        deep.put("name", "深度检测");
        deep.put("description", "使用全部7个检测器，最高准确率");
        deep.put("detectors", Arrays.asList("声纹特征分析", "音频元数据分析", "频谱分析", "语音自然度分析", "特征匹配", "语音活动检测", "启发式规则"));
        deep.put("timeEstimate", "60秒");
        deep.put("accuracy", "极高");
        modes.add(deep);
        
        return AjaxResult.success(modes);
    }

    /**
     * 验证检测模式是否有效
     */
    private boolean isValidMode(String mode) {
        return "fast".equalsIgnoreCase(mode) || 
               "standard".equalsIgnoreCase(mode) || 
               "deep".equalsIgnoreCase(mode);
    }

    /**
     * 通过文件名判断是否为支持的音频格式
     */
    private boolean isSupportedAudioFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || 
               lowerName.endsWith(".m4a") || lowerName.endsWith(".ogg") ||
               lowerName.endsWith(".flac") || lowerName.endsWith(".aac") ||
               lowerName.endsWith(".wma");
    }

    /**
     * 创建失败结果对象
     */
    private Map<String, Object> createFailedResult(int index, String reason) {
        Map<String, Object> failed = new HashMap<>();
        failed.put("index", index);
        failed.put("reason", reason);
        return failed;
    }
}
