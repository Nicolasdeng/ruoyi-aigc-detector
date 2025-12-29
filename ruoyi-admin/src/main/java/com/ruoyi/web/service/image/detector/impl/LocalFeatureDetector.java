package com.ruoyi.web.service.image.detector.impl;

import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地特征分析检测器
 * 基于文件特征进行启发式判断
 * 
 * @author ruoyi
 */
@Component
public class LocalFeatureDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(LocalFeatureDetector.class);
    
    @Override
    public String getName() {
        return "本地特征分析";
    }
    
    @Override
    public double getWeight() {
        return 0.20;
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            File imageFile = new File(filePath);
            long fileSize = imageFile.length();
            String fileName = imageFile.getName().toLowerCase();
            
            // 读取图片元数据
            BufferedImage image = ImageIO.read(imageFile);
            
            // 多维度特征分析（权重化评分系统）
            double totalScore = 0.0;
            double maxScore = 0.0;
            List<String> indicators = new ArrayList<>();
            
            // 特征1: 文件名包含AI相关关键词 (权重: 4.0，提高权重)
            double nameWeight = 4.0;
            maxScore += nameWeight;
            if (fileName.contains("ai") || fileName.contains("generated") || 
                fileName.contains("midjourney") || fileName.contains("dalle") ||
                fileName.contains("stable") || fileName.contains("diffusion") ||
                fileName.contains("sd") || fileName.contains("comfyui") ||
                fileName.contains("leonardo") || fileName.contains("playground") ||
                fileName.contains("dreamstudio") || fileName.contains("firefly") ||
                fileName.contains("niji") || fileName.contains("artbreeder") ||
                fileName.contains("豆包") || fileName.contains("doubao") ||
                fileName.contains("kimi") || fileName.contains("通义") ||
                fileName.contains("文心") || fileName.contains("智谱")) {
                totalScore += nameWeight;
                indicators.add("文件名含AI关键词(+高)");
            }
            
            // 特征2: 图片实际分辨率分析 (权重: 3.5，提高权重并扩展检测范围)
            double resolutionWeight = 3.5;
            maxScore += resolutionWeight;
            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                
                // 扩展常见AI生成尺寸（包括各种比例）
                boolean isCommonAiSize = 
                    (width == 512 && height == 512) ||
                    (width == 768 && height == 768) ||
                    (width == 1024 && height == 1024) ||
                    (width == 2048 && height == 2048) ||
                    (width == 512 && height == 768) ||
                    (width == 768 && height == 512) ||
                    (width == 1024 && height == 768) ||
                    (width == 768 && height == 1024) ||
                    (width == 1024 && height == 1536) ||
                    (width == 1536 && height == 1024) ||
                    (width == 1536 && height == 1536) ||
                    // 常见的手机屏幕尺寸也可能是AI生成后调整的
                    (width == 1080 && height == 1920) ||
                    (width == 1920 && height == 1080);
                
                // 检查是否为正方形（AI非常常见的特征）
                boolean isSquare = width == height;
                
                // 检查是否在AI常用尺寸范围内（256-4096）
                boolean inAiRange = (width >= 256 && width <= 4096) && (height >= 256 && height <= 4096);
                
                if (isCommonAiSize) {
                    totalScore += resolutionWeight;
                    indicators.add(String.format("标准AI尺寸(%dx%d)(+高)", width, height));
                } else if (isSquare && inAiRange) {
                    totalScore += resolutionWeight * 0.8;
                    indicators.add(String.format("正方形AI尺寸(%dx%d)(+高)", width, height));
                } else if (inAiRange && ((width % 64 == 0 && height % 64 == 0) || 
                                        (width % 128 == 0 && height % 128 == 0))) {
                    // AI模型通常以64或128为单位生成
                    totalScore += resolutionWeight * 0.6;
                    indicators.add(String.format("AI模型对齐尺寸(%dx%d)(+中)", width, height));
                } else if ((width == 512 || width == 768 || width == 1024 || width == 2048) ||
                          (height == 512 || height == 768 || height == 1024 || height == 2048)) {
                    totalScore += resolutionWeight * 0.4;
                    indicators.add("包含AI常用尺寸维度(+低)");
                }
            }
            
            // 特征3: 文件格式与大小组合分析 (权重: 2.5，提高并优化判断)
            double formatWeight = 2.5;
            maxScore += formatWeight;
            if (fileName.endsWith(".png")) {
                // PNG是AI生成的最常见格式，特别是透明背景图
                if (fileSize > 200000 && fileSize < 8000000) {
                    totalScore += formatWeight;
                    indicators.add("PNG格式+合理文件大小(+高)");
                } else if (fileSize >= 50000 && fileSize <= 15000000) {
                    totalScore += formatWeight * 0.7;
                    indicators.add("PNG格式(+中)");
                }
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                // JPEG也常用于AI生成图，特别是优化后的
                if (fileSize > 100000 && fileSize < 5000000) {
                    totalScore += formatWeight * 0.6;
                    indicators.add("JPEG格式+合理大小(+中)");
                } else if (fileSize >= 50000) {
                    totalScore += formatWeight * 0.3;
                    indicators.add("JPEG格式(+低)");
                }
            } else if (fileName.endsWith(".webp")) {
                // WebP是新格式，AI工具越来越多使用
                totalScore += formatWeight * 0.8;
                indicators.add("WebP新格式(+高)");
            }
            
            // 特征4: 文件名模式分析 (权重: 2.0)
            double patternWeight = 2.0;
            maxScore += patternWeight;
            
            // 长数字序列（通常是生成ID）
            if (fileName.matches(".*\\d{8,}.*")) {
                totalScore += patternWeight * 0.8;
                indicators.add("文件名含长数字序列(+中)");
            } else if (fileName.matches(".*\\d{4,}.*")) {
                totalScore += patternWeight * 0.4;
                indicators.add("文件名含数字序列(+低)");
            }
            
            // UUID或哈希格式
            if (fileName.matches(".*[a-f0-9]{8}[-_][a-f0-9]{4}[-_][a-f0-9]{4}.*") ||
                fileName.matches(".*[a-f0-9]{32,}.*")) {
                totalScore += patternWeight * 0.6;
                indicators.add("文件名含UUID/哈希(+中)");
            }
            
            // 特征5: 图片纵横比分析 (权重: 1.5)
            double aspectWeight = 1.5;
            maxScore += aspectWeight;
            if (image != null) {
                double aspectRatio = (double) image.getWidth() / image.getHeight();
                
                // 1:1 正方形最常见
                if (Math.abs(aspectRatio - 1.0) < 0.01) {
                    totalScore += aspectWeight;
                    indicators.add("正方形比例(+高)");
                }
                // 3:4 或 4:3 也较常见
                else if (Math.abs(aspectRatio - 0.75) < 0.05 || Math.abs(aspectRatio - 1.33) < 0.05) {
                    totalScore += aspectWeight * 0.6;
                    indicators.add("标准比例3:4或4:3(+中)");
                }
                // 16:9 或 9:16
                else if (Math.abs(aspectRatio - 1.78) < 0.05 || Math.abs(aspectRatio - 0.56) < 0.05) {
                    totalScore += aspectWeight * 0.4;
                    indicators.add("宽屏比例16:9(+低)");
                }
            }
            
            // 特征6: 颜色深度与通道分析 (权重: 1.0)
            double colorWeight = 1.0;
            maxScore += colorWeight;
            if (image != null) {
                int colorType = image.getType();
                boolean hasAlpha = image.getColorModel().hasAlpha();
                
                // PNG with alpha channel是AI生成的典型特征
                if (fileName.endsWith(".png") && hasAlpha) {
                    totalScore += colorWeight;
                    indicators.add("PNG透明通道(+高)");
                } else if (hasAlpha) {
                    totalScore += colorWeight * 0.5;
                    indicators.add("包含透明通道(+低)");
                }
            }
            
            // 计算最终AI概率分数 (0.15-0.95范围，提高基准值)
            double normalizedScore = totalScore / maxScore;
            double aiProbability = 0.20 + (normalizedScore * 0.75); // 映射到20%-95%
            
            // 如果匹配了多个高权重特征，显著提升置信度
            long highIndicatorCount = indicators.stream()
                    .filter(s -> s.contains("(+高)"))
                    .count();
            
            if (highIndicatorCount >= 2) {
                aiProbability = Math.min(0.95, aiProbability + 0.15);
                indicators.add("匹配多个高权重特征(+额外加成)");
            } else if (highIndicatorCount == 1) {
                aiProbability = Math.min(0.95, aiProbability + 0.08);
            }
            
            // 如果得分率超过60%，额外加成
            if (normalizedScore > 0.6) {
                aiProbability = Math.min(0.95, aiProbability + 0.05);
            }
            
            result.put("score", Math.round(aiProbability * 100) / 100.0);
            result.put("isAI", aiProbability > 0.50); // 降低判定阈值
            result.put("indicators", indicators);
            result.put("totalScore", Math.round(totalScore * 100) / 100.0);
            result.put("maxScore", Math.round(maxScore * 100) / 100.0);
            result.put("normalizedScore", Math.round(normalizedScore * 100) / 100.0);
            
        } catch (Exception e) {
            log.error("本地特征分析失败: " + filePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> detectByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            String urlLower = imageUrl.toLowerCase();
            double aiProbability = 0.5;
            List<String> indicators = new ArrayList<>();
            
            // URL包含AI相关域名或路径
            if (urlLower.contains("midjourney") || urlLower.contains("openai") ||
                urlLower.contains("stable-diffusion") || urlLower.contains("ai-generated") ||
                urlLower.contains("leonardo") || urlLower.contains("playground")) {
                aiProbability += 0.3;
                indicators.add("URL包含AI服务域名");
            }
            
            // URL包含常见AI图片特征词
            if (urlLower.contains("generated") || urlLower.contains("ai")) {
                aiProbability += 0.2;
                indicators.add("URL包含AI相关关键词");
            }
            
            result.put("score", Math.min(aiProbability, 1.0));
            result.put("isAI", aiProbability > 0.6);
            result.put("indicators", indicators);
            
        } catch (Exception e) {
            log.error("URL特征分析失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 本地分析始终可用
    }
}
