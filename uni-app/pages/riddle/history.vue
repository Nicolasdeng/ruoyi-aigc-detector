<template>
	<view class="history-container">
		<view class="header">
			<text class="title">📋 答题历史</text>
		</view>

		<!-- 统计卡片 -->
		<view class="stats-card">
			<view class="stat-item">
				<text class="stat-value">{{ statistics.totalCount || 0 }}</text>
				<text class="stat-label">总题数</text>
			</view>
			<view class="stat-item">
				<text class="stat-value correct">{{ statistics.correctCount || 0 }}</text>
				<text class="stat-label">答对</text>
			</view>
			<view class="stat-item">
				<text class="stat-value wrong">{{ statistics.wrongCount || 0 }}</text>
				<text class="stat-label">答错</text>
			</view>
			<view class="stat-item">
				<text class="stat-value">{{ accuracy }}%</text>
				<text class="stat-label">正确率</text>
			</view>
		</view>

		<!-- 历史列表 -->
		<view class="history-list">
			<view 
				v-for="item in historyList" 
				:key="item.id"
				class="history-item"
				@click="viewDetail(item)"
			>
				<view class="item-header">
					<view class="result-badge" :class="item.isCorrect === '1' ? 'correct' : 'wrong'">
						{{ item.isCorrect === '1' ? '✓ 答对' : '✗ 答错' }}
					</view>
					<text class="time">{{ formatTime(item.answerTime) }}</text>
				</view>
				
				<view class="question-text">{{ item.question.question }}</view>
				
				<view class="answer-row">
					<text class="label">你的答案：</text>
					<text class="user-answer">{{ item.userAnswer }}</text>
				</view>
				
				<view class="answer-row" v-if="item.isCorrect === '0'">
					<text class="label">正确答案：</text>
					<text class="correct-answer">{{ item.question.answer }}</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view class="empty" v-if="historyList.length === 0">
				<text class="empty-icon">📝</text>
				<text class="empty-text">还没有答题记录哦</text>
				<button class="btn-start" @click="goToRiddle">开始答题</button>
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
			historyList: [],
			statistics: {}
		};
	},
	
	computed: {
		accuracy() {
			if (!this.statistics.totalCount || this.statistics.totalCount === 0) {
				return 0;
			}
			return Math.round((this.statistics.correctCount / this.statistics.totalCount) * 100);
		}
	},
	
	onLoad() {
		this.checkLoginStatus();
	},
	
	methods: {
		// 检查登录状态
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
			this.loadHistory();
			this.loadStatistics();
		},
		
		// 加载历史记录
		async loadHistory() {
			try {
				uni.showLoading({ title: '加载中...' });
				const res = await http.get('/riddle/history');
				if (res.code === 200) {
					this.historyList = res.data || [];
				}
			} catch (error) {
				console.error('加载历史失败:', error);
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		
		// 加载统计数据
		async loadStatistics() {
			try {
				const res = await http.get('/riddle/statistics');
				if (res.code === 200) {
					this.statistics = res.data || {};
				}
			} catch (error) {
				console.error('加载统计失败:', error);
			}
		},
		
		// 查看详情
		viewDetail(item) {
			uni.showModal({
				title: '题目详情',
				content: `${item.question.question}\n\n答案：${item.question.answer}\n\n${item.question.explanation || ''}`,
				showCancel: false
			});
		},
		
		// 格式化时间
		formatTime(time) {
			if (!time) return '';
			const date = new Date(time);
			const now = new Date();
			const diff = now - date;
			
			if (diff < 60000) {
				return '刚刚';
			} else if (diff < 3600000) {
				return Math.floor(diff / 60000) + '分钟前';
			} else if (diff < 86400000) {
				return Math.floor(diff / 3600000) + '小时前';
			} else {
				return date.toLocaleDateString();
			}
		},
		
		// 返回答题页面
		goToRiddle() {
			uni.navigateBack();
		}
	}
};
</script>

<style scoped>
.history-container {
	min-height: 100vh;
	background: #f5f5f5;
	padding: 20rpx;
}

.header {
	background: white;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	text-align: center;
}

.title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.stats-card {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 20rpx;
	display: flex;
	justify-content: space-around;
}

.stat-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	color: white;
}

.stat-value {
	font-size: 48rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.stat-value.correct {
	color: #52c41a;
}

.stat-value.wrong {
	color: #ff4d4f;
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.9;
}

.history-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.history-item {
	background: white;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.result-badge {
	padding: 8rpx 24rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	font-weight: bold;
}

.result-badge.correct {
	background: #f6ffed;
	color: #52c41a;
}

.result-badge.wrong {
	background: #fff1f0;
	color: #ff4d4f;
}

.time {
	font-size: 24rpx;
	color: #999;
}

.question-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.6;
	margin-bottom: 20rpx;
}

.answer-row {
	font-size: 26rpx;
	margin-bottom: 10rpx;
	display: flex;
	align-items: flex-start;
}

.label {
	color: #999;
	margin-right: 10rpx;
}

.user-answer {
	color: #666;
	flex: 1;
}

.correct-answer {
	color: #52c41a;
	font-weight: bold;
	flex: 1;
}

.empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 100rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 40rpx;
}

.btn-start {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	border: none;
	border-radius: 44rpx;
	padding: 20rpx 60rpx;
}
</style>
