package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 论文检测记录对象 paper_detection_record
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public class PaperDetectionRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 检测ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 论文标题 */
    @Excel(name = "论文标题")
    private String title;

    /** 论文内容 */
    private String content;

    /** 上传文件URL */
    @Excel(name = "上传文件URL")
    private String fileUrl;

    /** 字数 */
    @Excel(name = "字数")
    private Integer wordCount;

    /** AI风险等级(low/medium/high) */
    @Excel(name = "AI风险等级")
    private String aiRiskLevel;

    /** AI概率评分(0-100) */
    @Excel(name = "AI概率评分")
    private BigDecimal aiScore;

    /** 重复度等级(low/medium/high) */
    @Excel(name = "重复度等级")
    private String duplicateLevel;

    /** 重复度评分(0-100) */
    @Excel(name = "重复度评分")
    private BigDecimal duplicateScore;

    /** 风格异常评分(0-100) */
    @Excel(name = "风格异常评分")
    private BigDecimal styleScore;

    /** 总段落数 */
    @Excel(name = "总段落数")
    private Integer totalParagraphs;

    /** 高风险段落数 */
    @Excel(name = "高风险段落数")
    private Integer highRiskParagraphs;

    /** 检测状态(processing/completed/failed) */
    @Excel(name = "检测状态")
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 检测耗时(秒) */
    @Excel(name = "检测耗时")
    private Integer detectDuration;

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

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setFileUrl(String fileUrl) 
    {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() 
    {
        return fileUrl;
    }

    public void setWordCount(Integer wordCount) 
    {
        this.wordCount = wordCount;
    }

    public Integer getWordCount() 
    {
        return wordCount;
    }

    public void setAiRiskLevel(String aiRiskLevel) 
    {
        this.aiRiskLevel = aiRiskLevel;
    }

    public String getAiRiskLevel() 
    {
        return aiRiskLevel;
    }

    public void setAiScore(BigDecimal aiScore) 
    {
        this.aiScore = aiScore;
    }

    public BigDecimal getAiScore() 
    {
        return aiScore;
    }

    public void setDuplicateLevel(String duplicateLevel) 
    {
        this.duplicateLevel = duplicateLevel;
    }

    public String getDuplicateLevel() 
    {
        return duplicateLevel;
    }

    public void setDuplicateScore(BigDecimal duplicateScore) 
    {
        this.duplicateScore = duplicateScore;
    }

    public BigDecimal getDuplicateScore() 
    {
        return duplicateScore;
    }

    public void setStyleScore(BigDecimal styleScore) 
    {
        this.styleScore = styleScore;
    }

    public BigDecimal getStyleScore() 
    {
        return styleScore;
    }

    public void setTotalParagraphs(Integer totalParagraphs) 
    {
        this.totalParagraphs = totalParagraphs;
    }

    public Integer getTotalParagraphs() 
    {
        return totalParagraphs;
    }

    public void setHighRiskParagraphs(Integer highRiskParagraphs) 
    {
        this.highRiskParagraphs = highRiskParagraphs;
    }

    public Integer getHighRiskParagraphs() 
    {
        return highRiskParagraphs;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setErrorMsg(String errorMsg) 
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() 
    {
        return errorMsg;
    }

    public void setDetectDuration(Integer detectDuration) 
    {
        this.detectDuration = detectDuration;
    }

    public Integer getDetectDuration() 
    {
        return detectDuration;
    }

    @Override
    public String toString() {
        return "PaperDetectionRecord{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", wordCount=" + wordCount +
                ", aiRiskLevel='" + aiRiskLevel + '\'' +
                ", aiScore=" + aiScore +
                ", duplicateLevel='" + duplicateLevel + '\'' +
                ", duplicateScore=" + duplicateScore +
                ", styleScore=" + styleScore +
                ", totalParagraphs=" + totalParagraphs +
                ", highRiskParagraphs=" + highRiskParagraphs +
                ", status='" + status + '\'' +
                ", detectDuration=" + detectDuration +
                '}';
    }
}
