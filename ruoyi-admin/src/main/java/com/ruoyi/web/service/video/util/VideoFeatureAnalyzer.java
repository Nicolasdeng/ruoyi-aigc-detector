package com.ruoyi.web.service.video.util;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import nu.pattern.OpenCV;
import javax.annotation.PostConstruct;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.*;

/**
 * 视频特征分析工具类
 * 提供8个核心视频特征分析方法，用于AI视频生成检测
 * 
 * @author ruoyi
 */
@Component  // 已禁用：需要OpenCV库支持
public class VideoFeatureAnalyzer {
    
    private static final Logger log = LoggerFactory.getLogger(VideoFeatureAnalyzer.class);
    private boolean opencvLoaded = false;
    
    @PostConstruct
    public void init() {
        try {
            // 使用 nu.pattern.OpenCV 自动加载本地库
            OpenCV.loadLocally();
            opencvLoaded = true;
            log.info("VideoFeatureAnalyzer OpenCV加载成功");
        } catch (Exception e) {
            log.warn("OpenCV加载失败，视频帧提取功能将不可用: {}", e.getMessage());
            log.warn("详细错误信息: ", e);
            opencvLoaded = false;
        }
    }

    /**
     * 从视频文件中提取指定数量的帧
     * 
     * @param filePath 视频文件路径
     * @param frameCount 需要提取的帧数
     * @return 提取的视频帧列表
     */
    public List<BufferedImage> extractFrames(String filePath, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        
        try {
            // 检查文件是否存在
            File videoFile = new File(filePath);
            if (!videoFile.exists()) {
                log.warn("视频文件不存在: {}", filePath);
                return frames;
            }
            
            // 打开视频文件
            VideoCapture capture = new VideoCapture(filePath);
            if (!capture.isOpened()) {
                log.warn("无法打开视频文件: {}", filePath);
                return frames;
            }
            
            // 获取视频总帧数
            double totalFrames = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
            if (totalFrames <= 0) {
                log.warn("无法获取视频总帧数: {}", filePath);
                capture.release();
                return frames;
            }
            
            // 计算帧间隔
            int interval = (int) Math.max(1, totalFrames / frameCount);
            
            Mat frame = new Mat();
            int extractedCount = 0;
            int currentFrame = 0;
            
            // 提取帧
            while (extractedCount < frameCount && capture.read(frame)) {
                if (currentFrame % interval == 0) {
                    BufferedImage bufferedImage = matToBufferedImage(frame);
                    if (bufferedImage != null) {
                        frames.add(bufferedImage);
                        extractedCount++;
                    }
                }
                currentFrame++;
            }
            
            capture.release();
            log.info("从视频 {} 中成功提取 {} 帧", filePath, frames.size());
            
        } catch (Exception e) {
            log.error("提取视频帧时发生错误: {}", filePath, e);
        }
        
        return frames;
    }
    
    /**
     * 将OpenCV的Mat对象转换为BufferedImage
     * 
     * @param mat OpenCV Mat对象
     * @return BufferedImage对象
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        try {
            if (mat == null || mat.empty()) {
                return null;
            }
            
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (mat.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            
            int bufferSize = mat.channels() * mat.cols() * mat.rows();
            byte[] buffer = new byte[bufferSize];
            mat.get(0, 0, buffer);
            
            BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
            final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
            
            return image;
        } catch (Exception e) {
            log.error("Mat转BufferedImage时发生错误", e);
            return null;
        }
    }

    /**
     * 分析时间一致性（帧间一致性）
     * 检测相邻帧之间的变化是否自然
     * 
     * @param frames 视频帧列表
     * @return 时间一致性得分（0-100）
     */
    public double analyzeTemporalConsistency(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 2) {
            return 50.0;
        }

        double totalDiff = 0.0;
        int comparisons = 0;

        // 计算相邻帧的差异
        for (int i = 0; i < frames.size() - 1; i++) {
            BufferedImage frame1 = frames.get(i);
            BufferedImage frame2 = frames.get(i + 1);

            double frameDiff = calculateFrameDifference(frame1, frame2);
            totalDiff += frameDiff;
            comparisons++;
        }

        if (comparisons == 0) return 50.0;

        double avgDiff = totalDiff / comparisons;
        
        // 差异越小，一致性越高，得分越高
        // AI生成的视频通常一致性过高（不自然）
        double score = 100.0 - Math.min(avgDiff * 10, 100.0);
        
        return score;
    }

    /**
     * 分析运动特征
     * 检测视频中的运动模式是否符合物理规律
     * 
     * @param frames 视频帧列表
     * @return 运动特征得分（0-100）
     */
    public double analyzeMotionPatterns(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 3) {
            return 50.0;
        }

        double motionConsistency = 0.0;
        double motionSmoothness = 0.0;

        // 分析运动方向和速度的一致性
        List<Double> motionVectors = new ArrayList<>();
        for (int i = 0; i < frames.size() - 1; i++) {
            double motion = calculateMotionVector(frames.get(i), frames.get(i + 1));
            motionVectors.add(motion);
        }

        // 计算运动一致性
        if (!motionVectors.isEmpty()) {
            double avgMotion = motionVectors.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = motionVectors.stream()
                .mapToDouble(m -> Math.pow(m - avgMotion, 2))
                .average().orElse(0.0);
            
            motionConsistency = 100.0 - Math.min(Math.sqrt(variance) * 20, 100.0);
        }

        // 计算运动平滑度
        if (motionVectors.size() >= 2) {
            double totalChange = 0.0;
            for (int i = 0; i < motionVectors.size() - 1; i++) {
                totalChange += Math.abs(motionVectors.get(i + 1) - motionVectors.get(i));
            }
            double avgChange = totalChange / (motionVectors.size() - 1);
            motionSmoothness = 100.0 - Math.min(avgChange * 30, 100.0);
        }

        // AI生成的视频运动往往过于平滑
        return (motionConsistency + motionSmoothness) / 2.0;
    }

    /**
     * 分析色彩风格
     * 检测色彩分布、饱和度、色调等特征
     * 
     * @param frames 视频帧列表
     * @return 色彩风格得分（0-100）
     */
    public double analyzeColorStyle(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            return 50.0;
        }

        double saturationScore = 0.0;
        double colorDistributionScore = 0.0;

        // 分析每一帧的色彩特征
        List<Map<String, Double>> colorFeatures = new ArrayList<>();
        for (BufferedImage frame : frames) {
            Map<String, Double> features = extractColorFeatures(frame);
            colorFeatures.add(features);
        }

        // 计算饱和度得分（AI生成视频通常饱和度偏高）
        double avgSaturation = colorFeatures.stream()
            .mapToDouble(f -> f.getOrDefault("saturation", 50.0))
            .average().orElse(50.0);
        
        // 饱和度在40-70之间为正常，超出则可能是AI生成
        if (avgSaturation >= 40 && avgSaturation <= 70) {
            saturationScore = 100.0 - Math.abs(avgSaturation - 55) * 2;
        } else {
            saturationScore = 100.0 - Math.min(Math.abs(avgSaturation - 55) * 3, 100.0);
        }

        // 计算色彩分布均匀度（AI生成视频色彩分布往往过于均匀）
        double avgDistribution = colorFeatures.stream()
            .mapToDouble(f -> f.getOrDefault("distribution", 50.0))
            .average().orElse(50.0);
        
        colorDistributionScore = 100.0 - Math.abs(avgDistribution - 50.0) * 2;

        return (saturationScore + colorDistributionScore) / 2.0;
    }

    /**
     * 分析细节质量
     * 检测图像细节、纹理清晰度等
     * 
     * @param frames 视频帧列表
     * @return 细节质量得分（0-100）
     */
    public double analyzeDetailQuality(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) {
            return 50.0;
        }

        double totalSharpness = 0.0;
        double totalTextureComplexity = 0.0;
        int count = 0;

        // 分析每一帧的细节质量
        for (BufferedImage frame : frames) {
            double sharpness = calculateSharpness(frame);
            double texture = calculateTextureComplexity(frame);
            
            totalSharpness += sharpness;
            totalTextureComplexity += texture;
            count++;
        }

        if (count == 0) return 50.0;

        double avgSharpness = totalSharpness / count;
        double avgTexture = totalTextureComplexity / count;

        // AI生成的视频细节往往不够自然
        double sharpnessScore = Math.min(avgSharpness * 100, 100.0);
        double textureScore = Math.min(avgTexture * 100, 100.0);

        return (sharpnessScore + textureScore) / 2.0;
    }

    /**
     * 分析噪声模式
     * 检测视频中的噪声特征
     * 
     * @param frames 视频帧列表
     * @return 噪声模式特征Map
     */
    public Map<String, Double> analyzeNoisePattern(List<BufferedImage> frames) {
        Map<String, Double> noiseFeatures = new HashMap<>();
        
        if (frames == null || frames.isEmpty()) {
            noiseFeatures.put("noiseLevel", 0.0);
            noiseFeatures.put("noiseUniformity", 0.0);
            return noiseFeatures;
        }

        double totalNoise = 0.0;
        List<Double> noiseValues = new ArrayList<>();

        // 计算每一帧的噪声水平
        for (BufferedImage frame : frames) {
            double noise = calculateNoiseLevel(frame);
            totalNoise += noise;
            noiseValues.add(noise);
        }

        double avgNoise = totalNoise / frames.size();
        
        // 计算噪声均匀度
        double variance = noiseValues.stream()
            .mapToDouble(n -> Math.pow(n - avgNoise, 2))
            .average().orElse(0.0);
        double uniformity = 1.0 / (1.0 + Math.sqrt(variance));

        noiseFeatures.put("noiseLevel", avgNoise);
        noiseFeatures.put("noiseUniformity", uniformity);
        
        return noiseFeatures;
    }

    /**
     * 分析边缘稳定性
     * 检测视频中边缘的稳定性和清晰度
     * 
     * @param frames 视频帧列表
     * @return 边缘稳定性得分（0-100）
     */
    public double analyzeEdgeStability(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 2) {
            return 50.0;
        }

        double totalEdgeStability = 0.0;
        int comparisons = 0;

        // 比较相邻帧的边缘稳定性
        for (int i = 0; i < frames.size() - 1; i++) {
            double edgeConsistency = calculateEdgeConsistency(frames.get(i), frames.get(i + 1));
            totalEdgeStability += edgeConsistency;
            comparisons++;
        }

        if (comparisons == 0) return 50.0;

        double avgStability = totalEdgeStability / comparisons;
        
        // AI生成的视频边缘往往过于稳定（不自然）
        return avgStability * 100;
    }

    /**
     * 分析光照一致性
     * 检测视频中光照变化是否自然
     * 
     * @param frames 视频帧列表
     * @return 光照一致性得分（0-100）
     */
    public double analyzeLightingConsistency(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 2) {
            return 50.0;
        }

        double totalLightingChange = 0.0;
        int comparisons = 0;

        // 分析相邻帧的光照变化
        for (int i = 0; i < frames.size() - 1; i++) {
            double lightingChange = calculateLightingChange(frames.get(i), frames.get(i + 1));
            totalLightingChange += lightingChange;
            comparisons++;
        }

        if (comparisons == 0) return 50.0;

        double avgChange = totalLightingChange / comparisons;
        
        // AI生成的视频光照变化往往过于平滑
        double score = 100.0 - Math.min(avgChange * 50, 100.0);
        
        return score;
    }

    /**
     * 检测AI指纹
     * 检测特定AI工具的生成痕迹
     * 
     * @param frames 视频帧列表
     * @return AI指纹特征Map
     */
    public Map<String, Object> detectAiFingerprint(List<BufferedImage> frames) {
        Map<String, Object> fingerprints = new HashMap<>();
        
        if (frames == null || frames.isEmpty()) {
            fingerprints.put("hasFingerprint", false);
            return fingerprints;
        }

        // 检测重复模式
        double repeatPattern = detectRepeatPattern(frames);
        fingerprints.put("repeatPattern", repeatPattern);

        // 检测生成痕迹
        double generationArtifact = detectGenerationArtifact(frames);
        fingerprints.put("generationArtifact", generationArtifact);

        // 检测水印或标记
        boolean hasWatermark = detectWatermark(frames);
        fingerprints.put("hasWatermark", hasWatermark);

        // 综合判断是否有AI指纹
        boolean hasFingerprint = repeatPattern > 0.6 || generationArtifact > 0.7 || hasWatermark;
        fingerprints.put("hasFingerprint", hasFingerprint);
        
        return fingerprints;
    }

    // ========== 辅助方法 ==========

    /**
     * 计算两帧之间的差异
     */
    private double calculateFrameDifference(BufferedImage frame1, BufferedImage frame2) {
        if (frame1 == null || frame2 == null) return 0.0;
        
        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());
        
        long totalDiff = 0;
        int sampleSize = Math.min(width * height, 10000); // 采样以提高性能
        int step = Math.max((width * height) / sampleSize, 1);
        
        int count = 0;
        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb1 = frame1.getRGB(x, y);
                int rgb2 = frame2.getRGB(x, y);
                
                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;
                
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;
                
                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                count++;
            }
        }
        
        return count > 0 ? (double) totalDiff / (count * 765.0) : 0.0;
    }

    /**
     * 计算运动向量
     */
    private double calculateMotionVector(BufferedImage frame1, BufferedImage frame2) {
        // 简化的运动向量计算
        return calculateFrameDifference(frame1, frame2) * 10;
    }

    /**
     * 提取色彩特征
     */
    private Map<String, Double> extractColorFeatures(BufferedImage frame) {
        Map<String, Double> features = new HashMap<>();
        
        if (frame == null) {
            features.put("saturation", 50.0);
            features.put("distribution", 50.0);
            return features;
        }

        long totalSaturation = 0;
        int[] histogram = new int[256];
        int sampleSize = Math.min(frame.getWidth() * frame.getHeight(), 10000);
        int step = Math.max((frame.getWidth() * frame.getHeight()) / sampleSize, 1);
        
        int count = 0;
        for (int y = 0; y < frame.getHeight(); y += step) {
            for (int x = 0; x < frame.getWidth(); x += step) {
                int rgb = frame.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                
                int max = Math.max(Math.max(r, g), b);
                int min = Math.min(Math.min(r, g), b);
                int saturation = max > 0 ? ((max - min) * 100) / max : 0;
                
                totalSaturation += saturation;
                histogram[(r + g + b) / 3]++;
                count++;
            }
        }

        double avgSaturation = count > 0 ? (double) totalSaturation / count : 50.0;
        features.put("saturation", avgSaturation);

        // 计算色彩分布均匀度
        double distribution = calculateHistogramUniformity(histogram);
        features.put("distribution", distribution);

        return features;
    }

    /**
     * 计算清晰度
     */
    private double calculateSharpness(BufferedImage frame) {
        if (frame == null) return 0.5;

        int width = frame.getWidth();
        int height = frame.getHeight();
        double totalGradient = 0.0;
        int count = 0;

        // 使用Sobel算子计算边缘强度
        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                int rgb = frame.getRGB(x, y);
                int gray = ((rgb >> 16) & 0xff + (rgb >> 8) & 0xff + (rgb & 0xff)) / 3;

                int rgbX1 = frame.getRGB(x + 1, y);
                int grayX1 = ((rgbX1 >> 16) & 0xff + (rgbX1 >> 8) & 0xff + (rgbX1 & 0xff)) / 3;

                int rgbY1 = frame.getRGB(x, y + 1);
                int grayY1 = ((rgbY1 >> 16) & 0xff + (rgbY1 >> 8) & 0xff + (rgbY1 & 0xff)) / 3;

                int gx = Math.abs(grayX1 - gray);
                int gy = Math.abs(grayY1 - gray);
                double gradient = Math.sqrt(gx * gx + gy * gy);

                totalGradient += gradient;
                count++;
            }
        }

        return count > 0 ? Math.min(totalGradient / (count * 255.0), 1.0) : 0.5;
    }

    /**
     * 计算纹理复杂度
     */
    private double calculateTextureComplexity(BufferedImage frame) {
        if (frame == null) return 0.5;

        int width = frame.getWidth();
        int height = frame.getHeight();
        double totalVariance = 0.0;
        int count = 0;

        // 计算局部方差作为纹理复杂度指标
        int windowSize = 5;
        for (int y = windowSize; y < height - windowSize; y += windowSize) {
            for (int x = windowSize; x < width - windowSize; x += windowSize) {
                double variance = calculateLocalVariance(frame, x, y, windowSize);
                totalVariance += variance;
                count++;
            }
        }

        return count > 0 ? Math.min(totalVariance / count, 1.0) : 0.5;
    }

    /**
     * 计算局部方差
     */
    private double calculateLocalVariance(BufferedImage frame, int cx, int cy, int windowSize) {
        double sum = 0.0;
        double sumSq = 0.0;
        int count = 0;

        for (int y = cy - windowSize; y <= cy + windowSize; y++) {
            for (int x = cx - windowSize; x <= cx + windowSize; x++) {
                if (x >= 0 && x < frame.getWidth() && y >= 0 && y < frame.getHeight()) {
                    int rgb = frame.getRGB(x, y);
                    int gray = ((rgb >> 16) & 0xff + (rgb >> 8) & 0xff + (rgb & 0xff)) / 3;
                    sum += gray;
                    sumSq += gray * gray;
                    count++;
                }
            }
        }

        if (count == 0) return 0.0;

        double mean = sum / count;
        double variance = (sumSq / count) - (mean * mean);
        return variance / 65025.0; // 归一化到0-1
    }

    /**
     * 计算噪声水平
     */
    private double calculateNoiseLevel(BufferedImage frame) {
        if (frame == null) return 0.0;

        // 使用高频分量估算噪声
        double sharpness = calculateSharpness(frame);
        double texture = calculateTextureComplexity(frame);
        
        // 噪声通常表现为高频但低纹理复杂度
        return Math.max(0.0, sharpness - texture);
    }

    /**
     * 计算边缘一致性
     */
    private double calculateEdgeConsistency(BufferedImage frame1, BufferedImage frame2) {
        if (frame1 == null || frame2 == null) return 0.5;

        double edge1 = calculateSharpness(frame1);
        double edge2 = calculateSharpness(frame2);

        return 1.0 - Math.abs(edge1 - edge2);
    }

    /**
     * 计算光照变化
     */
    private double calculateLightingChange(BufferedImage frame1, BufferedImage frame2) {
        if (frame1 == null || frame2 == null) return 0.0;

        int width = Math.min(frame1.getWidth(), frame2.getWidth());
        int height = Math.min(frame1.getHeight(), frame2.getHeight());

        long totalBrightness1 = 0;
        long totalBrightness2 = 0;
        int count = 0;

        int step = Math.max((width * height) / 5000, 1);

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int rgb1 = frame1.getRGB(x, y);
                int rgb2 = frame2.getRGB(x, y);

                int brightness1 = ((rgb1 >> 16) & 0xff + (rgb1 >> 8) & 0xff + (rgb1 & 0xff)) / 3;
                int brightness2 = ((rgb2 >> 16) & 0xff + (rgb2 >> 8) & 0xff + (rgb2 & 0xff)) / 3;

                totalBrightness1 += brightness1;
                totalBrightness2 += brightness2;
                count++;
            }
        }

        if (count == 0) return 0.0;

        double avgBrightness1 = (double) totalBrightness1 / count;
        double avgBrightness2 = (double) totalBrightness2 / count;

        return Math.abs(avgBrightness1 - avgBrightness2) / 255.0;
    }

    /**
     * 计算直方图均匀度
     */
    private double calculateHistogramUniformity(int[] histogram) {
        if (histogram == null || histogram.length == 0) return 0.0;

        int total = 0;
        for (int count : histogram) {
            total += count;
        }

        if (total == 0) return 0.0;

        double expectedCount = (double) total / histogram.length;
        double chiSquare = 0.0;

        for (int count : histogram) {
            double diff = count - expectedCount;
            chiSquare += (diff * diff) / expectedCount;
        }

        // 归一化到0-100
        return Math.min(chiSquare / histogram.length, 100.0);
    }

    /**
     * 检测重复模式
     */
    private double detectRepeatPattern(List<BufferedImage> frames) {
        if (frames == null || frames.size() < 4) return 0.0;

        // 检测帧序列中的重复模式
        int repeatCount = 0;
        int totalComparisons = 0;

        for (int i = 0; i < frames.size() - 2; i++) {
            for (int j = i + 2; j < frames.size(); j++) {
                double similarity = 1.0 - calculateFrameDifference(frames.get(i), frames.get(j));
                if (similarity > 0.95) {
                    repeatCount++;
                }
                totalComparisons++;
            }
        }

        return totalComparisons > 0 ? (double) repeatCount / totalComparisons : 0.0;
    }

    /**
     * 检测生成痕迹
     */
    private double detectGenerationArtifact(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) return 0.0;

        double totalArtifact = 0.0;

        for (BufferedImage frame : frames) {
            // 检测典型的AI生成痕迹
            double noise = calculateNoiseLevel(frame);
            double texture = calculateTextureComplexity(frame);
            
            // AI生成通常噪声低但纹理过于规则
            double artifact = (noise < 0.1 && texture > 0.7) ? 0.8 : 0.2;
            totalArtifact += artifact;
        }

        return totalArtifact / frames.size();
    }

    /**
     * 检测水印
     */
    private boolean detectWatermark(List<BufferedImage> frames) {
        if (frames == null || frames.isEmpty()) return false;

        // 简化的水印检测：检查角落区域是否有固定模式
        BufferedImage firstFrame = frames.get(0);
        if (firstFrame == null) return false;

        int width = firstFrame.getWidth();
        int height = firstFrame.getHeight();
        
        // 检查右下角区域
        int cornerSize = Math.min(width / 10, height / 10);
        boolean hasConsistentPattern = true;

        for (BufferedImage frame : frames) {
            if (frame == null) continue;
            
            // 如果角落区域在不同帧中差异很大，说明没有水印
            double diff = calculateRegionDifference(
                firstFrame, frame,
                width - cornerSize, height - cornerSize,
                cornerSize, cornerSize
            );
            
            if (diff > 0.1) {
                hasConsistentPattern = false;
                break;
            }
        }

        return hasConsistentPattern && frames.size() > 2;
    }

    /**
     * 计算区域差异
     */
    private double calculateRegionDifference(BufferedImage img1, BufferedImage img2,
                                            int x, int y, int width, int height) {
        if (img1 == null || img2 == null) return 0.0;

        long totalDiff = 0;
        int count = 0;

        int maxX = Math.min(x + width, Math.min(img1.getWidth(), img2.getWidth()));
        int maxY = Math.min(y + height, Math.min(img1.getHeight(), img2.getHeight()));

        for (int py = y; py < maxY; py++) {
            for (int px = x; px < maxX; px++) {
                int rgb1 = img1.getRGB(px, py);
                int rgb2 = img2.getRGB(px, py);

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = rgb1 & 0xff;

                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = rgb2 & 0xff;

                totalDiff += Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
                count++;
            }
        }

        return count > 0 ? (double) totalDiff / (count * 765.0) : 0.0;
    }
}
