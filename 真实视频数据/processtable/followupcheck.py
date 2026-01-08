import pandas as pd
from datetime import datetime, timedelta

# 读取数据
print("正在读取数据...")
df = pd.read_csv('user_follow_up.csv')

# 确保时间列为datetime类型
df['followTime'] = pd.to_datetime(df['followTime'])

# 按upID分组
print("正在分析数据...")
results = []

for up_id, group in df.groupby('upID'):
    users = group['userID'].tolist()
    times = group['followTime'].tolist()

    # 检查每对用户
    for i in range(len(users)):
        for j in range(i + 1, len(users)):
            time_diff = abs((times[i] - times[j]).days)

            # 如果时间差在10天以内
            if time_diff <= 10:
                results.append({
                    'user1': users[i],
                    'user2': users[j],
                    'upid': up_id,
                    'followtime1': times[i].strftime('%Y-%m-%d'),
                    'followtime2': times[j].strftime('%Y-%m-%d'),
                    'time_diff_days': time_diff
                })

# 转换为DataFrame
if results:
    result_df = pd.DataFrame(results)

    # 排序：先按upID，再按时间差
    result_df = result_df.sort_values(['upid', 'time_diff_days'])

    # 保存为CSV
    output_file = 'similar_follow_time_users.csv'
    result_df.to_csv(output_file, index=False, encoding='utf-8')

    print(f"\n✅ 分析完成！找到 {len(result_df)} 对用户在10天内关注了同一个UP主")
    print(f"📁 结果已保存到: {output_file}")

    # 显示统计信息
    print("\n📊 统计信息:")
    print(f"涉及的UP主数量: {result_df['upid'].nunique()}")
    print(f"涉及的用户数量: {len(set(result_df['user1'].tolist() + result_df['user2'].tolist()))}")

    # 显示示例数据
    print("\n📋 前10条结果示例:")
    print(result_df.head(10).to_string(index=False))
else:
    print("❌ 没有找到在10天内关注同一个UP主的用户对")

# 可选：更详细的统计信息
if results:
    print("\n🔍 详细统计:")

    # 每个UP主有多少对用户
    up_stats = result_df.groupby('upid').size().reset_index(name='user_pairs_count')
    up_stats = up_stats.sort_values('user_pairs_count', ascending=False)

    print(f"\n每个UP主的用户对数量:")
    print(up_stats.head(10).to_string(index=False))

    # 时间差分布
    time_diff_stats = result_df['time_diff_days'].value_counts().sort_index()
    print(f"\n时间差分布:")
    for days, count in time_diff_stats.items():
        print(f"  相差{days}天: {count}对")