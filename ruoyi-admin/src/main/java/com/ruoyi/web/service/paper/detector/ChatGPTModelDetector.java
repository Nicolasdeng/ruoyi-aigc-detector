package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChatGPT模型检测器
 * 
 * ChatGPT生成文本的主要特征：
 * 1. 均衡性：句长、段落长度分布均匀
 * 2. 模板化：常用固定句式和开头
 * 3. 过渡自然：使用大量过渡词
 * 4. 正式但不僵硬：语气适中
 * 5. 列举清晰：喜欢用数字列表
 * 
 * @author ruoyi
 */
@Component
public class ChatGPTModelDetector implements IAiModelDetector {

    // ChatGPT常用开头短语
    private static final Set<String> CHATGPT_OPENING_PHRASES = new HashSet<>(Arrays.asList(
        "在当今社会", "众所周知", "不可否认", "毫无疑问", "值得注意的是",
        "需要强调的是", "从某种意义上说", "总的来说", "一般来说", "换句话说",
        "重要的是", "有趣的是", "令人惊讶的是", "显而易见", "事实上"
    ));

    // ChatGPT常用过渡词
    private static final Set<String> CHATGPT_TRANSITION_WORDS = new HashSet<>(Arrays.asList(
        "然而", "因此", "此外", "同时", "另一方面", "与此同时", "相反",
        "尽管如此", "总之", "综上所述", "换言之", "例如", "比如", "具体来说",
        "更重要的是", "不仅如此", "进一步说", "也就是说", "总而言之"
    ));

    // ChatGPT常用结尾短语
    private static final Set<String> CHATGPT_CLOSING_PHRASES = new HashSet<>(Arrays.asList(
        "综上所述", "总而言之", "总的来说", "由此可见", "因此可以得出",
        "综合以上分析", "通过以上论述", "基于以上讨论", "归根结底", "最终",
        "展望未来", "未来发展", "有待进一步", "值得深入研究"
    ));

    @Override
    public String getModelName() {
        return "ChatGPT";
    }

    @Override
    public String getDetectorName() {
        return "ChatGPT模型检测器";
    }

    @Override
    public BigDecimal detectModel(String content) {
        Map<String, Object> result = detectModelWithDetails(content);
        double score = (double) result.get("score");
        return BigDecimal.valueOf(score);
    }

    /**
     * 检测模型并返回详细信息
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

        result.put("score", Math.min(100.0, finalScore));
        
        Map<String, Object> details = new HashMap<>();
        details.put("baseScore", baseScore);
        details.put("paperScore", paperScore);
        result.put("details", details);

        return result;
    }

    /**
     * 计算基础特征得分（40%权重）
     * ChatGPT生成文本的基础特征
     */
    private double calculateBaseScore(String text) {
        // 5个维度检测，总分100
        double balanceScore = detectTextBalance(text);      // 30分：文本均衡性
        double templateScore = detectTemplateUsage(text);   // 25分：模板化特征
        double transitionScore = detectTransitions(text);   // 20分：过渡词使用
        double toneScore = detectFormalTone(text);          // 15分：正式语气
        double listScore = detectListUsage(text);           // 10分：列表使用

        return balanceScore + templateScore + transitionScore + toneScore + listScore;
    }

    /**
     * 计算论文专属特征得分（60%权重）
     * 
     * ChatGPT在论文写作中的特点：
     * - 语言连贯性强：自然流畅，过渡自然（70-88分是显著特征）
     * - 写作风格优秀：表达清晰，句式多样（68-85分）
     * - 学术规范性中等：格式正确但不够深入（60-78分）
     * - 论证结构好：逻辑清晰但深度适中（62-80分）
     * - 知识深度适中：知识广泛但不够深入（55-75分）
     * - 创新性一般：观点常规（50-70分）
     * - 数据实证弱：较少数据支撑（45-65分）
     * - 参考文献一般：引用标准但不够丰富（48-68分）
     */
    private double calculatePaperScore(String text) {
        // 使用PaperFeatureAnalyzer分析8大论文特征
        Map<String, Double> features = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        
        double languageCoherence = features.get("languageCoherence");
        double writingStyle = features.get("writingStyle");
        double academicFormality = features.get("academicFormality");
        double argumentationStructure = features.get("argumentationStructure");
        double knowledgeDepth = features.get("knowledgeDepth");
        double innovation = features.get("innovation");
        double empiricalEvidence = features.get("empiricalEvidence");
        double referencePattern = features.get("referencePattern");

        // ChatGPT的论文特征权重配置（总和100%）
        double score = 0;
        
        // 1. 语言连贯性（23%）- ChatGPT的最强特点
        if (languageCoherence >= 70 && languageCoherence <= 88) {
            score += 23;
        } else if (languageCoherence >= 65 && languageCoherence <= 92) {
            score += 18;
        } else if (languageCoherence >= 60) {
            score += 12;
        }
        
        // 2. 写作风格（22%）- 表达清晰自然
        if (writingStyle >= 68 && writingStyle <= 85) {
            score += 22;
        } else if (writingStyle >= 62 && writingStyle <= 90) {
            score += 17;
        } else if (writingStyle >= 55) {
            score += 11;
        }
        
        // 3. 学术规范性（18%）- 格式正确但深度适中
        if (academicFormality >= 60 && academicFormality <= 78) {
            score += 18;
        } else if (academicFormality >= 55 && academicFormality <= 83) {
            score += 14;
        } else if (academicFormality >= 50) {
            score += 9;
        }
        
        // 4. 论证结构（15%）- 逻辑清晰但深度适中
        if (argumentationStructure >= 62 && argumentationStructure <= 80) {
            score += 15;
        } else if (argumentationStructure >= 55 && argumentationStructure <= 85) {
            score += 12;
        } else if (argumentationStructure >= 50) {
            score += 8;
        }
        
        // 5. 知识深度（10%）- 广泛但不深入
        if (knowledgeDepth >= 55 && knowledgeDepth <= 75) {
            score += 10;
        } else if (knowledgeDepth >= 50 && knowledgeDepth <= 80) {
            score += 7;
        } else if (knowledgeDepth >= 45) {
            score += 4;
        }
        
        // 6. 创新性（5%）- 观点常规
        if (innovation >= 50 && innovation <= 70) {
            score += 5;
        } else if (innovation >= 45 && innovation <= 75) {
            score += 3;
        } else if (innovation >= 40) {
            score += 2;
        }
        
        // 7. 数据实证（4%）- 较少数据支撑
        if (empiricalEvidence >= 45 && empiricalEvidence <= 65) {
            score += 4;
        } else if (empiricalEvidence >= 40 && empiricalEvidence <= 70) {
            score += 3;
        } else if (empiricalEvidence >= 35) {
            score += 1;
        }
        
        // 8. 参考文献（3%）- 引用标准但不丰富
        if (referencePattern >= 48 && referencePattern <= 68) {
            score += 3;
        } else if (referencePattern >= 43 && referencePattern <= 73) {
            score += 2;
        } else if (referencePattern >= 38) {
            score += 1;
        }
        
        return score;
    }

    /**
     * 检测文本均衡性（30分）
     * ChatGPT生成的文本句长、段落长度分布均匀
     */
    private double detectTextBalance(String text) {
        double score = 0;

        // 分析句子长度分布
        String[] sentences = text.split("[。！？]");
        if (sentences.length < 3) {
            return 0;
        }

        List<Integer> sentenceLengths = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                sentenceLengths.add(trimmed.length());
            }
        }

        if (sentenceLengths.isEmpty()) {
            return 0;
        }

        // 计算句长标准差
        double avgLength = sentenceLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = sentenceLengths.stream()
            .mapToDouble(len -> Math.pow(len - avgLength, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // ChatGPT的句长标准差通常在15-35之间
        if (stdDev >= 15 && stdDev <= 35) {
            score += 15;
        } else if (stdDev >= 10 && stdDev <= 40) {
            score += 10;
        } else if (stdDev >= 5 && stdDev <= 50) {
            score += 5;
        }

        // 检查句长分布的均匀性
        int shortSentences = 0;  // <20字
        int mediumSentences = 0; // 20-40字
        int longSentences = 0;   // >40字

        for (int len : sentenceLengths) {
            if (len < 20) {
                shortSentences++;
            } else if (len <= 40) {
                mediumSentences++;
            } else {
                longSentences++;
            }
        }

        int total = sentenceLengths.size();
        double shortRatio = (double) shortSentences / total;
        double mediumRatio = (double) mediumSentences / total;
        double longRatio = (double) longSentences / total;

        // ChatGPT倾向于中等句子占比最高
        if (mediumRatio >= 0.4 && shortRatio >= 0.2 && longRatio >= 0.2) {
            score += 15;
        } else if (mediumRatio >= 0.3) {
            score += 10;
        } else if (mediumRatio >= 0.2) {
            score += 5;
        }

        return score;
    }

    /**
     * 检测模板化特征（25分）
     * ChatGPT常用固定开头和结尾
     */
    private double detectTemplateUsage(String text) {
        double score = 0;

        // 检查开头短语
        int openingCount = 0;
        for (String phrase : CHATGPT_OPENING_PHRASES) {
            if (text.contains(phrase)) {
                openingCount++;
            }
        }

        // 每千字开头短语密度
        double openingDensity = (openingCount * 1000.0) / text.length();
        if (openingDensity >= 2.0) {
            score += 10;
        } else if (openingDensity >= 1.0) {
            score += 7;
        } else if (openingDensity >= 0.5) {
            score += 4;
        }

        // 检查结尾短语
        int closingCount = 0;
        String lastPart = text.substring(Math.max(0, text.length() - 200));
        for (String phrase : CHATGPT_CLOSING_PHRASES) {
            if (lastPart.contains(phrase)) {
                closingCount++;
            }
        }

        if (closingCount >= 2) {
            score += 10;
        } else if (closingCount >= 1) {
            score += 5;
        }

        // 检查"首先...其次...最后"结构
        boolean hasFirstly = text.contains("首先") || text.contains("第一");
        boolean hasSecondly = text.contains("其次") || text.contains("第二");
        boolean hasFinally = text.contains("最后") || text.contains("最终");

        if (hasFirstly && hasSecondly && hasFinally) {
            score += 5;
        } else if ((hasFirstly && hasSecondly) || (hasSecondly && hasFinally)) {
            score += 2;
        }

        return score;
    }

    /**
     * 检测过渡词使用（20分）
     * ChatGPT善于使用过渡词保持连贯性
     */
    private double detectTransitions(String text) {
        double score = 0;

        int transitionCount = 0;
        for (String word : CHATGPT_TRANSITION_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                transitionCount++;
            }
        }

        // 每千字过渡词密度
        double transitionDensity = (transitionCount * 1000.0) / text.length();
        
        // ChatGPT的过渡词密度通常在3-8之间
        if (transitionDensity >= 3.0 && transitionDensity <= 8.0) {
            score += 15;
        } else if (transitionDensity >= 2.0 && transitionDensity <= 10.0) {
            score += 10;
        } else if (transitionDensity >= 1.0) {
            score += 5;
        }

        // 检查段落开头的过渡词
        String[] paragraphs = text.split("\n");
        int paragraphsWithTransition = 0;
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            for (String word : CHATGPT_TRANSITION_WORDS) {
                if (trimmed.startsWith(word)) {
                    paragraphsWithTransition++;
                    break;
                }
            }
        }

        if (paragraphs.length > 0) {
            double transitionParagraphRatio = (double) paragraphsWithTransition / paragraphs.length;
            if (transitionParagraphRatio >= 0.3) {
                score += 5;
            } else if (transitionParagraphRatio >= 0.2) {
                score += 3;
            }
        }

        return score;
    }

    /**
     * 检测正式语气（15分）
     * ChatGPT的语气正式但不僵硬
     */
    private double detectFormalTone(String text) {
        double score = 0;

        // 检测第一人称使用频率（ChatGPT较少使用）
        int firstPersonCount = 0;
        String[] firstPersonWords = {"我", "我们", "我的", "我们的", "本人"};
        for (String word : firstPersonWords) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                firstPersonCount++;
            }
        }

        double firstPersonDensity = (firstPersonCount * 1000.0) / text.length();
        if (firstPersonDensity <= 2.0) {
            score += 7;
        } else if (firstPersonDensity <= 5.0) {
            score += 4;
        }

        // 检测疑问句使用（ChatGPT适度使用）
        int questionCount = text.split("\\?|？").length - 1;
        double questionDensity = (questionCount * 1000.0) / text.length();
        if (questionDensity >= 1.0 && questionDensity <= 3.0) {
            score += 5;
        } else if (questionDensity >= 0.5 && questionDensity <= 4.0) {
            score += 3;
        }

        // 检测感叹句使用（ChatGPT较少使用）
        int exclamationCount = text.split("!|！").length - 1;
        double exclamationDensity = (exclamationCount * 1000.0) / text.length();
        if (exclamationDensity <= 1.0) {
            score += 3;
        } else if (exclamationDensity <= 2.0) {
            score += 1;
        }

        return score;
    }

    /**
     * 检测列表使用（10分）
     * ChatGPT喜欢用数字列表组织内容
     */
    private double detectListUsage(String text) {
        double score = 0;

        // 检测数字列表标记
        Pattern numberListPattern = Pattern.compile("[1-9][\\.、]");
        Matcher matcher = numberListPattern.matcher(text);
        int numberListCount = 0;
        while (matcher.find()) {
            numberListCount++;
        }

        if (numberListCount >= 3) {
            score += 5;
        } else if (numberListCount >= 2) {
            score += 3;
        } else if (numberListCount >= 1) {
            score += 1;
        }

        // 检测"首先...其次...再次...最后"这样的序号词
        String[] orderWords = {"首先", "其次", "再次", "然后", "接着", "最后"};
        int orderWordCount = 0;
        for (String word : orderWords) {
            if (text.contains(word)) {
                orderWordCount++;
            }
        }

        if (orderWordCount >= 4) {
            score += 5;
        } else if (orderWordCount >= 3) {
            score += 3;
        } else if (orderWordCount >= 2) {
            score += 1;
        }

        return score;
    }

    @Override
    public Map<String, String> getFeatureDetails(String text) {
        Map<String, String> features = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return features;
        }

        // 基础特征分析
        // 句长分析
        String[] sentences = text.split("[。！？]");
        List<Integer> sentenceLengths = new ArrayList<>();
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                sentenceLengths.add(trimmed.length());
            }
        }

        if (!sentenceLengths.isEmpty()) {
            double avgLength = sentenceLengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            double variance = sentenceLengths.stream()
                .mapToDouble(len -> Math.pow(len - avgLength, 2))
                .average().orElse(0);
            double stdDev = Math.sqrt(variance);
            features.put("平均句长", String.format("%.1f字", avgLength));
            features.put("句长标准差", String.format("%.1f", stdDev));
        }

        // 模板化特征统计
        int openingCount = 0;
        for (String phrase : CHATGPT_OPENING_PHRASES) {
            if (text.contains(phrase)) {
                openingCount++;
            }
        }
        features.put("开头短语数", String.valueOf(openingCount));

        // 过渡词统计
        int transitionCount = 0;
        for (String word : CHATGPT_TRANSITION_WORDS) {
            Pattern pattern = Pattern.compile(word);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                transitionCount++;
            }
        }
        double transitionDensity = (transitionCount * 1000.0) / text.length();
        features.put("过渡词密度", String.format("%.1f/千字", transitionDensity));

        // 列表使用
        Pattern numberListPattern = Pattern.compile("[1-9][\\.、]");
        Matcher matcher = numberListPattern.matcher(text);
        int numberListCount = 0;
        while (matcher.find()) {
            numberListCount++;
        }
        features.put("数字列表标记", String.valueOf(numberListCount));

        // 论文专属特征分析
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
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
    public List<String> generateSuggestions(String text) {
        List<String> suggestions = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return suggestions;
        }

        Map<String, Object> detection = detectModelWithDetails(text);
        double score = (double) detection.get("score");

        // 分析基础特征
        double balanceScore = detectTextBalance(text);
        double templateScore = detectTemplateUsage(text);
        double transitionScore = detectTransitions(text);
        double toneScore = detectFormalTone(text);
        double listScore = detectListUsage(text);

        suggestions.add("=== ChatGPT特征分析 ===");
        
        // 基础特征分析
        if (balanceScore >= 20) {
            suggestions.add("【文本均衡性】句长分布过于均匀，建议：");
            suggestions.add("  - 增加句式变化，有意设置一些短句和长句形成对比");
            suggestions.add("  - 避免所有句子都保持相似的长度");
        }

        if (templateScore >= 15) {
            suggestions.add("【模板化特征】固定表达过多，建议：");
            suggestions.add("  - 减少使用'众所周知'、'毫无疑问'等ChatGPT常用开头");
            suggestions.add("  - 避免使用'综上所述'、'总而言之'等固定结尾");
            suggestions.add("  - 采用更个性化的表达方式");
        }

        if (transitionScore >= 15) {
            suggestions.add("【过渡词使用】过渡词密度过高，建议：");
            suggestions.add("  - 减少'然而'、'因此'、'此外'等过渡词的频率");
            suggestions.add("  - 不要在每个段落开头都使用过渡词");
            suggestions.add("  - 让段落之间的衔接更自然流畅");
        }

        if (toneScore >= 10) {
            suggestions.add("【正式语气】语气过于标准化，建议：");
            suggestions.add("  - 适当增加个人色彩和观点表达");
            suggestions.add("  - 在合适的地方使用第一人称");
        }

        if (listScore >= 7) {
            suggestions.add("【列表使用】数字列表过多，建议：");
            suggestions.add("  - 减少'首先...其次...最后'等序号结构");
            suggestions.add("  - 尝试用更自然的方式组织内容");
        }

        // 论文专属特征分析
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(text);
        double languageCoherence = paperFeatures.get("languageCoherence");
        double writingStyle = paperFeatures.get("writingStyle");
        double knowledgeDepth = paperFeatures.get("knowledgeDepth");
        double innovation = paperFeatures.get("innovation");
        double empiricalEvidence = paperFeatures.get("empiricalEvidence");

        suggestions.add("\n=== 论文专属特征分析 ===");

        if (languageCoherence >= 70 && languageCoherence <= 88) {
            suggestions.add("【语言连贯性】流畅度符合ChatGPT特征（70-88分），建议：");
            suggestions.add("  - 适当打破过于流畅的表达，增加思考停顿");
            suggestions.add("  - 在复杂观点处增加解释性插入语");
        }

        if (writingStyle >= 68 && writingStyle <= 85) {
            suggestions.add("【写作风格】清晰度符合ChatGPT特征（68-85分），建议：");
            suggestions.add("  - 增加个人化的表达风格");
            suggestions.add("  - 在适当位置使用更复杂或更简洁的句式");
        }

        if (knowledgeDepth < 70) {
            suggestions.add("【知识深度】专业深度不足（<70分），建议：");
            suggestions.add("  - 增加专业术语和概念的深入阐述");
            suggestions.add("  - 提供更多理论依据和学术背景");
            suggestions.add("  - 展示对专业领域的深入理解");
        }

        if (innovation < 65) {
            suggestions.add("【创新性】观点新颖性不足（<65分），建议：");
            suggestions.add("  - 提出独特的见解和批判性思考");
            suggestions.add("  - 尝试从新角度分析问题");
            suggestions.add("  - 挑战现有观点或提出改进方案");
        }

        if (empiricalEvidence < 60) {
            suggestions.add("【数据实证】数据支撑不足（<60分），建议：");
            suggestions.add("  - 增加实验数据、统计数据或案例研究");
            suggestions.add("  - 用具体数字和事实支持论点");
            suggestions.add("  - 引用权威研究和实证结果");
        }

        // 综合优化建议
        suggestions.add("\n=== 综合优化建议 ===");
        
        if (score >= 70) {
            suggestions.add("【高匹配度】文本高度符合ChatGPT特征，建议全面改写：");
            suggestions.add("  1. 打破固定的表达模式和句式结构");
            suggestions.add("  2. 增加专业深度和独特见解");
            suggestions.add("  3. 使用更多实证数据和具体案例");
            suggestions.add("  4. 融入个人经验和独特观点");
            suggestions.add("  5. 避免使用ChatGPT的标志性表达");
        } else if (score >= 50) {
            suggestions.add("【较高匹配度】文本部分符合ChatGPT特征，建议重点优化：");
            suggestions.add("  1. 减少模板化的开头和结尾");
            suggestions.add("  2. 降低过渡词的使用频率");
            suggestions.add("  3. 增加知识深度和创新性");
            suggestions.add("  4. 强化数据和实证支撑");
        } else if (score >= 30) {
            suggestions.add("【中等匹配度】文本存在ChatGPT痕迹，建议适度调整：");
            suggestions.add("  1. 检查是否有过于流畅的表达");
            suggestions.add("  2. 增加个人化的语言风格");
            suggestions.add("  3. 补充更多专业内容和数据");
        }

        return suggestions;
    }

    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore) {
        return generateSuggestions(content, matchScore.doubleValue());
    }

    /**
     * 生成优化建议（double参数版本，内部辅助方法）
     * @param text 待分析文本
     * @param score 匹配得分
     * @return 优化建议列表
     */
    public List<String> generateSuggestions(String text, double score) {
        List<String> suggestions = generateSuggestions(text);
        
        // 根据得分添加优先级标记
        if (score >= 70) {
            suggestions.add(0, "【检测结果】匹配度：" + String.format("%.1f", score) + "% - 高度疑似ChatGPT生成");
        } else if (score >= 50) {
            suggestions.add(0, "【检测结果】匹配度：" + String.format("%.1f", score) + "% - 较高疑似ChatGPT生成");
        } else if (score >= 30) {
            suggestions.add(0, "【检测结果】匹配度：" + String.format("%.1f", score) + "% - 中等疑似ChatGPT生成");
        } else {
            suggestions.add(0, "【检测结果】匹配度：" + String.format("%.1f", score) + "% - 低疑似ChatGPT生成");
        }
        
        return suggestions;
    }
}
