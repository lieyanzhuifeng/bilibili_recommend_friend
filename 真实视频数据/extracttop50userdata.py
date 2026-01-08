import pandas as pd

# 读取用户统计文件
user_stats = pd.read_csv('user_watch_stats.csv')

# 按观看次数从高到低排序，取前50个用户
top_50_users = user_stats.sort_values('watch_count', ascending=False).head(50)

print("前50个最活跃的用户:")
print("-" * 40)
for i, (_, row) in enumerate(top_50_users.iterrows(), 1):
    print(f"{i:2d}. {row['user_id']}: {row['watch_count']} 次观看")

# 提取用户ID列表
top_user_ids = top_50_users['user_id'].tolist()
print(f"\n共提取 {len(top_user_ids)} 个用户ID")

# 读取原始数据文件
print("\n正在读取原始数据...")
data_df = pd.read_csv('data.csv')

# 筛选前50个用户的记录
top_users_data = data_df[data_df['user_id'].isin(top_user_ids)]

print(f"\n原始数据总记录数: {len(data_df)}")
print(f"前50用户记录数: {len(top_users_data)}")
print(f"占比: {len(top_users_data)/len(data_df)*100:.2f}%")

# 保存结果
top_users_data.to_csv('smalldata.csv', index=False)
top_50_users.to_csv('top_50_users_info.csv', index=False)

print("\n已保存:")
print(f"1. 前50用户完整观看记录: smalldata.csv")
print(f"2. 前50用户统计信息: top_50_users_info.csv")