<template>
  <div class="recommendations-page">
    <h2>好友推荐</h2>

    <div class="controls">
      <!-- 推荐API按钮组 -->
      <div class="recommend-api-buttons">
        <h3>选择推荐方式</h3>
        <div class="api-buttons">
          <button
            v-for="api in recommendApis"
            :key="api.key"
            @click="fetchRecommendByApi(api.key)"
            :disabled="loading || currentApi === api.key"
            :class="['api-btn', { active: currentApi === api.key }]"
          >
            {{ api.name }}
          </button>
        </div>
      </div>

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

    <!-- API推荐结果展示区域 -->
    <div v-if="currentApi" class="api-results-section">
      <h3>{{ apiResultsTitle }}</h3>
      <div v-if="apiResults.length > 0" class="user-cards">
        <div v-for="user in apiResults" :key="user.userId || user.id" class="user-card">
          <img :src="avatar(user)" alt="{{ user.username }}" class="avatar" />
          <div class="user-info">
            <div class="username">{{ user.username }}</div>
            <div class="user-stats">
              <span v-if="user.commonUpCount" class="stat-item">共同关注: {{ user.commonUpCount }}</span>
              <span v-if="user.sharedVideoCount" class="stat-item">共享视频: {{ user.sharedVideoCount }}</span>
              <span v-if="user.commentCount" class="stat-item">评论数: {{ user.commentCount }}</span>
              <span v-if="user.similarityRate" class="stat-item similarity-rate">相似度: {{ user.similarityRate }}%</span>
              <span v-if="user.score && !user.similarityRate" class="stat-item similarity-rate">匹配分数: {{ user.score }}</span>
            </div>
          </div>
          <button
            class="add-friend-btn"
            @click="sendFriendRequest(user.userId || user.id, user.username)"
            :disabled="isRequesting(user.userId || user.id)"
          >
            {{ getRequestStatus(user.userId || user.id) }}
          </button>
        </div>
      </div>
      <div v-else-if="loading" class="loading-placeholder">
        加载中...
      </div>
      <div v-else class="empty-state">
        暂无推荐结果
      </div>
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

    // 推荐API相关状态
    const currentApi = ref(null)
    const apiResults = ref([])
    const apiResultsTitle = ref('')

    // 推荐API列表
    const recommendApis = [
      { key: 'commonUp', name: '共同关注UP主', title: '共同关注UP主的用户推荐' },
      { key: 'sharedVideo', name: '共享视频', title: '观看视频相似度推荐' },
      { key: 'userBehavior', name: '用户行为相似度', title: '用户行为相似度推荐' },
      { key: 'coComment', name: '同视频评论', title: '同视频评论用户推荐' },
      { key: 'theme', name: '内容类型重合度', title: '内容类型重合度推荐' }
    ]

    // 模拟数据 - 每种推荐类型都有独特的模拟用户
    const mockData = {
      commonUp: [
        { userId: 101, username: '动漫爱好者小A', commonUpCount: 12, similarityRate: 85, commonUpNames: ['动漫小站', '二次元研究所'] },
        { userId: 102, username: '科技迷小王', commonUpCount: 8, similarityRate: 72, commonUpNames: ['科技最前线', '数码评测室'] },
        { userId: 103, username: '游戏玩家小李', commonUpCount: 15, similarityRate: 90, commonUpNames: ['游戏解说UP', '电竞俱乐部'] },
        { userId: 104, username: '音乐制作人小张', commonUpCount: 10, similarityRate: 78, commonUpNames: ['音乐教室', '乐器大师'] },
        { userId: 105, username: '美食博主小陈', commonUpCount: 13, similarityRate: 82, commonUpNames: ['美食家', '烘焙达人'] }
      ],
      sharedVideo: [
        { userId: 201, username: '电影收藏家老周', sharedVideoCount: 35, similarityRate: 88, lastWatched: '《星际穿越》影评' },
        { userId: 202, username: '音乐发烧友小吴', sharedVideoCount: 27, similarityRate: 76, lastWatched: '2024流行音乐盘点' },
        { userId: 203, username: '美食达人小赵', sharedVideoCount: 42, similarityRate: 92, lastWatched: '家常菜教程合集' },
        { userId: 204, username: '旅行摄影师小郑', sharedVideoCount: 31, similarityRate: 84, lastWatched: '环球旅行Vlog' },
        { userId: 205, username: '编程学习者小刘', sharedVideoCount: 29, similarityRate: 80, lastWatched: 'Python入门教程' }
      ],
      userBehavior: [
        { userId: 301, username: '夜猫子阿杰', score: 85, activeTime: '晚上22点-凌晨2点', behaviorType: '深度观看' },
        { userId: 302, username: '早鸟阿敏', score: 78, activeTime: '早上6点-9点', behaviorType: '快速浏览' },
        { userId: 303, username: '周末战士阿强', score: 91, activeTime: '周六日全天', behaviorType: '高互动' },
        { userId: 304, username: '上班族阿丽', score: 75, activeTime: '午休和下班后', behaviorType: '碎片时间' },
        { userId: 305, username: '学生党阿伟', score: 87, activeTime: '晚上8点-11点', behaviorType: '系统性学习' }
      ],
      coComment: [
        { userId: 401, username: '评论活跃者阿红', commentCount: 234, similarityRate: 79, avgCommentLength: 56, frequentTopics: ['电影讨论'] },
        { userId: 402, username: '深度思考者阿蓝', commentCount: 156, similarityRate: 83, avgCommentLength: 120, frequentTopics: ['技术分析'] },
        { userId: 403, username: '话题引导者阿紫', commentCount: 312, similarityRate: 87, avgCommentLength: 88, frequentTopics: ['创意讨论'] },
        { userId: 404, username: '幽默评论员阿黄', commentCount: 189, similarityRate: 77, avgCommentLength: 45, frequentTopics: ['搞笑梗'] },
        { userId: 405, username: '专业评论家阿绿', commentCount: 143, similarityRate: 85, avgCommentLength: 156, frequentTopics: ['专业评测'] }
      ],
      theme: [
        { userId: 501, username: '技术控阿牛', similarityRate: 89, favoriteThemes: ['人工智能', '前端开发'], contentPreference: '深度技术内容' },
        { userId: 502, username: '创意达人阿花', similarityRate: 77, favoriteThemes: ['设计', '创意'], contentPreference: '视觉创意内容' },
        { userId: 503, username: '生活方式博主阿草', similarityRate: 82, favoriteThemes: ['生活技巧', '家居'], contentPreference: '实用生活内容' },
        { userId: 504, username: '健康爱好者阿树', similarityRate: 79, favoriteThemes: ['健身', '营养'], contentPreference: '健康养生内容' },
        { userId: 505, username: '财经达人阿金', similarityRate: 84, favoriteThemes: ['投资', '理财'], contentPreference: '财经分析内容' }
      ]
    }

    // 生成随机头像
      function avatar(u) {
        if (u && u.avatar) return u.avatar
        const name = u && u.username ? u.username : 'User'
        const colors = ['#4285F4', '#EA4335', '#FBBC05', '#34A853', '#673AB7', '#FF9800']
        const color = colors[name.charCodeAt(0) % colors.length]
        const initial = name.charAt(0).toUpperCase()
        return `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40"><circle cx="20" cy="20" r="20" fill="${color}"/><text x="50%" y="50%" font-size="16" text-anchor="middle" dy=".3em" fill="white">${initial}</text></svg>`
      }

      // 根据API类型获取推荐数据
      async function fetchRecommendByApi(apiKey) {
        // 确保当前API已设置
        currentApi.value = apiKey
        // 清空之前的结果
        apiResults.value = []

        // 找到对应的API配置
        const apiConfig = recommendApis.find(api => api.key === apiKey)
        if (apiConfig) {
          apiResultsTitle.value = apiConfig.title
        }

        showNotification(`正在获取${apiConfig?.name || apiKey}推荐...`, 'info')

        try {
          // 确保模拟数据存在，如果不存在则记录警告
          if (!mockData[apiKey]) {
            console.warn(`警告: ${apiKey} 的模拟数据不存在`)
            // 创建空数组防止后续错误
            mockData[apiKey] = []
          }

          let result
          const id = userStore.userId || 1

          // 根据API类型调用不同的接口
          switch (apiKey) {
            case 'commonUp':
              result = await recommendStore.fetchCommonUp(id)
              break
            case 'sharedVideo':
              result = await recommendStore.fetchSharedVideo(id)
              break
            case 'userBehavior':
              result = await recommendStore.fetchUserBehavior(id)
              break
            case 'coComment':
              result = await recommendStore.fetchCoComment(id)
              break
            case 'theme':
              result = await recommendStore.fetchTheme(id)
              break
            default:
              throw new Error(`未知的API类型: ${apiKey}`)
          }

          // 验证结果有效性
          const isValidResult = result && Array.isArray(result) && result.length > 0

          if (isValidResult) {
            // 处理并显示真实数据
            apiResults.value = result.map(item => ({
              ...item,
              // 确保所有结果都有必要的字段
              username: item.username || `用户${item.userId || item.id}`,
              similarityRate: item.similarityRate || item.score || 0
            }))
            showNotification(`${apiConfig?.name}推荐获取成功`, 'success')
          } else {
            // 当API返回空结果时，使用模拟数据
            console.log(`API返回空结果，使用模拟数据`)
            useMockDataForApi(apiKey, apiConfig)
          }
        } catch (error) {
          // 记录详细错误信息
          console.error(`获取${apiKey}推荐失败:`, error)
          // 无论何种错误，都使用模拟数据
          useMockDataForApi(apiKey, apiConfig, true)
        }
      }

      // 专用函数：使用指定API类型的模拟数据
      function useMockDataForApi(apiKey, apiConfig, isError = false) {
        // 确保模拟数据存在
        if (mockData[apiKey] && mockData[apiKey].length > 0) {
          // 复制模拟数据以避免直接引用
          apiResults.value = JSON.parse(JSON.stringify(mockData[apiKey]))

          // 根据情况显示不同的通知
          if (isError) {
            showNotification(`加载失败，使用${apiConfig?.name || apiKey}模拟数据`, 'warning')
          } else {
            showNotification(`暂无真实数据，使用${apiConfig?.name || apiKey}模拟数据`, 'info')
          }
        } else {
          // 如果连模拟数据都没有，创建一个默认的模拟用户
          const defaultMockUser = {
            userId: Date.now(),
            username: `模拟用户_${apiKey}`,
            similarityRate: Math.floor(Math.random() * 30) + 70, // 70-99的随机相似度
            isDefaultMock: true
          }
          apiResults.value = [defaultMockUser]
          showNotification(`暂无数据，显示默认模拟用户`, 'info')
        }
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
          showNotification('所有推荐数据加载完成', 'success')
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

  /* API推荐按钮组样式 */
  .recommend-api-buttons {
    margin-bottom: 20px;
  }

  .api-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-top: 10px;
  }

  .api-btn {
    padding: 8px 16px;
    border: 1px solid #ccc;
    background-color: #fff;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .api-btn:hover:not(:disabled) {
    background-color: #f0f0f0;
    border-color: #999;
  }

  .api-btn.active {
    background-color: #1890ff;
    color: white;
    border-color: #1890ff;
  }

  .api-btn:disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  /* API结果展示区域样式 */
  .api-results-section {
    margin-top: 30px;
    padding: 20px;
    background-color: #fafafa;
    border-radius: 8px;
  }

  .api-results-section h3 {
    margin-top: 0;
    color: #333;
    font-size: 18px;
    margin-bottom: 15px;
  }

  .user-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 15px;
  }

  .user-card {
    background-color: white;
    border-radius: 8px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    gap: 15px;
  }

  .user-info {
    flex: 1;
  }

  .user-stats {
    margin-top: 8px;
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    font-size: 14px;
    color: #666;
  }

  .stat-item {
    display: inline-block;
  }

  .similarity-rate {
    color: #52c41a;
    font-weight: 500;
  }

  .loading-placeholder, .empty-state {
    text-align: center;
    padding: 40px;
    color: #999;
    font-size: 16px;
  }

  .add-friend-btn {
    padding: 6px 12px;
    background-color: #1890ff;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s ease;
  }

  .add-friend-btn:hover:not(:disabled) {
    background-color: #40a9ff;
  }

  .add-friend-btn:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
}
</style>
