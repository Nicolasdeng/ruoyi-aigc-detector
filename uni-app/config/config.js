/**
 * 应用配置文件
 * 集中管理后端API地址、应用设置等配置项
 */

// 开发环境配置
const development = {
  // 后端API基础地址
  baseUrl: 'http://localhost:8080',
  
  // API超时时间（毫秒）
  timeout: 30000,
  
  // 文件上传超时时间（毫秒）
  uploadTimeout: 60000,
  
  // 是否启用调试模式
  debug: true
}

// 生产环境配置
const production = {
  // 后端API基础地址 - 部署时需要修改为实际服务器地址
  baseUrl: 'https://your-api-domain.com',
  
  timeout: 30000,
  
  uploadTimeout: 60000,
  
  debug: false
}

// 根据环境变量选择配置
// 可以通过 manifest.json 中的配置来切换环境
const config = process.env.NODE_ENV === 'production' ? production : development

export default config
