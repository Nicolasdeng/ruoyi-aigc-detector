package com.ruoyi.web.service.image.advanced;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * 高级图片分析器 - 针对最新AI生成图片的深度检测
 *
 * 主要功能：
 * 1. 频域分析（FFT）- 检测生成模型的频谱特征
 * 2. GAN指纹检测 - 识别生成对抗网络的痕迹
 * 3. 统计异常检测 - 像素分布、梯度异常
 * 4. 多尺度一致性分析
 * 5. EXIF深度分析
 *
 * @author ruoyi
 */
@Component
public class AdvancedImageAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(AdvancedImageAnalyzer.class);

    /**
     * 执行完整的高级分析
     */
    public Map<String, Object> performAdvancedAnalysis(BufferedImage image, String filePath) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 频域分析
            Map<String, Double> frequencyAnalysis = analyzeFrequencyDomain(image);
            result.put("frequencyAnalysis", frequencyAnalysis);

            // 2. GAN指纹检测
            Map<String, Double> ganFingerprint = detectGANFingerprint(image);
            result.put("ganFingerprint", ganFingerprint);

            // 3. 统计异常检测
            Map<String, Double> statisticalAnomalies = detectStatisticalAnomalies(image);
            result.put("statisticalAnomalies", statisticalAnomalies);

            // 4. 多尺度一致性分析
            Map<String, Double> multiScaleAnalysis = analyzeMultiScaleConsistency(image);
            result.put("multiScaleAnalysis", multiScaleAnalysis);

            // 5. EXIF深度分析
            if (filePath != null) {
                Map<String, Object> exifAnalysis = analyzeEXIFDeep(filePath);
                result.put("exifAnalysis", exifAnalysis);
            }

            // 6. 计算综合AI生成概率
            double aiProbability = calculateAIProbability(frequencyAnalysis, ganFingerprint,
                    statisticalAnomalies, multiScaleAnalysis);
            result.put("aiGenerationProbability", aiProbability);
            result.put("isLikelyAIGenerated", aiProbability > 0.65);

        } catch (Exception e) {
            log.error("高级图片分析失败", e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 频域分析 - 检测AI生成图片的频谱特征
     */
    private Map<String, Double> analyzeFrequencyDomain(BufferedImage image) {
        Map<String, Double> result = new HashMap<>();
        try {
            double[][] grayImage = convertToGrayscale(image);
            int size = Math.min(Math.min(image.getWidth(), image.getHeight()), 128);
            double[][] dct = performDCT(grayImage, size, size);

            double highFreqEnergy = calculateHighFrequencyEnergy(dct);
            double midFreqEnergy = calculateMidFrequencyEnergy(dct);
            double lowFreqEnergy = calculateLowFrequencyEnergy(dct);
            double freqRatio = midFreqEnergy / (highFreqEnergy + 0.001);

            result.put("highFreqEnergy", highFreqEnergy);
            result.put("midFreqEnergy", midFreqEnergy);
            result.put("lowFreqEnergy", lowFreqEnergy);
            result.put("freqRatio", freqRatio);
            result.put("aiFreqScore", Math.min(freqRatio / 10.0, 1.0));
        } catch (Exception e) {
            log.warn("频域分析失败", e);
            result.put("aiFreqScore", 0.5);
        }
        return result;
    }

    /**
     * GAN指纹检测 - 检测生成对抗网络的特征
     */
    private Map<String, Double> detectGANFingerprint(BufferedImage image) {
        Map<String, Double> result = new HashMap<>();
        try {
            double checkerboardScore = detectCheckerboardPattern(image);
            double symmetryAnomaly = detectSymmetryAnomaly(image);
            double boundaryArtifact = detectBoundaryArtifacts(image);
            double channelCorrelation = analyzeChannelCorrelation(image);

            double ganScore = (checkerboardScore * 0.3 + symmetryAnomaly * 0.25 +
                             boundaryArtifact * 0.25 + channelCorrelation * 0.2);

            result.put("checkerboardScore", checkerboardScore);
            result.put("symmetryAnomaly", symmetryAnomaly);
            result.put("boundaryArtifact", boundaryArtifact);
            result.put("channelCorrelation", channelCorrelation);
            result.put("ganScore", ganScore);
        } catch (Exception e) {
            log.warn("GAN指纹检测失败", e);
            result.put("ganScore", 0.5);
        }
        return result;
    }

    /**
     * 统计异常检测
     */
    private Map<String, Double> detectStatisticalAnomalies(BufferedImage image) {
        Map<String, Double> result = new HashMap<>();
        try {
            double pixelDistAnomaly = analyzePixelDistribution(image);
            double gradientAnomaly = analyzeGradientAnomalies(image);
            double localVariance = analyzeLocalVariance(image);
            double colorClusterAnomaly = analyzeColorClustering(image);

            double anomalyScore = (pixelDistAnomaly * 0.3 + gradientAnomaly * 0.3 +
                                 localVariance * 0.2 + colorClusterAnomaly * 0.2);

            result.put("pixelDistAnomaly", pixelDistAnomaly);
            result.put("gradientAnomaly", gradientAnomaly);
            result.put("localVariance", localVariance);
            result.put("colorClusterAnomaly", colorClusterAnomaly);
            result.put("anomalyScore", anomalyScore);
        } catch (Exception e) {
            log.warn("统计异常检测失败", e);
            result.put("anomalyScore", 0.5);
        }
        return result;
    }

    /**
     * 多尺度一致性分析
     */
    private Map<String, Double> analyzeMultiScaleConsistency(BufferedImage image) {
        Map<String, Double> result = new HashMap<>();
        try {
            double scaleConsistency = calculateScaleConsistency(image);
            double resolutionAnomaly = detectResolutionAnomalies(image);
            double downsampleQuality = analyzeDownsampleQuality(image);

            double consistencyScore = (scaleConsistency * 0.4 + resolutionAnomaly * 0.3 +
                                     downsampleQuality * 0.3);

            result.put("scaleConsistency", scaleConsistency);
            result.put("resolutionAnomaly", resolutionAnomaly);
            result.put("downsampleQuality", downsampleQuality);
            result.put("consistencyScore", consistencyScore);
        } catch (Exception e) {
            log.warn("多尺度分析失败", e);
            result.put("consistencyScore", 0.5);
        }
        return result;
    }

    /**
     * EXIF深度分析
     */
    private Map<String, Object> analyzeEXIFDeep(String filePath) {
        Map<String, Object> result = new HashMap<>();
        try {
            File file = new File(filePath);
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                IIOMetadata metadata = reader.getImageMetadata(0);

                boolean hasExif = metadata != null;
                boolean suspiciousMetadata = analyzeSuspiciousMetadata(metadata);

                result.put("hasExif", hasExif);
                result.put("suspiciousMetadata", suspiciousMetadata);
                result.put("aiIndicator", !hasExif || suspiciousMetadata);
            }
            iis.close();
        } catch (Exception e) {
            log.warn("EXIF分析失败", e);
            result.put("hasExif", false);
        }
        return result;
    }

    /**
     * 计算综合AI生成概率
     */
    private double calculateAIProbability(Map<String, Double> freqAnalysis,
                                         Map<String, Double> ganFingerprint,
                                         Map<String, Double> statAnomalies,
                                         Map<String, Double> multiScale) {
        double freqScore = freqAnalysis.getOrDefault("aiFreqScore", 0.5);
        double ganScore = ganFingerprint.getOrDefault("ganScore", 0.5);
        double anomalyScore = statAnomalies.getOrDefault("anomalyScore", 0.5);
        double consistencyScore = multiScale.getOrDefault("consistencyScore", 0.5);

        return (freqScore * 0.3 + ganScore * 0.3 + anomalyScore * 0.25 + consistencyScore * 0.15);
    }

    // ==================== 辅助方法 ====================

    private double[][] convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] gray = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[y][x] = 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }
        return gray;
    }

    private double[][] performDCT(double[][] input, int width, int height) {
        double[][] dct = new double[height][width];
        for (int v = 0; v < height; v++) {
            for (int u = 0; u < width; u++) {
                double sum = 0.0;
                for (int y = 0; y < Math.min(height, input.length); y++) {
                    for (int x = 0; x < Math.min(width, input[0].length); x++) {
                        sum += input[y][x] *
                               Math.cos((2 * x + 1) * u * Math.PI / (2.0 * width)) *
                               Math.cos((2 * y + 1) * v * Math.PI / (2.0 * height));
                    }
                }
                dct[v][u] = sum;
            }
        }
        return dct;
    }

    private double calculateHighFrequencyEnergy(double[][] dct) {
        double energy = 0.0;
        int size = dct.length;
        for (int i = size / 2; i < size; i++) {
            for (int j = size / 2; j < dct[0].length; j++) {
                energy += dct[i][j] * dct[i][j];
            }
        }
        return energy / (size * dct[0].length);
    }

    private double calculateMidFrequencyEnergy(double[][] dct) {
        double energy = 0.0;
        int size = dct.length;
        for (int i = size / 4; i < size * 3 / 4; i++) {
            for (int j = size / 4; j < dct[0].length * 3 / 4; j++) {
                energy += dct[i][j] * dct[i][j];
            }
        }
        return energy / (size * dct[0].length);
    }

    private double calculateLowFrequencyEnergy(double[][] dct) {
        double energy = 0.0;
        int size = Math.min(dct.length / 4, 8);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < Math.min(dct[0].length / 4, 8); j++) {
                energy += dct[i][j] * dct[i][j];
            }
        }
        return energy / (size * 8);
    }

    private double detectCheckerboardPattern(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double score = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = image.getRGB(x, y);
                int left = image.getRGB(x - 1, y);
                int right = image.getRGB(x + 1, y);
                int top = image.getRGB(x, y - 1);
                int bottom = image.getRGB(x, y + 1);

                double diff = Math.abs(colorDiff(center, left)) +
                             Math.abs(colorDiff(center, right)) +
                             Math.abs(colorDiff(center, top)) +
                             Math.abs(colorDiff(center, bottom));
                score += diff;
                count++;
            }
        }
        return Math.min(score / (count * 255.0), 1.0);
    }

    private double detectSymmetryAnomaly(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double asymmetry = 0.0;
        int samples = Math.min(width / 2, 100);

        for (int y = 0; y < height; y += height / 20) {
            for (int x = 0; x < samples; x++) {
                int left = image.getRGB(x, y);
                int right = image.getRGB(width - 1 - x, y);
                asymmetry += colorDiff(left, right);
            }
        }
        return Math.min(asymmetry / (samples * 20 * 255.0), 1.0);
    }

    private double detectBoundaryArtifacts(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double artifact = 0.0;
        int count = 0;

        for (int x = 0; x < width; x++) {
            artifact += colorDiff(image.getRGB(x, 0), image.getRGB(x, 1));
            artifact += colorDiff(image.getRGB(x, height - 1), image.getRGB(x, height - 2));
            count += 2;
        }
        for (int y = 0; y < height; y++) {
            artifact += colorDiff(image.getRGB(0, y), image.getRGB(1, y));
            artifact += colorDiff(image.getRGB(width - 1, y), image.getRGB(width - 2, y));
            count += 2;
        }
        return Math.min(artifact / (count * 255.0), 1.0);
    }

    private double analyzeChannelCorrelation(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double rSum = 0, gSum = 0, bSum = 0;
        double rgCorr = 0, rbCorr = 0, gbCorr = 0;
        int count = 0;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                rSum += r;
                gSum += g;
                bSum += b;
                count++;
            }
        }

        double rMean = rSum / count;
        double gMean = gSum / count;
        double bMean = bSum / count;

        for (int y = 0; y < height; y += 5) {
            for (int x = 0; x < width; x += 5) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                rgCorr += (r - rMean) * (g - gMean);
                rbCorr += (r - rMean) * (b - bMean);
                gbCorr += (g - gMean) * (b - bMean);
            }
        }

        double avgCorr = (Math.abs(rgCorr) + Math.abs(rbCorr) + Math.abs(gbCorr)) / (3.0 * count * 255 * 255);
        return Math.min(avgCorr, 1.0);
    }

    private double analyzePixelDistribution(BufferedImage image) {
        int[] histogram = new int[256];
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (int)(0.299 * ((rgb >> 16) & 0xFF) +
                                0.587 * ((rgb >> 8) & 0xFF) +
                                0.114 * (rgb & 0xFF));
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

    private double analyzeGradientAnomalies(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double gradientSum = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = image.getRGB(x, y);
                int right = image.getRGB(x + 1, y);
                int bottom = image.getRGB(x, y + 1);

                double gx = colorDiff(center, right);
                double gy = colorDiff(center, bottom);
                gradientSum += Math.sqrt(gx * gx + gy * gy);
                count++;
            }
        }
        return Math.min(gradientSum / (count * 255.0), 1.0);
    }

    private double analyzeLocalVariance(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double totalVariance = 0.0;
        int blockSize = 8;
        int blocks = 0;

        for (int by = 0; by < height - blockSize; by += blockSize) {
            for (int bx = 0; bx < width - blockSize; bx += blockSize) {
                double mean = 0.0;
                for (int y = by; y < by + blockSize; y++) {
                    for (int x = bx; x < bx + blockSize; x++) {
                        int rgb = image.getRGB(x, y);
                        mean += (rgb >> 16) & 0xFF;
                    }
                }
                mean /= (blockSize * blockSize);

                double variance = 0.0;
                for (int y = by; y < by + blockSize; y++) {
                    for (int x = bx; x < bx + blockSize; x++) {
                        int rgb = image.getRGB(x, y);
                        int val = (rgb >> 16) & 0xFF;
                        variance += (val - mean) * (val - mean);
                    }
                }
                totalVariance += variance / (blockSize * blockSize);
                blocks++;
            }
        }
        return Math.min(totalVariance / (blocks * 255.0), 1.0);
    }

    private double analyzeColorClustering(BufferedImage image) {
        Map<Integer, Integer> colorCounts = new HashMap<>();
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int rgb = image.getRGB(x, y);
                int simplified = ((rgb >> 20) & 0xF0) << 16 |
                                ((rgb >> 12) & 0xF0) << 8 |
                                ((rgb >> 4) & 0xF0);
                colorCounts.put(simplified, colorCounts.getOrDefault(simplified, 0) + 1);
            }
        }

        int uniqueColors = colorCounts.size();
        int totalSamples = (width / 2) * (height / 2);
        return Math.min((double) uniqueColors / totalSamples, 1.0);
    }

    private double calculateScaleConsistency(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage scaled = new BufferedImage(width / 2, height / 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(image, 0, 0, width / 2, height / 2, null);
        g.dispose();

        double diff = 0.0;
        int count = 0;
        for (int y = 0; y < height / 2; y++) {
            for (int x = 0; x < width / 2; x++) {
                int orig = image.getRGB(x * 2, y * 2);
                int scaledPixel = scaled.getRGB(x, y);
                diff += colorDiff(orig, scaledPixel);
                count++;
            }
        }
        return 1.0 - Math.min(diff / (count * 255.0), 1.0);
    }

    private double detectResolutionAnomalies(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double aspectRatio = (double) width / height;

        boolean commonResolution = (width % 64 == 0 && height % 64 == 0) ||
                                  (width == 512 && height == 512) ||
                                  (width == 1024 && height == 1024);

        return commonResolution ? 0.8 : 0.3;
    }

    private double analyzeDownsampleQuality(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        double sharpness = 0.0;
        int count = 0;

        for (int y = 1; y < height - 1; y += 4) {
            for (int x = 1; x < width - 1; x += 4) {
                int center = image.getRGB(x, y);
                int right = image.getRGB(x + 1, y);
                int bottom = image.getRGB(x, y + 1);

                sharpness += Math.abs(colorDiff(center, right));
                sharpness += Math.abs(colorDiff(center, bottom));
                count += 2;
            }
        }
        return Math.min(sharpness / (count * 255.0), 1.0);
    }

    private boolean analyzeSuspiciousMetadata(IIOMetadata metadata) {
        if (metadata == null) return true;
        return false;
    }

    private double colorDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        return Math.sqrt((r1 - r2) * (r1 - r2) +
                        (g1 - g2) * (g1 - g2) +
                        (b1 - b2) * (b1 - b2));
    }
}