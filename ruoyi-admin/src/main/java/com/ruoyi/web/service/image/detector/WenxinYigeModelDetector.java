package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * 文心一格AI图片模型检测器
 * 
 * 文心一格特征：
 * - 中国风审美倾向
 * - 传统文化元素融合
 * - 饱和度偏高但不过分(0.65-0.85)
 * - 色调偏暖，符合东方审美
 * - 细节丰富但不如商业级工具
 * - 构图符合东方美学
 * - 百度技术特征：色彩管理中规中矩
 * 
 * @author ruoyi
 */
@Component
public class WenxinYigeModelDetector implements IImageAiModelDetector {
    
    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;
    
    @Override
    public String getModelName() {
        return "文心一格";
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
     * 文心一格：饱和度偏高(0.65-0.85)、色彩分布均衡(0.6-0.8)、色调偏暖(0.5-0.8)
     */
    private double calculateColorScore(BufferedImage image) {
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        
        double score = 0.0;
        
        // 饱和度评分(8分)：文心一格饱和度偏高但不过分
        if (saturation >= 0.65 && saturation <= 0.85) {
            score += 8.0;
        } else if (saturation >= 0.55 && saturation < 0.65) {
            score += 5.0;
        } else if (saturation > 0.85 && saturation <= 0.95) {
            score += 6.0;
        } else {
            score += 2.0;
        }
        
        // 色彩分布均衡度评分(6分)：中规中矩
        if (colorUniformity >= 0.6 && colorUniformity <= 0.8) {
            score += 6.0;
        } else if (colorUniformity >= 0.5 && colorUniformity < 0.6) {
            score += 4.0;
        } else if (colorUniformity > 0.8) {
            score += 5.0;
        } else {
            score += 2.0;
        }
        
        // 色调偏向评分(6分)：偏暖色调符合东方审美
        if (warmTone >= 0.5 && warmTone <= 0.8) {
            score += 6.0;
        } else if (warmTone >= 0.4 && warmTone < 0.5) {
            score += 4.0;
        } else if (warmTone > 0.8) {
            score += 3.0;
        } else {
            score += 1.0;
        }
        
        return score;
    }
    
    /**
     * 计算纹理与细节得分(满分20分)
     * 文心一格：细节丰富(0.6-0.8)、边缘适中(0.55-0.75)、纹理自然(0.5-0.75)
     */
    private double calculateTextureScore(BufferedImage image) {
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        
        double score = 0.0;
        
        // 高频细节评分(8分)：细节丰富但不如顶级商业工具
        if (highFreqDetails >= 0.6 && highFreqDetails <= 0.8) {
            score += 8.0;
        } else if (highFreqDetails >= 0.5 && highFreqDetails < 0.6) {
            score += 5.0;
        } else if (highFreqDetails > 0.8) {
            score += 6.0;
        } else {
            score += 3.0;
        }
        
        // 边缘锐度评分(6分)：适中清晰度
        if (edgeSharpness >= 0.55 && edgeSharpness <= 0.75) {
            score += 6.0;
        } else if (edgeSharpness >= 0.45 && edgeSharpness < 0.55) {
            score += 4.0;
        } else if (edgeSharpness > 0.75) {
            score += 5.0;
        } else {
            score += 2.0;
        }
        
        // 纹理复杂度评分(6分)：自然但不过于复杂
        if (textureComplexity >= 0.5 && textureComplexity <= 0.75) {
            score += 6.0;
        } else if (textureComplexity >= 0.4 && textureComplexity < 0.5) {
            score += 4.0;
        } else if (textureComplexity > 0.75) {
            score += 3.0;
        } else {
            score += 2.0;
        }
        
        return score;
    }
    
    /**
     * 计算噪声与质量得分(满分20分)
     * 文心一格：噪点适中(0.25-0.45)、质量良好(0.6-0.8)
     */
    private double calculateNoiseScore(BufferedImage image) {
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        
        double score = 0.0;
        
        // 噪点密度评分(10分)：适中噪点是文心一格的典型特征
        if (noiseDensity >= 0.25 && noiseDensity <= 0.45) {
            score += 10.0;
        } else if (noiseDensity >= 0.15 && noiseDensity < 0.25) {
            score += 7.0;
        } else if (noiseDensity > 0.45 && noiseDensity <= 0.6) {
            score += 6.0;
        } else {
            score += 3.0;
        }
        
        // 整体质量评分(10分)：质量良好但不是顶级
        if (contrast >= 0.6 && contrast <= 0.8) {
            score += 10.0;
        } else if (contrast >= 0.5 && contrast < 0.6) {
            score += 7.0;
        } else if (contrast > 0.8) {
            score += 8.0;
        } else {
            score += 4.0;
        }
        
        return score;
    }
    
    /**
     * 计算风格与构图得分(满分20分)
     * 文心一格：构图符合东方美学(0.55-0.8)、中国风审美倾向
     */
    private double calculateStyleScore(BufferedImage image) {
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        
        double score = 0.0;
        
        // 东方美学构图评分(10分)：通过色彩和谐度判断
        double orientalComposition = (colorUniformity + warmTone) / 2.0;
        if (orientalComposition >= 0.55 && orientalComposition <= 0.8) {
            score += 10.0;
        } else if (orientalComposition >= 0.45 && orientalComposition < 0.55) {
            score += 7.0;
        } else if (orientalComposition > 0.8) {
            score += 8.0;
        } else {
            score += 4.0;
        }
        
        // 中国风审美评分(10分)：暖色调+适中饱和度的组合
        double chineseStyleScore = (warmTone * 0.6 + saturation * 0.4);
        if (chineseStyleScore >= 0.6 && chineseStyleScore <= 0.82) {
            score += 10.0;
        } else if (chineseStyleScore >= 0.5 && chineseStyleScore < 0.6) {
            score += 7.0;
        } else if (chineseStyleScore > 0.82) {
            score += 6.0;
        } else {
            score += 3.0;
        }
        
        return score;
    }
    
    /**
     * 计算AI指纹得分(满分20分)
     * 文心一格：百度技术特征 - 色彩管理中规中矩、质量-噪点平衡(差值0.2-0.5)
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        
        double score = 0.0;
        
        // 百度技术指纹(10分)：质量-噪点差值反映处理水平
        double qualityNoiseGap = contrast - noiseDensity;
        if (qualityNoiseGap >= 0.2 && qualityNoiseGap <= 0.5) {
            score += 10.0;
        } else if (qualityNoiseGap >= 0.1 && qualityNoiseGap < 0.2) {
            score += 7.0;
        } else if (qualityNoiseGap > 0.5 && qualityNoiseGap <= 0.7) {
            score += 8.0;
        } else {
            score += 4.0;
        }
        
        // 中国特色处理(10分)：暖色调+适中饱和度的技术实现
        double chineseTechFeature = warmTone * 0.5 + saturation * 0.5;
        if (chineseTechFeature >= 0.6 && chineseTechFeature <= 0.8) {
            score += 10.0;
        } else if (chineseTechFeature >= 0.5 && chineseTechFeature < 0.6) {
            score += 7.0;
        } else if (chineseTechFeature > 0.8) {
            score += 6.0;
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
        
        // 文心一格特有特征
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        
        details.put("东方美学构图", (colorUniformity + warmTone) / 2.0);
        details.put("中国风审美", warmTone * 0.6 + saturation * 0.4);
        details.put("质量噪点差值", contrast - noiseDensity);
        details.put("中国特色技术", warmTone * 0.5 + saturation * 0.5);
        
        return details;
    }
    
    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片高度符合文心一格的特征：中国风审美、暖色调倾向、适中饱和度和细节处理，非常可能由文心一格生成";
        } else if (score >= 60) {
            return "图片较符合文心一格的特征：具有东方美学元素和百度技术特点，可能由文心一格生成";
        } else if (score >= 40) {
            return "图片部分符合文心一格的特征：色彩或构图存在文心一格的某些典型特点";
        } else {
            return "图片不太符合文心一格的特征：缺乏中国风审美和百度技术指纹";
        }
    }
}
