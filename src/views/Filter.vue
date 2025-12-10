<template>
  <div class="filter-container">
    <h1 class="page-title">筛选条件</h1>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-group">
        <label class="search-label">搜索视频</label>
        <div class="search-input-wrapper">
          <input
            v-model="searchKeywords.video"
            type="text"
            placeholder="输入视频标题关键词"
            class="search-input"
            @input="debounceSearch('video')"
          />
          <div v-if="videoSearchResults.length > 0" class="search-dropdown">
            <div
              v-for="video in videoSearchResults"
              :key="video.videoId"
              class="search-item"
              @click="selectVideo(video)"
            >
              {{ video.title }}
            </div>
          </div>
        </div>
      </div>
      <div class="search-group">
        <label class="search-label">搜索标签</label>
        <div class="search-input-wrapper">
          <input
            v-model="searchKeywords.tag"
            type="text"
            placeholder="输入标签名称关键词"
            class="search-input"
            @input="debounceSearch('tag')"
          />
          <div v-if="tagSearchResults.length > 0" class="search-dropdown">
            <div
              v-for="tag in tagSearchResults"
              :key="tag.tagId"
              class="search-item"
              @click="selectTag(tag)"
            >
              {{ tag.tagName }} ({{ tag.videoCount }}个视频)
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选条件选择区域 -->
    <div class="filter-section">
      <Card title="筛选条件" shadow>
        <!-- 责任链筛选 -->
        <div class="filter-content">
          <div class="filter-tags">
            <button
              v-for="api in chainFilterApis"
              :key="api.key"
              class="filter-tag"
              :class="{ 'active': selectedChainFilters.includes(api.key) }"
              @click="toggleChainFilter(api.key)"
            >
              {{ api.name }}
            </button>
          </div>

          <!-- 责任链筛选参数 -->
          <div v-if="selectedChainFilters.length > 0" class="chain-filters">
            <h4 class="chain-title">筛选参数</h4>
            <div class="filter-group">
              <label class="filter-label">用户活跃度</label>
              <select v-model="chainParams.activity" class="filter-select">
                <option value="0">全部</option>
                <option value="1">高度活跃</option>
                <option value="2">中度活跃</option>
                <option value="3">轻度活跃</option>
              </select>
            </div>
            <div class="filter-group">
              <label class="filter-label">夜猫子程度</label>
              <select v-model="chainParams.nightOwl" class="filter-select">
                <option value="0">全部</option>
                <option value="1">重度夜猫子</option>
                <option value="2">中度夜猫子</option>
                <option value="3">轻度夜猫子</option>
              </select>
            </div>
          </div>
        </div>
      </Card>

      <!-- 筛选操作按钮 -->
      <div class="action-section">
        <Button
          type="primary"
          size="large"
          :loading="loading"
          :disabled="loading || !selectedChainFilters.length"
          @click="applyFilter"
        >
          {{ loading ? '筛选中...' : '应用筛选' }}
        </Button>
        <Button
          type="outline"
          size="large"
          @click="resetFilters"
        >
          重置筛选条件
        </Button>
      </div>
    </div>

    <!-- 筛选结果展示区域 -->
    <div v-if="filterResults.length > 0" class="results-section">
      <Card title="筛选结果" shadow>
        <h2 class="results-title">共找到 {{ filterResults.length }} 位用户</h2>
        <div class="user-grid">
          <div
            v-for="user in filterResults"
            :key="user.userId"
            class="user-card"
            @mouseenter="showTooltip(user, $event)"
            @mouseleave="hideTooltip"
          >
            <div class="user-avatar">
              <img :src="getUserAvatar(user)" alt="用户头像">
            </div>
            <div class="user-info">
              <h3 class="username">{{ user.username }}</h3>
              <Button
                type="primary"
                size="small"
                :loading="addingFriendId === user.userId"
                @click.stop="sendFriendRequest(user.userId)"
              >
                添加好友
              </Button>
            </div>
          </div>
        </div>
      </Card>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && selectedFilterType" class="empty-state">
      <p>没有找到符合条件的用户</p>
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
        <!-- 优先使用定制化的提示文本 -->
        <div v-if="getCustomTooltipText(tooltipUser)" v-html="getCustomTooltipText(tooltipUser)"></div>
        <!-- 如果没有定制化文本，则显示详细信息 -->
        <div v-else>
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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { filterApi, friendApi, searchApi } from '@/services/api'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import { getUserAvatar } from '@/utils/avatar'

// 筛选相关API配置 - 责任链筛选
const chainFilterApis = [
  { key: 'chainSameUpVideoCount', name: 'UP主视频观看比例', endpoint: '/api/chain/same-up-video-count' },
  { key: 'chainSameTagVideoCount', name: '标签视频观看比例', endpoint: '/api/chain/same-tag-video-count' },
  { key: 'chainDeepVideo', name: '深度视频', endpoint: '/api/chain/deep-video' },
  { key: 'chainSeries', name: '系列作品', endpoint: '/api/chain/series' }
]

// 响应式数据
const selectedChainFilters = ref([])
const chainParams = ref({ activity: 0, nightOwl: 0 })
const filterResults = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('info')
const tooltipVisible = ref(false)
const tooltipUser = ref(null)
const tooltipStyle = ref({})
const addingFriendId = ref(null)

// 搜索相关数据
const searchKeywords = ref({ video: '', tag: '' })
const videoSearchResults = ref([])
const tagSearchResults = ref([])
const searchTimeout = ref(null)

// 筛选参数配置
const filterParamsConfig = {
  sameTag: [
    { name: 'tagId', label: '标签ID', type: 'input', inputType: 'number', placeholder: '请输入标签ID' },
    { name: 'durationOption', label: '关注时长选项', type: 'select', options: [
      { value: -1, label: '全部' },
      { value: 0, label: '1个月内' },
      { value: 1, label: '3个月内' },
      { value: 2, label: '6个月内' },
      { value: 3, label: '1年内' }
    ] }
  ],
  sameUpVideoCount: [
    { name: 'upId', label: 'UP主ID', type: 'input', inputType: 'number', placeholder: '请输入UP主ID' },
    { name: 'ratioOption', label: '观看比例选项', type: 'select', options: [
      { value: 0, label: '大于20%' },
      { value: 1, label: '大于50%' },
      { value: 2, label: '大于80%' }
    ] }
  ],
  sameTagVideoCount: [
    { name: 'tagId', label: '标签ID', type: 'input', inputType: 'number', placeholder: '请输入标签ID' },
    { name: 'ratioOption', label: '观看比例选项', type: 'select', options: [
      { value: 0, label: '大于20%' },
      { value: 1, label: '大于50%' },
      { value: 2, label: '大于80%' }
    ] }
  ],
  nightOwl: [
    { name: 'option', label: '筛选选项', type: 'select', options: [
      { value: 0, label: '严格夜猫子(23:00-05:00)' },
      { value: 1, label: '宽松夜猫子(22:00-06:00)' }
    ] }
  ],
  deepVideo: [
    { name: 'videoId', label: '视频ID', type: 'input', inputType: 'number', placeholder: '请输入视频ID' },
    { name: 'option', label: '筛选选项', type: 'select', options: [
      { value: 0, label: '观看完成率>80%' },
      { value: 1, label: '观看完成率>50%' },
      { value: 2, label: '观看时长>10分钟' }
    ] }
  ],
  series: [
    { name: 'tagId', label: '标签ID', type: 'input', inputType: 'number', placeholder: '请输入标签ID' }
  ],
  // 责任链筛选参数与对应基础筛选相同
  chainSameUpVideoCount: [
    { name: 'upId', label: 'UP主ID', type: 'input', inputType: 'number', placeholder: '请输入UP主ID' },
    { name: 'ratioOption', label: '观看比例选项', type: 'select', options: [
      { value: 0, label: '大于20%' },
      { value: 1, label: '大于50%' },
      { value: 2, label: '大于80%' }
    ] }
  ],
  chainSameTagVideoCount: [
    { name: 'tagId', label: '标签ID', type: 'input', inputType: 'number', placeholder: '请输入标签ID' },
    { name: 'ratioOption', label: '观看比例选项', type: 'select', options: [
      { value: 0, label: '大于20%' },
      { value: 1, label: '大于50%' },
      { value: 2, label: '大于80%' }
    ] }
  ],
  chainDeepVideo: [
    { name: 'videoId', label: '视频ID', type: 'input', inputType: 'number', placeholder: '请输入视频ID' },
    { name: 'option', label: '筛选选项', type: 'select', options: [
      { value: 0, label: '观看完成率>80%' },
      { value: 1, label: '观看完成率>50%' },
      { value: 2, label: '观看时长>10分钟' }
    ] }
  ],
  chainSeries: [
    { name: 'tagId', label: '标签ID', type: 'input', inputType: 'number', placeholder: '请输入标签ID' }
  ]
}

// 获取筛选参数
function getFilterParams(filterType) {
  return filterParamsConfig[filterType] || []
}

// 切换责任链筛选
function toggleChainFilter(filterKey) {
  const index = selectedChainFilters.value.indexOf(filterKey)
  if (index > -1) {
    selectedChainFilters.value.splice(index, 1)
  } else {
    selectedChainFilters.value.push(filterKey)
  }
}

// 重置其他筛选参数
function resetOtherFilterParams() {
  otherFilterParams.value = {}
}

// 搜索防抖函数
function debounceSearch(type) {
  if (searchTimeout.value) {
    clearTimeout(searchTimeout.value)
  }

  searchTimeout.value = setTimeout(async () => {
    const keyword = searchKeywords.value[type]
    if (keyword.trim() === '') {
      type === 'video' ? videoSearchResults.value = [] : tagSearchResults.value = []
      return
    }

    try {
      if (type === 'video') {
        const results = await searchApi.searchVideos(keyword)
        videoSearchResults.value = results.data || []
      } else {
        const results = await searchApi.searchTags(keyword)
        tagSearchResults.value = results.data || []
      }
    } catch (error) {
      console.error(`搜索${type}失败:`, error)
      showMessage(`搜索${type}失败，请稍后重试`, 'error')
    }
  }, 500)
}

// 选择视频
function selectVideo(video) {
  // 清空搜索框和结果
  searchKeywords.value.video = ''
  videoSearchResults.value = []
}

// 选择标签
function selectTag(tag) {
  // 清空搜索框和结果
  searchKeywords.value.tag = ''
  tagSearchResults.value = []
}

// 检查筛选参数是否有效
function isFilterParamsValid() {
  // 责任链筛选不需要额外的ID参数，只需要activity和nightOwl参数（可选）
  return true
}

// 应用筛选
async function applyFilter() {
  // 检查是否选择了筛选条件
  if (selectedChainFilters.value.length === 0) {
    showMessage('请选择至少一个筛选条件', 'warning')
    return
  }

  // 检查参数有效性
  if (!isFilterParamsValid()) {
    showMessage('请填写完整的筛选条件', 'warning')
    return
  }

  loading.value = true
  message.value = ''

  try {
    // 处理责任链筛选（支持多选，结果合并）
    const chainResults = []

    for (const filterKey of selectedChainFilters.value) {
      let chainFilterResults

      // 根据筛选类型调用相应的API
      switch (filterKey) {
        case 'chainSameUpVideoCount':
          chainFilterResults = await filterApi.chainSameUpVideoCount({}, chainParams.value)
          break
        case 'chainSameTagVideoCount':
          chainFilterResults = await filterApi.chainSameTagVideoCount({ ...{}, ...chainParams.value })
          break
        case 'chainDeepVideo':
          chainFilterResults = await filterApi.chainDeepVideo({ ...{}, ...chainParams.value })
          break
        case 'chainSeries':
          chainFilterResults = await filterApi.chainSeries({ ...{}, ...chainParams.value })
          break
        default:
          throw new Error('未知的责任链筛选类型')
      }

      if (Array.isArray(chainFilterResults)) {
        chainResults.push(...chainFilterResults)
      }
    }

    // 去重并合并责任链筛选结果
    const uniqueUsers = new Map()
    chainResults.forEach(user => {
      if (!uniqueUsers.has(user.userId)) {
        uniqueUsers.set(user.userId, user)
      }
    })
    const results = Array.from(uniqueUsers.values())

    // 处理结果
    filterResults.value = Array.isArray(results) ? results : []

    if (filterResults.value.length > 0) {
      showMessage(`成功筛选出 ${filterResults.value.length} 位用户`, 'success')
    } else {
      showMessage('没有找到符合条件的用户', 'info')
    }
  } catch (error) {
    console.error('筛选失败:', error)
    showMessage('筛选失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

// 重置筛选条件
function resetFilters() {
  selectedChainFilters.value = []
  chainParams.value = { activity: 0, nightOwl: 0 }
  filterResults.value = []
  message.value = ''
  searchKeywords.value = { video: '', tag: '' }
  videoSearchResults.value = []
  tagSearchResults.value = []
}

// 发送好友请求
async function sendFriendRequest(targetUserId) {
  addingFriendId.value = targetUserId
  try {
    await friendApi.sendFriendRequest(targetUserId)
    showMessage('好友请求发送成功', 'success')
  } catch (error) {
    console.error('发送好友请求失败:', error)
    showMessage('发送好友请求失败，请稍后重试', 'error')
  } finally {
    addingFriendId.value = null
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
  const rect = event.currentTarget.getBoundingClientRect()
  tooltipStyle.value = {
    left: `${rect.left + rect.width / 2}px`,
    top: `${rect.top - 10}px`,
    transform: 'translateX(-50%) translateY(-100%)'
  }
}

// 隐藏悬停提示
function hideTooltip() {
  tooltipVisible.value = false
  tooltipUser.value = null
}

// 获取用户详细信息
function getDetailedInfo(user) {
  if (!user) return {}

  // 过滤掉不需要显示的字段
  const excludedFields = ['userId', 'username', 'avatar', 'sourceApis']
  const detailedInfo = {}

  for (const [key, value] of Object.entries(user)) {
    if (!excludedFields.includes(key)) {
      detailedInfo[key] = value
    }
  }

  return detailedInfo
}

// 格式化字段名
function formatKey(key) {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
}

// 格式化字段值
function formatValue(value, key) {
  if (value === null || value === undefined) return 'N/A'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (typeof value === 'number') {
    // 根据字段名进行特殊格式化
    if (key.includes('ratio') || key.includes('similarity') || key.includes('rate')) {
      return `${(value * 100).toFixed(1)}%`
    }
    return value.toString()
  }
  if (Array.isArray(value)) {
    return value.join(', ')
  }
  if (typeof value === 'object') {
    return JSON.stringify(value)
  }
  return value
}

// 根据API类型获取定制化的提示文本
function getCustomTooltipText(user) {
  if (!user || !user.sourceApis) return ''

  const tooltipTexts = []

  // 处理所有筛选来源API
  user.sourceApis.forEach(apiKey => {
    let text = ''

    switch (apiKey) {
      case 'sameTag':
        text = `<span class="highlight">${user.username}</span>与你观看过相同标签的视频，相似度为<span class="highlight-important">${user.similarityRate}</span>`
        break
      case 'sameUpVideoCount':
        text = `<span class="highlight">${user.username}</span>观看该UP主视频的比例为<span class="highlight-important">${user.ratio}%</span>`
        break
      case 'sameTagVideoCount':
        text = `<span class="highlight">${user.username}</span>观看该标签视频的比例为<span class="highlight-important">${user.ratio}%</span>`
        break
      case 'nightOwl':
        const nightOwlLevel = {
          0: '轻度',
          1: '中度',
          2: '重度'
        }[user.option] || '未知'
        text = `<span class="highlight">${user.username}</span>是<span class="highlight-important">${nightOwlLevel}夜猫子</span>，与你有相似的作息时间`
        break
      case 'deepVideo':
        text = `<span class="highlight">${user.username}</span>经常观看深度内容，观看时长超过<span class="highlight-important">${user.ratio}%</span>的用户`
        break
      case 'series':
        text = `<span class="highlight">${user.username}</span>与你都喜欢观看系列作品，共同观看过<span class="highlight-important">${user.seriesCount}</span>个系列的视频`
        break
      // 责任链筛选
      case 'chainSameUpVideoCount':
      case 'chainSameTagVideoCount':
      case 'chainDeepVideo':
      case 'chainSeries':
        // 责任链筛选可以根据具体情况定制显示文本
        text = `<span class="highlight">${user.username}</span>符合你的复合筛选条件，活跃度为<span class="highlight-important">${user.activity}</span>，夜猫子程度为<span class="highlight-important">${user.nightOwl}</span>`
        break
    }

    if (text) {
      tooltipTexts.push(text)
    }
  })

  // 如果有多个筛选条件，用换行符分隔显示
  if (tooltipTexts.length > 0) {
    return tooltipTexts.join('<br>')
  }

  // 默认返回空字符串，使用原来的详细信息显示
  return ''
}
</script>

<style scoped>
.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 30px;
  color: var(--text-primary);
  text-align: center;
}

/* 搜索区域样式 */
.search-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  justify-content: center;
}

.search-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 300px;
}

.search-label {
  font-size: var(--font-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.search-input-wrapper {
  position: relative;
}

.search-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  color: var(--text-primary);
  background-color: var(--bg-primary);
  transition: all var(--transition-fast);
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(251, 114, 153, 0.1);
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: 0 0 var(--radius-md) var(--radius-md);
  box-shadow: var(--shadow-medium);
  z-index: 100;
  max-height: 200px;
  overflow-y: auto;
}

.search-item {
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.search-item:hover {
  background-color: var(--bg-secondary);
}

/* 筛选区域样式 */
.filter-section {
  margin-bottom: 30px;
}

/* 责任链筛选标签样式 */
.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.filter-tag {
  padding: 8px 16px;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-full);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-size: var(--font-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.filter-tag:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.filter-tag.active {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
  color: var(--text-light);
}

/* 筛选组样式 */
.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.filter-label {
  font-size: var(--font-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.filter-select,
.filter-input {
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: var(--font-sm);
  color: var(--text-primary);
  background-color: var(--bg-primary);
  transition: all var(--transition-fast);
}

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(251, 114, 153, 0.1);
}

/* 筛选内容样式 */
.filter-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 责任链筛选样式 */
.chain-filters {
  margin-top: 20px;
  padding: 20px;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.chain-title {
  margin: 0 0 15px 0;
  font-size: var(--font-md);
  font-weight: 600;
  color: var(--text-primary);
}

.action-section {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  justify-content: center;
}

.results-section {
  margin-top: 30px;
}

.results-title {
  margin: 0 0 20px 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.user-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.user-card {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-light);
  transition: all var(--transition-normal);
  text-align: center;
  cursor: pointer;
}

.user-card:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-2px);
}

.user-avatar {
  width: 100px;
  height: 100px;
  margin: 0 auto 15px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid var(--primary-color);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  margin: 0 0 15px 0;
  font-size: var(--font-lg);
  font-weight: 600;
  color: var(--text-primary);
}

.empty-state {
  text-align: center;
  padding: 50px;
  color: var(--text-secondary);
  font-size: var(--font-md);
}

.message {
  position: fixed;
  top: 80px;
  right: 20px;
  padding: 12px 20px;
  border-radius: var(--radius-md);
  color: var(--text-light);
  font-weight: 500;
  z-index: 1000;
  transition: all var(--transition-fast);
}

.message.success {
  background-color: var(--success-color);
}

.message.error {
  background-color: var(--error-color);
}

.message.warning {
  background-color: var(--warning-color);
}

.message.info {
  background-color: var(--info-color);
}

/* 悬停提示样式 */
.tooltip {
  position: fixed;
  background-color: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-medium);
  padding: 12px;
  z-index: 1000;
  min-width: 200px;
  max-width: 300px;
  border: 1px solid var(--border-color);
}

.tooltip-header {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
  margin-bottom: 8px;
}

.tooltip-header h4 {
  margin: 0;
  font-size: var(--font-md);
  color: var(--text-primary);
}

.tooltip-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: var(--font-sm);
}

.tooltip-label {
  color: var(--text-secondary);
  font-weight: 500;
}

.tooltip-value {
  color: var(--text-primary);
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-container {
    padding: 10px;
  }

  .page-title {
    font-size: 1.5rem;
  }

  .user-grid {
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 15px;
  }

  .action-section {
    flex-direction: column;
  }
}
</style>
