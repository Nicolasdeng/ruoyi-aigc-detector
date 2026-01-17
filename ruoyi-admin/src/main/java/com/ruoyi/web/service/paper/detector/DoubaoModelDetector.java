package com.ruoyi.web.service.paper.detector;

import com.ruoyi.web.service.paper.IAiModelDetector;
import com.ruoyi.web.service.paper.library.DoubaoFeatureLibrary;
import com.ruoyi.web.service.paper.util.PaperFeatureAnalyzer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 豆包AI模型检测器（增强版）
 * 识别豆包(Doubao)生成文本的特征
 * 
 * 豆包特点：
 * 1. 结构模板化 - 喜欢使用"首先、其次、最后"的固定模式
 * 2. 学术规范强 - 严格遵循学术写作规范，但可能过于刻板
 * 3. 逻辑连贯性高 - 论证结构完整，但缺乏深度
 * 4. 成语使用频繁 - 大量使用四字成语和固定搭配
 * 5. 套话较多 - "具有重要意义"等模板化表达
 * 
 * @author ruoyi
 * @date 2025-12-26
 */
@Service
public class DoubaoModelDetector implements IAiModelDetector
{
    // 豆包高频关键短语
    private static final List<String> KEY_PHRASES = Arrays.asList(
        "随着.*的发展", "显然", "不难发现", "可以看出", "具有重要意义",
        "发挥重要作用", "起到关键作用", "产生深远影响", "由此可见",
        "综上所述", "总而言之", "不难看出", "众所周知", "毋庸置疑"
    );
    
    // 豆包偏好的逻辑连接词模式
    private static final Pattern LOGIC_PATTERN = Pattern.compile(
        "首先.*其次.*最后|一方面.*另一方面|不仅.*而且|既.*又"
    );
    
    // 豆包高频词汇
    private static final List<String> HIGH_FREQ_WORDS = Arrays.asList(
        "重要", "关键", "有效", "显著", "然而", "但是", "因此", "所以"
    );
    
    @Override
    public BigDecimal detectModel(String content)
    {
        if (content == null || content.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // 第一部分:原有基础特征检测（占25%权重）
        double baseScore = calculateBaseScore(content);
        
        // 第二部分：论文专属特征检测（占40%权重）
        double paperScore = calculatePaperScore(content);
        
        // 第三部分：增强特征库检测（占35%权重）- 新增
        double enhancedScore = DoubaoFeatureLibrary.calculateDoubaoScore(content);
        
        // 综合得分（三部分加权）
        double totalScore = baseScore * 0.25 + paperScore * 0.40 + enhancedScore * 0.35;
        
        return BigDecimal.valueOf(Math.min(totalScore, 100)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算基础特征得分
     */
    private double calculateBaseScore(String content)
    {
        double totalScore = 0;
        Map<String, String> features = getFeatureDetails(content);
        
        // 1. 关键短语匹配 (30分)
        int keyPhraseCount = Integer.parseInt((String) features.get("keyPhraseCount"));
        if (keyPhraseCount >= 4) {
            totalScore += 30;
        } else if (keyPhraseCount >= 3) {
            totalScore += 22;
        } else if (keyPhraseCount >= 2) {
            totalScore += 15;
        } else if (keyPhraseCount >= 1) {
            totalScore += 8;
        }
        
        // 2. 句子长度特征 (25分)
        double avgLength = Double.parseDouble((String) features.get("avgSentenceLength"));
        double lengthStdDev = Double.parseDouble((String) features.get("sentenceLengthStdDev"));
        if (avgLength >= 20 && avgLength <= 35 && lengthStdDev < avgLength * 0.25) {
            totalScore += 25;
        } else if (avgLength >= 18 && avgLength <= 40 && lengthStdDev < avgLength * 0.35) {
            totalScore += 18;
        } else if (avgLength >= 15 && avgLength <= 45) {
            totalScore += 10;
        }
        
        // 3. 完整句式结构比例 (20分)
        double completeRate = Double.parseDouble((String) features.get("completeStructureRate"));
        if (completeRate > 0.90) {
            totalScore += 20;
        } else if (completeRate > 0.85) {
            totalScore += 15;
        } else if (completeRate > 0.80) {
            totalScore += 10;
        }
        
        // 4. 逻辑连接词密度 (15分)
        boolean hasLogicPattern = Boolean.parseBoolean((String) features.get("hasLogicPattern"));
        double connectorDensity = Double.parseDouble((String) features.get("connectorDensity"));
        if (hasLogicPattern && connectorDensity > 0.7) {
            totalScore += 15;
        } else if (connectorDensity > 0.6) {
            totalScore += 10;
        } else if (connectorDensity > 0.5) {
            totalScore += 5;
        }
        
        // 5. 四字成语密度 (10分)
        double idiomDensity = Double.parseDouble((String) features.get("idiomDensity"));
        if (idiomDensity > 0.15) {
            totalScore += 10;
        } else if (idiomDensity > 0.10) {
            totalScore += 6;
        } else if (idiomDensity > 0.05) {
            totalScore += 3;
        }
        
        return totalScore;
    }
    
    /**
     * 计算论文专属特征得分（针对豆包特点优化）
     */
    private double calculatePaperScore(String content)
    {
        // 获取8大特征得分
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(content);
        
        // 豆包特点权重配置：
        // 1. 结构模板化 -> 论证结构得分高但过于规整
        // 2. 学术规范强 -> 学术规范性得分很高
        // 3. 逻辑连贯性高但深度不足 -> 语言连贯性高，知识深度中等
        // 4. 写作风格固定 -> 写作风格得分中等偏上
        
        double score = 0;
        
        // 1. 学术规范性（权重20%）- 豆包的强项
        double formalityScore = paperFeatures.get("academicFormality");
        if (formalityScore >= 75) {
            score += 20; // 过于规范是豆包特征
        } else if (formalityScore >= 65) {
            score += 15;
        } else if (formalityScore >= 55) {
            score += 10;
        }
        
        // 2. 论证结构（权重25%）- 豆包的显著特征，结构完整但模板化
        double argumentScore = paperFeatures.get("argumentationStructure");
        if (argumentScore >= 75) {
            score += 25; // 结构过于完整是豆包特征
        } else if (argumentScore >= 65) {
            score += 20;
        } else if (argumentScore >= 55) {
            score += 12;
        }
        
        // 3. 知识深度（权重15%）- 豆包偏中等，不太深入
        double depthScore = paperFeatures.get("knowledgeDepth");
        if (depthScore >= 45 && depthScore <= 65) {
            score += 15; // 中等深度是豆包特征
        } else if (depthScore >= 40 && depthScore <= 70) {
            score += 10;
        } else if (depthScore < 40) {
            score += 5; // 深度不足也符合豆包
        }
        
        // 4. 写作风格（权重15%）- 豆包风格相对固定
        double styleScore = paperFeatures.get("writingStyle");
        if (styleScore >= 50 && styleScore <= 70) {
            score += 15; // 中等偏上的风格得分
        } else if (styleScore >= 45 && styleScore <= 75) {
            score += 10;
        }
        
        // 5. 语言连贯性（权重15%）- 豆包连贯性很好
        double coherenceScore = paperFeatures.get("languageCoherence");
        if (coherenceScore >= 70) {
            score += 15;
        } else if (coherenceScore >= 60) {
            score += 10;
        } else if (coherenceScore >= 50) {
            score += 5;
        }
        
        // 6. 创新性（权重5%）- 豆包创新性偏低
        double innovationScore = paperFeatures.get("innovation");
        if (innovationScore < 40) {
            score += 5; // 创新性低是豆包特征
        } else if (innovationScore < 50) {
            score += 3;
        }
        
        // 7. 参考文献（权重3%）- 豆包引用规范但可能单一
        double referenceScore = paperFeatures.get("referencePattern");
        if (referenceScore >= 50 && referenceScore <= 70) {
            score += 3;
        } else if (referenceScore >= 45) {
            score += 2;
        }
        
        // 8. 数据实证（权重2%）- 次要特征
        double evidenceScore = paperFeatures.get("empiricalEvidence");
        if (evidenceScore >= 40 && evidenceScore <= 65) {
            score += 2;
        } else if (evidenceScore >= 35) {
            score += 1;
        }
        
        return score;
    }
    
    @Override
    public String getDetectorName()
    {
        return "DoubaoModelDetector";
    }
    
    @Override
    public String getModelName()
    {
        return "豆包(Doubao)";
    }
    
    @Override
    public Map<String, String> getFeatureDetails(String content)
    {
        Map<String, String> features = new HashMap<>();
        
        // === 第一部分：原有基础特征 ===
        
        // 1. 统计关键短语
        long keyPhraseCount = KEY_PHRASES.stream()
            .filter(phrase -> {
                if (phrase.contains(".*")) {
                    return Pattern.compile(phrase).matcher(content).find();
                }
                return content.contains(phrase);
            })
            .count();
        features.put("keyPhraseCount", String.valueOf((int) keyPhraseCount));
        
        // 2. 分析句子长度
        List<String> sentences = splitSentences(content);
        if (!sentences.isEmpty()) {
            List<Integer> lengths = sentences.stream()
                .map(String::length)
                .collect(Collectors.toList());
            
            double avgLength = lengths.stream().mapToInt(Integer::intValue).average().orElse(0);
            double variance = lengths.stream()
                .mapToDouble(len -> Math.pow(len - avgLength, 2))
                .average().orElse(0);
            double stdDev = Math.sqrt(variance);
            
            features.put("avgSentenceLength", String.valueOf(avgLength));
            features.put("sentenceLengthStdDev", String.valueOf(stdDev));
            
            // 3. 完整句式结构比例
            long completeStructures = sentences.stream()
                .filter(this::isCompleteStructure)
                .count();
            features.put("completeStructureRate", String.valueOf((double) completeStructures / sentences.size()));
        } else {
            features.put("avgSentenceLength", "0.0");
            features.put("sentenceLengthStdDev", "0.0");
            features.put("completeStructureRate", "0.0");
        }
        
        // 4. 逻辑连接词模式
        features.put("hasLogicPattern", String.valueOf(LOGIC_PATTERN.matcher(content).find()));
        
        // 统计段落中含有逻辑连接词的比例
        String[] paragraphs = content.split("\n+");
        long paragraphsWithConnector = Arrays.stream(paragraphs)
            .filter(p -> p.contains("首先") || p.contains("其次") || p.contains("最后") ||
                        p.contains("因此") || p.contains("所以") || p.contains("然而"))
            .count();
        features.put("connectorDensity", 
            String.valueOf(paragraphs.length > 0 ? (double) paragraphsWithConnector / paragraphs.length : 0.0));
        
        // 5. 四字成语密度
        int idiomCount = countIdioms(content);
        int totalWords = countWords(content);
        features.put("idiomDensity", String.valueOf(totalWords > 0 ? (double) idiomCount / totalWords : 0.0));
        features.put("idiomCount", String.valueOf(idiomCount));
        
        // 6. 高频词统计
        long highFreqWordCount = HIGH_FREQ_WORDS.stream()
            .filter(content::contains)
            .count();
        features.put("highFreqWordCount", String.valueOf((int) highFreqWordCount));
        
        // === 第二部分：增强特征库特征 ===
        Map<String, Object> enhancedFeatures = DoubaoFeatureLibrary.getFeatureDetails(content);
        features.put("enhancedFeatures", enhancedFeatures.toString());
        
        // 添加增强特征库的得分
        features.put("enhancedLibraryScore", String.valueOf(DoubaoFeatureLibrary.calculateDoubaoScore(content)));
        
        // === 第三部分：论文专属特征（8大维度）===
        Map<String, Double> paperFeatures = PaperFeatureAnalyzer.analyzeAllFeatures(content);
        features.put("paperFeatures", paperFeatures.toString());
        
        // 添加各项论文特征得分
        features.put("academicFormality", String.valueOf(paperFeatures.get("academicFormality")));
        features.put("argumentationStructure", String.valueOf(paperFeatures.get("argumentationStructure")));
        features.put("knowledgeDepth", String.valueOf(paperFeatures.get("knowledgeDepth")));
        features.put("writingStyle", String.valueOf(paperFeatures.get("writingStyle")));
        features.put("referencePattern", String.valueOf(paperFeatures.get("referencePattern")));
        features.put("innovation", String.valueOf(paperFeatures.get("innovation")));
        features.put("languageCoherence", String.valueOf(paperFeatures.get("languageCoherence")));
        features.put("empiricalEvidence", String.valueOf(paperFeatures.get("empiricalEvidence")));
        
        return features;
    }
    
    @Override
    public List<String> generateSuggestions(String content, BigDecimal matchScore)
    {
        List<String> suggestions = new ArrayList<>();
        
        if (matchScore.doubleValue() < 40) {
            return suggestions; // 匹配度低，不需要建议
        }
        
        Map<String, String> features = getFeatureDetails(content);
        
        // === 第一部分：基础特征建议 ===
        
        int keyPhraseCount = Integer.parseInt(features.get("keyPhraseCount"));
        if (keyPhraseCount >= 3) {
            suggestions.add("【套话问题】减少“随着...的发展”、“显然”、“不难发现”等固定开头");
            suggestions.add("【模板化】用更具体的表达替代“具有重要意义”等空洞表达");
        }
        
        double avgLength = Double.parseDouble(features.get("avgSentenceLength"));
        double stdDev = Double.parseDouble(features.get("sentenceLengthStdDev"));
        if (avgLength >= 20 && avgLength <= 35 && stdDev < avgLength * 0.25) {
            suggestions.add("【句式单一】增加句子长度变化，短句<15字，长句>40字");
            suggestions.add("【写作技巧】短句强调重点，长句详细论述");
        }
        
        boolean hasLogicPattern = Boolean.parseBoolean(features.get("hasLogicPattern"));
        double connectorDensity = Double.parseDouble(features.get("connectorDensity"));
        if (hasLogicPattern || connectorDensity > 0.6) {
            suggestions.add("【结构模板化】避免每段都用“首先、其次、最后”的固定模式");
            suggestions.add("【过度连接】减少逻辑连接词使用，让论述更自然");
            suggestions.add("【直接表达】可以直接陈述观点，无需总强调逻辑层次");
        }
        
        double completeRate = Double.parseDouble(features.get("completeStructureRate"));
        if (completeRate > 0.90) {
            suggestions.add("【过于规范】偶尔使用口语化表达或不完整句式");
            suggestions.add("【增加变化】适当打断完整的主谓宾结构");
        }
        
        double idiomDensity = Double.parseDouble(features.get("idiomDensity"));
        if (idiomDensity > 0.10) {
            suggestions.add("【成语堆砌】减少四字成语，用具体描述代替");
            suggestions.add("【内容实质】注重论述实质，避免过多修饰");
        }
        
        // === 第二部分：论文专属特征建议 ===
        
        // 学术规范性建议
        double formalityScore = Double.parseDouble(features.get("academicFormality"));
        if (formalityScore >= 75) {
            suggestions.add("【学术规范过度】适当放松学术规范，增加个人色彩");
            suggestions.add("【人性化表达】可以使用一些非正式但专业的表达方式");
        }
        
        // 论证结构建议
        double argumentScore = Double.parseDouble(features.get("argumentationStructure"));
        if (argumentScore >= 75) {
            suggestions.add("【结构过于完整】论证结构可以更灵活，不必过于工整");
            suggestions.add("【深度优先】相比完整的结构，更应注重论证深度");
        }
        
        // 知识深度建议
        double depthScore = Double.parseDouble(features.get("knowledgeDepth"));
        if (depthScore >= 45 && depthScore <= 65) {
            suggestions.add("【知识深度不足】增加专业术语和深层概念的阐述");
            suggestions.add("【理论深化】引入更多学术理论和研究成果");
        } else if (depthScore < 40) {
            suggestions.add("【知识深度欠缺】大幅增加专业性内容和学术深度");
            suggestions.add("【专业术语】使用更多领域专业术语和概念");
        }
        
        // 创新性建议
        double innovationScore = Double.parseDouble(features.get("innovation"));
        if (innovationScore < 40) {
            suggestions.add("【缺乏创新】增加个人观点和独特见解");
            suggestions.add("【批判性思维】对现有观点提出质疑和新角度");
        }
        
        // 语言连贯性建议
        double coherenceScore = Double.parseDouble(features.get("languageCoherence"));
        if (coherenceScore >= 70) {
            suggestions.add("【过度连贯】可以适当打破连贯性，增加思维跳跃");
            suggestions.add("【自然表达】减少刻意的过渡，让表达更自然");
        }
        
        // === 第三部分：增强特征库建议 ===
        
        Map<String, Object> enhancedFeatures = DoubaoFeatureLibrary.getFeatureDetails(content);
        
        // 转折词库建议
        int transitionWordCount = (int) enhancedFeatures.get("transitionWordCount");
        if (transitionWordCount >= 8) {
            suggestions.add("【转折词过度】减少“然而”、“但是”、“不过”等转折词的使用频率");
            suggestions.add("【自然过渡】用更自然的方式表达转折，不必每次都用明显的转折词");
        }
        
        // 学术短语建议
        int academicPhraseCount = (int) enhancedFeatures.get("academicPhraseCount");
        if (academicPhraseCount >= 6) {
            suggestions.add("【学术套话】减少“由此可见”、“综上所述”等学术套话");
            suggestions.add("【直接表达】可以更直接地表达观点，无需总用学术短语过渡");
        }
        
        // 段落开头模式建议
        int paragraphStarterCount = (int) enhancedFeatures.get("paragraphStarterCount");
        if (paragraphStarterCount >= 5) {
            suggestions.add("【开头模板化】段落开头过于固定，增加开头方式的多样性");
            suggestions.add("【灵活起笔】尝试用案例、数据、问题等不同方式开启段落");
        }
        
        // 结论标志词建议
        int conclusionMarkerCount = (int) enhancedFeatures.get("conclusionMarkerCount");
        if (conclusionMarkerCount >= 4) {
            suggestions.add("【结论标志过多】减少“总之”、“因此”等结论标志词");
            suggestions.add("【隐含结论】让结论自然呈现，不必总用明显的标志词");
        }
        
        // 数据表达模式建议
        int dataPatternCount = (int) enhancedFeatures.get("dataPatternCount");
        if (dataPatternCount >= 3) {
            suggestions.add("【数据表达模板化】数据引用方式过于固定");
            suggestions.add("【数据融合】将数据更自然地融入论述，而非独立呈现");
        }
        
        // 句式模板建议
        int sentenceTemplateCount = (int) enhancedFeatures.get("sentenceTemplateCount");
        if (sentenceTemplateCount >= 5) {
            suggestions.add("【句式模板化】减少固定句式模板的使用");
            suggestions.add("【句式创新】尝试用不同的句式结构表达相同意思");
        }
        
        // === 第四部分：综合优化建议 ===
        
        if (matchScore.doubleValue() >= 70) {
            suggestions.add("【高度疑似AI】建议大幅改写，加入真实案例和个人经历");
            suggestions.add("【人工特征】增加主观判断、情感表达和不完美之处");
            suggestions.add("【深度重构】用自己的理解重新组织内容框架");
        } else if (matchScore.doubleValue() >= 60) {
            suggestions.add("【疑似AI】加入个人观点和真实案例");
            suggestions.add("【个性化】增加主观感受和非正式表达");
            suggestions.add("【重新表达】用自己的话重新组织内容");
        }
        
        return suggestions;
    }
    
    @Override
    public List<String> generateSuggestions(String text)
    {
        // 默认使用50分作为基准匹配度
        return generateSuggestions(text, BigDecimal.valueOf(50));
    }

    public List<String> generateSuggestions(String text, double score)
    {
        // 将double转换为BigDecimal后委托给已有方法
        return generateSuggestions(text, BigDecimal.valueOf(score));
    }
    
    /**
     * 分句
     */
    private List<String> splitSentences(String content)
    {
        String[] sentences = content.split("[。！？.!?]+");
        return Arrays.stream(sentences)
            .map(String::trim)
            .filter(s -> !s.isEmpty() && s.length() > 5)
            .collect(Collectors.toList());
    }
    
    /**
     * 判断是否为完整句式结构
     */
    private boolean isCompleteStructure(String sentence)
    {
        return sentence.length() > 10 &&
               (sentence.contains("是") || sentence.contains("为") || 
                sentence.contains("有") || sentence.contains("在") ||
                sentence.contains("能") || sentence.contains("可以") ||
                sentence.matches(".*[^，。！？]*[，。！？].*"));
    }
    
    /**
     * 统计四字成语数量（简化版）
     */
    private int countIdioms(String content)
    {
        // 简单的四字词组匹配
        Pattern idiomPattern = Pattern.compile("[\\u4e00-\\u9fa5]{4}(?=[^\\u4e00-\\u9fa5]|$)");
        java.util.regex.Matcher matcher = idiomPattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            String word = matcher.group();
            // 过滤一些常见的非成语四字词
            if (!word.matches(".*[的了着过].*") && !word.matches("\\d+")) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 统计词数
     */
    private int countWords(String content)
    {
        int chineseCount = content.replaceAll("[^\\u4e00-\\u9fa5]", "").length();
        String[] englishWords = content.replaceAll("[\\u4e00-\\u9fa5]", "").trim().split("\\s+");
        int englishCount = englishWords.length > 0 && !englishWords[0].isEmpty() ? englishWords.length : 0;
        return chineseCount + englishCount;
    }
}
