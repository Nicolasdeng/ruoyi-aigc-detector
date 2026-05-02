package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatGPT/DALL-E 3 模型检测器（增强版）
 *
 * DALL-E 3 新特性：
 * 1. 超高质量渲染 - 细节极其丰富，几乎无瑕疵
 * 2. 文字处理能力强 - 可以准确渲染文字
 * 3. 光影真实度极高 - 接近照片级别
 * 4. 色彩自然度提升 - 更接近真实照片
 * 5. 构图专业性强 - 符合摄影美学
 * 6. 低噪点高清晰 - 输出质量极高
 * 7. 边缘处理完美 - 无明显AI痕迹
 * 8. 纹理连贯性强 - 材质表现真实
 *
 * @author ruoyi
 */
@Component
public class ChatGPTModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "ChatGPT/DALL-E-3";
    }

    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        if (image == null) {
            return createEmptyResult();
        }

        // 1. 超高质量渲染检测 (25分)
        double qualityScore = analyzeUltraHighQuality(image);

        // 2. 光影真实度分析 (20分)
        double lightingScore = analyzeLightingRealism(image);

        // 3. 色彩自然度分析 (20分)
        double colorScore = analyzeColorNaturalness(image);

        // 4. 纹理连贯性分析 (15分)
        double textureScore = analyzeTextureCoherence(image);

        // 5. AI指纹特征 (20分) - DALL-E 3特有的细微特征
        double fingerprintScore = analyzeDallE3Fingerprint(image);

        // 计算总分
        double totalScore = qualityScore + lightingScore + colorScore + textureScore + fingerprintScore;

        // 获取详细特征
        Map<String, Double> featureDetails = getFeatureDetails(image);

        // 生成建议
        String suggestions = generateSuggestions(totalScore);

        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        result.setTotalScore(totalScore);
        result.setColorScore(colorScore);
        result.setTextureScore(textureScore);
        result.setNoiseScore(qualityScore);
        result.setStyleScore(lightingScore);
        result.setFingerprintScore(fingerprintScore);
        result.setFeatureDetails(featureDetails);
        result.setSuggestions(suggestions);

        return result;
    }

    /**
     * 分析超高质量渲染 (25分)
     * DALL-E 3特点：极低噪点(10分)、超高清晰度(10分)、完美边缘(5分)
     */
    private double analyzeUltraHighQuality(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 极低噪点检测 (10分)
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
            if (noiseDensity < 0.15) {
                score += 10.0 * (0.15 - noiseDensity) / 0.15;
            } else if (noiseDensity < 0.25) {
                score += 5.0 * (0.25 - noiseDensity) / 0.1;
            }

            // 2. 超高清晰度 (10分)
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            if (edgeSharpness > 0.75) {
                score += 10.0 * (edgeSharpness - 0.75) / 0.25;
            } else if (edgeSharpness > 0.6) {
                score += 5.0 * (edgeSharpness - 0.6) / 0.15;
            }

            // 3. 完美边缘处理 (5分)
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            if (highFreq > 0.7) {
                score += 5.0 * (highFreq - 0.7) / 0.3;
            } else if (highFreq > 0.5) {
                score += 2.5 * (highFreq - 0.5) / 0.2;
            }
        } else {
            score = 12.5;
        }

        return Math.min(score, 25.0);
    }

    /**
     * 分析光影真实度 (20分)
     * DALL-E 3特点：光影自然(10分)、对比度适中(5分)、高光处理真实(5分)
     */
    private double analyzeLightingRealism(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 光影自然度 (10分)
            double contrast = featureAnalyzer.analyzeContrast(image);
            if (contrast >= 0.55 && contrast <= 0.75) {
                score += 10.0;
            } else if (contrast >= 0.45 && contrast <= 0.85) {
                score += 6.0;
            } else {
                score += 3.0;
            }

            // 2. 对比度适中 (5分)
            if (contrast >= 0.5 && contrast <= 0.7) {
                score += 5.0;
            } else if (contrast >= 0.4 && contrast <= 0.8) {
                score += 3.0;
            }

            // 3. 高光处理真实 (5分) - 通过色调分析
            double warmTone = featureAnalyzer.analyzeWarmTone(image);
            if (warmTone >= 0.45 && warmTone <= 0.55) {
                score += 5.0;
            } else if (warmTone >= 0.35 && warmTone <= 0.65) {
                score += 3.0;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析色彩自然度 (20分)
     * DALL-E 3特点：色彩真实(8分)、饱和度适中(7分)、色彩分布均匀(5分)
     */
    private double analyzeColorNaturalness(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 色彩真实度 (8分)
            double saturation = featureAnalyzer.analyzeSaturation(image);
            if (saturation >= 0.5 && saturation <= 0.7) {
                score += 8.0;
            } else if (saturation >= 0.4 && saturation <= 0.8) {
                score += 5.0;
            } else {
                score += 2.0;
            }

            // 2. 饱和度适中 (7分)
            if (saturation >= 0.55 && saturation <= 0.65) {
                score += 7.0;
            } else if (saturation >= 0.45 && saturation <= 0.75) {
                score += 4.0;
            }

            // 3. 色彩分布均匀 (5分)
            double uniformity = featureAnalyzer.analyzeColorUniformity(image);
            if (uniformity > 0.75) {
                score += 5.0 * (uniformity - 0.75) / 0.25;
            } else if (uniformity > 0.6) {
                score += 2.5 * (uniformity - 0.6) / 0.15;
            }
        } else {
            score = 10.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 分析纹理连贯性 (15分)
     * DALL-E 3特点：纹理真实(8分)、细节丰富(7分)
     */
    private double analyzeTextureCoherence(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 纹理真实度 (8分)
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
            if (textureComplexity > 0.6) {
                score += 8.0 * (textureComplexity - 0.6) / 0.4;
            } else if (textureComplexity > 0.4) {
                score += 4.0 * (textureComplexity - 0.4) / 0.2;
            }

            // 2. 细节丰富度 (7分)
            double highFreq = featureAnalyzer.analyzeHighFrequencyDetails(image);
            if (highFreq > 0.65) {
                score += 7.0 * (highFreq - 0.65) / 0.35;
            } else if (highFreq > 0.5) {
                score += 3.5 * (highFreq - 0.5) / 0.15;
            }
        } else {
            score = 7.5;
        }

        return Math.min(score, 15.0);
    }

    /**
     * 分析DALL-E 3特有指纹 (20分)
     * 检测DALL-E 3的独特生成特征
     */
    private double analyzeDallE3Fingerprint(BufferedImage image) {
        double score = 0.0;

        if (featureAnalyzer != null) {
            // 1. 完美渲染特征 (10分) - 综合质量评估
            double contrast = featureAnalyzer.analyzeContrast(image);
            double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
            double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);

            double renderQuality = (contrast * 0.3 + edgeSharpness * 0.4 + (1.0 - noiseDensity) * 0.3);
            if (renderQuality > 0.75) {
                score += 10.0 * (renderQuality - 0.75) / 0.25;
            } else if (renderQuality > 0.6) {
                score += 5.0 * (renderQuality - 0.6) / 0.15;
            }

            // 2. 专业摄影感 (10分)
            double saturation = featureAnalyzer.analyzeSaturation(image);
            double uniformity = featureAnalyzer.analyzeColorUniformity(image);
            double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);

            double professionalScore = (saturation * 0.3 + uniformity * 0.3 + textureComplexity * 0.4);
            if (professionalScore > 0.7) {
                score += 10.0 * (professionalScore - 0.7) / 0.3;
            } else if (professionalScore > 0.55) {
                score += 5.0 * (professionalScore - 0.55) / 0.15;
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
        if (score >= 85) {
            return "图片特征与ChatGPT/DALL-E 3生成高度吻合。该图片极可能由最新版DALL-E 3生成，具有超高质量渲染、完美光影、自然色彩等顶级特征，几乎达到照片级真实度。";
        } else if (score >= 70) {
            return "图片特征与ChatGPT/DALL-E 3生成较为吻合。该图片很可能由DALL-E 3或类似高端AI工具生成，质量极高但仍有细微AI痕迹。";
        } else if (score >= 50) {
            return "图片特征与ChatGPT/DALL-E 3生成部分吻合。该图片可能由DALL-E或其他AI工具生成，建议结合其他检测器综合判断。";
        } else {
            return "图片特征与ChatGPT/DALL-E 3生成不太吻合。该图片不太可能由最新版DALL-E 3生成，可能是其他AI工具、早期版本或人工创作。";
        }
    }

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