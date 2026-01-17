package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * 通义千问视频AI模型检测器
 * 
 * 通义千问视频生成工具的主要特征：
 * 1. 中文场景理解好 - 对中文文本提示的理解和执行准确
 * 2. 文本到视频效果自然 - 文本描述与视频内容高度一致
 * 3. 色彩平衡 - 色彩自然、饱和度适中
 * 4. 运动合理 - 物体运动符合物理规律
 * 5. 细节质量良好 - 细节清晰、纹理自然
 * 6. 场景转换流畅 - 镜头切换和场景过渡自然
 * 7. 光照真实 - 光影效果符合自然规律
 * 
 * @author ruoyi
 */
@Component
public class TongyiQianwenVideoDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "通义千问视频";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), 0.0);
            result.setScores(new HashMap<>());
            result.setSuggestions(new ArrayList<>());
            return result;
        }

        try {
            // 使用VideoFeatureAnalyzer进行特征分析
            Map<String, Double> featureScores = new HashMap<>();
            
            // 1. 时间一致性分析 (20%)
            double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
            featureScores.put("时间一致性", temporalConsistency);
            
            // 2. 运动特征分析 (20%)
            double motionPatterns = featureAnalyzer.analyzeMotionPatterns(frames);
            featureScores.put("运动特征", motionPatterns);
            
            // 3. 色彩风格分析 (20%)
            double colorStyle = featureAnalyzer.analyzeColorStyle(frames);
            featureScores.put("色彩风格", colorStyle);
            
            // 4. 细节质量分析 (15%)
            double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
            featureScores.put("细节质量", detailQuality);
            
            // 5. 光照一致性分析 (10%)
            double lightingConsistency = featureAnalyzer.analyzeLightingConsistency(frames);
            featureScores.put("光照一致性", lightingConsistency);
            
            // 6. 噪声模式分析 (5%)
            Map<String, Double> noisePatternMap = featureAnalyzer.analyzeNoisePattern(frames);
            double noisePattern = noisePatternMap.getOrDefault("overall_score", 0.0);
            featureScores.put("噪声模式", noisePattern);
            
            // 7. 边缘稳定性分析 (5%)
            double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
            featureScores.put("边缘稳定性", edgeStability);
            
            // 8. 通义千问特征检测 (5%)
            double tongyiSignature = detectTongyiQianwenSignature(frames, featureScores);
            featureScores.put("通义千问特征", tongyiSignature);
            
            // 计算总体置信度
            double confidence = calculateConfidence(featureScores);
            
            // 生成建议
            List<String> suggestions = generateSuggestions(confidence, featureScores);
            
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), confidence);
            result.setScores(featureScores);
            result.setSuggestions(suggestions);
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), 0.0);
            result.setScores(new HashMap<>());
            result.setSuggestions(Arrays.asList("检测过程中发生错误: " + e.getMessage()));
            return result;
        }
    }

    /**
     * 检测通义千问视频特有签名特征
     */
    private double detectTongyiQianwenSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signatureScore = 0.0;
        
        // 1. 中文场景理解特征 (30分)
        // 通过分析场景的自然度和合理性来评估
        double sceneNaturalness = analyzeSceneNaturalness(frames, scores);
        if (sceneNaturalness > 0.75) {
            signatureScore += 30;
        } else if (sceneNaturalness > 0.65) {
            signatureScore += 20;
        } else if (sceneNaturalness > 0.55) {
            signatureScore += 10;
        }
        
        // 2. 色彩平衡特征 (25分)
        double colorBalance = analyzeColorBalance(frames);
        if (colorBalance > 0.75) {
            signatureScore += 25;
        } else if (colorBalance > 0.65) {
            signatureScore += 18;
        } else if (colorBalance > 0.55) {
            signatureScore += 10;
        }
        
        // 3. 运动合理性特征 (25分)
        double motionRealism = analyzeMotionRealism(frames);
        if (motionRealism > 0.75) {
            signatureScore += 25;
        } else if (motionRealism > 0.65) {
            signatureScore += 18;
        } else if (motionRealism > 0.55) {
            signatureScore += 10;
        }
        
        // 4. 场景转换流畅度 (20分)
        double sceneTransition = analyzeSceneTransition(frames);
        if (sceneTransition > 0.75) {
            signatureScore += 20;
        } else if (sceneTransition > 0.65) {
            signatureScore += 14;
        } else if (sceneTransition > 0.55) {
            signatureScore += 7;
        }
        
        return Math.min(100, signatureScore);
    }

    /**
     * 分析场景自然度
     * 评估视频场景的整体自然度和合理性
     */
    private double analyzeSceneNaturalness(List<BufferedImage> frames, Map<String, Double> scores) {
        // 结合多个特征评估场景自然度
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double lightingConsistency = scores.getOrDefault("光照一致性", 0.0);
        
        // 加权计算：时间一致性40% + 细节质量35% + 光照一致性25%
        double naturalness = temporalConsistency * 0.4 + detailQuality * 0.35 + lightingConsistency * 0.25;
        
        // 分析场景复杂度
        double sceneComplexity = analyzeSceneComplexity(frames);
        
        // 如果场景复杂度适中（0.4-0.7），给予加分
        if (sceneComplexity >= 0.4 && sceneComplexity <= 0.7) {
            naturalness = Math.min(1.0, naturalness * 1.1);
        }
        
        return naturalness;
    }

    /**
     * 分析场景复杂度
     */
    private double analyzeSceneComplexity(List<BufferedImage> frames) {
        if (frames.isEmpty()) return 0.0;
        
        double totalComplexity = 0.0;
        int validFrames = 0;
        
        for (BufferedImage frame : frames) {
            if (frame == null) continue;
            
            // 计算边缘密度作为复杂度指标
            double edgeDensity = calculateEdgeDensity(frame);
            
            // 计算颜色多样性
            double colorDiversity = calculateColorDiversity(frame);
            
            // 综合评估：边缘密度60% + 颜色多样性40%
            double complexity = edgeDensity * 0.6 + colorDiversity * 0.4;
            
            totalComplexity += complexity;
            validFrames++;
        }
        
        return validFrames > 0 ? totalComplexity / validFrames : 0.0;
    }

    /**
     * 计算边缘密度
     */
    private double calculateEdgeDensity(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        int edgeCount = 0;
        int totalPixels = width * height;
        
        // 简化的Sobel边缘检测
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = frame.getRGB(x, y) & 0xFF;
                int left = frame.getRGB(x - 1, y) & 0xFF;
                int right = frame.getRGB(x + 1, y) & 0xFF;
                int top = frame.getRGB(x, y - 1) & 0xFF;
                int bottom = frame.getRGB(x, y + 1) & 0xFF;
                
                int gx = Math.abs(right - left);
                int gy = Math.abs(bottom - top);
                int gradient = gx + gy;
                
                if (gradient > 30) {
                    edgeCount++;
                }
            }
        }
        
        return (double) edgeCount / totalPixels;
    }

    /**
     * 计算颜色多样性
     */
    private double calculateColorDiversity(BufferedImage frame) {
        Set<Integer> uniqueColors = new HashSet<>();
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        // 采样像素以提高性能
        int step = Math.max(1, Math.min(width, height) / 100);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                // 降低精度以合并相似颜色
                int r = ((rgb >> 16) & 0xFF) / 16;
                int g = ((rgb >> 8) & 0xFF) / 16;
                int b = (rgb & 0xFF) / 16;
                uniqueColors.add((r << 8) | (g << 4) | b);
            }
        }
        
        // 归一化到0-1范围
        return Math.min(1.0, uniqueColors.size() / 1000.0);
    }

    /**
     * 分析色彩平衡
     * 通义千问视频的色彩通常饱和度适中、色调平衡
     */
    private double analyzeColorBalance(List<BufferedImage> frames) {
        if (frames.isEmpty()) return 0.0;
        
        double totalBalance = 0.0;
        int validFrames = 0;
        
        for (BufferedImage frame : frames) {
            if (frame == null) continue;
            
            // 计算RGB通道的平衡度
            double[] channelAvg = calculateChannelAverages(frame);
            double rAvg = channelAvg[0];
            double gAvg = channelAvg[1];
            double bAvg = channelAvg[2];
            
            // 计算通道间的差异
            double maxDiff = Math.max(Math.abs(rAvg - gAvg), 
                            Math.max(Math.abs(gAvg - bAvg), Math.abs(rAvg - bAvg)));
            
            // 差异越小，平衡度越高
            double balance = 1.0 - (maxDiff / 255.0);
            
            // 计算饱和度适中性
            double saturation = calculateAverageSaturation(frame);
            // 理想饱和度范围：0.4-0.7
            double saturationScore = 1.0;
            if (saturation < 0.4) {
                saturationScore = saturation / 0.4;
            } else if (saturation > 0.7) {
                saturationScore = 1.0 - ((saturation - 0.7) / 0.3);
            }
            
            // 综合评分：平衡度60% + 饱和度适中性40%
            double frameBalance = balance * 0.6 + saturationScore * 0.4;
            
            totalBalance += frameBalance;
            validFrames++;
        }
        
        return validFrames > 0 ? totalBalance / validFrames : 0.0;
    }

    /**
     * 计算RGB通道平均值
     */
    private double[] calculateChannelAverages(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        long rSum = 0, gSum = 0, bSum = 0;
        int pixelCount = 0;
        
        // 采样以提高性能
        int step = Math.max(1, Math.min(width, height) / 100);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                rSum += (rgb >> 16) & 0xFF;
                gSum += (rgb >> 8) & 0xFF;
                bSum += rgb & 0xFF;
                pixelCount++;
            }
        }
        
        return new double[] {
            pixelCount > 0 ? (double) rSum / pixelCount : 0,
            pixelCount > 0 ? (double) gSum / pixelCount : 0,
            pixelCount > 0 ? (double) bSum / pixelCount : 0
        };
    }

    /**
     * 计算平均饱和度
     */
    private double calculateAverageSaturation(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        double totalSaturation = 0.0;
        int pixelCount = 0;
        
        // 采样以提高性能
        int step = Math.max(1, Math.min(width, height) / 100);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                
                double saturation = max > 0 ? (double) (max - min) / max : 0;
                totalSaturation += saturation;
                pixelCount++;
            }
        }
        
        return pixelCount > 0 ? totalSaturation / pixelCount : 0.0;
    }

    /**
     * 分析运动合理性
     * 通义千问视频的物体运动符合物理规律
     */
    private double analyzeMotionRealism(List<BufferedImage> frames) {
        if (frames.size() < 3) return 0.5;
        
        // 计算运动向量的一致性和合理性
        double totalRealism = 0.0;
        int validPairs = 0;
        
        for (int i = 0; i < frames.size() - 2; i++) {
            BufferedImage frame1 = frames.get(i);
            BufferedImage frame2 = frames.get(i + 1);
            BufferedImage frame3 = frames.get(i + 2);
            
            if (frame1 == null || frame2 == null || frame3 == null) continue;
            
            // 计算相邻帧间的运动一致性
            double motion12 = calculateMotionMagnitude(frame1, frame2);
            double motion23 = calculateMotionMagnitude(frame2, frame3);
            
            // 运动应该是连续的，不应该有突变
            double motionConsistency = 1.0 - Math.min(1.0, Math.abs(motion12 - motion23) / (motion12 + motion23 + 0.01));
            
            // 检查运动是否过于平滑（AI生成的典型特征）
            double smoothnessPenalty = 0.0;
            if (motionConsistency > 0.95 && motion12 > 0.01) {
                smoothnessPenalty = 0.1; // 轻微惩罚，因为通义千问的运动比较自然
            }
            
            double realism = Math.max(0, motionConsistency - smoothnessPenalty);
            totalRealism += realism;
            validPairs++;
        }
        
        return validPairs > 0 ? totalRealism / validPairs : 0.5;
    }

    /**
     * 计算运动幅度
     */
    private double calculateMotionMagnitude(BufferedImage frame1, BufferedImage frame2) {
        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());
        
        long totalDiff = 0;
        int pixelCount = 0;
        
        // 采样以提高性能
        int step = Math.max(1, Math.min(width, height) / 50);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb1 = frame1.getRGB(x, y);
                int rgb2 = frame2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xFF;
                int g1 = (rgb1 >> 8) & 0xFF;
                int b1 = rgb1 & 0xFF;
                
                int r2 = (rgb2 >> 16) & 0xFF;
                int g2 = (rgb2 >> 8) & 0xFF;
                int b2 = rgb2 & 0xFF;
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
                pixelCount++;
            }
        }
        
        // 归一化到0-1范围
        return pixelCount > 0 ? (double) totalDiff / (pixelCount * 255 * 3) : 0.0;
    }

    /**
     * 分析场景转换流畅度
     * 通义千问视频的场景转换和镜头切换通常比较自然
     */
    private double analyzeSceneTransition(List<BufferedImage> frames) {
        if (frames.size() < 5) return 0.5;
        
        // 检测场景变化点
        List<Integer> sceneChanges = detectSceneChanges(frames);
        
        if (sceneChanges.isEmpty()) {
            // 没有场景变化，返回中等分数
            return 0.6;
        }
        
        // 分析每个场景变化的流畅度
        double totalSmoothness = 0.0;
        int validChanges = 0;
        
        for (int changePoint : sceneChanges) {
            if (changePoint < 2 || changePoint >= frames.size() - 2) continue;
            
            // 分析变化点前后的过渡
            double transitionSmoothness = analyzeTransitionSmoothness(frames, changePoint);
            totalSmoothness += transitionSmoothness;
            validChanges++;
        }
        
        return validChanges > 0 ? totalSmoothness / validChanges : 0.6;
    }

    /**
     * 检测场景变化点
     */
    private List<Integer> detectSceneChanges(List<BufferedImage> frames) {
        List<Integer> changes = new ArrayList<>();
        double threshold = 0.3; // 场景变化阈值
        
        for (int i = 1; i < frames.size(); i++) {
            double diff = calculateMotionMagnitude(frames.get(i - 1), frames.get(i));
            if (diff > threshold) {
                changes.add(i);
            }
        }
        
        return changes;
    }

    /**
     * 分析转换流畅度
     */
    private double analyzeTransitionSmoothness(List<BufferedImage> frames, int changePoint) {
        // 分析变化点前后的运动幅度变化
        double before1 = changePoint >= 2 ? 
            calculateMotionMagnitude(frames.get(changePoint - 2), frames.get(changePoint - 1)) : 0;
        double change = calculateMotionMagnitude(frames.get(changePoint - 1), frames.get(changePoint));
        double after1 = changePoint < frames.size() - 1 ? 
            calculateMotionMagnitude(frames.get(changePoint), frames.get(changePoint + 1)) : 0;
        
        // 计算变化的平滑程度
        double smoothness1 = before1 > 0 ? 1.0 - Math.min(1.0, Math.abs(change - before1) / before1) : 0.5;
        double smoothness2 = after1 > 0 ? 1.0 - Math.min(1.0, Math.abs(change - after1) / after1) : 0.5;
        
        return (smoothness1 + smoothness2) / 2.0;
    }

    /**
     * 计算置信度
     * 使用加权平均，重点关注通义千问视频的核心特征
     */
    private double calculateConfidence(Map<String, Double> scores) {
        double confidence = 0.0;
        
        // 权重分配
        confidence += scores.getOrDefault("时间一致性", 0.0) * 0.20;
        confidence += scores.getOrDefault("运动特征", 0.0) * 0.20;
        confidence += scores.getOrDefault("色彩风格", 0.0) * 0.20;
        confidence += scores.getOrDefault("细节质量", 0.0) * 0.15;
        confidence += scores.getOrDefault("光照一致性", 0.0) * 0.10;
        confidence += scores.getOrDefault("噪声模式", 0.0) * 0.05;
        confidence += scores.getOrDefault("边缘稳定性", 0.0) * 0.05;
        confidence += scores.getOrDefault("通义千问特征", 0.0) * 0.05;
        
        // 特征组合加成
        // 如果色彩平衡好(>=75) && 运动合理(>=70) && 场景自然(>=70)，置信度提升
        if (scores.getOrDefault("色彩风格", 0.0) >= 0.75 &&
            scores.getOrDefault("运动特征", 0.0) >= 0.70 &&
            scores.getOrDefault("细节质量", 0.0) >= 0.70) {
            confidence = Math.min(1.0, confidence * 1.15);
        }
        
        return confidence;
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames, Map<String, Object> metadata) {
        // 调用基础检测方法
        return detectModel(frames);
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
            details.append("通义千问视频特征分析:\n\n");
            
            // 基础特征分析
            Map<String, Double> scores = new HashMap<>();
            scores.put("时间一致性", featureAnalyzer.analyzeTemporalConsistency(frames));
            scores.put("运动特征", featureAnalyzer.analyzeMotionPatterns(frames));
            scores.put("色彩风格", featureAnalyzer.analyzeColorStyle(frames));
            scores.put("细节质量", featureAnalyzer.analyzeDetailQuality(frames));
            
            details.append("基础特征:\n");
            for (Map.Entry<String, Double> entry : scores.entrySet()) {
                details.append(String.format("  - %s: %.2f%%\n", entry.getKey(), entry.getValue() * 100));
            }
            
            // 通义千问特有特征
            Map<String, Double> tongyiFeatures = new HashMap<>();
            tongyiFeatures.put("场景自然度", analyzeSceneNaturalness(frames, scores));
            tongyiFeatures.put("色彩平衡", analyzeColorBalance(frames));
            tongyiFeatures.put("运动合理性", analyzeMotionRealism(frames));
            tongyiFeatures.put("场景转换流畅度", analyzeSceneTransition(frames));
            
            details.append("\n通义千问特征:\n");
            for (Map.Entry<String, Double> entry : tongyiFeatures.entrySet()) {
                details.append(String.format("  - %s: %.2f%%\n", entry.getKey(), entry.getValue() * 100));
            }
            
            // 综合评估
            details.append(String.format("\n综合评估:\n  - 帧数: %d\n", frames.size()));
            details.append(String.format("  - 分辨率: %dx%d\n", frames.get(0).getWidth(), frames.get(0).getHeight()));
            
            result.put("details", details.toString());
            result.put("scores", scores);
            result.put("tongyiFeatures", tongyiFeatures);
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
        details.append("通义千问视频特征分数详情:\n\n");
        
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            details.append(String.format("%s: %.2f%%\n", entry.getKey(), entry.getValue() * 100));
        }
        
        result.put("details", details.toString());
        result.put("scores", scores);
        return result;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        if (result == null) {
            return Arrays.asList("无效的检测结果");
        }
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public List<String> generateSuggestions(double confidence, Map<String, Double> featureScores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence >= 0.80) {
            suggestions.add("视频高度疑似由通义千问视频生成");
            suggestions.add("特征：场景自然、色彩平衡、运动合理");
            
            if (featureScores.getOrDefault("色彩风格", 0.0) >= 0.80) {
                suggestions.add("色彩特征非常典型：饱和度适中、色调平衡");
            }
            if (featureScores.getOrDefault("运动特征", 0.0) >= 0.80) {
                suggestions.add("运动特征明显：符合物理规律、过渡自然");
            }
            if (featureScores.getOrDefault("细节质量", 0.0) >= 0.80) {
                suggestions.add("细节质量良好：纹理清晰、结构合理");
            }
            
        } else if (confidence >= 0.60) {
            suggestions.add("视频可能由通义千问视频生成");
            suggestions.add("建议结合其他检测方法进一步确认");
            
            // 指出弱项
            if (featureScores.getOrDefault("时间一致性", 0.0) < 0.60) {
                suggestions.add("时间一致性较低，可能是视频质量问题");
            }
            if (featureScores.getOrDefault("色彩风格", 0.0) < 0.60) {
                suggestions.add("色彩平衡度不足，可能经过后期处理");
            }
            
        } else if (confidence >= 0.40) {
            suggestions.add("视频具有部分通义千问视频特征");
            suggestions.add("但特征不够明显，可能是其他AI工具生成或真实视频");
            suggestions.add("建议使用多模型检测进行综合判断");
            
        } else {
            suggestions.add("视频不太可能由通义千问视频生成");
            suggestions.add("特征与通义千问视频的典型特征不符");
            
            // 给出可能的其他来源建议
            if (featureScores.getOrDefault("运动特征", 0.0) >= 0.80) {
                suggestions.add("运动特征较强，可能是Runway或Pika生成");
            }
            if (featureScores.getOrDefault("色彩风格", 0.0) >= 0.85) {
                suggestions.add("色彩风格化强，可能是Stable Diffusion系列生成");
            }
        }
        
        return suggestions;
    }
}
