import csv

# 你的genre列表
genres = [
    "Action", "Adventure", "Animation", "Biography", "Comedy",
    "Crime", "Documentary", "Drama", "Family", "Fantasy",
    "History", "Horror", "Music", "Musical", "Mystery",
    "NOT AVAILABLE", "News", "Reality-TV", "Romance", "Sci-Fi",
    "Short", "Sport", "Thriller", "War", "Western"
]

# 定义分类
category_list = [
    "Action", "Adventure", "Animation", "Comedy", "Crime",
    "Documentary", "Drama", "Family", "Fantasy", "Horror",
    "Music", "Musical", "Mystery", "Romance", "Sci-Fi",
    "Sport", "Thriller", "War", "Western"
]

theme_list = [
    "Biography", "History", "News", "Reality-TV", "Short",
    "NOT AVAILABLE"
]

# 生成video_category.csv
with open('video_category.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['categoryID', 'categoryName'])
    for i, category in enumerate(sorted(set(category_list)), 1):
        writer.writerow([i, category])
        print(f"category: {i:3d}. {category}")

print("\n" + "="*40)

# 生成video_theme.csv
with open('video_theme.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['themeID', 'themeName'])
    for i, theme in enumerate(sorted(set(theme_list)), 1):
        writer.writerow([i, theme])
        print(f"theme: {i:3d}. {theme}")

print(f"\n✅ 已生成: video_category.csv 和 video_theme.csv")