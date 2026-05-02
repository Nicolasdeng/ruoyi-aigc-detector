<template>
	<view class="container">
		<!-- 配额显示组件 -->
		<quota-display ref="quotaDisplay" @quota-updated="onQuotaUpdated"></quota-display>
		
		<!-- 顶部导航 -->
		<view class="header">
			<text class="title">论文查重 & AI检测</text>
		</view>

		<!-- 论文标题输入 -->
		<view class="input-section">
			<view class="label">论文标题</view>
			<input 
				class="title-input" 
				v-model="paperTitle" 
				placeholder="请输入论文标题"
				maxlength="100"
			/>
		</view>

		<!-- 论文内容输入 -->
		<view class="input-section">
			<view class="label">
				论文内容
				<text class="word-count">{{ wordCount }}/{{ maxWords }}字</text>
			</view>
			<textarea 
				class="content-input" 
				v-model="paperContent" 
				placeholder="请粘贴论文内容（至少100字）"
				:maxlength="maxWords"
				@input="onContentInput"
			/>
		</view>

		<!-- 合规声明 -->
		<view class="disclaimer">
			<text class="disclaimer-text">
				⚠️ 检测结果仅供参考，不承诺100%通过查重系统
			</text>
		</view>

		<!-- 检测按钮 -->
		<view class="button-section">
			<button 
				class="detect-btn" 
				:class="{ disabled: !canDetect }"
				:disabled="!canDetect || detecting"
				@click="submitDetection"
			>
				<text v-if="!detecting">{{ buttonText }}</text>
				<view v-else class="detecting">
					<text class="loading-icon">⏳</text>
					<text>检测中...</text>
				</view>
			</button>
		</view>

		<!-- 检测进度 -->
		<view v-if="detecting" class="progress-section">
			<view class="progress-bar">
				<view class="progress-fill" :style="{ width: progress + '%' }"></view>
			</view>
			<text class="progress-text">{{ progressText }}</text>
		</view>

	</view>
</template>

<script>
import { checkLoginLocal } from '@/utils/auth.js'
import http from '@/utils/http.js'

export default {
	data() {
		return {
			paperTitle: '',
			paperContent: '',
			maxWords: 50000,
			detecting: false,
			progress: 0,
			progressText: '正在分析论文结构...'
		}
	},
	onLoad() {
		this.checkLoginStatus()
	},
	computed: {
		wordCount() {
			return this.paperContent.length;
		},
		canDetect() {
			return this.paperTitle.trim().length > 0 && 
			       this.paperContent.trim().length >= 100;
		},
		buttonText() {
			if (this.paperTitle.trim().length === 0) {
				return '请输入论文标题';
			}
			if (this.paperContent.trim().length < 100) {
				return `还需${100 - this.paperContent.trim().length}字`;
			}
			return '开始检测';
		}
	},
	methods: {
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
		
		onContentInput(e) {
			this.paperContent = e.detail.value;
		},
		
		async submitDetection() {
			if (!this.canDetect || this.detecting) {
				return;
			}
			
			// 检查配额
			if (!this.$refs.quotaDisplay.checkQuota()) {
				return;
			}
			
			this.detecting = true;
			this.progress = 0;
			this.simulateProgress();
			
			try {
				const data = await http.post('/paper/detection/submit', {
					title: this.paperTitle,
					content: this.paperContent
				});
				
				if (data) {
					// 检测成功后消耗配额
					await this.$refs.quotaDisplay.consumeQuota();
					
					this.progress = 100;
					this.progressText = '检测完成！';
					
					// 延迟跳转，让用户看到完成状态
					setTimeout(() => {
						uni.navigateTo({
							url: `/pages/paper/result?id=${data.detectionId}`
						});
					}, 500);
				}
			} catch (error) {
				this.detecting = false;
				uni.showToast({
					title: error.message || '检测失败，请重试',
					icon: 'none',
					duration: 2000
				});
			}
		},
		
		simulateProgress() {
			// 模拟进度条
			const steps = [
				{ progress: 20, text: '正在分析论文结构...' },
				{ progress: 40, text: '检测AI生成痕迹...' },
				{ progress: 60, text: '分析文本重复度...' },
				{ progress: 80, text: '识别模板化表达...' },
				{ progress: 95, text: '生成检测报告...' }
			];
			
			let index = 0;
			const timer = setInterval(() => {
				if (index < steps.length && this.detecting) {
					this.progress = steps[index].progress;
					this.progressText = steps[index].text;
					index++;
				} else {
					clearInterval(timer);
				}
			}, 1000);
		},
		
		// 配额更新回调
		onQuotaUpdated(quota) {
			// 配额信息已更新，可以在这里处理额外逻辑
			console.log('配额已更新:', quota);
		}
	}
}
</script>

<style scoped>
.container {
	min-height: 100vh;
	background: #f5f5f5;
	padding: 20rpx;
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx;
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 20rpx;
}

.title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.input-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.label {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	margin-bottom: 20rpx;
}

.word-count {
	font-size: 24rpx;
	color: #999;
}

.title-input {
	width: 100%;
	height: 80rpx;
	border: 2rpx solid #e5e5e5;
	border-radius: 8rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
}

.content-input {
	width: 100%;
	min-height: 600rpx;
	border: 2rpx solid #e5e5e5;
	border-radius: 8rpx;
	padding: 20rpx;
	font-size: 28rpx;
	line-height: 1.6;
}

.disclaimer {
	padding: 20rpx 30rpx;
	background: #fff3cd;
	border-radius: 8rpx;
	margin-bottom: 20rpx;
}

.disclaimer-text {
	font-size: 24rpx;
	color: #856404;
	line-height: 1.5;
}

.button-section {
	padding: 20rpx 0;
}

.detect-btn {
	width: 100%;
	height: 90rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 32rpx;
	font-weight: bold;
	border-radius: 45rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.detect-btn.disabled {
	background: #ccc;
}

.detecting {
	display: flex;
	align-items: center;
	gap: 10rpx;
}

.loading-icon {
	animation: rotate 1s linear infinite;
}

@keyframes rotate {
	from { transform: rotate(0deg); }
	to { transform: rotate(360deg); }
}

.progress-section {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-top: 20rpx;
}

.progress-bar {
	width: 100%;
	height: 10rpx;
	background: #e5e5e5;
	border-radius: 5rpx;
	overflow: hidden;
	margin-bottom: 20rpx;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
	transition: width 0.3s ease;
}

.progress-text {
	font-size: 24rpx;
	color: #666;
	text-align: center;
	display: block;
}
</style>
