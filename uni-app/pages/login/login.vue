<template>
	<view class="login-container">
		<view class="login-content">
			<!-- Logo和标题 -->
			<view class="header">
				<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
				<text class="title">AI内容检测平台</text>
				<text class="subtitle">智能识别AI生成内容</text>
			</view>

			<!-- 登录按钮 -->
			<view class="login-section">
				<button 
					class="login-btn" 
					type="primary" 
					@click="handleWechatLogin"
					:loading="loading"
					:disabled="loading">
					<text v-if="!loading">微信授权登录</text>
					<text v-else>登录中...</text>
				</button>
				
				<view class="tips">
					<text class="tips-text">登录即表示同意</text>
					<text class="tips-link" @click="showProtocol">《用户协议》</text>
					<text class="tips-text">和</text>
					<text class="tips-link" @click="showPrivacy">《隐私政策》</text>
				</view>
			</view>

			<!-- 功能介绍 -->
			<view class="features">
				<view class="feature-item">
					<text class="feature-icon">🖼️</text>
					<text class="feature-text">图像检测</text>
				</view>
				<view class="feature-item">
					<text class="feature-icon">📝</text>
					<text class="feature-text">文本检测</text>
				</view>
				<view class="feature-item">
					<text class="feature-icon">🎬</text>
					<text class="feature-text">视频检测</text>
				</view>
				<view class="feature-item">
					<text class="feature-icon">🎵</text>
					<text class="feature-text">音频检测</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { login, saveUserInfo, autoLogin } from '@/utils/auth.js';

export default {
	data() {
		return {
			loading: false,
			agreementConfirmed: false, // 协议确认状态
			fromAgreementPage: '' // 记录从哪个协议页面返回
		};
	},
	
	onLoad() {
		// 尝试自动登录
		this.tryAutoLogin();
		
		// 检查是否已确认过协议
		this.checkAgreementStatus();
		
		// 监听协议同意事件
		this.setupAgreementListeners();
	},
	
	onUnload() {
		// 移除事件监听
		uni.$off('userAgreementAgreed');
		uni.$off('privacyPolicyAgreed');
	},
	
	methods: {
		/**
		 * 检查协议确认状态
		 */
		checkAgreementStatus() {
			const agreed = uni.getStorageSync('agreement_confirmed');
			this.agreementConfirmed = !!agreed;
		},
		
		/**
		 * 设置协议同意事件监听
		 */
		setupAgreementListeners() {
			// 监听用户协议同意事件
			uni.$on('userAgreementAgreed', () => {
				console.log('用户已同意用户协议');
				this.fromAgreementPage = 'user';
				this.checkBothAgreementsConfirmed();
			});
			
			// 监听隐私政策同意事件
			uni.$on('privacyPolicyAgreed', () => {
				console.log('用户已同意隐私政策');
				this.fromAgreementPage = 'privacy';
				this.checkBothAgreementsConfirmed();
			});
		},
		
		/**
		 * 检查是否两个协议都已确认
		 */
		checkBothAgreementsConfirmed() {
			// 如果是从协议页面返回，且之前没有确认过协议，则显示另一个协议
			if (!this.agreementConfirmed) {
				if (this.fromAgreementPage === 'user') {
					// 已同意用户协议，跳转到隐私政策
					uni.navigateTo({
						url: '/pages/agreement/privacy-policy?fromLogin=true'
					});
				} else if (this.fromAgreementPage === 'privacy') {
					// 已同意隐私政策，跳转到用户协议
					uni.navigateTo({
						url: '/pages/agreement/user-agreement?fromLogin=true'
					});
				}
				
				// 标记协议已确认
				this.agreementConfirmed = true;
				uni.setStorageSync('agreement_confirmed', true);
			}
		},
		/**
		 * 尝试自动登录
		 */
		async tryAutoLogin() {
			try {
				const success = await autoLogin();
				if (success) {
					// 自动登录成功，跳转到首页
					uni.reLaunch({
						url: '/pages/index/index'
					});
				}
			} catch (error) {
				console.log('自动登录失败:', error);
			}
		},
		
		/**
		 * 微信登录处理
		 */
		async handleWechatLogin() {
			try {
				this.loading = true;
				
				// 1. 调用wx.login获取code
				const loginRes = await this.wxLogin();
				const code = loginRes.code;
				
				if (!code) {
					throw new Error('获取登录凭证失败');
				}
				
				// 2. 尝试获取用户信息授权（可选，失败不影响登录）
				let userProfile = null;
				try {
					userProfile = await this.getUserProfile();
				} catch (profileError) {
					console.log('用户未授权获取信息，使用默认信息登录:', profileError);
					// 使用默认用户信息
					userProfile = {
						nickName: '微信用户',
						avatarUrl: ''
					};
				}
				
				// 3. 收集设备信息
				const deviceInfo = await this.getDeviceInfo();
				
				// 4. 调用后端登录接口
				const loginResult = await login(code, userProfile, deviceInfo);
				
				// 5. 保存用户信息
				saveUserInfo(loginResult.token, loginResult.user);
				
				// 6. 提示登录成功
				uni.showToast({
					title: loginResult.isNewUser ? '注册成功' : '登录成功',
					icon: 'success',
					duration: 1500
				});
				
				// 7. 延迟跳转到首页
				setTimeout(() => {
					uni.reLaunch({
						url: '/pages/index/index'
					});
				}, 1500);
				
			} catch (error) {
				console.error('登录失败:', error);
				uni.showToast({
					title: error.message || '登录失败，请重试',
					icon: 'none',
					duration: 2000
				});
			} finally {
				this.loading = false;
			}
		},
		
		/**
		 * 显示协议确认对话框
		 */
		showAgreementConfirmDialog() {
			uni.showModal({
				title: '服务协议和隐私政策',
				content: '欢迎使用AI内容检测平台！\n\n在使用我们的服务前，请您仔细阅读并同意《用户服务协议》和《隐私政策》。我们将严格保护您的个人信息安全。',
				confirmText: '同意',
				cancelText: '取消',
				success: (res) => {
					if (res.confirm) {
						// 用户点击同意，跳转到用户协议页面
						uni.navigateTo({
							url: '/pages/agreement/user-agreement?fromLogin=true'
						});
					}
				}
			});
		},
		
		/**
		 * 获取设备信息
		 */
		getDeviceInfo() {
			return new Promise((resolve) => {
				uni.getSystemInfo({
					success: (res) => {
						resolve({
							deviceModel: res.model || '',
							deviceBrand: res.brand || '',
							systemVersion: res.system || '',
							platform: res.platform || '',
							screenWidth: res.screenWidth || 0,
							screenHeight: res.screenHeight || 0,
							pixelRatio: res.pixelRatio || 1,
							language: res.language || 'zh_CN',
							wifiEnabled: res.wifiEnabled || false,
							locationEnabled: res.locationEnabled || false,
							bluetoothEnabled: res.bluetoothEnabled || false,
							cameraAuthorized: res.cameraAuthorized || false,
							albumAuthorized: res.albumAuthorized || false,
							microphoneAuthorized: res.microphoneAuthorized || false,
							notificationAuthorized: res.notificationAuthorized || false
						});
					},
					fail: () => {
						// 获取失败时返回空对象
						resolve({});
					}
				});
			});
		},
		
		/**
		 * 调用wx.login
		 */
		wxLogin() {
			return new Promise((resolve, reject) => {
				uni.login({
					provider: 'weixin',
					success: (res) => {
						resolve(res);
					},
					fail: (err) => {
						reject(err);
					}
				});
			});
		},
		
		/**
		 * 获取用户信息
		 */
		getUserProfile() {
			return new Promise((resolve, reject) => {
				uni.getUserProfile({
					desc: '用于完善会员资料',
					success: (res) => {
						resolve(res.userInfo);
					},
					fail: (err) => {
						reject(new Error('获取用户信息失败'));
					}
				});
			});
		},
		
		/**
		 * 显示用户协议
		 */
		showProtocol() {
			uni.navigateTo({
				url: '/pages/agreement/user-agreement'
			});
		},
		
		/**
		 * 显示隐私政策
		 */
		showPrivacy() {
			uni.navigateTo({
				url: '/pages/agreement/privacy-policy'
			});
		}
	}
};
</script>

<style scoped>
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
}

.login-content {
	width: 100%;
	max-width: 600rpx;
}

/* 头部样式 */
.header {
	text-align: center;
	margin-bottom: 100rpx;
}

.logo {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 40rpx;
}

.title {
	display: block;
	font-size: 48rpx;
	font-weight: bold;
	color: #FFFFFF;
	margin-bottom: 20rpx;
}

.subtitle {
	display: block;
	font-size: 28rpx;
	color: rgba(255, 255, 255, 0.8);
}

/* 登录区域 */
.login-section {
	margin-bottom: 80rpx;
}

.login-btn {
	width: 100%;
	height: 90rpx;
	line-height: 90rpx;
	background: #FFFFFF;
	color: #667eea;
	border-radius: 45rpx;
	font-size: 32rpx;
	font-weight: bold;
	box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.1);
}

.login-btn::after {
	border: none;
}

.tips {
	text-align: center;
	margin-top: 30rpx;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.8);
}

.tips-text {
	color: rgba(255, 255, 255, 0.8);
}

.tips-link {
	color: #FFFFFF;
	text-decoration: underline;
	margin: 0 5rpx;
}

/* 功能介绍 */
.features {
	display: flex;
	justify-content: space-around;
	padding: 40rpx 0;
	background: rgba(255, 255, 255, 0.1);
	border-radius: 20rpx;
	backdrop-filter: blur(10rpx);
}

.feature-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.feature-icon {
	font-size: 48rpx;
	margin-bottom: 10rpx;
}

.feature-text {
	font-size: 24rpx;
	color: #FFFFFF;
}
</style>
