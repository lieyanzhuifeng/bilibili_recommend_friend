import pandas as pd
import numpy as np


def time_to_seconds(time_str):
    """将HH:MM:SS格式的时间转换为秒数"""
    if pd.isna(time_str):
        return 0

    try:
        if isinstance(time_str, str):
            parts = time_str.split(':')
            if len(parts) == 3:  # HH:MM:SS
                hours, minutes, seconds = parts
                return int(hours) * 3600 + int(minutes) * 60 + int(seconds)
            elif len(parts) == 2:  # MM:SS
                minutes, seconds = parts
                return int(minutes) * 60 + int(seconds)
            else:
                return int(time_str)
        else:
            return int(time_str)
    except:
        return 0


def seconds_to_time(seconds):
    """将秒数转换为HH:MM:SS格式"""
    try:
        seconds = int(seconds)
        hours = seconds // 3600
        minutes = (seconds % 3600) // 60
        secs = seconds % 60
        return f"{hours:02d}:{minutes:02d}:{secs:02d}"
    except:
        return "00:00:00"


def calculate_video_median_watch_time(stats_file, output_file='video_median_watch_time.csv'):
    """
    计算每个视频的中位数观看时长

    参数:
        stats_file: user_video_stats.csv文件路径
        output_file: 输出文件路径
    """

    print("开始计算视频中位数观看时长...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        df = pd.read_csv(stats_file)

        print(f"总记录数: {len(df)}")
        print(f"唯一视频数: {df['videoID'].nunique()}")
        print(f"唯一用户数: {df['userID'].nunique()}")

        # 2. 数据预处理
        print("\n数据预处理...")

        # 转换totalWatchDuration为秒数
        df['duration_seconds'] = df['totalWatchDuration'].apply(time_to_seconds)

        # 计算每次观看的平均时长（总时长/观看次数）
        df['avg_watch_seconds'] = df['duration_seconds'] / df['watchCount']

        # 3. 展开数据：根据watchCount复制记录
        print("展开观看记录...")

        expanded_records = []
        for _, row in df.iterrows():
            video_id = row['videoID']
            user_id = row['userID']
            avg_seconds = row['avg_watch_seconds']
            watch_count = int(row['watchCount'])

            # 为每次观看创建一条记录
            for _ in range(watch_count):
                expanded_records.append({
                    'videoID': video_id,
                    'userID': user_id,
                    'watch_seconds': avg_seconds
                })

        expanded_df = pd.DataFrame(expanded_records)
        print(f"展开后记录数: {len(expanded_df)}")
        print(f"模拟的总观看次数: {len(expanded_df)}")

        # 4. 按视频计算中位数观看时长
        print("\n计算每个视频的中位数观看时长...")

        video_stats = []

        for video_id, group in expanded_df.groupby('videoID'):
            # 获取所有观看时长的列表
            watch_times = group['watch_seconds'].values

            # 计算中位数
            median_seconds = np.median(watch_times)

            # 统计信息
            video_stats.append({
                'videoID': video_id,
                'median_watch_seconds': median_seconds,
                'median_watch_time': seconds_to_time(median_seconds),
                'total_watches': len(watch_times),
                'unique_users': group['userID'].nunique(),
                'min_watch_seconds': watch_times.min(),
                'max_watch_seconds': watch_times.max(),
                'mean_watch_seconds': watch_times.mean(),
                'std_watch_seconds': watch_times.std() if len(watch_times) > 1 else 0
            })

        # 5. 创建DataFrame
        stats_df = pd.DataFrame(video_stats)

        # 按videoID排序
        stats_df = stats_df.sort_values('videoID').reset_index(drop=True)

        # 6. 保存结果
        stats_df.to_csv(output_file, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   统计视频数: {len(stats_df)}")
        print(f"   保存到: {output_file}")

        # 7. 统计信息
        print(f"\n📊 整体统计信息:")

        # 中位数时长分布
        print(f"   中位数观看时长分布:")
        median_times = stats_df['median_watch_seconds']
        print(f"     最小值: {seconds_to_time(median_times.min())} ({median_times.min():.0f}秒)")
        print(f"     最大值: {seconds_to_time(median_times.max())} ({median_times.max():.0f}秒)")
        print(f"     平均值: {seconds_to_time(median_times.mean())} ({median_times.mean():.0f}秒)")
        print(f"     中位数: {seconds_to_time(median_times.median())} ({median_times.median():.0f}秒)")

        # 按时长分段统计
        print(f"\n   中位数时长分段统计:")
        bins = [0, 60, 300, 600, 1800, 3600, float('inf')]  # 0-1分,1-5分,5-10分,10-30分,30-60分,60+分
        labels = ['<1分钟', '1-5分钟', '5-10分钟', '10-30分钟', '30-60分钟', '>60分钟']

        stats_df['duration_category'] = pd.cut(stats_df['median_watch_seconds'], bins=bins, labels=labels)

        for category in labels:
            count = (stats_df['duration_category'] == category).sum()
            if count > 0:
                percentage = count / len(stats_df) * 100
                print(f"     {category}: {count} 个视频 ({percentage:.1f}%)")

        # 8. 最热门视频（观看次数最多）
        print(f"\n🏆 观看次数最多的视频 (前10名):")
        top_videos = stats_df.sort_values('total_watches', ascending=False).head(10)

        for i, row in top_videos.iterrows():
            median_time = seconds_to_time(row['median_watch_seconds'])
            print(f"   第{i + 1:2d}名: 视频{row['videoID']}")
            print(f"       中位数观看时长: {median_time}")
            print(f"       总观看次数: {row['total_watches']} 次")
            print(f"       唯一用户数: {row['unique_users']} 人")
            print(f"       平均观看时长: {seconds_to_time(row['mean_watch_seconds'])}")
            print()

        # 9. 显示前20条记录
        print(f"\n📋 数据示例 (前20条):")
        print("=" * 90)
        print(
            f"{'videoID':<8} {'中位数时长':<12} {'总观看次数':<12} {'唯一用户':<10} {'最短时长':<12} {'最长时长':<12}")
        print("-" * 90)

        for i, row in stats_df.head(20).iterrows():
            print(f"{row['videoID']:<8} {row['median_watch_time']:<12} {row['total_watches']:<12} "
                  f"{row['unique_users']:<10} {seconds_to_time(row['min_watch_seconds']):<12} "
                  f"{seconds_to_time(row['max_watch_seconds']):<12}")

        return stats_df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None


# 简化版本（只计算中位数）
def calculate_video_median_simple(stats_file, output_file='video_median_simple.csv'):
    """简化版本：只计算中位数"""

    df = pd.read_csv(stats_file)

    # 转换时长
    df['duration_seconds'] = df['totalWatchDuration'].apply(time_to_seconds)
    df['avg_watch_seconds'] = df['duration_seconds'] / df['watchCount']

    # 展开数据
    expanded_data = []
    for _, row in df.iterrows():
        for _ in range(int(row['watchCount'])):
            expanded_data.append({
                'videoID': row['videoID'],
                'watch_seconds': row['avg_watch_seconds']
            })

    expanded_df = pd.DataFrame(expanded_data)

    # 计算中位数
    result = expanded_df.groupby('videoID')['watch_seconds'].agg(
        median_seconds='median',
        total_watches='count',
        mean_seconds='mean',
        min_seconds='min',
        max_seconds='max'
    ).reset_index()

    # 格式化
    result['median_time'] = result['median_seconds'].apply(seconds_to_time)
    result['mean_time'] = result['mean_seconds'].apply(seconds_to_time)

    # 排序保存
    result = result.sort_values('videoID')
    result.to_csv(output_file, index=False)

    print(f"✅ 生成 {len(result)} 个视频的中位数统计")
    return result


# 主程序
if __name__ == "__main__":
    # 文件路径
    stats_file = 'user_video_stats.csv'

    print("🎬 计算视频中位数观看时长")
    print("=" * 50)

    # 选择版本
    print("选择版本:")
    print("1. 完整版 (带详细分析)")
    print("2. 简化版 (只计算中位数)")

    choice = input("请输入选择 (1-2): ").strip() or "1"

    if choice == "1":
        output_file = 'video_median_watch_time.csv'
        result = calculate_video_median_watch_time(stats_file, output_file)
    else:
        output_file = 'video_median_simple.csv'
        result = calculate_video_median_simple(stats_file, output_file)

    if result is not None:
        print(f"\n🎉 成功生成 {output_file}!")
        print(f"   统计了 {len(result)} 个视频的中位数观看时长")

        # 显示统计摘要
        print(f"\n📈 统计摘要:")

        # 中位数分布
        if 'median_seconds' in result.columns:
            median_col = 'median_seconds'
        else:
            median_col = 'median_watch_seconds'

        median_values = result[median_col]

        print(f"   中位数观看时长范围:")
        print(f"     最短: {seconds_to_time(median_values.min())}")
        print(f"     最长: {seconds_to_time(median_values.max())}")
        print(f"     平均: {seconds_to_time(median_values.mean())}")

        # 按时长分类
        print(f"\n   视频分布:")
        short_videos = len(result[median_values <= 300])  # 5分钟以内
        medium_videos = len(result[(median_values > 300) & (median_values <= 1800)])  # 5-30分钟
        long_videos = len(result[median_values > 1800])  # 30分钟以上

        total = len(result)
        print(f"     短视频 (<5分钟): {short_videos} 个 ({short_videos / total * 100:.1f}%)")
        print(f"     中视频 (5-30分钟): {medium_videos} 个 ({medium_videos / total * 100:.1f}%)")
        print(f"     长视频 (>30分钟): {long_videos} 个 ({long_videos / total * 100:.1f}%)")

        # 显示样本
        print(f"\n📋 样本数据:")
        print(result.head(10).to_string(index=False))