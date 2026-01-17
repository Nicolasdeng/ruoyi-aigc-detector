package com.ruoyi.web.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.web.domain.AiDetectionRecord;
import com.ruoyi.web.mapper.AiDetectionMapper;
import com.ruoyi.web.service.IAiAudioDetectionService;
import com.ruoyi.web.service.audio.IAudioAiModelDetector;
import com.ruoyi.web.service.audio.AudioModelDetectionResult;
import com.ruoyi.web.service.audio.util.AudioFeatureAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * AI音频检测Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class AiAudioDetectionServiceImpl implements IAiAudioDetectionService {
    
    private static final Logger log = LoggerFactory.getLogger(AiAudioDetectionServiceImpl.class);

    @Autowired
    private AiDetectionMapper aiDetectionMapper;
    
    @Autowired
    private List<IAudioAiModelDetector> audioAiModelDetectors;
    
    @Autowired
    private AudioFeatureAnalyzer audioFeatureAnalyzer;

    @Value("${ai.detection.huggingface.token:}")
    private String huggingfaceToken;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();
    
    // 并行执行AI模型检测的线程池
    private final ExecutorService detectorExecutor = Executors.newFixedThreadPool(8);

    /**
     * 检测上传的音频（支持检测模式选择）
     */
    @Override
    public AiDetectionRecord detectAudio(MultipartFile file, String mode, Long userId) throws Exception {
        // 验证检测模式
        if (mode == null || mode.isEmpty()) {
            mode = "standard";
        }
        if (!Arrays.asList("fast", "standard", "deep").contains(mode)) {
            throw new IllegalArgumentException("无效的检测模式: " + mode + "，仅支持 fast、standard、deep");
        }
        
        // 上传文件
        String fileName = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        String fileUrl = fileName; // 数据库存储相对路径
        String fullPath = RuoYiConfig.getUploadPath() + fileName; // 实际文件路径
        
        // 创建检测记录
        AiDetectionRecord record = new AiDetectionRecord();
        record.setUserId(userId);
        record.setFileUrl(fileUrl);
        record.setFileType("audio");
        record.setFileSize(file.getSize());
        record.setStatus("PROCESSING");
        aiDetectionMapper.insertRecord(record);
        
        try {
            // 根据检测模式执行不同级别的检测
            Map<String, Object> detectionResults = performMultiApiDetection(fullPath, mode);
            
            // 更新记录
            record.setDetectionResult((String) detectionResults.get("result"));
            record.setConfidenceScore((BigDecimal) detectionResults.get("confidence"));
            record.setDetectionDetails(JSON.toJSONString(detectionResults.get("details")));
            record.setApiResults(JSON.toJSONString(detectionResults.get("apiResults")));
            record.setStatus("COMPLETED");
            
            aiDetectionMapper.updateRecord(record);
            
            log.info("音频检测完成，用户ID: {} - 文件: {} - 结果: {} - 置信度: {}", 
                    userId, fileUrl, record.getDetectionResult(), record.getConfidenceScore());
            
        } catch (Exception e) {
            log.error("音频检测失败: " + fileUrl, e);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            aiDetectionMapper.updateRecord(record);
            throw e;
        }
        
        return record;
    }

    /**
     * 通过URL检测音频（支持检测模式选择）
     */
    @Override
    public AiDetectionRecord detectAudioByUrl(String audioUrl, String mode, Long userId) throws Exception {
        // 验证检测模式
        if (mode == null || mode.isEmpty()) {
            mode = "standard";
        }
        if (!Arrays.asList("fast", "standard", "deep").contains(mode)) {
            throw new IllegalArgumentException("无效的检测模式: " + mode + "，仅支持 fast、standard、deep");
        }
        
        // 创建检测记录
        AiDetectionRecord record = new AiDetectionRecord();
        record.setUserId(userId);
        record.setFileUrl(audioUrl);
        record.setFileType("audio");
        record.setStatus("PROCESSING");
        aiDetectionMapper.insertRecord(record);
        
        try {
            // 根据检测模式执行不同级别的检测
            Map<String, Object> detectionResults = performMultiApiDetectionByUrl(audioUrl, mode);
            
            // 更新记录
            record.setDetectionResult((String) detectionResults.get("result"));
            record.setConfidenceScore((BigDecimal) detectionResults.get("confidence"));
            record.setDetectionDetails(JSON.toJSONString(detectionResults.get("details")));
            record.setApiResults(JSON.toJSONString(detectionResults.get("apiResults")));
            record.setStatus("COMPLETED");
            
            aiDetectionMapper.updateRecord(record);
            
            log.info("音频URL检测完成，用户ID: {} - URL: {} - 结果: {} - 置信度: {}", 
                    userId, audioUrl, record.getDetectionResult(), record.getConfidenceScore());
            
        } catch (Exception e) {
            log.error("音频URL检测失败: " + audioUrl, e);
            record.setStatus("FAILED");
            record.setErrorMessage(e.getMessage());
            aiDetectionMapper.updateRecord(record);
            throw e;
        }
        
        return record;
    }

    /**
     * 执行多API检测（本地文件）
     * @param filePath 文件路径
     * @param mode 检测模式：fast(快速)、standard(标准)、deep(深度)
     */
    private Map<String, Object> performMultiApiDetection(String filePath, String mode) throws Exception {
        List<Map<String, Object>> apiResults = new ArrayList<>();
        
        // 第一步：执行基础检测（30%权重）
        Map<String, Object> basicDetectionResults = performBasicDetection(filePath, mode);
        apiResults.addAll((List<Map<String, Object>>) basicDetectionResults.get("results"));
        double basicScore = (double) basicDetectionResults.get("score");
        
        // 第二步：执行AI模型检测（70%权重）
        Map<String, Object> aiModelResults = performAiModelDetection(filePath);
        apiResults.addAll((List<Map<String, Object>>) aiModelResults.get("results"));
        double aiModelScore = (double) aiModelResults.get("score");
        String detectedModel = (String) aiModelResults.get("detectedModel");
        
        // 第三步：加权融合结果
        double finalScore = basicScore * 0.3 + aiModelScore * 0.7;
        
        // 汇总结果
        return aggregateResultsWithAiModel(apiResults, mode, finalScore, detectedModel, 
                                           basicScore, aiModelScore);
    }
    
    /**
     * 执行基础检测（声纹、元数据、频谱）
     */
    private Map<String, Object> performBasicDetection(String filePath, String mode) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 根据检测模式决定使用哪些API
        switch (mode) {
            case "fast":
                // 快速模式：仅使用音频元数据分析
                try {
                    Map<String, Object> metadataResult = detectWithAudioMetadata(filePath);
                    results.add(metadataResult);
                    log.info("快速模式-音频元数据分析完成: {}", metadataResult);
                } catch (Exception e) {
                    log.warn("音频元数据分析失败: {}", e.getMessage());
                }
                break;
                
            case "standard":
                // 标准模式：使用声纹特征分析 + 音频元数据分析
                try {
                    Map<String, Object> voiceprintResult = detectWithVoiceprint(filePath);
                    results.add(voiceprintResult);
                    log.info("标准模式-声纹特征分析完成: {}", voiceprintResult);
                } catch (Exception e) {
                    log.warn("声纹特征分析失败: {}", e.getMessage());
                }
                
                try {
                    Map<String, Object> metadataResult = detectWithAudioMetadata(filePath);
                    results.add(metadataResult);
                    log.info("标准模式-音频元数据分析完成: {}", metadataResult);
                } catch (Exception e) {
                    log.warn("音频元数据分析失败: {}", e.getMessage());
                }
                break;
                
            case "deep":
                // 深度模式：使用所有检测API
                try {
                    Map<String, Object> voiceprintResult = detectWithVoiceprint(filePath);
                    results.add(voiceprintResult);
                    log.info("深度模式-声纹特征分析完成: {}", voiceprintResult);
                } catch (Exception e) {
                    log.warn("声纹特征分析失败: {}", e.getMessage());
                }
                
                try {
                    Map<String, Object> metadataResult = detectWithAudioMetadata(filePath);
                    results.add(metadataResult);
                    log.info("深度模式-音频元数据分析完成: {}", metadataResult);
                } catch (Exception e) {
                    log.warn("音频元数据分析失败: {}", e.getMessage());
                }
                
                try {
                    Map<String, Object> spectrumResult = detectWithSpectrum(filePath);
                    results.add(spectrumResult);
                    log.info("深度模式-频谱分析完成: {}", spectrumResult);
                } catch (Exception e) {
                    log.warn("频谱分析失败: {}", e.getMessage());
                }
                break;
        }
        
        // 计算基础检测的平均分数
        double avgScore = results.isEmpty() ? 0.5 : 
            results.stream()
                   .mapToDouble(r -> ((Number) r.getOrDefault("score", 0.5)).doubleValue())
                   .average()
                   .orElse(0.5);
        
        Map<String, Object> basicResults = new HashMap<>();
        basicResults.put("results", results);
        basicResults.put("score", avgScore);
        return basicResults;
    }
    
    /**
     * 执行AI模型检测（并行调用8个检测器）
     */
    private Map<String, Object> performAiModelDetection(String filePath) {
        log.info("开始AI模型检测，文件路径: {}", filePath);
        
        // 提取音频特征（使用静态方法）
        Map<String, Object> audioFeatures = AudioFeatureAnalyzer.analyzeAllFeatures(filePath);
        
        // 并行调用所有检测器
        List<CompletableFuture<AudioModelDetectionResult>> futures = audioAiModelDetectors.stream()
            .map(detector -> CompletableFuture.supplyAsync(() -> {
                try {
                    AudioModelDetectionResult result = detector.detect(filePath, audioFeatures);
                    log.info("检测器 {} 完成，分数: {}", detector.getDetectorName(), result.getScore());
                    return result;
                } catch (Exception e) {
                    log.error("检测器 {} 执行失败", detector.getDetectorName(), e);
                    return AudioModelDetectionResult.failure(
                        detector.getDetectorName(), 
                        "检测失败: " + e.getMessage()
                    );
                }
            }, detectorExecutor))
            .collect(Collectors.toList());
        
        // 等待所有检测器完成
        List<AudioModelDetectionResult> detectionResults = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        // 转换为Map格式（兼容原有结构）
        List<Map<String, Object>> apiResults = new ArrayList<>();
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        String topModel = null;
        double topScore = 0.0;
        
        for (int i = 0; i < detectionResults.size(); i++) {
            AudioModelDetectionResult result = detectionResults.get(i);
            IAudioAiModelDetector detector = audioAiModelDetectors.get(i);
            
            if (!result.isSuccess()) {
                log.warn("跳过失败的检测结果: {}", result.getErrorMessage());
                continue;
            }
            
            Map<String, Object> apiResult = new HashMap<>();
            apiResult.put("apiName", result.getModelName() + "检测");
            apiResult.put("weight", detector.getWeight());
            apiResult.put("score", result.getScore());
            apiResult.put("isAI", result.getScore() > 0.6);
            apiResult.put("modelName", result.getModelName());
            apiResult.put("details", result.getDetails());
            apiResults.add(apiResult);
            
            // 计算加权分数
            totalWeightedScore += result.getScore() * detector.getWeight();
            totalWeight += detector.getWeight();
            
            // 记录最高分数的模型
            if (result.getScore() > topScore) {
                topScore = result.getScore();
                topModel = result.getModelName();
            }
        }
        
        // 计算平均AI分数
        double avgAiScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.5;
        
        Map<String, Object> aiModelResults = new HashMap<>();
        aiModelResults.put("results", apiResults);
        aiModelResults.put("score", avgAiScore);
        aiModelResults.put("detectedModel", topModel);
        aiModelResults.put("topScore", topScore);
        
        log.info("AI模型检测完成，平均分数: {}, 最可能模型: {} (分数: {})", 
                 avgAiScore, topModel, topScore);
        
        return aiModelResults;
    }

    /**
     * 执行多API检测（URL）
     * @param audioUrl 音频URL
     * @param mode 检测模式：fast(快速)、standard(标准)、deep(深度)
     */
    private Map<String, Object> performMultiApiDetectionByUrl(String audioUrl, String mode) throws Exception {
        List<Map<String, Object>> apiResults = new ArrayList<>();
        
        // URL检测目前只支持启发式检测，所有模式使用相同逻辑
        try {
            Map<String, Object> heuristicResult = detectWithHeuristics(audioUrl);
            apiResults.add(heuristicResult);
            log.info("URL启发式检测完成: {}", heuristicResult);
        } catch (Exception e) {
            log.warn("URL启发式检测失败: {}", e.getMessage());
        }
        
        // 汇总结果
        return aggregateResults(apiResults, mode);
    }

    /**
     * 声纹特征分析
     * 分析音频的声纹特征，检测AI合成痕迹
     */
    private Map<String, Object> detectWithVoiceprint(String filePath) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", "声纹特征分析");
        result.put("weight", 0.50);
        
        try {
            File audioFile = new File(filePath);
            
            // 获取音频信息
            Map<String, Object> audioInfo = getAudioInfo(audioFile);
            double duration = (double) audioInfo.getOrDefault("duration", 30.0);
            int sampleRate = (int) audioInfo.getOrDefault("sampleRate", 44100);
            
            // 模拟声纹特征分析
            // 实际项目中应使用音频处理库（如librosa的Java替代品）提取MFCC等特征
            List<Map<String, String>> features = new ArrayList<>();
            
            // 特征1: 音调变化的自然度
            double pitchNaturalness = 0.3 + Math.random() * 0.5; // 0.3-0.8
            features.add(Map.of("feature", "音调自然度", "score", String.format("%.2f", pitchNaturalness)));
            
            // 特征2: 音色一致性
            double timbreConsistency = 0.4 + Math.random() * 0.4; // 0.4-0.8
            features.add(Map.of("feature", "音色一致性", "score", String.format("%.2f", timbreConsistency)));
            
            // 特征3: 呼吸声和停顿
            double breathingNaturalness = 0.3 + Math.random() * 0.5; // 0.3-0.8
            features.add(Map.of("feature", "呼吸自然度", "score", String.format("%.2f", breathingNaturalness)));
            
            // 计算综合AI分数
            double avgAiScore = (pitchNaturalness + timbreConsistency + breathingNaturalness) / 3.0;
            
            result.put("score", avgAiScore);
            result.put("isAI", avgAiScore > 0.6);
            result.put("details", Map.of(
                "duration", duration,
                "sampleRate", sampleRate,
                "features", features,
                "avgAiScore", avgAiScore
            ));
            
        } catch (Exception e) {
            log.error("声纹特征分析失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取音频基本信息
     * 注意：这是简化版本，实际项目中应使用音频处理库获取准确信息
     */
    private Map<String, Object> getAudioInfo(File audioFile) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            long fileSize = audioFile.length();
            String fileName = audioFile.getName().toLowerCase();
            
            // 根据文件大小和格式估算音频时长和采样率
            // 实际项目中应使用音频处理库获取准确信息
            double estimatedDuration = fileSize / (1024.0 * 10); // 粗略估算
            int sampleRate = 44100; // 默认采样率
            
            // 根据文件扩展名调整
            if (fileName.endsWith(".mp3")) {
                sampleRate = 44100;
            } else if (fileName.endsWith(".wav")) {
                sampleRate = 48000;
            } else if (fileName.endsWith(".m4a")) {
                sampleRate = 44100;
            }
            
            info.put("fileSize", fileSize);
            info.put("duration", Math.min(estimatedDuration, 300.0)); // 限制最大5分钟
            info.put("sampleRate", sampleRate);
            
        } catch (Exception e) {
            log.warn("获取音频信息失败", e);
            info.put("duration", 30.0);
            info.put("sampleRate", 44100);
        }
        
        return info;
    }

    /**
     * 音频元数据分析
     */
    private Map<String, Object> detectWithAudioMetadata(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", "音频元数据分析");
        result.put("weight", 0.30);
        
        try {
            File audioFile = new File(filePath);
            String fileName = audioFile.getName().toLowerCase();
            long fileSize = audioFile.length();
            
            // 简单的元数据分析
            double aiProbability = 0.5;
            List<String> indicators = new ArrayList<>();
            
            // 检查文件扩展名
            String extension = "";
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                extension = fileName.substring(lastDot);
            }
            
            // MP3格式更常见于AI合成音频
            if (".mp3".equals(extension)) {
                aiProbability += 0.05;
                indicators.add("MP3格式");
            }
            
            // 检查文件大小（AI合成音频通常较小且大小规律）
            if (fileSize < 1 * 1024 * 1024) { // 小于1MB
                aiProbability += 0.1;
                indicators.add("文件较小");
            }
            
            // 检查文件名关键词
            if (fileName.contains("ai") || fileName.contains("tts") || 
                fileName.contains("synthetic") || fileName.contains("generated") ||
                fileName.contains("elevenlabs") || fileName.contains("speechify")) {
                aiProbability += 0.25;
                indicators.add("文件名包含AI/TTS关键词");
            }
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileName", fileName);
            metadata.put("fileSize", fileSize);
            metadata.put("extension", extension);
            metadata.put("lastModified", new Date(audioFile.lastModified()));
            
            result.put("score", Math.min(aiProbability, 1.0));
            result.put("isAI", aiProbability > 0.6);
            result.put("indicators", indicators);
            result.put("metadata", metadata);
            
        } catch (Exception e) {
            log.error("音频元数据分析失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 频谱分析
     * 分析音频频谱特征，检测AI生成特征
     */
    private Map<String, Object> detectWithSpectrum(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", "频谱分析");
        result.put("weight", 0.20);
        
        try {
            File audioFile = new File(filePath);
            
            // 模拟频谱分析
            // 实际项目中应使用FFT等算法分析频谱
            double aiProbability = 0.5;
            List<String> features = new ArrayList<>();
            
            // 特征1: 高频成分异常
            double highFreqAnomaly = Math.random();
            if (highFreqAnomaly > 0.6) {
                aiProbability += 0.15;
                features.add("高频成分异常");
            }
            
            // 特征2: 频谱过于平滑
            double spectrumSmoothness = Math.random();
            if (spectrumSmoothness > 0.7) {
                aiProbability += 0.1;
                features.add("频谱过于规律");
            }
            
            // 特征3: 缺少环境噪声
            double ambientNoise = Math.random();
            if (ambientNoise < 0.3) {
                aiProbability += 0.1;
                features.add("缺少自然环境噪声");
            }
            
            result.put("score", Math.min(aiProbability, 1.0));
            result.put("isAI", aiProbability > 0.6);
            result.put("features", features);
            
        } catch (Exception e) {
            log.error("频谱分析失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * URL启发式检测
     */
    private Map<String, Object> detectWithHeuristics(String audioUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", "URL启发式检测");
        result.put("weight", 0.40);
        
        try {
            String urlLower = audioUrl.toLowerCase();
            double aiProbability = 0.5;
            List<String> indicators = new ArrayList<>();
            
            // 检查URL中的AI相关关键词
            if (urlLower.contains("elevenlabs") || urlLower.contains("speechify") ||
                urlLower.contains("wellsaidlabs") || urlLower.contains("resemble") ||
                urlLower.contains("tts") || urlLower.contains("text-to-speech") ||
                urlLower.contains("ai-voice") || urlLower.contains("synthetic")) {
                aiProbability += 0.35;
                indicators.add("URL包含AI语音合成平台或TTS关键词");
            }
            
            // 检查URL中的音频格式
            if (urlLower.endsWith(".mp3") || urlLower.endsWith(".wav") || urlLower.endsWith(".m4a")) {
                aiProbability += 0.05;
                indicators.add("常见音频格式");
            }
            
            result.put("score", Math.min(aiProbability, 1.0));
            result.put("isAI", aiProbability > 0.6);
            result.put("indicators", indicators);
            
        } catch (Exception e) {
            log.error("URL启发式检测失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 汇总包含AI模型检测的结果
     * @param apiResults API检测结果列表
     * @param mode 检测模式
     * @param finalScore 最终加权分数
     * @param detectedModel 检测到的AI模型
     * @param basicScore 基础检测分数
     * @param aiModelScore AI模型检测分数
     */
    private Map<String, Object> aggregateResultsWithAiModel(
            List<Map<String, Object>> apiResults, String mode, double finalScore,
            String detectedModel, double basicScore, double aiModelScore) {
        
        if (apiResults.isEmpty()) {
            throw new RuntimeException("所有检测API均失败");
        }
        
        // 统计投票
        int aiCount = 0;
        int humanCount = 0;
        double maxScore = 0.0;
        double minScore = 1.0;
        
        for (Map<String, Object> apiResult : apiResults) {
            double score = ((Number) apiResult.getOrDefault("score", 0.5)).doubleValue();
            boolean isAI = (Boolean) apiResult.getOrDefault("isAI", false);
            
            maxScore = Math.max(maxScore, score);
            minScore = Math.min(minScore, score);
            
            if (isAI) {
                aiCount++;
            } else {
                humanCount++;
            }
        }
        
        // 优化的判定逻辑（融合AI模型检测结果）
        String result;
        BigDecimal confidence;
        
        // 所有检测器一致判定为AI且分数高（高可信度）
        if (aiCount == apiResults.size() && finalScore > 0.75) {
            result = "AI_GENERATED";
            confidence = BigDecimal.valueOf(Math.min(finalScore * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 大多数检测器判定为AI且分数高（中高可信度）
        else if (aiCount > humanCount && finalScore > 0.65) {
            result = "AI_GENERATED";
            confidence = BigDecimal.valueOf(finalScore * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // AI模型检测分数极高（强特征匹配）
        else if (aiModelScore > 0.80) {
            result = "AI_GENERATED";
            confidence = BigDecimal.valueOf(Math.min(aiModelScore * 100, 92.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 所有检测器一致判定为真实录音（高可信度）
        else if (humanCount == apiResults.size() && finalScore < 0.25) {
            result = "HUMAN_CREATED";
            confidence = BigDecimal.valueOf(Math.min((1 - finalScore) * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 大多数检测器判定为真实录音且分数低（中高可信度）
        else if (humanCount > aiCount && finalScore < 0.35) {
            result = "HUMAN_CREATED";
            confidence = BigDecimal.valueOf((1 - finalScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 其他情况为不确定
        else {
            result = "UNCERTAIN";
            // 不确定时显示偏向程度
            if (finalScore > 0.5) {
                confidence = BigDecimal.valueOf((finalScore - 0.5) * 100)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                confidence = BigDecimal.valueOf((0.5 - finalScore) * 100)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }
        
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("result", result);
        aggregated.put("confidence", confidence);
        Map<String, Object> details = new HashMap<>();
        details.put("mode", mode);
        details.put("finalScore", finalScore);
        details.put("basicScore", basicScore);
        details.put("aiModelScore", aiModelScore);
        details.put("detectedModel", detectedModel != null ? detectedModel : "未识别");
        details.put("apiCount", apiResults.size());
        details.put("aiVotes", aiCount);
        details.put("humanVotes", humanCount);
        details.put("maxScore", maxScore);
        details.put("minScore", minScore);
        details.put("scoreRange", maxScore - minScore);
        details.put("weightDistribution", "基础检测30% + AI模型检测70%");
        aggregated.put("details", details);
        aggregated.put("apiResults", apiResults);
        
        return aggregated;
    }
    
    /**
     * 汇总多个API的检测结果（保留原方法用于URL检测）
     * @param apiResults API检测结果列表
     * @param mode 检测模式
     */
    private Map<String, Object> aggregateResults(List<Map<String, Object>> apiResults, String mode) {
        if (apiResults.isEmpty()) {
            throw new RuntimeException("所有检测API均失败");
        }
        
        // 加权平均计算
        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        int aiCount = 0;
        int humanCount = 0;
        double maxScore = 0.0;
        double minScore = 1.0;
        
        for (Map<String, Object> apiResult : apiResults) {
            double weight = ((Number) apiResult.getOrDefault("weight", 1.0)).doubleValue();
            double score = ((Number) apiResult.getOrDefault("score", 0.5)).doubleValue();
            boolean isAI = (Boolean) apiResult.getOrDefault("isAI", false);
            
            totalWeightedScore += score * weight;
            totalWeight += weight;
            maxScore = Math.max(maxScore, score);
            minScore = Math.min(minScore, score);
            
            if (isAI) {
                aiCount++;
            } else {
                humanCount++;
            }
        }
        
        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.5;
        
        // 优化的判定逻辑
        String result;
        BigDecimal confidence;
        
        // 所有引擎一致判定为AI（高可信度）
        if (aiCount == apiResults.size() && finalScore > 0.75) {
            result = "AI_GENERATED";
            confidence = BigDecimal.valueOf(Math.min(finalScore * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 大多数引擎判定为AI且分数高（中高可信度）
        else if (aiCount > humanCount && finalScore > 0.65) {
            result = "AI_GENERATED";
            confidence = BigDecimal.valueOf(finalScore * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 所有引擎一致判定为真实录音（高可信度）
        else if (humanCount == apiResults.size() && finalScore < 0.25) {
            result = "HUMAN_CREATED";
            confidence = BigDecimal.valueOf(Math.min((1 - finalScore) * 100, 95.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 大多数引擎判定为真实录音且分数低（中高可信度）
        else if (humanCount > aiCount && finalScore < 0.35) {
            result = "HUMAN_CREATED";
            confidence = BigDecimal.valueOf((1 - finalScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        // 其他情况为不确定
        else {
            result = "UNCERTAIN";
            // 不确定时显示偏向程度
            if (finalScore > 0.5) {
                confidence = BigDecimal.valueOf((finalScore - 0.5) * 100)
                        .setScale(2, RoundingMode.HALF_UP);
            } else {
                confidence = BigDecimal.valueOf((0.5 - finalScore) * 100)
                        .setScale(2, RoundingMode.HALF_UP);
            }
        }
        
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("result", result);
        aggregated.put("confidence", confidence);
        aggregated.put("details", Map.of(
                "mode", mode,
                "finalScore", finalScore,
                "apiCount", apiResults.size(),
                "aiVotes", aiCount,
                "humanVotes", humanCount,
                "maxScore", maxScore,
                "minScore", minScore,
                "scoreRange", maxScore - minScore
        ));
        aggregated.put("apiResults", apiResults);
        
        return aggregated;
    }
}
