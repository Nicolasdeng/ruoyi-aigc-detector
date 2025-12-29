package com.ruoyi.web.service;

import java.util.Map;

/**
 * AI文本检测Service接口
 * 
 * @author ruoyi
 */
public interface IAiTextDetectionService {
    
    /**
     * 文本AI检测（多模型分析）
     * 
     * @param text 待检测的文本内容
     * @return 检测结果（包含多个模型的评分）
     */
    Map<String, Object> detectText(String text) throws Exception;
}
