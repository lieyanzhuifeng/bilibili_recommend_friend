<template>
  <nav class="navbar">
    <div class="navbar-container">
      <div class="navbar-logo">
        <router-link to="/profile" class="logo-link">
          <span class="logo-text">哔哩哔哩</span>
        </router-link>
      </div>

      <div class="navbar-menu">
        <router-link
          to="/profile"
          class="nav-item"
          :class="{ active: currentRoute === '/profile' }"
        >
          <i class="nav-icon">👤</i>
          <span>个人主页</span>
        </router-link>

        <router-link
          to="/friends"
          class="nav-item"
          :class="{ active: currentRoute === '/friends' }"
        >
          <i class="nav-icon">👥</i>
          <span>好友</span>
          <span v-if="totalUnreadCount > 0" class="badge">{{ totalUnreadCount }}</span>
        </router-link>

        <router-link
          to="/friend-management"
          class="nav-item"
          :class="{ active: currentRoute === '/friend-management' }"
        >
          <i class="nav-icon">🔄</i>
          <span>好友管理</span>
        </router-link>

        <router-link
          to="/user-info"
          class="nav-item"
          :class="{ active: currentRoute === '/user-info' }"
        >
          <i class="nav-icon">📝</i>
          <span>个人信息</span>
        </router-link>

        <router-link
          to="/recommendations"
          class="nav-item"
          :class="{ active: currentRoute === '/recommendations' }"
        >
          <i class="nav-icon">🔍</i>
          <span>推荐</span>
        </router-link>


      </div>

      <div class="navbar-user">
        <div class="user-info">
          <img
            :src="userStore.avatar"
            alt="用户头像"
            class="user-avatar"
          />
          <span class="user-name">{{ userStore.username }}</span>
        </div>
        <button @click="handleLogout" class="logout-btn">
          退出登录
        </button>
      </div>
    </div>
  </nav>
</template>

<script>
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'

export default {
  name: 'Navbar',
  setup() {
    const userStore = useUserStore()
    const chatStore = useChatStore()

    return {
      userStore,
      chatStore
    }
  },
  computed: {
    currentRoute() {
      return this.$route.path
    },
    totalUnreadCount() {
      return this.chatStore.totalUnreadCount
    }
  },
  mounted() {
    // 确保加载用户信息
    if (!this.userStore.isLoggedIn) {
      this.userStore.loadUserInfo()
    }

    // 如果没有登录，重定向到登录页
    if (!this.userStore.isLoggedIn) {
      this.$router.push('/')
    }
  },
  methods: {
    handleLogout() {
      this.userStore.logout()
      this.$router.push('/')
    }
  }
}
</script>

<style scoped>
/* 在原有样式基础上添加 */
.badge {
  position: absolute;
  top: -5px;
  right: -10px;
  background-color: var(--danger-color);
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.nav-item {
  position: relative;
}
</style>

<style scoped>
.navbar {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  height: 60px;
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.navbar-logo {
  display: flex;
  align-items: center;
}

.logo-link {
  text-decoration: none;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
  color: #FB7299;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 30px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  text-decoration: none;
  color: #666;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav-item:hover {
  color: #FB7299;
  background: #FFF5F5;
}

.nav-item.active {
  color: #FB7299;
  background: #FFF0F0;
  font-weight: 500;
}

.nav-icon {
  font-size: 18px;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #FB7299;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.logout-btn {
  padding: 6px 16px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.3s;
}

.logout-btn:hover {
  background: #ff7875;
}

@media (max-width: 768px) {
  .navbar-menu {
    gap: 15px;
  }

  .nav-item span {
    display: none;
  }

  .user-name {
    display: none;
  }
}
</style>
