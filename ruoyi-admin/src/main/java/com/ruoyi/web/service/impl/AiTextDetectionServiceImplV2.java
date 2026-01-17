package com.ruoyi.web.service.impl;

import com.ruoyi.web.service.IAiTextDetectionService;
import com.ruoyi.web.service.paper.IAiModelDetector;
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
    
    @Autowired(required = false)
    private List<IAiModelDetector> aiModelDetectors;
    
    /**
     * 文本AI检测（多检测器并行分析）
     * 
     * @param text 要检测的文本
     * @param userId 用户ID
     * @return 检测结果
     * @throws Exception 检测异常
     */
    @Override
    public Map<String, Object> detectText(String text, Long userId) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("开始文本AI检测，用户ID: {}, 文本长度: {}，可用检测器数量: {}", userId, text.length(), textDetectors.size());
        
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
            
            // 6. AI模型反向推理检测（增强检测准确性）
            Map<String, Object> aiModelResult = detectAiModel(text);
            if (aiModelResult != null && (Boolean) aiModelResult.getOrDefault("detected", false)) {
                // 将AI模型检测结果添加到聚合结果中
                aggregatedResult.put("aiModelDetection", aiModelResult);
                
                // 如果检测到特定AI模型，调整最终评分和建议
                double modelScore = ((Number) aiModelResult.get("maxScore")).doubleValue();
                double confidence = ((Number) aiModelResult.get("confidence")).doubleValue();
                
                if (modelScore >= 60 && confidence >= 60) {
                    // 高置信度检测到AI模型特征，提高风险评分
                    Double currentScore = (Double) aggregatedResult.get("score");
                    double adjustedScore = Math.max(currentScore, modelScore * 0.8); // 取当前分数和模型分数的较大值
                    aggregatedResult.put("score", adjustedScore);
                    
                    // 更新判定结果
                    if (adjustedScore >= 70) {
                        aggregatedResult.put("result", "AI");
                        aggregatedResult.put("isAI", true);
                        aggregatedResult.put("riskLevel", "HIGH");
                    }
                    
                    // 添加AI模型相关的优化建议
                    @SuppressWarnings("unchecked")
                    List<String> suggestions = (List<String>) aggregatedResult.getOrDefault("suggestions", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<String> modelSuggestions = (List<String>) aiModelResult.get("modelSuggestions");
                    if (modelSuggestions != null && !modelSuggestions.isEmpty()) {
                        // 在建议列表开头添加模型特征信息
                        String detectedModel = (String) aiModelResult.get("detectedModel");
                        suggestions.add(0, String.format("检测到%s生成特征(置信度: %.0f%%)", detectedModel, confidence));
                        suggestions.addAll(modelSuggestions);
                        aggregatedResult.put("suggestions", suggestions);
                    }
                    
                    log.info("AI模型检测: 检测到{}, 评分: {}, 置信度: {}%", 
                        aiModelResult.get("detectedModel"), modelScore, confidence);
                }
            }
            
            // 7. 添加执行时间
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
    
    /**
     * AI模型检测 - 反向推理识别可能使用的AI模型
     * 集成论文检测的AI模型检测器，提高文本检测准确性
     * 
     * @param text 待检测文本
     * @return 检测结果，包含最可能的AI模型及置信度
     */
    private Map<String, Object> detectAiModel(String text) {
        Map<String, Object> result = new HashMap<>();
        
        if (aiModelDetectors == null || aiModelDetectors.isEmpty()) {
            result.put("detected", false);
            result.put("message", "AI模型检测器未配置");
            return result;
        }
        
        try {
            // 对每个AI模型进行检测
            Map<String, Double> modelScores = new HashMap<>();
            Map<String, Map<String, String>> modelFeatures = new HashMap<>();
            
        for (IAiModelDetector detector : aiModelDetectors) {
            try {
                String modelName = detector.getModelName();
                double score = detector.detectModel(text).doubleValue();
                Map<String, String> features = detector.getFeatureDetails(text);
                
                modelScores.put(modelName, score);
                modelFeatures.put(modelName, features);
                } catch (Exception e) {
                    log.warn("AI模型检测器 {} 执行失败", detector.getClass().getSimpleName(), e);
                }
            }
            
            if (modelScores.isEmpty()) {
                result.put("detected", false);
                result.put("message", "未检测到AI模型特征");
                return result;
            }
            
            // 找出得分最高的模型
            Map.Entry<String, Double> maxEntry = modelScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
            
            if (maxEntry == null) {
                result.put("detected", false);
                return result;
            }
            
            String detectedModel = maxEntry.getKey();
            double maxScore = maxEntry.getValue();
            
            // 计算置信度（基于最高分与次高分的差距）
            List<Double> sortedScores = modelScores.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
            
            double confidence = 0;
            if (sortedScores.size() >= 2) {
                double gap = sortedScores.get(0) - sortedScores.get(1);
                confidence = Math.min(gap / sortedScores.get(0), 1.0) * 100;
            } else if (sortedScores.size() == 1) {
                confidence = sortedScores.get(0) > 50 ? 80 : 50;
            }
            
            // 组装结果
            result.put("detected", maxScore >= 40); // 阈值：40分以上认为检测到AI特征
            result.put("detectedModel", detectedModel);
            result.put("maxScore", maxScore);
            result.put("confidence", Math.round(confidence * 100) / 100.0);
            result.put("allScores", modelScores);
            result.put("modelFeatures", modelFeatures.get(detectedModel));
            
            // 如果置信度较高，获取该模型的优化建议
            if (maxScore >= 50) {
                IAiModelDetector detector = aiModelDetectors.stream()
                    .filter(d -> d.getModelName().equals(detectedModel))
                    .findFirst()
                    .orElse(null);
                
                if (detector != null) {
                    List<String> suggestions = detector.generateSuggestions(text, maxScore);
                    result.put("modelSuggestions", suggestions);
                }
            }
            
            log.debug("AI模型检测完成 - 检测到: {}, 评分: {}, 置信度: {}%", 
                detectedModel, maxScore, confidence);
            
        } catch (Exception e) {
            log.error("AI模型检测过程中发生异常", e);
            result.put("detected", false);
            result.put("error", true);
            result.put("errorMessage", e.getMessage());
        }
        
        return result;
    }
}
