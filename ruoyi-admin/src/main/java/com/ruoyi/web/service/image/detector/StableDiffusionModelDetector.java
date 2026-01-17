package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Stable Diffusion模型检测器
 * 特征：开源模型、质量不稳定、饱和度适中、风格多样、细节丰富度取决于模型和参数
 * 
 * @author ruoyi
 */
@Component
public class StableDiffusionModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Stable Diffusion";
    }

    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());

        if (featureAnalyzer == null) {
            // 降级处理:给予基础分数
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
     * SD特征：饱和度适中(0.4-0.7)、色彩分布较为自然、色调灵活多样
     */
    private double calculateColorScore(BufferedImage image) {
        double score = 0.0;

        // 1. 饱和度适中 (8分)
        double saturation = featureAnalyzer.analyzeSaturation(image);
        if (saturation >= 0.4 && saturation <= 0.7) {
            score += 8.0; // 适中饱和度，SD典型特征
        } else if (saturation >= 0.3 && saturation < 0.4 || saturation > 0.7 && saturation <= 0.8) {
            score += 5.0; // 稍微偏离
        } else {
            score += 2.0; // 明显不符合
        }

        // 2. 色彩分布自然 (7分)
        double uniformity = featureAnalyzer.analyzeColorUniformity(image);
        if (uniformity >= 0.5 && uniformity <= 0.8) {
            score += 7.0; // 自然分布
        } else if (uniformity >= 0.4 && uniformity < 0.5 || uniformity > 0.8 && uniformity <= 0.9) {
            score += 4.0;
        } else {
            score += 2.0;
        }

        // 3. 色调灵活性 (5分)
        // SD不强制色调，根据对比度判断色调多样性
        double contrast = featureAnalyzer.analyzeContrast(image);
        if (contrast >= 0.4 && contrast <= 0.8) {
            score += 5.0; // 色调灵活多样
        } else {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算纹理与细节得分（满分20分）
     * SD特征：细节丰富度取决于模型，整体细节中等偏上，纹理复杂度中等
     */
    private double calculateTextureScore(BufferedImage image) {
        double score = 0.0;

        // 1. 高频细节 (8分)
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        if (highFreq >= 0.5 && highFreq <= 0.8) {
            score += 8.0; // 中等偏上细节
        } else if (highFreq >= 0.4 && highFreq < 0.5 || highFreq > 0.8 && highFreq <= 0.9) {
            score += 5.0;
        } else {
            score += 2.0;
        }

        // 2. 边缘清晰度 (7分)
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        if (edgeSharpness >= 0.5 && edgeSharpness <= 0.8) {
            score += 7.0; // 边缘中等清晰
        } else if (edgeSharpness >= 0.4 && edgeSharpness < 0.5) {
            score += 4.0;
        } else {
            score += 2.0;
        }

        // 3. 纹理复杂度 (5分)
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        if (textureComplexity >= 0.5 && textureComplexity <= 0.8) {
            score += 5.0; // 中等纹理复杂度
        } else if (textureComplexity >= 0.4 && textureComplexity < 0.5) {
            score += 3.0;
        } else {
            score += 1.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算噪声与质量得分（满分20分）
     * SD特征：可能存在较多噪点、质量不稳定、取决于采样步数和模型
     */
    private double calculateNoiseScore(BufferedImage image) {
        double score = 0.0;

        // 1. 噪点密度 (10分)
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        // SD可能有较多噪点，0.3-0.6是典型范围
        if (noiseDensity >= 0.3 && noiseDensity <= 0.6) {
            score += 10.0; // 典型SD噪点水平
        } else if (noiseDensity < 0.3) {
            score += 6.0; // 质量较好的SD输出
        } else if (noiseDensity > 0.6 && noiseDensity <= 0.8) {
            score += 7.0; // 采样步数较少的SD输出
        } else {
            score += 3.0; // 质量很差或非SD
        }

        // 2. 整体质量 (10分)
        // 综合评估：高频细节和边缘清晰度
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double quality = (highFreq + edgeSharpness) / 2.0;
        
        if (quality >= 0.5 && quality <= 0.8) {
            score += 10.0; // 中等质量，SD典型
        } else if (quality >= 0.4 && quality < 0.5) {
            score += 6.0;
        } else if (quality > 0.8) {
            score += 7.0; // 质量过高，可能不是SD
        } else {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算风格与构图得分（满分20分）
     * SD特征：风格多样化、没有固定模式、构图灵活
     */
    private double calculateStyleScore(BufferedImage image) {
        double score = 0.0;

        // 1. 风格多样性 (10分)
        // 通过色彩分布和纹理复杂度判断风格灵活性
        double uniformity = featureAnalyzer.analyzeColorUniformity(image);
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        double styleDiversity = Math.abs(uniformity - 0.6) + Math.abs(textureComplexity - 0.6);
        
        // 风格多样性越高，差值越大
        if (styleDiversity >= 0.2 && styleDiversity <= 0.6) {
            score += 10.0; // 风格灵活多样
        } else if (styleDiversity >= 0.1 && styleDiversity < 0.2) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        // 2. 构图灵活性 (10分)
        // SD没有固定构图模式，对比度和饱和度应该较为均衡
        double saturation = featureAnalyzer.analyzeSaturation(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double balance = Math.abs(saturation - contrast);
        
        if (balance <= 0.3) {
            score += 10.0; // 构图均衡灵活
        } else if (balance <= 0.5) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算AI指纹得分（满分20分）
     * SD特征：开源模型指纹、质量波动、参数依赖性强
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double score = 0.0;

        // 1. 开源模型特征 (10分)
        // SD的开源特性导致输出质量波动较大
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double qualityVariation = Math.abs(noiseDensity - highFreq);
        
        if (qualityVariation >= 0.2 && qualityVariation <= 0.5) {
            score += 10.0; // 典型SD质量波动
        } else if (qualityVariation >= 0.1 && qualityVariation < 0.2) {
            score += 6.0;
        } else {
            score += 3.0;
        }

        // 2. 参数依赖性 (10分)
        // SD的输出高度依赖采样参数，体现在细节和噪点的关系上
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double parameterDependency = Math.abs((highFreq + edgeSharpness) / 2.0 - 0.6);
        
        if (parameterDependency <= 0.2) {
            score += 10.0; // 典型SD参数设置
        } else if (parameterDependency <= 0.3) {
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
        
        return details;
    }

    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片特征高度符合Stable Diffusion生成特征：质量波动适中，风格灵活多样，具有开源模型的典型特征。建议检查采样步数、CFG Scale等参数设置。";
        } else if (score >= 60) {
            return "图片具有较明显的Stable Diffusion特征：饱和度适中，细节丰富度中等，存在一定噪点。可能使用了标准的SD模型和参数。";
        } else if (score >= 40) {
            return "图片部分特征与Stable Diffusion相似：可能使用了SD的变体模型或经过了后期处理。建议结合其他检测器综合判断。";
        } else {
            return "图片特征与Stable Diffusion生成特征差异较大：可能是其他AI工具生成或真实图片。SD通常具有中等饱和度和一定的质量波动。";
        }
    }
}
