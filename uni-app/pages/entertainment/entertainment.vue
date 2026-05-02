<template>
	<view class="entertainment-container">
		<!-- 头部标题 -->
		<view class="header">
			<text class="title">娱乐中心</text>
			<text class="subtitle">放松一下，玩点有趣的</text>
		</view>

		<!-- 功能卡片列表 -->
		<view class="card-list">
			<!-- 脑筋急转弯 -->
			<view class="fun-card" @click="navigateToRiddle">
				<view class="card-icon-wrapper riddle-bg">
					<text class="icon">🧠</text>
				</view>
				<view class="card-content">
					<view class="card-title">脑筋急转弯</view>
					<view class="card-desc">挑战你的思维，趣味答题</view>
					<view class="card-stats">
						<text class="stat-item">📝 {{ riddleStats.totalQuestions }}道题</text>
						<text class="stat-item">✅ 已答{{ riddleStats.answeredCount }}题</text>
					</view>
				</view>
				<view class="arrow">›</view>
			</view>

			<!-- 预留：猜谜语 -->
			<view class="fun-card disabled">
				<view class="card-icon-wrapper puzzle-bg">
					<text class="icon">🎯</text>
				</view>
				<view class="card-content">
					<view class="card-title">
						猜谜语
						<text class="coming-soon">敬请期待</text>
					</view>
					<view class="card-desc">中国传统谜语大挑战</view>
					<view class="card-stats">
						<text class="stat-item">🎨 即将上线</text>
					</view>
				</view>
			</view>

			<!-- 预留：成语接龙 -->
			<view class="fun-card disabled">
				<view class="card-icon-wrapper idiom-bg">
					<text class="icon">📚</text>
				</view>
				<view class="card-content">
					<view class="card-title">
						成语接龙
						<text class="coming-soon">敬请期待</text>
					</view>
					<view class="card-desc">传承中华文化，妙语连珠</view>
					<view class="card-stats">
						<text class="stat-item">🏆 即将上线</text>
					</view>
				</view>
			</view>

			<!-- 预留：智力问答 -->
			<view class="fun-card disabled">
				<view class="card-icon-wrapper quiz-bg">
					<text class="icon">💡</text>
				</view>
				<view class="card-content">
					<view class="card-title">
						智力问答
						<text class="coming-soon">敬请期待</text>
					</view>
					<view class="card-desc">百科知识，趣味竞答</view>
					<view class="card-stats">
						<text class="stat-item">🌟 即将上线</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部提示 -->
		<view class="footer-tip">
			<text>💫 更多精彩内容持续更新中...</text>
		</view>
	</view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'

export default {
	data() {
		return {
			riddleStats: {
				totalQuestions: 0,
				answeredCount: 0
			}
		};
	},
	onLoad() {
		this.checkLoginStatus();
	},
	onShow() {
		// 每次显示页面时刷新统计数据
		this.checkLoginStatus();
	},
	methods: {
		// 登录状态检查
	checkLoginStatus() {
		const isLoggedIn = checkLoginLocal();
		if (!isLoggedIn) {
			uni.showToast({
				title: '请先登录',
				icon: 'none',
				duration: 2000
			});
			setTimeout(() => {
				uni.reLaunch({
					url: '/pages/login/login'
				});
			}, 2000);
			return;
		}
		// 登录成功后加载数据
		this.loadRiddleStats();
	},

		// 加载脑筋急转弯统计数据
		async loadRiddleStats() {
			try {
				const res = await http.get('/riddle/statistics');
				if (res.data.code === 200) {
					const data = res.data.data;
					this.riddleStats = {
						totalQuestions: data.totalQuestions || 0,
						answeredCount: data.answeredCount || 0
					};
				}
			} catch (error) {
				console.error('加载统计数据失败:', error);
			}
		},

		// 跳转到脑筋急转弯
		navigateToRiddle() {
			uni.navigateTo({
				url: '/pages/riddle/riddle'
			});
		}
	}
};
</script>

<style scoped>
.entertainment-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 30rpx;
}

/* 头部 */
.header {
	text-align: center;
	margin-bottom: 60rpx;
	padding-top: 20rpx;
}

.title {
	display: block;
	font-size: 52rpx;
	font-weight: bold;
	color: #ffffff;
	margin-bottom: 16rpx;
	text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.subtitle {
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.9);
}

/* 卡片列表 */
.card-list {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.fun-card {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 32rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
	transition: all 0.3s ease;
}

.fun-card:active {
	transform: scale(0.98);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.fun-card.disabled {
	opacity: 0.6;
	pointer-events: none;
}

/* 图标 */
.card-icon-wrapper {
	width: 100rpx;
	height: 100rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 24rpx;
	flex-shrink: 0;
}

.riddle-bg {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.puzzle-bg {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.idiom-bg {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.quiz-bg {
	background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.icon {
	font-size: 48rpx;
}

/* 卡片内容 */
.card-content {
	flex: 1;
}

.card-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333333;
	margin-bottom: 12rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.coming-soon {
	font-size: 22rpx;
	color: #ff6b6b;
	background: #ffe5e5;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	font-weight: normal;
}

.card-desc {
	font-size: 26rpx;
	color: #666666;
	margin-bottom: 16rpx;
}

.card-stats {
	display: flex;
	gap: 20rpx;
	flex-wrap: wrap;
}

.stat-item {
	font-size: 24rpx;
	color: #999999;
}

/* 箭头 */
.arrow {
	font-size: 48rpx;
	color: #cccccc;
	margin-left: 16rpx;
}

/* 底部提示 */
.footer-tip {
	text-align: center;
	margin-top: 60rpx;
	padding: 20rpx;
}

.footer-tip text {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
}
</style>
