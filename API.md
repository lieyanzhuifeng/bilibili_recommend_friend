# Bilibili 推荐交友系统 API 文档

本文档详细描述了 Bilibili 推荐交友系统后端提供的所有 API 接口，包括请求方法、路径、参数、响应格式等信息。

## 目录

- [1. 用户相关 API](#1-用户相关-api)
- [2. 好友相关 API](#2-好友相关-api)
- [3. 消息相关 API](#3-消息相关-api)
- [4. 推荐相关 API](#4-推荐相关-api)
- [5. 筛选相关 API](#5-筛选相关-api)

## 1. 用户相关 API

### 1.1 获取用户信息

**请求方法:** GET
**API 路径:** `/api/users/{userId}`
**功能描述:** 通过用户ID获取用户详细信息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "bio": "个人简介",
    "createTime": "2023-01-01T00:00:00"
  },
  "message": "获取用户信息成功"
}
```

## 2. 好友相关 API

### 2.1 发送好友申请

**请求方法:** POST
**API 路径:** `/api/friends/request`
**功能描述:** 发送好友申请给目标用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 发送者用户ID |
| `targetUserId` | Long | 是 | 接收者用户ID |

**响应数据:**
```json
"好友申请发送成功"
```

### 2.2 接受好友申请

**请求方法:** POST
**API 路径:** `/api/friends/accept`
**功能描述:** 接受来自指定用户的好友申请

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 接收者用户ID |
| `requesterId` | Long | 是 | 申请者用户ID |

**响应数据:**
```json
"好友申请已接受"
```

### 2.3 拒绝好友申请

**请求方法:** POST
**API 路径:** `/api/friends/reject`
**功能描述:** 拒绝来自指定用户的好友申请

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 接收者用户ID |
| `requesterId` | Long | 是 | 申请者用户ID |

**响应数据:**
```json
"好友申请已拒绝"
```

### 2.4 删除好友

**请求方法:** DELETE
**API 路径:** `/api/friends/remove`
**功能描述:** 删除指定的好友关系

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 当前用户ID |
| `targetUserId` | Long | 是 | 要删除的好友用户ID |

**响应数据:**
```json
"好友删除成功"
```

### 2.5 检查好友关系

**请求方法:** GET
**API 路径:** `/api/friends/check`
**功能描述:** 检查两个用户是否为好友关系

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `targetUserId` | Long | 是 | 目标用户ID |

**响应数据:**
```json
true  // 是好友
// 或
false // 不是好友
```

### 2.6 获取好友列表

**请求方法:** GET
**API 路径:** `/api/friends/list/{userId}`
**功能描述:** 获取用户的好友列表

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 456,
    "username": "好友1",
    "avatar": "头像URL",
    "bio": "好友简介"
  },
  // 更多好友...
]
```

### 2.7 获取待处理好友申请

**请求方法:** GET
**API 路径:** `/api/friends/requests/{userId}`
**功能描述:** 获取用户收到的待处理好友申请列表

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 789,
    "username": "申请者1",
    "avatar": "头像URL",
    "bio": "申请者简介"
  },
  // 更多申请者...
]
```

### 2.8 获取好友数量

**请求方法:** GET
**API 路径:** `/api/friends/count/{userId}`
**功能描述:** 获取用户的好友数量

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
5  // 好友数量
```

### 2.9 获取待处理申请数量

**请求方法:** GET
**API 路径:** `/api/friends/requests/count/{userId}`
**功能描述:** 获取用户收到的待处理好友申请数量

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
3  // 待处理申请数量
```

## 3. 消息相关 API

### 3.1 发送消息

**请求方法:** POST
**API 路径:** `/api/messages/send`
**功能描述:** 发送消息给指定用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `senderId` | Long | 是 | 发送者用户ID |
| `receiverId` | Long | 是 | 接收者用户ID |
| `content` | String | 是 | 消息内容 |

**响应数据:**
```json
"消息发送成功"
```

### 3.2 获取最近聊天记录

**请求方法:** GET
**API 路径:** `/api/messages/recent-chat`
**功能描述:** 获取两个用户之间的最近聊天记录

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `user1` | Long | 是 | 用户1 ID |
| `user2` | Long | 是 | 用户2 ID |
| `limit` | int | 否 | 返回消息数量限制，默认50 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": false
  },
  // 更多消息...
]
```

### 3.3 获取增量消息（基于消息ID）

**请求方法:** GET
**API 路径:** `/api/messages/new-messages/id`
**功能描述:** 获取比指定消息ID更新的消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `lastMessageId` | Long | 是 | 最后一条已收到消息的ID |

**响应数据:**
```json
[
  {
    "messageId": 10,
    "senderId": 789,
    "receiverId": 123,
    "content": "新消息内容",
    "sendTime": "2023-01-02T12:00:00",
    "read": false
  },
  // 更多新消息...
]
```

### 3.4 获取增量消息（基于时间戳）

**请求方法:** GET
**API 路径:** `/api/messages/new-messages/time`
**功能描述:** 获取比指定时间更新的消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `lastTime` | LocalDateTime | 是 | ISO格式的时间戳 |

**响应数据:**
```json
[
  {
    "messageId": 10,
    "senderId": 789,
    "receiverId": 123,
    "content": "新消息内容",
    "sendTime": "2023-01-02T12:00:00",
    "read": false
  },
  // 更多新消息...
]
```

### 3.5 获取完整聊天记录

**请求方法:** GET
**API 路径:** `/api/messages/full-chat`
**功能描述:** 获取两个用户之间的完整聊天记录

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `user1` | Long | 是 | 用户1 ID |
| `user2` | Long | 是 | 用户2 ID |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": true
  },
  // 所有聊天记录...
]
```

### 3.6 获取未读消息数量

**请求方法:** GET
**API 路径:** `/api/messages/unread-count`
**功能描述:** 获取用户的未读消息数量

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `lastMessageId` | Long | 是 | 最后一条已收到消息的ID |

**响应数据:**
```json
3  // 未读消息数量
```

### 3.7 删除消息

**请求方法:** DELETE
**API 路径:** `/api/messages/{messageId}`
**功能描述:** 根据消息ID删除消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `messageId` | Long | 是 | 消息ID，通过路径参数传递 |

**响应数据:**
```json
"消息删除成功"
```

### 3.8 删除发送者的所有消息

**请求方法:** DELETE
**API 路径:** `/api/messages/sender/{senderId}`
**功能描述:** 删除指定发送者发出的所有消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `senderId` | Long | 是 | 发送者用户ID，通过路径参数传递 |

**响应数据:**
```json
"成功删除 10 条消息"
```

### 3.9 删除接收者的所有消息

**请求方法:** DELETE
**API 路径:** `/api/messages/receiver/{receiverId}`
**功能描述:** 删除发送给指定接收者的所有消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `receiverId` | Long | 是 | 接收者用户ID，通过路径参数传递 |

**响应数据:**
```json
"成功删除 15 条消息"
```

### 3.10 删除聊天记录

**请求方法:** DELETE
**API 路径:** `/api/messages/chat-history`
**功能描述:** 删除两个用户之间的所有聊天记录

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `user1` | Long | 是 | 用户1 ID |
| `user2` | Long | 是 | 用户2 ID |

**响应数据:**
```json
"成功删除 20 条聊天记录"
```

### 3.11 获取发送者的所有消息

**请求方法:** GET
**API 路径:** `/api/messages/sender/{senderId}`
**功能描述:** 获取指定发送者发出的所有消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `senderId` | Long | 是 | 发送者用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": true
  },
  // 更多消息...
]
```

### 3.12 获取接收者的所有消息

**请求方法:** GET
**API 路径:** `/api/messages/receiver/{receiverId}`
**功能描述:** 获取发送给指定接收者的所有消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `receiverId` | Long | 是 | 接收者用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 789,
    "receiverId": 123,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": false
  },
  // 更多消息...
]
```

### 3.13 获取时间段内的聊天记录

**请求方法:** GET
**API 路径:** `/api/messages/time-range`
**功能描述:** 获取两个用户在指定时间段内的聊天记录

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `user1` | Long | 是 | 用户1 ID |
| `user2` | Long | 是 | 用户2 ID |
| `startTime` | LocalDateTime | 是 | ISO格式的开始时间 |
| `endTime` | LocalDateTime | 是 | ISO格式的结束时间 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": true
  },
  // 时间段内的消息...
]
```

### 3.14 统计发送者的消息数量

**请求方法:** GET
**API 路径:** `/api/messages/count/sender/{senderId}`
**功能描述:** 统计指定发送者发出的消息数量

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `senderId` | Long | 是 | 发送者用户ID，通过路径参数传递 |

**响应数据:**
```json
10  // 消息数量
```

### 3.15 统计接收者的消息数量

**请求方法:** GET
**API 路径:** `/api/messages/count/receiver/{receiverId}`
**功能描述:** 统计发送给指定接收者的消息数量

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `receiverId` | Long | 是 | 接收者用户ID，通过路径参数传递 |

**响应数据:**
```json
15  // 消息数量
```

### 3.16 统计总消息数量

**请求方法:** GET
**API 路径:** `/api/messages/count/total`
**功能描述:** 统计系统中的总消息数量

**响应数据:**
```json
1000  // 总消息数量
```

### 3.17 搜索用户消息内容

**请求方法:** GET
**API 路径:** `/api/messages/search/user`
**功能描述:** 搜索用户参与的消息中包含指定关键词的消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `keyword` | String | 是 | 搜索关键词 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "包含关键词的消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": true
  },
  // 更多匹配的消息...
]
```

### 3.18 搜索聊天记录内容

**请求方法:** GET
**API 路径:** `/api/messages/search/chat`
**功能描述:** 搜索两个用户之间聊天记录中包含指定关键词的消息

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `user1` | Long | 是 | 用户1 ID |
| `user2` | Long | 是 | 用户2 ID |
| `keyword` | String | 是 | 搜索关键词 |

**响应数据:**
```json
[
  {
    "messageId": 1,
    "senderId": 123,
    "receiverId": 456,
    "content": "包含关键词的消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "read": true
  },
  // 更多匹配的消息...
]
```

### 3.19 标记消息为已读

**请求方法:** PUT
**API 路径:** `/api/messages/{messageId}/read`
**功能描述:** 标记指定消息为已读状态

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `messageId` | Long | 是 | 消息ID，通过路径参数传递 |

**响应数据:**
```json
"消息已标记为已读"
```

### 3.20 标记所有消息为已读

**请求方法:** PUT
**API 路径:** `/api/messages/mark-all-read`
**功能描述:** 标记用户的所有未读消息为已读状态

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `lastMessageId` | Long | 是 | 最后一条已收到消息的ID |

**响应数据:**
```json
"成功标记 5 条消息为已读"
```

### 3.21 获取用户的聊天伙伴列表

**请求方法:** GET
**API 路径:** `/api/messages/chat-partners/{userId}`
**功能描述:** 获取与用户有过聊天记录的所有伙伴ID列表

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[456, 789, 101]  // 聊天伙伴用户ID列表
```

## 4. 推荐相关 API

### 4.1 获取同视频评论推荐用户

**请求方法:** GET
**API 路径:** `/api/recommend/co-comment/{userId}`
**功能描述:** 推荐与用户在同一视频下有评论互动的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 456,
    "username": "用户1",
    "commonVideoCount": 5,
    "similarityScore": 0.85
  },
  // 更多推荐...
]
```

### 4.2 获取回复推荐用户

**请求方法:** GET
**API 路径:** `/api/recommend/reply/{userId}`
**功能描述:** 推荐与用户有过回复互动的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 789,
    "username": "用户2",
    "replyCount": 10,
    "similarityScore": 0.75
  },
  // 更多推荐...
]
```

### 4.3 获取观看视频相似度推荐用户

**请求方法:** GET
**API 路径:** `/api/recommend/shared-video/{userId}`
**功能描述:** 推荐观看视频相似度高的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 101,
    "username": "用户3",
    "sharedVideoCount": 20,
    "similarityScore": 0.9
  },
  // 更多推荐...
]
```

### 4.4 获取分区重合度推荐用户

**请求方法:** GET
**API 路径:** `/api/recommend/category/{userId}`
**功能描述:** 推荐与用户观看分区重合度高的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 202,
    "username": "用户4",
    "commonCategoryCount": 8,
    "similarityScore": 0.82
  },
  // 更多推荐...
]
```

### 4.5 获取内容类型重合度推荐用户

**请求方法:** GET
**API 路径:** `/api/recommend/theme/{userId}`
**功能描述:** 推荐与用户观看内容类型重合度高的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 303,
    "username": "用户5",
    "commonThemeCount": 12,
    "similarityScore": 0.78
  },
  // 更多推荐...
]
```

### 4.6 获取用户行为相似度推荐

**请求方法:** GET
**API 路径:** `/api/recommend/user-behavior/{userId}`
**功能描述:** 推荐与用户行为模式相似度高的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 404,
    "username": "用户6",
    "behaviorSimilarity": 0.87,
    "activeTimeSimilarity": 0.92
  },
  // 更多推荐...
]
```

### 4.7 获取共同关注UP主推荐

**请求方法:** GET
**API 路径:** `/api/recommend/common-up/{userId}`
**功能描述:** 推荐与用户有共同关注UP主的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 505,
    "username": "用户7",
    "commonUpCount": 15,
    "similarityScore": 0.83
  },
  // 更多推荐...
]
```

### 4.8 获取收藏夹相似度推荐

**请求方法:** GET
**API 路径:** `/api/recommend/favorite-similarity/{userId}`
**功能描述:** 推荐与用户收藏夹内容相似度高的其他用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 606,
    "username": "用户8",
    "commonFavoriteCount": 25,
    "similarityScore": 0.88
  },
  // 更多推荐...
]
```

### 4.9 通过评论推荐好友

**请求方法:** GET
**API 路径:** `/api/recommend/comment-friends/{userId}`
**功能描述:** 基于评论内容相似度推荐好友

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID，通过路径参数传递 |

**响应数据:**
```json
[
  {
    "userId": 707,
    "username": "用户9",
    "commentSimilarity": 0.91,
    "commonTopicCount": 10
  },
  // 更多推荐...
]
```

## 5. 筛选相关 API

### 5.1 同一UP主筛选推荐

**请求方法:** GET
**API 路径:** `/api/filter/same-up`
**功能描述:** 筛选关注同一UP主的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `upId` | Long | 是 | UP主ID |
| `durationOption` | Integer | 否 | 关注时长选项，默认-1表示所有 |

**响应数据:**
```json
[
  {
    "userId": 808,
    "username": "用户10",
    "followTime": "2023-01-01T00:00:00",
    "activityLevel": "high"
  },
  // 更多筛选结果...
]
```

### 5.2 同一标签筛选

**请求方法:** GET
**API 路径:** `/api/filter/same-tag`
**功能描述:** 筛选对同一标签感兴趣的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `tagId` | Long | 是 | 标签ID |
| `durationOption` | Integer | 否 | 兴趣时长选项，默认-1表示所有 |

**响应数据:**
```json
[
  {
    "userId": 909,
    "username": "用户11",
    "interestLevel": 0.95,
    "relatedVideosCount": 30
  },
  // 更多筛选结果...
]
```

### 5.3 UP主视频观看比例筛选

**请求方法:** POST
**API 路径:** `/api/filter/same-up-video-count`
**功能描述:** 筛选观看同一UP主视频比例符合条件的用户

**请求参数:** (JSON格式)
```json
{
  "upId": 101,
  "minRatio": 0.3,
  "maxRatio": 1.0
}
```

**响应数据:**
```json
[
  {
    "userId": 1001,
    "username": "用户12",
    "videoRatio": 0.45,
    "watchedVideoCount": 18
  },
  // 更多筛选结果...
]
```

### 5.4 标签视频观看比例筛选

**请求方法:** GET
**API 路径:** `/api/filter/same-tag-video-count`
**功能描述:** 筛选观看同一标签视频比例符合条件的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `tagId` | Long | 是 | 标签ID |
| `ratioOption` | Integer | 是 | 比例选项（1-低，2-中，3-高） |

**响应数据:**
```json
[
  {
    "userId": 1101,
    "username": "用户13",
    "videoRatio": 0.65,
    "watchedVideoCount": 42
  },
  // 更多筛选结果...
]
```

### 5.5 关注时间缘分推荐

**请求方法:** GET
**API 路径:** `/api/filter/follow-time`
**功能描述:** 推荐在相近时间段关注同一UP主的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `userId` | Long | 是 | 用户ID |
| `upId` | Long | 是 | UP主ID |

**响应数据:**
```json
[
  {
    "userId": 1201,
    "username": "用户14",
    "followTime": "2023-02-15T00:00:00",
    "timeDifference": 3600 // 秒
  },
  // 更多推荐...
]
```

### 5.6 夜猫子用户筛选

**请求方法:** GET
**API 路径:** `/api/filter/night-owl`
**功能描述:** 筛选活跃时间在夜间的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `option` | String | 是 | 选项（'very_active', 'moderately_active', 'somewhat_active'） |

**响应数据:**
```json
[
  {
    "userId": 1301,
    "username": "用户15",
    "nightActivityScore": 0.9,
    "avgNightActiveTime": "02:30"
  },
  // 更多筛选结果...
]
```

### 5.7 用户活跃度筛选

**请求方法:** GET
**API 路径:** `/api/filter/user-activity`
**功能描述:** 根据用户活跃度进行筛选

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `option` | String | 是 | 选项（'very_active', 'active', 'moderately_active', 'less_active'） |

**响应数据:**
```json
[
  {
    "userId": 1401,
    "username": "用户16",
    "activityScore": 0.95,
    "avgDailyActiveTime": "120" // 分钟
  },
  // 更多筛选结果...
]
```

### 5.8 深度视频筛选

**请求方法:** GET
**API 路径:** `/api/filter/deep-video`
**功能描述:** 筛选观看同一视频的深度用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `videoId` | Long | 是 | 视频ID |
| `option` | Integer | 是 | 选项（1-完整观看，2-高互动，3-重复观看） |

**响应数据:**
```json
[
  {
    "userId": 1501,
    "username": "用户17",
    "watchRatio": 0.98,
    "interactionScore": 0.85
  },
  // 更多筛选结果...
]
```

### 5.9 系列作品筛选

**请求方法:** GET
**API 路径:** `/api/filter/series`
**功能描述:** 筛选观看同一标签系列作品的用户

**请求参数:**
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| `tagId` | Long | 是 | 标签ID |

**响应数据:**
```json
[
  {
    "userId": 1601,
    "username": "用户18",
    "seriesCompletionRate": 0.92,
    "watchedSeriesCount": 8
  },
  // 更多筛选结果...
]
```