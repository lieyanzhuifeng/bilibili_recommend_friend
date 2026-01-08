<template>
  <div class="chat-container">
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
          <img :src="getUserAvatar(currentChat.avatar, currentChat.id)" alt="头像" />
        </div>
        <div class="contact-details">
          <h3 class="contact-name">{{ currentChat.name }}</h3>
        </div>
      </div>
          <div class="chat-actions">
            <button class="action-btn" title="查看资料" @click="viewUserProfile">
              <i>👤</i>
            </button>
            <button class="action-btn" title="搜索消息" @click="toggleMessageSearch">
              <i>🔍</i>
            </button>
            <button class="action-btn" title="更多选项" @click="showMoreOptions = !showMoreOptions">
              <i>•••</i>
            </button>

            <!-- 更多选项下拉菜单 -->
            <div v-if="showMoreOptions" class="options-dropdown">
              <button class="option-item" @click="clearChat">
                <i>🗑️</i> 清空聊天
              </button>
              <button class="option-item" @click="deleteChatHistory">
                <i>🚮</i> 删除聊天记录
              </button>
              <button class="option-item" @click="blockUser">
                <i>🚫</i> 屏蔽用户
              </button>
            </div>
          </div>
        </div>

        <!-- 消息搜索区域 -->
        <div v-if="showMessageSearch" class="message-search-area">
          <div class="search-input-wrapper">
            <input
              type="text"
              v-model="messageSearchQuery"
              placeholder="搜索消息内容..."
              class="message-search-input"
              @keydown.enter="searchMessages"
            />
            <button class="search-btn" @click="searchMessages">搜索</button>
            <button class="close-btn" @click="toggleMessageSearch">×</button>
          </div>
          <div v-if="messageSearchResults.length > 0" class="search-results">
            <div class="search-results-header">
              <h4>搜索结果 ({{ messageSearchResults.length }})</h4>
              <button class="clear-btn" @click="clearSearchResults">清空</button>
            </div>
            <div
              v-for="message in messageSearchResults"
              :key="message.id"
              class="search-result-item"
              @click="scrollToMessage(message.id)"
            >
              <p class="result-content">{{ message.content }}</p>
              <span class="result-time">{{ formatTime(message.createdAt) }}</span>
            </div>
          </div>
        </div>



    <!-- 聊天消息区域 -->
    <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="message in currentMessages"
            :key="message.id"
            class="message"
            :class="{
              'sent': isMyMessage(message),
              'received': !isMyMessage(message),
              'failed': message.status === 'failed',
              'unread': !message.isRead && !isMyMessage(message)
            }"
            @contextmenu.prevent="showMessageMenu($event, message)"
          >
            <div class="message-content">
              <div class="message-bubble">
                {{ message.content }}
              </div>
              <div class="message-meta">
                <span class="message-time">{{ formatTime(message.createdAt) }}</span>
                <span v-if="isMyMessage(message)" class="message-status" :title="getStatusTitle(message.status, message.isTemp)">
                  {{ getStatusIcon(message.status, message.isTemp) }}
                </span>
                <!-- <span v-if="!isMyMessage(message) && !message.isRead" class="unread-indicator">未读</span> -->
              </div>
            </div>
            <div v-if="!isMyMessage(message)" class="message-avatar">
              <img :src="currentChat.avatar" alt="头像" />
            </div>
          </div>

          <!-- 消息操作菜单 -->
          <div
            v-if="messageMenuVisible"
            class="message-menu"
            :style="{ top: messageMenuPosition.y + 'px', left: messageMenuPosition.x + 'px' }"
          >
            <button
              class="menu-item"
              @click="deleteMessage(selectedMessage)"
            >
              删除消息
            </button>
            <button
              v-if="!isMyMessage(selectedMessage) && !selectedMessage.isRead"
              class="menu-item"
              @click="markAsRead(selectedMessage)"
            >
              标记为已读
            </button>
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
        <img :src="getUserAvatar(currentChat?.avatar, currentChat?.id)" alt="头像" />
      </div>
      <div class="typing-bubble">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>

    <!-- 错误弹窗 -->
    <div v-if="showErrorModal" class="error-modal-overlay" @click.self="closeErrorModal">
      <div class="error-modal-content">
        <h4>操作失败</h4>
        <p>{{ errorMessage }}</p>
        <div class="error-actions">
          <Button type="secondary" size="small" @click="closeErrorModal">取消</Button>
          <Button v-if="errorAction" type="primary" size="small" @click="errorAction">重试</Button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useChatStore } from '../stores/chat'
import { useUserStore } from '../stores/user'
import { getUserAvatar, generateRandomAvatar } from '../utils/avatar'
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
    const messageSearchQuery = ref('')
    const messageInput = ref('')
    const isSending = ref(false)
    const showSendingIndicator = ref(false)
    const isTyping = ref(false)
    const showMoreOptions = ref(false)
    const showMessageSearch = ref(false)
    const showErrorModal = ref(false)
    const errorMessage = ref('')
    const errorAction = ref(null)
    const failedMessage = ref(null)
    const messageSearchResults = ref([])
    const messageMenuVisible = ref(false)
    const messageMenuPosition = ref({ x: 0, y: 0 })
    const selectedMessage = ref(null)

    // 计算属性
    const currentChat = computed(() => {
      if (!currentChatId.value) return null
      return chatStore.conversations.find(c => c.id === currentChatId.value)
    })

    const currentMessages = computed(() => {
      return chatStore.getCurrentConversation || []
    })

    // 方法
    const selectChat = async (conversation) => {
      try {
        console.log(`选择聊天会话: ${conversation.id} - ${conversation.name}`)

        // 设置当前聊天ID
        currentChatId.value = conversation.id

        // 设置当前聊天到store
        if (chatStore.setCurrentChat) {
          chatStore.setCurrentChat(conversation.id)
        }

        // 获取聊天历史，添加错误处理
        try {
          console.log(`正在获取与${conversation.id}的历史消息`)
          if (chatStore.fetchChatHistory) {
            await chatStore.fetchChatHistory(conversation.id)
            console.log('历史消息获取完成')
          } else {
            console.warn('chatStore.fetchChatHistory方法不存在，尝试使用备用方式获取消息')

            // 备用逻辑：如果store没有fetchChatHistory方法，尝试直接设置模拟消息
            if (!chatStore.getCurrentConversation || chatStore.getCurrentConversation.length === 0) {
              // 添加一些模拟消息以便展示
              const mockMessages = [
                {
                  id: 'mock-1',
                  content: '嗨！很高兴认识你！',
                  createdAt: new Date(Date.now() - 3600000).toISOString(),
                  senderId: conversation.id,
                  status: 'received',
                  isRead: true
                },
                {
                  id: 'mock-2',
                  content: '你好！',
                  createdAt: new Date(Date.now() - 1800000).toISOString(),
                  senderId: 'current-user',
                  status: 'sent',
                  isRead: true
                }
              ]

              // 如果有设置消息的方法，使用它
              if (chatStore.setMessages) {
                chatStore.setMessages(conversation.id, mockMessages)
              }
            }
          }
        } catch (fetchError) {
          console.error(`获取历史消息失败:`, fetchError)
          console.info('显示模拟消息以便用户体验')

          // 显示模拟消息
          const mockMessages = [
            {
              id: 'mock-error-1',
              content: '历史消息暂时无法加载，但您可以继续发送新消息。',
              createdAt: new Date().toISOString(),
              senderId: 'system',
              status: 'received',
              isRead: true
            }
          ]

          if (chatStore.setMessages) {
            chatStore.setMessages(conversation.id, mockMessages)
          }
        }

        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })

        // 标记消息为已读
        if (chatStore.markAsRead) {
          chatStore.markAsRead(conversation.id).catch(err => {
            console.warn('标记已读失败:', err)
          })
        }
      } catch (error) {
        console.error('选择聊天会话失败:', error)
        showError('加载聊天会话失败')
      }
    }

    const formatTime = (timestamp) => {
      if (!timestamp) return ''
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

    const isMyMessage = (message) => {
      const userStore = useUserStore()
      return message.senderId === userStore.userId
    }

    const getStatusIcon = (status, isTemp = false) => {
      switch (status) {
        case 'sending':
          return '⏱️'
        case 'sent':
          // 临时消息发送成功后显示特殊标记
          return isTemp ? '✓ (临时)' : '✓'
        case 'failed':
          // 失败的临时消息显示更明显的失败标记
          return isTemp ? '❌ (重试)' : '❌'
        case 'received':
          return '✓✓'
        default:
          return ''
      }
    }

    const getStatusTitle = (status, isTemp = false) => {
      switch (status) {
        case 'sending':
          return '发送中'
        case 'sent':
          return isTemp ? '已发送 (临时)' : '已发送'
        case 'failed':
          return isTemp ? '发送失败 (点击重试)' : '发送失败'
        case 'received':
          return '已送达'
        default:
          return ''
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
        // 清空输入框
        messageInput.value = ''

        // 调用store的发送消息方法，由store负责创建临时消息并添加到状态中
        await chatStore.sendMessage(currentChatId.value, content)

        // 滚动到底部
        nextTick(() => {
          scrollToBottom()
        })

        // 注意：不再手动刷新消息列表，避免重复显示
        // 让store负责更新消息状态，而不是重新获取整个列表
      } catch (error) {
        console.error('发送消息失败:', error)
        failedMessage.value = { content }
        showErrorModal.value = true
        errorMessage.value = error.message || '消息发送失败，请稍后重试'
        errorAction.value = resendMessage
      } finally {
        isSending.value = false
        showSendingIndicator.value = false
      }
    }

    const resendMessage = () => {
      if (failedMessage.value) {
        messageInput.value = failedMessage.value.content
        showErrorModal.value = false
        errorAction.value = null
        nextTick(() => {
          sendMessage()
        })
      }
    }

    const closeErrorModal = () => {
      showErrorModal.value = false
      errorMessage.value = ''
      errorAction.value = null
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
          chatStore.clearMessages(currentChatId.value)
          showMoreOptions.value = false
        }
      }
    }

    const deleteChatHistory = async () => {
      if (currentChat.value) {
        if (confirm('确定要删除与该用户的所有聊天记录吗？此操作不可恢复。')) {
          try {
            await chatStore.deleteChatHistory(currentChatId.value)
            currentChatId.value = null
            showMoreOptions.value = false
          } catch (error) {
            showError('删除聊天记录失败')
          }
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

    const toggleMessageSearch = () => {
      showMessageSearch.value = !showMessageSearch.value
      if (!showMessageSearch.value) {
        clearSearchResults()
      } else {
        nextTick(() => {
          document.querySelector('.message-search-input').focus()
        })
      }
    }

    const searchMessages = async () => {
      if (!messageSearchQuery.value.trim() || !currentChatId.value) {
        return
      }

      try {
        const results = await chatStore.searchMessages(messageSearchQuery.value, currentChatId.value)
        messageSearchResults.value = results.map(msg => ({
          ...msg,
          isMine: isMyMessage(msg)
        }))
      } catch (error) {
        showError('搜索消息失败')
      }
    }

    const clearSearchResults = () => {
      messageSearchResults.value = []
      messageSearchQuery.value = ''
    }

    const scrollToMessage = (messageId) => {
      const messageElement = document.querySelector(`[data-message-id="${messageId}"]`)
      if (messageElement && messagesContainer.value) {
        messagesContainer.value.scrollTop = messageElement.offsetTop - messagesContainer.value.offsetTop
        // 高亮显示消息
        messageElement.classList.add('highlight')
        setTimeout(() => {
          messageElement.classList.remove('highlight')
        }, 2000)
      }
      // 关闭搜索面板
      toggleMessageSearch()
    }

    const showMessageMenu = (event, message) => {
      messageMenuPosition.value = {
        x: event.clientX,
        y: event.clientY
      }
      selectedMessage.value = message
      messageMenuVisible.value = true
    }

    const hideMessageMenu = () => {
      messageMenuVisible.value = false
      selectedMessage.value = null
    }

    const deleteMessage = async () => {
      if (!selectedMessage.value || !currentChatId.value) {
        return
      }

      if (confirm('确定要删除这条消息吗？')) {
        try {
          await chatStore.deleteMessage(selectedMessage.value.id, currentChatId.value)
        } catch (error) {
          showError('删除消息失败')
        }
      }
      hideMessageMenu()
    }

    const markAsRead = async () => {
      if (!selectedMessage.value || isMyMessage(selectedMessage.value)) {
        return
      }

      try {
        await chatStore.markAsRead(currentChatId.value)
      } catch (error) {
        showError('标记已读失败')
      }
      hideMessageMenu()
    }

    // 自动调整输入框高度
    const adjustTextareaHeight = () => {
      if (messageInputRef.value) {
        messageInputRef.value.style.height = 'auto'
        messageInputRef.value.style.height = Math.min(messageInputRef.value.scrollHeight, 120) + 'px'
      }
    }

    // 检查是否有新消息
    const checkForNewMessages = async () => {
      try {
        // 获取最新的会话列表
        await chatStore.fetchChatPartners()

        // 如果有当前聊天，检查是否有新消息
        if (currentChatId.value) {
          // 记录获取前的最新消息时间戳
          const currentMessages = chatStore.getCurrentConversation || []
          const lastMessageTime = currentMessages.length > 0
            ? new Date(currentMessages[currentMessages.length - 1].createdAt).getTime()
            : 0

          // 调用fetchRecentChat时传入最后一条消息的时间戳，让服务器只返回新消息
          await chatStore.fetchRecentChat(currentChatId.value, 50, lastMessageTime)
        }
      } catch (error) {
        console.error('检查新消息失败:', error)
      }
    }

    // 头像生成函数已从utils/avatar.js导入

    // 监听输入
    watch(messageInput, () => {
      adjustTextareaHeight()
    })

    // 监听当前聊天变化
    watch(currentChat, (newChat) => {
      nextTick(() => {
        scrollToBottom()
        adjustTextareaHeight()
      })

      // 动态设置页面标题为聊天对象的名称
      if (newChat) {
        document.title = `${newChat.name} - Bfriend`
      } else {
        document.title = '选择聊天 - Bfriend'
      }
    })

    // 监听点击外部关闭选项菜单和消息菜单
    const handleClickOutside = (event) => {
      const dropdown = document.querySelector('.options-dropdown')
      const actionsButton = document.querySelector('.action-btn:last-child')
      const messageMenu = document.querySelector('.message-menu')

      // 关闭聊天选项菜单
      if (dropdown && actionsButton && !dropdown.contains(event.target) && !actionsButton.contains(event.target)) {
        showMoreOptions.value = false
      }

      // 关闭消息操作菜单
      if (messageMenu && !messageMenu.contains(event.target)) {
        hideMessageMenu()
      }
    }

    // 生命周期钩子
    onMounted(async () => {
      try {
        // 直接从URL参数获取userid，确保值有效
        const userId = route.query.userId || route.query.friendId
        const userName = route.query.userName || route.query.friendName

        if (userId && typeof userId === 'string') {
          console.log(`正在加载与用户${userId}的聊天记录`)

          // 查找是否已有该用户的会话
          let conversation = chatStore.conversations?.find(c => c.id === userId)

          // 如果没有找到会话，创建一个新的会话对象
          if (!conversation) {
            console.log(`未找到会话，为用户${userId}创建新会话`)
            conversation = {
              id: userId,
              name: userName || `用户${userId}`,
              avatar: getUserAvatar(null, userId.toString()),
              isOnline: false,
              isFriend: true, // 从好友页面过来的都默认为好友
              lastMessage: '',
              lastMessageTime: new Date().toISOString(),
              unreadCount: 0,
              // 确保会话对象有必要的属性
              messages: [],
              createdAt: new Date().toISOString()
            }

            // 安全地添加到会话列表
            if (Array.isArray(chatStore.conversations)) {
              chatStore.conversations.push(conversation)
            }
          }

          // 设置当前聊天ID
          currentChatId.value = userId

          // 选择该会话并获取历史消息
          await selectChat(conversation)
        } else {
          console.warn('URL参数中未提供有效的userId')
          showError('请选择一个好友进行聊天')
          setTimeout(() => {
            navigateToFriends()
          }, 2000)
        }
      } catch (error) {
        console.error('初始化聊天页面失败:', error)
        showError('加载聊天页面失败，请重试')
        setTimeout(() => {
          navigateToFriends()
        }, 2000)
      } finally {
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

        // 定期检查新消息（WebSocket连接正常时可以减少轮询频率或关闭）
        const messageInterval = setInterval(() => {
          checkForNewMessages()
        },1000) // 每30秒检查一次新消息

        // 清理定时器
        onUnmounted(() => {
          clearInterval(messageInterval)
        })
      }
    })

    onUnmounted(() => {
      document.removeEventListener('click', handleClickOutside)
      window.removeEventListener('resize', () => {})
    })

    // 添加好友
    const addFriend = (userId) => {
      // 调用用户相关API添加好友
      alert(`发送好友请求给用户${userId}`)
      // 实际应用中这里应该调用API发送好友请求
    }

    return {
      currentChatId,
      currentChat,
      currentMessages,
      messageSearchQuery,
      messageSearchResults,
      messageInput,
      isSending,
      showSendingIndicator,
      isTyping,
      showMoreOptions,
      getUserAvatar,
      showMessageSearch,
      showErrorModal,
      errorMessage,
      messageMenuVisible,
      messageMenuPosition,
      selectedMessage,
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
      deleteChatHistory,
      blockUser,
      navigateToFriends,
      toggleMessageSearch,
      searchMessages,
      clearSearchResults,
      scrollToMessage,
      showMessageMenu,
      deleteMessage,
      markAsRead,
      isMyMessage,
      getStatusIcon,
      getStatusTitle,
      addFriend
    }
  }
}
</script>

<style scoped>
.chat-container {
  height: calc(100vh - var(--navbar-height, 60px));
  margin-top: var(--navbar-height, 60px);
  background-color: var(--background-color);
  z-index: 100;
}

/* 主聊天窗口样式调整 */
.chat-main {
  height: 100%;
  width: 100%;
}

/* 添加好友按钮样式 */
.add-friend-btn {
  padding: 6px 12px;
  background: linear-gradient(135deg, #ff85a2 0%, #ff6b95 100%);
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 133, 162, 0.3);
}

.add-friend-btn:hover {
  background: linear-gradient(135deg, #ff6b95 0%, #ff5288 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 133, 162, 0.4);
}

.add-friend-btn:active {
  transform: translateY(0);
}

.add-icon {
  font-size: 14px;
  font-weight: bold;
  color: #fff;
}

.btn-text {
  white-space: nowrap;
  font-size: 12px;
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

/* 非好友提示样式 */
.non-friend-notice {
  background-color: #fff3cd;
  border-bottom: 1px solid #ffeaa7;
  padding: 12px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  color: #856404;
  font-size: 14px;
}

/* 消息搜索区域 */
.message-search-area {
  background-color: white;
  padding: 15px 20px;
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 50;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  background-color: var(--input-background);
  border-radius: 24px;
  padding: 5px 10px;
  border: 1px solid var(--border-color);
}

.message-search-input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  padding: 8px 15px;
  font-size: 15px;
  color: var(--text-primary);
}

.search-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 16px;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: var(--primary-hover);
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 0 10px;
  transition: color 0.3s;
}

.close-btn:hover {
  color: var(--text-primary);
}

.search-results {
  margin-top: 15px;
  max-height: 200px;
  overflow-y: auto;
  background-color: var(--input-background);
  border-radius: var(--border-radius);
  padding: 10px;
}

.search-results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-color);
}

.search-results-header h4 {
  margin: 0;
  font-size: 14px;
  color: var(--text-primary);
}

.clear-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 14px;
  cursor: pointer;
  padding: 5px 10px;
  transition: background-color 0.3s;
  border-radius: var(--border-radius);
}

.clear-btn:hover {
  background-color: var(--hover-background);
}

.search-result-item {
  padding: 10px;
  margin-bottom: 5px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-result-item:hover {
  background-color: var(--hover-background);
}

.result-content {
  margin: 0;
  font-size: 14px;
  color: var(--text-primary);
  word-break: break-word;
}

.result-time {
  font-size: 12px;
  color: var(--text-secondary);
  align-self: flex-end;
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
  cursor: pointer;
  transition: background-color 0.2s;
  padding: 5px;
  border-radius: var(--border-radius);
}

.message:hover {
  background-color: rgba(0, 0, 0, 0.02);
}

.message.highlight {
  animation: message-highlight 2s ease-out;
}

@keyframes message-highlight {
  0%, 100% {
    background-color: transparent;
  }
  25%, 75% {
    background-color: rgba(24, 144, 255, 0.15);
  }
}

.message.failed .message-bubble {
  opacity: 0.7;
  border: 1px dashed var(--danger-color) !important;
}

.message.unread .message-bubble {
  background-color: var(--unread-background) !important;
  border-left: 3px solid var(--warning-color);
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

.message-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;
}

.message-status {
  font-size: 12px;
  color: var(--text-secondary);
  cursor: help;
}

.unread-indicator {
  font-size: 12px;
  color: var(--warning-color);
  font-weight: 500;
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

/* 消息右键菜单 */
.message-menu {
  position: fixed;
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 5px 0;
  min-width: 140px;
  z-index: 1000;
  overflow: hidden;
}

.menu-item {
  width: 100%;
  padding: 10px 16px;
  background: none;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 14px;
  color: var(--text-primary);
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-item:hover {
  background-color: var(--hover-background);
}

.menu-item.danger {
  color: var(--danger-color);
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

  .message-search-area {
    padding: 10px 15px;
  }
}
</style>
