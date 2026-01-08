import pandas as pd

# 读取数据
df = pd.read_csv('smalldata.csv')

# 提取所有genre单词
all_genres = []
for genres in df['genres']:
    for genre in str(genres).split(','):
        all_genres.append(genre.strip())

# 去重并排序
unique_genres = sorted(set(all_genres))

# 输出结果
print(f"找到 {len(unique_genres)} 个唯一genre:")
for genre in unique_genres:
    print(f"  {genre}")

# 保存到文件
with open('genres.txt', 'w') as f:
    for genre in unique_genres:
        f.write(f"{genre}\n")