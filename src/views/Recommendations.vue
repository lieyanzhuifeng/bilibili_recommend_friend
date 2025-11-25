<template>
  <div class="recommendations-page">
    <h2>好友推荐与筛选</h2>

    <div class="controls">
      <div class="filter-section">
        <h3>推荐筛选</h3>
        <div class="filter-controls">
          <select v-model="activeFilter" @change="applyFilter">
            <option value="all">全部推荐</option>
            <option value="commonUp">共同关注UP主</option>
            <option value="sharedVideo">共享视频</option>
            <option value="userBehavior">用户行为相似度</option>
            <option value="coComment">同视频评论</option>
            <option value="theme">内容类型重合度</option>
          </select>

          <!-- 筛选参数输入 -->
          <div v-if="activeFilter === 'sameUp'" class="filter-params">
            <input v-model="filterParams.upId" placeholder="UP主ID" type="number" />
            <select v-model="filterParams.durationOption">
              <option value="week">近一周</option>
              <option value="month">近一月</option>
              <option value="year">近一年</option>
            </select>
          </div>

          <div v-if="activeFilter === 'sameTag'" class="filter-params">
            <input v-model="filterParams.tagId" placeholder="标签ID" type="number" />
            <select v-model="filterParams.durationOption">
              <option value="week">近一周</option>
              <option value="month">近一月</option>
              <option value="year">近一年</option>
            </select>
          </div>

          <div v-if="activeFilter === 'sameTagVideoCount'" class="filter-params">
            <input v-model="filterParams.tagId" placeholder="标签ID" type="number" />
            <input v-model="filterParams.ratioOption" placeholder="观看比例阈值" type="number" step="0.1" min="0" max="1" />
          </div>

          <div v-if="activeFilter === 'deepVideo'" class="filter-params">
            <input v-model="filterParams.videoId" placeholder="视频ID" type="number" />
            <select v-model="filterParams.option">
              <option value="fullWatch">完整观看</option>
              <option value="highInteraction">高互动</option>
            </select>
          </div>

          <button @click="applyFilter" :disabled="loading">
            {{ loading ? '筛选中...' : '应用筛选' }}
          </button>
        </div>
      </div>

      <button @click="fetchAll" :disabled="loading">
        {{ loading ? '加载中...' : '刷新全部推荐' }}
      </button>
    </div>

    <!-- 提示消息 -->
    <div v-if="notification" :class="['notification', notification.type]">
      {{ notification.message }}
      <button @click="clearNotification" class="close-btn">×</button>
    </div>

    <div class="sections">
      <section v-if="coComment.length">
        <h3>同视频评论推荐</h3>
        <div class="recommend-grid">
          <div v-for="u in coComment" :key="u.userId || u.id" class="user-card">
            <img :src="avatar(u)" alt="{{ u.username }}" class="avatar"/>
            <div class="user-info">
              <div class="username">{{ u.username }}</div>
              <div v-if="u.commentCount" class="info">评论: {{ u.commentCount }}条</div>
            </div>
            <button
              class="add-friend-btn"
              @click="sendFriendRequest(u.userId || u.id, u.username)"
              :disabled="isRequesting(u.userId || u.id)"
            >
              {{ getRequestStatus(u.userId || u.id) }}
            </button>
          </div>
        </div>
      </section>

      <section v-if="reply.length">
        <h3>回复推荐</h3>
        <div class="recommend-grid">
          <div v-for="u in reply" :key="u.userId || u.id" class="user-card">
            <img :src="avatar(u)" alt="{{ u.username }}" class="avatar"/>
            <div class="user-info">
              <div class="username">{{ u.username }}</div>
              <div v-if="u.replyCount" class="info">回复: {{ u.replyCount }}条</div>
            </div>
            <button
              class="add-friend-btn"
              @click="sendFriendRequest(u.userId || u.id, u.username)"
              :disabled="isRequesting(u.userId || u.id)"
            >
              {{ getRequestStatus(u.userId || u.id) }}
            </button>
          </div>
        </div>
      </section>

      <section v-if="sharedVideo.length">
        <h3>观看视频相似度推荐</h3>
        <div class="recommend-grid">
          <div v-for="u in sharedVideo" :key="u.userId || u.id" class="user-card">
            <img :src="avatar(u)" alt="{{ u.username }}" class="avatar"/>
            <div class="user-info">
              <div class="username">{{ u.username }}</div>
              <div class="info">相似度: {{ u.similarityRate }}%</div>
              <div v-if="u.sharedVideoCount" class="info">共同观看: {{ u.sharedVideoCount }}个视频</div>
            </div>
            <button
              class="add-friend-btn"
              @click="sendFriendRequest(u.userId || u.id, u.username)"
              :disabled="isRequesting(u.userId || u.id)"
            >
              {{ getRequestStatus(u.userId || u.id) }}
            </button>
          </div>
        </div>
      </section>

      <!-- 筛选结果显示 -->
      <section v-if="showFilteredResults">
        <h3>{{ filterTitle }}</h3>
        <div class="recommend-grid">
          <div v-for="u in filteredResults" :key="u.userId || u.id" class="user-card">
            <img :src="avatar(u)" alt="{{ u.username }}" class="avatar"/>
            <div class="user-info">
              <div class="username">{{ u.username }}</div>
              <div v-if="u.similarityRate" class="info">相似度: {{ u.similarityRate }}%</div>
              <div v-else-if="u.score" class="info">匹配度: {{ u.score }}</div>
              <div v-if="u.sharedVideoCount" class="info">共同观看: {{ u.sharedVideoCount }}个视频</div>
              <div v-if="u.commentCount" class="info">评论: {{ u.commentCount }}条</div>
              <div v-if="u.commonUpCount" class="info">共同关注UP主: {{ u.commonUpCount }}个</div>
              <div v-if="u.watchRatio" class="info">观看比例: {{ (u.watchRatio * 100).toFixed(1) }}%</div>
            </div>
            <button
              class="add-friend-btn"
              @click="sendFriendRequest(u.userId || u.id, u.username)"
              :disabled="isRequesting(u.userId || u.id)"
            >
              {{ getRequestStatus(u.userId || u.id) }}
            </button>
          </div>
        </div>
      </section>

      <section v-if="!showFilteredResults && otherLists.length">
        <h3>其他推荐</h3>
        <div class="recommend-grid">
          <div v-for="u in otherLists" :key="u.userId || u.id" class="user-card">
            <img :src="avatar(u)" alt="{{ u.username }}" class="avatar"/>
            <div class="user-info">
              <div class="username">{{ u.username }}</div>
              <div v-if="u.score" class="info">匹配度: {{ u.score }}</div>
            </div>
            <button
              class="add-friend-btn"
              @click="sendFriendRequest(u.userId || u.id, u.username)"
              :disabled="isRequesting(u.userId || u.id)"
            >
              {{ getRequestStatus(u.userId || u.id) }}
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && isAllEmpty" class="empty-state">
      <p>暂无推荐好友</p>
      <button @click="fetchAll">尝试重新加载</button>
    </div>
  </div>
</template>

<script>
import { onMounted, computed, ref } from 'vue'
import { useRecommendStore } from '../stores/recommend'
import { useUserStore } from '../stores/user'
import { friendApi, recommendApi, filterApi } from '../services/api'
import { useRouter } from 'vue-router'

export default {
  name: 'Recommendations',
  setup() {
    const recommendStore = useRecommendStore()
    const userStore = useUserStore()
    const router = useRouter()

    // 请求状态管理
    const requestStatus = ref({})
    const notification = ref(null)
    const loading = computed(() => recommendStore.loading)

    // 筛选相关状态
    const activeFilter = ref('all')
    const filterParams = ref({
      upId: '',
      tagId: '',
      videoId: '',
      durationOption: 'month',
      ratioOption: 0.5,
      option: 'fullWatch'
    })
    const filteredResults = ref([])

    // 生成随机头像
    function avatar(u) {
      if (u && u.avatar) return u.avatar
      const name = u && u.username ? u.username : 'User'
      const colors = ['#4285F4', '#EA4335', '#FBBC05', '#34A853', '#673AB7', '#FF9800']
      const color = colors[name.charCodeAt(0) % colors.length]
      const initial = name.charAt(0).toUpperCase()
      return `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="${color}"/><text x="50%" y="50%" font-size="16" text-anchor="middle" dy=".3em" fill="white">${initial}</text></svg>`
    }

    async function fetchAll() {
      const id = userStore.userId || 1
      if (!id) return

      // 只触发几个常用推荐，store 已经有错误处理
      try {
        // 确保使用用户要求的5个推荐API
        await Promise.all([
          recommendStore.fetchCommonUp(id),
          recommendStore.fetchSharedVideo(id),
          recommendStore.fetchUserBehavior(id),
          recommendStore.fetchCoComment(id),
          recommendStore.fetchTheme(id)
        ])

        // 重置筛选结果
        filteredResults.value = []
        activeFilter.value = 'all'
      } catch (err) {
        console.error('获取推荐数据失败:', err)
        showNotification('获取推荐失败，请稍后重试', 'error')
      }
    }

    // 应用筛选
    async function applyFilter() {
      const id = userStore.userId || 1
      if (!id) return

      try {
        let results = []

        // 根据选择的筛选类型调用不同的API
        switch (activeFilter.value) {
          case 'commonUp':
            results = await recommendApi.commonUp(id)
            break
          case 'sharedVideo':
            results = await recommendApi.sharedVideo(id)
            break
          case 'userBehavior':
            results = await recommendApi.userBehavior(id)
            break
          case 'coComment':
            results = await recommendApi.coComment(id)
            break
          case 'theme':
            results = await recommendApi.theme(id)
            break
          case 'sameUp':
            if (!filterParams.value.upId) {
              showNotification('请输入UP主ID', 'error')
              return
            }
            results = await filterApi.sameUp({
              upId: filterParams.value.upId,
              durationOption: filterParams.value.durationOption
            })
            break
          case 'sameTag':
            if (!filterParams.value.tagId) {
              showNotification('请输入标签ID', 'error')
              return
            }
            results = await filterApi.sameTag({
              tagId: filterParams.value.tagId,
              durationOption: filterParams.value.durationOption
            })
            break
          case 'sameTagVideoCount':
            if (!filterParams.value.tagId) {
              showNotification('请输入标签ID', 'error')
              return
            }
            results = await filterApi.sameTagVideoCount({
              tagId: filterParams.value.tagId,
              ratioOption: filterParams.value.ratioOption
            })
            break
          case 'deepVideo':
            if (!filterParams.value.videoId) {
              showNotification('请输入视频ID', 'error')
              return
            }
            results = await filterApi.deepVideo({
              videoId: filterParams.value.videoId,
              option: filterParams.value.option
            })
            break
          case 'sameUpVideoCount':
            if (!filterParams.value.upId) {
              showNotification('请输入UP主ID', 'error')
              return
            }
            results = await filterApi.sameUpVideoCount({
              upId: filterParams.value.upId,
              userId: id
            })
            break
          case 'all':
          default:
            filteredResults.value = []
            return
        }

        // 处理返回结果
        if (results && Array.isArray(results)) {
          filteredResults.value = results.map(item => ({
            ...item,
            userId: item.userId || item.id,
            username: item.username || `用户${item.userId || item.id}`
          }))
          showNotification(`筛选完成，找到 ${filteredResults.value.length} 个结果`, 'success')
        } else {
          filteredResults.value = []
          showNotification('未找到符合条件的结果', 'info')
        }
      } catch (error) {
        console.error('筛选失败:', error)
        showNotification('筛选失败，请稍后重试', 'error')
      }
    }

    // 发送好友申请
    async function sendFriendRequest(userId, username) {
      try {
        // 设置请求状态
        requestStatus.value[userId] = 'sending'

        await friendApi.sendFriendRequest(userId)

        // 更新状态为已发送
        requestStatus.value[userId] = 'sent'

        showNotification(`已向 ${username} 发送好友请求`, 'success')

        // 3秒后恢复按钮状态
        setTimeout(() => {
          delete requestStatus.value[userId]
        }, 3000)

      } catch (error) {
        console.error('发送好友请求失败:', error)
        requestStatus.value[userId] = 'failed'
        showNotification('发送好友请求失败，请稍后重试', 'error')

        // 2秒后恢复按钮状态
        setTimeout(() => {
          delete requestStatus.value[userId]
        }, 2000)
      }
    }

    // 显示通知
    function showNotification(message, type = 'info') {
      notification.value = {
        message,
        type
      }

      // 3秒后自动清除
      setTimeout(() => {
        notification.value = null
      }, 3000)
    }

    // 清除通知
    function clearNotification() {
      notification.value = null
    }

    // 检查是否正在请求中
    function isRequesting(userId) {
      const status = requestStatus.value[userId]
      return status === 'sending' || status === 'sent'
    }

    // 获取请求状态文本
    function getRequestStatus(userId) {
      const status = requestStatus.value[userId]
      switch (status) {
        case 'sending':
          return '发送中...'
        case 'sent':
          return '已发送'
        case 'failed':
          return '发送失败'
        default:
          return '添加好友'
      }
    }

    // 检查是否所有列表都为空
    const isAllEmpty = computed(() => {
      return coComment.value.length === 0 &&
             reply.value.length === 0 &&
             sharedVideo.value.length === 0 &&
             otherLists.value.length === 0
    })

    onMounted(() => {
      fetchAll()
    })

    const coComment = computed(() => recommendStore.coComment || [])
    const reply = computed(() => recommendStore.reply || [])
    const sharedVideo = computed(() => recommendStore.sharedVideo || [])
    const otherLists = computed(() => {
      return [
        ...(recommendStore.category || []),
        ...(recommendStore.theme || []),
        ...(recommendStore.userBehavior || []),
        ...(recommendStore.commonUp || []),
        ...(recommendStore.favoriteSimilarity || []),
        ...(recommendStore.commentFriends || [])
      ]
    })

    // 筛选结果是否显示
    const showFilteredResults = computed(() => {
      return activeFilter.value !== 'all' && filteredResults.value.length > 0
    })

    // 获取当前筛选类型的标题
    const filterTitle = computed(() => {
      const titles = {
        commonUp: '共同关注UP主推荐',
        sharedVideo: '共享视频推荐',
        userBehavior: '用户行为相似度推荐',
        coComment: '同视频评论推荐',
        theme: '内容类型重合度推荐',
        sameUp: '同一UP主筛选结果',
        sameTag: '同一标签筛选结果',
        sameUpVideoCount: 'UP主视频观看比例筛选结果',
        sameTagVideoCount: '标签视频观看比例筛选结果',
        deepVideo: '深度视频筛选结果'
      }
      return titles[activeFilter.value] || '筛选结果'
    })

    return {
      fetchAll,
      applyFilter,
      coComment,
      reply,
      sharedVideo,
      otherLists,
      avatar,
      loading,
      sendFriendRequest,
      isRequesting,
      getRequestStatus,
      notification,
      clearNotification,
      isAllEmpty,
      activeFilter,
      filterParams,
      filteredResults,
      showFilteredResults,
      filterTitle
    }
  }
}
</script>

<style scoped>
.recommendations-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.controls {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-start;
}

.filter-section {
  flex: 1;
  min-width: 300px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.filter-section h3 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #333;
  font-size: 16px;
}

.filter-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-controls select,
.filter-controls input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-params {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 10px;
  padding: 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
}

.filter-params input,
.filter-params select {
  min-width: 120px;
}

.controls button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.controls button:hover:not(:disabled) {
  background-color: #0056b3;
}

.controls button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 通知样式 */
.notification {
  padding: 12px 20px;
  margin-bottom: 20px;
  border-radius: 5px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  animation: slideDown 0.3s ease-out;
}

.notification.success {
  background-color: #d4edda;
  color: #155724;
  border-left: 4px solid #28a745;
}

.notification.error {
  background-color: #f8d7da;
  color: #721c24;
  border-left: 4px solid #dc3545;
}

.notification.info {
  background-color: #d1ecf1;
  color: #0c5460;
  border-left: 4px solid #17a2b8;
}

.notification .close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  margin-left: 10px;
  color: inherit;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 推荐部分样式 */
.sections section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sections h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #007bff;
  padding-bottom: 8px;
}

/* 用户卡片网格 */
.recommend-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.user-card {
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  flex: 1;
}

.username {
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}

.info {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.add-friend-btn {
  padding: 8px 16px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.add-friend-btn:hover:not(:disabled) {
  background-color: #218838;
}

.add-friend-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #6c757d;
}

.empty-state p {
  margin-bottom: 20px;
  font-size: 18px;
}

.empty-state button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}

.empty-state button:hover {
  background-color: #0056b3;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .recommend-grid {
    grid-template-columns: 1fr;
  }

  .user-card {
    flex-direction: column;
    text-align: center;
  }

  .add-friend-btn {
    width: 100%;
  }
}
</style>
