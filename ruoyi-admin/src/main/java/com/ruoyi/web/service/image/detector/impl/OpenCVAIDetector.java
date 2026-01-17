package com.ruoyi.web.service.image.detector.impl;

import com.ruoyi.web.service.image.detector.IImageDetector;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
// import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 基于OpenCV的AI图片检测器(完全本地,无需网络)
 * 使用计算机视觉算法分析图片特征
 * 
 * @author AI Commander
 */
@Component  // 已禁用：需要OpenCV库支持
public class OpenCVAIDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(OpenCVAIDetector.class);
    
    private boolean opencvLoaded = false;
    
    @PostConstruct
    public void init() {
        try {
            // 使用 nu.pattern.OpenCV 自动加载本地库
            OpenCV.loadLocally();
            opencvLoaded = true;
            log.info("OpenCV AI检测器初始化成功");
        } catch (Exception e) {
            log.warn("OpenCV加载失败，检测器不可用: {}", e.getMessage());
            log.warn("详细错误信息: ", e);
            opencvLoaded = false;
        }
    }
    
    @Override
    public String getName() {
        return "OpenCV AI检测器";
    }
    
    @Override
    public double getWeight() {
        return 0.40; // 40%权重
    }
    
    @Override
    public boolean isAvailable() {
        return opencvLoaded;
    }
    
    @Override
    public Map<String, Object> detectByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", false);
        result.put("error", "OpenCV检测器不支持URL检测，请使用本地文件");
        result.put("score", 0.5);
        return result;
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        
        if (!opencvLoaded) {
            result.put("available", false);
            result.put("error", "OpenCV未加载");
            result.put("score", 0.5);
            return result;
        }
        
        try {
            Mat image = Imgcodecs.imread(filePath);
            if (image.empty()) {
                throw new Exception("无法读取图片");
            }
            
            double totalScore = 0.0;
            List<String> indicators = new ArrayList<>();
            
            // 1. 频域分析（权重35%）- AI图片频域特征异常
            double fftScore = analyzeFrequencyDomain(image);
            if (fftScore > 0.6) {
                indicators.add("频域特征异常");
            }
            totalScore += fftScore * 0.35;
            
            // 2. 噪声分析（权重25%）- AI图片噪声过于规律
            double noiseScore = analyzeNoisePattern(image);
            if (noiseScore > 0.6) {
                indicators.add("噪声模式异常");
            }
            totalScore += noiseScore * 0.25;
            
            // 3. 边缘质量分析（权重20%）- AI图片边缘过于完美
            double edgeScore = analyzeEdgeQuality(image);
            if (edgeScore > 0.6) {
                indicators.add("边缘质量异常");
            }
            totalScore += edgeScore * 0.20;
            
            // 4. 纹理分析（权重15%）- AI图片纹理可能重复
            double textureScore = analyzeTexture(image);
            if (textureScore > 0.6) {
                indicators.add("纹理特征异常");
            }
            totalScore += textureScore * 0.15;
            
            // 5. 色彩一致性（权重5%）
            double colorScore = analyzeColorConsistency(image);
            if (colorScore > 0.6) {
                indicators.add("色彩分布异常");
            }
            totalScore += colorScore * 0.05;
            
            result.put("score", totalScore);
            result.put("isAI", totalScore > 0.5);
            result.put("indicators", indicators);
            result.put("available", true);
            result.put("details", Map.of(
                "fft", fftScore,
                "noise", noiseScore,
                "edge", edgeScore,
                "texture", textureScore,
                "color", colorScore
            ));
            
            image.release();
            
        } catch (Exception e) {
            log.error("OpenCV AI检测失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    /**
     * 频域分析 - AI图片在频域有独特特征
     */
    private double analyzeFrequencyDomain(Mat image) {
        try {
            // 转灰度图
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            
            // 调整大小以加快处理
            Mat resized = new Mat();
            Imgproc.resize(gray, resized, new Size(256, 256));
            
            // 转换为浮点型
            Mat floatImg = new Mat();
            resized.convertTo(floatImg, CvType.CV_32F);
            
            // 执行DFT
            Mat dft = new Mat();
            Core.dft(floatImg, dft, Core.DFT_COMPLEX_OUTPUT, 0);
            
            // 计算幅度谱
            List<Mat> planes = new ArrayList<>();
            Core.split(dft, planes);
            Mat magnitude = new Mat();
            Core.magnitude(planes.get(0), planes.get(1), magnitude);
            
            // 对数变换
            Core.add(magnitude, Scalar.all(1), magnitude);
            Core.log(magnitude, magnitude);
            
            // 分析高频和低频能量比
            Rect lowFreqROI = new Rect(
                magnitude.cols() / 2 - 20, 
                magnitude.rows() / 2 - 20, 
                40, 40
            );
            Mat lowFreq = new Mat(magnitude, lowFreqROI);
            double lowEnergy = Core.sumElems(lowFreq).val[0];
            
            double totalEnergy = Core.sumElems(magnitude).val[0];
            double highEnergy = totalEnergy - lowEnergy;
            
            double ratio = lowEnergy / (highEnergy + 1);
            
            // AI图片通常低频能量占比异常高
            double score = 0.0;
            if (ratio > 15.0) {
                score = 0.8; // 高度可疑
            } else if (ratio > 10.0) {
                score = 0.6;
            } else if (ratio > 7.0) {
                score = 0.4;
            } else {
                score = 0.2;
            }
            
            // 清理
            gray.release();
            resized.release();
            floatImg.release();
            dft.release();
            magnitude.release();
            lowFreq.release();
            
            return score;
            
        } catch (Exception e) {
            log.warn("频域分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 噪声模式分析 - AI图片噪声过于规律
     */
    private double analyzeNoisePattern(Mat image) {
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            
            // 应用高斯模糊
            Mat blurred = new Mat();
            Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);
            
            // 计算噪声（原图 - 模糊图）
            Mat noise = new Mat();
            Core.subtract(gray, blurred, noise);
            
            // 计算噪声的标准差
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(noise, mean, stddev);
            
            double noiseStd = stddev.get(0, 0)[0];
            
            // AI图片噪声标准差通常很小（过于干净）
            double score = 0.0;
            if (noiseStd < 3.0) {
                score = 0.8; // 噪声过小，高度可疑
            } else if (noiseStd < 5.0) {
                score = 0.6;
            } else if (noiseStd < 8.0) {
                score = 0.4;
            } else {
                score = 0.2;
            }
            
            gray.release();
            blurred.release();
            noise.release();
            
            return score;
            
        } catch (Exception e) {
            log.warn("噪声分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 边缘质量分析 - AI图片边缘可能过于锐利或模糊
     */
    private double analyzeEdgeQuality(Mat image) {
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            
            // Canny边缘检测
            Mat edges = new Mat();
            Imgproc.Canny(gray, edges, 50, 150);
            
            // 计算边缘像素比例
            int edgePixels = Core.countNonZero(edges);
            double edgeRatio = (double) edgePixels / (edges.rows() * edges.cols());
            
            // 计算边缘强度
            Mat sobelX = new Mat();
            Mat sobelY = new Mat();
            Imgproc.Sobel(gray, sobelX, CvType.CV_64F, 1, 0);
            Imgproc.Sobel(gray, sobelY, CvType.CV_64F, 0, 1);
            
            Mat magnitude = new Mat();
            Core.magnitude(sobelX, sobelY, magnitude);
            
            Scalar meanMag = Core.mean(magnitude);
            double avgEdgeStrength = meanMag.val[0];
            
            // AI图片边缘特征异常
            double score = 0.0;
            if (edgeRatio > 0.15 && avgEdgeStrength > 40) {
                score = 0.7; // 边缘过于锐利
            } else if (edgeRatio < 0.05 && avgEdgeStrength < 20) {
                score = 0.7; // 边缘过于模糊
            } else if (edgeRatio > 0.12 || edgeRatio < 0.07) {
                score = 0.5;
            } else {
                score = 0.3;
            }
            
            gray.release();
            edges.release();
            sobelX.release();
            sobelY.release();
            magnitude.release();
            
            return score;
            
        } catch (Exception e) {
            log.warn("边缘分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 纹理分析 - 检测重复纹理
     */
    private double analyzeTexture(Mat image) {
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            
            // 缩小图像加快处理
            Mat small = new Mat();
            Imgproc.resize(gray, small, new Size(128, 128));
            
            // 计算局部二值模式（LBP）的方差
            // 这里简化为计算像素梯度的方差
            Mat dx = new Mat();
            Mat dy = new Mat();
            Imgproc.Sobel(small, dx, CvType.CV_64F, 1, 0);
            Imgproc.Sobel(small, dy, CvType.CV_64F, 0, 1);
            
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(dx, mean, stddev);
            double varianceX = stddev.get(0, 0)[0];
            
            Core.meanStdDev(dy, mean, stddev);
            double varianceY = stddev.get(0, 0)[0];
            
            double avgVariance = (varianceX + varianceY) / 2.0;
            
            // AI图片纹理方差可能异常
            double score = 0.0;
            if (avgVariance < 10.0 || avgVariance > 80.0) {
                score = 0.6;
            } else if (avgVariance < 15.0 || avgVariance > 60.0) {
                score = 0.4;
            } else {
                score = 0.2;
            }
            
            gray.release();
            small.release();
            dx.release();
            dy.release();
            
            return score;
            
        } catch (Exception e) {
            log.warn("纹理分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 色彩一致性分析
     */
    private double analyzeColorConsistency(Mat image) {
        try {
            // 转HSV色彩空间
            Mat hsv = new Mat();
            Imgproc.cvtColor(image, hsv, Imgproc.COLOR_BGR2HSV);
            
            // 分离通道
            List<Mat> channels = new ArrayList<>();
            Core.split(hsv, channels);
            
            // 计算饱和度的标准差
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble stddev = new MatOfDouble();
            Core.meanStdDev(channels.get(1), mean, stddev);
            
            double satStd = stddev.get(0, 0)[0];
            
            // AI图片饱和度分布可能过于均匀
            double score = 0.0;
            if (satStd < 30.0) {
                score = 0.6;
            } else if (satStd < 40.0) {
                score = 0.4;
            } else {
                score = 0.2;
            }
            
            hsv.release();
            for (Mat ch : channels) {
                ch.release();
            }
            
            return score;
            
        } catch (Exception e) {
            log.warn("色彩分析失败", e);
            return 0.5;
        }
    }
}
