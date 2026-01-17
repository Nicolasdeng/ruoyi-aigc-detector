package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Perplexity AI模型检测器
 * 
 * Perplexity特征：
 * 1. 引用来源：频繁使用引用标记和来源说明
 * 2. 问答式：倾向于问题-回答的结构
 * 3. 信息密集：内容信息量大，数据准确
 * 4. 链接引用：经常使用"根据...""据...报道"等引用句式
 * 5. 实时性强：强调最新信息和时效性
 * 
 * 检测维度（总分100）：
 * - 引用特征：30分
 * - 问答结构：25分
 * - 信息密度：20分
 * - 来源标记：15分
 * - 时效性词汇：10分
 *
 * @author ruoyi
 */
@Component
public class PerplexityModelDetector implements IAiModelDetector {

    @Override
    public String getModelName() {
        return "Perplexity";
    }

    @Override
    public String getDetectorName() {
        return "Perplexity模型检测器";
    }

    @Override
    public BigDecimal detectModel(String content) {
        Map<String, Object> result = detectModelWithDetails(content);
        double score = (Double) result.get("score");
        return BigDecimal.valueOf(score);
    }

    public Map<String, Object> detectModelWithDetails(String text) {
        Map<String, Object> result = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            result.put("score", 0.0);
            result.put("confidence", 0.0);
            result.put("details", "文本为空");
            return result;
        }
        
        // 基础特征得分（40%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（60%权重）
        double paperScore = calculatePaperScore(text);
        
        // 综合得分
        double finalScore = baseScore * 0.4 + paperScore * 0.6;
        
        Map<String, Double> dimensionScores = new HashMap<>();
        dimensionScores.put("基础特征", baseScore);
        dimensionScores.put("论文特征", paperScore);
        
        result.put("score", Math.min(100.0, finalScore));
        result.put("dimensionScores", dimensionScores);
        result.put("details", String.format("基础特征:%.1f 论文特征:%.1f 综合得分:%.1f",
                baseScore, paperScore, finalScore));
        
        return result;
    }
    
    /**
     * 计算基础特征得分（保留原有检测逻辑）
     */
    private double calculateBaseScore(String text) {
        double totalScore = 0.0;
        
        // 1. 引用特征检测（30分）
        double citationScore = detectCitationFeatures(text);
        totalScore += citationScore;
        
        // 2. 问答结构检测（25分）
        double qaScore = detectQuestionAnswerStructure(text);
        totalScore += qaScore;
        
        // 3. 信息密度检测（20分）
        double densityScore = detectInformationDensity(text);
        totalScore += densityScore;
        
        // 4. 来源标记检测（15分）
        double sourceScore = detectSourceMarkers(text);
        totalScore += sourceScore;
        
        // 5. 时效性词汇检测（10分）
        double timelinessScore = detectTimelinessVocabulary(text);
        totalScore += timelinessScore;
        
        return totalScore;
    }
    
    /**
     * 计算论文专属特征得分
     * Perplexity特点：搜索整合能力强、引用来源丰富、实时信息准确、知识深度和参考文献是其优势
     * 
     * 权重分配：
     * - 参考文献：25%（引用来源丰富，70-90分是显著特征）
     * - 知识深度：22%（搜索整合能力强，知识覆盖广，65-85分）
     * - 学术规范性：18%（引用规范，格式严谨，68-85分）
     * - 数据实证：15%（实时信息准确，数据支撑强，62-82分）
     * - 论证结构：10%（逻辑清晰但重在信息呈现，55-75分）
     * - 语言连贯性：6%（连贯性适中，58-75分）
     * - 写作风格：2%（偏信息整合风格，50-68分）
     * - 创新性：2%（重在整合而非创新，45-65分）
     */
    private double calculatePaperScore(String text) {
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 参考文献（25%权重，目标区间70-90）
        score += adjustScoreRange(features.get("referencePattern"), 70, 90) * 0.25;
        
        // 知识深度（22%权重，目标区间65-85）
        score += adjustScoreRange(features.get("knowledgeDepth"), 65, 85) * 0.22;
        
        // 学术规范性（18%权重，目标区间68-85）
        score += adjustScoreRange(features.get("academicFormality"), 68, 85) * 0.18;
        
        // 数据实证（15%权重，目标区间62-82）
        score += adjustScoreRange(features.get("empiricalEvidence"), 62, 82) * 0.15;
        
        // 论证结构（10%权重，目标区间55-75）
        score += adjustScoreRange(features.get("argumentationStructure"), 55, 75) * 0.10;
        
        // 语言连贯性（6%权重，目标区间58-75）
        score += adjustScoreRange(features.get("languageCoherence"), 58, 75) * 0.06;
        
        // 写作风格（2%权重，目标区间50-68）
        score += adjustScoreRange(features.get("writingStyle"), 50, 68) * 0.02;
        
        // 创新性（2%权重，目标区间45-65）
        score += adjustScoreRange(features.get("innovation"), 45, 65) * 0.02;
        
        return score;
    }
    
    /**
     * 将0-100的得分映射到目标区间
     */
    private double adjustScoreRange(double score, double minTarget, double maxTarget) {
        // 将0-100映射到minTarget-maxTarget
        return minTarget + (score / 100.0) * (maxTarget - minTarget);
    }

    /**
     * 检测引用特征
     * Perplexity经常使用[1]、[2]等引用标记，或"根据研究"等引用句式
     */
    private double detectCitationFeatures(String text) {
        double score = 0;
        
        // 检测数字引用标记 [1], [2], [3] 等
        Pattern numberRefPattern = Pattern.compile("\\[\\d+\\]");
        Matcher numberRefMatcher = numberRefPattern.matcher(text);
        int numberRefCount = 0;
        while (numberRefMatcher.find()) {
            numberRefCount++;
        }
        
        // 检测引用句式
        String[] citationPhrases = {
            "根据", "据", "显示", "表明", "指出", "研究发现",
            "数据显示", "报告指出", "调查显示", "统计表明"
        };
        int citationPhraseCount = 0;
        for (String phrase : citationPhrases) {
            Pattern pattern = Pattern.compile(phrase);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                citationPhraseCount++;
            }
        }
        
        // 计算引用密度（每千字）
        int textLength = text.length();
        double refDensity = (numberRefCount + citationPhraseCount) * 1000.0 / textLength;
        
        // Perplexity的引用密度通常在5-15/千字
        if (refDensity >= 5 && refDensity <= 15) {
            score += 20;
        } else if (refDensity >= 3 && refDensity < 5) {
            score += 10;
        } else if (refDensity > 15 && refDensity <= 20) {
            score += 10;
        }
        
        // 如果有数字引用标记，额外加分
        if (numberRefCount >= 3) {
            score += 10;
        } else if (numberRefCount >= 1) {
            score += 5;
        }
        
        return Math.min(score, 30);
    }

    /**
     * 检测问答结构
     * Perplexity倾向于使用问题-回答的结构
     */
    private double detectQuestionAnswerStructure(String text) {
        double score = 0;
        
        // 检测疑问句
        String[] questionMarkers = {"？", "吗", "呢", "什么", "如何", "怎么", "为什么", "是否"};
        int questionCount = 0;
        for (String marker : questionMarkers) {
            Pattern pattern = Pattern.compile(marker);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                questionCount++;
            }
        }
        
        // 检测回答句式
        String[] answerPhrases = {
            "答案是", "简单来说", "总的来说", "具体而言",
            "可以", "能够", "需要", "应该", "包括", "主要有"
        };
        int answerPhraseCount = 0;
        for (String phrase : answerPhrases) {
            if (text.contains(phrase)) {
                answerPhraseCount++;
            }
        }
        
        // 计算问答比例
        int textLength = text.length();
        double qaDensity = questionCount * 1000.0 / textLength;
        
        // Perplexity的问句密度通常在2-6/千字
        if (qaDensity >= 2 && qaDensity <= 6) {
            score += 15;
        } else if (qaDensity >= 1 && qaDensity < 2) {
            score += 8;
        }
        
        // 如果问答配对良好，额外加分
        if (questionCount > 0 && answerPhraseCount >= questionCount * 0.5) {
            score += 10;
        } else if (answerPhraseCount > 0) {
            score += 5;
        }
        
        return Math.min(score, 25);
    }

    /**
     * 检测信息密度
     * Perplexity的内容信息量大，包含大量具体数据和细节
     */
    private double detectInformationDensity(String text) {
        double score = 0;
        
        // 检测数字和数据
        Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)?(%|万|亿|千|百|个|次|年|月|日)?");
        Matcher numberMatcher = numberPattern.matcher(text);
        int numberCount = 0;
        while (numberMatcher.find()) {
            numberCount++;
        }
        
        // 检测专业术语和名词
        Pattern nounPattern = Pattern.compile("[A-Z][a-z]+|[A-Z]{2,}");
        Matcher nounMatcher = nounPattern.matcher(text);
        int properNounCount = 0;
        while (nounMatcher.find()) {
            properNounCount++;
        }
        
        // 计算信息密度
        int textLength = text.length();
        double infoDensity = (numberCount + properNounCount) * 1000.0 / textLength;
        
        // Perplexity的信息密度通常在10-30/千字
        if (infoDensity >= 10 && infoDensity <= 30) {
            score += 15;
        } else if (infoDensity >= 5 && infoDensity < 10) {
            score += 8;
        } else if (infoDensity > 30 && infoDensity <= 40) {
            score += 8;
        }
        
        // 如果数字较多，额外加分
        if (numberCount >= 5) {
            score += 5;
        }
        
        return Math.min(score, 20);
    }

    /**
     * 检测来源标记
     * Perplexity经常标注信息来源
     */
    private double detectSourceMarkers(String text) {
        double score = 0;
        
        // 检测来源引导词
        String[] sourceMarkers = {
            "来源", "出处", "引用", "参考", "摘自",
            "根据...的数据", "据...报道", "...指出", "...表示",
            "...发布", "...公布", "...显示"
        };
        
        int sourceCount = 0;
        for (String marker : sourceMarkers) {
            if (text.contains(marker)) {
                sourceCount++;
            }
        }
        
        // 检测网址或链接引用
        Pattern urlPattern = Pattern.compile("(https?://|www\\.)");
        Matcher urlMatcher = urlPattern.matcher(text);
        int urlCount = 0;
        while (urlMatcher.find()) {
            urlCount++;
        }
        
        // 来源标记评分
        if (sourceCount >= 3) {
            score += 10;
        } else if (sourceCount >= 1) {
            score += 5;
        }
        
        // 如果有URL引用，额外加分
        if (urlCount >= 1) {
            score += 5;
        }
        
        return Math.min(score, 15);
    }

    /**
     * 检测时效性词汇
     * Perplexity强调最新信息和实时性
     */
    private double detectTimelinessVocabulary(String text) {
        double score = 0;
        
        // 时效性词汇
        String[] timelinessWords = {
            "最新", "最近", "目前", "现在", "当前", "今年", "本月",
            "刚刚", "近期", "新", "更新", "实时", "即时"
        };
        
        int timelinessCount = 0;
        for (String word : timelinessWords) {
            if (text.contains(word)) {
                timelinessCount++;
            }
        }
        
        // 检测具体时间引用
        Pattern timePattern = Pattern.compile("\\d{4}年|\\d{1,2}月|\\d{1,2}日");
        Matcher timeMatcher = timePattern.matcher(text);
        int timeRefCount = 0;
        while (timeMatcher.find()) {
            timeRefCount++;
        }
        
        // 评分
        if (timelinessCount >= 3) {
            score += 6;
        } else if (timelinessCount >= 1) {
            score += 3;
        }
        
        if (timeRefCount >= 2) {
            score += 4;
        } else if (timeRefCount >= 1) {
            score += 2;
        }
        
        return Math.min(score, 10);
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> details = new HashMap<>();
        
        // Perplexity基础特征详情
        Pattern numberRefPattern = Pattern.compile("\\[\\d+\\]");
        Matcher numberRefMatcher = numberRefPattern.matcher(text);
        int refCount = 0;
        while (numberRefMatcher.find()) {
            refCount++;
        }
        details.put("数字引用", refCount + "处");
        
        int questionCount = text.split("？").length - 1;
        details.put("疑问句", questionCount + "个");
        
        Pattern numberPattern = Pattern.compile("\\d+");
        Matcher numberMatcher = numberPattern.matcher(text);
        int numberCount = 0;
        while (numberMatcher.find()) {
            numberCount++;
        }
        details.put("数据点", numberCount + "个");
        
        int sourceCount = 0;
        String[] sourceMarkers = {"根据", "据", "显示", "表明"};
        for (String marker : sourceMarkers) {
            if (text.contains(marker)) {
                sourceCount++;
            }
        }
        details.put("来源引用", sourceCount + "处");
        
        // 论文专属特征得分
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        details.put("学术规范性", String.format("%.1f分", features.get("academicFormality")));
        details.put("论证结构", String.format("%.1f分", features.get("argumentationStructure")));
        details.put("知识深度", String.format("%.1f分", features.get("knowledgeDepth")));
        details.put("写作风格", String.format("%.1f分", features.get("writingStyle")));
        details.put("参考文献", String.format("%.1f分", features.get("referencePattern")));
        details.put("创新性", String.format("%.1f分", features.get("innovation")));
        details.put("语言连贯性", String.format("%.1f分", features.get("languageCoherence")));
        details.put("数据实证", String.format("%.1f分", features.get("empiricalEvidence")));
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        return generateSuggestions(content, matchScore.doubleValue());
    }

    @Override
    public List<String> generateSuggestions(String text) {
        Map<String, Object> result = detectModelWithDetails(text);
        double score = (Double) result.get("score");
        return generateSuggestions(text, score);
    }

    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = new ArrayList<>();
        
        // 一、Perplexity基础特征分析
        suggestions.add("=== Perplexity基础特征分析 ===");
        
        double citationScore = detectCitationFeatures(text);
        if (citationScore >= 20) {
            suggestions.add("✓ 引用特征明显：频繁使用引用标记和来源说明");
            suggestions.add("  建议：减少引用标记，将引用内容融入正文");
        }
        
        double qaScore = detectQuestionAnswerStructure(text);
        if (qaScore >= 18) {
            suggestions.add("✓ 问答结构明显：倾向于问题-回答的结构");
            suggestions.add("  建议：减少问答式结构，改用陈述式表达");
        }
        
        double densityScore = detectInformationDensity(text);
        if (densityScore >= 15) {
            suggestions.add("✓ 信息密集：内容信息量大，数据准确");
            suggestions.add("  建议：适当减少数据堆砌，增加分析解释");
        }
        
        double sourceScore = detectSourceMarkers(text);
        if (sourceScore >= 10) {
            suggestions.add("✓ 来源标记丰富：经常标注信息来源");
            suggestions.add("  建议：减少来源标注，改用间接引用");
        }
        
        double timelinessScore = detectTimelinessVocabulary(text);
        if (timelinessScore >= 6) {
            suggestions.add("✓ 时效性强：强调最新信息和时效性");
            suggestions.add("  建议：减少时效性词汇，使用更中性表达");
        }
        
        // 二、论文专属特征分析
        suggestions.add("\n=== 论文专属特征分析 ===");
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double referenceScore = features.get("referencePattern");
        suggestions.add(String.format("参考文献模式：%.1f分（Perplexity目标区间70-90分）", referenceScore));
        if (referenceScore >= 70 && referenceScore <= 90) {
            suggestions.add("  ✓ 引用来源丰富，符合Perplexity特征");
        }
        
        double knowledgeScore = features.get("knowledgeDepth");
        suggestions.add(String.format("知识深度：%.1f分（Perplexity目标区间65-85分）", knowledgeScore));
        if (knowledgeScore >= 65 && knowledgeScore <= 85) {
            suggestions.add("  ✓ 知识整合能力强，覆盖广泛");
        }
        
        double formalityScore = features.get("academicFormality");
        suggestions.add(String.format("学术规范性：%.1f分（Perplexity目标区间68-85分）", formalityScore));
        if (formalityScore >= 68 && formalityScore <= 85) {
            suggestions.add("  ✓ 引用规范，格式严谨");
        }
        
        double evidenceScore = features.get("empiricalEvidence");
        suggestions.add(String.format("数据实证：%.1f分（Perplexity目标区间62-82分）", evidenceScore));
        if (evidenceScore >= 62 && evidenceScore <= 82) {
            suggestions.add("  ✓ 实时信息准确，数据支撑强");
        }
        
        double structureScore = features.get("argumentationStructure");
        suggestions.add(String.format("论证结构：%.1f分（Perplexity目标区间55-75分）", structureScore));
        
        double coherenceScore = features.get("languageCoherence");
        suggestions.add(String.format("语言连贯性：%.1f分（Perplexity目标区间58-75分）", coherenceScore));
        
        double styleScore = features.get("writingStyle");
        suggestions.add(String.format("写作风格：%.1f分（Perplexity目标区间50-68分）", styleScore));
        
        double innovationScore = features.get("innovation");
        suggestions.add(String.format("创新性：%.1f分（Perplexity目标区间45-65分）", innovationScore));
        
        // 三、综合优化建议
        suggestions.add("\n=== 综合优化建议 ===");
        if (score >= 70) {
            suggestions.add("⚠ 高匹配度（≥70分）：文本高度疑似Perplexity生成");
            suggestions.add("优化重点：");
            suggestions.add("1. 减少直接引用，增加原创性解释");
            suggestions.add("2. 弱化问答结构，采用连贯叙述");
            suggestions.add("3. 适度减少数据密度，增强论证深度");
            suggestions.add("4. 减少来源标注频率");
            suggestions.add("5. 降低时效性词汇使用");
        } else if (score >= 50) {
            suggestions.add("⚠ 较高匹配度（50-70分）：文本具有明显Perplexity特征");
            suggestions.add("优化建议：");
            suggestions.add("1. 整合引用内容，使表达更自然");
            suggestions.add("2. 平衡信息呈现与深度分析");
            suggestions.add("3. 增加个人观点和批判性思考");
        } else {
            suggestions.add("✓ 中等匹配度（<50分）：Perplexity特征不明显");
            suggestions.add("文本具有较好的原创性和独立性");
        }
        
        return suggestions;
    }
}
