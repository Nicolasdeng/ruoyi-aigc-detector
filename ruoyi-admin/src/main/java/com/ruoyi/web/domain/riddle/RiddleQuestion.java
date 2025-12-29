package com.ruoyi.web.domain.riddle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 脑筋急转弯题目对象 riddle_question
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
public class RiddleQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 题目ID */
    private Long id;

    /** 分类（logic:逻辑推理/word:文字游戏/math:数学思维/life:生活常识/funny:搞笑幽默） */
    @Excel(name = "分类")
    private String category;

    /** 难度（easy:简单/medium:中等/hard:困难） */
    @Excel(name = "难度")
    private String difficulty;

    /** 题目内容 */
    @Excel(name = "题目内容")
    private String question;

    /** 答案 */
    @Excel(name = "答案")
    private String answer;

    /** 答案解析 */
    @Excel(name = "答案解析")
    private String explanation;

    /** 查看次数 */
    @Excel(name = "查看次数")
    private Integer views;

    /** 点赞数 */
    @Excel(name = "点赞数")
    private Integer likes;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态")
    private String status;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setCategory(String category) 
    {
        this.category = category;
    }

    public String getCategory() 
    {
        return category;
    }

    public void setDifficulty(String difficulty) 
    {
        this.difficulty = difficulty;
    }

    public String getDifficulty() 
    {
        return difficulty;
    }

    public void setQuestion(String question) 
    {
        this.question = question;
    }

    public String getQuestion() 
    {
        return question;
    }

    public void setAnswer(String answer) 
    {
        this.answer = answer;
    }

    public String getAnswer() 
    {
        return answer;
    }

    public void setExplanation(String explanation) 
    {
        this.explanation = explanation;
    }

    public String getExplanation() 
    {
        return explanation;
    }

    public void setViews(Integer views) 
    {
        this.views = views;
    }

    public Integer getViews() 
    {
        return views;
    }

    public void setLikes(Integer likes) 
    {
        this.likes = likes;
    }

    public Integer getLikes() 
    {
        return likes;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("category", getCategory())
            .append("difficulty", getDifficulty())
            .append("question", getQuestion())
            .append("answer", getAnswer())
            .append("explanation", getExplanation())
            .append("views", getViews())
            .append("likes", getLikes())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
