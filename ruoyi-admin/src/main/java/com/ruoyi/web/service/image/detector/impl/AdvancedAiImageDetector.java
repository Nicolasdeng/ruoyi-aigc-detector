package com.ruoyi.web.service.image.detector.impl;

import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 高级AI图片检测器
 * 基于AI图片生成原理的反向推理检测
 * 特别针对即梦AI等主流生成模型的特征分析
 * 
 * 检测原理：
 * 1. Diffusion模型特征：噪声残留、过度平滑、细节一致性异常
 * 2. GAN模型特征：频域异常、棋盘效应、伪影
 * 3. 即梦AI特征：特定色彩分布、风格一致性、纹理模式
 * 
 * @author ruoyi
 */
@Component
public class AdvancedAiImageDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(AdvancedAiImageDetector.class);
    
    @Override
    public String getName() {
        return "高级AI生成检测";
    }
    
    @Override
    public double getWeight() {
        return 0.35; // 高权重，因为基于深度算法分析
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            File imageFile = new File(filePath);
            BufferedImage image = ImageIO.read(imageFile);
            
            if (image == null) {
                throw new Exception("无法读取图片");
            }
            
            List<String> indicators = new ArrayList<>();
            double totalScore = 0.0;
            double maxScore = 100.0;
            
            // ========== 核心检测算法 ==========
            
            // 1. 频域分析 - 检测生成模型的频率特征（权重：25分）
            double frequencyScore = analyzeFrequencyDomain(image, indicators);
            totalScore += frequencyScore;
            
            // 2. 颜色分布异常检测（权重：20分）
            double colorScore = analyzeColorDistribution(image, indicators);
            totalScore += colorScore;
            
            // 3. 纹理一致性分析 - AI生成图片纹理过于规整（权重：15分）
            double textureScore = analyzeTextureConsistency(image, indicators);
            totalScore += textureScore;
            
            // 4. 边缘锐度分析 - AI图片边缘处理特征（权重：15分）
            double edgeScore = analyzeEdgeSharpness(image, indicators);
            totalScore += edgeScore;
            
            // 5. 噪声模式检测 - Diffusion模型残留噪声（权重：10分）
            double noiseScore = analyzeNoisePattern(image, indicators);
            totalScore += noiseScore;
            
            // 6. 即梦AI专项特征检测（权重：15分）
            double jimengScore = detectJimengAiFeatures(image, imageFile.getName(), indicators);
            totalScore += jimengScore;
            
            // 计算最终AI概率
            double normalizedScore = totalScore / maxScore;
            double aiProbability = 0.15 + (normalizedScore * 0.80); // 映射到15%-95%
            
            // 高置信度提升
            if (normalizedScore > 0.7) {
                aiProbability = Math.min(0.95, aiProbability + 0.10);
                indicators.add("【高置信度】多项核心指标异常");
            } else if (normalizedScore > 0.5) {
                aiProbability = Math.min(0.95, aiProbability + 0.05);
            }
            
            result.put("score", Math.round(aiProbability * 100) / 100.0);
            result.put("isAI", aiProbability > 0.45);
            result.put("indicators", indicators);
            result.put("detailScore", Map.of(
                "frequencyDomain", Math.round(frequencyScore * 100) / 100.0,
                "colorDistribution", Math.round(colorScore * 100) / 100.0,
                "textureConsistency", Math.round(textureScore * 100) / 100.0,
                "edgeSharpness", Math.round(edgeScore * 100) / 100.0,
                "noisePattern", Math.round(noiseScore * 100) / 100.0,
                "jimengFeatures", Math.round(jimengScore * 100) / 100.0,
                "totalScore", Math.round(totalScore * 100) / 100.0,
                "maxScore", maxScore
            ));
            
        } catch (Exception e) {
            log.error("高级AI检测失败: " + filePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    /**
     * 频域分析 - AI生成图片在频域有特殊模式
     * Diffusion模型生成的图片高频分量异常
     */
    private double analyzeFrequencyDomain(BufferedImage image, List<String> indicators) {
        double score = 0.0;
        double maxScore = 25.0;
        
        try {
            int width = Math.min(image.getWidth(), 512);
            int height = Math.min(image.getHeight(), 512);
            BufferedImage resized = resizeImage(image, width, height);
            
            // 计算图像梯度（简化的频率分析）
            double[][] gradients = computeGradientMagnitude(resized);
            
            // 分析高频能量分布
            double highFreqEnergy = 0.0;
            double midFreqEnergy = 0.0;
            double totalEnergy = 0.0;
            
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    double grad = gradients[y][x];
                    totalEnergy += grad;
                    
                    // 高频：强烈的梯度变化
                    if (grad > 100) {
                        highFreqEnergy += grad;
                    } else if (grad > 30) {
                        midFreqEnergy += grad;
                    }
                }
            }
            
            double highFreqRatio = totalEnergy > 0 ? highFreqEnergy / totalEnergy : 0;
            double midFreqRatio = totalEnergy > 0 ? midFreqEnergy / totalEnergy : 0;
            
            // AI生成图片通常高频能量偏低（过度平滑）
            if (highFreqRatio < 0.15 && midFreqRatio > 0.3) {
                score += 15.0;
                indicators.add(String.format("【频域】高频能量偏低(%.2f%%)", highFreqRatio * 100));
            } else if (highFreqRatio < 0.25) {
                score += 8.0;
                indicators.add("【频域】高频分量较低");
            }
            
            // 检测周期性模式（GAN常见问题）
            if (detectPeriodicPattern(gradients)) {
                score += 10.0;
                indicators.add("【频域】检测到周期性伪影（GAN特征）");
            }
            
        } catch (Exception e) {
            log.warn("频域分析失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * 颜色分布分析 - AI生成图片色彩分布有特殊规律
     */
    private double analyzeColorDistribution(BufferedImage image, List<String> indicators) {
        double score = 0.0;
        double maxScore = 20.0;
        
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int sampleSize = Math.min(width * height, 100000);
            
            int[] redHist = new int[256];
            int[] greenHist = new int[256];
            int[] blueHist = new int[256];
            int[] saturationHist = new int[256];
            
            Random rand = new Random(42);
            
            for (int i = 0; i < sampleSize; i++) {
                int x = rand.nextInt(width);
                int y = rand.nextInt(height);
                
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                redHist[r]++;
                greenHist[g]++;
                blueHist[b]++;
                
                // 计算饱和度
                float[] hsb = Color.RGBtoHSB(r, g, b, null);
                int sat = (int) (hsb[1] * 255);
                saturationHist[sat]++;
            }
            
            // 分析色彩分布的集中度（AI图片颜色往往更集中）
            double colorConcentration = calculateConcentration(redHist) +
                                       calculateConcentration(greenHist) +
                                       calculateConcentration(blueHist);
            colorConcentration /= 3.0;
            
            if (colorConcentration > 0.4) {
                score += 10.0;
                indicators.add(String.format("【色彩】颜色分布过于集中(%.2f)", colorConcentration));
            } else if (colorConcentration > 0.3) {
                score += 5.0;
                indicators.add("【色彩】颜色集中度较高");
            }
            
            // 分析饱和度分布（即梦AI特征：饱和度偏高且均匀）
            double satConcentration = calculateConcentration(saturationHist);
            double avgSaturation = calculateAverage(saturationHist);
            
            if (avgSaturation > 100 && satConcentration > 0.35) {
                score += 10.0;
                indicators.add(String.format("【即梦特征】高饱和度集中分布(%.1f)", avgSaturation));
            } else if (avgSaturation > 80) {
                score += 5.0;
                indicators.add("【色彩】整体饱和度偏高");
            }
            
        } catch (Exception e) {
            log.warn("颜色分析失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * 纹理一致性分析 - AI生成的纹理过于规整
     */
    private double analyzeTextureConsistency(BufferedImage image, List<String> indicators) {
        double score = 0.0;
        double maxScore = 15.0;
        
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 分割图片为多个区域分析纹理
            int gridSize = 8;
            double[] regionVariances = new double[gridSize * gridSize];
            
            int blockWidth = width / gridSize;
            int blockHeight = height / gridSize;
            
            for (int gy = 0; gy < gridSize; gy++) {
                for (int gx = 0; gx < gridSize; gx++) {
                    int startX = gx * blockWidth;
                    int startY = gy * blockHeight;
                    int endX = Math.min(startX + blockWidth, width);
                    int endY = Math.min(startY + blockHeight, height);
                    
                    regionVariances[gy * gridSize + gx] = 
                        calculateRegionVariance(image, startX, startY, endX, endY);
                }
            }
            
            // 计算区域间方差的标准差
            double varianceStd = calculateStandardDeviation(regionVariances);
            double varianceMean = Arrays.stream(regionVariances).average().orElse(0);
            
            // AI图片纹理过于一致（方差的标准差偏小）
            if (varianceMean > 0 && varianceStd / varianceMean < 0.4) {
                score += 10.0;
                indicators.add(String.format("【纹理】区域纹理异常一致(CV=%.2f)", varianceStd / varianceMean));
            } else if (varianceMean > 0 && varianceStd / varianceMean < 0.6) {
                score += 5.0;
                indicators.add("【纹理】纹理一致性较高");
            }
            
            // 检测重复纹理（AI常见问题）
            if (detectRepeatingTextures(regionVariances)) {
                score += 5.0;
                indicators.add("【纹理】检测到重复纹理模式");
            }
            
        } catch (Exception e) {
            log.warn("纹理分析失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * 边缘锐度分析 - AI图片边缘处理特征
     */
    private double analyzeEdgeSharpness(BufferedImage image, List<String> indicators) {
        double score = 0.0;
        double maxScore = 15.0;
        
        try {
            int width = Math.min(image.getWidth(), 512);
            int height = Math.min(image.getHeight(), 512);
            BufferedImage resized = resizeImage(image, width, height);
            
            double[][] gradients = computeGradientMagnitude(resized);
            
            // 统计边缘强度分布
            List<Double> edgeStrengths = new ArrayList<>();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (gradients[y][x] > 20) { // 只考虑明显的边缘
                        edgeStrengths.add(gradients[y][x]);
                    }
                }
            }
            
            if (!edgeStrengths.isEmpty()) {
                // AI图片边缘往往过于平滑或过于锐利
                double edgeMean = edgeStrengths.stream().mapToDouble(d -> d).average().orElse(0);
                double edgeStd = calculateStandardDeviation(
                    edgeStrengths.stream().mapToDouble(d -> d).toArray()
                );
                
                // 检测边缘强度分布异常
                if (edgeStd < edgeMean * 0.3) {
                    score += 8.0;
                    indicators.add("【边缘】边缘强度分布过于均匀");
                }
                
                // 检测"过度锐化"（AI后处理常见）
                long strongEdges = edgeStrengths.stream()
                    .filter(e -> e > edgeMean + 2 * edgeStd)
                    .count();
                double strongEdgeRatio = (double) strongEdges / edgeStrengths.size();
                
                if (strongEdgeRatio > 0.15) {
                    score += 7.0;
                    indicators.add(String.format("【边缘】过度锐化特征(%.1f%%)", strongEdgeRatio * 100));
                }
            }
            
        } catch (Exception e) {
            log.warn("边缘分析失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * 噪声模式检测 - Diffusion模型残留噪声特征
     */
    private double analyzeNoisePattern(BufferedImage image, List<String> indicators) {
        double score = 0.0;
        double maxScore = 10.0;
        
        try {
            int width = Math.min(image.getWidth(), 512);
            int height = Math.min(image.getHeight(), 512);
            BufferedImage resized = resizeImage(image, width, height);
            
            // 分析局部像素变化（高频噪声）
            double totalNoise = 0.0;
            int count = 0;
            
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    int centerRgb = resized.getRGB(x, y);
                    int centerR = (centerRgb >> 16) & 0xFF;
                    int centerG = (centerRgb >> 8) & 0xFF;
                    int centerB = centerRgb & 0xFF;
                    
                    // 与相邻像素的差异
                    double localVariance = 0;
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dx = -1; dx <= 1; dx++) {
                            if (dx == 0 && dy == 0) continue;
                            
                            int neighborRgb = resized.getRGB(x + dx, y + dy);
                            int neighborR = (neighborRgb >> 16) & 0xFF;
                            int neighborG = (neighborRgb >> 8) & 0xFF;
                            int neighborB = neighborRgb & 0xFF;
                            
                            localVariance += Math.abs(centerR - neighborR);
                            localVariance += Math.abs(centerG - neighborG);
                            localVariance += Math.abs(centerB - neighborB);
                        }
                    }
                    
                    totalNoise += localVariance / 24.0; // 8邻域，3通道
                    count++;
                }
            }
            
            double avgNoise = count > 0 ? totalNoise / count : 0;
            
            // Diffusion模型生成的图片通常有特定范围的噪声
            if (avgNoise > 3 && avgNoise < 12) {
                score += 10.0;
                indicators.add(String.format("【噪声】检测到Diffusion模型特征噪声(%.2f)", avgNoise));
            } else if (avgNoise > 2 && avgNoise < 15) {
                score += 5.0;
                indicators.add("【噪声】噪声模式异常");
            }
            
        } catch (Exception e) {
            log.warn("噪声分析失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    /**
     * 即梦AI专项特征检测
     * 基于即梦AI的生成特点进行专项分析
     */
    private double detectJimengAiFeatures(BufferedImage image, String fileName, List<String> indicators) {
        double score = 0.0;
        double maxScore = 15.0;
        
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 特征1：即梦AI常用尺寸（1024x1024, 1536x1024等）
            if ((width == 1024 && height == 1024) ||
                (width == 1536 && height == 1024) ||
                (width == 1024 && height == 1536) ||
                (width == 2048 && height == 2048)) {
                score += 5.0;
                indicators.add(String.format("【即梦】标准生成尺寸(%dx%d)", width, height));
            }
            
            // 特征2：文件名模式（即梦AI生成的文件名特征）
            String lowerName = fileName.toLowerCase();
            if (lowerName.contains("jimeng") || lowerName.contains("即梦") ||
                lowerName.matches(".*\\d{13,}.*") || // 13位以上时间戳
                lowerName.matches(".*[a-f0-9]{32}.*")) { // MD5哈希
                score += 3.0;
                indicators.add("【即梦】文件名符合即梦AI模式");
            }
            
            // 特征3：即梦AI色彩风格 - 偏向高饱和度、高对比度
            double[] colorStats = analyzeColorStyle(image);
            double avgSaturation = colorStats[0];
            double avgBrightness = colorStats[1];
            double contrast = colorStats[2];
            
            if (avgSaturation > 0.5 && contrast > 0.4) {
                score += 4.0;
                indicators.add(String.format("【即梦风格】高饱和高对比(S:%.2f,C:%.2f)", avgSaturation, contrast));
            } else if (avgSaturation > 0.4) {
                score += 2.0;
                indicators.add("【即梦风格】饱和度偏高");
            }
            
            // 特征4：即梦AI的细节处理特征 - 细节丰富但略显人工
            double detailScore = analyzeDetailQuality(image);
            if (detailScore > 0.6) {
                score += 3.0;
                indicators.add("【即梦】细节质量符合AI生成特征");
            }
            
        } catch (Exception e) {
            log.warn("即梦AI特征检测失败", e);
        }
        
        return Math.min(score, maxScore);
    }
    
    // ========== 辅助方法 ==========
    
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }
    
    private double[][] computeGradientMagnitude(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] gradients = new double[height][width];
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                // Sobel算子
                int gx = 0, gy = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int rgb = image.getRGB(x + dx, y + dy);
                        int gray = ((rgb >> 16) & 0xFF + (rgb >> 8) & 0xFF + (rgb & 0xFF)) / 3;
                        
                        // Sobel X
                        if (dx == -1) gx -= gray * (dy == 0 ? 2 : 1);
                        if (dx == 1) gx += gray * (dy == 0 ? 2 : 1);
                        
                        // Sobel Y
                        if (dy == -1) gy -= gray * (dx == 0 ? 2 : 1);
                        if (dy == 1) gy += gray * (dx == 0 ? 2 : 1);
                    }
                }
                
                gradients[y][x] = Math.sqrt(gx * gx + gy * gy);
            }
        }
        
        return gradients;
    }
    
    private boolean detectPeriodicPattern(double[][] data) {
        // 简化的周期性检测
        int height = data.length;
        int width = data[0].length;
        
        // 检查是否存在明显的重复模式（8x8块）
        int blockSize = 8;
        int repeatCount = 0;
        
        for (int y = 0; y < height - blockSize * 2; y += blockSize) {
            for (int x = 0; x < width - blockSize * 2; x += blockSize) {
                double correlation = calculateBlockCorrelation(data, x, y, x + blockSize, y, blockSize);
                if (correlation > 0.8) {
                    repeatCount++;
                }
            }
        }
        
        return repeatCount > 10;
    }
    
    private double calculateBlockCorrelation(double[][] data, int x1, int y1, int x2, int y2, int size) {
        double sum = 0;
        int count = 0;
        
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                if (y1 + dy < data.length && x1 + dx < data[0].length &&
                    y2 + dy < data.length && x2 + dx < data[0].length) {
                    double diff = Math.abs(data[y1 + dy][x1 + dx] - data[y2 + dy][x2 + dx]);
                    sum += diff;
                    count++;
                }
            }
        }
        
        double avgDiff = count > 0 ? sum / count : Double.MAX_VALUE;
        return 1.0 / (1.0 + avgDiff / 10.0);
    }
    
    private double calculateConcentration(int[] histogram) {
        int total = Arrays.stream(histogram).sum();
        if (total == 0) return 0;
        
        // 计算前20%的bin占总数的比例
        int[] sorted = Arrays.copyOf(histogram, histogram.length);
        Arrays.sort(sorted);
        
        int topCount = (int) (histogram.length * 0.2);
        int topSum = 0;
        for (int i = histogram.length - topCount; i < histogram.length; i++) {
            topSum += sorted[i];
        }
        
        return (double) topSum / total;
    }
    
    private double calculateAverage(int[] histogram) {
        long sum = 0;
        long count = 0;
        
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
            count += histogram[i];
        }
        
        return count > 0 ? (double) sum / count : 0;
    }
    
    private double calculateRegionVariance(BufferedImage image, int startX, int startY, int endX, int endY) {
        List<Integer> grayValues = new ArrayList<>();
        
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int rgb = image.getRGB(x, y);
                int gray = ((rgb >> 16) & 0xFF + (rgb >> 8) & 0xFF + (rgb & 0xFF)) / 3;
                grayValues.add(gray);
            }
        }
        
        if (grayValues.isEmpty()) return 0;
        
        double mean = grayValues.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = grayValues.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        
        return Math.sqrt(variance);
    }
    
    private double calculateStandardDeviation(double[] values) {
        if (values.length == 0) return 0;
        
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        
        return Math.sqrt(variance);
    }
    
    private boolean detectRepeatingTextures(double[] variances) {
        if (variances.length < 4) return false;
        
        // 检查方差值是否有重复模式
        Map<Integer, Integer> valueCount = new HashMap<>();
        for (double v : variances) {
            int key = (int) (v * 10); // 精度到0.1
            valueCount.put(key, valueCount.getOrDefault(key, 0) + 1);
        }
        
        // 如果某个值重复次数过多，可能是重复纹理
        return valueCount.values().stream().anyMatch(count -> count > variances.length * 0.3);
    }
    
    private double[] analyzeColorStyle(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int sampleSize = Math.min(width * height, 50000);
        
        double totalSat = 0, totalBri = 0;
        List<Integer> brightness = new ArrayList<>();
        Random rand = new Random(42);
        
        for (int i = 0; i < sampleSize; i++) {
            int x = rand.nextInt(width);
            int y = rand.nextInt(height);
            int rgb = image.getRGB(x, y);
            
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            float[] hsb = Color.RGBtoHSB(r, g, b, null);
            totalSat += hsb[1];
            totalBri += hsb[2];
            brightness.add((int) (hsb[2] * 255));
        }
        
        double avgSat = totalSat / sampleSize;
        double avgBri = totalBri / sampleSize;
        
        // 计算对比度（亮度标准差）
        double briMean = brightness.stream().mapToInt(Integer::intValue).average().orElse(0);
        double briStd = Math.sqrt(brightness.stream()
            .mapToDouble(b -> Math.pow(b - briMean, 2))
            .average()
            .orElse(0));
        double contrast = briStd / 255.0;
        
        return new double[]{avgSat, avgBri, contrast};
    }
    
    private double analyzeDetailQuality(BufferedImage image) {
        try {
            int width = Math.min(image.getWidth(), 512);
            int height = Math.min(image.getHeight(), 512);
            BufferedImage resized = resizeImage(image, width, height);
            
            double[][] gradients = computeGradientMagnitude(resized);
            
            // 统计中等强度边缘（细节区域）
            int detailCount = 0;
            int totalPixels = 0;
            
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double grad = gradients[y][x];
                    if (grad > 10 && grad < 80) { // 中等梯度 = 细节
                        detailCount++;
                    }
                    totalPixels++;
                }
            }
            
            return (double) detailCount / totalPixels;
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    @Override
    public Map<String, Object> detectByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", false);
        result.put("score", 0.5);
        result.put("isAI", false);
        result.put("message", "URL检测需要下载图片后分析");
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
