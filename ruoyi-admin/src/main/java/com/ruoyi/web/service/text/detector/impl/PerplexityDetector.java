package com.ruoyi.web.service.text.detector.impl;

import com.ruoyi.web.service.text.detector.ITextDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 困惑度检测器
 * 基于n-gram语言模型计算文本的困惑度，判断AI生成概率
 * 
 * 原理：AI生成的文本倾向于使用高概率的词汇序列，导致困惑度较低
 * 真人写作具有更多的不确定性和创造性，困惑度较高
 * 
 * @author ruoyi
 * @date 2026-01-05
 */
@Component
public class PerplexityDetector implements ITextDetector {
    
    private static final Logger log = LoggerFactory.getLogger(PerplexityDetector.class);
    
    private static final String NAME = "困惑度检测器";
    private static final double WEIGHT = 0.25; // 高权重
    
    // 基于大规模中文语料库的统计频率（简化版，实际应用中应使用更大的语料库）
    // 这里使用常见词作为高频词示例
    private static final Set<String> HIGH_FREQ_WORDS = new HashSet<>(Arrays.asList(
        "的", "是", "在", "了", "和", "有", "我", "他", "不", "人", "都", "一", "到", "说", "要", "上",
        "这", "来", "个", "中", "大", "为", "们", "地", "她", "于", "着", "会", "可", "能", "就", "得",
        "发展", "社会", "经济", "工作", "问题", "进行", "建设", "重要", "国家", "企业", "管理", "技术",
        "系统", "研究", "分析", "方法", "通过", "实现", "提高", "加强", "促进", "推动", "创新", "改革"
    ));
    
    // AI常用的高频2-gram（基于对主流AI模型的观察）
    private static final Set<String> AI_COMMON_BIGRAMS = new HashSet<>(Arrays.asList(
        "可以_说", "总而言之", "综上所述", "由此可见", "不难_看出", "显而易见",
        "首先_需要", "其次_要", "最后_是", "一方面_另一方面", "不仅_而且",
        "具有_重要", "发挥_作用", "起到_关键", "产生_影响", "实现_目标",
        "通过_分析", "根据_研究", "基于_数据", "采用_方法", "运用_技术"
    ));
    
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
            // 1. 文本预处理
            List<String> words = tokenize(text);
            
            if (words.size() < 10) {
                return createLowConfidenceResult("文本过短，无法准确计算困惑度", startTime);
            }
            
            // 2. 计算1-gram困惑度（词频分布）
            double unigramPerplexity = calculateUnigramPerplexity(words);
            
            // 3. 计算2-gram困惑度（词对频率）
            double bigramPerplexity = calculateBigramPerplexity(words);
            
            // 4. 计算3-gram困惑度（三词组合）
            double trigramPerplexity = calculateTrigramPerplexity(words);
            
            // 5. 综合计算最终困惑度得分
            double avgPerplexity = (unigramPerplexity * 0.3 + bigramPerplexity * 0.4 + trigramPerplexity * 0.3);
            
            // 6. 计算AI生成概率得分（0-100）
            // 困惑度越低，AI生成概率越高
            double aiScore = calculateAiScore(avgPerplexity);
            
            // 7. 判断风险等级
            String result = getResultLevel(aiScore);
            double confidence = calculateConfidence(words.size(), avgPerplexity);
            
            // 8. 构建返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("name", NAME);
            response.put("score", aiScore);
            response.put("result", result);
            response.put("confidence", confidence);
            response.put("weight", WEIGHT);
            response.put("responseTime", System.currentTimeMillis() - startTime);
            
            Map<String, Object> details = new HashMap<>();
            details.put("avgPerplexity", Math.round(avgPerplexity * 100.0) / 100.0);
            details.put("unigramPerplexity", Math.round(unigramPerplexity * 100.0) / 100.0);
            details.put("bigramPerplexity", Math.round(bigramPerplexity * 100.0) / 100.0);
            details.put("trigramPerplexity", Math.round(trigramPerplexity * 100.0) / 100.0);
            details.put("wordCount", words.size());
            details.put("analysis", generateAnalysis(avgPerplexity, aiScore));
            response.put("details", details);
            
            log.debug("困惑度检测完成 - 平均困惑度: {}, AI得分: {}, 置信度: {}", 
                avgPerplexity, aiScore, confidence);
            
            return response;
            
        } catch (Exception e) {
            log.error("困惑度检测失败", e);
            throw new Exception("困惑度检测失败: " + e.getMessage());
        }
    }
    
    /**
     * 分词（简化版，实际应使用专业分词工具）
     */
    private List<String> tokenize(String text) {
        // 移除标点符号
        String cleaned = text.replaceAll("[\\p{Punct}\\s]+", " ");
        
        List<String> words = new ArrayList<>();
        
        // 简单分词：中文按字分，英文按词分
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
    
    /**
     * 判断是否为中文字符
     */
    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
    
    /**
     * 计算1-gram困惑度
     */
    private double calculateUnigramPerplexity(List<String> words) {
        Map<String, Long> wordFreq = words.stream()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        
        double entropy = 0.0;
        int totalWords = words.size();
        
        for (Long freq : wordFreq.values()) {
            double prob = (double) freq / totalWords;
            entropy -= prob * Math.log(prob) / Math.log(2);
        }
        
        // 困惑度 = 2^熵
        return Math.pow(2, entropy);
    }
    
    /**
     * 计算2-gram困惑度
     */
    private double calculateBigramPerplexity(List<String> words) {
        if (words.size() < 2) {
            return 1.0;
        }
        
        List<String> bigrams = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            bigrams.add(words.get(i) + "_" + words.get(i + 1));
        }
        
        Map<String, Long> bigramFreq = bigrams.stream()
            .collect(Collectors.groupingBy(b -> b, Collectors.counting()));
        
        // 检测AI常用bigram的占比
        long aiCommonCount = bigrams.stream()
            .filter(AI_COMMON_BIGRAMS::contains)
            .count();
        double aiCommonRatio = (double) aiCommonCount / bigrams.size();
        
        double entropy = 0.0;
        for (Long freq : bigramFreq.values()) {
            double prob = (double) freq / bigrams.size();
            entropy -= prob * Math.log(prob) / Math.log(2);
        }
        
        double perplexity = Math.pow(2, entropy);
        
        // AI常用bigram占比高，降低困惑度
        return perplexity * (1 - aiCommonRatio * 0.3);
    }
    
    /**
     * 计算3-gram困惑度
     */
    private double calculateTrigramPerplexity(List<String> words) {
        if (words.size() < 3) {
            return 1.0;
        }
        
        List<String> trigrams = new ArrayList<>();
        for (int i = 0; i < words.size() - 2; i++) {
            trigrams.add(words.get(i) + "_" + words.get(i + 1) + "_" + words.get(i + 2));
        }
        
        Map<String, Long> trigramFreq = trigrams.stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        
        // 计算独特三元组的比例
        double uniqueRatio = (double) trigramFreq.size() / trigrams.size();
        
        double entropy = 0.0;
        for (Long freq : trigramFreq.values()) {
            double prob = (double) freq / trigrams.size();
            entropy -= prob * Math.log(prob) / Math.log(2);
        }
        
        double perplexity = Math.pow(2, entropy);
        
        // 独特三元组占比高，增加困惑度（更像真人写作）
        return perplexity * (0.7 + uniqueRatio * 0.6);
    }
    
    /**
     * 根据困惑度计算AI生成得分
     * 
     * 参考值：
     * - AI生成：困惑度 20-60，得分 70-100
     * - 真人写作：困惑度 70-150，得分 0-30
     * - 过渡区：困惑度 60-70，得分 30-70
     */
    private double calculateAiScore(double perplexity) {
        if (perplexity <= 20) {
            return 100.0; // 极低困惑度，几乎确定是AI
        } else if (perplexity <= 60) {
            // 线性映射：20-60 -> 100-70
            return 100 - (perplexity - 20) * 0.75;
        } else if (perplexity <= 70) {
            // 线性映射：60-70 -> 70-30
            return 70 - (perplexity - 60) * 4;
        } else if (perplexity <= 150) {
            // 线性映射：70-150 -> 30-0
            return Math.max(0, 30 - (perplexity - 70) * 0.375);
        } else {
            return 0.0; // 极高困惑度，几乎确定是真人
        }
    }
    
    /**
     * 计算置信度
     */
    private double calculateConfidence(int wordCount, double perplexity) {
        // 基础置信度根据文本长度
        double baseConfidence;
        if (wordCount < 50) {
            baseConfidence = 0.5;
        } else if (wordCount < 100) {
            baseConfidence = 0.7;
        } else if (wordCount < 200) {
            baseConfidence = 0.85;
        } else {
            baseConfidence = 0.95;
        }
        
        // 根据困惑度的极端程度调整置信度
        // 极低或极高的困惑度更可信
        double extremeness;
        if (perplexity < 40 || perplexity > 120) {
            extremeness = 1.0;
        } else if (perplexity < 50 || perplexity > 100) {
            extremeness = 0.9;
        } else if (perplexity < 60 || perplexity > 80) {
            extremeness = 0.8;
        } else {
            extremeness = 0.7; // 中间区域置信度较低
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
    private String generateAnalysis(double perplexity, double aiScore) {
        StringBuilder analysis = new StringBuilder();
        
        if (perplexity < 40) {
            analysis.append("文本困惑度极低(").append(String.format("%.2f", perplexity))
                    .append(")，词汇序列高度可预测，强烈符合AI生成特征。");
        } else if (perplexity < 60) {
            analysis.append("文本困惑度较低(").append(String.format("%.2f", perplexity))
                    .append(")，词汇选择偏向常见组合，可能是AI生成。");
        } else if (perplexity < 80) {
            analysis.append("文本困惑度中等(").append(String.format("%.2f", perplexity))
                    .append(")，难以明确判断。");
        } else if (perplexity < 120) {
            analysis.append("文本困惑度较高(").append(String.format("%.2f", perplexity))
                    .append(")，词汇使用具有创造性，倾向真人写作。");
        } else {
            analysis.append("文本困惑度很高(").append(String.format("%.2f", perplexity))
                    .append(")，词汇组合多样且独特，强烈符合真人写作特征。");
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
