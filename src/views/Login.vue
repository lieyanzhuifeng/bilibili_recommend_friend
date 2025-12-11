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
            v-model="userId"
            type="text"
            placeholder="请输入用户ID"
            class="form-input"
            @keyup.enter="handleLogin"
          />
        </div>
        <button
          @click="handleLogin"
          class="login-btn"
          :disabled="!userId.trim() || loading"
        >
          {{ loading ? '登录中...' : '登录（无需密码）' }}
        </button>
      </div>
      <div class="login-tips">
        <p>提示：输入用户ID即可登录</p>
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '../stores/user'
import { loginApi } from '../services/api'

export default {
  name: 'Login',
  setup() {
    const userStore = useUserStore()

    return {
      userStore
    }
  },
  data() {
    return {
      userId: '',
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.userId.trim() || this.loading) {
        if (!this.userId.trim()) {
          alert('请输入用户ID');
        }
        return;
      }
      console.log('开始登录流程，用户ID:', this.userId)

      this.loading = true;
      try {
        // 调用真实登录API
        console.log('准备调用登录API')
        const response = await loginApi.login({ userId: this.userId })
        console.log('登录API返回数据:', response)

        // 根据API文档，响应数据包含success、data、message字段
        if (response && response.success && response.data) {
          // API调用成功，使用API返回的用户数据
          const userData = {
            ...response.data,
            avatar: response.data.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${response.data.username || this.userId}`,
            token: response.data.token || `mock-token-${Date.now()}`,
            userId: response.data.userId || this.userId,
            username: response.data.username || `用户${this.userId}`
          };

          console.log('使用用户数据:', userData)

          // 保存用户信息到store
          this.userStore.setUserInfo(userData)

          // 同时保存到localStorage
          localStorage.setItem('user', JSON.stringify(userData))
          localStorage.setItem('token', userData.token)

          console.log('用户信息已保存，准备跳转')
          // 跳转到个人主页
          this.$router.push('/profile');
        } else {
          throw new Error(response?.message || '登录失败，请检查用户ID是否正确');
        }
      } catch (error) {
        console.error('登录失败:', error)
        // 显示错误信息
        alert(`登录失败: ${error.message || '未知错误'}`)
      } finally {
        console.log('登录流程结束')
        this.loading = false;
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
