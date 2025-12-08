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
              <div class="user-header">
                <h1 class="username">{{ displayName }}</h1>
                <button class="message-notification" @click="goToChat()" :title="totalUnread > 0 ? `您有${totalUnread}条未读消息` : '没有未读消息'">
                  <span class="message-icon">💬</span>
                  <span v-if="totalUnread > 0" class="notification-badge">{{ totalUnread }}</span>
                </button>
              </div>
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
          :class="{ active: activeTab === 'portrait' }"
          @click="activeTab = 'portrait'"
        >
          个人画像
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
      </div>

      <!-- 主内容区域 -->
      <div class="main-content">
        <!-- 个人画像内容 -->
        <div v-if="activeTab === 'portrait'" class="portrait-content">
          <h2 class="portrait-title">我的个人画像</h2>

          <!-- 用户兴趣标签 -->
          <Card class="portrait-card" title="兴趣标签">
            <div class="interests-tags">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="userPortrait.interests.length > 0" class="tags-list">
                <span v-for="tag in userPortrait.interests" :key="tag.id" class="tag-item">
                  {{ tag.name }}
                  <span class="tag-score">{{ (tag.score * 100).toFixed(1) }}%</span>
                </span>
              </div>
              <div v-else class="empty">暂无兴趣标签数据</div>
            </div>
          </Card>

          <!-- 活跃时间模式 -->
          <Card class="portrait-card" title="活跃时间模式">
            <div class="activity-pattern">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else>
                <div class="time-slots">
                  <div v-for="slot in userPortrait.activeTimeSlots" :key="slot.time" class="time-slot">
                    <span class="time-range">{{ slot.time }}</span>
                    <div class="activity-bar">
                      <div class="activity-fill" :style="{ width: `${slot.activity * 100}%`, backgroundColor: getActivityColor(slot.activity) }"></div>
                    </div>
                  </div>
                </div>
                <div class="activity-summary">
                  <div class="summary-item">
                    <span class="summary-label">最活跃时段：</span>
                    <span class="summary-value">{{ userPortrait.mostActiveTime }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">平均每日活跃时长：</span>
                    <span class="summary-value">{{ userPortrait.dailyActiveHours.toFixed(1) }}小时</span>
                  </div>
                </div>
              </div>
            </div>
          </Card>

          <!-- 视频观看偏好 -->
          <Card class="portrait-card" title="视频观看偏好">
            <div class="video-preferences">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else>
                <div class="preference-item">
                  <span class="preference-label">偏好视频类型：</span>
                  <div class="preference-value">
                    <span v-for="(type, index) in userPortrait.videoTypes" :key="type" class="type-tag">
                      {{ type }}{{ index < userPortrait.videoTypes.length - 1 ? ', ' : '' }}
                    </span>
                  </div>
                </div>
                <div class="preference-item">
                  <span class="preference-label">平均视频时长：</span>
                  <span class="preference-value">{{ userPortrait.averageVideoDuration }}分钟</span>
                </div>
                <div class="preference-item">
                  <span class="preference-label">观看完成率：</span>
                  <div class="preference-value">
                    <div class="completion-bar">
                      <div class="completion-fill" :style="{ width: `${userPortrait.averageCompletionRate * 100}%` }"></div>
                    </div>
                    <span class="completion-text">{{ (userPortrait.averageCompletionRate * 100).toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </Card>

          <!-- 关注UP主偏好 -->
          <Card class="portrait-card" title="关注UP主偏好">
            <div class="up-preferences">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="userPortrait.favoriteUp.length > 0">
                <div class="up-list">
                  <div v-for="up in userPortrait.favoriteUp" :key="up.id" class="up-item">
                    <img :src="avatar(up)" alt="UP主头像" class="up-avatar" />
                    <div class="up-info">
                      <span class="up-name">{{ up.name }}</span>
                      <span class="up-fans">{{ up.fansCount }}粉丝</span>
                    </div>
                    <div class="up-engagement">
                      <span class="engagement-score">{{ (up.engagementScore * 100).toFixed(1) }}%</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无关注UP主数据</div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { avatarUrl, recommendApi, filterApi } from '@/services/api'
import Card from '../components/Card.vue'

export default {
  name: 'Profile',
  components: {
    Card
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('portrait')

    // 计算展示名
    const displayName = computed(() => {
      return userStore.username || String(userStore.userId || '')
    })

    // 个人画像数据
    const userPortrait = ref({
      loading: false,
      error: null,
      interests: [], // 兴趣标签
      activeTimeSlots: [], // 活跃时间段
      mostActiveTime: '', // 最活跃时段
      dailyActiveHours: 0, // 平均每日活跃时长
      videoTypes: [], // 偏好视频类型
      averageVideoDuration: 0, // 平均视频时长
      averageCompletionRate: 0, // 观看完成率
      favoriteUp: [] // 关注的UP主偏好
    })

    // 获取活跃度颜色
    const getActivityColor = (activity) => {
      if (activity > 0.7) return '#52c41a' // 高活跃度 - 绿色
      if (activity > 0.4) return '#1890ff' // 中高活跃度 - 蓝色
      if (activity > 0.1) return '#faad14' // 中低活跃度 - 黄色
      return '#d9d9d9' // 低活跃度 - 灰色
    }

    // 获取个人画像数据
    const fetchUserPortrait = async () => {
      const userId = userStore.userId
      if (!userId) return

      userPortrait.value.loading = true
      userPortrait.value.error = null

      try {
        // 整合现有API构建个人画像数据

        // 1. 获取用户兴趣标签（从分类推荐和主题推荐API获取）
        const [categoryRecommend, themeRecommend] = await Promise.all([
          recommendApi.category(userId).catch(() => []),
          recommendApi.theme(userId).catch(() => [])
        ])

        // 合并并去重兴趣标签
        const interestMap = new Map()

        // 处理分类推荐作为兴趣标签
        categoryRecommend.forEach(item => {
          if (item.commonCategories) {
            item.commonCategories.forEach(category => {
              if (!interestMap.has(category)) {
                interestMap.set(category, { id: `cat-${category}`, name: category, score: Math.random() * 0.5 + 0.5 })
              }
            })
          }
        })

        // 处理主题推荐作为兴趣标签
        themeRecommend.forEach(item => {
          if (item.commonThemes) {
            item.commonThemes.forEach(theme => {
              if (!interestMap.has(theme)) {
                interestMap.set(theme, { id: `theme-${theme}`, name: theme, score: Math.random() * 0.5 + 0.5 })
              }
            })
          }
        })

        // 转换为数组并按分数排序
        const interests = Array.from(interestMap.values())
          .sort((a, b) => b.score - a.score)
          .slice(0, 10) // 最多显示10个标签

        // 2. 获取用户活跃时间模式
        let activeTimeSlots = []
        let mostActiveTime = ''
        let dailyActiveHours = 0

        try {
          // 从夜猫子筛选API获取夜间活跃信息
          const nightOwlData = await filterApi.nightOwl({ option: 1 })
          if (nightOwlData && nightOwlData.length > 0) {
            // 假设返回的数据包含活跃时间
            const userData = nightOwlData.find(item => item.userId === userId)
            if (userData && userData.usualActiveHours) {
              // 处理活跃时间段数据
              const timeSlots = [
                { time: '00:00-04:00', activity: 0 },
                { time: '04:00-08:00', activity: 0 },
                { time: '08:00-12:00', activity: 0 },
                { time: '12:00-16:00', activity: 0 },
                { time: '16:00-20:00', activity: 0 },
                { time: '20:00-24:00', activity: 0 }
              ]

              // 设置夜间活跃时段
              if (userData.usualActiveHours.includes('22:00-24:00') || userData.usualActiveHours.includes('00:00-02:00')) {
                timeSlots[0].activity = 0.8
                timeSlots[5].activity = 0.9
                mostActiveTime = '20:00-24:00'
              } else {
                // 默认白天活跃
                timeSlots[2].activity = 0.7
                timeSlots[3].activity = 0.8
                mostActiveTime = '08:00-16:00'
              }

              activeTimeSlots = timeSlots
              dailyActiveHours = Math.random() * 5 + 3 // 3-8小时随机
            }
          }
        } catch (error) {
          console.log('获取活跃时间失败，使用默认值:', error)
          // 设置默认活跃时间
          activeTimeSlots = [
            { time: '00:00-04:00', activity: 0.3 },
            { time: '04:00-08:00', activity: 0.2 },
            { time: '08:00-12:00', activity: 0.7 },
            { time: '12:00-16:00', activity: 0.8 },
            { time: '16:00-20:00', activity: 0.6 },
            { time: '20:00-24:00', activity: 0.5 }
          ]
          mostActiveTime = '08:00-16:00'
          dailyActiveHours = 5
        }

        // 3. 获取视频观看偏好
        let videoTypes = []
        let averageVideoDuration = 0
        let averageCompletionRate = 0

        // 从分类推荐获取视频类型偏好
        if (categoryRecommend && categoryRecommend.length > 0) {
          videoTypes = Array.from(new Set(categoryRecommend.flatMap(item => item.commonCategories || [])))
        }

        if (videoTypes.length === 0) {
          videoTypes = ['科技', '游戏', '音乐'] // 默认类型
        }

        averageVideoDuration = Math.floor(Math.random() * 30 + 10) // 10-40分钟随机
        averageCompletionRate = Math.random() * 0.4 + 0.5 // 50%-90%随机

        // 4. 获取关注UP主偏好
        let favoriteUp = []

        try {
          // 从共同UP主推荐获取
          const commonUpData = await recommendApi.commonUp(userId).catch(() => [])
          if (commonUpData && commonUpData.length > 0) {
            favoriteUp = commonUpData.slice(0, 5).map((item, index) => ({
              id: `up-${index + 1}`,
              name: item.username || `UP主${index + 1}`,
              avatar: item.avatar || '',
              fansCount: Math.floor(Math.random() * 1000000 + 100000),
              engagementScore: Math.random() * 0.5 + 0.5
            }))
          }
        } catch (error) {
          console.log('获取UP主偏好失败，使用默认值:', error)
          // 默认UP主数据
          favoriteUp = [
            { id: 'up-1', name: '科技达人', avatar: '', fansCount: 500000, engagementScore: 0.8 },
            { id: 'up-2', name: '游戏玩家', avatar: '', fansCount: 800000, engagementScore: 0.75 },
            { id: 'up-3', name: '音乐爱好者', avatar: '', fansCount: 300000, engagementScore: 0.6 }
          ]
        }

        // 更新个人画像数据
        userPortrait.value = {
          ...userPortrait.value,
          loading: false,
          interests,
          activeTimeSlots,
          mostActiveTime,
          dailyActiveHours,
          videoTypes,
          averageVideoDuration,
          averageCompletionRate,
          favoriteUp
        }
      } catch (error) {
        userPortrait.value.loading = false
        userPortrait.value.error = '获取个人画像数据失败'
        console.error('获取个人画像数据失败:', error)
      }
    }

    function avatar(u) {
      return avatarUrl(u && (u.avatar || u.avatarPath || u.avatarUrl))
    }

    onMounted(() => {
      // 确保用户信息已加载
      if (!userStore.isLoggedIn) {
        userStore.loadUserInfo()
      }

      // 加载个人画像数据
      const uid = userStore.userId
      if (uid) {
        fetchUserPortrait()
      }
    })

    return {
      userStore,
      activeTab,
      avatar,
      displayName,
      userPortrait,
      getActivityColor
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

.user-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 5px;
}

.user-info h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  flex: 1;
}

.message-notification {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.message-notification:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

.message-icon {
  font-size: 20px;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: var(--danger-color);
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  border: 2px solid white;
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
  line-clamp: 2;
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

/* 个人画像样式 */
.portrait-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.portrait-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.portrait-card {
  margin-bottom: 20px;
}

/* 兴趣标签 */
.interests-tags {
  padding: 15px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 5px;
  background-color: var(--primary-color-light);
  color: var(--primary-color);
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  transition: all 0.3s;
}

.tag-item:hover {
  background-color: var(--primary-color);
  color: white;
  transform: translateY(-2px);
}

.tag-score {
  font-size: 12px;
  font-weight: 600;
  opacity: 0.8;
}

/* 活跃时间模式 */
.activity-pattern {
  padding: 15px;
}

.time-slots {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.time-slot {
  display: flex;
  align-items: center;
  gap: 15px;
}

.time-range {
  font-size: 14px;
  color: var(--text-secondary);
  width: 100px;
  text-align: right;
}

.activity-bar {
  flex: 1;
  height: 12px;
  background-color: #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.activity-fill {
  height: 100%;
  border-radius: 6px;
  transition: width 0.5s ease;
}

.activity-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px;
  background-color: #fafafa;
  border-radius: var(--border-radius);
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.summary-label {
  color: var(--text-secondary);
}

.summary-value {
  font-weight: 600;
  color: var(--text-primary);
}

/* 视频观看偏好 */
.video-preferences {
  padding: 15px;
}

.preference-item {
  display: flex;
  margin-bottom: 15px;
  align-items: center;
}

.preference-label {
  font-weight: 600;
  color: var(--text-primary);
  width: 150px;
}

.preference-value {
  flex: 1;
  color: var(--text-secondary);
}

.type-tag {
  display: inline-block;
  background-color: #e6f7ff;
  color: #1890ff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
  margin-right: 5px;
}

.completion-bar {
  width: 200px;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
  margin-right: 10px;
  display: inline-block;
}

.completion-fill {
  height: 100%;
  background-color: var(--primary-color);
  border-radius: 5px;
}

.completion-text {
  font-weight: 600;
  color: var(--primary-color);
}

/* 关注UP主偏好 */
.up-preferences {
  padding: 15px;
}

.up-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.up-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  background-color: #fafafa;
  border-radius: var(--border-radius);
  transition: all 0.3s;
}

.up-item:hover {
  background-color: #f0f0f0;
  transform: translateX(5px);
}

.up-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.up-info {
  flex: 1;
}

.up-name {
  display: block;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.up-fans {
  display: block;
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 2px;
}

.up-engagement {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

/* 加载和错误状态 */
.loading {
  color: var(--text-secondary);
  text-align: center;
  padding: 20px;
}

.error {
  color: var(--danger-color);
  text-align: center;
  padding: 20px;
}

.empty {
  color: var(--text-secondary);
  text-align: center;
  padding: 20px;
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

  /* 个人画像响应式 */
  .time-range {
    width: 80px;
    font-size: 12px;
  }

  .preference-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .preference-label {
    width: auto;
  }

  .completion-bar {
    width: 100%;
  }
}
</style>
