package com.ruoyi.web.service.video.detector;

import com.ruoyi.web.service.video.IVideoAiModelDetector;
import com.ruoyi.web.service.video.VideoModelDetectionResult;
import com.ruoyi.web.service.video.util.VideoFeatureAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Stable Video Diffusion (SVD)视频生成模型检测器
 * 
 * 特征分析：
 * 1. 高时间一致性：帧间过渡平滑
 * 2. 扩散噪声模式：独特的扩散模型噪声特征
 * 3. 细节丰富：纹理复杂度高
 * 4. 色彩自然：色彩分布接近真实视频
 * 5. 轻微模糊：扩散过程导致的轻微模糊
 * 
 * @author ruoyi
 */
@Component
public class StableVideoDiffusionDetector implements IVideoAiModelDetector {

    @Autowired
    private VideoFeatureAnalyzer featureAnalyzer;

    @Override
    public String getModelName() {
        return "Stable Video Diffusion";
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> videoFrames, Map<String, Object> videoMetadata) {
        // 调用基础检测方法
        VideoModelDetectionResult result = detectModel(videoFrames);
        
        // 如果提供了视频元数据，可以进行额外的分析
        if (videoMetadata != null && !videoMetadata.isEmpty()) {
            Map<String, Double> scores = result.getScores();
            
            // 分析视频分辨率特征 (SVD通常支持高分辨率)
            if (videoMetadata.containsKey("width") && videoMetadata.containsKey("height")) {
                int width = (Integer) videoMetadata.get("width");
                int height = (Integer) videoMetadata.get("height");
                
                // SVD支持高分辨率视频生成
                if (width >= 1024 || height >= 1024) {
                    // 高分辨率符合SVD特征，轻微提升置信度
                    double confidence = result.getConfidence();
                    result.setConfidence(Math.min(100.0, confidence * 1.05));
                }
            }
            
            // 分析帧率特征
            if (videoMetadata.containsKey("fps")) {
                double fps = ((Number) videoMetadata.get("fps")).doubleValue();
                
                // SVD通常在24-30fps范围内表现最佳
                if (fps >= 24 && fps <= 30) {
                    double confidence = result.getConfidence();
                    result.setConfidence(Math.min(100.0, confidence * 1.03));
                }
            }
        }
        
        return result;
    }

    @Override
    public VideoModelDetectionResult detectModel(List<BufferedImage> frames) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, String> features = new HashMap<>();
        
        // 1. 时间一致性分析 (权重25%) - SVD时间一致性高
        double temporalScore = featureAnalyzer.analyzeTemporalConsistency(frames);
        scores.put("temporal_consistency", temporalScore);
        features.put("temporal_consistency", String.format("时间一致性: %.2f分 - %s", 
            temporalScore, getTemporalDescription(temporalScore)));
        
        // 2. 运动特征分析 (权重15%)
        double motionScore = featureAnalyzer.analyzeMotionPatterns(frames);
        scores.put("motion_patterns", motionScore);
        features.put("motion_patterns", String.format("运动特征: %.2f分 - %s", 
            motionScore, getMotionDescription(motionScore)));
        
        // 3. 细节质量分析 (权重20%) - SVD细节丰富
        double detailScore = featureAnalyzer.analyzeDetailQuality(frames);
        scores.put("detail_quality", detailScore);
        features.put("detail_quality", String.format("细节质量: %.2f分 - %s", 
            detailScore, getDetailDescription(detailScore)));
        
        // 4. 色彩风格分析 (权重15%) - SVD色彩自然
        double colorScore = featureAnalyzer.analyzeColorStyle(frames);
        scores.put("color_style", colorScore);
        features.put("color_style", String.format("色彩风格: %.2f分 - %s", 
            colorScore, getColorDescription(colorScore)));
        
        // 5. 噪声模式分析 (权重15%) - SVD扩散噪声特征
        Map<String, Double> noisePatternMap = featureAnalyzer.analyzeNoisePattern(frames);
        double noiseScore = noisePatternMap.getOrDefault("overall_score", 0.0);
        scores.put("noise_pattern", noiseScore);
        features.put("noise_pattern", String.format("噪声模式: %.2f分 - %s", 
            noiseScore, getNoiseDescription(noiseScore)));
        
        // 6. SVD特征检测 (权重10%)
        double svdSignature = detectSVDSignature(frames, scores);
        scores.put("svd_signature", svdSignature);
        features.put("svd_signature", String.format("SVD特征: %.2f分 - %s", 
            svdSignature, getSVDSignatureDescription(svdSignature)));
        
        // 计算总置信度
        double confidence = calculateConfidence(scores);
        
        // 创建结果对象
        VideoModelDetectionResult result = new VideoModelDetectionResult(getModelName(), confidence);
        result.setScores(scores);
        // 将 Map<String, String> 转换为 List<String>
        List<String> featureList = new ArrayList<>(features.values());
        result.setFeatures(featureList);
        
        // 生成建议
        List<String> suggestions = generateSuggestions(confidence, scores);
        result.setSuggestions(suggestions);
        
        return result;
    }

    /**
     * 检测Stable Video Diffusion特有特征
     */
    private double detectSVDSignature(List<BufferedImage> frames, Map<String, Double> scores) {
        double signature = 0.0;
        
        // 1. 扩散噪声特征 (30分)
        // SVD具有独特的扩散模型噪声模式
        double noiseScore = scores.getOrDefault("noise_pattern", 0.0);
        double diffusionPattern = analyzeDiffusionPattern(frames);
        if (diffusionPattern > 0.7 && noiseScore > 60) {
            signature += 30;
        } else if (diffusionPattern > 0.5) {
            signature += 20;
        }
        
        // 2. 高时间一致性特征 (25分)
        double temporalScore = scores.getOrDefault("temporal_consistency", 0.0);
        if (temporalScore > 85) {
            signature += 25;
        } else if (temporalScore > 75) {
            signature += 15;
        }
        
        // 3. 细节丰富特征 (25分)
        double detailScore = scores.getOrDefault("detail_quality", 0.0);
        if (detailScore > 75) {
            signature += 25;
        } else if (detailScore > 65) {
            signature += 15;
        }
        
        // 4. 轻微模糊特征 (20分)
        // SVD由于扩散过程会有轻微模糊
        double blurLevel = analyzeBlurLevel(frames);
        if (blurLevel > 0.3 && blurLevel < 0.6) {  // 中等模糊
            signature += 20;
        } else if (blurLevel > 0.2 && blurLevel < 0.7) {
            signature += 10;
        }
        
        return signature;
    }

    /**
     * 分析扩散模式
     */
    private double analyzeDiffusionPattern(List<BufferedImage> frames) {
        if (frames.isEmpty()) {
            return 0.0;
        }
        
        double totalDiffusion = 0.0;
        
        for (BufferedImage frame : frames) {
            // 分析帧的高频成分分布
            double highFreqRatio = analyzeHighFrequency(frame);
            
            // 分析纹理的规律性
            double textureRegularity = analyzeTextureRegularity(frame);
            
            // 扩散模型特征：高频成分适中，纹理规律性较高
            if (highFreqRatio > 0.3 && highFreqRatio < 0.6 && textureRegularity > 0.6) {
                totalDiffusion += 1.0;
            } else if (highFreqRatio > 0.25 && highFreqRatio < 0.7) {
                totalDiffusion += 0.5;
            }
        }
        
        return totalDiffusion / frames.size();
    }

    /**
     * 分析高频成分
     */
    private double analyzeHighFrequency(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        double totalHighFreq = 0.0;
        int count = 0;
        
        // 使用简单的边缘检测来估算高频成分
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = frame.getRGB(x, y);
                int right = frame.getRGB(x + 1, y);
                int bottom = frame.getRGB(x, y + 1);
                
                double diff = Math.abs(getGray(center) - getGray(right)) + 
                             Math.abs(getGray(center) - getGray(bottom));
                
                totalHighFreq += diff / 510.0;  // 归一化到0-1
                count++;
            }
        }
        
        return count > 0 ? totalHighFreq / count : 0.0;
    }

    /**
     * 分析纹理规律性
     */
    private double analyzeTextureRegularity(BufferedImage frame) {
        return calculateTextureComplexity(frame);
    }
    
    /**
     * 计算纹理复杂度（本地实现）
     */
    private double calculateTextureComplexity(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        double totalComplexity = 0.0;
        int count = 0;
        
        // 使用局部方差来估算纹理复杂度
        int windowSize = 3;
        for (int y = windowSize; y < height - windowSize; y++) {
            for (int x = windowSize; x < width - windowSize; x++) {
                double variance = calculateLocalVariance(frame, x, y, windowSize);
                totalComplexity += variance;
                count++;
            }
        }
        
        return count > 0 ? (totalComplexity / count) * 100.0 : 0.0;
    }
    
    /**
     * 计算局部方差
     */
    private double calculateLocalVariance(BufferedImage frame, int centerX, int centerY, int windowSize) {
        double sum = 0.0;
        double sumSq = 0.0;
        int count = 0;
        
        for (int dy = -windowSize; dy <= windowSize; dy++) {
            for (int dx = -windowSize; dx <= windowSize; dx++) {
                int x = centerX + dx;
                int y = centerY + dy;
                if (x >= 0 && x < frame.getWidth() && y >= 0 && y < frame.getHeight()) {
                    int gray = getGray(frame.getRGB(x, y));
                    sum += gray;
                    sumSq += gray * gray;
                    count++;
                }
            }
        }
        
        if (count == 0) return 0.0;
        
        double mean = sum / count;
        double variance = (sumSq / count) - (mean * mean);
        return Math.sqrt(Math.max(0, variance));
    }

    /**
     * 分析模糊程度
     */
    private double analyzeBlurLevel(List<BufferedImage> frames) {
        if (frames.isEmpty()) {
            return 0.0;
        }
        
        double totalBlur = 0.0;
        
        for (BufferedImage frame : frames) {
            double sharpness = calculateSharpness(frame);
            // 锐度低表示模糊程度高
            double blur = 1.0 - (sharpness / 100.0);
            totalBlur += blur;
        }
        
        return totalBlur / frames.size();
    }
    
    /**
     * 计算锐度（本地实现）
     */
    private double calculateSharpness(BufferedImage frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        
        double totalSharpness = 0.0;
        int count = 0;
        
        // 使用Laplacian算子估算锐度
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = getGray(frame.getRGB(x, y));
                int left = getGray(frame.getRGB(x - 1, y));
                int right = getGray(frame.getRGB(x + 1, y));
                int top = getGray(frame.getRGB(x, y - 1));
                int bottom = getGray(frame.getRGB(x, y + 1));
                
                // Laplacian: 4*center - (left + right + top + bottom)
                double laplacian = Math.abs(4 * center - left - right - top - bottom);
                totalSharpness += laplacian;
                count++;
            }
        }
        
        return count > 0 ? (totalSharpness / count) : 0.0;
    }

    /**
     * 获取灰度值
     */
    private int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }

    /**
     * 计算总置信度
     */
    private double calculateConfidence(Map<String, Double> scores) {
        // 权重配置
        double temporalWeight = 0.25;     // 时间一致性权重（SVD时间一致性高）
        double motionWeight = 0.15;       // 运动特征权重
        double detailWeight = 0.20;       // 细节质量权重（SVD细节丰富）
        double colorWeight = 0.15;        // 色彩风格权重
        double noiseWeight = 0.15;        // 噪声模式权重（SVD扩散噪声）
        double signatureWeight = 0.10;    // SVD特征权重
        
        double confidence = 0.0;
        confidence += scores.getOrDefault("temporal_consistency", 0.0) * temporalWeight;
        confidence += scores.getOrDefault("motion_patterns", 0.0) * motionWeight;
        confidence += scores.getOrDefault("detail_quality", 0.0) * detailWeight;
        confidence += scores.getOrDefault("color_style", 0.0) * colorWeight;
        confidence += scores.getOrDefault("noise_pattern", 0.0) * noiseWeight;
        confidence += scores.getOrDefault("svd_signature", 0.0) * signatureWeight;
        
        // 特征组合加成
        double temporalScore = scores.getOrDefault("temporal_consistency", 0.0);
        double detailScore = scores.getOrDefault("detail_quality", 0.0);
        double svdScore = scores.getOrDefault("svd_signature", 0.0);
        
        // 如果高时间一致性+细节丰富+SVD特征都高，额外加成
        if (temporalScore > 85 && detailScore > 75 && svdScore > 60) {
            confidence = Math.min(100.0, confidence * 1.12);
        }
        
        return Math.min(100.0, Math.max(0.0, confidence));
    }

    /**
     * 生成特征详情描述
     */
    private String generateFeatureDetails(Map<String, Double> scores, Map<String, String> features) {
        StringBuilder details = new StringBuilder();
        details.append("=== Stable Video Diffusion视频特征分析 ===\n\n");
        
        details.append("【核心特征】\n");
        details.append(features.get("temporal_consistency")).append("\n");
        details.append(features.get("detail_quality")).append("\n");
        details.append(features.get("noise_pattern")).append("\n");
        details.append(features.get("svd_signature")).append("\n\n");
        
        details.append("【辅助特征】\n");
        details.append(features.get("motion_patterns")).append("\n");
        details.append(features.get("color_style")).append("\n");
        
        return details.toString();
    }

    @Override
    public Map<String, Object> getFeatureDetails(List<BufferedImage> frames) {
        VideoModelDetectionResult result = detectModel(frames);
        Map<String, String> features = new HashMap<>();
        for (String feature : result.getFeatures()) {
            // 将List<String>转换为Map<String, String>以便生成详情
            String[] parts = feature.split(": ", 2);
            if (parts.length == 2) {
                features.put(parts[0], parts[1]);
            }
        }
        String detailsText = generateFeatureDetails(result.getScores(), features);
        Map<String, Object> details = new HashMap<>();
        details.put("details", detailsText);
        details.put("scores", result.getScores());
        details.put("features", result.getFeatures());
        return details;
    }

    @Override
    public Map<String, Object> getFeatureDetails(Map<String, Double> scores) {
        Map<String, Object> details = new HashMap<>();
        
        // 时间一致性特征
        double temporalScore = scores.getOrDefault("temporal_consistency", 0.0);
        details.put("temporal_consistency", Map.of(
            "score", temporalScore,
            "description", getTemporalDescription(temporalScore),
            "weight", "25%"
        ));
        
        // 运动特征
        double motionScore = scores.getOrDefault("motion_patterns", 0.0);
        details.put("motion_patterns", Map.of(
            "score", motionScore,
            "description", getMotionDescription(motionScore),
            "weight", "15%"
        ));
        
        // 细节质量
        double detailScore = scores.getOrDefault("detail_quality", 0.0);
        details.put("detail_quality", Map.of(
            "score", detailScore,
            "description", getDetailDescription(detailScore),
            "weight", "20%"
        ));
        
        // 色彩风格
        double colorScore = scores.getOrDefault("color_style", 0.0);
        details.put("color_style", Map.of(
            "score", colorScore,
            "description", getColorDescription(colorScore),
            "weight", "15%"
        ));
        
        // 噪声模式
        double noiseScore = scores.getOrDefault("noise_pattern", 0.0);
        details.put("noise_pattern", Map.of(
            "score", noiseScore,
            "description", getNoiseDescription(noiseScore),
            "weight", "15%"
        ));
        
        // SVD特征
        double svdScore = scores.getOrDefault("svd_signature", 0.0);
        details.put("svd_signature", Map.of(
            "score", svdScore,
            "description", getSVDSignatureDescription(svdScore),
            "weight", "10%"
        ));
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(VideoModelDetectionResult result) {
        return generateSuggestions(result.getConfidence(), result.getScores());
    }

    @Override
    public List<String> generateSuggestions(double confidence, Map<String, Double> scores) {
        List<String> suggestions = new ArrayList<>();
        
        if (confidence > 70) {
            suggestions.add("视频极可能由Stable Video Diffusion生成");
            suggestions.add("建议：检测到明显的扩散模型特征和高时间一致性");
            
            double temporalScore = scores.getOrDefault("temporal_consistency", 0.0);
            if (temporalScore > 85) {
                suggestions.add("- 时间一致性极高，符合SVD特征");
            }
            
            double detailScore = scores.getOrDefault("detail_quality", 0.0);
            if (detailScore > 75) {
                suggestions.add("- 细节丰富，典型的扩散模型生成风格");
            }
            
        } else if (confidence > 40) {
            suggestions.add("视频可能由Stable Video Diffusion生成");
            suggestions.add("建议：检测到部分扩散模型特征，但不够明显");
            
            double svdScore = scores.getOrDefault("svd_signature", 0.0);
            if (svdScore < 50) {
                suggestions.add("- SVD特征不够典型，建议进一步分析");
            }
            
        } else {
            suggestions.add("视频不太可能由Stable Video Diffusion生成");
            suggestions.add("建议：特征不符合扩散模型典型风格");
        }
        
        return suggestions;
    }

    // 描述方法
    private String getTemporalDescription(double score) {
        if (score > 85) return "时间连贯性极高（典型SVD特征）";
        if (score > 75) return "时间连贯性很好";
        if (score > 60) return "时间连贯性中等";
        return "时间连贯性较差";
    }

    private String getMotionDescription(double score) {
        if (score > 75) return "运动平滑自然";
        if (score > 60) return "运动较为平滑";
        return "运动特征正常";
    }

    private String getDetailDescription(double score) {
        if (score > 75) return "细节非常丰富（典型SVD特征）";
        if (score > 65) return "细节较丰富";
        return "细节正常";
    }

    private String getColorDescription(double score) {
        if (score > 70) return "色彩自然真实";
        if (score > 50) return "色彩较为自然";
        return "色彩正常";
    }

    private String getNoiseDescription(double score) {
        if (score > 70) return "扩散噪声模式明显（典型SVD特征）";
        if (score > 50) return "扩散噪声模式中等";
        return "噪声模式不明显";
    }

    private String getSVDSignatureDescription(double score) {
        if (score > 70) return "SVD特征非常明显（扩散模式+高一致性+细节丰富）";
        if (score > 50) return "SVD特征较明显";
        return "SVD特征不明显";
    }
}
