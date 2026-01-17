package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Azure TTS AI音频检测器
 * 
 * 特征分析：
 * 1. 企业级稳定性和一致性
 * 2. 标准化的韵律模式
 * 3. 清晰的发音和音素转换
 * 4. 中等情感表达能力
 * 5. 极低的噪声水平
 * 6. 规范的语速控制
 * 
 * @author ruoyi
 */
@Component
public class AzureTtsDetector implements IAudioAiModelDetector {

    @Override
    public String getDetectorName() {
        return "Azure TTS";
    }

    @Override
    public double getWeight() {
        return 0.12;
    }

    @Override
    public AudioModelDetectionResult detect(String audioPath, Map<String, Object> audioFeatures) {
        try {
            // 提取关键特征
            double rhythmRegularity = getFeatureValue(audioFeatures, "rhythmRegularity");
            double spectrumSmoothness = getFeatureValue(audioFeatures, "spectrumSmoothness");
            double phonemeTransitionSmoothness = getFeatureValue(audioFeatures, "phonemeTransitionSmoothness");
            double noiseUniformity = getFeatureValue(audioFeatures, "noiseUniformity");
            double emotionVariation = getFeatureValue(audioFeatures, "emotionVariation");
            double speechRateStability = getFeatureValue(audioFeatures, "speechRateStability");
            double breathSoundPresence = getFeatureValue(audioFeatures, "breathSoundPresence");
            double timbreVariation = getFeatureValue(audioFeatures, "timbreVariation");

            double totalScore = 0.0;
            double confidence = 0.0;

            // 1. 韵律规整性：85-95分（标准化韵律）
            if (rhythmRegularity >= 85 && rhythmRegularity <= 95) {
                totalScore += 25;
                confidence += 22;
            } else if (rhythmRegularity >= 80 && rhythmRegularity < 85) {
                totalScore += 12;
                confidence += 10;
            }

            // 2. 音素转换平滑度：88-96分（清晰发音）
            if (phonemeTransitionSmoothness >= 88 && phonemeTransitionSmoothness <= 96) {
                totalScore += 25;
                confidence += 20;
            } else if (phonemeTransitionSmoothness >= 82 && phonemeTransitionSmoothness < 88) {
                totalScore += 12;
                confidence += 10;
            }

            // 3. 噪声均匀性：>92分（极低噪声）
            if (noiseUniformity > 92) {
                totalScore += 20;
                confidence += 18;
            } else if (noiseUniformity > 88) {
                totalScore += 10;
                confidence += 9;
            }

            // 4. 频谱平滑度：82-92分（企业级质量）
            if (spectrumSmoothness >= 82 && spectrumSmoothness <= 92) {
                totalScore += 15;
                confidence += 15;
            } else if (spectrumSmoothness >= 75 && spectrumSmoothness < 82) {
                totalScore += 7;
                confidence += 7;
            }

            // 5. 语速稳定性：85-95分（规范控制）
            if (speechRateStability >= 85 && speechRateStability <= 95) {
                totalScore += 15;
                confidence += 12;
            } else if (speechRateStability >= 78 && speechRateStability < 85) {
                totalScore += 7;
                confidence += 6;
            }

            // 6. 情感变化度：45-65分（中等情感）
            if (emotionVariation >= 45 && emotionVariation <= 65) {
                totalScore += 12;
                confidence += 10;
            } else if (emotionVariation >= 35 && emotionVariation < 45) {
                totalScore += 6;
                confidence += 5;
            }

            // 7. 呼吸音存在：<12分（很少呼吸音）
            if (breathSoundPresence < 12) {
                totalScore += 10;
                confidence += 8;
            } else if (breathSoundPresence < 20) {
                totalScore += 5;
                confidence += 4;
            }

            // 8. 音色变化度：75-88分（稳定音色）
            if (timbreVariation >= 75 && timbreVariation <= 88) {
                totalScore += 10;
                confidence += 8;
            }

            // 构建结果
            AudioModelDetectionResult result = AudioModelDetectionResult.success(
                getDetectorName(),
                "Azure Cognitive Services TTS",
                totalScore,
                Math.min(confidence, 100.0)
            );

            // 添加详细特征信息
            result.addFeature("rhythmRegularity", rhythmRegularity);
            result.addFeature("phonemeTransitionSmoothness", phonemeTransitionSmoothness);
            result.addFeature("noiseUniformity", noiseUniformity);
            result.addFeature("spectrumSmoothness", spectrumSmoothness);
            result.addFeature("speechRateStability", speechRateStability);
            result.addFeature("emotionVariation", emotionVariation);
            result.addFeature("breathSoundPresence", breathSoundPresence);
            result.addFeature("timbreVariation", timbreVariation);
            result.addFeature("detectionReason", buildDetectionReason(
                rhythmRegularity, phonemeTransitionSmoothness, noiseUniformity, 
                spectrumSmoothness, speechRateStability, emotionVariation
            ));

            return result;

        } catch (Exception e) {
            return AudioModelDetectionResult.failure(getDetectorName(), 
                "Azure TTS检测异常: " + e.getMessage());
        }
    }

    /**
     * 获取特征值
     */
    private double getFeatureValue(Map<String, Object> features, String key) {
        Object value = features.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    /**
     * 构建检测原因说明
     */
    private String buildDetectionReason(double rhythmRegularity, double phonemeTransitionSmoothness,
                                       double noiseUniformity, double spectrumSmoothness,
                                       double speechRateStability, double emotionVariation) {
        StringBuilder reason = new StringBuilder();
        
        if (rhythmRegularity >= 85 && rhythmRegularity <= 95) {
            reason.append("标准化韵律模式(").append(String.format("%.1f", rhythmRegularity)).append("); ");
        }
        
        if (phonemeTransitionSmoothness >= 88 && phonemeTransitionSmoothness <= 96) {
            reason.append("清晰的发音(").append(String.format("%.1f", phonemeTransitionSmoothness)).append("); ");
        }
        
        if (noiseUniformity > 92) {
            reason.append("极低噪声水平(").append(String.format("%.1f", noiseUniformity)).append("); ");
        }
        
        if (spectrumSmoothness >= 82 && spectrumSmoothness <= 92) {
            reason.append("企业级音质(").append(String.format("%.1f", spectrumSmoothness)).append("); ");
        }
        
        if (speechRateStability >= 85 && speechRateStability <= 95) {
            reason.append("规范的语速(").append(String.format("%.1f", speechRateStability)).append("); ");
        }
        
        if (emotionVariation >= 45 && emotionVariation <= 65) {
            reason.append("中等情感表达(").append(String.format("%.1f", emotionVariation)).append("); ");
        }
        
        return reason.length() > 0 ? reason.toString() : "特征匹配度较低";
    }
}
