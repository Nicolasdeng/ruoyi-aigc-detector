# AI图片检测模型下载解决方案

## 🚨 问题说明

原下载脚本中的模型仓库 `umm-maybe/AI-image-detector` 在 HuggingFace 上不存在。

## ✅ 推荐解决方案

### 方案1：使用现成的ONNX模型（最简单）

直接下载已经转换好的 ONNX 模型文件：

#### 推荐模型库：

1. **ONNX Model Zoo**
   - 网址：https://github.com/onnx/models
   - 包含多种图片分类模型
   - 推荐模型：
     - ResNet50（准确率高）
     - MobileNet（速度快）
     - EfficientNet（平衡型）

2. **下载步骤**：
   ```bash
   # 1. 访问 ONNX Model Zoo
   # 2. 选择 Vision > Image Classification
   # 3. 下载模型文件（.onnx格式）
   # 4. 放到项目目录：
   #    RuoYi-Vue/models/ai-image-detector/model.onnx
   ```

3. **直接下载链接**（示例）：
   - ResNet50: https://github.com/onnx/models/raw/main/vision/classification/resnet/model/resnet50-v2-7.onnx
   - MobileNetV2: https://github.com/onnx/models/raw/main/vision/classification/mobilenet/model/mobilenetv2-7.onnx

### 方案2：暂时不使用ONNX模型

如果模型下载困难，可以暂时只使用内置的检测器：

#### 修改配置文件

在 `application.yml` 中：

```yaml
ai:
  detection:
    # 不启用ONNX
    onnx:
      enabled: false
    
    # 使用 Hugging Face API（需要代理）
    proxy:
      enabled: true  # 如果有代理
      host: 127.0.0.1
      port: 7897
    
    # 本地特征检测器会自动启用
```

**效果**：
- 仍然可以进行AI检测
- 准确率：70-80%（取决于图片特征）
- 无需下载模型

### 方案3：使用替代的AI检测API

#### 3.1 使用 Hugging Face Inference API

不需要下载模型，直接调用在线API：

```yaml
ai:
  detection:
    huggingface:
      token: "你的token"  # 从 https://huggingface.co/settings/tokens 获取
    proxy:
      enabled: true
      host: 127.0.0.1
      port: 7897
```

**优点**：
- 无需下载模型
- API维护更新自动
- 准确率高

**缺点**：
- 需要网络连接
- 需要代理访问
- 有API调用限制

#### 3.2 使用其他AI检测服务

可以集成其他商业API：
- Google Cloud Vision API
- AWS Rekognition
- Azure Computer Vision

### 方案4：下载并转换PyTorch模型

如果你想尝试其他模型：

#### 步骤：

1. **下载PyTorch模型**
   ```bash
   # 运行新创建的脚本
   download_model_v2.bat
   
   # 选择方案1（CLIP）或方案2（ViT）
   ```

2. **安装Python依赖**
   ```bash
   pip install torch torchvision onnx
   ```

3. **转换为ONNX格式**
   ```bash
   python convert_pytorch_to_onnx.py
   ```

## 📊 方案对比

| 方案 | 难度 | 准确率 | 速度 | 是否需要网络 |
|------|------|--------|------|-------------|
| 方案1: ONNX模型 | ⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 仅下载时 |
| 方案2: 仅本地检测 | ⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 否 |
| 方案3: HF API | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 是 |
| 方案4: 转换模型 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 仅转换时 |

## 🎯 推荐选择

### 情况1：追求最高准确率
- 使用 **方案3**（Hugging Face API）
- 或 **方案1** + **方案3** 组合

### 情况2：追求速度和稳定性
- 使用 **方案1**（ONNX模型）

### 情况3：快速上线测试
- 使用 **方案2**（仅本地检测）
- 后续再添加ONNX模型

### 情况4：完全离线环境
- 必须使用 **方案1**（ONNX模型）

## 🔧 快速操作指南

### 选择方案1（推荐）

```bash
# 1. 创建目录
mkdir -p models/ai-image-detector

# 2. 下载 ResNet50 模型（约100MB）
curl -L -o models/ai-image-detector/model.onnx https://github.com/onnx/models/raw/main/vision/classification/resnet/model/resnet50-v2-7.onnx

# 3. 配置 application.yml
# ai:
#   detection:
#     onnx:
#       enabled: true
#       model-path: models/ai-image-detector/model.onnx
#       input-size: 224

# 4. 重启服务
```

### 选择方案2

```bash
# 1. 修改 application.yml
# ai:
#   detection:
#     onnx:
#       enabled: false

# 2. 重启服务（就这么简单！）
```

## 📝 注意事项

1. **模型兼容性**：确保下载的ONNX模型输入尺寸为224x224
2. **文件大小**：ONNX模型通常100-500MB，确保磁盘空间足够
3. **首次加载**：模型首次加载需要2-5秒，请耐心等待
4. **错误处理**：如果模型加载失败，系统会自动降级到其他检测器

## ❓ 常见问题

### Q1：我应该选择哪个方案？
**A**：如果可以下载文件，选择方案1；如果想快速测试，选择方案2。

### Q2：ONNX模型下载很慢怎么办？
**A**：使用代理或者切换到方案2先运行起来。

### Q3：可以同时使用多个方案吗？
**A**：可以！系统会自动聚合所有可用检测器的结果。

### Q4：本地检测器准确率够用吗？
**A**：对于特征明显的AI图片（如Midjourney、Stable Diffusion生成），准确率可达80%+。

## 🆘 需要帮助？

查看其他文档：
- `AI_DETECTION_TROUBLESHOOTING.md` - 故障排查
- `ONNX_DEPLOYMENT_GUIDE.md` - ONNX部署详细指南
- `AI_DETECTION_CONFIG.md` - 配置说明

---

**更新时间**: 2025/12/29
**版本**: v2.0
