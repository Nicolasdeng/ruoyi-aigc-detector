package com.ruoyi.web.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.mapper.AiDetectionMapper;
import com.ruoyi.web.service.IAiImageDetectionService;
import com.ruoyi.web.service.image.IDetectionAggregator;
import com.ruoyi.web.service.image.IImageUploadService;
import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * AI图片检测Service业务层处理（重构版V2）
 * 采用模块化设计，将功能拆分为独立组件
 * 
 * @author ruoyi
 */
@Service("aiImageDetectionServiceV2")
public class AiImageDetectionServiceImplV2 implements IAiImageDetectionService {
    
    private static final Logger log = LoggerFactory.getLogger(AiImageDetectionServiceImplV2.class);

    @Autowired
    private AiDetectionMapper aiDetectionMapper;
    
    @Autowired
    private IImageUploadService imageUploadService;
    
    @Autowired
    private IDetectionAggregator detectionAggregator;
    
    @Autowired
    private List<IImageDetector> imageDetectors; // 自动注入所有检测器
    
    @Autowired(required = false)
    private List<IImageAiModelDetector> aiModelDetectors; // 自动注入所有AI模型检测器
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 检测上传的图片
     */
    @Override
    public AiDetectionRecord detectImage(MultipartFile file, Long userId) throws Exception {
        // 1. 上传图片
        Map<String, Object> uploadResult = imageUploadService.uploadImage(file);
        String fileUrl = (String) uploadResult.get("fileUrl");
        String fullPath = (String) uploadResult.get("fullPath");
        Long fileSize = ((Number) uploadResult.get("fileSize")).longValue();
        
        // 2. 创建检测记录
        AiDetectionRecord record = new AiDetectionRecord();
        record.setUserId(userId);
        record.setFileUrl(fileUrl);
        record.setFileType("image");
        record.setFileSize(fileSize);
        record.setStatus("PROCESSING");
        aiDetectionMapper.insertRecord(record);
        
        try {
            // 3. 执行多引擎检测
            List<Map<String, Object>> detectionResults = performMultiDetectorCheck(fullPath);
            
            // 4. 执行AI模型识别
            Map<String, Object> aiModelResult = performAiModelDetection(fullPath);
            
            // 5. 聚合结果
            Map<String, Object> aggregatedResult = detectionAggregator.aggregateResults(detectionResults);
            
            // 6. 合并AI模型识别结果
            if (aiModelResult != null && !aiModelResult.isEmpty()) {
                aggregatedResult.put("aiModelDetection", aiModelResult);
            }
            
            // 7. 更新记录
            updateRecordWithResults(record, aggregatedResult);
            
            log.info("图片检测完成，用户ID: {} - 文件: {} - 结果: {} - 置信度: {}", 
                    userId, fileUrl, record.getDetectionResult(), record.getConfidenceScore());
            
        } catch (Exception e) {
            log.error("图片检测失败: " + fileUrl, e);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            aiDetectionMapper.updateRecord(record);
            throw e;
        }
        
        return record;
    }

    /**
     * 通过URL检测图片
     */
    @Override
    public AiDetectionRecord detectImageByUrl(String imageUrl, Long userId) throws Exception {
        // 1. 下载图片
        String localPath = imageUploadService.downloadImageFromUrl(imageUrl);
        
        // 2. 创建检测记录
        AiDetectionRecord record = new AiDetectionRecord();
        record.setUserId(userId);
        record.setFileUrl(imageUrl);
        record.setFileType("image");
        record.setStatus("PROCESSING");
        aiDetectionMapper.insertRecord(record);
        
        try {
            // 3. 执行多引擎检测
            List<Map<String, Object>> detectionResults = performMultiDetectorCheck(localPath);
            
            // 4. 执行AI模型识别
            Map<String, Object> aiModelResult = performAiModelDetection(localPath);
            
            // 5. 聚合结果
            Map<String, Object> aggregatedResult = detectionAggregator.aggregateResults(detectionResults);
            
            // 6. 合并AI模型识别结果
            if (aiModelResult != null && !aiModelResult.isEmpty()) {
                aggregatedResult.put("aiModelDetection", aiModelResult);
            }
            
            // 7. 更新记录
            updateRecordWithResults(record, aggregatedResult);
            
            log.info("图片URL检测完成，用户ID: {} - 文件: {} - 结果: {} - 置信度: {}", 
                    userId, imageUrl, record.getDetectionResult(), record.getConfidenceScore());
            
        } catch (Exception e) {
            log.error("图片URL检测失败: " + imageUrl, e);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            aiDetectionMapper.updateRecord(record);
            throw e;
        }
        
        return record;
    }

    /**
     * 执行多检测器并发检测
     */
    private List<Map<String, Object>> performMultiDetectorCheck(String filePath) {
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        // 并发调用所有可用的检测器
        for (IImageDetector detector : imageDetectors) {
            if (detector.isAvailable()) {
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        Map<String, Object> result = detector.detect(filePath);
                        log.info("{} 检测完成: {}", detector.getName(), result);
                        return result;
                    } catch (Exception e) {
                        log.warn("{} 检测失败: {}", detector.getName(), e.getMessage());
                        return createErrorResult(detector, e.getMessage());
                    }
                }, executorService));
            } else {
                log.warn("{} 不可用，跳过", detector.getName());
            }
        }
        
        // 等待所有检测器完成（最多10秒，提高响应速度）
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));
            allOf.get(30, TimeUnit.SECONDS);
            
            for (CompletableFuture<Map<String, Object>> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    log.warn("获取检测器结果失败", e);
                }
            }
        } catch (TimeoutException e) {
            log.warn("部分检测器超时（30秒），使用已完成的结果继续检测");
            for (CompletableFuture<Map<String, Object>> future : futures) {
                if (future.isDone() && !future.isCompletedExceptionally()) {
                    try {
                        results.add(future.get());
                    } catch (Exception ex) {
                        log.warn("获取已完成结果失败", ex);
                    }
                }
            }
        } catch (Exception e) {
            log.error("等待检测器结果时发生错误", e);
            // 即使出错也尝试收集已完成的结果
            for (CompletableFuture<Map<String, Object>> future : futures) {
                if (future.isDone() && !future.isCompletedExceptionally()) {
                    try {
                        results.add(future.get());
                    } catch (Exception ex) {
                        log.warn("获取已完成结果失败", ex);
                    }
                }
            }
        }
        
        // 至少需要1个检测器成功（本地检测器应该总是成功）
        long successCount = results.stream()
                .filter(r -> r.containsKey("available") && (Boolean) r.get("available"))
                .count();
        
        if (successCount < 1) {
            log.error("所有检测引擎均失败，检测结果: {}", results);
            throw new RuntimeException("所有检测引擎均失败，无法完成检测");
        }
        
        log.info("检测完成，成功的检测器数量: {}/{}", successCount, futures.size());
        
        return results;
    }

    /**
     * 创建错误结果
     */
    private Map<String, Object> createErrorResult(IImageDetector detector, String errorMsg) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("apiName", detector.getName());
        result.put("weight", detector.getWeight());
        result.put("score", 0.5);
        result.put("isAI", false);
        result.put("error", errorMsg);
        result.put("available", false);
        return result;
    }

    /**
     * 更新检测记录
     */
    private void updateRecordWithResults(AiDetectionRecord record, Map<String, Object> aggregatedResult) {
        record.setDetectionResult((String) aggregatedResult.get("result"));
        record.setConfidenceScore((BigDecimal) aggregatedResult.get("confidence"));
        record.setDetectionDetails(JSON.toJSONString(aggregatedResult.get("details")));
        record.setApiResults(JSON.toJSONString(aggregatedResult.get("apiResults")));
        record.setStatus("COMPLETED");
        
        aiDetectionMapper.updateRecord(record);
    }

    /**
     * 执行AI模型检测
     * 识别图片可能由哪个AI工具生成
     */
    private Map<String, Object> performAiModelDetection(String filePath) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否有可用的AI模型检测器
        if (aiModelDetectors == null || aiModelDetectors.isEmpty()) {
            log.warn("未找到AI模型检测器，跳过AI模型识别");
            return result;
        }
        
        try {
            // 读取图片
            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                log.warn("无法读取图片文件: {}", filePath);
                return result;
            }
            
            // 存储所有检测器的结果
            List<Map<String, Object>> allResults = new ArrayList<>();
            IImageAiModelDetector.ImageModelDetectionResult bestResult = null;
            double highestScore = 0.0;
            
            // 调用所有AI模型检测器
            for (IImageAiModelDetector detector : aiModelDetectors) {
                try {
                    IImageAiModelDetector.ImageModelDetectionResult detectionResult = 
                        detector.detectModel(image);
                    
                    // 构建检测结果
                    Map<String, Object> detectorResult = new HashMap<>();
                    detectorResult.put("modelName", detectionResult.getModelName());
                    detectorResult.put("totalScore", detectionResult.getTotalScore());
                    detectorResult.put("colorScore", detectionResult.getColorScore());
                    detectorResult.put("textureScore", detectionResult.getTextureScore());
                    detectorResult.put("noiseScore", detectionResult.getNoiseScore());
                    detectorResult.put("styleScore", detectionResult.getStyleScore());
                    detectorResult.put("fingerprintScore", detectionResult.getFingerprintScore());
                    detectorResult.put("suggestions", detectionResult.getSuggestions());
                    
                    allResults.add(detectorResult);
                    
                    // 记录最高分
                    if (detectionResult.getTotalScore() > highestScore) {
                        highestScore = detectionResult.getTotalScore();
                        bestResult = detectionResult;
                    }
                    
                    log.info("AI模型检测 - {}: 总分={}, 色彩={}, 纹理={}, 噪声={}, 风格={}, 指纹={}", 
                            detectionResult.getModelName(),
                            detectionResult.getTotalScore(),
                            detectionResult.getColorScore(),
                            detectionResult.getTextureScore(),
                            detectionResult.getNoiseScore(),
                            detectionResult.getStyleScore(),
                            detectionResult.getFingerprintScore());
                    
                } catch (Exception e) {
                    log.warn("AI模型检测器 {} 执行失败: {}", 
                            detector.getModelName(), e.getMessage());
                }
            }
            
            // 设置最佳匹配结果
            if (bestResult != null) {
                result.put("mostLikelyModel", bestResult.getModelName());
                result.put("confidence", highestScore);
                result.put("colorScore", bestResult.getColorScore());
                result.put("textureScore", bestResult.getTextureScore());
                result.put("noiseScore", bestResult.getNoiseScore());
                result.put("styleScore", bestResult.getStyleScore());
                result.put("fingerprintScore", bestResult.getFingerprintScore());
                result.put("featureDetails", bestResult.getFeatureDetails());
                result.put("suggestions", bestResult.getSuggestions());
                result.put("allResults", allResults);
                
                log.info("AI模型识别完成 - 最可能的模型: {} (置信度: {})", 
                        bestResult.getModelName(), highestScore);
            } else {
                log.warn("所有AI模型检测器均未返回有效结果");
            }
            
        } catch (Exception e) {
            log.error("执行AI模型检测时发生错误: " + filePath, e);
        }
        
        return result;
    }
}
