<template>
  <div class="profile-container">
    <!-- 顶部个人信息卡片 -->
    <div class="profile-header">
      <div class="profile-cover">
        <div class="cover-overlay">
          <div class="avatar-section">
            <img
              :src="userStore.avatar"
              alt="用户头像"
              class="user-avatar"
            />
            <div class="user-info">
              <h1 class="username">{{ userStore.username }}</h1>
              <p class="user-id">UID: {{ userStore.userId }}</p>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ userStore.followingCount }}</span>
                  <span class="stat-label">关注</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ userStore.followersCount }}</span>
                  <span class="stat-label">粉丝</span>
                </div>
                <div class="stat-item">
                  <span class="stat-number">{{ userStore.level }}</span>
                  <span class="stat-label">等级</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容导航 -->
    <div class="content-nav">
      <div class="nav-tabs">
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'activities' }"
          @click="activeTab = 'activities'"
        >
          动态
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'collections' }"
          @click="activeTab = 'collections'"
        >
          收藏
        </button>
        <button
          class="tab-btn"
          :class="{ active: activeTab === 'history' }"
          @click="activeTab = 'history'"
        >
          历史记录
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="profile-content">
      <!-- 左侧边栏 -->
      <div class="sidebar-left">
        <Card class="info-card">
          <template #title>
            <h3>个人信息</h3>
          </template>
          <div class="info-content">
            <div class="info-item">
              <span class="info-label">简介：</span>
              <span class="info-value">{{ userStore.bio || '这个人很懒，什么都没有写~' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间：</span>
              <span class="info-value">{{ userStore.registerDate }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">在线状态：</span>
              <span class="info-value online">在线</span>
            </div>
          </div>
          <template #footer>
            <router-link to="/user-info" class="edit-info-btn">
              编辑资料
            </router-link>
          </template>
        </Card>

        <Card class="friends-card">
          <template #title>
            <h3>最近互动好友</h3>
          </template>
          <div class="friends-list">
            <div
              v-for="friend in recentFriends"
              :key="friend.id"
              class="friend-item"
              @click="goToChat(friend.id)"
            >
              <img :src="friend.avatar" alt="好友头像" class="friend-avatar" />
              <span class="friend-name">{{ friend.name }}</span>
              <span v-if="friend.unread > 0" class="unread-badge">{{ friend.unread }}</span>
            </div>
          </div>
          <template #footer>
            <router-link to="/friends" class="view-all-btn">
              查看全部
            </router-link>
          </template>
        </Card>
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 动态内容 -->
        <div v-if="activeTab === 'activities'" class="activities-content">
          <Card class="activity-card" v-for="activity in activities" :key="activity.id">
            <div class="activity-header">
              <div class="activity-info">
                <span class="activity-time">{{ activity.time }}</span>
                <span class="activity-type">{{ activity.type }}</span>
              </div>
              <h3 class="activity-title">{{ activity.title }}</h3>
            </div>
            <div class="activity-content">
              <p>{{ activity.content }}</p>
              <div v-if="activity.image" class="activity-image">
                <img :src="activity.image" alt="动态图片" />
              </div>
            </div>
            <div class="activity-stats">
              <button class="stat-btn">
                <span class="icon">👍</span>
                <span>{{ activity.likes }}</span>
              </button>
              <button class="stat-btn">
                <span class="icon">💬</span>
                <span>{{ activity.comments }}</span>
              </button>
              <button class="stat-btn">
                <span class="icon">🔄</span>
                <span>{{ activity.shares }}</span>
              </button>
            </div>
          </Card>
        </div>

        <!-- 收藏内容 -->
        <div v-if="activeTab === 'collections'" class="collections-content">
          <Card class="collection-card" v-for="item in collections" :key="item.id">
            <div class="collection-item">
              <img :src="item.cover" alt="收藏封面" class="collection-cover" />
              <div class="collection-info">
                <h3 class="collection-title">{{ item.title }}</h3>
                <p class="collection-desc">{{ item.description }}</p>
                <div class="collection-stats">
                  <span>{{ item.views }} 播放</span>
                  <span>{{ item.duration }}</span>
                </div>
              </div>
            </div>
          </Card>
        </div>

        <!-- 历史记录 -->
        <div v-if="activeTab === 'history'" class="history-content">
          <Card class="history-card" v-for="item in history" :key="item.id">
            <div class="history-item">
              <img :src="item.cover" alt="历史封面" class="history-cover" />
              <div class="history-info">
                <h3 class="history-title">{{ item.title }}</h3>
                <p class="history-desc">{{ item.description }}</p>
                <div class="history-time">
                  最近观看：{{ item.time }}
                </div>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useChatStore } from '../stores/chat'
import Card from '../components/Card.vue'

export default {
  name: 'UserProfile',
  components: {
    Card
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const chatStore = useChatStore()
    const activeTab = ref('activities')

    // 模拟数据
    const activities = ref([
      {
        id: 1,
        time: '2小时前',
        type: '动态',
        title: '分享了一个视频',
        content: '这个视频真的太棒了，大家快来看看吧！',
        image: 'https://picsum.photos/400/225?random=1',
        likes: 42,
        comments: 8,
        shares: 3
      },
      {
        id: 2,
        time: '昨天',
        type: '评论',
        title: '评论了一个视频',
        content: 'UP主辛苦了，内容质量很高！',
        likes: 15,
        comments: 2,
        shares: 0
      },
      {
        id: 3,
        time: '3天前',
        type: '收藏',
        title: '收藏了一个视频',
        content: '《零基础入门Vue 3》系列教程',
        image: 'https://picsum.photos/400/225?random=2',
        likes: 0,
        comments: 0,
        shares: 0
      }
    ])

    const collections = ref([
      {
        id: 1,
        title: 'Vue 3 高级特性详解',
        description: '深入讲解Vue 3的Composition API、响应式原理等',
        cover: 'https://picsum.photos/200/112?random=3',
        views: '12.5万',
        duration: '45:32'
      },
      {
        id: 2,
        title: '前端性能优化实战',
        description: '从网络请求到渲染优化的全方位指南',
        cover: 'https://picsum.photos/200/112?random=4',
        views: '8.2万',
        duration: '38:15'
      },
      {
        id: 3,
        title: 'CSS Grid 布局完全指南',
        description: '掌握现代Web布局技术',
        cover: 'https://picsum.photos/200/112?random=5',
        views: '5.7万',
        duration: '28:45'
      }
    ])

    const history = ref([
      {
        id: 1,
        title: 'TypeScript 实战教程',
        description: '从基础到高级，全面掌握TypeScript',
        cover: 'https://picsum.photos/200/112?random=6',
        time: '今天 14:30'
      },
      {
        id: 2,
        title: 'React Hooks 深入理解',
        description: '剖析React Hooks的实现原理和最佳实践',
        cover: 'https://picsum.photos/200/112?random=7',
        time: '昨天 20:15'
      },
      {
        id: 3,
        title: 'Node.js 服务端开发',
        description: '使用Node.js构建高性能Web应用',
        cover: 'https://picsum.photos/200/112?random=8',
        time: '2天前 16:45'
      }
    ])

    // 计算最近互动的好友
    const recentFriends = ref([])

    const goToChat = (userId) => {
      router.push(`/chat/${userId}`)
    }

    onMounted(() => {
      // 确保用户信息已加载
      if (!userStore.isLoggedIn) {
        userStore.loadUserInfo()
      }

      // 从聊天store获取最近互动的好友
      if (chatStore.friends.length > 0) {
        recentFriends.value = chatStore.friends.slice(0, 3)
      }
    })

    return {
      userStore,
      activeTab,
      activities,
      collections,
      history,
      recentFriends,
      goToChat
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background-color: var(--background-color);
}

/* 顶部个人信息卡片 */
.profile-header {
  position: relative;
  height: 300px;
  margin-bottom: 20px;
}

.profile-cover {
  height: 100%;
  background: linear-gradient(135deg, var(--primary-color), #ff4d7a);
  position: relative;
  overflow: hidden;
}

.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0,0,0,0.7), transparent);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
}

.user-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid white;
  object-fit: cover;
}

.user-info h1 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: 700;
}

.user-id {
  margin: 0 0 15px 0;
  opacity: 0.9;
}

.user-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-number {
  font-size: 20px;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

/* 内容导航 */
.content-nav {
  background-color: white;
  padding: 0 20px;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-tabs {
  display: flex;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
}

.tab-btn {
  padding: 15px 0;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
  position: relative;
  transition: color 0.3s;
}

.tab-btn:hover {
  color: var(--primary-color);
}

.tab-btn.active {
  color: var(--primary-color);
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background-color: var(--primary-color);
  border-radius: 3px;
}

/* 内容区域 */
.profile-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 左侧边栏 */
.sidebar-left {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card .info-content {
  padding: 15px;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
}

.info-label {
  font-weight: 600;
  margin-right: 10px;
  color: var(--text-primary);
  min-width: 70px;
}

.info-value {
  color: var(--text-secondary);
  flex: 1;
}

.info-value.online {
  color: #52c41a;
}

.edit-info-btn {
  display: block;
  width: 100%;
  padding: 8px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--border-radius);
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: background-color 0.3s;
}

.edit-info-btn:hover {
  background-color: #e85c87;
}

.friends-list {
  padding: 15px;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  position: relative;
}

.friend-item:last-child {
  border-bottom: none;
}

.friend-item:hover {
  background-color: var(--hover-background);
}

.friend-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.friend-name {
  flex: 1;
  font-size: 14px;
}

.unread-badge {
  background-color: var(--danger-color);
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.view-all-btn {
  display: block;
  width: 100%;
  padding: 8px;
  background-color: transparent;
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  border-radius: var(--border-radius);
  text-align: center;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.3s;
}

.view-all-btn:hover {
  background-color: var(--primary-color);
  color: white;
}

/* 主内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 动态内容 */
.activity-card {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.activity-info {
  display: flex;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 14px;
}

.activity-type {
  background-color: var(--primary-color-light);
  color: var(--primary-color);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.activity-title {
  margin: 5px 0 10px 0;
  font-size: 18px;
}

.activity-content {
  margin-bottom: 15px;
}

.activity-image img {
  max-width: 100%;
  border-radius: var(--border-radius);
  margin-top: 10px;
}

.activity-stats {
  display: flex;
  gap: 20px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.stat-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.stat-btn:hover {
  color: var(--primary-color);
}

.stat-btn .icon {
  font-size: 16px;
}

/* 收藏内容 */
.collection-item, .history-item {
  display: flex;
  gap: 15px;
  cursor: pointer;
  transition: transform 0.2s;
}

.collection-item:hover, .history-item:hover {
  transform: translateX(5px);
}

.collection-cover, .history-cover {
  width: 120px;
  height: 68px;
  border-radius: var(--border-radius);
  object-fit: cover;
  flex-shrink: 0;
}

.collection-info, .history-info {
  flex: 1;
  min-width: 0;
}

.collection-title, .history-title {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collection-desc, .history-desc {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.collection-stats {
  display: flex;
  gap: 15px;
  font-size: 12px;
  color: var(--text-secondary);
}

.history-time {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
  }

  .sidebar-left {
    display: none;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
    padding-bottom: 20px;
  }

  .user-avatar {
    width: 80px;
    height: 80px;
  }

  .user-info h1 {
    font-size: 20px;
  }

  .user-stats {
    gap: 20px;
  }
}
</style>
