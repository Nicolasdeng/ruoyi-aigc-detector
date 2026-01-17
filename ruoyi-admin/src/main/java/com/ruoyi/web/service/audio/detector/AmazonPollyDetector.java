package com.ruoyi.web.service.audio.detector;

import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Amazon Polly AI音频模型检测器
 * 
 * Amazon Polly特征分析：
 * 1. 多语言支持 - 支持多种语言和方言，音色标准化程度高
 * 2. SSML标记优化 - 对SSML标记的处理非常精准，韵律控制优秀
 * 3. 神经网络音质 - 使用Neural TTS技术，音质清晰自然
 * 4. 标准化输出 - AWS服务的标准化特征，噪声控制优秀
 * 5. 语速控制 - 语速稳定性高，转换流畅
 * 
 * @author ruoyi
 */
@Component
public class AmazonPollyDetector implements IAudioAiModelDetector {

    private static final String DETECTOR_NAME = "Amazon Polly";
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

            // Amazon Polly特征评分
            double score = 0;
            int matchCount = 0;
            StringBuilder reason = new StringBuilder();

            // 1. 优秀韵律规律性（83-94分）- SSML标记优化带来的精准韵律控制
            if (rhythmRegularity >= 83 && rhythmRegularity <= 94) {
                score += 15;
                matchCount++;
                reason.append("✓ 韵律规律性符合Amazon Polly特征(").append(String.format("%.1f", rhythmRegularity)).append("分)\n");
            }

            // 2. 标准音色变化度（75-88分）- 多语言支持但保持标准化
            if (timbreVariation >= 75 && timbreVariation <= 88) {
                score += 13;
                matchCount++;
                reason.append("✓ 音色变化度符合Amazon Polly特征(").append(String.format("%.1f", timbreVariation)).append("分)\n");
            }

            // 3. 高质量频谱平滑度（86-96分）- Neural TTS技术的高音质输出
            if (spectrumSmoothness >= 86 && spectrumSmoothness <= 96) {
                score += 16;
                matchCount++;
                reason.append("✓ 频谱平滑度符合Amazon Polly特征(").append(String.format("%.1f", spectrumSmoothness)).append("分)\n");
            }

            // 4. 少量呼吸音（8-22分）- AWS标准化处理，呼吸音较少
            if (breathSoundPresence >= 8 && breathSoundPresence <= 22) {
                score += 14;
                matchCount++;
                reason.append("✓ 呼吸音存在度符合Amazon Polly特征(").append(String.format("%.1f", breathSoundPresence)).append("分)\n");
            }

            // 5. 中等情感表达（50-72分）- 标准化但有一定情感表现
            if (emotionVariation >= 50 && emotionVariation <= 72) {
                score += 12;
                matchCount++;
                reason.append("✓ 情感变化度符合Amazon Polly特征(").append(String.format("%.1f", emotionVariation)).append("分)\n");
            }

            // 6. 极低噪声水平（>88分）- AWS云服务的高质量音频处理
            if (noiseUniformity > 88) {
                score += 15;
                matchCount++;
                reason.append("✓ 噪声均匀性符合Amazon Polly特征(").append(String.format("%.1f", noiseUniformity)).append("分)\n");
            }

            // 7. 优秀发音转换（85-96分）- SSML优化的流畅转换
            if (phonemeTransitionSmoothness >= 85 && phonemeTransitionSmoothness <= 96) {
                score += 15;
                matchCount++;
                reason.append("✓ 音素转换平滑度符合Amazon Polly特征(").append(String.format("%.1f", phonemeTransitionSmoothness)).append("分)\n");
            }

            // 8. 稳定语速控制（80-92分）- 标准化语速处理
            if (speechRateStability >= 80 && speechRateStability <= 92) {
                score += 13;
                matchCount++;
                reason.append("✓ 语速稳定性符合Amazon Polly特征(").append(String.format("%.1f", speechRateStability)).append("分)\n");
            }

            // 9. 自然音高控制（82-94分）- Neural TTS的自然音高
            if (pitchNaturalness >= 82 && pitchNaturalness <= 94) {
                score += 14;
                matchCount++;
                reason.append("✓ 音高自然度符合Amazon Polly特征(").append(String.format("%.1f", pitchNaturalness)).append("分)\n");
            }

            // 综合评分（满分127分）
            double confidence = (score / 127.0) * 100;

            // 至少匹配5个特征且总分>70分才判定为Amazon Polly生成
            if (matchCount >= 5 && score > 70) {
                AudioModelDetectionResult result = AudioModelDetectionResult.success(
                        DETECTOR_NAME,
                        "Amazon Polly",
                        score,
                        confidence
                );

                result.addFeature("matchedFeatures", matchCount);
                result.addFeature("totalFeatures", 9);
                result.addFeature("detectionReason", reason.toString());
                result.addFeature("characteristic", "AWS Neural TTS，SSML优化，多语言支持，标准化高质量输出");

                return result;
            }

            // 不符合Amazon Polly特征
            return AudioModelDetectionResult.failure(
                    DETECTOR_NAME,
                    String.format("特征匹配度不足(匹配%d/9个特征，得分%.1f/127分)", matchCount, score)
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
