package com.ruoyi.web.service.image.detector.impl;

import ai.onnxruntime.*;
import com.ruoyi.web.service.image.detector.IImageDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地ONNX模型检测器
 * 使用ONNX Runtime在本地运行AI检测模型
 * 
 * @author ruoyi
 */
@Component
public class LocalOnnxDetector implements IImageDetector {
    
    private static final Logger log = LoggerFactory.getLogger(LocalOnnxDetector.class);
    
    @Value("${ai.detection.onnx.enabled:false}")
    private boolean enabled;
    
    @Value("${ai.detection.onnx.model-path:models/ai-image-detector/model.onnx}")
    private String modelPath;
    
    @Value("${ai.detection.onnx.input-size:224}")
    private int inputSize;
    
    private OrtEnvironment env;
    private OrtSession session;
    private boolean modelLoaded = false;
    
    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("ONNX本地模型检测器未启用");
            return;
        }
        
        try {
            log.info("开始加载ONNX模型: {}", modelPath);
            
            // 创建ONNX环境
            env = OrtEnvironment.getEnvironment();
            
            // 尝试从多个位置加载模型
            byte[] modelBytes = loadModelBytes();
            if (modelBytes == null) {
                log.warn("未找到ONNX模型文件，检测器将不可用");
                return;
            }
            
            // 创建会话选项
            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            
            // 创建会话
            session = env.createSession(modelBytes, sessionOptions);
            modelLoaded = true;
            
            log.info("ONNX模型加载成功！输入尺寸: {}x{}", inputSize, inputSize);
            log.info("模型输入: {}", session.getInputNames());
            log.info("模型输出: {}", session.getOutputNames());
            
        } catch (Exception e) {
            log.error("加载ONNX模型失败", e);
            modelLoaded = false;
        }
    }
    
    /**
     * 从多个位置尝试加载模型文件
     */
    private byte[] loadModelBytes() {
        try {
            // 尝试1: 从classpath加载
            try {
                ClassPathResource resource = new ClassPathResource(modelPath);
                if (resource.exists()) {
                    log.info("从classpath加载模型: {}", modelPath);
                    try (InputStream is = resource.getInputStream()) {
                        return is.readAllBytes();
                    }
                }
            } catch (Exception e) {
                log.debug("从classpath加载失败: {}", e.getMessage());
            }
            
            // 尝试2: 从绝对路径加载
            File modelFile = new File(modelPath);
            if (modelFile.exists()) {
                log.info("从文件系统加载模型: {}", modelPath);
                return java.nio.file.Files.readAllBytes(modelFile.toPath());
            }
            
            // 尝试3: 从项目根目录加载
            File rootModelFile = new File("models/ai-image-detector/model.onnx");
            if (rootModelFile.exists()) {
                log.info("从项目根目录加载模型: {}", rootModelFile.getAbsolutePath());
                return java.nio.file.Files.readAllBytes(rootModelFile.toPath());
            }
            
            log.warn("未找到模型文件，尝试的路径:");
            log.warn("  1. classpath: {}", modelPath);
            log.warn("  2. 绝对路径: {}", modelPath);
            log.warn("  3. 项目根目录: models/ai-image-detector/model.onnx");
            
            return null;
            
        } catch (Exception e) {
            log.error("加载模型文件失败", e);
            return null;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) {
                session.close();
            }
            if (env != null) {
                env.close();
            }
            log.info("ONNX资源已释放");
        } catch (Exception e) {
            log.error("释放ONNX资源失败", e);
        }
    }
    
    @Override
    public String getName() {
        return "ONNX本地模型";
    }
    
    @Override
    public double getWeight() {
        return 0.40; // 较高权重，因为是深度学习模型
    }
    
    @Override
    public Map<String, Object> detect(String filePath) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", modelLoaded);
        
        if (!modelLoaded) {
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", "模型未加载");
            return result;
        }
        
        try {
            // 1. 加载并预处理图片
            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                throw new Exception("无法读取图片");
            }
            
            // 2. 预处理图片
            float[] inputData = preprocessImage(image);
            
            // 3. 创建输入张量
            long[] shape = {1, 3, inputSize, inputSize};
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData), shape);
            
            // 4. 运行推理
            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put(session.getInputNames().iterator().next(), inputTensor);
            
            OrtSession.Result output = session.run(inputs);
            
            // 5. 解析输出
            float[] outputData = ((float[][]) output.get(0).getValue())[0];
            
            // 假设输出是 [human_score, ai_score]
            float humanScore = outputData[0];
            float aiScore = outputData[1];
            
            // 应用softmax
            float expHuman = (float) Math.exp(humanScore);
            float expAi = (float) Math.exp(aiScore);
            float sum = expHuman + expAi;
            
            float aiProbability = expAi / sum;
            float humanProbability = expHuman / sum;
            
            result.put("score", Math.round(aiProbability * 100) / 100.0);
            result.put("isAI", aiProbability > 0.5);
            result.put("aiScore", Math.round(aiProbability * 100) / 100.0);
            result.put("humanScore", Math.round(humanProbability * 100) / 100.0);
            result.put("confidence", Math.round(Math.max(aiProbability, humanProbability) * 100) / 100.0);
            
            // 清理
            inputTensor.close();
            output.close();
            
            log.info("ONNX检测完成 - AI概率: {}%, 人类概率: {}%", 
                    Math.round(aiProbability * 100), Math.round(humanProbability * 100));
            
        } catch (Exception e) {
            log.error("ONNX模型推理失败: " + filePath, e);
            result.put("score", 0.5);
            result.put("isAI", false);
            result.put("error", e.getMessage());
            result.put("available", false);
        }
        
        return result;
    }
    
    /**
     * 图片预处理：调整大小并标准化
     */
    private float[] preprocessImage(BufferedImage image) {
        // 1. 调整图片大小
        BufferedImage resized = new BufferedImage(inputSize, inputSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, inputSize, inputSize, null);
        graphics.dispose();
        
        // 2. 提取像素并标准化 (ImageNet标准: mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
        float[] data = new float[3 * inputSize * inputSize];
        
        float[] mean = {0.485f, 0.456f, 0.406f};
        float[] std = {0.229f, 0.224f, 0.225f};
        
        int[] pixels = resized.getRGB(0, 0, inputSize, inputSize, null, 0, inputSize);
        
        for (int i = 0; i < inputSize * inputSize; i++) {
            int pixel = pixels[i];
            
            // 提取RGB值 (0-255)
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // 归一化到 [0, 1]
            float rNorm = r / 255.0f;
            float gNorm = g / 255.0f;
            float bNorm = b / 255.0f;
            
            // 标准化 (ImageNet标准)
            data[i] = (rNorm - mean[0]) / std[0];  // R通道
            data[inputSize * inputSize + i] = (gNorm - mean[1]) / std[1];  // G通道
            data[2 * inputSize * inputSize + i] = (bNorm - mean[2]) / std[2];  // B通道
        }
        
        return data;
    }
    
    @Override
    public Map<String, Object> detectByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("apiName", getName());
        result.put("weight", getWeight());
        result.put("available", false);
        result.put("score", 0.5);
        result.put("isAI", false);
        result.put("error", "URL检测暂不支持");
        return result;
    }
    
    @Override
    public boolean isAvailable() {
        return enabled && modelLoaded;
    }
}
