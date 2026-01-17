package com.ruoyi.web.service.image;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * 图片AI模型检测器接口
 * 用于识别图片是由哪个AI生成工具生成的
 * 
 * @author ruoyi
 */
public interface IImageAiModelDetector {
    
    /**
     * 获取AI模型名称
     * 
     * @return 模型名称（如：DALL-E、Midjourney等）
     */
    String getModelName();
    
    /**
     * 检测图片是否由该AI模型生成
     * 
     * @param image 待检测的图片
     * @return 检测结果对象
     */
    ImageModelDetectionResult detectModel(BufferedImage image);
    
    /**
     * 获取特征详情
     * 
     * @param image 待检测的图片
     * @return 特征详情Map，键为特征名称，值为特征分数
     */
    Map<String, Double> getFeatureDetails(BufferedImage image);
    
    /**
     * 生成检测建议
     * 
     * @param score 检测得分
     * @return 检测建议文本
     */
    String generateSuggestions(double score);
    
    /**
     * 图片模型检测结果
     */
    class ImageModelDetectionResult {
        /** AI模型名称 */
        private String modelName;
        
        /** 总分（0-100） */
        private double totalScore;
        
        /** 色彩特征分数 */
        private double colorScore;
        
        /** 纹理与细节分数 */
        private double textureScore;
        
        /** 噪声与质量分数 */
        private double noiseScore;
        
        /** 风格与构图分数 */
        private double styleScore;
        
        /** AI特征指纹分数 */
        private double fingerprintScore;
        
        /** 特征详情 */
        private Map<String, Double> featureDetails;
        
        /** 检测建议 */
        private String suggestions;
        
        /**
         * 无参构造函数
         */
        public ImageModelDetectionResult() {
        }
        
        public ImageModelDetectionResult(String modelName) {
            this.modelName = modelName;
        }
        
        /**
         * 完整构造函数
         * 
         * @param modelName AI模型名称
         * @param totalScore 总分
         * @param colorScore 色彩特征分数
         * @param textureScore 纹理与细节分数
         * @param noiseScore 噪声与质量分数
         * @param styleScore 风格与构图分数
         * @param fingerprintScore AI特征指纹分数
         * @param featureDetails 特征详情
         * @param suggestions 检测建议
         */
        public ImageModelDetectionResult(
                String modelName,
                double totalScore,
                double colorScore,
                double textureScore,
                double noiseScore,
                double styleScore,
                double fingerprintScore,
                Map<String, Double> featureDetails,
                String suggestions) {
            this.modelName = modelName;
            this.totalScore = totalScore;
            this.colorScore = colorScore;
            this.textureScore = textureScore;
            this.noiseScore = noiseScore;
            this.styleScore = styleScore;
            this.fingerprintScore = fingerprintScore;
            this.featureDetails = featureDetails;
            this.suggestions = suggestions;
        }
        
        // Getters and Setters
        public String getModelName() {
            return modelName;
        }
        
        public void setModelName(String modelName) {
            this.modelName = modelName;
        }
        
        public double getTotalScore() {
            return totalScore;
        }
        
        public void setTotalScore(double totalScore) {
            this.totalScore = totalScore;
        }
        
        public double getColorScore() {
            return colorScore;
        }
        
        public void setColorScore(double colorScore) {
            this.colorScore = colorScore;
        }
        
        public double getTextureScore() {
            return textureScore;
        }
        
        public void setTextureScore(double textureScore) {
            this.textureScore = textureScore;
        }
        
        public double getNoiseScore() {
            return noiseScore;
        }
        
        public void setNoiseScore(double noiseScore) {
            this.noiseScore = noiseScore;
        }
        
        public double getStyleScore() {
            return styleScore;
        }
        
        public void setStyleScore(double styleScore) {
            this.styleScore = styleScore;
        }
        
        public double getFingerprintScore() {
            return fingerprintScore;
        }
        
        public void setFingerprintScore(double fingerprintScore) {
            this.fingerprintScore = fingerprintScore;
        }
        
        public Map<String, Double> getFeatureDetails() {
            return featureDetails;
        }
        
        public void setFeatureDetails(Map<String, Double> featureDetails) {
            this.featureDetails = featureDetails;
        }
        
        public String getSuggestions() {
            return suggestions;
        }
        
        public void setSuggestions(String suggestions) {
            this.suggestions = suggestions;
        }

    }
}
