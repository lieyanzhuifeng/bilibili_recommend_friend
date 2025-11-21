import { defineStore } from 'pinia'
import {
  sendMessage as apiSendMessage,
  getRecentChat,
  getNewMessagesById,
  getFullChatHistory,
  getUnreadMessageCount,
  getChatPartners,
  searchMessagesInChat,
  sendFriendRequest,
  getFriendsList,
  acceptFriendRequest,
  rejectFriendRequest
} from '@/services/api.js'
import { useUserStore } from './user.js'

export const useChatStore = defineStore('chat', {
  state: () => ({
    friends: [], // 好友列表
    conversations: {}, // 会话记录 { friendId: [messages] }
    currentChat: null, // 当前聊天的好友ID
    unreadCount: {}, // 未读消息数 { friendId: count }
    lastMessageIds: {} // 最后消息ID { friendId: messageId }
  }),

  getters: {
    // 获取当前用户ID
    currentUser() {
      const userStore = useUserStore()
      return userStore.userId || 1 // 如果没有获取到用户ID，默认使用1
    },

    // 获取所有好友
    getAllFriends: (state) => state.friends,

    // 获取指定好友的会话记录
    getConversation: (state) => (friendId) => {
      return state.conversations[friendId] || []
    },

    // 获取当前聊天的会话记录
    getCurrentConversation: (state) => {
      if (!state.currentChat) return []
      return state.conversations[state.currentChat] || []
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
    // 初始化数据
    async initData() {
      // 优先从localStorage加载好友列表
      this.loadFriendsFromLocalStorage();

      // 如果localStorage中没有好友数据，则从API获取
      if (this.friends.length === 0) {
        try {
          // 获取真实的好友列表
          const result = await getFriendsList(this.currentUser)
          if (result.success) {
            // 将好友数据转换为前端需要的格式
            this.friends = result.data.map(user => ({
              id: user.userId,
              username: user.username || `用户${user.userId}`,
              avatar: user.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${user.userId}`,
              status: user.status || (Math.random() > 0.5 ? 'online' : 'offline'),
              lastSeen: user.lastSeen || (Math.random() > 0.5 ? '刚刚在线' : '1小时前在线'),
              bio: user.bio || '这个用户很懒，什么都没有写~',
              level: user.level || Math.floor(Math.random() * 6) + 1
            }))

            // 保存到localStorage
            this.saveFriendsToLocalStorage();
          }
        } catch (e) {
          console.error('初始化好友数据失败:', e)
        }
      }

      // 获取所有好友的聊天记录
      if (this.friends.length > 0) {
        for (const friend of this.friends) {
          try {
            const result = await getRecentChat(this.currentUser, friend.id)
            if (result.success) {
              // 转换消息格式以匹配前端需求
              const messages = result.data.map(msg => ({
                id: msg.messageId,
                content: msg.content,
                senderId: msg.senderId,
                receiverId: msg.receiverId,
                timestamp: msg.sendTime
              }));

              // 存储消息
              this.conversations[friend.id] = messages;

              // 记录最后消息ID用于增量获取
              if (messages.length > 0) {
                this.lastMessageIds[friend.id] = messages[messages.length - 1].id;
              }
            }
          } catch (e) {
            console.error(`获取与用户${friend.id}的聊天记录失败:`, e);
          }
        }
      }
    },

    // 获取聊天伙伴列表
    async fetchChatPartners() {
      try {
        const result = await getChatPartners(this.currentUser)
        if (result.success) {
          // 根据partner IDs构建好友列表（实际应用中应该从用户服务获取详细信息）
          this.friends = result.data.map(id => ({
            id,
            username: `用户${id}`,
            avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`,
            status: Math.random() > 0.5 ? 'online' : 'offline',
            lastSeen: new Date().toISOString()
          }))
        }
      } catch (e) {
        console.error('获取聊天伙伴列表失败:', e)
      }
    },

    // 获取最近聊天记录
    async fetchRecentChats() {
      for (const friend of this.friends) {
        try {
          const result = await getRecentChat(this.currentUser, friend.id)
          if (result.success) {
            // 转换消息格式以匹配前端需求
            const messages = result.data.map(msg => ({
              id: msg.messageId,
              content: msg.content,
              senderId: msg.senderId,
              receiverId: msg.receiverId,
              timestamp: msg.sendTime
            }))

            // 存储消息
            this.conversations[friend.id] = messages

            // 记录最后消息ID用于增量获取
            if (messages.length > 0) {
              this.lastMessageIds[friend.id] = messages[messages.length - 1].id
            }
          }
        } catch (e) {
          console.error(`获取与用户${friend.id}的聊天记录失败:`, e)
        }
      }
    },

    // 获取新消息（增量获取）
    async fetchNewMessages(friendId) {
      try {
        const lastMessageId = this.lastMessageIds[friendId] || 0
        const result = await getNewMessagesById(this.currentUser, lastMessageId)

        if (result.success && result.data.length > 0) {
          // 转换消息格式
          const newMessages = result.data.map(msg => ({
            id: msg.messageId,
            content: msg.content,
            senderId: msg.senderId,
            receiverId: msg.receiverId,
            timestamp: msg.sendTime
          }))

          // 添加到现有对话中
          if (!this.conversations[friendId]) {
            this.conversations[friendId] = []
          }

          this.conversations[friendId].push(...newMessages)

          // 更新最后消息ID
          const lastMsg = newMessages[newMessages.length - 1]
          this.lastMessageIds[friendId] = lastMsg.id

          // 如果不是当前聊天，增加未读数
          if (this.currentChat !== friendId) {
            this.unreadCount[friendId] = (this.unreadCount[friendId] || 0) + newMessages.length
          }

          return newMessages
        }
      } catch (e) {
        console.error(`获取与用户${friendId}的新消息失败:`, e)
      }

      return []
    },

    // 设置当前聊天的好友
    setCurrentChat(friendId) {
      this.currentChat = friendId
      // 清除未读消息数
      if (this.unreadCount[friendId]) {
        this.unreadCount[friendId] = 0
      }
    },

    // 发送消息
    async sendMessage(friendId, content) {
      try {
        // 创建本地消息对象用于即时显示
        const localMessage = {
          id: Date.now(), // 临时ID，后续会被真实ID替换
          content,
          senderId: this.currentUser,
          receiverId: friendId,
          timestamp: new Date().toISOString(),
          status: 'sending' // 发送状态
        }

        // 添加到本地对话中
        if (!this.conversations[friendId]) {
          this.conversations[friendId] = []
        }

        this.conversations[friendId].push(localMessage)

        // 调用API发送消息
        const result = await apiSendMessage(this.currentUser, friendId, content)

        if (result.success) {
          // 更新消息状态为已发送
          localMessage.status = 'sent'

          // 获取真实的服务器消息（如果需要精确同步）
          // 这里可以根据实际需求决定是否重新获取最新消息
        } else {
          // 发送失败，更新状态
          localMessage.status = 'failed'
          console.error('发送消息失败:', result.error)
        }
      } catch (e) {
        // 发送失败，更新状态
        if (this.conversations[friendId] && this.conversations[friendId].length > 0) {
          const lastMessage = this.conversations[friendId][this.conversations[friendId].length - 1]
          if (lastMessage.content === content && lastMessage.status === 'sending') {
            lastMessage.status = 'failed'
          }
        }
        console.error('发送消息异常:', e)
      }
    },

    // 接收消息（通常由轮询或WebSocket触发）
    receiveMessage(friendId, message) {
      // 转换消息格式
      const receivedMessage = {
        id: message.messageId,
        content: message.content,
        senderId: message.senderId,
        receiverId: message.receiverId,
        timestamp: message.sendTime
      }

      if (!this.conversations[friendId]) {
        this.conversations[friendId] = []
      }

      this.conversations[friendId].push(receivedMessage)

      // 更新最后消息ID
      this.lastMessageIds[friendId] = receivedMessage.id

      // 如果不是当前聊天，增加未读数
      if (this.currentChat !== friendId) {
        this.unreadCount[friendId] = (this.unreadCount[friendId] || 0) + 1
      }
    },

    // 添加好友
    async addFriend(friend) {
      // 先检查是否已经是好友
      if (this.friends.find(f => f.id === friend.id)) {
        return { success: false, message: '该用户已经是您的好友' };
      }

      try {
        // 调用后端API发送好友申请
        const result = await sendFriendRequest(this.currentUser, friend.id);

        if (result.success) {
          // 添加到本地好友列表
          this.friends.push({
            ...friend,
            status: 'pending', // 标记为待确认状态
            lastSeen: '刚刚发送申请',
            bio: friend.bio || '这个用户很懒，什么都没有写~',
            level: friend.level || Math.floor(Math.random() * 6) + 1
          });

          // 保存到localStorage
          this.saveFriendsToLocalStorage();

          return { success: true, message: '好友申请已发送' };
        } else {
          return { success: false, message: result.error || '发送好友申请失败' };
        }
      } catch (error) {
        console.error('添加好友失败:', error);
        return { success: false, message: '网络错误，请稍后重试' };
      }
    },

    // 接受好友申请
    async acceptFriendRequest(requesterId) {
      try {
        const result = await acceptFriendRequest(this.currentUser, requesterId);

        if (result.success) {
          // 更新好友状态
          const friend = this.friends.find(f => f.id === requesterId);
          if (friend) {
            friend.status = 'friend';
            friend.lastSeen = '刚刚成为好友';
          }

          // 保存到localStorage
          this.saveFriendsToLocalStorage();

          return { success: true, message: '已接受好友申请' };
        } else {
          return { success: false, message: result.error || '接受好友申请失败' };
        }
      } catch (error) {
        console.error('接受好友申请失败:', error);
        return { success: false, message: '网络错误，请稍后重试' };
      }
    },

    // 拒绝好友申请
    async rejectFriendRequest(requesterId) {
      try {
        const result = await rejectFriendRequest(this.currentUser, requesterId);

        if (result.success) {
          // 从好友列表中移除
          this.friends = this.friends.filter(f => f.id !== requesterId);

          // 保存到localStorage
          this.saveFriendsToLocalStorage();

          return { success: true, message: '已拒绝好友申请' };
        } else {
          return { success: false, message: result.error || '拒绝好友申请失败' };
        }
      } catch (error) {
        console.error('拒绝好友申请失败:', error);
        return { success: false, message: '网络错误，请稍后重试' };
      }
    },

    // 保存好友列表到localStorage
    saveFriendsToLocalStorage() {
      localStorage.setItem('friends', JSON.stringify(this.friends))
    },

    // 从localStorage加载好友列表
    loadFriendsFromLocalStorage() {
      const friendsStr = localStorage.getItem('friends')
      if (friendsStr) {
        try {
          this.friends = JSON.parse(friendsStr)
          // 确保friends始终是数组
          if (!Array.isArray(this.friends)) {
            this.friends = []
          }
        } catch (e) {
          console.error('Failed to parse friends from localStorage', e)
          // 解析失败时清空friends
          this.friends = []
        }
      } else {
        // 如果localStorage中没有friends项，初始化为空数组
        this.friends = []
      }
    },

    // 删除好友
    removeFriend(friendId) {
      const index = this.friends.findIndex(f => f.id === friendId)
      if (index > -1) {
        this.friends.splice(index, 1)
        // 删除相关会话
        delete this.conversations[friendId]
        delete this.unreadCount[friendId]
        delete this.lastMessageIds[friendId]
        // 如果正在聊天，清除当前聊天
        if (this.currentChat === friendId) {
          this.currentChat = null
        }
        // 更新localStorage
        this.saveFriendsToLocalStorage()
      }
    },

    // 更新好友状态
    updateFriendStatus(friendId, status) {
      const friend = this.getFriendInfo(friendId)
      if (friend) {
        friend.status = status
        friend.lastSeen = new Date().toISOString()
        // 更新localStorage
        this.saveFriendsToLocalStorage()
      }
    }
  }
})
