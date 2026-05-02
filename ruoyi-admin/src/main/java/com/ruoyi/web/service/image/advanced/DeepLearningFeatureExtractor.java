package com.ruoyi.web.service.image.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * 深度学习特征提取器
 * 模拟深度学习模型的特征提取，用于AI图片检测
 *
 * 主要功能：
 * 1. 纹理模式分析
 * 2. 边缘一致性检测
 * 3. 颜色分布异常检测
 * 4. 局部结构分析
 * 5. 全局语义特征
 *
 * @author ruoyi
 */
@Component
public class DeepLearningFeatureExtractor {

    private static final Logger log = LoggerFactory.getLogger(DeepLearningFeatureExtractor.class);

    /**
     * 提取深度特征
     */
    public Map<String, Object> extractDeepFeatures(BufferedImage image) {
        Map<String, Object> features = new HashMap<>();

        try {
            // 1. 纹理模式特征
            Map<String, Double> textureFeatures = extractTextureFeatures(image);
            features.put("textureFeatures", textureFeatures);

            // 2. 边缘一致性特征
            Map<String, Double> edgeFeatures = extractEdgeConsistencyFeatures(image);
            features.put("edgeFeatures", edgeFeatures);

            // 3. 颜色分布特征
            Map<String, Double> colorFeatures = extractColorDistributionFeatures(image);
            features.put("colorFeatures", colorFeatures);

            // 4. 局部结构特征
            Map<String, Double> structureFeatures = extractLocalStructureFeatures(image);
            features.put("structureFeatures", structureFeatures);

            // 5. 全局语义特征
            Map<String, Double> semanticFeatures = extractGlobalSemanticFeatures(image);
            features.put("semanticFeatures", semanticFeatures);

            // 6. 计算综合AI特征分数
            double aiFeatureScore = calculateAIFeatureScore(textureFeatures, edgeFeatures,
                    colorFeatures, structureFeatures, semanticFeatures);
            features.put("aiFeatureScore", aiFeatureScore);
            features.put("isAIGenerated", aiFeatureScore > 0.7);

        } catch (Exception e) {
            log.error("深度特征提取失败", e);
            features.put("error", e.getMessage());
        }

        return features;
    }

    /**
     * 提取纹理模式特征
     */
    private Map<String, Double> extractTextureFeatures(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        // LBP (Local Binary Pattern) 特征
        double lbpUniformity = calculateLBPUniformity(image);
        features.put("lbpUniformity", lbpUniformity);

        // Gabor滤波器响应
        double gaborResponse = calculateGaborResponse(image);
        features.put("gaborResponse", gaborResponse);

        // 纹理粗糙度
        double textureRoughness = calculateTextureRoughness(image);
        features.put("textureRoughness", textureRoughness);

        // AI生成图片通常纹理过于均匀
        double textureScore = (lbpUniformity * 0.4 + gaborResponse * 0.3 + textureRoughness * 0.3);
        features.put("textureScore", textureScore);

        return features;
    }

    /**
     * 提取边缘一致性特征
     */
    private Map<String, Double> extractEdgeConsistencyFeatures(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        double edgeStrength = calculateEdgeStrength(image);
        double edgeCoherence = calculateEdgeCoherence(image);
        double edgeSharpness = calculateEdgeSharpness(image);

        features.put("edgeStrength", edgeStrength);
        features.put("edgeCoherence", edgeCoherence);
        features.put("edgeSharpness", edgeSharpness);

        double edgeScore = (edgeStrength * 0.3 + edgeCoherence * 0.4 + edgeSharpness * 0.3);
        features.put("edgeScore", edgeScore);

        return features;
    }

    /**
     * 提取颜色分布特征
     */
    private Map<String, Double> extractColorDistributionFeatures(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        double colorVariance = calculateColorVariance(image);
        double colorEntropy = calculateColorEntropy(image);
        double colorSmoothness = calculateColorSmoothness(image);

        features.put("colorVariance", colorVariance);
        features.put("colorEntropy", colorEntropy);
        features.put("colorSmoothness", colorSmoothness);

        double colorScore = (colorVariance * 0.3 + colorEntropy * 0.4 + colorSmoothness * 0.3);
        features.put("colorScore", colorScore);

        return features;
    }

    /**
     * 提取局部结构特征
     */
    private Map<String, Double> extractLocalStructureFeatures(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        double structuralComplexity = calculateStructuralComplexity(image);
        double localPatternConsistency = calculateLocalPatternConsistency(image);
        double spatialFrequency = calculateSpatialFrequency(image);

        features.put("structuralComplexity", structuralComplexity);
        features.put("localPatternConsistency", localPatternConsistency);
        features.put("spatialFrequency", spatialFrequency);

        double structureScore = (structuralComplexity * 0.3 + localPatternConsistency * 0.4 +
                                spatialFrequency * 0.3);
        features.put("structureScore", structureScore);

        return features;
    }

    /**
     * 提取全局语义特征
     */
    private Map<String, Double> extractGlobalSemanticFeatures(BufferedImage image) {
        Map<String, Double> features = new HashMap<>();

        double globalCoherence = calculateGlobalCoherence(image);
        double compositionBalance = calculateCompositionBalance(image);
        double visualComplexity = calculateVisualComplexity(image);

        features.put("globalCoherence", globalCoherence);
        features.put("compositionBalance", compositionBalance);
        features.put("visualComplexity", visualComplexity);

        double semanticScore = (globalCoherence * 0.4 + compositionBalance * 0.3 +
                               visualComplexity * 0.3);
        features.put("semanticScore", semanticScore);

        return features;
    }

    /**
     * 计算AI特征分数
     */
    private double calculateAIFeatureScore(Map<String, Double> texture, Map<String, Double> edge,
                                          Map<String, Double> color, Map<String, Double> structure,
                                          Map<String, Double> semantic) {
        double textureScore = texture.getOrDefault("textureScore", 0.5);
        double edgeScore = edge.getOrDefault("edgeScore", 0.5);
        double colorScore = color.getOrDefault("colorScore", 0.5);
        double structureScore = structure.getOrDefault("structureScore", 0.5);
        double semanticScore = semantic.getOrDefault("semanticScore", 0.5);

        return (textureScore * 0.25 + edgeScore * 0.25 + colorScore * 0.2 +
                structureScore * 0.15 + semanticScore * 0.15);
    }

    // ==================== 纹理特征计算 ====================

    private double calculateLBPUniformity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] lbpHistogram = new int[256];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = getGray(image.getRGB(x, y));
                int lbp = 0;

                if (getGray(image.getRGB(x-1, y-1)) >= center) lbp |= 1;
                if (getGray(image.getRGB(x, y-1)) >= center) lbp |= 2;
                if (getGray(image.getRGB(x+1, y-1)) >= center) lbp |= 4;
                if (getGray(image.getRGB(x+1, y)) >= center) lbp |= 8;
                if (getGray(image.getRGB(x+1, y+1)) >= center) lbp |= 16;
                if (getGray(image.getRGB(x, y+1)) >= center) lbp |= 32;
                if (getGray(image.getRGB(x-1, y+1)) >= center) lbp |= 64;
                if (getGray(image.getRGB(x-1, y)) >= center) lbp |= 128;

                lbpHistogram[lbp]++;
            }
        }

        double entropy = 0.0;
        int totalPixels = (width - 2) * (height - 2);
        for (int count : lbpHistogram) {
            if (count > 0) {
                double p = (double) count / totalPixels;
                entropy -= p * Math.log(p);
            }
        }
        return Math.min(entropy / 5.5, 1.0);
    }

    private double calculateGaborResponse(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double response = 0.0;
        int count = 0;

        for (int y = 2; y < height - 2; y += 4) {
            for (int x = 2; x < width - 2; x += 4) {
                double sum = 0.0;
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        int gray = getGray(image.getRGB(x + dx, y + dy));
                        double weight = Math.exp(-(dx*dx + dy*dy) / 2.0) *
                                       Math.cos(2 * Math.PI * dx / 4.0);
                        sum += gray * weight;
                    }
                }
                response += Math.abs(sum);
                count++;
            }
        }
        return Math.min(response / (count * 255.0), 1.0);
    }

    private double calculateTextureRoughness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double roughness = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = getGray(image.getRGB(x, y));
                int sum = 0;
                sum += Math.abs(center - getGray(image.getRGB(x-1, y)));
                sum += Math.abs(center - getGray(image.getRGB(x+1, y)));
                sum += Math.abs(center - getGray(image.getRGB(x, y-1)));
                sum += Math.abs(center - getGray(image.getRGB(x, y+1)));
                roughness += sum / 4.0;
                count++;
            }
        }
        return Math.min(roughness / (count * 255.0), 1.0);
    }

    // ==================== 边缘特征计算 ====================

    private double calculateEdgeStrength(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double strength = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = getGray(image.getRGB(x+1, y)) - getGray(image.getRGB(x-1, y));
                int gy = getGray(image.getRGB(x, y+1)) - getGray(image.getRGB(x, y-1));
                strength += Math.sqrt(gx * gx + gy * gy);
                count++;
            }
        }
        return Math.min(strength / (count * 255.0), 1.0);
    }

    private double calculateEdgeCoherence(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double coherence = 0.0;
        int count = 0;

        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                int gx = getGray(image.getRGB(x+1, y)) - getGray(image.getRGB(x-1, y));
                int gy = getGray(image.getRGB(x, y+1)) - getGray(image.getRGB(x, y-1));
                double angle = Math.atan2(gy, gx);

                int gx2 = getGray(image.getRGB(x+2, y)) - getGray(image.getRGB(x, y));
                int gy2 = getGray(image.getRGB(x, y+2)) - getGray(image.getRGB(x, y));
                double angle2 = Math.atan2(gy2, gx2);

                coherence += Math.abs(Math.cos(angle - angle2));
                count++;
            }
        }
        return coherence / count;
    }

    private double calculateEdgeSharpness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double sharpness = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = getGray(image.getRGB(x, y));
                int laplacian = -4 * center +
                               getGray(image.getRGB(x-1, y)) +
                               getGray(image.getRGB(x+1, y)) +
                               getGray(image.getRGB(x, y-1)) +
                               getGray(image.getRGB(x, y+1));
                sharpness += Math.abs(laplacian);
                count++;
            }
        }
        return Math.min(sharpness / (count * 255.0), 1.0);
    }

    // ==================== 颜色特征计算 ====================

    private double calculateColorVariance(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[] rSum = {0, 0, 0};
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                rSum[0] += (rgb >> 16) & 0xFF;
                rSum[1] += (rgb >> 8) & 0xFF;
                rSum[2] += rgb & 0xFF;
                count++;
            }
        }

        double[] mean = {rSum[0]/count, rSum[1]/count, rSum[2]/count};
        double variance = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                double r = ((rgb >> 16) & 0xFF) - mean[0];
                double g = ((rgb >> 8) & 0xFF) - mean[1];
                double b = (rgb & 0xFF) - mean[2];
                variance += (r*r + g*g + b*b);
            }
        }
        return Math.min(Math.sqrt(variance / count) / 255.0, 1.0);
    }

    private double calculateColorEntropy(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = getGray(image.getRGB(x, y));
                histogram[gray]++;
            }
        }

        double entropy = 0.0;
        int totalPixels = width * height;
        for (int count : histogram) {
            if (count > 0) {
                double p = (double) count / totalPixels;
                entropy -= p * Math.log(p) / Math.log(2);
            }
        }
        return Math.min(entropy / 8.0, 1.0);
    }

    private double calculateColorSmoothness(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double smoothness = 0.0;
        int count = 0;

        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int rgb1 = image.getRGB(x, y);
                int rgb2 = image.getRGB(x+1, y);
                int rgb3 = image.getRGB(x, y+1);

                smoothness += colorDistance(rgb1, rgb2);
                smoothness += colorDistance(rgb1, rgb3);
                count += 2;
            }
        }
        return 1.0 - Math.min(smoothness / (count * 255.0), 1.0);
    }

    // ==================== 结构特征计算 ====================

    private double calculateStructuralComplexity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Set<Integer> uniquePatterns = new HashSet<>();

        for (int y = 0; y < height - 3; y += 2) {
            for (int x = 0; x < width - 3; x += 2) {
                int pattern = 0;
                for (int dy = 0; dy < 3; dy++) {
                    for (int dx = 0; dx < 3; dx++) {
                        int gray = getGray(image.getRGB(x + dx, y + dy));
                        pattern = pattern * 31 + gray / 32;
                    }
                }
                uniquePatterns.add(pattern);
            }
        }

        int maxPatterns = (width / 2) * (height / 2);
        return Math.min((double) uniquePatterns.size() / maxPatterns, 1.0);
    }

    private double calculateLocalPatternConsistency(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double consistency = 0.0;
        int count = 0;

        for (int y = 0; y < height - 8; y += 4) {
            for (int x = 0; x < width - 8; x += 4) {
                double block1Avg = getBlockAverage(image, x, y, 4);
                double block2Avg = getBlockAverage(image, x + 4, y, 4);
                double block3Avg = getBlockAverage(image, x, y + 4, 4);

                consistency += Math.abs(block1Avg - block2Avg);
                consistency += Math.abs(block1Avg - block3Avg);
                count += 2;
            }
        }
        return 1.0 - Math.min(consistency / (count * 255.0), 1.0);
    }

    private double calculateSpatialFrequency(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double rowFreq = 0.0;
        double colFreq = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width - 1; x++) {
                int diff = getGray(image.getRGB(x, y)) - getGray(image.getRGB(x + 1, y));
                rowFreq += diff * diff;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height - 1; y++) {
                int diff = getGray(image.getRGB(x, y)) - getGray(image.getRGB(x, y + 1));
                colFreq += diff * diff;
            }
        }

        double sf = Math.sqrt(rowFreq / (width * height) + colFreq / (width * height));
        return Math.min(sf / 255.0, 1.0);
    }

    // ==================== 语义特征计算 ====================

    private double calculateGlobalCoherence(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int gridSize = 16;
        double coherence = 0.0;
        int count = 0;

        for (int gy = 0; gy < height / gridSize - 1; gy++) {
            for (int gx = 0; gx < width / gridSize - 1; gx++) {
                double avg1 = getBlockAverage(image, gx * gridSize, gy * gridSize, gridSize);
                double avg2 = getBlockAverage(image, (gx + 1) * gridSize, gy * gridSize, gridSize);
                double avg3 = getBlockAverage(image, gx * gridSize, (gy + 1) * gridSize, gridSize);

                coherence += Math.abs(avg1 - avg2);
                coherence += Math.abs(avg1 - avg3);
                count += 2;
            }
        }
        return 1.0 - Math.min(coherence / (count * 255.0), 1.0);
    }

    private double calculateCompositionBalance(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        double leftSum = 0, rightSum = 0, topSum = 0, bottomSum = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width / 2; x++) {
                leftSum += getGray(image.getRGB(x, y));
            }
            for (int x = width / 2; x < width; x++) {
                rightSum += getGray(image.getRGB(x, y));
            }
        }

        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width; x++) {
                topSum += getGray(image.getRGB(x, y));
            }
        }
        for (int y = height / 2; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bottomSum += getGray(image.getRGB(x, y));
            }
        }

        double hBalance = 1.0 - Math.abs(leftSum - rightSum) / (leftSum + rightSum);
        double vBalance = 1.0 - Math.abs(topSum - bottomSum) / (topSum + bottomSum);

        return (hBalance + vBalance) / 2.0;
    }

    private double calculateVisualComplexity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int edgeCount = 0;
        int totalPixels = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = getGray(image.getRGB(x+1, y)) - getGray(image.getRGB(x-1, y));
                int gy = getGray(image.getRGB(x, y+1)) - getGray(image.getRGB(x, y-1));
                double gradient = Math.sqrt(gx * gx + gy * gy);

                if (gradient > 30) {
                    edgeCount++;
                }
                totalPixels++;
            }
        }
        return Math.min((double) edgeCount / totalPixels * 10, 1.0);
    }

    // ==================== 辅助方法 ====================

    private int getGray(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (int)(0.299 * r + 0.587 * g + 0.114 * b);
    }

    private double getBlockAverage(BufferedImage image, int startX, int startY, int blockSize) {
        double sum = 0.0;
        int count = 0;

        for (int y = startY; y < startY + blockSize && y < image.getHeight(); y++) {
            for (int x = startX; x < startX + blockSize && x < image.getWidth(); x++) {
                sum += getGray(image.getRGB(x, y));
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    private double colorDistance(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        return Math.sqrt((r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2));
    }
}