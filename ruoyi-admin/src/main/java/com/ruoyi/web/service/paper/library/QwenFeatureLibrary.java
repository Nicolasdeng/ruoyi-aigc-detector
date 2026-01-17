package com.ruoyi.web.service.paper.library;

import java.util.*;

/**
 * 千问AI模型特征库
 * 
 * 千问特征：
 * 1. 多角度论述：习惯从多个视角分析问题
 * 2. 引用丰富：频繁引用文献、数据来源
 * 3. 列举详细：使用列表、枚举方式组织内容
 * 4. 数据驱动：强调数据支撑和量化分析
 * 
 * @author ruoyi
 */
public class QwenFeatureLibrary {
    
    /**
     * 千问多角度论述标志库（权重：0.18）
     */
    public static final Set<String> QWEN_MULTI_PERSPECTIVE_MARKERS = new HashSet<>(Arrays.asList(
        // 视角切换
        "从多个角度看", "从不同角度分析", "多维度考察",
        "从理论角度", "从实践角度", "从历史角度",
        "从现实角度", "从未来角度", "从宏观角度",
        "从微观角度", "从整体角度", "从局部角度",
        
        // 对比分析
        "对比来看", "相比之下", "比较而言", "对照分析",
        "横向比较", "纵向比较", "交叉对比",
        
        // 层次分析
        "深层次分析", "浅层次看", "表面上看", "实质上",
        "本质上", "根本上", "深入探讨", "进一步分析"
    ));
    
    /**
     * 千问引用来源模式库（权重：0.20）
     */
    public static final Set<String> QWEN_CITATION_SOURCES = new HashSet<>(Arrays.asList(
        // 学术引用
        "根据研究", "根据调查", "根据统计", "根据报告",
        "研究显示", "调查表明", "统计数据显示", "报告指出",
        "文献记载", "文献表明", "学术研究指出",
        
        // 权威来源
        "专家指出", "学者认为", "权威数据显示",
        "官方数据", "官方报告", "权威机构",
        
        // 实证引用
        "实验证明", "实践表明", "案例显示", "事实证明",
        "历史数据", "历史经验", "实际情况",
        
        // 多来源对比
        "多项研究表明", "众多学者认为", "大量数据显示",
        "多方面证据", "综合多种来源"
    ));
    
    /**
     * 千问列举式表达库（权重：0.15）
     */
    public static final Set<String> QWEN_ENUMERATION_EXPRESSIONS = new HashSet<>(Arrays.asList(
        // 列举标志
        "主要体现在", "具体表现为", "主要包括", "具体包括",
        "体现在以下几个方面", "表现在以下方面",
        "可以归纳为", "可以总结为", "可以概括为",
        
        // 分点说明
        "其一", "其二", "其三", "其四",
        "第一点", "第二点", "第三点",
        "一是", "二是", "三是", "四是",
        
        // 详细展开
        "详细而言", "具体来讲", "分别是", "分别为",
        "依次为", "按顺序", "逐一分析"
    ));
    
    /**
     * 千问数据引用特征库（权重：0.15）
     */
    public static final Set<String> QWEN_DATA_REFERENCES = new HashSet<>(Arrays.asList(
        // 数据来源
        "数据来源于", "数据表明", "数据显示", "数据证实",
        "统计显示", "统计表明", "统计结果", "调查数据",
        
        // 量化表达
        "占比", "比例为", "达到", "约为", "超过", "不足",
        "增长率", "下降率", "平均值", "中位数",
        
        // 趋势描述
        "呈现上升趋势", "呈现下降趋势", "保持稳定",
        "波动变化", "显著增长", "明显下降",
        
        // 数据对比
        "相比去年", "同比增长", "环比下降",
        "与之相比", "数据对比显示"
    ));
    
    /**
     * 千问逻辑推理标志库（权重：0.12）
     */
    public static final Set<String> QWEN_LOGIC_REASONING = new HashSet<>(Arrays.asList(
        // 因果推理
        "原因在于", "导致了", "引发了", "造成了",
        "由此导致", "因而", "从而", "以致",
        
        // 条件推理
        "前提是", "基础是", "关键在于", "核心是",
        "只有", "只要", "必须", "需要",
        
        // 结果推导
        "由此可知", "因此得出", "推断出", "判断为",
        "可以推论", "据此推测", "由此推断",
        
        // 逻辑关联
        "相关性", "关联度", "因果关系", "必然联系",
        "内在联系", "逻辑链条"
    ));
    
    /**
     * 千问分类组织词库（权重：0.10）
     */
    public static final Set<String> QWEN_CLASSIFICATION_MARKERS = new HashSet<>(Arrays.asList(
        // 分类标志
        "可分为", "分为", "划分为", "归类为",
        "分成", "分别是", "包含", "涵盖",
        
        // 类别说明
        "第一类", "第二类", "第三类",
        "A类", "B类", "C类",
        "主要类型", "次要类型", "特殊类型",
        
        // 层级结构
        "上层", "中层", "下层", "顶层", "底层",
        "一级", "二级", "三级"
    ));
    
    /**
     * 千问总结归纳词库（权重：0.08）
     */
    public static final Set<String> QWEN_SUMMARY_MARKERS = new HashSet<>(Arrays.asList(
        "总结来看", "归纳而言", "概括来说", "总结如下",
        "综合以上", "综合上述", "总体来看", "整体而言",
        "归纳起来", "总结起来", "概括起来",
        "最终可以总结", "可以归纳出", "可以概括为"
    ));
    
    /**
     * 千问问题提出模式（权重：0.07）
     */
    public static final Set<String> QWEN_QUESTION_PATTERNS = new HashSet<>(Arrays.asList(
        "问题是", "关键问题", "核心问题", "主要问题",
        "值得注意的问题", "需要解决的问题",
        "如何", "怎样", "为什么", "是什么",
        "在什么情况下", "什么条件下", "什么原因"
    ));
    
    /**
     * 千问强调标志词库（权重：0.05）
     */
    public static final Set<String> QWEN_EMPHASIS_MARKERS = new HashSet<>(Arrays.asList(
        "尤其是", "特别是", "尤为重要", "格外重要",
        "值得强调", "需要注意", "必须指出", "应当重视",
        "关键在于", "核心在于", "重点在于"
    ));
    
    /**
     * 千问引用格式模式（权重：0.05）
     */
    public static final List<String> QWEN_CITATION_PATTERNS = Arrays.asList(
        "\\([\\d]{4}[a-z]?\\)", // (2023a)
        "（[\\d]{4}）", // （2023）
        "\\[\\d+[,-]?\\d*\\]", // [1] [1-3] [1,2,3]
        "[，。][\\u4e00-\\u9fa5]{2,4}等人?[，。（\\(][\\d]{4}", // ，张三等人(2023
        "见[\\u4e00-\\u9fa5]{2,4}等?[，。（\\(][\\d]{4}" // 见李四等，2023
    );
    
    /**
     * 千问特有句式模板（权重：0.05）
     */
    public static final Set<String> QWEN_SENTENCE_TEMPLATES = new HashSet<>(Arrays.asList(
        // "通过...发现..."结构
        "通过.*发现", "通过.*得出", "经过.*表明",
        
        // "在...方面"结构
        "在.*方面", "在.*领域", "在.*范围",
        
        // "基于...分析"结构
        "基于.*分析", "基于.*研究", "基于.*考察",
        
        // "从...到..."结构
        "从.*到", "由.*至", "自.*始"
    ));
    
    /**
     * 计算千问特征匹配度
     * 
     * @param text 待分析文本
     * @return 匹配度分数 (0-100)
     */
    public static double calculateQwenScore(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // 1. 多角度论述标志（权重18%）
        score += calculateFeatureFrequency(text, QWEN_MULTI_PERSPECTIVE_MARKERS) * 0.18;
        
        // 2. 引用来源模式（权重20%）
        score += calculateFeatureFrequency(text, QWEN_CITATION_SOURCES) * 0.20;
        
        // 3. 列举式表达（权重15%）
        score += calculateFeatureFrequency(text, QWEN_ENUMERATION_EXPRESSIONS) * 0.15;
        
        // 4. 数据引用特征（权重15%）
        score += calculateFeatureFrequency(text, QWEN_DATA_REFERENCES) * 0.15;
        
        // 5. 逻辑推理标志（权重12%）
        score += calculateFeatureFrequency(text, QWEN_LOGIC_REASONING) * 0.12;
        
        // 6. 分类组织词（权重10%）
        score += calculateFeatureFrequency(text, QWEN_CLASSIFICATION_MARKERS) * 0.10;
        
        // 7. 总结归纳词（权重8%）
        score += calculateFeatureFrequency(text, QWEN_SUMMARY_MARKERS) * 0.08;
        
        // 8. 问题提出模式（权重7%）
        score += calculateFeatureFrequency(text, QWEN_QUESTION_PATTERNS) * 0.07;
        
        // 9. 强调标志词（权重5%）
        score += calculateFeatureFrequency(text, QWEN_EMPHASIS_MARKERS) * 0.05;
        
        // 10. 引用格式（权重5%）
        score += calculateCitationPatternMatch(text, QWEN_CITATION_PATTERNS) * 0.05;
        
        // 11. 句式模板（权重5%）
        score += calculateTemplateMatch(text, QWEN_SENTENCE_TEMPLATES) * 0.05;
        
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
        
        // 归一化：每800字出现1次引用得25分
        return Math.min((matchCount * 800.0 / text.length()) * 25, 100.0);
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
        
        // 归一化：每600字出现1次模板得12分
        return Math.min((matchCount * 600.0 / text.length()) * 12, 100.0);
    }
    
    /**
     * 获取千问特征详情
     */
    public static Map<String, Object> getFeatureDetails(String text) {
        Map<String, Object> details = new HashMap<>();
        
        details.put("multiPerspectiveMarkers", countMatches(text, QWEN_MULTI_PERSPECTIVE_MARKERS));
        details.put("citationSources", countMatches(text, QWEN_CITATION_SOURCES));
        details.put("enumerationExpressions", countMatches(text, QWEN_ENUMERATION_EXPRESSIONS));
        details.put("dataReferences", countMatches(text, QWEN_DATA_REFERENCES));
        details.put("logicReasoning", countMatches(text, QWEN_LOGIC_REASONING));
        details.put("classificationMarkers", countMatches(text, QWEN_CLASSIFICATION_MARKERS));
        details.put("summaryMarkers", countMatches(text, QWEN_SUMMARY_MARKERS));
        details.put("questionPatterns", countMatches(text, QWEN_QUESTION_PATTERNS));
        details.put("emphasisMarkers", countMatches(text, QWEN_EMPHASIS_MARKERS));
        details.put("citations", countCitations(text, QWEN_CITATION_PATTERNS));
        details.put("templates", countTemplates(text, QWEN_SENTENCE_TEMPLATES));
        
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
