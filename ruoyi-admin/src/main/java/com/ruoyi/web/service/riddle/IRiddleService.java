package com.ruoyi.web.service.riddle;

import java.util.List;
import java.util.Map;
import com.ruoyi.web.domain.riddle.RiddleQuestion;
import com.ruoyi.web.domain.riddle.RiddleHistory;

/**
 * 脑筋急转弯Service接口
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public interface IRiddleService 
{
    /**
     * 获取随机题目
     * 
     * @param category 分类（可选）
     * @param difficulty 难度（可选）
     * @return 题目（不包含答案）
     */
    public RiddleQuestion getRandomQuestion(String category, String difficulty);

    /**
     * 查看答案
     * 
     * @param questionId 题目ID
     * @return 完整题目信息
     */
    public RiddleQuestion viewAnswer(Long questionId);

    /**
     * 记录答题
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param userAnswer 用户答案
     * @param timeSpent 答题耗时
     * @return 结果
     */
    public Map<String, Object> submitAnswer(Long userId, Long questionId, String userAnswer, Integer timeSpent);

    /**
     * 查询答题历史
     * 
     * @param userId 用户ID
     * @return 历史列表
     */
    public List<RiddleHistory> getUserHistory(Long userId);

    /**
     * 获取用户统计
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> getUserStatistics(Long userId);

    /**
     * 点赞题目
     * 
     * @param questionId 题目ID
     * @return 结果
     */
    public int likeQuestion(Long questionId);

    /**
     * 查询题目列表（管理端）
     * 
     * @param riddleQuestion 查询条件
     * @return 题目列表
     */
    public List<RiddleQuestion> selectRiddleQuestionList(RiddleQuestion riddleQuestion);

    /**
     * 新增题目
     * 
     * @param riddleQuestion 题目信息
     * @return 结果
     */
    public int insertRiddleQuestion(RiddleQuestion riddleQuestion);

    /**
     * 修改题目
     * 
     * @param riddleQuestion 题目信息
     * @return 结果
     */
    public int updateRiddleQuestion(RiddleQuestion riddleQuestion);

    /**
     * 批量删除题目
     * 
     * @param ids 题目ID数组
     * @return 结果
     */
    public int deleteRiddleQuestionByIds(Long[] ids);
}
