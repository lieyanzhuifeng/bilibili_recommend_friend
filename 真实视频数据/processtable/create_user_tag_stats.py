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


def generate_user_tag_stats(stats_file, tag_file, output_file='user_tag_stats.csv'):
    """
    生成user_tag_stats表数据

    参数:
        stats_file: user_video_stats.csv文件路径
        tag_file: tag_video.csv文件路径
        output_file: 输出文件路径
    """

    print("开始生成user_tag_stats表数据...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        stats_df = pd.read_csv(stats_file)
        tag_df = pd.read_csv(tag_file)

        print(
            f"观看统计: {len(stats_df)} 条 (用户: {stats_df['userID'].nunique()}, 视频: {stats_df['videoID'].nunique()})")
        print(f"标签关系: {len(tag_df)} 条 (视频: {tag_df['videoID'].nunique()}, 标签: {tag_df['tagID'].nunique()})")

        # 2. 数据预处理
        print("\n数据预处理...")

        # 转换观看时长为秒数
        stats_df['duration_seconds'] = stats_df['totalWatchDuration'].apply(time_to_seconds)

        # 检查数据
        print(f"总观看时长: {stats_df['duration_seconds'].sum() / 3600:.1f} 小时")
        print(f"平均每次观看时长: {stats_df['duration_seconds'].mean() / 60:.1f} 分钟")

        # 3. 关联数据
        print("\n关联用户观看数据和标签...")

        # 合并数据
        merged_df = pd.merge(stats_df, tag_df, on='videoID', how='inner')

        print(f"匹配到的记录: {len(merged_df)} 条")
        print(f"涉及用户: {merged_df['userID'].nunique()}")
        print(f"涉及标签: {merged_df['tagID'].nunique()}")

        if len(merged_df) == 0:
            print("❌ 错误: 没有匹配到标签数据")
            return None

        # 4. 按用户和标签分组统计
        print("\n按用户和标签分组统计...")

        user_tag_stats = []

        # 使用groupby高效计算
        for (user_id, tag_id), group in merged_df.groupby(['userID', 'tagID']):
            # 总观看时长（秒）
            total_seconds = group['duration_seconds'].sum()

            # 唯一视频数
            unique_videos = group['videoID'].nunique()

            # 总观看次数
            total_watches = group['watchCount'].sum()

            user_tag_stats.append({
                'userID': user_id,
                'tagID': tag_id,
                'totalWatchDuration': int(total_seconds),  # 转换为整数秒
                'uniqueVideos': int(unique_videos),
                'totalWatches': int(total_watches)  # 额外信息，不输出
            })

        # 5. 创建DataFrame
        tag_stats_df = pd.DataFrame(user_tag_stats)

        # 添加自增主键
        tag_stats_df.insert(0, 'tagStatID', range(1, len(tag_stats_df) + 1))

        # 按userID和tagID排序
        tag_stats_df = tag_stats_df.sort_values(['userID', 'tagID']).reset_index(drop=True)

        # 只保留需要的列
        final_df = tag_stats_df[['tagStatID', 'userID', 'tagID', 'totalWatchDuration', 'uniqueVideos']]

        # 6. 保存结果
        final_df.to_csv(output_file, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   生成记录数: {len(final_df)}")
        print(f"   保存到: {output_file}")

        # 7. 统计信息
        print(f"\n📊 统计信息:")
        print(f"   用户-标签关系总数: {len(final_df)}")
        print(f"   覆盖用户数: {final_df['userID'].nunique()}")
        print(f"   覆盖标签数: {final_df['tagID'].nunique()}")

        # 平均每个用户的标签数
        avg_tags_per_user = final_df.groupby('userID')['tagID'].count().mean()
        print(f"   平均每个用户的标签数: {avg_tags_per_user:.1f}")

        # 平均每个标签的用户数
        avg_users_per_tag = final_df.groupby('tagID')['userID'].count().mean()
        print(f"   平均每个标签的用户数: {avg_users_per_tag:.1f}")

        # 8. 时长和视频数分布
        print(f"\n💹 数据分布:")

        print(f"   总观看时长统计:")
        total_hours = final_df['totalWatchDuration'].sum() / 3600
        avg_seconds = final_df['totalWatchDuration'].mean()
        median_seconds = final_df['totalWatchDuration'].median()

        print(f"     总时长: {total_hours:.1f} 小时")
        print(f"     平均每个用户-标签组合: {avg_seconds / 60:.1f} 分钟")
        print(f"     中位数: {median_seconds / 60:.1f} 分钟")

        print(f"\n   唯一视频数统计:")
        print(f"     平均每个用户观看每个标签的视频数: {final_df['uniqueVideos'].mean():.1f}")
        print(f"     最大值: {final_df['uniqueVideos'].max()}")
        print(f"     最小值: {final_df['uniqueVideos'].min()}")

        # 9. 热门标签（被最多用户观看）
        print(f"\n🏆 最受欢迎的标签 (按用户数排名前10):")
        tag_popularity = final_df.groupby('tagID').agg({
            'userID': 'nunique',
            'totalWatchDuration': 'sum',
            'uniqueVideos': 'sum'
        }).sort_values('userID', ascending=False).head(10)

        for i, (tag_id, data) in enumerate(tag_popularity.iterrows(), 1):
            users = int(data['userID'])
            total_hours = data['totalWatchDuration'] / 3600
            avg_videos = data['uniqueVideos'] / users if users > 0 else 0

            print(f"   第{i:2d}名: 标签{tag_id}")
            print(f"       观看用户数: {users} 人")
            print(f"       总被观看时长: {total_hours:.1f} 小时")
            print(f"       平均每个用户观看视频数: {avg_videos:.1f} 部")
            print()

        # 10. 最活跃的用户（观看最多标签）
        print(f"\n👑 最活跃的用户 (按标签数排名前10):")
        user_activity = final_df.groupby('userID').agg({
            'tagID': 'nunique',
            'totalWatchDuration': 'sum',
            'uniqueVideos': 'sum'
        }).sort_values('tagID', ascending=False).head(10)

        for i, (user_id, data) in enumerate(user_activity.iterrows(), 1):
            tags = int(data['tagID'])
            total_hours = data['totalWatchDuration'] / 3600
            total_videos = int(data['uniqueVideos'])

            print(f"   第{i:2d}名: 用户{user_id}")
            print(f"       观看标签数: {tags} 个")
            print(f"       总观看时长: {total_hours:.1f} 小时")
            print(f"       观看的唯一视频数: {total_videos} 部")
            print()

        # 11. 显示前20条记录
        print(f"\n📋 数据示例 (前20条):")
        print("=" * 70)
        print(f"{'tagStatID':<10} {'userID':<8} {'tagID':<8} {'总时长(秒)':<12} {'唯一视频数':<12}")
        print("-" * 70)

        for i, row in final_df.head(20).iterrows():
            print(
                f"{row['tagStatID']:<10} {row['userID']:<8} {row['tagID']:<8} {row['totalWatchDuration']:<12} {row['uniqueVideos']:<12}")

        # 12. 用户标签偏好分析
        print(f"\n🎯 用户标签偏好示例 (前5个用户):")
        print("-" * 60)

        sample_users = final_df['userID'].unique()[:5]
        for user_id in sample_users:
            user_tags = final_df[final_df['userID'] == user_id]

            if not user_tags.empty:
                print(f"\n用户 {user_id}:")
                print(f"  共观看 {len(user_tags)} 个标签的视频")

                # 按观看时长排序
                top_tags = user_tags.sort_values('totalWatchDuration', ascending=False).head(3)

                print(f"  最常看的3个标签:")
                for j, (_, tag_row) in enumerate(top_tags.iterrows(), 1):
                    hours = tag_row['totalWatchDuration'] / 3600
                    print(f"    第{j}名: 标签{tag_row['tagID']} - {hours:.2f}小时, {tag_row['uniqueVideos']}个视频")

        return final_df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None


# 更简洁的版本
def generate_user_tag_stats_simple(stats_file, tag_file, output_file='user_tag_stats.csv'):
    """简洁版本"""

    print("生成用户标签统计 (简洁版)...")

    # 读取数据
    stats = pd.read_csv(stats_file)
    tags = pd.read_csv(tag_file)

    # 转换时长
    def to_seconds(t):
        if isinstance(t, str):
            try:
                h, m, s = map(int, t.split(':'))
                return h * 3600 + m * 60 + s
            except:
                return 0
        return int(t)

    stats['duration_sec'] = stats['totalWatchDuration'].apply(to_seconds)

    # 合并数据
    merged = pd.merge(stats, tags, on='videoID')

    # 分组统计
    grouped = merged.groupby(['userID', 'tagID']).agg(
        total_duration=('duration_sec', 'sum'),
        unique_videos=('videoID', 'nunique')
    ).reset_index()

    # 添加主键
    grouped.insert(0, 'tagStatID', range(1, len(grouped) + 1))

    # 重命名和排序
    result = grouped.rename(columns={
        'total_duration': 'totalWatchDuration',
        'unique_videos': 'uniqueVideos'
    })

    result = result.sort_values(['userID', 'tagID'])

    # 保存
    result.to_csv(output_file, index=False)

    print(f"✅ 生成 {len(result)} 条记录")
    print(f"📊 统计: {result['userID'].nunique()} 用户, {result['tagID'].nunique()} 标签")

    return result


# 批量处理版本
def batch_generate_tag_stats(stats_files, tag_file, output_dir='./'):
    """批量处理多个统计文件"""
    import glob
    import os

    if isinstance(stats_files, str):
        files = glob.glob(stats_files)
    else:
        files = stats_files

    print(f"找到 {len(files)} 个统计文件")

    for file in files:
        print(f"\n处理: {os.path.basename(file)}")
        base_name = os.path.splitext(os.path.basename(file))[0]
        output_file = os.path.join(output_dir, f"{base_name}_tag_stats.csv")
        generate_user_tag_stats(file, tag_file, output_file)


# 主程序
if __name__ == "__main__":
    # 文件路径
    stats_file = 'user_video_stats.csv'
    tag_file = 'tag_video.csv'

    print("🎯 生成 user_tag_stats 表数据")
    print("=" * 50)

    # 选择版本
    print("选择版本:")
    print("1. 完整版 (带详细分析)")
    print("2. 简洁版 (快速生成)")

    choice = input("请输入选择 (1-2): ").strip() or "1"

    if choice == "1":
        output_file = 'user_tag_stats.csv'
        result = generate_user_tag_stats(stats_file, tag_file, output_file)
    else:
        output_file = 'user_tag_stats.csv'
        result = generate_user_tag_stats_simple(stats_file, tag_file, output_file)

    if result is not None:
        print(f"\n🎉 成功生成 {output_file}!")
        print(f"   文件包含 {len(result)} 条用户-标签关系")

        # 显示统计摘要
        print(f"\n📈 统计摘要:")
        print(f"   用户数: {result['userID'].nunique()}")
        print(f"   标签数: {result['tagID'].nunique()}")
        print(f"   总观看时长: {result['totalWatchDuration'].sum() / 3600:.1f} 小时")

        # 显示样本
        print(f"\n📋 样本数据:")
        print(result.head(10).to_string(index=False))