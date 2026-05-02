<template>
  <view class="container">
    <!-- 配额显示组件 -->
    <quota-display ref="quotaDisplay" @quota-updated="onQuotaUpdated"></quota-display>
    
    <!-- 顶部标题栏 -->
    <view class="header">
      <view class="header-content">
        <text class="title">AI视频检测</text>
        <text class="subtitle">智能识别AI生成视频</text>
      </view>
    </view>

    <!-- 检测区域 -->
    <view class="detect-area">
      <!-- 上传视频区域 -->
      <view class="upload-section" v-if="!videoUrl">
        <view class="upload-box" @tap="chooseVideo">
          <text class="upload-icon">🎬</text>
          <text class="upload-text">点击上传视频</text>
          <text class="upload-tip">支持MP4、MOV格式，最大100MB</text>
        </view>
      </view>

      <!-- 视频预览区域 -->
      <view class="preview-section" v-if="videoUrl">
        <video 
          class="preview-video" 
          :src="videoUrl" 
          controls
          :show-center-play-btn="true"
          :show-play-btn="true"
        ></video>
        <view class="reupload-btn" @tap="chooseVideo">
          <text class="reupload-text">重新上传</text>
        </view>
      </view>

      <!-- 检测按钮 -->
      <view class="action-section" v-if="videoUrl && !detecting">
        <button class="detect-btn" @tap="startDetection">开始检测</button>
      </view>

      <!-- 检测中状态 -->
      <view class="detecting-section" v-if="detecting">
        <view class="loading-animation">
          <view class="loading-circle"></view>
          <text class="loading-text">AI视频分析中...</text>
          <text class="loading-desc">这可能需要一些时间</text>
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

          <!-- 详细信息 -->
          <view class="details-box">
            <view class="detail-item">
              <text class="detail-label">检测结果：</text>
              <text class="detail-value" :style="{color: getResultColor()}">{{ getResultText() }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">视频时长：</text>
              <text class="detail-value">{{ detectionResult.duration }}秒</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">分析帧数：</text>
              <text class="detail-value">{{ detectionResult.frames }}帧</text>
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
    <view class="info-section" v-if="!videoUrl && !detectionResult">
      <view class="info-title">
        <text>功能特色</text>
      </view>
      <view class="info-list">
        <view class="info-item">
          <text class="info-icon">🎬</text>
          <view class="info-content">
            <text class="info-name">逐帧分析</text>
            <text class="info-desc">对视频每一帧进行AI检测</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">🔍</text>
          <view class="info-content">
            <text class="info-name">深度学习</text>
            <text class="info-desc">基于先进的视频检测模型</text>
          </view>
        </view>
        <view class="info-item">
          <text class="info-icon">📊</text>
          <view class="info-content">
            <text class="info-name">综合判断</text>
            <text class="info-desc">多维度分析给出准确结果</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'

export default {
  data() {
    return {
      videoUrl: '',
      tempFilePath: '',
      detecting: false,
      detectionResult: null,
      baseUrl: 'http://localhost:8080'
    }
  },
  onLoad() {
    this.checkLoginStatus()
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

    // 选择视频
    chooseVideo() {
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        maxDuration: 60,
        camera: 'back',
        success: (res) => {
          this.tempFilePath = res.tempFilePath
          this.videoUrl = res.tempFilePath
          this.detectionResult = null
        }
      })
    },

    // 开始检测
    startDetection() {
      if (!this.tempFilePath) {
        uni.showToast({
          title: '请先上传视频',
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

      // 获取用户信息
      const userInfo = uni.getStorageSync('userInfo') || {}
      const userId = userInfo.userId || null

      // 实际API调用
      uni.uploadFile({
        url: `${this.baseUrl}/ai/detection/video/upload`,
        filePath: this.tempFilePath,
        name: 'file',
        formData: {
          userId: userId
        },
        header: {
          'Authorization': uni.getStorageSync('token') || ''
        },
        timeout: 120000, // 2分钟超时
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200 && data.data) {
              const record = data.data
              
              // 解析检测详情
              let details = {}
              try {
                if (record.detectionDetails) {
                  details = typeof record.detectionDetails === 'string' 
                    ? JSON.parse(record.detectionDetails) 
                    : record.detectionDetails
                }
              } catch (e) {
                console.warn('解析detectionDetails失败:', e)
              }
              
              // 转换为前端需要的格式
              this.detectionResult = {
                score: parseFloat(record.confidenceScore || 0),
                isAI: record.detectionResult === 'AI_GENERATED',
                duration: details.duration || details.details?.duration || 0,
                frames: details.sampledFrames || details.details?.sampledFrames || 0,
                result: record.detectionResult,
                recordId: record.id
              }
              
              // 检测成功后消耗配额
              await this.$refs.quotaDisplay.consumeQuota()
              
              uni.showToast({
                title: '检测完成',
                icon: 'success',
                duration: 2000
              })
            } else {
              throw new Error(data.msg || '检测失败')
            }
          } catch (e) {
            console.error('处理响应失败:', e)
            uni.showToast({
              title: e.message || '检测失败，请重试',
              icon: 'none',
              duration: 3000
            })
          }
        },
        fail: (err) => {
          console.error('上传失败:', err)
          let errorMsg = '上传失败，请重试'
          
          if (err.errMsg) {
            if (err.errMsg.includes('timeout')) {
              errorMsg = '检测超时，请稍后重试'
            } else if (err.errMsg.includes('fail')) {
              errorMsg = '网络连接失败，请检查网络'
            }
          }
          
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 3000
          })
        },
        complete: () => {
          this.detecting = false
        }
      })
    },

    // 重置检测
    resetDetection() {
      this.videoUrl = ''
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

    // 获取结果图标
    getResultIcon() {
      if (!this.detectionResult) return ''
      return this.detectionResult.isAI ? '🤖' : '👤'
    },

    // 获取结果图标样式
    getResultIconClass() {
      if (!this.detectionResult) return ''
      return this.detectionResult.isAI ? 'icon-ai' : 'icon-human'
    },

    // 获取结果标题
    getResultTitle() {
      if (!this.detectionResult) return ''
      return this.detectionResult.isAI ? 'AI生成视频' : '真实拍摄视频'
    },

    // 获取结果文本
    getResultText() {
      if (!this.detectionResult) return ''
      return this.detectionResult.isAI ? 'AI生成' : '真实拍摄'
    },

    // 获取结果颜色
    getResultColor() {
      if (!this.detectionResult) return '#333'
      return this.detectionResult.isAI ? '#ff6b6b' : '#51cf66'
    },

    // 获取置信度颜色
    getConfidenceColor() {
      if (!this.detectionResult) return '#4a90e2'
      const score = this.detectionResult.score
      if (score >= 80) return '#ff6b6b'
      if (score >= 60) return '#ffa94d'
      return '#51cf66'
    },

    // 配额更新回调
    onQuotaUpdated(quota) {
      // 配额信息已更新，可以在这里处理额外逻辑
      console.log('配额已更新:', quota)
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
  font-size: 120rpx;
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
  position: relative;
}

.preview-video {
  width: 100%;
  height: 400rpx;
  border-radius: 20rpx;
  background: #000;
}

.reupload-btn {
  margin-top: 20rpx;
  padding: 12rpx 24rpx;
  background: #f0f0f0;
  border-radius: 40rpx;
  text-align: center;
}

.reupload-text {
  font-size: 24rpx;
  color: #666;
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
  margin-bottom: 12rpx;
}

.loading-desc {
  font-size: 24rpx;
  color: #999;
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
</style>
