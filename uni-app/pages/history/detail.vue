<template>
  <view class="detail-container">
    <!-- 顶部信息卡片 -->
    <view class="info-card">
      <view class="info-row">
        <text class="label">检测类型：</text>
        <text class="value">{{ detectionTypeText }}</text>
      </view>
      <view class="info-row">
        <text class="label">检测时间：</text>
        <text class="value">{{ record.createTime }}</text>
      </view>
      <view class="info-row">
        <text class="label">AI概率：</text>
        <text class="value" :class="getProbabilityClass(record.aiProbability)">
          {{ record.aiProbability }}%
        </text>
      </view>
      <view class="info-row">
        <text class="label">检测状态：</text>
        <text class="value" :class="getStatusClass(record.status)">
          {{ getStatusText(record.status) }}
        </text>
      </view>
    </view>

    <!-- 检测内容 -->
    <view class="content-card">
      <view class="card-title">检测内容</view>
      <view class="content-box">
        <!-- 文本内容 -->
        <view v-if="record.detectionType === 'TEXT'" class="text-content">
          {{ record.content }}
        </view>
        
        <!-- 图片内容 -->
        <view v-else-if="record.detectionType === 'IMAGE'" class="image-content">
          <image :src="getFullImageUrl(record.filePath)" mode="aspectFit" class="preview-image" />
        </view>
        
        <!-- 视频内容 -->
        <view v-else-if="record.detectionType === 'VIDEO'" class="video-content">
          <video :src="getFullVideoUrl(record.filePath)" controls class="preview-video"></video>
        </view>
        
        <!-- 音频内容 -->
        <view v-else-if="record.detectionType === 'AUDIO'" class="audio-content">
          <view class="audio-info">
            <text class="audio-icon">🎵</text>
            <text class="audio-name">{{ getFileName(record.filePath) }}</text>
          </view>
          <audio :src="getFullAudioUrl(record.filePath)" controls class="preview-audio"></audio>
        </view>
      </view>
    </view>

    <!-- 检测结果详情 -->
    <view class="result-card" v-if="record.result">
      <view class="card-title">检测结果详情</view>
      <view class="result-content">
        <text>{{ record.result }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-secondary" @click="shareResult">分享结果</button>
      <button class="btn btn-primary" @click="reDetect">重新检测</button>
    </view>
  </view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'

export default {
  data() {
    return {
      recordId: '',
      record: {
        detectionType: '',
        content: '',
        filePath: '',
        aiProbability: 0,
        status: '',
        createTime: '',
        result: ''
      }
    }
  },
  computed: {
    detectionTypeText() {
      const typeMap = {
        'TEXT': '文本检测',
        'IMAGE': '图片检测',
        'VIDEO': '视频检测',
        'AUDIO': '音频检测',
        'PAPER': '论文检测'
      }
      return typeMap[this.record.detectionType] || '未知类型'
    }
  },
  onLoad(options) {
    // 检查登录状态
    this.checkLoginStatus()
    
    if (options.id) {
      this.recordId = options.id
      this.loadDetail()
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
    
    // 加载详情
    async loadDetail() {
      uni.showLoading({
        title: '加载中...'
      })
      
      try {
        const res = await http.get(`/ai/history/${this.recordId}`)
        if (res.code === 200) {
          this.record = res.data
        } else {
          uni.showToast({
            title: res.msg || '加载失败',
            icon: 'none'
          })
        }
      } catch (error) {
        console.error('加载详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    },
    
    // 获取完整图片URL
    getFullImageUrl(path) {
      if (!path) return ''
      if (path.startsWith('http')) return path
      return 'http://localhost:8080' + path
    },
    
    // 获取完整视频URL
    getFullVideoUrl(path) {
      if (!path) return ''
      if (path.startsWith('http')) return path
      return 'http://localhost:8080' + path
    },
    
    // 获取完整音频URL
    getFullAudioUrl(path) {
      if (!path) return ''
      if (path.startsWith('http')) return path
      return 'http://localhost:8080' + path
    },
    
    // 获取文件名
    getFileName(path) {
      if (!path) return '未知文件'
      const parts = path.split('/')
      return parts[parts.length - 1]
    },
    
    // 获取概率样式类
    getProbabilityClass(probability) {
      if (probability >= 70) return 'high-risk'
      if (probability >= 40) return 'medium-risk'
      return 'low-risk'
    },
    
    // 获取状态样式类
    getStatusClass(status) {
      const classMap = {
        'SUCCESS': 'status-success',
        'FAILED': 'status-failed',
        'PROCESSING': 'status-processing'
      }
      return classMap[status] || ''
    },
    
    // 获取状态文本
    getStatusText(status) {
      const textMap = {
        'SUCCESS': '检测成功',
        'FAILED': '检测失败',
        'PROCESSING': '检测中'
      }
      return textMap[status] || '未知状态'
    },
    
    // 分享结果
    shareResult() {
      uni.showToast({
        title: '分享功能开发中',
        icon: 'none'
      })
    },
    
    // 重新检测
    reDetect() {
      const typePageMap = {
        'TEXT': '/pages/detect/text',
        'IMAGE': '/pages/detect/image',
        'VIDEO': '/pages/detect/video',
        'AUDIO': '/pages/detect/audio',
        'PAPER': '/pages/detect/paper'
      }
      
      const targetPage = typePageMap[this.record.detectionType]
      if (targetPage) {
        uni.navigateTo({
          url: targetPage
        })
      }
    }
  }
}
</script>

<style scoped>
.detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20rpx;
}

.info-card,
.content-card,
.result-card {
  background: white;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-size: 28rpx;
  color: #666;
}

.value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.high-risk {
  color: #f56c6c;
  font-weight: bold;
}

.medium-risk {
  color: #e6a23c;
  font-weight: bold;
}

.low-risk {
  color: #67c23a;
  font-weight: bold;
}

.status-success {
  color: #67c23a;
}

.status-failed {
  color: #f56c6c;
}

.status-processing {
  color: #409eff;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.content-box {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
}

.text-content {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
  word-break: break-all;
}

.image-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.preview-image {
  width: 100%;
  max-height: 600rpx;
  border-radius: 12rpx;
}

.video-content {
  width: 100%;
}

.preview-video {
  width: 100%;
  height: 400rpx;
  border-radius: 12rpx;
}

.audio-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.audio-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.audio-icon {
  font-size: 48rpx;
}

.audio-name {
  font-size: 28rpx;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-audio {
  width: 100%;
}

.result-content {
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
}

.btn {
  flex: 1;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 44rpx;
  font-size: 28rpx;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2rpx solid #667eea;
}
</style>
