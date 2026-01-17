package com.ruoyi.web.service.paper.library;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kimi AI模型特征库
 * 特点：注重文本连贯性、代词回指、段落衔接、复杂句式
 * 
 * @author ruoyi
 */
public class KimiFeatureLibrary {
    
    /**
     * 连贯性标志词库 (权重: 18%)
     * Kimi注重文本流畅性和上下文衔接
     */
    public static final Set<String> KIMI_COHERENCE_MARKERS = new HashSet<>(Arrays.asList(
        "由此可见", "基于此", "据此", "在此基础上", "承上所述",
        "综上可知", "由前文可知", "如前所述", "正如上文所提", "延续前文",
        "接续上述观点", "顺着这一思路", "沿着这个方向", "循着这条线索",
        "紧接着", "紧随其后", "随后", "继而", "进而", "由此",
        "承接上文", "呼应前文", "与前文呼应", "照应前述"
    ));
    
    /**
     * 代词回指模式库 (权重: 16%)
     * Kimi善用代词建立段落间联系
     */
    public static final Set<String> KIMI_PRONOUN_PATTERNS = new HashSet<>(Arrays.asList(
        "这一观点", "该观点", "此观点", "上述观点", "前述观点",
        "这一问题", "该问题", "此问题", "上述问题", "前述问题",
        "这一现象", "该现象", "此现象", "上述现象", "前述现象",
        "这一理论", "该理论", "此理论", "上述理论", "前述理论",
        "这些因素", "这些特征", "这些条件", "这些要素", "这些方面",
        "上述内容", "前述内容", "以上内容", "如上所述", "正如上述",
        "这种情况", "此种情形", "该种状态", "这类问题", "此类现象"
    ));
    
    /**
     * 段落衔接词库 (权重: 15%)
     * Kimi擅长使用过渡性词汇
     */
    public static final Set<String> KIMI_TRANSITION_WORDS = new HashSet<>(Arrays.asList(
        "不仅如此", "更进一步", "进一步而言", "更深层次地看",
        "从另一个角度", "换个角度看", "换言之", "也就是说", "用另一种方式表达",
        "与此同时", "在此期间", "在这个过程中", "在这一阶段",
        "相应地", "相对应地", "对应地", "相比之下", "对比来看",
        "总的来说", "总体而言", "整体上看", "宏观来看", "微观来看",
        "具体来说", "详细而言", "深入分析", "细致剖析"
    ));
    
    /**
     * 长句式特征库 (权重: 14%)
     * Kimi倾向使用复杂句式，包含多个分句
     */
    public static final List<Pattern> KIMI_COMPLEX_SENTENCE_PATTERNS = Arrays.asList(
        Pattern.compile("[^。]*?，[^，。]*?，[^，。]*?，[^。]*?。"),  // 三个逗号的长句
        Pattern.compile("[^。]{80,}。"),  // 80字以上的单句
        Pattern.compile("[^。]*?虽然[^。]*?但是[^。]*?。"),  // 转折复句
        Pattern.compile("[^。]*?不仅[^。]*?而且[^。]*?。"),  // 递进复句
        Pattern.compile("[^。]*?如果[^。]*?那么[^。]*?。"),  // 假设复句
        Pattern.compile("[^。]*?因为[^。]*?所以[^。]*?。"),  // 因果复句
        Pattern.compile("[^。]*?一方面[^。]*?另一方面[^。]*?。"),  // 并列复句
        Pattern.compile("[^。]*?既[^。]*?又[^。]*?。")  // 并列复句
    );
    
    /**
     * 逻辑层次词库 (权重: 12%)
     * Kimi善于构建多层次的逻辑结构
     */
    public static final Set<String> KIMI_LOGIC_LAYERS = new HashSet<>(Arrays.asList(
        "首先", "其次", "再次", "最后", "第一", "第二", "第三",
        "一方面", "另一方面", "此外", "除此之外", "另外",
        "主要体现在", "具体表现为", "集中体现在", "突出表现在",
        "从宏观层面", "从微观层面", "从理论层面", "从实践层面",
        "在表层", "在深层", "在浅层次", "在深层次", "更深层次"
    ));
    
    /**
     * 引用整合模式 (权重: 10%)
     * Kimi善于整合多个引用来源
     */
    public static final List<Pattern> KIMI_CITATION_INTEGRATION = Arrays.asList(
        Pattern.compile("根据[^，。]*?的研究.*?同时.*?也[指出|认为|表明]"),
        Pattern.compile("学者[^，。]*?指出.*?而[^，。]*?则认为"),
        Pattern.compile("[^，。]*?等人[的研究]*?.*?[^，。]*?等[也|则|同样]"),
        Pattern.compile("多项研究.*?表明"),
        Pattern.compile("综合.*?的观点"),
        Pattern.compile("结合.*?和.*?的理论")
    );
    
    /**
     * 递进论述模式 (权重: 8%)
     * Kimi倾向逐步深入论述
     */
    public static final Set<String> KIMI_PROGRESSIVE_MARKERS = new HashSet<>(Arrays.asList(
        "进而", "从而", "由此", "因而", "继而",
        "进一步", "更进一步", "深入分析", "深化理解",
        "更为重要的是", "更关键的是", "更核心的问题在于",
        "不仅仅是", "不单单是", "不只是", "不限于",
        "在更大程度上", "在更广范围内", "在更深层次上"
    ));
    
    /**
     * 总结归纳词库 (权重: 7%)
     * Kimi注重阶段性总结
     */
    public static final Set<String> KIMI_SUMMARY_MARKERS = new HashSet<>(Arrays.asList(
        "综上所述", "总而言之", "概括来说", "归纳而言",
        "由此可见", "由上可知", "基于以上分析", "通过以上论述",
        "总结以上观点", "综合上述内容", "汇总前文所述",
        "简言之", "简而言之", "简单来说", "简要概括"
    ));
    
    /**
     * 句间关联词 (权重: 5%)
     * Kimi强调句子之间的关联
     */
    public static final Set<String> KIMI_SENTENCE_CONNECTORS = new HashSet<>(Arrays.asList(
        "这意味着", "这表明", "这说明", "这显示",
        "由此可见", "可见", "可以看出", "不难发现",
        "值得注意的是", "需要指出的是", "应当强调的是",
        "这就解释了", "这就揭示了", "这就证明了"
    ));
    
    /**
     * 段落展开模式 (权重: 5%)
     * Kimi的段落展开特点
     */
    public static final List<Pattern> KIMI_PARAGRAPH_PATTERNS = Arrays.asList(
        Pattern.compile("^[^。]*?提出.*?[^。]*?分析.*?[^。]*?得出"),  // 提出-分析-结论
        Pattern.compile("^[^。]*?现象.*?[^。]*?原因.*?[^。]*?对策"),  // 现象-原因-对策
        Pattern.compile("^[^。]*?问题.*?[^。]*?影响.*?[^。]*?建议"),  // 问题-影响-建议
        Pattern.compile("^从.*?来看.*?[^。]*?从.*?角度.*?[^。]*?综合而言")  // 多角度分析
    );
    
    /**
     * 计算Kimi特征得分
     * 
     * @param text 待检测文本
     * @return Kimi特征得分 (0-100)
     */
    public static double calculateKimiScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int textLength = text.length();
        
        // 1. 连贯性标志 (18%)
        double coherenceScore = 0.0;
        for (String marker : KIMI_COHERENCE_MARKERS) {
            int count = countOccurrences(text, marker);
            coherenceScore += count;
        }
        coherenceScore = normalize(coherenceScore, textLength, 1000) * 18.0;
        totalScore += coherenceScore;
        
        // 2. 代词回指 (16%)
        double pronounScore = 0.0;
        for (String pattern : KIMI_PRONOUN_PATTERNS) {
            int count = countOccurrences(text, pattern);
            pronounScore += count;
        }
        pronounScore = normalize(pronounScore, textLength, 1000) * 16.0;
        totalScore += pronounScore;
        
        // 3. 段落衔接 (15%)
        double transitionScore = 0.0;
        for (String word : KIMI_TRANSITION_WORDS) {
            int count = countOccurrences(text, word);
            transitionScore += count;
        }
        transitionScore = normalize(transitionScore, textLength, 1000) * 15.0;
        totalScore += transitionScore;
        
        // 4. 长句式特征 (14%)
        double complexSentenceScore = 0.0;
        for (Pattern pattern : KIMI_COMPLEX_SENTENCE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                complexSentenceScore += 1.0;
            }
        }
        complexSentenceScore = normalize(complexSentenceScore, textLength, 2000) * 14.0;
        totalScore += complexSentenceScore;
        
        // 5. 逻辑层次 (12%)
        double logicScore = 0.0;
        for (String layer : KIMI_LOGIC_LAYERS) {
            int count = countOccurrences(text, layer);
            logicScore += count;
        }
        logicScore = normalize(logicScore, textLength, 1000) * 12.0;
        totalScore += logicScore;
        
        // 6. 引用整合 (10%)
        double citationScore = 0.0;
        for (Pattern pattern : KIMI_CITATION_INTEGRATION) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                citationScore += 1.0;
            }
        }
        citationScore = normalize(citationScore, textLength, 2000) * 10.0;
        totalScore += citationScore;
        
        // 7. 递进论述 (8%)
        double progressiveScore = 0.0;
        for (String marker : KIMI_PROGRESSIVE_MARKERS) {
            int count = countOccurrences(text, marker);
            progressiveScore += count;
        }
        progressiveScore = normalize(progressiveScore, textLength, 1000) * 8.0;
        totalScore += progressiveScore;
        
        // 8. 总结归纳 (7%)
        double summaryScore = 0.0;
        for (String marker : KIMI_SUMMARY_MARKERS) {
            int count = countOccurrences(text, marker);
            summaryScore += count;
        }
        summaryScore = normalize(summaryScore, textLength, 1500) * 7.0;
        totalScore += summaryScore;
        
        // 9. 句间关联 (5%)
        double connectorScore = 0.0;
        for (String connector : KIMI_SENTENCE_CONNECTORS) {
            int count = countOccurrences(text, connector);
            connectorScore += count;
        }
        connectorScore = normalize(connectorScore, textLength, 1500) * 5.0;
        totalScore += connectorScore;
        
        // 10. 段落展开 (5%)
        double paragraphScore = 0.0;
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            for (Pattern pattern : KIMI_PARAGRAPH_PATTERNS) {
                if (pattern.matcher(para).find()) {
                    paragraphScore += 1.0;
                }
            }
        }
        paragraphScore = normalize(paragraphScore, paragraphs.length, 2) * 5.0;
        totalScore += paragraphScore;
        
        return Math.min(100.0, totalScore);
    }
    
    /**
     * 获取详细的特征统计信息
     * 
     * @param text 待检测文本
     * @return 特征详情Map
     */
    public static Map<String, Object> getFeatureDetails(String text) {
        Map<String, Object> details = new HashMap<>();
        
        if (text == null || text.trim().isEmpty()) {
            return details;
        }
        
        int textLength = text.length();
        
        // 连贯性标志统计
        int coherenceCount = 0;
        for (String marker : KIMI_COHERENCE_MARKERS) {
            coherenceCount += countOccurrences(text, marker);
        }
        details.put("coherenceMarkers", coherenceCount);
        details.put("coherenceScore", normalize(coherenceCount, textLength, 1000) * 18.0);
        
        // 代词回指统计
        int pronounCount = 0;
        for (String pattern : KIMI_PRONOUN_PATTERNS) {
            pronounCount += countOccurrences(text, pattern);
        }
        details.put("pronounPatterns", pronounCount);
        details.put("pronounScore", normalize(pronounCount, textLength, 1000) * 16.0);
        
        // 段落衔接统计
        int transitionCount = 0;
        for (String word : KIMI_TRANSITION_WORDS) {
            transitionCount += countOccurrences(text, word);
        }
        details.put("transitionWords", transitionCount);
        details.put("transitionScore", normalize(transitionCount, textLength, 1000) * 15.0);
        
        // 长句式统计
        int complexSentenceCount = 0;
        for (Pattern pattern : KIMI_COMPLEX_SENTENCE_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                complexSentenceCount++;
            }
        }
        details.put("complexSentences", complexSentenceCount);
        details.put("complexSentenceScore", normalize(complexSentenceCount, textLength, 2000) * 14.0);
        
        // 逻辑层次统计
        int logicCount = 0;
        for (String layer : KIMI_LOGIC_LAYERS) {
            logicCount += countOccurrences(text, layer);
        }
        details.put("logicLayers", logicCount);
        details.put("logicScore", normalize(logicCount, textLength, 1000) * 12.0);
        
        // 引用整合统计
        int citationCount = 0;
        for (Pattern pattern : KIMI_CITATION_INTEGRATION) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                citationCount++;
            }
        }
        details.put("citationIntegrations", citationCount);
        details.put("citationScore", normalize(citationCount, textLength, 2000) * 10.0);
        
        // 递进论述统计
        int progressiveCount = 0;
        for (String marker : KIMI_PROGRESSIVE_MARKERS) {
            progressiveCount += countOccurrences(text, marker);
        }
        details.put("progressiveMarkers", progressiveCount);
        details.put("progressiveScore", normalize(progressiveCount, textLength, 1000) * 8.0);
        
        // 总结归纳统计
        int summaryCount = 0;
        for (String marker : KIMI_SUMMARY_MARKERS) {
            summaryCount += countOccurrences(text, marker);
        }
        details.put("summaryMarkers", summaryCount);
        details.put("summaryScore", normalize(summaryCount, textLength, 1500) * 7.0);
        
        // 句间关联统计
        int connectorCount = 0;
        for (String connector : KIMI_SENTENCE_CONNECTORS) {
            connectorCount += countOccurrences(text, connector);
        }
        details.put("sentenceConnectors", connectorCount);
        details.put("connectorScore", normalize(connectorCount, textLength, 1500) * 5.0);
        
        // 段落展开统计
        int paragraphPatternCount = 0;
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            for (Pattern pattern : KIMI_PARAGRAPH_PATTERNS) {
                if (pattern.matcher(para).find()) {
                    paragraphPatternCount++;
                }
            }
        }
        details.put("paragraphPatterns", paragraphPatternCount);
        details.put("paragraphScore", normalize(paragraphPatternCount, paragraphs.length, 2) * 5.0);
        
        // 总分
        details.put("totalScore", calculateKimiScore(text));
        
        return details;
    }
    
    /**
     * 统计字符串在文本中出现的次数
     */
    private static int countOccurrences(String text, String target) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(target, index)) != -1) {
            count++;
            index += target.length();
        }
        return count;
    }
    
    /**
     * 归一化函数
     * 将计数值归一化到0-1之间
     * 
     * @param count 特征计数
     * @param textLength 文本长度
     * @param baseline 基准长度(每N个字符预期出现1次)
     * @return 归一化后的得分(0-1)
     */
    private static double normalize(double count, int textLength, int baseline) {
        if (textLength == 0) {
            return 0.0;
        }
        double expected = (double) textLength / baseline;
        double ratio = count / Math.max(expected, 0.1);
        return Math.min(1.0, ratio);
    }
}
