<template>
  <div class="friends-container">
    <div class="friends-header">
      <h1>我的好友</h1>
      <div class="header-actions">
        <Button type="primary" size="medium" @click="showAddFriendModal = true">
          添加好友
        </Button>
      </div>
    </div>

    <!-- 好友分类标签 -->
    <div class="friends-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'all' }"
        @click="activeTab = 'all'"
      >
        全部好友
        <span class="tab-count">({{ filteredFriends?.length || 0 }})</span>
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'online' }"
        @click="activeTab = 'online'"
      >
        在线好友
        <span class="tab-count">({{ onlineFriends?.length || 0 }})</span>
      </button>
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'unread' }"
        @click="activeTab = 'unread'"
      >
        未读消息
        <span class="tab-count">({{ unreadFriends?.length || 0 }})</span>
      </button>
    </div>

    <!-- 好友列表 -->
    <div class="friends-list">
      <Card class="friend-card" v-for="friend in displayFriends" :key="friend.id">
        <div class="friend-card-content">
          <div class="friend-avatar-container">
            <img :src="friend.avatar" alt="好友头像" class="friend-avatar" />
            <div :class="['online-status', { online: friend.status === 'online' }]"></div>
          </div>

          <div class="friend-info">
            <div class="friend-main-info">
              <h3 class="friend-name">{{ friend.username }}</h3>
              <span v-if="chatStore && chatStore.getUnreadCount && chatStore.getUnreadCount(friend.id) > 0" class="unread-badge">{{ chatStore.getUnreadCount(friend.id) }}</span>
            </div>
            <p class="friend-bio">{{ friend.bio || '这个人很懒，什么都没有写~' }}</p>
            <div class="friend-meta">
              <span class="last-seen">{{ formatLastSeen(friend.lastSeen) }}</span>
              <span class="friend-level">Lv.{{ friend.level || 1 }}</span>
            </div>
          </div>

          <div class="friend-actions">
            <Button
              type="primary"
              size="small"
              @click="startChat(friend.id)"
              :disabled="friend.status !== 'online'"
            >
              {{ friend.status === 'online' ? '发消息' : '离线' }}
            </Button>
            <Button
              type="outline"
              size="small"
              @click="viewProfile(friend.id)"
            >
              查看资料
            </Button>
            <Button
              type="success"
              size="small"
              @click="testChat(friend.id, friend.username)"
            >
              测试聊天
            </Button>
          </div>
        </div>
      </Card>
    </div>

    <!-- 空状态 -->
    <div v-if="(!displayFriends || displayFriends.length === 0)" class="empty-state">
      <div class="empty-icon">👥</div>
      <h3>暂无好友</h3>
      <p>{{ getEmptyStateMessage() }}</p>
      <Button v-if="activeTab === 'all'" type="primary" @click="showAddFriendModal = true">
        添加好友
      </Button>
    </div>

    <!-- 添加好友弹窗 -->
    <div v-if="showAddFriendModal" class="modal-overlay" @click.self="closeAddFriendModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加好友</h3>
          <button class="close-btn" @click="closeAddFriendModal">×</button>
        </div>
        <div class="modal-body">
          <div class="search-section">
            <h4>通过用户ID查找</h4>
            <div class="search-container">
              <input
                type="text"
                v-model="userIdQuery"
                placeholder="输入用户ID..."
                class="search-input"
                @keyup.enter="searchUserById"
              />
              <i class="search-icon">🔍</i>
            </div>
            <Button
              type="primary"
              @click="searchUserById"
              :disabled="searchingById || !userIdQuery.trim()"
              style="margin-top: 10px; width: 100%;"
              size="medium"
            >
              {{ searchingById ? '搜索中...' : '搜索' }}
            </Button>
          </div>

          <div v-if="idSearchError" class="search-error" style="margin-top: 15px;">
            {{ idSearchError }}
          </div>

          <div v-if="searchedUser" class="user-info-card">
            <div class="user-info-header">
              <img
                :src="searchedUser.avatar || generateRandomAvatar(searchedUser.id)"
                alt="用户头像"
                class="user-avatar"
              />
              <div class="user-main-info">
                <h3>{{ searchedUser.username || searchedUser.name }}</h3>
                <p>UID: {{ searchedUser.id }}</p>
              </div>
              <Button
                :type="isFriend(searchedUser.id) ? 'secondary' : 'primary'"
                size="small"
                :disabled="isFriend(searchedUser.id)"
                @click="addFriend(searchedUser.id)"
              >
                {{ isFriend(searchedUser.id) ? '已是好友' : '添加好友' }}
              </Button>
            </div>

            <div class="user-details">
              <p v-if="searchedUser.bio" class="user-bio">{{ searchedUser.bio }}</p>
              <p v-else class="user-bio">这个人很懒，什么都没有写~</p>
              <div class="user-stats">
                <div class="stat-item">
                  <span class="stat-label">注册时间：</span>
                  <span class="stat-value">{{ formatDate(searchedUser.createdAt) }}</span>
                </div>
                <div class="stat-item" v-if="searchedUser.level">
                  <span class="stat-label">等级：</span>
                  <span class="stat-value">Lv.{{ searchedUser.level }}</span>
                </div>
                <div class="stat-item" v-if="searchedUser.status">
                  <span class="stat-label">状态：</span>
                  <span :class="['stat-value', { 'online': searchedUser.status === 'online' }]">
                    {{ searchedUser.status === 'online' ? '在线' : '离线' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="searchingById" class="searching-indicator">
            <div class="loading-spinner"></div>
            <span>查找中...</span>
          </div>
        </div>
      </div>
    </div>


  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { friendApi, userApi } from '../services/api'
import Card from '../components/Card.vue'
import Button from '../components/Button.vue'

export default {
  name: 'Friends',
  components: {
    Card,
    Button
  },
  setup() {
    const router = useRouter()
    const chatStore = useChatStore()

    // 聊天功能已迁移到Chat.vue页面

    // 响应式数据
    const activeTab = ref('all')
    const showAddFriendModal = ref(false)
    // 用户ID搜索相关
    const userIdQuery = ref('')
    const searchedUser = ref(null)
    const searchingById = ref(false)
    const idSearchError = ref('')

    // 计算属性
    const filteredFriends = computed(() => {
      // 添加空值检查，确保friends始终是数组
      let friends = Array.isArray(chatStore.friends) ? chatStore.friends : []

      return friends
    })

    const onlineFriends = computed(() => {
      return filteredFriends.value.filter(friend => friend.status === 'online')
    })

    const unreadFriends = computed(() => {
      if (!chatStore || !chatStore.getUnreadCount) {
        return []
      }
      return filteredFriends.value.filter(friend => {
        try {
          return chatStore.getUnreadCount(friend.id) > 0
        } catch {
          return false
        }
      })
    })

    const displayFriends = computed(() => {
      switch (activeTab.value) {
        case 'online':
          return Array.isArray(onlineFriends.value) ? onlineFriends.value : []
        case 'unread':
          return Array.isArray(unreadFriends.value) ? unreadFriends.value : []
        default:
          return Array.isArray(filteredFriends.value) ? filteredFriends.value : []
      }
    })

    // 方法
    const startChat = (friendId) => {
      // 导航到聊天页面并传递好友ID
      router.push({ path: '/chat', query: { userId: friendId } })
    }

    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '未知'

      const date = new Date(dateString)
      return date.toLocaleDateString()
    }

    // 格式化最后在线时间
    const formatLastSeen = (timestamp) => {
      if (!timestamp) return '未知'

      const now = new Date()
      const lastSeen = new Date(timestamp)
      const diff = now - lastSeen

      const minutes = Math.floor(diff / (1000 * 60))
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(diff / (1000 * 60 * 60 * 24))

      if (minutes < 1) return '刚刚在线'
      if (minutes < 60) return `${minutes}分钟前在线`
      if (hours < 24) return `${hours}小时前在线`
      if (days < 7) return `${days}天前在线`

      return lastSeen.toLocaleDateString()
    }

    // 格式化消息时间
    const formatMessageTime = (timestamp) => {
      if (!timestamp) return ''

      const date = new Date(timestamp)
      return date.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    }

    const viewProfile = (userId) => {
      // 这里可以实现查看好友个人资料的逻辑
      console.log('查看好友资料:', userId)
    }

    const closeAddFriendModal = () => {
      showAddFriendModal.value = false
      // 重置ID搜索相关数据
      userIdQuery.value = ''
      searchedUser.value = null
      idSearchError.value = ''
    }

    // 通过用户ID查找用户
    const searchUserById = async () => {
      if (!userIdQuery.value.trim()) {
        idSearchError.value = '请输入用户ID'
        return
      }

      // 验证ID是否为数字
      const userId = parseInt(userIdQuery.value.trim())
      if (isNaN(userId)) {
        idSearchError.value = '请输入有效的用户ID'
        return
      }

      searchingById.value = true
      idSearchError.value = ''
      searchedUser.value = null

      try {
        // 调用获取用户信息API
        const userInfo = await userApi.getUserInfo(userId)

        // 确保用户有头像
        if (userInfo) {
          // 确保搜索结果中包含正确的用户ID和用户名
          searchedUser.value = {
            ...userInfo,
            // 确保用户ID存在且为数字类型
            id: userId,
            // 确保用户名存在
            username: userInfo.data.username || userInfo.name || `用户${userId}`,
            // 确保头像存在
            avatar: userInfo.avatar || generateRandomAvatar(userInfo.username || userInfo.name || userId.toString())
          }
          // 日志显示搜索结果，包含用户ID和用户名
          console.log('搜索结果:', {
            userId: searchedUser.value.id,
            username: searchedUser.value.username
          })
        } else {
          idSearchError.value = '未找到该用户'
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        idSearchError.value = error.message || '查找失败，请稍后重试'
      } finally {
        searchingById.value = false
      }
    }

    const searchUsers = async () => {
      if (!addFriendQuery.value.trim()) {
        searchError.value = '请输入搜索内容'
        return
      }

      searching.value = true
      searchError.value = ''
      searchResults.value = []

      try {
        // 调用搜索用户API
        const response = await friendApi.searchUsers(addFriendQuery.value)
        if (response && response.users) {
          searchResults.value = response.users
          if (response.users.length === 0) {
            searchError.value = '未找到相关用户'
          }
        } else {
          searchError.value = '搜索失败，请稍后重试'
        }
      } catch (error) {
        console.error('搜索用户失败:', error)
        searchError.value = error.message || '搜索失败，请稍后重试'
      } finally {
        searching.value = false
      }
    }

    const isFriend = (userId) => {
      return Array.isArray(chatStore.friends) && chatStore.friends.some(friend => friend.id === userId)
    }

    // 添加防止重复点击的状态
    const isSendingRequest = ref(false)

    const addFriend = async (userId) => {
      // 防止重复点击
      if (isSendingRequest.value) {
        return
      }

      isSendingRequest.value = true
      try {
        // 调用发送好友申请API
        const response = await friendApi.sendFriendRequest(userId)

        // 显示成功消息（使用ID搜索的错误提示区域）
        idSearchError.value = '好友申请已发送！'

        // 添加友好的消息提示，包括用户名信息
        if (searchedUser.value) {
          console.log(`已向 ${searchedUser.value.username || searchedUser.value.name || '用户'} (ID: ${userId}) 发送好友申请`)
        }

        setTimeout(() => {
          idSearchError.value = ''
        }, 2000)

        // 可以选择在发送申请后清空搜索结果
        setTimeout(() => {
          searchedUser.value = null
        }, 2500)
      } catch (error) {
        console.error('发送好友申请失败:', error)
        // 提供更具体的错误信息
        if (error.message === '用户未登录，请先登录') {
          idSearchError.value = '请先登录后再发送好友申请'
        } else if (error.message && error.message.includes('Duplicate entry')) {
          // 处理重复添加好友的情况
          idSearchError.value = '已经向该用户发送过好友申请或已是好友'
        } else {
          idSearchError.value = error.message || '发送好友申请失败'
        }
      } finally {
        // 确保状态重置
        isSendingRequest.value = false
      }
    }

    const getEmptyStateMessage = () => {
      switch (activeTab.value) {
        case 'online':
          return '当前没有在线好友'
        case 'unread':
          return '没有未读消息'
        default:
          return '添加你的第一个好友开始聊天吧！'
      }
    }

    // 测试聊天按钮点击事件
    const testChat = (friendId, friendName) => {
      // 直接跳转到专门的聊天页面，传递好友ID作为参数
      router.push(`/chat?friendId=${friendId}&friendName=${encodeURIComponent(friendName)}`)
    }

    // 发送消息
    const sendMessage = () => {
      if (!newMessage.value.trim() || !currentChatFriend.value) return

      sendTestMessage(
        newMessage.value.trim(),
        currentChatFriend.value.id,
        currentChatFriend.value.name
      )
    }

    // 生成随机头像
    const generateRandomAvatar = (name) => {
      // 使用用户名生成种子，确保同一个用户总是得到相同的头像
      const seed = name ? name.toLowerCase().replace(/\s+/g, '-') : 'default'
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`
    }

    // 获取好友列表
    const fetchFriends = async () => {
      try {
        // 先检查localStorage中是否有用户信息
        const userStr = localStorage.getItem('user')
        let userId = null

        if (userStr) {
          try {
            const userInfo = JSON.parse(userStr)
            userId = userInfo.userId || localStorage.getItem('userId')
          } catch (e) {
            console.error('解析用户信息失败:', e)
          }
        }

        // 如果用户未登录，不调用API，避免显示错误
        if (!userId) {
          console.log('用户未登录，跳过获取好友列表')
          return
        }

        const response = await friendApi.getFriends()
        console.log('API返回的好友列表数据:', response)

        // 灵活处理返回数据，支持直接数组或嵌套在friends字段中的数组
        let friendsData = []
        if (response) {
          if (Array.isArray(response)) {
            friendsData = response
          } else if (Array.isArray(response.friends)) {
            friendsData = response.friends
          }
        }

        // 清空现有好友列表
        chatStore.friends = []

        // 数据转换：将API返回数据映射为组件模板所需格式
        friendsData.forEach(friend => {
          // 创建转换后的好友对象
          const transformedFriend = {
            // 确保用户ID存在
            id: friend.id || friend.userId || '',
            // 确保用户名存在
            username: friend.username || friend.name || `用户${friend.id || friend.userId || ''}`,
            // 确保头像存在
            avatar: friend.avatar || friend.avatarPath || generateRandomAvatar(friend.username || friend.name || friend.id || friend.userId || 'default'),
            // 确保状态字段存在，默认为离线
            status: friend.status || 'offline',
            // 将registerTime映射为lastSeen，用于显示最后在线时间
            lastSeen: friend.lastSeen || friend.registerTime || new Date().toISOString(),
            // 确保简介字段存在
            bio: friend.bio || '这个人很懒，什么都没有写~',
            // 确保等级字段存在
            level: friend.level || 1
          }

          // 添加转换后的好友到store
          chatStore.addFriend(transformedFriend)
        })

        if (friendsData.length === 0) {
          console.warn('获取到的好友列表为空')
        }
      } catch (error) {
        console.error('获取好友列表失败:', error)
        // 静默处理错误，避免影响页面显示
      }
    }

    onMounted(() => {
      // 尝试从API获取好友数据
      fetchFriends()

      // 聊天功能已迁移到Chat.vue页面
    })

    return {
      activeTab,
      displayFriends,
      onlineFriends,
      unreadFriends,
      showAddFriendModal,
      // 用户ID搜索相关
      userIdQuery,
      searchedUser,
      searchingById,
      idSearchError,
      startChat,
      viewProfile,
      closeAddFriendModal,
      searchUserById,
      isFriend,
      addFriend,
      getEmptyStateMessage,
      formatLastSeen,
      formatDate,
      generateRandomAvatar,
      // 简化的聊天入口
      testChat
  }
  }
}
</script>

<style scoped>
.friends-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  min-height: 100vh;
  background-color: var(--background-color);
}

.friends-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.friends-header h1 {
  margin: 0;
  font-size: 28px;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input {
  padding: 8px 35px 8px 15px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  background-color: white;
  font-size: 14px;
  transition: border-color 0.3s;
  min-width: 200px;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.2);
}

.search-icon {
  position: absolute;
  right: 10px;
  color: var(--text-secondary);
  font-size: 16px;
  pointer-events: none;
}



/* 好友分类标签 */
.friends-tabs {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  background-color: white;
  padding: 10px;
  border-radius: var(--border-radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tab-btn {
  padding: 8px 20px;
  background: none;
  border: none;
  border-radius: var(--border-radius);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.tab-btn:hover {
  color: var(--primary-color);
  background-color: var(--hover-background);
}

.tab-btn.active {
  color: white;
  background-color: var(--primary-color);
}

.tab-count {
  font-size: 12px;
  opacity: 0.8;
}

/* 好友列表 */
.friends-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.friend-card {
  overflow: hidden;
}

.friend-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  transition: background-color 0.3s;
}

.friend-card-content:hover {
  background-color: var(--hover-background);
}

.friend-avatar-container {
  position: relative;
}

.friend-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-color);
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: #ccc;
  border: 2px solid white;
}

.online-status.online {
  background-color: #52c41a;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.friend-main-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.friend-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-badge {
  background-color: var(--danger-color);
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
}

.friend-bio {
  margin: 5px 0;
  font-size: 14px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.friend-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 12px;
  color: var(--text-secondary);
}

.friend-level {
  color: var(--primary-color);
  font-weight: 600;
}

.friend-actions {
  display: flex;
  gap: 10px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: var(--text-primary);
}

.empty-state p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
}

/* 搜索区域样式 */
.search-section {
  margin-bottom: 25px;
}

.search-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 16px;
  color: var(--text-primary);
}

/* 分割线 */
.section-divider {
  position: relative;
  height: 1px;
  background-color: var(--border-color);
  margin: 25px 0;
  text-align: center;
}

.section-divider span {
  position: relative;
  top: -10px;
  padding: 0 15px;
  background-color: white;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 用户信息卡片 */
.user-info-card {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: var(--border-radius);
  border: 1px solid var(--border-color);
}

.user-info-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid var(--border-color);
}

.user-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.user-main-info {
  flex: 1;
}

.user-main-info h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.user-main-info p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.user-details {
  padding: 10px 0;
}

.user-bio {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.user-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.stat-item {
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.stat-label {
  color: var(--text-secondary);
}

.stat-value {
  color: var(--text-primary);
  font-weight: 500;
}

.stat-value.online {
  color: #52c41a;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: var(--border-radius);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: var(--text-secondary);
  cursor: pointer;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: var(--hover-background);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

.search-results {
  margin-top: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  margin-bottom: 10px;
  transition: all 0.3s;
}

.search-result-item:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px rgba(251, 114, 153, 0.15);
}

.search-result-item.is-friend {
  opacity: 0.6;
}

.search-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
}

.search-user-info {
  flex: 1;
}

.search-user-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: var(--text-primary);
}

.search-user-info p {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.searching-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-top: 2px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.search-error {
  margin-top: 15px;
  padding: 10px;
  background-color: rgba(245, 34, 45, 0.1);
  color: var(--danger-color);
  border-radius: var(--border-radius);
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-header {
    flex-direction: column;
    text-align: center;
  }

  .user-stats {
    justify-content: center;
    flex-direction: column;
    align-items: center;
    gap: 10px;
  }

  .stat-item {
    justify-content: center;
  }
  .friends-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .header-actions {
    flex-direction: column;
  }

  .search-container {
    width: 100%;
  }

  .search-input {
    width: 100%;
    min-width: unset;
  }

  .friends-tabs {
    overflow-x: auto;
    padding: 10px 5px;
    gap: 10px;
  }

  .friend-card-content {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .friend-avatar-container {
    margin: 0 auto;
  }

  .friend-actions {
    justify-content: center;
  }

  .modal-content {
    width: 95%;
    margin: 20px;
  }
}
</style>
