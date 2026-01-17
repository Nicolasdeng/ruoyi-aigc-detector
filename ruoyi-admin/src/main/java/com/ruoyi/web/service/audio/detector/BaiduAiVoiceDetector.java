package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 百度AI语音检测器
 * 
 * 百度AI语音(Baidu AI Voice)特征分析：
 * 1. 中文优化：针对中文语音优化，发音准确清晰
 * 2. 自然停顿：停顿节奏自然，符合中文表达习惯
 * 3. 标准音色：音色标准稳定，适合多种应用场景
 * 4. 清晰发音：发音清晰，音素转换流畅
 * 5. 稳定韵律：韵律节奏稳定，适合长文本合成
 * 6. 情感适中：情感表达适中，不过度夸张
 * 
 * 权重：0.13（13%）
 * 
 * @author ruoyi
 */
@Component
public class BaiduAiVoiceDetector implements IAudioAiModelDetector {
    
    private static final Logger log = LoggerFactory.getLogger(BaiduAiVoiceDetector.class);
    
    private static final String DETECTOR_NAME = "百度AI语音";
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
            
            // 百度AI语音特征评分
            double score = 0.0;
            int matchCount = 0;
            
            // 1. 稳定韵律节奏（82-94分）- 中文优化的韵律控制
            if (rhythmRegularity >= 82 && rhythmRegularity <= 94) {
                score += 18;
                matchCount++;
            }
            
            // 2. 标准音色稳定性（78-88分）- 音色标准不过度变化
            if (timbreVariation >= 78 && timbreVariation <= 88) {
                score += 16;
                matchCount++;
            }
            
            // 3. 清晰频谱特征（85-95分）- 频谱清晰度高
            if (spectrumSmoothness >= 85 && spectrumSmoothness <= 95) {
                score += 17;
                matchCount++;
            }
            
            // 4. 自然停顿表现（20-35分）- 符合中文表达的停顿
            if (breathSoundPresence >= 20 && breathSoundPresence <= 35) {
                score += 16;
                matchCount++;
            }
            
            // 5. 适中情感表达（50-70分）- 情感表达适度
            if (emotionVariation >= 50 && emotionVariation <= 70) {
                score += 15;
                matchCount++;
            }
            
            // 6. 较低噪声水平（>88分）- 音质清晰
            if (noiseUniformity > 88) {
                score += 17;
                matchCount++;
            }
            
            // 7. 流畅发音转换（86-96分）- 音素转换清晰流畅
            if (phonemeTransitionSmoothness >= 86 && phonemeTransitionSmoothness <= 96) {
                score += 18;
                matchCount++;
            }
            
            // 8. 稳定语速控制（80-92分）- 语速控制稳定
            if (speechRateStability >= 80 && speechRateStability <= 92) {
                score += 16;
                matchCount++;
            }
            
            // 综合评分（满分133分）
            double confidence = (score / 133.0) * 100;
            
            // 判断是否为百度AI语音生成
            // 至少匹配5个特征且总分>70分认为可能是百度AI语音
            if (matchCount >= 5 && score > 70) {
                String detectionReason = buildDetectionReason(
                    rhythmRegularity, timbreVariation, spectrumSmoothness,
                    breathSoundPresence, emotionVariation, noiseUniformity,
                    phonemeTransitionSmoothness, speechRateStability, matchCount, score
                );
                
                AudioModelDetectionResult result = AudioModelDetectionResult.success(
                    DETECTOR_NAME,
                    "百度AI语音",
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
            
            return AudioModelDetectionResult.failure(DETECTOR_NAME, "特征不匹配百度AI语音模式");
            
        } catch (Exception e) {
            log.error("百度AI语音检测失败: {}", e.getMessage(), e);
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
        reason.append("检测到").append(matchCount).append("个百度AI语音特征匹配，总分").append(String.format("%.1f", score)).append("/133：");
        
        if (rhythm >= 82 && rhythm <= 94) {
            reason.append("\n• 稳定韵律节奏(").append(String.format("%.1f", rhythm)).append("分) - 中文优化的韵律控制");
        }
        if (timbre >= 78 && timbre <= 88) {
            reason.append("\n• 标准音色稳定性(").append(String.format("%.1f", timbre)).append("分) - 音色标准不过度变化");
        }
        if (spectrum >= 85 && spectrum <= 95) {
            reason.append("\n• 清晰频谱特征(").append(String.format("%.1f", spectrum)).append("分) - 频谱清晰度高");
        }
        if (breath >= 20 && breath <= 35) {
            reason.append("\n• 自然停顿表现(").append(String.format("%.1f", breath)).append("分) - 符合中文表达的停顿");
        }
        if (emotion >= 50 && emotion <= 70) {
            reason.append("\n• 适中情感表达(").append(String.format("%.1f", emotion)).append("分) - 情感表达适度");
        }
        if (noise > 88) {
            reason.append("\n• 较低噪声水平(").append(String.format("%.1f", noise)).append("分) - 音质清晰");
        }
        if (phoneme >= 86 && phoneme <= 96) {
            reason.append("\n• 流畅发音转换(").append(String.format("%.1f", phoneme)).append("分) - 音素转换清晰流畅");
        }
        if (speech >= 80 && speech <= 92) {
            reason.append("\n• 稳定语速控制(").append(String.format("%.1f", speech)).append("分) - 语速控制稳定");
        }
        
        return reason.toString();
    }
}
