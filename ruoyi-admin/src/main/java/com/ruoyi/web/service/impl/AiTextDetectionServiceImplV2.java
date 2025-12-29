package com.ruoyi.web.service.impl;

import com.ruoyi.web.service.IAiTextDetectionService;
import com.ruoyi.web.service.text.ITextDetectionAggregator;
import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI文本检测Service业务层处理（优化版）
 * 使用多个独立检测器进行并行检测，提高准确性和可维护性
 * 
 * @author ruoyi
 */
@Service("aiTextDetectionServiceV2")
public class AiTextDetectionServiceImplV2 implements IAiTextDetectionService {
    
    private static final Logger log = LoggerFactory.getLogger(AiTextDetectionServiceImplV2.class);
    
    @Autowired
    private List<ITextDetector> textDetectors;
    
    @Autowired
    private ITextDetectionAggregator aggregator;
    
    /**
     * 文本AI检测（多检测器并行分析）
     * 
     * @param text 要检测的文本
     * @return 检测结果
     * @throws Exception 检测异常
     */
    @Override
    public Map<String, Object> detectText(String text) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("开始文本AI检测，文本长度: {}，可用检测器数量: {}", text.length(), textDetectors.size());
        
        try {
            // 1. 过滤可用的检测器
            List<ITextDetector> availableDetectors = textDetectors.stream()
                .filter(ITextDetector::isAvailable)
                .collect(Collectors.toList());
            
            if (availableDetectors.isEmpty()) {
                log.warn("没有可用的文本检测器");
                return createErrorResult("没有可用的检测器");
            }
            
            log.info("可用检测器: {}", availableDetectors.stream()
                .map(ITextDetector::getName)
                .collect(Collectors.joining(", ")));
            
            // 2. 并行调用所有检测器
            List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
            for (ITextDetector detector : availableDetectors) {
                CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        Map<String, Object> result = detector.detect(text);
                        // 添加权重信息到结果中
                        result.put("weight", detector.getWeight());
                        return result;
                    } catch (Exception e) {
                        log.error("检测器 {} 执行失败", detector.getName(), e);
                        return createDetectorErrorResult(detector.getName());
                    }
                });
                futures.add(future);
            }
            
            // 3. 等待所有检测器完成（最多10秒）
            List<Map<String, Object>> results = new ArrayList<>();
            for (CompletableFuture<Map<String, Object>> future : futures) {
                try {
                    Map<String, Object> result = future.get(10, TimeUnit.SECONDS);
                    results.add(result);
                } catch (Exception e) {
                    log.warn("检测器超时或执行异常", e);
                }
            }
            
            // 4. 检查是否有足够的成功结果
            long successCount = results.stream()
                .filter(r -> !r.containsKey("error") || !(Boolean) r.get("error"))
                .count();
            
            if (successCount == 0) {
                log.error("所有检测器都失败了");
                return createErrorResult("所有检测器执行失败");
            }
            
            log.info("成功执行的检测器数量: {}/{}", successCount, availableDetectors.size());
            
            // 5. 聚合结果
            Map<String, Object> aggregatedResult = aggregator.aggregate(results, text.length());
            
            // 6. 添加执行时间
            long totalTime = System.currentTimeMillis() - startTime;
            aggregatedResult.put("totalResponseTime", totalTime);
            
            log.info("文本检测完成 - 耗时: {}ms, 综合评分: {}, 判定: {}", 
                totalTime, aggregatedResult.get("score"), aggregatedResult.get("result"));
            
            return aggregatedResult;
            
        } catch (Exception e) {
            log.error("文本检测过程中发生异常", e);
            throw new Exception("文本检测失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建检测器错误结果
     */
    private Map<String, Object> createDetectorErrorResult(String detectorName) {
        Map<String, Object> result = new HashMap<>();
        result.put("name", detectorName);
        result.put("score", 50.0);
        result.put("result", "UNCERTAIN");
        result.put("confidence", 0.0);
        result.put("responseTime", 0L);
        result.put("error", true);
        return result;
    }
    
    /**
     * 创建整体错误结果
     */
    private Map<String, Object> createErrorResult(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("score", 50.0);
        result.put("isAI", false);
        result.put("result", "UNCERTAIN");
        result.put("riskLevel", "MEDIUM");
        result.put("confidence", 0.0);
        result.put("models", new ArrayList<>());
        result.put("error", true);
        result.put("errorMessage", errorMessage);
        
        List<String> suggestions = new ArrayList<>();
        suggestions.add("检测失败，请稍后重试");
        suggestions.add("如果问题持续，请联系技术支持");
        result.put("suggestions", suggestions);
        
        return result;
    }
}
