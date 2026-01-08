import pandas as pd

# 读取数据
df = pd.read_csv('data.csv')

# 提取唯一用户ID
unique_user_ids = df['user_id'].unique().tolist()
total_users = len(unique_user_ids)

print("=" * 50)
print("用户ID统计报告:")
print("=" * 50)
print(f"总记录数: {len(df)}")
print(f"唯一用户数: {total_users}")
print(f"平均每个用户记录数: {len(df)/total_users:.2f}")

# 查看用户出现频率分布
print("\n用户出现频率分布:")
user_freq = df['user_id'].value_counts()
print(user_freq.describe())

# 查看最活跃的用户
print(f"\n最活跃的前5个用户:")
for user_id, count in user_freq.head().items():
    print(f"  {user_id}: {count} 次观看")

# 查看最少观看的用户
print(f"\n最少观看的用户 (只看过1次):")
one_time_users = user_freq[user_freq == 1]
print(f"  有 {len(one_time_users)} 个用户只看过1次")

# 保存唯一用户ID
with open('unique_user_ids.txt', 'w') as f:
    for user_id in unique_user_ids:
        f.write(f"{user_id}\n")

# 保存用户统计信息
user_stats = pd.DataFrame({
    'user_id': user_freq.index,
    'watch_count': user_freq.values
})
user_stats.to_csv('user_watch_stats.csv', index=False)

print(f"\n已保存:")
print(f"  1. 唯一用户ID列表到 unique_user_ids.txt")
print(f"  2. 用户观看统计到 user_watch_stats.csv")