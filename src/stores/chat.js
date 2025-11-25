import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    friends: [], // 好友列表
    conversations: [], // 会话列表（包含好友信息和最新消息）
    messages: {}, // 消息记录 { friendId: [messages] }
    currentChat: null, // 当前聊天的好友ID
    unreadCount: {} // 未读消息数 { friendId: count }
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

    // 设置当前聊天的好友
    setCurrentChat(friendId) {
      this.currentChat = friendId
      // 清除未读消息数
      if (this.unreadCount[friendId]) {
        this.unreadCount[friendId] = 0
      }
    },

    // 发送消息
    sendMessage(friendId, message) {
      // 初始化该好友的消息数组
      if (!this.messages[friendId]) {
        this.messages[friendId] = []
      }

      // 添加消息到消息存储
      this.messages[friendId].push(message)

      // 更新会话列表中的最后一条消息和时间
      const conversation = this.conversations.find(c => c.id === friendId)
      if (conversation) {
        conversation.lastMessage = message.content
        conversation.lastMessageTime = message.createdAt || message.timestamp
      } else {
        // 如果会话不存在，创建新会话
        this.conversations.push({
          id: friendId,
          name: this.friends.find(f => f.id === friendId)?.name || '未知用户',
          avatar: this.friends.find(f => f.id === friendId)?.avatar || '',
          lastMessage: message.content,
          lastMessageTime: message.createdAt || message.timestamp,
          unreadCount: 0
        })
      }

      // 按最后消息时间排序会话列表
      this.conversations.sort((a, b) => {
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
      })
    },

    // 接收消息
    receiveMessage(friendId, messageObj) {
      // 支持传入完整消息对象
      let message;
      if (typeof messageObj === 'object') {
        message = messageObj;
        // 确保消息有必要的字段
        if (!message.senderId) message.senderId = friendId;
        if (!message.receiverId) message.receiverId = 'current';
        if (!message.id) message.id = Date.now();
        if (!message.status) message.status = 'received';
        // 使用createdAt代替timestamp
        if (!message.createdAt && message.timestamp) {
          message.createdAt = message.timestamp;
        } else if (!message.createdAt) {
          message.createdAt = new Date().toISOString();
        }
      } else {
        // 兼容旧的调用方式
        message = {
          id: Date.now() + 1,
          content: messageObj,
          senderId: friendId,
          receiverId: 'current',
          createdAt: new Date().toISOString(),
          status: 'received'
        }
      }

      // 确保消息数组存在
      if (!this.messages[friendId]) {
        this.messages[friendId] = []
      }

      // 添加消息到消息存储
      this.messages[friendId].push(message)

      // 更新会话列表中的最后一条消息
      const conversation = this.conversations.find(c => c.id === friendId)
      if (conversation) {
        conversation.lastMessage = message.content
        conversation.lastMessageTime = message.createdAt
      } else {
        // 创建新会话
        this.conversations.push({
          id: friendId,
          name: this.friends.find(f => f.id === friendId)?.name || '未知用户',
          avatar: this.friends.find(f => f.id === friendId)?.avatar || '',
          lastMessage: message.content,
          lastMessageTime: message.createdAt,
          unreadCount: this.currentChat !== friendId ? 1 : 0
        })
      }

      // 如果不是当前聊天，增加未读数
      if (this.currentChat !== friendId) {
        this.unreadCount[friendId] = (this.unreadCount[friendId] || 0) + 1
        // 更新会话列表中的未读数
        if (conversation) {
          conversation.unreadCount = this.unreadCount[friendId]
        }
      }

      // 按时间排序会话
      this.conversations.sort((a, b) => {
        return new Date(b.lastMessageTime) - new Date(a.lastMessageTime)
      })
    },

    // 更新消息状态
    updateMessageStatus(messageId, friendId, status) {
      if (!this.messages[friendId]) return

      const message = this.messages[friendId].find(m => m.id === messageId)
      if (message) {
        message.status = status
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
