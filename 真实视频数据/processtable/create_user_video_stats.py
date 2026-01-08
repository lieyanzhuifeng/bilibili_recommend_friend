import pandas as pd
from datetime import time


def generate_user_video_stats(input_file, output_file='user_video_stats.csv'):
    """
    从smalldata.csv生成user_video_stats表

    参数:
        input_file: smalldata.csv文件路径
        output_file: 输出文件路径
    """

    print("开始生成user_video_stats表数据...")
    print("-" * 50)

    # 1. 读取数据
    print("读取数据文件...")
    df = pd.read_csv(input_file)

    print(f"原始数据记录数: {len(df)}")
    print(f"列名: {list(df.columns)}")

    # 2. 检查必要的列是否存在
    required_cols = ['user_id', 'movie_id', 'duration']
    for col in required_cols:
        if col not in df.columns:
            print(f"错误: 缺少必要的列 '{col}'")
            print(f"可用列: {list(df.columns)}")
            return None

    # 3. 按用户和视频分组统计
    print("\n按用户和视频分组统计...")

    # 分组统计
    stats = df.groupby(['user_id', 'movie_id']).agg(
        watchCount=('duration', 'count'),  # 观看次数
        totalWatchDuration=('duration', 'sum')  # 总观看时长
    ).reset_index()

    print(f"生成统计记录数: {len(stats)}")

    # 4. 添加自增的statid
    stats.insert(0, 'statid', range(1, len(stats) + 1))

    # 5. 重命名列以匹配表结构
    stats = stats.rename(columns={
        'user_id': 'userID',
        'movie_id': 'videoID'
    })

    # 6. 将秒数转换为time格式（HH:MM:SS）
    def seconds_to_time(seconds):
        """将秒数转换为time格式"""
        try:
            seconds = int(seconds)
            hours = seconds // 3600
            minutes = (seconds % 3600) // 60
            secs = seconds % 60
            return f"{hours:02d}:{minutes:02d}:{secs:02d}"
        except:
            return "00:00:00"

    stats['totalWatchDuration'] = stats['totalWatchDuration'].apply(seconds_to_time)

    # 7. 保存结果
    stats.to_csv(output_file, index=False)

    print(f"\n处理完成!")
    print(f"生成记录数: {len(stats)}")
    print(f"保存到: {output_file}")

    # 8. 统计信息
    print(f"\n统计信息:")
    print(f"唯一用户数: {stats['userID'].nunique()}")
    print(f"唯一视频数: {stats['videoID'].nunique()}")

    # 观看次数分布
    print(f"\n观看次数分布:")
    watch_counts = stats['watchCount'].value_counts().sort_index()
    for count, freq in watch_counts.head(10).items():
        print(f"  观看{count}次: {freq} 条记录")

    if len(watch_counts) > 10:
        print(f"  ... 还有 {len(watch_counts) - 10} 种其他观看次数")

    # 最活跃的用户
    print(f"\n最活跃的用户 (按观看视频数排名前5):")
    user_activity = stats.groupby('userID').agg(
        video_count=('videoID', 'nunique'),
        total_watch_count=('watchCount', 'sum')
    ).sort_values('video_count', ascending=False)

    for i, (user_id, row) in enumerate(user_activity.head().iterrows(), 1):
        print(f"  第{i}名: 用户{user_id} - 观看了{row['video_count']}部不同视频，总观看{row['total_watch_count']}次")

    # 最受欢迎的视频
    print(f"\n最受欢迎的视频 (按观看用户数排名前5):")
    video_popularity = stats.groupby('videoID').agg(
        user_count=('userID', 'nunique'),
        total_watch_count=('watchCount', 'sum')
    ).sort_values('user_count', ascending=False)

    for i, (video_id, row) in enumerate(video_popularity.head().iterrows(), 1):
        print(f"  第{i}名: 视频{video_id} - 被{row['user_count']}个用户观看，总观看{row['total_watch_count']}次")

    # 9. 显示前10条记录
    print(f"\n前10条记录:")
    print("-" * 80)
    print(f"{'statid':<6} {'userID':<8} {'videoID':<8} {'watchCount':<10} {'totalWatchDuration':<15}")
    print("-" * 80)

    for i, row in stats.head(10).iterrows():
        print(
            f"{row['statid']:<6} {row['userID']:<8} {row['videoID']:<8} {row['watchCount']:<10} {row['totalWatchDuration']:<15}")

    return stats


# 主程序
if __name__ == "__main__":
    # 文件路径
    input_file = 'smalldata.csv'
    output_file = 'user_video_stats.csv'

    # 生成user_video_stats表
    generate_user_video_stats(input_file, output_file)