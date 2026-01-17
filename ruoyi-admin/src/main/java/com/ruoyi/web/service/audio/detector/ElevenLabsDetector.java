package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ElevenLabs AI音频检测器
 * 
 * 特征分析：
 * 1. 极高的音频质量和自然度
 * 2. 丰富的情感表达能力
 * 3. 自然的呼吸音和停顿
 * 4. 优秀的音色一致性
 * 5. 流畅的语速控制
 * 6. 较低的噪声水平
 * 
 * @author ruoyi
 */
@Component
public class ElevenLabsDetector implements IAudioAiModelDetector {

    @Override
    public String getDetectorName() {
        return "ElevenLabs AI";
    }

    @Override
    public double getWeight() {
        return 0.15;
    }

    @Override
    public AudioModelDetectionResult detect(String audioPath, Map<String, Object> audioFeatures) {
        try {
            // 提取关键特征
            double spectrumSmoothness = getFeatureValue(audioFeatures, "spectrumSmoothness");
            double timbreVariation = getFeatureValue(audioFeatures, "timbreVariation");
            double breathSoundPresence = getFeatureValue(audioFeatures, "breathSoundPresence");
            double noiseUniformity = getFeatureValue(audioFeatures, "noiseUniformity");
            double phonemeTransitionSmoothness = getFeatureValue(audioFeatures, "phonemeTransitionSmoothness");
            double emotionVariation = getFeatureValue(audioFeatures, "emotionVariation");
            double speechRateStability = getFeatureValue(audioFeatures, "speechRateStability");
            double pitchNaturalness = getFeatureValue(audioFeatures, "pitchNaturalness");

            double totalScore = 0.0;
            double confidence = 0.0;

            // 1. 频谱平滑度：90-98分（极高质量）
            if (spectrumSmoothness >= 90 && spectrumSmoothness <= 98) {
                totalScore += 30;
                confidence += 25;
            } else if (spectrumSmoothness >= 85 && spectrumSmoothness < 90) {
                totalScore += 15;
                confidence += 12;
            }

            // 2. 情感变化度：75-95分（丰富的情感表达）
            if (emotionVariation >= 75 && emotionVariation <= 95) {
                totalScore += 25;
                confidence += 22;
            } else if (emotionVariation >= 65 && emotionVariation < 75) {
                totalScore += 12;
                confidence += 10;
            }

            // 3. 呼吸音存在：20-40分（自然呼吸）
            if (breathSoundPresence >= 20 && breathSoundPresence <= 40) {
                totalScore += 20;
                confidence += 18;
            } else if (breathSoundPresence >= 10 && breathSoundPresence < 20) {
                totalScore += 10;
                confidence += 8;
            }

            // 4. 音色变化度：85-95分（优秀的音色一致性）
            if (timbreVariation >= 85 && timbreVariation <= 95) {
                totalScore += 15;
                confidence += 15;
            } else if (timbreVariation >= 75 && timbreVariation < 85) {
                totalScore += 8;
                confidence += 7;
            }

            // 5. 语速稳定性：80-92分（流畅的语速控制）
            if (speechRateStability >= 80 && speechRateStability <= 92) {
                totalScore += 15;
                confidence += 12;
            } else if (speechRateStability >= 70 && speechRateStability < 80) {
                totalScore += 7;
                confidence += 6;
            }

            // 6. 噪声均匀性：>88分（低噪声）
            if (noiseUniformity > 88) {
                totalScore += 12;
                confidence += 10;
            } else if (noiseUniformity > 80) {
                totalScore += 6;
                confidence += 5;
            }

            // 7. 音素转换平滑度：>85分
            if (phonemeTransitionSmoothness > 85) {
                totalScore += 10;
                confidence += 8;
            }

            // 8. 音高自然度：>82分
            if (pitchNaturalness > 82) {
                totalScore += 8;
                confidence += 6;
            }

            // 构建结果
            AudioModelDetectionResult result = AudioModelDetectionResult.success(
                getDetectorName(),
                "ElevenLabs TTS",
                totalScore,
                Math.min(confidence, 100.0)
            );

            // 添加详细特征信息
            result.addFeature("spectrumSmoothness", spectrumSmoothness);
            result.addFeature("emotionVariation", emotionVariation);
            result.addFeature("breathSoundPresence", breathSoundPresence);
            result.addFeature("timbreVariation", timbreVariation);
            result.addFeature("speechRateStability", speechRateStability);
            result.addFeature("noiseUniformity", noiseUniformity);
            result.addFeature("phonemeTransitionSmoothness", phonemeTransitionSmoothness);
            result.addFeature("pitchNaturalness", pitchNaturalness);
            result.addFeature("detectionReason", buildDetectionReason(
                spectrumSmoothness, emotionVariation, breathSoundPresence, 
                timbreVariation, speechRateStability, noiseUniformity
            ));

            return result;

        } catch (Exception e) {
            return AudioModelDetectionResult.failure(getDetectorName(), 
                "ElevenLabs检测异常: " + e.getMessage());
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
    private String buildDetectionReason(double spectrumSmoothness, double emotionVariation,
                                       double breathSoundPresence, double timbreVariation,
                                       double speechRateStability, double noiseUniformity) {
        StringBuilder reason = new StringBuilder();
        
        if (spectrumSmoothness >= 90 && spectrumSmoothness <= 98) {
            reason.append("极高的频谱质量(").append(String.format("%.1f", spectrumSmoothness)).append("); ");
        }
        
        if (emotionVariation >= 75 && emotionVariation <= 95) {
            reason.append("丰富的情感表达(").append(String.format("%.1f", emotionVariation)).append("); ");
        }
        
        if (breathSoundPresence >= 20 && breathSoundPresence <= 40) {
            reason.append("自然的呼吸音(").append(String.format("%.1f", breathSoundPresence)).append("); ");
        }
        
        if (timbreVariation >= 85 && timbreVariation <= 95) {
            reason.append("优秀的音色一致性(").append(String.format("%.1f", timbreVariation)).append("); ");
        }
        
        if (speechRateStability >= 80 && speechRateStability <= 92) {
            reason.append("流畅的语速控制(").append(String.format("%.1f", speechRateStability)).append("); ");
        }
        
        if (noiseUniformity > 88) {
            reason.append("低噪声水平(").append(String.format("%.1f", noiseUniformity)).append("); ");
        }
        
        return reason.length() > 0 ? reason.toString() : "特征匹配度较低";
    }
}
