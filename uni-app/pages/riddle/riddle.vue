<template>
	<view class="riddle-container">
		<!-- 顶部导航 -->
		<view class="header">
			<view class="title">🧠 脑筋急转弯</view>
			<view class="stats">
				<text>已答对: {{ statistics.correctCount || 0 }}</text>
				<text class="divider">|</text>
				<text>总题数: {{ statistics.totalCount || 0 }}</text>
			</view>
		</view>

		<!-- 难度选择 -->
		<view class="difficulty-selector">
			<view 
				v-for="item in difficultyList" 
				:key="item.value"
				:class="['difficulty-item', { active: difficulty === item.value }]"
				@click="selectDifficulty(item.value)"
			>
				{{ item.label }}
			</view>
		</view>

		<!-- 题目卡片 -->
		<view class="question-card" v-if="currentQuestion">
			<view class="question-header">
				<view class="category-tag">{{ getCategoryName(currentQuestion.category) }}</view>
				<view class="difficulty-tag" :class="currentQuestion.difficulty">
					{{ getDifficultyName(currentQuestion.difficulty) }}
				</view>
			</view>
			
			<view class="question-content">
				<text class="question-text">{{ currentQuestion.question }}</text>
			</view>

			<view class="question-meta">
				<text>👀 {{ currentQuestion.views || 0 }}次查看</text>
				<text>❤️ {{ currentQuestion.likes || 0 }}个赞</text>
			</view>

			<!-- 答案区域 -->
			<view class="answer-section" v-if="showAnswer">
				<view class="answer-title">💡 答案：</view>
				<view class="answer-content">{{ currentQuestion.answer }}</view>
				<view class="explanation-title" v-if="currentQuestion.explanation">📖 解析：</view>
				<view class="explanation-content" v-if="currentQuestion.explanation">
					{{ currentQuestion.explanation }}
				</view>
			</view>
		</view>

		<!-- 加载中 -->
		<view class="loading" v-else>
			<text>正在加载题目...</text>
		</view>

		<!-- 操作按钮 -->
		<view class="action-buttons">
			<button class="btn btn-primary" @click="toggleAnswer" v-if="!showAnswer">
				显示答案
			</button>
			<button class="btn btn-secondary" @click="getNextQuestion">
				下一题
			</button>
		</view>

		<!-- 底部导航 -->
		<view class="bottom-nav">
			<view class="nav-item" @click="goToHistory">
				<text class="icon">📋</text>
				<text>查看历史</text>
			</view>
			<view class="nav-item" @click="shareQuestion">
				<text class="icon">📤</text>
				<text>分享题目</text>
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
			currentQuestion: null,
			showAnswer: false,
			difficulty: '',
			statistics: {},
			difficultyList: [
				{ label: '全部', value: '' },
				{ label: '简单', value: 'easy' },
				{ label: '中等', value: 'medium' },
				{ label: '困难', value: 'hard' }
			],
			categoryMap: {
				'logic': '逻辑推理',
				'word': '文字游戏',
				'math': '数学思维',
				'life': '生活常识',
				'funny': '搞笑幽默'
			},
			difficultyMap: {
				'easy': '简单',
				'medium': '中等',
				'hard': '困难'
			}
		};
	},
	
	onLoad() {
		this.checkLoginStatus();
	},
	
	methods: {
		// 检查登录状态
		checkLoginStatus() {
			// 使用本地同步检查，避免不必要的网络请求
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
			this.loadStatistics();
			this.getRandomQuestion();
		},
		
		// 获取随机题目
		async getRandomQuestion() {
			try {
				uni.showLoading({ title: '加载中...' });
				
				const params = {};
				if (this.difficulty) {
					params.difficulty = this.difficulty;
				}
				
				const res = await http.get('/riddle/random', params);
				
				if (res.code === 200 && res.data) {
					this.currentQuestion = res.data;
					this.showAnswer = false;
				} else {
					uni.showToast({
						title: '暂无题目',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('获取题目失败:', error);
				uni.showToast({
					title: '获取题目失败',
					icon: 'none'
				});
			} finally {
				uni.hideLoading();
			}
		},
		
		// 显示/隐藏答案
		async toggleAnswer() {
			if (!this.showAnswer && this.currentQuestion) {
				try {
					const res = await http.get(`/riddle/answer/${this.currentQuestion.id}`);
					if (res.code === 200 && res.data) {
						this.currentQuestion = res.data;
						this.showAnswer = true;
					}
				} catch (error) {
					console.error('获取答案失败:', error);
				}
			} else {
				this.showAnswer = !this.showAnswer;
			}
		},
		
		// 获取下一题
		getNextQuestion() {
			this.getRandomQuestion();
		},
		
		// 选择难度
		selectDifficulty(value) {
			this.difficulty = value;
			this.getRandomQuestion();
		},
		
		// 加载统计数据
		async loadStatistics() {
			try {
				const res = await http.get('/riddle/statistics');
				if (res.code === 200) {
					this.statistics = res.data || {};
				}
			} catch (error) {
				console.error('获取统计失败:', error);
			}
		},
		
		// 查看历史
		goToHistory() {
			uni.navigateTo({
				url: '/pages/riddle/history'
			});
		},
		
		// 分享题目
		shareQuestion() {
			if (!this.currentQuestion) {
				uni.showToast({
					title: '暂无题目可分享',
					icon: 'none'
				});
				return;
			}
			
			uni.showModal({
				title: '分享题目',
				content: this.currentQuestion.question,
				confirmText: '复制',
				success: (res) => {
					if (res.confirm) {
						uni.setClipboardData({
							data: `【脑筋急转弯】${this.currentQuestion.question}`,
							success: () => {
								uni.showToast({
									title: '已复制到剪贴板',
									icon: 'success'
								});
							}
						});
					}
				}
			});
		},
		
		// 获取分类名称
		getCategoryName(category) {
			return this.categoryMap[category] || category;
		},
		
		// 获取难度名称
		getDifficultyName(difficulty) {
			return this.difficultyMap[difficulty] || difficulty;
		}
	}
};
</script>

<style scoped>
.riddle-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 20rpx;
	padding-bottom: 140rpx;
}

.header {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.title {
	font-size: 40rpx;
	font-weight: bold;
	color: #333;
	text-align: center;
	margin-bottom: 20rpx;
}

.stats {
	display: flex;
	justify-content: center;
	align-items: center;
	color: #666;
	font-size: 28rpx;
}

.divider {
	margin: 0 20rpx;
}

.difficulty-selector {
	display: flex;
	justify-content: space-around;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
}

.difficulty-item {
	flex: 1;
	text-align: center;
	padding: 16rpx 0;
	border-radius: 12rpx;
	font-size: 28rpx;
	color: #666;
	transition: all 0.3s;
}

.difficulty-item.active {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	font-weight: bold;
}

.question-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 20rpx;
	padding: 40rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.question-header {
	display: flex;
	justify-content: space-between;
	margin-bottom: 30rpx;
}

.category-tag {
	background: #f0f0f0;
	padding: 10rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #666;
}

.difficulty-tag {
	padding: 10rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: white;
}

.difficulty-tag.easy {
	background: #52c41a;
}

.difficulty-tag.medium {
	background: #faad14;
}

.difficulty-tag.hard {
	background: #f5222d;
}

.question-content {
	min-height: 200rpx;
	margin-bottom: 30rpx;
}

.question-text {
	font-size: 32rpx;
	line-height: 1.8;
	color: #333;
}

.question-meta {
	display: flex;
	justify-content: space-around;
	padding: 20rpx 0;
	border-top: 1rpx solid #f0f0f0;
	color: #999;
	font-size: 24rpx;
}

.answer-section {
	margin-top: 30rpx;
	padding-top: 30rpx;
	border-top: 2rpx dashed #e0e0e0;
}

.answer-title, .explanation-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #667eea;
	margin-bottom: 16rpx;
}

.answer-content {
	font-size: 32rpx;
	color: #f5222d;
	font-weight: bold;
	margin-bottom: 20rpx;
	padding: 20rpx;
	background: #fff3f3;
	border-radius: 12rpx;
}

.explanation-content {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
	padding: 20rpx;
	background: #f7f7f7;
	border-radius: 12rpx;
}

.loading {
	text-align: center;
	padding: 100rpx 0;
	color: white;
	font-size: 32rpx;
}

.action-buttons {
	display: flex;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 20rpx;
}

.btn {
	flex: 1;
	height: 88rpx;
	line-height: 88rpx;
	border-radius: 44rpx;
	font-size: 32rpx;
	border: none;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.btn-primary {
	background: white;
	color: #667eea;
}

.btn-secondary {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
}

.bottom-nav {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
	background: rgba(255, 255, 255, 0.95);
	padding: 20rpx;
	box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.nav-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	font-size: 24rpx;
	color: #666;
}

.nav-item .icon {
	font-size: 40rpx;
	margin-bottom: 8rpx;
}
</style>
