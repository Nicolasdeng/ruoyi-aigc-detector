package com.ruoyi.web.service.video;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频AI模型检测结果
 * 
 * @author ruoyi
 */
public class VideoModelDetectionResult {
    
    /** 模型名称 */
    private String modelName;
    
    /** 置信度 (0-100) */
    private double confidence;
    
    /** 各维度得分 */
    private Map<String, Double> scores;
    
    /** 检测到的特征列表 */
    private List<String> features;
    
    /** 优化建议列表 */
    private List<String> suggestions;
    
    public VideoModelDetectionResult() {
        this.scores = new HashMap<>();
        this.features = new ArrayList<>();
        this.suggestions = new ArrayList<>();
    }
    
    public VideoModelDetectionResult(String modelName, double confidence) {
        this();
        this.modelName = modelName;
        this.confidence = confidence;
    }

    // Getters and Setters
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public Map<String, Double> getScores() {
        return scores;
    }
    
    public void setScores(Map<String, Double> scores) {
        this.scores = scores;
    }
    
    public void addScore(String dimension, double score) {
        this.scores.put(dimension, score);
    }
    
    public List<String> getFeatures() {
        return features;
    }
    
    public void setFeatures(List<String> features) {
        this.features = features;
    }
    
    public void addFeature(String feature) {
        this.features.add(feature);
    }
    
    public List<String> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
    
    public void addSuggestion(String suggestion) {
        this.suggestions.add(suggestion);
    }
    
    @Override
    public String toString() {
        return "VideoModelDetectionResult{" +
                "modelName='" + modelName + '\'' +
                ", confidence=" + confidence +
                ", scores=" + scores +
                ", features=" + features +
                ", suggestions=" + suggestions +
                '}';
    }
}
