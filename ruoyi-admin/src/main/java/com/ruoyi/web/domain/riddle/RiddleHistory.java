package com.ruoyi.web.domain.riddle;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户答题历史对象 riddle_history
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public class RiddleHistory
{
    private static final long serialVersionUID = 1L;

    /** 历史记录ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 题目ID */
    private Long questionId;

    /** 用户答案 */
    private String userAnswer;

    /** 是否正确（0错误 1正确） */
    private String isCorrect;

    /** 答题时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date answerTime;

    /** 答题耗时（秒） */
    private Integer timeSpent;

    /** 题目信息（用于返回） */
    private RiddleQuestion question;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setQuestionId(Long questionId) 
    {
        this.questionId = questionId;
    }

    public Long getQuestionId() 
    {
        return questionId;
    }

    public void setUserAnswer(String userAnswer) 
    {
        this.userAnswer = userAnswer;
    }

    public String getUserAnswer() 
    {
        return userAnswer;
    }

    public void setIsCorrect(String isCorrect) 
    {
        this.isCorrect = isCorrect;
    }

    public String getIsCorrect() 
    {
        return isCorrect;
    }

    public void setAnswerTime(Date answerTime) 
    {
        this.answerTime = answerTime;
    }

    public Date getAnswerTime() 
    {
        return answerTime;
    }

    public void setTimeSpent(Integer timeSpent) 
    {
        this.timeSpent = timeSpent;
    }

    public Integer getTimeSpent() 
    {
        return timeSpent;
    }

    public RiddleQuestion getQuestion() 
    {
        return question;
    }

    public void setQuestion(RiddleQuestion question) 
    {
        this.question = question;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("questionId", getQuestionId())
            .append("userAnswer", getUserAnswer())
            .append("isCorrect", getIsCorrect())
            .append("answerTime", getAnswerTime())
            .append("timeSpent", getTimeSpent())
            .toString();
    }
}
