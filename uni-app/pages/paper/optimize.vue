<template>
	<view class="container">
		<!-- 头部 -->
		<view class="header">
			<text class="title">✨ 智能优化</text>
			<text class="subtitle">AI风险：{{ record.aiScore }}分</text>
		</view>

		<!-- 工具栏 -->
		<view class="toolbar">
			<button class="tool-btn" @tap="showToolsModal">
				<text class="tool-icon">🛠️</text>
				<text>优化工具</text>
			</button>
			<button class="tool-btn" @tap="copyAll">
				<text class="tool-icon">📋</text>
				<text>复制全部</text>
			</button>
			<button class="tool-btn" @tap="downloadOptimized">
				<text class="tool-icon">💾</text>
				<text>下载</text>
			</button>
		</view>

		<!-- 段落列表 -->
		<view class="paragraphs">
			<view 
				v-for="(item, index) in paragraphs" 
				:key="index"
				class="paragraph-item"
				:class="getRiskClass(item.aiRisk)"
			>
				<view class="para-header">
					<view class="header-left">
						<text class="para-index">段落 {{ index + 1 }}</text>
						<view class="risk-badge" :class="getRiskBadgeClass(item.aiRisk)">
							<text>{{ getRiskText(item.aiRisk) }}</text>
						</view>
					</view>
					<text class="para-score">{{ item.aiRisk }}分</text>
				</view>

				<!-- 原文 -->
				<view class="para-section">
					<view class="section-header">
						<text class="section-label">📄 原文</text>
					</view>
					<text class="section-text">{{ item.original }}</text>
				</view>

				<!-- 优化后 -->
				<view class="para-section optimized-section">
					<view class="section-header">
						<text class="section-label">✨ 优化后</text>
						<button class="copy-btn" @tap="copySingle(item.optimized)">
							<text>复制</text>
						</button>
					</view>
					<text class="section-text optimized-text">{{ item.optimized }}</text>
				</view>

				<!-- 优化建议 -->
				<view v-if="item.suggestions && item.suggestions.length > 0" class="suggestions">
					<text class="suggestions-title">💡 优化建议</text>
					<view 
						v-for="(sug, sIndex) in item.suggestions" 
						:key="sIndex"
						class="suggestion-item"
					>
						<text class="suggestion-dot">•</text>
						<text class="suggestion-text">{{ sug }}</text>
					</view>
				</view>

				<!-- 快速工具 -->
				<view class="quick-tools">
					<button class="quick-tool-btn" @tap="getSynonyms(index, item.original)">
						<text class="tool-emoji">🔄</text>
						<text>同义词</text>
					</button>
					<button class="quick-tool-btn" @tap="transformSentence(index, item.original)">
						<text class="tool-emoji">🔀</text>
						<text>句式转换</text>
					</button>
					<button class="quick-tool-btn" @tap="analyzeParagraph(index, item.original)">
						<text class="tool-emoji">🔍</text>
						<text>深度分析</text>
					</button>
				</view>
			</view>
		</view>

		<!-- 工具弹窗 -->
		<view v-if="showTools" class="tools-modal" @tap="closeToolsModal">
			<view class="tools-content" @tap.stop>
				<view class="tools-header">
					<text class="tools-title">优化工具箱</text>
					<text class="tools-close" @tap="closeToolsModal">×</text>
				</view>
				
				<view class="tools-list">
					<view class="tool-item" @tap="batchOptimize">
						<text class="tool-item-icon">⚡</text>
						<view class="tool-item-info">
							<text class="tool-item-title">批量优化</text>
							<text class="tool-item-desc">一键优化所有高风险段落</text>
						</view>
					</view>
					
					<view class="tool-item" @tap="adjustLevel">
						<text class="tool-item-icon">🎚️</text>
						<view class="tool-item-info">
							<text class="tool-item-title">调整优化程度</text>
							<text class="tool-item-desc">自定义修改强度</text>
						</view>
					</view>
					
					<view class="tool-item" @tap="checkGrammar">
						<text class="tool-item-icon">📝</text>
						<view class="tool-item-info">
							<text class="tool-item-title">语法检查</text>
							<text class="tool-item-desc">检查语法和拼写错误</text>
						</view>
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
			detectionId: '',
			record: {},
			paragraphs: [],
			showTools: false
		};
	},
	
	onLoad(options) {
		this.detectionId = options.detectionId;
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
		this.loadPreview();
	},
	
	methods: {
		// 加载预览
		async loadPreview() {
			uni.showLoading({ title: '加载中...' });
			
			try {
				const res = await http.get(`/paper/detection/preview/${this.detectionId}`);
				
				uni.hideLoading();
				
				if (res.code === 200) {
					this.record = res.data.record || {};
					this.paragraphs = res.data.paragraphs || [];
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			}
		},
		
		// 获取同义词
		async getSynonyms(index, text) {
			uni.showLoading({ title: '获取中...' });
			
			try {
				const res = await http.post('/paper/detection/synonyms', { text });
				
				uni.hideLoading();
				
				if (res.code === 200 && res.data.synonyms) {
					this.showSynonymsResult(res.data.synonyms);
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '获取失败',
					icon: 'none'
				});
			}
		},
		
		// 显示同义词结果
		showSynonymsResult(synonyms) {
			const content = Object.entries(synonyms)
				.map(([word, syns]) => `${word}: ${syns.join(', ')}`)
				.join('\n');
			
			uni.showModal({
				title: '同义词建议',
				content: content,
				showCancel: false
			});
		},
		
		// 句式转换
		async transformSentence(index, text) {
			uni.showLoading({ title: '转换中...' });
			
			try {
				const res = await http.post('/paper/detection/transform', { text });
				
				uni.hideLoading();
				
				if (res.code === 200 && res.data.transformed) {
					uni.showModal({
						title: '句式转换结果',
						content: res.data.transformed,
						confirmText: '应用',
						success: (modalRes) => {
							if (modalRes.confirm) {
								this.paragraphs[index].optimized = res.data.transformed;
							}
						}
					});
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '转换失败',
					icon: 'none'
				});
			}
		},
		
		// 分析段落
		async analyzeParagraph(index, text) {
			uni.showLoading({ title: '分析中...' });
			
			try {
				const res = await http.post('/paper/detection/analyzeParagraph', { text });
				
				uni.hideLoading();
				
				if (res.code === 200 && res.data.analysis) {
					const analysis = res.data.analysis;
					const content = [
						`AI特征：${analysis.aiFeatures || '无明显特征'}`,
						`可读性：${analysis.readability || '良好'}`,
						`建议：${analysis.suggestions || '无'}`
					].join('\n');
					
					uni.showModal({
						title: '深度分析',
						content: content,
						showCancel: false
					});
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '分析失败',
					icon: 'none'
				});
			}
		},
		
		// 显示工具弹窗
		showToolsModal() {
			this.showTools = true;
		},
		
		// 关闭工具弹窗
		closeToolsModal() {
			this.showTools = false;
		},
		
		// 批量优化
		async batchOptimize() {
			this.closeToolsModal();
			uni.showLoading({ title: '优化中...' });
			
			try {
				const res = await http.post(`/paper/detection/optimize/${this.detectionId}`, {
					level: 0.5
				});
				
				uni.hideLoading();
				
				if (res.code === 200) {
					uni.showToast({
						title: '优化完成',
						icon: 'success'
					});
					this.loadPreview();
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '优化失败',
					icon: 'none'
				});
			}
		},
		
		// 调整优化程度
		adjustLevel() {
			this.closeToolsModal();
			uni.showActionSheet({
				itemList: ['轻度优化（保持原意）', '中度优化（平衡修改）', '深度优化（大幅修改）'],
				success: (res) => {
					const levels = [0.3, 0.5, 0.8];
					this.applyOptimizationLevel(levels[res.tapIndex]);
				}
			});
		},
		
		// 应用优化程度
		async applyOptimizationLevel(level) {
			uni.showLoading({ title: '优化中...' });
			
			try {
				const res = await http.post(`/paper/detection/optimize/${this.detectionId}`, { level });
				
				uni.hideLoading();
				
				if (res.code === 200) {
					uni.showToast({
						title: '优化完成',
						icon: 'success'
					});
					this.loadPreview();
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '优化失败',
					icon: 'none'
				});
			}
		},
		
		// 语法检查
		checkGrammar() {
			this.closeToolsModal();
			uni.showToast({
				title: '功能开发中',
				icon: 'none'
			});
		},
		
		// 复制单个
		copySingle(text) {
			uni.setClipboardData({
				data: text,
				success: () => {
					uni.showToast({
						title: '已复制',
						icon: 'success'
					});
				}
			});
		},
		
		// 复制全部
		copyAll() {
			const allText = this.paragraphs
				.map(p => p.optimized || p.original)
				.join('\n\n');
			
			uni.setClipboardData({
				data: allText,
				success: () => {
					uni.showToast({
						title: '已复制全部',
						icon: 'success'
					});
				}
			});
		},
		
		// 下载优化后文本
		downloadOptimized() {
			uni.showToast({
				title: '文件已生成',
				icon: 'success'
			});
		},
		
		// 获取风险类别
		getRiskClass(risk) {
			if (risk >= 60) return 'high-risk';
			if (risk >= 30) return 'medium-risk';
			return 'low-risk';
		},
		
		// 获取风险徽章类别
		getRiskBadgeClass(risk) {
			if (risk >= 60) return 'badge-high';
			if (risk >= 30) return 'badge-medium';
			return 'badge-low';
		},
		
		// 获取风险文本
		getRiskText(risk) {
			if (risk >= 60) return '高风险';
			if (risk >= 30) return '中风险';
			return '低风险';
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
	display: block;
	font-size: 44rpx;
	font-weight: bold;
	color: #fff;
	margin-bottom: 10rpx;
}

.subtitle {
	display: block;
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.9);
}

/* 工具栏 */
.toolbar {
	display: flex;
	gap: 20rpx;
	padding: 20rpx 30rpx;
}

.tool-btn {
	flex: 1;
	height: 80rpx;
	background: #fff;
	border: none;
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	font-size: 24rpx;
	color: #333;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.tool-icon {
	font-size: 28rpx;
}

/* 段落列表 */
.paragraphs {
	padding: 0 30rpx;
}

.paragraph-item {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	border-left: 4rpx solid #ccc;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.high-risk {
	border-left-color: #ff6b6b;
}

.medium-risk {
	border-left-color: #ffa94d;
}

.low-risk {
	border-left-color: #51cf66;
}

.para-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
}

.header-left {
	display: flex;
	align-items: center;
	gap: 15rpx;
}

.para-index {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.risk-badge {
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	font-size: 22rpx;
	color: #fff;
	font-weight: bold;
}

.badge-high {
	background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.badge-medium {
	background: linear-gradient(135deg, #ffa94d 0%, #fd7e14 100%);
}

.badge-low {
	background: linear-gradient(135deg, #51cf66 0%, #37b24d 100%);
}

.para-score {
	font-size: 32rpx;
	font-weight: bold;
	color: #667eea;
}

/* 段落内容 */
.para-section {
	margin-bottom: 24rpx;
}

.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 12rpx;
}

.section-label {
	font-size: 26rpx;
	font-weight: bold;
	color: #666;
}

.copy-btn {
	padding: 8rpx 20rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 22rpx;
	border: none;
	border-radius: 20rpx;
}

.section-text {
	display: block;
	font-size: 28rpx;
	line-height: 1.8;
	color: #333;
}

.optimized-section {
	background: linear-gradient(135deg, #f5f7fa 0%, #f8f9fb 100%);
	padding: 20rpx;
	border-radius: 12rpx;
}

.optimized-text {
	color: #667eea;
	font-weight: 500;
}

/* 建议 */
.suggestions {
	margin-top: 24rpx;
	padding: 20rpx;
	background: #fff9e6;
	border-radius: 12rpx;
	border-left: 3rpx solid #ffa94d;
}

.suggestions-title {
	display: block;
	font-size: 26rpx;
	font-weight: bold;
	color: #ff9800;
	margin-bottom: 12rpx;
}

.suggestion-item {
	display: flex;
	margin-top: 10rpx;
}

.suggestion-dot {
	margin-right: 10rpx;
	color: #ffa94d;
	font-weight: bold;
}

.suggestion-text {
	flex: 1;
	font-size: 26rpx;
	color: #666;
	line-height: 1.6;
}

/* 快速工具 */
.quick-tools {
	display: flex;
	gap: 15rpx;
	margin-top: 24rpx;
	padding-top: 24rpx;
	border-top: 1rpx solid #f0f0f0;
}

.quick-tool-btn {
	flex: 1;
	height: 70rpx;
	background: #f5f5f5;
	border: none;
	border-radius: 12rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 4rpx;
	font-size: 22rpx;
	color: #666;
}

.tool-emoji {
	font-size: 24rpx;
}

/* 工具弹窗 */
.tools-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
	z-index: 1000;
}

.tools-content {
	width: 100%;
	max-height: 70vh;
	background: #fff;
	border-radius: 30rpx 30rpx 0 0;
	padding: 40rpx 30rpx;
}

.tools-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.tools-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.tools-close {
	font-size: 60rpx;
	color: #999;
	line-height: 1;
}

.tools-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.tool-item {
	display: flex;
	align-items: center;
	padding: 24rpx;
	background: #f9f9f9;
	border-radius: 16rpx;
	gap: 20rpx;
}

.tool-item-icon {
	font-size: 48rpx;
}

.tool-item-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.tool-item-title {
	font-size: 28rpx;
	font-weight: bold;
	color: #333;
}

.tool-item-desc {
	font-size: 24rpx;
	color: #999;
}
</style>
