import pandas as pd
import numpy as np
from collections import defaultdict


def time_to_seconds(time_str):
    """将HH:MM:SS格式的时间转换为秒数"""
    if pd.isna(time_str):
        return 0

    try:
        # 处理可能的格式
        if isinstance(time_str, str):
            parts = time_str.split(':')
            if len(parts) == 3:  # HH:MM:SS
                hours, minutes, seconds = parts
                return int(hours) * 3600 + int(minutes) * 60 + int(seconds)
            elif len(parts) == 2:  # MM:SS
                minutes, seconds = parts
                return int(minutes) * 60 + int(seconds)
            else:
                return int(time_str)  # 直接是秒数
        else:
            return int(time_str)
    except:
        return 0


def calculate_user_preferences(videos_file, stats_file,
                               categories_output='user_top_categories.csv',
                               themes_output='user_top_themes.csv'):
    """
    根据观看时长计算用户偏好分类

    参数:
        videos_file: videos.csv文件路径
        stats_file: user_video_stats.csv文件路径
        categories_output: 分区偏好输出文件
        themes_output: 主题偏好输出文件
    """

    print("开始计算用户偏好分类...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        videos_df = pd.read_csv(videos_file)
        stats_df = pd.read_csv(stats_file)

        print(f"视频数据: {len(videos_df)} 条")
        print(f"观看统计: {len(stats_df)} 条")
        print(f"唯一用户数: {stats_df['userID'].nunique()}")
        print(f"唯一视频数: {stats_df['videoID'].nunique()}")

        # 2. 创建视频信息字典
        video_info = {}
        for _, row in videos_df.iterrows():
            video_info[row['videoID']] = {
                'categoryID': row['categoryID'],
                'themeID': row['themeID']
            }

        # 3. 转换观看时长为秒数
        print("\n转换观看时长格式...")
        stats_df['duration_seconds'] = stats_df['totalWatchDuration'].apply(time_to_seconds)

        # 检查时长转换
        print(f"总观看时长: {stats_df['duration_seconds'].sum() / 3600:.1f} 小时")
        print(f"平均每次观看时长: {stats_df['duration_seconds'].mean() / 60:.1f} 分钟")

        # 4. 统计每个用户的分类观看时长
        print("\n统计用户分类观看时长...")

        # 初始化统计字典
        user_category_duration = defaultdict(lambda: defaultdict(float))  # userID -> categoryID -> 秒数
        user_theme_duration = defaultdict(lambda: defaultdict(float))  # userID -> themeID -> 秒数
        user_total_duration = defaultdict(float)  # userID -> 总秒数

        for _, row in stats_df.iterrows():
            user_id = row['userID']
            video_id = row['videoID']
            duration_seconds = row['duration_seconds']

            # 获取视频的分类信息
            if video_id in video_info:
                video_data = video_info[video_id]
                category_id = video_data['categoryID']
                theme_id = video_data['themeID']

                # 累加时长
                user_category_duration[user_id][category_id] += duration_seconds
                user_theme_duration[user_id][theme_id] += duration_seconds
                user_total_duration[user_id] += duration_seconds
            else:
                print(f"警告: 视频{video_id}在videos.csv中找不到")

        print(f"统计完成，处理了 {len(user_total_duration)} 个用户")

        # 5. 计算用户偏好并选择前3高
        print("\n计算用户偏好比例...")

        categories_data = []  # 存储user_top_categories数据
        themes_data = []  # 存储user_top_themes数据

        users_with_insufficient_data = []

        for user_id in user_total_duration:
            total_duration = user_total_duration[user_id]

            # 计算category偏好
            category_items = []
            for cat_id, duration in user_category_duration[user_id].items():
                proportion = round(duration / total_duration, 4)  # 保留4位小数
                category_items.append((cat_id, proportion, duration))

            # 按时长降序排序，取前3
            category_items.sort(key=lambda x: x[2], reverse=True)  # 按时长排序
            top_categories = category_items[:3]

            # 计算theme偏好
            theme_items = []
            for theme_id, duration in user_theme_duration[user_id].items():
                proportion = round(duration / total_duration, 4)
                theme_items.append((theme_id, proportion, duration))

            # 按时长降序排序，取前3
            theme_items.sort(key=lambda x: x[2], reverse=True)
            top_themes = theme_items[:3]

            # 记录数据不足的用户
            if len(category_items) < 3 or len(theme_items) < 3:
                users_with_insufficient_data.append(user_id)

            # 添加到结果
            for cat_id, proportion, _ in top_categories:
                categories_data.append({
                    'userID': user_id,
                    'categoryID': cat_id,
                    'proportion': proportion
                })

            for theme_id, proportion, _ in top_themes:
                themes_data.append({
                    'userID': user_id,
                    'themeID': theme_id,
                    'proportion': proportion
                })

        # 6. 创建DataFrame并保存
        print("\n保存结果...")

        categories_df = pd.DataFrame(categories_data)
        themes_df = pd.DataFrame(themes_data)

        # 排序
        categories_df = categories_df.sort_values(['userID', 'proportion'], ascending=[True, False])
        themes_df = themes_df.sort_values(['userID', 'proportion'], ascending=[True, False])

        # 保存
        categories_df.to_csv(categories_output, index=False)
        themes_df.to_csv(themes_output, index=False)

        print(f"✅ 处理完成!")
        print(f"   分区偏好: {len(categories_df)} 条记录 -> {categories_output}")
        print(f"   主题偏好: {len(themes_df)} 条记录 -> {themes_output}")

        # 7. 统计信息
        print(f"\n📊 统计信息:")

        # 用户覆盖率
        total_users = len(user_total_duration)
        users_with_categories = categories_df['userID'].nunique()
        users_with_themes = themes_df['userID'].nunique()

        print(f"   总用户数: {total_users}")
        print(f"   有分区偏好的用户: {users_with_categories} ({users_with_categories / total_users * 100:.1f}%)")
        print(f"   有主题偏好的用户: {users_with_themes} ({users_with_themes / total_users * 100:.1f}%)")

        if users_with_insufficient_data:
            print(f"   数据不足的用户数: {len(users_with_insufficient_data)} (分类数或主题数少于3个)")

        # 比例分布统计
        print(f"\n💹 比例分布统计:")

        def print_proportion_stats(df, col_name, pref_type):
            proportions = df['proportion'].values
            print(f"   {pref_type}:")
            print(f"     最小值: {proportions.min():.4f}")
            print(f"     最大值: {proportions.max():.4f}")
            print(f"     平均值: {proportions.mean():.4f}")
            print(f"     中位数: {np.median(proportions):.4f}")

            # 比例范围统计
            ranges = [(0, 0.2), (0.2, 0.4), (0.4, 0.6), (0.6, 0.8), (0.8, 1.0)]
            for low, high in ranges:
                count = ((proportions >= low) & (proportions < high)).sum()
                if high < 1.0:
                    print(f"     {low:.1f}-{high:.1f}: {count} 条 ({count / len(proportions) * 100:.1f}%)")
                else:
                    print(f"     {low:.1f}-{high:.1f}: {count} 条 ({count / len(proportions) * 100:.1f}%)")

        print_proportion_stats(categories_df, 'categoryID', '分区偏好比例')
        print_proportion_stats(themes_df, 'themeID', '主题偏好比例')

        # 最常见的偏好分类
        print(f"\n🏆 最受欢迎的分区 (前5):")
        top_categories = categories_df['categoryID'].value_counts().head()
        for cat_id, count in top_categories.items():
            percentage = count / len(categories_df) * 100
            print(f"   分区{cat_id}: {count} 次 ({percentage:.1f}%)")

        print(f"\n🏆 最受欢迎的主题 (前5):")
        top_themes = themes_df['themeID'].value_counts().head()
        for theme_id, count in top_themes.items():
            percentage = count / len(themes_df) * 100
            print(f"   主题{theme_id}: {count} 次 ({percentage:.1f}%)")

        # 8. 显示前10条记录
        print(f"\n📋 数据示例 (前5个用户的偏好):")
        print("=" * 70)

        unique_users = sorted(categories_df['userID'].unique())[:5]
        for user_id in unique_users:
            user_cats = categories_df[categories_df['userID'] == user_id]
            user_themes = themes_df[themes_df['userID'] == user_id]

            print(f"\n用户 {user_id}:")
            print(f"  分区偏好:")
            for _, row in user_cats.iterrows():
                print(f"    分区{row['categoryID']}: {row['proportion']:.4f}")

            print(f"  主题偏好:")
            for _, row in user_themes.iterrows():
                print(f"    主题{row['themeID']}: {row['proportion']:.4f}")

        return categories_df, themes_df

    except Exception as e:
        print(f"❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None, None


# 主程序
if __name__ == "__main__":
    # 文件路径
    videos_file = 'videoswithoutuserID.csv'
    stats_file = 'user_video_stats.csv'

    # 生成用户偏好表
    categories_df, themes_df = calculate_user_preferences(
        videos_file=videos_file,
        stats_file=stats_file,
        categories_output='user_top_categories.csv',
        themes_output='user_top_themes.csv'
    )