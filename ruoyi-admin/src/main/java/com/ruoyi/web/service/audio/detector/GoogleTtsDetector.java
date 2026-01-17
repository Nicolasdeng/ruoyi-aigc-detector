package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Google TTS AI音频检测器
 * 
 * 特征分析：
 * 1. 高度自然的韵律节奏
 * 2. 优秀的音高控制
 * 3. 中等偏上的情感表达
 * 4. 清晰的频谱特征
 * 5. 较低的背景噪声
 * 6. 自然的语速变化
 * 
 * @author ruoyi
 */
@Component
public class GoogleTtsDetector implements IAudioAiModelDetector {

    @Override
    public String getDetectorName() {
        return "Google TTS";
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
            double pitchNaturalness = getFeatureValue(audioFeatures, "pitchNaturalness");
            double emotionVariation = getFeatureValue(audioFeatures, "emotionVariation");
            double spectrumSmoothness = getFeatureValue(audioFeatures, "spectrumSmoothness");
            double noiseUniformity = getFeatureValue(audioFeatures, "noiseUniformity");
            double speechRateStability = getFeatureValue(audioFeatures, "speechRateStability");
            double breathSoundPresence = getFeatureValue(audioFeatures, "breathSoundPresence");
            double phonemeTransitionSmoothness = getFeatureValue(audioFeatures, "phonemeTransitionSmoothness");

            double totalScore = 0.0;
            double confidence = 0.0;

            // 1. 韵律规整性：80-92分（自然韵律）
            if (rhythmRegularity >= 80 && rhythmRegularity <= 92) {
                totalScore += 25;
                confidence += 20;
            } else if (rhythmRegularity >= 75 && rhythmRegularity < 80) {
                totalScore += 12;
                confidence += 10;
            }

            // 2. 音高自然度：85-95分（优秀音高控制）
            if (pitchNaturalness >= 85 && pitchNaturalness <= 95) {
                totalScore += 25;
                confidence += 22;
            } else if (pitchNaturalness >= 78 && pitchNaturalness < 85) {
                totalScore += 12;
                confidence += 10;
            }

            // 3. 情感变化度：60-80分（中等偏上情感）
            if (emotionVariation >= 60 && emotionVariation <= 80) {
                totalScore += 20;
                confidence += 18;
            } else if (emotionVariation >= 50 && emotionVariation < 60) {
                totalScore += 10;
                confidence += 9;
            }

            // 4. 频谱平滑度：83-93分（清晰频谱）
            if (spectrumSmoothness >= 83 && spectrumSmoothness <= 93) {
                totalScore += 15;
                confidence += 15;
            } else if (spectrumSmoothness >= 75 && spectrumSmoothness < 83) {
                totalScore += 7;
                confidence += 7;
            }

            // 5. 噪声均匀性：>87分（较低噪声）
            if (noiseUniformity > 87) {
                totalScore += 15;
                confidence += 12;
            } else if (noiseUniformity > 80) {
                totalScore += 7;
                confidence += 6;
            }

            // 6. 语速稳定性：72-88分（自然语速变化）
            if (speechRateStability >= 72 && speechRateStability <= 88) {
                totalScore += 12;
                confidence += 10;
            } else if (speechRateStability >= 65 && speechRateStability < 72) {
                totalScore += 6;
                confidence += 5;
            }

            // 7. 呼吸音存在：15-30分（适度呼吸音）
            if (breathSoundPresence >= 15 && breathSoundPresence <= 30) {
                totalScore += 10;
                confidence += 8;
            } else if (breathSoundPresence >= 8 && breathSoundPresence < 15) {
                totalScore += 5;
                confidence += 4;
            }

            // 8. 音素转换平滑度：>82分
            if (phonemeTransitionSmoothness > 82) {
                totalScore += 10;
                confidence += 8;
            }

            // 构建结果
            AudioModelDetectionResult result = AudioModelDetectionResult.success(
                getDetectorName(),
                "Google Cloud Text-to-Speech",
                totalScore,
                Math.min(confidence, 100.0)
            );

            // 添加详细特征信息
            result.addFeature("rhythmRegularity", rhythmRegularity);
            result.addFeature("pitchNaturalness", pitchNaturalness);
            result.addFeature("emotionVariation", emotionVariation);
            result.addFeature("spectrumSmoothness", spectrumSmoothness);
            result.addFeature("noiseUniformity", noiseUniformity);
            result.addFeature("speechRateStability", speechRateStability);
            result.addFeature("breathSoundPresence", breathSoundPresence);
            result.addFeature("phonemeTransitionSmoothness", phonemeTransitionSmoothness);
            result.addFeature("detectionReason", buildDetectionReason(
                rhythmRegularity, pitchNaturalness, emotionVariation, 
                spectrumSmoothness, noiseUniformity, speechRateStability
            ));

            return result;

        } catch (Exception e) {
            return AudioModelDetectionResult.failure(getDetectorName(), 
                "Google TTS检测异常: " + e.getMessage());
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
    private String buildDetectionReason(double rhythmRegularity, double pitchNaturalness,
                                       double emotionVariation, double spectrumSmoothness,
                                       double noiseUniformity, double speechRateStability) {
        StringBuilder reason = new StringBuilder();
        
        if (rhythmRegularity >= 80 && rhythmRegularity <= 92) {
            reason.append("自然韵律节奏(").append(String.format("%.1f", rhythmRegularity)).append("); ");
        }
        
        if (pitchNaturalness >= 85 && pitchNaturalness <= 95) {
            reason.append("优秀音高控制(").append(String.format("%.1f", pitchNaturalness)).append("); ");
        }
        
        if (emotionVariation >= 60 && emotionVariation <= 80) {
            reason.append("中等偏上情感(").append(String.format("%.1f", emotionVariation)).append("); ");
        }
        
        if (spectrumSmoothness >= 83 && spectrumSmoothness <= 93) {
            reason.append("清晰频谱特征(").append(String.format("%.1f", spectrumSmoothness)).append("); ");
        }
        
        if (noiseUniformity > 87) {
            reason.append("较低噪声水平(").append(String.format("%.1f", noiseUniformity)).append("); ");
        }
        
        if (speechRateStability >= 72 && speechRateStability <= 88) {
            reason.append("自然语速变化(").append(String.format("%.1f", speechRateStability)).append("); ");
        }
        
        return reason.length() > 0 ? reason.toString() : "特征匹配度较低";
    }
}
