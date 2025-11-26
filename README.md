# B站好友推荐系统 API 文档

## 基础信息
- **基础URL**: `http://localhost:8080`
- **跨域支持**: 已启用
- **数据格式**: JSON

## 推荐类 API (`/api/recommend`)

### 1. 同视频评论推荐
**GET** `/api/recommend/co-comment/{userId}`

**描述**: 推荐在同一视频下评论过的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<CoCommentRecommendationDTO>`
```json
[
  {
    "userId": 123,
    "username": "用户A",
    "avatarPath": "/avatars/123.jpg",
    "videoTitles": ["视频标题1", "视频标题2"]
  }
]
```

### 2. 回复推荐
**GET** `/api/recommend/reply/{userId}`

**描述**: 推荐回复过自己评论的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<BaseDTO>`
```json
[
  {
    "userId": 456,
    "username": "用户B",
    "avatarPath": "/avatars/456.jpg"
  }
]
```

### 3. 观看视频相似度推荐
**GET** `/api/recommend/shared-video/{userId}`

**描述**: 推荐观看视频相似的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<SharedVideoRecommendationDTO>`
```json
[
  {
    "userId": 789,
    "username": "用户C",
    "avatarPath": "/avatars/789.jpg",
    "similarityRate": 0.75
  }
]
```

### 4. 分区重合度推荐
**GET** `/api/recommend/category/{userId}`

**描述**: 推荐分区偏好相似的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<CategoryRecommendationDTO>`
```json
[
  {
    "userId": 101,
    "username": "用户D",
    "avatarPath": "/avatars/101.jpg",
    "categoryMatchScore": 1.85,
    "commonCategories": ["游戏", "音乐"]
  }
]
```

### 5. 内容类型重合度推荐
**GET** `/api/recommend/theme/{userId}`

**描述**: 推荐内容类型偏好相似的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<ThemeRecommendationDTO>`
```json
[
  {
    "userId": 202,
    "username": "用户E",
    "avatarPath": "/avatars/202.jpg",
    "themeMatchScore": 1.65,
    "commonThemes": ["科技", "生活"]
  }
]
```

### 6. 用户行为相似度推荐
**GET** `/api/recommend/user-behavior/{userId}`

**描述**: 推荐用户行为相似的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<UserBehaviorRecommendationDTO>`
```json
[
  {
    "userId": 303,
    "username": "用户F",
    "avatarPath": "/avatars/303.jpg",
    "behaviorScore": 0.88
  }
]
```

### 7. 共同关注UP主推荐
**GET** `/api/recommend/common-up/{userId}`

**描述**: 推荐有共同关注UP主的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<CommonUpRecommendationDTO>`
```json
[
  {
    "userId": 404,
    "username": "用户G",
    "avatarPath": "/avatars/404.jpg",
    "commonUpCount": 3,
    "commonUpNames": ["影视飓风", "罗翔说刑法", "雨哥到处跑"]
  }
]
```

### 8. 收藏夹相似度推荐
**GET** `/api/recommend/favorite-similarity/{userId}`

**描述**: 推荐收藏夹相似的用户

**参数**:
- `userId` (路径参数): 用户ID

**返回**: `List<FavoriteSimilarityDTO>`
```json
[
  {
    "userId": 505,
    "username": "用户H",
    "avatarPath": "/avatars/505.jpg",
    "similarityScore": 0.75
  }
]
```

## 筛选类 API (`/api/filter`)

### 1. 同一UP主筛选
**GET** `/api/filter/same-up`

**描述**: 筛选观看同一UP主的用户

**参数**:
- `upId` (查询参数): UP主ID
- `durationOption` (查询参数, 可选): 时长等级 (-1=全部, 0=0-10分钟, 1=10-50分钟, 2=50-100分钟, 3=100-200分钟, 4=200分钟以上)

**返回**: `List<SameUpRecommendationDTO>`
```json
[
  {
    "userId": 606,
    "username": "用户I",
    "avatarPath": "/avatars/606.jpg",
    "totalWatchDuration": 3959,
    "watchCount": 4,
    "durationLevel": "100-200分钟",
    "upName": "影视飓风"
  }
]
```

### 2. 同一标签筛选
**GET** `/api/filter/same-tag`

**描述**: 筛选观看同一标签的用户

**参数**:
- `tagId` (查询参数): 标签ID
- `durationOption` (查询参数, 可选): 时长等级

**返回**: `List<SameTagRecommendationDTO>`
```json
[
  {
    "userId": 707,
    "username": "用户J",
    "avatarPath": "/avatars/707.jpg",
    "totalWatchDuration": 2850,
    "watchCount": 3,
    "durationLevel": "50-100分钟",
    "tagName": "科技"
  }
]
```

### 3. UP主视频观看比例筛选
**POST** `/api/filter/same-up-video-count`

**描述**: 筛选UP主视频观看比例相似的用户

**参数**:
- `upId` (Body): UP主ID
- `ratioOption` (Body): 比例选项

**返回**: `List<SameUpVideoCountDTO>`
```json
[
  {
    "userId": 808,
    "username": "用户K",
    "avatarPath": "/avatars/808.jpg",
    "watchedCount": 15,
    "totalCount": 20,
    "watchRatio": 0.75
  }
]
```

### 4. 标签视频观看比例筛选
**GET** `/api/filter/same-tag-video-count`

**描述**: 筛选标签视频观看比例相似的用户

**参数**:
- `tagId` (查询参数): 标签ID
- `ratioOption` (查询参数): 比例选项

**返回**: `List<SameTagVideoCountDTO>`
```json
[
  {
    "userId": 909,
    "username": "用户L",
    "avatarPath": "/avatars/909.jpg",
    "watchedCount": 8,
    "totalCount": 10,
    "watchRatio": 0.8
  }
]
```

### 5. 关注时间缘分推荐
**GET** `/api/filter/follow-time`

**描述**: 推荐关注时间相近的用户

**参数**:
- `userId` (查询参数): 用户ID
- `upId` (查询参数): UP主ID

**返回**: `List<FollowTimeRecommendationDTO>`
```json
[
  {
    "userId": 1010,
    "username": "用户M",
    "avatarPath": "/avatars/1010.jpg",
    "recommendationScore": 8,
    "followDurationScore": 4,
    "timeDifferenceScore": 4,
    "timeDifferenceDays": 2,
    "upName": "小潮院长"
  }
]
```

### 6. 夜猫子用户筛选
**GET** `/api/filter/night-owl`

**描述**: 筛选夜猫子用户

**参数**:
- `option` (查询参数): 筛选选项

**返回**: `List<NightOwlRecommendationDTO>`
```json
[
  {
    "userId": 1111,
    "username": "用户N",
    "avatarPath": "/avatars/1111.jpg",
    "nightActivityScore": 0.9
  }
]
```

### 7. 用户活跃度筛选
**GET** `/api/filter/user-activity`

**描述**: 筛选用户活跃度

**参数**:
- `option` (查询参数): 筛选选项

**返回**: `List<UserActivityRecommendationDTO>`
```json
[
  {
    "userId": 1212,
    "username": "用户O",
    "avatarPath": "/avatars/1212.jpg",
    "activityLevel": "高活跃度"
  }
]
```

### 8. 深度视频筛选
**GET** `/api/filter/deep-video`

**描述**: 筛选深度观看视频的用户

**参数**:
- `videoId` (查询参数): 视频ID
- `option` (查询参数): 筛选选项 (0=深度观看, 1=极其深度观看)

**返回**: `List<DeepVideoRecommendationDTO>`
```json
[
  {
    "userId": 1313,
    "username": "用户P",
    "avatarPath": "/avatars/1313.jpg",
    "videoId": 20,
    "videoTitle": "影视飓风的舆情，说明互联网已经烂完了",
    "totalWatchDuration": 4834,
    "watchCount": 6
  }
]
```

### 9. 系列作品筛选
**GET** `/api/filter/series`

**描述**: 筛选看完整个系列的用户

**参数**:
- `tagId` (查询参数): 标签ID

**返回**: `List<SeriesRecommendationDTO>`
```json
[
  {
    "userId": 1414,
    "username": "用户Q",
    "avatarPath": "/avatars/1414.jpg",
    "seriesName": "每日必看系列"
  }
]
```
### 10. 基于用户评论内容相似度推荐好友
- **端点**: `GET /api/recommend/comment-friends/{userId}`
- **描述**: 通过分析用户的历史评论内容，找到评论内容相似的其他用户进行好友推荐

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| userId | Long | 是 | 目标用户ID，路径参数 |

### 响应数据
返回 `List<FriendRecommendationDTO>` 对象数组，包含以下字段：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| userComment | Comment | 用户自己的评论 |
| matchedComment | Comment | 匹配到的其他用户评论 |
| video | Video | 相关视频信息 |
| recommendedUser | User | 推荐用户信息 |
| matchScore | double | 评论内容匹配分数 |

### Comment 对象结构
```json
{
  "commentId": "评论ID",
  "videoId": "视频ID", 
  "userId": "用户ID",
  "content": "评论内容",
  "parentId": "父评论ID"
}
```

### User 对象结构
```json
{
  "userId": "用户ID",
  "username": "用户名",
  "registerTime": "注册时间",
  "avatarPath": "头像路径"
}
```

### Video 对象结构
```json
{
  "videoId": "视频ID",
  "uploaderId": "上传者ID",
  "title": "视频标题",
  "categoryId": "分类ID", 
  "themeId": "主题ID",
  "publishTime": "发布时间",
  "duration": "视频时长"
}
```

### 响应示例
```json
[
  {
    "userComment": {
      "commentId": 12345,
      "videoId": 67890,
      "userId": 1001,
      "content": "这个视频太精彩了！",
      "parentId": 0
    },
    "matchedComment": {
      "commentId": 12346,
      "videoId": 67890,
      "userId": 1002,
      "content": "确实很精彩的内容",
      "parentId": 0
    },
    "video": {
      "videoId": 67890,
      "uploaderId": 5001,
      "title": "精彩视频标题",
      "categoryId": 1,
      "themeId": 2,
      "publishTime": "2024-01-15T10:30:00",
      "duration": "00:15:30"
    },
    "recommendedUser": {
      "userId": 1002,
      "username": "推荐用户昵称",
      "registerTime": "2023-05-20T14:25:00",
      "avatarPath": "/avatars/1002.jpg"
    },
    "matchScore": 0.85
  }
]
```

### 使用场景
- 为用户推荐兴趣相投的潜在好友
- 基于评论内容的社区用户发现
- 增强用户社交互动和社区粘性

### 注意事项
- 需要确保评论内容长度满足最小字数要求
- 匹配分数超过阈值才会被推荐
- 推荐结果按匹配分数降序排列

### 模型配置方法
基于用户评论内容相似度推荐好友方法需要在本地下载两个模型
分别为bert-base-multilingual-uncased-sentiment文件夹
和paraphrase-multilingual-MiniLM-L12-v2文件夹
将这两个模型文件夹放在**rec-system**文件夹下，**在使用平台打开项目时，必需选择打开rec-system文件夹，否则无法运行**



## 其他api
- **基础路径**：`/api/users`
- **控制器**：`UserController`
- **说明**：提供用户信息查询、用户/视频/标签的模糊搜索功能。

---

### 1. 根据用户ID获取用户信息

**端点**：`GET /{userId}`

**描述**：根据用户ID获取指定用户的详细信息。

**路径参数**：
| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| userId | Long | 是   | 用户ID |

**响应示例**：
```json
{
  "success": true,
  "message": "获取用户信息成功",
  "data": {
    "userId": 1,
    "username": "张三",
    "registerTime": "2023-10-01T12:00:00",
    "avatarPath": "/avatars/1.jpg"
  }
}
```

**错误响应**：
```json
{
  "success": false,
  "message": "用户不存在"
}
```

---

### 2. 根据用户名模糊搜索用户

**端点**：`GET /search`

**描述**：根据用户名关键词模糊匹配用户列表。

**查询参数**：
| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

**响应示例**：
```json
{
  "success": true,
  "message": "搜索完成，找到 2 个用户",
  "keyword": "张",
  "total": 2,
  "data": [
    {
      "userId": 1,
      "username": "张三",
      "registerTime": "2023-10-01T12:00:00",
      "avatarPath": "/avatars/1.jpg"
    },
    {
      "userId": 2,
      "username": "张四",
      "registerTime": "2023-10-02T12:00:00",
      "avatarPath": "/avatars/2.jpg"
    }
  ]
}
```

---

### 3. 根据视频标题模糊搜索视频

**端点**：`GET /videos/search`

**描述**：根据视频标题关键词模糊匹配视频列表。

**查询参数**：
| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

**响应示例**：
```json
{
  "success": true,
  "message": "搜索完成，找到 1 个视频",
  "keyword": "教程",
  "total": 1,
  "data": [
    {
      "videoId": 101,
      "uploaderId": 1,
      "title": "Spring Boot 教程",
      "categoryId": 2,
      "themeId": 5,
      "publishTime": "2024-01-15T10:00:00",
      "duration": "00:15:30"
    }
  ]
}
```

---

### 4. 根据标签名称模糊搜索标签

**端点**：`GET /tags/search`

**描述**：根据标签名称关键词模糊匹配标签列表。

**查询参数**：
| 参数名 | 类型 | 必需 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

**响应示例**：
```json
{
  "success": true,
  "message": "搜索完成，找到 3 个标签",
  "keyword": "科技",
  "total": 3,
  "data": [
    {
      "tagId": 10,
      "tagName": "科技前沿"
    },
    {
      "tagId": 11,
      "tagName": "科技资讯"
    }
  ]
}
```





