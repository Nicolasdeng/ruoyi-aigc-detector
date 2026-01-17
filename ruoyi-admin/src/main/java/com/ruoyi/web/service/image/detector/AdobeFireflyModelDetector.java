package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Adobe Firefly模型检测器
 * 特征：商业级质量、版权安全、色彩专业、细节精致、渲染高质量
 * 
 * @author ruoyi
 */
@Component
public class AdobeFireflyModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Adobe Firefly";
    }

    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        ImageModelDetectionResult result = new ImageModelDetectionResult();
        result.setModelName(getModelName());

        if (featureAnalyzer == null) {
            // 降级处理：给予基础分数
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

        // 计算总分
        double totalScore = colorScore + textureScore + noiseScore + styleScore + fingerprintScore;

        // 设置结果
        result.setTotalScore(totalScore);
        result.setColorScore(colorScore);
        result.setTextureScore(textureScore);
        result.setNoiseScore(noiseScore);
        result.setStyleScore(styleScore);
        result.setFingerprintScore(fingerprintScore);
        result.setFeatureDetails(getFeatureDetails(image));
        result.setSuggestions(generateSuggestions(totalScore));

        return result;
    }

    /**
     * 计算色彩特征得分（满分20分）
     * Firefly特征：专业级色彩管理、饱和度适中偏高、色彩分布精准
     */
    private double calculateColorScore(BufferedImage image) {
        double score = 0.0;

        // 1. 专业饱和度 (8分)
        double saturation = featureAnalyzer.analyzeSaturation(image);
        if (saturation >= 0.6 && saturation <= 0.8) {
            score += 8.0; // 专业级饱和度
        } else if (saturation >= 0.5 && saturation < 0.6 || saturation > 0.8 && saturation <= 0.85) {
            score += 5.0;
        } else {
            score += 2.0;
        }

        // 2. 色彩分布精准 (7分)
        double uniformity = featureAnalyzer.analyzeColorUniformity(image);
        if (uniformity >= 0.7 && uniformity <= 0.9) {
            score += 7.0; // 精准的色彩分布
        } else if (uniformity >= 0.6 && uniformity < 0.7) {
            score += 4.0;
        } else {
            score += 2.0;
        }

        // 3. 自然色调 (5分)
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        if (warmTone >= 0.4 && warmTone <= 0.7) {
            score += 5.0; // 自然平衡的色调
        } else if (warmTone >= 0.3 && warmTone < 0.4 || warmTone > 0.7 && warmTone <= 0.8) {
            score += 3.0;
        } else {
            score += 1.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算纹理与细节得分（满分20分）
     * Firefly特征：细节精致、边缘清晰、纹理自然丰富
     */
    private double calculateTextureScore(BufferedImage image) {
        double score = 0.0;

        // 1. 精致细节 (8分)
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        if (highFreq >= 0.7 && highFreq <= 0.9) {
            score += 8.0; // 细节精致丰富
        } else if (highFreq >= 0.6 && highFreq < 0.7) {
            score += 5.0;
        } else {
            score += 2.0;
        }

        // 2. 边缘清晰 (7分)
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        if (edgeSharpness >= 0.7 && edgeSharpness <= 0.9) {
            score += 7.0; // 边缘清晰锐利
        } else if (edgeSharpness >= 0.6 && edgeSharpness < 0.7) {
            score += 4.0;
        } else {
            score += 2.0;
        }

        // 3. 纹理自然 (5分)
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        if (textureComplexity >= 0.6 && textureComplexity <= 0.85) {
            score += 5.0; // 纹理自然丰富
        } else if (textureComplexity >= 0.5 && textureComplexity < 0.6) {
            score += 3.0;
        } else {
            score += 1.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算噪声与质量得分（满分20分）
     * Firefly特征：商业级质量、极低噪点、渲染精细
     */
    private double calculateNoiseScore(BufferedImage image) {
        double score = 0.0;

        // 1. 极低噪点 (10分)
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        if (noiseDensity <= 0.2) {
            score += 10.0; // 商业级低噪点
        } else if (noiseDensity <= 0.3) {
            score += 7.0;
        } else if (noiseDensity <= 0.4) {
            score += 4.0;
        } else {
            score += 1.0;
        }

        // 2. 高质量渲染 (10分)
        // 综合评估：高频细节和边缘清晰度
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double quality = (highFreq + edgeSharpness) / 2.0;
        
        if (quality >= 0.7) {
            score += 10.0; // 商业级质量
        } else if (quality >= 0.6) {
            score += 6.0;
        } else {
            score += 2.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算风格与构图得分（满分20分）
     * Firefly特征：专业构图、商业美学、平衡感强
     */
    private double calculateStyleScore(BufferedImage image) {
        double score = 0.0;

        // 1. 专业构图 (10分)
        // 通过对比度和色彩均匀度判断构图专业性
        double contrast = featureAnalyzer.analyzeContrast(image);
        double uniformity = featureAnalyzer.analyzeColorUniformity(image);
        double composition = (contrast + uniformity) / 2.0;
        
        if (composition >= 0.65 && composition <= 0.85) {
            score += 10.0; // 专业构图
        } else if (composition >= 0.55 && composition < 0.65) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        // 2. 商业美学 (10分)
        // 饱和度和色调的平衡体现商业美学
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        double aesthetics = (saturation + warmTone) / 2.0;
        
        if (aesthetics >= 0.5 && aesthetics <= 0.75) {
            score += 10.0; // 商业美学平衡
        } else if (aesthetics >= 0.4 && aesthetics < 0.5) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算AI指纹得分（满分20分）
     * Firefly特征：Adobe技术指纹、版权安全渲染、专业级后处理
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double score = 0.0;

        // 1. Adobe技术指纹 (10分)
        // 高质量+低噪点是Adobe的典型特征
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double adobeFingerprint = highFreq - noiseDensity;
        
        if (adobeFingerprint >= 0.5 && adobeFingerprint <= 0.8) {
            score += 10.0; // 典型Adobe指纹
        } else if (adobeFingerprint >= 0.4 && adobeFingerprint < 0.5) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        // 2. 专业级处理 (10分)
        // 边缘清晰度和纹理复杂度的平衡
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        double professionalProcessing = (edgeSharpness + textureComplexity) / 2.0;
        
        if (professionalProcessing >= 0.65 && professionalProcessing <= 0.85) {
            score += 10.0; // 专业级处理
        } else if (professionalProcessing >= 0.55 && professionalProcessing < 0.65) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    @Override
    public Map<String, Double> getFeatureDetails(BufferedImage image) {
        Map<String, Double> details = new HashMap<>();
        
        if (featureAnalyzer == null) {
            details.put("error", 1.0);
            return details;
        }

        details.put("饱和度", featureAnalyzer.analyzeSaturation(image));
        details.put("色彩分布均匀度", featureAnalyzer.analyzeColorUniformity(image));
        details.put("对比度", featureAnalyzer.analyzeContrast(image));
        details.put("高频细节", featureAnalyzer.analyzeHighFrequencyDetails(image));
        details.put("噪点密度", featureAnalyzer.analyzeNoiseDensity(image));
        details.put("边缘锐度", featureAnalyzer.analyzeEdgeSharpness(image));
        details.put("纹理复杂度", featureAnalyzer.analyzeTextureComplexity(image));
        details.put("色调偏向", featureAnalyzer.analyzeWarmTone(image));
        
        return details;
    }

    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片特征高度符合Adobe Firefly生成特征：商业级质量，专业色彩管理，细节精致，噪点极低。具有Adobe典型的专业渲染特征和版权安全性。";
        } else if (score >= 60) {
            return "图片具有较明显的Adobe Firefly特征：质量优秀，色彩专业，细节丰富。可能使用了Firefly的标准设置和商业级参数。";
        } else if (score >= 40) {
            return "图片部分特征与Adobe Firefly相似：具有一定的专业性和商业美学。可能使用了Firefly或经过了专业后期处理。";
        } else {
            return "图片特征与Adobe Firefly生成特征差异较大：可能是其他AI工具生成或真实图片。Firefly通常具有极高的质量和专业的色彩管理。";
        }
    }
}
