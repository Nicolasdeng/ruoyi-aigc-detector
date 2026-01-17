package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.IImageAiModelDetector.ImageModelDetectionResult;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 通义万相AI图片模型检测器
 * 
 * 通义万相特征：
 * - 电商场景优化，实用主义导向
 * - 细节清晰，产品展示优先
 * - 饱和度适中偏高(0.6-0.8)
 * - 色彩分布精准(0.65-0.85)
 * - 噪点控制良好(0.2-0.35)
 * - 阿里技术特征：商业化导向、实用性强
 * - 质量稳定，适合批量生成
 * 
 * @author ruoyi
 */
@Component
public class TongyiWanxiangModelDetector implements IImageAiModelDetector {
    
    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;
    
    @Override
    public String getModelName() {
        return "通义万相";
    }
    
    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        
        // 降级处理：如果特征分析器不可用，返回基础分数
        if (featureAnalyzer == null) {
            result.setTotalScore(50.0);
            result.setColorScore(10.0);
            result.setTextureScore(10.0);
            result.setNoiseScore(10.0);
            result.setStyleScore(10.0);
            result.setFingerprintScore(10.0);
            result.setSuggestions("特征分析器不可用，使用基础评分");
            return result;
        }
        
        // 计算各维度得分
        double colorScore = calculateColorScore(image);
        double textureScore = calculateTextureScore(image);
        double noiseScore = calculateNoiseScore(image);
        double styleScore = calculateStyleScore(image);
        double fingerprintScore = calculateFingerprintScore(image);
        
        // 设置结果
        result.setColorScore(colorScore);
        result.setTextureScore(textureScore);
        result.setNoiseScore(noiseScore);
        result.setStyleScore(styleScore);
        result.setFingerprintScore(fingerprintScore);
        result.setTotalScore(colorScore + textureScore + noiseScore + styleScore + fingerprintScore);
        result.setFeatureDetails(getFeatureDetails(image));
        result.setSuggestions(generateSuggestions(result.getTotalScore()));
        
        return result;
    }
    
    /**
     * 计算色彩特征得分(满分20分)
     * 通义万相：饱和度适中偏高(0.6-0.8)、色彩分布精准(0.65-0.85)、色调实用(0.3-0.7)
     */
    private double calculateColorScore(BufferedImage image) {
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        
        double score = 0.0;
        
        // 饱和度评分(8分)：适中偏高，适合电商展示
        if (saturation >= 0.6 && saturation <= 0.8) {
            score += 8.0;
        } else if (saturation >= 0.5 && saturation < 0.6) {
            score += 5.0;
        } else if (saturation > 0.8 && saturation <= 0.9) {
            score += 6.0;
        } else {
            score += 2.0;
        }
        
        // 色彩分布精准度评分(8分)：商业级色彩管理
        if (colorUniformity >= 0.65 && colorUniformity <= 0.85) {
            score += 8.0;
        } else if (colorUniformity >= 0.55 && colorUniformity < 0.65) {
            score += 5.0;
        } else if (colorUniformity > 0.85) {
            score += 7.0;
        } else {
            score += 2.0;
        }
        
        // 色调评分(4分)：实用性导向，冷暖色调均可
        if (warmTone >= 0.3 && warmTone <= 0.7) {
            score += 4.0;
        } else if (warmTone >= 0.2 && warmTone < 0.3) {
            score += 3.0;
        } else if (warmTone > 0.7 && warmTone <= 0.8) {
            score += 3.0;
        } else {
            score += 1.0;
        }
        
        return score;
    }
    
    /**
     * 计算纹理与细节得分(满分20分)
     * 通义万相：细节清晰(0.65-0.85)、边缘锐利(0.65-0.85)、纹理适中(0.55-0.75)
     */
    private double calculateTextureScore(BufferedImage image) {
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        
        double score = 0.0;
        
        // 高频细节评分(8分)：细节清晰，产品展示需要
        if (highFreqDetails >= 0.65 && highFreqDetails <= 0.85) {
            score += 8.0;
        } else if (highFreqDetails >= 0.55 && highFreqDetails < 0.65) {
            score += 5.0;
        } else if (highFreqDetails > 0.85) {
            score += 7.0;
        } else {
            score += 3.0;
        }
        
        // 边缘锐度评分(8分)：边缘清晰，适合产品图
        if (edgeSharpness >= 0.65 && edgeSharpness <= 0.85) {
            score += 8.0;
        } else if (edgeSharpness >= 0.55 && edgeSharpness < 0.65) {
            score += 5.0;
        } else if (edgeSharpness > 0.85) {
            score += 7.0;
        } else {
            score += 2.0;
        }
        
        // 纹理复杂度评分(4分)：适中即可，不过分追求
        if (textureComplexity >= 0.55 && textureComplexity <= 0.75) {
            score += 4.0;
        } else if (textureComplexity >= 0.45 && textureComplexity < 0.55) {
            score += 3.0;
        } else if (textureComplexity > 0.75) {
            score += 3.0;
        } else {
            score += 1.0;
        }
        
        return score;
    }
    
    /**
     * 计算噪声与质量得分(满分20分)
     * 通义万相：噪点控制良好(0.2-0.35)、质量稳定(0.65-0.85)
     */
    private double calculateNoiseScore(BufferedImage image) {
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        
        double score = 0.0;
        
        // 噪点密度评分(10分)：噪点控制良好
        if (noiseDensity >= 0.2 && noiseDensity <= 0.35) {
            score += 10.0;
        } else if (noiseDensity >= 0.1 && noiseDensity < 0.2) {
            score += 8.0;
        } else if (noiseDensity > 0.35 && noiseDensity <= 0.5) {
            score += 6.0;
        } else {
            score += 3.0;
        }
        
        // 整体质量评分(10分)：质量稳定
        if (contrast >= 0.65 && contrast <= 0.85) {
            score += 10.0;
        } else if (contrast >= 0.55 && contrast < 0.65) {
            score += 7.0;
        } else if (contrast > 0.85) {
            score += 9.0;
        } else {
            score += 4.0;
        }
        
        return score;
    }
    
    /**
     * 计算风格与构图得分(满分20分)
     * 通义万相：实用主义构图(0.6-0.8)、商业化导向
     */
    private double calculateStyleScore(BufferedImage image) {
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        
        double score = 0.0;
        
        // 实用主义构图评分(10分)：清晰度+色彩均衡
        double practicalComposition = (edgeSharpness + colorUniformity) / 2.0;
        if (practicalComposition >= 0.6 && practicalComposition <= 0.8) {
            score += 10.0;
        } else if (practicalComposition >= 0.5 && practicalComposition < 0.6) {
            score += 7.0;
        } else if (practicalComposition > 0.8) {
            score += 9.0;
        } else {
            score += 4.0;
        }
        
        // 商业化导向评分(10分)：质量+均衡度
        double commercialScore = (contrast * 0.5 + colorUniformity * 0.5);
        if (commercialScore >= 0.65 && commercialScore <= 0.85) {
            score += 10.0;
        } else if (commercialScore >= 0.55 && commercialScore < 0.65) {
            score += 7.0;
        } else if (commercialScore > 0.85) {
            score += 8.0;
        } else {
            score += 3.0;
        }
        
        return score;
    }
    
    /**
     * 计算AI指纹得分(满分20分)
     * 通义万相：阿里技术特征 - 电商优化、批量生成稳定性(0.3-0.6)
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double contrast = featureAnalyzer.analyzeContrast(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        
        double score = 0.0;
        
        // 阿里技术指纹(10分)：质量稳定性-噪点控制
        double aliTechFeature = contrast - noiseDensity;
        if (aliTechFeature >= 0.3 && aliTechFeature <= 0.6) {
            score += 10.0;
        } else if (aliTechFeature >= 0.2 && aliTechFeature < 0.3) {
            score += 7.0;
        } else if (aliTechFeature > 0.6 && aliTechFeature <= 0.8) {
            score += 8.0;
        } else {
            score += 4.0;
        }
        
        // 电商优化特征(10分)：清晰度+色彩管理
        double ecommerceFeature = edgeSharpness * 0.6 + colorUniformity * 0.4;
        if (ecommerceFeature >= 0.65 && ecommerceFeature <= 0.85) {
            score += 10.0;
        } else if (ecommerceFeature >= 0.55 && ecommerceFeature < 0.65) {
            score += 7.0;
        } else if (ecommerceFeature > 0.85) {
            score += 8.0;
        } else {
            score += 3.0;
        }
        
        return score;
    }
    
    @Override
    public Map<String, Double> getFeatureDetails(BufferedImage image) {
        Map<String, Double> details = new HashMap<>();
        
        if (featureAnalyzer == null) {
            details.put("error", 0.0);
            details.put("message", 0.0);
            return details;
        }
        
        // 基础特征
        details.put("饱和度", featureAnalyzer.analyzeSaturation(image));
        details.put("色彩均匀度", featureAnalyzer.analyzeColorUniformity(image));
        details.put("暖色调倾向", featureAnalyzer.analyzeWarmTone(image));
        details.put("高频细节", featureAnalyzer.analyzeHighFrequencyDetails(image));
        details.put("边缘锐度", featureAnalyzer.analyzeEdgeSharpness(image));
        details.put("纹理复杂度", featureAnalyzer.analyzeTextureComplexity(image));
        details.put("噪点密度", featureAnalyzer.analyzeNoiseDensity(image));
        details.put("对比度", featureAnalyzer.analyzeContrast(image));
        
        // 通义万相特有特征
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        
        details.put("实用主义构图", (edgeSharpness + colorUniformity) / 2.0);
        details.put("商业化导向", contrast * 0.5 + colorUniformity * 0.5);
        details.put("质量稳定性", contrast - noiseDensity);
        details.put("电商优化特征", edgeSharpness * 0.6 + colorUniformity * 0.4);
        
        return details;
    }
    
    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片高度符合通义万相的特征：实用主义导向、细节清晰、色彩管理精准，非常适合电商场景，非常可能由通义万相生成";
        } else if (score >= 60) {
            return "图片较符合通义万相的特征：具有商业化导向和阿里技术特点，可能由通义万相生成";
        } else if (score >= 40) {
            return "图片部分符合通义万相的特征：质量稳定或细节清晰度存在通义万相的某些典型特点";
        } else {
            return "图片不太符合通义万相的特征：缺乏电商优化和阿里技术指纹";
        }
    }
}
