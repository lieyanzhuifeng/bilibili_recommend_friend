<template>
  <div class="login-container">
    <div class="login-box">
      <div class="logo">
        <h1>哔哩哔哩</h1>
        <p>年轻人的潮流文化社区</p>
      </div>
      <div class="login-form">
        <div class="form-item">
          <input 
            v-model="username" 
            type="text" 
            placeholder="请输入用户名"
            class="form-input"
            @keyup.enter="handleLogin"
          />
        </div>
        <button 
          @click="handleLogin" 
          class="login-btn"
          :disabled="!username.trim() || loading"
        >
          {{ loading ? '登录中...' : '登录（无需密码）' }}
        </button>
      </div>
      <div class="login-tips">
        <p>提示：输入任意用户名即可登录</p>
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'

export default {
  name: 'Login',
  setup() {
    const userStore = useUserStore()
    const chatStore = useChatStore()
    
    return {
      userStore,
      chatStore
    }
  },
  data() {
    return {
      username: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.username.trim() || this.loading) return
      
      this.loading = true
      
      try {
        // 模拟登录延迟
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 创建用户信息
        const userInfo = {
          username: this.username,
          userId: Date.now(),
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${this.username}`,
          createdAt: new Date().toISOString()
        }
        
        // 使用store管理用户状态
        this.userStore.setUserInfo(userInfo)
        
        // 初始化聊天模拟数据
        this.chatStore.initMockData()
        
        // 跳转到个人主页
        this.$router.push('/profile')
      } catch (error) {
        console.error('登录失败:', error)
        alert('登录失败，请重试')
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    // 检查用户是否已登录
    if (this.userStore.isLoggedIn) {
      this.$router.push('/profile')
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #ff6b6b, #556270);
  padding: 20px;
}

.login-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}

.logo h1 {
  color: #FB7299;
  font-size: 32px;
  margin: 0;
  font-weight: bold;
}

.logo p {
  color: #666;
  margin-top: 8px;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 20px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #FB7299;
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #FB7299;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #FF5288;
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.login-tips {
  text-align: center;
  color: #999;
  font-size: 12px;
}
</style>