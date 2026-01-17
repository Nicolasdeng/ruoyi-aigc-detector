package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * DALL-E模型检测器
 * 
 * DALL-E特征：
 * 1. 色彩特征：高饱和度、自然色调、分布均匀
 * 2. 纹理细节：渲染质量高、细节丰富、边缘清晰
 * 3. 噪声质量：低噪点、高质量输出
 * 4. 风格构图：构图合理、专业感强
 * 5. AI指纹：文字处理能力强、光影真实
 * 
 * @author ruoyi
 */
@Component
public class DallEModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "DALL-E";
    }

    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        if (image == null) {
            return createEmptyResult();
        }

        // 1. 色彩特征分析 (20分)
        double colorScore = analyzeColorFeatures(image);

        // 2. 纹理与细节分析 (20分)
        double textureScore = analyzeTextureFeatures(image);

        // 3. 噪声与质量分析 (20分)
        double noiseScore = analyzeNoiseFeatures(image);

        // 4. 风格与构图分析 (20分)
        double styleScore = analyzeStyleFeatures(image);

        // 5. AI特征指纹分析 (20分)
        double fingerprintScore = analyzeAiFingerprintFeatures(image);

        // 计算总分
        double totalScore = colorScore + textureScore + noiseScore + styleScore + fingerprintScore;

        // 获取详细特征
        Map<String, Double> featureDetails = getFeatureDetails(image);

        // 生成建议
        String suggestions = generateSuggestions(totalScore);

        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        result.setTotalScore(totalScore);
        result.setColorScore(colorScore);
        result.setTextureScore(textureScore);
        result.setNoiseScore(noiseScore);
        result.setStyleScore(styleScore);
        result.setFingerprintScore(fingerprintScore);
        result.setFeatureDetails(featureDetails);
        result.setSuggestions(suggestions);
        
        return result;
    }

    /**
     * 分析色彩特征 (20分)
     * DALL-E特点：高饱和度(8分)、色彩分布均匀(7分)、自然色调(5分)
     */
    private double analyzeColorFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 饱和度分析 (8分)
            double saturation = featureAnalyzer.analyzeSaturation(image);
            if (saturation > 0.6) {
                score += 8.0 * (saturation - 0.6) / 0.4; // 饱和度0.6-1.0区间得分
            } else if (saturation > 0.4) {
                score += 4.0 * (saturation - 0.4) / 0.2; // 饱和度0.4-0.6区间得分减半
            }

            // 2. 色彩分布均匀度 (7分)
            double uniformity = featureAnalyzer.analyzeColorUniformity(image);
            if (uniformity > 0.7) {
                score += 7.0 * (uniformity - 0.7) / 0.3; // 均匀度0.7-1.0区间得分
            } else if (uniformity > 0.5) {
                score += 3.5 * (uniformity - 0.5) / 0.2; // 均匀度0.5-0.7区间得分减半
            }

            // 3. 色调自然度 (5分) - DALL-E倾向自然色调
            double warmTone = featureAnalyzer.analyzeWarmTone(image);
            if (warmTone >= 0.4 && warmTone <= 0.6) {
                // 色调平衡(0.4-0.6)表示自然
                score += 5.0;
            } else if (warmTone >= 0.3 && warmTone <= 0.7) {
                score += 3.0;
            } else {
                score += 1.0;
            }
        } else {
            // 降级为基础分析
            score = 10.0; // 给予基础分
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析纹理与细节特征 (20分)
     * DALL-E特点：高频细节丰富(8分)、边缘清晰(7分)、纹理复杂(5分)
     */
    private double analyzeTextureFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 高频细节分析 (8分) - DALL-E细节丰富
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            if (highFreq > 0.5) {
                score += 8.0 * (highFreq - 0.5) / 0.5;
            } else {
                score += 4.0 * highFreq / 0.5;
            }

            // 2. 边缘锐度 (7分) - DALL-E边缘清晰
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            if (edgeSharpness > 0.6) {
                score += 7.0 * (edgeSharpness - 0.6) / 0.4;
            } else {
                score += 3.5 * edgeSharpness / 0.6;
            }

            // 3. 纹理复杂度 (5分)
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            if (textureComplexity > 0.5) {
                score += 5.0 * (textureComplexity - 0.5) / 0.5;
            } else {
                score += 2.5 * textureComplexity / 0.5;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析噪声与质量特征 (20分)
     * DALL-E特点：噪点密度低(10分)、整体质量高(10分)
     */
    private double analyzeNoiseFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 噪点密度分析 (10分) - DALL-E噪点少
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
            if (noiseDensity < 0.3) {
                score += 10.0 * (0.3 - noiseDensity) / 0.3;
            } else if (noiseDensity < 0.5) {
                score += 5.0 * (0.5 - noiseDensity) / 0.2;
            }

            // 2. 整体质量评估 (10分) - 综合对比度和清晰度
            double contrast = featureAnalyzer.analyzeContrast(image);
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            double qualityScore = (contrast + edgeSharpness) / 2.0;
            
            if (qualityScore > 0.7) {
                score += 10.0 * (qualityScore - 0.7) / 0.3;
            } else {
                score += 5.0 * qualityScore / 0.7;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析风格与构图特征 (20分)
     * DALL-E特点：构图合理(10分)、专业感强(10分)
     */
    private double analyzeStyleFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 构图合理性 (10分) - 通过色彩分布和对比度评估
            double uniformity = featureAnalyzer.analyzeColorUniformity(image);
            double contrast = featureAnalyzer.analyzeContrast(image);
            double compositionScore = (uniformity * 0.6 + contrast * 0.4);
            
            if (compositionScore > 0.6) {
                score += 10.0 * (compositionScore - 0.6) / 0.4;
            } else {
                score += 5.0 * compositionScore / 0.6;
            }

            // 2. 专业感评估 (10分) - 综合多个指标
            double saturation = featureAnalyzer.analyzeSaturation(image);
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            double professionalScore = (saturation * 0.3 + edgeSharpness * 0.4 + textureComplexity * 0.3);
            
            if (professionalScore > 0.6) {
                score += 10.0 * (professionalScore - 0.6) / 0.4;
            } else {
                score += 5.0 * professionalScore / 0.6;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析AI特征指纹 (20分)
     * DALL-E特点：光影真实(10分)、渲染质量高(10分)
     */
    private double analyzeAiFingerprintFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 光影真实度 (10分) - 通过对比度和色调评估
            double contrast = featureAnalyzer.analyzeContrast(image);
            double warmTone = featureAnalyzer.analyzeWarmTone(image);
            
            // DALL-E光影自然，对比度适中
            if (contrast >= 0.5 && contrast <= 0.8) {
                score += 5.0;
            } else if (contrast >= 0.4 && contrast <= 0.9) {
                score += 3.0;
            }
            
            // 色调平衡
            if (warmTone >= 0.4 && warmTone <= 0.6) {
                score += 5.0;
            } else if (warmTone >= 0.3 && warmTone <= 0.7) {
                score += 3.0;
            }

            // 2. 渲染质量 (10分) - 综合评估
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
            double renderScore = highFreq * (1.0 - noiseDensity);
            
            if (renderScore > 0.5) {
                score += 10.0 * (renderScore - 0.5) / 0.5;
            } else {
                score += 5.0 * renderScore / 0.5;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    @Override
    public Map<String, Double> getFeatureDetails(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        if (image == null || featureAnalyzer == null) {
            return features;
        }

        features.put("saturation", featureAnalyzer.analyzeSaturation(image));
        features.put("colorUniformity", featureAnalyzer.analyzeColorUniformity(image));
        features.put("contrast", featureAnalyzer.analyzeContrast(image));
        features.put("highFreqDetails", featureAnalyzer.analyzeHighFrequencyDetails(image));
        features.put("noiseDensity", featureAnalyzer.analyzeNoiseDensity(image));
        features.put("edgeSharpness", featureAnalyzer.analyzeEdgeSharpness(image));
        features.put("textureComplexity", featureAnalyzer.analyzeTextureComplexity(image));
        features.put("warmTone", featureAnalyzer.analyzeWarmTone(image));

        return features;
    }

    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片特征与DALL-E生成高度吻合。建议：该图片很可能由DALL-E生成，具有高质量渲染、自然色调和清晰细节等典型特征。";
        } else if (score >= 60) {
            return "图片特征与DALL-E生成较为吻合。建议：该图片可能由DALL-E生成，但部分特征不够明显，建议结合其他检测器综合判断。";
        } else if (score >= 40) {
            return "图片特征与DALL-E生成部分吻合。建议：该图片可能由DALL-E或其他AI工具生成，需要进一步分析其他特征。";
        } else {
            return "图片特征与DALL-E生成不太吻合。建议：该图片不太可能由DALL-E生成，可能是其他AI工具或人工创作。";
        }
    }

    /**
     * 创建空结果
     */
    private ImageModelDetectionResult createEmptyResult() {
        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        result.setTotalScore(0.0);
        result.setColorScore(0.0);
        result.setTextureScore(0.0);
        result.setNoiseScore(0.0);
        result.setStyleScore(0.0);
        result.setFingerprintScore(0.0);
        result.setFeatureDetails(new HashMap<>());
        result.setSuggestions("无法分析图片");
        return result;
    }
}
