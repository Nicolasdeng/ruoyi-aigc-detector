package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * 文心一格视频检测器
 * 
 * 特征分析：
 * 1. 中国风格偏好：倾向于中国文化元素和美学风格
 * 2. 转场自然：场景转换流畅自然，无明显痕迹
 * 3. 细节良好：细节处理得当，既不过分完美也不粗糙
 * 4. 色彩平衡：色彩饱和度适中，不会过于鲜艳或暗淡
 * 5. 语义理解强：内容连贯性好，符合逻辑
 * 
 * @author ruoyi
 */
@Component
public class WenxinYigeVideoDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "文心一格视频";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        return detectModel(frames, new HashMap<>());
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames, Map<String, Object> metadata) {
        if (frames == null || frames.isEmpty()) {
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), 0.0);
            result.setFeatures(Collections.singletonList("无有效帧"));
            result.setSuggestions(Arrays.asList("无法分析：未提供有效视频帧"));
            return result;
        }

        Map<String, Double> scores = new HashMap<>();
        
        // 1. 时间一致性分析（权重20%）
        double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
        scores.put("时间一致性", temporalConsistency);
        
        // 2. 运动特征分析（权重15%）
        double motionPatterns = featureAnalyzer.analyzeMotionPatterns(frames);
        scores.put("运动特征", motionPatterns);
        
        // 3. 细节质量分析（权重20%）
        double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
        scores.put("细节质量", detailQuality);
        
        // 4. 色彩风格分析（权重20%）
        double colorStyle = featureAnalyzer.analyzeColorStyle(frames);
        scores.put("色彩风格", colorStyle);
        
        // 5. 边缘稳定性分析（权重10%）
        double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
        scores.put("边缘稳定性", edgeStability);
        
        // 6. 光照一致性分析（权重10%）
        double lightingConsistency = featureAnalyzer.analyzeLightingConsistency(frames);
        scores.put("光照一致性", lightingConsistency);
        
        // 7. 文心一格特征检测（权重5%）
        double wenxinSignature = detectWenxinSignature(frames, scores);
        scores.put("文心一格特征", wenxinSignature);
        
        // 计算综合置信度
        double confidence = calculateConfidence(scores);
        
        // 创建结果对象
        VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), confidence);
        result.setScores(scores);
        
        // 从 getFeatureDetails 返回的 Map 中提取 "details" 字段作为特征字符串
        Map<String, Object> featureDetails = getFeatureDetails(frames);
        String detailsText = (String) featureDetails.get("details");
        result.setFeatures(Collections.singletonList(detailsText));
        
        result.setSuggestions(generateSuggestions(confidence, scores));
        
        return result;
    }

    /**
     * 检测文心一格视频特有签名
     * 
     * 特征签名：
     * 1. 转场自然度检测：场景转换平滑，帧间变化适中（满分30分）
     * 2. 细节平衡检测：细节质量在60-85之间，不过完美也不粗糙（满分25分）
     * 3. 色彩平衡检测：色彩饱和度适中，在60-80之间（满分25分）
     * 4. 中国风格检测：色调偏向中国传统美学（满分20分）
     */
    private double detectWenxinSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signatureScore = 0.0;
        
        // 1. 转场自然度检测（30分）
        double transitionNaturalness = analyzeTransitionNaturalness(frames);
        double temporalConsistency = scores.get("时间一致性");
        if (transitionNaturalness > 0.7 && temporalConsistency >= 70 && temporalConsistency <= 85) {
            signatureScore += 30;
        } else if (transitionNaturalness > 0.6) {
            signatureScore += 15;
        }
        
        // 2. 细节平衡检测（25分）
        double detailQuality = scores.get("细节质量");
        if (detailQuality >= 60 && detailQuality <= 85) {
            signatureScore += 25;
        } else if (detailQuality >= 55 && detailQuality <= 90) {
            signatureScore += 15;
        }
        
        // 3. 色彩平衡检测（25分）
        double colorBalance = analyzeColorBalance(frames);
        if (colorBalance > 0.7) {
            signatureScore += 25;
        } else if (colorBalance > 0.6) {
            signatureScore += 15;
        }
        
        // 4. 中国风格检测（20分）
        double chineseStyle = analyzeChineseStyle(frames);
        if (chineseStyle > 0.6) {
            signatureScore += 20;
        } else if (chineseStyle > 0.5) {
            signatureScore += 10;
        }
        
        return signatureScore;
    }

    /**
     * 分析转场自然度
     * 检测场景转换的平滑程度
     */
    private double analyzeTransitionNaturalness(List<BufferedImage> frames) {
        if (frames.size() < 3) {
            return 0.0;
        }
        
        List<Double> frameDifferences = new ArrayList<>();
        
        // 计算连续帧之间的差异
        for (int i = 0; i < frames.size() - 1; i++) {
            BufferedImage frame1 = frames.get(i);
            BufferedImage frame2 = frames.get(i + 1);
            
            double diff = calculateFrameDifference(frame1, frame2);
            frameDifferences.add(diff);
        }
        
        // 分析差异的波动性
        double meanDiff = frameDifferences.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = frameDifferences.stream()
            .mapToDouble(d -> Math.pow(d - meanDiff, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // 转场自然的特征：差异适中（不太大不太小），波动性小
        double naturalness = 0.0;
        
        // 差异适中性评分
        if (meanDiff > 15 && meanDiff < 35) {
            naturalness += 0.5;
        } else if (meanDiff > 10 && meanDiff < 40) {
            naturalness += 0.3;
        }
        
        // 波动性评分（标准差越小越好）
        if (stdDev < 10) {
            naturalness += 0.5;
        } else if (stdDev < 15) {
            naturalness += 0.3;
        }
        
        return naturalness;
    }

    /**
     * 计算两帧之间的差异
     */
    private double calculateFrameDifference(BufferedImage frame1, BufferedImage frame2) {
        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());
        
        long totalDiff = 0;
        int sampleStep = 10; // 采样步长
        
        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                int rgb1 = frame1.getRGB(x, y);
                int rgb2 = frame2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
            }
        }
        
        int samples = (width / sampleStep) * (height / sampleStep);
        return samples > 0 ? (double) totalDiff / samples / 3.0 : 0.0;
    }

    /**
     * 分析色彩平衡度
     * 检测色彩饱和度是否适中
     */
    private double analyzeColorBalance(List<BufferedImage> frames) {
        double totalBalance = 0.0;
        
        for (BufferedImage frame : frames) {
            double saturationScore = analyzeFrameSaturation(frame);
            
            // 色彩平衡：饱和度在60-80之间最好
            if (saturationScore >= 60 && saturationScore <= 80) {
                totalBalance += 1.0;
            } else if (saturationScore >= 55 && saturationScore <= 85) {
                totalBalance += 0.7;
            } else if (saturationScore >= 50 && saturationScore <= 90) {
                totalBalance += 0.4;
            }
        }
        
        return totalBalance / frames.size();
    }

    /**
     * 分析单帧的饱和度
     */
    private double analyzeFrameSaturation(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        double totalSaturation = 0.0;
        int sampleStep = 10;
        int sampleCount = 0;
        
        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                int rgb = frame.getRGB(x, y);
                
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // 计算HSV中的饱和度
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                double saturation = max == 0 ? 0 : (double) (max - min) / max;
                
                totalSaturation += saturation * 100;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? totalSaturation / sampleCount : 0.0;
    }

    /**
     * 分析中国风格特征
     * 检测色调是否偏向中国传统美学
     */
    private double analyzeChineseStyle(List<BufferedImage> frames) {
        double totalStyle = 0.0;
        
        for (BufferedImage frame : frames) {
            double styleScore = analyzeFrameChineseStyle(frame);
            totalStyle += styleScore;
        }
        
        return totalStyle / frames.size();
    }

    /**
     * 分析单帧的中国风格特征
     * 中国传统美学特点：
     * 1. 色调偏向红色、金色、青色等传统色彩
     * 2. 对比度适中，不会过于强烈
     * 3. 整体色调偏暖或偏冷，有明确倾向
     */
    private double analyzeFrameChineseStyle(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        int redTone = 0;
        int goldTone = 0;
        int cyanTone = 0;
        double totalContrast = 0.0;
        double warmTone = 0.0;
        
        int sampleStep = 10;
        int sampleCount = 0;
        
        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                int rgb = frame.getRGB(x, y);
                
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // 检测传统色彩倾向
                if (r > 150 && g < 100 && b < 100) redTone++; // 红色调
                if (r > 180 && g > 150 && b < 100) goldTone++; // 金色调
                if (r < 100 && g > 120 && b > 140) cyanTone++; // 青色调
                
                // 计算对比度
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                totalContrast += (max - min);
                
                // 计算暖冷色调
                warmTone += (r - b);
                
                sampleCount++;
            }
        }
        
        double styleScore = 0.0;
        
        // 传统色彩比例
        double traditionalColorRatio = (double) (redTone + goldTone + cyanTone) / sampleCount;
        if (traditionalColorRatio > 0.15) {
            styleScore += 0.4;
        } else if (traditionalColorRatio > 0.10) {
            styleScore += 0.2;
        }
        
        // 对比度适中性
        double avgContrast = totalContrast / sampleCount;
        if (avgContrast > 30 && avgContrast < 80) {
            styleScore += 0.3;
        } else if (avgContrast > 20 && avgContrast < 100) {
            styleScore += 0.15;
        }
        
        // 色调倾向明确性
        double avgWarmTone = Math.abs(warmTone / sampleCount);
        if (avgWarmTone > 20) {
            styleScore += 0.3;
        } else if (avgWarmTone > 10) {
            styleScore += 0.15;
        }
        
        return styleScore;
    }

    /**
     * 计算综合置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        // 基础加权计算
        double confidence = 0.0;
        confidence += scores.get("时间一致性") * 0.20;
        confidence += scores.get("运动特征") * 0.15;
        confidence += scores.get("细节质量") * 0.20;
        confidence += scores.get("色彩风格") * 0.20;
        confidence += scores.get("边缘稳定性") * 0.10;
        confidence += scores.get("光照一致性") * 0.10;
        confidence += scores.get("文心一格特征") * 0.05;
        
        // 特征组合加成
        // 如果转场自然、细节平衡、色彩平衡都达标，增加置信度
        if (scores.get("时间一致性") >= 70 && scores.get("时间一致性") <= 85 &&
            scores.get("细节质量") >= 60 && scores.get("细节质量") <= 85 &&
            scores.get("色彩风格") >= 60 && scores.get("色彩风格") <= 80 &&
            scores.get("文心一格特征") >= 60) {
            confidence *= 1.15;
        }
        
        return Math.min(confidence, 100.0);
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> frames) {
        Map<String, Object> result = new HashMap<>();
        
        if (frames == null || frames.isEmpty()) {
            result.put("details", "无效的帧数据");
            return result;
        }
        
        try {
            StringBuilder details = new StringBuilder();
            details.append("文心一格视频特征分析：\n\n");
            
            // 时间一致性
            double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
            details.append(String.format("1. 时间一致性：%.2f%%\n", temporalConsistency));
            if (temporalConsistency >= 70 && temporalConsistency <= 85) {
                details.append("   - 转场自然，帧间连贯性适中✓\n");
            }
            
            // 运动特征
            double motionPatterns = featureAnalyzer.analyzeMotionPatterns(frames);
            details.append(String.format("\n2. 运动特征：%.2f%%\n", motionPatterns));
            if (motionPatterns >= 50 && motionPatterns <= 80) {
                details.append("   - 运动幅度适中，不夸张✓\n");
            }
            
            // 细节质量
            double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
            details.append(String.format("\n3. 细节质量：%.2f%%\n", detailQuality));
            if (detailQuality >= 60 && detailQuality <= 85) {
                details.append("   - 细节处理平衡，不过完美也不粗糙✓\n");
            }
            
            // 色彩风格
            double colorStyle = featureAnalyzer.analyzeColorStyle(frames);
            details.append(String.format("\n4. 色彩风格：%.2f%%\n", colorStyle));
            if (colorStyle >= 60 && colorStyle <= 80) {
                details.append("   - 色彩饱和度适中✓\n");
            }
            
            Map<String, Double> scores = new HashMap<>();
            scores.put("时间一致性", temporalConsistency);
            scores.put("运动特征", motionPatterns);
            scores.put("细节质量", detailQuality);
            scores.put("色彩风格", colorStyle);
            
            // 文心一格特征
            double wenxinSignature = detectWenxinSignature(frames, scores);
            details.append(String.format("\n5. 文心一格特征签名：%.2f分\n", wenxinSignature));
            if (wenxinSignature >= 60) {
                details.append("   - 检测到明显的文心一格生成特征✓\n");
            }
            
            result.put("details", details.toString());
            result.put("scores", scores);
            result.put("wenxinSignature", wenxinSignature);
            return result;
            
        } catch (Exception e) {
            result.put("details", "特征提取失败: " + e.getMessage());
            result.put("error", e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> result = new HashMap<>();
        
        if (scores == null || scores.isEmpty()) {
            result.put("details", "无效的特征分数");
            return result;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("文心一格视频特征分析：\n\n");
        
        // 时间一致性
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        details.append(String.format("1. 时间一致性：%.2f%%\n", temporalConsistency));
        if (temporalConsistency >= 70 && temporalConsistency <= 85) {
            details.append("   - 转场自然，帧间连贯性适中✓\n");
        }
        
        // 运动特征
        double motionPatterns = scores.getOrDefault("运动特征", 0.0);
        details.append(String.format("\n2. 运动特征：%.2f%%\n", motionPatterns));
        if (motionPatterns >= 50 && motionPatterns <= 80) {
            details.append("   - 运动幅度适中，不夸张✓\n");
        }
        
        // 细节质量
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        details.append(String.format("\n3. 细节质量：%.2f%%\n", detailQuality));
        if (detailQuality >= 60 && detailQuality <= 85) {
            details.append("   - 细节处理平衡，不过完美也不粗糙✓\n");
        }
        
        // 色彩风格
        double colorStyle = scores.getOrDefault("色彩风格", 0.0);
        details.append(String.format("\n4. 色彩风格：%.2f%%\n", colorStyle));
        if (colorStyle >= 60 && colorStyle <= 80) {
            details.append("   - 色彩饱和度适中✓\n");
        }
        
        // 文心一格特征
        double wenxinSignature = scores.getOrDefault("文心一格特征", 0.0);
        details.append(String.format("\n5. 文心一格特征签名：%.2f分\n", wenxinSignature));
        if (wenxinSignature >= 60) {
            details.append("   - 检测到明显的文心一格生成特征✓\n");
        }
        
        result.put("details", details.toString());
        result.put("scores", scores);
        return result;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence >= 75) {
            suggestions.add("视频极可能由文心一格生成");
            suggestions.add("建议进行人工复审确认");
            
            double wenxinFeature = scores.getOrDefault("文心一格特征", 0.0);
            if (wenxinFeature >= 70) {
                suggestions.add("检测到明显的文心一格生成签名");
            }
            
            double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
            if (temporalConsistency >= 70 && temporalConsistency <= 85) {
                suggestions.add("转场自然度符合文心一格特征");
            }
            
            double detailQuality = scores.getOrDefault("细节质量", 0.0);
            if (detailQuality >= 60 && detailQuality <= 85) {
                suggestions.add("细节平衡度符合文心一格风格");
            }
            
        } else if (confidence >= 50) {
            suggestions.add("视频可能由文心一格生成");
            suggestions.add("建议结合其他检测方法综合判断");
            
            double colorStyle = scores.getOrDefault("色彩风格", 0.0);
            if (colorStyle >= 60 && colorStyle <= 80) {
                suggestions.add("色彩平衡度较符合文心一格特征");
            }
            
        } else if (confidence >= 30) {
            suggestions.add("视频存在部分文心一格特征");
            suggestions.add("可能为混合内容或经过后期处理");
            
        } else {
            suggestions.add("视频不太可能由文心一格生成");
            suggestions.add("可能为真实拍摄或其他AI工具生成");
        }
        
        return suggestions;
    }
}
