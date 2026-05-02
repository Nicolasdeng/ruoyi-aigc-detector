<template>
	<view class="membership-center">
		<!-- 顶部背景 -->
		<view class="header-bg">
			<view class="header-content">
				<!-- 会员状态卡片 -->
				<view class="membership-card">
					<view class="card-header">
						<view class="user-info">
							<image class="avatar" :src="userAvatar || '/static/default-avatar.png'" mode="aspectFill"></image>
							<view class="user-details">
								<text class="username">{{ userName || '未登录' }}</text>
								<view class="membership-badge" :class="membershipClass">
									<text class="badge-text">{{ membershipText }}</text>
								</view>
							</view>
						</view>
						<view v-if="membership.type !== 'FREE'" class="expire-info">
							<text class="expire-label">到期时间</text>
							<text class="expire-date">{{ formatDate(membership.expireTime) }}</text>
						</view>
					</view>
					
					<!-- 会员特权标签 -->
					<view class="privilege-tags">
						<view class="tag-item" v-for="(privilege, index) in currentPrivileges" :key="index">
							<text class="tag-icon">{{ privilege.icon }}</text>
							<text class="tag-text">{{ privilege.text }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 配额信息 -->
		<view class="quota-section">
			<view class="section-title">
				<text class="title-text">今日配额</text>
				<text class="refresh-btn" @click="refreshQuota">刷新</text>
			</view>
			<view class="quota-cards">
				<view class="quota-card">
					<text class="quota-label">剩余次数</text>
					<text class="quota-value">{{ displayQuota }}</text>
					<text class="quota-total" v-if="membership.type !== 'GOLD'">/ {{ displayTotal }}</text>
				</view>
				<view class="quota-card" v-if="membership.type !== 'GOLD'">
					<text class="quota-label">已使用</text>
					<text class="quota-value used">{{ quota.usedQuota }}</text>
				</view>
				<view class="quota-card" v-if="membership.type !== 'GOLD'">
					<text class="quota-label">广告奖励</text>
					<text class="quota-value reward">{{ quota.adBonusQuota }}</text>
				</view>
			</view>
			
			<!-- 获取更多配额按钮 - 仅免费用户显示 -->
			<view class="quota-actions" v-if="membership.type === 'FREE'">
				<button class="action-btn ad-btn" @click="watchAd" :disabled="adLoading">
					<text class="btn-icon">📺</text>
					<text class="btn-text">{{ adLoading ? '加载中...' : '看广告+5次' }}</text>
				</button>
				<button class="action-btn upgrade-btn" @click="goToPurchase">
					<text class="btn-icon">⭐</text>
					<text class="btn-text">升级会员</text>
				</button>
			</view>
		</view>
		
		<!-- 会员套餐 -->
		<view class="membership-plans" v-if="membership.type === 'FREE'">
			<view class="section-title">
				<text class="title-text">会员套餐</text>
			</view>
			<view class="plans-container">
				<view 
					class="plan-card" 
					v-for="plan in membershipPlans" 
					:key="plan.packageType"
					:class="{ 'recommended': plan.recommended }"
					@click="selectPlan(plan)"
				>
					<view v-if="plan.recommended" class="recommend-badge">推荐</view>
					<view class="plan-header">
						<text class="plan-name">{{ plan.name }}</text>
						<view class="plan-price-box">
							<view class="original-price">
								<text class="price-label">原价:</text>
								<text class="price-original">¥{{ plan.originalPrice }}</text>
							</view>
							<view class="current-price">
								<text class="price-value">¥{{ plan.currentPrice }}</text>
								<text class="price-unit">/{{ plan.unit }}</text>
							</view>
						</view>
					</view>
					<view class="plan-features">
						<view class="feature-item" v-for="(feature, index) in plan.features" :key="index">
							<text class="feature-icon">✓</text>
							<text class="feature-text">{{ feature }}</text>
						</view>
					</view>
					<button class="plan-btn">立即开通</button>
				</view>
			</view>
		</view>
		
		<!-- 我的订单 -->
		<view class="order-section">
			<view class="section-title">
				<text class="title-text">我的订单</text>
				<text class="more-btn" @click="viewAllOrders">查看全部 ></text>
			</view>
			<view v-if="orders.length > 0" class="order-list">
				<view class="order-item" v-for="order in orders.slice(0, 3)" :key="order.id" @click="viewOrderDetail(order)">
					<view class="order-info">
						<text class="order-title">{{ order.membershipName }}</text>
						<text class="order-status" :class="getOrderStatusClass(order.status)">{{ getOrderStatusText(order.status) }}</text>
					</view>
					<view class="order-detail">
						<text class="order-amount">¥{{ order.actualAmount }}</text>
						<text class="order-time">{{ formatOrderTime(order.createTime) }}</text>
					</view>
				</view>
			</view>
			<view v-else class="empty-state">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无订单</text>
			</view>
		</view>
	</view>
</template>

<script>
import http from '@/utils/http.js';

export default {
	data() {
		return {
			userAvatar: '',
			userName: '',
			membership: {
				type: 'FREE',
				expireTime: null
			},
			quota: {
				totalQuota: 5,
				usedQuota: 0,
				remainingQuota: 5,
				adBonusQuota: 0
			},
			membershipPlans: [
				{
					packageType: 'WEEK',
					name: '周卡会员',
					originalPrice: 5.9,
					currentPrice: 2.9,
					unit: '周',
					features: ['无限次检测', '所有检测功能', '无广告干扰', '优先客服'],
					recommended: false
				},
				{
					packageType: 'MONTH',
					name: '月卡会员',
					originalPrice: 19.9,
					currentPrice: 9.9,
					unit: '月',
					features: ['无限次检测', '所有检测功能', '无广告干扰', '优先客服'],
					recommended: true
				}
			],
			orders: [],
			adLoading: false
		};
	},
	computed: {
		membershipClass() {
			return `membership-${this.membership.type.toLowerCase()}`;
		},
		membershipText() {
			const typeMap = {
				'FREE': '免费会员',
				'GOLD': '黄金会员'
			};
			return typeMap[this.membership.type] || '免费会员';
		},
		currentPrivileges() {
			const privilegeMap = {
				'FREE': [
					{ icon: '✓', text: '每日5次' },
					{ icon: '✓', text: '基础功能' }
				],
				'GOLD': [
					{ icon: '✓', text: '无限次检测' },
					{ icon: '✓', text: '所有功能' },
					{ icon: '✓', text: '无广告' }
				]
			};
			return privilegeMap[this.membership.type] || privilegeMap['FREE'];
		},
		// 显示配额：黄金会员显示"无限"
		displayQuota() {
			return this.membership.type === 'GOLD' ? '∞' : this.quota.remainingQuota;
		},
		// 显示总配额：黄金会员显示"无限"
		displayTotal() {
			return this.membership.type === 'GOLD' ? '∞' : this.quota.totalQuota;
		}
	},
	onLoad() {
		this.loadUserInfo();
		this.loadMembershipInfo();
		this.loadQuotaInfo();
		this.loadOrders();
	},
	onShow() {
		// 从购买页面返回时刷新数据
		this.refreshData();
	},
	methods: {
		async loadUserInfo() {
			try {
				const userInfo = uni.getStorageSync('userInfo');
				if (userInfo) {
					this.userAvatar = userInfo.avatarUrl;
					this.userName = userInfo.nickName;
				}
			} catch (e) {
				console.error('加载用户信息失败', e);
			}
		},
		
		async loadMembershipInfo() {
			try {
				const res = await http.get('/quota/membership/info');
				if (res.code === 200) {
					this.membership = res.data;
				}
			} catch (e) {
				console.error('加载会员信息失败', e);
			}
		},
		
		async loadQuotaInfo() {
			try {
				const res = await http.get('/quota/quota/info');
				if (res.code === 200) {
					this.quota = res.data;
				}
			} catch (e) {
				console.error('加载配额信息失败', e);
			}
		},
		
		async loadOrders() {
			try {
				const res = await http.get('/quota/order/list', {
					pageNum: 1,
					pageSize: 5
				});
				if (res.code === 200) {
					this.orders = res.data.rows || [];
				}
			} catch (e) {
				console.error('加载订单失败', e);
			}
		},
		
		async refreshData() {
			await Promise.all([
				this.loadMembershipInfo(),
				this.loadQuotaInfo(),
				this.loadOrders()
			]);
		},
		
		async refreshQuota() {
			uni.showLoading({ title: '刷新中...' });
			await this.loadQuotaInfo();
			uni.hideLoading();
			uni.showToast({ title: '刷新成功', icon: 'success' });
		},
		
		async watchAd() {
			if (this.adLoading) return;
			
			this.adLoading = true;
			
			// TODO: 接入真实广告SDK
			// 这里模拟广告播放流程
			uni.showLoading({ title: '加载广告...' });
			
			setTimeout(async () => {
				uni.hideLoading();
				
				// 模拟广告播放完成
				try {
					const res = await http.post('/quota/quota/ad-reward');
					if (res.code === 200) {
						uni.showToast({ title: '获得5次检测机会', icon: 'success' });
						await this.loadQuotaInfo();
					} else {
						uni.showToast({ title: res.msg || '获取奖励失败', icon: 'none' });
					}
				} catch (e) {
					uni.showToast({ title: '网络错误', icon: 'none' });
				} finally {
					this.adLoading = false;
				}
			}, 2000);
		},
		
		goToPurchase() {
			uni.navigateTo({
				url: '/pages/membership/purchase'
			});
		},
		
		selectPlan(plan) {
			if (this.membership.type === 'GOLD') {
				uni.showToast({ title: '已是黄金会员', icon: 'none' });
				return;
			}
			
			uni.navigateTo({
				url: `/pages/membership/purchase?packageType=${plan.packageType}`
			});
		},
		
		viewAllOrders() {
			uni.navigateTo({
				url: '/pages/order/list'
			});
		},
		
		viewOrderDetail(order) {
			uni.navigateTo({
				url: `/pages/order/detail?orderNo=${order.orderNo}`
			});
		},
		
		formatDate(dateStr) {
			if (!dateStr) return '永久有效';
			const date = new Date(dateStr);
			const year = date.getFullYear();
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			return `${year}-${month}-${day}`;
		},
		
		formatOrderTime(dateStr) {
			const date = new Date(dateStr);
			const month = String(date.getMonth() + 1).padStart(2, '0');
			const day = String(date.getDate()).padStart(2, '0');
			const hour = String(date.getHours()).padStart(2, '0');
			const minute = String(date.getMinutes()).padStart(2, '0');
			return `${month}-${day} ${hour}:${minute}`;
		},
		
		getOrderStatusText(status) {
			const statusMap = {
				'PENDING': '待支付',
				'PAID': '已支付',
				'CANCELLED': '已取消',
				'REFUNDED': '已退款',
				'EXPIRED': '已过期'
			};
			return statusMap[status] || '未知';
		},
		
		getOrderStatusClass(status) {
			return `status-${status.toLowerCase()}`;
		}
	}
};
</script>

<style lang="scss" scoped>
.membership-center {
	min-height: 100vh;
	background: #f5f5f5;
}

/* 顶部背景 */
.header-bg {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	padding: 40rpx 30rpx 60rpx;
}

.membership-card {
	background: rgba(255, 255, 255, 0.95);
	border-radius: 24rpx;
	padding: 30rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 30rpx;
}

.user-info {
	display: flex;
	align-items: center;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.user-details {
	display: flex;
	flex-direction: column;
}

.username {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 10rpx;
}

.membership-badge {
	display: inline-flex;
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	align-self: flex-start;
}

.membership-free {
	background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
}

.membership-gold {
	background: linear-gradient(135deg, #ffd89b 0%, #f6a621 100%);
}

.badge-text {
	font-size: 24rpx;
	color: #fff;
	font-weight: 500;
}

.expire-info {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
}

.expire-label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 8rpx;
}

.expire-date {
	font-size: 28rpx;
	color: #666;
	font-weight: 500;
}

.privilege-tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.tag-item {
	display: flex;
	align-items: center;
	padding: 12rpx 20rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
}

.tag-icon {
	font-size: 24rpx;
	margin-right: 8rpx;
	color: #667eea;
}

.tag-text {
	font-size: 24rpx;
	color: #666;
}

/* 配额信息 */
.quota-section {
	margin: 30rpx;
	padding: 30rpx;
	background: #fff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.section-title {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
}

.title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.refresh-btn, .more-btn {
	font-size: 26rpx;
	color: #667eea;
}

.quota-cards {
	display: flex;
	gap: 20rpx;
	margin-bottom: 30rpx;
}

.quota-card {
	flex: 1;
	padding: 24rpx;
	background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
	border-radius: 16rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	border: 2rpx solid #e8e8e8;
}

.quota-label {
	font-size: 24rpx;
	color: #999;
	margin-bottom: 12rpx;
}

.quota-value {
	font-size: 48rpx;
	font-weight: 700;
	color: #667eea;
}

.quota-value.used {
	color: #ff6b6b;
}

.quota-value.reward {
	color: #51cf66;
}

.quota-total {
	font-size: 28rpx;
	color: #999;
}

.quota-actions {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	flex: 1;
	height: 88rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
	font-weight: 500;
	border: none;
}

.ad-btn {
	background: linear-gradient(135deg, #51cf66 0%, #37b24d 100%);
	color: #fff;
}

.upgrade-btn {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
}

.btn-icon {
	margin-right: 8rpx;
	font-size: 32rpx;
}

/* 会员套餐 */
.membership-plans {
	margin: 30rpx;
}

.plans-container {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.plan-card {
	position: relative;
	background: #fff;
	border-radius: 24rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	border: 2rpx solid transparent;
}

.plan-card.recommended {
	border-color: #667eea;
}

.recommend-badge {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	font-size: 24rpx;
}

.plan-header {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	margin-bottom: 24rpx;
}

.plan-name {
	font-size: 36rpx;
	font-weight: 600;
	color: #333;
}

.plan-price-box {
	display: flex;
	flex-direction: column;
	align-items: flex-end;
}

.original-price {
	display: flex;
	align-items: center;
	margin-bottom: 4rpx;
}

.price-label {
	font-size: 22rpx;
	color: #999;
	margin-right: 4rpx;
}

.price-original {
	font-size: 24rpx;
	color: #999;
	text-decoration: line-through;
}

.current-price {
	display: flex;
	align-items: baseline;
}

.price-value {
	font-size: 48rpx;
	font-weight: 700;
	color: #667eea;
}

.price-unit {
	font-size: 24rpx;
	color: #999;
	margin-left: 4rpx;
}

.plan-features {
	margin-bottom: 24rpx;
}

.feature-item {
	display: flex;
	align-items: center;
	padding: 12rpx 0;
}

.feature-icon {
	color: #51cf66;
	margin-right: 12rpx;
	font-weight: 600;
}

.feature-text {
	font-size: 28rpx;
	color: #666;
}

.plan-btn {
	width: 100%;
	height: 80rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	border-radius: 16rpx;
	font-size: 28rpx;
	font-weight: 500;
	border: none;
}

/* 订单 */
.order-section {
	margin: 30rpx;
	padding: 30rpx;
	background: #fff;
	border-radius: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.order-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.order-item {
	padding: 24rpx;
	background: #f8f9fa;
	border-radius: 16rpx;
	border-left: 6rpx solid #667eea;
}

.order-info {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.order-title {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.order-status {
	font-size: 24rpx;
	padding: 6rpx 16rpx;
	border-radius: 12rpx;
}

.status-pending {
	background: #fff3cd;
	color: #856404;
}

.status-paid {
	background: #d4edda;
	color: #155724;
}

.status-cancelled, .status-expired {
	background: #f8d7da;
	color: #721c24;
}

.status-refunded {
	background: #d1ecf1;
	color: #0c5460;
}

.order-detail {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.order-amount {
	font-size: 32rpx;
	color: #667eea;
	font-weight: 600;
}

.order-time {
	font-size: 24rpx;
	color: #999;
}

/* 空状态 */
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 60rpx 0;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	opacity: 0.5;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}
</style>
