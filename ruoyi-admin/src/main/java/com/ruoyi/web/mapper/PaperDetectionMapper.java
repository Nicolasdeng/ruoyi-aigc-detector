package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.PaperDetectionRecord;
import com.ruoyi.web.domain.PaperParagraphDetail;
import org.apache.ibatis.annotations.Param;

/**
 * 论文检测Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public interface PaperDetectionMapper 
{
    /**
     * 查询论文检测记录
     * 
     * @param id 论文检测记录主键
     * @return 论文检测记录
     */
    public PaperDetectionRecord selectPaperDetectionRecordById(Long id);

    /**
     * 查询论文检测记录列表
     * 
     * @param paperDetectionRecord 论文检测记录
     * @return 论文检测记录集合
     */
    public List<PaperDetectionRecord> selectPaperDetectionRecordList(PaperDetectionRecord paperDetectionRecord);

    /**
     * 新增论文检测记录
     * 
     * @param paperDetectionRecord 论文检测记录
     * @return 结果
     */
    public int insertPaperDetectionRecord(PaperDetectionRecord paperDetectionRecord);

    /**
     * 修改论文检测记录
     * 
     * @param paperDetectionRecord 论文检测记录
     * @return 结果
     */
    public int updatePaperDetectionRecord(PaperDetectionRecord paperDetectionRecord);

    /**
     * 删除论文检测记录
     * 
     * @param id 论文检测记录主键
     * @return 结果
     */
    public int deletePaperDetectionRecordById(Long id);

    /**
     * 批量删除论文检测记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deletePaperDetectionRecordByIds(Long[] ids);

    /**
     * 查询用户的检测记录列表
     * 
     * @param userId 用户ID
     * @return 检测记录列表
     */
    public List<PaperDetectionRecord> selectPaperDetectionRecordByUserId(Long userId);

    /**
     * 查询段落详情列表
     * 
     * @param detectionId 检测记录ID
     * @return 段落详情列表
     */
    public List<PaperParagraphDetail> selectParagraphDetailsByDetectionId(Long detectionId);

    /**
     * 新增段落详情
     * 
     * @param detail 段落详情
     * @return 结果
     */
    public int insertParagraphDetail(PaperParagraphDetail detail);

    /**
     * 批量新增段落详情
     * 
     * @param details 段落详情列表
     * @return 结果
     */
    public int batchInsertParagraphDetails(@Param("list") List<PaperParagraphDetail> details);

    /**
     * 查询高风险段落详情
     * 
     * @param detectionId 检测记录ID
     * @return 高风险段落列表
     */
    public List<PaperParagraphDetail> selectHighRiskParagraphs(Long detectionId);
}
