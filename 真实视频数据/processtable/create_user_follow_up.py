import pandas as pd
import random
from datetime import datetime, timedelta

# 配置参数
user_range = range(1, 51)
up_range = range(51, 151)
start_date = datetime(2017, 1, 1)
end_date = datetime(2019, 12, 31)

# 生成数据
records = []
for user_id in user_range:
    # 随机决定关注数量（0-30）
    follow_count = random.randint(0, 30)

    # 随机选择不重复的UP主
    if follow_count > 0:
        # 确保不超过可用的UP主数量
        available_ups = list(up_range)
        if follow_count > len(available_ups):
            follow_count = len(available_ups)

        followed_ups = random.sample(available_ups, follow_count)

        for up_id in followed_ups:
            # 随机生成日期
            days_diff = (end_date - start_date).days
            random_days = random.randint(0, days_diff)
            follow_date = start_date + timedelta(days=random_days)

            records.append({
                'userID': user_id,
                'upID': up_id,
                'followTime': follow_date.strftime('%Y-%m-%d')
            })

# 创建DataFrame并保存为CSV
df = pd.DataFrame(records)
df.to_csv('user_follow_up.csv', index=False, encoding='utf-8')

print(f"生成完成！共 {len(df)} 条记录")
print(f"文件已保存为: user_follow_up.csv")
print(f"\n数据预览:")
print(df.head())