package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 段落检测详情对象 paper_paragraph_detail
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
public class PaperParagraphDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 详情ID */
    private Long id;

    /** 检测记录ID */
    @Excel(name = "检测记录ID")
    private Long detectionId;

    /** 段落索引(从1开始) */
    @Excel(name = "段落索引")
    private Integer paragraphIndex;

    /** 段落内容 */
    private String paragraphContent;

    /** 段落字数 */
    @Excel(name = "段落字数")
    private Integer wordCount;

    /** AI风险评分(0-100) */
    @Excel(name = "AI风险评分")
    private BigDecimal aiRisk;

    /** 风险等级(low/medium/high) */
    @Excel(name = "风险等级")
    private String riskLevel;

    /** 风险类型JSON数组 */
    private String riskTypes;

    /** 风险原因JSON */
    private String riskReasons;

    /** 修改建议JSON */
    private String suggestions;

    /** API检测结果JSON */
    private String apiResults;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setDetectionId(Long detectionId) 
    {
        this.detectionId = detectionId;
    }

    public Long getDetectionId() 
    {
        return detectionId;
    }

    public void setParagraphIndex(Integer paragraphIndex) 
    {
        this.paragraphIndex = paragraphIndex;
    }

    public Integer getParagraphIndex() 
    {
        return paragraphIndex;
    }

    public void setParagraphContent(String paragraphContent) 
    {
        this.paragraphContent = paragraphContent;
    }

    public String getParagraphContent() 
    {
        return paragraphContent;
    }

    public void setWordCount(Integer wordCount) 
    {
        this.wordCount = wordCount;
    }

    public Integer getWordCount() 
    {
        return wordCount;
    }

    public void setAiRisk(BigDecimal aiRisk) 
    {
        this.aiRisk = aiRisk;
    }

    public BigDecimal getAiRisk() 
    {
        return aiRisk;
    }

    public void setRiskLevel(String riskLevel) 
    {
        this.riskLevel = riskLevel;
    }

    public String getRiskLevel() 
    {
        return riskLevel;
    }

    public void setRiskTypes(String riskTypes) 
    {
        this.riskTypes = riskTypes;
    }

    public String getRiskTypes() 
    {
        return riskTypes;
    }

    public void setRiskReasons(String riskReasons) 
    {
        this.riskReasons = riskReasons;
    }

    public String getRiskReasons() 
    {
        return riskReasons;
    }

    public void setSuggestions(String suggestions) 
    {
        this.suggestions = suggestions;
    }

    public String getSuggestions() 
    {
        return suggestions;
    }

    public void setApiResults(String apiResults) 
    {
        this.apiResults = apiResults;
    }

    public String getApiResults() 
    {
        return apiResults;
    }

    @Override
    public String toString() {
        return "PaperParagraphDetail{" +
                "id=" + id +
                ", detectionId=" + detectionId +
                ", paragraphIndex=" + paragraphIndex +
                ", wordCount=" + wordCount +
                ", aiRisk=" + aiRisk +
                ", riskLevel='" + riskLevel + '\'' +
                ", riskTypes='" + riskTypes + '\'' +
                '}';
    }
}
