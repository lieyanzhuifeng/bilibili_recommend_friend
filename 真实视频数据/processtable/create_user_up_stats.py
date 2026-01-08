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


def generate_user_up_stats(videos_file, stats_file, output_file='user_up_stats.csv'):
    """
    生成user_up_stats表数据

    参数:
        videos_file: videos.csv文件路径
        stats_file: user_video_stats.csv文件路径
        output_file: 输出文件路径
    """

    print("开始生成user_up_stats表数据...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        videos_df = pd.read_csv(videos_file)
        stats_df = pd.read_csv(stats_file)

        print(f"视频数据: {len(videos_df)} 条 (包含 {videos_df['userID'].nunique()} 个UP主)")
        print(f"观看统计: {len(stats_df)} 条 (包含 {stats_df['userID'].nunique()} 个观看者)")

        # 2. 数据预处理
        print("\n数据预处理...")

        # 创建视频到UP主的映射
        video_to_up = dict(zip(videos_df['videoID'], videos_df['userID']))

        # 转换观看时长为秒数
        stats_df['duration_seconds'] = stats_df['totalWatchDuration'].apply(time_to_seconds)

        # 3. 关联数据并计算UP主观看统计
        print("计算用户-UP主观看关系...")

        # 添加UP主ID列
        stats_df['upID'] = stats_df['videoID'].map(video_to_up)

        # 检查是否有找不到UP主的视频
        missing_up = stats_df['upID'].isna().sum()
        if missing_up > 0:
            print(f"⚠ 警告: {missing_up} 条记录找不到对应的UP主")
            # 删除这些记录
            stats_df = stats_df.dropna(subset=['upID'])

        # 4. 按用户和UP主分组统计
        user_up_stats = []

        # 使用groupby进行高效计算
        for (user_id, up_id), group in stats_df.groupby(['userID', 'upID']):
            # 总观看时长（秒）
            total_seconds = group['duration_seconds'].sum()

            # 唯一视频数
            unique_videos = group['videoID'].nunique()

            # 总观看次数（可选）
            total_watch_count = group['watchCount'].sum()

            user_up_stats.append({
                'userID': user_id,
                'upID': up_id,
                'totalWatchDuration': seconds_to_time(total_seconds),
                'uniqueVideos': unique_videos,
                'totalWatchCount': total_watch_count  # 额外信息，不输出
            })

        # 5. 创建DataFrame
        up_stats_df = pd.DataFrame(user_up_stats)

        # 添加自增主键
        up_stats_df.insert(0, 'upStatID', range(1, len(up_stats_df) + 1))

        # 按userID和upID排序
        up_stats_df = up_stats_df.sort_values(['userID', 'upID']).reset_index(drop=True)

        # 只保留需要的列
        final_df = up_stats_df[['upStatID', 'userID', 'upID', 'totalWatchDuration', 'uniqueVideos']]

        # 6. 保存结果
        final_df.to_csv(output_file, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   生成记录数: {len(final_df)}")
        print(f"   保存到: {output_file}")

        # 7. 统计信息
        print(f"\n📊 统计信息:")
        print(f"   观看关系总数: {len(final_df)}")
        print(f"   唯一观看者数: {final_df['userID'].nunique()}")
        print(f"   唯一UP主数: {final_df['upID'].nunique()}")

        # 平均每个用户关注的UP主数
        avg_up_per_user = final_df.groupby('userID')['upID'].count().mean()
        print(f"   平均每个用户关注的UP主数: {avg_up_per_user:.1f}")

        # 平均每个UP主的粉丝数（被多少用户观看）
        avg_fans_per_up = final_df.groupby('upID')['userID'].count().mean()
        print(f"   平均每个UP主的粉丝数: {avg_fans_per_up:.1f}")

        # 8. 时长和视频数分布
        print(f"\n💹 数据分布:")

        # 转换回秒数以计算统计
        final_df['duration_seconds'] = final_df['totalWatchDuration'].apply(time_to_seconds)

        print(f"   总观看时长统计:")
        print(f"     总时长: {final_df['duration_seconds'].sum() / 3600:.1f} 小时")
        print(f"     平均时长: {final_df['duration_seconds'].mean() / 60:.1f} 分钟")
        print(f"     中位数: {final_df['duration_seconds'].median() / 60:.1f} 分钟")

        print(f"\n   唯一视频数统计:")
        print(f"     平均每个用户观看每个UP主的视频数: {final_df['uniqueVideos'].mean():.1f}")
        print(f"     最大值: {final_df['uniqueVideos'].max()}")
        print(f"     最小值: {final_df['uniqueVideos'].min()}")

        # 9. 热门UP主（被最多用户观看）
        print(f"\n🏆 最受欢迎的UP主 (前5名):")
        top_ups = final_df.groupby('upID').agg({
            'userID': 'count',
            'duration_seconds': 'sum',
            'uniqueVideos': 'mean'
        }).sort_values('userID', ascending=False).head(5)

        for i, (up_id, data) in enumerate(top_ups.iterrows(), 1):
            fans = int(data['userID'])
            total_hours = data['duration_seconds'] / 3600
            avg_videos = data['uniqueVideos']
            print(f"   第{i}名: UP主{up_id}")
            print(f"       粉丝数: {fans} 人")
            print(f"       总被观看时长: {total_hours:.1f} 小时")
            print(f"       平均每个粉丝观看视频数: {avg_videos:.1f} 部")

        # 10. 最活跃的用户（观看最多UP主）
        print(f"\n👑 最活跃的用户 (前5名):")
        top_users = final_df.groupby('userID').agg({
            'upID': 'count',
            'duration_seconds': 'sum',
            'uniqueVideos': 'sum'
        }).sort_values('upID', ascending=False).head(5)

        for i, (user_id, data) in enumerate(top_users.iterrows(), 1):
            up_count = int(data['upID'])
            total_hours = data['duration_seconds'] / 3600
            total_videos = int(data['uniqueVideos'])
            print(f"   第{i}名: 用户{user_id}")
            print(f"       关注的UP主数: {up_count} 人")
            print(f"       总观看时长: {total_hours:.1f} 小时")
            print(f"       观看的唯一视频数: {total_videos} 部")

        # 11. 显示前10条记录
        print(f"\n📋 数据示例 (前10条):")
        print("=" * 70)
        print(f"{'upStatID':<8} {'userID':<8} {'upID':<8} {'totalWatchDuration':<15} {'uniqueVideos':<12}")
        print("-" * 70)

        for i, row in final_df.head(10).iterrows():
            print(
                f"{row['upStatID']:<8} {row['userID']:<8} {row['upID']:<8} {row['totalWatchDuration']:<15} {row['uniqueVideos']:<12}")

        return final_df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None


# 更简洁的版本
def generate_user_up_stats_simple(videos_file, stats_file, output_file='user_up_stats.csv'):
    """简洁版本"""

    # 读取数据
    videos = pd.read_csv(videos_file)
    stats = pd.read_csv(stats_file)

    # 创建视频-UP映射
    video_up_map = dict(zip(videos['videoID'], videos['userID']))

    # 添加UP主ID
    stats['upID'] = stats['videoID'].map(video_up_map)

    # 删除没有UP主的记录
    stats = stats.dropna(subset=['upID'])

    # 转换时长
    def to_seconds(t):
        if isinstance(t, str):
            h, m, s = map(int, t.split(':'))
            return h * 3600 + m * 60 + s
        return 0

    stats['duration_sec'] = stats['totalWatchDuration'].apply(to_seconds)

    # 分组统计
    grouped = stats.groupby(['userID', 'upID']).agg(
        total_duration=('duration_sec', 'sum'),
        unique_videos=('videoID', 'nunique')
    ).reset_index()

    # 格式化时长
    def format_duration(sec):
        h = sec // 3600
        m = (sec % 3600) // 60
        s = sec % 60
        return f"{int(h):02d}:{int(m):02d}:{int(s):02d}"

    grouped['totalWatchDuration'] = grouped['total_duration'].apply(format_duration)

    # 添加主键
    grouped.insert(0, 'upStatID', range(1, len(grouped) + 1))

    # 重命名和排序
    result = grouped[['upStatID', 'userID', 'upID', 'totalWatchDuration', 'unique_videos']]
    result = result.rename(columns={'unique_videos': 'uniqueVideos'})
    result = result.sort_values(['userID', 'upID'])

    # 保存
    result.to_csv(output_file, index=False)

    print(f"✅ 生成 {len(result)} 条记录")
    print(f"📊 统计: {result['userID'].nunique()} 用户, {result['upID'].nunique()} UP主")

    return result


# 主程序
if __name__ == "__main__":
    # 文件路径
    videos_file = 'videoswithoutduration.csv'
    stats_file = 'user_video_stats.csv'

    print("🎯 生成 user_up_stats 表数据")
    print("=" * 50)

    # 选择版本
    print("选择版本:")
    print("1. 完整版 (带详细分析)")
    print("2. 简洁版 (只生成数据)")

    choice = input("请输入选择 (1-2): ").strip() or "1"

    if choice == "1":
        output_file = 'user_up_stats.csv'
        result = generate_user_up_stats(videos_file, stats_file, output_file)
    else:
        output_file = 'user_up_stats_simple.csv'
        result = generate_user_up_stats_simple(videos_file, stats_file, output_file)

    if result is not None:
        print(f"\n🎉 成功生成 {output_file}!")
        print(f"   文件包含 {len(result)} 条用户-UP主关系")

        # 显示样本
        print(f"\n📋 样本数据:")
        print(result.head(10).to_string(index=False))