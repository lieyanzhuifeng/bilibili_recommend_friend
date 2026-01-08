<template>
  <div class="friend-management-container">
    <h1>好友管理</h1>

    <!-- 标签切换 -->
    <div class="tabs">
      <button
        :class="['tab-btn', { active: activeTab === 'requests' }]"
        @click="activeTab = 'requests'"
      >
        好友申请 <span v-if="requestCount > 0" class="badge">{{ requestCount }}</span>
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'friends' }]"
        @click="activeTab = 'friends'"
      >
        我的好友
      </button>
    </div>

    <!-- 好友申请列表 -->
    <div v-if="activeTab === 'requests'" class="requests-section">
      <div v-if="loadingRequests" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="error" class="error-message">{{ error }}</div>

      <div v-else-if="friendRequests.length === 0" class="empty-state">
        <p>暂无好友申请</p>
      </div>

      <div v-else class="request-list">
        <div
          v-for="request in friendRequests"
          :key="request.id"
          class="request-card"
        >
          <div class="request-header">
            <img
              :src="getUserAvatar(request.user.avatar, request.user.id)"
              alt="User Avatar"
              class="request-avatar"
            />
            <div class="request-info">
              <h4>{{ request.user.username }}</h4>
              <p>UID: {{ request.user.id }}</p>
              <p class="request-time">{{ formatDate(request.createdAt) }} 申请添加你为好友</p>
            </div>
          </div>
          <div class="request-actions">
            <button
              class="primary-btn"
              @click="acceptRequest(request.user.id)"
              :disabled="processingRequests.includes(request.user.id)"
            >
              {{ processingRequests.includes(request.user.id) ? '处理中...' : '接受' }}
            </button>
            <button
              class="secondary-btn"
              @click="rejectRequest(request.user.id)"
              :disabled="processingRequests.includes(request.user.id)"
            >
              拒绝
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 好友列表 -->
    <div v-if="activeTab === 'friends'" class="friends-section">
      <div class="friends-header">
        <h3>好友列表</h3>
        <p>共 {{ friends.length }} 位好友</p>
      </div>

      <div v-if="loadingFriends" class="loading-container">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else-if="error" class="error-message">{{ error }}</div>

      <div v-else-if="friends.length === 0" class="empty-state">
        <p>暂无好友</p>
        <router-link to="/friends" class="primary-btn small-btn">添加好友</router-link>
      </div>

      <div v-else class="friends-list">
        <div
          v-for="friend in friends"
          :key="friend.id"
          class="friend-card"
        >
          <div class="friend-avatar-container">
            <img
              :src="getUserAvatar(friend.avatar, friend.id)"
              alt="Friend Avatar"
              class="friend-avatar"
            />

          </div>
          <div class="friend-info">
            <h4>{{ friend.username }}</h4>
            <p>UID: {{ friend.id }}</p>

          </div>
          <div class="friend-actions">
            <button class="secondary-btn" @click="deleteFriend(friend.id)">
              删除好友
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { friendApi } from '../services/api';
import { getUserAvatar, generateRandomAvatar } from '../utils/avatar';

export default {
  name: 'FriendManagement',
  setup() {
    const activeTab = ref('requests');
    const friendRequests = ref([]);
    const friends = ref([]);
    const loadingRequests = ref(false);
    const loadingFriends = ref(false);
    const processingRequests = ref([]);
    const requestCount = ref(0);
    const error = ref('');

    // 获取当前用户ID
    const getCurrentUserId = () => {
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
      return userId;
    };

    // 获取好友申请列表
    const fetchFriendRequests = async () => {
      const userId = getCurrentUserId();
      loadingRequests.value = true;
      error.value = '';

      try {
        const requests = await friendApi.getFriendRequests(userId);
        // 打印API返回的数据结构以便调试
        console.log('API返回的好友申请数据:', requests);

        let apiData = [];
        // 灵活处理不同格式的返回数据
        if (requests && typeof requests === 'object' && Array.isArray(requests.requests)) {
          apiData = requests.requests;
        } else if (Array.isArray(requests)) {
          apiData = requests;
        }

        // 根据API返回的数据结构，将其转换为组件模板需要的格式
        friendRequests.value = apiData.map(userData => ({
          id: userData.userId,  // 使用userId作为请求的id
          user: {
            id: userData.userId,
            username: userData.username,
            avatar: userData.avatarPath || null
          },
          createdAt: userData.registerTime  // 使用registerTime作为创建时间
        }));

        requestCount.value = friendRequests.value.length;
      } catch (err) {
        console.error('获取好友申请失败:', err);
        error.value = '获取好友申请失败，请稍后重试';
      } finally {
        loadingRequests.value = false;
      }
    };

    // 获取好友列表
    const fetchFriends = async () => {
      const userId = getCurrentUserId();
      loadingFriends.value = true;
      error.value = '';

      try {
        const friendList = await friendApi.getFriendList(userId);
        // 打印API返回的数据结构以便调试
        console.log('API返回的好友列表数据:', friendList);

        let apiData = [];
        // 灵活处理不同格式的返回数据
        if (friendList && typeof friendList === 'object' && Array.isArray(friendList.friends)) {
          apiData = friendList.friends;
        } else if (Array.isArray(friendList)) {
          apiData = friendList;
        }

        // 根据API返回的数据结构，将其转换为组件模板需要的格式
        friends.value = apiData.map(userData => ({
          id: userData.userId,  // 使用userId作为好友的id
          username: userData.username,
          avatar: userData.avatarPath || null,
          isOnline: userData.isOnline || false,  // 默认设置为离线
          lastSeen: userData.lastSeen || userData.registerTime || null  // 使用registerTime作为备选
        }));
      } catch (err) {
        console.error('获取好友列表失败:', err);
        error.value = '获取好友列表失败，请稍后重试';
      } finally {
        loadingFriends.value = false;
      }
    };

    // 接受好友申请
    const acceptRequest = async (requesterId) => {
      // 防止重复点击
      if (processingRequests.value.includes(requesterId)) {
        return;
      }

      const userId = getCurrentUserId();
      processingRequests.value.push(requesterId);

      try {
        await friendApi.acceptRequest(userId, requesterId);
        // 移除已处理的请求
        friendRequests.value = friendRequests.value.filter(req => req.user.id !== requesterId);
        requestCount.value = friendRequests.value.length;
        // 更新好友列表
        await fetchFriends();
        alert('成功接受好友申请！');
      } catch (err) {
        console.error('接受好友申请失败:', err);
        // 处理重复添加的情况
        if (err.message && err.message.includes('Duplicate entry')) {
          alert('已经是好友关系，请刷新页面重试');
        } else {
          alert('接受好友申请失败，请稍后重试');
        }
      } finally {
        processingRequests.value = processingRequests.value.filter(id => id !== requesterId);
      }
    };

    // 拒绝好友申请
    const rejectRequest = async (requesterId) => {
      // 防止重复点击
      if (processingRequests.value.includes(requesterId)) {
        return;
      }

      const userId = getCurrentUserId();
      processingRequests.value.push(requesterId);

      try {
        await friendApi.rejectRequest(userId, requesterId);
        // 移除已处理的请求
        friendRequests.value = friendRequests.value.filter(req => req.user.id !== requesterId);
        requestCount.value = friendRequests.value.length;
        alert('已拒绝好友申请');
      } catch (err) {
        console.error('拒绝好友申请失败:', err);
        alert('拒绝好友申请失败，请稍后重试');
      } finally {
        processingRequests.value = processingRequests.value.filter(id => id !== requesterId);
      }
    };

    // 删除好友
    const deleteFriend = async (friendId) => {
      if (!confirm('确定要删除这位好友吗？')) {
        return;
      }

      const userId = getCurrentUserId();

      try {
        await friendApi.removeFriend(userId, friendId);
        // 更新好友列表
        friends.value = friends.value.filter(friend => friend.id !== friendId);
        alert('成功删除好友');
      } catch (err) {
        console.error('删除好友失败:', err);
        alert('删除好友失败，请稍后重试');
      }
    };

    // 格式化日期
    const formatDate = (dateString) => {
      const date = new Date(dateString);
      return date.toLocaleString();
    };

    // 格式化最后在线时间
    const formatLastSeen = (lastSeen) => {
      if (!lastSeen) return '未知';

      const now = new Date();
      const lastSeenDate = new Date(lastSeen);
      const diffMs = now - lastSeenDate;
      const diffMins = Math.floor(diffMs / 60000);
      const diffHours = Math.floor(diffMs / 3600000);
      const diffDays = Math.floor(diffMs / 86400000);

      if (diffMins < 1) return '刚刚';
      if (diffMins < 60) return `${diffMins}分钟前`;
      if (diffHours < 24) return `${diffHours}小时前`;
      if (diffDays < 7) return `${diffDays}天前`;

      return formatDate(lastSeen);
    };

    // 头像生成函数已从utils/avatar.js导入

    onMounted(() => {
      fetchFriendRequests();
      fetchFriends();
    });

    return {
      activeTab,
      friendRequests,
      friends,
      loadingRequests,
      loadingFriends,
      processingRequests,
      requestCount,
      error,
      acceptRequest,
      rejectRequest,
      deleteFriend,
      formatDate,
      formatLastSeen,
      getUserAvatar,
      generateRandomAvatar
    };
  }
};
</script>

<style scoped>
.friend-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.friend-management-container h1 {
  color: var(--text-primary);
  margin-bottom: 30px;
}

.tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  background-color: #f8f9fa;
  padding: 5px;
  border-radius: var(--border-radius);
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  background-color: rgba(251, 114, 153, 0.1);
  color: var(--primary-color);
}

.tab-btn.active {
  background-color: var(--primary-color);
  color: white;
}

.badge {
  background-color: var(--danger-color);
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  gap: 15px;
}

.loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid var(--border-color);
  border-top: 3px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  background-color: rgba(245, 34, 45, 0.1);
  color: var(--danger-color);
  padding: 15px;
  border-radius: var(--border-radius);
  margin-bottom: 20px;
  text-align: center;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  color: var(--text-secondary);
}

/* 申请列表样式 */
.request-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.request-card {
  background-color: white;
  border-radius: var(--border-radius);
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.request-header {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.request-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.request-info h4 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.request-info p {
  margin: 0 0 5px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.request-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.request-actions {
  display: flex;
  gap: 10px;
}

/* 好友列表样式 */
.friends-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.friends-header h3 {
  margin: 0;
  color: var(--text-primary);
}

.friends-header p {
  margin: 0;
  color: var(--text-secondary);
}

.friends-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.friend-card {
  background-color: white;
  border-radius: var(--border-radius);
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 20px;
  transition: all 0.3s;
}

.friend-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.friend-avatar-container {
  position: relative;
}

.friend-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
}

.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background-color: #ccc;
  border: 2px solid white;
}

.online-status.online {
  background-color: #52c41a;
}

.friend-info {
  flex: 1;
}

.friend-info h4 {
  margin: 0 0 5px 0;
  font-size: 18px;
  color: var(--text-primary);
}

.friend-info p {
  margin: 0 0 5px 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.friend-status {
  color: var(--text-secondary);
  font-size: 12px;
}

.friend-actions {
  display: flex;
  gap: 10px;
}

/* 按钮样式 */
.primary-btn {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.primary-btn:hover:not(:disabled) {
  background-color: #ff5277;
}

.primary-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.secondary-btn {
  background-color: white;
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 10px 20px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.secondary-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.small-btn {
  padding: 8px 16px;
  font-size: 14px;
  margin-top: 15px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .request-card {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .request-header {
    flex-direction: column;
  }

  .request-actions {
    justify-content: center;
  }

  .friend-card {
    flex-direction: column;
    text-align: center;
  }

  .friend-actions {
    justify-content: center;
  }
}
</style>
