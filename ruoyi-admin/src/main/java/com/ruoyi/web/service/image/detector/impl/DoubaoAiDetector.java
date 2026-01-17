package com.ruoyi.web.service.image.detector.impl;

import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * 豆包AI图片专用检测器
 * 基于豆包AI生成图片的特征进行针对性检测
 * 
 * 检测原理：
 * 1. 文件元数据特征：豆包生成的图片通常包含特定软件标记
 * 2. 像素级特征：AI生成图片的噪声模式、边缘平滑度异常
 * 3. 频域特征：高频信息分布不自然（Stable Diffusion系特征）
 * 4. 颜色分布：过于均匀或理想化的颜色分布
 * 5. 纹理一致性：局部纹理过于完美或重复
 * 
 * @author ruoyi
 */
@Component
public class DoubaoAiDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(DoubaoAiDetector.class);
    
    // 豆包AI常见关键词
    private static final Set<String> DOUBAO_KEYWORDS = new HashSet<>(Arrays.asList(
        "豆包", "doubao", "bytedance", "字节", "抖音", "douyin", 
        "coze", "扣子", "火山", "volcano"
    ));
    
    @Override
    public String getName() {
        return "豆包AI专用检测";
    }
    
    @Override
    public double getWeight() {
        return 0.35; // 较高权重，因为是专门针对豆包的检测
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            File imageFile = new File(filePath);
            String fileName = imageFile.getName().toLowerCase();
            BufferedImage image = ImageIO.read(imageFile);
            
            double totalScore = 0.0;
            double maxScore = 0.0;
            List<String> indicators = new ArrayList<>();
            
            // === 特征1: 文件名与元数据检测 (权重: 5.0) ===
            double metadataWeight = 5.0;
            maxScore += metadataWeight;
            
            // 检查文件名是否包含豆包相关关键词
            boolean hasDoubaoKeyword = DOUBAO_KEYWORDS.stream()
                    .anyMatch(fileName::contains);
            
            if (hasDoubaoKeyword) {
                totalScore += metadataWeight;
                indicators.add("文件名包含豆包关键词(+极高)");
            }
            
            // 检查EXIF/元数据
            Map<String, String> metadata = extractMetadata(imageFile);
            if (metadata.containsKey("Software") || metadata.containsKey("Generator")) {
                String software = metadata.getOrDefault("Software", "") + 
                                 metadata.getOrDefault("Generator", "");
                if (DOUBAO_KEYWORDS.stream().anyMatch(software.toLowerCase()::contains)) {
                    totalScore += metadataWeight * 0.8;
                    indicators.add("元数据包含豆包标识(+极高)");
                }
            }
            
            // === 特征2: 像素级噪声分析 (权重: 4.5) ===
            double noiseWeight = 4.5;
            maxScore += noiseWeight;
            
            if (image != null) {
                double noiseScore = analyzePixelNoise(image);
                if (noiseScore > 0.7) {
                    totalScore += noiseWeight;
                    indicators.add(String.format("AI典型噪声模式(%.2f)(+极高)", noiseScore));
                } else if (noiseScore > 0.5) {
                    totalScore += noiseWeight * 0.7;
                    indicators.add(String.format("疑似AI噪声(%.2f)(+高)", noiseScore));
                } else if (noiseScore > 0.3) {
                    totalScore += noiseWeight * 0.4;
                    indicators.add(String.format("轻微AI噪声特征(%.2f)(+中)", noiseScore));
                }
            }
            
            // === 特征3: 边缘平滑度异常检测 (权重: 4.0) ===
            double edgeWeight = 4.0;
            maxScore += edgeWeight;
            
            if (image != null) {
                double edgeAnomalyScore = detectEdgeAnomalies(image);
                if (edgeAnomalyScore > 0.7) {
                    totalScore += edgeWeight;
                    indicators.add(String.format("边缘过度平滑(%.2f)(+极高)", edgeAnomalyScore));
                } else if (edgeAnomalyScore > 0.5) {
                    totalScore += edgeWeight * 0.7;
                    indicators.add(String.format("边缘异常(%.2f)(+高)", edgeAnomalyScore));
                } else if (edgeAnomalyScore > 0.3) {
                    totalScore += edgeWeight * 0.4;
                    indicators.add(String.format("轻微边缘异常(%.2f)(+中)", edgeAnomalyScore));
                }
            }
            
            // === 特征4: 颜色分布分析 (权重: 3.5) ===
            double colorWeight = 3.5;
            maxScore += colorWeight;
            
            if (image != null) {
                double colorScore = analyzeColorDistribution(image);
                if (colorScore > 0.7) {
                    totalScore += colorWeight;
                    indicators.add(String.format("颜色分布过于理想(%.2f)(+高)", colorScore));
                } else if (colorScore > 0.5) {
                    totalScore += colorWeight * 0.6;
                    indicators.add(String.format("颜色分布异常(%.2f)(+中)", colorScore));
                } else if (colorScore > 0.3) {
                    totalScore += colorWeight * 0.3;
                    indicators.add(String.format("轻微颜色异常(%.2f)(+低)", colorScore));
                }
            }
            
            // === 特征5: 纹理重复性检测 (权重: 3.0) ===
            double textureWeight = 3.0;
            maxScore += textureWeight;
            
            if (image != null) {
                double textureScore = detectTextureRepetition(image);
                if (textureScore > 0.7) {
                    totalScore += textureWeight;
                    indicators.add(String.format("纹理异常重复(%.2f)(+高)", textureScore));
                } else if (textureScore > 0.5) {
                    totalScore += textureWeight * 0.6;
                    indicators.add(String.format("纹理疑似重复(%.2f)(+中)", textureScore));
                } else if (textureScore > 0.3) {
                    totalScore += textureWeight * 0.3;
                    indicators.add(String.format("轻微纹理重复(%.2f)(+低)", textureScore));
                }
            }
            
            // === 特征6: 高频信息分析 (权重: 2.5) ===
            double frequencyWeight = 2.5;
            maxScore += frequencyWeight;
            
            if (image != null) {
                double frequencyScore = analyzeHighFrequency(image);
                if (frequencyScore > 0.65) {
                    totalScore += frequencyWeight;
                    indicators.add(String.format("高频信息分布异常(%.2f)(+高)", frequencyScore));
                } else if (frequencyScore > 0.45) {
                    totalScore += frequencyWeight * 0.6;
                    indicators.add(String.format("高频信息可疑(%.2f)(+中)", frequencyScore));
                }
            }
            
            // === 特征7: 局部一致性检测 (权重: 2.0) ===
            double consistencyWeight = 2.0;
            maxScore += consistencyWeight;
            
            if (image != null) {
                double consistencyScore = detectLocalConsistency(image);
                if (consistencyScore > 0.7) {
                    totalScore += consistencyWeight;
                    indicators.add(String.format("局部过于一致(%.2f)(+高)", consistencyScore));
                } else if (consistencyScore > 0.5) {
                    totalScore += consistencyWeight * 0.5;
                    indicators.add(String.format("局部一致性异常(%.2f)(+中)", consistencyScore));
                }
            }
            
            // 计算最终AI概率分数
            double normalizedScore = totalScore / maxScore;
            double aiProbability = 0.15 + (normalizedScore * 0.80); // 15%-95%范围
            
            // 如果有明确的豆包标识，直接提升到90%+
            if (hasDoubaoKeyword || 
                (metadata.containsKey("Software") && 
                 DOUBAO_KEYWORDS.stream().anyMatch(metadata.get("Software").toLowerCase()::contains))) {
                aiProbability = Math.max(aiProbability, 0.90);
                indicators.add("检测到明确豆包标识(+决定性)");
            }
            
            // 如果匹配多个高权重特征
            long highIndicatorCount = indicators.stream()
                    .filter(s -> s.contains("(+极高)") || s.contains("(+高)"))
                    .count();
            
            if (highIndicatorCount >= 3) {
                aiProbability = Math.min(0.98, aiProbability + 0.15);
                indicators.add("匹配多个关键特征(+强力加成)");
            } else if (highIndicatorCount >= 2) {
                aiProbability = Math.min(0.95, aiProbability + 0.10);
                indicators.add("匹配关键特征(+加成)");
            }
            
            result.put("score", Math.round(aiProbability * 100) / 100.0);
            result.put("isAI", aiProbability > 0.55); // 略高于0.5的阈值
            result.put("indicators", indicators);
            result.put("totalScore", Math.round(totalScore * 100) / 100.0);
            result.put("maxScore", Math.round(maxScore * 100) / 100.0);
            result.put("normalizedScore", Math.round(normalizedScore * 100) / 100.0);
            result.put("metadata", metadata);
            
        } catch (Exception e) {
            log.error("豆包AI检测失败: " + filePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    /**
     * 提取图片元数据
     */
    private Map<String, String> extractMetadata(File imageFile) {
        Map<String, String> metadata = new HashMap<>();
        try (ImageInputStream iis = ImageIO.createImageInputStream(imageFile)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                reader.setInput(iis);
                IIOMetadata imageMetadata = reader.getImageMetadata(0);
                if (imageMetadata != null) {
                    String[] names = imageMetadata.getMetadataFormatNames();
                    for (String name : names) {
                        try {
                            org.w3c.dom.Node root = imageMetadata.getAsTree(name);
                            extractMetadataFromNode(root, metadata);
                        } catch (Exception e) {
                            // 忽略解析错误
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("无法提取元数据", e);
        }
        return metadata;
    }
    
    private void extractMetadataFromNode(org.w3c.dom.Node node, Map<String, String> metadata) {
        org.w3c.dom.NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                org.w3c.dom.Node attr = attributes.item(i);
                String name = attr.getNodeName();
                String value = attr.getNodeValue();
                if (name.toLowerCase().contains("software") || 
                    name.toLowerCase().contains("generator") ||
                    name.toLowerCase().contains("creator")) {
                    metadata.put(name, value);
                }
            }
        }
        
        org.w3c.dom.NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            extractMetadataFromNode(children.item(i), metadata);
        }
    }
    
    /**
     * 分析像素级噪声模式
     * AI生成图片通常有特定的噪声分布
     */
    private double analyzePixelNoise(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 采样分析（避免处理整个图片）
            int sampleSize = Math.min(width * height / 100, 10000);
            double noiseSum = 0.0;
            Random random = new Random(42); // 固定种子保证可重复
            
            for (int i = 0; i < sampleSize; i++) {
                int x = random.nextInt(width - 1);
                int y = random.nextInt(height - 1);
                
                // 计算相邻像素差异
                int rgb1 = image.getRGB(x, y);
                int rgb2 = image.getRGB(x + 1, y);
                int rgb3 = image.getRGB(x, y + 1);
                
                double diff1 = colorDifference(rgb1, rgb2);
                double diff2 = colorDifference(rgb1, rgb3);
                
                // AI生成的图片通常梯度变化较平滑
                if (diff1 < 10 && diff2 < 10) {
                    noiseSum += 0.6; // 过于平滑
                } else if (diff1 > 50 || diff2 > 50) {
                    noiseSum += 0.2; // 突变较大，可能是真实照片
                } else {
                    noiseSum += 0.4; // 中等变化
                }
            }
            
            return noiseSum / sampleSize;
            
        } catch (Exception e) {
            log.debug("噪声分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 检测边缘平滑度异常
     * AI生成图片的边缘通常过于平滑或不自然
     */
    private double detectEdgeAnomalies(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 简化的边缘检测（Sobel算子）
            int sampleSize = Math.min(width * height / 200, 5000);
            double smoothEdgeCount = 0;
            Random random = new Random(42);
            
            for (int i = 0; i < sampleSize; i++) {
                int x = random.nextInt(width - 2) + 1;
                int y = random.nextInt(height - 2) + 1;
                
                // 计算梯度
                double gx = calculateGradientX(image, x, y);
                double gy = calculateGradientY(image, x, y);
                double gradient = Math.sqrt(gx * gx + gy * gy);
                
                // AI生成的边缘通常在特定范围内（10-40）
                if (gradient > 10 && gradient < 40) {
                    smoothEdgeCount += 1.0;
                } else if (gradient < 10) {
                    smoothEdgeCount += 0.7; // 过于平滑
                }
            }
            
            return smoothEdgeCount / sampleSize;
            
        } catch (Exception e) {
            log.debug("边缘检测失败", e);
            return 0.5;
        }
    }
    
    /**
     * 分析颜色分布
     * AI生成图片的颜色分布通常过于理想化
     */
    private double analyzeColorDistribution(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 统计颜色直方图
            int[] redHist = new int[256];
            int[] greenHist = new int[256];
            int[] blueHist = new int[256];
            
            int sampleSize = Math.min(width * height / 50, 20000);
            Random random = new Random(42);
            
            for (int i = 0; i < sampleSize; i++) {
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                int rgb = image.getRGB(x, y);
                
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                
                redHist[red]++;
                greenHist[green]++;
                blueHist[blue]++;
            }
            
            // 计算分布的均匀度（AI生成图片通常更均匀）
            double redVariance = calculateVariance(redHist);
            double greenVariance = calculateVariance(greenHist);
            double blueVariance = calculateVariance(blueHist);
            
            double avgVariance = (redVariance + greenVariance + blueVariance) / 3.0;
            
            // 方差较小表示分布较均匀，AI图片的典型特征
            if (avgVariance < 50) {
                return 0.8;
            } else if (avgVariance < 100) {
                return 0.6;
            } else if (avgVariance < 200) {
                return 0.4;
            } else {
                return 0.2; // 方差大，可能是真实照片
            }
            
        } catch (Exception e) {
            log.debug("颜色分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 检测纹理重复性
     * AI生成图片有时会出现纹理重复或过于完美的模式
     */
    private double detectTextureRepetition(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 检查局部块的相似度
            int blockSize = 8;
            int compareDistance = 16; // 比较相距16像素的块
            
            if (width < blockSize * 3 || height < blockSize * 3) {
                return 0.5;
            }
            
            int sampleCount = 200;
            double similaritySum = 0.0;
            Random random = new Random(42);
            
            for (int i = 0; i < sampleCount; i++) {
                int x1 = random.nextInt(width - blockSize - compareDistance);
                int y1 = random.nextInt(height - blockSize);
                int x2 = x1 + compareDistance;
                
                double similarity = compareBlocks(image, x1, y1, x2, y1, blockSize);
                similaritySum += similarity;
            }
            
            double avgSimilarity = similaritySum / sampleCount;
            
            // 高相似度表示可能有重复纹理
            return avgSimilarity;
            
        } catch (Exception e) {
            log.debug("纹理检测失败", e);
            return 0.5;
        }
    }
    
    /**
     * 分析高频信息分布
     * AI生成图片的高频信息通常分布不自然
     */
    private double analyzeHighFrequency(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            
            // 简化的高频检测：计算二阶差分
            int sampleSize = Math.min(width * height / 300, 3000);
            double highFreqSum = 0.0;
            Random random = new Random(42);
            
            for (int i = 0; i < sampleSize; i++) {
                int x = random.nextInt(width - 2);
                int y = random.nextInt(height - 2);
                
                int rgb0 = image.getRGB(x, y);
                int rgb1 = image.getRGB(x + 1, y);
                int rgb2 = image.getRGB(x + 2, y);
                
                // 二阶差分
                double diff1 = colorDifference(rgb0, rgb1);
                double diff2 = colorDifference(rgb1, rgb2);
                double secondDiff = Math.abs(diff2 - diff1);
                
                // AI生成图片的二阶差分通常较小
                if (secondDiff < 5) {
                    highFreqSum += 0.7;
                } else if (secondDiff < 15) {
                    highFreqSum += 0.5;
                } else {
                    highFreqSum += 0.2;
                }
            }
            
            return highFreqSum / sampleSize;
            
        } catch (Exception e) {
            log.debug("高频分析失败", e);
            return 0.5;
        }
    }
    
    /**
     * 检测局部一致性
     * AI生成图片局部区域通常过于一致
     */
    private double detectLocalConsistency(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int windowSize = 5;
            
            if (width < windowSize * 2 || height < windowSize * 2) {
                return 0.5;
            }
            
            int sampleSize = Math.min(width * height / 400, 2500);
            double consistencySum = 0.0;
            Random random = new Random(42);
            
            for (int i = 0; i < sampleSize; i++) {
                int cx = random.nextInt(width - windowSize) + windowSize / 2;
                int cy = random.nextInt(height - windowSize) + windowSize / 2;
                
                // 计算窗口内的标准差
                double stdDev = calculateWindowStdDev(image, cx, cy, windowSize);
                
                // 标准差小表示区域一致
                if (stdDev < 10) {
                    consistencySum += 0.8; // 过于一致
                } else if (stdDev < 25) {
                    consistencySum += 0.5;
                } else {
                    consistencySum += 0.2;
                }
            }
            
            return consistencySum / sampleSize;
            
        } catch (Exception e) {
            log.debug("一致性检测失败", e);
            return 0.5;
        }
    }
    
    // ==================== 辅助方法 ====================
    
    private double colorDifference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;
        
        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;
        
        return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2) + Math.pow(b1 - b2, 2));
    }
    
    private double calculateGradientX(BufferedImage image, int x, int y) {
        int left = image.getRGB(x - 1, y);
        int right = image.getRGB(x + 1, y);
        return colorDifference(left, right);
    }
    
    private double calculateGradientY(BufferedImage image, int x, int y) {
        int top = image.getRGB(x, y - 1);
        int bottom = image.getRGB(x, y + 1);
        return colorDifference(top, bottom);
    }
    
    private double calculateVariance(int[] histogram) {
        double mean = 0.0;
        int total = 0;
        for (int i = 0; i < histogram.length; i++) {
            mean += i * histogram[i];
            total += histogram[i];
        }
        if (total == 0) return 0;
        mean /= total;
        
        double variance = 0.0;
        for (int i = 0; i < histogram.length; i++) {
            variance += histogram[i] * Math.pow(i - mean, 2);
        }
        return variance / total;
    }
    
    private double compareBlocks(BufferedImage image, int x1, int y1, int x2, int y2, int blockSize) {
        double totalDiff = 0.0;
        int count = 0;
        
        for (int dy = 0; dy < blockSize; dy++) {
            for (int dx = 0; dx < blockSize; dx++) {
                if (x1 + dx < image.getWidth() && y1 + dy < image.getHeight() &&
                    x2 + dx < image.getWidth() && y2 + dy < image.getHeight()) {
                    int rgb1 = image.getRGB(x1 + dx, y1 + dy);
                    int rgb2 = image.getRGB(x2 + dx, y2 + dy);
                    totalDiff += colorDifference(rgb1, rgb2);
                    count++;
                }
            }
        }
        
        if (count == 0) return 0.5;
        double avgDiff = totalDiff / count;
        
        // 差异小于20表示高度相似
        if (avgDiff < 20) return 0.8;
        else if (avgDiff < 40) return 0.6;
        else if (avgDiff < 60) return 0.4;
        else return 0.2;
    }
    
    private double calculateWindowStdDev(BufferedImage image, int cx, int cy, int windowSize) {
        List<Integer> values = new ArrayList<>();
        int halfSize = windowSize / 2;
        
        for (int dy = -halfSize; dy <= halfSize; dy++) {
            for (int dx = -halfSize; dx <= halfSize; dx++) {
                int x = cx + dx;
                int y = cy + dy;
                if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
                    int rgb = image.getRGB(x, y);
                    int gray = ((rgb >> 16) & 0xFF + (rgb >> 8) & 0xFF + (rgb & 0xFF)) / 3;
                    values.add(gray);
                }
            }
        }
        
        if (values.isEmpty()) return 0;
        
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        
        return Math.sqrt(variance);
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
            
            // 检查URL是否包含豆包相关域名
            if (DOUBAO_KEYWORDS.stream().anyMatch(urlLower::contains)) {
                aiProbability = 0.95;
                indicators.add("URL包含豆包相关域名(+决定性)");
            }
            
            result.put("score", aiProbability);
            result.put("isAI", aiProbability > 0.6);
            result.put("indicators", indicators);
            
        } catch (Exception e) {
            log.error("URL检测失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
}
