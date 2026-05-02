<template>
  <view class="container">
    <!-- 顶部标题区 -->
    <view class="header">
      <view class="header-content">
        <text class="title">AI检测工具</text>
        <text class="subtitle">智能识别AI生成内容</text>
      </view>
    </view>

    <!-- 检测类型卡片区 -->
    <view class="detect-types">
      <!-- 文本检测 -->
      <view class="type-card" @tap="navigateTo('/pages/detect/text')">
        <view class="card-icon-box text-icon">
          <text class="card-icon">📝</text>
        </view>
        <view class="card-content">
          <text class="card-title">文本检测</text>
          <text class="card-desc">识别AI生成的文本内容</text>
        </view>
        <view class="card-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 图片检测 -->
      <view class="type-card" @tap="navigateTo('/pages/detect/image')">
        <view class="card-icon-box image-icon">
          <text class="card-icon">🖼️</text>
        </view>
        <view class="card-content">
          <text class="card-title">图片检测</text>
          <text class="card-desc">识别AI生成的图片内容</text>
        </view>
        <view class="card-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 视频检测 -->
      <view class="type-card" @tap="navigateTo('/pages/detect/video')">
        <view class="card-icon-box video-icon">
          <text class="card-icon">🎬</text>
        </view>
        <view class="card-content">
          <text class="card-title">视频检测</text>
          <text class="card-desc">识别AI生成的视频内容</text>
        </view>
        <view class="card-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 音频检测 -->
      <view class="type-card" @tap="navigateTo('/pages/detect/audio')">
        <view class="card-icon-box audio-icon">
          <text class="card-icon">🎵</text>
        </view>
        <view class="card-content">
          <text class="card-title">音频检测</text>
          <text class="card-desc">识别AI生成的音频内容</text>
        </view>
        <view class="card-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 论文检测 -->
      <view class="type-card special-card" @tap="navigateTo('/pages/detect/paper')">
        <view class="card-icon-box paper-icon">
          <text class="card-icon">📄</text>
        </view>
        <view class="card-content">
          <text class="card-title">论文检测</text>
          <text class="card-desc">专业论文AI检测与优化</text>
          <view class="card-badge">
            <text class="badge-text">专业版</text>
          </view>
        </view>
        <view class="card-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'

export default {
  onLoad() {
    // 快速检查登录状态（仅本地检查，不发起网络请求）
    this.checkLoginStatus()
  },
  
  methods: {
    // 检查登录状态（同步本地检查，无网络请求）
    checkLoginStatus() {
      const isLoggedIn = checkLoginLocal()
      if (!isLoggedIn) {
        // 未登录，跳转到登录页
        uni.reLaunch({
          url: '/pages/login/login'
        })
      }
    },
    
    // 导航到检测页面
    navigateTo(url) {
      uni.navigateTo({
        url: url
      })
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
  padding: 80rpx 40rpx 40rpx;
  text-align: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  font-size: 56rpx;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 20rpx;
  letter-spacing: 2rpx;
}

.subtitle {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

/* 检测类型卡片 */
.detect-types {
  margin: 0 30rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.type-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 32rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
}

.type-card:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.special-card {
  background: linear-gradient(135deg, #fff5e6 0%, #ffe6cc 100%);
  border: 2rpx solid #ff9933;
}

.card-icon-box {
  width: 100rpx;
  height: 100rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.text-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.image-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.video-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.audio-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.paper-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.card-icon {
  font-size: 48rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.card-desc {
  font-size: 24rpx;
  color: #999;
}

.card-badge {
  position: absolute;
  top: -5rpx;
  right: 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.badge-text {
  font-size: 20rpx;
  color: #fff;
  font-weight: bold;
}

.card-arrow {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-icon {
  font-size: 48rpx;
  color: #ccc;
  font-weight: 300;
}
</style>
