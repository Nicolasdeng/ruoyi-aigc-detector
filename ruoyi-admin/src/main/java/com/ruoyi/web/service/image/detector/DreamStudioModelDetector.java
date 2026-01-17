package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * DreamStudio图片模型检测器
 * 
 * DreamStudio特征：
 * 1. Stable Diffusion官方平台
 * 2. 高质量专业化输出
 * 3. 参数可控性强
 * 4. 细节精致稳定
 * 5. 饱和度专业平衡
 * 6. 商业级渲染质量
 * 
 * @author ruoyi
 */
@Component
public class DreamStudioModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "DreamStudio";
    }

    @Override
    public ImageModelDetectionResult detectModel(BufferedImage image) {
        if (image == null) {
            return createDefaultResult();
        }

        // 降级处理：如果特征分析器不可用，返回基础评分
        if (featureAnalyzer == null) {
            return createFallbackResult();
        }

        // 计算各维度得分
        double colorScore = calculateColorScore(image);
        double textureScore = calculateTextureScore(image);
        double noiseScore = calculateNoiseScore(image);
        double styleScore = calculateStyleScore(image);
        double fingerprintScore = calculateFingerprintScore(image);

        // 计算总分
        double totalScore = colorScore + textureScore + noiseScore + styleScore + fingerprintScore;

        // 获取特征详情
        Map<String, Double> featureDetails = getFeatureDetails(image);

        // 生成建议
        String suggestions = generateSuggestions(totalScore);

        return new ImageModelDetectionResult(
            getModelName(),
            totalScore,
            colorScore,
            textureScore,
            noiseScore,
            styleScore,
            fingerprintScore,
            featureDetails,
            suggestions
        );
    }

    /**
     * 计算色彩特征得分（满分20分）
     * DreamStudio特征：饱和度专业平衡(0.6-0.8)、色彩分布精准(0.65-0.85)、色调可控(0.3-0.75)
     */
    private double calculateColorScore(BufferedImage image) {
        double score = 0.0;

        // 分析饱和度
        double saturation = featureAnalyzer.analyzeSaturation(image);
        if (saturation >= 0.6 && saturation <= 0.8) {
            score += 7.0; // 专业级饱和度平衡
        } else if (saturation >= 0.5 && saturation < 0.6) {
            score += 4.0;
        } else if (saturation > 0.8 && saturation <= 0.85) {
            score += 5.0; // 可能稍高但仍在范围
        }

        // 分析色彩分布均匀度
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        if (colorUniformity >= 0.65 && colorUniformity <= 0.85) {
            score += 7.0; // 精准色彩分布控制
        } else if (colorUniformity >= 0.55 && colorUniformity < 0.65) {
            score += 4.0;
        }

        // 分析色调倾向
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        if (warmTone >= 0.3 && warmTone <= 0.75) {
            score += 6.0; // 参数可控，冷暖平衡
        } else if (warmTone >= 0.2 && warmTone < 0.3) {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算纹理与细节得分（满分20分）
     * DreamStudio特征：细节精致稳定(0.7-0.9)、边缘清晰(0.7-0.85)、纹理专业(0.65-0.85)
     */
    private double calculateTextureScore(BufferedImage image) {
        double score = 0.0;

        // 分析高频细节
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        if (highFreqDetails >= 0.7 && highFreqDetails <= 0.9) {
            score += 7.5; // 官方平台细节稳定
        } else if (highFreqDetails >= 0.6 && highFreqDetails < 0.7) {
            score += 4.0;
        }

        // 分析边缘锐度
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        if (edgeSharpness >= 0.7 && edgeSharpness <= 0.85) {
            score += 7.0; // 专业级边缘处理
        } else if (edgeSharpness >= 0.6 && edgeSharpness < 0.7) {
            score += 4.0;
        }

        // 分析纹理复杂度
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        if (textureComplexity >= 0.65 && textureComplexity <= 0.85) {
            score += 5.5; // 商业级纹理质量
        } else if (textureComplexity >= 0.55 && textureComplexity < 0.65) {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算噪声与质量得分（满分20分）
     * DreamStudio特征：噪点控制极佳(0.15-0.35)、高质量渲染(0.75-0.95)
     */
    private double calculateNoiseScore(BufferedImage image) {
        double score = 0.0;

        // 分析噪点密度
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        if (noiseDensity >= 0.15 && noiseDensity <= 0.35) {
            score += 10.0; // 官方平台噪点控制优秀
        } else if (noiseDensity >= 0.1 && noiseDensity < 0.15) {
            score += 7.0; // 极低噪点也可能
        } else if (noiseDensity > 0.35 && noiseDensity <= 0.45) {
            score += 5.0; // 某些风格可能稍高
        }

        // 分析整体质量（对比度）
        double contrast = featureAnalyzer.analyzeContrast(image);
        if (contrast >= 0.75 && contrast <= 0.95) {
            score += 10.0; // 商业级高质量渲染
        } else if (contrast >= 0.65 && contrast < 0.75) {
            score += 6.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算风格与构图得分（满分20分）
     * DreamStudio特征：专业构图(0.7-0.9)、参数可控导向
     */
    private double calculateStyleScore(BufferedImage image) {
        double score = 0.0;

        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double contrast = featureAnalyzer.analyzeContrast(image);

        // 专业构图评分：均匀度+锐度组合
        double professionalComposition = (colorUniformity * 0.5 + edgeSharpness * 0.5);
        if (professionalComposition >= 0.7 && professionalComposition <= 0.9) {
            score += 10.0; // 专业级构图平衡
        } else if (professionalComposition >= 0.6 && professionalComposition < 0.7) {
            score += 6.0;
        }

        // 参数可控性评分：质量+细节稳定性
        double parameterControl = (contrast * 0.6 + highFreqDetails * 0.4);
        if (parameterControl >= 0.7 && parameterControl <= 0.9) {
            score += 10.0; // 强参数控制能力
        } else if (parameterControl >= 0.6 && parameterControl < 0.7) {
            score += 5.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算AI指纹得分（满分20分）
     * DreamStudio特征：SD官方技术指纹、商业级优化特征
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double score = 0.0;

        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double saturation = featureAnalyzer.analyzeSaturation(image);

        // DreamStudio技术指纹1：质量-噪点比值(官方平台优化)
        double qualityNoiseRatio = contrast - noiseDensity;
        if (qualityNoiseRatio >= 0.45 && qualityNoiseRatio <= 0.75) {
            score += 5.0; // SD官方平台优化特征
        } else if (qualityNoiseRatio >= 0.35 && qualityNoiseRatio < 0.45) {
            score += 3.0;
        }

        // DreamStudio技术指纹2：细节稳定性(细节+锐度一致性)
        double detailStability = Math.abs(highFreqDetails - edgeSharpness);
        if (detailStability >= 0.0 && detailStability <= 0.15) {
            score += 5.0; // 细节与锐度高度一致
        } else if (detailStability > 0.15 && detailStability <= 0.25) {
            score += 3.0;
        }

        // DreamStudio技术指纹3：商业级平衡度(色彩+质量平衡)
        double commercialBalance = (colorUniformity + contrast) / 2.0;
        if (commercialBalance >= 0.7 && commercialBalance <= 0.9) {
            score += 5.0; // 商业级质量平衡
        } else if (commercialBalance >= 0.6 && commercialBalance < 0.7) {
            score += 3.0;
        }

        // DreamStudio技术指纹4：参数控制精度(饱和度+均匀度精准度)
        double parameterPrecision = Math.abs(saturation - colorUniformity);
        if (parameterPrecision >= 0.05 && parameterPrecision <= 0.25) {
            score += 5.0; // 参数控制精准
        } else if (parameterPrecision >= 0.02 && parameterPrecision < 0.05) {
            score += 2.0; // 极高精准度
        } else if (parameterPrecision > 0.25 && parameterPrecision <= 0.35) {
            score += 2.0; // 某些风格差异可能更大
        }

        return Math.min(score, 20.0);
    }

    @Override
    public Map<String, Double> getFeatureDetails(BufferedImage image) {
        Map<String, Double> details = new HashMap<>();

        if (featureAnalyzer == null) {
            return details;
        }

        // 色彩特征
        details.put("saturation", featureAnalyzer.analyzeSaturation(image));
        details.put("colorUniformity", featureAnalyzer.analyzeColorUniformity(image));
        details.put("warmTone", featureAnalyzer.analyzeWarmTone(image));

        // 纹理与细节
        details.put("highFreqDetails", featureAnalyzer.analyzeHighFrequencyDetails(image));
        details.put("edgeSharpness", featureAnalyzer.analyzeEdgeSharpness(image));
        details.put("textureComplexity", featureAnalyzer.analyzeTextureComplexity(image));

        // 噪声与质量
        details.put("noiseDensity", featureAnalyzer.analyzeNoiseDensity(image));
        details.put("contrast", featureAnalyzer.analyzeContrast(image));

        // 计算复合特征
        double colorUniformity = details.get("colorUniformity");
        double edgeSharpness = details.get("edgeSharpness");
        double highFreqDetails = details.get("highFreqDetails");
        double contrast = details.get("contrast");
        double noiseDensity = details.get("noiseDensity");

        details.put("professionalComposition", (colorUniformity * 0.5 + edgeSharpness * 0.5));
        details.put("parameterControl", (contrast * 0.6 + highFreqDetails * 0.4));
        details.put("qualityNoiseRatio", contrast - noiseDensity);
        details.put("detailStability", Math.abs(highFreqDetails - edgeSharpness));

        return details;
    }

    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片特征高度符合DreamStudio生成模式，呈现典型的Stable Diffusion官方平台高质量输出，" +
                   "细节精致稳定，参数控制精准，商业级渲染质量，建议：该图片很可能由DreamStudio生成，" +
                   "展现了SD官方平台的专业优化能力。";
        } else if (score >= 60) {
            return "图片具有较多DreamStudio特征，展现出专业级质量和参数可控性，" +
                   "细节稳定性和色彩平衡度较高，建议：该图片可能由DreamStudio生成，" +
                   "或使用了SD官方平台的优化技术。";
        } else if (score >= 40) {
            return "图片显示部分DreamStudio特征，如较高质量和细节控制，" +
                   "但参数精准度或商业级特征不够明显，建议：该图片可能由DreamStudio生成，" +
                   "也可能是其他基于Stable Diffusion的工具或平台。";
        } else {
            return "图片特征与DreamStudio生成模式差异较大，缺乏典型的SD官方平台优化特征和商业级质量，" +
                   "建议：该图片很可能不是由DreamStudio生成，可能是其他AI工具生成或人工创作。";
        }
    }

    /**
     * 创建默认结果（图片为空时）
     */
    private ImageModelDetectionResult createDefaultResult() {
        return new ImageModelDetectionResult(
            getModelName(),
            0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            new HashMap<>(),
            "无法分析：图片为空"
        );
    }

    /**
     * 创建降级结果（特征分析器不可用时）
     */
    private ImageModelDetectionResult createFallbackResult() {
        Map<String, Double> fallbackDetails = new HashMap<>();
        fallbackDetails.put("status", 0.0);
        
        return new ImageModelDetectionResult(
            getModelName(),
            50.0, 10.0, 10.0, 10.0, 10.0, 10.0,
            fallbackDetails,
            "特征分析器不可用，返回基础评分"
        );
    }
}
