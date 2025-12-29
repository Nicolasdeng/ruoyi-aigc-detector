package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.AiDetectionRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI检测Mapper接口
 * 
 * @author ruoyi
 */
public interface AiDetectionMapper {
    
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
     * 新增检测记录
     * 
     * @param record 检测记录
     * @return 结果
     */
    int insertRecord(AiDetectionRecord record);

    /**
     * 修改检测记录
     * 
     * @param record 检测记录
     * @return 结果
     */
    int updateRecord(AiDetectionRecord record);

    /**
     * 删除检测记录
     * 
     * @param id 检测记录主键
     * @return 结果
     */
    int deleteRecordById(Long id);

    /**
     * 批量删除检测记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteRecordByIds(Long[] ids);

    /**
     * 查询总检测数
     * 
     * @return 总数
     */
    int selectTotalCount();

    /**
     * 根据检测结果查询数量
     * 
     * @param result 检测结果
     * @return 数量
     */
    int selectCountByResult(@Param("result") String result);

    /**
     * 查询今日检测数
     * 
     * @return 今日数量
     */
    int selectTodayCount();
}
