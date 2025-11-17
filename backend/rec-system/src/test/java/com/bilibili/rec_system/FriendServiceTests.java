package com.bilibili.rec_system;

import com.bilibili.rec_system.entity.Friend;
import com.bilibili.rec_system.entity.User;
import com.bilibili.rec_system.repository.FriendRepository;
import com.bilibili.rec_system.repository.UserRepository;
import com.bilibili.rec_system.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback
public class FriendServiceTests {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    private Long user1Id = 1L;
    private Long user2Id = 2L;
    private Long user3Id = 3L;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        // 从数据库获取已有的用户
        user1 = userRepository.findById(user1Id).get();
        user2 = userRepository.findById(user2Id).get();
        user3 = userRepository.findById(user3Id).get();

        System.out.println("使用的测试用户:");
        System.out.println("用户1: " + user1.getUsername() + " (ID: " + user1.getUserId() + ")");
        System.out.println("用户2: " + user2.getUsername() + " (ID: " + user2.getUserId() + ")");
        System.out.println("用户3: " + user3.getUsername() + " (ID: " + user3.getUserId() + ")");
    }

    // 打印完整Friend表的工具方法
    private void printFriendTable() {
        System.out.println("=== 完整Friend表数据 ===");
        System.out.println("userID | friendID | status   | createTime");
        System.out.println("-------|----------|----------|------------");

        List<Friend> allFriends = friendRepository.findAll();
        for (Friend friend : allFriends) {
            System.out.printf("%-6d | %-8d | %-8s | %s%n",
                    friend.getUserId(),
                    friend.getFriendId(),
                    friend.getStatus(),
                    friend.getCreateTime());
        }

        // 统计信息
        long totalCount = friendRepository.count();
        long friendCount = allFriends.stream().filter(f -> "friend".equals(f.getStatus())).count();
        long pendingCount = allFriends.stream().filter(f -> "pending".equals(f.getStatus())).count();

        System.out.println("统计信息:");
        System.out.println("总记录数: " + totalCount);
        System.out.println("好友关系数: " + friendCount);
        System.out.println("待处理申请数: " + pendingCount);
        System.out.println("=== Friend表数据结束 ===\n");
    }

    @Test
    void testCompleteFriendWorkflow() {
        System.out.println("=== 开始完整的好友流程测试 ===");
        System.out.println("=== 测试开始前的Friend表 ===");
        printFriendTable();

        // 1. 测试发送好友申请
        System.out.println("1. 测试发送好友申请");
        boolean sendResult1 = friendService.sendFriendRequest(user1.getUserId(), user2.getUserId());
        assertTrue(sendResult1, "第一次发送好友申请应该成功");
        printFriendTable();

        // 2. 测试重复发送申请
        System.out.println("2. 测试重复发送申请");
        boolean sendResult2 = friendService.sendFriendRequest(user1.getUserId(), user2.getUserId());
        assertFalse(sendResult2, "重复发送好友申请应该失败");

        // 3. 测试检查好友状态（还不是好友）
        System.out.println("3. 测试检查好友状态（申请中）");
        boolean isFriend1 = friendService.isFriend(user1.getUserId(), user2.getUserId());
        assertFalse(isFriend1, "申请中应该不是好友关系");

        // 4. 测试获取待处理申请
        System.out.println("4. 测试获取待处理申请");
        List<User> pendingRequests = friendService.getPendingFriendRequests(user2.getUserId());
        assertEquals(1, pendingRequests.size(), "user2应该有一个待处理申请");
        assertEquals(user1.getUserId(), pendingRequests.get(0).getUserId(), "申请者应该是user1");

        // 5. 测试接受好友申请
        System.out.println("5. 测试接受好友申请");
        boolean acceptResult = friendService.acceptFriendRequest(user2.getUserId(), user1.getUserId());
        assertTrue(acceptResult, "接受好友申请应该成功");
        printFriendTable();

        // 6. 测试检查好友状态（已成为好友）
        System.out.println("6. 测试检查好友状态（已成为好友）");
        boolean isFriend2 = friendService.isFriend(user1.getUserId(), user2.getUserId());
        assertTrue(isFriend2, "接受申请后应该是好友关系");

        // 7. 测试获取好友列表
        System.out.println("7. 测试获取好友列表");
        List<User> friends1 = friendService.getFriends(user1.getUserId());
        List<User> friends2 = friendService.getFriends(user2.getUserId());
        assertEquals(1, friends1.size(), "user1应该有一个好友");
        assertEquals(1, friends2.size(), "user2应该有一个好友");
        assertEquals(user2.getUserId(), friends1.get(0).getUserId(), "user1的好友应该是user2");
        assertEquals(user1.getUserId(), friends2.get(0).getUserId(), "user2的好友应该是user1");

        // 8. 测试删除好友
        System.out.println("8. 测试删除好友");
        boolean removeResult = friendService.removeFriend(user1.getUserId(), user2.getUserId());
        assertTrue(removeResult, "删除好友应该成功");
        printFriendTable();

        // 9. 测试删除后检查好友状态
        System.out.println("9. 测试删除后检查好友状态");
        boolean isFriend3 = friendService.isFriend(user1.getUserId(), user2.getUserId());
        assertFalse(isFriend3, "删除后应该不是好友关系");

        // 10. 测试删除后好友列表
        System.out.println("10. 测试删除后好友列表");
        List<User> friendsAfterRemove = friendService.getFriends(user1.getUserId());
        assertTrue(friendsAfterRemove.isEmpty(), "删除后好友列表应该为空");

        System.out.println("=== 完整的好友流程测试完成 ===");
        System.out.println("=== 测试结束后的Friend表 ===");
        printFriendTable();
    }

    @Test
    void testRejectFriendRequest() {
        System.out.println("=== 测试拒绝好友申请 ===");
        System.out.println("=== 测试开始前的Friend表 ===");
        printFriendTable();

        // 1. 发送申请
        friendService.sendFriendRequest(user1.getUserId(), user3.getUserId());
        System.out.println("发送申请后的Friend表:");
        printFriendTable();

        // 2. 拒绝申请
        boolean rejectResult = friendService.rejectFriendRequest(user3.getUserId(), user1.getUserId());
        assertTrue(rejectResult, "拒绝好友申请应该成功");
        System.out.println("拒绝申请后的Friend表:");
        printFriendTable();

        // 3. 验证申请被删除
        List<User> pendingRequests = friendService.getPendingFriendRequests(user3.getUserId());
        assertTrue(pendingRequests.isEmpty(), "拒绝后待处理申请应该为空");

        // 4. 验证不是好友
        boolean isFriend = friendService.isFriend(user1.getUserId(), user3.getUserId());
        assertFalse(isFriend, "拒绝申请后应该不是好友");

        System.out.println("=== 拒绝好友申请测试完成 ===");
    }

    @Test
    void testAcceptNonExistentRequest() {
        System.out.println("=== 测试接受不存在的申请 ===");
        printFriendTable();

        // 尝试接受不存在的申请
        boolean acceptResult = friendService.acceptFriendRequest(user2.getUserId(), user1.getUserId());
        assertFalse(acceptResult, "接受不存在的申请应该失败");

        System.out.println("=== 接受不存在申请测试完成 ===");
    }

    @Test
    void testRejectNonExistentRequest() {
        System.out.println("=== 测试拒绝不存在的申请 ===");
        printFriendTable();

        // 尝试拒绝不存在的申请
        boolean rejectResult = friendService.rejectFriendRequest(user2.getUserId(), user1.getUserId());
        assertFalse(rejectResult, "拒绝不存在的申请应该失败");

        System.out.println("=== 拒绝不存在申请测试完成 ===");
    }

    @Test
    void testRemoveNonFriend() {
        System.out.println("=== 测试删除非好友关系 ===");
        printFriendTable();

        // 尝试删除不存在的好友关系
        boolean removeResult = friendService.removeFriend(user1.getUserId(), user2.getUserId());
        assertFalse(removeResult, "删除非好友关系应该失败");

        System.out.println("=== 删除非好友关系测试完成 ===");
    }

    @Test
    void testMultipleFriends() {
        System.out.println("=== 测试多个好友关系 ===");
        System.out.println("=== 测试开始前的Friend表 ===");
        printFriendTable();

        // user1 添加 user2 和 user3 为好友
        friendService.sendFriendRequest(user1.getUserId(), user2.getUserId());
        friendService.acceptFriendRequest(user2.getUserId(), user1.getUserId());

        friendService.sendFriendRequest(user1.getUserId(), user3.getUserId());
        friendService.acceptFriendRequest(user3.getUserId(), user1.getUserId());

        System.out.println("建立多个好友关系后的Friend表:");
        printFriendTable();

        // 验证user1有2个好友
        List<User> friends = friendService.getFriends(user1.getUserId());
        assertEquals(2, friends.size(), "user1应该有2个好友");

        // 验证好友列表包含user2和user3
        List<Long> friendIds = friends.stream().map(User::getUserId).toList();
        assertTrue(friendIds.contains(user2.getUserId()), "好友列表应该包含user2");
        assertTrue(friendIds.contains(user3.getUserId()), "好友列表应该包含user3");

        System.out.println("=== 多个好友关系测试完成 ===");
    }

    @Test
    void testFriendStatusConsistency() {
        System.out.println("=== 测试好友状态一致性 ===");
        printFriendTable();

        // 建立好友关系
        friendService.sendFriendRequest(user1.getUserId(), user2.getUserId());
        friendService.acceptFriendRequest(user2.getUserId(), user1.getUserId());

        System.out.println("建立好友关系后的Friend表:");
        printFriendTable();

        // 验证双向好友状态
        boolean status1 = friendService.isFriend(user1.getUserId(), user2.getUserId());
        boolean status2 = friendService.isFriend(user2.getUserId(), user1.getUserId());

        assertTrue(status1, "user1->user2应该是好友");
        assertTrue(status2, "user2->user1应该是好友");

        System.out.println("=== 好友状态一致性测试完成 ===");
    }
}