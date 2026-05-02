<template>
  <view class="container">
    <!-- 配额显示组件 -->
    <quota-display ref="quotaDisplay" @quota-updated="onQuotaUpdated"></quota-display>
    
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-content">
        <text class="title">AI文本检测</text>
        <text class="subtitle">智能识别AI生成文本</text>
      </view>
    </view>

    <!-- 检测区域 -->
    <view class="detect-area">
      <!-- 文本输入区域 -->
      <view class="input-section">
        <textarea 
          class="text-input" 
          v-model="inputText"
          placeholder="请输入或粘贴需要检测的文本内容..."
          :maxlength="20000"
          :auto-height="true"
          :show-confirm-bar="false"
        ></textarea>
        <view class="input-footer">
          <text class="char-count">{{ inputText.length }}/20000</text>
          <button class="clear-btn" v-if="inputText" @tap="clearText">清空</button>
        </view>
      </view>

      <!-- 检测按钮 -->
      <view class="action-section" v-if="inputText && !detecting">
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
          <!-- 结果标题 -->
          <view class="result-header">
            <view class="result-icon" :class="getResultIconClass()">
              <text class="icon-text">{{ getResultIcon() }}</text>
            </view>
            <text class="result-title">{{ getResultTitle() }}</text>
          </view>

          <!-- 置信度 -->
          <view class="confidence-box">
            <view class="confidence-label">
              <text>AI生成概率</text>
            </view>
            <view class="confidence-bar">
              <view class="confidence-fill" :style="{width: detectionResult.score + '%', backgroundColor: getConfidenceColor()}"></view>
            </view>
            <text class="confidence-value">{{ detectionResult.score }}%</text>
          </view>

          <!-- 检测建议 -->
          <view class="suggestion-box">
            <view class="suggestion-header">
              <text class="suggestion-icon">💡</text>
              <text class="suggestion-title">分析说明</text>
            </view>
            <text class="suggestion-text">{{ getDetectionSuggestion() }}</text>
          </view>

          <!-- 详细信息 -->
          <view class="details-box">
            <view class="detail-item">
              <text class="detail-label">检测结果：</text>
              <text class="detail-value" :style="{color: getResultColor()}">{{ getResultText() }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">文本长度：</text>
              <text class="detail-value">{{ getTextLength() }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="result-actions">
            <button class="action-btn secondary" @tap="viewHistory">查看历史</button>
            <button class="action-btn primary" @tap="clearAndReset">再次检测</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能说明 -->
    <view class="info-section" v-if="!inputText && !detectionResult">
      <view class="info-title">
        <text>检测说明</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="info-icon">📝</text>
          <view class="info-content">
            <text class="info-name">支持多种文本</text>
            <text class="info-desc">文章、评论、对话等各类文本</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">🎯</text>
          <view class="info-content">
            <text class="info-name">AI模型分析</text>
            <text class="info-desc">基于最新AI检测模型</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">⚡</text>
          <view class="info-content">
            <text class="info-name">快速检测</text>
            <text class="info-desc">秒级返回检测结果</text>
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
      inputText: '',
      detecting: false,
      detectionResult: null,
      minTextLength: 10 // 最小文本长度常量
    }
  },
  
  onLoad() {
    this.checkLoginStatus()
  },
  
  computed: {
    // 计算属性：是否可以开始检测
    canDetect() {
      return this.inputText && this.inputText.length >= this.minTextLength && !this.detecting
    },
    
    // 计算属性：字符计数显示
    charCountDisplay() {
      return `${this.inputText.length}/20000`
    }
  },
  
  methods: {
    // 检查登录状态
    checkLoginStatus() {
      const isLoggedIn = checkLoginLocal()
      if (!isLoggedIn) {
        uni.showToast({
          title: '请先登录',
          icon: 'none',
          duration: 2000
        })
        setTimeout(() => {
          uni.reLaunch({
            url: '/pages/login/login'
          })
        }, 2000)
      }
    },
    
    // 清空文本
    clearText() {
      this.resetState()
    },

    // 重置状态（统一的重置方法）
    resetState() {
      this.inputText = ''
      this.detectionResult = null
      this.detecting = false
    },

    // 开始检测
    async startDetection() {
      // 验证输入
      if (!this.validateInput()) {
        return
      }

      // 检查配额
      if (!this.$refs.quotaDisplay.checkQuota()) {
        return
      }

      this.detecting = true
      this.detectionResult = null

      try {
        const res = await this.performDetection()
        this.handleDetectionSuccess(res)
        // 检测成功后消耗配额
        await this.$refs.quotaDisplay.consumeQuota()
      } catch (err) {
        this.handleDetectionError(err)
      } finally {
        this.detecting = false
      }
    },

    // 验证输入
    validateInput() {
      if (!this.inputText || this.inputText.length < this.minTextLength) {
        uni.showToast({
          title: `文本内容过短，请输入至少${this.minTextLength}个字符`,
          icon: 'none',
          duration: 2000
        })
        return false
      }
      return true
    },

    // 执行检测请求
    async performDetection() {
      try {
        const res = await http.post('/ai/detection/text/detect', {
          text: this.inputText
        })
        return res
      } catch (error) {
        throw error
      }
    },

    // 处理检测成功
    handleDetectionSuccess(data) {
      this.detectionResult = data
      uni.showToast({
        title: '检测完成',
        icon: 'success',
        duration: 1500
      })
    },

    // 处理检测错误
    handleDetectionError(err) {
      console.error('检测失败:', err)
      const errorMsg = err.message || '网络错误，请检查连接'
      uni.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 2500
      })
    },

    // 清空并重置（用于"再次检测"按钮）
    clearAndReset() {
      this.resetState()
    },

    // 查看历史记录
    viewHistory() {
      uni.navigateTo({
        url: '/pages/history/history'
      })
    },

    // 获取分数对应的颜色（统一颜色映射）
    getScoreColor(score) {
      const colorMap = [
        { threshold: 75, color: '#ff6b6b' },  // 高AI概率-红色
        { threshold: 60, color: '#ff8c42' },  // 中高AI概率-橙红色
        { threshold: 40, color: '#ffa94d' },  // 混合-橙色
        { threshold: 25, color: '#74c0fc' },  // 中低AI概率-浅蓝色
        { threshold: 0, color: '#51cf66' }    // 低AI概率-绿色
      ]
      
      for (const { threshold, color } of colorMap) {
        if (score >= threshold) return color
      }
      return '#51cf66'
    },

    // 获取结果图标
    getResultIcon() {
      return this.detectionResult?.isAI ? '🤖' : '👤'
    },

    // 获取结果图标样式
    getResultIconClass() {
      return this.detectionResult?.isAI ? 'icon-ai' : 'icon-human'
    },

    // 获取结果标题
    getResultTitle() {
      return this.detectionResult?.isAI ? 'AI生成文本' : '人工撰写文本'
    },

    // 获取结果文本（基于评分给出更明确的结论）
    getResultText() {
      if (!this.detectionResult) return ''
      const score = this.detectionResult.score
      
      const resultMap = [
        { threshold: 75, text: 'AI生成（高置信度）' },
        { threshold: 60, text: 'AI生成（中置信度）' },
        { threshold: 40, text: '混合内容（AI辅助人工）' },
        { threshold: 25, text: '人工创作（中置信度）' },
        { threshold: 0, text: '人工创作（高置信度）' }
      ]
      
      for (const { threshold, text } of resultMap) {
        if (score >= threshold) return text
      }
      return '人工创作（高置信度）'
    },

    // 获取结果颜色
    getResultColor() {
      return this.detectionResult ? this.getScoreColor(this.detectionResult.score) : '#333'
    },

    // 获取置信度颜色
    getConfidenceColor() {
      return this.detectionResult ? this.getScoreColor(this.detectionResult.score) : '#4a90e2'
    },

    // 获取单个模型评分颜色
    getModelScoreColor(score) {
      return this.getScoreColor(score)
    },
    
    // 获取检测建议
    getDetectionSuggestion() {
      if (!this.detectionResult) return ''
      const score = this.detectionResult.score
      
      const suggestionMap = [
        { 
          threshold: 75, 
          text: '该文本极有可能由AI生成，表现出明显的AI写作特征，如：句式规整、逻辑严密、缺乏个性化表达' 
        },
        { 
          threshold: 60, 
          text: '该文本很可能包含AI生成内容，建议进一步核实。可能存在AI辅助创作或内容重写的情况' 
        },
        { 
          threshold: 40, 
          text: '该文本可能是AI辅助人工创作的结果，既有AI生成的痕迹，也包含人工编辑和修改' 
        },
        { 
          threshold: 25, 
          text: '该文本大部分为人工创作，可能有轻微的AI辅助（如语法修正、润色等）' 
        },
        { 
          threshold: 0, 
          text: '该文本极有可能完全由人工创作，未检测到明显的AI生成特征，保留了自然的写作风格' 
        }
      ]
      
      for (const { threshold, text } of suggestionMap) {
        if (score >= threshold) return text
      }
      return suggestionMap[suggestionMap.length - 1].text
    },
    
    // 获取可靠性等级
    getReliabilityLevel() {
      if (!this.detectionResult) return ''
      
      const textLength = this.inputText.length
      const hasModels = this.detectionResult.models?.length > 0
      
      // 基于文本长度和模型数量判断可靠性
      if (textLength >= 500 && hasModels) return '高'
      if (textLength >= 200 && hasModels) return '中'
      if (textLength >= 100) return '中'
      return '低（建议输入更长文本）'
    },
    
    // 获取可靠性颜色
    getReliabilityColor() {
      const level = this.getReliabilityLevel()
      const colorMap = {
        '高': '#51cf66',
        '中': '#ffa94d',
        '默认': '#ff6b6b'
      }
      
      if (level.includes('高')) return colorMap['高']
      if (level.includes('中')) return colorMap['中']
      return colorMap['默认']
    },
    
    // 获取文本长度
    getTextLength() {
      const length = this.inputText?.length || 0
      return `${length}字符`
    },
    
    // 获取模型数量
    getModelCount() {
      const count = this.detectionResult?.models?.length || 0
      return `${count}个AI检测引擎`
    },
    
    // 配额更新回调
    onQuotaUpdated(data) {
      console.log('配额信息已更新:', data)
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

.input-section {
  margin-bottom: 30rpx;
}

.text-input {
  width: 100%;
  min-height: 400rpx;
  padding: 30rpx;
  background: #f8f9fa;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding: 0 10rpx;
}

.char-count {
  font-size: 24rpx;
  color: #999;
}

.clear-btn {
  padding: 8rpx 24rpx;
  background: #f0f0f0;
  color: #666;
  font-size: 24rpx;
  border: none;
  border-radius: 20rpx;
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
  background: #f8f9fa;
  border-radius: 20rpx;
  padding: 40rpx;
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.result-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.icon-ai {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.icon-human {
  background: linear-gradient(135deg, #51cf66 0%, #37b24d 100%);
}

.icon-text {
  font-size: 40rpx;
}

.result-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.confidence-box {
  margin-bottom: 30rpx;
}

.confidence-label {
  margin-bottom: 16rpx;
}

.confidence-label text {
  font-size: 26rpx;
  color: #666;
}

.confidence-bar {
  height: 16rpx;
  background: #e9ecef;
  border-radius: 8rpx;
  overflow: hidden;
  margin-bottom: 12rpx;
}

.confidence-fill {
  height: 100%;
  border-radius: 8rpx;
  transition: width 0.3s ease;
}

.confidence-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.details-box {
  border-top: 2rpx solid #e9ecef;
  padding-top: 30rpx;
  margin-bottom: 30rpx;
}

.detail-item {
  display: flex;
  margin-bottom: 20rpx;
  line-height: 1.6;
}

.detail-label {
  font-size: 26rpx;
  color: #666;
  min-width: 140rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #333;
  flex: 1;
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

.suggestion-box {
  background: #fff9e6;
  border-left: 6rpx solid #ffa94d;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 30rpx;
}

.suggestion-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.suggestion-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.suggestion-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #e67700;
}

.suggestion-text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.8;
}

.models-box {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 2rpx solid #e9ecef;
}

.models-title {
  margin-bottom: 20rpx;
}

.models-title text {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.model-item {
  margin-bottom: 24rpx;
}

.model-item:last-child {
  margin-bottom: 0;
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.model-name {
  font-size: 24rpx;
  color: #666;
}

.model-score {
  font-size: 24rpx;
  font-weight: bold;
}

.model-bar {
  height: 12rpx;
  background: #e9ecef;
  border-radius: 6rpx;
  overflow: hidden;
}

.model-fill {
  height: 100%;
  border-radius: 6rpx;
  transition: width 0.3s ease;
}
</style>
