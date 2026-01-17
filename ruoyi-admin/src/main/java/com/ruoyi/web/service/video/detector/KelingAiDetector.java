package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * 可灵AI视频检测器
 * 
 * 特征分析：
 * 1. 运动流畅：运动轨迹自然流畅，无明显顿挫
 * 2. 细节优秀：细节丰富且处理精细
 * 3. 色彩鲜艳：色彩饱和度较高，视觉效果突出
 * 4. 高帧间一致性：帧与帧之间过渡平滑
 * 5. 物理真实性强：符合物理规律，运动合理
 * 
 * @author ruoyi
 */
@Component
public class KelingAiDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "可灵AI";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> videoMetadata) {
        return detectModel(videoFrames);
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> videoFrames) {
        VideoModelDetectionResult result = detectModel(videoFrames);
        return getFeatureDetails(result.getScores());
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), 0.0);
            result.addFeature("无有效帧");
            return result;
        }

        Map<String, Double> scores = new HashMap<>();
        
        // 1. 时间一致性分析（权重25%）
        double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
        scores.put("时间一致性", temporalConsistency);
        
        // 2. 运动特征分析（权重20%）
        double motionPatterns = featureAnalyzer.analyzeMotionPatterns(frames);
        scores.put("运动特征", motionPatterns);
        
        // 3. 细节质量分析（权重20%）
        double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
        scores.put("细节质量", detailQuality);
        
        // 4. 色彩风格分析（权重15%）
        double colorStyle = featureAnalyzer.analyzeColorStyle(frames);
        scores.put("色彩风格", colorStyle);
        
        // 5. 边缘稳定性分析（权重10%）
        double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
        scores.put("边缘稳定性", edgeStability);
        
        // 6. 噪声模式分析（权重5%）
        Map<String, Double> noisePatternResult = featureAnalyzer.analyzeNoisePattern(frames);
        scores.put("噪声模式", noisePatternResult.getOrDefault("noiseLevel", 0.0) * 100);
        
        // 7. 可灵AI特征检测（权重5%）
        double kelingSignature = detectKelingSignature(frames, scores);
        scores.put("可灵AI特征", kelingSignature);
        
        // 计算综合置信度
        double confidence = calculateConfidence(scores);
        
        // 生成特征详情
        Map<String, Object> featureDetails = getFeatureDetails(scores);
        
        // 生成建议
        List<String> suggestions = generateSuggestions(confidence, scores);
        
        VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), confidence);
        result.setScores(scores);
        
        // 将特征详情转换为特征列表
        List<String> features = new ArrayList<>();
        for (Map.Entry<String, Object> entry : featureDetails.entrySet()) {
            features.add(entry.getKey() + ": " + entry.getValue());
        }
        result.setFeatures(features);
        result.setSuggestions(suggestions);
        return result;
    }

    /**
     * 检测可灵AI特有签名
     * 
     * 特征签名：
     * 1. 运动流畅性检测：运动连贯且符合物理规律（满分30分）
     * 2. 细节优秀检测：细节质量>80分（满分25分）
     * 3. 色彩鲜艳检测：色彩饱和度较高，>75分（满分25分）
     * 4. 物理真实性检测：运动符合物理规律（满分20分）
     */
    private double detectKelingSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signatureScore = 0.0;
        
        // 1. 运动流畅性检测（30分）
        double motionSmoothness = analyzeMotionSmoothness(frames);
        double temporalConsistency = scores.get("时间一致性");
        if (motionSmoothness > 0.8 && temporalConsistency >= 85) {
            signatureScore += 30;
        } else if (motionSmoothness > 0.7 && temporalConsistency >= 80) {
            signatureScore += 20;
        } else if (motionSmoothness > 0.6) {
            signatureScore += 10;
        }
        
        // 2. 细节优秀检测（25分）
        double detailQuality = scores.get("细节质量");
        if (detailQuality >= 85) {
            signatureScore += 25;
        } else if (detailQuality >= 80) {
            signatureScore += 18;
        } else if (detailQuality >= 75) {
            signatureScore += 10;
        }
        
        // 3. 色彩鲜艳检测（25分）
        double colorStyle = scores.get("色彩风格");
        if (colorStyle >= 80) {
            signatureScore += 25;
        } else if (colorStyle >= 75) {
            signatureScore += 18;
        } else if (colorStyle >= 70) {
            signatureScore += 10;
        }
        
        // 4. 物理真实性检测（20分）
        double physicsRealism = analyzePhysicsRealism(frames);
        if (physicsRealism > 0.8) {
            signatureScore += 20;
        } else if (physicsRealism > 0.7) {
            signatureScore += 13;
        } else if (physicsRealism > 0.6) {
            signatureScore += 7;
        }
        
        return signatureScore;
    }

    /**
     * 分析运动流畅性
     * 检测运动轨迹的平滑程度和连贯性
     */
    private double analyzeMotionSmoothness(List<BufferedImage> frames) {
        if (frames.size() < 3) {
            return 0.0;
        }
        
        List<Double> motionVectors = new ArrayList<>();
        
        // 计算运动向量的变化
        for (int i = 0; i < frames.size() - 2; i++) {
            BufferedImage frame1 = frames.get(i);
            BufferedImage frame2 = frames.get(i + 1);
            BufferedImage frame3 = frames.get(i + 2);
            
            double motion1 = calculateMotionMagnitude(frame1, frame2);
            double motion2 = calculateMotionMagnitude(frame2, frame3);
            
            // 运动加速度（二阶导数）
            double acceleration = Math.abs(motion2 - motion1);
            motionVectors.add(acceleration);
        }
        
        // 分析运动加速度的平滑性
        double meanAcceleration = motionVectors.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = motionVectors.stream()
            .mapToDouble(d -> Math.pow(d - meanAcceleration, 2))
            .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        // 流畅的运动特征：加速度变化小，标准差低
        double smoothness = 0.0;
        
        // 加速度平均值适中
        if (meanAcceleration < 5) {
            smoothness += 0.5;
        } else if (meanAcceleration < 10) {
            smoothness += 0.3;
        }
        
        // 加速度波动性小
        if (stdDev < 3) {
            smoothness += 0.5;
        } else if (stdDev < 5) {
            smoothness += 0.3;
        }
        
        return smoothness;
    }

    /**
     * 计算运动幅度
     */
    private double calculateMotionMagnitude(BufferedImage frame1, BufferedImage frame2) {
        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());
        
        long totalDiff = 0;
        int sampleStep = 15;
        int sampleCount = 0;
        
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
                
                int diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                totalDiff += diff;
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? (double) totalDiff / sampleCount / 3.0 : 0.0;
    }

    /**
     * 分析物理真实性
     * 检测运动是否符合物理规律
     */
    private double analyzePhysicsRealism(List<BufferedImage> frames) {
        if (frames.size() < 4) {
            return 0.0;
        }
        
        double realismScore = 0.0;
        
        // 1. 运动连续性分析
        double motionContinuity = analyzeMotionContinuity(frames);
        realismScore += motionContinuity * 0.4;
        
        // 2. 重力效应分析
        double gravityEffect = analyzeGravityEffect(frames);
        realismScore += gravityEffect * 0.3;
        
        // 3. 惯性效应分析
        double inertiaEffect = analyzeInertiaEffect(frames);
        realismScore += inertiaEffect * 0.3;
        
        return realismScore;
    }

    /**
     * 分析运动连续性
     */
    private double analyzeMotionContinuity(List<BufferedImage> frames) {
        List<Double> motionChanges = new ArrayList<>();
        
        for (int i = 0; i < frames.size() - 1; i++) {
            double motion = calculateMotionMagnitude(frames.get(i), frames.get(i + 1));
            motionChanges.add(motion);
        }
        
        // 检查运动是否连续（相邻帧的运动幅度不应突变）
        int continuousCount = 0;
        for (int i = 0; i < motionChanges.size() - 1; i++) {
            double ratio = motionChanges.get(i + 1) / (motionChanges.get(i) + 1.0);
            if (ratio > 0.7 && ratio < 1.3) {
                continuousCount++;
            }
        }
        
        return motionChanges.size() > 1 ? (double) continuousCount / (motionChanges.size() - 1) : 0.0;
    }

    /**
     * 分析重力效应
     * 检测向下运动是否有加速趋势
     */
    private double analyzeGravityEffect(List<BufferedImage> frames) {
        // 分析图像整体亮度中心的垂直位置变化
        List<Double> verticalCenters = new ArrayList<>();
        
        for (BufferedImage frame : frames) {
            double verticalCenter = calculateVerticalBrightnessCenter(frame);
            verticalCenters.add(verticalCenter);
        }
        
        // 检查是否存在自然的加速或减速模式
        int naturalMotionCount = 0;
        for (int i = 0; i < verticalCenters.size() - 2; i++) {
            double v1 = verticalCenters.get(i + 1) - verticalCenters.get(i);
            double v2 = verticalCenters.get(i + 2) - verticalCenters.get(i + 1);
            
            // 如果速度变化较小或呈现加速趋势，认为是自然运动
            if (Math.abs(v2 - v1) < 0.05 || (v2 > v1 && v1 > 0)) {
                naturalMotionCount++;
            }
        }
        
        return verticalCenters.size() > 2 ? (double) naturalMotionCount / (verticalCenters.size() - 2) : 0.0;
    }

    /**
     * 计算垂直亮度中心
     */
    private double calculateVerticalBrightnessCenter(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        double totalBrightness = 0.0;
        double weightedSum = 0.0;
        int sampleStep = 10;
        
        for (int y = 0; y < height; y += sampleStep) {
            for (int x = 0; x < width; x += sampleStep) {
                int rgb = frame.getRGB(x, y);
                
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                double brightness = (r + g + b) / 3.0;
                totalBrightness += brightness;
                weightedSum += brightness * y;
            }
        }
        
        return totalBrightness > 0 ? weightedSum / totalBrightness / height : 0.5;
    }

    /**
     * 分析惯性效应
     * 检测运动物体是否保持运动状态
     */
    private double analyzeInertiaEffect(List<BufferedImage> frames) {
        List<Double> motionMagnitudes = new ArrayList<>();
        
        for (int i = 0; i < frames.size() - 1; i++) {
            double magnitude = calculateMotionMagnitude(frames.get(i), frames.get(i + 1));
            motionMagnitudes.add(magnitude);
        }
        
        // 检查运动幅度是否保持相对稳定（惯性效应）
        if (motionMagnitudes.size() < 2) {
            return 0.0;
        }
        
        double meanMagnitude = motionMagnitudes.stream().mapToDouble(d -> d).average().orElse(0.0);
        
        int inertiaCount = 0;
        for (double magnitude : motionMagnitudes) {
            // 如果运动幅度在平均值的70%-130%范围内，认为保持了惯性
            if (magnitude > meanMagnitude * 0.7 && magnitude < meanMagnitude * 1.3) {
                inertiaCount++;
            }
        }
        
        return (double) inertiaCount / motionMagnitudes.size();
    }

    /**
     * 计算综合置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        // 基础加权计算
        double confidence = 0.0;
        confidence += scores.get("时间一致性") * 0.25;
        confidence += scores.get("运动特征") * 0.20;
        confidence += scores.get("细节质量") * 0.20;
        confidence += scores.get("色彩风格") * 0.15;
        confidence += scores.get("边缘稳定性") * 0.10;
        confidence += scores.get("噪声模式") * 0.05;
        confidence += scores.get("可灵AI特征") * 0.05;
        
        // 特征组合加成
        // 如果运动流畅、细节优秀、色彩鲜艳都达标，增加置信度
        if (scores.get("时间一致性") >= 85 &&
            scores.get("细节质量") >= 80 &&
            scores.get("色彩风格") >= 75 &&
            scores.get("可灵AI特征") >= 65) {
            confidence *= 1.18;
        }
        
        return Math.min(confidence, 100.0);
    }

    /**
     * 获取特征详情
     * @return
     */
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> features = new HashMap<>();
        features.put("model_name", getModelName());
        features.put("temporal_consistency", scores.get("时间一致性"));
        features.put("motion_patterns", scores.get("运动特征"));
        features.put("detail_quality", scores.get("细节质量"));
        features.put("color_style", scores.get("色彩风格"));
        features.put("keling_signature", scores.get("可灵AI特征"));
        return features;
    }

    /**
     * 生成优化建议
     */
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence >= 80) {
            suggestions.add("视频极可能由可灵AI生成");
            suggestions.add("建议进行人工复审确认");
            
            if (scores.get("可灵AI特征") >= 70) {
                suggestions.add("检测到明显的可灵AI生成签名");
            }
            if (scores.get("时间一致性") >= 85) {
                suggestions.add("帧间一致性极高，符合可灵AI特征");
            }
            if (scores.get("细节质量") >= 85) {
                suggestions.add("细节处理优秀，符合可灵AI风格");
            }
            if (scores.get("色彩风格") >= 80) {
                suggestions.add("色彩鲜艳度符合可灵AI特征");
            }
            
        } else if (confidence >= 60) {
            suggestions.add("视频很可能由可灵AI生成");
            suggestions.add("建议结合其他检测方法综合判断");
            
            if (scores.get("运动特征") >= 70) {
                suggestions.add("运动流畅性较符合可灵AI特征");
            }
            
        } else if (confidence >= 40) {
            suggestions.add("视频可能由可灵AI生成");
            suggestions.add("存在部分可灵AI特征，但不够明显");
            
        } else {
            suggestions.add("视频不太可能由可灵AI生成");
            suggestions.add("可能为真实拍摄或其他AI工具生成");
        }
        
        return suggestions;
    }
}
