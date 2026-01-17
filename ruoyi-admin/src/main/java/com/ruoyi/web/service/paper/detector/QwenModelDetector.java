package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.library.QwenFeatureLibrary;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 千问(Qwen)模型检测器
 * 
 * 千问AI生成文本的典型特征：
 * 1. 列举式论证（一、二、三或1、2、3结构）
 * 2. 括号使用频繁（用于补充说明）
 * 3. 设问句开头（"如何...？""什么是...？"）
 * 4. 长句比例高（35字以上）
 * 5. 被动语态使用多（"被认为""被视为""被广泛应用"）
 * 
 * @author ruoyi
 */
@Component
public class QwenModelDetector implements IAiModelDetector {

    // 列举式标记
    private static final Pattern ENUMERATE_PATTERN = Pattern.compile(
        "[一二三四五六七八九十]、|[①②③④⑤⑥⑦⑧⑨⑩]|\\d+[、.]"
    );
    
    // 括号使用
    private static final Pattern BRACKET_PATTERN = Pattern.compile("[（(][^）)]+[）)]");
    
    // 设问句模式
    private static final String[] QUESTION_STARTERS = {
        "如何", "怎样", "什么是", "为什么", "是什么", "有哪些", "能否"
    };
    
    // 被动语态标记
    private static final String[] PASSIVE_MARKERS = {
        "被认为", "被视为", "被广泛", "被应用", "被称为", "被定义为",
        "被看作", "被理解为", "得到", "受到", "被发现", "被证明"
    };

    @Override
    public String getModelName() {
        return "Qwen";
    }

    @Override
    public String getDetectorName() {
        return "QwenModelDetector";
    }

    @Override
    public BigDecimal detectModel(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 基础特征得分（25%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（40%权重）
        double paperScore = calculatePaperScore(text);
        
        // 增强特征库得分（35%权重）
        double enhancedScore = QwenFeatureLibrary.calculateQwenScore(text);
        
        // 加权组合：三层检测架构
        double finalScore = baseScore * 0.25 + paperScore * 0.40 + enhancedScore * 0.35;
        
        return BigDecimal.valueOf(Math.min(finalScore, 100.0)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算基础特征得分
     */
    private double calculateBaseScore(String text) {
        double score = 0.0;
        
        // 1. 列举式论证检测（30分）
        score += detectEnumerateStyle(text);
        
        // 2. 括号使用频率检测（25分）
        score += detectBracketUsage(text);
        
        // 3. 设问句开头检测（20分）
        score += detectQuestionStarters(text);
        
        // 4. 长句比例检测（15分）
        score += detectLongSentences(text);
        
        // 5. 被动语态密度检测（10分）
        score += detectPassiveVoice(text);

        return Math.min(score, 100.0);
    }
    
    /**
     * 计算论文专属特征得分（针对千问特点优化）
     * 千问特点：知识广度优势、多领域覆盖、引用多样性高、综合性强但深度可能不足
     */
    private double calculatePaperScore(String content) {
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(content);
        double score = 0;
        
        // 知识深度（权重15%）- 千问偏中等偏上
        double depthScore = paperFeatures.get("knowledgeDepth");
        if (depthScore >= 50 && depthScore <= 70) {
            score += 15; // 中等偏上深度是千问特征
        } else if (depthScore >= 40 && depthScore < 50) {
            score += 10;
        } else if (depthScore > 70 && depthScore <= 80) {
            score += 10;
        }
        
        // 参考文献（权重25%）- 千问的强项
        double referenceScore = paperFeatures.get("referencePattern");
        if (referenceScore >= 70) {
            score += 25; // 引用多样性高是千问显著特征
        } else if (referenceScore >= 60 && referenceScore < 70) {
            score += 20;
        } else if (referenceScore >= 50 && referenceScore < 60) {
            score += 12;
        }
        
        // 论证结构（权重20%）- 千问结构较完整
        double argumentScore = paperFeatures.get("argumentationStructure");
        if (argumentScore >= 65 && argumentScore <= 80) {
            score += 20; // 结构完整但不过度是千问特征
        } else if (argumentScore >= 55 && argumentScore < 65) {
            score += 15;
        } else if (argumentScore > 80) {
            score += 10; // 过于完整可能是其他模型
        }
        
        // 学术规范性（权重15%）- 千问规范性良好
        double formalityScore = paperFeatures.get("academicFormality");
        if (formalityScore >= 60 && formalityScore <= 75) {
            score += 15; // 规范但不过度
        } else if (formalityScore >= 50 && formalityScore < 60) {
            score += 10;
        } else if (formalityScore > 75 && formalityScore <= 85) {
            score += 10;
        }
        
        // 写作风格（权重10%）- 千问风格较多样
        double styleScore = paperFeatures.get("writingStyle");
        if (styleScore >= 55 && styleScore <= 75) {
            score += 10; // 风格多样性适中
        } else if (styleScore >= 45 && styleScore < 55) {
            score += 6;
        }
        
        // 语言连贯性（权重8%）- 千问连贯性良好
        double coherenceScore = paperFeatures.get("languageCoherence");
        if (coherenceScore >= 65 && coherenceScore <= 80) {
            score += 8;
        } else if (coherenceScore >= 55 && coherenceScore < 65) {
            score += 5;
        }
        
        // 创新性（权重5%）- 千问创新性中等
        double innovationScore = paperFeatures.get("innovation");
        if (innovationScore >= 35 && innovationScore <= 55) {
            score += 5; // 中等创新性
        } else if (innovationScore >= 25 && innovationScore < 35) {
            score += 3;
        }
        
        // 数据实证（权重2%）- 次要特征
        double evidenceScore = paperFeatures.get("empiricalEvidence");
        if (evidenceScore >= 40 && evidenceScore <= 65) {
            score += 2;
        }
        
        return score;
    }

    /**
     * 检测列举式论证风格
     */
    private double detectEnumerateStyle(String text) {
        Matcher matcher = ENUMERATE_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        // 计算列举标记密度（每1000字的标记数）
        double density = (count * 1000.0) / text.length();
        
        // 千问特征：密度在3-8之间为典型
        if (density >= 3 && density <= 8) {
            return 30.0;
        } else if (density >= 2 && density < 3) {
            return 20.0;
        } else if (density > 8 && density <= 10) {
            return 20.0;
        } else if (density >= 1 && density < 2) {
            return 10.0;
        }
        
        return 0.0;
    }

    /**
     * 检测括号使用频率
     */
    private double detectBracketUsage(String text) {
        Matcher matcher = BRACKET_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        // 计算括号密度（每1000字的括号对数）
        double density = (count * 1000.0) / text.length();
        
        // 千问特征：密度在4-10之间为典型（括号使用较频繁）
        if (density >= 4 && density <= 10) {
            return 25.0;
        } else if (density >= 2 && density < 4) {
            return 15.0;
        } else if (density > 10 && density <= 15) {
            return 15.0;
        } else if (density >= 1 && density < 2) {
            return 5.0;
        }
        
        return 0.0;
    }

    /**
     * 检测设问句开头
     */
    private double detectQuestionStarters(String text) {
        String[] paragraphs = text.split("\n+");
        int questionCount = 0;
        
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            
            for (String starter : QUESTION_STARTERS) {
                if (trimmed.startsWith(starter)) {
                    questionCount++;
                    break;
                }
            }
        }
        
        // 计算设问句段落比例
        double ratio = (double) questionCount / paragraphs.length;
        
        // 千问特征：10-30%的段落使用设问句开头
        if (ratio >= 0.1 && ratio <= 0.3) {
            return 20.0;
        } else if (ratio >= 0.05 && ratio < 0.1) {
            return 12.0;
        } else if (ratio > 0.3 && ratio <= 0.4) {
            return 12.0;
        } else if (ratio > 0 && ratio < 0.05) {
            return 5.0;
        }
        
        return 0.0;
    }

    /**
     * 检测长句比例
     */
    private double detectLongSentences(String text) {
        String[] sentences = text.split("[。！？;；]");
        int longCount = 0;
        int validCount = 0;
        
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.length() < 5) continue;
            
            validCount++;
            if (trimmed.length() >= 35) {
                longCount++;
            }
        }
        
        if (validCount == 0) return 0.0;
        
        // 计算长句比例
        double ratio = (double) longCount / validCount;
        
        // 千问特征：长句比例在40-60%之间
        if (ratio >= 0.4 && ratio <= 0.6) {
            return 15.0;
        } else if (ratio >= 0.3 && ratio < 0.4) {
            return 10.0;
        } else if (ratio > 0.6 && ratio <= 0.7) {
            return 10.0;
        } else if (ratio >= 0.2 && ratio < 0.3) {
            return 5.0;
        }
        
        return 0.0;
    }

    /**
     * 检测被动语态密度
     */
    private double detectPassiveVoice(String text) {
        int count = 0;
        for (String marker : PASSIVE_MARKERS) {
            int index = 0;
            while ((index = text.indexOf(marker, index)) != -1) {
                count++;
                index += marker.length();
            }
        }
        
        // 计算被动语态密度（每1000字的标记数）
        double density = (count * 1000.0) / text.length();
        
        // 千问特征：密度在3-8之间为典型
        if (density >= 3 && density <= 8) {
            return 10.0;
        } else if (density >= 2 && density < 3) {
            return 6.0;
        } else if (density > 8 && density <= 10) {
            return 6.0;
        } else if (density >= 1 && density < 2) {
            return 3.0;
        }
        
        return 0.0;
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> details = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return details;
        }

        // 基础特征统计
        // 列举式统计
        Matcher enumerateMatcher = ENUMERATE_PATTERN.matcher(text);
        int enumerateCount = 0;
        while (enumerateMatcher.find()) {
            enumerateCount++;
        }
        double enumerateDensity = (enumerateCount * 1000.0) / text.length();
        details.put("enumerate_count", String.valueOf(enumerateCount));
        details.put("enumerate_density", String.format("%.2f", enumerateDensity));
        
        // 括号统计
        Matcher bracketMatcher = BRACKET_PATTERN.matcher(text);
        int bracketCount = 0;
        while (bracketMatcher.find()) {
            bracketCount++;
        }
        double bracketDensity = (bracketCount * 1000.0) / text.length();
        details.put("bracket_count", String.valueOf(bracketCount));
        details.put("bracket_density", String.format("%.2f", bracketDensity));
        
        // 设问句统计
        String[] paragraphs = text.split("\n+");
        int questionCount = 0;
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            for (String starter : QUESTION_STARTERS) {
                if (trimmed.startsWith(starter)) {
                    questionCount++;
                    break;
                }
            }
        }
        details.put("question_paragraph_count", String.valueOf(questionCount));
        details.put("question_paragraph_ratio", String.format("%.2f%%", (questionCount * 100.0 / paragraphs.length)));
        
        // 长句统计
        String[] sentences = text.split("[。！？;；]");
        int longCount = 0;
        int validCount = 0;
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.length() < 5) continue;
            validCount++;
            if (trimmed.length() >= 35) {
                longCount++;
            }
        }
        details.put("long_sentence_count", String.valueOf(longCount));
        details.put("long_sentence_ratio", String.format("%.2f%%", validCount > 0 ? (longCount * 100.0 / validCount) : 0));
        
        // 被动语态统计
        int passiveCount = 0;
        for (String marker : PASSIVE_MARKERS) {
            int index = 0;
            while ((index = text.indexOf(marker, index)) != -1) {
                passiveCount++;
                index += marker.length();
            }
        }
        double passiveDensity = (passiveCount * 1000.0) / text.length();
        details.put("passive_voice_count", String.valueOf(passiveCount));
        details.put("passive_voice_density", String.format("%.2f", passiveDensity));
        
        // 论文专属特征统计
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        details.put("academicFormality", String.format("%.2f", paperFeatures.get("academicFormality")));
        details.put("argumentationStructure", String.format("%.2f", paperFeatures.get("argumentationStructure")));
        details.put("knowledgeDepth", String.format("%.2f", paperFeatures.get("knowledgeDepth")));
        details.put("writingStyle", String.format("%.2f", paperFeatures.get("writingStyle")));
        details.put("referencePattern", String.format("%.2f", paperFeatures.get("referencePattern")));
        details.put("innovation", String.format("%.2f", paperFeatures.get("innovation")));
        details.put("languageCoherence", String.format("%.2f", paperFeatures.get("languageCoherence")));
        details.put("empiricalEvidence", String.format("%.2f", paperFeatures.get("empiricalEvidence")));
        
        // 增强特征库统计
        Map<String, Object> enhancedFeatures = QwenFeatureLibrary.getFeatureDetails(text);
        details.put("enhancedFeatures", String.valueOf(enhancedFeatures));
        details.put("enhancedLibraryScore", String.valueOf(QwenFeatureLibrary.calculateQwenScore(text)));
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(String text) {
        return generateSuggestions(text, BigDecimal.valueOf(50));
    }

    public List<String> generateSuggestions(String text, double score) {
        return generateSuggestions(text, BigDecimal.valueOf(score));
    }

    @Override
    public List<String> generateSuggestions(String text, BigDecimal matchScore) {
        double score = matchScore.doubleValue();
        List<String> suggestions = new ArrayList<>();
        
        if (score < 30) {
            suggestions.add("文本特征与千问AI模型不太匹配，可能是人工创作或其他AI模型生成");
            return suggestions;
        }
        
        Map<String, String> features = getFeatureDetails(text);
        
        // 解析增强特征
        String enhancedFeaturesStr = features.get("enhancedFeatures");
        Map<String, Object> enhancedFeatures = QwenFeatureLibrary.getFeatureDetails(text);
        
        // === 增强特征库建议 ===
        suggestions.add("【增强特征库分析】");
        
        // 分析性过渡词建议
        int analyticalTransitionCount = (int) enhancedFeatures.get("analyticalTransitionCount");
        if (analyticalTransitionCount >= 8) {
            suggestions.add("【分析性过渡词过度】减少”由此可见“、”综上所述“、”据此分析“等表达的频率");
            suggestions.add("  - 使用更自然的论证过渡方式");
            suggestions.add("  - 避免在每个段落都使用标准化的过渡短语");
        }
        
        // 结论引导词建议
        int conclusionMarkerCount = (int) enhancedFeatures.get("conclusionMarkerCount");
        if (conclusionMarkerCount >= 6) {
            suggestions.add("【结论引导词过度】减少”总之“、”综上“、”由此可知“等结论标志词");
            suggestions.add("  - 让论证自然达到结论，而非强制标注");
            suggestions.add("  - 适度保留1-2处关键结论标记");
        }
        
        // 知识广度表达建议
        int knowledgeBreadthCount = (int) enhancedFeatures.get("knowledgeBreadthCount");
        if (knowledgeBreadthCount >= 10) {
            suggestions.add("【知识广度表达过度】减少”涉及“、”涵盖“、”包含“等表达的使用");
            suggestions.add("  - 深化单一主题的纵深论述");
            suggestions.add("  - 避免泛泛而谈的知识罗列");
        }
        
        // 多维度分析建议
        int multiDimensionalCount = (int) enhancedFeatures.get("multiDimensionalCount");
        if (multiDimensionalCount >= 7) {
            suggestions.add("【多维度分析过度】减少”多方面“、”多角度“、”多层次“等表达");
            suggestions.add("  - 聚焦核心维度的深入分析");
            suggestions.add("  - 避免为追求全面而失去重点");
        }
        
        // 引用整合方式建议
        int citationIntegrationCount = (int) enhancedFeatures.get("citationIntegrationCount");
        if (citationIntegrationCount >= 8) {
            suggestions.add("【引用整合方式模板化】减少”学者指出”、“研究表明”等引用模板");
            suggestions.add("  - 使用更多样化的引用表达方式");
            suggestions.add("  - 深化对引用文献的批判性分析");
        }
        
        // 假设性讨论建议
        int hypotheticalDiscussionCount = (int) enhancedFeatures.get("hypotheticalDiscussionCount");
        if (hypotheticalDiscussionCount >= 5) {
            suggestions.add("【假设性讨论过度】减少“假设”、“如果...那么”等假设性表达");
            suggestions.add("  - 基于实证数据进行论证");
            suggestions.add("  - 减少理论推演的比重");
        }
        
        // 知识综合表达建议
        int knowledgeSynthesisCount = (int) enhancedFeatures.get("knowledgeSynthesisCount");
        if (knowledgeSynthesisCount >= 6) {
            suggestions.add("【知识综合表达过度】减少“整合”、“融合”、“综合”等表达");
            suggestions.add("  - 展现原创性的理论贡献");
            suggestions.add("  - 避免简单的知识拼接");
        }
        
        // === 基础特征建议 ===
        suggestions.add("\n【基础特征分析】");
        
        // 列举式建议
        double enumerateDensity = Double.parseDouble(features.get("enumerate_density"));
        if (enumerateDensity >= 3) {
            suggestions.add("检测到频繁的列举式论证结构，建议：");
            suggestions.add("  - 减少一、二、三或1、2、3等明显的序号标记");
            suggestions.add("  - 改用更自然的过渡方式连接论点");
            suggestions.add("  - 适当合并相关论点，避免过度分点");
        }
        
        // 括号使用建议
        double bracketDensity = Double.parseDouble(features.get("bracket_density"));
        if (bracketDensity >= 4) {
            suggestions.add("检测到括号使用频繁，建议：");
            suggestions.add("  - 将括号内的补充说明融入正文");
            suggestions.add("  - 使用逗号、破折号等标点代替部分括号");
            suggestions.add("  - 保留关键的括号说明，删除冗余的补充");
        }
        
        // 设问句建议
        String questionRatio = features.get("question_paragraph_ratio");
        double questionPercent = Double.parseDouble(questionRatio.replace("%", ""));
        if (questionPercent >= 10) {
            suggestions.add("检测到较多设问句开头，建议：");
            suggestions.add("  - 减少以\"如何\"、\"什么是\"等疑问词开头的段落");
            suggestions.add("  - 改用陈述句直接表达观点");
            suggestions.add("  - 适当保留1-2个设问句增加互动性");
        }
        
        // 长句建议
        String longRatio = features.get("long_sentence_ratio");
        double longPercent = Double.parseDouble(longRatio.replace("%", ""));
        if (longPercent >= 40) {
            suggestions.add("检测到长句比例较高，建议：");
            suggestions.add("  - 将部分长句拆分为2-3个短句");
            suggestions.add("  - 简化复杂的句式结构");
            suggestions.add("  - 控制单句长度在20-30字为宜");
        }
        
        // 被动语态建议
        double passiveDensity = Double.parseDouble(features.get("passive_voice_density"));
        if (passiveDensity >= 3) {
            suggestions.add("检测到被动语态使用较多，建议：");
            suggestions.add("  - 将部分被动句改为主动句");
            suggestions.add("  - 减少\"被认为\"、\"被视为\"等表达");
            suggestions.add("  - 使用更直接的主动表达方式");
        }
        
        // === 论文专属特征建议 ===
        suggestions.add("\n【论文专属特征分析】");
        
        // 知识深度建议
        double depthScore = Double.parseDouble(features.get("knowledgeDepth"));
        if (depthScore >= 50 && depthScore <= 70) {
            suggestions.add("知识深度分析：中等偏上（千问典型特征）");
            suggestions.add("  - 深化专业概念的阐述，增加理论深度");
            suggestions.add("  - 补充前沿研究成果和案例分析");
            suggestions.add("  - 强化单一领域的纵深挖掘");
        }
        
        // 参考文献建议
        double referenceScore = Double.parseDouble(features.get("referencePattern"));
        if (referenceScore >= 70) {
            suggestions.add("引用模式分析：多样性高（千问显著特征）");
            suggestions.add("  - 虽然引用广泛，但需增加经典文献比重");
            suggestions.add("  - 深化对核心文献的解读和分析");
            suggestions.add("  - 减少泛泛而谈的引用方式");
        }
        
        // 论证结构建议
        double argumentScore = Double.parseDouble(features.get("argumentationStructure"));
        if (argumentScore >= 65 && argumentScore <= 80) {
            suggestions.add("论证结构分析：结构完整（千问特征）");
            suggestions.add("  - 避免过于标准化的论证模板");
            suggestions.add("  - 增加论证的层次递进感");
            suggestions.add("  - 强化论点之间的逻辑关联");
        }
        
        // 学术规范性建议
        double formalityScore = Double.parseDouble(features.get("academicFormality"));
        if (formalityScore >= 60 && formalityScore <= 75) {
            suggestions.add("学术规范性分析：规范良好（千问特征）");
            suggestions.add("  - 保持规范性同时增加表达灵活性");
            suggestions.add("  - 适当融入个人学术见解");
            suggestions.add("  - 避免过度依赖标准学术用语");
        }
        
        // 创新性建议
        double innovationScore = Double.parseDouble(features.get("innovation"));
        if (innovationScore >= 35 && innovationScore <= 55) {
            suggestions.add("创新性分析：中等水平（千问特征）");
            suggestions.add("  - 提出更具原创性的研究视角");
            suggestions.add("  - 强化批判性思维的体现");
            suggestions.add("  - 增加独特的研究方法或分析框架");
        }
        
        // === 综合优化建议 ===
        suggestions.add("\n【综合优化建议】");
        
        // 综合建议
        if (score >= 70) {
            suggestions.add("\n综合优化建议：");
            suggestions.add("  - 文本与千问AI特征高度匹配，建议全面人工改写");
            suggestions.add("  - 调整整体论证结构，避免明显的模板化痕迹");
            suggestions.add("  - 增加个人观点和独特见解");
            suggestions.add("  - 使用更多样化的表达方式和句式");
        } else if (score >= 50) {
            suggestions.add("匹配度：较高（50-70分）");
            suggestions.add("  - 文本呈现较明显的千问AI特征，需要重点优化");
            suggestions.add("  - 重构部分段落的论证逻辑");
            suggestions.add("  - 优化表达方式，减少AI痕迹");
            suggestions.add("  - 重点：深化知识深度，提升创新性");
        } else {
            suggestions.add("匹配度：中等（30-50分）");
            suggestions.add("  - 文本存在部分千问AI特征");
            suggestions.add("  - 针对性优化检测到的特征项");
            suggestions.add("  - 增强内容的个性化表达");
        }
        
        return suggestions;
    }
}
