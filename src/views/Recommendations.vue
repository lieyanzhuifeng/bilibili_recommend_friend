<template>
  <div class="recommendations-container">
    <div class="main-layout">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <h3 class="sidebar-title">筛选条件</h3>
        <div class="sidebar-section">
          <h4 class="section-title">用户活跃度</h4>
          <div class="slider-group">
            <input
              type="range"
              min="0"
              max="100"
              v-model.number="activityLevel"
              class="slider-input"
            >
            <div class="slider-value">{{ activityLevel }}%</div>
            <div class="slider-labels">
              <span>低</span>
              <span>中</span>
              <span>高</span>
            </div>
          </div>
        </div>
        <div class="sidebar-section">
          <h4 class="section-title">夜猫子程度</h4>
          <div class="slider-group">
            <input
              type="range"
              min="0"
              max="100"
              v-model.number="nightOwlLevel"
              class="slider-input"
            >
            <div class="slider-value">{{ nightOwlLevel }}%</div>
            <div class="slider-labels">
              <span>轻度</span>
              <span>中度</span>
              <span>重度</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- 主内容区域 -->
      <main class="main-content">
        <h1 class="page-title">用户推荐系统</h1>

    <!-- 推荐API选择区域 -->
    <div class="recommend-api-section">
      <h2 class="section-title">责任链推荐</h2>

      <!-- 推荐API选择按钮 -->
      <div class="recommend-api-buttons">
        <button
          v-for="recommendApi in recommendApis"
          :key="recommendApi.key"
          class="recommend-api-button"
          :class="{ active: selectedRecommendApi === recommendApi.key }"
          @click="selectRecommendApi(recommendApi.key)"
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
          :class="{ active: selectedFilterApis.includes(filterApi.key) }"
          @click="toggleFilterApiSelection(filterApi.key)"
        >
          {{ filterApi.name }}
        </button>
      </div>

      <!-- 筛选条件输入区域 -->
      <div v-if="selectedFilterApis.length > 0" class="filter-conditions">
        <!-- UP主视频观看比例 -->
        <div v-if="selectedFilterApis.includes('chainSameUpVideoCount')" class="filter-condition">
          <label>选择UP主:</label>
          <div class="search-input-group">
            <input
              type="text"
              v-model="filterSearchKeywords.up"
              @input="debounceFilterSearch('up')"
              placeholder="搜索UP主"
              class="search-input"
            >
            <div v-if="filterUpSearchResults.length > 0" class="search-results">
              <div
                v-for="up in filterUpSearchResults"
                :key="up.userId"
                class="search-result-item"
                @click="selectFilterUp(up)"
              >
                {{ up.username }}
              </div>
            </div>
          </div>
          <div v-if="filterFormData.chainSameUpVideoCount.upName" class="selected-item">
            {{ filterFormData.chainSameUpVideoCount.upName }}
            <button @click="removeFilterUp" class="remove-btn">×</button>
          </div>
          <div class="ratio-options">
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameUpVideoCount.ratioOption"
                value="HIGH"
              >
              高比例
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameUpVideoCount.ratioOption"
                value="MEDIUM"
              >
              中比例
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameUpVideoCount.ratioOption"
                value="LOW"
              >
              低比例
            </label>
          </div>
        </div>

        <!-- 标签视频观看比例 -->
        <div v-if="selectedFilterApis.includes('chainSameTagVideoCount')" class="filter-condition">
          <label>选择标签:</label>
          <div class="search-input-group">
            <input
              type="text"
              v-model="filterSearchKeywords.tag"
              @input="debounceFilterSearch('tag')"
              placeholder="搜索标签"
              class="search-input"
            >
            <div v-if="filterTagSearchResults.length > 0" class="search-results">
              <div
                v-for="tag in filterTagSearchResults"
                :key="tag.tagId"
                class="search-result-item"
                @click="selectFilterTag(tag)"
              >
                {{ tag.tagName }}
              </div>
            </div>
          </div>
          <div v-if="filterFormData.chainSameTagVideoCount.tagName" class="selected-item">
            {{ filterFormData.chainSameTagVideoCount.tagName }}
            <button @click="removeFilterTag" class="remove-btn">×</button>
          </div>
          <div class="ratio-options">
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameTagVideoCount.ratioOption"
                value="HIGH"
              >
              高比例
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameTagVideoCount.ratioOption"
                value="MEDIUM"
              >
              中比例
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainSameTagVideoCount.ratioOption"
                value="LOW"
              >
              低比例
            </label>
          </div>
        </div>

        <!-- 深度视频 -->
        <div v-if="selectedFilterApis.includes('chainDeepVideo')" class="filter-condition">
          <label>选择视频:</label>
          <div class="search-input-group">
            <input
              type="text"
              v-model="filterSearchKeywords.video"
              @input="debounceFilterSearch('video')"
              placeholder="搜索视频"
              class="search-input"
            >
            <div v-if="filterVideoSearchResults.length > 0" class="search-results">
              <div
                v-for="video in filterVideoSearchResults"
                :key="video.videoId"
                class="search-result-item"
                @click="selectFilterVideo(video)"
              >
                {{ video.title }}
              </div>
            </div>
          </div>
          <div v-if="filterFormData.chainDeepVideo.videoTitle" class="selected-item">
            {{ filterFormData.chainDeepVideo.videoTitle }}
            <button @click="removeFilterVideo" class="remove-btn">×</button>
          </div>
          <div class="ratio-options">
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainDeepVideo.option"
                value="HIGH"
              >
              高深度
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainDeepVideo.option"
                value="MEDIUM"
              >
              中深度
            </label>
            <label>
              <input
                type="radio"
                v-model="filterFormData.chainDeepVideo.option"
                value="LOW"
              >
              低深度
            </label>
          </div>
        </div>

        <!-- 系列作品 -->
        <div v-if="selectedFilterApis.includes('chainSeries')" class="filter-condition">
          <label>选择系列标签:</label>
          <div class="search-input-group">
            <input
              type="text"
              v-model="filterSearchKeywords.series"
              @input="debounceFilterSearch('series')"
              placeholder="搜索系列标签"
              class="search-input"
            >
            <div v-if="filterSeriesSearchResults.length > 0" class="search-results">
              <div
                v-for="series in filterSeriesSearchResults"
                :key="series.tagId"
                class="search-result-item"
                @click="selectFilterSeries(series)"
              >
                {{ series.tagName }}
              </div>
            </div>
          </div>
          <div v-if="filterFormData.chainSeries.tagName" class="selected-item">
            {{ filterFormData.chainSeries.tagName }}
            <button @click="removeFilterSeries" class="remove-btn">×</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 按钮区域 -->
    <div class="action-buttons">
      <button class="filter-btn" @click="applyFilter" :disabled="loading">
        {{ loading ? '筛选中...' : '应用筛选' }}
      </button>
      <button class="recommend-btn" @click="fetchRecommendations" :disabled="loading">
        {{ loading ? '获取中...' : '获取推荐' }}
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
const selectedRecommendApi = ref('')
const loading = ref(false)
const message = ref('')
const messageType = ref('info')
const tooltipVisible = ref(false)
const tooltipUser = ref(null)
const tooltipStyle = ref({})
const addingFriendId = ref(null)

// 新增的推荐和选中用户数据
const recommendations = ref([])
const selectedUsers = ref([])

// 筛选API相关数据
const filterApis = ref([
  { key: 'chainSameUpVideoCount', name: 'UP主视频观看比例' },
  { key: 'chainSameTagVideoCount', name: '标签视频观看比例' },
  { key: 'chainDeepVideo', name: '深度视频' },
  { key: 'chainSeries', name: '系列作品' }
])
const selectedFilterApis = ref([])
const filterSearchKeywords = ref({
  up: '',
  tag: '',
  video: '',
  series: ''
})
const filterUpSearchResults = ref([])
const filterTagSearchResults = ref([])
const filterVideoSearchResults = ref([])
const filterSeriesSearchResults = ref([])
const filterFormData = ref({
  chainSameUpVideoCount: {
    upId: '',
    upName: '',
    ratioOption: 'HIGH'
  },
  chainSameTagVideoCount: {
    tagId: '',
    tagName: '',
    ratioOption: 'HIGH'
  },
  chainDeepVideo: {
    videoId: '',
    videoTitle: '',
    option: 'HIGH'
  },
  chainSeries: {
    tagId: '',
    tagName: ''
  }
})

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
const activityLevel = ref(50) // 初始值设为中间值50%
const nightOwlLevel = ref(50) // 初始值设为中间值50%

// 二次筛选结果
const secondaryFilterResults = ref([])
const secondaryFilterApplied = ref(false)

// 头像生成函数已从utils/avatar.js导入

// API配置
const recommendApis = [
  { key: 'coComment', name: '同视频评论推荐', endpoint: '/api/recommend/co-comment/' },
  { key: 'reply', name: '评论回复推荐', endpoint: '/api/recommend/reply/' },
  { key: 'sharedVideo', name: '共同分享视频推荐', endpoint: '/api/recommend/shared-video/' },
  { key: 'category', name: '分类偏好推荐', endpoint: '/api/recommend/category/' },
  { key: 'theme', name: '主题偏好推荐', endpoint: '/api/recommend/theme/' },
  { key: 'favoriteSimilarity', name: '收藏相似度推荐', endpoint: '/api/recommend/favorite-similarity/' },
  { key: 'commentFriends', name: '评论好友推荐', endpoint: '/api/recommend/comment-friends/' }
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
function selectRecommendApi(apiKey) {
  selectedRecommendApi.value = apiKey
}

// 切换筛选API选择
function toggleFilterApiSelection(filterApiKey) {
  const index = selectedFilterApis.value.indexOf(filterApiKey)
  if (index > -1) {
    selectedFilterApis.value.splice(index, 1)
  } else {
    selectedFilterApis.value.push(filterApiKey)
  }
}

// 筛选搜索防抖
const filterSearchDebounce = {}
function debounceFilterSearch(type) {
  if (filterSearchDebounce[type]) {
    clearTimeout(filterSearchDebounce[type])
  }

  filterSearchDebounce[type] = setTimeout(async () => {
    const keyword = filterSearchKeywords.value[type]
    if (!keyword) {
      if (type === 'up') filterUpSearchResults.value = []
      if (type === 'tag') filterTagSearchResults.value = []
      if (type === 'video') filterVideoSearchResults.value = []
      if (type === 'series') filterSeriesSearchResults.value = []
      return
    }

    try {
      let results = []
      if (type === 'up') {
        const response = await searchApi.searchUsers(keyword, 'up')
        results = response.data || []
        filterUpSearchResults.value = results
      } else if (type === 'tag') {
        const response = await searchApi.searchTags(keyword)
        results = response.data || []
        filterTagSearchResults.value = results
      } else if (type === 'video') {
        const response = await searchApi.searchVideos(keyword)
        results = response.data || []
        filterVideoSearchResults.value = results
      } else if (type === 'series') {
        const response = await searchApi.searchTags(keyword)
        results = response.data || []
        filterSeriesSearchResults.value = results
      }
    } catch (error) {
      console.error(`搜索${type}失败:`, error)
    }
  }, 300)
}

// 选择筛选UP主
function selectFilterUp(up) {
  filterFormData.value.chainSameUpVideoCount.upId = up.upId
  filterFormData.value.chainSameUpVideoCount.upName = up.upName
  filterSearchKeywords.value.up = up.upName
  filterUpSearchResults.value = []
}

// 选择筛选标签
function selectFilterTag(tag) {
  filterFormData.value.chainSameTagVideoCount.tagId = tag.tagId
  filterFormData.value.chainSameTagVideoCount.tagName = tag.tagName
  filterSearchKeywords.value.tag = tag.tagName
  filterTagSearchResults.value = []
}

// 选择筛选视频
function selectFilterVideo(video) {
  filterFormData.value.chainDeepVideo.videoId = video.videoId
  filterFormData.value.chainDeepVideo.videoTitle = video.videoTitle
  filterSearchKeywords.value.video = video.videoTitle
  filterVideoSearchResults.value = []
}

// 选择筛选系列
function selectFilterSeries(series) {
  filterFormData.value.chainSeries.tagId = series.tagId
  filterFormData.value.chainSeries.tagName = series.tagName
  filterSearchKeywords.value.series = series.tagName
  filterSeriesSearchResults.value = []
}

// 应用筛选
async function applyFilter() {
  loading.value = true
  try {
    // 构建筛选参数
    const filterParams = {
      userId: getCurrentUserId(),
      activityLevel: activityLevel.value,
      nightOwlLevel: nightOwlLevel.value,
      filterChain: []
    }

    // 添加选中的筛选API参数
    if (selectedFilterApis.value.includes('chainSameUpVideoCount')) {
      const upData = filterFormData.value.chainSameUpVideoCount
      if (upData.upId) {
        filterParams.filterChain.push({
          type: 'chainSameUpVideoCount',
          upId: upData.upId,
          ratioOption: upData.ratioOption
        })
      }
    }

    if (selectedFilterApis.value.includes('chainSameTagVideoCount')) {
      const tagData = filterFormData.value.chainSameTagVideoCount
      if (tagData.tagId) {
        filterParams.filterChain.push({
          type: 'chainSameTagVideoCount',
          tagId: tagData.tagId,
          ratioOption: tagData.ratioOption
        })
      }
    }

    if (selectedFilterApis.value.includes('chainDeepVideo')) {
      const videoData = filterFormData.value.chainDeepVideo
      if (videoData.videoId) {
        filterParams.filterChain.push({
          type: 'chainDeepVideo',
          videoId: videoData.videoId,
          option: videoData.option
        })
      }
    }

    if (selectedFilterApis.value.includes('chainSeries')) {
      const seriesData = filterFormData.value.chainSeries
      if (seriesData.tagId) {
        filterParams.filterChain.push({
          type: 'chainSeries',
          tagId: seriesData.tagId
        })
      }
    }

    // 调用筛选API
    const response = await filterApi.filterByChain(filterParams)
    if (response.success) {
      // 筛选结果直接用于推荐数据
      recommendations.value = response.data || []
      showMessage('筛选成功', 'success')
    } else {
      showMessage(response.message || '筛选失败', 'error')
    }
  } catch (error) {
    console.error('筛选失败:', error)
    showMessage('筛选失败，请稍后重试', 'error')
  } finally {
    loading.value = false
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
    // 获取筛选参数
    const params = getChainParams()

    // 调用筛选API
    const response = await filterApi.filterByChain(params)

    // 处理响应结果
    secondaryFilterResults.value = response.data || []
    secondaryFilterApplied.value = true

    if (secondaryFilterResults.value.length > 0) {
      showMessage(`成功应用二次筛选，筛选结果包含 ${secondaryFilterResults.value.length} 位用户`, 'success')
    } else {
      showMessage('二次筛选没有找到符合条件的用户', 'info')
    }
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
async function fetchRecommendations() {
  if (!selectedRecommendApi.value) {
    showMessage('请选择一个推荐API', 'warning')
    return
  }

  loading.value = true
  message.value = '正在获取推荐用户...'

  try {
    // 构建请求参数
    const params = {
      userId: getCurrentUserId()
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
      default:
        showMessage('未知的推荐API', 'error')
        return
    }

    if (response.success) {
      recommendations.value = response.data || []
      showMessage('推荐获取成功', 'success')
    } else {
      showMessage(response.message || '获取推荐失败', 'error')
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
  if (!user || !user.sourceApis) return '';

  const tooltipTexts = [];

  // 处理所有推荐来源API
  user.sourceApis.forEach(apiKey => {
    let text = '';

    switch (apiKey) {
      case 'coComment':
        if (user.videoTitle) {
          text = `你和<span class="highlight">${user.username}</span>都在<span class="highlight-important">${user.videoTitle}</span>下评论过，认识一下叭。`;
        } else if (user.commonVideoCount !== undefined) {
          text = `你和<span class="highlight">${user.username}</span>都评论过<span class="highlight-important">${formatNumber(user.commonVideoCount, 'commonVideoCount')}个</span>相同的视频，认识一下叭。`;
        }
        break;
      case 'reply':
        text = `<span class="highlight">${user.username}</span>曾经回复过你的评论，认识一下叭。`;
        break;
      case 'sharedVideo':
        if (user.similarityRate !== undefined) {
          text = `你与<span class="highlight">${user.username}</span>观看过的视频的相似度为<span class="highlight-important">${formatNumber(user.similarityRate, 'similarityRate')}</span>`;
        } else if (user.similarityScore !== undefined) {
          text = `你与<span class="highlight">${user.username}</span>观看过的视频的相似度为<span class="highlight-important">${formatNumber(user.similarityScore, 'similarityScore')}</span>`;
        }
        break;
      case 'category':
        if (user.commonCategories) {
          const categories = user.commonCategories.join('、');
          const score = user.categoryMatchScore !== undefined ? mapCategoryScore(user.categoryMatchScore) :
                       user.categorySimilarity !== undefined ? formatNumber(user.categorySimilarity, 'categorySimilarity') : '';
          text = `<span class="highlight">${user.username}</span>和你都很喜欢看<span class="highlight-important">${categories}</span>分区的内容，他的推荐分数为<span class="highlight-important">${score}</span>`;
        }
        break;
      case 'theme':
        if (user.commonThemes) {
          const themes = user.commonThemes.join('、');
          const score = user.themeMatchScore !== undefined ? mapCategoryScore(user.themeMatchScore) :
                       user.themeSimilarity !== undefined ? formatNumber(user.themeSimilarity, 'themeSimilarity') : '';
          text = `<span class="highlight">${user.username}</span>和你都很喜欢看<span class="highlight-important">${themes}</span>的内容形式，他的推荐分数为<span class="highlight-important">${score}</span>`;
        }
        break;
      case 'favoriteSimilarity':
        const similarityScore = user.similarityScore !== undefined ? user.similarityScore :
                               user.favoriteSimilarity !== undefined ? user.favoriteSimilarity : 0;
        text = `你和<span class="highlight">${user.username}</span>收藏视频的相似度很高，为<span class="highlight-important">${formatNumber(similarityScore, 'similarityScore')}</span>`;
        break;
      case 'commentFriends':
        // 添加评论相关的数字展示
        const commonTopicCount = user.commonTopicCount !== undefined ? formatNumber(user.commonTopicCount, 'commonTopicCount') : 0;
        const commentSimilarity = user.commentSimilarity !== undefined ? formatNumber(user.commentSimilarity, 'commentSimilarity') : '0.00%';
        text = `<span class="highlight">${user.username}</span>和你有<span class="highlight-important">${commonTopicCount}个</span>共同话题，评论相似度为<span class="highlight-important">${commentSimilarity}</span>`;
        break;
    }

    if (text) {
      tooltipTexts.push(text);
    }
  });

  // 如果有多个推荐条件，用换行符分隔显示
  if (tooltipTexts.length > 0) {
    return tooltipTexts.join('<br>');
  }

  // 默认返回空字符串，使用原来的详细信息显示
  return '';
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
  font-size: 12px;
  color: #666;
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
</style>
