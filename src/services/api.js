// src/services/api.js
const API_BASE_URL = '/api/messages';

// 发送消息
export const sendMessage = async (senderId, receiverId, content) => {
  try {
    const response = await fetch(`${API_BASE_URL}/send`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        senderId,
        receiverId,
        content
      })
    });
    
    if (response.ok) {
      return { success: true };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('发送消息失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取最近聊天记录
export const getRecentChat = async (user1, user2, limit = 50) => {
  try {
    const response = await fetch(`${API_BASE_URL}/recent-chat?user1=${user1}&user2=${user2}&limit=${limit}`);
    
    if (response.ok) {
      const messages = await response.json();
      return { success: true, data: messages };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取聊天记录失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取增量消息（基于消息ID）
export const getNewMessagesById = async (userId, lastMessageId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/new-messages/id?userId=${userId}&lastMessageId=${lastMessageId}`);
    
    if (response.ok) {
      const messages = await response.json();
      return { success: true, data: messages };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取新消息失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取完整聊天记录
export const getFullChatHistory = async (user1, user2) => {
  try {
    const response = await fetch(`${API_BASE_URL}/full-chat?user1=${user1}&user2=${user2}`);
    
    if (response.ok) {
      const messages = await response.json();
      return { success: true, data: messages };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取完整聊天记录失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取未读消息数量
export const getUnreadMessageCount = async (userId, lastMessageId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/unread-count?userId=${userId}&lastMessageId=${lastMessageId}`);
    
    if (response.ok) {
      const count = await response.json();
      return { success: true, data: count };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取未读消息数量失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取用户的聊天伙伴列表
export const getChatPartners = async (userId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/chat-partners/${userId}`);
    
    if (response.ok) {
      const partners = await response.json();
      return { success: true, data: partners };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取聊天伙伴列表失败:', e);
    return { success: false, error: e.message };
  }
};

// 搜索聊天记录内容
export const searchMessagesInChat = async (user1, user2, keyword) => {
  try {
    const response = await fetch(`${API_BASE_URL}/search/chat?user1=${user1}&user2=${user2}&keyword=${encodeURIComponent(keyword)}`);
    
    if (response.ok) {
      const messages = await response.json();
      return { success: true, data: messages };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('搜索聊天记录失败:', e);
    return { success: false, error: e.message };
  }
};

// 好友相关 API
const FRIEND_API_BASE_URL = '/api/friends';

// 搜索用户
export const searchUsers = async (keyword) => {
  try {
    const response = await fetch(`${FRIEND_API_BASE_URL}/search?keyword=${encodeURIComponent(keyword)}`);
    
    if (response.ok) {
      const users = await response.json();
      return { success: true, data: users };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('搜索用户失败:', e);
    return { success: false, error: e.message };
  }
};

// 发送好友申请
export const sendFriendRequest = async (userId, targetUserId) => {
  try {
    const response = await fetch(`${FRIEND_API_BASE_URL}/request?userId=${userId}&targetUserId=${targetUserId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (response.ok) {
      const result = await response.text();
      return { success: true, data: result };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('发送好友申请失败:', e);
    return { success: false, error: e.message };
  }
};

// 接受好友申请
export const acceptFriendRequest = async (userId, requesterId) => {
  try {
    const response = await fetch(`${FRIEND_API_BASE_URL}/accept?userId=${userId}&requesterId=${requesterId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (response.ok) {
      const result = await response.text();
      return { success: true, data: result };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('接受好友申请失败:', e);
    return { success: false, error: e.message };
  }
};

// 拒绝好友申请
export const rejectFriendRequest = async (userId, requesterId) => {
  try {
    const response = await fetch(`${FRIEND_API_BASE_URL}/reject?userId=${userId}&requesterId=${requesterId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (response.ok) {
      const result = await response.text();
      return { success: true, data: result };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('拒绝好友申请失败:', e);
    return { success: false, error: e.message };
  }
};

// 获取好友列表
export const getFriendsList = async (userId) => {
  try {
    const response = await fetch(`${FRIEND_API_BASE_URL}/list/${userId}`);
    
    if (response.ok) {
      const friends = await response.json();
      return { success: true, data: friends };
    } else {
      const errorText = await response.text();
      throw new Error(errorText);
    }
  } catch (e) {
    console.error('获取好友列表失败:', e);
    return { success: false, error: e.message };
  }
};