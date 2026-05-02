<template>
	<view class="container">
		<!-- 头部 -->
		<view class="header">
			<text class="title">📈 数据趋势</text>
		</view>

		<!-- 时间范围选择 -->
		<view class="time-selector">
			<view 
				v-for="(item, index) in timeRanges" 
				:key="index"
				class="time-item"
				:class="{ active: currentRange === item.value }"
				@tap="selectTimeRange(item.value)"
			>
				<text>{{ item.label }}</text>
			</view>
		</view>

		<!-- 趋势图表区域 -->
		<view class="chart-section">
			<view class="section-title">检测次数趋势</view>
			<view class="chart-container">
				<view v-if="trendData && trendData.length > 0" class="trend-chart">
					<view 
						v-for="(item, index) in trendData" 
						:key="index"
						class="chart-bar-wrapper"
					>
						<view class="bar-value">{{ item.count }}</view>
						<view class="chart-bar">
							<view 
								class="bar-fill"
								:style="{ height: getBarHeight(item.count) + '%' }"
							></view>
						</view>
						<text class="bar-label">{{ formatDate(item.date) }}</text>
					</view>
				</view>
				<view v-else class="empty-chart">
					<text class="empty-icon">📊</text>
					<text class="empty-text">暂无数据</text>
				</view>
			</view>
		</view>

		<!-- 类型分布 -->
		<view class="distribution-section">
			<view class="section-title">检测类型分布</view>
			<view class="distribution-list">
				<view class="dist-item" v-for="(type, index) in typeDistribution" :key="index">
					<view class="dist-info">
						<text class="dist-icon">{{ type.icon }}</text>
						<text class="dist-name">{{ type.name }}</text>
					</view>
					<view class="dist-bar-wrapper">
						<view class="dist-bar">
							<view 
								class="dist-bar-fill"
								:style="{ 
									width: type.percentage + '%',
									background: type.color 
								}"
							></view>
						</view>
						<text class="dist-count">{{ type.count }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 总体统计 -->
		<view class="summary-section">
			<view class="section-title">总体统计</view>
			<view class="summary-grid">
				<view class="summary-item">
					<text class="summary-value">{{ summary.totalCount || 0 }}</text>
					<text class="summary-label">总检测次数</text>
				</view>
				<view class="summary-item">
					<text class="summary-value">{{ summary.avgPerDay || 0 }}</text>
					<text class="summary-label">日均检测</text>
				</view>
				<view class="summary-item">
					<text class="summary-value">{{ summary.peakDay || '-' }}</text>
					<text class="summary-label">峰值日期</text>
				</view>
				<view class="summary-item">
					<text class="summary-value">{{ summary.peakCount || 0 }}</text>
					<text class="summary-label">峰值次数</text>
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
			currentRange: 7,
			timeRanges: [
				{ label: '7天', value: 7 },
				{ label: '15天', value: 15 },
				{ label: '30天', value: 30 }
			],
			trendData: [],
			typeDistribution: [],
			summary: {},
			maxCount: 0
		};
	},
	
	onLoad() {
		// 检查登录状态
		this.checkLoginStatus()
		this.loadTrendData();
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
		
		// 选择时间范围
		selectTimeRange(days) {
			this.currentRange = days;
			this.loadTrendData();
		},
		
		// 加载趋势数据
		async loadTrendData() {
			uni.showLoading({ title: '加载中...' });
			
			try {
				const res = await http.get('/ai/statistics/trend', {
					params: { days: this.currentRange }
				});
				
				if (res.code === 200) {
					const data = res.data;
					
					// 处理趋势数据
					this.trendData = data.dailyTrend || [];
					this.maxCount = Math.max(...this.trendData.map(item => item.count), 1);
					
					// 处理类型分布
					this.processTypeDistribution(data.typeDistribution || {});
					
					// 处理总体统计
					this.summary = {
						totalCount: data.totalCount || 0,
						avgPerDay: Math.round(data.avgPerDay || 0),
						peakDay: data.peakDay || '-',
						peakCount: data.peakCount || 0
					};
				} else {
					uni.showToast({
						title: res.msg || '加载失败',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('加载趋势数据失败:', error);
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		
		// 处理类型分布数据
		processTypeDistribution(distribution) {
			const total = Object.values(distribution).reduce((sum, count) => sum + count, 0);
			
			const typeConfig = [
				{ key: 'text', name: '文本检测', icon: '📝', color: '#f093fb' },
				{ key: 'image', name: '图片检测', icon: '🖼️', color: '#667eea' },
				{ key: 'video', name: '视频检测', icon: '🎬', color: '#4facfe' },
				{ key: 'audio', name: '音频检测', icon: '🎵', color: '#43e97b' },
				{ key: 'paper', name: '论文检测', icon: '📄', color: '#fa709a' }
			];
			
			this.typeDistribution = typeConfig.map(config => ({
				...config,
				count: distribution[config.key] || 0,
				percentage: total > 0 ? Math.round((distribution[config.key] || 0) / total * 100) : 0
			})).filter(item => item.count > 0);
		},
		
		// 计算柱状图高度
		getBarHeight(count) {
			if (this.maxCount === 0) return 0;
			return Math.max((count / this.maxCount * 100), 5);
		},
		
		// 格式化日期
		formatDate(dateStr) {
			const date = new Date(dateStr);
			const month = date.getMonth() + 1;
			const day = date.getDate();
			return `${month}/${day}`;
		}
	}
};
</script>

<style scoped>
.container {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 40rpx;
}

.header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 80rpx 40rpx 40rpx;
	text-align: center;
}

.title {
	font-size: 44rpx;
	font-weight: bold;
	color: #fff;
}

/* 时间选择器 */
.time-selector {
	display: flex;
	gap: 20rpx;
	padding: 30rpx;
	background: #fff;
	margin-bottom: 20rpx;
}

.time-item {
	flex: 1;
	padding: 20rpx;
	text-align: center;
	background: #f5f5f5;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #666;
	transition: all 0.3s;
}

.time-item.active {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-weight: bold;
}

/* 图表区域 */
.chart-section, .distribution-section, .summary-section {
	background: #fff;
	margin-bottom: 20rpx;
	padding: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 30rpx;
}

.chart-container {
	min-height: 400rpx;
	padding: 20rpx;
	background: #f9f9f9;
	border-radius: 16rpx;
}

.trend-chart {
	display: flex;
	align-items: flex-end;
	justify-content: space-around;
	height: 350rpx;
	gap: 10rpx;
}

.chart-bar-wrapper {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 8rpx;
}

.bar-value {
	font-size: 22rpx;
	color: #666;
	font-weight: bold;
	min-height: 30rpx;
}

.chart-bar {
	width: 100%;
	height: 250rpx;
	background: #e8e8e8;
	border-radius: 8rpx 8rpx 0 0;
	display: flex;
	align-items: flex-end;
	overflow: hidden;
}

.bar-fill {
	width: 100%;
	background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
	border-radius: 8rpx 8rpx 0 0;
	transition: height 0.5s ease;
}

.bar-label {
	font-size: 20rpx;
	color: #999;
	margin-top: 8rpx;
}

.empty-chart {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	height: 350rpx;
	gap: 20rpx;
}

.empty-icon {
	font-size: 80rpx;
	opacity: 0.3;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 类型分布 */
.distribution-list {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.dist-item {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
}

.dist-info {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.dist-icon {
	font-size: 36rpx;
}

.dist-name {
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
}

.dist-bar-wrapper {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.dist-bar {
	flex: 1;
	height: 20rpx;
	background: #f0f0f0;
	border-radius: 10rpx;
	overflow: hidden;
}

.dist-bar-fill {
	height: 100%;
	border-radius: 10rpx;
	transition: width 0.5s ease;
}

.dist-count {
	font-size: 28rpx;
	color: #667eea;
	font-weight: bold;
	min-width: 60rpx;
	text-align: right;
}

/* 总体统计 */
.summary-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.summary-item {
	background: linear-gradient(135deg, #f5f7fa 0%, #f8f9fb 100%);
	padding: 30rpx;
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10rpx;
}

.summary-value {
	font-size: 40rpx;
	font-weight: bold;
	color: #667eea;
}

.summary-label {
	font-size: 24rpx;
	color: #999;
}
</style>
