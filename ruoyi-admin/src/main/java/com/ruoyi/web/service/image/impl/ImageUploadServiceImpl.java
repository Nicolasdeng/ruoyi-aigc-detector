package com.ruoyi.web.service.image.impl;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.web.service.image.IImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片上传服务实现
 * 
 * @author ruoyi
 */
@Service
public class ImageUploadServiceImpl implements IImageUploadService {
    
    private static final Logger log = LoggerFactory.getLogger(ImageUploadServiceImpl.class);
    
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
    
    @Override
    public Map<String, Object> uploadImage(MultipartFile file) throws Exception {
        // 验证文件
        if (!validateImage(file)) {
            throw new IllegalArgumentException("图片文件验证失败");
        }
        
        // 上传文件，获取相对路径（格式：/profile/upload/2025/12/29/xxx.png）
        String fileName = FileUploadUtils.upload(RuoYiConfig.getUploadPath(), file);
        
        // 构建完整的文件系统路径
        // fileName格式: /profile/upload/2025/12/29/xxx.png
        // RuoYiConfig.getUploadPath()格式: D:/ruoyi/uploadPath 或 D:/ruoyi/uploadPath/upload
        // 需要正确处理路径拼接，避免重复的upload目录
        String fullPath;
        if (fileName.startsWith("/profile/upload/")) {
            // 去掉/profile/upload/，得到相对路径（如：2025/12/29/xxx.png）
            String relativePath = fileName.substring("/profile/upload/".length());
            // 统一使用File.separator处理路径分隔符
            fullPath = RuoYiConfig.getUploadPath() + File.separator + relativePath.replace("/", File.separator);
        } else if (fileName.startsWith("/profile/")) {
            // 去掉/profile/，得到相对于uploadPath的路径
            String relativePath = fileName.substring("/profile/".length());
            fullPath = RuoYiConfig.getUploadPath() + File.separator + relativePath.replace("/", File.separator);
        } else {
            // 如果不是/profile开头，直接拼接（兼容性处理）
            fullPath = RuoYiConfig.getUploadPath() + File.separator + fileName.replace("/", File.separator);
        }
        
        // 获取图片信息
        Map<String, Object> imageInfo = getImageInfo(fullPath);
        
        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("fileUrl", fileName); // 相对路径（用于前端访问）
        result.put("fullPath", fullPath); // 绝对路径（用于后端处理）
        result.put("fileSize", file.getSize());
        result.put("originalName", file.getOriginalFilename());
        result.putAll(imageInfo);
        
        log.info("图片上传成功 - 访问路径: {}, 物理路径: {}, 大小: {} bytes", fileName, fullPath, file.getSize());
        
        return result;
    }
    
    @Override
    public boolean validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("文件为空");
            return false;
        }
        
        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("文件大小超过限制: {} bytes", file.getSize());
            return false;
        }
        
        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.warn("文件名为空");
            return false;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("不支持的文件格式: {}", extension);
            return false;
        }
        
        // 检查文件内容类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("无效的文件内容类型: {}", contentType);
            return false;
        }
        
        return true;
    }
    
    @Override
    public Map<String, Object> getImageInfo(String filePath) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            File imageFile = new File(filePath);
            BufferedImage image = ImageIO.read(imageFile);
            
            if (image != null) {
                info.put("width", image.getWidth());
                info.put("height", image.getHeight());
                info.put("aspectRatio", String.format("%.2f", (double) image.getWidth() / image.getHeight()));
                info.put("isSquare", image.getWidth() == image.getHeight());
                
                // 判断常见AI图片尺寸
                int width = image.getWidth();
                int height = image.getHeight();
                boolean isCommonAiSize = (width == 512 && height == 512) ||
                                        (width == 768 && height == 768) ||
                                        (width == 1024 && height == 1024) ||
                                        (width == 2048 && height == 2048);
                info.put("isCommonAiSize", isCommonAiSize);
            } else {
                log.warn("无法读取图片: {}", filePath);
            }
            
            // 文件信息
            info.put("fileSize", imageFile.length());
            info.put("fileName", imageFile.getName());
            info.put("extension", getFileExtension(imageFile.getName()));
            info.put("lastModified", imageFile.lastModified());
            
        } catch (Exception e) {
            log.error("获取图片信息失败: " + filePath, e);
        }
        
        return info;
    }
    
    @Override
    public String downloadImageFromUrl(String imageUrl) throws Exception {
        log.info("开始下载图片: {}", imageUrl);
        
        // 发送HTTP请求下载图片
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
        
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        
        if (response.statusCode() != 200) {
            throw new Exception("下载图片失败，HTTP状态码: " + response.statusCode());
        }
        
        // 生成临时文件名
        String extension = getExtensionFromUrl(imageUrl);
        String fileName = "temp_" + System.currentTimeMillis() + "." + extension;
        String uploadPath = RuoYiConfig.getUploadPath();
        String fullPath = uploadPath + File.separator + fileName;
        
        // 确保目录存在
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        // 保存文件
        try (InputStream inputStream = response.body();
             FileOutputStream outputStream = new FileOutputStream(fullPath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        log.info("图片下载成功: {}", fullPath);
        
        return fullPath;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * 从URL获取文件扩展名
     */
    private String getExtensionFromUrl(String url) {
        String extension = "jpg"; // 默认扩展名
        
        try {
            String path = new URI(url).getPath();
            if (path != null && path.contains(".")) {
                String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(ext)) {
                    extension = ext;
                }
            }
        } catch (Exception e) {
            log.warn("无法从URL提取扩展名，使用默认值: {}", extension);
        }
        
        return extension;
    }
}
