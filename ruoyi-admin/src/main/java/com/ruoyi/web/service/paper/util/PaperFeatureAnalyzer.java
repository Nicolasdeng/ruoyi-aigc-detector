package com.ruoyi.web.service.paper.util;

import java.util.*;
import java.util.regex.*;

/**
 * 论文特征分析工具类
 * 专门用于分析学术论文的各项特征，辅助AI生成检测
 * 
 * @author ruoyi
 */
public class PaperFeatureAnalyzer {

    /**
     * 分析学术规范性特征
     * 检测术语使用、引用格式、学术用语等
     * 
     * @param content 论文内容
     * @return 规范性得分 (0-100)
     */
    public static double analyzeAcademicFormality(String content) {
        double score = 50.0; // 基准分
        
        // 1. 学术术语检测
        String[] academicTerms = {
            "研究", "分析", "探讨", "表明", "证明", "假设", "理论",
            "方法", "结果", "结论", "数据", "实验", "模型", "框架",
            "文献", "综述", "概念", "定义", "指标", "评估", "验证"
        };
        int termCount = 0;
        for (String term : academicTerms) {
            termCount += countOccurrences(content, term);
        }
        double termDensity = (double) termCount / content.length() * 1000;
        score += Math.min(termDensity * 2, 20); // 最多+20分
        
        // 2. 引用格式检测
        int citationCount = 0;
        Pattern citationPattern = Pattern.compile("\\[[0-9,\\-]+\\]|（[^）]*[0-9]{4}[^）]*）");
        Matcher matcher = citationPattern.matcher(content);
        while (matcher.find()) {
            citationCount++;
        }
        score += Math.min(citationCount * 0.5, 15); // 最多+15分
        
        // 3. 口语化检测（负面特征）
        String[] colloquialPhrases = {
            "我觉得", "我认为", "可能吧", "大概", "应该是",
            "差不多", "基本上", "其实", "说实话", "老实说"
        };
        int colloquialCount = 0;
        for (String phrase : colloquialPhrases) {
            colloquialCount += countOccurrences(content, phrase);
        }
        score -= colloquialCount * 2; // 每个口语化表达-2分
        
        // 4. 学术连接词检测
        String[] academicConnectors = {
            "因此", "然而", "此外", "综上所述", "由此可见",
            "基于", "根据", "通过", "利用", "采用"
        };
        int connectorCount = 0;
        for (String connector : academicConnectors) {
            connectorCount += countOccurrences(content, connector);
        }
        score += Math.min(connectorCount * 0.3, 15); // 最多+15分
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析论证结构特征
     * 检测论点-论据关系、逻辑链完整性等
     * 
     * @param content 论文内容
     * @return 论证结构得分 (0-100)
     */
    public static double analyzeArgumentationStructure(String content) {
        double score = 50.0;
        
        // 1. 论证关键词检测
        String[] argumentKeywords = {
            "首先", "其次", "再次", "最后", "第一", "第二", "第三",
            "一方面", "另一方面", "总之", "综上", "总体而言"
        };
        int structureCount = 0;
        for (String keyword : argumentKeywords) {
            structureCount += countOccurrences(content, keyword);
        }
        
        // AI生成的论文往往使用过多结构词
        if (structureCount > content.length() / 200) {
            score -= 15; // 结构词过多，可能是AI生成
        } else if (structureCount > content.length() / 500) {
            score += 10; // 适当使用结构词
        }
        
        // 2. 因果关系检测
        String[] causalWords = {
            "因为", "所以", "由于", "导致", "造成", "引起",
            "因此", "从而", "以致", "由此"
        };
        int causalCount = 0;
        for (String word : causalWords) {
            causalCount += countOccurrences(content, word);
        }
        score += Math.min(causalCount * 0.4, 15);
        
        // 3. 转折与对比
        String[] contrastWords = {
            "但是", "然而", "相反", "不同的是", "与此相对",
            "尽管", "虽然", "即使", "对比", "比较"
        };
        int contrastCount = 0;
        for (String word : contrastWords) {
            contrastCount += countOccurrences(content, word);
        }
        score += Math.min(contrastCount * 0.5, 15);
        
        // 4. 段落结构分析
        String[] paragraphs = content.split("\n\n+");
        if (paragraphs.length > 0) {
            double avgLength = (double) content.length() / paragraphs.length;
            // 段落长度适中（200-500字）得分高
            if (avgLength >= 200 && avgLength <= 500) {
                score += 10;
            } else if (avgLength < 100 || avgLength > 800) {
                score -= 10; // 段落过短或过长
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析知识深度特征
     * 检测专业术语密度、概念阐述深度等
     * 
     * @param content 论文内容
     * @return 知识深度得分 (0-100)
     */
    public static double analyzeKnowledgeDepth(String content) {
        double score = 50.0;
        
        // 1. 专业术语密度
        // 检测长词（可能是专业术语）
        Pattern longWordPattern = Pattern.compile("[\\u4e00-\\u9fa5]{5,}");
        Matcher matcher = longWordPattern.matcher(content);
        int longWordCount = 0;
        while (matcher.find()) {
            longWordCount++;
        }
        double longWordDensity = (double) longWordCount / content.length() * 1000;
        score += Math.min(longWordDensity * 3, 20);
        
        // 2. 定义和解释
        String[] definitionPhrases = {
            "定义为", "是指", "即", "指的是", "称为", "被定义为",
            "可以理解为", "通常认为", "学术界认为"
        };
        int definitionCount = 0;
        for (String phrase : definitionPhrases) {
            definitionCount += countOccurrences(content, phrase);
        }
        score += Math.min(definitionCount * 1.5, 15);
        
        // 3. 数据和统计
        Pattern numberPattern = Pattern.compile("\\d+\\.\\d+%?|\\d+%");
        matcher = numberPattern.matcher(content);
        int numberCount = 0;
        while (matcher.find()) {
            numberCount++;
        }
        score += Math.min(numberCount * 0.5, 10);
        
        // 4. 理论框架
        String[] theoryKeywords = {
            "理论", "模型", "框架", "体系", "范式", "视角",
            "路径", "机制", "原理", "规律"
        };
        int theoryCount = 0;
        for (String keyword : theoryKeywords) {
            theoryCount += countOccurrences(content, keyword);
        }
        score += Math.min(theoryCount * 0.6, 15);
        
        // 5. 深度分析词汇
        String[] depthWords = {
            "深入", "详细", "系统", "全面", "综合", "深刻",
            "本质", "内在", "根本", "核心"
        };
        int depthCount = 0;
        for (String word : depthWords) {
            depthCount += countOccurrences(content, word);
        }
        
        // AI生成可能过度使用这些词
        if (depthCount > content.length() / 150) {
            score -= 10; // 过度使用深度词汇
        } else if (depthCount > content.length() / 300) {
            score += 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析写作风格特征
     * 检测句式复杂度、段落分布、修辞手法等
     * 
     * @param content 论文内容
     * @return 写作风格得分 (0-100)
     */
    public static double analyzeWritingStyle(String content) {
        double score = 50.0;
        
        // 1. 句式长度分析
        String[] sentences = content.split("[。！？]");
        if (sentences.length > 0) {
            List<Integer> lengths = new ArrayList<>();
            for (String sentence : sentences) {
                if (!sentence.trim().isEmpty()) {
                    lengths.add(sentence.length());
                }
            }
            
            // 计算句长变异系数
            double avgLength = lengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            double variance = lengths.stream()
                .mapToDouble(len -> Math.pow(len - avgLength, 2))
                .average().orElse(0);
            double stdDev = Math.sqrt(variance);
            double cv = avgLength > 0 ? (stdDev / avgLength) : 0;
            
            // 变异系数适中（0.3-0.6）说明句式多样
            if (cv >= 0.3 && cv <= 0.6) {
                score += 15;
            } else if (cv < 0.2 || cv > 0.8) {
                score -= 10; // 句式过于单一或混乱
            }
        }
        
        // 2. 学术套话检测
        String[] clichePhrases = {
            "众所周知", "不言而喻", "毋庸置疑", "显而易见",
            "总的来说", "一般来说", "可以说", "应该说"
        };
        int clicheCount = 0;
        for (String phrase : clichePhrases) {
            clicheCount += countOccurrences(content, phrase);
        }
        // AI生成容易使用套话
        if (clicheCount > content.length() / 300) {
            score -= 15;
        }
        
        // 3. 修辞手法检测
        String[] rhetoricalDevices = {
            "如同", "好比", "犹如", "正如", "恰似",
            "不仅...而且", "既...又", "无论...都"
        };
        int rhetoricCount = 0;
        for (String device : rhetoricalDevices) {
            rhetoricCount += countOccurrences(content, device);
        }
        score += Math.min(rhetoricCount * 1.0, 10);
        
        // 4. 被动语态检测
        String[] passiveMarkers = {
            "被", "受到", "得到", "遭到", "为...所"
        };
        int passiveCount = 0;
        for (String marker : passiveMarkers) {
            passiveCount += countOccurrences(content, marker);
        }
        double passiveDensity = (double) passiveCount / content.length() * 1000;
        // 适度使用被动语态
        if (passiveDensity >= 2 && passiveDensity <= 8) {
            score += 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析参考文献特征
     * 检测引用模式、文献来源等
     * 
     * @param content 论文内容
     * @return 参考文献得分 (0-100)
     */
    public static double analyzeReferencePattern(String content) {
        double score = 50.0;
        
        // 1. 引用数量分析
        Pattern citationPattern = Pattern.compile("\\[[0-9,\\-]+\\]");
        Matcher matcher = citationPattern.matcher(content);
        int citationCount = 0;
        while (matcher.find()) {
            citationCount++;
        }
        
        // 引用密度
        double citationDensity = (double) citationCount / content.length() * 1000;
        if (citationDensity >= 3 && citationDensity <= 10) {
            score += 20; // 引用密度适中
        } else if (citationDensity < 1) {
            score -= 15; // 缺少引用
        }
        
        // 2. 年份信息检测
        Pattern yearPattern = Pattern.compile("(19|20)\\d{2}");
        matcher = yearPattern.matcher(content);
        List<Integer> years = new ArrayList<>();
        while (matcher.find()) {
            try {
                years.add(Integer.parseInt(matcher.group()));
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }
        
        // 检查引用时效性
        if (!years.isEmpty()) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            long recentCount = years.stream()
                .filter(year -> currentYear - year <= 5)
                .count();
            double recentRatio = (double) recentCount / years.size();
            
            if (recentRatio >= 0.3) {
                score += 15; // 有较多近期文献
            } else if (recentRatio < 0.1) {
                score -= 10; // 文献过于陈旧
            }
        }
        
        // 3. 引用方式多样性
        String[] citationStyles = {
            "指出", "认为", "提出", "发现", "证明", "研究表明"
        };
        int styleCount = 0;
        for (String style : citationStyles) {
            if (content.contains(style)) {
                styleCount++;
            }
        }
        score += styleCount * 2; // 引用方式越多样越好
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析创新性特征
     * 检测新颖观点、批判性思维等
     * 
     * @param content 论文内容
     * @return 创新性得分 (0-100)
     */
    public static double analyzeInnovation(String content) {
        double score = 50.0;
        
        // 1. 创新关键词
        String[] innovationKeywords = {
            "创新", "新颖", "独特", "原创", "首次", "前沿",
            "突破", "开创", "探索", "尝试"
        };
        int innovationCount = 0;
        for (String keyword : innovationKeywords) {
            innovationCount += countOccurrences(content, keyword);
        }
        score += Math.min(innovationCount * 1.5, 15);
        
        // 2. 批判性思维
        String[] criticalThinking = {
            "质疑", "反思", "局限", "不足", "问题", "挑战",
            "争议", "存在的问题", "有待", "需要进一步"
        };
        int criticalCount = 0;
        for (String keyword : criticalThinking) {
            criticalCount += countOccurrences(content, keyword);
        }
        score += Math.min(criticalCount * 1.2, 15);
        
        // 3. 研究问题意识
        String[] questionMarkers = {
            "为什么", "如何", "是否", "能否", "怎样", "？"
        };
        int questionCount = 0;
        for (String marker : questionMarkers) {
            questionCount += countOccurrences(content, marker);
        }
        score += Math.min(questionCount * 0.8, 10);
        
        // 4. 保守性语言（负面）
        String[] conservativeWords = {
            "传统", "常规", "一般", "通常", "普遍",
            "标准", "规范", "按照", "遵循"
        };
        int conservativeCount = 0;
        for (String word : conservativeWords) {
            conservativeCount += countOccurrences(content, word);
        }
        // 过度保守可能缺乏创新
        if (conservativeCount > content.length() / 200) {
            score -= 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析语言连贯性特征
     * 检测概念重复、过渡词使用等
     * 
     * @param content 论文内容
     * @return 语言连贯性得分 (0-100)
     */
    public static double analyzeLanguageCoherence(String content) {
        double score = 50.0;
        
        // 1. 过渡词使用
        String[] transitionWords = {
            "同时", "此外", "另外", "而且", "并且",
            "接着", "随后", "然后", "进而", "继而"
        };
        int transitionCount = 0;
        for (String word : transitionWords) {
            transitionCount += countOccurrences(content, word);
        }
        double transitionDensity = (double) transitionCount / content.length() * 1000;
        
        // 适度使用过渡词
        if (transitionDensity >= 2 && transitionDensity <= 6) {
            score += 15;
        } else if (transitionDensity > 10) {
            score -= 10; // 过度使用，可能是AI生成
        }
        
        // 2. 指代词使用
        String[] pronouns = {
            "这", "那", "此", "其", "该", "上述", "前述"
        };
        int pronounCount = 0;
        for (String pronoun : pronouns) {
            pronounCount += countOccurrences(content, pronoun);
        }
        score += Math.min(pronounCount * 0.1, 10);
        
        // 3. 概念重复度分析
        String[] sentences = content.split("[。！？]");
        if (sentences.length >= 2) {
            int repetitionCount = 0;
            for (int i = 0; i < sentences.length - 1; i++) {
                String current = sentences[i].trim();
                String next = sentences[i + 1].trim();
                if (!current.isEmpty() && !next.isEmpty()) {
                    // 简单的词汇重叠检测
                    Set<String> currentWords = new HashSet<>(Arrays.asList(current.split("")));
                    Set<String> nextWords = new HashSet<>(Arrays.asList(next.split("")));
                    currentWords.retainAll(nextWords);
                    if (currentWords.size() > Math.min(current.length(), next.length()) * 0.5) {
                        repetitionCount++;
                    }
                }
            }
            
            double repetitionRatio = (double) repetitionCount / sentences.length;
            if (repetitionRatio < 0.3) {
                score += 10; // 概念使用多样
            } else if (repetitionRatio > 0.6) {
                score -= 10; // 过度重复
            }
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 分析数据与实证特征
     * 检测数据真实性、实验设计等
     * 
     * @param content 论文内容
     * @return 数据实证得分 (0-100)
     */
    public static double analyzeEmpiricalEvidence(String content) {
        double score = 50.0;
        
        // 1. 数据呈现
        String[] dataKeywords = {
            "数据", "样本", "调查", "实验", "测试", "分析",
            "统计", "结果", "图表", "表格"
        };
        int dataCount = 0;
        for (String keyword : dataKeywords) {
            dataCount += countOccurrences(content, keyword);
        }
        score += Math.min(dataCount * 0.5, 15);
        
        // 2. 具体数值
        Pattern numberPattern = Pattern.compile("\\d+\\.\\d+|\\d+%|n=\\d+");
        Matcher matcher = numberPattern.matcher(content);
        int numberCount = 0;
        while (matcher.find()) {
            numberCount++;
        }
        score += Math.min(numberCount * 0.3, 15);
        
        // 3. 方法描述
        String[] methodKeywords = {
            "方法", "步骤", "过程", "程序", "流程",
            "采用", "使用", "运用", "通过"
        };
        int methodCount = 0;
        for (String keyword : methodKeywords) {
            methodCount += countOccurrences(content, keyword);
        }
        score += Math.min(methodCount * 0.4, 10);
        
        // 4. 结果讨论
        String[] discussionKeywords = {
            "结果表明", "显示", "说明", "证明", "验证",
            "发现", "表明", "揭示", "反映"
        };
        int discussionCount = 0;
        for (String keyword : discussionKeywords) {
            discussionCount += countOccurrences(content, keyword);
        }
        score += Math.min(discussionCount * 0.6, 10);
        
        // 5. 模糊表述（负面）
        String[] vagueTerms = {
            "大约", "左右", "许多", "一些", "若干",
            "相当", "比较", "较为", "某种程度"
        };
        int vagueCount = 0;
        for (String term : vagueTerms) {
            vagueCount += countOccurrences(content, term);
        }
        // 过多模糊表述降低实证性
        if (vagueCount > content.length() / 250) {
            score -= 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 综合计算论文特征得分
     * 
     * @param content 论文内容
     * @return 特征得分映射
     */
    public static Map<String, Double> analyzeAllFeatures(String content) {
        Map<String, Double> features = new HashMap<>();
        
        features.put("academicFormality", analyzeAcademicFormality(content));
        features.put("argumentationStructure", analyzeArgumentationStructure(content));
        features.put("knowledgeDepth", analyzeKnowledgeDepth(content));
        features.put("writingStyle", analyzeWritingStyle(content));
        features.put("referencePattern", analyzeReferencePattern(content));
        features.put("innovation", analyzeInnovation(content));
        features.put("languageCoherence", analyzeLanguageCoherence(content));
        features.put("empiricalEvidence", analyzeEmpiricalEvidence(content));
        
        return features;
    }

    /**
     * 计算特征加权总分
     * 
     * @param features 特征得分映射
     * @return 加权总分
     */
    public static double calculateWeightedScore(Map<String, Double> features) {
        // 特征权重配置
        Map<String, Double> weights = new HashMap<>();
        weights.put("academicFormality", 0.15);
        weights.put("argumentationStructure", 0.15);
        weights.put("knowledgeDepth", 0.15);
        weights.put("writingStyle", 0.10);
        weights.put("referencePattern", 0.10);
        weights.put("innovation", 0.15);
        weights.put("languageCoherence", 0.10);
        weights.put("empiricalEvidence", 0.10);
        
        double totalScore = 0.0;
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            totalScore += entry.getValue() * weights.getOrDefault(entry.getKey(), 0.0);
        }
        
        return totalScore;
    }

    /**
     * 统计字符串中子串出现次数
     * 
     * @param text 文本
     * @param substring 子串
     * @return 出现次数
     */
    private static int countOccurrences(String text, String substring) {
        if (text == null || substring == null || substring.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
