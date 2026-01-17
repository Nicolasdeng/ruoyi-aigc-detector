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
 * 音频时域特征检测器
 * 分析音频的时域特征，检测AI生成痕迹
 * 
 * 主要检测特征：
 * 1. 零交叉率 (ZCR) - AI音频的ZCR分布通常过于规律
 * 2. 短时能量 (STE) - AI音频的能量变化范围较窄
 * 3. 静音段特征 - AI音频的静音段过于"干净"
 * 4. 能量包络 - AI音频的能量包络过于平滑
 * 
 * @author ruoyi
 */
@Component
public class AudioTimeFeatureDetector implements IAudioDetector {
    
    private static final Logger log = LoggerFactory.getLogger(AudioTimeFeatureDetector.class);
    
    private static final String DETECTOR_NAME = "时域特征检测器";
    
    // 帧长度（采样点数）
    private static final int FRAME_LENGTH = 512;
    
    // 帧移（采样点数）
    private static final int FRAME_SHIFT = 256;
    
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
            
            // 提取时域特征
            Map<String, Object> features = extractTimeFeatures(samples, format.getSampleRate());
            
            // 计算AI分数
            double aiScore = calculateAIScore(features);
            
            result.put("score", aiScore);
            result.put("isAI", aiScore > 0.6);
            result.put("confidence", Math.abs(aiScore - 0.5) * 2.0);
            result.put("features", features);
            
            log.debug("时域特征检测完成: {} - AI分数: {}", audioFilePath, aiScore);
            
        } catch (Exception e) {
            log.error("时域特征检测失败: " + audioFilePath, e);
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
            
            // 读取16位样本（小端序）
            if (bytesPerSample == 2) {
                short sample = (short) ((audioBytes[sampleIndex + 1] << 8) | (audioBytes[sampleIndex] & 0xFF));
                samples[i] = sample / 32768.0; // 归一化到[-1, 1]
            } else if (bytesPerSample == 1) {
                samples[i] = (audioBytes[sampleIndex] - 128) / 128.0;
            }
        }
        
        return samples;
    }
    
    /**
     * 提取时域特征
     */
    private Map<String, Object> extractTimeFeatures(double[] samples, float sampleRate) {
        Map<String, Object> features = new HashMap<>();
        
        // 1. 计算零交叉率 (ZCR)
        Map<String, Object> zcrFeatures = calculateZCR(samples);
        features.put("zeroCrossingRate", zcrFeatures);
        
        // 2. 计算短时能量 (STE)
        Map<String, Object> steFeatures = calculateSTE(samples);
        features.put("shortTimeEnergy", steFeatures);
        
        // 3. 分析静音段
        Map<String, Object> silenceFeatures = analyzeSilence(samples, sampleRate);
        features.put("silenceAnalysis", silenceFeatures);
        
        // 4. 计算能量包络
        Map<String, Object> envelopeFeatures = calculateEnergyEnvelope(samples);
        features.put("energyEnvelope", envelopeFeatures);
        
        return features;
    }
    
    /**
     * 计算零交叉率（Zero Crossing Rate）
     * AI音频的ZCR通常分布更规律，标准差较小
     */
    private Map<String, Object> calculateZCR(double[] samples) {
        int frameCount = (samples.length - FRAME_LENGTH) / FRAME_SHIFT + 1;
        double[] zcrValues = new double[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * FRAME_SHIFT;
            int end = Math.min(start + FRAME_LENGTH, samples.length);
            int zeroCrossings = 0;
            
            for (int j = start + 1; j < end; j++) {
                if ((samples[j] >= 0 && samples[j - 1] < 0) || (samples[j] < 0 && samples[j - 1] >= 0)) {
                    zeroCrossings++;
                }
            }
            
            zcrValues[i] = (double) zeroCrossings / (end - start);
        }
        
        // 计算统计特征
        double mean = Arrays.stream(zcrValues).average().orElse(0.0);
        double variance = Arrays.stream(zcrValues)
            .map(v -> Math.pow(v - mean, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // AI音频的ZCR标准差通常较小（过于规律）
        double regularity = 1.0 - Math.min(stdDev / 0.1, 1.0); // 标准差越小，规律性越高
        
        Map<String, Object> result = new HashMap<>();
        result.put("mean", mean);
        result.put("stdDev", stdDev);
        result.put("regularity", regularity);
        result.put("aiIndicator", regularity > 0.6); // 规律性过高表明可能是AI
        
        return result;
    }
    
    /**
     * 计算短时能量（Short-Time Energy）
     * AI音频的能量变化范围通常较窄
     */
    private Map<String, Object> calculateSTE(double[] samples) {
        int frameCount = (samples.length - FRAME_LENGTH) / FRAME_SHIFT + 1;
        double[] energyValues = new double[frameCount];
        
        for (int i = 0; i < frameCount; i++) {
            int start = i * FRAME_SHIFT;
            int end = Math.min(start + FRAME_LENGTH, samples.length);
            double energy = 0.0;
            
            for (int j = start; j < end; j++) {
                energy += samples[j] * samples[j];
            }
            
            energyValues[i] = energy / (end - start);
        }
        
        // 计算统计特征
        double mean = Arrays.stream(energyValues).average().orElse(0.0);
        double max = Arrays.stream(energyValues).max().orElse(0.0);
        double min = Arrays.stream(energyValues).min().orElse(0.0);
        double range = max - min;
        
        // AI音频的能量范围通常较窄（变化不够自然）
        double dynamicRange = range / (max + 1e-10);
        double narrowness = 1.0 - Math.min(dynamicRange, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("mean", mean);
        result.put("max", max);
        result.put("min", min);
        result.put("range", range);
        result.put("dynamicRange", dynamicRange);
        result.put("narrowness", narrowness);
        result.put("aiIndicator", narrowness > 0.5); // 动态范围窄表明可能是AI
        
        return result;
    }
    
    /**
     * 分析静音段特征
     * AI音频的静音段通常过于"干净"，缺少自然的背景噪声
     */
    private Map<String, Object> analyzeSilence(double[] samples, float sampleRate) {
        double silenceThreshold = 0.01; // 静音阈值
        List<Integer> silenceLengths = new ArrayList<>();
        int currentSilenceLength = 0;
        double totalSilenceSamples = 0;
        
        for (double sample : samples) {
            if (Math.abs(sample) < silenceThreshold) {
                currentSilenceLength++;
                totalSilenceSamples++;
            } else {
                if (currentSilenceLength > 0) {
                    silenceLengths.add(currentSilenceLength);
                    currentSilenceLength = 0;
                }
            }
        }
        
        if (currentSilenceLength > 0) {
            silenceLengths.add(currentSilenceLength);
        }
        
        double silenceRatio = totalSilenceSamples / samples.length;
        int silenceCount = silenceLengths.size();
        
        // 分析静音段的"纯净度"
        double averageSilenceAmplitude = 0.0;
        int silenceSampleCount = 0;
        
        for (double sample : samples) {
            if (Math.abs(sample) < silenceThreshold) {
                averageSilenceAmplitude += Math.abs(sample);
                silenceSampleCount++;
            }
        }
        
        if (silenceSampleCount > 0) {
            averageSilenceAmplitude /= silenceSampleCount;
        }
        
        // AI音频的静音段振幅通常接近0（过于干净）
        double silenceCleanliness = 1.0 - Math.min(averageSilenceAmplitude / silenceThreshold, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("silenceRatio", silenceRatio);
        result.put("silenceCount", silenceCount);
        result.put("averageSilenceAmplitude", averageSilenceAmplitude);
        result.put("silenceCleanliness", silenceCleanliness);
        result.put("aiIndicator", silenceCleanliness > 0.7); // 静音段过于干净表明可能是AI
        
        return result;
    }
    
    /**
     * 计算能量包络
     * AI音频的能量包络通常过于平滑
     */
    private Map<String, Object> calculateEnergyEnvelope(double[] samples) {
        int windowSize = 1024;
        int hopSize = 512;
        int envelopeLength = (samples.length - windowSize) / hopSize + 1;
        double[] envelope = new double[envelopeLength];
        
        for (int i = 0; i < envelopeLength; i++) {
            int start = i * hopSize;
            int end = Math.min(start + windowSize, samples.length);
            double maxAmplitude = 0.0;
            
            for (int j = start; j < end; j++) {
                maxAmplitude = Math.max(maxAmplitude, Math.abs(samples[j]));
            }
            
            envelope[i] = maxAmplitude;
        }
        
        // 计算包络的平滑度（一阶差分的平均绝对值）
        double[] diff = new double[envelope.length - 1];
        for (int i = 0; i < diff.length; i++) {
            diff[i] = Math.abs(envelope[i + 1] - envelope[i]);
        }
        
        double avgDiff = Arrays.stream(diff).average().orElse(0.0);
        double smoothness = 1.0 - Math.min(avgDiff / 0.1, 1.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("envelopeLength", envelope.length);
        result.put("avgDifference", avgDiff);
        result.put("smoothness", smoothness);
        result.put("aiIndicator", smoothness > 0.6); // 包络过于平滑表明可能是AI
        
        return result;
    }
    
    /**
     * 计算AI分数
     * 综合所有时域特征指标
     */
    private double calculateAIScore(Map<String, Object> features) {
        double score = 0.5; // 基础分数
        int indicatorCount = 0;
        int aiIndicatorCount = 0;
        
        // ZCR规律性
        @SuppressWarnings("unchecked")
        Map<String, Object> zcr = (Map<String, Object>) features.get("zeroCrossingRate");
        double zcrRegularity = (double) zcr.get("regularity");
        boolean zcrAI = (boolean) zcr.get("aiIndicator");
        score += zcrRegularity * 0.25;
        indicatorCount++;
        if (zcrAI) aiIndicatorCount++;
        
        // STE窄幅度
        @SuppressWarnings("unchecked")
        Map<String, Object> ste = (Map<String, Object>) features.get("shortTimeEnergy");
        double steNarrowness = (double) ste.get("narrowness");
        boolean steAI = (boolean) ste.get("aiIndicator");
        score += steNarrowness * 0.25;
        indicatorCount++;
        if (steAI) aiIndicatorCount++;
        
        // 静音段纯净度
        @SuppressWarnings("unchecked")
        Map<String, Object> silence = (Map<String, Object>) features.get("silenceAnalysis");
        double silenceCleanliness = (double) silence.get("silenceCleanliness");
        boolean silenceAI = (boolean) silence.get("aiIndicator");
        score += silenceCleanliness * 0.25;
        indicatorCount++;
        if (silenceAI) aiIndicatorCount++;
        
        // 能量包络平滑度
        @SuppressWarnings("unchecked")
        Map<String, Object> envelope = (Map<String, Object>) features.get("energyEnvelope");
        double envelopeSmoothness = (double) envelope.get("smoothness");
        boolean envelopeAI = (boolean) envelope.get("aiIndicator");
        score += envelopeSmoothness * 0.25;
        indicatorCount++;
        if (envelopeAI) aiIndicatorCount++;
        
        // 如果大多数指标都指向AI，提高分数
        if (aiIndicatorCount >= 3) {
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
                return 0.20;
            case "standard":
                return 0.15;
            case "deep":
                return 0.15;
            default:
                return 0.15;
        }
    }
    
    @Override
    public boolean isEnabledForMode(String mode) {
        // 所有模式都启用时域特征检测
        return true;
    }
}
