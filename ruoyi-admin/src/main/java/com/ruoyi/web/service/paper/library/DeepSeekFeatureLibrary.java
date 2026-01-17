package com.ruoyi.web.service.paper.library;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DeepSeek AI模型特征库
 * 特点：深度推理、技术细节、代码思维、层次分析
 * 
 * @author ruoyi
 */
public class DeepSeekFeatureLibrary {
    
    /**
     * 深度推理标志库 (权重: 20%)
     * DeepSeek擅长深入分析和推理
     */
    public static final Set<String> DEEPSEEK_REASONING_MARKERS = new HashSet<>(Arrays.asList(
        "深入分析", "深层次探讨", "深度剖析", "深入探究", "深刻理解",
        "从根本上", "本质上讲", "根源在于", "追根溯源", "究其根本",
        "深层原因", "深层逻辑", "内在机制", "内在联系", "内在规律",
        "背后的逻辑", "底层原理", "核心机制", "关键机理", "运作机制",
        "演化规律", "发展脉络", "逻辑链条", "因果链条", "推理链条"
    ));
    
    /**
     * 技术细节描述库 (权重: 18%)
     * DeepSeek注重技术细节和精确描述
     */
    public static final Set<String> DEEPSEEK_TECHNICAL_TERMS = new HashSet<>(Arrays.asList(
        "具体而言", "详细来说", "精确地说", "准确地讲", "严格来说",
        "技术层面", "实现层面", "操作层面", "执行层面", "应用层面",
        "参数设置", "配置选项", "优化策略", "算法选择", "方案设计",
        "性能指标", "评估标准", "衡量维度", "测试方法", "验证方式",
        "系统架构", "模块划分", "接口设计", "数据结构", "流程设计",
        "关键参数", "核心变量", "重要因子", "影响因素", "控制条件"
    ));
    
    /**
     * 代码思维模式库 (权重: 16%)
     * DeepSeek具有明显的程序化思维特征
     */
    public static final Set<String> DEEPSEEK_CODE_THINKING = new HashSet<>(Arrays.asList(
        "步骤如下", "具体步骤", "操作流程", "执行过程", "处理流程",
        "输入输出", "输入参数", "输出结果", "返回值", "调用方式",
        "条件判断", "分支处理", "循环遍历", "递归调用", "迭代过程",
        "初始化", "配置文件", "环境变量", "依赖关系", "版本控制",
        "异常处理", "错误检测", "边界条件", "特殊情况", "容错机制",
        "优化方案", "改进措施", "调优策略", "性能提升", "效率优化"
    ));
    
    /**
     * 层次分析模式 (权重: 15%)
     * DeepSeek善于构建多层次分析框架
     */
    public static final List<Pattern> DEEPSEEK_LAYERED_ANALYSIS = Arrays.asList(
        Pattern.compile("从.*?层面.*?从.*?层面.*?从.*?层面"),  // 多层面分析
        Pattern.compile("第一层次.*?第二层次.*?第三层次"),  // 层次递进
        Pattern.compile("宏观.*?中观.*?微观"),  // 三观分析
        Pattern.compile("理论.*?实践.*?应用"),  // 理论实践结合
        Pattern.compile("表层.*?中层.*?深层"),  // 深度层次
        Pattern.compile("基础层.*?中间层.*?应用层")  // 架构层次
    );
    
    /**
     * 量化分析词库 (权重: 12%)
     * DeepSeek倾向使用量化表述
     */
    public static final Set<String> DEEPSEEK_QUANTITATIVE_TERMS = new HashSet<>(Arrays.asList(
        "数据显示", "统计表明", "测量结果", "计算得出", "量化分析",
        "百分比", "比例关系", "数值范围", "阈值设定", "临界点",
        "平均值", "中位数", "标准差", "方差", "相关系数",
        "增长率", "变化率", "波动幅度", "趋势曲线", "分布特征",
        "显著性", "置信度", "误差范围", "精度要求", "准确率"
    ));
    
    /**
     * 对比分析模式 (权重: 10%)
     * DeepSeek擅长进行对比分析
     */
    public static final Set<String> DEEPSEEK_COMPARISON_MARKERS = new HashSet<>(Arrays.asList(
        "相比之下", "对比来看", "相较而言", "比较分析", "对照研究",
        "优势在于", "劣势表现", "优劣对比", "利弊权衡", "得失分析",
        "异同点", "相似之处", "差异之处", "共性特征", "个性特点",
        "A与B的区别", "两者的差异", "彼此的关联", "相互的影响"
    ));
    
    /**
     * 因果推理模式 (权重: 9%)
     * DeepSeek强调因果关系分析
     */
    public static final List<Pattern> DEEPSEEK_CAUSALITY_PATTERNS = Arrays.asList(
        Pattern.compile("由于.*?导致.*?进而.*?最终"),  // 因果链
        Pattern.compile("原因.*?在于.*?[，,].*?[导致|造成|引发]"),  // 原因分析
        Pattern.compile("[如果|若|假设].*?[那么|则].*?[从而|因此]"),  // 假设推理
        Pattern.compile("正因为.*?所以.*?才能"),  // 强因果
        Pattern.compile("之所以.*?是因为.*?这就")  // 解释说明
    );
    
    /**
     * 问题解决框架 (权重: 8%)
     * DeepSeek倾向使用结构化问题解决方法
     */
    public static final Set<String> DEEPSEEK_PROBLEM_SOLVING = new HashSet<>(Arrays.asList(
        "问题定义", "需求分析", "目标设定", "方案设计", "实施步骤",
        "解决方案", "应对策略", "处理办法", "改进措施", "优化建议",
        "可行性", "有效性", "适用性", "局限性", "可扩展性",
        "前提条件", "约束条件", "边界条件", "必要条件", "充分条件",
        "关键路径", "核心环节", "瓶颈问题", "突破口", "着力点"
    ));
    
    /**
     * 逻辑连接词库 (权重: 6%)
     * DeepSeek使用严谨的逻辑连接
     */
    public static final Set<String> DEEPSEEK_LOGIC_CONNECTORS = new HashSet<>(Arrays.asList(
        "因此", "所以", "故而", "由此", "从而",
        "进而", "继而", "然后", "接着", "随后",
        "然而", "但是", "不过", "可是", "只是",
        "同时", "并且", "而且", "此外", "另外",
        "即", "亦即", "也就是说", "换言之", "简言之"
    ));
    
    /**
     * 模型化思维特征 (权重: 4%)
     * DeepSeek倾向使用模型化、框架化表述
     */
    public static final Set<String> DEEPSEEK_MODEL_THINKING = new HashSet<>(Arrays.asList(
        "模型构建", "框架设计", "体系建立", "系统构成", "结构组成",
        "要素分析", "维度划分", "指标体系", "评价体系", "分析框架",
        "理论模型", "概念模型", "数学模型", "仿真模型", "预测模型"
    ));
    
    /**
     * 精确性表述 (权重: 2%)
     * DeepSeek追求表述的精确性
     */
    public static final Set<String> DEEPSEEK_PRECISION_TERMS = new HashSet<>(Arrays.asList(
        "精确", "准确", "严格", "精准", "确切",
        "明确", "清晰", "具体", "详尽", "完整"
    ));
    
    /**
     * 计算DeepSeek特征得分
     * 
     * @param text 待检测文本
     * @return DeepSeek特征得分 (0-100)
     */
    public static double calculateDeepSeekScore(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        int textLength = text.length();
        
        // 1. 深度推理 (20%)
        double reasoningScore = 0.0;
        for (String marker : DEEPSEEK_REASONING_MARKERS) {
            int count = countOccurrences(text, marker);
            reasoningScore += count;
        }
        reasoningScore = normalize(reasoningScore, textLength, 1000) * 20.0;
        totalScore += reasoningScore;
        
        // 2. 技术细节 (18%)
        double technicalScore = 0.0;
        for (String term : DEEPSEEK_TECHNICAL_TERMS) {
            int count = countOccurrences(text, term);
            technicalScore += count;
        }
        technicalScore = normalize(technicalScore, textLength, 1000) * 18.0;
        totalScore += technicalScore;
        
        // 3. 代码思维 (16%)
        double codeThinkingScore = 0.0;
        for (String term : DEEPSEEK_CODE_THINKING) {
            int count = countOccurrences(text, term);
            codeThinkingScore += count;
        }
        codeThinkingScore = normalize(codeThinkingScore, textLength, 1200) * 16.0;
        totalScore += codeThinkingScore;
        
        // 4. 层次分析 (15%)
        double layeredScore = 0.0;
        for (Pattern pattern : DEEPSEEK_LAYERED_ANALYSIS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                layeredScore += 1.0;
            }
        }
        layeredScore = normalize(layeredScore, textLength, 3000) * 15.0;
        totalScore += layeredScore;
        
        // 5. 量化分析 (12%)
        double quantitativeScore = 0.0;
        for (String term : DEEPSEEK_QUANTITATIVE_TERMS) {
            int count = countOccurrences(text, term);
            quantitativeScore += count;
        }
        quantitativeScore = normalize(quantitativeScore, textLength, 1500) * 12.0;
        totalScore += quantitativeScore;
        
        // 6. 对比分析 (10%)
        double comparisonScore = 0.0;
        for (String marker : DEEPSEEK_COMPARISON_MARKERS) {
            int count = countOccurrences(text, marker);
            comparisonScore += count;
        }
        comparisonScore = normalize(comparisonScore, textLength, 1500) * 10.0;
        totalScore += comparisonScore;
        
        // 7. 因果推理 (9%)
        double causalityScore = 0.0;
        for (Pattern pattern : DEEPSEEK_CAUSALITY_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                causalityScore += 1.0;
            }
        }
        causalityScore = normalize(causalityScore, textLength, 2000) * 9.0;
        totalScore += causalityScore;
        
        // 8. 问题解决 (8%)
        double problemSolvingScore = 0.0;
        for (String term : DEEPSEEK_PROBLEM_SOLVING) {
            int count = countOccurrences(text, term);
            problemSolvingScore += count;
        }
        problemSolvingScore = normalize(problemSolvingScore, textLength, 1500) * 8.0;
        totalScore += problemSolvingScore;
        
        // 9. 逻辑连接 (6%)
        double logicScore = 0.0;
        for (String connector : DEEPSEEK_LOGIC_CONNECTORS) {
            int count = countOccurrences(text, connector);
            logicScore += count;
        }
        logicScore = normalize(logicScore, textLength, 800) * 6.0;
        totalScore += logicScore;
        
        // 10. 模型化思维 (4%)
        double modelScore = 0.0;
        for (String term : DEEPSEEK_MODEL_THINKING) {
            int count = countOccurrences(text, term);
            modelScore += count;
        }
        modelScore = normalize(modelScore, textLength, 2000) * 4.0;
        totalScore += modelScore;
        
        // 11. 精确性表述 (2%)
        double precisionScore = 0.0;
        for (String term : DEEPSEEK_PRECISION_TERMS) {
            int count = countOccurrences(text, term);
            precisionScore += count;
        }
        precisionScore = normalize(precisionScore, textLength, 1000) * 2.0;
        totalScore += precisionScore;
        
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
        
        // 深度推理统计
        int reasoningCount = 0;
        for (String marker : DEEPSEEK_REASONING_MARKERS) {
            reasoningCount += countOccurrences(text, marker);
        }
        details.put("reasoningMarkers", reasoningCount);
        details.put("reasoningScore", normalize(reasoningCount, textLength, 1000) * 20.0);
        
        // 技术细节统计
        int technicalCount = 0;
        for (String term : DEEPSEEK_TECHNICAL_TERMS) {
            technicalCount += countOccurrences(text, term);
        }
        details.put("technicalTerms", technicalCount);
        details.put("technicalScore", normalize(technicalCount, textLength, 1000) * 18.0);
        
        // 代码思维统计
        int codeThinkingCount = 0;
        for (String term : DEEPSEEK_CODE_THINKING) {
            codeThinkingCount += countOccurrences(text, term);
        }
        details.put("codeThinkingTerms", codeThinkingCount);
        details.put("codeThinkingScore", normalize(codeThinkingCount, textLength, 1200) * 16.0);
        
        // 层次分析统计
        int layeredCount = 0;
        for (Pattern pattern : DEEPSEEK_LAYERED_ANALYSIS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                layeredCount++;
            }
        }
        details.put("layeredAnalysis", layeredCount);
        details.put("layeredScore", normalize(layeredCount, textLength, 3000) * 15.0);
        
        // 量化分析统计
        int quantitativeCount = 0;
        for (String term : DEEPSEEK_QUANTITATIVE_TERMS) {
            quantitativeCount += countOccurrences(text, term);
        }
        details.put("quantitativeTerms", quantitativeCount);
        details.put("quantitativeScore", normalize(quantitativeCount, textLength, 1500) * 12.0);
        
        // 对比分析统计
        int comparisonCount = 0;
        for (String marker : DEEPSEEK_COMPARISON_MARKERS) {
            comparisonCount += countOccurrences(text, marker);
        }
        details.put("comparisonMarkers", comparisonCount);
        details.put("comparisonScore", normalize(comparisonCount, textLength, 1500) * 10.0);
        
        // 因果推理统计
        int causalityCount = 0;
        for (Pattern pattern : DEEPSEEK_CAUSALITY_PATTERNS) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                causalityCount++;
            }
        }
        details.put("causalityPatterns", causalityCount);
        details.put("causalityScore", normalize(causalityCount, textLength, 2000) * 9.0);
        
        // 问题解决统计
        int problemSolvingCount = 0;
        for (String term : DEEPSEEK_PROBLEM_SOLVING) {
            problemSolvingCount += countOccurrences(text, term);
        }
        details.put("problemSolvingTerms", problemSolvingCount);
        details.put("problemSolvingScore", normalize(problemSolvingCount, textLength, 1500) * 8.0);
        
        // 逻辑连接统计
        int logicCount = 0;
        for (String connector : DEEPSEEK_LOGIC_CONNECTORS) {
            logicCount += countOccurrences(text, connector);
        }
        details.put("logicConnectors", logicCount);
        details.put("logicScore", normalize(logicCount, textLength, 800) * 6.0);
        
        // 模型化思维统计
        int modelCount = 0;
        for (String term : DEEPSEEK_MODEL_THINKING) {
            modelCount += countOccurrences(text, term);
        }
        details.put("modelThinkingTerms", modelCount);
        details.put("modelScore", normalize(modelCount, textLength, 2000) * 4.0);
        
        // 精确性表述统计
        int precisionCount = 0;
        for (String term : DEEPSEEK_PRECISION_TERMS) {
            precisionCount += countOccurrences(text, term);
        }
        details.put("precisionTerms", precisionCount);
        details.put("precisionScore", normalize(precisionCount, textLength, 1000) * 2.0);
        
        // 总分
        details.put("totalScore", calculateDeepSeekScore(text));
        
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
