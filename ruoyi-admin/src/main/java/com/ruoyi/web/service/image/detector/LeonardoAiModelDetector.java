package com.ruoyi.web.service.image.detector;

import com.ruoyi.web.service.image.IImageAiModelDetector;
import com.ruoyi.web.service.image.util.ImageFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Leonardo AI图片模型检测器
 * 
 * Leonardo AI特征：
 * 1. 游戏美术风格导向
 * 2. 概念设计专业化
 * 3. 风格化强烈
 * 4. 细节极其丰富
 * 5. 饱和度高且艺术化
 * 6. 角色和场景设计优化
 * 
 * @author ruoyi
 */
@Component
public class LeonardoAiModelDetector implements IImageAiModelDetector {

    @Autowired(required = false)
    private ImageFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Leonardo AI";
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
     * 计算色彩特征得分（满分20分）
     * Leonardo AI特征：饱和度高且艺术化(0.75-0.95)、色彩分布风格化(0.5-0.75)、色调游戏化(0.4-0.8)
     */
    private double calculateColorScore(BufferedImage image) {
        double score = 0.0;

        // 分析饱和度
        double saturation = featureAnalyzer.analyzeSaturation(image);
        if (saturation >= 0.75 && saturation <= 0.95) {
            score += 7.0; // 高饱和度艺术化风格
        } else if (saturation >= 0.65 && saturation < 0.75) {
            score += 4.0;
        } else if (saturation > 0.95) {
            score += 3.0; // 过高可能不是Leonardo
        }

        // 分析色彩分布均匀度
        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        if (colorUniformity >= 0.5 && colorUniformity <= 0.75) {
            score += 7.0; // 风格化分布，不追求完全均匀
        } else if (colorUniformity >= 0.4 && colorUniformity < 0.5) {
            score += 4.0;
        }

        // 分析色调倾向
        double warmTone = featureAnalyzer.analyzeWarmTone(image);
        if (warmTone >= 0.4 && warmTone <= 0.8) {
            score += 6.0; // 游戏美术色调灵活，冷暖皆可
        } else if (warmTone >= 0.3 && warmTone < 0.4) {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算纹理与细节得分（满分20分）
     * Leonardo AI特征：细节极其丰富(0.75-0.95)、边缘清晰度高(0.7-0.9)、纹理复杂度高(0.7-0.9)
     */
    private double calculateTextureScore(BufferedImage image) {
        double score = 0.0;

        // 分析高频细节
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        if (highFreqDetails >= 0.75 && highFreqDetails <= 0.95) {
            score += 7.5; // 概念设计细节极致
        } else if (highFreqDetails >= 0.65 && highFreqDetails < 0.75) {
            score += 4.0;
        }

        // 分析边缘锐度
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        if (edgeSharpness >= 0.7 && edgeSharpness <= 0.9) {
            score += 7.0; // 游戏美术边缘清晰
        } else if (edgeSharpness >= 0.6 && edgeSharpness < 0.7) {
            score += 4.0;
        }

        // 分析纹理复杂度
        double textureComplexity = featureAnalyzer.analyzeTextureComplexity(image);
        if (textureComplexity >= 0.7 && textureComplexity <= 0.9) {
            score += 5.5; // 角色场景纹理丰富
        } else if (textureComplexity >= 0.6 && textureComplexity < 0.7) {
            score += 3.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算噪声与质量得分（满分20分）
     * Leonardo AI特征：适度艺术噪点(0.3-0.5)、高质量渲染(0.7-0.9)
     */
    private double calculateNoiseScore(BufferedImage image) {
        double score = 0.0;

        // 分析噪点密度
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        if (noiseDensity >= 0.3 && noiseDensity <= 0.5) {
            score += 10.0; // 艺术化噪点处理
        } else if (noiseDensity >= 0.2 && noiseDensity < 0.3) {
            score += 6.0;
        } else if (noiseDensity > 0.5 && noiseDensity <= 0.6) {
            score += 4.0; // 风格化可能更高
        }

        // 分析整体质量（对比度）
        double contrast = featureAnalyzer.analyzeContrast(image);
        if (contrast >= 0.7 && contrast <= 0.9) {
            score += 10.0; // 专业级渲染质量
        } else if (contrast >= 0.6 && contrast < 0.7) {
            score += 6.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算风格与构图得分（满分20分）
     * Leonardo AI特征：游戏美术构图(0.65-0.85)、概念设计导向
     */
    private double calculateStyleScore(BufferedImage image) {
        double score = 0.0;

        double colorUniformity = featureAnalyzer.analyzeColorUniformity(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double saturation = featureAnalyzer.analyzeSaturation(image);

        // 游戏美术构图评分：清晰度+细节组合
        double gameArtComposition = (edgeSharpness * 0.6 + highFreqDetails * 0.4);
        if (gameArtComposition >= 0.65 && gameArtComposition <= 0.85) {
            score += 10.0; // 专业游戏美术构图
        } else if (gameArtComposition >= 0.55 && gameArtComposition < 0.65) {
            score += 6.0;
        }

        // 概念设计导向：饱和度+细节结合
        double conceptDesignScore = (saturation * 0.5 + highFreqDetails * 0.5);
        if (conceptDesignScore >= 0.7 && conceptDesignScore <= 0.9) {
            score += 10.0; // 强烈的概念设计风格
        } else if (conceptDesignScore >= 0.6 && conceptDesignScore < 0.7) {
            score += 5.0;
        }

        return Math.min(score, 20.0);
    }

    /**
     * 计算AI指纹得分（满分20分）
     * Leonardo AI特征：游戏引擎特征、风格化技术指纹
     */
    private double calculateFingerprintScore(BufferedImage image) {
        double score = 0.0;

        double highFreqDetails = featureAnalyzer.analyzeHighFrequencyDetails(image);
        double edgeSharpness = featureAnalyzer.analyzeEdgeSharpness(image);
        double noiseDensity = featureAnalyzer.analyzeNoiseDensity(image);
        double contrast = featureAnalyzer.analyzeContrast(image);
        double saturation = featureAnalyzer.analyzeSaturation(image);

        // Leonardo技术指纹1：细节-噪点比值(游戏引擎特征)
        double detailNoiseRatio = highFreqDetails - noiseDensity;
        if (detailNoiseRatio >= 0.25 && detailNoiseRatio <= 0.5) {
            score += 5.0; // 游戏引擎渲染特征
        } else if (detailNoiseRatio >= 0.15 && detailNoiseRatio < 0.25) {
            score += 3.0;
        }

        // Leonardo技术指纹2：风格化强度(饱和度+锐度组合)
        double stylizationIntensity = (saturation + edgeSharpness) / 2.0;
        if (stylizationIntensity >= 0.7 && stylizationIntensity <= 0.9) {
            score += 5.0; // 强风格化特征
        } else if (stylizationIntensity >= 0.6 && stylizationIntensity < 0.7) {
            score += 3.0;
        }

        // Leonardo技术指纹3：角色场景优化特征(质量-噪点差值)
        double characterSceneOptimization = contrast - noiseDensity;
        if (characterSceneOptimization >= 0.3 && characterSceneOptimization <= 0.6) {
            score += 5.0; // 角色场景渲染优化
        } else if (characterSceneOptimization >= 0.2 && characterSceneOptimization < 0.3) {
            score += 3.0;
        }

        // Leonardo技术指纹4：概念艺术平衡度
        double conceptArtBalance = Math.abs(highFreqDetails - saturation);
        if (conceptArtBalance >= 0.05 && conceptArtBalance <= 0.2) {
            score += 5.0; // 细节与色彩的艺术平衡
        } else if (conceptArtBalance >= 0.02 && conceptArtBalance < 0.05) {
            score += 2.0;
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
        double highFreqDetails = details.get("highFreqDetails");
        double edgeSharpness = details.get("edgeSharpness");
        double saturation = details.get("saturation");
        double noiseDensity = details.get("noiseDensity");

        details.put("gameArtComposition", (edgeSharpness * 0.6 + highFreqDetails * 0.4));
        details.put("conceptDesignScore", (saturation * 0.5 + highFreqDetails * 0.5));
        details.put("stylizationIntensity", (saturation + edgeSharpness) / 2.0);
        details.put("detailNoiseRatio", highFreqDetails - noiseDensity);

        return details;
    }

    @Override
    public String generateSuggestions(double score) {
        if (score >= 80) {
            return "图片特征高度符合Leonardo AI生成模式，呈现典型的游戏美术和概念设计风格，" +
                   "细节极其丰富，风格化强烈，建议：该图片很可能由Leonardo AI生成，" +
                   "特别适用于游戏角色、场景概念设计等领域。";
        } else if (score >= 60) {
            return "图片具有较多Leonardo AI特征，展现出游戏美术导向和概念设计倾向，" +
                   "细节丰富度和风格化程度较高，建议：该图片可能由Leonardo AI生成，" +
                   "或使用了类似的游戏美术生成工具。";
        } else if (score >= 40) {
            return "图片显示部分Leonardo AI特征，如较高饱和度和细节丰富度，" +
                   "但风格化强度或游戏美术特征不够明显，建议：该图片可能由Leonardo AI生成，" +
                   "也可能是其他概念设计工具或人工创作。";
        } else {
            return "图片特征与Leonardo AI生成模式差异较大，缺乏典型的游戏美术风格和概念设计特征，" +
                   "建议：该图片很可能不是由Leonardo AI生成，可能是其他AI工具生成或人工创作。";
        }
    }

    /**
     * 创建默认结果（图片为空时）
     */
    private ImageModelDetectionResult createDefaultResult() {
        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        result.setTotalScore(0.0);
        result.setColorScore(0.0);
        result.setTextureScore(0.0);
        result.setNoiseScore(0.0);
        result.setStyleScore(0.0);
        result.setFingerprintScore(0.0);
        result.setFeatureDetails(new HashMap<>());
        result.setSuggestions("无法分析：图片为空");
        return result;
    }

    /**
     * 创建降级结果（特征分析器不可用时）
     */
    private ImageModelDetectionResult createFallbackResult() {
        Map<String, Double> fallbackDetails = new HashMap<>();
        fallbackDetails.put("status", 0.0);
        
        ImageModelDetectionResult result = new ImageModelDetectionResult(getModelName());
        result.setTotalScore(50.0);
        result.setColorScore(10.0);
        result.setTextureScore(10.0);
        result.setNoiseScore(10.0);
        result.setStyleScore(10.0);
        result.setFingerprintScore(10.0);
        result.setFeatureDetails(fallbackDetails);
        result.setSuggestions("特征分析器不可用，返回基础评分");
        return result;
    }
}
