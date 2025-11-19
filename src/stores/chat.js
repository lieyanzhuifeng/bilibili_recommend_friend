import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    friends: [], // 好友列表
    conversations: {}, // 会话记录 { friendId: [messages] }
    currentChat: null, // 当前聊天的好友ID
    unreadCount: {} // 未读消息数 { friendId: count }
  }),
  
  getters: {
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
    // 初始化模拟数据
    initMockData() {
      // 模拟好友数据
      this.friends = [
        {
          id: 1,
          username: '小明',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xm',
          status: 'online',
          lastSeen: new Date().toISOString()
        },
        {
          id: 2,
          username: '小红',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xh',
          status: 'offline',
          lastSeen: new Date(Date.now() - 3600000).toISOString()
        },
        {
          id: 3,
          username: '小李',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=xl',
          status: 'online',
          lastSeen: new Date().toISOString()
        }
      ]
      
      // 模拟会话数据
      this.conversations = {
        1: [
          { id: 1, content: '你好！', senderId: 1, receiverId: 'current', timestamp: new Date(Date.now() - 3600000).toISOString() },
          { id: 2, content: '你好，有什么事吗？', senderId: 'current', receiverId: 1, timestamp: new Date(Date.now() - 3500000).toISOString() }
        ],
        2: [
          { id: 3, content: '明天一起去看电影吗？', senderId: 'current', receiverId: 2, timestamp: new Date(Date.now() - 7200000).toISOString() }
        ]
      }
      
      // 模拟未读消息
      this.unreadCount = {
        3: 2
      }
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
    sendMessage(friendId, content) {
      const message = {
        id: Date.now(),
        content,
        senderId: 'current',
        receiverId: friendId,
        timestamp: new Date().toISOString()
      }
      
      if (!this.conversations[friendId]) {
        this.conversations[friendId] = []
      }
      
      this.conversations[friendId].push(message)
      
      // 模拟对方回复（随机时间）
      if (Math.random() > 0.5) {
        setTimeout(() => {
          this.receiveMessage(friendId, this.generateReply(content))
        }, 1000 + Math.random() * 3000)
      }
    },
    
    // 接收消息
    receiveMessage(friendId, content) {
      const message = {
        id: Date.now() + 1,
        content,
        senderId: friendId,
        receiverId: 'current',
        timestamp: new Date().toISOString()
      }
      
      if (!this.conversations[friendId]) {
        this.conversations[friendId] = []
      }
      
      this.conversations[friendId].push(message)
      
      // 如果不是当前聊天，增加未读数
      if (this.currentChat !== friendId) {
        this.unreadCount[friendId] = (this.unreadCount[friendId] || 0) + 1
      }
    },
    
    // 生成模拟回复
    generateReply(content) {
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
        // 删除相关会话
        delete this.conversations[friendId]
        delete this.unreadCount[friendId]
        // 如果正在聊天，清除当前聊天
        if (this.currentChat === friendId) {
          this.currentChat = null
        }
      }
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