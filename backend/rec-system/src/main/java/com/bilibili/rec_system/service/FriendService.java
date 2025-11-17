package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.User;
import java.util.List;

public interface FriendService {

    // 1. 发送好友申请
    boolean sendFriendRequest(Long userId, Long targetUserId);

    // 2. 接受好友申请
    boolean acceptFriendRequest(Long userId, Long requesterId);

    // 3. 拒绝好友申请
    boolean rejectFriendRequest(Long userId, Long requesterId);

    // 4. 删除好友
    boolean removeFriend(Long userId, Long targetUserId);

    // 5. 检查是否为好友
    boolean isFriend(Long userId, Long targetUserId);

    // 6. 获取好友列表
    List<User> getFriends(Long userId);

    // 7. 获取待处理的好友申请列表
    List<User> getPendingFriendRequests(Long userId);
}