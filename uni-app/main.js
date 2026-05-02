// main.js
import App from './App'
import http from '@/utils/http.js'
import config from '@/config/config.js'

// 将http工具挂载到全局
uni.$http = http
uni.$config = config

// #ifndef VUE3
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  
  // 将http工具和配置挂载到全局属性
  app.config.globalProperties.$http = http
  app.config.globalProperties.$config = config
  
  return {
    app
  }
}
// #endif
