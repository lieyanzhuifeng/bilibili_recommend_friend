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
