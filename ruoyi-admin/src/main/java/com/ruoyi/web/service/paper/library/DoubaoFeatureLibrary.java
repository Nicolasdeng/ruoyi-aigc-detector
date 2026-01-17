package com.ruoyi.web.service.paper.library;

import java.util.*;

/**
 * 豆包AI模型特征库
 * 
 * 豆包特征：
 * 1. 结构化强：习惯使用"首先、其次、再次、最后"等层次化表达
 * 2. 学术规范：频繁使用"研究表明"、"据统计"等学术引用
 * 3. 转折明显：大量使用转折连词，逻辑清晰
 * 4. 总结性强：段落结尾常有小结性语句
 * 
 * @author ruoyi
 */
public class DoubaoFeatureLibrary {
    
    /**
     * 豆包高频转折词库（权重：0.15）
     */
    public static final Set<String> DOUBAO_TRANSITION_WORDS = new HashSet<>(Arrays.asList(
        // 层次化标志
        "首先", "其次", "再次", "最后", "第一", "第二", "第三",
        "首要的是", "其次是", "再者", "最终", "总之",
        
        // 强转折
        "然而", "但是", "不过", "可是", "却", "反之",
        "与此相反", "相反地", "恰恰相反",
        
        // 递进关系
        "而且", "并且", "同时", "此外", "另外", "除此之外",
        "不仅如此", "更重要的是", "更为关键的是",
        
        // 因果关系
        "因此", "所以", "由此可见", "综上所述", "总而言之",
        "基于此", "鉴于此", "正因如此"
    ));
    
    /**
     * 豆包学术规范短语库（权重：0.20）
     */
    public static final Set<String> DOUBAO_ACADEMIC_PHRASES = new HashSet<>(Arrays.asList(
        // 引用类
        "研究表明", "研究发现", "研究指出", "研究证实",
        "据统计", "据调查", "据报道", "据文献显示",
        "根据相关文献", "根据以往研究", "根据学者研究",
        
        // 观点类
        "学术界普遍认为", "学者们指出", "专家认为",
        "理论上认为", "从理论角度来看", "理论研究表明",
        
        // 分析类
        "综合分析", "深入分析", "系统分析", "全面分析",
        "对比分析", "实证分析", "定量分析", "定性分析",
        
        // 结论类
        "由此得出", "可以得出结论", "得出以下结论",
        "综合来看", "总体而言", "整体来说"
    ));
    
    /**
     * 豆包段落开头模式库（权重：0.12）
     */
    public static final Set<String> DOUBAO_PARAGRAPH_STARTERS = new HashSet<>(Arrays.asList(
        // 引入话题
        "关于", "针对", "对于", "就", "谈到",
        "在探讨", "在分析", "在研究", "在考察",
        
        // 提出观点
        "值得注意的是", "需要指出的是", "应当强调的是",
        "不可忽视的是", "显而易见的是", "毋庸置疑的是",
        
        // 承上启下
        "基于上述分析", "在此基础上", "由此延伸",
        "进一步来看", "更深层次地", "从另一个角度",
        
        // 具体论述
        "具体而言", "详细来说", "换言之", "也就是说",
        "更准确地说", "更具体地", "进一步说明"
    ));
    
    /**
     * 豆包结论标志词库（权重：0.10）
     */
    public static final Set<String> DOUBAO_CONCLUSION_MARKERS = new HashSet<>(Arrays.asList(
        "综上所述", "总之", "总而言之", "综合来看",
        "综上", "总的来说", "整体而言", "全面来看",
        "由此可见", "因此可以得出", "可以看出",
        "通过以上分析", "经过上述论证", "基于以上讨论",
        "最终得出", "归纳而言", "概括来说"
    ));
    
    /**
     * 豆包逻辑连接词库（权重：0.08）
     */
    public static final Set<String> DOUBAO_LOGIC_CONNECTORS = new HashSet<>(Arrays.asList(
        // 并列
        "一方面", "另一方面", "与此同时", "同样地",
        "相似地", "类似地", "同理",
        
        // 对比
        "相比之下", "对比来看", "反观", "对照而言",
        
        // 假设
        "假如", "倘若", "如果说", "若是",
        
        // 强调
        "事实上", "实际上", "确实", "的确",
        "诚然", "毫无疑问", "无可否认"
    ));
    
    /**
     * 豆包数据表达模式（权重：0.08）
     */
    public static final Set<String> DOUBAO_DATA_PATTERNS = new HashSet<>(Arrays.asList(
        "数据显示", "数据表明", "统计数据表明",
        "调查数据显示", "根据数据", "数据分析表明",
        "实证数据表明", "量化分析显示", "定量研究表明"
    ));
    
    /**
     * 豆包引用格式模式（权重：0.07）
     */
    public static final List<String> DOUBAO_CITATION_PATTERNS = Arrays.asList(
        "\\([\\d]{4}\\)", // (2023)
        "（[\\d]{4}）", // （2023）
        "[，。]([\\u4e00-\\u9fa5]+等)?[，。]?[\\d]{4}", // ，张三等，2023
        "\\[[\\d]+\\]" // [1]
    );
    
    /**
     * 豆包特有句式模板（权重：0.10）
     */
    public static final Set<String> DOUBAO_SENTENCE_TEMPLATES = new HashSet<>(Arrays.asList(
        // "...不仅...而且..."结构
        "不仅.*而且", "不但.*而且", "既.*又",
        
        // "通过...实现..."结构
        "通过.*实现", "借助.*达到", "凭借.*获得",
        
        // "在...背景下"结构
        "在.*背景下", "在.*情况下", "在.*条件下",
        
        // "为...提供..."结构
        "为.*提供", "为.*奠定", "为.*创造"
    ));
    
    /**
     * 豆包问题引导词库（权重：0.05）
     */
    public static final Set<String> DOUBAO_QUESTION_GUIDES = new HashSet<>(Arrays.asList(
        "那么", "如何", "怎样", "为什么",
        "问题在于", "关键问题是", "核心问题是",
        "值得思考的是", "值得探讨的是", "需要明确的是"
    ));
    
    /**
     * 豆包列举标志词（权重：0.05）
     */
    public static final Set<String> DOUBAO_ENUMERATION_MARKERS = new HashSet<>(Arrays.asList(
        "包括", "涵盖", "涉及", "囊括",
        "主要有", "分别为", "具体包括",
        "例如", "比如", "诸如", "如"
    ));
    
    /**
     * 计算豆包特征匹配度
     * 
     * @param text 待分析文本
     * @return 匹配度分数 (0-100)
     */
    public static double calculateDoubaoScore(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        int textLength = text.length();
        
        // 1. 转折词频率（权重15%）
        score += calculateFeatureFrequency(text, DOUBAO_TRANSITION_WORDS) * 0.15;
        
        // 2. 学术短语频率（权重20%）
        score += calculateFeatureFrequency(text, DOUBAO_ACADEMIC_PHRASES) * 0.20;
        
        // 3. 段落开头模式（权重12%）
        score += calculateParagraphStarterFrequency(text, DOUBAO_PARAGRAPH_STARTERS) * 0.12;
        
        // 4. 结论标志词（权重10%）
        score += calculateFeatureFrequency(text, DOUBAO_CONCLUSION_MARKERS) * 0.10;
        
        // 5. 逻辑连接词（权重8%）
        score += calculateFeatureFrequency(text, DOUBAO_LOGIC_CONNECTORS) * 0.08;
        
        // 6. 数据表达模式（权重8%）
        score += calculateFeatureFrequency(text, DOUBAO_DATA_PATTERNS) * 0.08;
        
        // 7. 引用格式（权重7%）
        score += calculateCitationPatternMatch(text, DOUBAO_CITATION_PATTERNS) * 0.07;
        
        // 8. 句式模板（权重10%）
        score += calculateTemplateMatch(text, DOUBAO_SENTENCE_TEMPLATES) * 0.10;
        
        // 9. 问题引导词（权重5%）
        score += calculateFeatureFrequency(text, DOUBAO_QUESTION_GUIDES) * 0.05;
        
        // 10. 列举标志词（权重5%）
        score += calculateFeatureFrequency(text, DOUBAO_ENUMERATION_MARKERS) * 0.05;
        
        return Math.min(score, 100.0);
    }
    
    /**
     * 计算特征词频率
     */
    private static double calculateFeatureFrequency(String text, Set<String> features) {
        int count = 0;
        for (String feature : features) {
            int index = 0;
            while ((index = text.indexOf(feature, index)) != -1) {
                count++;
                index += feature.length();
            }
        }
        // 归一化：每500字出现1次特征词得10分
        return Math.min((count * 500.0 / text.length()) * 10, 100.0);
    }
    
    /**
     * 计算段落开头特征频率
     */
    private static double calculateParagraphStarterFrequency(String text, Set<String> starters) {
        String[] paragraphs = text.split("\\n+");
        int matchCount = 0;
        
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            
            for (String starter : starters) {
                if (trimmed.startsWith(starter)) {
                    matchCount++;
                    break;
                }
            }
        }
        
        // 归一化：30%的段落匹配得100分
        return Math.min((matchCount * 100.0 / (paragraphs.length * 0.3)), 100.0);
    }
    
    /**
     * 计算引用模式匹配度
     */
    private static double calculateCitationPatternMatch(String text, List<String> patterns) {
        int matchCount = 0;
        
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                matchCount++;
            }
        }
        
        // 归一化：每1000字出现1次引用得20分
        return Math.min((matchCount * 1000.0 / text.length()) * 20, 100.0);
    }
    
    /**
     * 计算句式模板匹配度
     */
    private static double calculateTemplateMatch(String text, Set<String> templates) {
        int matchCount = 0;
        
        for (String template : templates) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(template);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                matchCount++;
            }
        }
        
        // 归一化：每500字出现1次模板得15分
        return Math.min((matchCount * 500.0 / text.length()) * 15, 100.0);
    }
    
    /**
     * 获取豆包特征详情
     */
    public static Map<String, Object> getFeatureDetails(String text) {
        Map<String, Object> details = new HashMap<>();
        
        details.put("transitionWords", countMatches(text, DOUBAO_TRANSITION_WORDS));
        details.put("academicPhrases", countMatches(text, DOUBAO_ACADEMIC_PHRASES));
        details.put("paragraphStarters", countParagraphStarters(text, DOUBAO_PARAGRAPH_STARTERS));
        details.put("conclusionMarkers", countMatches(text, DOUBAO_CONCLUSION_MARKERS));
        details.put("logicConnectors", countMatches(text, DOUBAO_LOGIC_CONNECTORS));
        details.put("dataPatterns", countMatches(text, DOUBAO_DATA_PATTERNS));
        details.put("citations", countCitations(text, DOUBAO_CITATION_PATTERNS));
        details.put("templates", countTemplates(text, DOUBAO_SENTENCE_TEMPLATES));
        
        return details;
    }
    
    private static int countMatches(String text, Set<String> features) {
        int count = 0;
        for (String feature : features) {
            int index = 0;
            while ((index = text.indexOf(feature, index)) != -1) {
                count++;
                index += feature.length();
            }
        }
        return count;
    }
    
    private static int countParagraphStarters(String text, Set<String> starters) {
        String[] paragraphs = text.split("\\n+");
        int count = 0;
        
        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isEmpty()) continue;
            
            for (String starter : starters) {
                if (trimmed.startsWith(starter)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private static int countCitations(String text, List<String> patterns) {
        int count = 0;
        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                count++;
            }
        }
        return count;
    }
    
    private static int countTemplates(String text, Set<String> templates) {
        int count = 0;
        for (String template : templates) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(template);
            java.util.regex.Matcher m = p.matcher(text);
            while (m.find()) {
                count++;
            }
        }
        return count;
    }
}
