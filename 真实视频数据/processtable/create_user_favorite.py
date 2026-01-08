import csv
import random
from datetime import datetime, timedelta

# 配置参数
user_start = 1
user_end = 50
video_start = 1
video_end = 2189
start_date = datetime(2017, 1, 1)
end_date = datetime(2019, 12, 31)

# 生成数据
data = []
for user_id in range(user_start, user_end + 1):
    # 每个用户随机收藏0-100个视频
    num_favorites = random.randint(0, 100)

    # 随机选择视频，避免重复收藏同一个视频
    total_videos = video_end - video_start + 1
    if num_favorites > total_videos:
        num_favorites = total_videos

    favorite_videos = random.sample(range(video_start, video_end + 1), num_favorites)

    for video_id in favorite_videos:
        # 随机生成收藏时间
        days_diff = (end_date - start_date).days
        random_days = random.randint(0, days_diff)
        random_seconds = random.randint(0, 86399)  # 0-23:59:59
        favorite_time = start_date + timedelta(days=random_days, seconds=random_seconds)

        data.append([user_id, video_id, favorite_time.strftime('%Y-%m-%d %H:%M:%S')])

# 随机打乱数据顺序
random.shuffle(data)

# 保存为CSV文件
with open('user_favorites.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['userID', 'videoID', 'favorite_time'])
    writer.writerows(data)

print(f"生成完成！共 {len(data)} 条记录")
print(f"文件已保存为: user_favorites.csv")
print(f"\n示例数据（前5行）:")
for row in data[:5]:
    print(row)