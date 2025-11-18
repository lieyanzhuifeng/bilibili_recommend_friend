import pymysql
import random


def insert_user_favorites_data():
    """向user_favorites表插入收藏数据"""
    try:
        # 连接数据库
        connection = pymysql.connect(
            host='47.100.240.111',
            port=3306,
            user='root',
            password='Db123456',
            database='b_friend_rec',
            charset='utf8mb4'
        )

        print("✅ 数据库连接成功!")

        with connection.cursor() as cursor:
            # 清空表（可选）
            cursor.execute("DELETE FROM user_favorites")
            print("🗑️ 已清空user_favorites表")

            inserted_count = 0

            # 为用户1-10插入收藏数据
            for user_id in range(1, 11):
                # 每个用户随机收藏5-15个视频
                num_favorites = random.randint(5, 15)

                # 从视频ID 1-105中随机选择不重复的视频
                favorite_videos = random.sample(range(1, 106), num_favorites)

                # 插入收藏记录
                for video_id in favorite_videos:
                    sql = "INSERT INTO user_favorites (userID, videoID) VALUES (%s, %s)"
                    cursor.execute(sql, (user_id, video_id))
                    inserted_count += 1

                print(f"✅ 用户 {user_id} 收藏了 {num_favorites} 个视频")

            # 提交事务
            connection.commit()
            print(f"\n✅ 插入完成！成功插入 {inserted_count} 条收藏记录")

            # 显示统计信息
            print("\n📊 收藏数据统计:")
            cursor.execute("""
                           SELECT COUNT(DISTINCT userID)  as unique_users,
                                  COUNT(DISTINCT videoID) as unique_videos,
                                  COUNT(*)                as total_favorites
                           FROM user_favorites
                           """)
            stats = cursor.fetchone()

            print(f"👥 涉及用户数: {stats[0]}")
            print(f"🎬 涉及视频数: {stats[1]}")
            print(f"❤️  总收藏数: {stats[2]}")

            # 显示前10条记录作为预览
            print("\n📋 数据预览 (前10条):")
            cursor.execute("""
                           SELECT userID, videoID
                           FROM user_favorites
                           ORDER BY userID, videoID LIMIT 10
                           """)
            preview_data = cursor.fetchall()

            print(f"{'用户ID':<8} {'视频ID':<8}")
            print("-" * 20)
            for record in preview_data:
                print(f"{record[0]:<8} {record[1]:<8}")

    except Exception as e:
        print(f"❌ 插入数据失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    print("❤️ 开始插入用户收藏数据...")
    insert_user_favorites_data()
    print("🎉 收藏数据插入完成！")


if __name__ == "__main__":
    main()