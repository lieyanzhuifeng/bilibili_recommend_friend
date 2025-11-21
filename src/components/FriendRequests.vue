<template>
  <div v-if="pendingRequests.length > 0" class="friend-requests-container">
    <h3>好友申请</h3>
    <div v-for="request in pendingRequests" :key="request.id" class="request-item">
      <img :src="request.avatar" :alt="request.username" class="avatar" />
      <div class="request-info">
        <div class="username">{{ request.username }}</div>
        <div class="request-message">请求添加您为好友</div>
      </div>
      <div class="request-actions">
        <button @click="acceptRequest(request.id)" class="accept-btn">接受</button>
        <button @click="rejectRequest(request.id)" class="reject-btn">拒绝</button>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue';
import { useChatStore } from '@/stores/chat';

export default defineComponent({
  name: 'FriendRequests',
  setup() {
    const chatStore = useChatStore();

    // 计算属性：获取待处理的好友申请
    const pendingRequests = computed(() => {
      return chatStore.friends.filter(friend => friend.status === 'pending');
    });

    // 接受好友申请
    const acceptRequest = async (requesterId) => {
      const result = await chatStore.acceptFriendRequest(requesterId);
      if (!result.success) {
        console.error('接受好友申请失败:', result.message);
      }
    };

    // 拒绝好友申请
    const rejectRequest = async (requesterId) => {
      const result = await chatStore.rejectFriendRequest(requesterId);
      if (!result.success) {
        console.error('拒绝好友申请失败:', result.message);
      }
    };

    return {
      pendingRequests,
      acceptRequest,
      rejectRequest
    };
  }
});
</script>

<style scoped>
.friend-requests-container {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.friend-requests-container h3 {
  margin-top: 0;
  color: #333;
  font-size: 18px;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.request-item:last-child {
  border-bottom: none;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 15px;
}

.request-info {
  flex: 1;
}

.username {
  font-weight: bold;
  color: #333;
}

.request-message {
  font-size: 14px;
  color: #666;
}

.request-actions {
  display: flex;
  gap: 10px;
}

.accept-btn, .reject-btn {
  padding: 5px 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.accept-btn {
  background-color: #4CAF50;
  color: white;
}

.reject-btn {
  background-color: #f44336;
  color: white;
}
</style>
