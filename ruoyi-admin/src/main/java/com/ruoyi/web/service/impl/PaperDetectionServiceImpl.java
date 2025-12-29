package com.ruoyi.web.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;
import com.ruoyi.web.mapper.PaperDetectionMapper;
import com.ruoyi.web.service.IPaperDetectionService;
import com.ruoyi.web.service.IPaperTextAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
            
            for (int i = 0; i < paragraphs.size(); i++) {
                String paragraph = paragraphs.get(i);
                
                // 分析段落风险
                BigDecimal aiRisk = textAnalyzer.analyzeParagraphRisk(paragraph);
                String riskLevel = textAnalyzer.getRiskLevel(aiRisk);
                List<String> riskTypes = textAnalyzer.identifyRiskTypes(paragraph, aiRisk);
                Map<String, Object> suggestions = textAnalyzer.generateSuggestions(paragraph, riskTypes);
                
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
                detail.setSuggestions(JSON.toJSONString(suggestions));
                
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
            
            // 8. 更新检测记录
            record.setAiScore(avgAiScore);
            record.setAiRiskLevel(aiRiskLevel);
            record.setDuplicateScore(duplicateScore);
            record.setDuplicateLevel(duplicateLevel);
            record.setStyleScore(styleScore);
            record.setHighRiskParagraphs(highRiskCount);
            record.setStatus("completed");
            record.setDetectDuration((int)((System.currentTimeMillis() - startTime) / 1000));
            
            paperDetectionMapper.updatePaperDetectionRecord(record);
            
            log.info("论文检测完成，ID: {}, 用户: {}, AI风险: {}, 耗时: {}秒", 
                detectionId, userId, avgAiScore, record.getDetectDuration());
            
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
