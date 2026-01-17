package com.ruoyi.web.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;
import com.ruoyi.web.mapper.PaperDetectionMapper;
import com.ruoyi.web.service.IPaperDetectionService;
import com.ruoyi.web.service.IPaperTextAnalyzer;
import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.ModelDetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 论文检测服务实现
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class PaperDetectionServiceImpl implements IPaperDetectionService
{
    private static final Logger log = LoggerFactory.getLogger(PaperDetectionServiceImpl.class);
    
    @Autowired
    private PaperDetectionMapper paperDetectionMapper;
    
    @Autowired
    private IPaperTextAnalyzer textAnalyzer;
    
    @Autowired
    private List<IAiModelDetector> aiModelDetectors;
    
    // 创建线程池用于并行执行AI模型检测
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Override
    @Transactional
    public Long submitDetection(String title, String content, Long userId)
    {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 创建检测记录
            PaperDetectionRecord record = new PaperDetectionRecord();
            record.setUserId(userId);
            record.setTitle(title);
            record.setContent(content);
            record.setWordCount(textAnalyzer.countWords(content));
            record.setStatus("processing");
            
            paperDetectionMapper.insertPaperDetectionRecord(record);
            Long detectionId = record.getId();
            
            // 3. 文本预处理 - 分段
            List<String> paragraphs = splitParagraphs(content);
            record.setTotalParagraphs(paragraphs.size());
            
            // 4. 逐段检测
            List<PaperParagraphDetail> details = new ArrayList<>();
            int highRiskCount = 0;
            
            // AI模型检测统计（新版）
            Map<String, Integer> aiModelCountMap = new HashMap<>();
            Map<String, List<Double>> aiModelScoresMap = new HashMap<>();
            List<Map<String, Object>> allModelResults = new ArrayList<>();
            
            for (int i = 0; i < paragraphs.size(); i++) {
                String paragraph = paragraphs.get(i);
                
                // 分析段落风险
                BigDecimal aiRisk = textAnalyzer.analyzeParagraphRisk(paragraph);
                String riskLevel = textAnalyzer.getRiskLevel(aiRisk);
                List<String> riskTypes = textAnalyzer.identifyRiskTypes(paragraph, aiRisk);
                Map<String, Object> suggestions = textAnalyzer.generateSuggestions(paragraph, riskTypes);
                
                // ===== 新增：并行执行所有AI模型检测器 =====
                List<ModelDetectionResult> modelResults = detectWithAllModels(paragraph);
                
                // 找出最匹配的AI模型（得分最高的）
                ModelDetectionResult bestMatch = modelResults.stream()
                    .max(Comparator.comparingDouble(ModelDetectionResult::getScore))
                    .orElse(null);
                
                if (bestMatch != null && bestMatch.getScore() >= 50.0) {
                    // 统计各AI模型检测次数
                    aiModelCountMap.put(bestMatch.getModelName(), 
                        aiModelCountMap.getOrDefault(bestMatch.getModelName(), 0) + 1);
                    
                    // 收集每个模型的得分
                    aiModelScoresMap.computeIfAbsent(bestMatch.getModelName(), k -> new ArrayList<>())
                        .add(bestMatch.getScore());
                    
                    // 保存详细检测结果
                    Map<String, Object> resultDetail = new HashMap<>();
                    resultDetail.put("paragraphIndex", i + 1);
                    resultDetail.put("detectedModel", bestMatch.getModelName());
                    resultDetail.put("confidence", bestMatch.getScore());
                    resultDetail.put("features", bestMatch.getFeatureDetails());
                    resultDetail.put("suggestions", bestMatch.getSuggestions());
                    allModelResults.add(resultDetail);
                }
                
                if ("high".equals(riskLevel) || "medium".equals(riskLevel)) {
                    highRiskCount++;
                }
                
                // 创建段落详情
                PaperParagraphDetail detail = new PaperParagraphDetail();
                detail.setDetectionId(detectionId);
                detail.setParagraphIndex(i + 1);
                detail.setParagraphContent(paragraph);
                detail.setWordCount(textAnalyzer.countWords(paragraph));
                detail.setAiRisk(aiRisk);
                detail.setRiskLevel(riskLevel);
                detail.setRiskTypes(JSON.toJSONString(riskTypes));
                
                // 将AI模型检测结果和原有建议合并
                Map<String, Object> enhancedSuggestions = new HashMap<>(suggestions);
                if (bestMatch != null && bestMatch.getScore() >= 50.0) {
                    enhancedSuggestions.put("aiModelDetection", Map.of(
                        "detectedModel", bestMatch.getModelName(),
                        "confidence", bestMatch.getScore(),
                        "modelSuggestions", bestMatch.getSuggestions()
                    ));
                }
                detail.setSuggestions(JSON.toJSONString(enhancedSuggestions));
                
                details.add(detail);
            }
            
            // 5. 批量插入段落详情
            if (!details.isEmpty()) {
                paperDetectionMapper.batchInsertParagraphDetails(details);
            }
            
            // 6. 计算整体评分
            BigDecimal avgAiScore = details.stream()
                .map(PaperParagraphDetail::getAiRisk)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(details.size()), 2, RoundingMode.HALF_UP);
            
            String aiRiskLevel = textAnalyzer.getRiskLevel(avgAiScore);
            
            // 7. 计算重复度和风格评分
            BigDecimal duplicateScore = textAnalyzer.calculateDuplicateScore(content);
            String duplicateLevel = textAnalyzer.getRiskLevel(duplicateScore);
            
            BigDecimal styleScore = textAnalyzer.calculateStyleScore(content);
            
            // 8. 生成增强版AI模型检测摘要
            Map<String, Object> aiModelSummary = new HashMap<>();
            if (!aiModelCountMap.isEmpty()) {
                // 找出检测次数最多的AI模型
                String mostDetectedModel = aiModelCountMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
                
                // 计算该模型的平均置信度
                double avgConfidence = 0.0;
                if (mostDetectedModel != null && aiModelScoresMap.containsKey(mostDetectedModel)) {
                    List<Double> scores = aiModelScoresMap.get(mostDetectedModel);
                    avgConfidence = scores.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);
                }
                
                // 计算所有模型的分布百分比
                int totalDetections = aiModelCountMap.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
                
                Map<String, Object> modelDistribution = new HashMap<>();
                for (Map.Entry<String, Integer> entry : aiModelCountMap.entrySet()) {
                    String modelName = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (count * 100.0) / paragraphs.size();
                    
                    double modelAvgConfidence = 0.0;
                    if (aiModelScoresMap.containsKey(modelName)) {
                        modelAvgConfidence = aiModelScoresMap.get(modelName).stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    }
                    
                    modelDistribution.put(modelName, Map.of(
                        "count", count,
                        "percentage", Math.round(percentage * 10) / 10.0,
                        "avgConfidence", Math.round(modelAvgConfidence * 10) / 10.0
                    ));
                }
                
                aiModelSummary.put("mostDetectedModel", mostDetectedModel);
                aiModelSummary.put("detectionCount", aiModelCountMap.get(mostDetectedModel));
                aiModelSummary.put("totalParagraphs", paragraphs.size());
                aiModelSummary.put("avgConfidence", Math.round(avgConfidence * 10) / 10.0);
                aiModelSummary.put("modelDistribution", modelDistribution);
                aiModelSummary.put("detectionDetails", allModelResults);
            }
            
            // 9. 更新检测记录
            record.setAiScore(avgAiScore);
            record.setAiRiskLevel(aiRiskLevel);
            record.setDuplicateScore(duplicateScore);
            record.setDuplicateLevel(duplicateLevel);
            record.setStyleScore(styleScore);
            record.setHighRiskParagraphs(highRiskCount);
            record.setStatus("completed");
            record.setDetectDuration((int)((System.currentTimeMillis() - startTime) / 1000));
            
            // 将增强版AI模型检测摘要保存到remark字段
            if (!aiModelSummary.isEmpty()) {
                String modelName = (String) aiModelSummary.get("mostDetectedModel");
                int detectionCount = (Integer) aiModelSummary.get("detectionCount");
                int totalParagraphs = (Integer) aiModelSummary.get("totalParagraphs");
                double avgConfidence = (Double) aiModelSummary.get("avgConfidence");
                
                String aiModelInfo = String.format("AI模型检测: %s (检出%d/%d段, 平均置信度%.1f%%)",
                    modelName, detectionCount, totalParagraphs, avgConfidence);
                
                // 如果有其他模型也被检测到，添加到备注中
                @SuppressWarnings("unchecked")
                Map<String, Object> distribution = (Map<String, Object>) aiModelSummary.get("modelDistribution");
                if (distribution.size() > 1) {
                    List<String> otherModels = distribution.entrySet().stream()
                        .filter(e -> !e.getKey().equals(modelName))
                        .map(e -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> info = (Map<String, Object>) e.getValue();
                            return String.format("%s(%.1f%%)", e.getKey(), info.get("percentage"));
                        })
                        .collect(Collectors.toList());
                    
                    if (!otherModels.isEmpty()) {
                        aiModelInfo += " | 其他: " + String.join(", ", otherModels);
                    }
                }
                
                record.setRemark(aiModelInfo);
            }
            
            paperDetectionMapper.updatePaperDetectionRecord(record);
            
            log.info("论文检测完成，ID: {}, 用户: {}, AI风险: {}, AI模型: {} (置信度: {}%), 耗时: {}秒", 
                detectionId, userId, avgAiScore, 
                aiModelSummary.getOrDefault("mostDetectedModel", "未检测到"),
                aiModelSummary.containsKey("avgConfidence") ? 
                    String.format("%.1f", (Double)aiModelSummary.get("avgConfidence")) : "N/A",
                record.getDetectDuration());
            
            return detectionId;
            
        } catch (Exception e) {
            log.error("论文检测失败: ", e);
            throw new RuntimeException("检测失败: " + e.getMessage());
        }
    }
    
    @Override
    public PaperDetectionRecord getDetectionRecord(Long id)
    {
        return paperDetectionMapper.selectPaperDetectionRecordById(id);
    }
    
    @Override
    public List<PaperDetectionRecord> selectPaperDetectionRecordList(PaperDetectionRecord paperDetectionRecord)
    {
        return paperDetectionMapper.selectPaperDetectionRecordList(paperDetectionRecord);
    }
    
    @Override
    public List<PaperParagraphDetail> getParagraphDetails(Long detectionId)
    {
        return paperDetectionMapper.selectParagraphDetailsByDetectionId(detectionId);
    }
    
    @Override
    public List<PaperParagraphDetail> getHighRiskParagraphs(Long detectionId)
    {
        return paperDetectionMapper.selectHighRiskParagraphs(detectionId);
    }
    
    @Override
    public Map<String, Object> getSuggestions(Long detectionId)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 获取高风险段落
        List<PaperParagraphDetail> highRiskParagraphs = getHighRiskParagraphs(detectionId);
        
        List<Map<String, Object>> paragraphSuggestions = new ArrayList<>();
        
        for (PaperParagraphDetail detail : highRiskParagraphs) {
            Map<String, Object> item = new HashMap<>();
            item.put("paragraphIndex", detail.getParagraphIndex());
            item.put("paragraphContent", detail.getParagraphContent());
            item.put("aiRisk", detail.getAiRisk());
            item.put("riskLevel", detail.getRiskLevel());
            
            // 解析风险类型
            List<String> riskTypes = JSON.parseArray(detail.getRiskTypes(), String.class);
            item.put("riskTypes", riskTypes);
            
            // 解析建议
            Map<String, Object> suggestions = JSON.parseObject(detail.getSuggestions(), Map.class);
            item.put("suggestions", suggestions);
            
            paragraphSuggestions.add(item);
        }
        
        result.put("totalHighRisk", highRiskParagraphs.size());
        result.put("paragraphs", paragraphSuggestions);
        
        return result;
    }
    
    @Override
    public List<PaperDetectionRecord> getUserDetectionHistory(Long userId)
    {
        return paperDetectionMapper.selectPaperDetectionRecordByUserId(userId);
    }
    
    @Override
    public int deletePaperDetectionRecordById(Long id)
    {
        return paperDetectionMapper.deletePaperDetectionRecordById(id);
    }
    
    @Override
    public int deletePaperDetectionRecordByIds(Long[] ids)
    {
        return paperDetectionMapper.deletePaperDetectionRecordByIds(ids);
    }
    
    /**
     * 使用所有AI模型检测器并行检测文本
     * 
     * @param text 待检测文本
     * @return 所有模型的检测结果列表
     */
    private List<ModelDetectionResult> detectWithAllModels(String text)
    {
        if (aiModelDetectors == null || aiModelDetectors.isEmpty()) {
            log.warn("未找到任何AI模型检测器");
            return Collections.emptyList();
        }
        
        try {
            // 并行执行所有检测器
            List<CompletableFuture<ModelDetectionResult>> futures = aiModelDetectors.stream()
                .map(detector -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // 调用检测器的 detectModel 方法获取得分
                        BigDecimal score = detector.detectModel(text);
                        
                        // 获取模型名称
                        String modelName = detector.getModelName();
                        
                        // 获取特征详情
                        Map<String, String> featureDetails = detector.getFeatureDetails(text);
                        
                        // 生成优化建议
                        List<String> suggestions = detector.generateSuggestions(text, score.doubleValue());
                        
                        // 创建并返回检测结果对象
                        return new ModelDetectionResult(
                            modelName,
                            score.doubleValue(),
                            featureDetails,
                            suggestions
                        );
                    } catch (Exception e) {
                        log.error("AI模型检测器执行失败: {}", detector.getClass().getSimpleName(), e);
                        return null;
                    }
                }, executorService))
                .collect(Collectors.toList());
            
            // 等待所有检测完成并收集结果
            return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("并行AI模型检测失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 将论文内容分段
     */
    private List<String> splitParagraphs(String content)
    {
        if (content == null || content.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按空行或换行符分段
        String[] paragraphs = content.split("\n+");
        
        return Arrays.stream(paragraphs)
            .map(String::trim)
            .filter(p -> !p.isEmpty() && p.length() > 20) // 过滤过短的段落
            .collect(Collectors.toList());
    }
}
