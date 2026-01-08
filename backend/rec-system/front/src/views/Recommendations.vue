<template>
  <div class="recommendations-container">
    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <h3 class="sidebar-title">筛选条件</h3>
        <div class="sidebar-section">
          <h4 class="section-title">用户活跃度</h4>
          <div class="checkbox-group">
            <div class="checkbox-item">
              <input type="checkbox" id="activity-1" v-model="activityOptions" value="1" class="radio-input" @change="calculateActivity">
              <label for="activity-1" class="radio-label">轻度活跃</label>
            </div>
            <div class="checkbox-item">
              <input type="checkbox" id="activity-2" v-model="activityOptions" value="2" class="radio-input" @change="calculateActivity">
              <label for="activity-2" class="radio-label">中度活跃</label>
            </div>
            <div class="checkbox-item">
              <input type="checkbox" id="activity-3" v-model="activityOptions" value="3" class="radio-input" @change="calculateActivity">
              <label for="activity-3" class="radio-label">重度活跃</label>
            </div>
          </div>
          <!-- <div class="filter-value-info">当前组合值: {{ activity }} (0-7)</div> -->
        </div>
        <div class="sidebar-section">
          <h4 class="section-title">夜猫子程度</h4>
          <div class="checkbox-group">
            <div class="checkbox-item">
              <input type="checkbox" id="nightOwl-1" value="1" v-model="nightOwlOptions" class="radio-input" @change="calculateNightOwl">
              <label for="nightOwl-1" class="radio-label">非夜猫子</label>
            </div>
            <div class="checkbox-item">
              <input type="checkbox" id="nightOwl-2" value="2" v-model="nightOwlOptions" class="radio-input" @change="calculateNightOwl">
              <label for="nightOwl-2" class="radio-label">轻度夜猫子</label>
            </div>
            <div class="checkbox-item">
              <input type="checkbox" id="nightOwl-3" value="3" v-model="nightOwlOptions" class="radio-input" @change="calculateNightOwl">
              <label for="nightOwl-3" class="radio-label">中度夜猫子</label>
            </div>
            <div class="checkbox-item">
              <input type="checkbox" id="nightOwl-4" value="4" v-model="nightOwlOptions" class="radio-input" @change="calculateNightOwl">
              <label for="nightOwl-4" class="radio-label">重度夜猫子</label>
            </div>
          </div>
          <!-- <div class="filter-value-info">当前组合值: {{ nightOwl }} (0-15)</div> -->
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="main-content">
        <h1 class="page-title">用户推荐系统</h1>

    <!-- 推荐API选择区域 -->
    <div class="recommend-api-section">
      <h2 class="section-title">推荐</h2>

      <!-- 推荐API选择按钮 -->
      <div class="recommend-api-buttons">
        <button
        v-for="recommendApi in recommendApis"
        :key="recommendApi.key"
        class="recommend-api-button"
        :class="{ active: selectedRecommendApi.includes(recommendApi.key) }"
        @click="toggleRecommendApiSelection(recommendApi.key)"
      >
  {{ recommendApi.name }}
</button>
      </div>
    </div>

    <!-- 筛选API选择区域 -->
    <div class="filter-api-section">
      <h2 class="section-title">筛选条件</h2>

      <!-- 筛选API选择按钮 -->
      <div class="filter-api-buttons">
        <button
          v-for="filterApi in filterApis"
          :key="filterApi.key"
          class="filter-api-button"
          :class="{ active: selectedFilterApis.filter(api => api.key === filterApi.key).length > 0 }"
          @click="toggleFilterApiSelection(filterApi.key)"
        >
          {{ filterApi.name }}
        </button>
      </div>

      <!-- 筛选条件输入区域 -->
      <div v-if="selectedFilterApis.length > 0" class="filter-conditions">
        <div class="filter-tip">提示：选择多个筛选条件可以组合使用，更精准地找到符合要求的用户</div>

        <!-- 遍历每个选中的筛选API实例 -->
        <div
          v-for="(filterApi, index) in selectedFilterApis"
          :key="filterApi.id"
          class="filter-condition"
        >
          <!-- 筛选条件标题、编号和删除按钮 -->
          <div class="filter-condition-header">
            <div class="filter-condition-title-row">
              <label>{{ getFilterApiName(filterApi.key) }} ({{ index + 1 }})</label>
              <button
                @click="removeFilterApi(filterApi.id)"
                class="remove-filter-btn"
                title="删除此筛选条件"
              >
                ×
              </button>
            </div>
            <div class="filter-condition-desc">{{ getFilterApiDesc(filterApi.key) }}</div>
          </div>

          <!-- 通用搜索输入区域 -->
          <template v-if="['chainSameUp', 'chainSameUpVideoCount', 'chainFollowTime'].includes(filterApi.key)">
            <div class="search-input-group">
              <input
                type="text"
                v-model="filterApi.searchKeywords.up"
                @input="debounceFilterSearch(filterApi.id, 'up')"
                placeholder="搜索UP主"
                class="search-input"
                title="输入UP主名称进行模糊搜索"
              >
              <div v-if="filterApi.searchResults.up.length > 0" class="search-results">
                <div
                  v-for="up in filterApi.searchResults.up"
                  :key="up.userId"
                  class="search-result-item"
                  @click="selectFilterUp(up, filterApi.id)"
                >
                  {{ up.username }}
                </div>
              </div>
            </div>
            <div v-if="filterFormData[filterApi.id].upName" class="selected-item">
              {{ filterFormData[filterApi.id].upName }}
              <button @click="removeFilterUp(filterApi.id)" class="remove-btn" title="移除选中的UP主">×</button>
            </div>
          </template>

          <template v-else-if="['chainSameTag', 'chainSameTagVideoCount'].includes(filterApi.key)">
            <div class="search-input-group">
              <input
                type="text"
                v-model="filterApi.searchKeywords.tag"
                @input="debounceFilterSearch(filterApi.id, 'tag')"
                placeholder="搜索标签"
                class="search-input"
                title="输入标签名称进行模糊搜索"
              >
              <div v-if="filterApi.searchResults.tag.length > 0" class="search-results">
                <div
                  v-for="tag in filterApi.searchResults.tag"
                  :key="tag.tagId"
                  class="search-result-item"
                  @click="selectFilterTag(tag, filterApi.id)"
                >
                  {{ tag.tagName }}
                </div>
              </div>
            </div>
            <div v-if="filterFormData[filterApi.id].tagName" class="selected-item">
              {{ filterFormData[filterApi.id].tagName }}
              <button @click="removeFilterTag(filterApi.id)" class="remove-btn" title="移除选中的标签">×</button>
            </div>
          </template>

          <template v-else-if="['chainDeepVideo'].includes(filterApi.key)">
            <div class="search-input-group">
              <input
                type="text"
                v-model="filterApi.searchKeywords.video"
                @input="debounceFilterSearch(filterApi.id, 'video')"
                placeholder="搜索视频"
                class="search-input"
                title="输入视频标题进行模糊搜索"
              >
              <div v-if="filterApi.searchResults.video.length > 0" class="search-results">
                <div
                  v-for="video in filterApi.searchResults.video"
                  :key="video.videoId"
                  class="search-result-item"
                  @click="selectFilterVideo(video, filterApi.id)"
                >
                  {{ video.title }}
                </div>
              </div>
            </div>
            <div v-if="filterFormData[filterApi.id].videoTitle" class="selected-item">
              {{ filterFormData[filterApi.id].videoTitle }}
              <button @click="removeFilterVideo(filterApi.id)" class="remove-btn" title="移除选中的视频">×</button>
            </div>
          </template>

          <!-- 滑块区域 -->
          <template v-if="['chainSameUpVideoCount', 'chainSameTagVideoCount'].includes(filterApi.key)">
            <div class="slider-container">
              <label for="ratioOption" class="slider-label">观看比例:</label>
              <input
                type="range"
                v-model="filterFormData[filterApi.id].ratioOption"
                min="0"
                max="4"
                step="1"
                class="slider"
                title="拖动滑块选择观看比例"
              >
              <div class="slider-value">{{ formatRatioOption(filterFormData[filterApi.id].ratioOption) }}</div>
            </div>
          </template>

          <template v-if="['chainSameUp', 'chainSameTag'].includes(filterApi.key)">
            <div class="slider-container">
              <label for="durationOption" class="slider-label">观看时长:</label>
              <input
                type="range"
                v-model="filterFormData[filterApi.id].durationOption"
                min="-1"
                max="4"
                step="1"
                class="slider"
                title="拖动滑块选择观看时长"
              >
              <div class="slider-value">{{ formatDurationOption(filterFormData[filterApi.id].durationOption) }}</div>
            </div>
          </template>

          <template v-if="['chainDeepVideo'].includes(filterApi.key)">
            <div class="slider-container">
              <label for="deepVideoOption" class="slider-label">观看深度:</label>
              <input
                type="range"
                v-model="filterFormData[filterApi.id].option"
                min="0"
                max="1"
                step="1"
                class="slider"
                title="拖动滑块选择观看深度"
              >
              <div class="slider-value">{{ formatDeepVideoOption(filterFormData[filterApi.id].option) }}</div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 按钮区域 -->
    <div class="action-buttons">
  <button class="get-users-btn" @click="fetchUsers" :disabled="loading">
    {{ loading ? '获取中...' : '获取用户' }}
  </button>
</div>

    <!-- 推荐结果展示区域 -->
    <div v-if="recommendations.length > 0" class="results-section">
      <h2 class="results-title">推荐结果 ({{ recommendations.length }})</h2>

      <div class="user-grid">
        <div
          v-for="user in recommendations"
          :key="user.userId"
          class="user-card"
          @mouseenter="showTooltip(user, $event)"
          @mouseleave="hideTooltip"
        >
          <div class="user-avatar">
            <img :src="getUserAvatar(user.avatarPath, user.userId)" alt="用户头像">
          </div>
          <div class="user-info">
            <h3 class="username">{{ user.username }}</h3>
            <button
              class="add-friend-btn"
              @click.stop="sendFriendRequest(user.userId, $event)"
              :disabled="addingFriendId === user.userId"
            >
              {{ addingFriendId === user.userId ? '添加中...' : '添加好友' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && (selectedRecommendApi || selectedFilterApis.length > 0)" class="empty-state">
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
        <!-- 显示定制化提示文本 -->
        <div v-if="getCustomTooltipText(tooltipUser)" class="custom-tooltip-text" v-html="getCustomTooltipText(tooltipUser)">
        </div>
        <!-- 显示所有详细信息 -->
      </div>
    </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
// 移除不存在的store导入
// import { useRecommendStore } from '@/store/recommend'
// import { useUserStore } from '@/store/user'
import { recommendApi, friendApi, searchApi, filterApi } from '@/services/api'
import { getUserAvatar, generateRandomAvatar } from '../utils/avatar';

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
const selectedRecommendApi = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('info')
const tooltipVisible = ref(false)
const tooltipUser = ref(null)
const tooltipStyle = ref({})
const addingFriendId = ref(null)
// 好友列表
const friendsList = ref([])
const friendsLoading = ref(false)

// 新增的推荐和选中用户数据
const recommendations = ref([])
const selectedUsers = ref([])

// 筛选API相关数据
const filterApis = ref([
  { key: 'chainSameUp', name: '同一UP主筛选' },
  { key: 'chainSameTag', name: '同一标签筛选' },
  { key: 'chainSameUpVideoCount', name: 'UP主视频观看比例' },
  { key: 'chainSameTagVideoCount', name: '标签视频观看比例' },
  { key: 'chainFollowTime', name: '关注时间缘分' },
  { key: 'chainDeepVideo', name: '深度视频筛选' },
])
// 修改为对象数组，支持重复选择同一API，每个对象包含key和唯一标识
const selectedFilterApis = ref([])
// 生成唯一ID的工具函数
const generateId = () => `filter_${Date.now()}_${Math.floor(Math.random() * 1000)}`
// 每个筛选实例将有自己的搜索状态和表单数据，不再使用全局搜索状态和按API类型存储的表单数据
const filterFormData = ref({})

// 搜索相关数据
const searchKeywords = ref({ up: '', tag: '', video: '', series: '' })
const upSearchResults = ref([])
const tagSearchResults = ref([])
const videoSearchResults = ref([])
const seriesSearchResults = ref([])
const searchTimeout = ref(null)

// 选中的筛选条件
const selectedUps = ref([])
const selectedTags = ref([])
const selectedVideos = ref([])
const selectedSeries = ref([])

// 侧边栏筛选条件（轮轴控制）
const activity = ref(0) // 初始值设为0，表示不筛选
const nightOwl = ref(0) // 初始值设为0，表示不筛选

// 存储多选选项
const activityOptions = ref([]) // 存储用户活跃度多选选项
const nightOwlOptions = ref([]) // 存储夜猫子指数多选选项

// 计算用户活跃度组合值
function calculateActivity() {
  // 转换为数字数组
  const options = activityOptions.value.map(Number).sort()

  // 如果没有选择任何选项，默认不筛选
  if (options.length === 0) {
    activity.value = 0
    return
  }

  // 组合计算规则
  if (options.length === 1) {
    // 单个选项，直接使用对应的值
    activity.value = options[0]
  } else if (options.length === 2) {
    // 两个选项的组合
    if (options.includes(1) && options.includes(2)) {
      activity.value = 4 // 轻度或中度
    } else if (options.includes(2) && options.includes(3)) {
      activity.value = 5 // 中度或重度
    } else if (options.includes(1) && options.includes(3)) {
      activity.value = 6 // 轻度或重度
    }
  } else if (options.length === 3) {
    // 三个选项都选了，对应全部活跃用户
    activity.value = 7
  }
}

// 计算夜猫子指数组合值
function calculateNightOwl() {
  // 转换为数字数组
  const options = nightOwlOptions.value.map(Number).sort()

  // 如果没有选择任何选项，默认不筛选
  if (options.length === 0) {
    nightOwl.value = 0
    return
  }

  // 组合计算规则
  if (options.length === 1) {
    // 单个选项，直接使用对应的值
    nightOwl.value = options[0]
  } else if (options.length >= 2 && options.length <= 4) {
    // 选择了多个时间段，对应全部夜猫子类型
    nightOwl.value = 15
  }
}

// 二次筛选结果
const secondaryFilterResults = ref([])
const secondaryFilterApplied = ref(false)

// 获取好友列表
async function fetchFriendsList() {
  try {
    friendsLoading.value = true
    const response = await friendApi.getFriends()
    // 处理不同的响应格式
    if (response.success !== undefined) {
      friendsList.value = response.success ? (response.data || []) : []
    } else if (Array.isArray(response)) {
      friendsList.value = response
    } else {
      friendsList.value = []
    }
  } catch (error) {
    console.error('获取好友列表失败:', error)
    friendsList.value = []
  } finally {
    friendsLoading.value = false
  }
}

// 在组件挂载时获取好友列表
onMounted(() => {
  fetchFriendsList()
})

// 头像生成函数已从utils/avatar.js导入

// 格式化比例选项
// 格式化比例选项
function formatRatioOption(value) {
  const ratioMap = {
    0: '0-20%',
    1: '20-40%',
    2: '40-60%',
    3: '60-80%',
    4: '80-100%'
  }
  return ratioMap[value] || '40-60%'
}

// 格式化时长选项
function formatDurationOption(value) {
  const durationMap = {
    '-1': '不限制时长',
    0: '0-1小时',
    1: '1-3小时',
    2: '3-10小时',
    3: '10-30小时',
    4: '30小时以上'
  }
  return durationMap[value] || '不限制时长'
}

// 格式化深度视频选项
function formatDeepVideoOption(value) {
  const optionMap = {
    0: '深度观看(≥5次或≥2倍时长)',
    1: '极其深度观看(≥10次或≥5倍时长)'
  }
  return optionMap[value] || '深度观看'
}

// API配置
const recommendApis = [
  { key: 'coComment', name: '同视频评论推荐', endpoint: '/api/chain/co-comment/' },
  { key: 'reply', name: '评论回复推荐', endpoint: '/api/chain/reply/' },
  { key: 'sharedVideo', name: '视频相似度推荐', endpoint: '/api/chain/shared-video/' },
  { key: 'category', name: '分区重合度推荐', endpoint: '/api/chain/category/' },
  { key: 'theme', name: '内容类型重合度推荐', endpoint: '/api/chain/theme/' },
  { key: 'userBehavior', name: '用户行为相似度推荐', endpoint: '/api/chain/user-behavior/' },
  { key: 'commonUp', name: '共同关注UP主推荐', endpoint: '/api/chain/common-up/' },
  { key: 'favoriteSimilarity', name: '收藏夹相似度推荐', endpoint: '/api/chain/favorite-similarity/' },
  { key: 'commentFriends', name: '通过评论推荐好友', endpoint: '/api/chain/comment-friends/' }
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
  let intersection = userIdSets.reduce((a, b) => {
    return new Set([...a].filter(x => b.has(x)))
  })

  // 如果应用了二次筛选，取与二次筛选结果的交集
  if (secondaryFilterApplied.value && secondaryFilterResults.value.length > 0) {
    const secondaryFilterUserIdSet = new Set(secondaryFilterResults.value.map(user => user.userId))
    intersection = new Set([...intersection].filter(id => secondaryFilterUserIdSet.has(id)))
  }

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

// 选择推荐API
function toggleRecommendApiSelection(apiKey) {
  const index = selectedRecommendApi.value.indexOf(apiKey)
  if (index > -1) {
    selectedRecommendApi.value.splice(index, 1)
  } else {
    selectedRecommendApi.value.push(apiKey)
  }
}

// 切换筛选API选择
function toggleFilterApiSelection(filterApiKey) {
  // 生成唯一ID
  const id = generateId()

  // 重复点击时添加新的筛选实例，而不是切换
  selectedFilterApis.value.push({
    id: id,
    key: filterApiKey,
    // 为每个筛选实例添加独立的搜索状态
    searchKeywords: {
      up: '',
      tag: '',
      video: '',
      series: ''
    },
    searchResults: {
      up: [],
      tag: [],
      video: [],
      series: []
    }
  })

  // 为新创建的筛选实例添加默认表单数据
  switch (filterApiKey) {
    case 'chainSameUp':
      filterFormData.value[id] = {
        upId: '',
        upName: '',
        durationOption: -1 // -1: 不限制时长, 0: 0-1小时, 1: 1-3小时, 2: 3-10小时, 3: 10-30小时, 4: 30小时以上
      }
      break
    case 'chainSameTag':
      filterFormData.value[id] = {
        tagId: '',
        tagName: '',
        durationOption: -1 // -1: 不限制时长, 0: 0-1小时, 1: 1-3小时, 2: 3-10小时, 3: 10-30小时, 4: 30小时以上
      }
      break
    case 'chainSameUpVideoCount':
      filterFormData.value[id] = {
        upId: '',
        upName: '',
        ratioOption: 2, // 0: 0-20%, 1: 20-40%, 2: 40-60%, 3: 60-80%, 4: 80-100%
        ratioValue: 2
      }
      break
    case 'chainSameTagVideoCount':
      filterFormData.value[id] = {
        tagId: '',
        tagName: '',
        ratioOption: 2, // 0: 0-20%, 1: 20-40%, 2: 40-60%, 3: 60-80%, 4: 80-100%
        ratioValue: 2
      }
      break
    case 'chainFollowTime':
      filterFormData.value[id] = {
        upId: '',
        upName: ''
      }
      break
    case 'chainDeepVideo':
      filterFormData.value[id] = {
        videoId: '',
        videoTitle: '',
        option: 0, // 0: 深度观看(≥5次或≥2倍时长), 1: 极其深度观看(≥10次或≥5倍时长)
        ratioValue: 0
      }
      break
    case 'chainSeries':
      filterFormData.value[id] = {
        tagId: '',
        tagName: ''
      }
      break
    default:
      filterFormData.value[id] = {}
  }
}

// 筛选搜索防抖
const filterSearchDebounce = {} // 使用对象存储每个筛选实例的防抖定时器

// 修改debounceFilterSearch函数，接收筛选实例ID作为参数
function debounceFilterSearch(filterId, type) {
  // 为每个筛选实例和搜索类型创建唯一的防抖键
  const debounceKey = `${filterId}_${type}`

  if (filterSearchDebounce[debounceKey]) {
    clearTimeout(filterSearchDebounce[debounceKey])
  }

  filterSearchDebounce[debounceKey] = setTimeout(async () => {
    // 找到对应的筛选实例
    const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
    if (!filterInstance) return

    const keyword = filterInstance.searchKeywords[type]
    if (!keyword) {
      filterInstance.searchResults[type] = []
      return
    }

    try {
      let results = []
      if (type === 'up') {
        const response = await searchApi.searchUsers(keyword)
        results = response.data || []
      } else if (type === 'tag') {
        const response = await searchApi.searchTags(keyword)
        results = response.data || []
      } else if (type === 'video') {
        const response = await searchApi.searchVideos(keyword)
        results = response.data || []
      } else if (type === 'series') {
        // 注意：searchApi中没有定义searchSeries，使用searchTags替代
        const response = await searchApi.searchTags(keyword)
        results = response.data || []
      }

      // 更新搜索结果到对应的筛选实例
      filterInstance.searchResults[type] = results
    } catch (error) {
      console.error(`搜索${type}失败:`, error)
    }
  }, 300)
}

// 选择筛选UP主
function selectFilterUp(up, filterId, isAutoFill = false) {
  // 处理用户对象和up主对象的兼容性，优先使用upId/upName，其次使用userId/username
  const upId = up.upId || up.userId
  const upName = up.upName || up.username

  // 只更新当前筛选实例对应的UP主筛选条件
  filterFormData.value[filterId].upId = upId
  filterFormData.value[filterId].upName = upName

  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 更新当前筛选实例搜索框的值，让用户看到选中的内容
  filterInstance.searchKeywords.up = upName

  // 只有在手动选择时才清空当前筛选实例的搜索结果数组
  if (!isAutoFill) {
    filterInstance.searchResults.up = []
  }
}

// 选择筛选标签
function selectFilterTag(tag, filterId, isAutoFill = false) {
  // 只更新当前筛选实例对应的标签筛选条件
  filterFormData.value[filterId].tagId = tag.tagId
  filterFormData.value[filterId].tagName = tag.tagName

  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 更新当前筛选实例搜索框的值，让用户看到选中的内容
  filterInstance.searchKeywords.tag = tag.tagName

  // 只有在手动选择时才清空当前筛选实例的搜索结果数组
  if (!isAutoFill) {
    filterInstance.searchResults.tag = []
  }
}

// 选择筛选视频
function selectFilterVideo(video, filterId, isAutoFill = false) {
  // 只更新当前筛选实例对应的视频筛选条件
  filterFormData.value[filterId].videoId = video.videoId
  filterFormData.value[filterId].videoTitle = video.title

  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 更新当前筛选实例搜索框的值，让用户看到选中的内容
  filterInstance.searchKeywords.video = video.title

  // 只有在手动选择时才清空当前筛选实例的搜索结果数组
  if (!isAutoFill) {
    filterInstance.searchResults.video = []
  }
}

// 选择筛选系列
function selectFilterSeries(series, filterId, isAutoFill = false) {
  // 只更新当前筛选实例对应的系列筛选条件
  filterFormData.value[filterId].tagId = series.tagId
  filterFormData.value[filterId].tagName = series.tagName

  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 更新当前筛选实例搜索框的值，让用户看到选中的内容
  filterInstance.searchKeywords.series = series.tagName

  // 只有在手动选择时才清空当前筛选实例的搜索结果数组
  if (!isAutoFill) {
    filterInstance.searchResults.series = []
  }
}

// 移除筛选UP主
function removeFilterUp(filterId) {
  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 只清除当前筛选实例对应的UP主筛选条件
  filterFormData.value[filterInstance.key].upId = ''
  filterFormData.value[filterInstance.key].upName = ''

  // 清除当前筛选实例搜索框的值和搜索结果
  filterInstance.searchKeywords.up = ''
  filterInstance.searchResults.up = []
}

// 移除筛选标签
function removeFilterTag(filterId) {
  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 只清除当前筛选实例对应的标签筛选条件
  filterFormData.value[filterInstance.key].tagId = ''
  filterFormData.value[filterInstance.key].tagName = ''

  // 清除当前筛选实例搜索框的值和搜索结果
  filterInstance.searchKeywords.tag = ''
  filterInstance.searchResults.tag = []
}

// 移除筛选视频
function removeFilterVideo(filterId) {
  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 只清除当前筛选实例对应的视频筛选条件
  filterFormData.value[filterInstance.key].videoId = ''
  filterFormData.value[filterInstance.key].videoTitle = ''

  // 清除当前筛选实例搜索框的值和搜索结果
  filterInstance.searchKeywords.video = ''
  filterInstance.searchResults.video = []
}

// 移除筛选系列
function removeFilterSeries(filterId) {
  // 找到对应的筛选实例
  const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
  if (!filterInstance) return

  // 只清除当前筛选实例对应的系列筛选条件
  filterFormData.value[filterInstance.key].tagId = ''
  filterFormData.value[filterInstance.key].tagName = ''

  // 清除当前筛选实例搜索框的值和搜索结果
  filterInstance.searchKeywords.series = ''
  filterInstance.searchResults.series = []
}

// 删除筛选API实例
function removeFilterApi(filterId) {
  const index = selectedFilterApis.value.findIndex(api => api.id === filterId)
  if (index > -1) {
    selectedFilterApis.value.splice(index, 1)
    // 删除对应的表单数据
    delete filterFormData.value[filterId]
  }
}

// 获取筛选API名称
function getFilterApiName(filterKey) {
  const api = filterApis.value.find(api => api.key === filterKey)
  return api ? api.name : filterKey
}

// 获取筛选API描述
function getFilterApiDesc(filterKey) {
  const descMap = {
    'chainSameUp': '筛选关注同一UP主的用户',
    'chainSameTag': '筛选关注同一标签的用户',
    'chainSameUpVideoCount': '筛选和您观看同一UP主视频比例相近的用户',
    'chainSameTagVideoCount': '筛选和您观看同一标签视频比例相近的用户',
    'chainFollowTime': '筛选在相近时间关注同一UP主的用户',
    'chainDeepVideo': '筛选和您观看同一视频深度相近的用户',
    'chainSeries': '筛选和您观看同一系列作品的用户'
  }
  return descMap[filterKey] || ''
}

// 应用筛选 - 处理单个筛选API实例
async function applyFilter(filterId) {
  try {
    // 找到对应的筛选实例
    const filterInstance = selectedFilterApis.value.find(api => api.id === filterId)
    if (!filterInstance) return []

    const filterApiKey = filterInstance.key
    const formData = filterFormData.value[filterId]

    let response

    switch (filterApiKey) {
      case 'chainDeepVideo':
        if (formData.videoId) {
          response = await filterApi.chainDeepVideo({
            userId: getCurrentUserId(),
            videoId: formData.videoId,
            option: parseInt(formData.option),
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个视频', 'warning')
          return []
        }
        break
      case 'chainSameUp':
        if (formData.upId) {
          response = await filterApi.chainSameUp({
            userId: getCurrentUserId(),
            upId: formData.upId,
            durationOption: formData.durationOption,
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个UP主', 'warning')
          return []
        }
        break
      case 'chainSameTag':
        if (formData.tagId) {
          response = await filterApi.chainSameTag({
            userId: getCurrentUserId(),
            tagId: formData.tagId,
            durationOption: formData.durationOption,
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个标签', 'warning')
          return []
        }
        break
      case 'chainFollowTime':
        if (formData.upId) {
          response = await filterApi.chainFollowTime({
            userId: getCurrentUserId(),
            upId: formData.upId,
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个UP主', 'warning')
          return []
        }
        break
      case 'chainSameUpVideoCount':
        if (formData.upId) {
          response = await filterApi.chainSameUpVideoCount({
            userId: getCurrentUserId(),
            upId: formData.upId,
            ratioOption: parseInt(formData.ratioOption),
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个UP主', 'warning')
          return []
        }
        break
      case 'chainSameTagVideoCount':
        if (formData.tagId) {
          response = await filterApi.chainSameTagVideoCount({
            userId: getCurrentUserId(),
            tagId: formData.tagId,
            ratioOption: parseInt(formData.ratioOption),
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个标签', 'warning')
          return []
        }
        break
      case 'chainSeries':
        if (formData.tagId) {
          response = await filterApi.chainSeries({
            userId: getCurrentUserId(),
            tagId: formData.tagId,
            activity: activity.value,
            nightOwl: nightOwl.value
          })
        } else {
          showMessage('请先选择一个系列标签', 'warning')
          return []
        }
        break
      default:
        showMessage('不支持的筛选条件', 'error')
        return []
    }

    // 处理响应结果
    // 检查响应是否为数组（直接返回数据的情况）
    if (Array.isArray(response)) {
      return response
    // 检查响应是否包含success属性（标准格式的情况）
    } else if (response.success) {
      return response.data || []
    } else {
      showMessage(response.message || '筛选失败', 'error')
      return []
    }
  } catch (error) {
    console.error('筛选失败:', error)
    showMessage('筛选失败，请稍后重试', 'error')
    return []
  }
}

// 搜索防抖函数
function debounceSearch(type) {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  searchTimeout.value = setTimeout(async () => {
    const keyword = searchKeywords.value[type]
    if (keyword.trim() === '') {
      type === 'up' ? upSearchResults.value = [] :
      type === 'tag' ? tagSearchResults.value = [] :
      type === 'video' ? videoSearchResults.value = [] :
      seriesSearchResults.value = []
      return
    }

    try {
      if (type === 'up') {
        const results = await searchApi.searchUps(keyword)
        upSearchResults.value = results.data || []
      } else if (type === 'tag') {
        const results = await searchApi.searchTags(keyword)
        tagSearchResults.value = results.data || []
      } else if (type === 'video') {
        const results = await searchApi.searchVideos(keyword)
        videoSearchResults.value = results.data || []
      } else if (type === 'series') {
        const results = await searchApi.searchTags(keyword)
        seriesSearchResults.value = results.data || []
      }
    } catch (error) {
      console.error(`搜索${type}失败:`, error)
      showMessage(`搜索${type}失败，请稍后重试`, 'error')
    }
  }, 500)
}

// 选择UP主
function selectUp(up) {
  if (!selectedUps.value.some(u => u.upId === up.upId)) {
    selectedUps.value.push(up)
  }
  searchKeywords.value.up = ''
  upSearchResults.value = []
}

// 移除UP主
function removeUp(upId) {
  selectedUps.value = selectedUps.value.filter(up => up.upId !== upId)
}

// 选择标签
function selectTag(tag) {
  if (!selectedTags.value.some(t => t.tagId === tag.tagId)) {
    selectedTags.value.push(tag)
  }
  searchKeywords.value.tag = ''
  tagSearchResults.value = []
}

// 移除标签
function removeTag(tagId) {
  selectedTags.value = selectedTags.value.filter(tag => tag.tagId !== tagId)
}

// 选择视频
function selectVideo(video) {
  if (!selectedVideos.value.some(v => v.videoId === video.videoId)) {
    selectedVideos.value.push(video)
  }
  searchKeywords.value.video = ''
  videoSearchResults.value = []
}

// 移除视频
function removeVideo(videoId) {
  selectedVideos.value = selectedVideos.value.filter(video => video.videoId !== videoId)
}

// 选择系列标签
function selectSeries(tag) {
  if (!selectedSeries.value.some(t => t.tagId === tag.tagId)) {
    selectedSeries.value.push(tag)
  }
  searchKeywords.value.series = ''
  seriesSearchResults.value = []
}

// 移除系列标签
function removeSeries(tagId) {
  selectedSeries.value = selectedSeries.value.filter(tag => tag.tagId !== tagId)
}

// 获取责任链筛选参数
function getChainParams() {
  const params = []

  // UP主视频观看比例
  if (selectedUps.value.length > 0) {
    const upParams = {
      type: 'upWatchRatio',
      params: {
        upIds: selectedUps.value.map(up => up.upId)
      }
    }
    params.push(upParams)
  }

  // 标签视频观看比例
  if (selectedTags.value.length > 0) {
    const tagParams = {
      type: 'tagWatchRatio',
      params: {
        tagIds: selectedTags.value.map(tag => tag.tagId)
      }
    }
    params.push(tagParams)
  }

  // 深度视频
  if (selectedVideos.value.length > 0) {
    const videoParams = {
      type: 'deepVideo',
      params: {
        videoIds: selectedVideos.value.map(video => video.videoId)
      }
    }
    params.push(videoParams)
  }

  // 系列作品
  if (selectedSeries.value.length > 0) {
    const seriesParams = {
      type: 'seriesWorks',
      params: {
        tagIds: selectedSeries.value.map(series => series.tagId)
      }
    }
    params.push(seriesParams)
  }

  return params
}

// 应用二次筛选
async function applySecondaryFilter() {
  if (selectedUps.value.length === 0 && selectedTags.value.length === 0 &&
      selectedVideos.value.length === 0 && selectedSeries.value.length === 0) {
    showMessage('请至少选择一个筛选条件', 'warning')
    return
  }

  loading.value = true

  try {
    // 目前二次筛选功能暂时不可用，因为后端API尚未实现
    showMessage('二次筛选功能正在开发中', 'info')

    // 为了演示，我们可以显示一个空结果
    secondaryFilterResults.value = []
    secondaryFilterApplied.value = true
  } catch (error) {
    console.error('二次筛选失败:', error)
    showMessage('二次筛选失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 重置二次筛选条件
function resetSecondaryFilter() {
  // 重置搜索
  searchKeywords.value = { up: '', tag: '', video: '', series: '' }
  upSearchResults.value = []
  tagSearchResults.value = []
  videoSearchResults.value = []
  seriesSearchResults.value = []

  // 重置选中的筛选条件
  selectedUps.value = []
  selectedTags.value = []
  selectedVideos.value = []
  selectedSeries.value = []

  // 重置二次筛选结果
  secondaryFilterResults.value = []
  secondaryFilterApplied.value = false

  showMessage('二次筛选条件已重置', 'info')
}

// 获取推荐
// 获取用户（合并筛选和推荐功能）
// 获取用户（合并筛选和推荐功能）
async function fetchUsers() {
  // 验证至少选择了一个API
  if (selectedRecommendApi.value.length === 0 && selectedFilterApis.value.length === 0) {
    showMessage('请至少选择一个推荐或筛选API', 'warning')
    return
  }

  loading.value = true
  message.value = '正在获取用户...'

  try {
    // 构建请求参数
    const params = {
      userId: getCurrentUserId(),
      activity: activity.value,
      nightOwl: nightOwl.value
    }

    // 收集所有API的结果
    const apiResultsByType = new Map() // 按API类型分组的结果
    const allUserDetails = {}

    // 调用所有选中的推荐API
    for (const apiKey of selectedRecommendApi.value) {
      const response = await recommendApi[apiKey](params)
      const userIds = extractUserIdsFromResponse(response)

      // 推荐API按key分组
      if (!apiResultsByType.has(apiKey)) {
        apiResultsByType.set(apiKey, new Set())
      }
      const userSet = apiResultsByType.get(apiKey)
      userIds.forEach(id => userSet.add(id))

      extractUserDetailsFromResponse(response, allUserDetails, apiKey)
    }

    // 调用所有选中的筛选API实例
    for (const filterApiItem of selectedFilterApis.value) {
      const filterId = filterApiItem.id
      const filterApiKey = filterApiItem.key
      const filteredResults = await applyFilter(filterId)

      if (filteredResults && filteredResults.length > 0) {
        // 提取筛选结果中的用户ID
        const filteredUserIds = filteredResults.map(user => {
          if (user && user.recommendedUser) {
            return user.recommendedUser.userid || user.recommendedUser.userId
          }
          return user.userid || user.userId
        }).filter(Boolean)

        // 筛选API按key分组，同一类型的结果取并集
        if (!apiResultsByType.has(filterApiKey)) {
          apiResultsByType.set(filterApiKey, new Set())
        }
        const userSet = apiResultsByType.get(filterApiKey)
        filteredUserIds.forEach(id => userSet.add(id))

        // 提取筛选结果的用户详细信息，使用filterApiKey作为API来源
        filteredResults.forEach(user => {
          let processedUser = { ...user }

          // 如果存在recommendedUser属性，合并其信息
          if (user && user.recommendedUser) {
            Object.keys(user.recommendedUser).forEach(key => {
              const normalizedKey = key === 'userid' ? 'userId' : key
              processedUser[normalizedKey] = user.recommendedUser[key]
            })
          }

          // 存储用户信息，合并已存在的用户数据
          const userId = processedUser.userId || processedUser.userid
          if (userId) {
            if (allUserDetails[userId]) {
              // 如果用户已经存在，合并数据，但保留之前的数据
              const existingUser = allUserDetails[userId]
              allUserDetails[userId] = {
                ...existingUser,
                ...processedUser,
                // 保留已有的sourceApis，如果没有则初始化
                sourceApis: existingUser.sourceApis || []
              }
            } else {
              // 如果用户不存在，创建新的用户对象，并初始化sourceApis
              allUserDetails[userId] = {
                ...processedUser,
                sourceApis: []
              }
            }

            // 将当前筛选API添加到sourceApis中，避免重复
            const user = allUserDetails[userId]
            if (user.sourceApis && !user.sourceApis.includes(filterApiKey)) {
              user.sourceApis.push(filterApiKey)
            }
          }
        })
      }
    }

    // 计算不同API类型之间的交集
    let finalUserIds = []
    const resultSets = Array.from(apiResultsByType.values())

    if (resultSets.length > 0) {
      // 初始化为第一个API类型的用户ID数组
      finalUserIds = Array.from(resultSets[0])

      // 计算所有API类型结果的交集
      for (let i = 1; i < resultSets.length; i++) {
        const currentSet = resultSets[i]
        finalUserIds = finalUserIds.filter(id => currentSet.has(id))
      }
    }

    // 根据交集的用户ID获取完整的用户信息
    const finalUsers = finalUserIds.map(userId => allUserDetails[userId]).filter(Boolean)

    // 过滤掉已经是好友的用户
    const friendUserIds = new Set(friendsList.value.map(friend => friend.userId || friend.userid))
    const filteredUsers = finalUsers.filter(user => {
      const userId = user.userId || user.userid
      return !friendUserIds.has(userId)
    })

    // 更新推荐结果
    recommendations.value = filteredUsers

    showMessage(`成功获取${filteredUsers.length}个用户`, 'success')
  } catch (error) {
    console.error('获取用户失败:', error)
    showMessage('获取用户失败', 'error')
  } finally {
    loading.value = false
  }
}
// 从API响应中提取用户ID
function extractUserIdsFromResponse(response) {
  let userData = []

  // 处理不同的响应格式
  if (response.success !== undefined) {
    if (response.success) {
      userData = response.data || []
      if (!Array.isArray(userData) && userData.users) {
        userData = userData.users
      }
    }
  } else if (Array.isArray(response)) {
    userData = response
  }

  // 提取用户ID
  return userData.map(user => {
    if (user && user.recommendedUser) {
      return user.recommendedUser.userid || user.recommendedUser.userId
    }
    return user.userid || user.userId
  }).filter(Boolean)
}

// 从API响应中提取用户详细信息
function extractUserDetailsFromResponse(response, userDetailsMap, apiKey) {
  let userData = []

  // 处理不同的响应格式
  if (response.success !== undefined) {
    if (response.success) {
      userData = response.data || []
      if (!Array.isArray(userData) && userData.users) {
        userData = userData.users
      }
    }
  } else if (Array.isArray(response)) {
    userData = response
  }

  // 提取用户详细信息
  userData.forEach(user => {
    let processedUser = { ...user }

    // 如果存在recommendedUser属性，合并其信息
    if (user && user.recommendedUser) {
      Object.keys(user.recommendedUser).forEach(key => {
        const normalizedKey = key === 'userid' ? 'userId' : key
        processedUser[normalizedKey] = user.recommendedUser[key]
      })
    }

    // 存储用户信息，合并已存在的用户数据
    const userId = processedUser.userId || processedUser.userid
    if (userId) {
      if (userDetailsMap[userId]) {
        // 如果用户已经存在，合并数据，但保留之前的数据
        const existingUser = userDetailsMap[userId]
        userDetailsMap[userId] = {
          ...existingUser,
          ...processedUser,
          // 保留已有的sourceApis，如果没有则初始化
          sourceApis: existingUser.sourceApis || []
        }
      } else {
        // 如果用户不存在，创建新的用户对象，并初始化sourceApis
        userDetailsMap[userId] = {
          ...processedUser,
          sourceApis: []
        }
      }

      // 将当前API添加到sourceApis中，避免重复
      const user = userDetailsMap[userId]
      if (user.sourceApis && !user.sourceApis.includes(apiKey)) {
        user.sourceApis.push(apiKey)
      }
    }
  })
}

async function fetchRecommendations() {
  if (!selectedRecommendApi.value) {
    showMessage('请选择一个推荐API', 'warning')
    return
  }

  loading.value = true
  message.value = '正在获取推荐用户...'

  try {
    // 构建请求参数，包含用户ID和责任链筛选参数
    const params = {
      userId: getCurrentUserId(),
      activity: activity.value,
      nightOwl: nightOwl.value
    }

    // 调用责任链推荐API
    let response = null
    switch (selectedRecommendApi.value) {
      case 'coComment':
        response = await recommendApi.coComment(params)
        break
      case 'reply':
        response = await recommendApi.reply(params)
        break
      case 'sharedVideo':
        response = await recommendApi.sharedVideo(params)
        break
      case 'category':
        response = await recommendApi.category(params)
        break
      case 'theme':
        response = await recommendApi.theme(params)
        break
      case 'favoriteSimilarity':
        response = await recommendApi.favoriteSimilarity(params)
        break
      case 'commentFriends':
        response = await recommendApi.commentFriends(params)
        break
      case 'userBehavior':
        response = await recommendApi.userBehavior(params)
        break
      case 'commonUp':
        response = await recommendApi.commonUp(params)
        break
      default:
        showMessage('未知的推荐API', 'error')
        return
    }

    // 添加调试信息
    console.log('API响应:', response)
    console.log('当前用户ID:', getCurrentUserId())
    console.log('activity:', activity.value)
    console.log('nightOwl:', nightOwl.value)

    // 检查响应数据类型
    console.log('response.data类型:', typeof response.data)
    console.log('response.data是否为数组:', Array.isArray(response.data))

    // 检查响应结构（处理有success字段和直接返回数组的情况）
    if (response.success !== undefined) {
      if (response.success) {
        console.log('推荐数据:', response.data)

        // 尝试多种方式提取用户数据
        let userData = response.data || []
        if (!Array.isArray(userData) && userData.users) {
          userData = userData.users
          console.log('从response.data.users提取用户数据:', userData)
        }

        // 检查是否存在recommendedUser属性，如果存在则优先使用该对象下的信息
        if (Array.isArray(userData)) {
          userData = userData.map(user => {
            if (user && user.recommendedUser) {
              const newUser = { ...user }
              // 将推荐用户的属性合并到主用户对象中
              Object.keys(user.recommendedUser).forEach(key => {
                // 如果属性名是userid（小写），则转换为userId（大写）以保持一致性
                const normalizedKey = key === 'userid' ? 'userId' : key
                newUser[normalizedKey] = user.recommendedUser[key]
              })
              return newUser
            }
            return user
          })
        }

        recommendations.value = userData || []
      } else {
        console.log('API请求失败:', response.message)
        showMessage(response.message || '获取推荐失败', 'error')
        return
      }
    } else if (Array.isArray(response)) {
      // API直接返回用户数组
      console.log('API直接返回用户数组:', response)
      // 检查是否存在recommendedUser属性，如果存在则优先使用该对象下的信息
      let processedData = response
      if (Array.isArray(processedData)) {
        processedData = processedData.map(user => {
          if (user && user.recommendedUser) {
            const newUser = { ...user }
            // 将推荐用户的属性合并到主用户对象中
            Object.keys(user.recommendedUser).forEach(key => {
              // 如果属性名是userid（小写），则转换为userId（大写）以保持一致性
              const normalizedKey = key === 'userid' ? 'userId' : key
              newUser[normalizedKey] = user.recommendedUser[key]
            })
            return newUser
          }
          return user
        })
      }
      recommendations.value = processedData || []
    } else if (Array.isArray(response.data)) {
      // API返回的是包含data字段的对象，且data是数组
      console.log('API返回data字段为用户数组:', response.data)
      // 检查是否存在recommendedUser属性，如果存在则优先使用该对象下的信息
      let processedData = response.data
      if (Array.isArray(processedData)) {
        processedData = processedData.map(user => {
          if (user && user.recommendedUser) {
            const newUser = { ...user }
            // 将推荐用户的属性合并到主用户对象中
            Object.keys(user.recommendedUser).forEach(key => {
              // 如果属性名是userid（小写），则转换为userId（大写）以保持一致性
              const normalizedKey = key === 'userid' ? 'userId' : key
              newUser[normalizedKey] = user.recommendedUser[key]
            })
            return newUser
          }
          return user
        })
      }
      recommendations.value = processedData || []
    } else {
      console.log('API响应格式未知:', response)
      recommendations.value = []
      showMessage('获取推荐失败，API响应格式未知', 'error')
      return
    }

    console.log('recommendations数组:', recommendations.value)
    console.log('recommendations数组长度:', recommendations.value.length)

    // 检查第一个用户对象的结构
    if (recommendations.value.length > 0) {
      console.log('第一个用户对象:', recommendations.value[0])
      console.log('用户对象属性:', Object.keys(recommendations.value[0]))
    }

    showMessage('推荐获取成功', 'success')
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

// 格式化数字显示的辅助函数
function formatNumber(value, key) {
  if (typeof value !== 'number') return value;

  // 数量类数字（整数）
  if (key.includes('Count') || key.includes('count')) {
    return Math.round(value);
  }

  // 比例或相似度类数字
  if (key.includes('similarity') || key.includes('Similarity') || key.includes('score') || key.includes('Score') || key.includes('rate') || key.includes('Rate')) {
    // 小于等于1的值转换为百分比
    if (value <= 1) {
      return `${(value * 100).toFixed(2)}%`;
    }
    // 大于1的值保留2位小数
    return value.toFixed(2);
  }

  // 其他数字默认保留2位小数
  return value.toFixed(2);
}

// 根据API类型获取定制化的提示文本，支持多推荐条件
function getCustomTooltipText(user) {
  if (!user) return '';

  const tooltipTexts = [];

  // 1. 先展示用户活跃度和夜猫子程度（如果存在）
  // 处理用户活跃度（从API返回的用户属性）
  if (user.activityLevel && user.activityLevel !== 'default') {
    let activityText = '';
    switch (user.activityLevel) {
      case 'light_user':
        activityText = '轻度活跃用户';
        break;
      case 'medium_user':
        activityText = '中度活跃用户';
        break;
      case 'heavy_user':
        activityText = '重度活跃用户';
        break;
      default:
        activityText = '活跃用户';
    }
    tooltipTexts.push(`✨ <span class="highlight">${user.username}</span>是${activityText}，`);
  }

  // 处理夜猫子指数（从API返回的用户属性）
  if (user.nightOwlIndex !== undefined) {
    let nightOwlText = '';
    if (user.nightOwlIndex <= 3) {
      nightOwlText = '非夜猫子用户';
    } else if (user.nightOwlIndex <= 7) {
      nightOwlText = '轻度夜猫子用户';
    } else if (user.nightOwlIndex <= 11) {
      nightOwlText = '中度夜猫子用户';
    } else {
      nightOwlText = '重度夜猫子用户';
    }
    tooltipTexts.push(`同时也是${nightOwlText}，`);
  }

  // 2. 处理所有推荐和筛选来源
  // 获取所有可能的API来源（兼容sourceApis数组）
  const apiKeys = user.sourceApis || [];

  // 兼容单个API来源的情况
  if (typeof user.apiKey === 'string') {
    apiKeys.push(user.apiKey);
  }

  // 处理所有API来源
  apiKeys.forEach(apiKey => {
    let text = '';

    // 推荐类API处理
    switch (apiKey) {
      // 推荐类API
      case 'coComment':
        if (user.videoTitle) {
            // 截断视频标题，避免过长
            const displayTitle = user.videoTitle.length > 20 
                ? user.videoTitle.substring(0, 20) + '...' 
                : user.videoTitle;
                
            text = `<span class="highlight">${user.username}</span>和你在视频<span class="highlight-important">《${displayTitle}》</span>下都发表过评论，对这部作品的看法说不定很相似呢！`;
        } else {
            text = `<span class="highlight">${user.username}</span>和你在同一视频下评论过，快去交流一下观后感吧～`;
        }
        break;

      case 'reply':
          text = `<span class="highlight">${user.username}</span>回复过你的评论，看来TA对你的观点很感兴趣呢！`;
          break;

      case 'commentFriends':
        // 1. 获取视频标题并处理长度
        const videoTitle = user.video?.title || '未知视频';
        const displayVideoTitle = videoTitle.length > 15 ? videoTitle.substring(0, 15) + '...' : videoTitle;

        // 2. 截断评论内容（摘要展示）
        const truncateContent = (content) => {
            if (!content) return '无评论内容';
            return content.length > 40 ? content.substring(0, 40) + '...' : content;
        };

        const mySummary = truncateContent(user.userComment?.content);
        const matchSummary = truncateContent(user.matchedComment?.content);

        // 3. 计算匹配度
        const matchPercent = user.matchScore !== undefined ? (user.matchScore * 100).toFixed(2) + '%' : '0.00%';

        // 4. 构建精简的 HTML 结构
        text = `
            <div style="border-bottom: 1px solid #eee; padding-bottom: 5px; margin-bottom: 5px;">
                <strong style="color: #ff1493;">🎬 关联视频：</strong> ${displayVideoTitle}
            </div>
            <div style="margin-top: 4px;">
                <span style="color: #666;">我的评论:</span> <span style="font-size: 12px;">"${mySummary}"</span>
            </div>
            <div style="margin-top: 4px;">
                <span style="color: #666;">${user.recommendedUser?.username || '对方'}的评论:</span> <span style="font-size: 12px;">"${matchSummary}"</span>
            </div>
            <div style="margin-top: 8px; text-align: right; color: #ff69b4; font-weight: bold;">
                匹配相似度: ${matchPercent}
            </div>
        `;
        break;

      case 'sharedVideo':
        if (user.totalSharedVideos !== undefined) {
          text = `你和<span class="highlight">${user.username}</span>有<span class="highlight-important">${user.totalSharedVideos}个</span>共同观看的视频，兴趣相投的TA说不定能成为你的好朋友哦！`;
        } else if (user.similarityRate !== undefined) {
          text = `你与<span class="highlight">${user.username}</span>观看过的视频相似度高达<span class="highlight-important">${formatNumber(user.similarityRate, 'similarityRate')}</span>，快认识一下这位志同道合的朋友吧～`;
        } else if (user.similarityScore !== undefined) {
          text = `你与<span class="highlight">${user.username}</span>观看过的视频相似度为<span class="highlight-important">${formatNumber(user.similarityScore, 'similarityScore')}</span>，有机会交流一下观影心得呀！`;
        }
        break;

      case 'category':
        if (user.commonCategories) {
          const categories = user.commonCategories.join('、');
          const score = user.categoryMatchScore !== undefined ? mapCategoryScore(user.categoryMatchScore) :
                       user.overallSimilarity !== undefined ? formatNumber(user.overallSimilarity, 'overallSimilarity') : '';
          text = `<span class="highlight">${user.username}</span>和你都超喜欢<span class="highlight-important">${categories}</span>分区的内容，推荐匹配度为<span class="highlight-important">${score}</span>，快认识一下这位同好呀～`;
        } else if (user.categorySimilarity) {
          text = `<span class="highlight">${user.username}</span>和你的视频分区兴趣高度重合，相似度为<span class="highlight-important">${formatNumber(user.categorySimilarity, 'categorySimilarity')}</span>，说不定你们能成为好朋友哦！`;
        }
        break;

      case 'theme':
        if (user.commonThemes) {
          const themes = user.commonThemes.join('、');
          const score = user.themeMatchScore !== undefined ? mapCategoryScore(user.themeMatchScore) :
                       user.overallSimilarity !== undefined ? formatNumber(user.overallSimilarity, 'overallSimilarity') : '';
          text = `<span class="highlight">${user.username}</span>和你都喜欢看<span class="highlight-important">${themes}</span>类型的内容，推荐匹配度为<span class="highlight-important">${score}</span>，快和TA打个招呼吧～`;
        } else if (user.themeSimilarity) {
          text = `<span class="highlight">${user.username}</span>和你的内容类型偏好相似度很高，为<span class="highlight-important">${formatNumber(user.themeSimilarity, 'themeSimilarity')}</span>，认识一下这位兴趣相投的朋友吧！`;
        }
        break;

      case 'favoriteSimilarity':
        const similarityScore = user.similarityScore !== undefined ? user.similarityScore :
                               user.favoriteSimilarity !== undefined ? user.favoriteSimilarity : 0;
        text = `你和<span class="highlight">${user.username}</span>收藏夹的相似度高达<span class="highlight-important">${formatNumber(similarityScore, 'similarityScore')}</span>，看来你们喜欢的视频风格很像呢！`;
        break;

      case 'userBehavior':
        // 先尝试新的属性名similarityScore，再兼容旧的overallSimilarity
        const similarity = user.similarityScore || user.overallSimilarity;
        
        if (similarity !== undefined && similarity !== null) {
            const formattedScore = formatNumber(similarity, 'similarityScore');
            text = `<span class="highlight">${user.username}</span>和你的行为模式相似度为<span class="highlight-important">${formattedScore}</span>，兴趣相投的你们说不定能成为好朋友呢～`;
        } else {
            // 如果没有相似度分数，使用通用提示
            text = `<span class="highlight">${user.username}</span>和你的观看习惯很相似，快去认识一下吧！`;
        }
        break;

      case 'commonUp':
        let commonUpText = '';
        
        // 优先显示数量
        if (user.commonUpCount || user.totalCommonUps) {
            const count = user.commonUpCount || user.totalCommonUps;
            commonUpText += `<span class="highlight">${user.username}</span>和你有<span class="highlight-important">${count}个</span>共同关注的UP主`;
        }
        
        // 如果有具体的UP主名称，补充显示
        if (user.commonUpNames && user.commonUpNames.length > 0) {
            const upNames = user.commonUpNames.join('、');
            if (commonUpText) {
                // 已有数量信息，追加名称
                commonUpText += `，包括：<span class="highlight-important">${upNames}</span>`;
            } else {
                // 没有数量信息，只显示名称
                commonUpText = `<span class="highlight">${user.username}</span>和你都关注了<span class="highlight-important">${upNames}</span>`;
            }
        }
        
        // 添加结尾提示
        if (commonUpText) {
            text = commonUpText + '，快认识一下这位同好呀！';
        }
        break;

      // 筛选类API处理
      case 'chainSameUp':
      case 'sameUp':
        if (user.upName) {
          // 使用 watchHours 而不是 watchDuration
          const durationText = user.watchHours ? `，观看时长达到了<span class="highlight-important">${formatNumber(user.watchHours, 'watchHours')}小时</span>` : '';
          text = `<span class="highlight">${user.username}</span>看过<span class="highlight-important">${user.upName}的视频</span>${durationText}，快认识一下这位同好呀！`;
        }
        break;

      case 'chainSameTag':
      case 'sameTag':
        if (user.tagName) {
          // 使用 totalWatchHours 而不是 watchDuration
          const durationText = user.totalWatchHours ? `，观看时长达到<span class="highlight-important">${formatNumber(user.totalWatchHours, 'totalWatchHours')}小时</span>` : '';
          text = `<span class="highlight">${user.username}</span>看过<span class="highlight-important">${user.tagName}</span>标签的视频${durationText}，快和TA交流一下吧～`;
        }
        break;

      case 'chainSameUpVideoCount':
      case 'sameUpVideoCount':
          if (user.upName) {
              // 适配新的DTO属性名
              const watchedCount = user.watchedVideoCount || user.videoCount || 0;
              const totalCount = user.totalVideoCount || 0;
              const ratio = user.watchRatio || user.videoRatio || 0;
              
              // 格式化比例显示
              const formattedRatio = formatNumber(ratio * 100, 'watchRatio') + '%';
              
              text = `TA观看了<span class="highlight">${user.upName}的<span class="highlight-important">${watchedCount}个</span>视频（共${totalCount}个），比例为<span class="highlight-important">${formattedRatio}</span>，快认识一下这位同好呀！`;
          }
          break;

      case 'chainSameTagVideoCount':
      case 'sameTagVideoCount':
        // 这里的 user 对象通常是 API 返回的 DTO 拍平后的结果
        if (user.tagName) {
          // 1. 修正 watchedVideoCount (后端返回的是该标签下观看的视频数)
          const videoCount = user.watchedVideoCount !== undefined ? user.watchedVideoCount : 0;
          
          // 2. 修正 watchRatio (后端返回的是 Double 类型的比例)
          // 假设后端返回的是 0.75 这种小数，所以乘以 100 变成 75
          const ratio = user.watchRatio !== undefined ? (user.watchRatio * 100).toFixed(1) : '0.0';
          
          text = `TA观看了带有<span class="highlight-important">${user.tagName}标签的<span class="highlight-important">${videoCount}个</span>视频，比例为<span class="highlight-important">${ratio}%</span>，快认识一下这位同好呀！`;
        }
        break;

      case 'chainFollowTime':
        case 'followTime':
            // 如果有推荐理由，直接显示
            if (user.recommendationReason) {
                text = `<span class="highlight">${user.username}</span>${user.recommendationReason}`;
            }
            // 兼容旧的数据格式
            else if (user.upName) {
                const timeDiffText = user.timeDifference ? `，关注时间相差<span class="highlight-important">${formatNumber(user.timeDifference / 3600, 'timeDifference')}小时</span>` : '';
                text = `你和<span class="highlight">${user.username}</span>在相近时间关注了<span class="highlight-important">${user.upName}</span>${timeDiffText}，说不定是缘分呢～`;
            }
            break;

      case 'chainDeepVideo':
      case 'deepVideo':
        if (user.videoTitle) {
          const watchRatioText = user.watchRatio ? `，观看比例达到<span class="highlight-important">${formatNumber(user.watchRatio * 100, 'watchRatio')}%</span>` : '';
          text = `你和<span class="highlight">${user.username}</span>都深度观看了<span class="highlight-important">${user.videoTitle}</span>${watchRatioText}，对这部作品的喜爱程度不相上下哦！`;
        }
        break;

      case 'chainSeries':
      case 'series':
        if (user.tagName) {
          text = `你和<span class="highlight">${user.username}</span>都观看了<span class="highlight-important">${user.tagName}</span>系列作品，TA已经看了<span class="highlight-important">${user.watchedVideos}/${user.totalVideos}</span>个，快和TA交流一下观看心得吧～`;
        }
        break;
    }

    if (text) {
      tooltipTexts.push(text);
    }
  });

  // 3. 添加匹配分数（如果有）
  if (user.matchScore !== undefined) {
    tooltipTexts.push(`你们的匹配分数为<span class="highlight-important">${formatNumber(user.matchScore, 'matchScore')}</span>，很适合成为朋友哦！`);
  }

  // 如果有多个推荐条件，用换行符分隔显示
  if (tooltipTexts.length > 0) {
    // 移除末尾多余的标点符号
    let finalText = tooltipTexts.join('<br>');
    finalText = finalText.replace(/，+$/g, '') + '<br>快和TA成为朋友吧！';
    return finalText;
  }

  // 默认返回一个友好的文本
  return `<span class="highlight">${user.username}</span>是一个很有趣的用户，快和TA成为朋友吧！`;
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

// 分区重合度映射函数 f(x)：将0-3分映射到80-100分
function mapCategoryScore(score) {
  // 使用一个简单的二次函数映射，确保分数在80-100之间
  const mappedScore = 80 + Math.pow(score / 3, 1.5) * 20;
  return Math.min(100, Math.max(80, mappedScore)).toFixed(2);
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
  // 复用formatNumber函数统一数字格式化
  const formattedValue = formatNumber(value, key);
  // 数组类型的值处理
  if (Array.isArray(value)) {
    return value.join('、');
  }
  return formattedValue;
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

  return (maxSimilarity * 100).toFixed(2)
}

// 发送好友申请
async function sendFriendRequest(targetUserId, event) {
  try {
    addingFriendId.value = targetUserId
    await friendApi.sendFriendRequest(targetUserId)
    showMessage('好友申请发送成功', 'success')
  } catch (error) {
    console.error('发送好友申请失败:', error)
    showMessage(error.message || '发送好友申请失败，请稍后重试', 'error')
  } finally {
    addingFriendId.value = null
  }
}

// 生成模拟头像路径
function generateAvatarPath(userId) {
  // 使用统一的头像生成工具
  return getUserAvatar(null, userId)
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
        user.interactionCount = Math.floor(Math.random() * 50) + 5
        user.similarityScore = similarity
        break
      case 'sharedVideo':
        user.sharedVideoCount = Math.floor(Math.random() * 100) + 10
        user.similarityScore = similarity
        break
      case 'category':
        user.commonCategories = ['科技', '游戏', '生活'].slice(0, Math.floor(Math.random() * 3) + 1)
        user.categorySimilarity = similarity
        break
      case 'theme':
        user.commonThemes = ['搞笑', '测评', '教程'].slice(0, Math.floor(Math.random() * 3) + 1)
        user.themeSimilarity = similarity
        break
      case 'favoriteSimilarity':
        user.commonFavoriteCount = Math.floor(Math.random() * 50) + 10
        user.favoriteSimilarity = similarity
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

/* 主布局 */
.main-layout {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

/* 侧边栏样式 */
.sidebar {
  width: 280px;
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.sidebar h3 {
  font-size: 1.2rem;
  margin-bottom: 15px;
  color: #333;
}

/* 侧边栏选项组 */
.sidebar-options {
  margin-bottom: 20px;
}

.sidebar-options h4 {
  font-size: 1rem;
  margin-bottom: 10px;
  color: #555;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
}

/* 主内容区域 */
.main-content {
  flex: 1;
}

/* 筛选API部分 */
.filter-api-section {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.filter-api-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-api-button {
  padding: 10px 18px;
  background-color: #f0f0f0;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.filter-api-button:hover {
  background-color: #e0e0e0;
  border-color: #999;
}

.filter-api-button.active {
  background-color: #4682b4;
  color: white;
  border-color: #36648b;
}

/* 筛选表单 */
.filter-forms {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.filter-form {
  background-color: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.filter-form-title {
  font-size: 1rem;
  margin-bottom: 15px;
  color: #333;
}

.form-field {
  margin-bottom: 15px;
}

.form-label {
  display: block;
  margin-bottom: 5px;
  font-size: 0.9rem;
  color: #555;
}

.form-select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9rem;
}

/* 推荐API部分 */
.recommend-api-section {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.recommend-api-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.recommend-api-button {
  padding: 10px 18px;
  background-color: #f0f0f0;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.recommend-api-button:hover {
  background-color: #e0e0e0;
  border-color: #999;
}

.recommend-api-button.active {
  background-color: #9370db;
  color: white;
  border-color: #7b68ee;
}

/* 行动按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn, .recommend-btn {
  padding: 14px 30px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-btn {
  background-color: #4682b4;
  color: white;
}

.filter-btn:hover:not(:disabled) {
  background-color: #36648b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(70, 130, 180, 0.3);
}

.recommend-btn {
  background-color: #ff69b4;
  color: white;
}

.recommend-btn:hover:not(:disabled) {
  background-color: #ff1493;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
}

.filter-btn:disabled, .recommend-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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

/* 搜索框样式 */
.search-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 10px;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #4682b4;
  box-shadow: 0 0 0 2px rgba(70, 130, 180, 0.1);
}

/* 筛选条件样式 */
.filter-conditions {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.filter-tip {
  background-color: #e8f4fd;
  color: #3366cc;
  padding: 10px 15px;
  border-radius: 5px;
  margin-bottom: 20px;
  font-size: 0.9rem;
  border-left: 3px solid #3366cc;
}

.filter-condition {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-condition:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.filter-condition-header {
  margin-bottom: 10px;
}

.filter-condition-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}

.filter-condition-title-row label {
  font-weight: 600;
  font-size: 1rem;
  color: #333;
}

.remove-filter-btn {
  background-color: #ff6b6b;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.remove-filter-btn:hover {
  background-color: #ff5252;
  transform: scale(1.1);
}

.filter-condition-desc {
  font-size: 0.8rem;
  color: #666;
  margin-bottom: 10px;
}

/* 搜索结果列表 */
.search-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: white;
  margin-bottom: 10px;
}

.search-result-item {
  padding: 10px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.search-result-item:hover {
  background-color: #f5f5f5;
}

.search-result-item:last-child {
  border-bottom: none;
}

/* 比例选项 */
.ratio-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.ratio-option {
  padding: 6px 12px;
  border: 2px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
}

.ratio-option:hover {
  border-color: #4682b4;
}

.ratio-option.active {
  background-color: #4682b4;
  color: white;
  border-color: #4682b4;
}

/* 滑块样式 */
.slider-container {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
}

.slider-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 5px;
}

.slider {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: #ddd;
  outline: none;
  -webkit-appearance: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #4682b4;
  cursor: pointer;
  transition: background 0.3s ease;
}

.slider::-webkit-slider-thumb:hover {
  background: #356a96;
}

.slider::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #4682b4;
  cursor: pointer;
  border: none;
  transition: background 0.3s ease;
}

.slider::-moz-range-thumb:hover {
  background: #356a96;
}

.slider-value {
  font-size: 0.9rem;
  color: #4682b4;
  font-weight: 500;
  margin-top: 5px;
  text-align: center;
}

/* 用户网格 */
.user-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.user-card {
  background-color: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 10px;
  border: 2px solid #ff69b4;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
  color: #333;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
  font-size: 16px;
}

/* 滑块样式 */
.slider-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 0;
}

.slider-input {
  width: 100%;
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(to right, #ff69b4, #ff1493);
  outline: none;
  -webkit-appearance: none;
}

.slider-input::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ff1493;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.slider-input::-moz-range-thumb {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #ff1493;
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.slider-value {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #ff69b4;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #666;
}

.slider-tip {
  margin-top: 8px;
  font-size: 11px;
  color: #888;
  font-style: italic;
  text-align: center;
}

/* 多选按钮组样式 */
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 10px 0;
}

.checkbox-item {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.radio-input {
  margin-right: 10px;
  width: 16px;
  height: 16px;
  accent-color: #ff69b4;
}

.radio-label {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: color 0.2s ease;
}

.checkbox-item:hover .radio-label {
  color: #ff69b4;
}

/* 筛选值信息样式 */
.filter-value-info {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 5px;
  font-size: 14px;
  color: #4682b4;
  text-align: center;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .filter-forms {
    grid-template-columns: 1fr;
  }

  .user-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 15px;
  }
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
  margin-bottom: 12px;
}

.add-friend-btn {
  padding: 8px 16px;
  background-color: #ff69b4;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.add-friend-btn:hover:not(:disabled) {
  background-color: #ff1493;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 105, 180, 0.3);
}

.add-friend-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
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

.custom-tooltip-text {
  color: #ffffff;
  font-weight: 500;
  line-height: 1.6;
  padding: 5px 0;
}

.custom-tooltip-text .highlight {
  color: #ff69b4;
  font-weight: 600;
}

.custom-tooltip-text .highlight-important {
  color: #00ffff;
  font-weight: 700;
}

.custom-tooltip-text .highlight-score {
  color: #ff9800;
  font-weight: 700;
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

/* 二次筛选区域样式 */
.secondary-filter-section {
  background-color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 30px;
}

.section-title {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 20px;
  font-weight: 500;
}

.filter-group {
  margin-bottom: 25px;
}

.filter-title {
  font-size: 1.1rem;
  color: #555;
  margin-bottom: 12px;
  font-weight: 500;
}

.search-box {
  position: relative;
  margin-bottom: 12px;
}

.search-box input {
  width: 100%;
  padding: 10px 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #ff69b4;
  box-shadow: 0 0 0 3px rgba(255, 105, 180, 0.1);
}

.search-input-group {
  position: relative;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 0 0 8px 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
}

.search-item {
  padding: 10px 15px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.search-item:hover {
  background-color: #f5f5f5;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  background-color: #ffe4f0;
  color: #ff1493;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  border: 2px solid #ff69b4;
}

.tag-remove {
  margin-left: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: #ff69b4;
  color: white;
  transition: background-color 0.2s ease;
}

.tag-remove:hover {
  background-color: #ff1493;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.apply-filter-btn {
  padding: 10px 20px;
  background-color: #ff69b4;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.apply-filter-btn:hover:not(:disabled) {
  background-color: #ff1493;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
}

.apply-filter-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.reset-filter-btn {
  padding: 10px 20px;
  background-color: #f0f0f0;
  color: #666;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reset-filter-btn:hover {
  background-color: #e0e0e0;
  border-color: #999;
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

  .filter-actions {
    flex-direction: column;
  }

  .apply-filter-btn,
  .reset-filter-btn {
    width: 100%;
  }
}
.get-users-btn {
  background-color: #409eff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin: 10px 0;
}

.get-users-btn:hover {
  background-color: #66b1ff;
}

.get-users-btn:disabled {
  background-color: #c6e2ff;
  cursor: not-allowed;
}
</style>
