<template>
  <div class="filter-container">
    <h1 class="page-title">用户筛选系统</h1>

    <!-- 筛选条件选择区域 -->
    <div class="filter-section">
      <Card title="筛选条件" shadow>
        <div class="filter-options">
          <!-- 筛选类型选择 -->
          <div class="filter-group">
            <label class="filter-label">筛选类型</label>
            <select v-model="selectedFilterType" class="filter-select">
              <option value="">请选择筛选类型</option>
              <option v-for="filter in filterTypes" :key="filter.key" :value="filter.key">
                {{ filter.name }}
              </option>
            </select>
          </div>

          <!-- 动态筛选条件 -->
          <div v-if="selectedFilterType" class="dynamic-filters">
            <div v-for="param in getFilterParams(selectedFilterType)" :key="param.name" class="filter-group">
              <label class="filter-label">{{ param.label }}</label>
              <template v-if="param.type === 'input'">
                <input
                  v-model="filterParams[param.name]"
                  :type="param.inputType || 'text'"
                  class="filter-input"
                  :placeholder="param.placeholder"
                >
              </template>
              <template v-else-if="param.type === 'select'">
                <select v-model="filterParams[param.name]" class="filter-select">
                  <option value="">请选择</option>
                  <option v-for="option in param.options" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </template>
            </div>
          </div>

          <!-- 责任链二次筛选选项 -->
          <div v-if="selectedFilterType && isChainSupported(selectedFilterType)" class="chain-filters">
            <h3 class="chain-title">责任链二次筛选</h3>
            <div class="filter-group">
              <label class="filter-label">活跃度筛选</label>
              <select v-model="chainParams.activity" class="filter-select">
                <option value="0">不筛选</option>
                <option value="1">高活跃度</option>
                <option value="2">中活跃度</option>
                <option value="3">低活跃度</option>
              </select>
            </div>
            <div class="filter-group">
              <label class="filter-label">夜猫子筛选</label>
              <select v-model="chainParams.nightOwl" class="filter-select">
                <option value="0">不筛选</option>
                <option value="1">是夜猫子</option>
                <option value="2">不是夜猫子</option>
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
          :disabled="!selectedFilterType || !isFilterParamsValid()"
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
import { filterApi, friendApi } from '@/services/api'
import Card from '@/components/Card.vue'
import Button from '@/components/Button.vue'
import { getUserAvatar } from '@/utils/avatar'

// 响应式数据
const selectedFilterType = ref('')
const filterParams = ref({})
const chainParams = ref({ activity: 0, nightOwl: 0 })
const filterResults = ref([])
const loading = ref(false)
const message = ref('')
const messageType = ref('info')
const tooltipVisible = ref(false)
const tooltipUser = ref(null)
const tooltipStyle = ref({})
const addingFriendId = ref(null)

// 筛选类型配置
const filterTypes = [
  { key: 'sameTag', name: '同一标签筛选', endpoint: '/api/filter/same-tag' },
  { key: 'sameUpVideoCount', name: 'UP主视频观看比例筛选', endpoint: '/api/filter/same-up-video-count' },
  { key: 'sameTagVideoCount', name: '标签视频观看比例筛选', endpoint: '/api/filter/same-tag-video-count' },
  { key: 'nightOwl', name: '夜猫子用户筛选', endpoint: '/api/filter/night-owl' },
  { key: 'deepVideo', name: '深度视频筛选', endpoint: '/api/filter/deep-video' },
  { key: 'series', name: '系列作品筛选', endpoint: '/api/filter/series' },
  // 责任链筛选
  { key: 'chainSameUpVideoCount', name: 'UP主视频观看比例 + 责任链筛选', endpoint: '/api/chain/same-up-video-count' },
  { key: 'chainSameTagVideoCount', name: '标签视频观看比例 + 责任链筛选', endpoint: '/api/chain/same-tag-video-count' },
  { key: 'chainDeepVideo', name: '深度视频筛选 + 责任链筛选', endpoint: '/api/chain/deep-video' },
  { key: 'chainSeries', name: '系列作品筛选 + 责任链筛选', endpoint: '/api/chain/series' }
]

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

// 检查是否支持责任链
function isChainSupported(filterType) {
  return filterType.startsWith('chain')
}

// 检查筛选参数是否有效
function isFilterParamsValid() {
  if (!selectedFilterType.value) return false

  const params = getFilterParams(selectedFilterType.value)
  for (const param of params) {
    if (filterParams.value[param.name] === undefined || filterParams.value[param.name] === null || filterParams.value[param.name] === '') {
      return false
    }
  }
  return true
}

// 应用筛选
async function applyFilter() {
  if (!selectedFilterType.value || !isFilterParamsValid()) {
    showMessage('请填写完整的筛选条件', 'warning')
    return
  }

  loading.value = true
  message.value = ''

  try {
    let results
    const isChainFilter = selectedFilterType.value.startsWith('chain')

    // 根据筛选类型调用相应的API
    switch (selectedFilterType.value) {
      case 'sameTag':
        results = await filterApi.sameTag(filterParams.value)
        break
      case 'sameUpVideoCount':
        results = await filterApi.sameUpVideoCount(filterParams.value)
        break
      case 'sameTagVideoCount':
        results = await filterApi.sameTagVideoCount(filterParams.value)
        break
      case 'nightOwl':
        results = await filterApi.nightOwl(filterParams.value)
        break
      case 'deepVideo':
        results = await filterApi.deepVideo(filterParams.value)
        break
      case 'series':
        results = await filterApi.series(filterParams.value)
        break
      // 责任链筛选
      case 'chainSameUpVideoCount':
        results = await filterApi.chainSameUpVideoCount(filterParams.value, chainParams.value)
        break
      case 'chainSameTagVideoCount':
        results = await filterApi.chainSameTagVideoCount({ ...filterParams.value, ...chainParams.value })
        break
      case 'chainDeepVideo':
        results = await filterApi.chainDeepVideo({ ...filterParams.value, ...chainParams.value })
        break
      case 'chainSeries':
        results = await filterApi.chainSeries({ ...filterParams.value, ...chainParams.value })
        break
      default:
        throw new Error('未知的筛选类型')
    }

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
  selectedFilterType.value = ''
  filterParams.value = {}
  chainParams.value = { activity: 0, nightOwl: 0 }
  filterResults.value = []
  message.value = ''
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

.filter-section {
  margin-bottom: 30px;
}

.filter-options {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.dynamic-filters {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed var(--border-color);
}

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
