<template>
  <div class="chat-container">
    <!-- 聊天侧边栏 -->
    <div class="chat-sidebar">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <input
          type="text"
          v-model="searchQuery"
          placeholder="搜索聊天..."
          class="search-input"
        />
        <i class="search-icon">🔍</i>
      </div>

      <!-- 聊天列表 -->
      <div class="chat-list">
        <div
          v-for="conversation in filteredConversations"
          :key="conversation.id"
          class="chat-item"
          :class="{ active: currentChatId === conversation.id }"
          @click="selectChat(conversation)"
        >
          <div class="chat-avatar">
            <img :src="conversation.avatar" alt="头像" />
            <span v-if="conversation.isOnline" class="online-indicator"></span>
          </div>
          <div class="chat-info">
            <div class="chat-header">
              <h4 class="chat-name">{{ conversation.name }}</h4>
              <span class="chat-time">{{ formatTime(conversation.lastMessageTime) }}</span>
            </div>
            <div class="chat-preview">
              <span v-if="conversation.unreadCount > 0" class="unread-badge">{{ conversation.unreadCount }}</span>
              <p class="chat-message">{{ conversation.lastMessage }}</p>
            </div>
          </div>
        </div>

        <!-- 无聊天时的提示 -->
        <div v-if="filteredConversations.length === 0" class="no-chats">
          <div class="no-chats-icon">💬</div>
          <p>暂无聊天记录</p>
          <Button type="primary" size="small" @click="navigateToFriends">添加好友</Button>
        </div>
      </div>
    </div>

    <!-- 聊天主窗口 -->
    <div class="chat-main">
      <!-- 未选择聊天时的提示 -->
      <div v-if="!currentChat" class="no-selection">
        <div class="no-selection-icon">💬</div>
        <h3>选择一个好友开始聊天</h3>
        <p>从左侧选择一个聊天或添加新朋友</p>
      </div>

      <!-- 聊天窗口 -->
      <template v-else>
        <!-- 聊天头部 -->
        <div class="chat-header-bar">
          <div class="chat-contact-info">
            <div class="contact-avatar">
              <img :src="currentChat.avatar" alt="头像" />
              <span v-if="currentChat.isOnline" class="online-indicator"></span>
            </div>
            <div class="contact-details">
              <h3 class="contact-name">{{ currentChat.name }}</h3>
              <p class="contact-status">{{ currentChat.isOnline ? '在线' : '离线' }}</p>
            </div>
          </div>
          <div class="chat-actions">
            <button class="action-btn" title="查看资料" @click="viewUserProfile">
              <i>👤</i>
            </button>
            <button class="action-btn" title="更多选项" @click="showMoreOptions = !showMoreOptions">
              <i>•••</i>
            </button>

            <!-- 更多选项下拉菜单 -->
            <div v-if="showMoreOptions" class="options-dropdown">
              <button class="option-item" @click="clearChat">
                <i>🗑️</i> 清空聊天
              </button>
              <button class="option-item" @click="blockUser">
                <i>🚫</i> 屏蔽用户
              </button>
            </div>
          </div>
        </div>

        <!-- 聊天消息区域 -->
        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="(message, index) in currentChat.messages"
            :key="index"
            class="message"
            :class="message.isMine ? 'sent' : 'received'"
          >
            <div class="message-content">
              <div class="message-bubble">
                {{ message.content }}
              </div>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            <div v-if="!message.isMine" class="message-avatar">
              <img :src="currentChat.avatar" alt="头像" />
            </div>
          </div>

          <!-- 输入框占位符 -->
          <div class="message-input-placeholder"></div>
        </div>

        <!-- 消息输入区域 -->
        <div class="chat-input-area">
          <div class="input-actions">
            <button class="input-action-btn" title="表情">
              <i>😊</i>
            </button>
            <button class="input-action-btn" title="图片">
              <i>🖼️</i>
            </button>
            <button class="input-action-btn" title="文件">
              <i>📎</i>
            </button>
            <button class="input-action-btn" title="语音">
              <i>🎤</i>
            </button>
          </div>

          <div class="message-input-wrapper">
            <textarea
              v-model="messageInput"
              placeholder="输入消息..."
              class="message-input"
              rows="1"
              @keydown.enter.prevent="handleEnterKey"
              ref="messageInputRef"
              :disabled="isSending"
            ></textarea>
            <div class="input-footer">
              <span class="char-count">{{ messageInput.length }}/500</span>
              <Button
                type="primary"
                size="small"
                @click="sendMessage"
                :disabled="!messageInput.trim() || isSending"
                :loading="isSending"
              >
                发送
              </Button>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- 发送中提示 -->
    <div v-if="showSendingIndicator" class="sending-indicator">
      <div class="sending-dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <span>发送中...</span>
    </div>

    <!-- 对方正在输入提示 -->
    <div v-if="isTyping" class="typing-indicator">
      <div class="typing-avatar">
        <img :src="currentChat?.avatar" alt="头像" />
      </div>
      <div class="typing-bubble">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <!-- 发送失败弹窗 -->
    <div v-if="showErrorModal" class="error-modal-overlay" @click.self="closeErrorModal">
      <div class="error-modal-content">
        <h4>发送失败</h4>
        <p>{{ errorMessage }}</p>
        <div class="error-actions">
          <Button type="secondary" size="small" @click="closeErrorModal">取消</Button>
          <Button type="primary" size="small" @click="resendMessage">重新发送</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { messageApi } from '../services/api'
import Button from '../components/Button.vue'

export default {
  name: 'Chat',
  components: {
    Button
  },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const chatStore = useChatStore()
    const messagesContainer = ref(null)
    const messageInputRef = ref(null)

    // 响应式数据
    const currentChatId = ref(null)
    const searchQuery = ref('')
    const messageInput = ref('')
    const isSending = ref(false)
    const showSendingIndicator = ref(false)
    const isTyping = ref(false)
    const showMoreOptions = ref(false)
    const showErrorModal = ref(false)
    const errorMessage = ref('')
    const failedMessage = ref(null)

    // 计算属性
    const currentChat = computed(() => {
      if (!currentChatId.value) return null
      return chatStore.conversations.find(c => c.id === currentChatId.value)
    })

    const filteredConversations = computed(() => {
      if (!searchQuery.value) {
        return chatStore.conversations
      }

      const query = searchQuery.value.toLowerCase()
      return chatStore.conversations.filter(conv =>
        conv.name.toLowerCase().includes(query) ||
        conv.lastMessage.toLowerCase().includes(query)
      )
    })

    // 方法
    const selectChat = async (conversation) => {
      currentChatId.value = conversation.id
      chatStore.setCurrentChat(conversation.id)

      // 获取聊天历史
      await fetchChatHistory(conversation.id)

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })
    }

    const formatTime = (timestamp) => {
      const date = new Date(timestamp)
      const now = new Date()
      const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))

      if (diffDays === 0) {
        // 今天
        return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else if (diffDays === 1) {
        // 昨天
        return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      } else if (diffDays < 7) {
        // 本周
        const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
        return days[date.getDay()]
      } else {
        // 其他
        return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
      }
    }

    const handleEnterKey = (event) => {
      if (event.shiftKey) {
        // Shift + Enter 换行
        return
      }
      // Enter 发送
      sendMessage()
    }

    // 获取聊天历史记录
    const fetchChatHistory = async (chatId) => {
      if (!chatId) return

      try {
        const isLoadingHistory = ref(true)
        const response = await messageApi.getChatHistory(chatId)

        if (response && response.messages) {
          const userId = parseInt(localStorage.getItem('userId')) || 1

          // 格式化消息，添加isMine字段和状态
          const formattedMessages = response.messages.map(msg => ({
            ...msg,
            id: msg.id || Date.now(),
            content: msg.content,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            createdAt: msg.createdAt || msg.timestamp || new Date().toISOString(),
            isMine: msg.senderId === userId,
            status: 'received'
          }))

          // 清空现有消息
          chatStore.clearMessages(chatId)
          // 添加新消息
          formattedMessages.forEach(msg => {
            chatStore.receiveMessage(chatId, msg)
          })

          // 更新会话列表中的最后一条消息
          const lastMessage = formattedMessages[formattedMessages.length - 1]
          if (lastMessage) {
            const conversation = chatStore.conversations.find(c => c.id === chatId)
            if (conversation) {
              conversation.lastMessage = lastMessage.content
              conversation.lastMessageTime = lastMessage.createdAt
            }
          }

          // 滚动到底部
          nextTick(() => {
            scrollToBottom()
          })

          // 标记已读
          if (chatStore.unreadCount[chatId]) {
            await messageApi.markAsRead(chatId)
            chatStore.unreadCount[chatId] = 0
            // 同时更新会话列表中的未读数
            const conversation = chatStore.conversations.find(c => c.id === chatId)
            if (conversation) {
              conversation.unreadCount = 0
            }
          }
        }
      } catch (error) {
        console.error('获取聊天历史失败:', error)
        showError('获取聊天历史失败')
      }
    }

    // 检查是否有新消息
    const checkForNewMessages = async () => {
      try {
        // 调用获取未读消息数量API
        const response = await messageApi.getUnreadCount()
        if (response && response.unreadCount > 0) {
          // 刷新未读计数
          chatStore.updateUnreadCount(response)
        }
      } catch (error) {
        console.error('检查新消息失败:', error)
      }
    }

    const sendMessage = async () => {
      if (!messageInput.value.trim() || !currentChatId.value || isSending.value) {
        return
      }

      const content = messageInput.value.trim()
      if (content.length > 500) {
        showError('消息内容不能超过500个字符')
        return
      }

      isSending.value = true
      showSendingIndicator.value = true

      try {
        const userId = parseInt(localStorage.getItem('userId')) || 1

        // 创建临时消息对象
        const tempMessage = {
          id: Date.now(),
          content: content,
          senderId: userId,
          receiverId: currentChatId.value,
          createdAt: new Date().toISOString(),
          isMine: true,
          status: 'sending'
        }

        // 先在本地显示消息
        chatStore.sendMessage(currentChatId.value, tempMessage)

        // 清空输入框
        messageInput.value = ''

        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })

        // 发送到服务器
        const response = await messageApi.sendMessage({
          receiverId: currentChatId.value,
          content: content,
          messageType: 'text'
        })

        // 更新消息状态为已发送
        chatStore.updateMessageStatus(tempMessage.id, currentChatId.value, 'sent')

      } catch (error) {
        console.error('发送消息失败:', error)
        failedMessage.value = { content }
        showErrorModal.value = true
        errorMessage.value = error.message || '消息发送失败，请稍后重试'
        // 更新消息状态为发送失败
        if (failedMessage.value && 'id' in failedMessage.value) {
          chatStore.updateMessageStatus(failedMessage.value.id, currentChatId.value, 'failed')
        }
      } finally {
        isSending.value = false
        showSendingIndicator.value = false
      }
    }

    const resendMessage = () => {
      if (failedMessage.value) {
        messageInput.value = failedMessage.value.content
        showErrorModal.value = false
        nextTick(() => {
          sendMessage()
        })
      }
    }

    const closeErrorModal = () => {
      showErrorModal.value = false
      failedMessage.value = null
    }

    const scrollToBottom = () => {
      if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
      }
    }

    const viewUserProfile = () => {
      if (currentChat.value) {
        // 这里可以跳转到用户资料页或打开用户资料弹窗
        console.log('查看用户资料:', currentChat.value.name)
        // 示例：router.push(`/user/${currentChat.value.id}`)
      }
    }

    const clearChat = () => {
      if (currentChat.value) {
        if (confirm('确定要清空聊天记录吗？此操作不可恢复。')) {
          chatStore.clearChat(currentChatId.value)
          showMoreOptions.value = false
        }
      }
    }

    const blockUser = () => {
      if (currentChat.value) {
        if (confirm(`确定要屏蔽 ${currentChat.value.name} 吗？`)) {
          chatStore.blockUser(currentChatId.value)
          currentChatId.value = null
          showMoreOptions.value = false
        }
      }
    }

    const navigateToFriends = () => {
      router.push('/friends')
    }

    const showError = (message) => {
      alert(message)
    }

    // 自动调整输入框高度
    const adjustTextareaHeight = () => {
      if (messageInputRef.value) {
        messageInputRef.value.style.height = 'auto'
        messageInputRef.value.style.height = Math.min(messageInputRef.value.scrollHeight, 120) + 'px'
      }
    }

    // 监听输入
    watch(messageInput, () => {
      adjustTextareaHeight()
    })

    // 监听当前聊天变化
    watch(currentChat, () => {
      nextTick(() => {
        scrollToBottom()
        adjustTextareaHeight()
      })
    })

    // 监听点击外部关闭选项菜单
    const handleClickOutside = (event) => {
      const dropdown = document.querySelector('.options-dropdown')
      const actionsButton = document.querySelector('.action-btn:last-child')

      if (dropdown && actionsButton && !dropdown.contains(event.target) && !actionsButton.contains(event.target)) {
        showMoreOptions.value = false
      }
    }

    // 获取会话列表
    const fetchConversations = async () => {
      try {
        const response = await messageApi.getConversations()
        if (response && response.conversations) {
          // 清空现有会话
          chatStore.conversations = []
          // 添加新会话
          response.conversations.forEach(conv => {
            // 确保会话有必要的字段
            const conversation = {
              ...conv,
              avatar: conv.avatar || generateRandomAvatar(conv.name),
              isOnline: conv.isOnline || false,
              lastMessage: conv.lastMessage || '',
              lastMessageTime: conv.lastMessageTime || new Date().toISOString(),
              unreadCount: conv.unreadCount || 0,
              messages: [] // 消息将在selectChat时获取
            }
            chatStore.conversations.push(conversation)
          })
        }
      } catch (error) {
        console.error('获取会话列表失败:', error)
        // 错误处理，可能需要显示提示
      }
    }

    // 生成随机头像
    const generateRandomAvatar = (name) => {
      // 使用用户名生成种子，确保同一个用户总是得到相同的头像
      const seed = name ? name.toLowerCase().replace(/\s+/g, '-') : 'default'
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`
    }

    // 生命周期钩子
    onMounted(async () => {
      // 从API获取会话列表
      await fetchConversations()

      // 检查URL查询参数，看是否从Friends.vue跳转过来
      const friendId = route.query.friendId
      const friendName = route.query.friendName

      if (friendId) {
        // 查找是否已有该好友的会话
        let conversation = chatStore.conversations.find(c => c.id === friendId)

        // 如果没有找到会话，创建一个新的会话对象
        if (!conversation) {
          conversation = {
            id: friendId,
            name: friendName || `用户${friendId}`,
            avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=${friendId}`,
            isOnline: false,
            lastMessage: '',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0
          }
          // 添加到会话列表
          chatStore.conversations.push(conversation)
        }

        // 选择该会话
        selectChat(conversation)
      } else if (chatStore.conversations.length > 0) {
        // 默认选择第一个聊天
        selectChat(chatStore.conversations[0])
      }

      // 添加点击外部事件监听
      document.addEventListener('click', handleClickOutside)

      // 调整输入框高度
      adjustTextareaHeight()

      // 监听窗口大小变化，调整布局
      window.addEventListener('resize', () => {
        nextTick(() => {
          scrollToBottom()
        })
      })

      // 定期检查新消息
      const messageInterval = setInterval(() => {
        checkForNewMessages()
      }, 30000) // 每30秒检查一次新消息

      // 清理定时器
      onUnmounted(() => {
        clearInterval(messageInterval)
      })
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      window.removeEventListener('resize', () => {})
    })

    return {
      currentChatId,
      currentChat,
      searchQuery,
      filteredConversations,
      messageInput,
      isSending,
      showSendingIndicator,
      isTyping,
      showMoreOptions,
      showErrorModal,
      errorMessage,
      messagesContainer,
      messageInputRef,
      selectChat,
      formatTime,
      handleEnterKey,
      sendMessage,
      resendMessage,
      closeErrorModal,
      viewUserProfile,
      clearChat,
      blockUser,
      navigateToFriends
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - var(--navbar-height, 60px));
  background-color: var(--background-color);
}

/* 侧边栏样式 */
.chat-sidebar {
  width: 320px;
  background-color: var(--sidebar-background);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  position: relative;
  padding: 15px;
  border-bottom: 1px solid var(--border-color);
}

.search-input {
  width: 100%;
  padding: 10px 40px 10px 15px;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  font-size: 14px;
  background-color: var(--input-background);
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(251, 114, 153, 0.1);
}

.search-icon {
  position: absolute;
  right: 25px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
  pointer-events: none;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  border-bottom: 1px solid var(--border-color);
  position: relative;
}

.chat-item:hover {
  background-color: var(--hover-background);
}

.chat-item.active {
  background-color: rgba(251, 114, 153, 0.1);
  border-left: 3px solid var(--primary-color);
}

.chat-avatar {
  position: relative;
  margin-right: 15px;
}

.chat-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 14px;
  height: 14px;
  background-color: #52c41a;
  border: 2px solid white;
  border-radius: 50%;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.chat-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-time {
  font-size: 12px;
  color: var(--text-secondary);
}

.chat-preview {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-badge {
  background-color: var(--danger-color);
  color: white;
  font-size: 12px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.chat-message {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.no-chats {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: var(--text-secondary);
  text-align: center;
  gap: 15px;
}

.no-chats-icon {
  font-size: 48px;
  opacity: 0.5;
}

/* 主聊天窗口样式 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: var(--chat-background);
  position: relative;
}

.no-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-secondary);
  text-align: center;
  gap: 15px;
}

.no-selection-icon {
  font-size: 72px;
  opacity: 0.3;
}

.no-selection h3 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.no-selection p {
  margin: 0;
  font-size: 16px;
}

.chat-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: white;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  position: relative;
}

.chat-contact-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.contact-avatar {
  position: relative;
}

.contact-avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.contact-details h3 {
  margin: 0;
  font-size: 18px;
  color: var(--text-primary);
}

.contact-status {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.action-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.action-btn:hover {
  background-color: var(--hover-background);
  color: var(--text-primary);
}

.options-dropdown {
  position: absolute;
  top: 100%;
  right: 20px;
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 100;
  min-width: 150px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
  transition: background-color 0.3s;
}

.option-item:hover {
  background-color: var(--hover-background);
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  max-width: 70%;
}

.message.sent {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.received {
  align-self: flex-start;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
  max-width: 100%;
}

.message-bubble {
  padding: 10px 15px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.4;
  word-wrap: break-word;
  max-width: 100%;
}

.message.sent .message-bubble {
  background-color: var(--primary-color);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.received .message-bubble {
  background-color: white;
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
  border: 1px solid var(--border-color);
}

.message-time {
  font-size: 12px;
  color: var(--text-secondary);
  text-align: center;
}

.message-avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.message-input-placeholder {
  height: 10px;
}

.chat-input-area {
  background-color: white;
  border-top: 1px solid var(--border-color);
  padding: 15px 20px;
}

.input-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.input-action-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: var(--text-secondary);
  cursor: pointer;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.input-action-btn:hover {
  background-color: var(--hover-background);
  color: var(--text-primary);
}

.message-input-wrapper {
  border: 1px solid var(--border-color);
  border-radius: 24px;
  overflow: hidden;
  background-color: var(--input-background);
}

.message-input {
  width: 100%;
  padding: 12px 20px;
  border: none;
  outline: none;
  font-size: 15px;
  line-height: 1.4;
  resize: none;
  background: none;
  color: var(--text-primary);
  min-height: 40px;
  max-height: 120px;
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 10px 20px;
}

.char-count {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 发送中提示 */
.sending-indicator {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  z-index: 1000;
}

.sending-dots,
.typing-dots {
  display: flex;
  gap: 5px;
}

.sending-dots span,
.typing-dots span {
  width: 8px;
  height: 8px;
  background-color: white;
  border-radius: 50%;
  animation: dotPulse 1.4s infinite ease-in-out both;
}

.sending-dots span:nth-child(1),
.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.sending-dots span:nth-child(2),
.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

/* 正在输入提示 */
.typing-indicator {
  position: fixed;
  bottom: 130px;
  left: 360px;
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: white;
  padding: 10px 15px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 999;
}

.typing-avatar img {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  object-fit: cover;
}

.typing-bubble {
  background-color: var(--input-background);
  padding: 8px 12px;
  border-radius: 16px;
}

.typing-bubble .typing-dots span {
  background-color: var(--text-secondary);
}

/* 错误弹窗 */
.error-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.error-modal-content {
  background-color: white;
  border-radius: var(--border-radius);
  padding: 20px;
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.error-modal-content h4 {
  margin: 0 0 10px 0;
  color: var(--danger-color);
}

.error-modal-content p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

/* 动画 */
@keyframes dotPulse {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }

  .chat-sidebar {
    width: 100%;
    height: 50%;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }

  .chat-main {
    height: 50%;
  }

  .message {
    max-width: 85%;
  }

  .typing-indicator {
    left: 20px;
    right: 20px;
    transform: none;
  }

  .chat-header-bar {
    padding: 12px 15px;
  }

  .chat-messages {
    padding: 15px;
  }

  .chat-input-area {
    padding: 15px;
  }
}

@media (max-width: 480px) {
  .chat-item {
    padding: 12px;
  }

  .chat-avatar img {
    width: 40px;
    height: 40px;
  }

  .message {
    max-width: 90%;
  }

  .message-bubble {
    font-size: 14px;
    padding: 8px 12px;
  }
}
</style>
