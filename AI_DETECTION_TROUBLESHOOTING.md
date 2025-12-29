# AI图片检测性能问题排查与解决方案

## 问题分析

### 问题1：检测速度慢（10秒+）

**根本原因：**
1. **HuggingFace API超时**：从日志看，HuggingFace检测器耗时10秒后超时
   - 错误：`java.net.ConnectException: null`
   - 原因：无法连接到 `api-inference.huggingface.co`（可能被墙或网络问题）

2. **检测器超时机制**：
   - 总超时设置为10秒（`ai.detection.timeout=60`配置未生效）
   - 实际在 `AiImageDetectionServiceImplV2` 中硬编码为10秒

### 问题2：ONNX本地模型不可用

**根本原因：**
1. **配置未启用**：`application.yml` 中缺少ONNX相关配置
   ```yaml
   ai.detection.onnx.enabled: false  # 默认未启用
   ```

2. **模型文件不存在**：
   - 默认路径：`models/ai-image-detector/model.onnx`
   - 模型文件未下载或路径错误

## 解决方案

### 方案1：启用并配置ONNX本地模型（推荐）

#### 步骤1：下载ONNX模型

```bash
# Windows环境执行
download_model.bat

# Linux/Mac环境执行
bash download_model.sh
```

#### 步骤2：修改配置文件

在 `application.yml` 中添加ONNX配置：

```yaml
ai:
  detection:
    # 全局超时配置（秒）
    timeout: 30
    
    # ONNX本地模型配置
    onnx:
      enabled: true
      model-path: models/ai-image-detector/model.onnx
      input-size: 224
    
    # Hugging Face API配置
    huggingface:
      token: 
      enabled: false  # 如果网络不通，建议禁用
    
    # 代理配置（如果需要访问HuggingFace）
    proxy:
      enabled: false
      host: 127.0.0.1
      port: 7890
```

#### 步骤3：重启服务

```bash
# 停止服务
# 启动服务
```

### 方案2：配置HuggingFace代理（如需使用）

如果需要使用HuggingFace API，需要配置代理：

```yaml
ai:
  detection:
    timeout: 30
    
    # 代理配置
    proxy:
      enabled: true
      host: 127.0.0.1
      port: 7890  # 你的代理端口
    
    # Hugging Face配置
    huggingface:
      token: hf_xxxxxxxxxxxxx  # 可选，提高限额
      enabled: true
```

### 方案3：仅使用本地特征检测（最快）

如果不需要AI模型检测，可以只使用本地特征分析：

```yaml
ai:
  detection:
    timeout: 5  # 降低超时时间
    
    onnx:
      enabled: false
    
    huggingface:
      enabled: false
    
    # 本地特征检测默认启用
```

## 性能优化建议

### 1. 调整超时时间

修改 `AiImageDetectionServiceImplV2.java`：

```java
// 第153行附近
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .get(30, TimeUnit.SECONDS);  // 改为30秒或更长
```

### 2. 异步处理优化

建议增加线程池大小（如果并发检测多张图片）：

```java
@Configuration
public class DetectionThreadPoolConfig {
    
    @Bean("detectionExecutor")
    public Executor detectionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("detection-");
        executor.initialize();
        return executor;
    }
}
```

### 3. 禁用不可用的检测器

在检测服务中添加检测器可用性检查：

```java
// 只使用可用的检测器
List<IImageDetector> availableDetectors = detectors.stream()
    .filter(IImageDetector::isAvailable)
    .collect(Collectors.toList());
```

## 预期效果

### 使用ONNX本地模型后：
- **检测速度**：0.5-2秒（取决于图片大小）
- **准确率**：较高（基于深度学习模型）
- **无需网络**：完全离线运行

### 仅使用本地特征分析：
- **检测速度**：0.1-0.5秒（非常快）
- **准确率**：中等（基于规则和特征）
- **无需网络**：完全离线运行

### 使用HuggingFace（需代理）：
- **检测速度**：2-5秒（取决于网络）
- **准确率**：高（专业模型）
- **需要网络**：需要稳定的国际网络连接

## 故障排查检查清单

### ONNX模型问题排查

1. **检查配置**
   ```bash
   # 查看application.yml中的配置
   ai.detection.onnx.enabled=true
   ```

2. **检查模型文件**
   ```bash
   # 确认文件存在
   ls -lh models/ai-image-detector/model.onnx
   # 应该看到约200-500MB的文件
   ```

3. **检查日志**
   ```
   启动日志应包含：
   - "开始加载ONNX模型"
   - "ONNX模型加载成功"
   
   如果失败，会显示：
   - "未找到ONNX模型文件"
   - "加载ONNX模型失败"
   ```

### HuggingFace问题排查

1. **网络连接测试**
   ```bash
   # 测试连接
   curl -I https://api-inference.huggingface.co
   
   # 如果失败，需要配置代理
   ```

2. **代理配置测试**
   ```bash
   # 通过代理测试
   curl -x http://127.0.0.1:7890 -I https://api-inference.huggingface.co
   ```

## 推荐配置（生产环境）

```yaml
ai:
  detection:
    # 超时30秒
    timeout: 30
    
    # 启用ONNX本地模型（主力）
    onnx:
      enabled: true
      model-path: models/ai-image-detector/model.onnx
      input-size: 224
    
    # 禁用HuggingFace（如果网络不稳定）
    huggingface:
      enabled: false
    
    # 本地特征检测（辅助）
    local-feature:
      enabled: true
```

这样配置后：
- ✅ 检测速度快（1-2秒）
- ✅ 准确率高（深度学习模型）
- ✅ 无需外网
- ✅ 稳定可靠

## 后续优化方向

1. **批量检测优化**：一次请求检测多张图片
2. **结果缓存**：相同图片不重复检测
3. **异步通知**：长时间检测使用WebSocket推送结果
4. **模型更新**：定期更新ONNX模型以提高准确率
