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

          <!-- 分类分布 -->
          <Card class="portrait-card" title="分类分布">
            <div class="distribution">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="Object.keys(userPortrait.categoryDistribution).length > 0" class="distribution-container">
                <div class="bubble-chart">
                  <div 
                    v-for="(score, name) in userPortrait.categoryDistribution" 
                    :key="`cat-${name}`" 
                    class="bubble-item"
                    :style="{
                      backgroundColor: getBubbleColor(score)
                    }"
                    :title="`${name}: ${score.toFixed(1)}%`"
                  >
                    <span class="bubble-name">{{ name }}</span>
                    <span class="bubble-score">{{ score.toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无分类分布数据</div>
            </div>
          </Card>

          <!-- 主题分布 -->
          <Card class="portrait-card" title="主题分布">
            <div class="distribution">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="Object.keys(userPortrait.themeDistribution).length > 0" class="distribution-container">
                <div class="bubble-chart">
                  <div 
                    v-for="(score, name) in userPortrait.themeDistribution" 
                    :key="`theme-${name}`" 
                    class="bubble-item"
                    :style="{
                      backgroundColor: getBubbleColor(score)
                    }"
                    :title="`${name}: ${score.toFixed(1)}%`"
                  >
                    <span class="bubble-name">{{ name }}</span>
                    <span class="bubble-score">{{ score.toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无主题分布数据</div>
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
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无关注UP主数据</div>
            </div>
          </Card>

          <!-- 收藏视频 -->
          <Card class="portrait-card" title="收藏视频">
            <div class="favorite-videos">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="userPortrait.favoriteVideos.length > 0">
                <div class="video-list">
                  <div v-for="video in userPortrait.favoriteVideos" :key="video.id" class="video-item">
                    <div class="video-title">{{ video.title }}</div>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无收藏视频数据</div>
            </div>
          </Card>

          <!-- 深度观看视频 -->
          <Card class="portrait-card" title="深度观看视频">
            <div class="deep-watched-videos">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else-if="userPortrait.deepWatchedVideos.length > 0">
                <div class="video-list">
                  <div v-for="video in userPortrait.deepWatchedVideos" :key="video.id" class="video-item">
                    <div class="video-title">{{ video.title }}</div>
                    <div class="video-info">
                      <span class="video-up">
                        <img :src="avatar({ avatarPath: video.upAvatar })" alt="UP主头像" class="up-avatar-small" />
                        {{ video.upName }}
                      </span>
                      <span class="video-stats">
                        <span>观看时长: {{ video.totalWatchDuration.toFixed(2) }}小时</span>
                        <span>观看次数: {{ video.watchCount }}次</span>
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty">暂无深度观看视频数据</div>
            </div>
          </Card>

          <!-- 行为统计 -->
          <Card class="portrait-card" title="行为统计">
            <div class="behavior-stats">
              <div v-if="userPortrait.loading" class="loading">加载中...</div>
              <div v-else-if="userPortrait.error" class="error">{{ userPortrait.error }}</div>
              <div v-else>
                <div class="stats-grid">
                  <div class="stat-item">
                    <span class="stat-label">点赞率</span>
                    <div class="stat-value">
                      <div class="stat-bar">
                        <div class="stat-fill" :style="{ width: `${userPortrait.behaviorStatistics.likeRate}%` }"></div>
                      </div>
                      <span class="stat-percent">{{ userPortrait.behaviorStatistics.likeRate.toFixed(1) }}%</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">投币率</span>
                    <div class="stat-value">
                      <div class="stat-bar">
                        <div class="stat-fill" :style="{ width: `${userPortrait.behaviorStatistics.coinRate}%` }"></div>
                      </div>
                      <span class="stat-percent">{{ userPortrait.behaviorStatistics.coinRate.toFixed(1) }}%</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">收藏率</span>
                    <div class="stat-value">
                      <div class="stat-bar">
                        <div class="stat-fill" :style="{ width: `${userPortrait.behaviorStatistics.favoriteRate}%` }"></div>
                      </div>
                      <span class="stat-percent">{{ userPortrait.behaviorStatistics.favoriteRate.toFixed(1) }}%</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">分享率</span>
                    <div class="stat-value">
                      <div class="stat-bar">
                        <div class="stat-fill" :style="{ width: `${userPortrait.behaviorStatistics.shareRate}%` }"></div>
                      </div>
                      <span class="stat-percent">{{ userPortrait.behaviorStatistics.shareRate.toFixed(1) }}%</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">总观看时长</span>
                    <div class="stat-value simple">
                      <span>{{ userPortrait.behaviorStatistics.totalWatchHours.toFixed(1) }}小时</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">活跃天数</span>
                    <div class="stat-value simple">
                      <span>{{ userPortrait.behaviorStatistics.activeDays }}天</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">夜间观看天数</span>
                    <div class="stat-value simple">
                      <span>{{ userPortrait.behaviorStatistics.nightWatchDays }}天</span>
                    </div>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">夜间观看分钟数</span>
                    <div class="stat-value simple">
                      <span>{{ userPortrait.behaviorStatistics.nightWatchMinutes }}分钟</span>
                    </div>
                  </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { avatarUrl, recommendApi, filterApi, userApi } from '@/services/api'
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

    const getBubbleColor = (score) => {
    if (score > 15) return '#ff7e5f' // 最高 - 橙红色
    if (score > 10) return '#feb47b' // 高 - 橙色
    if (score > 5) return '#6a89cc' // 中 - 蓝色
    if (score > 2) return '#82ccdd' // 低 - 浅蓝色
    return '#b8e994' // 最低 - 浅绿色
  }

    // 计算展示名
    const displayName = computed(() => {
      return userStore.username || String(userStore.userId || '')
    })

    // 个人画像数据
    const userPortrait = ref({
      loading: false,
      error: null,
      categoryDistribution: {}, // 分类分布
      themeDistribution: {}, // 主题分布
      favoriteUp: [], // 关注的UP主偏好
      favoriteVideos: [], // 收藏视频
      deepWatchedVideos: [], // 深度观看视频
      behaviorStatistics: { // 行为统计
        likeRate: 0, // 点赞率
        coinRate: 0, // 投币率
        favoriteRate: 0, // 收藏率
        shareRate: 0, // 分享率
        totalWatchHours: 0, // 总观看时长
        activeDays: 0, // 活跃天数
        nightWatchDays: 0, // 夜间观看天数
        nightWatchMinutes: 0 // 夜间观看分钟数
      }
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
        // 使用新的用户完整画像API
        const userProfile = await userApi.getUserProfile(userId)

        // 转换数据结构为现有的userPortrait格式

        // 1. 分类分布和主题分布
        const categoryDistribution = userProfile.categoryDistribution || {}
        const themeDistribution = userProfile.themeDistribution || {}

        // 2. 活跃时间模式
        let activeTimeSlots = []
        let mostActiveTime = ''
        let dailyActiveHours = 0

        if (userProfile.behaviorStatistics) {
          // 计算每日平均活跃时长（总观看时长/活跃天数）
          const { totalWatchHours, activeDays } = userProfile.behaviorStatistics
          dailyActiveHours = activeDays > 0 ? totalWatchHours / activeDays : 0

          // 判断最活跃时段（根据夜间观看分钟数）
          const { nightWatchMinutes } = userProfile.behaviorStatistics
          if (nightWatchMinutes > 360) { // 超过6小时
            mostActiveTime = '夜猫子型'
          } else if (nightWatchMinutes > 180) { // 超过3小时
            mostActiveTime = '混合型'
          } else {
            mostActiveTime = '早起型'
          }

          // 简单模拟活跃时间段
          if (nightWatchMinutes > 180) {
            activeTimeSlots = ['20:00-22:00', '22:00-00:00', '00:00-02:00']
          } else {
            activeTimeSlots = ['08:00-10:00', '12:00-14:00', '18:00-20:00']
          }
        }

        // 3. 视频观看偏好
        let videoTypes = []
        let averageVideoDuration = 0
        let averageCompletionRate = 0

        // 从分类分布获取视频类型偏好
        if (userProfile.categoryDistribution) {
          videoTypes = Object.keys(userProfile.categoryDistribution)
        }

        // 模拟视频时长和完成率
        if (userProfile.behaviorStatistics) {
          // 随机生成一些合理的数值
          averageVideoDuration = Math.floor(Math.random() * 30) + 5 // 5-35分钟
          averageCompletionRate = Math.random() * 0.5 + 0.4 // 40%-90%
        }

        // 4. 关注UP主偏好
        let favoriteUp = []

        if (userProfile.followedUps && userProfile.followedUps.length > 0) {
          favoriteUp = userProfile.followedUps.slice(0, 5).map((up, idx) => ({
            id: up.upId || `up-${idx}`,
            name: up.username || '未知UP主',
            avatar: up.avatarPath || ''
          }))
        }

        // 5. 收藏视频
        let favoriteVideos = []

        if (userProfile.favoriteVideos) {
          favoriteVideos = Object.entries(userProfile.favoriteVideos)
            .slice(0, 10) // 最多显示10个
            .map(([videoId, title]) => ({
              id: videoId,
              title
            }))
        }

        // 6. 深度观看视频
        let deepWatchedVideos = []

        if (userProfile.deepWatchedVideos && userProfile.deepWatchedVideos.length > 0) {
          deepWatchedVideos = userProfile.deepWatchedVideos.slice(0, 10).map(video => ({
            id: video.videoId,
            title: video.videoTitle,
            totalWatchDuration: video.totalWatchDuration,
            watchCount: video.watchCount,
            upId: video.userId,
            upName: video.username,
            upAvatar: video.avatarPath
          }))
        }

        // 7. 行为统计
        const behaviorStatistics = {
          likeRate: userProfile.behaviorStatistics?.likeRate || 0,
          coinRate: userProfile.behaviorStatistics?.coinRate || 0,
          favoriteRate: userProfile.behaviorStatistics?.favoriteRate || 0,
          shareRate: userProfile.behaviorStatistics?.shareRate || 0,
          totalWatchHours: userProfile.behaviorStatistics?.totalWatchHours || 0,
          activeDays: userProfile.behaviorStatistics?.activeDays || 0,
          nightWatchDays: userProfile.behaviorStatistics?.nightWatchDays || 0,
          nightWatchMinutes: userProfile.behaviorStatistics?.nightWatchMinutes || 0
        }

        // 更新个人画像数据
        userPortrait.value = {
          ...userPortrait.value,
          loading: false,
          categoryDistribution,
          themeDistribution,
          favoriteUp,
          favoriteVideos,
          deepWatchedVideos,
          behaviorStatistics
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
      getActivityColor,
      getBubbleColor
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

/* 收藏视频 */
.favorite-videos {
  padding: 15px;
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #fafafa;
  border-radius: var(--border-radius);
  transition: all 0.3s;
}

.video-item:hover {
  background-color: #f0f0f0;
  transform: translateX(5px);
}

.video-title {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
  margin-bottom: 5px;
}

.video-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 12px;
  color: var(--text-secondary);
}

.video-up {
  display: flex;
  align-items: center;
  gap: 5px;
}

.up-avatar-small {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  object-fit: cover;
}

.video-stats {
  display: flex;
  gap: 15px;
}

.video-id {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 行为统计 */
.behavior-stats {
  padding: 15px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.stat-value {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-bar {
  flex: 1;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.stat-fill {
  height: 100%;
  background-color: var(--primary-color);
  border-radius: 5px;
}

.stat-percent {
  font-weight: 600;
  color: var(--primary-color);
  font-size: 14px;
  min-width: 50px;
  text-align: right;
}

.stat-value.simple {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
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

/* 分布容器 */
.distribution-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 气泡图表 */
.bubble-chart {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 50px;
  padding: 20px;
  min-height: 200px;
}

/* 气泡项目 */
.bubble-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  padding: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  animation: float 3s ease-in-out infinite;
}

.bubble-item:hover {
  transform: scale(1.1) !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  z-index: 1;
}

/* 每个气泡不同的浮动动画延迟 */
.bubble-item:nth-child(odd) {
  animation-delay: 0.5s;
}

.bubble-item:nth-child(even) {
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

/* 气泡内容 */
.bubble-name {
  font-size: 16px;
  line-height: 1.2;
  margin-bottom: 4px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
  word-break: keep-all;
  white-space: nowrap;
}

.bubble-score {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 700;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .bubble-chart {
    gap: 30px;
  }
  
  .bubble-item {
    width: 20px;
    height: 20px;
  }
  
  .bubble-name {
    font-size: 12px;
  }
  
  .bubble-score {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .bubble-chart {
    gap: 30px;
  }
  
  .bubble-item {
    width: 20px;
    height: 20px;
  }
  
  .bubble-name {
    font-size: 11px;
  }
}
</style>



