package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 百度AI模型检测器
 * 
 * 特征分析：
 * 1. 简洁明了(30分)：表达简洁直接，重点突出
 * 2. 实用导向(25分)：注重实用性和可操作性
 * 3. 条理清晰(20分)：结构化和层次化的组织
 * 4. 关键词突出(15分)：重点内容明确标注
 * 5. 中规中矩(10分)：标准化的表达方式
 * 
 * @author ruoyi
 */
@Component
public class BaiduAiModelDetector implements IAiModelDetector {

    // 简洁表达词汇
    private static final Set<String> CONCISE_EXPRESSIONS = new HashSet<>(Arrays.asList(
        "简而言之", "总之", "总的来说", "总体而言", "归根结底",
        "概括来说", "简单说", "一句话", "要点是", "核心是",
        "关键在于", "重点是", "主要", "首要", "基本"
    ));

    // 实用性表达
    private static final Set<String> PRACTICAL_EXPRESSIONS = new HashSet<>(Arrays.asList(
        "具体操作", "实际应用", "操作方法", "实施步骤", "具体做法",
        "实践中", "应用场景", "实际情况", "实用技巧", "注意事项",
        "建议", "推荐", "可以", "应该", "需要", "必须"
    ));

    // 结构化标记
    private static final Set<String> STRUCTURAL_MARKERS = new HashSet<>(Arrays.asList(
        "首先", "其次", "再次", "最后", "第一", "第二", "第三",
        "一方面", "另一方面", "同时", "此外", "另外", "除此之外",
        "具体而言", "详细来说", "进一步", "深入分析"
    ));

    // 强调标记
    private static final Set<String> EMPHASIS_MARKERS = new HashSet<>(Arrays.asList(
        "值得注意", "特别是", "尤其是", "需要强调", "重要的是",
        "关键点", "核心要素", "必须", "务必", "千万",
        "极其", "非常", "十分", "相当", "格外"
    ));

    // 标准化表达
    private static final Set<String> STANDARD_EXPRESSIONS = new HashSet<>(Arrays.asList(
        "根据", "基于", "通过", "由于", "因此", "所以",
        "一般来说", "通常", "普遍", "常见", "传统",
        "标准", "规范", "常规", "正常", "合理"
    ));

    // 列表模式（数字或符号开头）
    private static final Pattern LIST_PATTERN = Pattern.compile("^[\\s]*[0-9①-⑩一二三四五六七八九十][\\.、）]");

    // 冒号后内容模式
    private static final Pattern COLON_PATTERN = Pattern.compile("[:：]\\s*(.+?)[\n。；]");

    @Override
    public String getModelName() {
        return "百度文心一言";
    }

    @Override
    public String getDetectorName() {
        return "百度AI模型检测器";
    }

    /**
     * 检测AI模型匹配度（返回Map，包含详细信息）
     */
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
        double totalScore = Math.min(100.0, baseScore * 0.4 + paperScore * 0.6);

        // 特征详情
        Map<String, Object> details = new HashMap<>();
        details.put("baseScore", String.format("%.1f", baseScore));
        details.put("paperScore", String.format("%.1f", paperScore));
        details.put("totalScore", String.format("%.1f", totalScore));

        result.put("score", totalScore);
        result.put("details", details);
        
        return result;
    }

    /**
     * 计算基础特征得分（原有5项特征）
     */
    private double calculateBaseScore(String text, int textLength) {
        // 1. 简洁明了检测(30分)
        double conciseScore = detectConciseness(text, textLength);

        // 2. 实用导向检测(25分)
        double practicalScore = detectPracticality(text, textLength);

        // 3. 条理清晰检测(20分)
        double structureScore = detectStructure(text, textLength);

        // 4. 关键词突出检测(15分)
        double emphasisScore = detectEmphasis(text, textLength);

        // 5. 中规中矩检测(10分)
        double standardScore = detectStandardization(text, textLength);

        return conciseScore + practicalScore + structureScore + 
               emphasisScore + standardScore;
    }

    /**
     * 计算论文专属特征得分
     * 百度AI特点：中文NLP能力强、知识图谱丰富、实体识别准确、语义理解深入
     * 
     * 权重配置：
     * - 知识深度23%（68-85分，知识图谱丰富）
     * - 学术规范性21%（70-87分，NLP能力强，规范性好）
     * - 语言连贯性20%（72-88分，语义理解深入）
     * - 数据实证15%（65-82分，实体识别准确）
     * - 论证结构12%（60-75分，逻辑较清晰）
     * - 写作风格6%（55-70分，风格中规中矩）
     * - 参考文献2%（50-65分，引用适中）
     * - 创新性1%（45-60分，创新性一般）
     */
    private double calculatePaperScore(String text) {
        // 使用PaperFeatureAnalyzer分析8大论文特征
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 知识深度23%（68-85分是百度AI显著特征）
        score += adjustScoreRange(features.get("knowledgeDepth"), 68, 85) * 0.23;
        
        // 学术规范性21%（70-87分）
        score += adjustScoreRange(features.get("academicFormality"), 70, 87) * 0.21;
        
        // 语言连贯性20%（72-88分）
        score += adjustScoreRange(features.get("languageCoherence"), 72, 88) * 0.20;
        
        // 数据实证15%（65-82分）
        score += adjustScoreRange(features.get("empiricalEvidence"), 65, 82) * 0.15;
        
        // 论证结构12%（60-75分）
        score += adjustScoreRange(features.get("argumentationStructure"), 60, 75) * 0.12;
        
        // 写作风格6%（55-70分）
        score += adjustScoreRange(features.get("writingStyle"), 55, 70) * 0.06;
        
        // 参考文献2%（50-65分）
        score += adjustScoreRange(features.get("referencePattern"), 50, 65) * 0.02;
        
        // 创新性1%（45-60分）
        score += adjustScoreRange(features.get("innovation"), 45, 60) * 0.01;
        
        return score * 100; // 转换为0-100分
    }

    /**
     * 将得分调整到目标区间
     * @param originalScore 原始得分(0-100)
     * @param minTarget 目标最小值
     * @param maxTarget 目标最大值
     * @return 调整后的得分(0-1)
     */
    private double adjustScoreRange(double originalScore, double minTarget, double maxTarget) {
        if (originalScore >= minTarget && originalScore <= maxTarget) {
            // 在目标区间内，得分较高
            double position = (originalScore - minTarget) / (maxTarget - minTarget);
            return 0.7 + position * 0.3; // 0.7-1.0
        } else if (originalScore < minTarget) {
            // 低于目标区间
            return originalScore / minTarget * 0.7; // 0-0.7
        } else {
            // 高于目标区间
            double excess = (originalScore - maxTarget) / (100 - maxTarget);
            return 1.0 - excess * 0.3; // 1.0-0.7
        }
    }

    /**
     * 检测简洁明了程度
     */
    private double detectConciseness(String text, int textLength) {
        double score = 0.0;
        
        // 统计简洁表达
        int conciseCount = 0;
        for (String phrase : CONCISE_EXPRESSIONS) {
            conciseCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double conciseDensity = (conciseCount * 1000.0) / textLength;
        
        // 句子平均长度
        String[] sentences = text.split("[。！？；]");
        int totalChars = 0;
        int validSentences = 0;
        for (String sentence : sentences) {
            String cleanSentence = sentence.trim();
            if (cleanSentence.length() > 0) {
                totalChars += cleanSentence.length();
                validSentences++;
            }
        }
        double avgSentenceLength = validSentences > 0 ? (double) totalChars / validSentences : 0;
        
        // 简洁表达密度：3-8/千字较好
        if (conciseDensity >= 3 && conciseDensity <= 8) {
            score += 15 * (conciseDensity / 8.0);
        } else if (conciseDensity > 8) {
            score += 15 * 0.8;
        } else {
            score += 15 * (conciseDensity / 3.0);
        }
        
        // 句子平均长度：15-35字较好（偏短）
        if (avgSentenceLength >= 15 && avgSentenceLength <= 35) {
            score += 15 * (1 - Math.abs(avgSentenceLength - 25) / 25.0);
        } else if (avgSentenceLength < 15) {
            score += 15 * 0.7;
        } else {
            score += 15 * (35.0 / avgSentenceLength);
        }
        
        return Math.min(score, 30.0);
    }

    /**
     * 检测实用导向
     */
    private double detectPracticality(String text, int textLength) {
        double score = 0.0;
        
        // 统计实用性表达
        int practicalCount = 0;
        for (String phrase : PRACTICAL_EXPRESSIONS) {
            practicalCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double practicalDensity = (practicalCount * 1000.0) / textLength;
        
        // 冒号后的内容（通常是具体说明）
        Matcher matcher = COLON_PATTERN.matcher(text);
        int colonCount = 0;
        while (matcher.find()) {
            colonCount++;
        }
        double colonDensity = (colonCount * 1000.0) / textLength;
        
        // 实用性表达密度：8-20/千字较好
        if (practicalDensity >= 8 && practicalDensity <= 20) {
            score += 15 * (practicalDensity / 20.0);
        } else if (practicalDensity > 20) {
            score += 15 * 0.9;
        } else {
            score += 15 * (practicalDensity / 8.0);
        }
        
        // 冒号使用密度：2-8/千字较好
        if (colonDensity >= 2 && colonDensity <= 8) {
            score += 10 * (colonDensity / 8.0);
        } else if (colonDensity > 8) {
            score += 10 * 0.8;
        } else {
            score += 10 * (colonDensity / 2.0);
        }
        
        return Math.min(score, 25.0);
    }

    /**
     * 检测条理清晰程度
     */
    private double detectStructure(String text, int textLength) {
        double score = 0.0;
        
        // 统计结构化标记
        int structureCount = 0;
        for (String marker : STRUCTURAL_MARKERS) {
            structureCount += countOccurrences(text, marker);
        }
        
        // 计算密度（每千字）
        double structureDensity = (structureCount * 1000.0) / textLength;
        
        // 统计列表项
        String[] lines = text.split("\n");
        int listCount = 0;
        for (String line : lines) {
            Matcher matcher = LIST_PATTERN.matcher(line);
            if (matcher.find()) {
                listCount++;
            }
        }
        double listDensity = (listCount * 1000.0) / textLength;
        
        // 结构化标记密度：5-15/千字较好
        if (structureDensity >= 5 && structureDensity <= 15) {
            score += 12 * (structureDensity / 15.0);
        } else if (structureDensity > 15) {
            score += 12 * 0.8;
        } else {
            score += 12 * (structureDensity / 5.0);
        }
        
        // 列表项密度：3-10/千字较好
        if (listDensity >= 3 && listDensity <= 10) {
            score += 8 * (listDensity / 10.0);
        } else if (listDensity > 10) {
            score += 8 * 0.8;
        } else {
            score += 8 * (listDensity / 3.0);
        }
        
        return Math.min(score, 20.0);
    }

    /**
     * 检测关键词突出程度
     */
    private double detectEmphasis(String text, int textLength) {
        double score = 0.0;
        
        // 统计强调标记
        int emphasisCount = 0;
        for (String marker : EMPHASIS_MARKERS) {
            emphasisCount += countOccurrences(text, marker);
        }
        
        // 计算密度（每千字）
        double emphasisDensity = (emphasisCount * 1000.0) / textLength;
        
        // 强调标记密度：4-12/千字较好
        if (emphasisDensity >= 4 && emphasisDensity <= 12) {
            score = 15 * (emphasisDensity / 12.0);
        } else if (emphasisDensity > 12) {
            score = 15 * 0.9;
        } else {
            score = 15 * (emphasisDensity / 4.0);
        }
        
        return Math.min(score, 15.0);
    }

    /**
     * 检测标准化程度
     */
    private double detectStandardization(String text, int textLength) {
        double score = 0.0;
        
        // 统计标准化表达
        int standardCount = 0;
        for (String phrase : STANDARD_EXPRESSIONS) {
            standardCount += countOccurrences(text, phrase);
        }
        
        // 计算密度（每千字）
        double standardDensity = (standardCount * 1000.0) / textLength;
        
        // 标准化表达密度：5-15/千字较好
        if (standardDensity >= 5 && standardDensity <= 15) {
            score = 10 * (standardDensity / 15.0);
        } else if (standardDensity > 15) {
            score = 10 * 0.9;
        } else {
            score = 10 * (standardDensity / 5.0);
        }
        
        return Math.min(score, 10.0);
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

        // 百度AI基础特征
        // 简洁表达统计
        int conciseCount = 0;
        for (String phrase : CONCISE_EXPRESSIONS) {
            conciseCount += countOccurrences(cleanText, phrase);
        }
        features.put("简洁表达", conciseCount + "次");

        // 实用性表达
        int practicalCount = 0;
        for (String phrase : PRACTICAL_EXPRESSIONS) {
            practicalCount += countOccurrences(cleanText, phrase);
        }
        features.put("实用性表达", practicalCount + "次");

        // 结构化标记
        int structureCount = 0;
        for (String marker : STRUCTURAL_MARKERS) {
            structureCount += countOccurrences(cleanText, marker);
        }
        features.put("结构化标记", structureCount + "次");

        // 强调标记
        int emphasisCount = 0;
        for (String marker : EMPHASIS_MARKERS) {
            emphasisCount += countOccurrences(cleanText, marker);
        }
        features.put("强调标记", emphasisCount + "次");

        // 标准化表达
        int standardCount = 0;
        for (String phrase : STANDARD_EXPRESSIONS) {
            standardCount += countOccurrences(cleanText, phrase);
        }
        features.put("标准化表达", standardCount + "次");

        // 列表项统计
        String[] lines = cleanText.split("\n");
        int listCount = 0;
        for (String line : lines) {
            Matcher matcher = LIST_PATTERN.matcher(line);
            if (matcher.find()) {
                listCount++;
            }
        }
        features.put("列表项", listCount + "个");

        // 句子平均长度
        String[] sentences = cleanText.split("[。！？；]");
        int totalChars = 0;
        int validSentences = 0;
        for (String sentence : sentences) {
            String cleanSentence = sentence.trim();
            if (cleanSentence.length() > 0) {
                totalChars += cleanSentence.length();
                validSentences++;
            }
        }
        double avgLength = validSentences > 0 ? (double) totalChars / validSentences : 0;
        features.put("平均句长", String.format("%.1f字", avgLength));

        // 论文专属特征（8大维度）
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

    /**
     * 实现接口方法：检测AI模型匹配度（返回BigDecimal）
     */
    @Override
    public BigDecimal detectModel(String content) {
        Map<String, Object> result = detectModelWithDetails(content);
        if (result != null && result.containsKey("score")) {
            Object scoreObj = result.get("score");
            if (scoreObj instanceof Double) {
                return BigDecimal.valueOf((Double) scoreObj);
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 实现接口方法：生成建议（单参数版本）
     */
    @Override
    public List<String> generateSuggestions(String text) {
        BigDecimal score = detectModel(text);
        return generateSuggestions(text, score.doubleValue());
    }

    /**
     * 实现接口方法：生成建议（double版本）
     */
    @Override
    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = new ArrayList<>();

        // 分析论文专属特征
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);

        // 第一部分：百度AI基础特征分析
        suggestions.add("=== 百度AI基础特征分析 ===");
        
        // 简洁表达分析
        int conciseCount = 0;
        for (String phrase : CONCISE_EXPRESSIONS) {
            conciseCount += countOccurrences(text, phrase);
        }
        if (conciseCount > 5) {
            suggestions.add("✓ 简洁表达频繁(" + conciseCount + "次)，符合百度AI特点");
        }
        
        // 实用性表达分析
        int practicalCount = 0;
        for (String phrase : PRACTICAL_EXPRESSIONS) {
            practicalCount += countOccurrences(text, phrase);
        }
        if (practicalCount > 8) {
            suggestions.add("✓ 实用性导向明显(" + practicalCount + "次)，注重可操作性");
        }
        
        // 结构化标记分析
        int structureCount = 0;
        for (String marker : STRUCTURAL_MARKERS) {
            structureCount += countOccurrences(text, marker);
        }
        if (structureCount > 5) {
            suggestions.add("✓ 结构化标记丰富(" + structureCount + "次)，条理清晰");
        }

        // 第二部分：论文专属特征分析
        suggestions.add("\n=== 论文专属特征分析 ===");
        
        double knowledgeDepth = paperFeatures.get("knowledgeDepth");
        if (knowledgeDepth >= 68 && knowledgeDepth <= 85) {
            suggestions.add("✓ 知识深度(" + String.format("%.1f", knowledgeDepth) + 
                          "分)处于百度AI典型区间(68-85)，知识图谱丰富");
        } else if (knowledgeDepth < 68) {
            suggestions.add("○ 知识深度偏低，建议增加专业术语和深度分析");
        } else {
            suggestions.add("○ 知识深度过高，可能超出百度AI典型范围");
        }
        
        double academicFormality = paperFeatures.get("academicFormality");
        if (academicFormality >= 70 && academicFormality <= 87) {
            suggestions.add("✓ 学术规范性(" + String.format("%.1f", academicFormality) + 
                          "分)优秀，NLP能力强");
        }
        
        double languageCoherence = paperFeatures.get("languageCoherence");
        if (languageCoherence >= 72 && languageCoherence <= 88) {
            suggestions.add("✓ 语言连贯性(" + String.format("%.1f", languageCoherence) + 
                          "分)流畅，语义理解深入");
        }
        
        double empiricalEvidence = paperFeatures.get("empiricalEvidence");
        if (empiricalEvidence >= 65 && empiricalEvidence <= 82) {
            suggestions.add("✓ 数据实证(" + String.format("%.1f", empiricalEvidence) + 
                          "分)准确，实体识别能力强");
        }

        // 第三部分：综合优化建议
        suggestions.add("\n=== 综合优化建议 ===");
        if (score >= 70) {
            suggestions.add("【高匹配度】文本具有明显的百度AI生成特征");
            suggestions.add("• 知识图谱丰富，但可增加更多原创观点");
            suggestions.add("• NLP能力强，规范性好，但可适当增加个性化表达");
            suggestions.add("• 语义理解深入，建议增强创新性和批判性思维");
            suggestions.add("• 实体识别准确，数据使用合理，建议增加实证深度");
        } else if (score >= 50) {
            suggestions.add("【较高匹配度】文本显示出一定的百度AI特征");
            suggestions.add("• 建议增强知识深度，丰富专业术语");
            suggestions.add("• 提升学术规范性，规范引用格式");
            suggestions.add("• 加强语义连贯性，优化逻辑结构");
            suggestions.add("• 增加数据实证，提高论证可信度");
        } else {
            suggestions.add("【中等匹配度】文本特征不明显");
            suggestions.add("• 可能为人工撰写或其他AI模型生成");
            suggestions.add("• 若需优化为百度AI风格，建议：");
            suggestions.add("  - 增强知识图谱整合能力");
            suggestions.add("  - 提升中文NLP规范性");
            suggestions.add("  - 深化语义理解和表达");
            suggestions.add("  - 强化实体识别和数据运用");
        }

        return suggestions;
    }

    /**
     * 实现接口方法：生成建议（BigDecimal版本）
     */
    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        return generateSuggestions(content, matchScore.doubleValue());
    }
}
