<template>
  <div class="friends-container">
    <div class="friends-header">
      <h1>我的好友</h1>
      <div class="header-actions">
        <div class="search-container">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="搜索好友..." 
            class="search-input"
          />
          <i class="search-icon">🔍</i>
        </div>
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
          <div class="search-container">
            <input 
              type="text" 
              v-model="addFriendQuery" 
              placeholder="输入好友昵称或ID..." 
              class="search-input"
              @keyup.enter="searchUsers"
            />
            <i class="search-icon">🔍</i>
          </div>
          
          <div v-if="searchResults?.length > 0" class="search-results">
            <div 
              v-for="user in searchResults" 
              :key="user.id" 
              class="search-result-item"
              :class="{ 'is-friend': isFriend(user.id) }"
            >
              <img :src="user.avatar" alt="用户头像" class="search-avatar" />
              <div class="search-user-info">
                <h4>{{ user.name }}</h4>
                <p>UID: {{ user.id }}</p>
              </div>
              <Button 
                :type="isFriend(user.id) ? 'secondary' : 'primary'" 
                size="small" 
                :disabled="isFriend(user.id)"
                @click="addFriend(user.id)"
              >
                {{ isFriend(user.id) ? '已是好友' : '添加好友' }}
              </Button>
            </div>
          </div>
          
          <div v-if="searching" class="searching-indicator">
            <div class="loading-spinner"></div>
            <span>搜索中...</span>
          </div>
          
          <div v-if="searchError" class="search-error">
            {{ searchError }}
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
    
    // 响应式数据
    const searchQuery = ref('')
    const activeTab = ref('all')
    const showAddFriendModal = ref(false)
    const addFriendQuery = ref('')
    const searchResults = ref([])
    const searching = ref(false)
    const searchError = ref('')

    // 计算属性
    const filteredFriends = computed(() => {
      // 添加空值检查，确保friends始终是数组
      let friends = Array.isArray(chatStore.friends) ? chatStore.friends : []
      
      // 根据搜索词过滤
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        friends = friends.filter(friend => 
          friend.username?.toLowerCase().includes(query) ||
          friend.id?.toString().includes(query)
        )
      }
      
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
        } catch (e) {
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
    const startChat = (userId) => {
      // 根据路由配置，只导航到/chat页面，不传递userId参数
      router.push('/chat')
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

    const viewProfile = (userId) => {
      // 这里可以实现查看好友个人资料的逻辑
      console.log('查看好友资料:', userId)
    }

    const closeAddFriendModal = () => {
      showAddFriendModal.value = false
      addFriendQuery.value = ''
      searchResults.value = []
      searchError.value = ''
    }

    const searchUsers = () => {
      if (!addFriendQuery.value.trim()) {
        searchError.value = '请输入搜索内容'
        return
      }

      searching.value = true
      searchError.value = ''
      searchResults.value = []

      // 模拟搜索延迟
      setTimeout(() => {
        // 模拟搜索结果
        const mockResults = [
          {
            id: 1001,
            name: '哔哩哔哩用户1',
            avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user1'
          },
          {
            id: 1002,
            name: '哔哩哔哩用户2',
            avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user2'
          },
          {
            id: 1003,
            name: 'Vue爱好者',
            avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=vue'
          }
        ].filter(user => 
          user.name.toLowerCase().includes(addFriendQuery.value.toLowerCase()) ||
          user.id.toString().includes(addFriendQuery.value)
        )

        searchResults.value = mockResults
        searching.value = false

        if (mockResults.length === 0) {
          searchError.value = '未找到相关用户'
        }
      }, 1000)
    }

    const isFriend = (userId) => {
      return Array.isArray(chatStore.friends) && chatStore.friends.some(friend => friend.id === userId)
    }

    const addFriend = (userId) => {
      // 模拟添加好友
      const newFriend = searchResults.value.find(user => user.id === userId)
      if (newFriend) {
        chatStore.addFriend({
          ...newFriend,
          isOnline: Math.random() > 0.5,
          lastSeen: Math.random() > 0.5 ? '刚刚在线' : '10分钟前在线',
          level: Math.floor(Math.random() * 6) + 1,
          bio: '很高兴成为你的好友！',
          unread: 0
        })
        
        // 从搜索结果中移除
        searchResults.value = searchResults.value.filter(user => user.id !== userId)
        
        // 显示成功消息
        searchError.value = '添加好友成功！'
        setTimeout(() => {
          searchError.value = ''
        }, 2000)
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

    onMounted(() => {
      // 确保加载好友数据，添加空值检查
      if (!Array.isArray(chatStore.friends) || chatStore.friends.length === 0) {
        // 确保initMockData方法存在
        if (chatStore.initMockData && typeof chatStore.initMockData === 'function') {
          chatStore.initMockData()
        } else {
          console.warn('chatStore.initMockData方法不存在')
        }
      }
    })

    return {
      searchQuery,
      activeTab,
      displayFriends,
      onlineFriends,
      unreadFriends,
      showAddFriendModal,
      addFriendQuery,
      searchResults,
      searching,
      searchError,
      startChat,
      viewProfile,
      closeAddFriendModal,
      searchUsers,
      isFriend,
      addFriend,
      getEmptyStateMessage,
      formatLastSeen
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