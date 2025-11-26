<template>
  <div class="recommendations-container">
    <h1 class="page-title">用户推荐系统</h1>

    <!-- API选择按钮区域 -->
    <div class="api-buttons">
      <button
        v-for="api in recommendApis"
        :key="api.key"
        class="api-button"
        :class="{ active: selectedApis.includes(api.key) }"
        @click="toggleApiSelection(api.key)"
      >
        {{ api.name }}
      </button>
    </div>

    <!-- 获取推荐按钮 -->
    <div class="action-section">
      <button
        class="fetch-button"
        :disabled="selectedApis.length === 0 || loading"
        @click="fetchRecommendations"
      >
        {{ loading ? '获取中...' : '获取推荐' }}
      </button>

      <div v-if="selectedApis.length > 0" class="selected-count">
        已选择 {{ selectedApis.length }} 个推荐来源
      </div>
    </div>

    <!-- 推荐结果展示区域 -->
    <div v-if="filteredResults.length > 0" class="results-section">
      <h2 class="results-title">推荐结果 ({{ filteredResults.length }})</h2>

      <div class="user-grid">
        <div
          v-for="user in filteredResults"
          :key="user.userId"
          class="user-card"
          @mouseenter="showTooltip(user, $event)"
          @mouseleave="hideTooltip"
        >
          <div class="user-avatar">
            <img :src="user.avatarPath || defaultAvatar" alt="用户头像">
          </div>
          <div class="user-info">
            <h3 class="username">{{ user.username }}</h3>
            <p class="similarity-rate">相似度: {{ getHighestSimilarity(user) }}%</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && selectedApis.length > 0" class="empty-state">
      <p>没有找到符合条件的用户推荐</p>
    </div>

    <!-- 提示信息 -->
    <div v-if="message" class="message" :class="messageType">
      {{ message }}
    </div>

    <!-- 悬停提示框 -->
    <div
      v-if="tooltipVisible"
      class="tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-header">
        <h4>{{ tooltipUser?.username }}</h4>
      </div>
      <div class="tooltip-content">
        <div
          v-for="(value, key) in getDetailedInfo(tooltipUser)"
          :key="key"
          class="tooltip-item"
        >
          <span class="tooltip-label">{{ formatKey(key) }}:</span>
          <span class="tooltip-value">{{ formatValue(value, key) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
// 移除不存在的store导入
// import { useRecommendStore } from '@/store/recommend'
// import { useUserStore } from '@/store/user'
import { recommendApi } from '@/services/api'

// 本地状态管理
// const recommendStore = useRecommendStore()
// const userStore = useUserStore()
// 从localStorage获取当前用户ID
const getCurrentUserId = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    try {
      const userInfo = JSON.parse(userStr)
      return userInfo.userId || localStorage.getItem('userId')
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  return localStorage.getItem('userId') || 123 // 默认值作为后备
}

// 响应式数据
const selectedApis = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('info')
const tooltipVisible = ref(false)
const tooltipUser = ref(null)
const tooltipStyle = ref({})

// 默认头像
const defaultAvatar = '/avatar-placeholder.png'

// API配置
const recommendApis = [
  { key: 'coComment', name: '同视频评论推荐', endpoint: '/api/recommend/co-comment/' },
  { key: 'reply', name: '回复互动推荐', endpoint: '/api/recommend/reply/' },
  { key: 'sharedVideo', name: '视频观看相似推荐', endpoint: '/api/recommend/shared-video/' },
  { key: 'category', name: '分区重合推荐', endpoint: '/api/recommend/category/' },
  { key: 'theme', name: '内容类型重合推荐', endpoint: '/api/recommend/theme/' },
  { key: 'userBehavior', name: '用户行为相似推荐', endpoint: '/api/recommend/user-behavior/' },
  { key: 'commonUp', name: '共同关注UP主推荐', endpoint: '/api/recommend/common-up/' },
  { key: 'favoriteSimilarity', name: '收藏夹相似推荐', endpoint: '/api/recommend/favorite-similarity/' },
  { key: 'commentFriends', name: '评论内容相似推荐', endpoint: '/api/recommend/comment-friends/' }
]

// 存储所有API的结果
const apiResults = ref({})

// 计算过滤后的结果（用户ID交集）
const filteredResults = computed(() => {
  if (selectedApis.value.length === 0) return []

  // 获取所有选中API的用户ID集合
  const userIdSets = selectedApis.value.map(apiKey => {
    const results = apiResults.value[apiKey] || []
    return new Set(results.map(user => user.userId))
  })

  // 计算用户ID的交集
  const intersection = userIdSets.reduce((a, b) => {
    return new Set([...a].filter(x => b.has(x)))
  })

  // 合并交集中用户的所有信息
  const mergedUsers = []
  intersection.forEach(userId => {
    // 创建一个合并的用户对象
    const mergedUser = { userId }

    // 从各个API结果中收集信息
    selectedApis.value.forEach(apiKey => {
      const userData = (apiResults.value[apiKey] || []).find(u => u.userId === userId)
      if (userData) {
        // 合并用户数据，但优先保留非空值
        Object.keys(userData).forEach(key => {
          if (userData[key] !== undefined && userData[key] !== null) {
            mergedUser[key] = userData[key]
          }
        })

        // 记录这个用户来自哪个API
        if (!mergedUser.sourceApis) mergedUser.sourceApis = []
        mergedUser.sourceApis.push(apiKey)
      }
    })

    mergedUsers.push(mergedUser)
  })

  // 按最高相似度排序
  return mergedUsers.sort((a, b) => getHighestSimilarity(b) - getHighestSimilarity(a))
})

// 切换API选择
function toggleApiSelection(apiKey) {
  const index = selectedApis.value.indexOf(apiKey)
  if (index > -1) {
    selectedApis.value.splice(index, 1)
  } else {
    selectedApis.value.push(apiKey)
  }
}

// 获取推荐数据
async function fetchRecommendations() {
  if (selectedApis.value.length === 0) {
    showMessage('请至少选择一个推荐来源', 'warning')
    return
  }

  loading.value = true
  message.value = ''

  // 获取当前用户ID
  const currentUserId = getCurrentUserId()
  if (!currentUserId) {
    showMessage('无法获取用户ID，请先登录', 'error')
    loading.value = false
    return
  }

  try {
    // 创建API请求数组
    const fetchPromises = selectedApis.value.map(async apiKey => {
      try {
        // 使用recommendApi调用实际的API
        const response = await recommendApi[apiKey](currentUserId)
        // 确保返回的数据是数组格式
        const results = Array.isArray(response) ? response : (response.results || response.data || [])
        apiResults.value[apiKey] = results
      } catch (err) {
        console.error(`获取${apiKey}推荐数据失败:`, err)
        // 出错时使用空数组，确保后续流程可以继续
        apiResults.value[apiKey] = []
      }
    })

    // 等待所有请求完成
    await Promise.all(fetchPromises)

    // 检查是否有有效的推荐结果
    let hasResults = false
    selectedApis.value.forEach(apiKey => {
      if (apiResults.value[apiKey] && apiResults.value[apiKey].length > 0) {
        hasResults = true
      }
    })

    if (!hasResults) {
      showMessage('没有找到推荐用户，请尝试选择其他推荐来源', 'info')
    } else if (filteredResults.value.length === 0) {
      showMessage('没有找到共同匹配的用户，各推荐来源之间无交集', 'info')
    } else {
      showMessage(`成功找到 ${filteredResults.value.length} 个匹配用户`, 'success')
    }
  } catch (error) {
    console.error('获取推荐失败:', error)
    showMessage('获取推荐失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 显示提示信息
function showMessage(text, type = 'info') {
  message.value = text
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

// 显示悬停提示
function showTooltip(user, event) {
  tooltipUser.value = user
  tooltipVisible.value = true

  // 计算提示框位置
  const rect = event.target.getBoundingClientRect()
  tooltipStyle.value = {
    left: `${rect.right + 10}px`,
    top: `${rect.top}px`
  }
}

// 隐藏悬停提示
function hideTooltip() {
  tooltipVisible.value = false
  tooltipUser.value = null
}

// 获取用户的详细信息（排除基本字段）
function getDetailedInfo(user) {
  if (!user) return {}

  const detailedInfo = {}
  Object.keys(user).forEach(key => {
    // 排除基本字段和sourceApis
    if (!['userId', 'username', 'avatarPath', 'sourceApis'].includes(key)) {
      detailedInfo[key] = user[key]
    }
  })

  return detailedInfo
}

// 格式化键名显示
function formatKey(key) {
  return key.replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
    .replace(/Similarity Score|Score|Count$/, match => {
      switch (match) {
        case 'Similarity Score': return '相似度'
        case 'Score': return '分数'
        case 'Count': return '数量'
        default: return match
      }
    })
}

// 格式化值显示
function formatValue(value, key) {
  // 相似度值转换为百分比
  if (key.toLowerCase().includes('similarity') && typeof value === 'number' && value <= 1) {
    return `${(value * 100).toFixed(1)}%`
  }
  return value
}

// 获取用户的最高相似度
function getHighestSimilarity(user) {
  if (!user) return 0

  let maxSimilarity = 0
  Object.keys(user).forEach(key => {
    if (key.toLowerCase().includes('similarity') &&
        typeof user[key] === 'number' &&
        user[key] > maxSimilarity) {
      maxSimilarity = user[key]
    }
  })

  return (maxSimilarity * 100).toFixed(1)
}

// 生成模拟头像路径
function generateAvatarPath(userId) {
  // 这里使用随机头像服务，实际项目中应该使用真实的头像服务
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userId}`
}

// 生成模拟数据
function generateMockData(apiKey) {
  const mockUsers = []
  const baseCount = Math.floor(Math.random() * 5) + 5

  for (let i = 1; i <= baseCount; i++) {
    const userId = 1000 + i + (apiKey.charCodeAt(0) * 100)
    const similarity = 0.5 + Math.random() * 0.5 // 0.5-1.0的相似度

    const user = {
      userId,
      username: `${getApiTypeName(apiKey)}用户${i}`,
      avatarPath: generateAvatarPath(userId)
    }

    // 根据API类型添加特定字段
    switch (apiKey) {
      case 'coComment':
        user.commonVideoCount = Math.floor(Math.random() * 20) + 1
        user.similarityScore = similarity
        break
      case 'reply':
        user.replyCount = Math.floor(Math.random() * 50) + 5
        user.similarityScore = similarity
        break
      case 'sharedVideo':
        user.sharedVideoCount = Math.floor(Math.random() * 100) + 10
        user.similarityScore = similarity
        break
      case 'category':
        user.commonCategoryCount = Math.floor(Math.random() * 15) + 3
        user.similarityScore = similarity
        break
      case 'theme':
        user.commonThemeCount = Math.floor(Math.random() * 25) + 5
        user.similarityScore = similarity
        break
      case 'userBehavior':
        user.behaviorSimilarity = similarity
        user.activeTimeSimilarity = 0.6 + Math.random() * 0.4
        break
      case 'commonUp':
        user.commonUpCount = Math.floor(Math.random() * 30) + 5
        user.similarityScore = similarity
        break
      case 'favoriteSimilarity':
        user.commonFavoriteCount = Math.floor(Math.random() * 50) + 10
        user.similarityScore = similarity
        break
      case 'commentFriends':
        user.commentSimilarity = similarity
        user.commonTopicCount = Math.floor(Math.random() * 15) + 3
        break
    }

    mockUsers.push(user)
  }

  return mockUsers
}

// 获取API类型的中文名
function getApiTypeName(apiKey) {
  const api = recommendApis.find(a => a.key === apiKey)
  return api ? api.name : apiKey
}

// 组件挂载时的初始化
onMounted(() => {
  // 初始化时可以自动选择一些API
  // selectedApis.value = ['commonUp', 'sharedVideo']
})
</script>

<style scoped>
.recommendations-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  position: relative;
}

.page-title {
  font-size: 2.5rem;
  color: #333;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 600;
}

.api-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
  margin-bottom: 25px;
}

.api-button {
  padding: 12px 20px;
  background-color: #f0f0f0;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  text-align: left;
}

.api-button:hover {
  background-color: #e0e0e0;
  border-color: #999;
}

.api-button.active {
  background-color: #ff69b4;
  color: white;
  border-color: #ff1493;
}

.action-section {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.fetch-button {
  padding: 14px 30px;
  background-color: #ff69b4;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.fetch-button:hover:not(:disabled) {
  background-color: #ff1493;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
}

.fetch-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

.results-section {
  margin-top: 40px;
}

.results-title {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 25px;
  font-weight: 500;
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.user-card {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.user-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  border-color: #ff69b4;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 15px;
  border: 3px solid #ff69b4;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  text-align: center;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.similarity-rate {
  font-size: 14px;
  color: #ff69b4;
  font-weight: 500;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
  font-size: 16px;
}

.message {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

.message.success {
  background-color: #4caf50;
}

.message.error {
  background-color: #f44336;
}

.message.warning {
  background-color: #ff9800;
}

.message.info {
  background-color: #2196f3;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.tooltip {
  position: fixed;
  background-color: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 15px 20px;
  border-radius: 8px;
  z-index: 1001;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.tooltip-header {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.tooltip-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.tooltip-content {
  font-size: 14px;
}

.tooltip-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 4px 0;
}

.tooltip-item:last-child {
  margin-bottom: 0;
}

.tooltip-label {
  color: #ccc;
  font-weight: 500;
}

.tooltip-value {
  color: #ff69b4;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .api-buttons {
    grid-template-columns: 1fr;
  }

  .user-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }

  .page-title {
    font-size: 2rem;
  }

  .results-title {
    font-size: 1.5rem;
  }

  .tooltip {
    max-width: 250px;
    font-size: 13px;
  }
}
</style>
