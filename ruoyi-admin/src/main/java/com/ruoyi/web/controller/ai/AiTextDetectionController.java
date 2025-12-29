package com.ruoyi.web.controller.ai;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.IAiTextDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI文本检测Controller（优化版）
 * 支持多检测器并行分析，提供更准确的检测结果
 * 
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/ai/detection/text")
public class AiTextDetectionController extends BaseController {
    
    @Autowired
    private IAiTextDetectionService aiTextDetectionService;

    /**
     * 文本AI检测（多检测器并行分析）
     * 
     * @param params 请求参数，包含text字段
     * @return 检测结果，包括综合评分、各检测器详情、风险等级等
     */
    @Anonymous
    @PostMapping("/detect")
    public AjaxResult detectText(@RequestBody Map<String, String> params) {
        try {
            String text = params.get("text");
            
            // 参数验证
            if (text == null || text.trim().isEmpty()) {
                return error("文本内容不能为空");
            }
            if (text.length() > 20000) {
                return error("文本长度不能超过20000字符");
            }
            if (text.length() < 10) {
                return error("文本内容过短，请输入至少10个字符");
            }
            
            // 调用优化的多检测器并行检测
            Map<String, Object> result = aiTextDetectionService.detectText(text);
            
            // 检查是否有错误
            if (result.containsKey("error") && (Boolean) result.get("error")) {
                String errorMessage = (String) result.getOrDefault("errorMessage", "检测失败");
                return error(errorMessage);
            }
            
            return success(result);
            
        } catch (Exception e) {
            logger.error("文本检测失败", e);
            return error("检测失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取可用检测器列表
     * 
     * @return 检测器列表
     */
    @Anonymous
    @GetMapping("/detectors")
    public AjaxResult getDetectors() {
        try {
            // 这里可以添加获取检测器列表的逻辑
            return success("检测器信息获取成功");
        } catch (Exception e) {
            logger.error("获取检测器列表失败", e);
            return error("获取失败: " + e.getMessage());
        }
    }
}
