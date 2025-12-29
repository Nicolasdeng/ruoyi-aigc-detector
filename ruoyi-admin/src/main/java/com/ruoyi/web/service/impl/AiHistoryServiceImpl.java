package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.mapper.AiDetectionMapper;
import com.ruoyi.web.service.IAiHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI检测历史记录Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class AiHistoryServiceImpl implements IAiHistoryService {
    
    @Autowired
    private AiDetectionMapper aiDetectionMapper;

    /**
     * 查询检测记录
     * 
     * @param id 检测记录主键
     * @return 检测记录
     */
    @Override
    public AiDetectionRecord selectRecordById(Long id) {
        return aiDetectionMapper.selectRecordById(id);
    }

    /**
     * 查询检测记录列表
     * 
     * @param record 检测记录
     * @return 检测记录集合
     */
    @Override
    public List<AiDetectionRecord> selectRecordList(AiDetectionRecord record) {
        return aiDetectionMapper.selectRecordList(record);
    }

    /**
     * 批量删除检测记录
     * 
     * @param ids 需要删除的检测记录主键
     * @return 结果
     */
    @Override
    public int deleteRecordByIds(Long[] ids) {
        return aiDetectionMapper.deleteRecordByIds(ids);
    }

    /**
     * 删除检测记录信息
     * 
     * @param id 检测记录主键
     * @return 结果
     */
    @Override
    public int deleteRecordById(Long id) {
        return aiDetectionMapper.deleteRecordById(id);
    }
}
