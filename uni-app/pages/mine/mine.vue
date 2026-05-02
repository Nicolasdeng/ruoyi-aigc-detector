<template>
  <view class="container">
    <!-- 顶部个人信息区 -->
    <view class="header">
      <view class="user-info">
        <view class="avatar-box">
          <text class="avatar-icon">👤</text>
        </view>
        <view class="user-details">
          <text class="username">访客用户</text>
          <text class="user-desc">欢迎使用AI检测工具</text>
        </view>
      </view>
    </view>

    <!-- 今日统计卡片 -->
    <view class="today-stats" v-if="todayStats">
      <view class="stats-header">
        <text class="stats-title">📊 今日数据</text>
        <text class="stats-date">{{ getCurrentDate() }}</text>
      </view>
      <view class="stats-grid">
        <view class="stat-box">
          <text class="stat-number">{{ todayStats.totalDetections || 0 }}</text>
          <text class="stat-label">检测次数</text>
        </view>
        <view class="stat-box">
          <text class="stat-number">{{ todayStats.textDetections || 0 }}</text>
          <text class="stat-label">文本检测</text>
        </view>
        <view class="stat-box">
          <text class="stat-number">{{ todayStats.imageDetections || 0 }}</text>
          <text class="stat-label">图片检测</text>
        </view>
        <view class="stat-box">
          <text class="stat-number">{{ todayStats.videoDetections || 0 }}</text>
          <text class="stat-label">视频检测</text>
        </view>
      </view>
    </view>

    <!-- 数据趋势卡片 -->
    <view class="trend-card" @tap="navigateToTrend">
      <view class="card-header">
        <text class="card-title">📈 数据趋势</text>
        <text class="card-action">查看详情 ›</text>
      </view>
      <text class="card-desc">查看您的检测数据变化趋势</text>
    </view>

    <!-- 检测历史卡片 -->
    <view class="history-card" @tap="navigateToHistory">
      <view class="card-header">
        <text class="card-title">📋 检测历史</text>
        <text class="card-action">查看全部 ›</text>
      </view>
      <text class="card-desc">查看所有检测记录</text>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-section">
      <!-- 统计信息 -->
      <view class="menu-item" @tap="navigateToTrend">
        <view class="menu-icon-box stats-icon">
          <text class="menu-icon">📊</text>
        </view>
        <view class="menu-content">
          <text class="menu-title">数据趋势</text>
          <text class="menu-desc">查看使用统计数据</text>
        </view>
        <view class="menu-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 历史记录 -->
      <view class="menu-item" @tap="navigateToHistory">
        <view class="menu-icon-box history-icon">
          <text class="menu-icon">📋</text>
        </view>
        <view class="menu-content">
          <text class="menu-title">检测历史</text>
          <text class="menu-desc">查看所有检测记录</text>
        </view>
        <view class="menu-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 设置 -->
      <view class="menu-item" @tap="showComingSoon">
        <view class="menu-icon-box settings-icon">
          <text class="menu-icon">⚙️</text>
        </view>
        <view class="menu-content">
          <text class="menu-title">设置</text>
          <text class="menu-desc">应用配置与偏好</text>
        </view>
        <view class="menu-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>

      <!-- 关于 -->
      <view class="menu-item" @tap="showAbout">
        <view class="menu-icon-box about-icon">
          <text class="menu-icon">ℹ️</text>
        </view>
        <view class="menu-content">
          <text class="menu-title">关于我们</text>
          <text class="menu-desc">应用信息与版本</text>
        </view>
        <view class="menu-arrow">
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>

    <!-- 版本信息 -->
    <view class="footer">
      <text class="version">Version 1.0.0</text>
    </view>
  </view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'

export default {
  data() {
    return {
      todayStats: null
    }
  },
  
  onShow() {
    // 检查登录状态
    this.checkLoginStatus()
    this.loadTodayStats();
  },
  
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
  
  methods: {
    // 加载今日统计数据
    async loadTodayStats() {
      try {
        const res = await http.get('/ai/statistics/today');
        if (res.code === 200) {
          this.todayStats = res.data;
        }
      } catch (error) {
        console.error('加载今日统计失败:', error);
      }
    },
    
    // 获取当前日期
    getCurrentDate() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    // 导航到数据趋势页面
    navigateToTrend() {
      uni.navigateTo({
        url: '/pages/statistics/trend'
      });
    },
    
    // 导航到历史记录页面
    navigateToHistory() {
      uni.navigateTo({
        url: '/pages/history/history'
      })
    },

    // 显示即将推出提示
    showComingSoon() {
      uni.showToast({
        title: '功能开发中，敬请期待',
        icon: 'none',
        duration: 2000
      })
    },

    // 显示关于信息
    showAbout() {
      uni.showModal({
        title: '关于AI检测工具',
        content: 'AI检测工具v1.0.0\n\n集成多种AI检测引擎，帮助您识别AI生成的图片、文本、视频和音频内容。\n\n准确性放第一位，多调用开源检测模型。',
        confirmText: '知道了',
        showCancel: false
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
  padding: 60rpx 40rpx 30rpx;
}

.user-info {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.avatar-box {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;
}

.avatar-icon {
  font-size: 60rpx;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.username {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.user-desc {
  font-size: 26rpx;
  color: #999;
}

/* 今日统计 */
.today-stats {
  margin: 0 30rpx 20rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.stats-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.stats-date {
  font-size: 24rpx;
  color: #999;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.stat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  background: linear-gradient(135deg, #f5f7fa 0%, #f8f9fb 100%);
  border-radius: 16rpx;
}

.stat-number {
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8rpx;
}

.stat-label {
  font-size: 22rpx;
  color: #666;
  text-align: center;
}

/* 趋势卡片 */
.trend-card {
  margin: 0 30rpx 20rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.card-action {
  font-size: 26rpx;
  color: #667eea;
}

.card-desc {
  font-size: 26rpx;
  color: #999;
}

/* 历史卡片 */
.history-card {
  margin: 0 30rpx 20rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.menu-section {
  margin: 0 30rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 24rpx;
  padding: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.menu-item {
  padding: 24rpx;
  display: flex;
  align-items: center;
  border-radius: 16rpx;
  margin-bottom: 8rpx;
  transition: background 0.3s ease;
}

.menu-item:active {
  background: #f8f9fa;
}

.menu-item:last-child {
  margin-bottom: 0;
}

.menu-icon-box {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.history-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.settings-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.about-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.menu-icon {
  font-size: 40rpx;
}

.menu-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.menu-title {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 6rpx;
}

.menu-desc {
  font-size: 24rpx;
  color: #999;
}

.menu-arrow {
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

.footer {
  margin-top: 40rpx;
  text-align: center;
  padding: 20rpx;
}

.version {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.7);
}
</style>
