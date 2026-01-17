package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 科大讯飞语音检测器
 * 
 * 科大讯飞(iFlytek)特征分析：
 * 1. 方言支持：支持多种方言和地方口音
 * 2. 清晰发音：发音准确清晰，音素转换自然
 * 3. 稳定韵律：韵律节奏稳定，适合长时间播报
 * 4. 企业级品质：音质稳定，适合商业应用
 * 5. 自然语速：语速控制自然流畅
 * 6. 适度情感：情感表达适中，符合中文习惯
 * 
 * 权重：0.13（13%）
 * 
 * @author ruoyi
 */
@Component
public class IFlyTekDetector implements IAudioAiModelDetector {
    
    private static final Logger log = LoggerFactory.getLogger(IFlyTekDetector.class);
    
    private static final String DETECTOR_NAME = "科大讯飞";
    private static final double WEIGHT = 0.13;
    
    @Override
    public String getDetectorName() {
        return DETECTOR_NAME;
    }
    
    @Override
    public double getWeight() {
        return WEIGHT;
    }
    
    @Override
    public AudioModelDetectionResult detect(String audioPath, Map<String, Object> audioFeatures) {
        try {
            // 提取关键特征
            double rhythmRegularity = getFeatureValue(audioFeatures, "rhythmRegularity");
            double timbreVariation = getFeatureValue(audioFeatures, "timbreVariation");
            double spectrumSmoothness = getFeatureValue(audioFeatures, "spectrumSmoothness");
            double breathSoundPresence = getFeatureValue(audioFeatures, "breathSoundPresence");
            double emotionVariation = getFeatureValue(audioFeatures, "emotionVariation");
            double noiseUniformity = getFeatureValue(audioFeatures, "noiseUniformity");
            double phonemeTransitionSmoothness = getFeatureValue(audioFeatures, "phonemeTransitionSmoothness");
            double speechRateStability = getFeatureValue(audioFeatures, "speechRateStability");
            
            // 科大讯飞特征评分
            double score = 0.0;
            int matchCount = 0;
            
            // 1. 极高韵律稳定性（85-96分）- 企业级稳定韵律
            if (rhythmRegularity >= 85 && rhythmRegularity <= 96) {
                score += 18;
                matchCount++;
            }
            
            // 2. 标准音色一致性（80-90分）- 音色标准统一
            if (timbreVariation >= 80 && timbreVariation <= 90) {
                score += 17;
                matchCount++;
            }
            
            // 3. 优秀频谱质量（88-97分）- 高质量频谱特征
            if (spectrumSmoothness >= 88 && spectrumSmoothness <= 97) {
                score += 18;
                matchCount++;
            }
            
            // 4. 少量呼吸音（10-25分）- 轻微自然呼吸感
            if (breathSoundPresence >= 10 && breathSoundPresence <= 25) {
                score += 16;
                matchCount++;
            }
            
            // 5. 适中情感表达（55-75分）- 情感表达适度自然
            if (emotionVariation >= 55 && emotionVariation <= 75) {
                score += 16;
                matchCount++;
            }
            
            // 6. 极低噪声水平（>90分）- 企业级音质纯净
            if (noiseUniformity > 90) {
                score += 18;
                matchCount++;
            }
            
            // 7. 超清晰发音转换（88-98分）- 音素转换极其流畅
            if (phonemeTransitionSmoothness >= 88 && phonemeTransitionSmoothness <= 98) {
                score += 19;
                matchCount++;
            }
            
            // 8. 自然语速控制（82-94分）- 语速控制自然流畅
            if (speechRateStability >= 82 && speechRateStability <= 94) {
                score += 17;
                matchCount++;
            }
            
            // 综合评分（满分139分）
            double confidence = (score / 139.0) * 100;
            
            // 判断是否为科大讯飞生成
            // 至少匹配5个特征且总分>75分认为可能是科大讯飞
            if (matchCount >= 5 && score > 75) {
                String detectionReason = buildDetectionReason(
                    rhythmRegularity, timbreVariation, spectrumSmoothness,
                    breathSoundPresence, emotionVariation, noiseUniformity,
                    phonemeTransitionSmoothness, speechRateStability, matchCount, score
                );
                
                AudioModelDetectionResult result = AudioModelDetectionResult.success(
                    DETECTOR_NAME,
                    "科大讯飞",
                    score,
                    confidence
                );
                
                // 添加详细特征信息
                result.addFeature("rhythmRegularity", rhythmRegularity);
                result.addFeature("timbreVariation", timbreVariation);
                result.addFeature("spectrumSmoothness", spectrumSmoothness);
                result.addFeature("breathSoundPresence", breathSoundPresence);
                result.addFeature("emotionVariation", emotionVariation);
                result.addFeature("noiseUniformity", noiseUniformity);
                result.addFeature("phonemeTransitionSmoothness", phonemeTransitionSmoothness);
                result.addFeature("speechRateStability", speechRateStability);
                result.addFeature("matchCount", matchCount);
                result.addFeature("detectionReason", detectionReason);
                
                return result;
            }
            
            return AudioModelDetectionResult.failure(DETECTOR_NAME, "特征不匹配科大讯飞模式");
            
        } catch (Exception e) {
            log.error("科大讯飞检测失败: {}", e.getMessage(), e);
            return AudioModelDetectionResult.failure(DETECTOR_NAME, "检测过程出错: " + e.getMessage());
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
    private String buildDetectionReason(
            double rhythm, double timbre, double spectrum,
            double breath, double emotion, double noise,
            double phoneme, double speech, int matchCount, double score) {
        
        StringBuilder reason = new StringBuilder();
        reason.append("检测到").append(matchCount).append("个科大讯飞特征匹配，总分").append(String.format("%.1f", score)).append("/139：");
        
        if (rhythm >= 85 && rhythm <= 96) {
            reason.append("\n• 极高韵律稳定性(").append(String.format("%.1f", rhythm)).append("分) - 企业级稳定韵律");
        }
        if (timbre >= 80 && timbre <= 90) {
            reason.append("\n• 标准音色一致性(").append(String.format("%.1f", timbre)).append("分) - 音色标准统一");
        }
        if (spectrum >= 88 && spectrum <= 97) {
            reason.append("\n• 优秀频谱质量(").append(String.format("%.1f", spectrum)).append("分) - 高质量频谱特征");
        }
        if (breath >= 10 && breath <= 25) {
            reason.append("\n• 少量呼吸音(").append(String.format("%.1f", breath)).append("分) - 轻微自然呼吸感");
        }
        if (emotion >= 55 && emotion <= 75) {
            reason.append("\n• 适中情感表达(").append(String.format("%.1f", emotion)).append("分) - 情感表达适度自然");
        }
        if (noise > 90) {
            reason.append("\n• 极低噪声水平(").append(String.format("%.1f", noise)).append("分) - 企业级音质纯净");
        }
        if (phoneme >= 88 && phoneme <= 98) {
            reason.append("\n• 超清晰发音转换(").append(String.format("%.1f", phoneme)).append("分) - 音素转换极其流畅");
        }
        if (speech >= 82 && speech <= 94) {
            reason.append("\n• 自然语速控制(").append(String.format("%.1f", speech)).append("分) - 语速控制自然流畅");
        }
        
        return reason.toString();
    }
}
