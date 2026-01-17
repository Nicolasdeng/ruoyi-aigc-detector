package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Resemble AI音频模型检测器
 * 
 * Resemble AI特征分析：
 * 1. 声音克隆技术 - 能够高度还原特定人声，音色相似度极高
 * 2. 高度定制化 - 支持情感、语速、音高的精细调节
 * 3. 实时语音合成 - 低延迟实时生成，但保持高质量
 * 4. 情感迁移 - 能够在保持音色的同时改变情感表达
 * 5. 自然度优化 - 注重自然度和真实感，呼吸音和停顿处理细腻
 * 
 * @author ruoyi
 */
@Component
public class ResembleAiDetector implements IAudioAiModelDetector {

    private static final String DETECTOR_NAME = "Resemble AI";
    private static final double WEIGHT = 0.10;

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
            // 提取8维音频特征
            double rhythmRegularity = getFeatureValue(audioFeatures, "rhythmRegularity");
            double timbreVariation = getFeatureValue(audioFeatures, "timbreVariation");
            double spectrumSmoothness = getFeatureValue(audioFeatures, "spectrumSmoothness");
            double breathSoundPresence = getFeatureValue(audioFeatures, "breathSoundPresence");
            double emotionVariation = getFeatureValue(audioFeatures, "emotionVariation");
            double noiseUniformity = getFeatureValue(audioFeatures, "noiseUniformity");
            double phonemeTransitionSmoothness = getFeatureValue(audioFeatures, "phonemeTransitionSmoothness");
            double speechRateStability = getFeatureValue(audioFeatures, "speechRateStability");
            double pitchNaturalness = getFeatureValue(audioFeatures, "pitchNaturalness");

            // Resemble AI特征评分
            double score = 0;
            int matchCount = 0;
            StringBuilder reason = new StringBuilder();

            // 1. 中高韵律规律性（78-92分）- 声音克隆保持原声韵律特征
            if (rhythmRegularity >= 78 && rhythmRegularity <= 92) {
                score += 14;
                matchCount++;
                reason.append("✓ 韵律规律性符合Resemble AI特征(").append(String.format("%.1f", rhythmRegularity)).append("分)\n");
            }

            // 2. 高音色还原度（82-95分）- 声音克隆技术的高相似度
            if (timbreVariation >= 82 && timbreVariation <= 95) {
                score += 16;
                matchCount++;
                reason.append("✓ 音色变化度符合Resemble AI特征(").append(String.format("%.1f", timbreVariation)).append("分)\n");
            }

            // 3. 优秀频谱质量（84-95分）- 高质量实时合成
            if (spectrumSmoothness >= 84 && spectrumSmoothness <= 95) {
                score += 15;
                matchCount++;
                reason.append("✓ 频谱平滑度符合Resemble AI特征(").append(String.format("%.1f", spectrumSmoothness)).append("分)\n");
            }

            // 4. 适度呼吸音（18-35分）- 注重自然度，保留适当呼吸感
            if (breathSoundPresence >= 18 && breathSoundPresence <= 35) {
                score += 13;
                matchCount++;
                reason.append("✓ 呼吸音存在度符合Resemble AI特征(").append(String.format("%.1f", breathSoundPresence)).append("分)\n");
            }

            // 5. 丰富情感表达（70-92分）- 情感迁移技术带来的高情感表现力
            if (emotionVariation >= 70 && emotionVariation <= 92) {
                score += 16;
                matchCount++;
                reason.append("✓ 情感变化度符合Resemble AI特征(").append(String.format("%.1f", emotionVariation)).append("分)\n");
            }

            // 6. 低噪声水平（>86分）- 高质量合成但保持自然感
            if (noiseUniformity > 86) {
                score += 14;
                matchCount++;
                reason.append("✓ 噪声均匀性符合Resemble AI特征(").append(String.format("%.1f", noiseUniformity)).append("分)\n");
            }

            // 7. 自然发音转换（82-94分）- 精细化调节的流畅转换
            if (phonemeTransitionSmoothness >= 82 && phonemeTransitionSmoothness <= 94) {
                score += 14;
                matchCount++;
                reason.append("✓ 音素转换平滑度符合Resemble AI特征(").append(String.format("%.1f", phonemeTransitionSmoothness)).append("分)\n");
            }

            // 8. 灵活语速控制（75-90分）- 高度定制化的语速调节
            if (speechRateStability >= 75 && speechRateStability <= 90) {
                score += 13;
                matchCount++;
                reason.append("✓ 语速稳定性符合Resemble AI特征(").append(String.format("%.1f", speechRateStability)).append("分)\n");
            }

            // 9. 高度自然音高（80-94分）- 克隆原声的自然音高特征
            if (pitchNaturalness >= 80 && pitchNaturalness <= 94) {
                score += 15;
                matchCount++;
                reason.append("✓ 音高自然度符合Resemble AI特征(").append(String.format("%.1f", pitchNaturalness)).append("分)\n");
            }

            // 综合评分（满分130分）
            double confidence = (score / 130.0) * 100;

            // 至少匹配5个特征且总分>70分才判定为Resemble AI生成
            if (matchCount >= 5 && score > 70) {
                AudioModelDetectionResult result = AudioModelDetectionResult.success(
                        DETECTOR_NAME,
                        "Resemble AI",
                        score,
                        confidence
                );

                result.addFeature("matchedFeatures", matchCount);
                result.addFeature("totalFeatures", 9);
                result.addFeature("detectionReason", reason.toString());
                result.addFeature("characteristic", "声音克隆技术，情感迁移，高度定制化，实时合成，注重自然度");

                return result;
            }

            // 不符合Resemble AI特征
            return AudioModelDetectionResult.failure(
                    DETECTOR_NAME,
                    String.format("特征匹配度不足(匹配%d/9个特征，得分%.1f/130分)", matchCount, score)
            );

        } catch (Exception e) {
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
}
