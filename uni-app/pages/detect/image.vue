<template>
  <view class="container">
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-content">
        <text class="title">AI图片检测</text>
        <text class="subtitle">智能识别AI生成图片</text>
      </view>
    </view>

    <!-- 检测区域 -->
    <view class="detect-area">
      <!-- 配额显示组件 -->
      <quota-display ref="quotaDisplay" @quota-updated="onQuotaUpdated"></quota-display>
      
      <!-- 上传图片区域 -->
      <view class="upload-section" v-if="!imageUrl">
        <view class="upload-box" @tap="chooseImage">
          <image class="upload-icon" src="/static/upload-icon.png" mode="aspectFit"></image>
          <text class="upload-text">点击上传图片</text>
          <text class="upload-tip">支持JPG、PNG格式，最大10MB</text>
        </view>
      </view>

      <!-- 图片预览区域 -->
      <view class="preview-section" v-if="imageUrl">
        <view class="preview-box">
          <image class="preview-image" :src="imageUrl" mode="aspectFill"></image>
          <view class="reupload-btn" @tap="chooseImage">
            <text class="reupload-text">重新上传</text>
          </view>
        </view>
      </view>

      <!-- 检测按钮 -->
      <view class="action-section" v-if="imageUrl && !detecting">
        <button class="detect-btn" @tap="startDetection">开始检测</button>
      </view>

      <!-- 检测中状态 -->
      <view class="detecting-section" v-if="detecting">
        <view class="loading-animation">
          <view class="loading-circle"></view>
          <text class="loading-text">AI智能分析中...</text>
        </view>
      </view>

      <!-- 检测结果 -->
      <view class="result-section" v-if="detectionResult">
        <view class="result-card">
          <!-- 风险等级标题 -->
          <view class="risk-header" :class="getRiskLevelClass()">
            <view class="risk-icon">{{ getRiskIcon() }}</view>
            <view class="risk-info">
              <text class="risk-title">{{ getRiskTitle() }}</text>
              <text class="risk-subtitle">{{ getRiskSubtitle() }}</text>
            </view>
          </view>

          <!-- AI生成概率 -->
          <view class="score-box">
            <view class="score-label">
              <text>AI生成概率</text>
              <text class="score-value">{{ getAIScore() }}%</text>
            </view>
            <view class="score-bar">
              <view class="score-fill" :style="{width: getAIScore() + '%', backgroundColor: getScoreColor()}"></view>
            </view>
          </view>

          <!-- 检测引擎统计 -->
          <view class="engine-stats">
            <view class="stat-item">
              <text class="stat-num">{{ getEngineCount() }}</text>
              <text class="stat-label">检测引擎</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-num">{{ getAIVotes() }}</text>
              <text class="stat-label">判定AI</text>
            </view>
            <view class="stat-divider"></view>
            <view class="stat-item">
              <text class="stat-num">{{ getHumanVotes() }}</text>
              <text class="stat-label">判定真实</text>
            </view>
          </view>

          <!-- AI模型识别 -->
          <view class="model-detect-box" v-if="getInferredModel()">
            <view class="model-title">
              <text class="title-icon">🔍</text>
              <text class="title-text">AI模型识别</text>
            </view>
            <view class="model-content">
              <text class="model-text">{{ getInferredModel() }}</text>
            </view>
          </view>

          <!-- 特征分析 -->
          <view class="features-box" v-if="getDetectedFeatures().length > 0">
            <view class="features-title">
              <text class="title-icon">🎯</text>
              <text class="title-text">检测到的AI特征</text>
            </view>
            <view class="feature-item" v-for="(feature, index) in getDetectedFeatures()" :key="index">
              <text class="feature-dot">✓</text>
              <text class="feature-text">{{ feature }}</text>
            </view>
          </view>

          <!-- 结果说明 -->
          <view class="explanation-box">
            <view class="explanation-title">
              <text class="title-icon">📝</text>
              <text class="title-text">检测说明</text>
            </view>
            <text class="explanation-text">{{ getExplanation() }}</text>
          </view>

          <!-- 使用建议 -->
          <view class="suggestions-box" v-if="getSuggestions().length > 0">
            <view class="suggestions-title">
              <text class="title-icon">💡</text>
              <text class="title-text">使用建议</text>
            </view>
            <view class="suggestion-item" v-for="(suggestion, index) in getSuggestions()" :key="index">
              <text class="suggestion-dot">•</text>
              <text class="suggestion-text">{{ suggestion }}</text>
            </view>
          </view>

          <!-- 免责声明 -->
          <view class="disclaimer-box">
            <view class="disclaimer-title">
              <text class="title-icon">⚠️</text>
              <text class="title-text">重要提示</text>
            </view>
            <view class="disclaimer-item">
              <text class="disclaimer-text">• 本检测为辅助判断工具，结果仅供参考</text>
            </view>
            <view class="disclaimer-item">
              <text class="disclaimer-text">• 不作为法律或官方证据使用</text>
            </view>
            <view class="disclaimer-item">
              <text class="disclaimer-text">• 建议结合实际场景综合判断</text>
            </view>
            <view class="disclaimer-item">
              <text class="disclaimer-text">• AI技术持续进化，检测存在一定误差</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="result-actions">
            <button class="action-btn secondary" @tap="viewHistory">查看历史</button>
            <button class="action-btn primary" @tap="resetDetection">再次检测</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能说明 -->
    <view class="info-section" v-if="!imageUrl && !detectionResult">
      <view class="info-title">
        <text>功能特色</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="info-icon">🎯</text>
          <view class="info-content">
            <text class="info-name">引擎检测</text>
            <text class="info-desc">先进识别技术，提高准确率</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">⚡</text>
          <view class="info-content">
            <text class="info-name">快速响应</text>
            <text class="info-desc">智能算法，秒级返回检测结果</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">🔒</text>
          <view class="info-content">
            <text class="info-name">隐私保护</text>
            <text class="info-desc">图片加密传输，自动删除检测记录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'
import QuotaDisplay from '@/components/quota-display/quota-display.vue'

export default {
  components: {
    QuotaDisplay
  },
  data() {
    return {
      imageUrl: '',
      tempFilePath: '',
      detecting: false,
      detectionResult: null
    }
  },
  onLoad() {
    // 检查登录状态（本地同步检查）
    this.checkLoginStatus()
  },

  methods: {
    // 检查登录状态（本地同步检查，不发起网络请求）
    checkLoginStatus() {
      const isLoggedIn = checkLoginLocal()
      if (!isLoggedIn) {
        uni.reLaunch({
          url: '/pages/login/login'
        })
      }
    },

    // 选择图片
    chooseImage() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.tempFilePath = res.tempFilePaths[0]
          this.imageUrl = res.tempFilePaths[0]
          this.detectionResult = null
        }
      })
    },

    // 开始检测
    async startDetection() {
      if (!this.tempFilePath) {
        uni.showToast({
          title: '请先上传图片',
          icon: 'none'
        })
        return
      }

      // 检查配额
      if (!this.$refs.quotaDisplay.checkQuota()) {
        return
      }

      this.detecting = true
      this.detectionResult = null

      try {
        // 使用封装的http工具上传图片
        const res = await http.upload('/ai/detection/image/upload', this.tempFilePath, {})
        
        if (res.data) {
          this.detectionResult = res.data
          
          // 检测成功后消耗配额
          await this.$refs.quotaDisplay.consumeQuota()
          
          // 保存到历史记录
          await this.saveToHistory(res.data)
          
          uni.showToast({
            title: '检测完成',
            icon: 'success',
            duration: 2000
          })
        }
      } catch (error) {
        console.error('检测失败:', error)
        uni.showToast({
          title: error.message || '检测失败，请重试',
          icon: 'none',
          duration: 3000
        })
      } finally {
        this.detecting = false
      }
    },

    // 保存到历史记录
    async saveToHistory(result) {
      try {
        await http.post('/api/user/detection/history', {
          type: 'image',
          fileUrl: result.fileUrl || '',
          result: result.detectionResult || '',
          confidence: result.confidenceScore || '0',
          details: result.detectionDetails || '{}'
        })
      } catch (error) {
        console.error('保存历史记录失败:', error)
        // 保存失败不影响主流程，只记录日志
      }
    },

    // 重置检测
    resetDetection() {
      this.imageUrl = ''
      this.tempFilePath = ''
      this.detectionResult = null
      this.detecting = false
    },

    // 查看历史记录
    viewHistory() {
      uni.navigateTo({
        url: '/pages/history/history'
      })
    },

    // 配额更新回调
    onQuotaUpdated(quota) {
      console.log('配额已更新:', quota)
    },

    // 获取风险等级样式类
    getRiskLevelClass() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return 'risk-medium'
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        const riskLevel = details.riskLevel || 'MEDIUM'
        return `risk-${riskLevel.toLowerCase()}`
      } catch (e) {
        return 'risk-medium'
      }
    },

    // 获取风险图标
    getRiskIcon() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return '⚠️'
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        const riskLevel = details.riskLevel || 'MEDIUM'
        if (riskLevel === 'HIGH') return '🔴'
        if (riskLevel === 'LOW') return '🟢'
        return '🟡'
      } catch (e) {
        return '⚠️'
      }
    },

    // 获取风险标题
    getRiskTitle() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return '中风险'
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        const riskLevel = details.riskLevel || 'MEDIUM'
        if (riskLevel === 'HIGH') return '高风险：明显AI生成特征'
        if (riskLevel === 'LOW') return '低风险：更像真实图片'
        return '中风险：存在AI特征'
      } catch (e) {
        return '中风险'
      }
    },

    // 获取风险副标题
    getRiskSubtitle() {
      const result = this.detectionResult.detectionResult
      if (result === 'AI_GENERATED') return '不建议用于需要真实性的场合'
      if (result === 'HUMAN_CREATED') return '但仍建议核实来源'
      return '建议人工进一步核实'
    },

    // 获取AI生成分数
    getAIScore() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return 50
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return Math.round(details.finalScore || 50)
      } catch (e) {
        return 50
      }
    },

    // 获取分数颜色
    getScoreColor() {
      const score = this.getAIScore()
      if (score > 70) return '#ff6b6b'
      if (score < 30) return '#51cf66'
      return '#ffa94d'
    },

    // 获取检测引擎数量
    getEngineCount() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return 0
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.apiCount || 0
      } catch (e) {
        return 0
      }
    },

    // 获取AI判定数量
    getAIVotes() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return 0
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.aiVotes || 0
      } catch (e) {
        return 0
      }
    },

    // 获取真实判定数量
    getHumanVotes() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return 0
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.humanVotes || 0
      } catch (e) {
        return 0
      }
    },

    // 获取说明文本
    getExplanation() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return '检测完成'
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.explanation || '检测完成'
      } catch (e) {
        return '检测完成'
      }
    },

    // 获取使用建议
    getSuggestions() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return []
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.suggestions || []
      } catch (e) {
        return []
      }
    },

    // 获取推理的AI模型类型
    getInferredModel() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return ''
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.inferredModel || ''
      } catch (e) {
        return ''
      }
    },

    // 获取检测到的特征
    getDetectedFeatures() {
      if (!this.detectionResult || !this.detectionResult.detectionDetails) return []
      try {
        const details = JSON.parse(this.detectionResult.detectionDetails)
        return details.detectedFeatures || []
      } catch (e) {
        return []
      }
    },

    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
      
      return `${date.getMonth() + 1}月${date.getDate()}日`
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 40rpx;
}

.header {
  padding: 60rpx 40rpx 40rpx;
  text-align: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 16rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.detect-area {
  margin: 0 30rpx;
  background: #ffffff;
  border-radius: 30rpx;
  padding: 40rpx;
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.1);
}

.upload-section {
  padding: 60rpx 0;
}

.upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  border: 4rpx dashed #ddd;
  border-radius: 20rpx;
  background: #fafafa;
}

.upload-icon {
  width: 120rpx;
  height: 120rpx;
  margin-bottom: 30rpx;
}

.upload-text {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 16rpx;
}

.upload-tip {
  font-size: 24rpx;
  color: #999;
}

.preview-section {
  margin-bottom: 40rpx;
}

.preview-box {
  position: relative;
  border-radius: 20rpx;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 500rpx;
  border-radius: 20rpx;
}

.reupload-btn {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background: rgba(0, 0, 0, 0.6);
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
}

.reupload-text {
  font-size: 24rpx;
  color: #ffffff;
}

.action-section {
  margin-top: 40rpx;
}

.detect-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  font-size: 32rpx;
  font-weight: bold;
  border: none;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detecting-section {
  padding: 80rpx 0;
}

.loading-animation {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-circle {
  width: 80rpx;
  height: 80rpx;
  border: 6rpx solid #f0f0f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 30rpx;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  font-size: 28rpx;
  color: #666;
}

.result-section {
  margin-top: 40rpx;
}

.result-card {
  background: #ffffff;
  border-radius: 20rpx;
  overflow: hidden;
}

/* 风险等级头部 */
.risk-header {
  padding: 40rpx;
  display: flex;
  align-items: center;
  color: #ffffff;
}

.risk-high {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.risk-medium {
  background: linear-gradient(135deg, #ffa94d 0%, #fd7e14 100%);
}

.risk-low {
  background: linear-gradient(135deg, #51cf66 0%, #37b24d 100%);
}

.risk-icon {
  font-size: 60rpx;
  margin-right: 24rpx;
}

.risk-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.risk-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 8rpx;
}

.risk-subtitle {
  font-size: 24rpx;
  opacity: 0.9;
}

/* AI生成概率 */
.score-box {
  padding: 30rpx 40rpx;
  background: #f8f9fa;
}

.score-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.score-label text:first-child {
  font-size: 26rpx;
  color: #666;
}

.score-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.score-bar {
  height: 20rpx;
  background: #e9ecef;
  border-radius: 10rpx;
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: 10rpx;
  transition: width 0.5s ease;
}

/* 引擎统计 */
.engine-stats {
  display: flex;
  padding: 30rpx 40rpx;
  background: #ffffff;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 40rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.stat-divider {
  width: 2rpx;
  height: 60rpx;
  background: #e9ecef;
  margin: 0 20rpx;
}

/* 说明和建议 */
.explanation-box,
.suggestions-box,
.disclaimer-box {
  padding: 30rpx 40rpx;
  border-top: 2rpx solid #f0f0f0;
}

.explanation-title,
.suggestions-title,
.disclaimer-title {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.title-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.title-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.explanation-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
}

.suggestion-item {
  display: flex;
  margin-bottom: 12rpx;
  line-height: 1.6;
}

.suggestion-dot {
  font-size: 26rpx;
  color: #667eea;
  margin-right: 12rpx;
}

.suggestion-text {
  flex: 1;
  font-size: 26rpx;
  color: #666;
}

/* 免责声明 */
.disclaimer-box {
  background: #fff9e6;
}

.disclaimer-item {
  margin-bottom: 8rpx;
}

.disclaimer-text {
  font-size: 24rpx;
  color: #856404;
  line-height: 1.6;
}

.result-actions {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  font-size: 28rpx;
  border: none;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.action-btn.secondary {
  background: #ffffff;
  color: #667eea;
  border: 2rpx solid #667eea;
}

.info-section {
  margin: 40rpx 30rpx 0;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 30rpx;
  padding: 40rpx;
}

.info-title {
  margin-bottom: 30rpx;
  text-align: center;
}

.info-title text {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-icon {
  font-size: 48rpx;
  margin-right: 24rpx;
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.info-desc {
  font-size: 24rpx;
  color: #999;
  line-height: 1.5;
}

/* 额度显示 */
.quota-section {
  margin-top: 30rpx;
  margin-bottom: 20rpx;
}

.quota-info {
  background: linear-gradient(135deg, #f6f8fb 0%, #f1f4f9 100%);
  border-radius: 16rpx;
  padding: 30rpx;
  border: 2rpx solid #e8edf5;
}

.quota-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.quota-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.quota-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.quota-details {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 20rpx 0;
}

.quota-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.quota-label {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.quota-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #667eea;
}

.quota-value.warning {
  color: #ff9800;
}

.quota-divider {
  width: 2rpx;
  height: 60rpx;
  background: #ddd;
}

.quota-tip {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx dashed #ddd;
  text-align: center;
}

.quota-tip text {
  font-size: 24rpx;
  color: #666;
}

/* AI模型识别 */
.model-detect-box {
  padding: 30rpx 40rpx;
  border-top: 2rpx solid #f0f0f0;
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
}

.model-title {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.model-content {
  background: #fff;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #667eea;
}

.model-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  font-weight: 500;
}

/* AI特征列表 */
.features-box {
  padding: 30rpx 40rpx;
  border-top: 2rpx solid #f0f0f0;
  background: #f8f9fa;
}

.features-title {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.feature-item {
  display: flex;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #e9ecef;
}

.feature-item:last-child {
  border-bottom: none;
}

.feature-dot {
  font-size: 24rpx;
  color: #667eea;
  margin-right: 12rpx;
  font-weight: bold;
}

.feature-text {
  flex: 1;
  font-size: 26rpx;
  color: #495057;
  line-height: 1.5;
}
</style>
