<template>
	<view class="quota-display">
		<view class="quota-card" @click="handleQuotaClick">
			<!-- 左侧：配额信息 -->
			<view class="quota-info">
				<view class="quota-label">
					<text class="label-icon">🎯</text>
					<text class="label-text">剩余次数</text>
				</view>
				<view class="quota-value">
					<text class="current">{{ quotaInfo.remainingQuota || 0 }}</text>
					<text class="total">/{{ quotaInfo.totalQuota || 3 }}</text>
				</view>
			</view>

			<!-- 中间：会员标识 -->
			<view class="membership-badge" v-if="membershipInfo.type !== 'FREE'">
				<text class="badge-icon">👑</text>
				<text class="badge-text">{{ getMembershipName() }}</text>
			</view>

			<!-- 右侧：操作按钮 -->
			<view class="quota-actions">
				<view class="action-btn refresh-btn" @click.stop="handleRefresh">
					<text class="btn-icon">🔄</text>
				</view>
				<view class="action-btn upgrade-btn" v-if="membershipInfo.type === 'FREE'" @click.stop="handleUpgrade">
					<text class="btn-text">升级</text>
				</view>
			</view>
		</view>

		<!-- 配额不足提示 -->
		<view class="quota-warning" v-if="quotaInfo.remainingQuota === 0">
			<text class="warning-icon">⚠️</text>
			<text class="warning-text">配额已用完，</text>
			<text class="warning-link" @click="handleWatchAd">看广告获取</text>
			<text class="warning-text">或</text>
			<text class="warning-link" @click="handleUpgrade">升级会员</text>
		</view>
	</view>
</template>

<script>
import http from '@/utils/http.js';

export default {
	name: 'QuotaDisplay',
	data() {
		return {
			quotaInfo: {
				remainingQuota: 0,
				totalQuota: 3,
				usedQuota: 0,
				adRewardQuota: 0,
				resetTime: null
			},
			membershipInfo: {
				type: 'FREE',
				expireTime: null
			},
			loading: false
		};
	},
	mounted() {
		this.loadQuotaInfo();
	},
	methods: {
		// 加载配额信息
		async loadQuotaInfo() {
			if (this.loading) return;
			
			try {
				this.loading = true;
				
				// 并行请求配额和会员信息
				const [quotaRes, membershipRes] = await Promise.all([
					http.get('/quota/quota/info'),
					http.get('/quota/membership/info')
				]);

				if (quotaRes.code === 200) {
					this.quotaInfo = quotaRes.data;
				}

				if (membershipRes.code === 200) {
					this.membershipInfo = membershipRes.data;
				}

				// 触发父组件更新事件
				this.$emit('quota-updated', {
					quota: this.quotaInfo,
					membership: this.membershipInfo
				});

			} catch (error) {
				console.error('加载配额信息失败', error);
			} finally {
				this.loading = false;
			}
		},

		// 刷新配额
		async handleRefresh() {
			uni.showLoading({ title: '刷新中...' });
			await this.loadQuotaInfo();
			uni.hideLoading();
			uni.showToast({ title: '刷新成功', icon: 'success' });
		},

		// 点击配额卡片
		handleQuotaClick() {
			uni.navigateTo({
				url: '/pages/membership/center'
			});
		},

		// 升级会员
		handleUpgrade() {
			uni.navigateTo({
				url: '/pages/membership/purchase'
			});
		},

		// 看广告获取配额
		async handleWatchAd() {
			try {
				uni.showLoading({ title: '加载广告...' });

				// 调用广告奖励接口
				const res = await http.post('/quota/quota/ad-reward');

				if (res.code === 200) {
					uni.hideLoading();
					uni.showToast({
						title: '获得5次检测机会',
						icon: 'success',
						duration: 2000
					});

					// 刷新配额信息
					await this.loadQuotaInfo();
				} else {
					uni.hideLoading();
					uni.showToast({
						title: res.msg || '广告加载失败',
						icon: 'none'
					});
				}
			} catch (error) {
				uni.hideLoading();
				console.error('看广告失败', error);
				uni.showToast({
					title: '广告加载失败',
					icon: 'none'
				});
			}
		},

		// 获取会员名称
		getMembershipName() {
			const typeMap = {
				'FREE': '免费版',
				'GOLD': '黄金会员',
				'PLATINUM': '铂金会员'
			};
			return typeMap[this.membershipInfo.type] || '免费版';
		},

		// 检查配额是否充足
		checkQuota() {
			if (this.quotaInfo.remainingQuota <= 0) {
				uni.showModal({
					title: '配额不足',
					content: '您的检测次数已用完，是否看广告获取配额或升级会员？',
					confirmText: '看广告',
					cancelText: '升级会员',
					success: (res) => {
						if (res.confirm) {
							this.handleWatchAd();
						} else if (res.cancel) {
							this.handleUpgrade();
						}
					}
				});
				return false;
			}
			return true;
		},

		// 消耗配额
		async consumeQuota() {
			try {
				const res = await http.post('/quota/quota/consume', { count: 1 });
				
				if (res.code === 200) {
					// 更新本地配额信息
					this.quotaInfo.remainingQuota = res.data.remainingQuota;
					this.quotaInfo.usedQuota = res.data.usedQuota;
					
					// 触发父组件更新事件
					this.$emit('quota-consumed', {
						quota: this.quotaInfo
					});
					
					return true;
				} else {
					uni.showToast({
						title: res.msg || '配额消耗失败',
						icon: 'none'
					});
					return false;
				}
			} catch (error) {
				console.error('消耗配额失败', error);
				uni.showToast({
					title: '配额消耗失败',
					icon: 'none'
				});
				return false;
			}
		}
	}
};
</script>

<style scoped>
.quota-display {
	padding: 20rpx 30rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.quota-card {
	display: flex;
	align-items: center;
	justify-content: space-between;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 16rpx;
	padding: 24rpx 28rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

/* 左侧配额信息 */
.quota-info {
	flex: 1;
}

.quota-label {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
}

.label-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
}

.label-text {
	font-size: 24rpx;
	color: #666;
}

.quota-value {
	display: flex;
	align-items: baseline;
}

.current {
	font-size: 48rpx;
	font-weight: bold;
	color: #667eea;
	line-height: 1;
}

.total {
	font-size: 28rpx;
	color: #999;
	margin-left: 4rpx;
}

/* 中间会员标识 */
.membership-badge {
	display: flex;
	align-items: center;
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	margin: 0 20rpx;
}

.badge-icon {
	font-size: 24rpx;
	margin-right: 6rpx;
}

.badge-text {
	font-size: 24rpx;
	color: #fff;
	font-weight: 500;
}

/* 右侧操作按钮 */
.quota-actions {
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.action-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 12rpx 20rpx;
	border-radius: 20rpx;
	transition: all 0.3s;
}

.refresh-btn {
	background: #f0f0f0;
}

.refresh-btn:active {
	background: #e0e0e0;
}

.btn-icon {
	font-size: 28rpx;
}

.upgrade-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.upgrade-btn:active {
	opacity: 0.8;
}

.btn-text {
	font-size: 26rpx;
	color: #fff;
	font-weight: 500;
}

/* 配额不足提示 */
.quota-warning {
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 20rpx;
	padding: 16rpx 24rpx;
	background: rgba(255, 255, 255, 0.2);
	border-radius: 12rpx;
	backdrop-filter: blur(10rpx);
}

.warning-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
}

.warning-text {
	font-size: 24rpx;
	color: #fff;
}

.warning-link {
	font-size: 24rpx;
	color: #ffe066;
	font-weight: 500;
	margin: 0 6rpx;
	text-decoration: underline;
}
</style>
