# uni-app AI图片检测功能实现指南

## 📋 概述

本文档说明了如何在uni-app中集成本地AI图片检测功能，实现与RuoYi-Vue后端的完整对接。

## 🎯 实现的功能

### 1. 核心功能
- ✅ 图片上传与AI检测
- ✅ 用户额度管理
- ✅ 检测结果展示
- ✅ 检测历史记录
- ✅ Token认证机制
- ✅ 错误处理与用户提示

### 2. 技术特点
- 🔐 统一的HTTP请求封装
- ⚙️ 集中的配置管理
- 💎 完善的额度显示
- 📊 详细的检测结果展示
- 🎨 美观的UI设计

## 📁 项目结构

```
uni-app/
├── config/
│   └── config.js              # 配置文件（API地址等）
├── utils/
│   └── http.js                # HTTP请求封装
├── pages/
│   └── detect/
│       └── image.vue          # 图片检测页面
└── main.js                    # 主入口文件
```

## 🚀 快速开始

### 步骤1：配置后端地址

编辑 `config/config.js`，设置你的后端API地址：

```javascript
// 开发环境配置
const development = {
  baseUrl: 'http://localhost:8080',  // 修改为你的后端地址
  timeout: 30000,
  uploadTimeout: 60000,
  debug: true
}

// 生产环境配置
const production = {
  baseUrl: 'https://your-api-domain.com',  // 修改为生产环境地址
  timeout: 30000,
  uploadTimeout: 60000,
  debug: false
}
```

### 步骤2：确认后端API接口

确保后端提供以下接口：

#### 1. 图片检测接口
```
POST /ai/detection/image/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}
X-Platform: miniapp

参数：
- file: 图片文件

返回：
{
  "code": 200,
  "data": {
    "detectionResult": "AI_GENERATED",  // 或 "HUMAN_CREATED"
    "confidenceScore": "85.5",
    "detectionDetails": "{...}",  // JSON字符串
    "fileUrl": "..."
  }
}
```

#### 2. 用户额度查询接口
```
GET /api/user/quota
Authorization: Bearer {token}

返回：
{
  "code": 200,
  "data": {
    "totalQuota": 100,
    "remainingQuota": 80,
    "usedQuota": 20,
    "quotaType": "FREE"
  }
}
```

#### 3. 历史记录保存接口
```
POST /api/user/detection/history
Content-Type: application/json
Authorization: Bearer {token}

参数：
{
  "type": "image",
  "fileUrl": "...",
  "result": "AI_GENERATED",
  "confidence": "85.5",
  "details": "{...}"
}

返回：
{
  "code": 200,
  "msg": "保存成功"
}
```

### 步骤3：运行项目

```bash
# 开发环境运行
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5

# 生产环境构建
npm run build:mp-weixin
npm run build:h5
```

## 📖 使用说明

### 在页面中使用HTTP工具

```vue
<script>
import http from '@/utils/http.js'

export default {
  methods: {
    async getData() {
      try {
        // GET请求
        const res = await http.get('/api/user/quota')
        console.log(res.data)
        
        // POST请求
        const res2 = await http.post('/api/user/data', { key: 'value' })
        
        // 文件上传
        const res3 = await http.upload('/api/upload', filePath, {
          extraData: 'value'
        })
      } catch (error) {
        console.error(error.message)
      }
    }
  }
}
</script>
```

### 在页面中使用配置

```vue
<script>
export default {
  onLoad() {
    // 访问配置
    console.log(this.$config.baseUrl)
    console.log(this.$config.debug)
  }
}
</script>
```

## 🎨 UI组件说明

### 图片检测页面包含以下组件：

1. **上传区域**
   - 支持相册选择和拍照
   - 图片预览
   - 重新上传功能

2. **额度显示**
   - 剩余额度
   - 本次消耗
   - 提示信息

3. **检测按钮**
   - 自动检查额度
   - 加载动画
   - 错误提示

4. **检测结果**
   - 风险等级（高/中/低）
   - AI生成概率
   - 检测引擎统计
   - 详细说明
   - 使用建议
   - 免责声明

## 🔧 配置选项

### HTTP配置

在 `config/config.js` 中可配置：

| 配置项 | 说明 | 默认值 |
|-------|------|-------|
| baseUrl | 后端API地址 | http://localhost:8080 |
| timeout | 普通请求超时时间（毫秒） | 30000 |
| uploadTimeout | 文件上传超时时间（毫秒） | 60000 |
| debug | 是否启用调试模式 | true |

### HTTP工具方法

| 方法 | 说明 | 参数 |
|-----|------|------|
| get(url, data) | GET请求 | url: 请求路径<br>data: 请求参数 |
| post(url, data) | POST请求 | url: 请求路径<br>data: 请求数据 |
| put(url, data) | PUT请求 | url: 请求路径<br>data: 请求数据 |
| delete(url, data) | DELETE请求 | url: 请求路径<br>data: 请求数据 |
| upload(url, filePath, formData) | 文件上传 | url: 上传路径<br>filePath: 文件路径<br>formData: 额外表单数据 |
| setBaseUrl(url) | 动态设置基础URL | url: 新的基础URL |
| getBaseUrl() | 获取当前基础URL | - |

## 🔐 认证机制

### Token存储

Token存储在本地缓存中：

```javascript
// 保存token
uni.setStorageSync('token', 'your-token')

// HTTP工具会自动从缓存读取token并添加到请求头
// Authorization: Bearer {token}
```

### Token过期处理

当后端返回401状态码时，HTTP工具会自动：
1. 清除本地token
2. 提示用户重新登录
3. 跳转到登录页面

## 📊 检测结果数据结构

### detectionDetails 字段结构

```json
{
  "finalScore": 85.5,           // 最终得分（0-100）
  "riskLevel": "HIGH",          // 风险等级：HIGH/MEDIUM/LOW
  "apiCount": 3,                // 参与检测的API数量
  "aiVotes": 2,                 // 判定为AI的API数量
  "humanVotes": 1,              // 判定为真实的API数量
  "explanation": "检测说明",     // 详细说明
  "suggestions": [              // 使用建议
    "建议1",
    "建议2"
  ]
}
```

## ⚠️ 常见问题

### 1. 跨域问题

**问题**：H5端运行时出现跨域错误

**解决方案**：
- 方法1：在后端配置CORS允许跨域
- 方法2：使用HBuilderX内置的代理配置

### 2. 请求超时

**问题**：上传大图片时超时

**解决方案**：
- 在 `config/config.js` 中增加 `uploadTimeout` 值
- 或者在上传前压缩图片

### 3. Token失效

**问题**：Token过期后页面卡住

**解决方案**：
- 确保登录页面路径正确：`/pages/login/login`
- 在登录成功后正确保存token

### 4. 额度显示不正确

**问题**：额度不更新或显示错误

**解决方案**：
- 检查后端额度管理接口是否正常
- 确认检测成功后是否正确扣除额度

## 🚀 性能优化建议

### 1. 图片压缩

```javascript
uni.chooseImage({
  count: 1,
  sizeType: ['compressed'],  // 使用压缩图
  sourceType: ['album', 'camera'],
  success: (res) => {
    // 处理图片
  }
})
```

### 2. 请求缓存

对于用户额度等不常变化的数据，可以添加本地缓存：

```javascript
// 带缓存的额度查询
async loadUserQuotaWithCache() {
  const cacheKey = 'user_quota_cache'
  const cacheTime = 5 * 60 * 1000  // 5分钟缓存
  
  const cache = uni.getStorageSync(cacheKey)
  if (cache && Date.now() - cache.time < cacheTime) {
    this.userQuota = cache.data
    return
  }
  
  const res = await http.get('/api/user/quota')
  if (res.data) {
    this.userQuota = res.data
    uni.setStorageSync(cacheKey, {
      data: res.data,
      time: Date.now()
    })
  }
}
```

## 📝 后续扩展

### 可以扩展的功能：

1. **文本检测**
   - 复用HTTP工具
   - 创建 `pages/detect/text.vue`
   - 调用后端文本检测接口

2. **视频检测**
   - 视频上传功能
   - 进度显示
   - 长时间检测处理

3. **批量检测**
   - 多图片选择
   - 队列处理
   - 批量结果展示

4. **充值功能**
   - 额度套餐选择
   - 支付接口对接
   - 充值记录查询

## 📞 技术支持

如遇到问题，请检查：

1. ✅ 配置文件是否正确设置
2. ✅ 后端服务是否正常运行
3. ✅ 网络连接是否正常
4. ✅ Token是否有效
5. ✅ 控制台是否有错误日志

## 🎉 总结

本实现已完成：
- ✅ HTTP请求封装（支持GET/POST/PUT/DELETE/Upload）
- ✅ 配置文件管理（开发/生产环境分离）
- ✅ 图片检测页面（完整UI和交互）
- ✅ 额度管理（显示、检查、扣除）
- ✅ Token认证（自动添加、过期处理）
- ✅ 错误处理（统一提示、友好反馈）
- ✅ 历史记录（自动保存）

所有代码均已按照uni-app规范和最佳实践编写，可直接用于生产环境！🚀
