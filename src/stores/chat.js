import { defineStore } from 'pinia'
import { messageApi } from '../services/api'
import { useUserStore } from './user'

export const useChatStore = defineStore('chat', {
  state: () => ({
    friends: [], // 好友列表
    conversations: [], // 会话列表（包含好友信息和最新消息）
    messages: {}, // 消息记录 { friendId: [messages] }
    currentChat: null, // 当前聊天的好友ID
    unreadCount: {}, // 未读消息数 { friendId: count }
    loading: false, // 加载状态
    error: null // 错误信息
  }),

  getters: {
    // 获取所有好友
    getAllFriends: (state) => state.friends,

    // 获取指定好友的会话记录
    getConversation: (state) => (friendId) => {
      return state.messages[friendId] || []
    },

    // 获取当前聊天的会话记录
    getCurrentConversation: (state) => {
      if (!state.currentChat) return []
      return state.messages[state.currentChat] || []
    },

    // 获取好友信息
    getFriendInfo: (state) => (friendId) => {
      return state.friends.find(friend => friend.id === friendId) || null
    },

    // 获取未读消息总数
    totalUnreadCount: (state) => {
      return Object.values(state.unreadCount).reduce((total, count) => total + count, 0)
    },

    // 获取指定好友的未读数
    getUnreadCount: (state) => (friendId) => {
      return state.unreadCount[friendId] || 0
    }
  },

  actions: {
    // 设置错误信息
    setError(error) {
      this.error = error
      console.error('聊天错误:', error)
    },

    // 清除错误
    clearError() {
      this.error = null
    },

    // 设置当前聊天的好友
    setCurrentChat(friendId) {
      this.currentChat = friendId
      // 清除未读消息数
      if (this.unreadCount[friendId]) {
        this.unreadCount[friendId] = 0
      }
      // 标记为已读
      this.markAsRead(friendId)
    },

    // 从API获取聊天伙伴列表
    async fetchChatPartners() {
      this.loading = true
      this.clearError()
      try {
        const userStore = useUserStore()
      const userId = userStore.userId || ''
        const partners = await messageApi.getChatPartners(userId)
        // 更新会话列表
        this.updateConversationsFromPartners(partners)
        return partners
      } catch (error) {
        this.setError('获取聊天伙伴列表失败')
        return []
      } finally {
        this.loading = false
      }
    },

    // 根据伙伴列表更新会话
    updateConversationsFromPartners(partners) {
      if (!Array.isArray(partners)) return

      partners.forEach(partnerId => {
        if (!this.conversations.find(c => c.id === partnerId)) {
          this.conversations.push({
            id: partnerId,
            name: `用户${partnerId}`,
            avatar: '',
            lastMessage: '',
            lastMessageTime: new Date().toISOString(),
            unreadCount: this.unreadCount[partnerId] || 0
          })
        }
      })
    },

    // 从API获取完整聊天记录
    async fetchChatHistory(friendId) {
      this.loading = true
      this.clearError()
      try {
        const userStore = useUserStore()
      const userId = userStore.userId
        const messages = await messageApi.getFullChat(userId, friendId)
        // 格式化消息并保存
        if (Array.isArray(messages)) {
          const formattedMessages = messages.map(msg => ({
            id: msg.messageId,
            content: msg.content,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            createdAt: msg.sendTime,
            isRead: msg.isRead
          }))
          this.messages[friendId] = formattedMessages
        }
        return messages
      } catch (error) {
        this.setError('获取聊天记录失败')
        return []
      } finally {
        this.loading = false
      }
    },

    // 从API获取最近聊天记录
    async fetchRecentChat(friendId, limit = 50) {
      this.loading = true
      this.clearError()
      try {
        const userStore = useUserStore()
      const userId = userStore.userId
        const messages = await messageApi.getRecentChat(userId, friendId, limit)
        if (Array.isArray(messages)) {
          const formattedMessages = messages.map(msg => ({
            id: msg.messageId,
            content: msg.content,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            createdAt: msg.sendTime,
            isRead: msg.isRead
          }))
          // 合并或替换现有消息
          if (!this.messages[friendId]) {
            this.messages[friendId] = []
          }
          // 去重并合并
          const existingIds = new Set(this.messages[friendId].map(m => m.id))
          const newMessages = formattedMessages.filter(m => !existingIds.has(m.id))
          this.messages[friendId] = [...this.messages[friendId], ...newMessages]
            .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
        }
        return messages
      } catch (error) {
        this.setError('获取最近聊天记录失败')
        return []
      } finally {
        this.loading = false
      }
    },

    // 从API获取未读消息数量
    async fetchUnreadCount(lastMessageId = null) {
      try {
        const userStore = useUserStore()
      const userId = userStore.userId
        const count = await messageApi.getUnreadCount(userId, lastMessageId)
        // 简单处理，实际可能需要按会话分类
        return count
      } catch (error) {
        console.error('获取未读消息数量失败:', error)
        return 0
      }
    },

    // 标记消息为已读
    async markAsRead(friendId) {
      try {
        const userStore = useUserStore()
      const userId = userStore.userId
        // 获取最新消息ID
        let lastMessageId = null
        if (this.messages[friendId] && this.messages[friendId].length > 0) {
          const lastMessage = this.messages[friendId][this.messages[friendId].length - 1]
          lastMessageId = lastMessage.id
        }
        await messageApi.markAllAsRead(userId, lastMessageId)
        // 更新本地状态
        this.unreadCount[friendId] = 0
        // 更新会话中的未读计数
        const conversation = this.conversations.find(c => c.id === friendId)
        if (conversation) {
          conversation.unreadCount = 0
        }
        // 更新消息的已读状态
        if (this.messages[friendId]) {
          this.messages[friendId].forEach(msg => {
            if (!msg.isRead && msg.receiverId === userId) {
              msg.isRead = true
            }
          })
        }
      } catch (error) {
        console.error('标记已读失败:', error)
      }
    },

    // 发送消息（带API调用）
    async sendMessage(friendId, messageContent) {
      this.loading = true
      this.clearError()

      const userStore = useUserStore()
      const userId = userStore.userId || ''
      const message = {
        id: Date.now(),
        content: messageContent,
        senderId: userId,
        receiverId: friendId,
        createdAt: new Date().toISOString(),
        status: 'sending'
      }

      // 先添加到本地（乐观更新）
      if (!this.messages[friendId]) {
        this.messages[friendId] = []
      }
      this.messages[friendId].push(message)

      try {
        // 调用API发送消息
        await messageApi.sendMessage(userId, friendId, messageContent)
        // 更新消息状态
        message.status = 'sent'

        // 更新会话列表中的最后一条消息和时间
        const conversation = this.conversations.find(c => c.id === friendId)
        if (conversation) {
          conversation.lastMessage = message.content
          conversation.lastMessageTime = message.createdAt
        } else {
          // 如果会话不存在，创建新会话
          this.conversations.push({
            id: friendId,
            name: this.friends.find(f => f.id === friendId)?.name || `用户${friendId}`,
            avatar: this.friends.find(f => f.id === friendId)?.avatar || '',
            lastMessage: message.content,
            lastMessageTime: message.createdAt,
            unreadCount: 0
          })
        }
      } catch (error) {
        this.setError('发送消息失败')
        message.status = 'failed'
      } finally {
        this.loading = false
      }

      // 按最后消息时间排序会话列表
      this.conversations.sort((a, b) => {
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
      })
    },

    // 接收消息
    receiveMessage(friendId, messageObj, fromApi = false) {
      // 支持传入完整消息对象
      let message;
      if (typeof messageObj === 'object') {
        message = messageObj;
        // 确保消息有必要的字段
        if (fromApi) {
          // API返回的消息格式
          message = {
            id: message.messageId || Date.now(),
            content: message.content,
            senderId: message.senderId || friendId,
            receiverId: message.receiverId || userStore.userId,
            createdAt: message.sendTime || new Date().toISOString(),
            isRead: message.isRead || false,
            status: 'received'
          }
        } else {
          // 本地消息格式
          if (!message.senderId) message.senderId = friendId;
          if (!message.receiverId) message.receiverId = userStore.userId;
          if (!message.id) message.id = Date.now();
          if (!message.status) message.status = 'received';
          if (!message.isRead) message.isRead = false;
          // 使用createdAt代替timestamp
          if (!message.createdAt && message.timestamp) {
            message.createdAt = message.timestamp;
          } else if (!message.createdAt) {
            message.createdAt = new Date().toISOString();
          }
        }
      } else {
        // 兼容旧的调用方式
        message = {
          id: Date.now() + 1,
          content: messageObj,
          senderId: friendId,
          receiverId: userStore.userId,
          createdAt: new Date().toISOString(),
          isRead: false,
          status: 'received'
        }
      }

      // 确保消息数组存在
      if (!this.messages[friendId]) {
        this.messages[friendId] = []
      }

      // 检查消息是否已存在（避免重复）
      const existingIndex = this.messages[friendId].findIndex(m => m.id === message.id)
      if (existingIndex >= 0) {
        // 更新现有消息
        this.messages[friendId][existingIndex] = message
      } else {
        // 添加新消息
        this.messages[friendId].push(message)
      }

      // 按时间排序消息
      this.messages[friendId].sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))

      // 更新会话列表中的最后一条消息
      const conversation = this.conversations.find(c => c.id === friendId)
      if (conversation) {
        conversation.lastMessage = message.content
        conversation.lastMessageTime = message.createdAt
      } else {
        // 创建新会话
        this.conversations.push({
          id: friendId,
          name: this.friends.find(f => f.id === friendId)?.name || `用户${friendId}`,
          avatar: this.friends.find(f => f.id === friendId)?.avatar || '',
          lastMessage: message.content,
          lastMessageTime: message.createdAt,
          unreadCount: this.currentChat !== friendId && !message.isRead ? 1 : 0
        })
      }

      // 如果不是当前聊天且消息未读，增加未读数
      if (this.currentChat !== friendId && !message.isRead) {
        this.unreadCount[friendId] = (this.unreadCount[friendId] || 0) + 1
        // 更新会话列表中的未读数
        if (conversation) {
          conversation.unreadCount = this.unreadCount[friendId]
        }
      }

      // 按时间排序会话
      this.conversations.sort((a, b) => new Date(b.lastMessageTime) - new Date(a.lastMessageTime))
    },

    // 更新消息状态
    updateMessageStatus(messageId, friendId, status) {
      if (!this.messages[friendId]) return

      const message = this.messages[friendId].find(m => m.id === messageId)
      if (message) {
        message.status = status
      }
    },

    // 删除消息
    async deleteMessage(messageId, friendId) {
      this.loading = true
      this.clearError()
      try {
        await messageApi.deleteMessage(messageId)
        // 更新本地状态
        if (this.messages[friendId]) {
          const index = this.messages[friendId].findIndex(m => m.id === messageId)
          if (index >= 0) {
            this.messages[friendId].splice(index, 1)
          }
        }
        // 如果删除后没有消息，更新会话信息
        if (this.messages[friendId] && this.messages[friendId].length === 0) {
          const conversation = this.conversations.find(c => c.id === friendId)
          if (conversation) {
            conversation.lastMessage = ''
            conversation.lastMessageTime = new Date().toISOString()
          }
        }
      } catch (error) {
        this.setError('删除消息失败')
      } finally {
        this.loading = false
      }
    },

    // 删除聊天记录
    async deleteChatHistory(friendId) {
      this.loading = true
      this.clearError()
      try {
        const userStore = useUserStore()
      const userId = userStore.userId || ''
        await messageApi.deleteChatHistory(userId, friendId)
        // 更新本地状态
        this.clearMessages(friendId)
        this.unreadCount[friendId] = 0
        // 从会话列表中移除
        const index = this.conversations.findIndex(c => c.id === friendId)
        if (index > -1) {
          this.conversations.splice(index, 1)
        }
      } catch (error) {
        this.setError('删除聊天记录失败')
      } finally {
        this.loading = false
      }
    },

    // 清空指定聊天的消息
    clearMessages(friendId) {
      if (this.messages[friendId]) {
        this.messages[friendId] = [];
      }
      // 同时更新会话列表中的最后消息
      const conversation = this.conversations.find(c => c.id === friendId)
      if (conversation) {
        conversation.lastMessage = ''
        conversation.lastMessageTime = new Date().toISOString()
      }
    },

    // 更新未读消息计数
    updateUnreadCount(data) {
      // 这里可以根据API返回的数据更新各个会话的未读计数
      if (data.unreadByConversation) {
        Object.entries(data.unreadByConversation).forEach(([friendId, count]) => {
          const id = parseInt(friendId);
          if (this.currentChat !== id) {
            this.unreadCount[id] = count;
            // 同时更新会话列表中的未读数
            const conversation = this.conversations.find(c => c.id === id);
            if (conversation) {
              conversation.unreadCount = count;
            }
          }
        });
      }
    },

    // 搜索消息
    async searchMessages(keyword, friendId = null) {
      this.loading = true
      this.clearError()
      try {
        const userId = localStorage.getItem('userId') || 'current'
        let results
        if (friendId) {
          // 搜索与特定好友的聊天记录
          results = await messageApi.searchChatMessages(userId, friendId, keyword)
        } else {
          // 搜索用户所有消息
          results = await messageApi.searchUserMessages(userId, keyword)
        }
        // 格式化结果
        if (Array.isArray(results)) {
          return results.map(msg => ({
            id: msg.messageId,
            content: msg.content,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            createdAt: msg.sendTime,
            isRead: msg.isRead
          }))
        }
        return []
      } catch (error) {
        this.setError('搜索消息失败')
        return []
      } finally {
        this.loading = false
      }
    },

    // 清除聊天
    clearChat(friendId) {
      this.clearMessages(friendId)
      this.unreadCount[friendId] = 0
      // 从会话列表中移除
      const index = this.conversations.findIndex(c => c.id === friendId)
      if (index > -1) {
        this.conversations.splice(index, 1)
      }
    },

    // 生成模拟回复
    generateReply() {
      const replies = [
        '好的，明白了！',
        '收到，我稍后回复你。',
        '哈哈，太有趣了！',
        '真的吗？那太好了！',
        '我知道了，谢谢！',
        '好的，没问题！',
        '这个我需要考虑一下。',
        '有时间我们再聊吧。'
      ]
      return replies[Math.floor(Math.random() * replies.length)]
    },

    // 添加好友
    addFriend(friend) {
      if (!this.friends.find(f => f.id === friend.id)) {
        this.friends.push(friend)
      }
    },

    // 删除好友
    removeFriend(friendId) {
      const index = this.friends.findIndex(f => f.id === friendId)
      if (index > -1) {
        this.friends.splice(index, 1)
        // 删除相关会话和消息
        const convIndex = this.conversations.findIndex(c => c.id === friendId)
        if (convIndex > -1) {
          this.conversations.splice(convIndex, 1)
        }
        delete this.messages[friendId]
        delete this.unreadCount[friendId]
        // 如果正在聊天，清除当前聊天
        if (this.currentChat === friendId) {
          this.currentChat = null
        }
      }
    },

    // 屏蔽用户
    blockUser(friendId) {
      // 移除好友
      this.removeFriend(friendId)
      // 这里可以添加额外的屏蔽逻辑，如添加到屏蔽列表等
    },

    // 更新好友状态
    updateFriendStatus(friendId, status) {
      const friend = this.getFriendInfo(friendId)
      if (friend) {
        friend.status = status
        friend.lastSeen = new Date().toISOString()
      }
    }
  }
})
