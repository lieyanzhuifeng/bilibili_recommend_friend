import pandas as pd


def extract_unique_movies(csv_file, output_file='unique_movies.csv'):
    """
    提取唯一的电影信息（基于movie_id）
    """
    # 读取数据
    df = pd.read_csv(csv_file)

    # 按movie_id去重，取第一条记录
    unique_movies = df[['movie_id', 'title', 'release_date', 'genres']].drop_duplicates(subset=['movie_id'])

    # 按movie_id排序（或者按title排序，根据你的需要）
    unique_movies = unique_movies.sort_values('movie_id').reset_index(drop=True)

    # 保存
    unique_movies.to_csv(output_file, index=False)

    print(f"提取完成！")
    print(f"原始记录数: {len(df)}")
    print(f"唯一电影数: {len(unique_movies)}")
    print(f"保存到: {output_file}")

    # 显示前几个
    print(f"\n前5部电影:")
    for i, row in unique_movies.head().iterrows():
        print(f"{row['movie_id']} | {row['title']} | {row['release_date']} | {row['genres']}")

    return unique_movies


# 使用
if __name__ == "__main__":
    extract_unique_movies('smalldata.csv', 'movies_unique.csv')