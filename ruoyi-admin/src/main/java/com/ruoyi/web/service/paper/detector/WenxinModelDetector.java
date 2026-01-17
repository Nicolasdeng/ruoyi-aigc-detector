package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文心一言AI模型检测器
 * 
 * 特征分析：
 * 1. 中文地道性(30分)：地道的中文表达和语言习惯
 * 2. 文化引用(25分)：中国文化元素和典故引用
 * 3. 情感丰富(20分)：情感表达较为丰富
 * 4. 口语化倾向(15分)：相对口语化的表达
 * 5. 接地气(10分)：贴近生活的表达方式
 * 
 * @author ruoyi
 */
@Component
public class WenxinModelDetector implements IAiModelDetector {

    // 地道中文表达词汇
    private static final Set<String> IDIOMATIC_CHINESE = new HashSet<>(Arrays.asList(
        "不得不说", "毫无疑问", "众所周知", "显而易见", "毋庸置疑",
        "不容忽视", "不言而喻", "有目共睹", "无可厚非", "理所当然",
        "显然", "无疑", "确实", "的确", "当然"
    ));

    // 文化典故引用词
    private static final Set<String> CULTURAL_REFERENCES = new HashSet<>(Arrays.asList(
        "孔子", "老子", "庄子", "孟子", "论语", "道德经",
        "诗经", "楚辞", "唐诗", "宋词", "成语", "典故",
        "传统文化", "中华文化", "华夏", "文明古国", "五千年",
        "儒家", "道家", "佛家", "禅意", "古人云"
    ));

    // 情感表达词汇
    private static final Set<String> EMOTIONAL_WORDS = new HashSet<>(Arrays.asList(
        "感动", "震撼", "惊喜", "欣慰", "欣喜", "喜悦", "激动",
        "温馨", "温暖", "感慨", "感叹", "感悟", "体会", "心得",
        "深刻", "难忘", "珍贵", "宝贵", "美好", "幸福"
    ));

    // 口语化表达
    private static final Set<String> COLLOQUIAL_EXPRESSIONS = new HashSet<>(Arrays.asList(
        "说实话", "老实说", "坦白说", "不瞒你说", "讲真的",
        "其实", "实际上", "事实上", "说白了", "简单来说",
        "换句话说", "打个比方", "举个例子", "比如说", "就好比",
        "怎么说呢", "可以说", "不妨", "何不", "不如"
    ));

    // 生活化表达
    private static final Set<String> LIFE_RELATED = new HashSet<>(Arrays.asList(
        "日常生活", "生活中", "平时", "平常", "日子",
        "家庭", "工作", "学习", "朋友", "亲人",
        "吃饭", "睡觉", "上班", "下班", "周末",
        "早上", "晚上", "中午", "今天", "昨天", "明天"
    ));

    // 成语和四字词语模式
    private static final Pattern IDIOM_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]{4}");

    // 设问句模式
    private static final Pattern RHETORICAL_QUESTION = Pattern.compile("(.*?)(是不是|难道|岂不是|何尝不是|又何尝|莫非|怎能|如何|怎样|怎么)(.+?)[？?]");

    @Override
    public String getModelName() {
        return "文心一言";
    }

    @Override
    public String getDetectorName() {
        return "文心一言模型检测器";
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
            result.put("score", 0);
            result.put("confidence", 0.0);
            result.put("details", Collections.emptyMap());
            return result;
        }

        // 清理文本
        String cleanText = text.trim();
        int textLength = cleanText.length();

        // 基础特征得分（40%权重）
        double baseScore = calculateBaseScore(cleanText, textLength);
        
        // 论文专属特征得分（60%权重）
        double paperScore = calculatePaperScore(cleanText);
        
        // 综合得分
        double totalScore = baseScore * 0.4 + paperScore * 0.6;

        // 特征详情
        Map<String, Object> details = new HashMap<>();
        details.put("idiomatic_chinese", String.format("%.1f分", detectIdiomaticChinese(cleanText, textLength)));
        details.put("cultural_references", String.format("%.1f分", detectCulturalReferences(cleanText, textLength)));
        details.put("emotional_richness", String.format("%.1f分", detectEmotionalRichness(cleanText, textLength)));
        details.put("colloquial_style", String.format("%.1f分", detectColloquialStyle(cleanText, textLength)));
        details.put("life_related", String.format("%.1f分", detectLifeRelated(cleanText, textLength)));
        details.put("base_score", String.format("%.1f分", baseScore));
        details.put("paper_score", String.format("%.1f分", paperScore));

        result.put("score", Math.min(100.0, totalScore));
        result.put("details", details);
        
        return result;
    }

    /**
     * 计算基础特征得分（文心一言特有特征）
     */
    private double calculateBaseScore(String text, int textLength) {
        // 1. 中文地道性检测(30分)
        double idiomaticScore = detectIdiomaticChinese(text, textLength);

        // 2. 文化引用检测(25分)
        double culturalScore = detectCulturalReferences(text, textLength);

        // 3. 情感丰富度检测(20分)
        double emotionalScore = detectEmotionalRichness(text, textLength);

        // 4. 口语化倾向检测(15分)
        double colloquialScore = detectColloquialStyle(text, textLength);

        // 5. 接地气检测(10分)
        double lifeRelatedScore = detectLifeRelated(text, textLength);

        // 计算总分
        return idiomaticScore + culturalScore + emotionalScore + 
               colloquialScore + lifeRelatedScore;
    }

    /**
     * 计算论文专属特征得分
     * 文心一言特点：中文理解能力强、知识广度优秀、语言流畅自然、学术规范性好
     * 
     * 权重分配：
     * - 语言连贯性 24%（75-92分）- 语言流畅自然是文心一言显著特征
     * - 学术规范性 22%（70-88分）- 学术规范性好
     * - 知识深度 20%（68-85分）- 知识广度优秀
     * - 写作风格 18%（65-82分）- 写作自然流畅
     * - 论证结构 8%（55-72分）- 结构合理但不突出
     * - 参考文献 4%（50-68分）- 引用适中
     * - 创新性 2%（45-62分）- 创新性一般
     * - 数据实证 2%（40-58分）- 数据能力一般
     */
    private double calculatePaperScore(String text) {
        // 获取所有论文特征
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 语言连贯性（24%，75-92分是文心一言显著特征）
        score += adjustScoreRange(features.get("languageCoherence"), 75, 92) * 0.24;
        
        // 学术规范性（22%，70-88分）
        score += adjustScoreRange(features.get("academicFormality"), 70, 88) * 0.22;
        
        // 知识深度（20%，68-85分）
        score += adjustScoreRange(features.get("knowledgeDepth"), 68, 85) * 0.20;
        
        // 写作风格（18%，65-82分）
        score += adjustScoreRange(features.get("writingStyle"), 65, 82) * 0.18;
        
        // 论证结构（8%，55-72分）
        score += adjustScoreRange(features.get("argumentationStructure"), 55, 72) * 0.08;
        
        // 参考文献（4%，50-68分）
        score += adjustScoreRange(features.get("referencePattern"), 50, 68) * 0.04;
        
        // 创新性（2%，45-62分）
        score += adjustScoreRange(features.get("innovation"), 45, 62) * 0.02;
        
        // 数据实证（2%，40-58分）
        score += adjustScoreRange(features.get("empiricalEvidence"), 40, 58) * 0.02;
        
        return score * 100; // 转换为百分制
    }

    /**
     * 将0-100的得分映射到目标区间
     */
    private double adjustScoreRange(double score, double minTarget, double maxTarget) {
        if (score < 0) score = 0;
        if (score > 100) score = 100;
        
        // 将0-100映射到minTarget-maxTarget
        return minTarget + (score / 100.0) * (maxTarget - minTarget);
    }

    /**
     * 检测中文地道性
     */
    private double detectIdiomaticChinese(String text, int textLength) {
        double score = 0.0;
        
        // 统计地道中文表达
        int idiomaticCount = 0;
        for (String phrase : IDIOMATIC_CHINESE) {
            idiomaticCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double idiomaticDensity = (idiomaticCount * 1000.0) / textLength;
        
        // 成语和四字词语统计
        int idiomCount = 0;
        Matcher matcher = IDIOM_PATTERN.matcher(text);
        while (matcher.find()) {
            String fourChar = matcher.group();
            // 简单判断：如果是常见四字组合，计入成语
            if (isLikelyIdiom(fourChar)) {
                idiomCount++;
            }
        }
        double idiomDensity = (idiomCount * 1000.0) / textLength;
        
        // 地道表达密度：3-10/千字较好
        if (idiomaticDensity >= 3 && idiomaticDensity <= 10) {
            score += 15 * (idiomaticDensity / 10.0);
        } else if (idiomaticDensity > 10) {
            score += 15 * 0.8;
        } else {
            score += 15 * (idiomaticDensity / 3.0);
        }
        
        // 成语密度：5-15/千字较好
        if (idiomDensity >= 5 && idiomDensity <= 15) {
            score += 15 * (idiomDensity / 15.0);
        } else if (idiomDensity > 15) {
            score += 15 * 0.8;
        } else {
            score += 15 * (idiomDensity / 5.0);
        }
        
        return Math.min(score, 30.0);
    }

    /**
     * 检测文化引用
     */
    private double detectCulturalReferences(String text, int textLength) {
        double score = 0.0;
        
        // 统计文化典故引用
        int culturalCount = 0;
        for (String phrase : CULTURAL_REFERENCES) {
            culturalCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double culturalDensity = (culturalCount * 1000.0) / textLength;
        
        // 文化引用密度：2-8/千字较好
        if (culturalDensity >= 2 && culturalDensity <= 8) {
            score = 25 * (culturalDensity / 8.0);
        } else if (culturalDensity > 8) {
            score = 25 * 0.8;
        } else {
            score = 25 * (culturalDensity / 2.0);
        }
        
        return Math.min(score, 25.0);
    }

    /**
     * 检测情感丰富度
     */
    private double detectEmotionalRichness(String text, int textLength) {
        double score = 0.0;
        
        // 统计情感词汇
        int emotionalCount = 0;
        for (String word : EMOTIONAL_WORDS) {
            emotionalCount += countOccurrences(text, word);
        }
        
        // 计算密度（每千字）
        double emotionalDensity = (emotionalCount * 1000.0) / textLength;
        
        // 感叹句统计
        int exclamationCount = countOccurrences(text, "！") + countOccurrences(text, "!");
        double exclamationDensity = (exclamationCount * 1000.0) / textLength;
        
        // 设问句统计
        Matcher matcher = RHETORICAL_QUESTION.matcher(text);
        int rhetoricalCount = 0;
        while (matcher.find()) {
            rhetoricalCount++;
        }
        double rhetoricalDensity = (rhetoricalCount * 1000.0) / textLength;
        
        // 情感词密度：5-15/千字较好
        if (emotionalDensity >= 5 && emotionalDensity <= 15) {
            score += 10 * (emotionalDensity / 15.0);
        } else if (emotionalDensity > 15) {
            score += 10 * 0.8;
        } else {
            score += 10 * (emotionalDensity / 5.0);
        }
        
        // 感叹句密度：2-6/千字较好
        if (exclamationDensity >= 2 && exclamationDensity <= 6) {
            score += 5 * (exclamationDensity / 6.0);
        } else if (exclamationDensity > 6) {
            score += 5 * 0.8;
        }
        
        // 设问句密度：1-4/千字较好
        if (rhetoricalDensity >= 1 && rhetoricalDensity <= 4) {
            score += 5 * (rhetoricalDensity / 4.0);
        } else if (rhetoricalDensity > 4) {
            score += 5 * 0.8;
        }
        
        return Math.min(score, 20.0);
    }

    /**
     * 检测口语化倾向
     */
    private double detectColloquialStyle(String text, int textLength) {
        double score = 0.0;
        
        // 统计口语化表达
        int colloquialCount = 0;
        for (String phrase : COLLOQUIAL_EXPRESSIONS) {
            colloquialCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double colloquialDensity = (colloquialCount * 1000.0) / textLength;
        
        // 口语化密度：4-12/千字较好
        if (colloquialDensity >= 4 && colloquialDensity <= 12) {
            score = 15 * (colloquialDensity / 12.0);
        } else if (colloquialDensity > 12) {
            score = 15 * 0.9;
        } else {
            score = 15 * (colloquialDensity / 4.0);
        }
        
        return Math.min(score, 15.0);
    }

    /**
     * 检测接地气程度
     */
    private double detectLifeRelated(String text, int textLength) {
        double score = 0.0;
        
        // 统计生活化表达
        int lifeCount = 0;
        for (String phrase : LIFE_RELATED) {
            lifeCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double lifeDensity = (lifeCount * 1000.0) / textLength;
        
        // 生活化密度：3-10/千字较好
        if (lifeDensity >= 3 && lifeDensity <= 10) {
            score = 10 * (lifeDensity / 10.0);
        } else if (lifeDensity > 10) {
            score = 10 * 0.9;
        } else {
            score = 10 * (lifeDensity / 3.0);
        }
        
        return Math.min(score, 10.0);
    }

    /**
     * 判断是否可能是成语
     */
    private boolean isLikelyIdiom(String fourChar) {
        // 简单启发式判断：包含常见成语字
        String[] commonIdiomChars = {"不", "无", "有", "而", "之", "其", "可", "以", "为"};
        int count = 0;
        for (String ch : commonIdiomChars) {
            if (fourChar.contains(ch)) {
                count++;
            }
        }
        return count >= 1;
    }

    /**
     * 统计子串出现次数
     */
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> features = new LinkedHashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return features;
        }

        String cleanText = text.trim();
        int textLength = cleanText.length();

        // === 文心一言基础特征 ===
        // 地道中文表达统计
        int idiomaticCount = 0;
        for (String phrase : IDIOMATIC_CHINESE) {
            idiomaticCount += countOccurrences(cleanText, phrase);
        }
        features.put("地道中文表达", idiomaticCount + "次");

        // 文化典故引用
        int culturalCount = 0;
        for (String phrase : CULTURAL_REFERENCES) {
            culturalCount += countOccurrences(cleanText, phrase);
        }
        features.put("文化典故引用", culturalCount + "次");

        // 情感词汇
        int emotionalCount = 0;
        for (String word : EMOTIONAL_WORDS) {
            emotionalCount += countOccurrences(cleanText, word);
        }
        features.put("情感词汇", emotionalCount + "次");

        // 口语化表达
        int colloquialCount = 0;
        for (String phrase : COLLOQUIAL_EXPRESSIONS) {
            colloquialCount += countOccurrences(cleanText, phrase);
        }
        features.put("口语化表达", colloquialCount + "次");

        // 生活化表达
        int lifeCount = 0;
        for (String phrase : LIFE_RELATED) {
            lifeCount += countOccurrences(cleanText, phrase);
        }
        features.put("生活化表达", lifeCount + "次");

        // 成语统计
        Matcher matcher = IDIOM_PATTERN.matcher(cleanText);
        int idiomCount = 0;
        while (matcher.find()) {
            String fourChar = matcher.group();
            if (isLikelyIdiom(fourChar)) {
                idiomCount++;
            }
        }
        features.put("成语使用", idiomCount + "次");

        // 感叹句
        int exclamationCount = countOccurrences(cleanText, "！") + countOccurrences(cleanText, "!");
        features.put("感叹句", exclamationCount + "个");

        // === 论文专属特征得分 ===
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(cleanText);
        features.put("学术规范性", String.format("%.1f分", paperFeatures.get("academicFormality")));
        features.put("论证结构", String.format("%.1f分", paperFeatures.get("argumentationStructure")));
        features.put("知识深度", String.format("%.1f分", paperFeatures.get("knowledgeDepth")));
        features.put("写作风格", String.format("%.1f分", paperFeatures.get("writingStyle")));
        features.put("参考文献", String.format("%.1f分", paperFeatures.get("referencePattern")));
        features.put("创新性", String.format("%.1f分", paperFeatures.get("innovation")));
        features.put("语言连贯性", String.format("%.1f分", paperFeatures.get("languageCoherence")));
        features.put("数据实证", String.format("%.1f分", paperFeatures.get("empiricalEvidence")));

        return features;
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

        // === 第一部分：文心一言基础特征分析 ===
        suggestions.add("=== 文心一言基础特征分析 ===");
        
        String cleanText = text.trim();
        int textLength = cleanText.length();
        
        // 地道中文表达分析
        int idiomaticCount = 0;
        for (String phrase : IDIOMATIC_CHINESE) {
            idiomaticCount += countOccurrences(cleanText, phrase);
        }
        double idiomaticDensity = (idiomaticCount * 1000.0) / textLength;
        suggestions.add(String.format("- 地道中文表达：检测到%d次，密度%.1f/千字（文心一言典型范围：3-10/千字）", 
            idiomaticCount, idiomaticDensity));

        // 文化典故引用分析
        int culturalCount = 0;
        for (String phrase : CULTURAL_REFERENCES) {
            culturalCount += countOccurrences(cleanText, phrase);
        }
        double culturalDensity = (culturalCount * 1000.0) / textLength;
        suggestions.add(String.format("- 文化典故引用：检测到%d次，密度%.1f/千字（文心一言典型范围：2-8/千字）", 
            culturalCount, culturalDensity));

        // 情感词汇分析
        int emotionalCount = 0;
        for (String word : EMOTIONAL_WORDS) {
            emotionalCount += countOccurrences(cleanText, word);
        }
        double emotionalDensity = (emotionalCount * 1000.0) / textLength;
        suggestions.add(String.format("- 情感词汇：检测到%d次，密度%.1f/千字（文心一言典型范围：5-15/千字）", 
            emotionalCount, emotionalDensity));

        // 口语化表达分析
        int colloquialCount = 0;
        for (String phrase : COLLOQUIAL_EXPRESSIONS) {
            colloquialCount += countOccurrences(cleanText, phrase);
        }
        double colloquialDensity = (colloquialCount * 1000.0) / textLength;
        suggestions.add(String.format("- 口语化表达：检测到%d次，密度%.1f/千字（文心一言典型范围：4-12/千字）", 
            colloquialCount, colloquialDensity));

        // === 第二部分：论文专属特征分析 ===
        suggestions.add("\n=== 论文专属特征分析 ===");
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(cleanText);
        
        suggestions.add(String.format("- 语言连贯性：%.1f分（文心一言典型：75-92分）- 权重24%%", 
            paperFeatures.get("languageCoherence")));
        suggestions.add(String.format("- 学术规范性：%.1f分（文心一言典型：70-88分）- 权重22%%", 
            paperFeatures.get("academicFormality")));
        suggestions.add(String.format("- 知识深度：%.1f分（文心一言典型：68-85分）- 权重20%%", 
            paperFeatures.get("knowledgeDepth")));
        suggestions.add(String.format("- 写作风格：%.1f分（文心一言典型：65-82分）- 权重18%%", 
            paperFeatures.get("writingStyle")));
        suggestions.add(String.format("- 论证结构：%.1f分（文心一言典型：55-72分）- 权重8%%", 
            paperFeatures.get("argumentationStructure")));
        suggestions.add(String.format("- 参考文献：%.1f分（文心一言典型：50-68分）- 权重4%%", 
            paperFeatures.get("referencePattern")));
        suggestions.add(String.format("- 创新性：%.1f分（文心一言典型：45-62分）- 权重2%%", 
            paperFeatures.get("innovation")));
        suggestions.add(String.format("- 数据实证：%.1f分（文心一言典型：40-58分）- 权重2%%", 
            paperFeatures.get("empiricalEvidence")));

        // === 第三部分：综合优化建议 ===
        suggestions.add("\n=== 综合优化建议 ===");
        
        if (score >= 70) {
            suggestions.add("【高匹配度】文本具有明显的文心一言生成特征：");
            suggestions.add("1. 语言流畅自然，中文表达地道，建议保持但适当增加个性化");
            suggestions.add("2. 学术规范性良好，可适当增强学术深度和专业性");
            suggestions.add("3. 建议减少口语化表达，增强学术严谨性");
            suggestions.add("4. 可适当增加原创性观点和批判性思维");
            suggestions.add("5. 建议增加数据实证和实验支持，提升论证可信度");
        } else if (score >= 50) {
            suggestions.add("【较高匹配度】文本显示出一定的文心一言特征：");
            suggestions.add("1. 中文表达较为自然，建议检查文化引用的准确性");
            suggestions.add("2. 平衡情感表达与客观论述，避免过于主观");
            suggestions.add("3. 建议增强论证逻辑的严密性");
            suggestions.add("4. 可适当补充专业术语和学术规范表达");
            suggestions.add("5. 建议增加文献引用的多样性和权威性");
        } else {
            suggestions.add("【中等匹配度】文本特征不完全符合文心一言模式：");
            suggestions.add("1. 可能为人工撰写或其他AI模型生成");
            suggestions.add("2. 建议保持当前的写作风格");
            suggestions.add("3. 如需优化，可参考文心一言的语言流畅性特点");
            suggestions.add("4. 建议在保持学术性的同时增强可读性");
        }

        return suggestions;
    }
}
