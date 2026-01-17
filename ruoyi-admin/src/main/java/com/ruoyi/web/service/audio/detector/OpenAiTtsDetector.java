package com.ruoyi.web.service.audio.detector;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OpenAI TTS检测器
 * 特征：高质量语音合成、自然的韵律、丰富的音色变化
 *
 * @author ruoyi
 */
@Component
@Slf4j
public class OpenAiTtsDetector implements IAudioAiModelDetector {
    
    //private static final Logger log = LoggerFactory.getLogger(OpenAiTtsDetector.java);


    
    @Override
    public String getDetectorName() {
        return "OpenAI TTS";
    }
    
    @Override
    public double getWeight() {
        return 0.15;
    }
    
    @Override
    public AudioModelDetectionResult detect(String audioPath, Map<String, Object> audioFeatures) {
        try {
            double score = 0.0;
            double confidence = 0.0;
            
            // 1. 检查频谱平滑度（OpenAI TTS特征：85-95分）
            if (audioFeatures.containsKey("spectrumSmoothness")) {
                double smoothness = (Double) audioFeatures.get("spectrumSmoothness");
                if (smoothness >= 85 && smoothness <= 95) {
                    score += 25;
                    confidence += 20;
                }
            }
            
            // 2. 检查音色变化度（OpenAI特征：丰富多样 70-90分）
            if (audioFeatures.containsKey("timbreVariation")) {
                double variation = (Double) audioFeatures.get("timbreVariation");
                if (variation >= 70 && variation <= 90) {
                    score += 20;
                    confidence += 18;
                }
            }
            
            // 3. 检查呼吸音存在度（OpenAI特征：几乎没有 <15分）
            if (audioFeatures.containsKey("breathSoundPresence")) {
                double breath = (Double) audioFeatures.get("breathSoundPresence");
                if (breath < 15) {
                    score += 15;
                    confidence += 15;
                }
            }
            
            // 4. 检查噪声均匀性（OpenAI特征：极高 >85分）
            if (audioFeatures.containsKey("noiseUniformity")) {
                double uniformity = (Double) audioFeatures.get("noiseUniformity");
                if (uniformity > 85) {
                    score += 15;
                    confidence += 12;
                }
            }
            
            // 5. 检查音素转换平滑度（OpenAI特征：非常平滑 >80分）
            if (audioFeatures.containsKey("phonemeTransitionSmoothness")) {
                double smoothness = (Double) audioFeatures.get("phonemeTransitionSmoothness");
                if (smoothness > 80) {
                    score += 15;
                    confidence += 15;
                }
            }
            
            // 6. 检查情感变化度（OpenAI特征：适中 50-70分）
            if (audioFeatures.containsKey("emotionVariation")) {
                double emotion = (Double) audioFeatures.get("emotionVariation");
                if (emotion >= 50 && emotion <= 70) {
                    score += 10;
                    confidence += 10;
                }
            }
            
            // 综合评分
            confidence = Math.min(90, confidence);
            
            AudioModelDetectionResult result = AudioModelDetectionResult.success(
                getDetectorName(),
                "OpenAI TTS",
                score,
                confidence
            );
            
            // 添加特征证据
            result.addFeature("spectrumSmoothness", audioFeatures.get("spectrumSmoothness"));
            result.addFeature("timbreVariation", audioFeatures.get("timbreVariation"));
            result.addFeature("breathSoundPresence", audioFeatures.get("breathSoundPresence"));
            
            return result;
            
        } catch (Exception e) {
            log.error("OpenAI TTS检测失败: {}", e.getMessage());
            return AudioModelDetectionResult.failure(getDetectorName(), e.getMessage());
        }
    }
}
