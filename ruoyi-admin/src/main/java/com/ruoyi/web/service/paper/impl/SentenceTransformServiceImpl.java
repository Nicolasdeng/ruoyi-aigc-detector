package com.ruoyi.web.service.paper.impl;

import com.ruoyi.web.service.paper.ISentenceTransformService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 句式变换服务实现
 * 提供学术论文句式多样化变换功能
 * 
 * @author ruoyi
 * @date 2025-12-25
 */
@Service
public class SentenceTransformServiceImpl implements ISentenceTransformService
{
    /**
     * 句式变换规则列表
     */
    private List<TransformRule> transformRules;
    
    /**
     * 初始化句式变换规则
     */
    @PostConstruct
    public void init()
    {
        transformRules = new ArrayList<>();
        
        // 1. 因果句式变换
        transformRules.add(new TransformRule(
            "因为(.+?)，所以(.+?)",
            "由于$1，因而$2",
            "因果句式"
        ));
        
        transformRules.add(new TransformRule(
            "由于(.+?)，所以(.+?)",
            "因为$1，因而$2",
            "因果句式"
        ));
        
        transformRules.add(new TransformRule(
            "(.+?)导致(.+?)",
            "$1引发了$2",
            "因果关系"
        ));
        
        transformRules.add(new TransformRule(
            "(.+?)引起(.+?)",
            "$1造成了$2",
            "因果关系"
        ));
        
        // 2. 条件句式变换
        transformRules.add(new TransformRule(
            "如果(.+?)，那么(.+?)",
            "倘若$1，则$2",
            "条件句式"
        ));
        
        transformRules.add(new TransformRule(
            "假如(.+?)，就(.+?)",
            "若是$1，便$2",
            "条件句式"
        ));
        
        // 3. 转折句式变换
        transformRules.add(new TransformRule(
            "虽然(.+?)，但是(.+?)",
            "尽管$1，然而$2",
            "转折句式"
        ));
        
        transformRules.add(new TransformRule(
            "尽管(.+?)，但(.+?)",
            "虽然$1，却$2",
            "转折句式"
        ));
        
        // 4. 并列句式变换
        transformRules.add(new TransformRule(
            "(.+?)和(.+?)",
            "$1以及$2",
            "并列关系"
        ));
        
        transformRules.add(new TransformRule(
            "不仅(.+?)，而且(.+?)",
            "既$1，又$2",
            "递进关系"
        ));
        
        transformRules.add(new TransformRule(
            "既(.+?)，也(.+?)",
            "不但$1，而且$2",
            "递进关系"
        ));
        
        // 5. 递进句式变换
        transformRules.add(new TransformRule(
            "(.+?)，进而(.+?)",
            "$1，从而$2",
            "递进关系"
        ));
        
        transformRules.add(new TransformRule(
            "(.+?)，从而(.+?)",
            "$1，进而$2",
            "递进关系"
        ));
        
        // 6. 目的句式变换
        transformRules.add(new TransformRule(
            "为了(.+?)，(.+?)",
            "旨在$1，$2",
            "目的句式"
        ));
        
        transformRules.add(new TransformRule(
            "以(.+?)为目的，(.+?)",
            "为了$1，$2",
            "目的句式"
        ));
        
        // 7. 比较句式变换
        transformRules.add(new TransformRule(
            "(.+?)比(.+?)更(.+?)",
            "$1相较于$2而言更$3",
            "比较句式"
        ));
        
        transformRules.add(new TransformRule(
            "相比(.+?)，(.+?)更(.+?)",
            "与$1相比，$2更为$3",
            "比较句式"
        ));
    }
    
    @Override
    public List<Map<String, String>> analyzeSentencePatterns(String text)
    {
        List<Map<String, String>> suggestions = new ArrayList<>();
        
        // 按句号分句
        String[] sentences = text.split("[。！？]");
        
        for (String sentence : sentences) {
            if (sentence.trim().isEmpty()) {
                continue;
            }
            
            // 对每个句子应用变换规则
            for (TransformRule rule : transformRules) {
                Pattern pattern = Pattern.compile(rule.getPattern());
                Matcher matcher = pattern.matcher(sentence);
                
                if (matcher.find()) {
                    Map<String, String> suggestion = new HashMap<>();
                    suggestion.put("original", sentence);
                    suggestion.put("transformed", matcher.replaceFirst(rule.getReplacement()));
                    suggestion.put("type", rule.getType());
                    suggestions.add(suggestion);
                    break; // 每个句子只应用一个规则
                }
            }
        }
        
        return suggestions;
    }
    
    @Override
    public List<String> transformSentence(String sentence)
    {
        List<String> transformations = new ArrayList<>();
        
        for (TransformRule rule : transformRules) {
            Pattern pattern = Pattern.compile(rule.getPattern());
            Matcher matcher = pattern.matcher(sentence);
            
            if (matcher.find()) {
                String transformed = matcher.replaceFirst(rule.getReplacement());
                transformations.add(transformed);
            }
        }
        
        return transformations;
    }
    
    @Override
    public String applyTransformations(String text, Map<String, String> transformations)
    {
        String result = text;
        
        // 按原句长度降序排序，避免短句先替换导致长句无法匹配
        List<String> sortedKeys = new ArrayList<>(transformations.keySet());
        sortedKeys.sort((a, b) -> b.length() - a.length());
        
        for (String original : sortedKeys) {
            String transformed = transformations.get(original);
            if (transformed != null && !transformed.isEmpty()) {
                result = result.replace(original, transformed);
            }
        }
        
        return result;
    }
    
    @Override
    public String autoTransform(String text, double transformRatio)
    {
        if (transformRatio <= 0 || transformRatio > 1.0) {
            return text;
        }
        
        List<Map<String, String>> suggestions = analyzeSentencePatterns(text);
        Map<String, String> transformations = new HashMap<>();
        Random random = new Random();
        
        // 随机选择要变换的句子
        for (Map<String, String> suggestion : suggestions) {
            if (random.nextDouble() < transformRatio) {
                transformations.put(
                    suggestion.get("original"),
                    suggestion.get("transformed")
                );
            }
        }
        
        return applyTransformations(text, transformations);
    }
    
    @Override
    public List<String> detectUniformPatterns(String text)
    {
        List<String> uniformSentences = new ArrayList<>();
        String[] sentences = text.split("[。！？]");
        
        // 统计句子长度
        Map<Integer, Integer> lengthCount = new HashMap<>();
        for (String sentence : sentences) {
            int length = sentence.trim().length();
            if (length > 0) {
                lengthCount.put(length, lengthCount.getOrDefault(length, 0) + 1);
            }
        }
        
        // 找出过于统一的句子长度
        for (Map.Entry<Integer, Integer> entry : lengthCount.entrySet()) {
            if (entry.getValue() >= 3) { // 3个或以上相同长度
                for (String sentence : sentences) {
                    if (sentence.trim().length() == entry.getKey()) {
                        uniformSentences.add(sentence.trim());
                    }
                }
            }
        }
        
        return uniformSentences;
    }
    
    @Override
    public List<String> getAllTransformRules()
    {
        List<String> rules = new ArrayList<>();
        for (TransformRule rule : transformRules) {
            rules.add(rule.getType() + ": " + rule.getDescription());
        }
        return rules;
    }
    
    /**
     * 句式变换规则内部类
     */
    private static class TransformRule
    {
        private String pattern;
        private String replacement;
        private String type;
        
        public TransformRule(String pattern, String replacement, String type)
        {
            this.pattern = pattern;
            this.replacement = replacement;
            this.type = type;
        }
        
        public String getPattern()
        {
            return pattern;
        }
        
        public String getReplacement()
        {
            return replacement;
        }
        
        public String getType()
        {
            return type;
        }
        
        public String getDescription()
        {
            return pattern + " → " + replacement;
        }
    }
}
