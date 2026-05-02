<template>
	<view class="container">
		<!-- 检测结果概览 -->
		<view class="result-overview">
			<view class="score-circle" :class="getRiskLevel(result.aiScore)">
				<text class="score">{{ result.aiScore }}</text>
				<text class="score-label">分</text>
			</view>
			<view class="result-info">
				<text class="result-title">{{ result.title }}</text>
				<text class="result-desc">{{ getResultDesc(result.aiScore) }}</text>
				<text class="detect-time">检测时间：{{ result.createTime }}</text>
			</view>
		</view>

		<!-- 统计信息 -->
		<view class="stats-section">
			<view class="stat-item">
				<text class="stat-value">{{ result.totalParagraphs }}</text>
				<text class="stat-label">总段落数</text>
			</view>
			<view class="stat-item">
				<text class="stat-value">{{ result.highRiskCount }}</text>
				<text class="stat-label">高风险段落</text>
			</view>
			<view class="stat-item">
				<text class="stat-value">{{ result.mediumRiskCount }}</text>
				<text class="stat-label">中风险段落</text>
			</view>
			<view class="stat-item">
				<text class="stat-value">{{ result.lowRiskCount }}</text>
				<text class="stat-label">低风险段落</text>
			</view>
		</view>

		<!-- 操作按钮 -->
		<view class="action-buttons">
			<button @click="handleOptimize" class="btn-optimize">
				<text class="btn-icon">✨</text>
				<text>一键智能优化</text>
			</button>
			
			<button @click="showOptimizePreview" class="btn-preview">
				<text class="btn-icon">👁</text>
				<text>查看优化建议</text>
			</button>
		</view>

		<!-- 段落详情 -->
		<view class="paragraphs-section">
			<view class="section-title">段落详情</view>
			<view 
				v-for="(para, index) in paragraphs" 
				:key="index"
				class="paragraph-card"
				:class="getRiskClass(para.aiRisk)"
			>
				<view class="para-header">
					<text class="para-index">段落 {{ index + 1 }}</text>
					<view class="para-risk">
						<text class="risk-label">AI风险：</text>
						<text class="risk-value">{{ para.aiRisk }}分</text>
					</view>
				</view>
				<view class="para-content">
					<text>{{ para.paragraphContent }}</text>
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
			detectionId: '',
			result: {},
			paragraphs: []
		};
	},
	
	onLoad(options) {
		this.detectionId = options.id;
		this.checkLoginStatus();
	},
	
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
			this.loadResult();
		},
	
	methods: {
		async loadResult() {
			uni.showLoading({ title: '加载中...' });
			
			try {
				const [resultRes, parasRes] = await Promise.all([
					http.get(`/paper/detection/result/${this.detectionId}`),
					http.get(`/paper/detection/paragraphs/${this.detectionId}`)
				]);
				
				uni.hideLoading();
				
				if (resultRes.code === 200) {
					this.result = resultRes.data;
				}
				
				if (parasRes.code === 200) {
					this.paragraphs = parasRes.data;
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			}
		},
		
		getRiskLevel(score) {
			if (score >= 60) return 'high';
			if (score >= 30) return 'medium';
			return 'low';
		},
		
		getResultDesc(score) {
			if (score >= 60) return 'AI生成风险较高，建议进行优化';
			if (score >= 30) return '存在一定AI生成特征，建议适当修改';
			return '内容自然度较好，通过检测概率高';
		},
		
		getRiskClass(risk) {
			if (risk >= 60) return 'high-risk';
			if (risk >= 30) return 'medium-risk';
			return 'low-risk';
		},
		
		async handleOptimize() {
			uni.showLoading({ title: '优化中...' });
			
			try {
				const res = await http.post(`/paper/detection/optimize/${this.detectionId}`, {
					level: 0.3
				});
				
				uni.hideLoading();
				
				if (res.code === 200) {
					// 显示优化结果并提供复制功能
					uni.showModal({
						title: '优化完成',
						content: '已生成优化后的内容，是否查看详情？',
						success: (modalRes) => {
							if (modalRes.confirm) {
								uni.navigateTo({
									url: `/pages/paper/optimize?detectionId=${this.detectionId}`
								});
							}
						}
					});
				} else {
					uni.showToast({
						title: res.msg || '优化失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '优化失败',
					icon: 'none'
				});
			}
		},
		
		showOptimizePreview() {
			uni.navigateTo({
				url: `/pages/paper/optimize?detectionId=${this.detectionId}`
			});
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

.result-overview {
	background: #fff;
	padding: 40rpx;
	display: flex;
	align-items: center;
	gap: 30rpx;
}

.score-circle {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	position: relative;
}

.score-circle.high {
	background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.score-circle.medium {
	background: linear-gradient(135deg, #ffa502 0%, #ff6348 100%);
}

.score-circle.low {
	background: linear-gradient(135deg, #26de81 0%, #20bf6b 100%);
}

.score {
	font-size: 56rpx;
	font-weight: bold;
	color: #fff;
}

.score-label {
	font-size: 24rpx;
	color: #fff;
	margin-top: -10rpx;
}

.result-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.result-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.result-desc {
	font-size: 26rpx;
	color: #666;
}

.detect-time {
	font-size: 24rpx;
	color: #999;
	margin-top: 5rpx;
}

.stats-section {
	display: flex;
	background: #fff;
	margin-top: 20rpx;
	padding: 30rpx;
}

.stat-item {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10rpx;
}

.stat-value {
	font-size: 40rpx;
	font-weight: bold;
	color: #667eea;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
	padding: 20rpx;
}

.btn-optimize, .btn-preview {
	flex: 1;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 10rpx;
	font-size: 28rpx;
}

.btn-optimize {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
}

.btn-preview {
	background: #f0f0f0;
	color: #333;
}

.btn-icon {
	margin-right: 10rpx;
	font-size: 32rpx;
}

.paragraphs-section {
	padding: 20rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 20rpx;
}

.paragraph-card {
	background: #fff;
	border-radius: 10rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	border-left: 4rpx solid #ccc;
}

.paragraph-card.high-risk {
	border-left-color: #f56c6c;
}

.paragraph-card.medium-risk {
	border-left-color: #e6a23c;
}

.paragraph-card.low-risk {
	border-left-color: #67c23a;
}

.para-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.para-index {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.para-risk {
	display: flex;
	align-items: center;
	gap: 5rpx;
}

.risk-label {
	font-size: 24rpx;
	color: #999;
}

.risk-value {
	font-size: 28rpx;
	font-weight: bold;
	color: #667eea;
}

.para-content {
	font-size: 28rpx;
	line-height: 1.8;
	color: #666;
}
</style>
