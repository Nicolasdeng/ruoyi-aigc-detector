package com.ruoyi.web.service.paper.impl;

import com.ruoyi.web.service.paper.ISynonymService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 同义词服务实现
 * 提供学术论文常用词汇的同义词替换功能
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class SynonymServiceImpl implements ISynonymService
{
    /**
     * 同义词词典
     */
    private Map<String, List<String>> synonymDict;
    
    /**
     * 初始化同义词词典
     */
    @PostConstruct
    public void init()
    {
        synonymDict = new HashMap<>();
        
        // 动词类 - 研究相关
        synonymDict.put("研究", Arrays.asList("探讨", "分析", "考察", "调研", "探究", "研讨"));
        synonymDict.put("分析", Arrays.asList("研究", "剖析", "解析", "探讨", "考察"));
        synonymDict.put("探讨", Arrays.asList("研究", "分析", "探究", "讨论", "商讨"));
        synonymDict.put("调查", Arrays.asList("调研", "考察", "查证", "了解"));
        synonymDict.put("探索", Arrays.asList("探究", "摸索", "寻求", "发掘"));
        
        // 动词类 - 表达相关
        synonymDict.put("表明", Arrays.asList("显示", "说明", "证明", "揭示", "体现", "表示"));
        synonymDict.put("说明", Arrays.asList("表明", "阐明", "解释", "证明", "显示"));
        synonymDict.put("显示", Arrays.asList("表明", "显现", "呈现", "展示", "展现"));
        synonymDict.put("证明", Arrays.asList("表明", "证实", "验证", "印证"));
        synonymDict.put("揭示", Arrays.asList("揭露", "显示", "表明", "展现"));
        
        // 动词类 - 提出相关
        synonymDict.put("提出", Arrays.asList("提议", "建议", "阐述", "论述", "提起"));
        synonymDict.put("提议", Arrays.asList("建议", "提出", "倡议"));
        synonymDict.put("建议", Arrays.asList("提议", "提出", "倡导", "主张"));
        synonymDict.put("阐述", Arrays.asList("论述", "陈述", "叙述", "表述"));
        
        // 动词类 - 影响相关
        synonymDict.put("影响", Arrays.asList("作用", "效应", "效果", "影响力"));
        synonymDict.put("促进", Arrays.asList("推动", "推进", "助推", "加快"));
        synonymDict.put("推动", Arrays.asList("促进", "推进", "带动", "推行"));
        synonymDict.put("导致", Arrays.asList("引发", "造成", "致使", "引起"));
        synonymDict.put("引起", Arrays.asList("引发", "导致", "产生", "造成"));
        
        // 动词类 - 发展相关
        synonymDict.put("发展", Arrays.asList("发育", "演进", "演变", "推进"));
        synonymDict.put("提高", Arrays.asList("提升", "增强", "改善", "优化"));
        synonymDict.put("提升", Arrays.asList("提高", "增强", "强化", "改进"));
        synonymDict.put("改善", Arrays.asList("改进", "优化", "提高", "完善"));
        synonymDict.put("完善", Arrays.asList("改善", "健全", "优化", "改进"));
        
        // 动词类 - 使用相关
        synonymDict.put("使用", Arrays.asList("运用", "利用", "应用", "采用"));
        synonymDict.put("运用", Arrays.asList("使用", "应用", "采用", "施用"));
        synonymDict.put("采用", Arrays.asList("使用", "运用", "应用", "运用"));
        synonymDict.put("应用", Arrays.asList("运用", "使用", "采用", "实施"));
        
        // 动词类 - 实现相关
        synonymDict.put("实现", Arrays.asList("达成", "完成", "达到", "成就"));
        synonymDict.put("达到", Arrays.asList("达成", "实现", "到达", "抵达"));
        synonymDict.put("完成", Arrays.asList("达成", "实现", "做完", "办完"));
        synonymDict.put("获得", Arrays.asList("取得", "得到", "收获", "赢得"));
        
        // 形容词类 - 重要性
        synonymDict.put("重要", Arrays.asList("关键", "核心", "主要", "显著", "突出"));
        synonymDict.put("关键", Arrays.asList("重要", "核心", "要害", "关键性"));
        synonymDict.put("核心", Arrays.asList("关键", "重要", "中心", "主要"));
        synonymDict.put("主要", Arrays.asList("重要", "核心", "首要", "主导"));
        
        // 形容词类 - 明显性
        synonymDict.put("明显", Arrays.asList("显著", "突出", "清晰", "鲜明", "明确"));
        synonymDict.put("显著", Arrays.asList("明显", "突出", "显然", "显明"));
        synonymDict.put("清晰", Arrays.asList("清楚", "明晰", "明确", "分明"));
        synonymDict.put("明确", Arrays.asList("清晰", "清楚", "明了", "确定"));
        
        // 形容词类 - 程度
        synonymDict.put("巨大", Arrays.asList("庞大", "巨大", "宏大", "巨型"));
        synonymDict.put("显著", Arrays.asList("显然", "明显", "突出", "显明"));
        synonymDict.put("快速", Arrays.asList("迅速", "飞快", "高速", "快捷"));
        synonymDict.put("缓慢", Arrays.asList("迟缓", "徐缓", "缓缓"));
        
        // 形容词类 - 评价
        synonymDict.put("有效", Arrays.asList("有用", "管用", "奏效", "灵验"));
        synonymDict.put("合理", Arrays.asList("恰当", "适当", "妥当", "合适"));
        synonymDict.put("准确", Arrays.asList("精确", "正确", "精准", "确切"));
        synonymDict.put("完整", Arrays.asList("完全", "齐全", "完备", "全面"));
        
        // 名词类 - 研究方法
        synonymDict.put("方法", Arrays.asList("手段", "途径", "方式", "措施", "办法"));
        synonymDict.put("手段", Arrays.asList("方法", "途径", "方式", "措施"));
        synonymDict.put("途径", Arrays.asList("方法", "手段", "渠道", "路径"));
        synonymDict.put("方式", Arrays.asList("方法", "手段", "形式", "模式"));
        
        // 名词类 - 问题相关
        synonymDict.put("问题", Arrays.asList("课题", "议题", "难题", "疑问"));
        synonymDict.put("难题", Arrays.asList("问题", "困难", "障碍", "难关"));
        synonymDict.put("障碍", Arrays.asList("阻碍", "困难", "难题", "壁垒"));
        synonymDict.put("困难", Arrays.asList("难题", "障碍", "困境", "难关"));
        
        // 名词类 - 结果相关
        synonymDict.put("结果", Arrays.asList("成果", "结局", "效果", "后果"));
        synonymDict.put("成果", Arrays.asList("结果", "成就", "收获", "果实"));
        synonymDict.put("效果", Arrays.asList("结果", "成效", "效应", "作用"));
        synonymDict.put("作用", Arrays.asList("效果", "效应", "功能", "功效"));
        
        // 名词类 - 过程相关
        synonymDict.put("过程", Arrays.asList("历程", "进程", "经过", "阶段"));
        synonymDict.put("阶段", Arrays.asList("时期", "时段", "期间", "阶层"));
        synonymDict.put("步骤", Arrays.asList("环节", "程序", "步伐", "流程"));
        
        // 名词类 - 目标相关
        synonymDict.put("目标", Arrays.asList("目的", "宗旨", "目标", "指标"));
        synonymDict.put("目的", Arrays.asList("目标", "宗旨", "意图", "用意"));
        synonymDict.put("意义", Arrays.asList("含义", "意思", "价值", "作用"));
        
        // 副词类 - 程度
        synonymDict.put("非常", Arrays.asList("十分", "极其", "相当", "颇为", "格外"));
        synonymDict.put("十分", Arrays.asList("非常", "极其", "相当", "很"));
        synonymDict.put("极其", Arrays.asList("非常", "十分", "极度", "极为"));
        synonymDict.put("相当", Arrays.asList("非常", "十分", "颇为", "很"));
        
        // 副词类 - 因果
        synonymDict.put("因此", Arrays.asList("所以", "故而", "由此", "因而"));
        synonymDict.put("所以", Arrays.asList("因此", "故而", "因而", "于是"));
        synonymDict.put("由于", Arrays.asList("因为", "鉴于", "基于"));
        synonymDict.put("因为", Arrays.asList("由于", "因", "缘于"));
        
        // 副词类 - 程度变化
        synonymDict.put("逐渐", Arrays.asList("逐步", "渐渐", "慢慢", "逐年"));
        synonymDict.put("逐步", Arrays.asList("逐渐", "渐渐", "按部就班"));
        synonymDict.put("迅速", Arrays.asList("快速", "飞快", "急速", "迅疾"));
        synonymDict.put("快速", Arrays.asList("迅速", "飞快", "急速"));
        
        // 连词类
        synonymDict.put("并且", Arrays.asList("而且", "并", "同时", "以及"));
        synonymDict.put("但是", Arrays.asList("然而", "可是", "不过", "只是"));
        synonymDict.put("然而", Arrays.asList("但是", "可是", "却", "不过"));
        synonymDict.put("虽然", Arrays.asList("尽管", "即使", "纵然"));
        
        // 学术专用词
        synonymDict.put("理论", Arrays.asList("学说", "理论体系", "观点"));
        synonymDict.put("观点", Arrays.asList("看法", "见解", "观念", "立场"));
        synonymDict.put("概念", Arrays.asList("观念", "理念", "思想"));
        synonymDict.put("特征", Arrays.asList("特点", "特色", "特性", "性质"));
        synonymDict.put("因素", Arrays.asList("要素", "元素", "成分"));
        synonymDict.put("现象", Arrays.asList("情况", "状况", "景象"));
        synonymDict.put("趋势", Arrays.asList("趋向", "动向", "走势"));
        synonymDict.put("优势", Arrays.asList("优点", "长处", "强项"));
        synonymDict.put("局限", Arrays.asList("限制", "局限性", "不足"));
        synonymDict.put("价值", Arrays.asList("意义", "作用", "价值观"));
    }
    
    @Override
    public List<String> getSynonyms(String word)
    {
        return synonymDict.getOrDefault(word, new ArrayList<>());
    }
    
    @Override
    public Map<String, List<String>> getSynonymSuggestions(String text)
    {
        Map<String, List<String>> suggestions = new HashMap<>();
        
        // 遍历所有词典中的词
        for (String word : synonymDict.keySet()) {
            if (text.contains(word)) {
                suggestions.put(word, synonymDict.get(word));
            }
        }
        
        return suggestions;
    }
    
    @Override
    public String replaceWithSynonyms(String text, Map<String, String> replacements)
    {
        String result = text;
        
        // 按词长度降序排序，避免短词先替换导致长词无法匹配
        List<String> sortedKeys = replacements.keySet().stream()
            .sorted((a, b) -> b.length() - a.length())
            .collect(Collectors.toList());
        
        for (String originalWord : sortedKeys) {
            String replacement = replacements.get(originalWord);
            if (replacement != null && !replacement.isEmpty()) {
                result = result.replace(originalWord, replacement);
            }
        }
        
        return result;
    }
    
    @Override
    public String autoOptimize(String text, double replaceRatio)
    {
        if (replaceRatio <= 0 || replaceRatio > 1.0) {
            return text;
        }
        
        Map<String, List<String>> suggestions = getSynonymSuggestions(text);
        Map<String, String> replacements = new HashMap<>();
        Random random = new Random();
        
        // 随机选择要替换的词
        for (Map.Entry<String, List<String>> entry : suggestions.entrySet()) {
            if (random.nextDouble() < replaceRatio) {
                String word = entry.getKey();
                List<String> synonyms = entry.getValue();
                if (!synonyms.isEmpty()) {
                    // 随机选择一个同义词
                    String synonym = synonyms.get(random.nextInt(synonyms.size()));
                    replacements.put(word, synonym);
                }
            }
        }
        
        return replaceWithSynonyms(text, replacements);
    }
    
    @Override
    public boolean hasSynonyms(String word)
    {
        return synonymDict.containsKey(word) && !synonymDict.get(word).isEmpty();
    }
    
    @Override
    public List<String> getAllSupportedWords()
    {
        return new ArrayList<>(synonymDict.keySet());
    }
}
