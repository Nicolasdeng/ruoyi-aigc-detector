package com.ruoyi.web.service.image.detector.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Hugging Face AI检测器
 * 
 * @author ruoyi
 */
@Component
public class HuggingFaceDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(HuggingFaceDetector.class);
    
    private static final String API_URL = "https://api-inference.huggingface.co/models/umm-maybe/AI-image-detector";
    
    @Value("${ai.detection.huggingface.token:}")
    private String apiToken;
    
    @Value("${ai.detection.huggingface.enabled:true}")
    private boolean enabled;
    
    @Value("${ai.detection.proxy.enabled:false}")
    private boolean proxyEnabled;
    
    @Value("${ai.detection.proxy.host:127.0.0.1}")
    private String proxyHost;
    
    @Value("${ai.detection.proxy.port:7890}")
    private int proxyPort;
    
    @Value("${ai.detection.timeout:60}")
    private int timeout;
    
    private HttpClient httpClient;
    
    private HttpClient getHttpClient() {
        if (httpClient == null) {
            HttpClient.Builder builder = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(timeout));
            
            // 如果启用代理，配置代理
            if (proxyEnabled) {
                builder.proxy(ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort)));
                log.info("Hugging Face 检测器已启用代理: {}:{}", proxyHost, proxyPort);
            }
            
            httpClient = builder.build();
        }
        return httpClient;
    }
    
    @Override
    public String getName() {
        return "Hugging Face AI Detector";
    }
    
    @Override
    public double getWeight() {
        return 0.30;
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            File imageFile = new File(filePath);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                    .timeout(Duration.ofSeconds(timeout));
            
            // 如果配置了token则添加
            if (apiToken != null && !apiToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + apiToken);
            }
            
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                parseResponse(response.body(), result);
            } else {
                throw new Exception("API返回错误: " + response.statusCode() + " - " + response.body());
            }
            
        } catch (Exception e) {
            log.error("Hugging Face API调用失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> detectByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", true);
        
        try {
            // 先下载图片
            HttpRequest downloadRequest = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .GET()
                    .timeout(Duration.ofSeconds(timeout))
                    .build();
            
            HttpResponse<byte[]> downloadResponse = getHttpClient().send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());
            
            if (downloadResponse.statusCode() != 200) {
                throw new Exception("下载图片失败: " + downloadResponse.statusCode());
            }
            
            byte[] imageBytes = downloadResponse.body();
            
            // 调用检测API
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(imageBytes))
                    .timeout(Duration.ofSeconds(timeout));
            
            if (apiToken != null && !apiToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + apiToken);
            }
            
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                parseResponse(response.body(), result);
            } else {
                throw new Exception("API返回错误: " + response.statusCode());
            }
            
        } catch (Exception e) {
            log.error("Hugging Face API调用失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        // 检查是否启用
        return enabled;
    }
    
    /**
     * 解析API响应
     */
    private void parseResponse(String responseBody, Map<String, Object> result) {
        try {
            JSONArray jsonResponse = JSON.parseArray(responseBody);
            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                JSONArray labels = jsonResponse.getJSONArray(0);
                
                double aiScore = 0.0;
                double humanScore = 0.0;
                
                for (int i = 0; i < labels.size(); i++) {
                    JSONObject labelObj = labels.getJSONObject(i);
                    String label = labelObj.getString("label");
                    double score = labelObj.getDoubleValue("score");
                    
                    if (label.toLowerCase().contains("artificial") || 
                        label.toLowerCase().contains("fake") ||
                        label.toLowerCase().contains("ai")) {
                        aiScore = Math.max(aiScore, score);
                    } else if (label.toLowerCase().contains("human") || 
                               label.toLowerCase().contains("real")) {
                        humanScore = Math.max(humanScore, score);
                    }
                }
                
                result.put("score", aiScore > humanScore ? aiScore : (1 - humanScore));
                result.put("isAI", aiScore > humanScore);
                result.put("details", labels);
                result.put("aiScore", aiScore);
                result.put("humanScore", humanScore);
            } else {
                result.put("score", 0.5);
                result.put("isAI", false);
                result.put("details", "无法解析响应");
            }
        } catch (Exception e) {
            log.error("解析Hugging Face响应失败", e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", "解析失败: " + e.getMessage());
        }
    }
}
