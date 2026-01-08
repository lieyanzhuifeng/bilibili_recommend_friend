import pandas as pd
import random


def generate_videos_table(movies_file, category_file, theme_file, output_file='videoswithoutuserID.csv'):
    """
    生成videos表数据

    参数:
        movies_file: movies_unique.csv文件路径
        category_file: video_category.csv文件路径
        theme_file: video_theme.csv文件路径
        output_file: 输出文件路径
    """

    print("开始生成videos表数据...")
    print("-" * 50)

    try:
        # 1. 读取数据
        print("读取数据文件...")
        movies_df = pd.read_csv(movies_file)
        category_df = pd.read_csv(category_file)
        theme_df = pd.read_csv(theme_file)

        # 打印列名，用于调试
        print(f"movies_unique.csv 列名: {list(movies_df.columns)}")
        print(f"video_category.csv 列名: {list(category_df.columns)}")
        print(f"video_theme.csv 列名: {list(theme_df.columns)}")

        print(f"\n电影数量: {len(movies_df)}")
        print(f"分类数量: {len(category_df)}")
        print(f"主题数量: {len(theme_df)}")

        # 2. 检查列名并创建映射字典
        # 处理category映射
        if 'categoryID' in category_df.columns and 'categoryName' in category_df.columns:
            category_dict = dict(zip(category_df['categoryName'], category_df['categoryID']))
        elif 'categoryid' in category_df.columns and 'categoryname' in category_df.columns:
            category_dict = dict(zip(category_df['categoryname'], category_df['categoryid']))
        else:
            # 尝试查找可能的列名
            print("\n警告: 无法找到标准的category列名")
            print(f"可用的列: {list(category_df.columns)}")

            # 假设第一列是ID，第二列是名称
            if len(category_df.columns) >= 2:
                id_col = category_df.columns[0]
                name_col = category_df.columns[1]
                category_dict = dict(zip(category_df[name_col], category_df[id_col]))
                print(f"使用列: {id_col} 作为ID, {name_col} 作为名称")
            else:
                raise ValueError("category文件格式不正确")

        # 处理theme映射
        if 'themeID' in theme_df.columns and 'themeName' in theme_df.columns:
            theme_dict = dict(zip(theme_df['themeName'], theme_df['themeID']))
        elif 'themeid' in theme_df.columns and 'themename' in theme_df.columns:
            theme_dict = dict(zip(theme_df['themename'], theme_df['themeid']))
        else:
            # 尝试查找可能的列名
            print("\n警告: 无法找到标准的theme列名")
            print(f"可用的列: {list(theme_df.columns)}")

            # 假设第一列是ID，第二列是名称
            if len(theme_df.columns) >= 2:
                id_col = theme_df.columns[0]
                name_col = theme_df.columns[1]
                theme_dict = dict(zip(theme_df[name_col], theme_df[id_col]))
                print(f"使用列: {id_col} 作为ID, {name_col} 作为名称")
            else:
                raise ValueError("theme文件格式不正确")

        print(f"\ncategory映射数量: {len(category_dict)}")
        print(f"theme映射数量: {len(theme_dict)}")

        # 3. 处理每部电影
        print("\n处理电影数据...")
        videos_data = []

        for idx, row in movies_df.iterrows():
            # 检查movies文件的列名
            movie_id = row.get('movie_id', row.get('movieid', None))
            title = row.get('title', '')
            release_date = row.get('release_date', row.get('release_date', ''))
            genres = row.get('genres', '')

            if movie_id is None:
                print(f"警告: 第{idx}行找不到movie_id")
                continue

            # 解析genres
            genres_list = [g.strip() for g in str(genres).split(',')]

            # 确定categoryID
            category_id = None
            for genre in genres_list:
                if genre in category_dict:
                    category_id = category_dict[genre]
                    break

            # 随机分配1-19
            if category_id is None:
                category_id = random.randint(1, 19)

            # 确定themeID
            theme_id = None
            for genre in genres_list:
                if genre in theme_dict and theme_dict[genre] != 6:  # 排除"NOT AVAILABLE"
                    theme_id = theme_dict[genre]
                    break

            # 随机分配1-5
            if theme_id is None:
                theme_id = random.randint(1, 5)

            # 添加到结果
            videos_data.append({
                'videoID': movie_id,
                'title': title,
                'categoryID': category_id,
                'themeID': theme_id,
                'publishTime': release_date
            })

            # 显示前3部电影的处理结果
            if idx < 3:
                print(f"电影 {movie_id}: {title[:25]}...")
                print(f"  genres: {genres_list}")
                print(f"  categoryID: {category_id}, themeID: {theme_id}")

        # 4. 创建DataFrame并保存
        if not videos_data:
            print("错误: 没有生成任何数据")
            return None

        videos_df = pd.DataFrame(videos_data)

        # 按videoID排序
        videos_df = videos_df.sort_values('videoID').reset_index(drop=True)

        # 保存结果
        videos_df.to_csv(output_file, index=False)

        print(f"\n处理完成!")
        print(f"生成记录数: {len(videos_df)}")
        print(f"保存到: {output_file}")

        # 5. 显示统计信息
        print(f"\n统计信息:")

        # category分布
        print(f"categoryID分布:")
        cat_counts = videos_df['categoryID'].value_counts().sort_index()
        for cat_id, count in cat_counts.items():
            print(f"  {cat_id:2d}: {count:3d} 部")

        # theme分布
        print(f"\nthemeID分布:")
        theme_counts = videos_df['themeID'].value_counts().sort_index()
        for theme_id, count in theme_counts.items():
            print(f"  {theme_id}: {count:3d} 部")

        # 6. 显示前5条记录
        print(f"\n前5条记录:")
        print("-" * 60)
        for i, row in videos_df.head().iterrows():
            title_display = row['title'][:30] + "..." if len(row['title']) > 30 else row['title']
            print(
                f"{row['videoID']:4d} | {title_display:33} | 分类: {row['categoryID']:2d} | 主题: {row['themeID']:2d} | {row['publishTime']}")

        return videos_df

    except Exception as e:
        print(f"\n错误: {e}")
        import traceback
        traceback.print_exc()
        return None


# 主程序
if __name__ == "__main__":
    # 文件路径
    movies_file = 'movies_unique.csv'
    category_file = 'video_category.csv'
    theme_file = 'video_theme.csv'
    output_file = 'videoswithoutuserID.csv'

    # 生成videos表
    generate_videos_table(movies_file, category_file, theme_file, output_file)