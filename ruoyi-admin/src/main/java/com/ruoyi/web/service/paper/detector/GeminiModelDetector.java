package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gemini模型检测器
 * 
 * Gemini生成文本的主要特征：
 * 1. 多角度分析：善于从不同维度探讨问题
 * 2. 概念解释：喜欢先定义再展开
 * 3. 结构清晰：使用明确的段落标题
 * 4. 客观中立：避免主观判断
 * 5. 详细论证：每个观点都有详细说明
 * 
 * @author ruoyi
 */
@Component
public class GeminiModelDetector implements IAiModelDetector {

    // Gemini常用概念引导词
    private static final Set<String> GEMINI_CONCEPT_WORDS = new HashSet<>(Arrays.asList(
        "所谓", "即", "指的是", "意味着", "可以理解为", "简单来说",
        "定义为", "本质上", "从定义上看", "具体而言", "换句话说"
    ));

    // Gemini常用多角度分析词
    private static final Set<String> GEMINI_PERSPECTIVE_WORDS = new HashSet<>(Arrays.asList(
        "从...角度", "从...方面", "从...来看", "从...而言", "角度分析",
        "层面考虑", "维度探讨", "视角审视", "立场出发", "观点来看"
    ));

    // Gemini常用客观表达词
    private static final Set<String> GEMINI_OBJECTIVE_WORDS = new HashSet<>(Arrays.asList(
        "可能", "也许", "或许", "一般而言", "通常情况下", "在大多数情况下",
        "据此可以", "有理由相信", "可以推断", "倾向于认为", "有证据表明"
    ));

    @Override
    public String getModelName() {
        return "Gemini";
    }

    @Override
    public String getDetectorName() {
        return "Gemini模型检测器";
    }

    @Override
    public BigDecimal detectModel(String content) {
        Map<String, Object> result = detectModelWithDetails(content);
        double score = (double) result.getOrDefault("score", 0.0);
        return BigDecimal.valueOf(score);
    }

    /**
     * 检测模型并返回详细结果
     * @param text 待检测文本
     * @return 包含得分和详细信息的Map
     */
    public Map<String, Object> detectModelWithDetails(String text) {
        Map<String, Object> result = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            result.put("score", 0);
            result.put("details", new HashMap<>());
            return result;
        }

        // 基础特征得分（40%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（60%权重）
        double paperScore = calculatePaperScore(text);
        
        // 综合得分
        double finalScore = baseScore * 0.4 + paperScore * 0.6;

        result.put("score", Math.min(100, finalScore));
        
        Map<String, Object> details = new HashMap<>();
        details.put("multiPerspective", detectMultiPerspective(text));
        details.put("conceptExplanation", detectConceptExplanation(text));
        details.put("clearStructure", detectClearStructure(text));
        details.put("objectiveTone", detectObjectiveTone(text));
        details.put("detailedArgument", detectDetailedArgument(text));
        result.put("details", details);

        return result;
    }

    /**
     * 计算基础特征得分（40%权重）
     * 保留原有的5个维度检测
     */
    private double calculateBaseScore(String text) {
        double perspectiveScore = detectMultiPerspective(text);    // 30分：多角度分析
        double conceptScore = detectConceptExplanation(text);      // 25分：概念解释
        double structureScore = detectClearStructure(text);        // 20分：结构清晰
        double objectiveScore = detectObjectiveTone(text);         // 15分：客观中立
        double detailScore = detectDetailedArgument(text);         // 10分：详细论证

        return perspectiveScore + conceptScore + structureScore + objectiveScore + detailScore;
    }

    /**
     * 计算论文专属特征得分（60%权重）
     * Gemini特点：多模态能力强、知识整合能力优秀、推理能力强、创新性好
     * 
     * 权重配置：
     * - 创新性22%（Gemini创新性强，70-90分是显著特征）
     * - 知识深度20%（知识整合能力优秀，68-88分）
     * - 论证结构18%（推理能力强，逻辑严密，72-90分）
     * - 学术规范性15%（格式规范但不拘泥，65-82分）
     * - 写作风格12%（表达清晰多样，62-80分）
     * - 语言连贯性8%（连贯性好，60-78分）
     * - 参考文献3%（引用适中，50-70分）
     * - 数据实证2%（数据支撑适中，48-68分）
     */
    private double calculatePaperScore(String text) {
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double innovation = features.get("innovation");              // 创新性
        double knowledgeDepth = features.get("knowledgeDepth");      // 知识深度
        double argumentation = features.get("argumentationStructure"); // 论证结构
        double academicFormality = features.get("academicFormality"); // 学术规范性
        double writingStyle = features.get("writingStyle");          // 写作风格
        double coherence = features.get("languageCoherence");        // 语言连贯性
        double reference = features.get("referencePattern");         // 参考文献
        double empirical = features.get("empiricalEvidence");        // 数据实证
        
        // Gemini的论文特征得分范围调整
        innovation = adjustScoreRange(innovation, 70, 90);           // 创新性强
        knowledgeDepth = adjustScoreRange(knowledgeDepth, 68, 88);   // 知识整合优秀
        argumentation = adjustScoreRange(argumentation, 72, 90);     // 推理严密
        academicFormality = adjustScoreRange(academicFormality, 65, 82); // 规范适中
        writingStyle = adjustScoreRange(writingStyle, 62, 80);       // 表达清晰
        coherence = adjustScoreRange(coherence, 60, 78);             // 连贯性好
        reference = adjustScoreRange(reference, 50, 70);             // 引用适中
        empirical = adjustScoreRange(empirical, 48, 68);             // 数据适中
        
        // 加权计算
        return innovation * 0.22 +
               knowledgeDepth * 0.20 +
               argumentation * 0.18 +
               academicFormality * 0.15 +
               writingStyle * 0.12 +
               coherence * 0.08 +
               reference * 0.03 +
               empirical * 0.02;
    }

    /**
     * 调整得分范围到目标区间
     */
    private double adjustScoreRange(double score, double targetMin, double targetMax) {
        // 将0-100的得分映射到目标区间
        return targetMin + (score / 100.0) * (targetMax - targetMin);
    }

    /**
     * 检测多角度分析（30分）
     * Gemini善于从不同维度探讨问题
     */
    private double detectMultiPerspective(String text) {
        double score = 0;

        // 检测角度分析词
        int perspectiveCount = 0;
        for (String word : GEMINI_PERSPECTIVE_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                perspectiveCount++;
            }
        }

        // 每千字角度分析词密度
        double perspectiveDensity = (perspectiveCount * 1000.0) / text.length();
        if (perspectiveDensity >= 2.0) {
            score += 15;
        } else if (perspectiveDensity >= 1.0) {
            score += 10;
        } else if (perspectiveDensity >= 0.5) {
            score += 5;
        }

        // 检测"从...角度/方面/来看"模式
        Pattern anglePattern = Pattern.compile("从[^，。！？]{1,10}(角度|方面|来看|而言|出发)");
        Matcher angleMatcher = anglePattern.matcher(text);
        int angleCount = 0;
        while (angleMatcher.find()) {
            angleCount++;
        }

        if (angleCount >= 3) {
            score += 15;
        } else if (angleCount >= 2) {
            score += 10;
        } else if (angleCount >= 1) {
            score += 5;
        }

        return score;
    }

    /**
     * 检测概念解释（25分）
     * Gemini喜欢先定义概念再展开
     */
    private double detectConceptExplanation(String text) {
        double score = 0;

        // 检测概念引导词
        int conceptCount = 0;
        for (String word : GEMINI_CONCEPT_WORDS) {
            if (text.contains(word)) {
                conceptCount++;
            }
        }

        if (conceptCount >= 5) {
            score += 15;
        } else if (conceptCount >= 3) {
            score += 10;
        } else if (conceptCount >= 1) {
            score += 5;
        }

        // 检测定义句式："X是指Y"、"X即Y"
        Pattern definitionPattern = Pattern.compile("[^，。！？]{2,15}(是指|即|指的是|意味着)[^，。！？]{5,}");
        Matcher defMatcher = definitionPattern.matcher(text);
        int definitionCount = 0;
        while (defMatcher.find()) {
            definitionCount++;
        }

        if (definitionCount >= 2) {
            score += 10;
        } else if (definitionCount >= 1) {
            score += 5;
        }

        return score;
    }

    /**
     * 检测结构清晰（20分）
     * Gemini使用明确的段落标题和分点
     */
    private double detectClearStructure(String text) {
        double score = 0;

        // 检测段落标题特征（冒号结尾）
        String[] lines = text.split("\n");
        int titleCount = 0;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.endsWith("：") || trimmed.endsWith(":")) {
                titleCount++;
            }
        }

        if (titleCount >= 3) {
            score += 10;
        } else if (titleCount >= 2) {
            score += 7;
        } else if (titleCount >= 1) {
            score += 4;
        }

        // 检测分点标记（1.、2.、一、二等）
        Pattern pointPattern = Pattern.compile("(^|\\n)\\s*([1-9一二三四五][\\.、]|[(（][1-9一二三四五][)）])");
        Matcher pointMatcher = pointPattern.matcher(text);
        int pointCount = 0;
        while (pointMatcher.find()) {
            pointCount++;
        }

        if (pointCount >= 4) {
            score += 10;
        } else if (pointCount >= 2) {
            score += 6;
        } else if (pointCount >= 1) {
            score += 3;
        }

        return score;
    }

    /**
     * 检测客观中立（15分）
     * Gemini避免主观判断，用词客观
     */
    private double detectObjectiveTone(String text) {
        double score = 0;

        // 检测客观表达词
        int objectiveCount = 0;
        for (String word : GEMINI_OBJECTIVE_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                objectiveCount++;
            }
        }

        double objectiveDensity = (objectiveCount * 1000.0) / text.length();
        if (objectiveDensity >= 3.0) {
            score += 10;
        } else if (objectiveDensity >= 2.0) {
            score += 7;
        } else if (objectiveDensity >= 1.0) {
            score += 4;
        }

        // 检测主观词汇（Gemini较少使用）
        String[] subjectiveWords = {"必须", "一定", "绝对", "肯定", "显然", "毋庸置疑"};
        int subjectiveCount = 0;
        for (String word : subjectiveWords) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                subjectiveCount++;
            }
        }

        double subjectiveDensity = (subjectiveCount * 1000.0) / text.length();
        if (subjectiveDensity <= 1.0) {
            score += 5;
        } else if (subjectiveDensity <= 2.0) {
            score += 3;
        }

        return score;
    }

    /**
     * 检测详细论证（10分）
     * Gemini每个观点都有详细说明
     */
    private double detectDetailedArgument(String text) {
        double score = 0;

        // 分析段落长度（Gemini段落通常较长且均匀）
        String[] paragraphs = text.split("\n");
        List<Integer> paragraphLengths = new ArrayList<>();
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 20) {
                paragraphLengths.add(trimmed.length());
            }
        }

        if (!paragraphLengths.isEmpty()) {
            double avgLength = paragraphLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            
            // Gemini段落平均长度通常在80-200字
            if (avgLength >= 80 && avgLength <= 200) {
                score += 5;
            } else if (avgLength >= 60 && avgLength <= 250) {
                score += 3;
            }
        }

        // 检测因果关系词（详细论证的标志）
        String[] causalWords = {"因为", "由于", "所以", "因此", "导致", "使得", "从而"};
        int causalCount = 0;
        for (String word : causalWords) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                causalCount++;
            }
        }

        double causalDensity = (causalCount * 1000.0) / text.length();
        if (causalDensity >= 3.0) {
            score += 5;
        } else if (causalDensity >= 2.0) {
            score += 3;
        }

        return score;
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> features = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return features;
        }

        // 基础特征统计
        // 角度分析统计
        int perspectiveCount = 0;
        for (String word : GEMINI_PERSPECTIVE_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                perspectiveCount++;
            }
        }
        double perspectiveDensity = (perspectiveCount * 1000.0) / text.length();
        features.put("角度分析密度", String.format("%.1f/千字", perspectiveDensity));

        // 概念解释统计
        int conceptCount = 0;
        for (String word : GEMINI_CONCEPT_WORDS) {
            if (text.contains(word)) {
                conceptCount++;
            }
        }
        features.put("概念引导词数", String.valueOf(conceptCount));

        // 段落结构
        String[] paragraphs = text.split("\n");
        List<Integer> paragraphLengths = new ArrayList<>();
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 20) {
                paragraphLengths.add(trimmed.length());
            }
        }
        if (!paragraphLengths.isEmpty()) {
            double avgLength = paragraphLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            features.put("平均段落长度", String.format("%.0f字", avgLength));
        }

        // 客观表达
        int objectiveCount = 0;
        for (String word : GEMINI_OBJECTIVE_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                objectiveCount++;
            }
        }
        double objectiveDensity = (objectiveCount * 1000.0) / text.length();
        features.put("客观表达密度", String.format("%.1f/千字", objectiveDensity));

        // 论文专属特征得分
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        features.put("创新性得分", String.format("%.1f", paperFeatures.get("innovation")));
        features.put("知识深度得分", String.format("%.1f", paperFeatures.get("knowledgeDepth")));
        features.put("论证结构得分", String.format("%.1f", paperFeatures.get("argumentationStructure")));
        features.put("学术规范性得分", String.format("%.1f", paperFeatures.get("academicFormality")));
        features.put("写作风格得分", String.format("%.1f", paperFeatures.get("writingStyle")));
        features.put("语言连贯性得分", String.format("%.1f", paperFeatures.get("languageCoherence")));
        features.put("参考文献得分", String.format("%.1f", paperFeatures.get("referencePattern")));
        features.put("数据实证得分", String.format("%.1f", paperFeatures.get("empiricalEvidence")));

        return features;
    }



    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        if (content == null || content.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 将BigDecimal转换为double后调用主方法
        return generateSuggestions(content, matchScore.doubleValue());
    }

    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return suggestions;
        }

        // 第一部分：Gemini基础特征分析
        suggestions.add("=== Gemini模型匹配度分析 ===");
        suggestions.add(String.format("综合匹配度：%.1f%%", score));
        suggestions.add("");
        
        Map<String, Object> detection = detectModelWithDetails(text);
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) detection.get("details");

        suggestions.add("=== Gemini基础特征分析 ===");
        
        double perspectiveScore = (double) details.get("multiPerspective");
        double conceptScore = (double) details.get("conceptExplanation");
        double structureScore = (double) details.get("clearStructure");
        double objectiveScore = (double) details.get("objectiveTone");
        double detailScore = (double) details.get("detailedArgument");

        if (perspectiveScore >= 20) {
            suggestions.add("✓ 多角度分析明显：大量使用'从...角度'等表达，体现Gemini特征");
        }
        if (conceptScore >= 15) {
            suggestions.add("✓ 概念解释详尽：频繁使用'所谓'、'即'等定义性词汇");
        }
        if (structureScore >= 15) {
            suggestions.add("✓ 结构工整清晰：善用数字标记和段落标题");
        }
        if (objectiveScore >= 10) {
            suggestions.add("✓ 表达客观中立：避免主观判断，用词谨慎");
        }
        if (detailScore >= 5) {
            suggestions.add("✓ 论证详细充分：段落长度适中，逻辑连贯");
        }

        // 第二部分：论文专属特征分析
        suggestions.add("\n=== 论文专属特征分析 ===");
        
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        double innovation = paperFeatures.get("innovation");
        double knowledgeDepth = paperFeatures.get("knowledgeDepth");
        double argumentation = paperFeatures.get("argumentationStructure");
        double writingStyle = paperFeatures.get("writingStyle");
        double coherence = paperFeatures.get("languageCoherence");

        if (innovation >= 70) {
            suggestions.add("✓ 创新性强：" + String.format("%.1f分，观点新颖，符合Gemini高创新性特征", innovation));
        } else if (innovation >= 50) {
            suggestions.add("○ 创新性中等：" + String.format("%.1f分，建议增加原创性观点和批判性思维", innovation));
        } else {
            suggestions.add("✗ 创新性不足：" + String.format("%.1f分，缺乏新颖观点", innovation));
        }

        if (knowledgeDepth >= 68) {
            suggestions.add("✓ 知识深度优秀：" + String.format("%.1f分，知识整合能力强，体现Gemini特征", knowledgeDepth));
        } else if (knowledgeDepth >= 50) {
            suggestions.add("○ 知识深度中等：" + String.format("%.1f分，建议增加专业术语和深度阐述", knowledgeDepth));
        } else {
            suggestions.add("✗ 知识深度不足：" + String.format("%.1f分，专业性欠缺", knowledgeDepth));
        }

        if (argumentation >= 72) {
            suggestions.add("✓ 论证严密：" + String.format("%.1f分，推理能力强，逻辑清晰，符合Gemini特征", argumentation));
        } else if (argumentation >= 50) {
            suggestions.add("○ 论证中等：" + String.format("%.1f分，建议强化逻辑链条和论证深度", argumentation));
        } else {
            suggestions.add("✗ 论证薄弱：" + String.format("%.1f分，逻辑性不足", argumentation));
        }

        if (writingStyle >= 62) {
            suggestions.add("✓ 写作风格清晰多样：" + String.format("%.1f分，表达灵活", writingStyle));
        }

        if (coherence >= 60) {
            suggestions.add("✓ 语言连贯性好：" + String.format("%.1f分，文章流畅", coherence));
        }

        // 第三部分：综合优化建议
        suggestions.add("\n=== 综合优化建议 ===");
        
        if (score >= 70) {
            suggestions.add("【高匹配度】文本与Gemini特征高度吻合");
            suggestions.add("→ 建议：适当减少概念性解释，增加实际案例");
            suggestions.add("→ 建议：打破固定的多角度分析模式，让论述更自然");
            suggestions.add("→ 建议：适当加入主观判断，避免过于客观中立");
        } else if (score >= 50) {
            suggestions.add("【较高匹配度】文本具有一定Gemini特征");
            suggestions.add("→ 建议：增强知识整合能力的体现");
            suggestions.add("→ 建议：提升创新性和推理严密性");
            suggestions.add("→ 建议：优化多角度分析的表达方式");
        } else {
            suggestions.add("【中等匹配度】文本与Gemini特征差异较大");
            suggestions.add("→ 建议：全面提升创新性、知识深度和论证能力");
            suggestions.add("→ 建议：学习Gemini的多维度分析方法");
            suggestions.add("→ 建议：增强概念解释和结构组织能力");
        }

        return suggestions;
    }

    @Override
    public List<String> generateSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return suggestions;
        }

        Map<String, Object> detection = detectModelWithDetails(text);
        double score = (double) detection.get("score");
        
        // 委托给带score参数的方法
        return generateSuggestions(text, score);
    }
}
