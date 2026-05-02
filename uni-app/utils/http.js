import config from '@/config/config.js'

/**
 * HTTP请求封装
 * 统一处理后端API调用、token认证、错误处理
 */
class Http {
  constructor() {
    // 从配置文件读取后端服务器地址
    this.baseUrl = config.baseUrl
    this.timeout = config.timeout
  }

  /**
   * 通用请求方法
   */
  request(options) {
    return new Promise((resolve, reject) => {
      // 获取token
      const token = uni.getStorageSync('token')
      
      // 针对微信登录接口使用表单格式，其他接口使用JSON格式
      let contentType = 'application/json'
      let requestData = options.data || {}
      
      if (options.url && options.url.includes('/wechat/login') && options.method === 'POST') {
        contentType = 'application/x-www-form-urlencoded'
        // 将data对象转换为URL编码格式: key1=value1&key2=value2
        if (requestData && typeof requestData === 'object') {
          requestData = Object.keys(requestData)
            .filter(key => requestData[key] !== undefined && requestData[key] !== null)
            .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(requestData[key])}`)
            .join('&')
        }
      }
      
      // 构建请求头
      const header = {
        'Content-Type': contentType,
        ...options.header
      }
      
      // 添加token认证
      if (token) {
        header['Authorization'] = `Bearer ${token}`
      }
      
      // 添加平台标识
      header['X-Platform'] = 'miniapp'
      
      uni.request({
        url: this.baseUrl + options.url,
        method: options.method || 'GET',
        data: requestData,
        header: header,
        timeout: this.timeout,
        success: (res) => {
          // 请求成功
          if (res.statusCode === 200) {
            const data = res.data
            
            // 业务成功
            if (data.code === 200) {
              resolve(data)
            } 
            // token过期或无效(仅在非登录接口时处理)
            else if (data.code === 401 && !options.url.includes('/wechat/login')) {
              this.handleTokenExpired()
              reject(new Error('请先登录'))
            } 
            // 其他业务错误
            else {
              reject(new Error(data.msg || '请求失败'))
            }
          } 
          // HTTP状态码错误
          else {
            reject(new Error(`服务器错误(${res.statusCode})`))
          }
        },
        fail: (error) => {
          // 网络错误
          console.error('请求失败:', error)
          
          let errorMsg = '网络请求失败'
          if (error.errMsg) {
            if (error.errMsg.includes('timeout')) {
              errorMsg = '请求超时，请稍后重试'
            } else if (error.errMsg.includes('fail')) {
              errorMsg = '网络连接失败，请检查网络'
            }
          }
          
          reject(new Error(errorMsg))
        }
      })
    })
  }

  /**
   * GET请求
   */
  get(url, data) {
    return this.request({
      url,
      method: 'GET',
      data
    })
  }

  /**
   * POST请求
   */
  post(url, data) {
    return this.request({
      url,
      method: 'POST',
      data
    })
  }

  /**
   * PUT请求
   */
  put(url, data) {
    return this.request({
      url,
      method: 'PUT',
      data
    })
  }

  /**
   * DELETE请求
   */
  delete(url, data) {
    return this.request({
      url,
      method: 'DELETE',
      data
    })
  }

  /**
   * 文件上传
   */
  upload(url, filePath, formData = {}) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      
      uni.uploadFile({
        url: this.baseUrl + url,
        filePath: filePath,
        name: 'file',
        formData: formData,
        header: {
          'Authorization': token ? `Bearer ${token}` : '',
          'X-Platform': 'miniapp'
        },
        timeout: config.uploadTimeout, // 从配置文件读取上传超时时间
        success: (res) => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data)
              if (data.code === 200) {
                resolve(data)
              } else if (data.code === 401) {
                this.handleTokenExpired()
                reject(new Error('请先登录'))
              } else {
                reject(new Error(data.msg || '上传失败'))
              }
            } catch (e) {
              reject(new Error('响应数据解析失败'))
            }
          } else {
            reject(new Error(`上传失败(${res.statusCode})`))
          }
        },
        fail: (error) => {
          console.error('上传失败:', error)
          
          let errorMsg = '上传失败'
          if (error.errMsg) {
            if (error.errMsg.includes('timeout')) {
              errorMsg = '上传超时，请稍后重试'
            } else if (error.errMsg.includes('fail')) {
              errorMsg = '网络连接失败，请检查网络'
            }
          }
          
          reject(new Error(errorMsg))
        }
      })
    })
  }

  /**
   * 处理token过期
   */
  handleTokenExpired() {
    // 清除本地token
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    
    // 提示用户
    uni.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none',
      duration: 2000
    })
    
    // 延迟跳转到登录页
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/login'
      })
    }, 2000)
  }

  /**
   * 设置基础URL
   */
  setBaseUrl(url) {
    this.baseUrl = url
  }

  /**
   * 获取基础URL
   */
  getBaseUrl() {
    return this.baseUrl
  }
}

// 导出单例
export default new Http()
