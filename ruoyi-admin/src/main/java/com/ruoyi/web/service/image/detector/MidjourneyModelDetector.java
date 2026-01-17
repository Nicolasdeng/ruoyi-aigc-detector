package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Midjourney模型检测器
 * 
 * Midjourney特征：
 * 1. 色彩特征：超高饱和度、艺术化色调、梦幻感
 * 2. 纹理细节：极致细节、质感强、纹理复杂
 * 3. 噪声质量：艺术化噪点、特殊质感
 * 4. 风格构图：强烈艺术风格、视觉冲击力
 * 5. AI指纹：典型MJ美学、独特渲染风格
 * 
 * @author ruoyi
 */
@Component
public class MidjourneyModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Midjourney";
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
     * Midjourney特点：超高饱和度(10分)、艺术化色调(5分)、色彩冲击力(5分)
     */
    private double analyzeColorFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 超高饱和度分析 (10分) - MJ以高饱和度著称
            double saturation = featureAnalyzer.analyzeSaturation(image);
            if (saturation > 0.7) {
                score += 10.0 * (saturation - 0.7) / 0.3;
            } else if (saturation > 0.5) {
                score += 5.0 * (saturation - 0.5) / 0.2;
            } else {
                score += 2.0 * saturation / 0.5;
            }

            // 2. 艺术化色调 (5分) - MJ倾向极端色调
            double warmTone = featureAnalyzer.analyzeWarmTone(image);
            if (warmTone < 0.3 || warmTone > 0.7) {
                // 极端色调(冷色或暖色)
                score += 5.0;
            } else if (warmTone < 0.4 || warmTone > 0.6) {
                score += 3.0;
            } else {
                score += 1.0;
            }

            // 3. 色彩冲击力 (5分) - 高对比度和高饱和度组合
            double contrast = featureAnalyzer.analyzeContrast(image);
            double impact = saturation * contrast;
            if (impact > 0.6) {
                score += 5.0 * (impact - 0.6) / 0.4;
            } else {
                score += 2.5 * impact / 0.6;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析纹理与细节特征 (20分)
     * Midjourney特点：极致细节(10分)、复杂纹理(10分)
     */
    private double analyzeTextureFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 极致细节 (10分) - MJ以细节丰富著称
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            if (highFreq > 0.6) {
                score += 10.0 * (highFreq - 0.6) / 0.4;
            } else if (highFreq > 0.4) {
                score += 5.0 * (highFreq - 0.4) / 0.2;
            } else {
                score += 2.5 * highFreq / 0.4;
            }

            // 2. 复杂纹理 (10分) - 纹理复杂度和边缘锐度
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            double textureScore = (textureComplexity * 0.6 + edgeSharpness * 0.4);
            
            if (textureScore > 0.6) {
                score += 10.0 * (textureScore - 0.6) / 0.4;
            } else {
                score += 5.0 * textureScore / 0.6;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析噪声与质量特征 (20分)
     * Midjourney特点：艺术化噪点(10分)、高质量渲染(10分)
     */
    private double analyzeNoiseFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 艺术化噪点 (10分) - MJ噪点适中，带来艺术质感
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
            if (noiseDensity >= 0.3 && noiseDensity <= 0.5) {
                // 适度噪点是MJ的特征
                score += 10.0;
            } else if (noiseDensity < 0.3) {
                score += 7.0 * (0.3 - noiseDensity) / 0.3;
            } else if (noiseDensity < 0.7) {
                score += 5.0 * (0.7 - noiseDensity) / 0.2;
            }

            // 2. 高质量渲染 (10分)
            double contrast = featureAnalyzer.analyzeContrast(image);
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            double qualityScore = (contrast + highFreq) / 2.0;
            
            if (qualityScore > 0.6) {
                score += 10.0 * (qualityScore - 0.6) / 0.4;
            } else {
                score += 5.0 * qualityScore / 0.6;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析风格与构图特征 (20分)
     * Midjourney特点：强烈艺术风格(15分)、视觉冲击力(5分)
     */
    private double analyzeStyleFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 强烈艺术风格 (15分) - 多维度综合评估
            double saturation = featureAnalyzer.analyzeSaturation(image);
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            double artisticScore = (saturation * 0.4 + textureComplexity * 0.3 + highFreq * 0.3);
            
            if (artisticScore > 0.6) {
                score += 15.0 * (artisticScore - 0.6) / 0.4;
            } else {
                score += 7.5 * artisticScore / 0.6;
            }

            // 2. 视觉冲击力 (5分)
            double contrast = featureAnalyzer.analyzeContrast(image);
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            double impactScore = (contrast + edgeSharpness) / 2.0;
            
            if (impactScore > 0.7) {
                score += 5.0 * (impactScore - 0.7) / 0.3;
            } else {
                score += 2.5 * impactScore / 0.7;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析AI特征指纹 (20分)
     * Midjourney特点：典型MJ美学(10分)、独特渲染风格(10分)
     */
    private double analyzeAiFingerprintFeatures(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 典型MJ美学 (10分) - 高饱和度+复杂纹理
            double saturation = featureAnalyzer.analyzeSaturation(image);
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            double aestheticScore = (saturation + textureComplexity) / 2.0;
            
            if (aestheticScore > 0.65) {
                score += 10.0 * (aestheticScore - 0.65) / 0.35;
            } else {
                score += 5.0 * aestheticScore / 0.65;
            }

            // 2. 独特渲染风格 (10分) - 细节丰富+适度噪点
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
            
            // MJ特征：高细节+适度噪点(0.3-0.5)
            double detailScore = highFreq * 0.7;
            double noiseScore = 0.3;
            if (noiseDensity >= 0.3 && noiseDensity <= 0.5) {
                noiseScore = 0.3; // 理想噪点范围
            } else if (noiseDensity < 0.3) {
                noiseScore = 0.15;
            } else if (noiseDensity < 0.7) {
                noiseScore = 0.15;
            }
            
            double renderScore = detailScore + noiseScore;
            score += 10.0 * renderScore;
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
            return "图片特征与Midjourney生成高度吻合。建议：该图片很可能由Midjourney生成，具有超高饱和度、极致细节和强烈艺术风格等典型特征。";
        } else if (score >= 60) {
            return "图片特征与Midjourney生成较为吻合。建议：该图片可能由Midjourney生成，但部分特征不够明显，建议结合其他检测器综合判断。";
        } else if (score >= 40) {
            return "图片特征与Midjourney生成部分吻合。建议：该图片可能由Midjourney或其他AI工具生成，需要进一步分析其他特征。";
        } else {
            return "图片特征与Midjourney生成不太吻合。建议：该图片不太可能由Midjourney生成，可能是其他AI工具或人工创作。";
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
