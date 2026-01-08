import pandas as pd
import random


def add_random_userid(input_file, output_file='videoswithoutduration.csv', userid_start=51, userid_end=150):
    """
    给videos.csv添加随机userID列

    参数:
        input_file: 输入文件路径 (videoswithoutuserID.csv)
        output_file: 输出文件路径
        userid_start: userID起始值
        userid_end: userID结束值
    """

    print(f"开始给 {input_file} 添加随机userID...")
    print(f"userID范围: {userid_start} - {userid_end}")
    print("-" * 50)

    try:
        # 1. 读取数据
        df = pd.read_csv(input_file)

        print(f"原始数据:")
        print(f"  视频数量: {len(df)}")
        print(f"  列名: {list(df.columns)}")
        print(f"  前3行数据:")
        print(df.head(3).to_string(index=False))

        # 2. 生成随机userID
        # 为每部视频从51-150之间随机分配一个userID
        userid_range = list(range(userid_start, userid_end + 1))

        # 设置随机种子确保可重复性
        random.seed(42)

        # 生成随机userID列表
        random_userids = [random.choice(userid_range) for _ in range(len(df))]

        # 3. 添加userID列
        df['userID'] = random_userids

        # 调整列顺序，让userID在合适的位置
        # 根据你的表结构，userID应该在videoID之后比较合理
        cols = ['videoID', 'userID', 'title', 'categoryID', 'themeID', 'publishTime']

        # 只保留实际存在的列
        existing_cols = [col for col in cols if col in df.columns]
        other_cols = [col for col in df.columns if col not in existing_cols]
        df = df[existing_cols + other_cols]

        # 4. 保存结果
        df.to_csv(output_file, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   处理视频数: {len(df)}")
        print(f"   保存到: {output_file}")

        # 5. 统计信息
        print(f"\n📊 统计信息:")

        # userID分布
        print(f"   userID分布统计:")
        userid_counts = df['userID'].value_counts().sort_index()

        # 显示部分统计
        unique_users = df['userID'].nunique()
        print(f"   唯一用户数: {unique_users}")
        print(f"   用户覆盖比例: {unique_users / (userid_end - userid_start + 1) * 100:.1f}%")

        # 每个用户的视频数统计
        print(f"\n   每个用户的视频数分布:")
        videos_per_user = df['userID'].value_counts()

        stats = videos_per_user.describe()
        print(f"     最小值: {stats['min']} 部")
        print(f"     最大值: {stats['max']} 部")
        print(f"     平均值: {stats['mean']:.1f} 部")
        print(f"     中位数: {videos_per_user.median()} 部")

        # 显示分布详情
        print(f"\n   详细分布:")
        for count, freq in videos_per_user.value_counts().sort_index().items():
            print(f"     有 {count} 部视频的用户: {freq} 人")

        # 6. 显示前10条记录
        print(f"\n📋 添加userID后的数据示例 (前10条):")
        print("=" * 70)
        print(f"{'videoID':<8} {'userID':<8} {'title':<30} {'category':<8} {'theme':<8} {'publishTime'}")
        print("-" * 70)

        for i, row in df.head(10).iterrows():
            title_short = row['title'][:28] + "..." if len(row['title']) > 28 else row['title']
            print(
                f"{row['videoID']:<8} {row['userID']:<8} {title_short:<30} {row['categoryID']:<8} {row['themeID']:<8} {row['publishTime']}")

        # 7. 按用户分组示例
        print(f"\n👥 用户视频示例 (前5个用户):")
        print("-" * 50)

        top_users = df['userID'].value_counts().head(5).index
        for user_id in top_users:
            user_videos = df[df['userID'] == user_id]
            print(f"用户 {user_id} 有 {len(user_videos)} 部视频:")
            for _, video in user_videos.head(3).iterrows():
                print(f"  - {video['title'][:30]}... (videoID: {video['videoID']})")
            if len(user_videos) > 3:
                print(f"  ... 还有 {len(user_videos) - 3} 部")
            print()

        return df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None


# 更简单的版本
def add_random_userid_simple(input_file, output_file='videoswithoutduration.csv'):
    """
    简单版本：直接添加随机userID
    """
    df = pd.read_csv(input_file)

    # 生成51-150的随机userID
    random.seed(42)  # 固定种子确保可重复
    df['userID'] = [random.randint(51, 150) for _ in range(len(df))]

    # 保存
    df.to_csv(output_file, index=False)

    print(f"添加完成！保存到 {output_file}")
    print(f"视频数: {len(df)}, 唯一用户数: {df['userID'].nunique()}")

    return df


# 批量处理版本
def batch_add_userid(file_pattern, output_dir='./'):
    """
    批量给多个文件添加userID
    """
    import glob
    import os

    files = glob.glob(file_pattern)
    print(f"找到 {len(files)} 个文件")

    for file in files:
        print(f"\n处理: {os.path.basename(file)}")
        add_random_userid(file, os.path.join(output_dir, f"with_userid_{os.path.basename(file)}"))


# 主程序
if __name__ == "__main__":
    # 文件路径
    input_file = 'videoswithoutuserID.csv'

    print("🎬 给videos.csv添加随机userID")
    print("=" * 50)

    # 选择版本
    print("选择版本:")
    print("1. 完整版 (带详细统计)")
    print("2. 简单版 (只添加列)")
    print("3. 自定义范围")

    choice = input("请输入选择 (1-3): ").strip()

    if choice == '1':
        # 完整版
        output_file = 'videoswithoutduration.csv'
        result = add_random_userid(input_file, output_file)

    elif choice == '2':
        # 简单版
        output_file = 'videos_with_userid_simple.csv'
        result = add_random_userid_simple(input_file, output_file)

    elif choice == '3':
        # 自定义范围
        try:
            start = int(input("请输入userID起始值: "))
            end = int(input("请输入userID结束值: "))
            output_file = 'videos_with_userid_custom.csv'
            result = add_random_userid(input_file, output_file, start, end)
        except ValueError:
            print("❌ 请输入有效的数字")
            result = None
    else:
        # 默认用完整版
        output_file = 'videoswithoutduration.csv'
        result = add_random_userid(input_file, output_file)

    if result is not None:
        print(f"\n🎉 成功生成文件!")
        print(f"   输出文件: {output_file}")
        print(f"   视频总数: {len(result)}")
        print(f"   用户ID范围: {result['userID'].min()} - {result['userID'].max()}")

        # 显示文件内容示例
        print(f"\n📋 最终文件格式示例:")
        print(result.head().to_string(index=False))