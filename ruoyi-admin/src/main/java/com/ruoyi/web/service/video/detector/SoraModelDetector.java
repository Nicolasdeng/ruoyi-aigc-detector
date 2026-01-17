package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Sora模型检测器
 * 
 * Sora视频生成特征：
 * 1. 超高质量 - 细节极其丰富，纹理清晰
 * 2. 物理真实性极强 - 完美遵循物理规律
 * 3. 长时间一致性 - 长视频保持高度一致
 * 4. 复杂场景理解 - 多对象交互自然
 * 5. 细节极其丰富 - 细微细节表现出色
 * 6. 光影效果真实 - 光照和阴影符合物理规律
 * 7. 运动连贯性强 - 运动轨迹平滑自然
 * 8. 高级语义理解 - 场景逻辑合理
 * 
 * @author ruoyi
 */
@Component
public class SoraModelDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Sora";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames) {
        return detectModel(videoFrames, null);
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> metadata) {
        List<BufferedImage> frames = videoFrames;
        Map<String, Double> scores = new HashMap<>();
        
        // 1. 时间一致性分析（30%权重）- Sora极强的长时间一致性
        double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
        scores.put("时间一致性", temporalConsistency);
        
        // 2. 细节质量分析（25%权重）- Sora的超高细节质量
        double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
        scores.put("细节质量", detailQuality);
        
        // 3. 运动特征分析（20%权重）- Sora的物理真实运动
        double motionScore = featureAnalyzer.analyzeMotionPatterns(frames);
        scores.put("运动特征", motionScore);
        
        // 4. 光照一致性分析（15%权重）- Sora的真实光影效果
        double lightingConsistency = featureAnalyzer.analyzeLightingConsistency(frames);
        scores.put("光照一致性", lightingConsistency);
        
        // 5. 边缘稳定性分析（5%权重）- Sora的边缘清晰稳定
        double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
        scores.put("边缘稳定性", edgeStability);
        
        // 6. Sora特有特征检测（5%权重）
        double soraSignature = detectSoraSignature(frames, scores);
        scores.put("Sora特征", soraSignature);
        
        // 计算综合置信度
        double confidence = calculateConfidence(scores);
        
        // 构造结果对象
        VideoModelDetectionResult result = new VideoModelDetectionResult(
            getModelName(),
            confidence
        );
        
        // 设置分数和建议
        result.setScores(scores);
        result.setSuggestions(generateSuggestions(confidence, scores));
        
        // 添加特征描述
        Map<String, Object> featureDetails = getFeatureDetails(scores);
        for (Map.Entry<String, Object> entry : featureDetails.entrySet()) {
            result.addFeature(entry.getKey() + ": " + entry.getValue());
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
        return getFeatureDetailsFromScores(scores);
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
     * 检测Sora特有签名特征
     */
    private double detectSoraSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signatureScore = 0.0;
        
        // 特征1: 超高质量检测（35分）
        // Sora的细节质量通常>=90，时间一致性>=90
        double ultraQuality = analyzeUltraQuality(frames, scores);
        if (ultraQuality > 0.9) {
            signatureScore += 35;
        } else if (ultraQuality > 0.8) {
            signatureScore += 20;
        }
        
        // 特征2: 物理真实性检测（30分）
        // Sora对物理规律的遵循极其精确
        double physicsRealism = analyzePhysicsRealism(frames);
        if (physicsRealism > 0.9) {
            signatureScore += 30;
        } else if (physicsRealism > 0.8) {
            signatureScore += 15;
        }
        
        // 特征3: 复杂场景理解检测（20分）
        // Sora对多对象、复杂场景的处理能力
        double sceneComplexity = analyzeSceneComplexity(frames);
        if (sceneComplexity > 0.85) {
            signatureScore += 20;
        } else if (sceneComplexity > 0.7) {
            signatureScore += 10;
        }
        
        // 特征4: 光影真实性检测（15分）
        // Sora的光照和阴影符合物理规律
        double lightingRealism = analyzeLightingRealism(frames, scores);
        if (lightingRealism > 0.9) {
            signatureScore += 15;
        } else if (lightingRealism > 0.8) {
            signatureScore += 8;
        }
        
        return signatureScore;
    }

    /**
     * 分析超高质量特征
     * Sora的特点是细节质量和时间一致性都极高
     */
    private double analyzeUltraQuality(List<BufferedImage> frames, Map<String, Double> scores) {
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        
        // Sora的超高质量体现在两方面都很高
        if (detailQuality >= 90 && temporalConsistency >= 90) {
            return 1.0;
        } else if (detailQuality >= 85 && temporalConsistency >= 85) {
            return 0.85;
        } else if (detailQuality >= 80 && temporalConsistency >= 80) {
            return 0.7;
        }
        
        return (detailQuality + temporalConsistency) / 200.0;
    }

    /**
     * 分析物理真实性
     * Sora对物理规律的遵循极其精确
     */
    private double analyzePhysicsRealism(List<BufferedImage> frames) {
        if (frames.size() < 3) {
            return 0.5;
        }
        
        double totalRealism = 0.0;
        int validSamples = 0;
        
        // 分析运动的物理真实性
        double motionRealism = analyzeMotionPhysics(frames);
        totalRealism += motionRealism;
        validSamples++;
        
        // 分析重力效应
        double gravityRealism = analyzeGravityRealism(frames);
        totalRealism += gravityRealism;
        validSamples++;
        
        // 分析惯性效应
        double inertiaRealism = analyzeInertiaRealism(frames);
        totalRealism += inertiaRealism;
        validSamples++;
        
        return validSamples > 0 ? totalRealism / validSamples : 0.5;
    }

    /**
     * 分析运动的物理真实性
     */
    private double analyzeMotionPhysics(List<BufferedImage> frames) {
        double totalPhysics = 0.0;
        int validPairs = 0;
        
        for (int i = 1; i < frames.size(); i++) {
            BufferedImage prev = frames.get(i - 1);
            BufferedImage curr = frames.get(i);
            
            // 计算帧间变化的物理合理性
            double motionChange = calculateFrameDifference(prev, curr);
            
            // Sora的运动变化通常很平滑且符合物理规律
            // 变化既不会太突兀也不会完全静止
            if (motionChange > 0.02 && motionChange < 0.3) {
                totalPhysics += 1.0;
            } else if (motionChange > 0.01 && motionChange < 0.4) {
                totalPhysics += 0.7;
            } else {
                totalPhysics += 0.3;
            }
            
            validPairs++;
        }
        
        return validPairs > 0 ? totalPhysics / validPairs : 0.5;
    }

    /**
     * 分析重力效应真实性
     */
    private double analyzeGravityRealism(List<BufferedImage> frames) {
        // Sora生成的视频中，物体下落等重力效应符合物理规律
        // 通过分析垂直方向的运动模式
        
        double totalGravity = 0.0;
        int validSamples = 0;
        
        for (int i = 2; i < frames.size(); i++) {
            BufferedImage prev2 = frames.get(i - 2);
            BufferedImage prev1 = frames.get(i - 1);
            BufferedImage curr = frames.get(i);
            
            // 分析垂直方向的加速度变化
            double accel = analyzeVerticalAcceleration(prev2, prev1, curr);
            
            // Sora的重力加速度通常符合物理规律
            if (Math.abs(accel - 9.8) < 2.0 || accel < 0.1) {
                totalGravity += 1.0;
            } else if (Math.abs(accel - 9.8) < 5.0) {
                totalGravity += 0.7;
            } else {
                totalGravity += 0.4;
            }
            
            validSamples++;
        }
        
        return validSamples > 0 ? totalGravity / validSamples : 0.5;
    }

    /**
     * 分析垂直加速度
     */
    private double analyzeVerticalAcceleration(BufferedImage prev2, BufferedImage prev1, BufferedImage curr) {
        // 简化实现：通过像素变化估算垂直运动
        double v1 = calculateVerticalMotion(prev2, prev1);
        double v2 = calculateVerticalMotion(prev1, curr);
        
        // 加速度 = 速度变化
        return Math.abs(v2 - v1) * 100; // 放大以便比较
    }

    /**
     * 计算垂直运动量
     */
    private double calculateVerticalMotion(BufferedImage prev, BufferedImage curr) {
        int width = Math.min(prev.getWidth(), curr.getWidth());
        int height = Math.min(prev.getHeight(), curr.getHeight());
        
        long verticalDiff = 0;
        int sampleCount = 0;
        int step = Math.max(1, height / 10);
        
        for (int y = step; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb1 = prev.getRGB(x, y);
                int rgb2 = curr.getRGB(x, y);
                
                verticalDiff += Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));
                sampleCount++;
            }
        }
        
        return sampleCount > 0 ? (double) verticalDiff / sampleCount : 0.0;
    }

    /**
     * 分析惯性效应真实性
     */
    private double analyzeInertiaRealism(List<BufferedImage> frames) {
        // Sora生成的视频中，物体运动的惯性效应真实
        // 运动物体不会突然停止或改变方向
        
        double totalInertia = 0.0;
        int validSamples = 0;
        
        for (int i = 2; i < frames.size(); i++) {
            BufferedImage prev2 = frames.get(i - 2);
            BufferedImage prev1 = frames.get(i - 1);
            BufferedImage curr = frames.get(i);
            
            // 分析运动方向的连续性
            double directionChange = analyzeMotionDirection(prev2, prev1, curr);
            
            // Sora的运动方向变化通常很平滑
            if (directionChange < 0.1) {
                totalInertia += 1.0;
            } else if (directionChange < 0.3) {
                totalInertia += 0.8;
            } else {
                totalInertia += 0.5;
            }
            
            validSamples++;
        }
        
        return validSamples > 0 ? totalInertia / validSamples : 0.5;
    }

    /**
     * 分析运动方向变化
     */
    private double analyzeMotionDirection(BufferedImage prev2, BufferedImage prev1, BufferedImage curr) {
        // 简化实现：通过像素差异的分布估算运动方向变化
        double motion1 = calculateFrameDifference(prev2, prev1);
        double motion2 = calculateFrameDifference(prev1, curr);
        
        return Math.abs(motion2 - motion1);
    }

    /**
     * 分析复杂场景理解能力
     * Sora对多对象、复杂场景的处理能力强
     */
    private double analyzeSceneComplexity(List<BufferedImage> frames) {
        if (frames.isEmpty()) {
            return 0.5;
        }
        
        double totalComplexity = 0.0;
        
        for (BufferedImage frame : frames) {
            // 分析场景复杂度
            double complexity = calculateSceneComplexity(frame);
            
            // Sora能很好地处理复杂场景
            if (complexity > 0.7) {
                totalComplexity += 1.0;
            } else if (complexity > 0.5) {
                totalComplexity += 0.8;
            } else {
                totalComplexity += 0.6;
            }
        }
        
        return frames.size() > 0 ? totalComplexity / frames.size() : 0.5;
    }

    /**
     * 计算场景复杂度
     */
    private double calculateSceneComplexity(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        // 计算颜色种类数（简化实现）
        Set<Integer> colors = new HashSet<>();
        int step = Math.max(1, width / 50);
        
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb = frame.getRGB(x, y);
                // 将RGB简化到16级
                int r = ((rgb >> 16) & 0xFF) / 16;
                int g = ((rgb >> 8) & 0xFF) / 16;
                int b = (rgb & 0xFF) / 16;
                colors.add((r << 8) | (g << 4) | b);
            }
        }
        
        // 颜色种类越多，场景越复杂
        // 最多4096种简化颜色
        return Math.min(1.0, colors.size() / 2048.0);
    }

    /**
     * 分析光影真实性
     * Sora的光照和阴影符合物理规律
     */
    private double analyzeLightingRealism(List<BufferedImage> frames, Map<String, Double> scores) {
        double lightingConsistency = scores.getOrDefault("光照一致性", 0.0);
        
        // Sora的光照一致性通常很高
        if (lightingConsistency >= 90) {
            return 1.0;
        } else if (lightingConsistency >= 85) {
            return 0.9;
        } else if (lightingConsistency >= 80) {
            return 0.8;
        }
        
        return lightingConsistency / 100.0;
    }

    /**
     * 计算帧间差异
     */
    private double calculateFrameDifference(BufferedImage frame1, BufferedImage frame2) {
        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());
        
        long totalDiff = 0;
        int pixelCount = 0;
        int step = Math.max(1, width / 100); // 采样以提高性能
        
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
                
                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                pixelCount++;
            }
        }
        
        // 归一化到0-1范围
        return pixelCount > 0 ? (double) totalDiff / (pixelCount * 255.0 * 3.0) : 0.0;
    }

    /**
     * 计算置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        double confidence = 0.0;
        
        // 加权计算
        confidence += scores.getOrDefault("时间一致性", 0.0) * 0.30;
        confidence += scores.getOrDefault("细节质量", 0.0) * 0.25;
        confidence += scores.getOrDefault("运动特征", 0.0) * 0.20;
        confidence += scores.getOrDefault("光照一致性", 0.0) * 0.15;
        confidence += scores.getOrDefault("边缘稳定性", 0.0) * 0.05;
        confidence += scores.getOrDefault("Sora特征", 0.0) * 0.05;
        
        // Sora特征组合加成
        // 当多个核心特征都很高时，说明很可能是Sora生成
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double lightingConsistency = scores.getOrDefault("光照一致性", 0.0);
        double soraSignature = scores.getOrDefault("Sora特征", 0.0);
        
        if (temporalConsistency >= 90 && detailQuality >= 90 && 
            lightingConsistency >= 85 && soraSignature >= 70) {
            confidence *= 1.25; // Sora的标志性超高质量
        } else if (temporalConsistency >= 85 && detailQuality >= 85 && 
                   lightingConsistency >= 80 && soraSignature >= 60) {
            confidence *= 1.15;
        }
        
        return Math.min(100.0, confidence);
    }

    /**
     * 从分数生成特征详情
     */
    private Map<String, Object> getFeatureDetailsFromScores(Map<String, Double> scores) {
        Map<String, Object> details = new HashMap<>();
        
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double motionScore = scores.getOrDefault("运动特征", 0.0);
        double lightingConsistency = scores.getOrDefault("光照一致性", 0.0);
        double soraSignature = scores.getOrDefault("Sora特征", 0.0);
        
        // 时间一致性评估
        if (temporalConsistency >= 90) {
            details.put("时间一致性", "极强 - 长时间保持高度一致");
        } else if (temporalConsistency >= 85) {
            details.put("时间一致性", "很强 - 一致性表现优秀");
        } else if (temporalConsistency >= 80) {
            details.put("时间一致性", "较强 - 一致性良好");
        } else {
            details.put("时间一致性", "一般 - 一致性中等");
        }
        
        // 细节质量评估
        if (detailQuality >= 90) {
            details.put("细节质量", "极高 - 细节极其丰富");
        } else if (detailQuality >= 85) {
            details.put("细节质量", "很高 - 细节表现优秀");
        } else if (detailQuality >= 80) {
            details.put("细节质量", "较高 - 细节良好");
        } else {
            details.put("细节质量", "中等 - 细节一般");
        }
        
        // 运动特征评估
        if (motionScore >= 85) {
            details.put("运动特征", "真实 - 完美符合物理规律");
        } else if (motionScore >= 75) {
            details.put("运动特征", "自然 - 运动合理");
        } else {
            details.put("运动特征", "一般 - 运动基本合理");
        }
        
        // 光照一致性评估
        if (lightingConsistency >= 90) {
            details.put("光照效果", "真实 - 光影符合物理规律");
        } else if (lightingConsistency >= 85) {
            details.put("光照效果", "自然 - 光照合理");
        } else if (lightingConsistency >= 80) {
            details.put("光照效果", "良好 - 光照较为合理");
        } else {
            details.put("光照效果", "一般 - 光照基本合理");
        }
        
        // Sora特有特征评估
        if (soraSignature >= 70) {
            details.put("Sora特征", "明显 - 具有典型的Sora特征");
        } else if (soraSignature >= 50) {
            details.put("Sora特征", "较明显 - 部分Sora特征");
        } else {
            details.put("Sora特征", "不明显 - Sora特征较弱");
        }
        
        return details;
    }

    /**
     * 从分数生成建议
     */
    private List<String> generateSuggestionsFromScores(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence >= 80) {
            suggestions.add("视频极可能由Sora生成");
            suggestions.add("特征：超高质量、物理真实性极强、长时间一致性");
            suggestions.add("建议：重点关注细节完美度、物理规律遵循度");
        } else if (confidence >= 60) {
            suggestions.add("视频较可能由Sora生成");
            suggestions.add("建议：进一步分析物理真实性和场景复杂度");
        } else if (confidence >= 40) {
            suggestions.add("视频可能由Sora生成，但特征不够典型");
            suggestions.add("建议：对比其他高端视频生成模型特征");
        } else {
            suggestions.add("视频不太像Sora生成");
            suggestions.add("Sora通常具有极高的质量和物理真实性");
        }
        
        // 基于具体分数的建议
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double soraSignature = scores.getOrDefault("Sora特征", 0.0);
        
        if (temporalConsistency >= 90 && detailQuality >= 90) {
            suggestions.add("检测到Sora标志性的超高质量特征");
        }
        
        if (soraSignature >= 70) {
            suggestions.add("检测到明显的Sora特有特征：物理真实性、复杂场景理解");
        }
        
        return suggestions;
    }
}
