import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    isAuthenticated: false
  }),

  getters: {
    getUserInfo: (state) => state.userInfo,
    isLoggedIn: (state) => state.isAuthenticated,
    username: (state) => state.userInfo?.username || '',
    userId: (state) => state.userInfo?.userId || '',
    avatar: (state) => state.userInfo?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'
  },

  actions: {
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.isAuthenticated = true
      localStorage.setItem('user', JSON.stringify(userInfo))
    },

    // 从localStorage加载用户信息
    loadUserInfo() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const userInfo = JSON.parse(userStr)
          this.userInfo = userInfo
          this.isAuthenticated = true
        } catch (error) {
          console.error('Failed to parse user info from localStorage', error)
          this.logout()
        }
      }
    },

    // 更新用户信息
    updateUserInfo(updates) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...updates }
        localStorage.setItem('user', JSON.stringify(this.userInfo))
      }
    },

    // 登出
    logout() {
      this.userInfo = null
      this.isAuthenticated = false
      localStorage.removeItem('user')
    }
  }
})
