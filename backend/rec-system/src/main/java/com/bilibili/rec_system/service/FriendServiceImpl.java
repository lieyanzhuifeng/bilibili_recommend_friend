package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.Friend;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.FriendRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public boolean sendFriendRequest(Long userId, Long targetUserId) {
        // 检查是否已经是好友或已有申请
        if (isFriend(userId, targetUserId)) {
            return false; // 已经是好友
        }

        // 检查是否已发送过申请
        Optional<Friend> existingRequest = friendRepository.findByUserAndFriend(userId, targetUserId);
        if (existingRequest.isPresent()) {
            return false; // 已发送过申请
        }

        // 创建新的好友申请
        Friend friendRequest = new Friend();
        friendRequest.setUserId(userId);
        friendRequest.setFriendId(targetUserId);
        friendRequest.setStatus("pending");
        friendRequest.setCreateTime(LocalDateTime.now());

        friendRepository.save(friendRequest);
        return true;
    }

    @Override
    @Transactional
    public boolean acceptFriendRequest(Long userId, Long requesterId) {
        // 查找待处理的申请
        Optional<Friend> pendingRequest = friendRepository.findPendingRequest(requesterId, userId);
        if (pendingRequest.isEmpty()) {
            return false; // 未找到待处理申请
        }

        // 更新原申请状态为 friend
        Friend request = pendingRequest.get();
        request.setStatus("accepted");
        friendRepository.save(request);

        // 创建反向好友关系
        Friend reverseFriend = new Friend();
        reverseFriend.setUserId(userId);
        reverseFriend.setFriendId(requesterId);
        reverseFriend.setStatus("accepted");
        reverseFriend.setCreateTime(LocalDateTime.now());

        friendRepository.save(reverseFriend);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectFriendRequest(Long userId, Long requesterId) {
        // 查找待处理的申请
        Optional<Friend> pendingRequest = friendRepository.findPendingRequest(requesterId, userId);
        if (pendingRequest.isEmpty()) {
            return false; // 未找到待处理申请
        }

        // 删除申请记录
        friendRepository.delete(pendingRequest.get());
        return true;
    }

    @Override
    @Transactional
    public boolean removeFriend(Long userId, Long targetUserId) {
        // 检查是否真的是好友
        if (!isFriend(userId, targetUserId)) {
            return false; // 原本不是好友
        }

        // 删除双向好友关系
        friendRepository.deleteFriendRelationship(userId, targetUserId);
        return true;
    }

    @Override
    public boolean isFriend(Long userId, Long targetUserId) {
        return friendRepository.isFriend(userId, targetUserId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        // 获取所有好友关系记录
        List<Friend> friendRelations = friendRepository.findAllFriends(userId);

        // 提取好友ID列表
        List<Long> friendIds = friendRelations.stream()
                .map(friend -> {
                    if (friend.getUserId().equals(userId)) {
                        return friend.getFriendId(); // 用户是发起方，好友是friendId
                    } else {
                        return friend.getUserId(); // 用户是被添加方，好友是userId
                    }
                })
                .distinct()
                .collect(Collectors.toList());

        // 查询好友的用户信息
        return userRepository.findByUserIds(friendIds);
    }

    @Override
    public List<User> getPendingFriendRequests(Long userId) {
        // 获取所有待处理申请
        List<Friend> pendingRequests = friendRepository.findPendingRequestsToUser(userId);

        // 提取申请者ID列表
        List<Long> requesterIds = pendingRequests.stream()
                .map(Friend::getUserId)
                .collect(Collectors.toList());

        // 查询申请者的用户信息
        return userRepository.findByUserIds(requesterIds);
    }
}