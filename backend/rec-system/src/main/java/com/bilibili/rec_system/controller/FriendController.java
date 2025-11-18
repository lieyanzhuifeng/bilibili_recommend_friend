package com.bilibili.rec_system.controller;

import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 发送好友申请
     */
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestParam Long userId,
                                               @RequestParam Long targetUserId) {
        try {
            boolean success = friendService.sendFriendRequest(userId, targetUserId);
            if (success) {
                return ResponseEntity.ok().body("好友申请发送成功");
            } else {
                return ResponseEntity.badRequest().body("好友申请发送失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发送好友申请时发生错误: " + e.getMessage());
        }
    }

    /**
     * 接受好友申请
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@RequestParam Long userId,
                                                 @RequestParam Long requesterId) {
        try {
            boolean success = friendService.acceptFriendRequest(userId, requesterId);
            if (success) {
                return ResponseEntity.ok().body("好友申请已接受");
            } else {
                return ResponseEntity.badRequest().body("接受好友申请失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("接受好友申请时发生错误: " + e.getMessage());
        }
    }

    /**
     * 拒绝好友申请
     */
    @PostMapping("/reject")
    public ResponseEntity<?> rejectFriendRequest(@RequestParam Long userId,
                                                 @RequestParam Long requesterId) {
        try {
            boolean success = friendService.rejectFriendRequest(userId, requesterId);
            if (success) {
                return ResponseEntity.ok().body("好友申请已拒绝");
            } else {
                return ResponseEntity.badRequest().body("拒绝好友申请失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("拒绝好友申请时发生错误: " + e.getMessage());
        }
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFriend(@RequestParam Long userId,
                                          @RequestParam Long targetUserId) {
        try {
            boolean success = friendService.removeFriend(userId, targetUserId);
            if (success) {
                return ResponseEntity.ok().body("好友删除成功");
            } else {
                return ResponseEntity.badRequest().body("好友删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除好友时发生错误: " + e.getMessage());
        }
    }

    /**
     * 检查是否为好友
     */
    @GetMapping("/check")
    public ResponseEntity<?> isFriend(@RequestParam Long userId,
                                      @RequestParam Long targetUserId) {
        try {
            boolean isFriend = friendService.isFriend(userId, targetUserId);
            return ResponseEntity.ok().body(isFriend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("检查好友关系时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getFriends(@PathVariable Long userId) {
        try {
            List<User> friends = friendService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取好友列表时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取待处理的好友申请列表
     */
    @GetMapping("/requests/{userId}")
    public ResponseEntity<?> getPendingFriendRequests(@PathVariable Long userId) {
        try {
            List<User> pendingRequests = friendService.getPendingFriendRequests(userId);
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取好友申请列表时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取好友数量
     */
    @GetMapping("/count/{userId}")
    public ResponseEntity<?> getFriendCount(@PathVariable Long userId) {
        try {
            List<User> friends = friendService.getFriends(userId);
            return ResponseEntity.ok(friends.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取好友数量时发生错误: " + e.getMessage());
        }
    }

    /**
     * 获取待处理申请数量
     */
    @GetMapping("/requests/count/{userId}")
    public ResponseEntity<?> getPendingRequestCount(@PathVariable Long userId) {
        try {
            List<User> pendingRequests = friendService.getPendingFriendRequests(userId);
            return ResponseEntity.ok(pendingRequests.size());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取待处理申请数量时发生错误: " + e.getMessage());
        }
    }
}