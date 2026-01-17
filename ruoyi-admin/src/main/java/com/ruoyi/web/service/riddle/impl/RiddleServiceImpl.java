package com.ruoyi.web.service.riddle.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.domain.riddle.RiddleQuestion;
import com.ruoyi.web.domain.riddle.RiddleHistory;
import com.ruoyi.web.mapper.riddle.RiddleQuestionMapper;
import com.ruoyi.web.mapper.riddle.RiddleHistoryMapper;
import com.ruoyi.web.service.riddle.IRiddleService;

/**
 * 脑筋急转弯Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
@Service
public class RiddleServiceImpl implements IRiddleService 
{
    @Autowired
    private RiddleQuestionMapper riddleQuestionMapper;

    @Autowired
    private RiddleHistoryMapper riddleHistoryMapper;

    /**
     * 获取随机题目（不返回答案）
     */
    @Override
    public RiddleQuestion getRandomQuestion(String category, String difficulty)
    {
        RiddleQuestion question = riddleQuestionMapper.selectRandomQuestion(category, difficulty);
        if (question != null) {
            // 增加查看次数
            riddleQuestionMapper.increaseViews(question.getId());
            // 不返回答案
            question.setAnswer(null);
            question.setExplanation(null);
        }
        return question;
    }

    /**
     * 查看答案
     */
    @Override
    public RiddleQuestion viewAnswer(Long questionId)
    {
        RiddleQuestion question = riddleQuestionMapper.selectRiddleQuestionById(questionId);
        return question;
    }

    /**
     * 提交答案
     */
    @Override
    public Map<String, Object> submitAnswer(Long userId, Long questionId, String userAnswer, Integer timeSpent)
    {
        Map<String, Object> result = new HashMap<>();
        
        // 获取题目
        RiddleQuestion question = riddleQuestionMapper.selectRiddleQuestionById(questionId);
        if (question == null) {
            result.put("success", false);
            result.put("message", "题目不存在");
            return result;
        }

        // 判断答案是否正确（忽略大小写和空格）
        String correctAnswer = question.getAnswer().trim().toLowerCase();
        String userAns = userAnswer.trim().toLowerCase();
        boolean isCorrect = correctAnswer.equals(userAns) || correctAnswer.contains(userAns) || userAns.contains(correctAnswer);

        // 记录答题历史（仅登录用户）
        if (userId != null) {
            RiddleHistory history = new RiddleHistory();
            history.setUserId(userId);
            history.setQuestionId(questionId);
            history.setUserAnswer(userAnswer);
            history.setIsCorrect(isCorrect ? "1" : "0");
            history.setAnswerTime(new Date());
            history.setTimeSpent(timeSpent);
            riddleHistoryMapper.insertRiddleHistory(history);
        }

        // 返回结果
        result.put("success", true);
        result.put("isCorrect", isCorrect);
        result.put("correctAnswer", question.getAnswer());
        result.put("explanation", question.getExplanation());
        
        return result;
    }

    /**
     * 查询用户历史
     */
    @Override
    public List<RiddleHistory> getUserHistory(Long userId)
    {
        RiddleHistory query = new RiddleHistory();
        query.setUserId(userId);
        return riddleHistoryMapper.selectRiddleHistoryList(query);
    }

    /**
     * 获取用户统计
     */
    @Override
    public Map<String, Object> getUserStatistics(Long userId)
    {
        return riddleHistoryMapper.selectUserStatistics(userId);
    }

    /**
     * 点赞题目
     */
    @Override
    public int likeQuestion(Long questionId)
    {
        return riddleQuestionMapper.increaseLikes(questionId);
    }

    /**
     * 查询题目列表
     */
    @Override
    public List<RiddleQuestion> selectRiddleQuestionList(RiddleQuestion riddleQuestion)
    {
        return riddleQuestionMapper.selectRiddleQuestionList(riddleQuestion);
    }

    /**
     * 新增题目
     */
    @Override
    public int insertRiddleQuestion(RiddleQuestion riddleQuestion)
    {
        return riddleQuestionMapper.insertRiddleQuestion(riddleQuestion);
    }

    /**
     * 修改题目
     */
    @Override
    public int updateRiddleQuestion(RiddleQuestion riddleQuestion)
    {
        return riddleQuestionMapper.updateRiddleQuestion(riddleQuestion);
    }

    /**
     * 批量删除题目
     */
    @Override
    public int deleteRiddleQuestionByIds(Long[] ids)
    {
        return riddleQuestionMapper.deleteRiddleQuestionByIds(ids);
    }
}
