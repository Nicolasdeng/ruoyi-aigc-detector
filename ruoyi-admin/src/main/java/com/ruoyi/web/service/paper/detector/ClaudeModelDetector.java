package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Claude AI模型检测器
 * 
 * Claude特征：
 * 1. 思考过程：经常展示思考和推理过程
 * 2. 谨慎表达：使用大量限定词和保留意见的表达
 * 3. 结构化思维：清晰的逻辑层次和分点论述
 * 4. 人性化语气：友好、谦逊的交流风格
 * 5. 细节关注：注重细节和准确性
 * 
 * 检测维度（总分100）：
 * - 思考过程标记：30分
 * - 限定词使用：25分
 * - 结构化表达：20分
 * - 谦逊语气：15分
 * - 细节密度：10分
 *
 * @author ruoyi
 */
@Component
public class ClaudeModelDetector implements IAiModelDetector {

    @Override
    public String getModelName() {
        return "Claude";
    }

    @Override
    public String getDetectorName() {
        return "ClaudeModelDetector";
    }

    @Override
    public BigDecimal detectModel(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 基础特征得分（40%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（60%权重）
        double paperScore = calculatePaperScore(text);
        
        // 综合得分
        double finalScore = baseScore * 0.4 + paperScore * 0.6;
        
        return BigDecimal.valueOf(Math.min(100.0, finalScore)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算基础特征得分（原有检测逻辑）
     */
    private double calculateBaseScore(String text) {
        double totalScore = 0.0;
        
        // 1. 思考过程标记检测（30分）
        totalScore += detectThinkingProcess(text);
        
        // 2. 限定词使用检测（25分）
        totalScore += detectQualifiers(text);
        
        // 3. 结构化表达检测（20分）
        totalScore += detectStructuredExpression(text);
        
        // 4. 谦逊语气检测（15分）
        totalScore += detectHumbleTone(text);
        
        // 5. 细节密度检测（10分）
        totalScore += detectDetailDensity(text);
        
        return totalScore;
    }
    
    /**
     * 计算论文专属特征得分
     * Claude特点：推理能力强、逻辑严密、写作质量高、创新性强、语言连贯性极高
     * 
     * 权重配置：
     * - 语言连贯性 24%（Claude语言流畅度极高，75-92分）
     * - 创新性 22%（推理和批判性思维强，70-88分）
     * - 论证结构 20%（逻辑严密，结构清晰，72-90分）
     * - 写作风格 18%（写作质量高，表达优雅，70-88分）
     * - 学术规范性 8%（规范但不刻板，60-78分）
     * - 知识深度 5%（深度适中，58-75分）
     * - 参考文献 2%（引用适中，50-68分）
     * - 数据实证 1%（重逻辑轻数据，45-62分）
     */
    private double calculatePaperScore(String text) {
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 语言连贯性（24%）- Claude的语言连贯性极高
        score += adjustScoreRange(features.get("languageCoherence"), 75, 92) * 0.24;
        
        // 创新性（22%）- 推理和批判性思维强
        score += adjustScoreRange(features.get("innovation"), 70, 88) * 0.22;
        
        // 论证结构（20%）- 逻辑严密，结构清晰
        score += adjustScoreRange(features.get("argumentationStructure"), 72, 90) * 0.20;
        
        // 写作风格（18%）- 写作质量高，表达优雅
        score += adjustScoreRange(features.get("writingStyle"), 70, 88) * 0.18;
        
        // 学术规范性（8%）- 规范但不刻板
        score += adjustScoreRange(features.get("academicFormality"), 60, 78) * 0.08;
        
        // 知识深度（5%）- 深度适中
        score += adjustScoreRange(features.get("knowledgeDepth"), 58, 75) * 0.05;
        
        // 参考文献（2%）- 引用适中
        score += adjustScoreRange(features.get("referencePattern"), 50, 68) * 0.02;
        
        // 数据实证（1%）- 重逻辑轻数据
        score += adjustScoreRange(features.get("empiricalEvidence"), 45, 62) * 0.01;
        
        return score;
    }
    
    /**
     * 将0-100的得分映射到目标区间
     */
    private double adjustScoreRange(double score, double min, double max) {
        return min + (score / 100.0) * (max - min);
    }

    /**
     * 检测思考过程标记
     * Claude经常展示思考和推理的过程
     */
    private double detectThinkingProcess(String text) {
        double score = 0;
        
        // 思考过程引导词
        String[] thinkingMarkers = {
            "让我", "我认为", "我觉得", "考虑到", "值得注意",
            "需要考虑", "换个角度", "进一步分析", "深入思考",
            "仔细想想", "回过头看", "权衡", "综合考虑"
        };
        
        int thinkingCount = 0;
        for (String marker : thinkingMarkers) {
            if (text.contains(marker)) {
                thinkingCount++;
            }
        }
        
        // 推理连接词
        String[] reasoningWords = {
            "因为", "所以", "由于", "导致", "造成", "基于此",
            "因此可见", "这意味着", "这表明", "可以推断"
        };
        
        int reasoningCount = 0;
        for (String word : reasoningWords) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                reasoningCount++;
            }
        }
        
        // 计算思考标记密度
        int textLength = text.length();
        double thinkingDensity = (thinkingCount + reasoningCount) * 1000.0 / textLength;
        
        // Claude的思考标记密度通常在4-10/千字
        if (thinkingDensity >= 4 && thinkingDensity <= 10) {
            score += 20;
        } else if (thinkingDensity >= 2 && thinkingDensity < 4) {
            score += 10;
        }
        
        // 如果思考过程标记丰富，额外加分
        if (thinkingCount >= 3) {
            score += 10;
        } else if (thinkingCount >= 1) {
            score += 5;
        }
        
        return Math.min(score, 30);
    }

    /**
     * 检测限定词使用
     * Claude倾向于使用谨慎的表达和限定词
     */
    private double detectQualifiers(String text) {
        double score = 0;
        
        // 限定词库
        String[] qualifiers = {
            "可能", "也许", "或许", "大概", "似乎", "看起来",
            "往往", "通常", "一般", "某种程度上", "在一定程度上",
            "相对", "比较", "较为", "有时", "可以说", "在某些情况下",
            "不一定", "未必", "不完全", "在很大程度上"
        };
        
        int qualifierCount = 0;
        for (String qualifier : qualifiers) {
            Pattern pattern = Pattern.compile(qualifier);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                qualifierCount++;
            }
        }
        
        // 保留意见的表达
        String[] reservationPhrases = {
            "需要注意的是", "值得指出", "应当说明", "必须承认",
            "公平地说", "客观来讲", "更准确地说"
        };
        
        int reservationCount = 0;
        for (String phrase : reservationPhrases) {
            if (text.contains(phrase)) {
                reservationCount++;
            }
        }
        
        // 计算限定词密度
        int textLength = text.length();
        double qualifierDensity = qualifierCount * 1000.0 / textLength;
        
        // Claude的限定词密度通常在6-15/千字
        if (qualifierDensity >= 6 && qualifierDensity <= 15) {
            score += 18;
        } else if (qualifierDensity >= 3 && qualifierDensity < 6) {
            score += 10;
        }
        
        // 如果有保留意见的表达，额外加分
        if (reservationCount >= 2) {
            score += 7;
        } else if (reservationCount >= 1) {
            score += 4;
        }
        
        return Math.min(score, 25);
    }

    /**
     * 检测结构化表达
     * Claude倾向于清晰的分点论述和层次结构
     */
    private double detectStructuredExpression(String text) {
        double score = 0;
        
        // 检测列举标记
        Pattern listPattern = Pattern.compile("(第[一二三四五]|1\\.|2\\.|3\\.|\\(1\\)|\\(2\\)|①|②|首先|其次|再次|最后)");
        Matcher listMatcher = listPattern.matcher(text);
        int listMarkerCount = 0;
        while (listMatcher.find()) {
            listMarkerCount++;
        }
        
        // 检测层次标记
        String[] hierarchyMarkers = {
            "总的来说", "具体而言", "更进一步", "深入来看",
            "从宏观角度", "从微观层面", "整体上", "细节上"
        };
        
        int hierarchyCount = 0;
        for (String marker : hierarchyMarkers) {
            if (text.contains(marker)) {
                hierarchyCount++;
            }
        }
        
        // 检测段落结构
        String[] paragraphs = text.split("\n\n|\n");
        int paragraphCount = paragraphs.length;
        
        // 评分：列举标记
        if (listMarkerCount >= 3) {
            score += 10;
        } else if (listMarkerCount >= 1) {
            score += 5;
        }
        
        // 评分：层次标记
        if (hierarchyCount >= 2) {
            score += 6;
        } else if (hierarchyCount >= 1) {
            score += 3;
        }
        
        // 评分：段落结构（3-8段为佳）
        if (paragraphCount >= 3 && paragraphCount <= 8) {
            score += 4;
        }
        
        return Math.min(score, 20);
    }

    /**
     * 检测谦逊语气
     * Claude倾向于友好、谦逊的表达方式
     */
    private double detectHumbleTone(String text) {
        double score = 0;
        
        // 谦逊表达
        String[] humblePhrases = {
            "我试图", "我尝试", "我希望", "我建议", "在我看来",
            "恕我直言", "如果可以的话", "不妨", "或许可以",
            "我理解", "我明白", "感谢", "请允许我"
        };
        
        int humbleCount = 0;
        for (String phrase : humblePhrases) {
            if (text.contains(phrase)) {
                humbleCount++;
            }
        }
        
        // 友好表达
        String[] friendlyPhrases = {
            "很高兴", "乐意", "愿意", "帮助", "分享",
            "希望这", "希望能", "祝", "期待"
        };
        
        int friendlyCount = 0;
        for (String phrase : friendlyPhrases) {
            if (text.contains(phrase)) {
                friendlyCount++;
            }
        }
        
        // 避免绝对化的表达
        String[] absoluteWords = {"必须", "一定", "绝对", "肯定", "毫无疑问"};
        int absoluteCount = 0;
        for (String word : absoluteWords) {
            if (text.contains(word)) {
                absoluteCount++;
            }
        }
        
        // 评分
        if (humbleCount >= 2) {
            score += 8;
        } else if (humbleCount >= 1) {
            score += 4;
        }
        
        if (friendlyCount >= 1) {
            score += 4;
        }
        
        // 如果避免了绝对化表达，加分
        if (absoluteCount == 0) {
            score += 3;
        }
        
        return Math.min(score, 15);
    }

    /**
     * 检测细节密度
     * Claude注重细节和准确性
     */
    private double detectDetailDensity(String text) {
        double score = 0;
        
        // 检测括号补充说明
        int parenthesesCount = text.split("\\(").length - 1;
        parenthesesCount += text.split("（").length - 1;
        
        // 检测引号强调
        int quotesCount = text.split("\"").length - 1;
        quotesCount += text.split("“").length - 1;
        
        // 检测具体数字
        Pattern numberPattern = Pattern.compile("\\d+");
        Matcher numberMatcher = numberPattern.matcher(text);
        int numberCount = 0;
        while (numberMatcher.find()) {
            numberCount++;
        }
        
        // 细节补充词
        String[] detailWords = {"即", "也就是说", "换句话说", "具体来说", "例如", "比如"};
        int detailWordCount = 0;
        for (String word : detailWords) {
            if (text.contains(word)) {
                detailWordCount++;
            }
        }
        
        // 评分
        if (parenthesesCount >= 2) {
            score += 3;
        }
        
        if (quotesCount >= 2) {
            score += 2;
        }
        
        if (numberCount >= 3) {
            score += 3;
        }
        
        if (detailWordCount >= 2) {
            score += 2;
        }
        
        return Math.min(score, 10);
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> details = new HashMap<>();
        
        // Claude基础特征
        int thinkingCount = 0;
        String[] thinkingMarkers = {"让我", "我认为", "考虑到", "值得注意"};
        for (String marker : thinkingMarkers) {
            if (text.contains(marker)) {
                thinkingCount++;
            }
        }
        details.put("思考标记", thinkingCount + "处");
        
        String[] qualifiers = {"可能", "也许", "或许", "似乎", "往往", "通常"};
        int qualifierCount = 0;
        for (String qualifier : qualifiers) {
            Pattern pattern = Pattern.compile(qualifier);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                qualifierCount++;
            }
        }
        details.put("限定词", qualifierCount + "个");
        
        Pattern listPattern = Pattern.compile("(首先|其次|再次|最后)");
        Matcher listMatcher = listPattern.matcher(text);
        int listCount = 0;
        while (listMatcher.find()) {
            listCount++;
        }
        details.put("分点标记", listCount + "处");
        
        String[] humblePhrases = {"我试图", "我建议", "在我看来", "恕我直言"};
        int humbleCount = 0;
        for (String phrase : humblePhrases) {
            if (text.contains(phrase)) {
                humbleCount++;
            }
        }
        details.put("谦逊表达", humbleCount + "处");
        
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
        // 委托给单参数方法
        return generateSuggestions(content);
    }

    public List<String> generateSuggestions(String text, double score) {
        // 将double转换为BigDecimal后委托给已有方法
        return generateSuggestions(text, BigDecimal.valueOf(score));
    }

    @Override
    public List<String> generateSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();
        
        double finalScore = detectModel(text).doubleValue();
        double baseScore = calculateBaseScore(text);
        double paperScore = calculatePaperScore(text);
        
        suggestions.add("=== Claude基础特征分析 ===");
        
        // 基础特征分析
        int thinkingCount = 0;
        String[] thinkingMarkers = {"让我", "我认为", "考虑到", "值得注意"};
        for (String marker : thinkingMarkers) {
            if (text.contains(marker)) {
                thinkingCount++;
            }
        }
        
        if (thinkingCount >= 3) {
            suggestions.add("✓ 思考过程标记丰富（" + thinkingCount + "处），符合Claude思考展示特征");
        } else if (thinkingCount > 0) {
            suggestions.add("○ 有思考标记（" + thinkingCount + "处），但不够明显");
        } else {
            suggestions.add("✗ 缺少思考过程标记");
        }
        
        String[] qualifiers = {"可能", "也许", "或许", "似乎", "往往", "通常"};
        int qualifierCount = 0;
        for (String qualifier : qualifiers) {
            Pattern pattern = Pattern.compile(qualifier);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                qualifierCount++;
            }
        }
        
        double qualifierDensity = qualifierCount * 1000.0 / text.length();
        if (qualifierDensity >= 6 && qualifierDensity <= 15) {
            suggestions.add("✓ 限定词密度适中（" + String.format("%.1f", qualifierDensity) + "/千字），符合Claude谨慎表达风格");
        } else if (qualifierDensity > 0) {
            suggestions.add("○ 限定词密度偏离Claude典型范围（" + String.format("%.1f", qualifierDensity) + "/千字）");
        }
        
        Pattern listPattern = Pattern.compile("(首先|其次|再次|最后)");
        Matcher listMatcher = listPattern.matcher(text);
        int listCount = 0;
        while (listMatcher.find()) {
            listCount++;
        }
        
        if (listCount >= 3) {
            suggestions.add("✓ 结构化标记清晰（" + listCount + "处）");
        } else if (listCount > 0) {
            suggestions.add("○ 有结构化标记（" + listCount + "处）");
        }
        
        String[] humblePhrases = {"我试图", "我建议", "在我看来", "恕我直言"};
        int humbleCount = 0;
        for (String phrase : humblePhrases) {
            if (text.contains(phrase)) {
                humbleCount++;
            }
        }
        
        if (humbleCount >= 2) {
            suggestions.add("✓ 谦逊语气明显（" + humbleCount + "处）");
        } else if (humbleCount > 0) {
            suggestions.add("○ 有谦逊表达（" + humbleCount + "处）");
        }
        
        suggestions.add("\n=== 论文专属特征分析 ===");
        
        // 论文专属特征分析
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double languageCoherence = features.get("languageCoherence");
        suggestions.add(String.format("语言连贯性: %.1f分 - %s", 
            languageCoherence,
            languageCoherence >= 75 ? "Claude语言流畅度极高的显著特征" :
            languageCoherence >= 60 ? "连贯性较好" : "连贯性需提升"));
        
        double innovation = features.get("innovation");
        suggestions.add(String.format("创新性: %.1f分 - %s", 
            innovation,
            innovation >= 70 ? "推理和批判性思维强，符合Claude特点" :
            innovation >= 55 ? "有一定创新性" : "创新性较弱"));
        
        double argumentation = features.get("argumentationStructure");
        suggestions.add(String.format("论证结构: %.1f分 - %s", 
            argumentation,
            argumentation >= 72 ? "逻辑严密，结构清晰，Claude典型特征" :
            argumentation >= 58 ? "结构较好" : "结构需优化"));
        
        double writingStyle = features.get("writingStyle");
        suggestions.add(String.format("写作风格: %.1f分 - %s", 
            writingStyle,
            writingStyle >= 70 ? "写作质量高，表达优雅" :
            writingStyle >= 55 ? "写作风格良好" : "写作需改进"));
        
        double academicFormality = features.get("academicFormality");
        suggestions.add(String.format("学术规范性: %.1f分 - %s", 
            academicFormality,
            academicFormality >= 60 ? "规范但不刻板，适度平衡" :
            academicFormality >= 45 ? "规范性一般" : "规范性不足"));
        
        double knowledgeDepth = features.get("knowledgeDepth");
        suggestions.add(String.format("知识深度: %.1f分 - %s", 
            knowledgeDepth,
            knowledgeDepth >= 58 ? "深度适中" : "深度有待提升"));
        
        double referencePattern = features.get("referencePattern");
        suggestions.add(String.format("参考文献: %.1f分 - %s", 
            referencePattern,
            referencePattern >= 50 ? "引用适中" : "引用较少"));
        
        double empiricalEvidence = features.get("empiricalEvidence");
        suggestions.add(String.format("数据实证: %.1f分 - %s", 
            empiricalEvidence,
            empiricalEvidence >= 45 ? "重逻辑轻数据，Claude典型特点" : "数据支撑不足"));
        
        suggestions.add("\n=== 综合优化建议 ===");
        
        // 综合建议
        if (finalScore >= 70) {
            suggestions.add("【高匹配度】文本具有明显的Claude特征");
            suggestions.add("• 语言流畅度和连贯性极高");
            suggestions.add("• 推理能力和批判性思维强");
            suggestions.add("• 逻辑严密，结构清晰");
            suggestions.add("• 建议：适当调整思考过程展示和限定词使用，使表达更自然");
        } else if (finalScore >= 50) {
            suggestions.add("【较高匹配度】文本部分符合Claude特征");
            suggestions.add("• 提升语言连贯性和写作质量");
            suggestions.add("• 加强逻辑推理和论证结构");
            suggestions.add("• 适当增加批判性思维元素");
        } else {
            suggestions.add("【中等匹配度】Claude特征不够明显");
            suggestions.add("• 重点提升语言流畅度和连贯性");
            suggestions.add("• 强化逻辑推理能力");
            suggestions.add("• 优化写作风格和表达质量");
        }
        
        return suggestions;
    }
}
