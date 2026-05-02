<template>
  <view class="privacy-policy-container">
    <view class="header">
      <text class="title">隐私政策</text>
      <text class="update-time">更新时间：{{ updateTime }}</text>
    </view>
    
    <scroll-view scroll-y class="content-scroll">
      <view class="policy-content">
        <rich-text :nodes="policyContent"></rich-text>
      </view>
    </scroll-view>
    
    <!-- 从登录页进入时显示同意按钮 -->
    <view v-if="fromLogin" class="footer-actions">
      <button class="agree-btn" @click="handleAgree">同意并继续</button>
    </view>
  </view>
</template>

<script>
import { get } from '@/utils/request'

export default {
  data() {
    return {
      policyContent: '',
      updateTime: '',
      fromLogin: false,
      defaultPolicy: `
        <h2>引言</h2>
        <p>欢迎使用我们的AI检测小程序。我们非常重视您的隐私保护和个人信息安全。本隐私政策将帮助您了解我们如何收集、使用、存储和保护您的个人信息。</p>
        
        <h2>一、我们收集的信息</h2>
        <h3>1.1 您主动提供的信息</h3>
        <p>• <strong>微信授权信息</strong>：当您首次登录时，我们会请求获取您的微信头像和昵称，用于创建您的账号并在小程序中显示。</p>
        <p>• <strong>检测内容</strong>：您上传或输入的文本、图片、视频、音频等内容，用于提供AI检测服务。</p>
        
        <h3>1.2 自动收集的信息</h3>
        <p>• <strong>设备信息</strong>：包括设备型号、操作系统版本、唯一设备标识符、IP地址等，用于改善服务质量和安全防护。</p>
        <p>• <strong>日志信息</strong>：包括登录时间、操作记录、访问页面等，用于分析用户行为和优化产品体验。</p>
        <p>• <strong>位置信息</strong>：在您明确授权的情况下，我们可能收集您的位置信息，用于提供基于地理位置的服务。</p>
        
        <h2>二、信息的使用</h2>
        <h3>2.1 服务提供</h3>
        <p>• 提供AI检测服务（文本、图片、视频、音频、论文检测）</p>
        <p>• 保存检测历史记录，便于您查看和管理</p>
        <p>• 生成个人统计数据和趋势分析</p>
        
        <h3>2.2 服务改进</h3>
        <p>• 分析用户使用习惯，优化产品功能和用户体验</p>
        <p>• 研发新功能和服务</p>
        <p>• 进行数据统计和分析</p>
        
        <h3>2.3 安全保障</h3>
        <p>• 识别和防范安全威胁、欺诈行为</p>
        <p>• 保护您和其他用户的合法权益</p>
        <p>• 履行法律法规要求的义务</p>
        
        <h2>三、信息的存储</h2>
        <h3>3.1 存储地点</h3>
        <p>您的个人信息将存储在中华人民共和国境内的服务器上。如需跨境传输，我们将严格遵守相关法律法规。</p>
        
        <h3>3.2 存储期限</h3>
        <p>• <strong>账号信息</strong>：在您使用服务期间持续保存，账号注销后将依法删除或匿名化处理</p>
        <p>• <strong>检测记录</strong>：默认保存180天，您可以随时删除</p>
        <p>• <strong>日志信息</strong>：保存不超过90天，用于安全分析和故障排查</p>
        
        <h2>四、信息的共享</h2>
        <p>我们不会向第三方出售、出租或交易您的个人信息。以下情况除外：</p>
        
        <h3>4.1 授权共享</h3>
        <p>• 经您明确同意的情况下，与第三方共享</p>
        
        <h3>4.2 法律要求</h3>
        <p>• 法律法规规定的情况</p>
        <p>• 司法机关或行政机关的要求</p>
        <p>• 保护国家安全、公共安全的需要</p>
        
        <h3>4.3 业务合作</h3>
        <p>• 为提供服务，我们可能与合作伙伴共享必要信息（如云存储服务提供商、AI模型服务商）</p>
        <p>• 我们会与合作伙伴签订严格的保密协议，要求其按照本政策及相关法律法规处理个人信息</p>
        
        <h2>五、您的权利</h2>
        <h3>5.1 访问权</h3>
        <p>您有权访问您的个人信息，除非法律法规另有规定。</p>
        
        <h3>5.2 更正权</h3>
        <p>当您发现个人信息有误时，您有权要求我们更正或补充。</p>
        
        <h3>5.3 删除权</h3>
        <p>以下情况下，您可以要求我们删除个人信息：</p>
        <p>• 处理目的已实现、无法实现或不再必要</p>
        <p>• 我们停止提供产品或服务，或保存期限已届满</p>
        <p>• 您撤回同意</p>
        <p>• 我们违法处理个人信息</p>
        
        <h3>5.4 撤回同意权</h3>
        <p>您有权撤回对个人信息处理的同意。撤回同意不影响撤回前的处理活动。</p>
        
        <h3>5.5 注销权</h3>
        <p>您可以随时申请注销账号。账号注销后，我们将停止提供服务，并依法删除或匿名化处理您的个人信息。</p>
        
        <h2>六、信息安全</h2>
        <h3>6.1 安全措施</h3>
        <p>• 采用行业标准的安全技术和管理措施保护您的个人信息</p>
        <p>• 使用加密技术确保数据传输和存储安全</p>
        <p>• 建立严格的数据访问权限控制和审计机制</p>
        <p>• 定期进行安全评估和漏洞扫描</p>
        
        <h3>6.2 安全事件处理</h3>
        <p>如发生个人信息安全事件，我们将：</p>
        <p>• 及时启动应急预案，阻止事件扩大</p>
        <p>• 按照法律法规要求，及时向您告知事件情况和我们采取的应对措施</p>
        <p>• 向有关主管部门报告</p>
        
        <h2>七、未成年人保护</h2>
        <p>我们非常重视未成年人的个人信息保护：</p>
        <p>• 如果您是未成年人，请在监护人的陪同下阅读本政策，并在监护人同意后使用我们的服务</p>
        <p>• 如果您是未成年人的监护人，当您对被监护人使用我们服务有疑问时，请联系我们</p>
        <p>• 如果我们发现在未获得监护人同意的情况下收集了未成年人的个人信息，将立即删除相关数据</p>
        
        <h2>八、隐私政策的更新</h2>
        <p>我们可能适时修订本隐私政策。修订后的政策将通过小程序公告、弹窗提示等方式通知您。</p>
        <p>如果修订导致您的权利实质减少，我们将在修订生效前通过显著方式提示您，并征得您的同意。</p>
        
        <h2>九、联系我们</h2>
        <p>如果您对本隐私政策有任何疑问、意见或建议，或需要行使上述权利，请通过以下方式联系我们：</p>
        <p>• 小程序内"我的-设置-隐私设置"</p>
        <p>• 客服邮箱：privacy@example.com</p>
        <p>• 客服电话：400-XXX-XXXX</p>
        <p>我们将在15个工作日内回复您的请求。</p>
        
        <h2>十、适用范围</h2>
        <p>本隐私政策适用于我们提供的所有服务。</p>
        <p>本隐私政策不适用于：</p>
        <p>• 第三方向您提供的服务</p>
        <p>• 其他第三方收集的信息</p>
        
        <p style="margin-top: 40px; text-align: center; color: #999;">本隐私政策最终解释权归本小程序所有</p>
      `
    }
  },
  
  onLoad(options) {
    // 判断是否从登录页进入
    this.fromLogin = options.from === 'login'
    this.loadPrivacyPolicy()
  },
  
  methods: {
    // 加载隐私政策
    async loadPrivacyPolicy() {
      try {
        const res = await get('/system/agreement/latest', {
          type: 'PRIVACY_POLICY'
        })
        
        if (res.code === 200 && res.data) {
          this.updateTime = res.data.updateTime || this.getCurrentDate()
          
          // 尝试解析内容
          try {
            const contentObj = JSON.parse(res.data.content)
            this.policyContent = this.formatContent(contentObj)
          } catch (e) {
            // 如果不是JSON格式，直接使用
            this.policyContent = res.data.content
          }
        } else {
          // 如果加载失败，使用默认协议
          this.useDefaultPolicy()
        }
      } catch (error) {
        console.error('加载隐私政策失败：', error)
        this.useDefaultPolicy()
      }
    },
    
    // 使用默认隐私政策
    useDefaultPolicy() {
      this.policyContent = this.defaultPolicy
      this.updateTime = this.getCurrentDate()
    },
    
    // 格式化内容
    formatContent(contentObj) {
      if (typeof contentObj === 'string') {
        return contentObj
      }
      
      // 如果是对象，转换为HTML格式
      let html = ''
      if (contentObj.sections && Array.isArray(contentObj.sections)) {
        contentObj.sections.forEach(section => {
          html += `<h2>${section.title}</h2>`
          if (section.content) {
            html += `<p>${section.content}</p>`
          }
          if (section.items && Array.isArray(section.items)) {
            section.items.forEach(item => {
              html += `<p>• ${item}</p>`
            })
          }
        })
      }
      return html || this.defaultPolicy
    },
    
    // 获取当前日期
    getCurrentDate() {
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      return `${year}年${month}月${day}日`
    },
    
    // 同意隐私政策
    handleAgree() {
      // 通知登录页用户已同意隐私政策
      uni.$emit('privacyPolicyAgreed')
      
      // 返回登录页
      uni.navigateBack({
        delta: 1
      })
    }
  }
}
</script>

<style scoped>
.privacy-policy-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.update-time {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.content-scroll {
  flex: 1;
  padding: 30rpx;
}

.policy-content {
  background-color: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  line-height: 1.8;
}

/* rich-text内部样式 */
.policy-content >>> h2 {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin: 40rpx 0 20rpx 0;
}

.policy-content >>> h2:first-child {
  margin-top: 0;
}

.policy-content >>> h3 {
  font-size: 28rpx;
  font-weight: bold;
  color: #666;
  margin: 30rpx 0 15rpx 0;
}

.policy-content >>> p {
  font-size: 28rpx;
  color: #666;
  margin: 15rpx 0;
  text-align: justify;
}

.policy-content >>> strong {
  color: #333;
  font-weight: bold;
}

.footer-actions {
  background-color: #fff;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #eee;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.agree-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 44rpx;
  font-size: 32rpx;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
}

.agree-btn::after {
  border: none;
}
</style>
