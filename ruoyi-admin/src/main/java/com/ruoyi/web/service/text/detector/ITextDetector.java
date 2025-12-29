package com.ruoyi.web.service.text.detector;

import java.util.Map;

/**
 * 文本检测器接口
 * 定义统一的文本AI检测方法
 * 
 * @author ruoyi
 */
public interface ITextDetector {
    
    /**
     * 检测文本是否为AI生成
     * 
     * @param text 要检测的文本内容
     * @return 检测结果 Map，包含以下字段：
     *         - name: 检测器名称
     *         - score: AI生成概率分数(0-100)
     *         - result: 检测结果(AI_GENERATED/HUMAN_CREATED/UNCERTAIN)
     *         - confidence: 置信度(0-1)
     *         - responseTime: 响应时间(毫秒)
     *         - details: 详细信息(可选)
     */
    Map<String, Object> detect(String text);
    
    /**
     * 获取检测器名称
     * 
     * @return 检测器名称
     */
    String getName();
    
    /**
     * 获取检测器权重
     * 用于结果聚合时的加权计算
     * 
     * @return 权重值(0-1)
     */
    double getWeight();
    
    /**
     * 检测器是否可用
     * 
     * @return true-可用，false-不可用
     */
    boolean isAvailable();
}
