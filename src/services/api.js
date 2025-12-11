const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

function buildQuery(params = {}) {
  const esc = encodeURIComponent
  const query = Object.keys(params)
    .filter((k) => params[k] !== undefined && params[k] !== null)
    .map((k) => `${esc(k)}=${esc(params[k])}`)
    .join('&')
  return query ? `?${query}` : ''
}

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`
  // 注入 Authorization header（如果 localStorage 中存在 token）
  const token = typeof localStorage !== 'undefined' ? localStorage.getItem('token') : null
  const headers = { ...(options.headers || {}) }
  if (token) headers['Authorization'] = `Bearer ${token}`

  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    const text = await res.text().catch(() => '')
    const err = new Error(`HTTP ${res.status} ${res.statusText}: ${text}`)
    err.status = res.status
    throw err
  }
  // Some endpoints may return empty body
  const contentType = res.headers.get('content-type') || ''
  if (contentType.includes('application/json')) {
    return res.json()
  }
  return res.text()
}

async function get(path, params) {
  const qp = buildQuery(params)
  return request(`${path}${qp}`, { method: 'GET' })
}

async function post(path, body) {
  const options = {
    method: 'POST',
  }

  // 只有当提供了body且不为空时，才添加Content-Type和body
  if (body !== undefined && body !== null) {
    options.headers = { 'Content-Type': 'application/json' }
    options.body = JSON.stringify(body)
  }

  return request(path, options)
}

export const userApi = {
  // 用户相关API（按照API2.md文档）
  getUserInfo: (userId) => get(`/api/users/${userId}`),
  // 搜索用户（根据关键词）
  searchUsers: (keyword) => get('/api/users/search', { keyword }),
  // 搜索视频（根据视频标题）
  searchVideos: (keyword) => get('/api/users/videos/search', { keyword }),
  // 搜索标签（根据标签名称）
  searchTags: (keyword) => get('/api/users/tags/search', { keyword }),
  // 获取用户完整画像
  getUserProfile: (userId) => get(`/api/user-profile/show/${userId}`)
}

export const friendApi = {
  sendRequest: (userId, targetUserId) => post(`/api/friends/request?userId=${userId}&targetUserId=${targetUserId}`),
  acceptRequest: (userId, requesterId) => post(`/api/friends/accept?userId=${userId}&requesterId=${requesterId}`),
  rejectRequest: (userId, requesterId) => post(`/api/friends/reject?userId=${userId}&requesterId=${requesterId}`),
  removeFriend: (userId, targetUserId) => request(`/api/friends/remove?userId=${userId}&targetUserId=${targetUserId}`, {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' }
  }),
  checkFriendship: (userId, targetUserId) => get(`/api/friends/check?userId=${userId}&targetUserId=${targetUserId}`),
  getFriendList: (userId) => get(`/api/friends/list/${userId}`),
  getFriendRequests: (userId) => get(`/api/friends/requests/${userId}`),
  getFriendCount: (userId) => get(`/api/friends/count/${userId}`),
  getRequestCount: (userId) => get(`/api/friends/requests/count/${userId}`),
  // 搜索用户方法（按照API2.md文档，参数名改为keyword）
  searchUsers: (keyword) => get('/api/users/search', { keyword }),
  // 新增方法，发送好友申请（适配组件中的调用方式）
  sendFriendRequest: (targetUserId) => {
    // 确保userId是有效的，避免使用'current'作为默认值
    const userStr = localStorage.getItem('user');
    let userId = null;

    if (userStr) {
      try {
        const userInfo = JSON.parse(userStr);
        userId = userInfo.userId || localStorage.getItem('userId');
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }

    if (!userId) {
      throw new Error('用户未登录，请先登录');
    }
    // 按照API2.md文档，使用请求体传递参数
    return post(`/api/friends/request?userId=${userId}&targetUserId=${targetUserId}`);
  },
  // 获取好友列表（适配组件中的调用方式）
  getFriends: () => {
    // 确保userId是有效的，避免使用'current'作为默认值
    const userStr = localStorage.getItem('user');
    let userId = null;

    if (userStr) {
      try {
        const userInfo = JSON.parse(userStr);
        userId = userInfo.userId || localStorage.getItem('userId');
      } catch (e) {
        console.error('解析用户信息失败:', e);
      }
    }

    if (!userId) {
      throw new Error('用户未登录，请先登录');
    }
    return get(`/api/friends/list/${userId}`);
  }
}

export const messageApi = {
  // 1. 发送消息
  sendMessage: (senderId, receiverId, content) => post(`/api/messages/send?senderId=${senderId}&receiverId=${receiverId}&content=${encodeURIComponent(content)}`),

  // 2. 获取最近聊天记录
  getRecentChat: (user1, user2, limit) => get('/api/messages/recent-chat', { user1, user2, limit }),

  // 3. 获取增量消息（基于ID）
  getNewMessagesById: (userId, lastMessageId) => get('/api/messages/new-messages/id', { userId, lastMessageId }),

  // 4. 获取增量消息（基于时间戳）
  getNewMessagesByTime: (userId, lastTime) => get('/api/messages/new-messages/time', { userId, lastTime }),

  // 5. 获取完整聊天记录
  getFullChat: (user1, user2) => get('/api/messages/full-chat', { user1, user2 }),

  // 6. 获取未读消息数量
  getUnreadCount: (userId, lastMessageId) => get('/api/messages/unread-count', { userId, lastMessageId }),

  // 7. 删除消息
  deleteMessage: (messageId) => request(`/api/messages/${messageId}`, { method: 'DELETE' }),

  // 8. 删除发送者所有消息
  deleteSenderMessages: (senderId) => request(`/api/messages/sender/${senderId}`, { method: 'DELETE' }),

  // 9. 删除接收者所有消息
  deleteReceiverMessages: (receiverId) => request(`/api/messages/receiver/${receiverId}`, { method: 'DELETE' }),

  // 10. 删除聊天记录
  deleteChatHistory: (user1, user2) => request('/api/messages/chat-history', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ user1, user2 })
  }),

  // 11. 获取发送者所有消息
  getSenderMessages: (senderId) => get(`/api/messages/sender/${senderId}`),

  // 12. 获取接收者所有消息
  getReceiverMessages: (receiverId) => get(`/api/messages/receiver/${receiverId}`),

  // 13. 获取时间段内聊天记录
  getTimeRangeMessages: (user1, user2, startTime, endTime) => get('/api/messages/time-range', { user1, user2, startTime, endTime }),

  // 14. 统计发送者消息数量
  getSenderMessageCount: (senderId) => get(`/api/messages/count/sender/${senderId}`),

  // 15. 统计接收者消息数量
  getReceiverMessageCount: (receiverId) => get(`/api/messages/count/receiver/${receiverId}`),

  // 16. 统计总消息数量
  getTotalMessageCount: () => get('/api/messages/count/total'),

  // 17. 搜索用户消息内容
  searchUserMessages: (userId, keyword) => get('/api/messages/search/user', { userId, keyword }),

  // 18. 搜索聊天记录内容
  searchChatMessages: (user1, user2, keyword) => get('/api/messages/search/chat', { user1, user2, keyword }),

  // 19. 标记消息为已读
  markMessageAsRead: (messageId) => request(`/api/messages/${messageId}/read`, { method: 'PUT' }),

  // 20. 标记所有消息为已读
  markAllAsRead: (userId, lastMessageId) => request('/api/messages/mark-all-read', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, lastMessageId })
  }),

  // 21. 获取聊天伙伴列表
  getChatPartners: (userId) => get(`/api/messages/chat-partners/${userId}`),

  // 组件适配方法
  // 发送消息适配
  sendMessageAdapted: async (data) => {
    const { receiverId, content } = data;
    const senderId = localStorage.getItem('userId') || 'current';
    const response = await post('/api/messages/send', { senderId, receiverId, content });
    return { message: response };
  },

  // 获取会话列表适配
  getConversations: async () => {
    const userId = localStorage.getItem('userId') || 'current';
    try {
      const partners = await get(`/api/messages/chat-partners/${userId}`);
      // 模拟转换为组件需要的格式
      const conversations = (Array.isArray(partners) ? partners : []).map(partnerId => ({
        id: partnerId,
        name: `用户${partnerId}`,
        avatar: avatarUrl({ id: partnerId }),
        isOnline: true,
        lastMessage: '',
        lastMessageTime: new Date().toISOString(),
        unreadCount: 0
      }));
      return { conversations };
    } catch (error) {
      console.error('获取会话列表失败:', error);
      return { conversations: [] };
    }
  },

  // 获取聊天历史适配
  getChatHistory: async (chatId) => {
    const userId = localStorage.getItem('userId') || 'current';
    try {
      const messages = await get('/api/messages/full-chat', { user1: userId, user2: chatId });
      const formattedMessages = (messages || []).map(msg => ({
        id: msg.messageId,
        content: msg.content,
        isMine: msg.senderId === userId,
        timestamp: msg.sendTime
      }));
      return { messages: formattedMessages };
    } catch (error) {
      console.error('获取聊天历史失败:', error);
      return { messages: [] };
    }
  },

  // 标记为已读适配
  markAsRead: async (chatId) => {
    const userId = localStorage.getItem('userId') || 'current';
    return request('/api/messages/mark-all-read', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, lastMessageId: null })
    });
  }
}

export const recommendApi = {
  // 推荐相关API（按照API2.md文档）
  coComment: (userId) => get(`/api/recommend/co-comment/${userId}`),
  reply: (userId) => get(`/api/recommend/reply/${userId}`),
  sharedVideo: (userId) => get(`/api/recommend/shared-video/${userId}`),
  category: (userId) => get(`/api/recommend/category/${userId}`),
  theme: (userId) => get(`/api/recommend/theme/${userId}`),
  favoriteSimilarity: (userId) => get(`/api/recommend/favorite-similarity/${userId}`),
  commentFriends: (userId) => get(`/api/recommend/comment-friends/${userId}`)
}

export const filterApi = {
  // 筛选相关API（按照API2.md文档）
  sameUp: ({ upId, durationOption } = {}) => get('/api/filter/same-up', { upId, durationOption }),
  sameTag: ({ tagId, durationOption } = {}) => get('/api/filter/same-tag', { tagId, durationOption }),
  sameUpVideoCount: (body) => post('/api/filter/same-up-video-count', body),
  sameTagVideoCount: ({ tagId, ratioOption } = {}) => get('/api/filter/same-tag-video-count', { tagId, ratioOption }),
  deepVideo: ({ videoId, option } = {}) => get('/api/filter/deep-video', { videoId, option }),

  // 其他筛选API
  followTime: ({ userId, upId } = {}) => get('/api/filter/follow-time', { userId, upId }),
  nightOwl: ({ option } = {}) => get('/api/filter/night-owl', { option }),
  userActivity: ({ option } = {}) => get('/api/filter/user-activity', { option }),
  series: ({ tagId } = {}) => get('/api/filter/series', { tagId }),

  // 责任链相关API（按照API2.md文档）
  chainSameUpVideoCount: (body, params = {}) => post(`/api/chain/same-up-video-count${buildQuery(params)}`, body),
  chainSameTagVideoCount: ({ tagId, ratioOption, ...params } = {}) => get('/api/chain/same-tag-video-count', { tagId, ratioOption, ...params }),
  chainDeepVideo: ({ videoId, option, ...params } = {}) => get('/api/chain/deep-video', { videoId, option, ...params }),
  chainSeries: ({ tagId, ...params } = {}) => get('/api/chain/series', { tagId, ...params }),
  // 新增：责任链筛选API
  filterByChain: (params) => post('/api/filter/filter-by-chain', params)
}

// 搜索相关API
export const searchApi = {
  // 搜索视频
  searchVideos: (keyword) => get('/api/users/videos/search', { keyword }),
  // 搜索标签
  searchTags: (keyword) => get('/api/users/tags/search', { keyword }),
  // 搜索UP主
  searchUps: (keyword) => get('/api/users/search', { keyword })
}

// 登录相关API
export const loginApi = {
  login: async ({ userId } = {}) => {
    console.log('登录请求参数:', { userId });
    try {
      const response = await get(`/api/users/${userId}`);
      console.log('登录响应结果:', response);
      return response;
    } catch (error) {
      console.error('登录API调用错误:', error);
      // 模拟API响应格式，以便前端正确处理
      throw {
        message: error.message || 'API调用失败'
      };
    }
  }
}

export default {
  userApi,
  friendApi,
  messageApi,
  recommendApi,
  filterApi,
  searchApi
}

// avatar URL helper: 若后端返回相对路径 (/avatars/...), 则拼接为完整 URL
// 如果没有头像路径，则返回随机头像
export function avatarUrl(pathOrObj, seed) {
  // 支持传入对象或字符串
  let path = ''
  if (typeof pathOrObj === 'string') path = pathOrObj
  else if (pathOrObj && pathOrObj.avatarPath) path = pathOrObj.avatarPath
  else if (pathOrObj && pathOrObj.avatar) path = pathOrObj.avatar
  else if (pathOrObj && pathOrObj.avatarUrl) path = pathOrObj.avatarUrl

  // 如果有有效路径，按规则返回
  if (path && (path.startsWith('http') || path.startsWith('data:'))) return path
  if (path && path.startsWith('/')) return `${API_BASE}${path}`
  if (path) return path

  // 如果没有有效路径，返回随机头像
  // 尝试从对象中提取种子信息
  let avatarSeed = seed || 'default'
  if (pathOrObj && !seed) {
    // 尝试从对象中获取用户名、ID等作为种子
    avatarSeed = pathOrObj.username ||
                 pathOrObj.name ||
                 pathOrObj.userId ||
                 pathOrObj.id ||
                 'default'
  }

  // 标准化种子字符串
  avatarSeed = String(avatarSeed).toLowerCase().replace(/\s+/g, '-')
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${avatarSeed}`
}
