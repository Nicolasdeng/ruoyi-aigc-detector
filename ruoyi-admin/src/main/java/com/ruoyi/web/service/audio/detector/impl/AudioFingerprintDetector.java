package com.ruoyi.web.service.audio.detector.impl;

import com.ruoyi.web.service.audio.detector.IAudioDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * 音频指纹匹配检测器
 * 基于AI语音平台的特征指纹进行检测
 * 
 * 检测策略：
 * 1. URL/文件名关键词匹配
 * 2. 元数据特征匹配
 * 3. AI平台指纹库匹配
 * 4. 生成模型特征识别
 * 
 * @author ruoyi
 */
@Component
public class AudioFingerprintDetector implements IAudioDetector {
    
    private static final Logger log = LoggerFactory.getLogger(AudioFingerprintDetector.class);
    
    private static final String DETECTOR_NAME = "指纹匹配检测器";
    
    // AI语音平台关键词库
    private static final Set<String> AI_PLATFORM_KEYWORDS = new HashSet<>(Arrays.asList(
        "elevenlabs", "eleven", "speechify", "wellsaid", "resemble",
        "murf", "play.ht", "descript", "overdub", "lovo",
        "synthesys", "voice", "ai", "tts", "text-to-speech",
        "synthetic", "generated", "azure", "google-tts", "amazon-polly",
        "ibm-watson", "nuance", "voicery", "replica", "sonantic"
    ));
    
    // TTS模型特征关键词
    private static final Set<String> TTS_MODEL_KEYWORDS = new HashSet<>(Arrays.asList(
        "tacotron", "wavenet", "fastspeech", "transformer",
        "vits", "glow-tts", "mellotron", "flowtron"
    ));
    
    @Override
    public Map<String, Object> detect(String audioFilePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("detectorName", DETECTOR_NAME);
        
        try {
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                throw new Exception("音频文件不存在: " + audioFilePath);
            }
            
            String fileName = audioFile.getName().toLowerCase();
            String filePath = audioFile.getPath().toLowerCase();
            
            // 提取指纹特征
            Map<String, Object> features = extractFingerprintFeatures(fileName, filePath, audioFile);
            
            // 计算AI分数
            double aiScore = calculateAIScore(features);
            
            result.put("score", aiScore);
            result.put("isAI", aiScore > 0.6);
            result.put("confidence", Math.abs(aiScore - 0.5) * 2.0);
            result.put("features", features);
            
            log.debug("指纹匹配检测完成: {} - AI分数: {}", audioFilePath, aiScore);
            
        } catch (Exception e) {
            log.error("指纹匹配检测失败: " + audioFilePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("confidence", 0.0);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 提取指纹特征
     */
    private Map<String, Object> extractFingerprintFeatures(String fileName, String filePath, File audioFile) {
        Map<String, Object> features = new HashMap<>();
        
        // 1. 文件名关键词匹配
        Map<String, Object> filenameFeatures = analyzeFilename(fileName, filePath);
        features.put("filename", filenameFeatures);
        
        // 2. 文件元数据分析
        Map<String, Object> metadataFeatures = analyzeMetadata(audioFile);
        features.put("metadata", metadataFeatures);
        
        // 3. 平台指纹匹配
        Map<String, Object> platformFeatures = matchPlatformFingerprint(fileName, filePath);
        features.put("platform", platformFeatures);
        
        return features;
    }
    
    /**
     * 分析文件名
     */
    private Map<String, Object> analyzeFilename(String fileName, String filePath) {
        List<String> matchedKeywords = new ArrayList<>();
        double matchScore = 0.0;
        
        // 检查AI平台关键词
        for (String keyword : AI_PLATFORM_KEYWORDS) {
            if (fileName.contains(keyword) || filePath.contains(keyword)) {
                matchedKeywords.add(keyword);
                matchScore += 0.15;
            }
        }
        
        // 检查TTS模型关键词
        for (String keyword : TTS_MODEL_KEYWORDS) {
            if (fileName.contains(keyword) || filePath.contains(keyword)) {
                matchedKeywords.add("model:" + keyword);
                matchScore += 0.10;
            }
        }
        
        // 检查文件名模式（AI生成音频的命名模式）
        if (fileName.matches(".*\\d{10,}.*")) { // 包含10位以上数字（时间戳）
            matchedKeywords.add("timestamp_pattern");
            matchScore += 0.05;
        }
        
        if (fileName.matches(".*[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}.*")) { // UUID模式
            matchedKeywords.add("uuid_pattern");
            matchScore += 0.05;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("matchedKeywords", matchedKeywords);
        result.put("matchCount", matchedKeywords.size());
        result.put("matchScore", Math.min(matchScore, 1.0));
        result.put("aiIndicator", matchScore > 0.2);
        
        return result;
    }
    
    /**
     * 分析文件元数据
     */
    private Map<String, Object> analyzeMetadata(File audioFile) {
        double suspicionScore = 0.0;
        List<String> indicators = new ArrayList<>();
        
        long fileSize = audioFile.length();
        String extension = getFileExtension(audioFile.getName()).toLowerCase();
        
        // 检查文件大小（AI生成音频通常较小且大小规律）
        if (fileSize < 500 * 1024) { // 小于500KB
            suspicionScore += 0.1;
            indicators.add("small_file_size");
        }
        
        // 检查文件扩展名（MP3格式在AI TTS中最常见）
        if (extension.equals("mp3")) {
            suspicionScore += 0.05;
            indicators.add("mp3_format");
        } else if (extension.equals("wav") && fileSize < 1024 * 1024) {
            suspicionScore += 0.08;
            indicators.add("small_wav_format");
        }
        
        // 检查文件修改时间（刚生成的文件）
        long lastModified = audioFile.lastModified();
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastModified;
        
        if (timeDiff < 60 * 1000) { // 1分钟内创建
            suspicionScore += 0.05;
            indicators.add("recently_created");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("fileSize", fileSize);
        result.put("extension", extension);
        result.put("indicators", indicators);
        result.put("suspicionScore", Math.min(suspicionScore, 1.0));
        result.put("aiIndicator", suspicionScore > 0.15);
        
        return result;
    }
    
    /**
     * 匹配平台指纹
     */
    private Map<String, Object> matchPlatformFingerprint(String fileName, String filePath) {
        Map<String, Object> result = new HashMap<>();
        List<String> matchedPlatforms = new ArrayList<>();
        double confidenceScore = 0.0;
        
        // ElevenLabs指纹
        if (containsAny(fileName + filePath, "elevenlabs", "eleven", "11labs")) {
            matchedPlatforms.add("ElevenLabs");
            confidenceScore = Math.max(confidenceScore, 0.9);
        }
        
        // Speechify指纹
        if (containsAny(fileName + filePath, "speechify")) {
            matchedPlatforms.add("Speechify");
            confidenceScore = Math.max(confidenceScore, 0.85);
        }
        
        // WellSaid Labs指纹
        if (containsAny(fileName + filePath, "wellsaid", "well-said")) {
            matchedPlatforms.add("WellSaid Labs");
            confidenceScore = Math.max(confidenceScore, 0.85);
        }
        
        // Google TTS指纹
        if (containsAny(fileName + filePath, "google", "gtts", "wavenet")) {
            matchedPlatforms.add("Google TTS");
            confidenceScore = Math.max(confidenceScore, 0.8);
        }
        
        // Amazon Polly指纹
        if (containsAny(fileName + filePath, "amazon", "polly", "aws")) {
            matchedPlatforms.add("Amazon Polly");
            confidenceScore = Math.max(confidenceScore, 0.8);
        }
        
        // Azure TTS指纹
        if (containsAny(fileName + filePath, "azure", "microsoft", "cognitive")) {
            matchedPlatforms.add("Azure TTS");
            confidenceScore = Math.max(confidenceScore, 0.8);
        }
        
        result.put("matchedPlatforms", matchedPlatforms);
        result.put("platformCount", matchedPlatforms.size());
        result.put("confidenceScore", confidenceScore);
        result.put("aiIndicator", !matchedPlatforms.isEmpty());
        
        return result;
    }
    
    /**
     * 检查字符串是否包含任一关键词
     */
    private boolean containsAny(String text, String... keywords) {
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * 计算AI分数
     */
    private double calculateAIScore(Map<String, Object> features) {
        double score = 0.5;
        
        // 文件名匹配
        @SuppressWarnings("unchecked")
        Map<String, Object> filename = (Map<String, Object>) features.get("filename");
        double filenameScore = (double) filename.get("matchScore");
        boolean filenameAI = (boolean) filename.get("aiIndicator");
        score += filenameScore * 0.35;
        
        // 元数据分析
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) features.get("metadata");
        double metadataScore = (double) metadata.get("suspicionScore");
        boolean metadataAI = (boolean) metadata.get("aiIndicator");
        score += metadataScore * 0.25;
        
        // 平台指纹
        @SuppressWarnings("unchecked")
        Map<String, Object> platform = (Map<String, Object>) features.get("platform");
        double platformScore = (double) platform.get("confidenceScore");
        boolean platformAI = (boolean) platform.get("aiIndicator");
        score += platformScore * 0.40;
        
        // 如果明确匹配到平台指纹，大幅提高分数
        if (platformAI && platformScore > 0.7) {
            score = Math.min(score + 0.15, 0.98);
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public String getDetectorName() {
        return DETECTOR_NAME;
    }
    
    @Override
    public double getWeight(String mode) {
        switch (mode.toLowerCase()) {
            case "fast":
                return 0.30;
            case "standard":
                return 0.15;
            case "deep":
                return 0.15;
            default:
                return 0.20;
        }
    }
    
    @Override
    public boolean isEnabledForMode(String mode) {
        return true;
    }
}
