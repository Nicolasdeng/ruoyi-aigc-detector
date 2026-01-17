package com.ruoyi.web.service.impl;

import com.ruoyi.web.service.IPaperTextAnalyzer;
import com.ruoyi.web.service.paper.IAiModelDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 论文文本分析器实现
 * 集成AI模型反向推理检测功能
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class PaperTextAnalyzerImpl implements IPaperTextAnalyzer
{
    @Autowired(required = false)
    private List<IAiModelDetector> aiModelDetectors;
    
    // AI常用的模板化表达
    private static final List<String> AI_TEMPLATES = Arrays.asList(
        "综上所述", "总而言之", "通过以上分析", "由此可见", "不难看出",
        "在当今社会", "随着.*的发展", "众所周知", "显而易见", "毋庸置疑",
        "首先.*其次.*最后", "一方面.*另一方面", "不仅.*而且", "既.*又",
        "具有重要意义", "发挥重要作用", "起到关键作用", "产生深远影响"
    );
    
    // 逻辑连接词
    private static final List<String> LOGIC_CONNECTORS = Arrays.asList(
        "首先", "其次", "再次", "最后", "然后", "接着", "随后",
        "因此", "所以", "由此", "从而", "进而", "继而",
        "但是", "然而", "不过", "可是", "虽然", "尽管",
        "同时", "此外", "另外", "而且", "并且", "以及"
    );
    
    @Override
    public BigDecimal analyzeParagraphRisk(String paragraph)
    {
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double totalScore = 0;
        
        // 1. 基础AI特征检测 (40分)
        // 1.1 句式规范性检测 (15分)
        totalScore += checkSentenceStructure(paragraph) * 0.5;
        
        // 1.2 词汇多样性分析 (10分)
        totalScore += checkVocabularyDiversity(paragraph) * 0.4;
        
        // 1.3 逻辑连贯性检测 (10分)
        totalScore += checkLogicalCoherence(paragraph) * 0.4;
        
        // 1.4 模板化表达检测 (5分)
        totalScore += checkTemplateExpressions(paragraph) * 0.25;
        
        // 2. AI模型特征反向推理 (60分)
        // 对文本进行AI模型检测，取最高分作为模型特征得分
        Map<String, Object> aiModelResult = detectAiModel(paragraph);
        if (aiModelResult != null && aiModelResult.containsKey("maxScore")) {
            double aiModelScore = ((Number) aiModelResult.get("maxScore")).doubleValue();
            // AI模型检测分数范围0-100，映射到0-60分
            totalScore += aiModelScore * 0.6;
        }
        
        return BigDecimal.valueOf(Math.min(totalScore, 100)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * AI模型检测 - 反向推理识别可能使用的AI模型
     * 
     * @param text 待检测文本
     * @return 检测结果，包含最可能的AI模型及置信度
     */
    public Map<String, Object> detectAiModel(String text)
    {
        Map<String, Object> result = new HashMap<>();
        
        if (aiModelDetectors == null || aiModelDetectors.isEmpty()) {
            result.put("detected", false);
            result.put("message", "AI模型检测器未配置");
            return result;
        }
        
        // 对每个AI模型进行检测
        Map<String, Double> modelScores = new HashMap<>();
        Map<String, Map<String, Object>> modelFeatures = new HashMap<>();
        
        for (IAiModelDetector detector : aiModelDetectors) {
            try {
                String modelName = detector.getModelName();
                double score = detector.detectModel(text).doubleValue();
                Map<String, String> features = detector.getFeatureDetails(text);
                
                modelScores.put(modelName, score);
                // 转换 Map<String, String> 为 Map<String, Object>
                Map<String, Object> featureObjects = new HashMap<>(features);
                modelFeatures.put(modelName, featureObjects);
            } catch (Exception e) {
                // 忽略单个检测器的错误
            }
        }
        
        if (modelScores.isEmpty()) {
            result.put("detected", false);
            result.put("message", "未检测到AI模型特征");
            return result;
        }
        
        // 找出得分最高的模型
        Map.Entry<String, Double> maxEntry = modelScores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        
        if (maxEntry == null) {
            result.put("detected", false);
            return result;
        }
        
        String detectedModel = maxEntry.getKey();
        double maxScore = maxEntry.getValue();
        
        // 计算置信度（基于最高分与次高分的差距）
        List<Double> sortedScores = modelScores.values().stream()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
        
        double confidence = 0;
        if (sortedScores.size() >= 2) {
            double gap = sortedScores.get(0) - sortedScores.get(1);
            confidence = Math.min(gap / sortedScores.get(0), 1.0) * 100;
        } else if (sortedScores.size() == 1) {
            confidence = sortedScores.get(0) > 50 ? 80 : 50;
        }
        
        // 组装结果
        result.put("detected", maxScore >= 40); // 阈值：40分以上认为检测到AI特征
        result.put("detectedModel", detectedModel);
        result.put("maxScore", maxScore);
        result.put("confidence", Math.round(confidence * 100) / 100.0);
        result.put("allScores", modelScores);
        result.put("modelFeatures", modelFeatures.get(detectedModel));
        
        // 如果置信度较高，获取该模型的优化建议
        if (maxScore >= 50) {
            IAiModelDetector detector = aiModelDetectors.stream()
                .filter(d -> d.getModelName().equals(detectedModel))
                .findFirst()
                .orElse(null);
            
            if (detector != null) {
                List<String> suggestions = detector.generateSuggestions(text, maxScore);
                result.put("modelSuggestions", suggestions);
            }
        }
        
        return result;
    }
    
    /**
     * 检测句式规范性
     */
    private double checkSentenceStructure(String paragraph)
    {
        List<String> sentences = splitSentences(paragraph);
        if (sentences.size() < 2) {
            return 0;
        }
        
        double score = 0;
        
        // 1. 计算句子长度方差 (15分)
        List<Integer> lengths = sentences.stream()
            .map(String::length)
            .collect(Collectors.toList());
        
        double avgLength = lengths.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = lengths.stream()
            .mapToDouble(len -> Math.pow(len - avgLength, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // 标准差越小，说明句子长度越统一，风险越高
        if (stdDev < avgLength * 0.2) {
            score += 15;
        } else if (stdDev < avgLength * 0.3) {
            score += 10;
        } else if (stdDev < avgLength * 0.4) {
            score += 5;
        }
        
        // 2. 检测句式结构规范性 (15分)
        // 检查是否所有句子都是完整的主谓宾结构
        long completeStructures = sentences.stream()
            .filter(s -> isCompleteStructure(s))
            .count();
        
        double completeRate = (double) completeStructures / sentences.size();
        if (completeRate > 0.9) {
            score += 15;
        } else if (completeRate > 0.8) {
            score += 10;
        } else if (completeRate > 0.7) {
            score += 5;
        }
        
        return score;
    }
    
    /**
     * 检测词汇多样性
     */
    private double checkVocabularyDiversity(String paragraph)
    {
        double score = 0;
        
        // 简单分词（按标点和空格分割）
        String[] words = paragraph.split("[\\s\\p{Punct}]+");
        List<String> wordList = Arrays.stream(words)
            .filter(w -> w.length() > 1) // 过滤单字
            .collect(Collectors.toList());
        
        if (wordList.isEmpty()) {
            return 0;
        }
        
        // 1. 计算词汇丰富度 TTR (Type-Token Ratio) (15分)
        Set<String> uniqueWords = new HashSet<>(wordList);
        double ttr = (double) uniqueWords.size() / wordList.size();
        
        // TTR越低，说明词汇重复度越高，风险越高
        if (ttr < 0.4) {
            score += 15;
        } else if (ttr < 0.5) {
            score += 10;
        } else if (ttr < 0.6) {
            score += 5;
        }
        
        // 2. 检测高频词重复 (10分)
        Map<String, Long> wordFreq = wordList.stream()
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
        
        long highFreqWords = wordFreq.values().stream()
            .filter(count -> count > wordList.size() * 0.05) // 出现频率超过5%
            .count();
        
        if (highFreqWords > 5) {
            score += 10;
        } else if (highFreqWords > 3) {
            score += 6;
        } else if (highFreqWords > 1) {
            score += 3;
        }
        
        return score;
    }
    
    /**
     * 检测逻辑连贯性
     */
    private double checkLogicalCoherence(String paragraph)
    {
        double score = 0;
        
        // 统计逻辑连接词出现次数
        long connectorCount = LOGIC_CONNECTORS.stream()
            .filter(paragraph::contains)
            .count();
        
        List<String> sentences = splitSentences(paragraph);
        double connectorRate = (double) connectorCount / Math.max(sentences.size(), 1);
        
        // 逻辑连接词过多，说明结构过于完美，风险较高
        if (connectorRate > 0.8) {
            score += 25;
        } else if (connectorRate > 0.6) {
            score += 18;
        } else if (connectorRate > 0.4) {
            score += 10;
        } else if (connectorRate > 0.3) {
            score += 5;
        }
        
        return score;
    }
    
    /**
     * 检测模板化表达
     */
    private double checkTemplateExpressions(String paragraph)
    {
        double score = 0;
        
        // 统计模板表达出现次数
        long templateCount = AI_TEMPLATES.stream()
            .filter(template -> {
                if (template.contains(".*")) {
                    Pattern pattern = Pattern.compile(template);
                    return pattern.matcher(paragraph).find();
                }
                return paragraph.contains(template);
            })
            .count();
        
        // 每出现一个模板表达，增加风险分数
        if (templateCount >= 4) {
            score = 20;
        } else if (templateCount == 3) {
            score = 15;
        } else if (templateCount == 2) {
            score = 10;
        } else if (templateCount == 1) {
            score = 5;
        }
        
        return score;
    }
    
    @Override
    public List<String> identifyRiskTypes(String paragraph, BigDecimal score)
    {
        List<String> types = new ArrayList<>();
        
        // 1. 基础特征检测
        double sentenceScore = checkSentenceStructure(paragraph);
        double vocabularyScore = checkVocabularyDiversity(paragraph);
        double logicScore = checkLogicalCoherence(paragraph);
        double templateScore = checkTemplateExpressions(paragraph);
        
        if (sentenceScore >= 15) {
            types.add("句式过于规范");
        }
        if (vocabularyScore >= 12) {
            types.add("词汇重复度高");
        }
        if (logicScore >= 15) {
            types.add("逻辑连接词过多");
        }
        if (templateScore >= 10) {
            types.add("模板化表达");
        }
        
        // 2. AI模型特征识别
        Map<String, Object> aiModelResult = detectAiModel(paragraph);
        if (aiModelResult != null && (Boolean) aiModelResult.getOrDefault("detected", false)) {
            String detectedModel = (String) aiModelResult.get("detectedModel");
            double modelScore = ((Number) aiModelResult.get("maxScore")).doubleValue();
            double confidence = ((Number) aiModelResult.get("confidence")).doubleValue();
            
            // 添加AI模型类型
            if (modelScore >= 60 && confidence >= 60) {
                types.add("检测到" + detectedModel + "特征(置信度:" + Math.round(confidence) + "%)");
            } else if (modelScore >= 50) {
                types.add("疑似" + detectedModel + "生成");
            }
            
            // 获取具体的特征描述
            @SuppressWarnings("unchecked")
            Map<String, Object> features = (Map<String, Object>) aiModelResult.get("modelFeatures");
            if (features != null && features.containsKey("topFeatures")) {
                @SuppressWarnings("unchecked")
                List<String> topFeatures = (List<String>) features.get("topFeatures");
                types.addAll(topFeatures);
            }
        }
        
        // 3. 如果分数高但没有识别到具体类型，添加通用类型
        if (types.isEmpty() && score.doubleValue() >= 40) {
            types.add("疑似AI生成");
        }
        
        return types;
    }
    
    @Override
    public Map<String, Object> generateSuggestions(String paragraph, List<String> riskTypes)
    {
        Map<String, Object> suggestions = new HashMap<>();
        List<String> allSuggestions = new ArrayList<>();
        
        // 1. 处理基础风险类型的建议
        for (String type : riskTypes) {
            List<String> typeSuggestions = new ArrayList<>();
            
            // 检测是否为AI模型特征类型
            if (type.startsWith("检测到") || type.startsWith("疑似")) {
                // AI模型特征，从detectAiModel结果中获取针对性建议
                Map<String, Object> aiModelResult = detectAiModel(paragraph);
                if (aiModelResult != null && aiModelResult.containsKey("modelSuggestions")) {
                    @SuppressWarnings("unchecked")
                    List<String> modelSuggestions = (List<String>) aiModelResult.get("modelSuggestions");
                    typeSuggestions.addAll(modelSuggestions);
                }
            } else {
                // 基础类型建议
                switch (type) {
                    case "句式过于规范":
                        typeSuggestions.add("调整句子长度，有长有短更自然");
                        typeSuggestions.add("偶尔使用一些口语化表达");
                        typeSuggestions.add("可以适当打断完整的句式结构");
                        typeSuggestions.add("尝试使用倒装句或疑问句增加变化");
                        break;
                        
                    case "词汇重复度高":
                        typeSuggestions.add("使用同义词替换高频词汇");
                        typeSuggestions.add("增加专业术语的个人理解和注释");
                        typeSuggestions.add("加入具体的实例或案例描述");
                        typeSuggestions.add("用更多样化的表达方式");
                        break;
                        
                    case "逻辑连接词过多":
                        typeSuggestions.add("不必每段都使用'首先、其次、最后'");
                        typeSuggestions.add("可以先说结论，再解释原因");
                        typeSuggestions.add("适当加入个人的思考过程");
                        typeSuggestions.add("减少过度使用逻辑连接词");
                        break;
                        
                    case "模板化表达":
                        typeSuggestions.add("避免使用'综上所述'等常见套话");
                        typeSuggestions.add("用具体例子替代空泛的总结");
                        typeSuggestions.add("加入自己的观察和体会");
                        typeSuggestions.add("使用更具体、更个性化的表达");
                        break;
                        
                    case "疑似AI生成":
                        typeSuggestions.add("增加个人经历和真实案例");
                        typeSuggestions.add("加入主观感受和思考过程");
                        typeSuggestions.add("使用更口语化、不那么完美的表达");
                        typeSuggestions.add("展现独特的观点和见解");
                        break;
                        
                    default:
                        // 对于其他特征类型（如具体AI模型的子特征）
                        if (!typeSuggestions.isEmpty()) {
                            break;
                        }
                        typeSuggestions.add("该特征表明文本可能由AI生成");
                        typeSuggestions.add("建议对相关内容进行人工润色");
                        break;
                }
            }
            
            if (!typeSuggestions.isEmpty()) {
                suggestions.put(type, typeSuggestions);
                allSuggestions.addAll(typeSuggestions);
            }
        }
        
        // 2. 添加AI模型检测摘要信息
        Map<String, Object> aiModelResult = detectAiModel(paragraph);
        if (aiModelResult != null && (Boolean) aiModelResult.getOrDefault("detected", false)) {
            suggestions.put("aiModelDetection", aiModelResult);
        }
        
        // 3. 去重并整理最终建议
        List<String> uniqueSuggestions = allSuggestions.stream()
            .distinct()
            .collect(Collectors.toList());
        
        suggestions.put("all", uniqueSuggestions);
        suggestions.put("riskTypes", riskTypes);
        
        return suggestions;
    }
    
    @Override
    public int countWords(String paragraph)
    {
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return 0;
        }
        // 简单统计：中文按字符数，英文按单词数
        int chineseCount = paragraph.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        String[] englishWords = paragraph.replaceAll("[\\u4e00-\\u9fa5]", "").trim().split("\\s+");
        int englishCount = englishWords.length > 0 && !englishWords[0].isEmpty() ? englishWords.length : 0;
        return chineseCount + englishCount;
    }
    
    @Override
    public List<String> splitSentences(String paragraph)
    {
        if (paragraph == null || paragraph.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 按句号、问号、感叹号分句
        String[] sentences = paragraph.split("[。！？.!?]+");
        return Arrays.stream(sentences)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
    
    @Override
    public BigDecimal calculateDuplicateScore(String content)
    {
        // 这里实现简化的重复度检测
        // 实际应用中可以调用专业的查重API
        
        List<String> sentences = splitSentences(content);
        if (sentences.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // 检测句子间的相似度
        int duplicateCount = 0;
        for (int i = 0; i < sentences.size() - 1; i++) {
            for (int j = i + 1; j < sentences.size(); j++) {
                if (calculateSimilarity(sentences.get(i), sentences.get(j)) > 0.7) {
                    duplicateCount++;
                }
            }
        }
        
        double rate = (double) duplicateCount / sentences.size();
        return BigDecimal.valueOf(Math.min(rate * 100, 100)).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public BigDecimal calculateStyleScore(String content)
    {
        // 风格异常评分：综合多个指标
        double score = 0;
        
        List<String> sentences = splitSentences(content);
        if (sentences.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 1. 段落长度异常 (30分)
        int avgParagraphLength = content.length() / Math.max(content.split("\n").length, 1);
        if (avgParagraphLength > 500) {
            score += 30;
        } else if (avgParagraphLength > 300) {
            score += 20;
        } else if (avgParagraphLength > 200) {
            score += 10;
        }
        
        // 2. 句子长度一致性 (35分)
        double sentenceStructureScore = checkSentenceStructure(content);
        score += sentenceStructureScore * 35 / 30;
        
        // 3. 专业术语密度 (35分)
        long technicalTerms = Arrays.stream(content.split("[\\s\\p{Punct}]+"))
            .filter(w -> w.length() > 3)
            .count();
        double termDensity = (double) technicalTerms / countWords(content);
        if (termDensity > 0.3) {
            score += 35;
        } else if (termDensity > 0.2) {
            score += 25;
        } else if (termDensity > 0.15) {
            score += 15;
        }
        
        return BigDecimal.valueOf(Math.min(score, 100)).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getRiskLevel(BigDecimal score)
    {
        if (score == null) {
            return "low";
        }
        
        double value = score.doubleValue();
        if (value >= 60) {
            return "high";
        } else if (value >= 30) {
            return "medium";
        } else {
            return "low";
        }
    }
    
    /**
     * 判断是否为完整句式结构
     */
    private boolean isCompleteStructure(String sentence)
    {
        // 简单判断：包含主语、谓语的标志
        return sentence.length() > 10 &&
               (sentence.contains("是") || sentence.contains("为") || 
                sentence.contains("有") || sentence.contains("在") ||
                sentence.matches(".*[^，。！？]*[，。！？].*"));
    }
    
    /**
     * 计算两个句子的相似度（简化版）
     */
    private double calculateSimilarity(String s1, String s2)
    {
        Set<Character> set1 = s1.chars().mapToObj(c -> (char)c).collect(Collectors.toSet());
        Set<Character> set2 = s2.chars().mapToObj(c -> (char)c).collect(Collectors.toSet());
        
        Set<Character> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<Character> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
}
