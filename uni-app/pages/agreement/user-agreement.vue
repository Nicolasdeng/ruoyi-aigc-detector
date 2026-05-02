<template>
	<view class="agreement-container">
		<view class="header">
			<text class="title">用户服务协议</text>
			<text class="update-time">更新日期：{{ agreementData.updateTime }}</text>
		</view>
		
		<scroll-view class="content" scroll-y>
			<view class="section" v-for="(section, index) in agreementData.sections" :key="index">
				<text class="section-title">{{ section.title }}</text>
				<text class="section-content" v-html="section.content"></text>
			</view>
		</scroll-view>
		
		<view class="footer" v-if="showAgree">
			<button class="btn-agree" @click="handleAgree">同意并继续</button>
		</view>
	</view>
</template>

<script>
import { get, post } from '@/utils/request.js'

export default {
	data() {
		return {
			showAgree: false,
			agreementData: {
				updateTime: '',
				sections: []
			}
		}
	},
	onLoad(options) {
		// 判断是否从登录页进入（需要显示同意按钮）
		this.showAgree = options.from === 'login'
		
		// 加载协议内容
		this.loadAgreement()
	},
	methods: {
		/**
		 * 加载用户协议
		 */
		async loadAgreement() {
			// 如果是从登录页进入（需要确认协议），直接使用默认协议，不调用需要认证的API
			if (this.showAgree) {
				this.loadDefaultAgreement()
				return
			}
			
			// 其他情况（如从设置页查看协议）才调用API加载最新协议
			try {
				const res = await get('/api/agreement/user/latest', {
					type: 'USER_AGREEMENT'
				}, {
					showLoading: true,
					showError: false
				})
				
				if (res.code === 200 && res.data) {
					this.agreementData = {
						updateTime: res.data.updateTime,
						sections: this.parseContent(res.data.content)
					}
				}
			} catch (error) {
				console.error('加载用户协议失败:', error)
				// 使用默认协议内容
				this.loadDefaultAgreement()
			}
		},
		
		/**
		 * 解析协议内容
		 */
		parseContent(content) {
			// 如果后端返回的是JSON格式
			if (typeof content === 'object') {
				return content
			}
			
			// 如果是纯文本，按段落分割
			const sections = []
			const paragraphs = content.split('\n\n')
			
			paragraphs.forEach((paragraph, index) => {
				if (paragraph.trim()) {
					sections.push({
						title: `第${index + 1}条`,
						content: paragraph.trim()
					})
				}
			})
			
			return sections
		},
		
		/**
		 * 加载默认协议内容
		 */
		loadDefaultAgreement() {
			this.agreementData = {
				updateTime: '2026年1月11日',
				sections: [
					{
						title: '一、协议的接受',
						content: '欢迎使用AI检测小程序。在使用本服务前，请您仔细阅读并充分理解本协议的全部内容。您使用本服务即表示您已阅读、理解并同意接受本协议的全部内容。'
					},
					{
						title: '二、服务说明',
						content: 'AI检测小程序是一款提供AI内容检测服务的应用程序，包括文本检测、图片检测、视频检测、音频检测和论文检测等功能。我们致力于为用户提供准确、可靠的AI内容识别服务。'
					},
					{
						title: '三、用户账号',
						content: '1. 您使用微信账号授权登录本小程序，我们将获取您的微信头像和昵称用于账号识别。\n2. 您应对账号和密码的安全负责，因您保管不善可能导致的任何损失应由您自行承担。\n3. 您不得将账号转让、出借或以其他方式提供给他人使用。'
					},
					{
						title: '四、用户行为规范',
						content: '1. 您在使用本服务时，应遵守中华人民共和国相关法律法规。\n2. 不得利用本服务从事违法犯罪活动。\n3. 不得上传、发布违法、虚假、侵权或不良信息。\n4. 不得干扰或破坏本服务的正常运行。'
					},
					{
						title: '五、知识产权',
						content: '1. 本服务的所有内容，包括但不限于文本、图片、软件、标识等，其知识产权归本公司所有。\n2. 未经本公司书面许可，您不得复制、传播、展示、修改本服务的任何内容。\n3. 您上传的内容，您仍保留其知识产权，但授予本公司使用该内容以提供服务的权利。'
					},
					{
						title: '六、隐私保护',
						content: '我们重视您的隐私保护，具体内容请参见《隐私政策》。我们将采取合理措施保护您的个人信息安全。'
					},
					{
						title: '七、免责声明',
						content: '1. 本服务提供的检测结果仅供参考，不构成任何法律意见或专业建议。\n2. 因不可抗力、网络故障等原因导致的服务中断或数据丢失，本公司不承担责任。\n3. 因您违反本协议导致的任何损失，由您自行承担。'
					},
					{
						title: '八、协议的变更',
						content: '本公司有权根据需要修改本协议，修改后的协议将在小程序内公布。您继续使用本服务即表示接受修改后的协议。'
					},
					{
						title: '九、其他',
						content: '1. 本协议的解释、效力及纠纷的解决，适用中华人民共和国法律。\n2. 若本协议的任何条款被认定为无效，不影响其他条款的效力。\n3. 如您对本协议有任何疑问，请联系我们的客服。'
					}
				]
			}
		},
		
		/**
		 * 同意协议
		 */
		handleAgree() {
			uni.navigateBack()
			
			// 通知登录页用户已同意协议
			uni.$emit('agreementAccepted', {
				type: 'USER_AGREEMENT',
				accepted: true
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.agreement-container {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background-color: #f8f8f8;
}

.header {
	background-color: #fff;
	padding: 40rpx 30rpx 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	
	.title {
		display: block;
		font-size: 40rpx;
		font-weight: bold;
		color: #333;
		margin-bottom: 20rpx;
	}
	
	.update-time {
		display: block;
		font-size: 24rpx;
		color: #999;
	}
}

.content {
	flex: 1;
	padding: 30rpx;
	
	.section {
		background-color: #fff;
		border-radius: 16rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
		
		.section-title {
			display: block;
			font-size: 32rpx;
			font-weight: bold;
			color: #333;
			margin-bottom: 20rpx;
		}
		
		.section-content {
			display: block;
			font-size: 28rpx;
			color: #666;
			line-height: 1.8;
			white-space: pre-wrap;
		}
	}
}

.footer {
	background-color: #fff;
	padding: 30rpx;
	box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
	
	.btn-agree {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #fff;
		font-size: 32rpx;
		border-radius: 44rpx;
		border: none;
		
		&:active {
			opacity: 0.8;
		}
	}
}
</style>
