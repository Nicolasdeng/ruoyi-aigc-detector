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
 * Gen-1模型检测器
 * 
 * Gen-1视频生成特征（Runway早期版本）：
 * 1. 风格化转换 - 擅长将视频转换为不同艺术风格
 * 2. 结构保持 - 保持原始视频的结构和运动
 * 3. 纹理生成 - 生成风格化纹理
 * 4. 运动保持 - 运动模式与原视频一致
 * 5. 色彩风格化 - 强烈的风格化色彩
 * 6. 边缘处理 - 边缘可能被风格化
 * 7. 时间一致性 - 中等到良好的时间一致性
 * 8. 艺术感强 - 明显的艺术化处理
 * 
 * @author ruoyi
 */
@Component
public class Gen1ModelDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Gen-1";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> videoMetadata) {
        // 带元数据的检测方法，直接调用基础检测方法
        // 元数据可用于未来的增强检测逻辑
        return detectModel(videoFrames);
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        Map<String, Double> scores = new HashMap<>();
        
        // 1. 时间一致性分析（20%权重）- Gen-1的中等时间一致性
        double temporalConsistency = featureAnalyzer.analyzeTemporalConsistency(frames);
        scores.put("时间一致性", temporalConsistency);
        
        // 2. 色彩风格分析（25%权重）- Gen-1的强烈风格化色彩
        double colorStyle = featureAnalyzer.analyzeColorStyle(frames);
        scores.put("色彩风格", colorStyle);
        
        // 3. 运动特征分析（20%权重）- Gen-1的运动保持能力
        double motionScore = featureAnalyzer.analyzeMotionPatterns(frames);
        scores.put("运动特征", motionScore);
        
        // 4. 细节质量分析（15%权重）- Gen-1的纹理生成质量
        double detailQuality = featureAnalyzer.analyzeDetailQuality(frames);
        scores.put("细节质量", detailQuality);
        
        // 5. 边缘稳定性分析（10%权重）- Gen-1的边缘风格化
        double edgeStability = featureAnalyzer.analyzeEdgeStability(frames);
        scores.put("边缘稳定性", edgeStability);
        
        // 6. 噪声模式分析（5%权重）- Gen-1的艺术化噪声
        Map<String, Double> noisePatternResult = featureAnalyzer.analyzeNoisePattern(frames);
        scores.put("噪声模式", noisePatternResult.getOrDefault("noiseLevel", 0.0) * 100);
        
        // 7. Gen-1特有特征检测（5%权重）
        double gen1Signature = detectGen1Signature(frames, scores);
        scores.put("Gen1特征", gen1Signature);
        
        // 计算综合置信度
        double confidence = calculateConfidence(scores);
        
        // 获取特征详情
        Map<String, Object> featureDetails = getFeatureDetails(scores);
        
        // 生成建议
        List<String> suggestions = generateSuggestions(confidence, scores);
        
        // 创建结果对象
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
     * 检测Gen-1特有签名特征
     */
    private double detectGen1Signature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signatureScore = 0.0;
        
        // 特征1: 风格化强度检测（35分）
        // Gen-1的核心特点是强烈的风格化转换
        double styleIntensity = analyzeStyleIntensity(frames, scores);
        if (styleIntensity > 0.8) {
            signatureScore += 35;
        } else if (styleIntensity > 0.7) {
            signatureScore += 25;
        } else if (styleIntensity > 0.6) {
            signatureScore += 15;
        }
        
        // 特征2: 结构保持检测（30分）
        // Gen-1在风格化的同时保持原始结构
        double structurePreservation = analyzeStructurePreservation(frames);
        if (structurePreservation > 0.8) {
            signatureScore += 30;
        } else if (structurePreservation > 0.7) {
            signatureScore += 20;
        } else if (structurePreservation > 0.6) {
            signatureScore += 10;
        }
        
        // 特征3: 艺术化纹理检测（20分）
        // Gen-1生成的艺术化纹理特征
        double artisticTexture = analyzeArtisticTexture(frames);
        if (artisticTexture > 0.75) {
            signatureScore += 20;
        } else if (artisticTexture > 0.6) {
            signatureScore += 12;
        } else if (artisticTexture > 0.5) {
            signatureScore += 6;
        }
        
        // 特征4: 运动保持检测（15分）
        // Gen-1保持原始视频的运动模式
        double motionPreservation = analyzeMotionPreservation(frames, scores);
        if (motionPreservation > 0.8) {
            signatureScore += 15;
        } else if (motionPreservation > 0.7) {
            signatureScore += 10;
        } else if (motionPreservation > 0.6) {
            signatureScore += 5;
        }
        
        return signatureScore;
    }

    /**
     * 分析风格化强度
     * Gen-1的特点是强烈的风格化转换
     */
    private double analyzeStyleIntensity(List<BufferedImage> frames, Map<String, Double> scores) {
        double colorStyle = scores.getOrDefault("色彩风格", 0.0);
        double noisePattern = scores.getOrDefault("噪声模式", 0.0) / 100.0;
        
        // Gen-1的色彩风格通常很强烈
        double styleScore = 0.0;
        
        if (colorStyle >= 80) {
            styleScore += 0.5;
        } else if (colorStyle >= 70) {
            styleScore += 0.35;
        } else if (colorStyle >= 60) {
            styleScore += 0.2;
        }
        
        // 分析色彩饱和度变化
        double saturationVariation = analyzeSaturationVariation(frames);
        if (saturationVariation > 0.3) {
            styleScore += 0.3;
        } else if (saturationVariation > 0.2) {
            styleScore += 0.2;
        } else if (saturationVariation > 0.1) {
            styleScore += 0.1;
        }
        
        // 艺术化噪声
        if (noisePattern >= 60) {
            styleScore += 0.2;
        } else if (noisePattern >= 50) {
            styleScore += 0.1;
        }
        
        return Math.min(1.0, styleScore);
    }

    /**
     * 分析饱和度变化
     */
    private double analyzeSaturationVariation(List<BufferedImage> frames) {
        if (frames.isEmpty()) {
            return 0.0;
        }
        
        double totalVariation = 0.0;
        
        for (BufferedImage frame : frames) {
            double saturation = VideoDetectionUtils.calculateSaturation(frame);
            totalVariation += saturation;
        }
        
        return frames.size() > 0 ? totalVariation / frames.size() : 0.0;
    }


    /**
     * 分析结构保持能力
     * Gen-1在风格化的同时保持原始结构
     */
    private double analyzeStructurePreservation(List<BufferedImage> frames) {
        if (frames.size() < 2) {
            return 0.5;
        }
        
        double totalPreservation = 0.0;
        int validPairs = 0;
        
        for (int i = 1; i < frames.size(); i++) {
            BufferedImage prev = frames.get(i - 1);
            BufferedImage curr = frames.get(i);
            
            // 计算结构相似度（使用基类方法）
            double structureSimilarity = calculateStructureSimilarity(prev, curr);
            
            // Gen-1的结构相似度通常较高
            if (structureSimilarity > 0.7) {
                totalPreservation += 1.0;
            } else if (structureSimilarity > 0.6) {
                totalPreservation += 0.8;
            } else if (structureSimilarity > 0.5) {
                totalPreservation += 0.6;
            } else {
                totalPreservation += 0.3;
            }
            
            validPairs++;
        }
        
        return validPairs > 0 ? totalPreservation / validPairs : 0.5;
    }

    /**
     * 分析艺术化纹理
     * Gen-1生成的艺术化纹理特征
     */
    private double analyzeArtisticTexture(List<BufferedImage> frames) {
        if (frames.isEmpty()) {
            return 0.5;
        }
        
        double totalTexture = 0.0;
        
        for (BufferedImage frame : frames) {
            // 分析纹理的艺术化程度
            double textureScore = calculateTextureArtistry(frame);
            totalTexture += textureScore;
        }
        
        return frames.size() > 0 ? totalTexture / frames.size() : 0.5;
    }

    /**
     * 计算纹理艺术化程度
     */
    private double calculateTextureArtistry(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        // 计算纹理复杂度（使用基类方法）
        double complexity = calculateTextureComplexity(frame, width, height);
        
        // 计算纹理规律性（艺术化纹理通常有一定规律）
        double regularity = calculateTextureRegularity(frame, width, height);
        
        // 艺术化纹理：中等到高复杂度 + 一定规律性
        double artistryScore = 0.0;
        
        if (complexity > 0.5 && complexity < 0.8) {
            artistryScore += 0.5;
        } else if (complexity >= 0.4) {
            artistryScore += 0.3;
        }
        
        if (regularity > 0.3 && regularity < 0.7) {
            artistryScore += 0.5;
        } else if (regularity >= 0.2) {
            artistryScore += 0.3;
        }
        
        return Math.min(1.0, artistryScore);
    }

    /**
     * 计算纹理复杂度
     */
    private double calculateTextureComplexity(BufferedImage frame, int width, int height) {
        return VideoDetectionUtils.calculateTextureComplexity(frame);
    }

    /**
     * 计算纹理规律性
     */
    private double calculateTextureRegularity(BufferedImage frame, int width, int height) {
        // 简化实现：通过检测重复模式来判断规律性
        int patternMatches = 0;
        int totalChecks = 0;
        int step = Math.max(1, width / 20);
        int patternSize = 5;
        
        for (int y = 0; y < height - patternSize * 2; y += step) {
            for (int x = 0; x < width - patternSize * 2; x += step) {
                // 比较相邻区域的相似性
                boolean similar = areRegionsSimilar(frame, x, y, x + patternSize, y, patternSize);
                if (similar) {
                    patternMatches++;
                }
                totalChecks++;
            }
        }
        
        return totalChecks > 0 ? (double) patternMatches / totalChecks : 0.0;
    }

    /**
     * 比较两个区域是否相似
     */
    private boolean areRegionsSimilar(BufferedImage frame, int x1, int y1, int x2, int y2, int size) {
        int diffThreshold = 30;
        int totalDiff = 0;
        int pixelCount = 0;
        
        for (int dy = 0; dy < size && y1 + dy < frame.getHeight() && y2 + dy < frame.getHeight(); dy++) {
            for (int dx = 0; dx < size && x1 + dx < frame.getWidth() && x2 + dx < frame.getWidth(); dx++) {
                int rgb1 = frame.getRGB(x1 + dx, y1 + dy);
                int rgb2 = frame.getRGB(x2 + dx, y2 + dy);
                
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
        
        double avgDiff = pixelCount > 0 ? (double) totalDiff / pixelCount : 0;
        return avgDiff < diffThreshold;
    }

    /**
     * 计算结构相似度
     */
    private double calculateStructureSimilarity(BufferedImage frame1, BufferedImage frame2) {
        return 1.0 - VideoDetectionUtils.calculateFrameDifference(frame1, frame2);
    }

    /**
     * 分析运动保持能力
     * Gen-1保持原始视频的运动模式
     */
    private double analyzeMotionPreservation(List<BufferedImage> frames, Map<String, Double> scores) {
        double motionScore = scores.getOrDefault("运动特征", 0.0);
        
        // Gen-1的运动保持通常较好
        if (motionScore >= 75) {
            return 1.0;
        } else if (motionScore >= 65) {
            return 0.8;
        } else if (motionScore >= 55) {
            return 0.6;
        }
        
        return motionScore / 100.0;
    }

    /**
     * 计算综合置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        double confidence = 0.0;
        
        // 加权计算
        confidence += scores.getOrDefault("时间一致性", 0.0) * 0.20;
        confidence += scores.getOrDefault("色彩风格", 0.0) * 0.25;
        confidence += scores.getOrDefault("运动特征", 0.0) * 0.20;
        confidence += scores.getOrDefault("细节质量", 0.0) * 0.15;
        confidence += scores.getOrDefault("边缘稳定性", 0.0) * 0.10;
        confidence += scores.getOrDefault("噪声模式", 0.0) * 0.05;
        confidence += scores.getOrDefault("Gen1特征", 0.0) * 0.05;
        
        // Gen-1特征组合加成
        // 当风格化强、结构保持好时，很可能是Gen-1
        double colorStyle = scores.getOrDefault("色彩风格", 0.0);
        double motionScore = scores.getOrDefault("运动特征", 0.0);
        double gen1Signature = scores.getOrDefault("Gen1特征", 0.0);
        
        if (colorStyle >= 80 && motionScore >= 70 && gen1Signature >= 70) {
            confidence *= 1.20; // Gen-1的标志性风格化特征
        } else if (colorStyle >= 75 && motionScore >= 65 && gen1Signature >= 60) {
            confidence *= 1.12;
        }
        
        return Math.min(100.0, confidence);
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> videoFrames) {
        // 先进行检测获取分数
        VideoModelDetectionResult result = detectModel(videoFrames);
        return getFeatureDetails(result.getScores());
    }

    /**
     * 获取特征详情
     * @return
     */
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> details = new HashMap<>();
        
        double temporalConsistency = scores.getOrDefault("时间一致性", 0.0);
        double colorStyle = scores.getOrDefault("色彩风格", 0.0);
        double motionScore = scores.getOrDefault("运动特征", 0.0);
        double detailQuality = scores.getOrDefault("细节质量", 0.0);
        double gen1Signature = scores.getOrDefault("Gen1特征", 0.0);
        
        // 时间一致性评估
        if (temporalConsistency >= 80) {
            details.put("时间一致性", "良好 - 风格化过程保持一致");
        } else if (temporalConsistency >= 70) {
            details.put("时间一致性", "中等 - 一致性尚可");
        } else {
            details.put("时间一致性", "一般 - 一致性较弱");
        }
        
        // 色彩风格评估
        if (colorStyle >= 80) {
            details.put("色彩风格", "强烈 - 明显的风格化色彩");
        } else if (colorStyle >= 70) {
            details.put("色彩风格", "较强 - 有一定风格化");
        } else if (colorStyle >= 60) {
            details.put("色彩风格", "中等 - 轻度风格化");
        } else {
            details.put("色彩风格", "自然 - 风格化不明显");
        }
        
        // 运动特征评估
        if (motionScore >= 75) {
            details.put("运动特征", "保持良好 - 运动模式一致");
        } else if (motionScore >= 65) {
            details.put("运动特征", "基本保持 - 运动较为合理");
        } else {
            details.put("运动特征", "一般 - 运动保持一般");
        }
        
        // 细节质量评估
        if (detailQuality >= 75) {
            details.put("细节质量", "良好 - 艺术化纹理丰富");
        } else if (detailQuality >= 65) {
            details.put("细节质量", "中等 - 纹理一般");
        } else {
            details.put("细节质量", "简单 - 纹理较简单");
        }
        
        // Gen-1特有特征评估
        if (gen1Signature >= 70) {
            details.put("Gen1特征", "明显 - 具有典型的Gen-1风格化特征");
        } else if (gen1Signature >= 50) {
            details.put("Gen1特征", "较明显 - 部分Gen-1特征");
        } else {
            details.put("Gen1特征", "不明显 - Gen-1特征较弱");
        }
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    /**
     * 生成建议
     */
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence >= 80) {
            suggestions.add("视频极可能由Gen-1生成");
            suggestions.add("特征：强烈风格化、结构保持、艺术化纹理");
            suggestions.add("建议：重点关注风格化强度和运动保持");
        } else if (confidence >= 60) {
            suggestions.add("视频较可能由Gen-1生成");
            suggestions.add("建议：进一步分析风格化特征和纹理艺术性");
        } else if (confidence >= 40) {
            suggestions.add("视频可能由Gen-1生成，但特征不够典型");
            suggestions.add("建议：对比其他风格转换模型特征");
        } else {
            suggestions.add("视频不太像Gen-1生成");
            suggestions.add("Gen-1通常具有明显的风格化转换特征");
        }
        
        // 基于具体分数的建议
        double colorStyle = scores.getOrDefault("色彩风格", 0.0);
        double motionScore = scores.getOrDefault("运动特征", 0.0);
        double gen1Signature = scores.getOrDefault("Gen1特征", 0.0);
        
        if (colorStyle >= 80 && motionScore >= 70) {
            suggestions.add("检测到Gen-1标志性的风格化转换特征");
        }
        
        if (gen1Signature >= 70) {
            suggestions.add("检测到明显的Gen-1特有特征：艺术化纹理、结构保持");
        }
        
        return suggestions;
    }
}
