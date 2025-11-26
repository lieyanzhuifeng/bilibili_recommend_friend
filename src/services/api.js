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
  getUserInfo: (userId) => get(`/api/users/${userId}`)
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
  // 新增方法，用于组件中的搜索用户
  searchUsers: (query) => get('/api/users/search', { query }),
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
  // 适配组件调用的发送消息方法 - 使用URL查询参数
  sendMessage: async (data) => {
    const { receiverId, content } = data;
    const senderId = localStorage.getItem('userId') || 'current';
    // 使用URL查询参数传递senderId、receiverId和content
    const response = await post(`/api/messages/send?senderId=${senderId}&receiverId=${receiverId}&content=${encodeURIComponent(content)}`);
    // 返回格式适配前端组件
    return { message: response.message || response };
  },
  // 获取会话列表（适配组件调用）
  getConversations: async () => {
    const userId = localStorage.getItem('userId') || 'current';
    const chatPartners = await get(`/api/messages/chat-partners/${userId}`);
    // 转换为组件需要的格式
    const conversations = (chatPartners.partners || []).map(partner => ({
      id: partner.id,
      name: partner.name || partner.username,
      avatar: partner.avatar,
      isOnline: partner.status === 'online',
      lastMessage: partner.lastMessage?.content || '',
      lastMessageTime: partner.lastMessage?.timestamp || new Date().toISOString(),
      unreadCount: partner.unreadCount || 0
    }));
    return { conversations };
  },
  // 获取聊天历史（适配组件调用）
  getChatHistory: async (chatId) => {
    const userId = localStorage.getItem('userId') || 'current';
    const messages = await get('/api/messages/full-chat', { user1: userId, user2: chatId });
    // 转换为组件需要的格式
    const formattedMessages = (messages.messages || []).map(msg => ({
      id: msg.id,
      content: msg.content,
      isMine: msg.senderId === userId,
      timestamp: msg.timestamp
    }));
    return { messages: formattedMessages };
  },
  // 标记为已读（适配组件调用）
  markAsRead: async (chatId) => {
    const userId = localStorage.getItem('userId') || 'current';
    return request('/api/messages/mark-all-read', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId, lastMessageId: null })
    });
  },
  // 获取未读消息数量（适配组件调用）
  getUnreadCount: async () => {
    const userId = localStorage.getItem('userId') || 'current';
    const response = await get('/api/messages/unread-count', { userId });
    return response;
  },
  // 保留原有API方法供其他场景使用
  getRecentChat: (user1, user2, limit) => get('/api/messages/recent-chat', { user1, user2, limit }),
  getNewMessagesById: (userId, lastMessageId) => get('/api/messages/new-messages/id', { userId, lastMessageId }),
  getNewMessagesByTime: (userId, lastTime) => get('/api/messages/new-messages/time', { userId, lastTime }),
  getFullChat: (user1, user2) => get('/api/messages/full-chat', { user1, user2 }),
  deleteMessage: (messageId) => request(`/api/messages/${messageId}`, { method: 'DELETE' }),
  deleteSenderMessages: (senderId) => request(`/api/messages/sender/${senderId}`, { method: 'DELETE' }),
  deleteReceiverMessages: (receiverId) => request(`/api/messages/receiver/${receiverId}`, { method: 'DELETE' }),
  deleteChatHistory: (user1, user2) => request('/api/messages/chat-history', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ user1, user2 })
  }),
  getSenderMessages: (senderId) => get(`/api/messages/sender/${senderId}`),
  getReceiverMessages: (receiverId) => get(`/api/messages/receiver/${receiverId}`),
  getTimeRangeMessages: (user1, user2, startTime, endTime) => get('/api/messages/time-range', { user1, user2, startTime, endTime }),
  getSenderMessageCount: (senderId) => get(`/api/messages/count/sender/${senderId}`),
  getReceiverMessageCount: (receiverId) => get(`/api/messages/count/receiver/${receiverId}`),
  getTotalMessageCount: () => get('/api/messages/count/total'),
  searchUserMessages: (userId, keyword) => get('/api/messages/search/user', { userId, keyword }),
  searchChatMessages: (user1, user2, keyword) => get('/api/messages/search/chat', { user1, user2, keyword }),
  markMessageAsRead: (messageId) => request(`/api/messages/${messageId}/read`, { method: 'PUT' }),
  markAllAsRead: (userId, lastMessageId) => request('/api/messages/mark-all-read', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId, lastMessageId })
  }),
  getChatPartners: (userId) => get(`/api/messages/chat-partners/${userId}`)
}

export const recommendApi = {
  // 主要推荐API（按照API文档格式）
  commonUp: (userId) => get(`/api/recommend/common-up/${userId}`),
  sharedVideo: (userId) => get(`/api/recommend/shared-video/${userId}`),
  userBehavior: (userId) => get(`/api/recommend/user-behavior/${userId}`),
  coComment: (userId) => get(`/api/recommend/co-comment/${userId}`),
  theme: (userId) => get(`/api/recommend/theme/${userId}`),

  // 其他推荐API（与文档保持一致）
  reply: (userId) => get(`/api/recommend/reply/${userId}`),
  category: (userId) => get(`/api/recommend/category/${userId}`),
  favoriteSimilarity: (userId) => get(`/api/recommend/favorite-similarity/${userId}`),
  commentFriends: (userId) => get(`/api/recommend/comment-friends/${userId}`),
  getUserRecommend: (userId) => get(`/api/recommend/user/${userId}`),
  getCommonFriend: (userId) => get(`/api/recommend/common-friend/${userId}`),
  getCommonInterest: (userId) => get(`/api/recommend/common-interest/${userId}`)
}

export const filterApi = {
  // 主要筛选API（按照用户要求的5个）
  sameUp: (params = {}) => get('/api/filter/same-up', params),
  sameTag: (params = {}) => get('/api/filter/same-tag', params),
  sameUpVideoCount: (body) => post('/api/filter/same-up-video-count', body),
  sameTagVideoCount: (params = {}) => get('/api/filter/same-tag-video-count', params),
  deepVideo: (params = {}) => get('/api/filter/deep-video', params),

  // 其他筛选API
  followTime: ({ userId, upId } = {}) => get('/api/filter/follow-time', { userId, upId }),
  nightOwl: ({ option } = {}) => get('/api/filter/night-owl', { option }),
  userActivity: ({ option } = {}) => get('/api/filter/user-activity', { option }),
  series: ({ tagId } = {}) => get('/api/filter/series', { tagId })
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
  filterApi
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
