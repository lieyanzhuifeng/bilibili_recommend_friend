package com.bilibili.rec_system.service;

import com.bilibili.rec_system.entity.Message;
import com.bilibili.rec_system.repository.MessageRepository;
import com.bilibili.rec_system.service.MessageService;
import com.bilibili.rec_system.service.network.NetworkMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private NetworkMessageService networkMessageService;

    @Override
    public boolean sendMessage(Long senderId, Long receiverId, String content) {
        try {
            // 参数验证
            if (senderId == null || receiverId == null || content == null || content.trim().isEmpty()) {
                System.err.println("发送消息参数无效: senderId=" + senderId + ", receiverId=" + receiverId);
                return false;
            }

            // 创建消息对象
            Message message = new Message();
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setContent(content.trim());
            message.setSendTime(LocalDateTime.now());

            // 获取接收方网络地址和端口
            String receiverAddress = getReceiverAddress(receiverId);
            int receiverPort = getReceiverPort(receiverId);
            
            // 尝试通过网络包直接发送给接收方
            boolean sentOverNetwork = false;
            if (receiverAddress != null && receiverPort > 0) {
                try {
                    sentOverNetwork = networkMessageService.sendMessageOverNetwork(message, receiverAddress, receiverPort);
                    if (sentOverNetwork) {
                        System.out.println("消息已成功通过网络发送给接收方: " + receiverId);
                    } else {
                        System.err.println("通过网络发送消息失败，将仅保存到数据库");
                    }
                } catch (Exception networkException) {
                    System.err.println("网络发送过程中发生异常: " + networkException.getMessage());
                    networkException.printStackTrace();
                    // 继续执行，将消息保存到数据库
                }
            } else {
                System.err.println("无法获取接收方网络地址或端口，将仅保存到数据库");
            }

            // 将消息保存到数据库用于事后验证
            // 这样即使网络发送失败，消息也不会丢失
            try {
                messageRepository.save(message);
                System.out.println("消息已保存到数据库，用于事后验证");
            } catch (Exception dbException) {
                System.err.println("保存消息到数据库失败: " + dbException.getMessage());
                dbException.printStackTrace();
                // 如果网络发送也失败了，且数据库保存也失败了，则整个发送过程失败
                return sentOverNetwork; // 如果网络发送成功则返回true，否则返回false
            }
            
            // 返回网络发送的结果
            // 在实际应用中，您可能想要不同的逻辑：
            // 1. 只要数据库保存成功就返回true（保证消息不丢失）
            // 2. 网络发送和数据库保存都成功才返回true（保证即时性和持久性）
            // 3. 网络发送成功就返回true（优先考虑即时性）
            return sentOverNetwork;

        } catch (Exception e) {
            System.err.println("发送消息过程中发生未预期的异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Message> getRecentChat(Long user1, Long user2, int limit) {
        try {
            if (user1 == null || user2 == null || limit <= 0) {
                return Collections.emptyList();
            }

            List<Message> messages = messageRepository.findRecentChatBetweenUsers(user1, user2, limit);
            Collections.reverse(messages);
            return messages;

        } catch (Exception e) {
            System.err.println("获取最近聊天记录失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getNewMessages(Long userId, Long lastMessageId) {
        try {
            if (userId == null || lastMessageId == null) {
                return Collections.emptyList();
            }

            return messageRepository.findNewMessages(userId, lastMessageId);

        } catch (Exception e) {
            System.err.println("获取增量消息失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getNewMessages(Long userId, LocalDateTime lastTime) {
        try {
            if (userId == null || lastTime == null) {
                return Collections.emptyList();
            }

            return messageRepository.findNewMessagesByTime(userId, lastTime);

        } catch (Exception e) {
            System.err.println("获取增量消息失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getFullChatHistory(Long user1, Long user2) {
        try {
            if (user1 == null || user2 == null) {
                return Collections.emptyList();
            }

            return messageRepository.findChatBetweenUsers(user1, user2);

        } catch (Exception e) {
            System.err.println("获取完整聊天记录失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Long getUnreadMessageCount(Long userId, Long lastMessageId) {
        try {
            if (userId == null || lastMessageId == null) {
                return 0L;
            }

            return messageRepository.countUnreadMessages(userId, lastMessageId);

        } catch (Exception e) {
            System.err.println("获取未读消息数量失败: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public boolean deleteMessage(Long messageId) {
        try {
            if (messageId == null) {
                return false;
            }

            int deletedCount = messageRepository.deleteByMessageId(messageId);
            return deletedCount > 0;

        } catch (Exception e) {
            System.err.println("删除消息失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int deleteMessagesBySender(Long senderId) {
        try {
            if (senderId == null) {
                return 0;
            }

            return messageRepository.deleteBySenderId(senderId);

        } catch (Exception e) {
            System.err.println("删除发送者消息失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int deleteMessagesByReceiver(Long receiverId) {
        try {
            if (receiverId == null) {
                return 0;
            }

            return messageRepository.deleteByReceiverId(receiverId);

        } catch (Exception e) {
            System.err.println("删除接收者消息失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public int deleteChatHistory(Long user1, Long user2) {
        try {
            if (user1 == null || user2 == null) {
                return 0;
            }

            return messageRepository.deleteChatHistory(user1, user2);

        } catch (Exception e) {
            System.err.println("删除聊天记录失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Message> getMessagesBySender(Long senderId) {
        try {
            if (senderId == null) {
                return Collections.emptyList();
            }

            return messageRepository.findBySenderId(senderId);

        } catch (Exception e) {
            System.err.println("获取发送者消息失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getMessagesByReceiver(Long receiverId) {
        try {
            if (receiverId == null) {
                return Collections.emptyList();
            }

            return messageRepository.findByReceiverId(receiverId);

        } catch (Exception e) {
            System.err.println("获取接收者消息失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> getMessagesBetweenUsers(Long user1, Long user2, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            if (user1 == null || user2 == null || startTime == null || endTime == null) {
                return Collections.emptyList();
            }

            return messageRepository.findMessagesBetweenUsersInTimeRange(user1, user2, startTime, endTime);

        } catch (Exception e) {
            System.err.println("获取时间段聊天记录失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Long getMessageCountBySender(Long senderId) {
        try {
            if (senderId == null) {
                return 0L;
            }

            return messageRepository.countBySenderId(senderId);

        } catch (Exception e) {
            System.err.println("统计发送者消息数量失败: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getMessageCountByReceiver(Long receiverId) {
        try {
            if (receiverId == null) {
                return 0L;
            }

            return messageRepository.countByReceiverId(receiverId);

        } catch (Exception e) {
            System.err.println("统计接收者消息数量失败: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getTotalMessageCount() {
        try {
            return messageRepository.countTotalMessages();

        } catch (Exception e) {
            System.err.println("统计总消息数量失败: " + e.getMessage());
            return 0L;
        }
    }

    @Override
    public List<Message> searchMessagesByContent(Long userId, String keyword) {
        try {
            if (userId == null || keyword == null || keyword.trim().isEmpty()) {
                return Collections.emptyList();
            }

            return messageRepository.searchByContentAndUser(userId, "%" + keyword.trim() + "%");

        } catch (Exception e) {
            System.err.println("搜索用户消息失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Message> searchMessagesInChat(Long user1, Long user2, String keyword) {
        try {
            if (user1 == null || user2 == null || keyword == null || keyword.trim().isEmpty()) {
                return Collections.emptyList();
            }

            return messageRepository.searchByContentInChat(user1, user2, "%" + keyword.trim() + "%");

        } catch (Exception e) {
            System.err.println("搜索聊天记录失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean markMessageAsRead(Long messageId) {
        try {
            if (messageId == null) {
                return false;
            }

            // 在实际项目中，这里可能需要更新消息的已读状态
            // 由于当前表结构没有已读字段，这里返回true表示操作成功
            return true;

        } catch (Exception e) {
            System.err.println("标记消息为已读失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int markAllMessagesAsRead(Long userId, Long lastMessageId) {
        try {
            if (userId == null || lastMessageId == null) {
                return 0;
            }

            // 在实际项目中，这里可能需要批量更新消息的已读状态
            // 由于当前表结构没有已读字段，这里返回未读消息数量
            return getUnreadMessageCount(userId, lastMessageId).intValue();

        } catch (Exception e) {
            System.err.println("标记所有消息为已读失败: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Long> getChatPartners(Long userId) {
        try {
            if (userId == null) {
                return Collections.emptyList();
            }

            return messageRepository.findChatPartnersByUserId(userId);

        } catch (Exception e) {
            System.err.println("获取聊天伙伴列表失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Object[]> getRecentChatsWithPreview(Long userId, int limit) {
        try {
            if (userId == null || limit <= 0) {
                return Collections.emptyList();
            }

            return messageRepository.findRecentChatsWithPreview(userId, limit);

        } catch (Exception e) {
            System.err.println("获取最近聊天列表失败: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 根据接收方ID获取接收方的网络地址
     * 在实际应用中，这可能需要查询用户表或其他方式获取
     * @param receiverId 接收方ID
     * @return 接收方网络地址
     */
    private String getReceiverAddress(Long receiverId) {
        // 这里应该实现根据receiverId获取接收方网络地址的逻辑
        // 暂时返回一个默认值用于演示
        // 在实际应用中，您可能需要查询数据库或其他服务来获取这个信息
        return "127.0.0.1"; // 默认本地地址
    }
    
    /**
     * 根据接收方ID获取接收方的监听端口
     * 在实际应用中，这可能需要查询用户表或其他方式获取
     * @param receiverId 接收方ID
     * @return 接收方监听端口
     */
    private int getReceiverPort(Long receiverId) {
        // 这里应该实现根据receiverId获取接收方端口的逻辑
        // 暂时返回一个默认值用于演示
        // 在实际应用中，您可能需要查询数据库或其他服务来获取这个信息
        return 9000; // 默认端口
    }
}