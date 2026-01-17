package com.ruoyi.web.service.paper;

import java.util.List;
import java.util.Map;

/**
 * AI模型检测结果
 * 封装单个AI模型检测器的检测结果
 * 
 * @author ruoyi
 * @date 2026-01-08
 */
public class ModelDetectionResult
{
    /** AI模型名称 */
    private String modelName;
    
    /** 匹配得分 (0-100) */
    private double score;
    
    /** 特征详情 */
    private Map<String, String> featureDetails;
    
    /** 优化建议 */
    private List<String> suggestions;
    
    public ModelDetectionResult()
    {
    }
    
    public ModelDetectionResult(String modelName, double score)
    {
        this.modelName = modelName;
        this.score = score;
    }
    
    public ModelDetectionResult(String modelName, double score, Map<String, String> featureDetails, List<String> suggestions)
    {
        this.modelName = modelName;
        this.score = score;
        this.featureDetails = featureDetails;
        this.suggestions = suggestions;
    }
    
    public String getModelName()
    {
        return modelName;
    }
    
    public void setModelName(String modelName)
    {
        this.modelName = modelName;
    }
    
    public double getScore()
    {
        return score;
    }
    
    public void setScore(double score)
    {
        this.score = score;
    }
    
    public Map<String, String> getFeatureDetails()
    {
        return featureDetails;
    }
    
    public void setFeatureDetails(Map<String, String> featureDetails)
    {
        this.featureDetails = featureDetails;
    }
    
    public List<String> getSuggestions()
    {
        return suggestions;
    }
    
    public void setSuggestions(List<String> suggestions)
    {
        this.suggestions = suggestions;
    }
    
    @Override
    public String toString()
    {
        return "ModelDetectionResult{" +
                "modelName='" + modelName + '\'' +
                ", score=" + score +
                ", featureDetails=" + featureDetails +
                ", suggestions=" + suggestions +
                '}';
    }
}
