import pandas as pd
import numpy as np
import random


def time_to_seconds(time_str):
    """将HH:MM:SS格式的时间转换为秒数"""
    if pd.isna(time_str) or time_str == '':
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


def add_duration_column(videos_file, median_file, output_file='videos_with_duration.csv'):
    """
    给videos.csv添加duration列

    参数:
        videos_file: videos.csv文件路径
        median_file: video_median_simple.csv文件路径
        output_file: 输出文件路径
    """

    print("开始给videos.csv添加duration列...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        videos_df = pd.read_csv(videos_file)
        median_df = pd.read_csv(median_file)

        print(f"视频数据: {len(videos_df)} 条")
        print(f"中位数数据: {len(median_df)} 条")

        # 2. 创建视频ID到中位数时长的映射
        print("\n创建视频时长映射...")

        # 确保列名正确
        median_columns = list(median_df.columns)
        print(f"中位数数据列: {median_columns}")

        # 查找中位数时间列
        time_column = None
        for col in ['median_time', 'medianTime', 'median_watch_time']:
            if col in median_df.columns:
                time_column = col
                break

        if time_column is None:
            # 尝试查找包含'time'的列
            time_cols = [col for col in median_df.columns if 'time' in col.lower()]
            if time_cols:
                time_column = time_cols[0]
            else:
                print("❌ 错误: 找不到中位数时间列")
                return None

        print(f"使用列 '{time_column}' 作为中位数时间")

        # 创建映射字典
        median_dict = dict(zip(median_df['videoID'], median_df[time_column]))

        # 3. 为每个视频添加duration列
        print("\n添加duration列...")

        durations = []
        zero_count = 0
        random_count = 0
        mapped_count = 0

        # 设置随机种子确保可重复性
        random.seed(42)

        for video_id in videos_df['videoID']:
            if video_id in median_dict:
                median_time = median_dict[video_id]

                # 转换中位数时长为秒数
                median_seconds = time_to_seconds(median_time)

                if median_seconds == 0:
                    # 如果中位数时长为0，在0-1小时内随机生成
                    random_seconds = random.randint(1, 3600)  # 1秒到1小时
                    duration = seconds_to_time(random_seconds)
                    zero_count += 1
                    random_count += 1
                else:
                    # 使用中位数时长
                    duration = median_time
                    mapped_count += 1

                durations.append(duration)
            else:
                # 视频不在中位数数据中，随机生成0-1小时
                random_seconds = random.randint(1, 3600)
                duration = seconds_to_time(random_seconds)
                random_count += 1
                durations.append(duration)

        # 4. 添加duration列到videos_df
        videos_df['duration'] = durations

        # 调整列顺序（把duration放在合适的位置）
        # 通常duration可以放在publishTime之后
        cols = list(videos_df.columns)

        # 如果publishTime在列中，把duration放在它后面
        if 'publishTime' in cols:
            publish_index = cols.index('publishTime')
            # 移除duration列
            cols.remove('duration')
            # 在publishTime后插入duration
            cols.insert(publish_index + 1, 'duration')
            videos_df = videos_df[cols]

        # 5. 保存结果
        videos_df.to_csv(output_file, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   处理视频数: {len(videos_df)}")
        print(f"   保存到: {output_file}")

        # 6. 统计信息
        print(f"\n📊 统计信息:")
        print(f"   成功映射: {mapped_count} 个视频")
        print(f"   中位数为0的: {zero_count} 个视频")
        print(f"   随机生成的: {random_count} 个视频")

        # 7. 时长分布统计
        print(f"\n💹 duration列统计:")

        # 转换所有duration为秒数
        videos_df['duration_seconds'] = videos_df['duration'].apply(time_to_seconds)

        durations_sec = videos_df['duration_seconds']

        print(f"   时长范围:")
        print(f"     最短: {seconds_to_time(durations_sec.min())} ({durations_sec.min()}秒)")
        print(f"     最长: {seconds_to_time(durations_sec.max())} ({durations_sec.max()}秒)")
        print(f"     平均: {seconds_to_time(durations_sec.mean())} ({durations_sec.mean():.0f}秒)")
        print(f"     中位数: {seconds_to_time(durations_sec.median())} ({durations_sec.median():.0f}秒)")

        # 按时长分段统计
        print(f"\n   时长分段统计:")
        bins = [0, 60, 300, 600, 1800, 3600, float('inf')]
        labels = ['<1分钟', '1-5分钟', '5-10分钟', '10-30分钟', '30-60分钟', '>60分钟']

        videos_df['duration_category'] = pd.cut(videos_df['duration_seconds'], bins=bins, labels=labels)

        for category in labels:
            count = (videos_df['duration_category'] == category).sum()
            if count > 0:
                percentage = count / len(videos_df) * 100
                print(f"     {category}: {count} 个视频 ({percentage:.1f}%)")

        # 8. 显示前10条记录
        print(f"\n📋 添加duration后的数据示例 (前10条):")
        print("=" * 100)
        print(
            f"{'videoID':<8} {'userID':<8} {'title':<25} {'category':<8} {'theme':<8} {'publishTime':<12} {'duration':<10}")
        print("-" * 100)

        for i, row in videos_df.head(10).iterrows():
            title_short = row['title'][:23] + "..." if len(row['title']) > 23 else row['title']
            print(f"{row['videoID']:<8} {row.get('userID', 'N/A'):<8} {title_short:<25} "
                  f"{row['categoryID']:<8} {row['themeID']:<8} {row['publishTime']:<12} {row['duration']:<10}")

        # 9. 按时长排序显示
        print(f"\n⏱️  时长最长的视频 (前5名):")
        longest = videos_df.sort_values('duration_seconds', ascending=False).head(5)
        for i, row in longest.iterrows():
            print(f"   视频{row['videoID']}: {row['title'][:30]}... - {row['duration']}")

        print(f"\n⏱️  时长最短的视频 (前5名):")
        shortest = videos_df.sort_values('duration_seconds', ascending=True).head(5)
        for i, row in shortest.iterrows():
            print(f"   视频{row['videoID']}: {row['title'][:30]}... - {row['duration']}")

        return videos_df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None


# 更简单的版本
def add_duration_simple(videos_file, median_file, output_file='videos_with_duration.csv'):
    """简化版本"""

    videos = pd.read_csv(videos_file)
    median = pd.read_csv(median_file)

    # 创建映射
    median_map = {}
    for _, row in median.iterrows():
        video_id = row['videoID']
        # 查找时间列
        for col in row.index:
            if 'time' in col.lower() and col != 'videoID':
                median_map[video_id] = row[col]
                break

    # 添加duration列
    durations = []
    for video_id in videos['videoID']:
        if video_id in median_map:
            median_time = median_map[video_id]
            # 检查是否为0或空
            if pd.isna(median_time) or str(median_time).strip() in ['0', '00:00:00', '0:00:00']:
                # 随机生成1秒到1小时
                random_seconds = random.randint(1, 3600)
                h = random_seconds // 3600
                m = (random_seconds % 3600) // 60
                s = random_seconds % 60
                durations.append(f"{h:02d}:{m:02d}:{s:02d}")
            else:
                durations.append(str(median_time))
        else:
            # 随机生成
            random_seconds = random.randint(1, 3600)
            h = random_seconds // 3600
            m = (random_seconds % 3600) // 60
            s = random_seconds % 60
            durations.append(f"{h:02d}:{m:02d}:{s:02d}")

    videos['duration'] = durations

    # 保存
    videos.to_csv(output_file, index=False)

    print(f"✅ 添加完成！保存到 {output_file}")
    print(f"   视频数: {len(videos)}")
    print(f"   时长范围示例: {durations[:5]}")

    return videos


# 批量处理版本
def batch_add_duration(videos_files, median_file, output_dir='./'):
    """批量处理多个videos文件"""
    import glob
    import os

    if isinstance(videos_files, str):
        files = glob.glob(videos_files)
    else:
        files = videos_files

    print(f"找到 {len(files)} 个videos文件")

    for file in files:
        print(f"\n处理: {os.path.basename(file)}")
        base_name = os.path.splitext(os.path.basename(file))[0]
        output_file = os.path.join(output_dir, f"{base_name}_with_duration.csv")
        add_duration_column(file, median_file, output_file)


# 主程序
if __name__ == "__main__":
    # 文件路径
    videos_file = 'videoswithoutduration.csv'
    median_file = 'video_median_simple.csv'

    print("🎬 给videos.csv添加duration列")
    print("=" * 50)

    # 选择版本
    print("选择版本:")
    print("1. 完整版 (带详细统计)")
    print("2. 简单版 (快速添加)")
    print("3. 自定义输出文件")

    choice = input("请输入选择 (1-3): ").strip() or "1"

    if choice == "1":
        output_file = 'videos_with_duration.csv'
        result = add_duration_column(videos_file, median_file, output_file)

    elif choice == "2":
        output_file = 'videos.csv'
        result = add_duration_simple(videos_file, median_file, output_file)

    elif choice == "3":
        output_file = input("请输入输出文件名: ").strip()
        if not output_file:
            output_file = 'videos_with_duration.csv'
        result = add_duration_column(videos_file, median_file, output_file)

    else:
        output_file = 'videos_with_duration.csv'
        result = add_duration_column(videos_file, median_file, output_file)

    if result is not None:
        print(f"\n🎉 成功生成 {output_file}!")
        print(f"   视频总数: {len(result)}")
        print(f"   新增duration列示例: {result['duration'].iloc[:5].tolist()}")

        # 显示文件格式
        print(f"\n📋 最终文件格式:")
        print(result.head().to_string(index=False))