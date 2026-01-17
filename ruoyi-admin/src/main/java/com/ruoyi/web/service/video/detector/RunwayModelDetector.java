package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Runway Gen-2/Gen-3 AI视频模型检测器
 * 
 * 特征：
 * 1. 时间一致性：极高的帧间一致性（过度平滑）
 * 2. 运动特征：运动流畅但略显僵硬，缺乏自然的随机性
 * 3. 细节质量：细节丰富但有时过于完美
 * 4. 色彩风格：色彩饱和度高，对比度强烈
 * 5. AI指纹：特有的纹理模式和生成痕迹
 * 
 * @author ruoyi
 */
@Component
public class RunwayModelDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Runway Gen-2/Gen-3";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> metadata) {
        // 带元数据的检测方法，委托给基础检测方法
        return detectModel(videoFrames);
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> videoFrames) {
        VideoModelDetectionResult result = detectModel(videoFrames);
        return getFeatureDetails(result.getScores());
    }

    @Override
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> details = new HashMap<>();
        
        details.put("modelName", getModelName());
        details.put("scores", scores);
        
        // 添加特征详细描述
        if (scores.containsKey("时间一致性")) {
            double temporal = scores.get("时间一致性");
            details.put("temporalConsistencyLevel", temporal > 85 ? "极高（疑似AI生成）" : temporal > 70 ? "较高" : "正常");
        }
        
        if (scores.containsKey("运动特征")) {
            double motion = scores.get("运动特征");
            details.put("motionSmoothnessLevel", motion > 80 ? "过度平滑" : motion > 65 ? "较平滑" : "自然");
        }
        
        if (scores.containsKey("细节质量")) {
            double detail = scores.get("细节质量");
            details.put("detailQualityLevel", detail > 85 ? "过于完美" : detail > 70 ? "良好" : "正常");
        }
        
        if (scores.containsKey("色彩风格")) {
            double color = scores.get("色彩风格");
            details.put("colorStyleLevel", color > 75 ? "偏高（Runway特征）" : color > 60 ? "中等" : "正常");
        }
        
        if (scores.containsKey("Runway特征")) {
            double signature = scores.get("Runway特征");
            details.put("runwaySignatureLevel", signature > 70 ? "明显" : signature > 50 ? "存在" : "不明显");
        }
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames) {
        if (videoFrames == null || videoFrames.isEmpty()) {
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), 0.0);
            result.addSuggestion("视频帧为空，无法检测");
            return result;
        }

        // 1. 分析时间一致性（权重：25%）
        double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(videoFrames);
        
        // 2. 分析运动特征（权重：25%）
        double motionPattern = featureAnalyzer.analyzeMotionPatterns(videoFrames);
        
        // 3. 分析细节质量（权重：20%）
        double detailQuality = featureAnalyzer.analyzeDetailQuality(videoFrames);
        
        // 4. 分析色彩风格（权重：15%）
        double colorStyle = featureAnalyzer.analyzeColorStyle(videoFrames);

        // 5. 分析AI指纹（权重：15%）
        Map<String, Object> aiFingerprint = featureAnalyzer.detectAiFingerprint(videoFrames);
        boolean hasFingerprint = (boolean) aiFingerprint.getOrDefault("hasFingerprint", false);
        double fingerprintScore = hasFingerprint ? 85.0 : 40.0;

        // 6. Runway特有特征检测
        double runwaySignature = detectRunwaySignature(videoFrames);

        // 创建得分映射
        Map<String, Double> scores = new HashMap<>();
        scores.put("时间一致性", temporalConsistency);
        scores.put("运动特征", motionPattern);
        scores.put("细节质量", detailQuality);
        scores.put("色彩风格", colorStyle);
        scores.put("AI指纹", fingerprintScore);
        scores.put("Runway特征", runwaySignature);

        // 计算综合置信度
        double confidence = calculateConfidence(scores);

        // 创建结果对象
        VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), confidence);
        result.setScores(scores);
        
        // 添加特征描述
        List<String> features = new ArrayList<>();
        features.add("帧间一致性: " + (temporalConsistency > 85 ? "极高（疑似AI生成）" : "正常"));
        features.add("运动流畅度: " + (motionPattern > 80 ? "过度平滑" : "自然"));
        features.add("细节完美度: " + (detailQuality > 85 ? "过于完美" : "正常"));
        features.add("色彩饱和度: " + (colorStyle > 75 ? "偏高（Runway特征）" : "正常"));
        features.add("生成痕迹: " + (hasFingerprint ? "检测到Runway特征" : "无明显痕迹"));
        features.add("Runway特有模式: " + (runwaySignature > 70 ? "检测到" : "未检测到"));
        result.setFeatures(features);

        // 生成建议
        result.setSuggestions(generateSuggestions(confidence, scores));

        return result;
    }

    /**
     * 检测Runway特有签名特征
     */
    private double detectRunwaySignature(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 3) {
            return 0.0;
        }

        double signatureScore = 0.0;
        int featureCount = 0;

        // 1. 检测边缘稳定性（Runway视频边缘非常稳定）
        double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
        if (edgeStability > 90) {
            signatureScore += 25.0;
        } else if (edgeStability > 80) {
            signatureScore += 15.0;
        }
        featureCount++;

        // 2. 检测光照一致性（Runway处理光照变化很平滑）
        double lightingConsistency = featureAnalyzer.analyzeLightingConsistency(frames);
        if (lightingConsistency > 85) {
            signatureScore += 25.0;
        } else if (lightingConsistency > 75) {
            signatureScore += 15.0;
        }
        featureCount++;

        // 3. 检测噪声模式（Runway生成的视频噪声极低且均匀）
        Map<String, Double> noisePattern = featureAnalyzer.analyzeNoisePattern(frames);
        double noiseLevel = noisePattern.getOrDefault("noiseLevel", 0.5);
        double noiseUniformity = noisePattern.getOrDefault("noiseUniformity", 0.5);
        
        if (noiseLevel < 0.1 && noiseUniformity > 0.8) {
            signatureScore += 25.0; // 低噪声+高均匀性 = Runway特征
        } else if (noiseLevel < 0.15 && noiseUniformity > 0.7) {
            signatureScore += 15.0;
        }
        featureCount++;

        // 4. 检测纹理规律性（Runway生成的纹理有特定模式）
        double textureRegularity = analyzeTextureRegularity(frames);
        if (textureRegularity > 0.8) {
            signatureScore += 25.0;
        } else if (textureRegularity > 0.7) {
            signatureScore += 15.0;
        }
        featureCount++;

        return featureCount > 0 ? signatureScore / featureCount * 4 : 0.0;
    }

    /**
     * 分析纹理规律性
     */
    private double analyzeTextureRegularity(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            return 0.0;
        }

        // 简化实现：通过细节质量的均匀性判断纹理规律性
        List<Double> detailScores = new ArrayList<>();
        
        for (BufferedImage frame : frames) {
            double detail = featureAnalyzer.analyzeDetailQuality(Collections.singletonList(frame));
            detailScores.add(detail);
        }

        if (detailScores.isEmpty()) {
            return 0.0;
        }

        // 计算方差，方差越小说明越规律
        double mean = detailScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = detailScores.stream()
            .mapToDouble(d -> Math.pow(d - mean, 2))
            .average().orElse(0.0);
        
        // 归一化：方差越小，规律性越高
        return 1.0 / (1.0 + Math.sqrt(variance) / 10.0);
    }

    /**
     * 计算综合置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        double temporal = scores.getOrDefault("时间一致性", 0.0);
        double motion = scores.getOrDefault("运动特征", 0.0);
        double detail = scores.getOrDefault("细节质量", 0.0);
        double color = scores.getOrDefault("色彩风格", 0.0);
        double fingerprint = scores.getOrDefault("AI指纹", 0.0);
        double signature = scores.getOrDefault("Runway特征", 0.0);

        // 加权计算
        double confidence = temporal * 0.25 +     // 时间一致性 25%
                           motion * 0.25 +         // 运动特征 25%
                           detail * 0.20 +         // 细节质量 20%
                           color * 0.15 +          // 色彩风格 15%
                           fingerprint * 0.10 +    // AI指纹 10%
                           signature * 0.05;       // Runway特征 5%

        // 特征组合加成
        if (temporal > 85 && motion > 80 && color > 75) {
            confidence = Math.min(confidence * 1.1, 100.0); // 多个高分特征同时出现，提升10%
        }

        if (signature > 70 && fingerprint > 80) {
            confidence = Math.min(confidence * 1.05, 100.0); // Runway特征明显，再提升5%
        }

        return Math.min(Math.max(confidence, 0.0), 100.0);
    }

    /**
     * 生成优化建议
     */
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();

        if (confidence >= 80) {
            suggestions.add("高度疑似Runway Gen-2/Gen-3生成的视频");
            suggestions.add("检测到明显的时间一致性过高特征");
            suggestions.add("运动模式显示出AI生成的平滑特征");
            suggestions.add("建议进一步人工审核确认");
        } else if (confidence >= 60) {
            suggestions.add("可能由Runway Gen-2/Gen-3生成");
            suggestions.add("部分特征与Runway生成视频相符");
            suggestions.add("建议结合其他检测器结果综合判断");
        } else if (confidence >= 40) {
            suggestions.add("存在部分Runway特征，但不明显");
            suggestions.add("可能是真实视频或其他AI工具生成");
            suggestions.add("建议进一步分析其他特征");
        } else {
            suggestions.add("不太可能是Runway生成的视频");
            suggestions.add("视频特征更接近真实拍摄或其他生成工具");
        }

        // 根据具体特征添加建议
        if (scores.get("时间一致性") != null && scores.get("时间一致性") > 85) {
            suggestions.add("注意：帧间一致性异常高，这是Runway的典型特征");
        }
        if (scores.get("色彩风格") != null && scores.get("色彩风格") > 75) {
            suggestions.add("注意：色彩饱和度偏高，符合Runway的色彩风格");
        }

        return suggestions;
    }
}
