package com.ruoyi.web.service.audio;

import java.util.HashMap;
import java.util.Map;

/**
 * 音频AI模型检测结果
 * 
 * @author ruoyi
 */
public class AudioModelDetectionResult {
    
    /** 检测器名称 */
    private String detectorName;
    
    /** AI模型名称 */
    private String modelName;
    
    /** 检测分数（0-100） */
    private double score;
    
    /** 置信度（0-1） */
    private double confidence;
    
    /** 检测到的特征 */
    private Map<String, Object> features;
    
    /** 详细信息 */
    private String details;
    
    /** 是否检测成功 */
    private boolean success;
    
    /** 错误信息 */
    private String errorMessage;
    
    public AudioModelDetectionResult() {
        this.features = new HashMap<>();
        this.success = true;
    }
    
    public AudioModelDetectionResult(String detectorName, String modelName) {
        this();
        this.detectorName = detectorName;
        this.modelName = modelName;
    }
    
    /**
     * 创建成功的检测结果
     */
    public static AudioModelDetectionResult success(String detectorName, String modelName, 
                                                     double score, double confidence) {
        AudioModelDetectionResult result = new AudioModelDetectionResult(detectorName, modelName);
        result.setScore(score);
        result.setConfidence(confidence);
        result.setSuccess(true);
        return result;
    }
    
    /**
     * 创建失败的检测结果
     */
    public static AudioModelDetectionResult failure(String detectorName, String errorMessage) {
        AudioModelDetectionResult result = new AudioModelDetectionResult();
        result.setDetectorName(detectorName);
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setScore(0);
        result.setConfidence(0);
        return result;
    }
    
    /**
     * 添加特征
     */
    public void addFeature(String key, Object value) {
        this.features.put(key, value);
    }
    
    /**
     * 批量添加特征
     */
    public void addFeatures(Map<String, Object> features) {
        if (features != null) {
            this.features.putAll(features);
        }
    }
    
    // Getters and Setters
    
    public String getDetectorName() {
        return detectorName;
    }
    
    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = Math.max(0, Math.min(100, score));
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = Math.max(0, Math.min(1, confidence));
    }
    
    public Map<String, Object> getFeatures() {
        return features;
    }
    
    public void setFeatures(Map<String, Object> features) {
        this.features = features;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "AudioModelDetectionResult{" +
                "detectorName='" + detectorName + '\'' +
                ", modelName='" + modelName + '\'' +
                ", score=" + score +
                ", confidence=" + confidence +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
