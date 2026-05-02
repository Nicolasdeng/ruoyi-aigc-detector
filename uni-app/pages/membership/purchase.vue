<template>
	<view class="purchase-page">
		<!-- 套餐选择 -->
		<view class="plan-selection">
			<view class="section-title">选择套餐</view>
			<view class="plans-list">
				<view 
					class="plan-item" 
					v-for="plan in plans" 
					:key="plan.type"
					:class="{ 'active': selectedPlan === plan.type, 'recommended': plan.recommended }"
					@click="selectPlan(plan.type)"
				>
					<view v-if="plan.recommended" class="recommend-tag">推荐</view>
					<view class="plan-header">
						<text class="plan-name">{{ plan.name }}</text>
						<view class="plan-price-box">
							<view class="original-price">
								<text class="price-label">原价</text>
								<text class="price-original">¥{{ plan.originalPrice }}</text>
							</view>
							<view class="current-price">
								<text class="price-symbol">¥</text>
								<text class="price-value">{{ plan.currentPrice }}</text>
								<text class="price-unit">/{{ plan.duration }}</text>
							</view>
						</view>
					</view>
					<view class="plan-desc">{{ plan.description }}</view>
					<view class="plan-features">
						<view class="feature-row" v-for="(feature, index) in plan.features" :key="index">
							<text class="feature-icon">✓</text>
							<text class="feature-text">{{ feature }}</text>
						</view>
					</view>
					<view class="select-indicator" v-if="selectedPlan === plan.type">
						<text class="indicator-icon">✓</text>
					</view>
				</view>
			</view>
		</view>
		
		<!-- 价格明细 -->
		<view class="price-detail">
			<view class="section-title">价格明细</view>
			<view class="detail-list">
				<view class="detail-row">
					<text class="detail-label">原价</text>
					<text class="detail-value original">¥{{ originalPrice }}</text>
				</view>
				<view class="detail-row">
					<text class="detail-label">优惠金额</text>
					<text class="detail-value discount">-¥{{ saveAmount }}</text>
				</view>
				<view class="detail-row total">
					<text class="detail-label">实付金额</text>
					<text class="detail-value final">¥{{ currentPrice }}</text>
				</view>
			</view>
		</view>
		
		<!-- 购买协议 -->
		<view class="agreement-box">
			<checkbox-group @change="onAgreementChange">
				<label class="agreement-label">
					<checkbox :checked="agreedToTerms" color="#667eea" />
					<text class="agreement-text">我已阅读并同意</text>
					<text class="agreement-link" @click.stop="viewAgreement('user')">《用户协议》</text>
					<text class="agreement-text">和</text>
					<text class="agreement-link" @click.stop="viewAgreement('payment')">《支付协议》</text>
				</label>
			</checkbox-group>
		</view>
		
		<!-- 底部按钮 -->
		<view class="bottom-bar">
			<view class="price-summary">
				<text class="summary-label">实付</text>
				<text class="summary-price">¥{{ currentPrice }}</text>
			</view>
			<button class="pay-btn" @click="confirmPurchase" :disabled="!agreedToTerms || processing">
				{{ processing ? '处理中...' : '立即支付' }}
			</button>
		</view>
	</view>
</template>

<script>
import http from '@/utils/http.js';

export default {
	data() {
		return {
			selectedPlan: 'MONTH',
			plans: [
				{
					type: 'WEEK',
					name: '周卡套餐',
					originalPrice: 5.9,
					currentPrice: 2.9,
					duration: '7天',
					description: '短期体验首选',
					features: ['无限次数使用', '全部检测功能', '无广告体验', '在线客服支持'],
					recommended: false
				},
				{
					type: 'MONTH',
					name: '月卡套餐',
					originalPrice: 19.9,
					currentPrice: 9.9,
					duration: '30天',
					description: '性价比之选',
					features: ['无限次数使用', '全部检测功能', '无广告体验', '优先客服支持'],
					recommended: true
				}
			],
			agreedToTerms: false,
			processing: false
		};
	},
	computed: {
		selectedPlanInfo() {
			return this.plans.find(p => p.type === this.selectedPlan) || this.plans[0];
		},
		originalPrice() {
			return this.selectedPlanInfo.originalPrice.toFixed(2);
		},
		currentPrice() {
			return this.selectedPlanInfo.currentPrice.toFixed(2);
		},
		saveAmount() {
			return (this.selectedPlanInfo.originalPrice - this.selectedPlanInfo.currentPrice).toFixed(2);
		}
	},
	onLoad(options) {
		// 从URL参数获取预选套餐
		if (options.type) {
			this.selectedPlan = options.type;
		}
	},
	methods: {
		selectPlan(type) {
			this.selectedPlan = type;
		},
		
		onAgreementChange(e) {
			this.agreedToTerms = e.detail.value.length > 0;
		},
		
		viewAgreement(type) {
			const url = type === 'user' ? '/pages/agreement/user-agreement' : '/pages/agreement/payment-agreement';
			uni.navigateTo({ url });
		},
		
		async confirmPurchase() {
			if (!this.agreedToTerms) {
				uni.showToast({ title: '请先同意相关协议', icon: 'none' });
				return;
			}
			
			if (this.processing) return;
			
			this.processing = true;
			
			try {
				// 1. 创建订单
				const orderRes = await http.post('/quota/order/create', {
					packageType: this.selectedPlan
				});
				
				if (orderRes.code !== 200) {
					throw new Error(orderRes.msg || '创建订单失败');
				}
				
				const orderNo = orderRes.data.orderNo;
				
				// 2. 发起微信支付
				const payRes = await http.post('/quota/wechat-pay/create', {
					orderNo: orderNo
				});
				
				if (payRes.code !== 200) {
					throw new Error(payRes.msg || '发起支付失败');
				}
				
				// 3. 调用微信支付
				await this.requestWechatPayment(payRes.data);
				
				// 4. 支付成功，跳转到会员中心
				uni.redirectTo({
					url: '/pages/membership/center'
				});
				
			} catch (e) {
				console.error('购买失败', e);
				uni.showToast({
					title: e.message || '购买失败，请重试',
					icon: 'none'
				});
			} finally {
				this.processing = false;
			}
		},
		
		requestWechatPayment(paymentParams) {
			return new Promise((resolve, reject) => {
				uni.requestPayment({
					provider: 'wxpay',
					timeStamp: paymentParams.timeStamp,
					nonceStr: paymentParams.nonceStr,
					package: paymentParams.package,
					signType: paymentParams.signType,
					paySign: paymentParams.paySign,
					success: (res) => {
						uni.showToast({ title: '支付成功', icon: 'success' });
						resolve(res);
					},
					fail: (err) => {
						if (err.errMsg.includes('cancel')) {
							uni.showToast({ title: '已取消支付', icon: 'none' });
						} else {
							uni.showToast({ title: '支付失败', icon: 'none' });
						}
						reject(err);
					}
				});
			});
		}
	}
};
</script>

<style lang="scss" scoped>
.purchase-page {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 180rpx;
}

/* 套餐选择 */
.plan-selection {
	margin: 30rpx;
}

.section-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 24rpx;
}

.plans-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.plan-item {
	position: relative;
	background: #fff;
	border-radius: 24rpx;
	padding: 30rpx;
	border: 3rpx solid transparent;
	transition: all 0.3s;
}

.plan-item.active {
	border-color: #667eea;
	box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.2);
}

.plan-item.recommended {
	background: linear-gradient(135deg, #f8f9ff 0%, #fff 100%);
}

.recommend-tag {
	position: absolute;
	top: 0;
	right: 30rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	padding: 8rpx 24rpx;
	border-radius: 0 0 16rpx 16rpx;
	font-size: 24rpx;
}

.plan-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
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
	gap: 8rpx;
}

.original-price {
	display: flex;
	align-items: center;
	gap: 6rpx;
}

.price-label {
	font-size: 22rpx;
	color: #999;
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

.price-symbol {
	font-size: 28rpx;
	color: #667eea;
	font-weight: 500;
}

.price-value {
	font-size: 48rpx;
	font-weight: 700;
	color: #667eea;
	margin: 0 4rpx;
}

.price-unit {
	font-size: 24rpx;
	color: #999;
}

.plan-desc {
	font-size: 26rpx;
	color: #999;
	margin-bottom: 24rpx;
}

.plan-features {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.feature-row {
	display: flex;
	align-items: center;
}

.feature-icon {
	color: #51cf66;
	font-weight: 600;
	margin-right: 12rpx;
	font-size: 28rpx;
}

.feature-text {
	font-size: 28rpx;
	color: #666;
}

.select-indicator {
	position: absolute;
	top: 30rpx;
	right: 30rpx;
	width: 48rpx;
	height: 48rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
}

.indicator-icon {
	color: #fff;
	font-size: 32rpx;
	font-weight: 600;
}

/* 价格明细 */
.price-detail {
	margin: 0 30rpx 30rpx;
	padding: 30rpx;
	background: #fff;
	border-radius: 24rpx;
}

.detail-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.detail-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.detail-row.total {
	padding-top: 20rpx;
	border-top: 2rpx dashed #e8e8e8;
	margin-top: 10rpx;
}

.detail-label {
	font-size: 28rpx;
	color: #666;
}

.detail-value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.detail-value.original {
	text-decoration: line-through;
}

.detail-value.discount {
	color: #ff6b6b;
}

.detail-value.final {
	font-size: 36rpx;
	color: #667eea;
	font-weight: 700;
}

/* 购买协议 */
.agreement-box {
	margin: 0 30rpx 30rpx;
	padding: 30rpx;
	background: #fff;
	border-radius: 24rpx;
}

.agreement-label {
	display: flex;
	align-items: center;
}

.agreement-text {
	font-size: 26rpx;
	color: #666;
	margin: 0 4rpx;
}

.agreement-link {
	font-size: 26rpx;
	color: #667eea;
}

/* 底部按钮 */
.bottom-bar {
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	padding: 20rpx 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 -4rpx 12rpx rgba(0, 0, 0, 0.05);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.price-summary {
	flex: 1;
	display: flex;
	align-items: baseline;
}

.summary-label {
	font-size: 28rpx;
	color: #666;
	margin-right: 8rpx;
}

.summary-price {
	font-size: 48rpx;
	font-weight: 700;
	color: #ff6b6b;
}

.pay-btn {
	flex-shrink: 0;
	width: 300rpx;
	height: 88rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: #fff;
	border-radius: 44rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
	display: flex;
	align-items: center;
	justify-content: center;
}

.pay-btn[disabled] {
	opacity: 0.6;
}
</style>
