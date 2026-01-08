import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
// 导入主题样式
import './assets/styles/theme.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

app.mount('#app')

// 延迟初始化WebSocket服务，确保所有store都已创建
setTimeout(async () => {
  try {
    // 动态导入WebSocket服务，避免循环依赖
    const { webSocketService } = await import('./services/websocket')
    console.log('初始化WebSocket服务...')
    await webSocketService.init()
    // 全局注入WebSocket服务
    app.provide('webSocketService', webSocketService)
  } catch (error) {
    console.error('WebSocket服务初始化失败:', error)
  }
}, 1000)

