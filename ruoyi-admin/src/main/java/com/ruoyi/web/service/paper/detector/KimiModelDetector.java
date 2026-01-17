package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.library.KimiFeatureLibrary;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kimi模型检测器
 * 
 * Kimi AI生成文本的典型特征：
 * 1. 逻辑严密性（因果关系清晰，论证链完整）
 * 2. 排比句使用频繁（三个或以上并列结构）
 * 3. 段落一致性高（主题句+支撑句结构规范）
 * 4. 过渡段落（"综上所述""由此可见"等）
 * 5. 学术化表达（专业术语密度适中）
 * 
 * @author ruoyi
 */
@Component
public class KimiModelDetector implements IAiModelDetector {

    // 因果关系标记
    private static final String[] CAUSAL_MARKERS = {
        "因此", "所以", "因而", "故此", "由此", "从而", "以致",
        "因为", "由于", "鉴于", "基于", "缘于", "导致", "使得"
    };
    
    // 排比句检测模式（寻找相似结构的连续句子）
    private static final Pattern PARALLEL_PATTERN = Pattern.compile(
        "([^，。！？；;]{5,15})[，,]\\s*([^，。！？；;]{5,15})[，,]\\s*([^，。！？；;]{5,15})[。！？；;]"
    );
    
    // 过渡性短语
    private static final String[] TRANSITION_PHRASES = {
        "综上所述", "由此可见", "总而言之", "概括而言", "归纳起来",
        "总的来说", "综合来看", "不难看出", "可以看出", "显而易见"
    };
    
    // 学术术语标记（通用学术词汇）
    private static final String[] ACADEMIC_TERMS = {
        "研究", "分析", "探讨", "阐述", "论证", "验证", "表明", "显示",
        "揭示", "证实", "说明", "体现", "反映", "表现", "呈现", "展现",
        "理论", "方法", "模型", "体系", "框架", "机制", "要素", "因素"
    };

    @Override
    public String getModelName() {
        return "Kimi";
    }

    @Override
    public String getDetectorName() {
        return "Kimi模型检测器";
    }

    @Override
    public BigDecimal detectModel(String content) {
        double score = detectModelWithDetails(content);
        return BigDecimal.valueOf(score);
    }

    /**
     * 检测文本是否为Kimi AI生成（原detectModel方法重命名）
     * @param text 待检测文本
     * @return 匹配度分数（0-100）
     */
    public double detectModelWithDetails(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        // 基础特征得分（25%权重）
        double baseScore = calculateBaseScore(text);
        
        // 论文专属特征得分（40%权重）
        double paperScore = calculatePaperScore(text);
        
        // 增强特征库得分（35%权重）
        double enhancedScore = KimiFeatureLibrary.calculateKimiScore(text);
        
        // 综合得分
        double finalScore = baseScore * 0.25 + paperScore * 0.40 + enhancedScore * 0.35;

        return Math.min(finalScore, 100.0);
    }

    /**
     * 计算基础特征得分（原有检测逻辑）
     */
    private double calculateBaseScore(String text) {
        double score = 0.0;
        
        // 1. 逻辑严密性检测（30分）
        score += detectLogicalCoherence(text);
        
        // 2. 排比句检测（25分）
        score += detectParallelStructure(text);
        
        // 3. 段落一致性检测（20分）
        score += detectParagraphConsistency(text);
        
        // 4. 过渡段落检测（15分）
        score += detectTransitionPhrases(text);
        
        // 5. 学术化表达检测（10分）
        score += detectAcademicStyle(text);

        return Math.min(score, 100.0);
    }

    /**
     * 计算论文专属特征得分
     * Kimi特点：长文本处理能力强、上下文连贯性高、细节丰富、逻辑严密
     */
    private double calculatePaperScore(String text) {
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double score = 0.0;
        
        // 1. 语言连贯性（25%）- Kimi的显著优势，连贯性极高70-85分
        double coherence = features.get("languageCoherence");
        if (coherence >= 70 && coherence <= 85) {
            score += 25.0;
        } else if (coherence >= 60 && coherence < 70) {
            score += 18.0;
        } else if (coherence > 85 && coherence <= 90) {
            score += 18.0;
        } else if (coherence >= 50 && coherence < 60) {
            score += 10.0;
        }
        
        // 2. 论证结构（22%）- Kimi逻辑严密，结构完整70-85分
        double argumentation = features.get("argumentationStructure");
        if (argumentation >= 70 && argumentation <= 85) {
            score += 22.0;
        } else if (argumentation >= 60 && argumentation < 70) {
            score += 16.0;
        } else if (argumentation > 85 && argumentation <= 90) {
            score += 16.0;
        } else if (argumentation >= 50 && argumentation < 60) {
            score += 9.0;
        }
        
        // 3. 写作风格（18%）- Kimi细节丰富，风格多样60-80分
        double style = features.get("writingStyle");
        if (style >= 60 && style <= 80) {
            score += 18.0;
        } else if (style >= 50 && style < 60) {
            score += 13.0;
        } else if (style > 80 && style <= 85) {
            score += 13.0;
        } else if (style >= 40 && style < 50) {
            score += 7.0;
        }
        
        // 4. 知识深度（15%）- Kimi知识深度中等偏上55-70分
        double depth = features.get("knowledgeDepth");
        if (depth >= 55 && depth <= 70) {
            score += 15.0;
        } else if (depth >= 45 && depth < 55) {
            score += 11.0;
        } else if (depth > 70 && depth <= 80) {
            score += 11.0;
        } else if (depth >= 35 && depth < 45) {
            score += 6.0;
        }
        
        // 5. 学术规范性（10%）- Kimi规范性良好65-80分
        double formality = features.get("academicFormality");
        if (formality >= 65 && formality <= 80) {
            score += 10.0;
        } else if (formality >= 55 && formality < 65) {
            score += 7.0;
        } else if (formality > 80 && formality <= 85) {
            score += 7.0;
        } else if (formality >= 45 && formality < 55) {
            score += 4.0;
        }
        
        // 6. 创新性（5%）- Kimi创新性中等40-60分
        double innovation = features.get("innovation");
        if (innovation >= 40 && innovation <= 60) {
            score += 5.0;
        } else if (innovation >= 30 && innovation < 40) {
            score += 3.5;
        } else if (innovation > 60 && innovation <= 70) {
            score += 3.5;
        } else if (innovation >= 20 && innovation < 30) {
            score += 2.0;
        }
        
        // 7. 参考文献（3%）- Kimi引用适中45-65分
        double reference = features.get("referencePattern");
        if (reference >= 45 && reference <= 65) {
            score += 3.0;
        } else if (reference >= 35 && reference < 45) {
            score += 2.0;
        } else if (reference > 65 && reference <= 75) {
            score += 2.0;
        } else if (reference >= 25 && reference < 35) {
            score += 1.0;
        }
        
        // 8. 数据实证（2%）- Kimi数据使用较少35-55分
        double evidence = features.get("empiricalEvidence");
        if (evidence >= 35 && evidence <= 55) {
            score += 2.0;
        } else if (evidence >= 25 && evidence < 35) {
            score += 1.4;
        } else if (evidence > 55 && evidence <= 65) {
            score += 1.4;
        } else if (evidence >= 15 && evidence < 25) {
            score += 0.7;
        }
        
        return Math.min(score, 100.0);
    }

    /**
     * 检测逻辑严密性（因果关系的使用）
     */
    private double detectLogicalCoherence(String text) {
        int count = 0;
        for (String marker : CAUSAL_MARKERS) {
            int index = 0;
            while ((index = text.indexOf(marker, index)) != -1) {
                count++;
                index += marker.length();
            }
        }
        
        // 计算因果标记密度（每1000字的标记数）
        double density = (count * 1000.0) / text.length();
        
        // Kimi特征：密度在5-12之间为典型（逻辑关系标记丰富）
        if (density >= 5 && density <= 12) {
            return 30.0;
        } else if (density >= 3 && density < 5) {
            return 20.0;
        } else if (density > 12 && density <= 15) {
            return 20.0;
        } else if (density >= 2 && density < 3) {
            return 10.0;
        }
        
        return 0.0;
    }

    /**
     * 检测排比句结构
     */
    private double detectParallelStructure(String text) {
        Matcher matcher = PARALLEL_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            // 检查三个部分的相似度
            String part1 = matcher.group(1);
            String part2 = matcher.group(2);
            String part3 = matcher.group(3);
            
            // 简单的相似度检测：长度相近且结构相似
            if (isSimilarStructure(part1, part2, part3)) {
                count++;
            }
        }
        
        // 计算排比句密度（每1000字的排比句数）
        double density = (count * 1000.0) / text.length();
        
        // Kimi特征：密度在1-4之间为典型
        if (density >= 1 && density <= 4) {
            return 25.0;
        } else if (density >= 0.5 && density < 1) {
            return 15.0;
        } else if (density > 4 && density <= 6) {
            return 15.0;
        } else if (density >= 0.3 && density < 0.5) {
            return 8.0;
        }
        
        return 0.0;
    }

    /**
     * 检查三个部分的结构相似度
     */
    private boolean isSimilarStructure(String part1, String part2, String part3) {
        int len1 = part1.length();
        int len2 = part2.length();
        int len3 = part3.length();
        
        // 长度相近（允许30%的差异）
        int maxLen = Math.max(Math.max(len1, len2), len3);
        int minLen = Math.min(Math.min(len1, len2), len3);
        
        return (maxLen - minLen) <= maxLen * 0.3;
    }

    /**
     * 检测段落一致性
     */
    private double detectParagraphConsistency(String text) {
        String[] paragraphs = text.split("\n+");
        int consistentCount = 0;
        
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.length() < 50) continue; // 忽略太短的段落
            
            // 检查段落是否有清晰的结构：主题句+支撑句
            String[] sentences = trimmed.split("[。！？]");
            if (sentences.length >= 3) {
                // 第一句通常较短且概括（主题句特征）
                // 后续句子提供支撑
                String firstSentence = sentences[0].trim();
                if (firstSentence.length() >= 15 && firstSentence.length() <= 40) {
                    // 检查后续句子是否与首句相关（简单方法：共享关键词）
                    boolean hasSupport = false;
                    for (int i = 1; i < sentences.length; i++) {
                        if (sentences[i].length() > 20) {
                            hasSupport = true;
                            break;
                        }
                    }
                    if (hasSupport) {
                        consistentCount++;
                    }
                }
            }
        }
        
        // 计算一致性段落比例
        double ratio = paragraphs.length > 0 ? (double) consistentCount / paragraphs.length : 0;
        
        // Kimi特征：60-90%的段落结构一致
        if (ratio >= 0.6 && ratio <= 0.9) {
            return 20.0;
        } else if (ratio >= 0.5 && ratio < 0.6) {
            return 14.0;
        } else if (ratio > 0.9) {
            return 14.0;
        } else if (ratio >= 0.4 && ratio < 0.5) {
            return 8.0;
        }
        
        return 0.0;
    }

    /**
     * 检测过渡性短语
     */
    private double detectTransitionPhrases(String text) {
        int count = 0;
        for (String phrase : TRANSITION_PHRASES) {
            int index = 0;
            while ((index = text.indexOf(phrase, index)) != -1) {
                count++;
                index += phrase.length();
            }
        }
        
        // 计算过渡短语密度（每1000字的短语数）
        double density = (count * 1000.0) / text.length();
        
        // Kimi特征：密度在2-6之间为典型
        if (density >= 2 && density <= 6) {
            return 15.0;
        } else if (density >= 1 && density < 2) {
            return 10.0;
        } else if (density > 6 && density <= 8) {
            return 10.0;
        } else if (density >= 0.5 && density < 1) {
            return 5.0;
        }
        
        return 0.0;
    }

    /**
     * 检测学术化表达
     */
    private double detectAcademicStyle(String text) {
        int count = 0;
        for (String term : ACADEMIC_TERMS) {
            int index = 0;
            while ((index = text.indexOf(term, index)) != -1) {
                count++;
                index += term.length();
            }
        }
        
        // 计算学术术语密度（每1000字的术语数）
        double density = (count * 1000.0) / text.length();
        
        // Kimi特征：密度在8-20之间为典型（适中的学术化）
        if (density >= 8 && density <= 20) {
            return 10.0;
        } else if (density >= 5 && density < 8) {
            return 6.0;
        } else if (density > 20 && density <= 25) {
            return 6.0;
        } else if (density >= 3 && density < 5) {
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

        // 论文专属特征分析
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        details.put("academicFormality", String.format("%.2f", paperFeatures.get("academicFormality")));
        details.put("argumentationStructure", String.format("%.2f", paperFeatures.get("argumentationStructure")));
        details.put("knowledgeDepth", String.format("%.2f", paperFeatures.get("knowledgeDepth")));
        details.put("writingStyle", String.format("%.2f", paperFeatures.get("writingStyle")));
        details.put("referencePattern", String.format("%.2f", paperFeatures.get("referencePattern")));
        details.put("innovation", String.format("%.2f", paperFeatures.get("innovation")));
        details.put("languageCoherence", String.format("%.2f", paperFeatures.get("languageCoherence")));
        details.put("empiricalEvidence", String.format("%.2f", paperFeatures.get("empiricalEvidence")));

        // 增强特征库分析
        Map<String, Object> enhancedFeatures = KimiFeatureLibrary.getFeatureDetails(text);
        details.put("enhancedFeatures", String.valueOf(enhancedFeatures));
        details.put("enhancedLibraryScore", String.valueOf(KimiFeatureLibrary.calculateKimiScore(text)));

        // 基础特征统计 - 因果标记统计
        int causalCount = 0;
        for (String marker : CAUSAL_MARKERS) {
            int index = 0;
            while ((index = text.indexOf(marker, index)) != -1) {
                causalCount++;
                index += marker.length();
            }
        }
        double causalDensity = (causalCount * 1000.0) / text.length();
        details.put("causal_marker_count", String.valueOf(causalCount));
        details.put("causal_density", String.format("%.2f", causalDensity));
        
        // 排比句统计
        Matcher parallelMatcher = PARALLEL_PATTERN.matcher(text);
        int parallelCount = 0;
        while (parallelMatcher.find()) {
            String part1 = parallelMatcher.group(1);
            String part2 = parallelMatcher.group(2);
            String part3 = parallelMatcher.group(3);
            if (isSimilarStructure(part1, part2, part3)) {
                parallelCount++;
            }
        }
        double parallelDensity = (parallelCount * 1000.0) / text.length();
        details.put("parallel_structure_count", String.valueOf(parallelCount));
        details.put("parallel_density", String.format("%.2f", parallelDensity));
        
        // 段落一致性统计
        String[] paragraphs = text.split("\n+");
        int consistentCount = 0;
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.length() < 50) continue;
            String[] sentences = trimmed.split("[。！？]");
            if (sentences.length >= 3) {
                String firstSentence = sentences[0].trim();
                if (firstSentence.length() >= 15 && firstSentence.length() <= 40) {
                    boolean hasSupport = false;
                    for (int i = 1; i < sentences.length; i++) {
                        if (sentences[i].length() > 20) {
                            hasSupport = true;
                            break;
                        }
                    }
                    if (hasSupport) {
                        consistentCount++;
                    }
                }
            }
        }
        details.put("consistent_paragraph_count", String.valueOf(consistentCount));
        details.put("paragraph_consistency_ratio", String.format("%.2f%%", 
            paragraphs.length > 0 ? (consistentCount * 100.0 / paragraphs.length) : 0));
        
        // 过渡短语统计
        int transitionCount = 0;
        for (String phrase : TRANSITION_PHRASES) {
            int index = 0;
            while ((index = text.indexOf(phrase, index)) != -1) {
                transitionCount++;
                index += phrase.length();
            }
        }
        double transitionDensity = (transitionCount * 1000.0) / text.length();
        details.put("transition_phrase_count", String.valueOf(transitionCount));
        details.put("transition_density", String.format("%.2f", transitionDensity));
        
        // 学术术语统计
        int academicCount = 0;
        for (String term : ACADEMIC_TERMS) {
            int index = 0;
            while ((index = text.indexOf(term, index)) != -1) {
                academicCount++;
                index += term.length();
            }
        }
        double academicDensity = (academicCount * 1000.0) / text.length();
        details.put("academic_term_count", String.valueOf(academicCount));
        details.put("academic_density", String.format("%.2f", academicDensity));
        
        return details;
    }

    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        return generateSuggestions(content, matchScore.doubleValue());
    }

    @Override
    public List<String> generateSuggestions(String text) {
        double score = detectModelWithDetails(text);
        return generateSuggestions(text, score);
    }

    /**
     * 生成修改建议（原generateSuggestions方法）
     * @param text 文本内容
     * @param score 匹配分数
     * @return 建议列表
     */
    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = new ArrayList<>();
        
        if (score < 30) {
            suggestions.add("文本特征与Kimi AI模型不太匹配，可能是人工创作或其他AI模型生成");
            return suggestions;
        }
        
        Map<String, String> features = getFeatureDetails(text);
        
        // === 增强特征库分析（优先级最高）===
        suggestions.add("【Kimi增强特征库分析】");
        
        Object enhancedFeaturesObj = features.get("enhancedFeatures");
        Map<String, Object> enhancedFeatures = null;
        if (enhancedFeaturesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> tempMap = (Map<String, Object>) enhancedFeaturesObj;
            enhancedFeatures = tempMap;
        }
        
        if (enhancedFeatures != null) {
            // 1. 连贯性标记（权重15%）
            Integer coherenceCount = (Integer) enhancedFeatures.get("coherenceMarkerCount");
            if (coherenceCount != null && coherenceCount >= 12) {
                suggestions.add("检测到Kimi特征：连贯性标记过多（" + coherenceCount + "处），建议：");
                suggestions.add("  - 减少\"然而\"、\"此外\"、\"因此\"等连接词的使用");
                suggestions.add("  - 让段落间的关系更自然，不必每次都用连接词标注");
                suggestions.add("  - 保留2-3个关键位置的连接词即可");
            }
            
            // 2. 代词回指（权重15%）
            Integer pronounCount = (Integer) enhancedFeatures.get("pronounReferenceCount");
            if (pronounCount != null && pronounCount >= 10) {
                suggestions.add("检测到Kimi特征：代词回指频繁（" + pronounCount + "处），建议：");
                suggestions.add("  - 减少\"这\"、\"此\"、\"该\"等指示代词的重复使用");
                suggestions.add("  - 直接使用具体名词，而非频繁回指");
                suggestions.add("  - 避免在连续句子中过度使用指代");
            }
            
            // 3. 逻辑链接词（权重14%）
            Integer logicCount = (Integer) enhancedFeatures.get("logicalConnectorCount");
            if (logicCount != null && logicCount >= 15) {
                suggestions.add("检测到Kimi特征：逻辑链接词密集（" + logicCount + "处），建议：");
                suggestions.add("  - 减少\"因为...所以\"、\"由于...因此\"等因果关联词");
                suggestions.add("  - 简化逻辑表达，让推理更隐含");
                suggestions.add("  - 不必在每个论证中都显式标注逻辑关系");
            }
            
            // 4. 句式整齐度（权重13%）
            Integer structureCount = (Integer) enhancedFeatures.get("parallelStructureCount");
            if (structureCount != null && structureCount >= 8) {
                suggestions.add("检测到Kimi特征：句式结构过于整齐（" + structureCount + "处），建议：");
                suggestions.add("  - 打破排比和对仗的句式结构");
                suggestions.add("  - 使用长短句交替，增加节奏变化");
                suggestions.add("  - 避免多个段落使用相同的句式模板");
            }
            
            // 5. 过渡性表达（权重12%）
            Integer transitionCount = (Integer) enhancedFeatures.get("transitionExpressionCount");
            if (transitionCount != null && transitionCount >= 10) {
                suggestions.add("检测到Kimi特征：过渡性表达过多（" + transitionCount + "处），建议：");
                suggestions.add("  - 减少\"综上所述\"、\"总而言之\"等总结性表达");
                suggestions.add("  - 删除\"换言之\"、\"也就是说\"等解释性过渡");
                suggestions.add("  - 让观点直接呈现，不必反复标注转折");
            }
            
            // 6. 细节展开模式（权重11%）
            Integer detailCount = (Integer) enhancedFeatures.get("detailExpansionCount");
            if (detailCount != null && detailCount >= 8) {
                suggestions.add("检测到Kimi特征：细节展开模式明显（" + detailCount + "处），建议：");
                suggestions.add("  - 不是每个观点都需要详细展开");
                suggestions.add("  - 减少\"具体而言\"、\"详细来说\"等引导语");
                suggestions.add("  - 部分内容保持概括性，不必面面俱到");
            }
            
            // 7. 层次递进（权重10%）
            Integer progressionCount = (Integer) enhancedFeatures.get("progressiveLayeringCount");
            if (progressionCount != null && progressionCount >= 7) {
                suggestions.add("检测到Kimi特征：层次递进结构明显（" + progressionCount + "处），建议：");
                suggestions.add("  - 减少\"首先...其次...最后\"的固定模式");
                suggestions.add("  - 打破\"从...到...再到\"的递进链条");
                suggestions.add("  - 不必每次讨论都按严格的层次展开");
            }
            
            // 8. 观点平衡（权重5%）
            Integer balanceCount = (Integer) enhancedFeatures.get("balancedViewCount");
            if (balanceCount != null && balanceCount >= 5) {
                suggestions.add("检测到Kimi特征：观点平衡表达频繁（" + balanceCount + "处），建议：");
                suggestions.add("  - 不必在每个论点后都补充\"一方面...另一方面\"");
                suggestions.add("  - 允许有倾向性的观点，不必总是保持中立");
                suggestions.add("  - 减少刻意的正反平衡结构");
            }
            
            // 9. 主题重申（权重3%）
            Integer restatementCount = (Integer) enhancedFeatures.get("thematicRestatementCount");
            if (restatementCount != null && restatementCount >= 4) {
                suggestions.add("检测到Kimi特征：主题重申过多（" + restatementCount + "处），建议：");
                suggestions.add("  - 减少段首段尾的主题重复");
                suggestions.add("  - 避免在每个段落结尾都回扣主题");
                suggestions.add("  - 主题表达一次即可，不必反复强调");
            }
            
            // 10. 学术性缓冲（权重2%）
            Integer hedgingCount = (Integer) enhancedFeatures.get("academicHedgingCount");
            if (hedgingCount != null && hedgingCount >= 6) {
                suggestions.add("检测到Kimi特征：学术性缓冲表达较多（" + hedgingCount + "处），建议：");
                suggestions.add("  - 减少\"可能\"、\"或许\"、\"在一定程度上\"等模糊表达");
                suggestions.add("  - 部分论述可以更直接、更确定");
                suggestions.add("  - 不必在每个结论前都加缓冲词");
            }
        }
        
        // === 基础特征分析 ===
        suggestions.add("\n【基础特征分析】");
        
        // 因果关系建议
        double causalDensity = Double.parseDouble(features.get("causal_density").toString());
        if (causalDensity >= 5) {
            suggestions.add("检测到频繁使用因果关系标记，建议：");
            suggestions.add("  - 减少\"因此\"、\"所以\"、\"由于\"等连接词");
            suggestions.add("  - 使用隐含的逻辑关系，让读者自行推理");
            suggestions.add("  - 部分论证采用并列或递进关系代替因果");
        }
        
        // 排比句建议
        double parallelDensity = Double.parseDouble(features.get("parallel_density").toString());
        if (parallelDensity >= 1) {
            suggestions.add("检测到较多排比句结构，建议：");
            suggestions.add("  - 打破部分排比结构，使用不同句式");
            suggestions.add("  - 改变句子长度和节奏，增加变化");
            suggestions.add("  - 保留1-2处关键排比，删除其他重复结构");
        }
        
        // 段落结构建议
        String consistencyRatio = features.get("paragraph_consistency_ratio").toString();
        double consistencyPercent = Double.parseDouble(consistencyRatio.replace("%", ""));
        if (consistencyPercent >= 60) {
            suggestions.add("检测到段落结构高度一致，建议：");
            suggestions.add("  - 调整部分段落的组织方式");
            suggestions.add("  - 有的段落可以开门见山，有的可以层层递进");
            suggestions.add("  - 避免所有段落都使用\"总-分\"结构");
        }
        
        // 过渡短语建议
        double transitionDensity = Double.parseDouble(features.get("transition_density").toString());
        if (transitionDensity >= 2) {
            suggestions.add("检测到过渡性短语使用频繁，建议：");
            suggestions.add("  - 减少\"综上所述\"、\"由此可见\"等总结性短语");
            suggestions.add("  - 使用更自然的过渡方式连接段落");
            suggestions.add("  - 让内容的逻辑关系自然呈现，而非刻意标注");
        }
        
        // 学术化表达建议
        double academicDensity = Double.parseDouble(features.get("academic_density").toString());
        if (academicDensity >= 8) {
            suggestions.add("检测到学术术语密度适中，建议：");
            suggestions.add("  - 适当增加通俗化表达，提高可读性");
            suggestions.add("  - 用具体案例代替部分抽象术语");
            suggestions.add("  - 保持学术性的同时增加文本的生动性");
        }
        
        // === 论文专属特征分析 ===
        suggestions.add("\n【论文专属特征分析】");
        
        // 语言连贯性分析（Kimi强项）
        double coherence = Double.parseDouble(features.get("languageCoherence").toString());
        if (coherence >= 70) {
            suggestions.add("语言连贯性过高（" + String.format("%.1f", coherence) + "分），这是Kimi的显著特征：");
            suggestions.add("  - 适当打破过于流畅的连贯性，引入一些自然的思维跳跃");
            suggestions.add("  - 减少过渡词的使用频率，让段落间的转换更自然");
            suggestions.add("  - 在某些地方可以保留思考的痕迹，不必过度润色");
        }
        
        // 论证结构分析
        double argumentation = Double.parseDouble(features.get("argumentationStructure").toString());
        if (argumentation >= 70) {
            suggestions.add("论证结构过于严密（" + String.format("%.1f", argumentation) + "分）：");
            suggestions.add("  - 打破部分严密的逻辑链条，允许一些开放性讨论");
            suggestions.add("  - 不是每个观点都需要完整的论证，可以适当留白");
            suggestions.add("  - 增加探索性表达，而非只有确定性结论");
        }
        
        // 写作风格分析
        double style = Double.parseDouble(features.get("writingStyle").toString());
        if (style >= 60) {
            suggestions.add("写作风格细节丰富但较统一（" + String.format("%.1f", style) + "分）：");
            suggestions.add("  - 减少对细节的过度展开，保持适度的概括性");
            suggestions.add("  - 增加风格变化，不同章节可以采用不同的叙述方式");
            suggestions.add("  - 简化部分过于精细的描述，提高表达效率");
        }
        
        // 知识深度分析
        double depth = Double.parseDouble(features.get("knowledgeDepth").toString());
        if (depth >= 45 && depth <= 70) {
            suggestions.add("知识深度处于Kimi典型范围（" + String.format("%.1f", depth) + "分）：");
            suggestions.add("  - 在关键论点上增加更深入的理论分析");
            suggestions.add("  - 补充更专业的术语和前沿研究引用");
            suggestions.add("  - 提升某些章节的专业深度，避免过于平均");
        }
        
        // 创新性分析
        double innovation = Double.parseDouble(features.get("innovation").toString());
        if (innovation < 50) {
            suggestions.add("创新性不足（" + String.format("%.1f", innovation) + "分），需要提升：");
            suggestions.add("  - 提出更多独特的观点和见解");
            suggestions.add("  - 对现有理论进行批判性分析");
            suggestions.add("  - 尝试新的研究角度或方法论");
        }
        
        // === 综合优化建议 ===
        suggestions.add("\n【综合优化建议】");
        if (score >= 70) {
            suggestions.add("文本与Kimi AI特征高度匹配（匹配度：" + String.format("%.1f%%", score) + "），主要问题：");
            suggestions.add("  1. 连贯性和逻辑性过强，显得过于完美和机械");
            suggestions.add("  2. 细节虽然丰富但缺乏个性化特征");
            suggestions.add("  3. 需要打破流畅性，增加真实的思考痕迹");
            suggestions.add("  4. 建议增加作者个人经验和独特视角");
            suggestions.add("  5. 适当保留一些不完美的表达，增强真实性");
        } else if (score >= 50) {
            suggestions.add("文本呈现较明显的Kimi AI特征（匹配度：" + String.format("%.1f%%", score) + "）：");
            suggestions.add("  1. 保持逻辑清晰的同时，增加表达的多样性");
            suggestions.add("  2. 减少过度连贯的段落衔接，允许适度跳跃");
            suggestions.add("  3. 在细节描述上更有选择性，避免面面俱到");
            suggestions.add("  4. 增加创新性观点和批判性思考");
        } else if (score >= 30) {
            suggestions.add("文本与Kimi AI有一定相似度（匹配度：" + String.format("%.1f%%", score) + "）：");
            suggestions.add("  1. 继续优化逻辑连贯性和论证结构");
            suggestions.add("  2. 注意保持个性化表达，避免过度规范");
            suggestions.add("  3. 平衡好细节与概括的比例");
        }
        
        return suggestions;
    }
}
