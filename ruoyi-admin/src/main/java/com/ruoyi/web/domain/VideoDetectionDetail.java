package com.ruoyi.web.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * 视频检测详细信息
 * 
 * @author ruoyi
 */
public class VideoDetectionDetail {
    
    /** 视频基本信息 */
    private VideoInfo videoInfo;
    
    /** AI生成概率 */
    private BigDecimal aiProbability;
    
    /** 判定结果 */
    private String detectionResult;
    
    /** 置信度等级 */
    private String confidenceLevel;
    
    /** 详细分析报告 */
    private AnalysisReport analysisReport;
    
    /** API检测结果列表 */
    private List<ApiResult> apiResults;
    
    /**
     * 视频基本信息
     */
    public static class VideoInfo {
        private String fileName;
        private Long fileSize;
        private Integer duration;
        private String format;
        private Integer width;
        private Integer height;
        private String codec;
        private Integer frameRate;
        private Integer totalFrames;
        
        // Getters and Setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        
        public Integer getWidth() { return width; }
        public void setWidth(Integer width) { this.width = width; }
        
        public Integer getHeight() { return height; }
        public void setHeight(Integer height) { this.height = height; }
        
        public String getCodec() { return codec; }
        public void setCodec(String codec) { this.codec = codec; }
        
        public Integer getFrameRate() { return frameRate; }
        public void setFrameRate(Integer frameRate) { this.frameRate = frameRate; }
        
        public Integer getTotalFrames() { return totalFrames; }
        public void setTotalFrames(Integer totalFrames) { this.totalFrames = totalFrames; }
    }
    
    /**
     * 分析报告
     */
    public static class AnalysisReport {
        private FrameAnalysis frameAnalysis;
        private MotionAnalysis motionAnalysis;
        private QualityAnalysis qualityAnalysis;
        private MetadataAnalysis metadataAnalysis;
        
        // Getters and Setters
        public FrameAnalysis getFrameAnalysis() { return frameAnalysis; }
        public void setFrameAnalysis(FrameAnalysis frameAnalysis) { this.frameAnalysis = frameAnalysis; }
        
        public MotionAnalysis getMotionAnalysis() { return motionAnalysis; }
        public void setMotionAnalysis(MotionAnalysis motionAnalysis) { this.motionAnalysis = motionAnalysis; }
        
        public QualityAnalysis getQualityAnalysis() { return qualityAnalysis; }
        public void setQualityAnalysis(QualityAnalysis qualityAnalysis) { this.qualityAnalysis = qualityAnalysis; }
        
        public MetadataAnalysis getMetadataAnalysis() { return metadataAnalysis; }
        public void setMetadataAnalysis(MetadataAnalysis metadataAnalysis) { this.metadataAnalysis = metadataAnalysis; }
    }
    
    /**
     * 视频帧分析
     */
    public static class FrameAnalysis {
        private Integer sampledFrames;
        private BigDecimal avgAiScore;
        private BigDecimal maxAiScore;
        private BigDecimal minAiScore;
        private List<FrameDetail> frameDetails;
        
        // Getters and Setters
        public Integer getSampledFrames() { return sampledFrames; }
        public void setSampledFrames(Integer sampledFrames) { this.sampledFrames = sampledFrames; }
        
        public BigDecimal getAvgAiScore() { return avgAiScore; }
        public void setAvgAiScore(BigDecimal avgAiScore) { this.avgAiScore = avgAiScore; }
        
        public BigDecimal getMaxAiScore() { return maxAiScore; }
        public void setMaxAiScore(BigDecimal maxAiScore) { this.maxAiScore = maxAiScore; }
        
        public BigDecimal getMinAiScore() { return minAiScore; }
        public void setMinAiScore(BigDecimal minAiScore) { this.minAiScore = minAiScore; }
        
        public List<FrameDetail> getFrameDetails() { return frameDetails; }
        public void setFrameDetails(List<FrameDetail> frameDetails) { this.frameDetails = frameDetails; }
    }
    
    /**
     * 单帧详情
     */
    public static class FrameDetail {
        private Integer frameNumber;
        private BigDecimal aiScore;
        private String timestamp;
        
        // Getters and Setters
        public Integer getFrameNumber() { return frameNumber; }
        public void setFrameNumber(Integer frameNumber) { this.frameNumber = frameNumber; }
        
        public BigDecimal getAiScore() { return aiScore; }
        public void setAiScore(BigDecimal aiScore) { this.aiScore = aiScore; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
    
    /**
     * 运动特征分析
     */
    public static class MotionAnalysis {
        private BigDecimal motionScore;
        private String motionPattern;
        private List<String> anomalies;
        
        // Getters and Setters
        public BigDecimal getMotionScore() { return motionScore; }
        public void setMotionScore(BigDecimal motionScore) { this.motionScore = motionScore; }
        
        public String getMotionPattern() { return motionPattern; }
        public void setMotionPattern(String motionPattern) { this.motionPattern = motionPattern; }
        
        public List<String> getAnomalies() { return anomalies; }
        public void setAnomalies(List<String> anomalies) { this.anomalies = anomalies; }
    }
    
    /**
     * 质量分析
     */
    public static class QualityAnalysis {
        private BigDecimal qualityScore;
        private String resolution;
        private Boolean hasArtifacts;
        private List<String> qualityIssues;
        
        // Getters and Setters
        public BigDecimal getQualityScore() { return qualityScore; }
        public void setQualityScore(BigDecimal qualityScore) { this.qualityScore = qualityScore; }
        
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
        
        public Boolean getHasArtifacts() { return hasArtifacts; }
        public void setHasArtifacts(Boolean hasArtifacts) { this.hasArtifacts = hasArtifacts; }
        
        public List<String> getQualityIssues() { return qualityIssues; }
        public void setQualityIssues(List<String> qualityIssues) { this.qualityIssues = qualityIssues; }
    }
    
    /**
     * 元数据分析
     */
    public static class MetadataAnalysis {
        private Boolean hasCreationInfo;
        private Boolean hasSoftwareTag;
        private List<String> suspiciousMetadata;
        
        // Getters and Setters
        public Boolean getHasCreationInfo() { return hasCreationInfo; }
        public void setHasCreationInfo(Boolean hasCreationInfo) { this.hasCreationInfo = hasCreationInfo; }
        
        public Boolean getHasSoftwareTag() { return hasSoftwareTag; }
        public void setHasSoftwareTag(Boolean hasSoftwareTag) { this.hasSoftwareTag = hasSoftwareTag; }
        
        public List<String> getSuspiciousMetadata() { return suspiciousMetadata; }
        public void setSuspiciousMetadata(List<String> suspiciousMetadata) { this.suspiciousMetadata = suspiciousMetadata; }
    }
    
    /**
     * API检测结果
     */
    public static class ApiResult {
        private String apiName;
        private String provider;
        private BigDecimal score;
        private Boolean isAI;
        private BigDecimal weight;
        private String model;
        private Object details;
        
        // Getters and Setters
        public String getApiName() { return apiName; }
        public void setApiName(String apiName) { this.apiName = apiName; }
        
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        
        public BigDecimal getScore() { return score; }
        public void setScore(BigDecimal score) { this.score = score; }
        
        public Boolean getIsAI() { return isAI; }
        public void setIsAI(Boolean isAI) { this.isAI = isAI; }
        
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }
    }
    
    // Main class Getters and Setters
    public VideoInfo getVideoInfo() { return videoInfo; }
    public void setVideoInfo(VideoInfo videoInfo) { this.videoInfo = videoInfo; }
    
    public BigDecimal getAiProbability() { return aiProbability; }
    public void setAiProbability(BigDecimal aiProbability) { this.aiProbability = aiProbability; }
    
    public String getDetectionResult() { return detectionResult; }
    public void setDetectionResult(String detectionResult) { this.detectionResult = detectionResult; }
    
    public String getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }
    
    public AnalysisReport getAnalysisReport() { return analysisReport; }
    public void setAnalysisReport(AnalysisReport analysisReport) { this.analysisReport = analysisReport; }
    
    public List<ApiResult> getApiResults() { return apiResults; }
    public void setApiResults(List<ApiResult> apiResults) { this.apiResults = apiResults; }
}
