import pandas as pd
import re


def generate_video_tags(videos_file,
                        tags_output='tag.csv',
                        relations_output='tag_video.csv'):
    """
    生成视频标签系统

    参数:
        videos_file: videos.csv文件路径
        tags_output: 标签表输出文件
        relations_output: 标签关系表输出文件
    """

    print("🎯 生成视频标签系统")
    print("=" * 60)

    try:
        # 1. 读取视频数据
        print("读取视频数据...")
        videos_df = pd.read_csv(videos_file)

        print(f"视频数量: {len(videos_df)}")
        print(f"列名: {list(videos_df.columns)}")

        # 显示一些标题示例
        print("\n📋 标题示例:")
        for i, title in enumerate(videos_df['title'].head(10), 1):
            print(f"  {i:2d}. {title}")

        # 2. 预定义具体标签（基于内容的关键词）
        print("\n🏷️  定义具体标签...")

        tags_definitions = [
            # 基于你的数据中的具体内容
            {"tagName": "Nazi", "keywords": ["nazi", "hitler", "holocaust", "wwii", "germany"]},
            {"tagName": "Horror", "keywords": ["horror", "scary", "ghost", "terror", "fear", "haunted", "amityville"]},
            {"tagName": "Christmas", "keywords": ["christmas", "xmas", "santa", "holiday", "noel"]},
            {"tagName": "Superhero", "keywords": ["superhero", "marvel", "dc", "batman", "spiderman", "wonder"]},
            {"tagName": "Fantasy", "keywords": ["fantasy", "magic", "dragon", "wizard", "elf", "warcraft"]},
            {"tagName": "Action", "keywords": ["action", "fight", "battle", "combat", "explosion", "chase"]},
            {"tagName": "Comedy", "keywords": ["comedy", "funny", "humor", "joke", "laugh"]},
            {"tagName": "Romance", "keywords": ["romance", "love", "heart", "kiss", "couple", "valentine"]},
            {"tagName": "Drama", "keywords": ["drama", "emotional", "serious", "story", "life"]},
            {"tagName": "Thriller", "keywords": ["thriller", "suspense", "tense", "mystery", "crime"]},
            {"tagName": "Sci-Fi", "keywords": ["sci-fi", "science fiction", "future", "space", "alien"]},
            {"tagName": "War", "keywords": ["war", "battle", "soldier", "military", "army", "ridge", "raid"]},
            {"tagName": "Crime", "keywords": ["crime", "criminal", "heist", "robbery", "gangster"]},
            {"tagName": "Documentary", "keywords": ["documentary", "doc", "real", "true story", "fact"]},
            {"tagName": "Family", "keywords": ["family", "kid", "children", "parent", "child"]},
            {"tagName": "Adventure", "keywords": ["adventure", "journey", "expedition", "quest", "explore"]},
            {"tagName": "Animation", "keywords": ["animation", "animated", "cartoon", "pixar", "disney"]},
            {"tagName": "Biography", "keywords": ["biography", "bio", "true story", "real life", "historical"]},
            {"tagName": "History", "keywords": ["history", "historical", "past", "era", "period"]},
            {"tagName": "Music", "keywords": ["music", "song", "concert", "band", "singer"]},
            {"tagName": "Musical", "keywords": ["musical", "song and dance", "broadway", "stage"]},
            {"tagName": "Mystery", "keywords": ["mystery", "whodunit", "clue", "detective", "investigation"]},
            {"tagName": "Sport", "keywords": ["sport", "sports", "game", "athlete", "competition"]},
            {"tagName": "Vampire", "keywords": ["vampire", "dracula", "blood", "fang", "undead"]},
            {"tagName": "Zombie", "keywords": ["zombie", "undead", "apocalypse", "walking dead"]},
            {"tagName": "Pirate", "keywords": ["pirate", "caribbean", "ship", "treasure"]},
            {"tagName": "Mafia", "keywords": ["mafia", "gangster", "crime family", "godfather"]},
            {"tagName": "Wedding", "keywords": ["wedding", "marriage", "bride", "groom"]},
            {"tagName": "Prison", "keywords": ["prison", "jail", "inmate", "cell"]},
            {"tagName": "Beach", "keywords": ["beach", "bay", "ocean", "sea", "coast"]},
            {"tagName": "School", "keywords": ["school", "student", "teacher", "classroom"]},
            {"tagName": "Lawyer", "keywords": ["lawyer", "attorney", "court", "trial"]},
            {"tagName": "Doctor", "keywords": ["doctor", "hospital", "medical", "surgeon"]},
            {"tagName": "Police", "keywords": ["police", "cop", "detective", "investigator"]},
            {"tagName": "Spy", "keywords": ["spy", "agent", "espionage", "secret"]},
            {"tagName": "Time Travel", "keywords": ["time travel", "time machine", "back to future"]},
            {"tagName": "Artificial Intelligence", "keywords": ["ai", "robot", "android", "cyborg"]},
            {"tagName": "Alien", "keywords": ["alien", "extraterrestrial", "ufo", "space"]},
            {"tagName": "Apocalypse", "keywords": ["apocalypse", "end of world", "dystopia"]},
            {"tagName": "Drug", "keywords": ["drug", "cocaine", "heroin", "addiction"]},
            {"tagName": "Terrorism", "keywords": ["terrorism", "terrorist", "isis", "bomb"]},
        ]

        print(f"定义 {len(tags_definitions)} 个具体标签")

        # 3. 创建标签表
        print("\n📋 创建标签表...")

        tags_data = []
        for i, tag_def in enumerate(tags_definitions, 1):
            tags_data.append({
                'tagID': i,
                'tagName': tag_def['tagName'],
                'description': f"包含关键词: {', '.join(tag_def['keywords'])}"
            })

        tags_df = pd.DataFrame(tags_data)
        tags_df.to_csv(tags_output, index=False)

        print(f"标签表保存到: {tags_output}")
        print(f"标签数量: {len(tags_df)}")

        # 显示前10个标签
        print("\n🏷️  标签示例:")
        for i, row in tags_df.head(10).iterrows():
            print(f"  {row['tagID']:3d}. {row['tagName']:20} - {row['description'][:40]}...")

        # 4. 创建标签关键词映射（小写处理）
        tag_keywords_map = {}
        for tag_def in tags_definitions:
            tag_name = tag_def['tagName']
            keywords = [kw.lower() for kw in tag_def['keywords']]
            tag_keywords_map[tag_name] = keywords

        # 5. 为每个视频匹配标签
        print("\n🔍 为视频匹配标签...")

        relations_data = []
        relation_id = 1

        for _, video in videos_df.iterrows():
            video_id = video['videoID']
            title = str(video['title']).lower()

            matched_tags = set()

            # 检查每个标签的关键词
            for tag_name, keywords in tag_keywords_map.items():
                for keyword in keywords:
                    # 使用单词边界匹配，避免部分匹配（如"cat"匹配"category"）
                    pattern = r'\b' + re.escape(keyword) + r'\b'
                    if re.search(pattern, title):
                        matched_tags.add(tag_name)
                        break  # 找到一个关键词就足够

            # 如果没有匹配到任何标签，使用通用标签
            if not matched_tags:
                # 基于标题中的其他词
                title_words = set(re.findall(r'\b\w+\b', title))

                # 检查一些通用关键词
                generic_mappings = {
                    'Drama': {'life', 'story', 'heart', 'time'},
                    'Action': {'man', 'force', 'power', 'fire'},
                    'Mystery': {'secret', 'dark', 'night', 'shadow'},
                }

                for tag_name, generic_words in generic_mappings.items():
                    if generic_words.intersection(title_words):
                        matched_tags.add(tag_name)
                        break

            # 添加标签关系到数据
            for tag_name in matched_tags:
                # 找到对应的tagID
                tag_row = tags_df[tags_df['tagName'] == tag_name]
                if not tag_row.empty:
                    tag_id = tag_row.iloc[0]['tagID']
                    relations_data.append({
                        'relationID': relation_id,
                        'videoID': video_id,
                        'tagID': tag_id
                    })
                    relation_id += 1

        # 6. 创建标签关系表
        relations_df = pd.DataFrame(relations_data)
        relations_df.to_csv(relations_output, index=False)

        print(f"\n✅ 处理完成!")
        print(f"   标签关系表保存到: {relations_output}")
        print(f"   标签关系数量: {len(relations_df)}")

        # 7. 统计信息
        print(f"\n📊 统计信息:")

        # 每个视频的平均标签数
        avg_tags_per_video = len(relations_df) / len(videos_df)
        print(f"   平均每个视频标签数: {avg_tags_per_video:.2f}")

        # 标签使用频率
        tag_usage = relations_df['tagID'].value_counts()
        tag_usage_df = tag_usage.reset_index()
        tag_usage_df.columns = ['tagID', 'usage_count']

        # 关联标签信息
        tag_usage_df = tag_usage_df.merge(tags_df[['tagID', 'tagName']], on='tagID')
        tag_usage_df = tag_usage_df.sort_values('usage_count', ascending=False)

        print(f"\n🏆 最常用的标签 (前10名):")
        for i, row in tag_usage_df.head(10).iterrows():
            print(f"   第{i + 1:2d}名: {row['tagName']:20} - {row['usage_count']:3d} 个视频")

        # 8. 显示视频标签示例
        print(f"\n📋 视频标签示例 (前10个视频):")
        print("=" * 70)

        sample_videos = videos_df.head(10)
        for _, video in sample_videos.iterrows():
            video_id = video['videoID']
            title = video['title']

            # 获取该视频的标签
            video_tags = relations_df[relations_df['videoID'] == video_id]
            if not video_tags.empty:
                tag_ids = video_tags['tagID'].tolist()
                tag_names = []
                for tag_id in tag_ids:
                    tag_row = tags_df[tags_df['tagID'] == tag_id]
                    if not tag_row.empty:
                        tag_names.append(tag_row.iloc[0]['tagName'])

                print(f"视频 {video_id:3d}: {title[:40]:40}...")
                print(f"    标签: {', '.join(tag_names)}")
            else:
                print(f"视频 {video_id:3d}: {title[:40]:40}...")
                print(f"    无匹配标签")
            print()

        return tags_df, relations_df

    except Exception as e:
        print(f"\n❌ 处理失败: {e}")
        import traceback
        traceback.print_exc()
        return None, None


# 更智能的版本，使用自然语言处理关键词提取
def generate_video_tags_enhanced(videos_file, tags_output='video_tags_enhanced.csv',
                                 relations_output='video_tag_relations_enhanced.csv'):
    """
    增强版：更智能的标签匹配
    """

    print("🤖 智能视频标签生成系统")
    print("=" * 60)

    try:
        videos_df = pd.read_csv(videos_file)

        # 从标题中提取常见词频
        from collections import Counter
        all_words = []

        for title in videos_df['title']:
            words = re.findall(r'\b\w+\b', str(title).lower())
            # 过滤停用词
            stop_words = {'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for', 'of', 'with', 'by'}
            words = [w for w in words if w not in stop_words and len(w) > 2]
            all_words.extend(words)

        word_freq = Counter(all_words)

        print(f"分析 {len(videos_df)} 个视频标题")
        print(f"提取到 {len(word_freq)} 个独特词汇")

        # 显示最常见词汇
        print(f"\n🔤 标题中最常见的词汇 (前20):")
        for word, count in word_freq.most_common(20):
            print(f"  {word:15}: {count:3d} 次")

        # 基于高频词创建标签
        print(f"\n🏷️  基于高频词创建标签...")

        # 手动定义一些高质量标签
        enhanced_tags = [
            # 基于具体内容
            {"tagName": "Nazi/WWII", "keywords": ["nazi", "hitler", "holocaust", "wwii", "germany", "war"]},
            {"tagName": "Horror/Supernatural", "keywords": ["horror", "scary", "ghost", "terror", "vampire", "zombie"]},
            {"tagName": "Holiday/Christmas", "keywords": ["christmas", "xmas", "santa", "holiday", "noel"]},
            {"tagName": "Comedy/Romance", "keywords": ["comedy", "funny", "love", "romance", "heart", "couple"]},
            {"tagName": "Action/Adventure", "keywords": ["action", "adventure", "battle", "fight", "quest", "journey"]},
            {"tagName": "Drama/Crime", "keywords": ["drama", "crime", "murder", "mystery", "detective", "police"]},
            {"tagName": "Fantasy/Sci-Fi", "keywords": ["fantasy", "sci-fi", "magic", "space", "alien", "future"]},
            {"tagName": "Family/Animation",
             "keywords": ["family", "animation", "kid", "children", "cartoon", "disney"]},
            {"tagName": "Documentary/Biography",
             "keywords": ["documentary", "biography", "true", "story", "real", "history"]},
            {"tagName": "Thriller/Suspense",
             "keywords": ["thriller", "suspense", "mystery", "secret", "danger", "chase"]},

            # 基于高频词
            {"tagName": "War/Military", "keywords": ["war", "battle", "soldier", "military", "army", "navy"]},
            {"tagName": "School/Teen", "keywords": ["school", "high", "teen", "student", "college", "prom"]},
            {"tagName": "Wedding/Marriage", "keywords": ["wedding", "marriage", "bride", "groom", "ceremony"]},
            {"tagName": "Prison/Jail", "keywords": ["prison", "jail", "inmate", "cell", "escape", "convict"]},
            {"tagName": "Drug/Addiction",
             "keywords": ["drug", "cocaine", "heroin", "addiction", "narcotic", "overdose"]},
            {"tagName": "Terrorism/ISIS", "keywords": ["terrorism", "terrorist", "isis", "bomb", "attack", "jihad"]},
            {"tagName": "AI/Robot", "keywords": ["ai", "robot", "android", "cyborg", "machine", "artificial"]},
            {"tagName": "Time Travel", "keywords": ["time", "travel", "future", "past", "machine", "loop"]},
            {"tagName": "Apocalypse/Dystopia",
             "keywords": ["apocalypse", "dystopia", "end", "world", "catastrophe", "survival"]},
            {"tagName": "Beach/Ocean", "keywords": ["beach", "ocean", "sea", "bay", "coast", "water"]},
        ]

        # 创建标签表
        tags_data = []
        for i, tag_def in enumerate(enhanced_tags, 1):
            tags_data.append({
                'tagID': i,
                'tagName': tag_def['tagName'],
                'description': f"关键词: {', '.join(tag_def['keywords'][:5])}"
            })

        tags_df = pd.DataFrame(tags_data)
        tags_df.to_csv(tags_output, index=False)

        print(f"创建 {len(tags_df)} 个增强标签")

        # 匹配标签
        relations_data = []
        relation_id = 1

        tag_keywords_map = {}
        for tag_def in enhanced_tags:
            tag_keywords_map[tag_def['tagName']] = [kw.lower() for kw in tag_def['keywords']]

        for _, video in videos_df.iterrows():
            video_id = video['videoID']
            title = str(video['title']).lower()

            matched_tags = set()

            for tag_name, keywords in tag_keywords_map.items():
                for keyword in keywords:
                    if re.search(r'\b' + re.escape(keyword) + r'\b', title):
                        matched_tags.add(tag_name)
                        break

            # 添加到关系表
            for tag_name in matched_tags:
                tag_row = tags_df[tags_df['tagName'] == tag_name]
                if not tag_row.empty:
                    tag_id = tag_row.iloc[0]['tagID']
                    relations_data.append({
                        'relationID': relation_id,
                        'videoID': video_id,
                        'tagID': tag_id
                    })
                    relation_id += 1

        # 保存关系表
        relations_df = pd.DataFrame(relations_data)
        relations_df.to_csv(relations_output, index=False)

        print(f"\n✅ 增强版完成!")
        print(f"   标签关系: {len(relations_df)} 条")
        print(f"   平均每个视频: {len(relations_df) / len(videos_df):.2f} 个标签")

        return tags_df, relations_df

    except Exception as e:
        print(f"❌ 错误: {e}")
        return None, None


# 主程序
if __name__ == "__main__":
    videos_file = 'videos.csv'

    print("🎬 视频标签系统生成器")
    print("=" * 50)

    print("选择版本:")
    print("1. 基础版 (预定义标签)")
    print("2. 增强版 (智能匹配)")

    choice = input("请输入选择 (1-2): ").strip() or "1"

    if choice == "1":
        tags_file = 'tag.csv'
        relations_file = 'tag_video.csv'
        tags_df, relations_df = generate_video_tags(videos_file, tags_file, relations_file)
    else:
        tags_file = 'video_tags_enhanced.csv'
        relations_file = 'video_tag_relations_enhanced.csv'
        tags_df, relations_df = generate_video_tags_enhanced(videos_file, tags_file, relations_file)

    if tags_df is not None and relations_df is not None:
        print(f"\n🎉 成功生成标签系统!")
        print(f"   标签表: {tags_file} ({len(tags_df)} 个标签)")
        print(f"   关系表: {relations_file} ({len(relations_df)} 条关系)")

        # 显示统计
        print(f"\n📊 最终统计:")
        print(f"   视频总数: {len(pd.read_csv(videos_file))}")
        print(f"   有标签的视频: {relations_df['videoID'].nunique()}")
        print(f"   标签覆盖率: {relations_df['videoID'].nunique() / len(pd.read_csv(videos_file)) * 100:.1f}%")

        # 显示热门标签
        tag_counts = relations_df['tagID'].value_counts().head(10)
        print(f"\n🔥 最热门标签:")
        for tag_id, count in tag_counts.items():
            tag_name = tags_df[tags_df['tagID'] == tag_id]['tagName'].iloc[0]
            print(f"   {tag_name:25}: {count:3d} 个视频")