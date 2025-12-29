package com.ruoyi.web.service;

import com.ruoyi.web.domain.AiDetectionRecord;

import java.util.List;

/**
 * AI检测历史记录Service接口
 * 
 * @author ruoyi
 */
public interface IAiHistoryService {
    
    /**
     * 查询检测记录
     * 
     * @param id 检测记录主键
     * @return 检测记录
     */
    AiDetectionRecord selectRecordById(Long id);

    /**
     * 查询检测记录列表
     * 
     * @param record 检测记录
     * @return 检测记录集合
     */
    List<AiDetectionRecord> selectRecordList(AiDetectionRecord record);

    /**
     * 批量删除检测记录
     * 
     * @param ids 需要删除的检测记录主键集合
     * @return 结果
     */
    int deleteRecordByIds(Long[] ids);

    /**
     * 删除检测记录信息
     * 
     * @param id 检测记录主键
     * @return 结果
     */
    int deleteRecordById(Long id);
}
