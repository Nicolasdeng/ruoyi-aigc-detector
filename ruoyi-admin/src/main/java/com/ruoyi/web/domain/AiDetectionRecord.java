package com.ruoyi.web.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * AI检测记录对象 ai_detection_record
 * 
 * @author ruoyi
 */
public class AiDetectionRecord extends BaseEntity {
    
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID")
    private Long userId;

    /** 文件URL */
    @Excel(name = "文件URL")
    private String fileUrl;

    /** 文件类型：image/video/audio/text */
    @Excel(name = "文件类型")
    private String fileType;

    /** 文件大小(字节) */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** 检测结果：AI_GENERATED/HUMAN_CREATED/UNCERTAIN */
    @Excel(name = "检测结果")
    private String detectionResult;

    /** 置信度分数(0-100) */
    @Excel(name = "置信度分数")
    private BigDecimal confidenceScore;

    /** 检测详情(JSON格式) */
    private String detectionDetails;

    /** API检测结果汇总(JSON格式) */
    private String apiResults;

    /** 状态：PENDING/PROCESSING/COMPLETED/FAILED */
    @Excel(name = "状态")
    private String status;

    /** 错误信息 */
    private String errorMessage;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setDetectionResult(String detectionResult) {
        this.detectionResult = detectionResult;
    }

    public String getDetectionResult() {
        return detectionResult;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setDetectionDetails(String detectionDetails) {
        this.detectionDetails = detectionDetails;
    }

    public String getDetectionDetails() {
        return detectionDetails;
    }

    public void setApiResults(String apiResults) {
        this.apiResults = apiResults;
    }

    public String getApiResults() {
        return apiResults;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("userId", getUserId())
            .append("fileUrl", getFileUrl())
            .append("fileType", getFileType())
            .append("fileSize", getFileSize())
            .append("detectionResult", getDetectionResult())
            .append("confidenceScore", getConfidenceScore())
            .append("detectionDetails", getDetectionDetails())
            .append("apiResults", getApiResults())
            .append("status", getStatus())
            .append("errorMessage", getErrorMessage())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
