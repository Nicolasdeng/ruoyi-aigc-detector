package com.ruoyi.web.mapper.riddle;

import java.util.List;
import com.ruoyi.web.domain.riddle.RiddleHistory;
import org.apache.ibatis.annotations.Param;

/**
 * 答题历史Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public interface RiddleHistoryMapper 
{
    /**
     * 查询答题历史列表
     * 
     * @param riddleHistory 答题历史
     * @return 答题历史集合
     */
    public List<RiddleHistory> selectRiddleHistoryList(RiddleHistory riddleHistory);

    /**
     * 新增答题历史
     * 
     * @param riddleHistory 答题历史
     * @return 结果
     */
    public int insertRiddleHistory(RiddleHistory riddleHistory);

    /**
     * 查询用户答题统计
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    public java.util.Map<String, Object> selectUserStatistics(Long userId);

    /**
     * 查询用户是否已答过该题
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 历史记录
     */
    public RiddleHistory selectByUserAndQuestion(@Param("userId") Long userId, 
                                                 @Param("questionId") Long questionId);
}
