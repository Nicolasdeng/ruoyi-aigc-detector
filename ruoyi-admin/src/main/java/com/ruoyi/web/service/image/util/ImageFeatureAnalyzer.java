package com.ruoyi.web.service.image.util;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片特征分析工具类
 * 提供各种图片特征提取方法
 * 
 * @author ruoyi
 */
@Component
public class ImageFeatureAnalyzer {
    
    private static final Logger log = LoggerFactory.getLogger(ImageFeatureAnalyzer.class);
    private boolean opencvLoaded = false;
    
    @PostConstruct
    public void init() {
        try {
            // 使用 nu.pattern.OpenCV 自动加载本地库
            OpenCV.loadLocally();
            opencvLoaded = true;
            log.info("ImageFeatureAnalyzer OpenCV加载成功");
        } catch (Exception e) {
            log.warn("OpenCV加载失败，将使用Java原生实现: {}", e.getMessage());
            opencvLoaded = false;
        }
    }
    
    /**
     * 分析色彩饱和度
     */
    public double analyzeSaturation(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double totalSaturation = 0;
        int pixelCount = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
                totalSaturation += hsb[1]; // Saturation
                pixelCount++;
            }
        }
        
        return pixelCount > 0 ? totalSaturation / pixelCount : 0;
    }
    
    /**
     * 分析色彩分布均匀度
     */
    public double analyzeColorUniformity(BufferedImage image) {
        Map<Integer, Integer> colorCount = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                colorCount.put(rgb, colorCount.getOrDefault(rgb, 0) + 1);
            }
        }
        
        // 计算颜色分布的标准差
        double mean = (double) totalPixels / colorCount.size();
        double variance = 0;
        for (int count : colorCount.values()) {
            variance += Math.pow(count - mean, 2);
        }
        variance /= colorCount.size();
        double stdDev = Math.sqrt(variance);
        
        // 标准差越小，分布越均匀
        return Math.max(0, 1.0 - stdDev / mean);
    }
    
    /**
     * 分析对比度
     */
    public double analyzeContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int maxBrightness = 0;
        int minBrightness = 255;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int brightness = (r + g + b) / 3;
                
                maxBrightness = Math.max(maxBrightness, brightness);
                minBrightness = Math.min(minBrightness, brightness);
            }
        }
        
        return (double) (maxBrightness - minBrightness) / 255.0;
    }
    
    /**
     * 分析高频细节丰富度
     */
    public double analyzeHighFrequencyDetails(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double totalEdgeStrength = 0;
        int edgeCount = 0;
        
        // 简单的边缘检测
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int centerRgb = image.getRGB(x, y);
                int centerBrightness = getBrightness(centerRgb);
                
                int[] dx = {-1, 0, 1};
                int[] dy = {-1, 0, 1};
                double gradient = 0;
                
                for (int i = 0; i < dx.length; i++) {
                    for (int j = 0; j < dy.length; j++) {
                        if (i == 1 && j == 1) continue;
                        int neighborRgb = image.getRGB(x + dx[i], y + dy[j]);
                        int neighborBrightness = getBrightness(neighborRgb);
                        gradient += Math.abs(centerBrightness - neighborBrightness);
                    }
                }
                
                if (gradient > 50) { // 阈值
                    totalEdgeStrength += gradient;
                    edgeCount++;
                }
            }
        }
        
        double avgEdgeStrength = edgeCount > 0 ? totalEdgeStrength / edgeCount : 0;
        return Math.min(1.0, avgEdgeStrength / 500.0);
    }
    
    /**
     * 分析噪点密度
     */
    public double analyzeNoiseDensity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int noiseCount = 0;
        int totalPixels = 0;
        
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int centerRgb = image.getRGB(x, y);
                int centerBrightness = getBrightness(centerRgb);
                
                // 检查周围8个像素
                int[] neighbors = new int[8];
                neighbors[0] = getBrightness(image.getRGB(x-1, y-1));
                neighbors[1] = getBrightness(image.getRGB(x, y-1));
                neighbors[2] = getBrightness(image.getRGB(x+1, y-1));
                neighbors[3] = getBrightness(image.getRGB(x-1, y));
                neighbors[4] = getBrightness(image.getRGB(x+1, y));
                neighbors[5] = getBrightness(image.getRGB(x-1, y+1));
                neighbors[6] = getBrightness(image.getRGB(x, y+1));
                neighbors[7] = getBrightness(image.getRGB(x+1, y+1));
                
                int avgNeighbor = 0;
                for (int n : neighbors) avgNeighbor += n;
                avgNeighbor /= 8;
                
                if (Math.abs(centerBrightness - avgNeighbor) > 30) {
                    noiseCount++;
                }
                totalPixels++;
            }
        }
        
        return totalPixels > 0 ? (double) noiseCount / totalPixels : 0;
    }
    
    /**
     * 分析边缘锐度
     */
    public double analyzeEdgeSharpness(BufferedImage image) {
        double detailScore = analyzeHighFrequencyDetails(image);
        double noiseScore = analyzeNoiseDensity(image);
        
        // 边缘锐度 = 细节丰富 - 噪点密度
        return Math.max(0, Math.min(1.0, detailScore - noiseScore * 0.5));
    }
    
    /**
     * 分析纹理复杂度
     */
    public double analyzeTextureComplexity(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 计算局部方差
        double totalVariance = 0;
        int windowSize = 5;
        int count = 0;
        
        for (int y = windowSize; y < height - windowSize; y += windowSize) {
            for (int x = windowSize; x < width - windowSize; x += windowSize) {
                List<Integer> windowPixels = new ArrayList<>();
                
                for (int dy = -windowSize/2; dy <= windowSize/2; dy++) {
                    for (int dx = -windowSize/2; dx <= windowSize/2; dx++) {
                        windowPixels.add(getBrightness(image.getRGB(x + dx, y + dy)));
                    }
                }
                
                double mean = windowPixels.stream().mapToInt(Integer::intValue).average().orElse(0);
                double variance = windowPixels.stream()
                    .mapToDouble(p -> Math.pow(p - mean, 2))
                    .average().orElse(0);
                
                totalVariance += variance;
                count++;
            }
        }
        
        double avgVariance = count > 0 ? totalVariance / count : 0;
        return Math.min(1.0, avgVariance / 1000.0);
    }
    
    /**
     * 分析色调偏向（是否偏暖色）
     */
    public double analyzeWarmTone(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        long totalWarmness = 0;
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                
                // 暖色调：红色和黄色分量高
                int warmness = r + g - b;
                totalWarmness += Math.max(0, warmness);
            }
        }
        
        double avgWarmness = (double) totalWarmness / (width * height);
        return Math.min(1.0, avgWarmness / 255.0);
    }
    
    /**
     * 获取像素亮度
     */
    private int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }
}
