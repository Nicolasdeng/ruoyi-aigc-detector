package com.ruoyi.web.service;

import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;

import java.util.List;
import java.util.Map;

/**
 * 论文检测服务接口
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface IPaperDetectionService 
{
    /**
     * 提交论文检测
     * 
     * @param title 论文标题
     * @param content 论文内容
     * @param userId 用户ID
     * @return 检测记录ID
     */
    Long submitDetection(String title, String content, Long userId);
    
    /**
     * 查询检测记录
     * 
     * @param id 检测记录ID
     * @return 检测记录
     */
    PaperDetectionRecord getDetectionRecord(Long id);
    
    /**
     * 查询检测记录列表
     * 
     * @param paperDetectionRecord 查询条件
     * @return 检测记录列表
     */
    List<PaperDetectionRecord> selectPaperDetectionRecordList(PaperDetectionRecord paperDetectionRecord);
    
    /**
     * 查询段落详情列表
     * 
     * @param detectionId 检测记录ID
     * @return 段落详情列表
     */
    List<PaperParagraphDetail> getParagraphDetails(Long detectionId);
    
    /**
     * 查询高风险段落
     * 
     * @param detectionId 检测记录ID
     * @return 高风险段落列表
     */
    List<PaperParagraphDetail> getHighRiskParagraphs(Long detectionId);
    
    /**
     * 获取修改建议
     * 
     * @param detectionId 检测记录ID
     * @return 修改建议
     */
    Map<String, Object> getSuggestions(Long detectionId);
    
    /**
     * 查询用户的检测历史
     * 
     * @param userId 用户ID
     * @return 检测历史列表
     */
    List<PaperDetectionRecord> getUserDetectionHistory(Long userId);
    
    /**
     * 删除检测记录
     * 
     * @param id 检测记录ID
     * @return 结果
     */
    int deletePaperDetectionRecordById(Long id);
    
    /**
     * 批量删除检测记录
     * 
     * @param ids 需要删除的检测记录主键集合
     * @return 结果
     */
    int deletePaperDetectionRecordByIds(Long[] ids);
}
