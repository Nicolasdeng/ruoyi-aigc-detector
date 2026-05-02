# AI检测工具小程序 - 框架重构说明

## 📋 项目概述

本项目是一个基于uni-app开发的AI检测工具小程序，集成了图片、文本、视频、音频四种AI内容检测功能。

## 🎯 重构目标

将原有的单一检测页面重构为多功能模块化架构，提供更好的用户体验和可扩展性。

## 📁 新架构设计

### 底部导航栏 (TabBar)
1. **检测（首页）** - `/pages/index/index` - 展示4种检测类型的入口
2. **历史记录** - `/pages/history/history` - 查看所有检测历史
3. **我的** - `/pages/mine/mine` - 用户信息和设置

### 检测功能页面
1. **图片检测** - `/pages/detect/image` - 上传图片并检测AI生成概率
2. **文本检测** - `/pages/detect/text` - 输入文本并检测AI生成概率
3. **视频检测** - `/pages/detect/video` - 上传视频并逐帧分析
4. **音频检测** - `/pages/detect/audio` - 上传音频并分析声纹特征

## ✅ 已完成工作

### 前端 (uni-app)
- ✅ 文件结构重构完成
- ✅ `pages/index/index.vue` - 首页（4个检测入口卡片）
- ✅ `pages/detect/image.vue` - 图片检测（原detect.vue重命名）
- ✅ `pages/detect/text.vue` - 文本检测（框架完成）
- ✅ `pages/detect/video.vue` - 视频检测（框架完成）
- ✅ `pages/detect/audio.vue` - 音频检测（框架完成）
- ✅ `pages/mine/mine.vue` - 我的页面
- ✅ `pages/history/history.vue` - 历史记录（保持原有功能）
- ✅ `pages.json` - 配置文件已更新

### 后端 (若依框架)
- ✅ 数据库表设计（ai_detection_record, ai_detection_api_config）
- ✅ AiDetectionController.java（已添加@Anonymous注解）
- ✅ AiDetectionServiceImpl.java（集成Hugging Face检测）
- ✅ Mapper和XML文件完成
- ✅ 配置文件已更新（application.yml）

## 🎨 UI设计特点

### 统一主题色
- 主渐变色：`#667eea` → `#764ba2`
- 各检测类型卡片有独特的渐变色：
  - 图片：紫色渐变 (#667eea → #764ba2)
  - 文本：粉红渐变 (#f093fb → #f5576c)
  - 视频：蓝色渐变 (#4facfe → #00f2fe)
  - 音频：绿色渐变 (#43e97b → #38f9d7)

### 交互设计
- 点击卡片时有缩放动画效果
- 加载状态有旋转动画
- 检测结果用颜色和图标直观展示
- 置信度用进度条可视化

## 🔧 技术要点

### 导航方式
- 使用 `uni.navigateTo` 跳转到检测页面
- 使用 `uni.switchTab` 切换底部导航
- 所有检测页面的baseUrl配置为：`http://localhost:8080`

### 文件上传
- 图片：使用 `uni.chooseImage` API
- 视频：使用 `uni.chooseVideo` API
- 音频：使用 `uni.chooseMessageFile` API（因uni-app无直接音频选择API）
- 上传使用 `uni.uploadFile` 与后端交互

### 检测流程
1. 用户选择/输入内容
2. 点击"开始检测"按钮
3. 显示加载动画
4. 调用后端API（或模拟结果）
5. 展示检测结果（置信度、详细信息）
6. 提供"查看历史"和"再次检测"操作

## 📝 待完善功能

### 后端扩展（按优先级）
1. **文本检测接口** - `/ai/detection/text/detect`
   - 集成文本AI检测模型（如GPTZero、OpenAI Detector等）
   - 实现多模型综合判定

2. **视频检测接口** - `/ai/detection/video/upload`
   - 视频帧提取
   - 逐帧AI检测
   - 综合分析结果

3. **音频检测接口** - `/ai/detection/audio/upload`
   - 音频特征提取
   - 声纹分析
   - AI合成音频识别

4. **更多开源模型集成**
   - Hugging Face其他检测模型
   - GitHub开源检测工具
   - 自定义规则增强

### 前端优化
1. 实际API对接（目前使用模拟数据）
2. 错误处理优化
3. 离线缓存支持
4. 用户认证功能
5. 统计分析页面

## 🚀 快速开始

### 启动后端
```bash
cd RuoYi-Vue
mvn clean install
java -jar ruoyi-admin/target/ruoyi-admin.jar
```
后端服务将运行在：`http://localhost:8080`

### 启动前端
1. 使用HBuilderX打开uni-app目录
2. 选择运行到浏览器/小程序/App
3. 开始测试

## 📊 项目结构

```
uni-app/
├── pages/
│   ├── index/
│   │   └── index.vue          # 首页（检测类型选择）
│   ├── detect/
│   │   ├── image.vue          # 图片检测
│   │   ├── text.vue           # 文本检测
│   │   ├── video.vue          # 视频检测
│   │   └── audio.vue          # 音频检测
│   ├── history/
│   │   └── history.vue        # 历史记录
│   └── mine/
│       └── mine.vue           # 我的页面
├── static/                     # 静态资源
├── App.vue                     # 应用配置
├── main.js                     # 入口文件
├── pages.json                  # 页面配置
└── manifest.json               # 应用配置
```

## 🔐 安全说明

- 后端Controller已添加 `@Anonymous` 注解，允许匿名访问
- 文件上传有大小限制（图片10MB、视频100MB、音频50MB）
- 上传文件存储在配置的upload路径

## 📞 技术支持

- **准确性放第一位**
- **多调用开源检测模型**
- **框架先搭好，后续慢慢完善**

## 📅 更新日志

### 2025-12-20
- ✅ 完成框架重构
- ✅ 创建所有页面模板
- ✅ 配置底部导航栏
- ✅ 统一UI风格
- ✅ 图片检测功能已完善（已对接后端）
- ⏳ 文本/视频/音频检测待对接后端API

---

**版本**: v1.0.0  
**作者**: AI Commander  
**最后更新**: 2025-12-20
