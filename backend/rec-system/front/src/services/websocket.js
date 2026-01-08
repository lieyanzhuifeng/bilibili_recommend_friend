// 注意：由于Vite的ES模块兼容性问题，我们使用原生WebSocket API实现
class WebSocketService {
  constructor() {
    this.ws = null
    this.isConnected = false
    this.userId = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectDelay = 3000 // 3秒后重连
    this.callbacks = {
      onConnect: [],
      onDisconnect: [],
      onMessage: []
    }
  }

  // 初始化WebSocket连接
  async init() {
    try {
      // 动态导入userStore，避免循环依赖
      const { useUserStore } = await import('../stores/user')
      const userStore = useUserStore()
      this.userId = userStore.userId
      
      if (!this.userId) {
        console.error('WebSocket初始化失败：用户ID不存在')
        return false
      }

      // 创建WebSocket连接
      this.connect()
      return true
    } catch (err) {
      console.error('WebSocket初始化失败:', err)
      return false
    }
  }

  // 连接WebSocket服务器
  connect() {
    try {
      // 使用原生WebSocket API连接到服务器
      // 注意：这里假设后端支持直接WebSocket连接，而不需要STOMP协议
      this.ws = new WebSocket('ws://localhost:8080/ws')
      
      // 配置WebSocket事件处理
      this.ws.onopen = (event) => {
        console.log('WebSocket连接成功:', event)
        this.isConnected = true
        this.reconnectAttempts = 0
        
        // 触发连接成功回调
        this.callbacks.onConnect.forEach(callback => callback())
      }
      
      this.ws.onmessage = async (event) => {
        try {
          // 直接处理消息，假设消息格式为JSON
          const messageData = JSON.parse(event.data)
          console.log('收到实时消息:', messageData)
          
          // 触发消息接收回调
          this.callbacks.onMessage.forEach(callback => callback(messageData))
          
          // 动态导入并调用chatStore处理消息，避免循环依赖
          const { useChatStore } = await import('../stores/chat')
          const chatStore = useChatStore()
          chatStore.receiveMessage(messageData.senderId, messageData, true)
        } catch (err) {
          console.error('处理WebSocket消息失败:', err)
        }
      }
      
      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭:', event)
        this.isConnected = false
        
        // 触发断开连接回调
        this.callbacks.onDisconnect.forEach(callback => callback())
        
        // 尝试重连
        this.attemptReconnect()
      }
      
      this.ws.onerror = (event) => {
        console.error('WebSocket连接错误:', event)
      }
    } catch (err) {
      console.error('WebSocket连接失败:', err)
      this.attemptReconnect()
    }
  }

  // 发送消息
  sendMessage(message) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.error('WebSocket未连接，无法发送消息')
      return false
    }

    try {
      this.ws.send(JSON.stringify(message))
      return true
    } catch (err) {
      console.error('发送WebSocket消息失败:', err)
      return false
    }
  }

  // 断开连接
  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.isConnected = false
  }

  // 尝试重连
  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`尝试重连WebSocket... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectDelay)
    } else {
      console.error('WebSocket重连失败，已达到最大重试次数')
    }
  }

  // 注册回调
  on(event, callback) {
    if (this.callbacks[event]) {
      this.callbacks[event].push(callback)
    }
  }

  // 移除回调
  off(event, callback) {
    if (this.callbacks[event]) {
      this.callbacks[event] = this.callbacks[event].filter(cb => cb !== callback)
    }
  }

  // 获取连接状态
  getConnectionStatus() {
    return this.isConnected
  }
}

// 导出单例实例
export const webSocketService = new WebSocketService()
