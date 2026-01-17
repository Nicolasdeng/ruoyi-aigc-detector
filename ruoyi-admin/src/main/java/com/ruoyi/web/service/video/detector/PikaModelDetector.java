package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoDetectionUtils;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Pika Labs视频生成模型检测器
 * 
 * 特征分析：
 * 1. 运动夸张：运动幅度大，变化明显
 * 2. 转场效果：转场明显，风格化强
 * 3. 细节略模糊：细节不如Runway精细
 * 4. 色彩鲜艳：饱和度高，对比度强
 * 5. 风格化强：艺术化风格明显
 * 
 * @author ruoyi
 */
@Component
public class PikaModelDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Pika Labs";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        return detectModel(frames, null);
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> videoMetadata) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, String> features = new HashMap<>();
        
        // 1. 时间一致性分析 (权重15%)
        double temporalScore = featureAnalyzer.analyzeTemporalConsistency(videoFrames);
        scores.put("temporal_consistency", temporalScore);
        features.put("temporal_consistency", String.format("时间一致性: %.2f分 - %s", 
            temporalScore, getTemporalDescription(temporalScore)));
        
        // 2. 运动特征分析 (权重30%) - Pika运动幅度大
        double motionScore = featureAnalyzer.analyzeMotionPatterns(videoFrames);
        scores.put("motion_patterns", motionScore);
        features.put("motion_patterns", String.format("运动特征: %.2f分 - %s", 
            motionScore, getMotionDescription(motionScore)));
        
        // 3. 细节质量分析 (权重15%) - Pika细节略模糊
        double detailScore = featureAnalyzer.analyzeDetailQuality(videoFrames);
        scores.put("detail_quality", detailScore);
        features.put("detail_quality", String.format("细节质量: %.2f分 - %s", 
            detailScore, getDetailDescription(detailScore)));
        
        // 4. 色彩风格分析 (权重20%) - Pika色彩鲜艳
        double colorScore = featureAnalyzer.analyzeColorStyle(videoFrames);
        scores.put("color_style", colorScore);
        features.put("color_style", String.format("色彩风格: %.2f分 - %s", 
            colorScore, getColorDescription(colorScore)));
        
        // 5. AI指纹检测 (权重10%)
        Map<String, Object> fingerprintData = featureAnalyzer.detectAiFingerprint(videoFrames);
        double fingerprintScore = (Boolean) fingerprintData.getOrDefault("hasFingerprint", false) ? 80.0 : 20.0;
        scores.put("ai_fingerprint", fingerprintScore);
        features.put("ai_fingerprint", String.format("AI指纹: %.2f分 - %s", 
            fingerprintScore, getFingerprintDescription(fingerprintScore)));
        
        // 6. Pika特征检测 (权重10%)
        double pikaSignature = detectPikaSignature(videoFrames, scores);
        scores.put("pika_signature", pikaSignature);
        features.put("pika_signature", String.format("Pika特征: %.2f分 - %s", 
            pikaSignature, getPikaSignatureDescription(pikaSignature)));
        
        // 计算总置信度
        double confidence = calculateConfidence(scores);
        
        // 生成特征详情
        String featureDetails = generateFeatureDetails(scores, features);
        
        // 生成建议
        List<String> suggestions = generateSuggestionsFromScores(confidence, scores);
        
        // 组装结果
        VideoModelDetectionResult result = new VideoModelDetectionResult(
            getModelName(),
            confidence
        );
        result.setScores(scores);
        result.setSuggestions(suggestions);
        
        // 将特征详情转换为features列表
        for (Map.Entry<String, String> entry : features.entrySet()) {
            result.addFeature(entry.getValue());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> videoFrames) {
        VideoModelDetectionResult result = detectModel(videoFrames);
        return getFeatureDetails(result.getScores());
    }

    @Override
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> details = new HashMap<>();
        
        // 基础特征分数
        details.put("temporal_consistency", scores.getOrDefault("temporal_consistency", 0.0));
        details.put("motion_patterns", scores.getOrDefault("motion_patterns", 0.0));
        details.put("detail_quality", scores.getOrDefault("detail_quality", 0.0));
        details.put("color_style", scores.getOrDefault("color_style", 0.0));
        details.put("ai_fingerprint", scores.getOrDefault("ai_fingerprint", 0.0));
        details.put("pika_signature", scores.getOrDefault("pika_signature", 0.0));
        
        // 特征描述
        details.put("temporal_description", getTemporalDescription(scores.getOrDefault("temporal_consistency", 0.0)));
        details.put("motion_description", getMotionDescription(scores.getOrDefault("motion_patterns", 0.0)));
        details.put("detail_description", getDetailDescription(scores.getOrDefault("detail_quality", 0.0)));
        details.put("color_description", getColorDescription(scores.getOrDefault("color_style", 0.0)));
        details.put("fingerprint_description", getFingerprintDescription(scores.getOrDefault("ai_fingerprint", 0.0)));
        details.put("pika_description", getPikaSignatureDescription(scores.getOrDefault("pika_signature", 0.0)));
        
        // Pika核心特征标识
        double motionScore = scores.getOrDefault("motion_patterns", 0.0);
        double colorScore = scores.getOrDefault("color_style", 0.0);
        double pikaScore = scores.getOrDefault("pika_signature", 0.0);
        
        boolean isPikaLike = motionScore > 70 && colorScore > 75 && pikaScore > 60;
        details.put("is_pika_like", isPikaLike);
        details.put("pika_confidence", calculateConfidence(scores));
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        return generateSuggestionsFromScores(confidence, scores);
    }

    /**
     * 检测Pika Labs特有特征
     */
    private double detectPikaSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signature = 0.0;
        
        // 1. 运动夸张特征 (25分)
        // Pika运动幅度大，变化明显
        double motionScore = scores.getOrDefault("motion_patterns", 0.0);
        if (motionScore > 75) {  // 运动幅度大
            signature += 25;
        } else if (motionScore > 60) {
            signature += 15;
        }
        
        // 2. 转场风格化特征 (25分)
        // 分析帧间变化的风格化程度
        double styleTransition = analyzeStyleTransition(frames);
        if (styleTransition > 0.7) {  // 转场风格化强
            signature += 25;
        } else if (styleTransition > 0.5) {
            signature += 15;
        }
        
        // 3. 细节模糊特征 (25分)
        // Pika细节不如Runway精细
        double detailScore = scores.getOrDefault("detail_quality", 0.0);
        if (detailScore >= 50 && detailScore <= 70) {  // 中等细节
            signature += 25;
        } else if (detailScore >= 40 && detailScore < 50) {
            signature += 15;
        }
        
        // 4. 色彩鲜艳特征 (25分)
        // Pika色彩饱和度高
        double colorScore = scores.getOrDefault("color_style", 0.0);
        if (colorScore > 80) {  // 色彩鲜艳
            signature += 25;
        } else if (colorScore > 70) {
            signature += 15;
        }
        
        return signature;
    }

    /**
     * 分析转场风格化程度
     */
    private double analyzeStyleTransition(List<BufferedImage> frames) {
        if (frames.size() < 3) {
            return 0.0;
        }
        
        double totalStyle = 0.0;
        int transitionCount = 0;
        
        // 检测转场点
        for (int i = 1; i < frames.size() - 1; i++) {
            BufferedImage prev = frames.get(i - 1);
            BufferedImage curr = frames.get(i);
            BufferedImage next = frames.get(i + 1);
            
            // 计算前后帧差异
            double prevDiff = VideoDetectionUtils.calculateFrameDifference(prev, curr);
            double nextDiff = VideoDetectionUtils.calculateFrameDifference(curr, next);
            
            // 如果当前帧与前后帧差异都较大，可能是转场
            if (prevDiff > 0.2 && nextDiff > 0.2) {
                transitionCount++;
                
                // 分析转场的风格化程度
                double styleScore = analyzeFrameStyle(curr);
                totalStyle += styleScore;
            }
        }
        
        return transitionCount > 0 ? totalStyle / transitionCount : 0.0;
    }

    /**
     * 分析帧的风格化程度
     */
    private double analyzeFrameStyle(BufferedImage frame) {
        return VideoDetectionUtils.calculateSaturation(frame);
    }

    /**
     * 计算总置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        // 权重配置
        double temporalWeight = 0.15;     // 时间一致性权重
        double motionWeight = 0.30;       // 运动特征权重（Pika运动特征明显）
        double detailWeight = 0.15;       // 细节质量权重
        double colorWeight = 0.20;        // 色彩风格权重（Pika色彩鲜艳）
        double fingerprintWeight = 0.10;  // AI指纹权重
        double signatureWeight = 0.10;    // Pika特征权重
        
        double confidence = 0.0;
        confidence += scores.getOrDefault("temporal_consistency", 0.0) * temporalWeight;
        confidence += scores.getOrDefault("motion_patterns", 0.0) * motionWeight;
        confidence += scores.getOrDefault("detail_quality", 0.0) * detailWeight;
        confidence += scores.getOrDefault("color_style", 0.0) * colorWeight;
        confidence += scores.getOrDefault("ai_fingerprint", 0.0) * fingerprintWeight;
        confidence += scores.getOrDefault("pika_signature", 0.0) * signatureWeight;
        
        // 特征组合加成
        double motionScore = scores.getOrDefault("motion_patterns", 0.0);
        double colorScore = scores.getOrDefault("color_style", 0.0);
        double pikaScore = scores.getOrDefault("pika_signature", 0.0);
        
        // 如果运动夸张+色彩鲜艳+Pika特征都高，额外加成
        if (motionScore > 70 && colorScore > 75 && pikaScore > 60) {
            confidence = Math.min(100.0, confidence * 1.15);
        }
        
        return Math.min(100.0, Math.max(0.0, confidence));
    }

    /**
     * 生成特征详情描述
     */
    private String generateFeatureDetails(Map<String, Double> scores, Map<String, String> features) {
        StringBuilder details = new StringBuilder();
        details.append("=== Pika Labs视频特征分析 ===\n\n");
        
        details.append("【核心特征】\n");
        details.append(features.get("motion_patterns")).append("\n");
        details.append(features.get("color_style")).append("\n");
        details.append(features.get("pika_signature")).append("\n\n");
        
        details.append("【辅助特征】\n");
        details.append(features.get("temporal_consistency")).append("\n");
        details.append(features.get("detail_quality")).append("\n");
        details.append(features.get("ai_fingerprint")).append("\n");
        
        return details.toString();
    }

    /**
     * 生成优化建议
     */
    private List<String> generateSuggestionsFromScores(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence > 70) {
            suggestions.add("视频极可能由Pika Labs生成");
            suggestions.add("建议：检测到明显的运动夸张和色彩鲜艳特征");
            
            double motionScore = scores.getOrDefault("motion_patterns", 0.0);
            if (motionScore > 75) {
                suggestions.add("- 运动特征非常明显，符合Pika Labs风格");
            }
            
            double colorScore = scores.getOrDefault("color_style", 0.0);
            if (colorScore > 80) {
                suggestions.add("- 色彩饱和度极高，典型的Pika风格");
            }
            
        } else if (confidence > 40) {
            suggestions.add("视频可能由Pika Labs生成");
            suggestions.add("建议：检测到部分Pika特征，但不够明显");
            
            double pikaScore = scores.getOrDefault("pika_signature", 0.0);
            if (pikaScore < 50) {
                suggestions.add("- Pika特征不够典型，建议进一步分析");
            }
            
        } else {
            suggestions.add("视频不太可能由Pika Labs生成");
            suggestions.add("建议：特征不符合Pika Labs典型风格");
        }
        
        return suggestions;
    }

    // 描述方法
    private String getTemporalDescription(double score) {
        if (score > 80) return "时间连贯性好";
        if (score > 60) return "时间连贯性中等";
        return "时间连贯性较差";
    }

    private String getMotionDescription(double score) {
        if (score > 75) return "运动幅度大，变化明显（典型Pika特征）";
        if (score > 60) return "运动幅度较大";
        return "运动幅度正常";
    }

    private String getDetailDescription(double score) {
        if (score > 80) return "细节非常精细";
        if (score >= 50 && score <= 70) return "细节中等（符合Pika特征）";
        return "细节较模糊";
    }

    private String getColorDescription(double score) {
        if (score > 80) return "色彩极其鲜艳（典型Pika特征）";
        if (score > 70) return "色彩较鲜艳";
        return "色彩正常";
    }

    private String getFingerprintDescription(double score) {
        if (score > 70) return "AI生成特征明显";
        if (score > 40) return "AI生成特征中等";
        return "AI生成特征不明显";
    }

    private String getPikaSignatureDescription(double score) {
        if (score > 70) return "Pika特征非常明显（运动夸张+色彩鲜艳+风格化强）";
        if (score > 50) return "Pika特征较明显";
        return "Pika特征不明显";
    }
}
