# B站推荐交友系统 API 文档

## 目录

- [推荐相关API](#推荐相关api)
- [筛选相关API](#筛选相关api)
- [用户相关API](#用户相关api)
- [好友相关API](#好友相关api)
- [消息相关API](#消息相关api)

## 推荐相关API

### 1. 同视频评论推荐

**请求路径**: `GET /api/recommend/co-comment/{userId}`

**功能**: 根据用户评论过的相同视频推荐潜在好友

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "commonVideoCount": 5,
    "similarityScore": 0.85
  }
]
```
显示：你和username都在videotitle下评论过，认识一下叭。

### 2. 评论回复推荐

**请求路径**: `GET /api/recommend/reply/{userId}`

**功能**: 基于评论互动推荐用户

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "interactionCount": 10,
    "lastInteractionTime": "2023-01-01T12:00:00"
  }
]
```
显示：username曾经回复过你的评论，认识一下叭。

### 3. 共同分享视频推荐

**请求路径**: `GET /api/recommend/shared-video/{userId}`

**功能**: 基于共同分享的视频推荐用户

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "sharedVideoCount": 3,
    "similarityScore": 0.72
  }
]
```
显示：你与username观看过的视频的相似度为similarityRate%，认识一下叭。

### 4. 分类偏好推荐

**请求路径**: `GET /api/recommend/category/{userId}`

**功能**: 基于视频分类偏好推荐用户

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "categorySimilarity": 0.88,
    "commonCategories": ["科技", "游戏"]
  }
]
```

### 5. 主题偏好推荐

**请求路径**: `GET /api/recommend/theme/{userId}`

**功能**: 基于视频主题偏好推荐用户

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "themeSimilarity": 0.75,
    "commonThemes": ["AI", "编程"]
  }
]
```

### 6. 收藏相似度推荐

**请求路径**: `GET /api/recommend/favorite-similarity/{userId}`

**功能**: 基于收藏视频相似度推荐用户

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "favoriteSimilarity": 0.81,
    "commonFavoriteCount": 12
  }
]
```

### 7. 评论好友推荐

**请求路径**: `GET /api/recommend/comment-friends/{userId}`

**功能**: 基于评论行为推荐潜在好友

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "commentSimilarity": 0.87,
    "interactionCount": 15
  }
]
```

## 筛选相关API

### 1. 同一标签筛选

**请求路径**: `GET /api/filter/same-tag`

**功能**: 筛选关注同一标签的用户

**参数**:
- `tagId`: 标签ID (必填)
- `durationOption`: 关注时长选项 (可选，默认-1)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "followDate": "2023-01-01T12:00:00",
    "commonTagInterest": 0.78
  }
]
```

### 2. UP主视频观看比例筛选

**请求路径**: `POST /api/filter/same-up-video-count`

**功能**: 根据UP主视频观看比例筛选用户

**参数**:
- 请求体: `{"upId": 123, "ratioOption": 1}`

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "videoWatchCount": 25,
    "watchRatio": 0.65
  }
]
```

### 3. 标签视频观看比例筛选

**请求路径**: `GET /api/filter/same-tag-video-count`

**功能**: 根据标签视频观看比例筛选用户

**参数**:
- `tagId`: 标签ID (必填)
- `ratioOption`: 比例选项 (必填)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "tagVideoCount": 30,
    "watchRatio": 0.72
  }
]
```

### 4. 夜猫子用户筛选

**请求路径**: `GET /api/filter/night-owl`

**功能**: 筛选夜间活跃的用户

**参数**:
- `option`: 筛选选项 (必填)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "nightActivityScore": 0.88,
    "usualActiveHours": ["22:00-24:00", "00:00-02:00"]
  }
]
```

### 5. 深度视频筛选

**请求路径**: `GET /api/filter/deep-video`

**功能**: 根据视频观看深度筛选用户

**参数**:
- `videoId`: 视频ID (必填)
- `option`: 筛选选项 (必填)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "watchCompletionRate": 0.92,
    "watchTime": 1200
  }
]
```

### 6. 系列作品筛选

**请求路径**: `GET /api/filter/series`

**功能**: 根据系列作品兴趣筛选用户

**参数**:
- `tagId`: 标签ID (必填)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "seriesWatchCount": 25,
    "completionRate": 0.85
  }
]
```

## 责任链相关API

### 1. UP主视频观看比例筛选 + 责任链筛选

**请求路径**: `POST /api/chain/same-up-video-count`

**功能**: 先根据UP主视频观看比例筛选用户，再通过责任链进行二次筛选

**参数**:
- 请求体: `{"upId": 123, "ratioOption": 1}`
- `activity`: 活跃度筛选选项 (可选，默认0)
- `nightOwl`: 夜猫子筛选选项 (可选，默认0)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "videoWatchCount": 25,
    "watchRatio": 0.65
  }
]
```

### 2. 标签视频观看比例筛选 + 责任链筛选

**请求路径**: `GET /api/chain/same-tag-video-count`

**功能**: 先根据标签视频观看比例筛选用户，再通过责任链进行二次筛选

**参数**:
- `tagId`: 标签ID (必填)
- `ratioOption`: 比例选项 (必填)
- `activity`: 活跃度筛选选项 (可选，默认0)
- `nightOwl`: 夜猫子筛选选项 (可选，默认0)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "tagVideoCount": 30,
    "watchRatio": 0.72
  }
]
```

### 3. 深度视频筛选 + 责任链筛选

**请求路径**: `GET /api/chain/deep-video`

**功能**: 先根据视频观看深度筛选用户，再通过责任链进行二次筛选

**参数**:
- `videoId`: 视频ID (必填)
- `option`: 筛选选项 (必填)
- `activity`: 活跃度筛选选项 (可选，默认0)
- `nightOwl`: 夜猫子筛选选项 (可选，默认0)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "watchCompletionRate": 0.92,
    "watchTime": 1200
  }
]
```

### 4. 系列作品筛选 + 责任链筛选

**请求路径**: `GET /api/chain/series`

**功能**: 先根据系列作品兴趣筛选用户，再通过责任链进行二次筛选

**参数**:
- `tagId`: 标签ID (必填)
- `activity`: 活跃度筛选选项 (可选，默认0)
- `nightOwl`: 夜猫子筛选选项 (可选，默认0)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "seriesWatchCount": 25,
    "completionRate": 0.85
  }
]
```

## 用户相关API

### 1. 获取用户信息

**请求路径**: `GET /api/users/{userId}`

**功能**: 通过用户ID获取用户信息

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "bio": "个人简介",
    "registrationDate": "2023-01-01T12:00:00"
  },
  "message": "获取用户信息成功"
}
```

### 2. 搜索用户

**请求路径**: `GET /api/users/search`

**功能**: 根据用户名模糊搜索用户

**参数**:
- `keyword`: 搜索关键词 (必填)

**响应**: 
```json
{
  "success": true,
  "data": [
    {
      "userId": 123,
      "username": "用户名",
      "avatar": "头像URL"
    }
  ],
  "total": 1,
  "keyword": "搜索关键词",
  "message": "搜索完成，找到 1 个用户"
}
```

### 3. 搜索视频

**请求路径**: `GET /api/users/videos/search`

**功能**: 根据视频标题模糊搜索视频

**参数**:
- `keyword`: 搜索关键词 (必填)

**响应**: 
```json
{
  "success": true,
  "data": [
    {
      "videoId": 123,
      "title": "视频标题",
      "uploaderId": 456,
      "uploadDate": "2023-01-01T12:00:00",
      "duration": 300
    }
  ],
  "total": 1,
  "keyword": "搜索关键词",
  "message": "搜索完成，找到 1 个视频"
}
```

### 4. 搜索标签

**请求路径**: `GET /api/users/tags/search`

**功能**: 根据标签名称模糊搜索标签

**参数**:
- `keyword`: 搜索关键词 (必填)

**响应**: 
```json
{
  "success": true,
  "data": [
    {
      "tagId": 123,
      "tagName": "标签名称",
      "videoCount": 150
    }
  ],
  "total": 1,
  "keyword": "搜索关键词",
  "message": "搜索完成，找到 1 个标签"
}
```

## 好友相关API

### 1. 发送好友申请

**请求路径**: `POST /api/friends/request`

**功能**: 发送好友申请

**参数**:
- `userId`: 用户ID (必填)
- `targetUserId`: 目标用户ID (必填)

**响应**: 
```
"好友申请发送成功"
```

### 2. 接受好友申请

**请求路径**: `POST /api/friends/accept`

**功能**: 接受好友申请

**参数**:
- `userId`: 用户ID (必填)
- `requesterId`: 请求者ID (必填)

**响应**: 
```
"好友申请已接受"
```

### 3. 拒绝好友申请

**请求路径**: `POST /api/friends/reject`

**功能**: 拒绝好友申请

**参数**:
- `userId`: 用户ID (必填)
- `requesterId`: 请求者ID (必填)

**响应**: 
```
"好友申请已拒绝"
```

### 4. 删除好友

**请求路径**: `DELETE /api/friends/remove`

**功能**: 删除好友

**参数**:
- `userId`: 用户ID (必填)
- `targetUserId`: 目标用户ID (必填)

**响应**: 
```
"好友删除成功"
```

### 5. 检查好友关系

**请求路径**: `GET /api/friends/check`

**功能**: 检查两个用户是否为好友

**参数**:
- `userId`: 用户ID (必填)
- `targetUserId`: 目标用户ID (必填)

**响应**: 
```
true 或 false
```

### 6. 获取好友列表

**请求路径**: `GET /api/friends/list/{userId}`

**功能**: 获取用户的好友列表

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "friendSince": "2023-01-01T12:00:00"
  }
]
```

### 7. 获取待处理好友申请

**请求路径**: `GET /api/friends/requests/{userId}`

**功能**: 获取待处理的好友申请列表

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[
  {
    "userId": 123,
    "username": "用户名",
    "avatar": "头像URL",
    "requestTime": "2023-01-01T12:00:00"
  }
]
```

### 8. 获取好友数量

**请求路径**: `GET /api/friends/count/{userId}`

**功能**: 获取用户的好友数量

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```
5
```

### 9. 获取待处理申请数量

**请求路径**: `GET /api/friends/requests/count/{userId}`

**功能**: 获取用户的待处理好友申请数量

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```
3
```

## 消息相关API

### 1. 发送消息

**请求路径**: `POST /api/messages/send`

**功能**: 发送消息给另一个用户

**参数**:
- `senderId`: 发送者ID (必填)
- `receiverId`: 接收者ID (必填)
- `content`: 消息内容 (必填)

**响应**: 
```
"消息发送成功"
```

### 2. 获取最近聊天记录

**请求路径**: `GET /api/messages/recent-chat`

**功能**: 获取两个用户之间的最近聊天记录

**参数**:
- `user1`: 用户1 ID (必填)
- `user2`: 用户2 ID (必填)
- `limit`: 限制数量 (可选，默认50)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": false
  }
]
```

### 3. 获取增量消息（基于ID）

**请求路径**: `GET /api/messages/new-messages/id`

**功能**: 获取指定消息ID之后的新消息

**参数**:
- `userId`: 用户ID (必填)
- `lastMessageId`: 最后消息ID (必填)

**响应**: 
```json
[
  {
    "messageId": 124,
    "senderId": 456,
    "receiverId": 789,
    "content": "新消息内容",
    "sendTime": "2023-01-01T13:00:00",
    "isRead": false
  }
]
```

### 4. 获取增量消息（基于时间戳）

**请求路径**: `GET /api/messages/new-messages/time`

**功能**: 获取指定时间戳之后的新消息

**参数**:
- `userId`: 用户ID (必填)
- `lastTime`: 最后时间 (必填，ISO格式)

**响应**: 
```json
[
  {
    "messageId": 124,
    "senderId": 456,
    "receiverId": 789,
    "content": "新消息内容",
    "sendTime": "2023-01-01T13:00:00",
    "isRead": false
  }
]
```

### 5. 获取完整聊天记录

**请求路径**: `GET /api/messages/full-chat`

**功能**: 获取两个用户之间的完整聊天记录

**参数**:
- `user1`: 用户1 ID (必填)
- `user2`: 用户2 ID (必填)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": true
  }
]
```

### 6. 获取未读消息数量

**请求路径**: `GET /api/messages/unread-count`

**功能**: 获取未读消息数量

**参数**:
- `userId`: 用户ID (必填)
- `lastMessageId`: 最后消息ID (必填)

**响应**: 
```
5
```

### 7. 删除消息

**请求路径**: `DELETE /api/messages/{messageId}`

**功能**: 根据消息ID删除消息

**参数**:
- `messageId`: 消息ID (路径参数)

**响应**: 
```
"消息删除成功"
```

### 8. 删除发送者所有消息

**请求路径**: `DELETE /api/messages/sender/{senderId}`

**功能**: 删除指定发送者的所有消息

**参数**:
- `senderId`: 发送者ID (路径参数)

**响应**: 
```
"成功删除 10 条消息"
```

### 9. 删除接收者所有消息

**请求路径**: `DELETE /api/messages/receiver/{receiverId}`

**功能**: 删除发送给指定接收者的所有消息

**参数**:
- `receiverId`: 接收者ID (路径参数)

**响应**: 
```
"成功删除 15 条消息"
```

### 10. 删除聊天记录

**请求路径**: `DELETE /api/messages/chat-history`

**功能**: 删除两个用户之间的所有聊天记录

**参数**:
- `user1`: 用户1 ID (必填)
- `user2`: 用户2 ID (必填)

**响应**: 
```
"成功删除 20 条聊天记录"
```

### 11. 获取发送者所有消息

**请求路径**: `GET /api/messages/sender/{senderId}`

**功能**: 获取指定发送者发送的所有消息

**参数**:
- `senderId`: 发送者ID (路径参数)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": true
  }
]
```

### 12. 获取接收者所有消息

**请求路径**: `GET /api/messages/receiver/{receiverId}`

**功能**: 获取发送给指定接收者的所有消息

**参数**:
- `receiverId`: 接收者ID (路径参数)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": false
  }
]
```

### 13. 获取时间段内聊天记录

**请求路径**: `GET /api/messages/time-range`

**功能**: 获取指定时间段内的聊天记录

**参数**:
- `user1`: 用户1 ID (必填)
- `user2`: 用户2 ID (必填)
- `startTime`: 开始时间 (必填，ISO格式)
- `endTime`: 结束时间 (必填，ISO格式)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": true
  }
]
```

### 14. 统计发送者消息数量

**请求路径**: `GET /api/messages/count/sender/{senderId}`

**功能**: 统计指定发送者发送的消息数量

**参数**:
- `senderId`: 发送者ID (路径参数)

**响应**: 
```
50
```

### 15. 统计接收者消息数量

**请求路径**: `GET /api/messages/count/receiver/{receiverId}`

**功能**: 统计发送给指定接收者的消息数量

**参数**:
- `receiverId`: 接收者ID (路径参数)

**响应**: 
```
45
```

### 16. 统计总消息数量

**请求路径**: `GET /api/messages/count/total`

**功能**: 统计系统中的总消息数量

**参数**: 无

**响应**: 
```
1000
```

### 17. 搜索用户消息内容

**请求路径**: `GET /api/messages/search/user`

**功能**: 搜索用户收发的消息内容

**参数**:
- `userId`: 用户ID (必填)
- `keyword`: 搜索关键词 (必填)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "包含关键词的消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": true
  }
]
```

### 18. 搜索聊天记录内容

**请求路径**: `GET /api/messages/search/chat`

**功能**: 搜索两个用户之间的聊天记录内容

**参数**:
- `user1`: 用户1 ID (必填)
- `user2`: 用户2 ID (必填)
- `keyword`: 搜索关键词 (必填)

**响应**: 
```json
[
  {
    "messageId": 123,
    "senderId": 456,
    "receiverId": 789,
    "content": "包含关键词的消息内容",
    "sendTime": "2023-01-01T12:00:00",
    "isRead": true
  }
]
```

### 19. 标记消息为已读

**请求路径**: `PUT /api/messages/{messageId}/read`

**功能**: 标记指定消息为已读

**参数**:
- `messageId`: 消息ID (路径参数)

**响应**: 
```
"消息已标记为已读"
```

### 20. 标记所有消息为已读

**请求路径**: `PUT /api/messages/mark-all-read`

**功能**: 标记所有指定消息ID之后的消息为已读

**参数**:
- `userId`: 用户ID (必填)
- `lastMessageId`: 最后消息ID (必填)

**响应**: 
```
"成功标记 10 条消息为已读"
```

### 21. 获取聊天伙伴列表

**请求路径**: `GET /api/messages/chat-partners/{userId}`

**功能**: 获取与用户有聊天记录的伙伴ID列表

**参数**:
- `userId`: 用户ID (路径参数)

**响应**: 
```json
[123, 456, 789]
```