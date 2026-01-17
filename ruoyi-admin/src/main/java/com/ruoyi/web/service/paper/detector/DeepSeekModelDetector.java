package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.library.DeepSeekFeatureLibrary;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeepSeek AI模型检测器
 * 
 * DeepSeek特征分析：
 * 1. 理工科思维强：逻辑推导、公式表达、因果关系明确
 * 2. 数据支撑：喜欢用具体数据、百分比、统计数字
 * 3. 量化表达：数字化描述、量化指标、具体数值
 * 4. 专业术语密集：技术性词汇、学术概念、领域专用术语
 * 5. 情感词汇缺失：客观理性，较少使用情感修饰词
 * 
 * @author ruoyi
 */
@Component
public class DeepSeekModelDetector implements IAiModelDetector {

    @Override
    public String getModelName() {
        return "DeepSeek";
    }

    @Override
    public String getDetectorName() {
        return "DeepSeek模型检测器";
    }

    @Override
    public BigDecimal detectModel(String content) {
        double score = detectModelWithDetails(content);
        return BigDecimal.valueOf(score);
    }

    /**
     * 详细检测方法（返回double类型供内部使用）
     */
    public double detectModelWithDetails(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        // 三层检测架构
        // 基础特征得分（25%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（40%权重）
        double paperScore = calculatePaperScore(text);
        
        // 增强特征库得分（35%权重）
        double enhancedScore = DeepSeekFeatureLibrary.calculateDeepSeekScore(text);
        
        // 综合得分
        double finalScore = baseScore * 0.25 + paperScore * 0.40 + enhancedScore * 0.35;
        
        return Math.min(finalScore, 100.0);
    }

    /**
     * 计算基础特征得分
     */
    private double calculateBaseScore(String text) {
        double score = 0.0;
        int textLength = text.length();

        // 1. 数据支撑特征检测 (30分)
        score += detectDataSupport(text, textLength);

        // 2. 量化表达特征检测 (25分)
        score += detectQuantitativeExpression(text, textLength);

        // 3. 逻辑推导特征检测 (20分)
        score += detectLogicalDeduction(text, textLength);

        // 4. 专业术语密集度检测 (15分)
        score += detectTechnicalTerms(text, textLength);

        // 5. 情感词汇缺失检测 (10分)
        score += detectEmotionalLack(text, textLength);

        return Math.min(score, 100.0);
    }

    /**
     * 计算论文专属特征得分
     * DeepSeek特点：技术深度强、代码友好、逻辑严密、专业性高
     * - 知识深度高权重（技术深度强）
     * - 论证结构高权重（逻辑严密）
     * - 学术规范性中高权重（专业性高）
     * - 数据实证中高权重（数据支撑强）
     */
    private double calculatePaperScore(String text) {
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 知识深度 (25%) - DeepSeek在技术深度方面表现突出 (70-90分是显著特征)
        double knowledgeDepth = features.getOrDefault("knowledgeDepth", 0.0);
        if (knowledgeDepth >= 70 && knowledgeDepth <= 90) {
            score += 25.0;
        } else if (knowledgeDepth >= 60 || knowledgeDepth <= 95) {
            score += 18.0;
        } else if (knowledgeDepth >= 50) {
            score += 10.0;
        }
        
        // 论证结构 (22%) - 逻辑推导严密 (70-88分)
        double argumentationStructure = features.getOrDefault("argumentationStructure", 0.0);
        if (argumentationStructure >= 70 && argumentationStructure <= 88) {
            score += 22.0;
        } else if (argumentationStructure >= 60 || argumentationStructure <= 92) {
            score += 16.0;
        } else if (argumentationStructure >= 50) {
            score += 9.0;
        }
        
        // 学术规范性 (18%) - 专业性强 (68-85分)
        double academicFormality = features.getOrDefault("academicFormality", 0.0);
        if (academicFormality >= 68 && academicFormality <= 85) {
            score += 18.0;
        } else if (academicFormality >= 58 || academicFormality <= 90) {
            score += 13.0;
        } else if (academicFormality >= 48) {
            score += 7.0;
        }
        
        // 数据实证 (15%) - 数据支撑能力强 (65-85分)
        double empiricalEvidence = features.getOrDefault("empiricalEvidence", 0.0);
        if (empiricalEvidence >= 65 && empiricalEvidence <= 85) {
            score += 15.0;
        } else if (empiricalEvidence >= 55 || empiricalEvidence <= 90) {
            score += 11.0;
        } else if (empiricalEvidence >= 45) {
            score += 6.0;
        }
        
        // 写作风格 (10%) - 技术性强、客观 (55-75分)
        double writingStyle = features.getOrDefault("writingStyle", 0.0);
        if (writingStyle >= 55 && writingStyle <= 75) {
            score += 10.0;
        } else if (writingStyle >= 45 || writingStyle <= 80) {
            score += 7.0;
        } else if (writingStyle >= 35) {
            score += 4.0;
        }
        
        // 语言连贯性 (5%) - 逻辑连贯性好但不如Kimi (60-75分)
        double languageCoherence = features.getOrDefault("languageCoherence", 0.0);
        if (languageCoherence >= 60 && languageCoherence <= 75) {
            score += 5.0;
        } else if (languageCoherence >= 50 || languageCoherence <= 80) {
            score += 3.0;
        }
        
        // 参考文献 (3%) - 引用适中但偏重技术文献 (50-70分)
        double referencePattern = features.getOrDefault("referencePattern", 0.0);
        if (referencePattern >= 50 && referencePattern <= 70) {
            score += 3.0;
        } else if (referencePattern >= 40 || referencePattern <= 75) {
            score += 2.0;
        }
        
        // 创新性 (2%) - 技术创新性中等 (45-65分)
        double innovation = features.getOrDefault("innovation", 0.0);
        if (innovation >= 45 && innovation <= 65) {
            score += 2.0;
        } else if (innovation >= 35 || innovation <= 70) {
            score += 1.0;
        }
        
        return Math.min(score, 100.0);
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> details = new HashMap<>();
        int textLength = text.length();
        int charCount = textLength;

        // 获取论文专属特征
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        details.put("academicFormality", String.format("%.2f", paperFeatures.getOrDefault("academicFormality", 0.0)));
        details.put("argumentationStructure", String.format("%.2f", paperFeatures.getOrDefault("argumentationStructure", 0.0)));
        details.put("knowledgeDepth", String.format("%.2f", paperFeatures.getOrDefault("knowledgeDepth", 0.0)));
        details.put("writingStyle", String.format("%.2f", paperFeatures.getOrDefault("writingStyle", 0.0)));
        details.put("referencePattern", String.format("%.2f", paperFeatures.getOrDefault("referencePattern", 0.0)));
        details.put("innovation", String.format("%.2f", paperFeatures.getOrDefault("innovation", 0.0)));
        details.put("languageCoherence", String.format("%.2f", paperFeatures.getOrDefault("languageCoherence", 0.0)));
        details.put("empiricalEvidence", String.format("%.2f", paperFeatures.getOrDefault("empiricalEvidence", 0.0)));

        // 获取增强特征库信息
        details.put("enhancedLibraryScore", String.valueOf(DeepSeekFeatureLibrary.calculateDeepSeekScore(text)));

        // 数据支撑特征
        int percentCount = countPatternOccurrences(text, "\\d+%|百分之\\d+");
        int numberWithUnitCount = countPatternOccurrences(text, "\\d+(?:个|次|项|种|类|人|倍|万|亿|千|百)");
        int statisticsPhraseCount = countPhrases(text, Arrays.asList(
            "数据显示", "统计表明", "研究发现", "实验结果", "根据调查",
            "占比", "增长", "下降", "提升", "降低"
        ));
        double dataScore = calculateDataSupportScore(percentCount, numberWithUnitCount, 
                                                     statisticsPhraseCount, charCount);
        details.put("dataSupport_percentCount", String.valueOf(percentCount));
        details.put("dataSupport_numberWithUnitCount", String.valueOf(numberWithUnitCount));
        details.put("dataSupport_statisticsPhraseCount", String.valueOf(statisticsPhraseCount));
        details.put("dataSupport_score", String.format("%.2f", dataScore));

        // 量化表达特征
        int specificNumberCount = countPatternOccurrences(text, "\\d+\\.\\d+|\\d{2,}");
        int comparisonPhraseCount = countPhrases(text, Arrays.asList(
            "相比", "对比", "高于", "低于", "超过", "达到", "约为", "大约"
        ));
        int rangeExpressionCount = countPatternOccurrences(text, "\\d+-\\d+|从\\d+到\\d+");
        double quantitativeScore = calculateQuantitativeScore(specificNumberCount, 
                                                             comparisonPhraseCount, 
                                                             rangeExpressionCount, charCount);
        details.put("quantitative_specificNumberCount", String.valueOf(specificNumberCount));
        details.put("quantitative_comparisonPhraseCount", String.valueOf(comparisonPhraseCount));
        details.put("quantitative_rangeExpressionCount", String.valueOf(rangeExpressionCount));
        details.put("quantitative_score", String.format("%.2f", quantitativeScore));

        // 逻辑推导特征
        int deductionPhraseCount = countPhrases(text, Arrays.asList(
            "因此可以得出", "由此推断", "可以推导", "根据以上分析", "综合以上",
            "假设", "如果", "那么", "推论", "结论"
        ));
        int causalChainCount = countPatternOccurrences(text, "由于.*?因此|因为.*?所以|既然.*?就");
        int formulaCount = countPatternOccurrences(text, "[A-Z]=|∑|∫|[α-ω]|×|÷|≈|≤|≥");
        double logicalScore = calculateLogicalScore(deductionPhraseCount, causalChainCount, 
                                                   formulaCount, charCount);
        details.put("logical_deductionPhraseCount", String.valueOf(deductionPhraseCount));
        details.put("logical_causalChainCount", String.valueOf(causalChainCount));
        details.put("logical_formulaCount", String.valueOf(formulaCount));
        details.put("logical_score", String.format("%.2f", logicalScore));

        // 专业术语特征
        int technicalTermCount = countTechnicalTerms(text);
        int academicPhraseCount = countPhrases(text, Arrays.asList(
            "算法", "模型", "系统", "框架", "机制", "结构", "方法", "理论",
            "技术", "方案", "策略", "优化", "效率", "性能", "指标"
        ));
        double technicalScore = calculateTechnicalScore(technicalTermCount, academicPhraseCount, 
                                                       charCount);
        details.put("technical_technicalTermCount", String.valueOf(technicalTermCount));
        details.put("technical_academicPhraseCount", String.valueOf(academicPhraseCount));
        details.put("technical_score", String.format("%.2f", technicalScore));

        // 情感词汇检测
        int emotionalWordCount = countEmotionalWords(text);
        double emotionalDensity = (emotionalWordCount * 1000.0) / charCount;
        double emotionalScore = calculateEmotionalScore(emotionalDensity);
        details.put("emotional_emotionalWordCount", String.valueOf(emotionalWordCount));
        details.put("emotional_emotionalDensity", String.format("%.2f", emotionalDensity));
        details.put("emotional_score", String.format("%.2f", emotionalScore));

        return details;
    }

    @Override
    public List<String> generateSuggestions(String text) {
        double score = detectModelWithDetails(text);
        return generateSuggestions(text, score);
    }

    @Override
    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = new ArrayList<>();
        Map<String, String> details = getFeatureDetails(text);

        // DeepSeek增强特征库分析
        suggestions.add("=== DeepSeek增强特征库分析 ===");
        Map<String, Object> enhancedFeatures = DeepSeekFeatureLibrary.getFeatureDetails(text);
        
        // 1. 理工思维特征
        double logicalReasoningScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("logicalReasoningScore")));
        if (logicalReasoningScore >= 0.6) {
            suggestions.add("• 理工思维特征明显（得分：" + String.format("%.2f", logicalReasoningScore) + "）：");
            suggestions.add("  - 逻辑推导词使用频繁，建议简化推理链条");
            suggestions.add("  - 增加直观描述，降低抽象推理密度");
        }
        
        // 2. 数据论证特征
        double dataEvidenceScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("dataEvidenceScore")));
        if (dataEvidenceScore >= 0.6) {
            suggestions.add("• 数据论证特征突出（得分：" + String.format("%.2f", dataEvidenceScore) + "）：");
            suggestions.add("  - 数据引用密集，建议保留关键数据删除冗余");
            suggestions.add("  - 用定性描述替换部分具体数字");
        }
        
        // 3. 量化表达特征
        double quantificationScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("quantificationScore")));
        if (quantificationScore >= 0.6) {
            suggestions.add("• 量化表达特征显著（得分：" + String.format("%.2f", quantificationScore) + "）：");
            suggestions.add("  - 精确数字过多，建议用'显著'、'大幅'等模糊表达");
            suggestions.add("  - 减少对比性短语和区间表达");
        }
        
        // 4. 技术术语特征
        double technicalTermScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("technicalTermScore")));
        if (technicalTermScore >= 0.6) {
            suggestions.add("• 技术术语密度高（得分：" + String.format("%.2f", technicalTermScore) + "）：");
            suggestions.add("  - 专业术语过于密集，建议用通俗语言解释");
            suggestions.add("  - 增加实例说明，降低学术化程度");
        }
        
        // 5. 因果分析特征
        double causalAnalysisScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("causalAnalysisScore")));
        if (causalAnalysisScore >= 0.6) {
            suggestions.add("• 因果分析特征明显（得分：" + String.format("%.2f", causalAnalysisScore) + "）：");
            suggestions.add("  - 因果链条过于严密，建议简化逻辑关系");
            suggestions.add("  - 减少'因此'、'所以'等因果词汇");
        }
        
        // 6. 公式推导特征
        double formulaScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("formulaScore")));
        if (formulaScore >= 0.5) {
            suggestions.add("• 公式推导特征存在（得分：" + String.format("%.2f", formulaScore) + "）：");
            suggestions.add("  - 数学符号和公式使用较多");
            suggestions.add("  - 建议增加文字解释，降低公式密度");
        }
        
        // 7. 算法思维特征
        double algorithmThinkingScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("algorithmThinkingScore")));
        if (algorithmThinkingScore >= 0.5) {
            suggestions.add("• 算法思维特征明显（得分：" + String.format("%.2f", algorithmThinkingScore) + "）：");
            suggestions.add("  - 算法相关词汇密集");
            suggestions.add("  - 建议增加应用场景描述");
        }
        
        // 8. 系统性表述特征
        double systematicScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("systematicScore")));
        if (systematicScore >= 0.6) {
            suggestions.add("• 系统性表述特征突出（得分：" + String.format("%.2f", systematicScore) + "）：");
            suggestions.add("  - 系统性词汇使用频繁");
            suggestions.add("  - 建议增加具体细节和案例");
        }
        
        // 9. 性能优化特征
        double optimizationScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("optimizationScore")));
        if (optimizationScore >= 0.5) {
            suggestions.add("• 性能优化特征存在（得分：" + String.format("%.2f", optimizationScore) + "）：");
            suggestions.add("  - 优化相关词汇较多");
            suggestions.add("  - 建议增加实际效果描述");
        }
        
        // 10. 客观理性特征
        double objectivityScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("objectivityScore")));
        if (objectivityScore >= 0.7) {
            suggestions.add("• 客观理性特征极强（得分：" + String.format("%.2f", objectivityScore) + "）：");
            suggestions.add("  - 情感词汇极度缺乏");
            suggestions.add("  - 建议增加主观评价和情感修饰");
        }
        
        // 11. 代码友好特征
        double codeFriendlyScore = Double.parseDouble(String.valueOf(enhancedFeatures.get("codeFriendlyScore")));
        if (codeFriendlyScore >= 0.5) {
            suggestions.add("• 代码友好特征明显（得分：" + String.format("%.2f", codeFriendlyScore) + "）：");
            suggestions.add("  - 代码相关表达较多");
            suggestions.add("  - 建议增加自然语言描述");
        }

        suggestions.add("");
        // 一、基础特征分析
        suggestions.add("=== 基础特征分析 ===");
        
        // 数据支撑建议
        int percentCount = Integer.parseInt(details.get("dataSupport_percentCount"));
        int numberWithUnitCount = Integer.parseInt(details.get("dataSupport_numberWithUnitCount"));
        if (percentCount > 5 || numberWithUnitCount > 8) {
            suggestions.add("• 数据引用过于频繁，建议：");
            suggestions.add("  - 保留关键数据，删除冗余统计信息");
            suggestions.add("  - 用定性描述替换部分具体数字");
            suggestions.add("  - 避免连续使用多个百分比数据");
        }

        // 量化表达建议
        int specificNumberCount = Integer.parseInt(details.get("quantitative_specificNumberCount"));
        int comparisonPhraseCount = Integer.parseInt(details.get("quantitative_comparisonPhraseCount"));
        if (specificNumberCount > 10 || comparisonPhraseCount > 8) {
            suggestions.add("• 量化表达过于密集，建议：");
            suggestions.add("  - 减少精确数字的使用，用'大幅'、'显著'等模糊表达");
            suggestions.add("  - 降低对比性短语的频率");
            suggestions.add("  - 用趋势描述代替具体数值对比");
        }

        // 逻辑推导建议
        int deductionPhraseCount = Integer.parseInt(details.get("logical_deductionPhraseCount"));
        int causalChainCount = Integer.parseInt(details.get("logical_causalChainCount"));
        if (deductionPhraseCount > 6 || causalChainCount > 4) {
            suggestions.add("• 逻辑推导过于严密，建议：");
            suggestions.add("  - 简化因果链条，避免过度推导");
            suggestions.add("  - 减少'因此'、'推断'等逻辑词汇");
            suggestions.add("  - 增加直观描述，降低推理密度");
        }

        // 专业术语建议
        int technicalTermCount = Integer.parseInt(details.get("technical_technicalTermCount"));
        if (technicalTermCount > 15) {
            suggestions.add("• 专业术语过于密集，建议：");
            suggestions.add("  - 用通俗语言解释部分专业概念");
            suggestions.add("  - 减少学术化词汇的堆砌");
            suggestions.add("  - 增加实例说明，降低抽象度");
        }

        // 情感词汇建议
        int emotionalWordCount = Integer.parseInt(details.get("emotional_emotionalWordCount"));
        if (emotionalWordCount < 3) {
            suggestions.add("• 文本过于客观理性，建议：");
            suggestions.add("  - 适当增加情感修饰词（如'令人振奋'、'令人担忧'）");
            suggestions.add("  - 表达个人观点和态度");
            suggestions.add("  - 增加主观评价和感受描述");
        }

        // 二、论文专属特征分析
        suggestions.add("\n=== 论文专属特征分析 ===");
        
        // 知识深度分析
        double knowledgeDepth = Double.parseDouble(details.get("knowledgeDepth"));
        if (knowledgeDepth >= 70 && knowledgeDepth <= 90) {
            suggestions.add("• 知识深度极高（" + String.format("%.1f", knowledgeDepth) + "分）：");
            suggestions.add("  - DeepSeek显著特征：技术深度强、专业术语密集");
            suggestions.add("  - 建议：适当降低专业术语密度，增加通俗解释");
        } else if (knowledgeDepth >= 60) {
            suggestions.add("• 知识深度较高（" + String.format("%.1f", knowledgeDepth) + "分）");
        }
        
        // 论证结构分析
        double argumentationStructure = Double.parseDouble(details.get("argumentationStructure"));
        if (argumentationStructure >= 70 && argumentationStructure <= 88) {
            suggestions.add("• 论证结构严密（" + String.format("%.1f", argumentationStructure) + "分）：");
            suggestions.add("  - DeepSeek显著特征：逻辑推导链完整、因果关系明确");
            suggestions.add("  - 建议：简化推理链条，避免过度推导");
        } else if (argumentationStructure >= 60) {
            suggestions.add("• 论证结构良好（" + String.format("%.1f", argumentationStructure) + "分）");
        }
        
        // 学术规范性分析
        double academicFormality = Double.parseDouble(details.get("academicFormality"));
        if (academicFormality >= 68 && academicFormality <= 85) {
            suggestions.add("• 学术规范性强（" + String.format("%.1f", academicFormality) + "分）：");
            suggestions.add("  - DeepSeek显著特征：专业性高、术语使用规范");
            suggestions.add("  - 建议：增加个性化表达，避免过于模板化");
        }
        
        // 数据实证分析
        double empiricalEvidence = Double.parseDouble(details.get("empiricalEvidence"));
        if (empiricalEvidence >= 65 && empiricalEvidence <= 85) {
            suggestions.add("• 数据实证性强（" + String.format("%.1f", empiricalEvidence) + "分）：");
            suggestions.add("  - DeepSeek显著特征：数据支撑充分、量化表达密集");
            suggestions.add("  - 建议：减少数字堆砌，用定性描述替换部分数据");
        }
        
        // 写作风格分析
        double writingStyle = Double.parseDouble(details.get("writingStyle"));
        if (writingStyle >= 55 && writingStyle <= 75) {
            suggestions.add("• 写作风格技术化（" + String.format("%.1f", writingStyle) + "分）：");
            suggestions.add("  - DeepSeek特征：技术性强、客观理性、缺乏变化");
            suggestions.add("  - 建议：增加句式变化和修辞手法");
        }

        // 三、综合优化建议
        suggestions.add("\n=== 综合优化建议 ===");
        if (score >= 70) {
            suggestions.add("✓ 高度匹配DeepSeek生成特征（" + String.format("%.1f", score) + "分）");
            suggestions.add("核心优化方向：");
            suggestions.add("1. 降低技术深度和专业术语密度");
            suggestions.add("2. 简化逻辑推导链条，增加直观表达");
            suggestions.add("3. 减少数据堆砌，增加定性描述");
            suggestions.add("4. 添加个人观点和情感色彩");
            suggestions.add("5. 增加写作风格的变化性和个性化");
        } else if (score >= 50) {
            suggestions.add("✓ 较高匹配DeepSeek生成特征（" + String.format("%.1f", score) + "分）");
            suggestions.add("建议适度调整技术深度和逻辑严密性");
        } else if (score >= 30) {
            suggestions.add("✓ 中等匹配DeepSeek生成特征（" + String.format("%.1f", score) + "分）");
            suggestions.add("存在部分DeepSeek特征，可针对性优化");
        } else {
            suggestions.add("✓ 低匹配DeepSeek生成特征（" + String.format("%.1f", score) + "分）");
            suggestions.add("文本特征分布较为均衡，未检测到明显DeepSeek特征");
        }

        return suggestions;
    }

    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        return generateSuggestions(content, matchScore.doubleValue());
    }

    /**
     * 检测数据支撑特征
     */
    private double detectDataSupport(String text, int textLength) {
        int charCount = textLength;
        
        // 百分比表达
        int percentCount = countPatternOccurrences(text, "\\d+%|百分之\\d+");
        
        // 带单位的数字
        int numberWithUnitCount = countPatternOccurrences(text, "\\d+(?:个|次|项|种|类|人|倍|万|亿|千|百)");
        
        // 统计短语
        int statisticsPhraseCount = countPhrases(text, Arrays.asList(
            "数据显示", "统计表明", "研究发现", "实验结果", "根据调查",
            "占比", "增长", "下降", "提升", "降低"
        ));

        return calculateDataSupportScore(percentCount, numberWithUnitCount, statisticsPhraseCount, charCount);
    }

    private double calculateDataSupportScore(int percentCount, int numberWithUnitCount, 
                                            int statisticsPhraseCount, int charCount) {
        double score = 0.0;
        
        // 百分比密度评分 (最高12分)
        double percentDensity = (percentCount * 1000.0) / charCount;
        if (percentDensity >= 3 && percentDensity <= 8) {
            score += 12.0;
        } else if (percentDensity >= 2 || percentDensity <= 10) {
            score += 8.0;
        } else if (percentDensity >= 1) {
            score += 4.0;
        }
        
        // 数字单位密度评分 (最高12分)
        double numberDensity = (numberWithUnitCount * 1000.0) / charCount;
        if (numberDensity >= 5 && numberDensity <= 12) {
            score += 12.0;
        } else if (numberDensity >= 3 || numberDensity <= 15) {
            score += 8.0;
        } else if (numberDensity >= 1) {
            score += 4.0;
        }
        
        // 统计短语评分 (最高6分)
        double statisticsDensity = (statisticsPhraseCount * 1000.0) / charCount;
        if (statisticsDensity >= 2 && statisticsDensity <= 6) {
            score += 6.0;
        } else if (statisticsDensity >= 1 || statisticsDensity <= 8) {
            score += 4.0;
        }
        
        return Math.min(score, 30.0);
    }

    /**
     * 检测量化表达特征
     */
    private double detectQuantitativeExpression(String text, int textLength) {
        int charCount = textLength;
        
        // 具体数字（包含小数）
        int specificNumberCount = countPatternOccurrences(text, "\\d+\\.\\d+|\\d{2,}");
        
        // 比较性短语
        int comparisonPhraseCount = countPhrases(text, Arrays.asList(
            "相比", "对比", "高于", "低于", "超过", "达到", "约为", "大约"
        ));
        
        // 区间表达
        int rangeExpressionCount = countPatternOccurrences(text, "\\d+-\\d+|从\\d+到\\d+");

        return calculateQuantitativeScore(specificNumberCount, comparisonPhraseCount, 
                                         rangeExpressionCount, charCount);
    }

    private double calculateQuantitativeScore(int specificNumberCount, int comparisonPhraseCount, 
                                             int rangeExpressionCount, int charCount) {
        double score = 0.0;
        
        // 具体数字密度评分 (最高12分)
        double numberDensity = (specificNumberCount * 1000.0) / charCount;
        if (numberDensity >= 8 && numberDensity <= 20) {
            score += 12.0;
        } else if (numberDensity >= 5 || numberDensity <= 25) {
            score += 8.0;
        } else if (numberDensity >= 3) {
            score += 4.0;
        }
        
        // 比较短语密度评分 (最高8分)
        double comparisonDensity = (comparisonPhraseCount * 1000.0) / charCount;
        if (comparisonDensity >= 4 && comparisonDensity <= 10) {
            score += 8.0;
        } else if (comparisonDensity >= 2 || comparisonDensity <= 12) {
            score += 5.0;
        }
        
        // 区间表达评分 (最高5分)
        if (rangeExpressionCount >= 3) {
            score += 5.0;
        } else if (rangeExpressionCount >= 2) {
            score += 3.0;
        } else if (rangeExpressionCount >= 1) {
            score += 1.0;
        }
        
        return Math.min(score, 25.0);
    }

    /**
     * 检测逻辑推导特征
     */
    private double detectLogicalDeduction(String text, int textLength) {
        int charCount = textLength;
        
        // 推导短语
        int deductionPhraseCount = countPhrases(text, Arrays.asList(
            "因此可以得出", "由此推断", "可以推导", "根据以上分析", "综合以上",
            "假设", "如果", "那么", "推论", "结论"
        ));
        
        // 因果链
        int causalChainCount = countPatternOccurrences(text, "由于.*?因此|因为.*?所以|既然.*?就");
        
        // 公式符号
        int formulaCount = countPatternOccurrences(text, "[A-Z]=|∑|∫|[α-ω]|×|÷|≈|≤|≥");

        return calculateLogicalScore(deductionPhraseCount, causalChainCount, formulaCount, charCount);
    }

    private double calculateLogicalScore(int deductionPhraseCount, int causalChainCount, 
                                        int formulaCount, int charCount) {
        double score = 0.0;
        
        // 推导短语密度评分 (最高10分)
        double deductionDensity = (deductionPhraseCount * 1000.0) / charCount;
        if (deductionDensity >= 4 && deductionDensity <= 10) {
            score += 10.0;
        } else if (deductionDensity >= 2 || deductionDensity <= 12) {
            score += 6.0;
        } else if (deductionDensity >= 1) {
            score += 3.0;
        }
        
        // 因果链评分 (最高6分)
        double causalDensity = (causalChainCount * 1000.0) / charCount;
        if (causalDensity >= 2 && causalDensity <= 6) {
            score += 6.0;
        } else if (causalDensity >= 1 || causalDensity <= 8) {
            score += 4.0;
        }
        
        // 公式符号评分 (最高4分)
        if (formulaCount >= 3) {
            score += 4.0;
        } else if (formulaCount >= 1) {
            score += 2.0;
        }
        
        return Math.min(score, 20.0);
    }

    /**
     * 检测专业术语特征
     */
    private double detectTechnicalTerms(String text, int textLength) {
        int charCount = textLength;
        
        // 专业术语计数
        int technicalTermCount = countTechnicalTerms(text);
        
        // 学术性短语
        int academicPhraseCount = countPhrases(text, Arrays.asList(
            "算法", "模型", "系统", "框架", "机制", "结构", "方法", "理论",
            "技术", "方案", "策略", "优化", "效率", "性能", "指标"
        ));

        return calculateTechnicalScore(technicalTermCount, academicPhraseCount, charCount);
    }

    private double calculateTechnicalScore(int technicalTermCount, int academicPhraseCount, 
                                          int charCount) {
        double score = 0.0;
        
        // 专业术语密度评分 (最高10分)
        double technicalDensity = (technicalTermCount * 1000.0) / charCount;
        if (technicalDensity >= 15 && technicalDensity <= 35) {
            score += 10.0;
        } else if (technicalDensity >= 10 || technicalDensity <= 40) {
            score += 6.0;
        } else if (technicalDensity >= 5) {
            score += 3.0;
        }
        
        // 学术短语评分 (最高5分)
        double academicDensity = (academicPhraseCount * 1000.0) / charCount;
        if (academicDensity >= 8 && academicDensity <= 20) {
            score += 5.0;
        } else if (academicDensity >= 5 || academicDensity <= 25) {
            score += 3.0;
        }
        
        return Math.min(score, 15.0);
    }

    /**
     * 检测情感词汇缺失特征
     */
    private double detectEmotionalLack(String text, int textLength) {
        int charCount = textLength;
        int emotionalWordCount = countEmotionalWords(text);
        double emotionalDensity = (emotionalWordCount * 1000.0) / charCount;
        
        return calculateEmotionalScore(emotionalDensity);
    }

    private double calculateEmotionalScore(double emotionalDensity) {
        // DeepSeek特点是情感词汇少，所以密度越低分数越高
        if (emotionalDensity < 2) {
            return 10.0;
        } else if (emotionalDensity < 4) {
            return 7.0;
        } else if (emotionalDensity < 6) {
            return 4.0;
        } else {
            return 1.0;
        }
    }

    /**
     * 统计专业术语数量
     */
    private int countTechnicalTerms(String text) {
        List<String> technicalTerms = Arrays.asList(
            "算法", "模型", "系统", "框架", "架构", "机制", "结构", "流程",
            "方法", "策略", "方案", "理论", "技术", "工具", "平台", "协议",
            "接口", "模块", "组件", "服务", "资源", "配置", "参数", "变量",
            "函数", "类", "对象", "实例", "属性", "方法论", "范式", "模式",
            "优化", "性能", "效率", "指标", "阈值", "权重", "因子", "系数",
            "矩阵", "向量", "张量", "维度", "特征", "样本", "数据集", "训练",
            "验证", "测试", "评估", "准确率", "召回率", "精度", "误差"
        );
        
        int count = 0;
        for (String term : technicalTerms) {
            count += countOccurrences(text, term);
        }
        return count;
    }

    /**
     * 统计情感词汇数量
     */
    private int countEmotionalWords(String text) {
        List<String> emotionalWords = Arrays.asList(
            "激动", "兴奋", "喜悦", "快乐", "幸福", "满意", "欣慰", "骄傲",
            "担心", "焦虑", "忧虑", "恐惧", "害怕", "紧张", "不安", "沮丧",
            "愤怒", "生气", "恼怒", "失望", "遗憾", "惋惜", "悲伤", "难过",
            "惊讶", "震惊", "吃惊", "意外", "感动", "温暖", "美好", "精彩",
            "令人振奋", "令人担忧", "令人欣慰", "令人遗憾", "非常", "十分",
            "特别", "极其", "相当", "颇为", "深感", "倍感"
        );
        
        int count = 0;
        for (String word : emotionalWords) {
            count += countOccurrences(text, word);
        }
        return count;
    }

    /**
     * 统计短语出现次数
     */
    private int countPhrases(String text, List<String> phrases) {
        int count = 0;
        for (String phrase : phrases) {
            count += countOccurrences(text, phrase);
        }
        return count;
    }

    /**
     * 统计字符串出现次数
     */
    private int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }

    /**
     * 统计正则模式匹配次数
     */
    private int countPatternOccurrences(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
