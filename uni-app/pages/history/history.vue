<template>
	<view class="container">
		<!-- 头部 -->
		<view class="header">
			<text class="title">📋 检测历史</text>
		</view>

		<!-- 统计信息 -->
		<view class="stats-section">
			<view class="stat-card">
				<text class="stat-value">{{ summary.totalDetections || 0 }}</text>
				<text class="stat-label">总检测数</text>
			</view>
			<view class="stat-card">
				<text class="stat-value ai">{{ summary.aiCount || 0 }}</text>
				<text class="stat-label">AI生成</text>
			</view>
			<view class="stat-card">
				<text class="stat-value human">{{ summary.humanCount || 0 }}</text>
				<text class="stat-label">人类创作</text>
			</view>
		</view>

		<!-- 操作栏 -->
		<view class="action-bar">
			<view class="filter-tabs">
				<view 
					v-for="(tab, index) in filterTabs" 
					:key="index"
					class="filter-tab"
					:class="{ active: currentFilter === tab.value }"
					@tap="changeFilter(tab.value)"
				>
					<text>{{ tab.label }}</text>
				</view>
			</view>
			<button class="export-btn" @tap="exportHistory">
				<text class="btn-icon">📤</text>
				<text>导出</text>
			</button>
		</view>

		<!-- 历史记录列表 -->
		<view class="history-list">
			<view 
				v-for="(item, index) in historyList" 
				:key="index"
				class="history-item"
				@tap="viewDetail(item)"
				@longpress="showActionMenu(item)"
			>
				<view class="item-header">
					<view class="item-type" :class="getTypeClass(item.detectionType)">
						<text>{{ getTypeName(item.detectionType) }}</text>
					</view>
					<text class="item-time">{{ formatTime(item.createTime) }}</text>
				</view>
				
				<view class="item-content">
					<text class="item-desc">{{ getItemDesc(item) }}</text>
				</view>
				
				<view class="item-footer">
					<view class="result-badge" :class="getResultClass(item.aiScore)">
						<text>AI概率: {{ item.aiScore }}%</text>
					</view>
					<text class="status-text">{{ getStatusText(item.status) }}</text>
				</view>
			</view>

			<!-- 空状态 -->
			<view v-if="historyList.length === 0 && !loading" class="empty-state">
				<text class="empty-icon">📭</text>
				<text class="empty-text">暂无检测历史</text>
				<button class="empty-btn" @tap="gotoHome">开始检测</button>
			</view>

			<!-- 加载更多 -->
			<view v-if="hasMore && !loading" class="load-more" @tap="loadMore">
				<text>加载更多</text>
			</view>
			
			<view v-if="loading" class="loading">
				<text>加载中...</text>
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
			summary: {},
			currentFilter: 'all',
			filterTabs: [
				{ label: '全部', value: 'all' },
				{ label: '文本', value: 'text' },
				{ label: '图片', value: 'image' },
				{ label: '视频', value: 'video' },
				{ label: '音频', value: 'audio' }
			],
			historyList: [],
			pageNum: 1,
			pageSize: 10,
			hasMore: true,
			loading: false
		};
	},
	
	onLoad() {
		this.checkLoginStatus();
	},
	
	onShow() {
		this.loadSummary();
		this.refreshList();
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
		// 加载统计摘要
		async loadSummary() {
			try {
				const res = await http.get('/ai/statistics/summary');
				if (res.code === 200) {
					this.summary = res.data;
				}
			} catch (error) {
				console.error('加载统计失败:', error);
			}
		},
		
		// 刷新列表
		refreshList() {
			this.pageNum = 1;
			this.historyList = [];
			this.hasMore = true;
			this.loadHistoryList();
		},
		
		// 加载历史列表
		async loadHistoryList() {
			if (this.loading || !this.hasMore) return;
			
			this.loading = true;
			
			try {
				const params = {
					pageNum: this.pageNum,
					pageSize: this.pageSize
				};
				
				if (this.currentFilter !== 'all') {
					params.detectionType = this.currentFilter;
				}
				
				const res = await http.get('/ai/history/list', { params });
				
				if (res.code === 200) {
					const newList = res.rows || [];
					this.historyList = [...this.historyList, ...newList];
					this.hasMore = newList.length === this.pageSize;
				} else {
					uni.showToast({
						title: res.msg || '加载失败',
						icon: 'none'
					});
				}
			} catch (error) {
				console.error('加载历史失败:', error);
				uni.showToast({
					title: '加载失败',
					icon: 'none'
				});
			} finally {
				this.loading = false;
			}
		},
		
		// 加载更多
		loadMore() {
			this.pageNum++;
			this.loadHistoryList();
		},
		
		// 切换筛选
		changeFilter(filter) {
			this.currentFilter = filter;
			this.refreshList();
		},
		
		// 查看详情
		async viewDetail(item) {
			try {
				const res = await http.get(`/ai/history/${item.id}`);
				
				if (res.code === 200) {
					const detail = res.data;
					
					// 根据类型跳转到对应详情页
					if (item.detectionType === 'paper') {
						uni.navigateTo({
							url: `/pages/paper/result?id=${detail.relatedId}`
						});
					} else {
						// 显示详细信息弹窗
						this.showDetailModal(detail);
					}
				}
			} catch (error) {
				uni.showToast({
					title: '加载详情失败',
					icon: 'none'
				});
			}
		},
		
		// 显示详情弹窗
		showDetailModal(detail) {
			const content = [
				`检测类型: ${this.getTypeName(detail.detectionType)}`,
				`AI概率: ${detail.aiScore}%`,
				`检测时间: ${detail.createTime}`,
				`状态: ${this.getStatusText(detail.status)}`
			].join('\n');
			
			uni.showModal({
				title: '检测详情',
				content: content,
				showCancel: false
			});
		},
		
		// 显示操作菜单
		showActionMenu(item) {
			uni.showActionSheet({
				itemList: ['查看详情', '删除记录'],
				success: (res) => {
					if (res.tapIndex === 0) {
						this.viewDetail(item);
					} else if (res.tapIndex === 1) {
						this.confirmDelete(item);
					}
				}
			});
		},
		
		// 确认删除
		confirmDelete(item) {
			uni.showModal({
				title: '确认删除',
				content: '确定要删除这条记录吗？',
				success: (res) => {
					if (res.confirm) {
						this.deleteRecord(item.id);
					}
				}
			});
		},
		
		// 删除记录
		async deleteRecord(id) {
			try {
				const res = await http.delete(`/ai/history/${id}`);
				
				if (res.code === 200) {
					uni.showToast({
						title: '删除成功',
						icon: 'success'
					});
					this.refreshList();
					this.loadSummary();
				} else {
					uni.showToast({
						title: res.msg || '删除失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.showToast({
					title: '删除失败',
					icon: 'none'
				});
			}
		},
		
		// 导出历史
		async exportHistory() {
			uni.showLoading({ title: '导出中...' });
			
			try {
				const res = await http.post('/ai/history/export', {
					detectionType: this.currentFilter === 'all' ? null : this.currentFilter
				});
				
				uni.hideLoading();
				
				if (res.code === 200) {
					uni.showModal({
						title: '导出成功',
						content: `文件已导出: ${res.data.fileName || '历史记录.xlsx'}`,
						showCancel: false
					});
				} else {
					uni.showToast({
						title: res.msg || '导出失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.hideLoading();
				uni.showToast({
					title: '导出失败',
					icon: 'none'
				});
			}
		},
		
		// 跳转首页
		gotoHome() {
			uni.switchTab({
				url: '/pages/index/index'
			});
		},
		
		// 获取类型名称
		getTypeName(type) {
			const names = {
				text: '文本',
				image: '图片',
				video: '视频',
				audio: '音频',
				paper: '论文'
			};
			return names[type] || '未知';
		},
		
		// 获取类型样式
		getTypeClass(type) {
			return `type-${type}`;
		},
		
		// 获取结果样式
		getResultClass(score) {
			if (score >= 60) return 'result-high';
			if (score >= 30) return 'result-medium';
			return 'result-low';
		},
		
		// 获取状态文本
		getStatusText(status) {
			const texts = {
				completed: '已完成',
				processing: '处理中',
				failed: '失败'
			};
			return texts[status] || '未知';
		},
		
		// 获取项目描述
		getItemDesc(item) {
			if (item.fileName) return item.fileName;
			if (item.textContent) return item.textContent.substring(0, 50) + '...';
			return '检测记录';
		},
		
		// 格式化时间
		formatTime(timeStr) {
			if (!timeStr) return '';
			const date = new Date(timeStr);
			const now = new Date();
			const diff = now - date;
			
			if (diff < 60000) return '刚刚';
			if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
			if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
			if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前';
			
			return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`;
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

/* 统计卡片 */
.stats-section {
	display: flex;
	gap: 20rpx;
	padding: 30rpx;
}

.stat-card {
	flex: 1;
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx 20rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.stat-value {
	font-size: 48rpx;
	font-weight: bold;
	color: #667eea;
	margin-bottom: 8rpx;
}

.stat-value.ai {
	color: #ff6b6b;
}

.stat-value.human {
	color: #51cf66;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
}

/* 操作栏 */
.action-bar {
	padding: 0 30rpx 20rpx;
	display: flex;
	gap: 20rpx;
	align-items: center;
}

.filter-tabs {
	flex: 1;
	display: flex;
	background: #fff;
	border-radius: 16rpx;
	padding: 6rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	overflow-x: auto;
}

.filter-tab {
	flex-shrink: 0;
	padding: 16rpx 24rpx;
	text-align: center;
	border-radius: 12rpx;
	font-size: 26rpx;
	color: #666;
	transition: all 0.3s;
}

.filter-tab.active {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-weight: bold;
}

.export-btn {
	padding: 16rpx 24rpx;
	background: #fff;
	border: none;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 26rpx;
	color: #667eea;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.btn-icon {
	font-size: 28rpx;
}

/* 历史列表 */
.history-list {
	padding: 0 30rpx;
}

.history-item {
	background: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.item-type {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #fff;
	font-weight: bold;
}

.type-text {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.type-image {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.type-video {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.type-audio {
	background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.type-paper {
	background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.item-time {
	font-size: 24rpx;
	color: #999;
}

.item-content {
	margin-bottom: 20rpx;
}

.item-desc {
	font-size: 28rpx;
	color: #666;
	line-height: 1.6;
}

.item-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.result-badge {
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
	color: #fff;
	font-weight: bold;
}

.result-high {
	background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
}

.result-medium {
	background: linear-gradient(135deg, #ffa94d 0%, #fd7e14 100%);
}

.result-low {
	background: linear-gradient(135deg, #51cf66 0%, #37b24d 100%);
}

.status-text {
	font-size: 24rpx;
	color: #999;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 0;
	gap: 24rpx;
}

.empty-icon {
	font-size: 120rpx;
	opacity: 0.3;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

.empty-btn {
	width: 300rpx;
	height: 80rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	font-size: 28rpx;
	border: none;
	border-radius: 40rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

/* 加载状态 */
.load-more, .loading {
	padding: 30rpx 0;
	text-align: center;
	font-size: 28rpx;
	color: #999;
}
</style>
