package com.ruoyi.web.service.audio.detector.impl;

import com.ruoyi.web.service.audio.detector.IAudioDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.util.*;

/**
 * 音频频域特征检测器
 * 分析音频的频域特征，检测AI生成痕迹
 * 
 * 主要检测特征：
 * 1. 频谱包络平滑度 - AI音频频谱过于光滑
 * 2. 高频成分分析 - AI音频高频衰减不自然
 * 3. 频谱质心变化 - AI音频频谱质心变化单一
 * 4. 谐波结构 - AI音频谐波过于规整
 * 
 * @author ruoyi
 */
@Component
public class AudioFreqFeatureDetector implements IAudioDetector {
    
    private static final Logger log = LoggerFactory.getLogger(AudioFreqFeatureDetector.class);
    
    private static final String DETECTOR_NAME = "频域特征检测器";
    
    @Override
    public Map<String, Object> detect(String audioFilePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("detectorName", DETECTOR_NAME);
        
        try {
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                throw new Exception("音频文件不存在: " + audioFilePath);
            }
            
            // 读取音频数据
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioInputStream.getFormat();
            
            // 转换为PCM格式（如果需要）
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
                );
                audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
                format = targetFormat;
            }
            
            // 读取音频样本
            byte[] audioBytes = audioInputStream.readAllBytes();
            double[] samples = convertBytesToSamples(audioBytes, format);
            audioInputStream.close();
            
            // 提取频域特征
            Map<String, Object> features = extractFreqFeatures(samples, format.getSampleRate());
            
            // 计算AI分数
            double aiScore = calculateAIScore(features);
            
            result.put("score", aiScore);
            result.put("isAI", aiScore > 0.6);
            result.put("confidence", Math.abs(aiScore - 0.5) * 2.0);
            result.put("features", features);
            
            log.debug("频域特征检测完成: {} - AI分数: {}", audioFilePath, aiScore);
            
        } catch (Exception e) {
            log.error("频域特征检测失败: " + audioFilePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("confidence", 0.0);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 将字节数组转换为样本数组
     */
    private double[] convertBytesToSamples(byte[] audioBytes, AudioFormat format) {
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        int numSamples = audioBytes.length / bytesPerSample / format.getChannels();
        double[] samples = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            int sampleIndex = i * bytesPerSample * format.getChannels();
            
            if (bytesPerSample == 2) {
                short sample = (short) ((audioBytes[sampleIndex + 1] << 8) | (audioBytes[sampleIndex] & 0xFF));
                samples[i] = sample / 32768.0;
            } else if (bytesPerSample == 1) {
                samples[i] = (audioBytes[sampleIndex] - 128) / 128.0;
            }
        }
        
        return samples;
    }
    
    /**
     * 提取频域特征（简化版本，使用统计分析替代FFT）
     */
    private Map<String, Object> extractFreqFeatures(double[] samples, float sampleRate) {
        Map<String, Object> features = new HashMap<>();
        
        // 1. 分析高频成分（通过高通滤波后的能量）
        Map<String, Object> highFreqFeatures = analyzeHighFrequency(samples);
        features.put("highFrequency", highFreqFeatures);
        
        // 2. 分析频谱平滑度（通过自相关）
        Map<String, Object> smoothnessFeatures = analyzeSpectralSmoothness(samples);
        features.put("spectralSmoothness", smoothnessFeatures);
        
        // 3. 分析谐波结构（通过周期性检测）
        Map<String, Object> harmonicFeatures = analyzeHarmonicStructure(samples);
        features.put("harmonicStructure", harmonicFeatures);
        
        return features;
    }
    
    /**
     * 分析高频成分
     * AI音频的高频衰减模式通常不自然
     */
    private Map<String, Object> analyzeHighFrequency(double[] samples) {
        // 简单的高通滤波：计算一阶差分
        double highFreqEnergy = 0.0;
        double totalEnergy = 0.0;
        
        for (int i = 1; i < samples.length; i++) {
            double diff = samples[i] - samples[i - 1];
            highFreqEnergy += diff * diff;
            totalEnergy += samples[i] * samples[i];
        }
        
        double highFreqRatio = highFreqEnergy / (totalEnergy + 1e-10);
        
        // AI音频的高频能量比例通常异常（过高或过低）
        double normalRange = 0.3; // 正常范围中心
        double deviation = Math.abs(highFreqRatio - normalRange);
        double abnormality = Math.min(deviation / normalRange, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("highFreqRatio", highFreqRatio);
        result.put("abnormality", abnormality);
        result.put("aiIndicator", abnormality > 0.4);
        
        return result;
    }
    
    /**
     * 分析频谱平滑度
     * AI音频的频谱通常过于平滑
     */
    private Map<String, Object> analyzeSpectralSmoothness(double[] samples) {
        int frameSize = 2048;
        int hopSize = 1024;
        int frameCount = (samples.length - frameSize) / hopSize + 1;
        
        double totalVariation = 0.0;
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * hopSize;
            int end = Math.min(start + frameSize, samples.length);
            
            // 计算帧内的变化率
            double frameVariation = 0.0;
            for (int j = start + 1; j < end; j++) {
                frameVariation += Math.abs(samples[j] - samples[j - 1]);
            }
            
            totalVariation += frameVariation / (end - start);
        }
        
        double avgVariation = totalVariation / frameCount;
        double smoothness = 1.0 - Math.min(avgVariation / 0.5, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("avgVariation", avgVariation);
        result.put("smoothness", smoothness);
        result.put("aiIndicator", smoothness > 0.65);
        
        return result;
    }
    
    /**
     * 分析谐波结构
     * AI音频的谐波通常过于规整
     */
    private Map<String, Object> analyzeHarmonicStructure(double[] samples) {
        // 使用自相关分析周期性
        int maxLag = Math.min(1000, samples.length / 4);
        double[] autocorr = new double[maxLag];
        
        for (int lag = 0; lag < maxLag; lag++) {
            double sum = 0.0;
            for (int i = 0; i < samples.length - lag; i++) {
                sum += samples[i] * samples[i + lag];
            }
            autocorr[lag] = sum / (samples.length - lag);
        }
        
        // 归一化
        double maxAutocorr = autocorr[0];
        for (int i = 0; i < maxLag; i++) {
            autocorr[i] /= maxAutocorr;
        }
        
        // 寻找峰值（谐波）
        int peakCount = 0;
        for (int i = 1; i < maxLag - 1; i++) {
            if (autocorr[i] > autocorr[i - 1] && autocorr[i] > autocorr[i + 1] && autocorr[i] > 0.3) {
                peakCount++;
            }
        }
        
        // AI音频的谐波峰值通常更规律
        double harmonicRegularity = Math.min(peakCount / 10.0, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("peakCount", peakCount);
        result.put("harmonicRegularity", harmonicRegularity);
        result.put("aiIndicator", harmonicRegularity > 0.6);
        
        return result;
    }
    
    /**
     * 计算AI分数
     */
    private double calculateAIScore(Map<String, Object> features) {
        double score = 0.5;
        int indicatorCount = 0;
        int aiIndicatorCount = 0;
        
        // 高频异常
        @SuppressWarnings("unchecked")
        Map<String, Object> highFreq = (Map<String, Object>) features.get("highFrequency");
        double highFreqAbnormality = (double) highFreq.get("abnormality");
        boolean highFreqAI = (boolean) highFreq.get("aiIndicator");
        score += highFreqAbnormality * 0.35;
        indicatorCount++;
        if (highFreqAI) aiIndicatorCount++;
        
        // 频谱平滑度
        @SuppressWarnings("unchecked")
        Map<String, Object> smoothness = (Map<String, Object>) features.get("spectralSmoothness");
        double spectralSmoothness = (double) smoothness.get("smoothness");
        boolean smoothnessAI = (boolean) smoothness.get("aiIndicator");
        score += spectralSmoothness * 0.35;
        indicatorCount++;
        if (smoothnessAI) aiIndicatorCount++;
        
        // 谐波规律性
        @SuppressWarnings("unchecked")
        Map<String, Object> harmonic = (Map<String, Object>) features.get("harmonicStructure");
        double harmonicRegularity = (double) harmonic.get("harmonicRegularity");
        boolean harmonicAI = (boolean) harmonic.get("aiIndicator");
        score += harmonicRegularity * 0.30;
        indicatorCount++;
        if (harmonicAI) aiIndicatorCount++;
        
        // 如果大多数指标都指向AI，提高分数
        if (aiIndicatorCount >= 2) {
            score = Math.min(score + 0.1, 0.95);
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public String getDetectorName() {
        return DETECTOR_NAME;
    }
    
    @Override
    public double getWeight(String mode) {
        switch (mode.toLowerCase()) {
            case "fast":
                return 0.25;
            case "standard":
                return 0.20;
            case "deep":
                return 0.15;
            default:
                return 0.20;
        }
    }
    
    @Override
    public boolean isEnabledForMode(String mode) {
        return true;
    }
}
