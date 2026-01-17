package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 突发性检测器
 * 分析文本中词汇使用的突发性特征，判断AI生成概率
 * 
 * 原理：真人写作时会出现某些词突然大量使用的现象（突发性）
 * AI生成的文本词汇分布更均匀，缺乏自然的突发性
 * 
 * @author ruoyi
 * @date 2026-01-05
 */
@Component
public class BurstinessDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(BurstinessDetector.class);
    
    private static final String NAME = "突发性检测器";
    private static final double WEIGHT = 0.20;
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public double getWeight() {
        return WEIGHT;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public Map<String, Object> detect(String text) throws Exception {
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 将文本分段
            List<String> segments = splitIntoSegments(text, 50); // 每50字一段
            
            if (segments.size() < 3) {
                return createLowConfidenceResult("文本过短，无法分析突发性", startTime);
            }
            
            // 2. 对每段进行分词
            List<List<String>> segmentWords = segments.stream()
                .map(this::tokenize)
                .collect(Collectors.toList());
            
            // 3. 计算词汇突发性得分
            double burstinessScore = calculateBurstiness(segmentWords);
            
            // 4. 计算词频变异系数
            double cvScore = calculateCoefficientOfVariation(segmentWords);
            
            // 5. 计算词汇峰度（Kurtosis）
            double kurtosisScore = calculateKurtosis(segmentWords);
            
            // 6. 综合计算AI生成得分
            double aiScore = calculateAiScore(burstinessScore, cvScore, kurtosisScore);
            
            // 7. 判断风险等级
            String result = getResultLevel(aiScore);
            double confidence = calculateConfidence(segments.size(), burstinessScore);
            
            // 8. 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("name", NAME);
            response.put("score", aiScore);
            response.put("result", result);
            response.put("confidence", confidence);
            response.put("weight", WEIGHT);
            response.put("responseTime", System.currentTimeMillis() - startTime);
            
            Map<String, Object> details = new HashMap<>();
            details.put("burstinessScore", Math.round(burstinessScore * 100.0) / 100.0);
            details.put("cvScore", Math.round(cvScore * 100.0) / 100.0);
            details.put("kurtosisScore", Math.round(kurtosisScore * 100.0) / 100.0);
            details.put("segmentCount", segments.size());
            details.put("analysis", generateAnalysis(burstinessScore, cvScore, kurtosisScore));
            response.put("details", details);
            
            log.debug("突发性检测完成 - 突发性得分: {}, AI得分: {}, 置信度: {}", 
                burstinessScore, aiScore, confidence);
            
            return response;
            
        } catch (Exception e) {
            log.error("突发性检测失败", e);
            throw new Exception("突发性检测失败: " + e.getMessage());
        }
    }
    
    /**
     * 将文本分段
     */
    private List<String> splitIntoSegments(String text, int segmentLength) {
        List<String> segments = new ArrayList<>();
        int length = text.length();
        
        for (int i = 0; i < length; i += segmentLength) {
            int end = Math.min(i + segmentLength, length);
            segments.add(text.substring(i, end));
        }
        
        return segments;
    }
    
    /**
     * 简单分词
     */
    private List<String> tokenize(String text) {
        String cleaned = text.replaceAll("[\\p{Punct}\\s]+", " ");
        List<String> words = new ArrayList<>();
        
        StringBuilder currentWord = new StringBuilder();
        for (char c : cleaned.toCharArray()) {
            if (isChinese(c)) {
                if (currentWord.length() > 0) {
                    words.add(currentWord.toString());
                    currentWord = new StringBuilder();
                }
                words.add(String.valueOf(c));
            } else if (c == ' ') {
                if (currentWord.length() > 0) {
                    words.add(currentWord.toString());
                    currentWord = new StringBuilder();
                }
            } else {
                currentWord.append(c);
            }
        }
        
        if (currentWord.length() > 0) {
            words.add(currentWord.toString());
        }
        
        return words.stream()
            .map(String::toLowerCase)
            .filter(w -> w.length() > 0)
            .collect(Collectors.toList());
    }
    
    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
    
    /**
     * 计算突发性得分
     * 分析词汇在不同段落中的分布是否具有突发性
     */
    private double calculateBurstiness(List<List<String>> segmentWords) {
        // 统计所有词汇
        Map<String, List<Integer>> wordPositions = new HashMap<>();
        
        for (int i = 0; i < segmentWords.size(); i++) {
            List<String> words = segmentWords.get(i);
            for (String word : words) {
                wordPositions.computeIfAbsent(word, k -> new ArrayList<>()).add(i);
            }
        }
        
        // 计算每个词的突发性得分
        List<Double> burstinessScores = new ArrayList<>();
        
        for (Map.Entry<String, List<Integer>> entry : wordPositions.entrySet()) {
            List<Integer> positions = entry.getValue();
            if (positions.size() >= 2) {
                // 计算位置间隔的方差
                double avgInterval = (double) (positions.get(positions.size() - 1) - positions.get(0)) / (positions.size() - 1);
                
                double variance = 0.0;
                for (int i = 1; i < positions.size(); i++) {
                    int interval = positions.get(i) - positions.get(i - 1);
                    variance += Math.pow(interval - avgInterval, 2);
                }
                variance /= (positions.size() - 1);
                
                // 突发性 = 方差 / 平均间隔（标准化）
                if (avgInterval > 0) {
                    double burstiness = variance / avgInterval;
                    burstinessScores.add(burstiness);
                }
            }
        }
        
        // 返回平均突发性得分
        return burstinessScores.isEmpty() ? 0.0 : 
            burstinessScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * 计算变异系数（Coefficient of Variation）
     * 衡量词频分布的离散程度
     */
    private double calculateCoefficientOfVariation(List<List<String>> segmentWords) {
        // 统计每个段落的词频
        List<Map<String, Long>> segmentFreqs = segmentWords.stream()
            .map(words -> words.stream()
                .collect(Collectors.groupingBy(w -> w, Collectors.counting())))
            .collect(Collectors.toList());
        
        // 获取所有出现过的词
        Set<String> allWords = segmentWords.stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        
        List<Double> cvScores = new ArrayList<>();
        
        // 对每个词计算在不同段落中的频率变异系数
        for (String word : allWords) {
            List<Long> freqs = new ArrayList<>();
            for (Map<String, Long> segFreq : segmentFreqs) {
                freqs.add(segFreq.getOrDefault(word, 0L));
            }
            
            // 计算均值和标准差
            double mean = freqs.stream().mapToLong(Long::longValue).average().orElse(0.0);
            if (mean > 0) {
                double variance = freqs.stream()
                    .mapToDouble(f -> Math.pow(f - mean, 2))
                    .average()
                    .orElse(0.0);
                double stdDev = Math.sqrt(variance);
                
                // 变异系数 = 标准差 / 均值
                double cv = stdDev / mean;
                cvScores.add(cv);
            }
        }
        
        // 返回平均变异系数
        return cvScores.isEmpty() ? 0.0 :
            cvScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    /**
     * 计算峰度（Kurtosis）
     * 衡量词频分布的尖峰程度
     */
    private double calculateKurtosis(List<List<String>> segmentWords) {
        // 统计全文词频
        Map<String, Long> wordFreq = segmentWords.stream()
            .flatMap(List::stream)
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        
        List<Long> freqs = new ArrayList<>(wordFreq.values());
        
        if (freqs.size() < 4) {
            return 0.0;
        }
        
        // 计算均值
        double mean = freqs.stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        // 计算标准差
        double variance = freqs.stream()
            .mapToDouble(f -> Math.pow(f - mean, 2))
            .average()
            .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        
        if (stdDev == 0) {
            return 0.0;
        }
        
        // 计算峰度
        double fourthMoment = freqs.stream()
            .mapToDouble(f -> Math.pow((f - mean) / stdDev, 4))
            .average()
            .orElse(0.0);
        
        // 峰度 = 四阶中心矩 - 3（标准正态分布的峰度为0）
        return fourthMoment - 3.0;
    }
    
    /**
     * 计算AI生成得分
     * 
     * 突发性越低，变异系数越小，峰度越低 → AI概率越高
     */
    private double calculateAiScore(double burstiness, double cv, double kurtosis) {
        double score = 0.0;
        
        // 1. 突发性得分（权重40%）
        if (burstiness < 0.5) {
            score += 40;
        } else if (burstiness < 1.0) {
            score += 40 - (burstiness - 0.5) * 60;
        } else if (burstiness < 2.0) {
            score += Math.max(0, 10 - (burstiness - 1.0) * 10);
        }
        
        // 2. 变异系数得分（权重35%）
        if (cv < 0.5) {
            score += 35;
        } else if (cv < 1.0) {
            score += 35 - (cv - 0.5) * 50;
        } else if (cv < 2.0) {
            score += Math.max(0, 10 - (cv - 1.0) * 10);
        }
        
        // 3. 峰度得分（权重25%）
        // 负峰度（平顶分布）或接近0的峰度 → AI概率高
        if (kurtosis >= -1 && kurtosis <= 1) {
            score += 25;
        } else if (kurtosis > 1 && kurtosis <= 3) {
            score += 25 - (kurtosis - 1) * 10;
        } else if (kurtosis < -1 && kurtosis >= -3) {
            score += 25 - (Math.abs(kurtosis) - 1) * 10;
        } else {
            score += Math.max(0, 5 - Math.abs(kurtosis - 2) * 2);
        }
        
        return Math.min(100, Math.max(0, score));
    }
    
    /**
     * 计算置信度
     */
    private double calculateConfidence(int segmentCount, double burstiness) {
        // 基础置信度根据段落数
        double baseConfidence;
        if (segmentCount < 5) {
            baseConfidence = 0.5;
        } else if (segmentCount < 10) {
            baseConfidence = 0.7;
        } else if (segmentCount < 20) {
            baseConfidence = 0.85;
        } else {
            baseConfidence = 0.95;
        }
        
        // 根据突发性的极端程度调整
        double extremeness;
        if (burstiness < 0.3 || burstiness > 3.0) {
            extremeness = 1.0;
        } else if (burstiness < 0.5 || burstiness > 2.5) {
            extremeness = 0.9;
        } else if (burstiness < 0.8 || burstiness > 2.0) {
            extremeness = 0.8;
        } else {
            extremeness = 0.7;
        }
        
        return Math.round(baseConfidence * extremeness * 1000.0) / 10.0;
    }
    
    /**
     * 判断风险等级
     */
    private String getResultLevel(double score) {
        if (score >= 70) {
            return "AI_GENERATED";
        } else if (score >= 40) {
            return "UNCERTAIN";
        } else {
            return "HUMAN_WRITTEN";
        }
    }
    
    /**
     * 生成分析说明
     */
    private String generateAnalysis(double burstiness, double cv, double kurtosis) {
        StringBuilder analysis = new StringBuilder();
        
        if (burstiness < 0.5) {
            analysis.append("词汇分布过于均匀，缺乏自然的突发性特征。");
        } else if (burstiness < 1.5) {
            analysis.append("词汇分布较为均匀，突发性不明显。");
        } else if (burstiness < 2.5) {
            analysis.append("词汇使用具有一定突发性，符合真人写作特征。");
        } else {
            analysis.append("词汇使用具有明显的突发性，强烈符合真人写作特征。");
        }
        
        analysis.append(" ");
        
        if (cv < 0.5) {
            analysis.append("词频变异系数很低，表明用词过于规律。");
        } else if (cv < 1.5) {
            analysis.append("词频变异系数中等，用词规律性一般。");
        } else {
            analysis.append("词频变异系数较高，用词具有多样性。");
        }
        
        return analysis.toString();
    }
    
    /**
     * 创建低置信度结果
     */
    private Map<String, Object> createLowConfidenceResult(String reason, long startTime) {
        Map<String, Object> response = new HashMap<>();
        response.put("name", NAME);
        response.put("score", 50.0);
        response.put("result", "UNCERTAIN");
        response.put("confidence", 30.0);
        response.put("weight", WEIGHT);
        response.put("responseTime", System.currentTimeMillis() - startTime);
        
        Map<String, Object> details = new HashMap<>();
        details.put("analysis", reason);
        response.put("details", details);
        
        return response;
    }
}
