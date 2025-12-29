# AI图片检测问题快速修复指南

## 问题现象

1. **检测速度慢**：每次检测耗时10秒以上
2. **ONNX模型不可用**：日志显示 "ONNX本地模型 不可用，跳过"
3. **HuggingFace超时**：`java.net.ConnectException` 错误

## 快速修复步骤

### 第一步：下载ONNX模型（必须）

```bash
# Windows系统
cd D:\RuoYi-Vue
download_model.bat

# Linux/Mac系统
cd /path/to/RuoYi-Vue
bash download_model.sh
```

**验证模型下载成功**：
```bash
# 应该看到约200-500MB的文件
ls -lh models/ai-image-detector/model.onnx
```

### 第二步：检查配置文件

打开 `ruoyi-admin/src/main/resources/application.yml`，确认以下配置存在：

```yaml
ai:
  detection:
    timeout: 30
    
    onnx:
      enabled: true
      model-path: models/ai-image-detector/model.onnx
      input-size: 224
    
    huggingface:
      enabled: false  # 禁用避免超时
```

### 第三步：重启服务

```bash
# 停止当前服务（Ctrl+C 或关闭进程）
# 重新启动服务
```

### 第四步：验证启动日志

启动后查看日志，应该看到：
```
开始加载ONNX模型: models/ai-image-detector/model.onnx
ONNX模型加载成功！输入尺寸: 224x224
```

如果看到错误，检查：
- 模型文件是否存在
- 文件路径是否正确
- 配置中 `enabled: true` 是否设置

## 预期效果

修复后：
- ✅ 检测速度：**1-2秒**（不再是10秒）
- ✅ ONNX模型：**正常加载和使用**
- ✅ 无需外网：**完全离线运行**
- ✅ 准确率高：**基于深度学习模型**

## 常见问题

### Q1: 模型下载失败怎么办？

**方案A：手动下载**
1. 访问：https://huggingface.co/umm-maybe/AI-image-detector/tree/main
2. 下载 `model.onnx` 文件
3. 放到项目的 `models/ai-image-detector/` 目录下

**方案B：使用代理**
```bash
# 设置代理后再下载
export HTTP_PROXY=http://127.0.0.1:7890
export HTTPS_PROXY=http://127.0.0.1:7890
bash download_model.sh
```

### Q2: 启动时仍然显示"ONNX本地模型 不可用"？

检查清单：
1. ✓ 配置文件中 `ai.detection.onnx.enabled: true`
2. ✓ 模型文件存在于 `models/ai-image-detector/model.onnx`
3. ✓ 文件大小正常（200-500MB）
4. ✓ 重启了服务

### Q3: 想同时使用HuggingFace API怎么办？

修改配置：
```yaml
ai:
  detection:
    proxy:
      enabled: true
      host: 127.0.0.1
      port: 7890  # 你的代理端口
    
    huggingface:
      enabled: true
      token: hf_xxxxx  # 可选，提高限额
```

### Q4: 只想使用本地特征检测（最快）？

```yaml
ai:
  detection:
    timeout: 5
    
    onnx:
      enabled: false
    
    huggingface:
      enabled: false
```

速度：0.1-0.5秒，但准确率较低

## 技术细节

### 修改内容汇总

1. **application.yml**：
   - 添加了ONNX配置
   - 将超时时间从10秒增加到30秒
   - 默认禁用HuggingFace

2. **HuggingFaceDetector.java**：
   - 添加了 `enabled` 配置支持
   - 通过 `isAvailable()` 控制是否启用

3. **AiImageDetectionServiceImplV2.java**：
   - 超时时间从10秒增加到30秒
   - 只调用可用的检测器（`isAvailable()` 返回true）

### 检测器优先级

1. **ONNX本地模型**（推荐）
   - 权重：0.40
   - 速度：快（1-2秒）
   - 准确率：高

2. **HuggingFace API**（可选）
   - 权重：0.30
   - 速度：中（2-5秒，需网络）
   - 准确率：高

3. **本地特征分析**（始终启用）
   - 权重：0.20
   - 速度：最快（0.1-0.5秒）
   - 准确率：中

## 验证修复成功

测试检测功能：
1. 上传一张图片进行检测
2. 查看响应时间（应该在1-2秒内）
3. 查看日志，确认使用了ONNX模型：
   ```
   ONNX本地模型 检测完成: {...}
   检测完成，成功的检测器数量: 2/2
   ```

## 需要帮助？

详细文档：
- `AI_DETECTION_TROUBLESHOOTING.md` - 完整故障排查指南
- `ONNX_DEPLOYMENT_GUIDE.md` - ONNX模型部署指南
- `PROXY_AND_LOCAL_MODEL_GUIDE.md` - 代理和模型配置指南
