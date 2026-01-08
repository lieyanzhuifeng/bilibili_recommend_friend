import { defineStore } from 'pinia'
import { messageApi, userApi, friendApi } from '../services/api'
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

    // 获取聊天伙伴列表（聊天过的用户和好友的并集）
    async fetchChatPartners() {
      try {
        const userStore = useUserStore()
        const userId = userStore.userId || ''

        // 从本地存储获取聊天过的用户ID列表
        const chattedUsers = JSON.parse(localStorage.getItem('chattedUsers') || '[]')

        let friendIds = []

        // 只有当userId有效时，才从API获取好友列表
        if (userId) {
          // 从API获取好友列表
          const friendsResponse = await friendApi.getFriendList(userId)
          this.friends = friendsResponse.data || []

          // 提取好友ID列表
          friendIds = this.friends.map(friend => friend.id)
        } else {
          // 如果userId无效，使用空好友列表
          this.friends = []
          friendIds = []
        }

        // 合并聊天用户和好友ID，使用Map确保每个ID只保留一个，并且优先标记为好友
        const partnerMap = new Map()

        // 首先添加聊天用户
        chattedUsers.forEach(id => {
          if (id !== userId) {
            partnerMap.set(id, {
              id,
              isFriend: friendIds.includes(id)
            })
          }
        })

        // 然后添加好友（确保所有好友都被包含）
        friendIds.forEach(id => {
          if (id !== userId) {
            partnerMap.set(id, {
              id,
              isFriend: true // 明确标记为好友
            })
          }
        })

        // 转换为数组
        const filteredPartners = Array.from(partnerMap.keys())

        // 为非好友用户获取完整信息
        const userInfoMap = {}
        for (const partnerId of filteredPartners) {
          // 如果不是好友，调用API获取用户信息
          if (!friendIds.includes(partnerId)) {
            try {
              const userInfoResponse = await userApi.getUserInfo(partnerId)
              if (userInfoResponse && userInfoResponse.data) {
                userInfoMap[partnerId] = userInfoResponse.data
              }
            } catch (err) {
              console.log(`获取用户${partnerId}信息失败，使用默认信息`, err)
            }
          }
        }

        // 更新会话列表，传递用户信息映射
        this.updateConversationsFromPartners(filteredPartners, userInfoMap)

        return filteredPartners
      } catch (error) {
        console.error('获取聊天伙伴失败:', error)
        return []
      }
    },

    // 根据伙伴列表更新会话
    updateConversationsFromPartners(partners, userInfoMap = {}) {
      if (!Array.isArray(partners)) return

      partners.forEach(partnerId => {
        // 查找现有会话
        const existingConversation = this.conversations.find(c => c.id === partnerId)

        // 从好友列表中查找用户信息
        const friendInfo = this.friends.find(f => f.id === partnerId)
        // 从用户信息映射中查找非好友用户信息
        const userInfo = userInfoMap[partnerId]

        // 优先使用好友信息，然后是非好友用户信息
        const displayInfo = friendInfo || userInfo

        if (existingConversation) {
          // 如果会话已存在，更新信息（特别是好友状态）
          existingConversation.name = displayInfo?.username || displayInfo?.name || `用户${partnerId}`
          existingConversation.avatar = displayInfo?.avatar || ''
          existingConversation.isFriend = !!friendInfo // 更新好友状态
          existingConversation.unreadCount = this.unreadCount[partnerId] || 0
        } else {
          // 如果会话不存在，创建新会话
          this.conversations.push({
            id: partnerId,
            name: displayInfo?.username || displayInfo?.name || `用户${partnerId}`,
            avatar: displayInfo?.avatar || '',
            lastMessage: '',
            lastMessageTime: new Date().toISOString(),
            unreadCount: this.unreadCount[partnerId] || 0,
            isFriend: !!friendInfo // 标记是否为好友
          })
        }
      })

      // 确保没有重复会话
      this.removeDuplicateConversations()
    },

    // 移除重复的会话
    removeDuplicateConversations() {
      const uniqueIds = new Set()
      const uniqueConversations = []

      // 倒序遍历，保留最后出现的相同ID会话（通常包含最新信息）
      for (let i = this.conversations.length - 1; i >= 0; i--) {
        const conversation = this.conversations[i]
        if (!uniqueIds.has(conversation.id)) {
          uniqueIds.add(conversation.id)
          uniqueConversations.unshift(conversation) // 保持原始顺序
        }
      }

      this.conversations = uniqueConversations
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

          // 获取本地临时消息
          const tempMessages = this.messages[friendId] ?
            this.messages[friendId].filter(msg => msg.isTemp && msg.status !== 'failed') : []

          // 检查是否需要移除API返回的重复消息
          let finalMessages = [...formattedMessages]
          if (tempMessages.length > 0 && formattedMessages.length > 0) {
            // 检查是否有临时消息与API最后一条消息重复
            const lastApiMessage = formattedMessages[formattedMessages.length - 1]

            const hasDuplicate = tempMessages.some(tempMsg =>
              tempMsg.content === lastApiMessage.content &&
              Math.abs(new Date(tempMsg.createdAt) - new Date(lastApiMessage.createdAt)) < 5000 &&
              tempMsg.senderId === lastApiMessage.senderId &&
              tempMsg.receiverId === lastApiMessage.receiverId
            )

            if (hasDuplicate) {
              finalMessages = formattedMessages.slice(0, -1)
            }
          }

          // 合并临时消息和API消息
          this.messages[friendId] = [...finalMessages, ...tempMessages]
            .sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
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
    async fetchRecentChat(friendId, limit = 50, lastMessageTime = 0) {
      this.loading = true
      this.clearError()
      try {
        const userStore = useUserStore()
      const userId = userStore.userId
        // 传入lastMessageTime参数，只获取最新消息
        const messages = await messageApi.getRecentChat(userId, friendId, limit, lastMessageTime)
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

          // 获取本地临时消息
          const tempMessages = this.messages[friendId].filter(msg => msg.isTemp)

          // 检查是否需要移除API返回的重复消息
          let filteredMessages = [...formattedMessages]
          if (tempMessages.length > 0 && formattedMessages.length > 0) {
            // 只检查最近的临时消息和API返回的最后一条消息
            const lastApiMessage = formattedMessages[formattedMessages.length - 1]

            // 检查是否有临时消息与API最后一条消息重复
            const hasDuplicate = tempMessages.some(tempMsg =>
              // 比较内容、时间（近似）、发送者和接收者
              tempMsg.content === lastApiMessage.content &&
              Math.abs(new Date(tempMsg.createdAt) - new Date(lastApiMessage.createdAt)) < 5000 && // 5秒内视为同一时间
              tempMsg.senderId === lastApiMessage.senderId &&
              tempMsg.receiverId === lastApiMessage.receiverId
            )

            // 如果有重复，移除API返回的最后一条消息
            if (hasDuplicate) {
              filteredMessages = formattedMessages.slice(0, -1)
            }
          }

          // 去重并合并 - 改进版：不仅通过ID去重，还通过内容和时间戳去重，防止临时消息转正后重复
          // 同时删除已被服务器确认的临时消息
          const existingMessages = [...this.messages[friendId]]
          const newMessages = []
          const tempMessagesToRemove = []

          // 首先，收集所有已被服务器确认的临时消息（通过isServerConfirmed标志）
          existingMessages.forEach((msg, index) => {
            if (msg.isTemp && msg.isServerConfirmed) {
              tempMessagesToRemove.push(index)
            }
          })

          // 遍历API返回的消息，检查是否为真正的新消息
          for (const apiMsg of filteredMessages) {
            // 检查是否已存在相同ID的消息
            const idExists = existingMessages.some(m => m.id === apiMsg.id)

            // 检查是否存在内容、发送者、接收者相同且时间相近的消息（处理临时消息转正情况）
            const contentExists = existingMessages.some((m, index) => {
              if (m.content === apiMsg.content &&
                  m.senderId === apiMsg.senderId &&
                  m.receiverId === apiMsg.receiverId &&
                  Math.abs(new Date(m.createdAt) - new Date(apiMsg.createdAt)) < 5000) {
                // 如果是临时消息，标记为需要删除
                if (m.isTemp) {
                  tempMessagesToRemove.push(index)
                }
                return true
              }
              return false
            })

            // 只有当消息完全新时才添加
            if (!idExists && !contentExists) {
              newMessages.push(apiMsg)
              existingMessages.push(apiMsg)
            }
          }

          // 删除已被服务器确认的临时消息
          // 从后往前删除，避免索引变化问题
          // 使用Set确保每个索引只删除一次
          const uniqueIndexes = Array.from(new Set(tempMessagesToRemove)).sort((a, b) => b - a)
          for (let i = 0; i < uniqueIndexes.length; i++) {
            existingMessages.splice(uniqueIndexes[i], 1)
          }

          // 更新消息列表并排序
          this.messages[friendId] = existingMessages.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt))
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
        status: 'sending',
        isTemp: true // 标记为临时消息
      }

      // 先添加到本地（乐观更新）
      if (!this.messages[friendId]) {
        this.messages[friendId] = []
      }
      this.messages[friendId].push(message)

      try {
        // 调用API发送消息
        await messageApi.sendMessage(userId, friendId, messageContent)
        // 更新消息状态，但保留临时标记以便在下一轮刷新中删除
        message.status = 'sent'
        message.isServerConfirmed = true // 标记为已被服务器确认

        // 确保会话存在且信息正确
        const conversation = this.conversations.find(c => c.id === friendId)
        const isFriendNow = !!this.friends.find(f => f.id === friendId)

        if (conversation) {
          // 更新会话信息
          conversation.lastMessage = message.content
          conversation.lastMessageTime = message.createdAt

          // 更新好友状态
          if (conversation.isFriend !== isFriendNow) {
            conversation.isFriend = isFriendNow
            // 如果变成好友，获取完整用户信息
            if (isFriendNow) {
              const friendInfo = this.friends.find(f => f.id === friendId)
              if (friendInfo) {
                conversation.name = friendInfo.username || friendInfo.name || conversation.name
                conversation.avatar = friendInfo.avatar || conversation.avatar
              }
            }
          }
        } else {
          // 如果会话不存在，检查是否为好友
          const friendInfo = this.friends.find(f => f.id === friendId)

          // 如果是好友，直接使用好友信息
          if (friendInfo) {
            this.conversations.push({
              id: friendId,
              name: friendInfo.username || friendInfo.name || `用户${friendId}`,
              avatar: friendInfo.avatar || '',
              lastMessage: message.content,
              lastMessageTime: message.createdAt,
              unreadCount: 0,
              isFriend: true
            })
          } else {
            // 如果不是好友，尝试获取用户信息
            try {
              const userInfo = await userApi.getUserInfo(friendId)
              this.conversations.push({
                id: friendId,
                name: userInfo?.username || `用户${friendId}`,
                avatar: userInfo?.avatar || '',
                lastMessage: message.content,
                lastMessageTime: message.createdAt,
                unreadCount: 0,
                isFriend: false
              })
            } catch (error) {
              console.error('Failed to get user info:', error)
              // 如果获取失败，使用默认信息
              this.conversations.push({
                id: friendId,
                name: `用户${friendId}`,
                avatar: '',
                lastMessage: message.content,
                lastMessageTime: message.createdAt,
                unreadCount: 0,
                isFriend: false
              })
            }
          }
        }
      } catch (error) {
        this.setError('发送消息失败')
        // 只在发送失败时设置failed状态并保留临时标记
        message.status = 'failed'
        // 保持isTemp为true，用于在UI中显示失败标志
      } finally {
        this.loading = false
      }

      // 按最后消息时间排序会话列表
      this.conversations.sort((a, b) => {
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
      })
    },

    // 接收消息
    async receiveMessage(friendId, messageObj, fromApi = false) {
      const userStore = useUserStore()

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

      // 确保会话存在且信息正确
      const conversation = this.conversations.find(c => c.id === friendId)
      const isFriendNow = !!this.friends.find(f => f.id === friendId)

      if (conversation) {
        // 更新会话信息
        conversation.lastMessage = message.content
        conversation.lastMessageTime = message.createdAt

        // 更新好友状态
        if (conversation.isFriend !== isFriendNow) {
          conversation.isFriend = isFriendNow
          // 如果变成好友，获取完整用户信息
          if (isFriendNow) {
            const friendInfo = this.friends.find(f => f.id === friendId)
            if (friendInfo) {
              conversation.name = friendInfo.username || friendInfo.name || conversation.name
              conversation.avatar = friendInfo.avatar || conversation.avatar
            }
          }
        }
      } else {
        // 如果会话不存在，检查是否为好友
        const friendInfo = this.friends.find(f => f.id === friendId)

        // 如果是好友，直接使用好友信息
        if (friendInfo) {
          this.conversations.push({
            id: friendId,
            name: friendInfo.username || friendInfo.name || `用户${friendId}`,
            avatar: friendInfo.avatar || '',
            lastMessage: message.content,
            lastMessageTime: message.createdAt,
            unreadCount: this.currentChat !== friendId && !message.isRead ? 1 : 0,
            isFriend: true
          })
        } else {
          // 如果不是好友，尝试获取用户信息
          try {
            const userInfo = await userApi.getUserInfo(friendId)
            this.conversations.push({
              id: friendId,
              name: userInfo?.username || `用户${friendId}`,
              avatar: userInfo?.avatar || '',
              lastMessage: message.content,
              lastMessageTime: message.createdAt,
              unreadCount: this.currentChat !== friendId && !message.isRead ? 1 : 0,
              isFriend: false
            })
          } catch (error) {
            console.error('Failed to get user info:', error)
            // 如果获取失败，使用默认信息
            this.conversations.push({
              id: friendId,
              name: `用户${friendId}`,
              avatar: '',
              lastMessage: message.content,
              lastMessageTime: message.createdAt,
              unreadCount: this.currentChat !== friendId && !message.isRead ? 1 : 0,
              isFriend: false
            })
          }
        }
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
