import pymysql


def update_user_video_stats_table():
    """更新user_video_stats表结构，将totalWatchDuration改为TIME类型"""
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
            # 检查表是否存在
            cursor.execute("SHOW TABLES LIKE 'user_video_stats'")
            table_exists = cursor.fetchone()

            if table_exists:
                print("📋 删除现有的user_video_stats表...")
                cursor.execute("DROP TABLE user_video_stats")

            # 创建新表，totalWatchDuration改为TIME类型
            print("🔄 创建新的user_video_stats表...")
            create_table_sql = """
                               CREATE TABLE user_video_stats \
                               ( \
                                   statID             INT AUTO_INCREMENT PRIMARY KEY, \
                                   userID             INT NOT NULL, \
                                   videoID            INT NOT NULL, \
                                   watchCount         INT DEFAULT 0, \
                                   totalWatchDuration TIME, \
                                   FOREIGN KEY (userID) REFERENCES users (userID), \
                                   FOREIGN KEY (videoID) REFERENCES videos (videoID), \
                                   UNIQUE KEY unique_user_video (userID, videoID)
                               ) \
                               """
            cursor.execute(create_table_sql)

            # 提交事务
            connection.commit()
            print("✅ user_video_stats表更新完成！")

            # 显示表结构
            print("\n📊 新表结构:")
            cursor.execute("DESCRIBE user_video_stats")
            columns = cursor.fetchall()
            for column in columns:
                print(f"  {column[0]:20} {column[1]:20} {column[2]:10} {column[3]:10}")

    except Exception as e:
        print(f"❌ 更新表结构失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def initialize_user_video_stats():
    """从user_watch_log表初始化user_video_stats表（使用TIME类型）"""
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
            # 从user_watch_log表统计数据并插入到user_video_stats
            print("📊 从user_watch_log表统计数据...")

            sql = """
                  INSERT INTO user_video_stats (userID, videoID, watchCount, totalWatchDuration)
                  SELECT userID, \
                         videoID, \
                         COUNT(*)       as watchCount, \
                         SEC_TO_TIME(SUM( \
                                 CASE \
                                     WHEN watchduration IS NULL THEN 0 \
                                     ELSE TIME_TO_SEC(watchduration) \
                                     END \
                                     )) as totalWatchDuration
                  FROM user_watch_log
                  GROUP BY userID, videoID \
                  """

            cursor.execute(sql)
            inserted_count = cursor.rowcount

            # 提交事务
            connection.commit()

            print(f"✅ 初始化完成！成功插入 {inserted_count} 条记录")

            # 显示统计信息
            print("\n📈 统计信息:")
            cursor.execute("""
                           SELECT COUNT(DISTINCT userID)                            as unique_users,
                                  COUNT(DISTINCT videoID)                           as unique_videos,
                                  SUM(watchCount)                                   as total_watches,
                                  SEC_TO_TIME(SUM(TIME_TO_SEC(totalWatchDuration))) as total_duration
                           FROM user_video_stats
                           """)
            stats = cursor.fetchall()

            print(f"👥 涉及用户数: {stats[0][0]}")
            print(f"🎬 涉及视频数: {stats[0][1]}")
            print(f"📺 总观看次数: {stats[0][2]}")
            print(f"⏱️  总观看时长: {stats[0][3]}")

            # 显示前10条记录作为预览
            print("\n📋 数据预览 (前10条):")
            cursor.execute("""
                           SELECT statID, userID, videoID, watchCount, totalWatchDuration
                           FROM user_video_stats
                           ORDER BY TIME_TO_SEC(totalWatchDuration) DESC LIMIT 10
                           """)
            preview_data = cursor.fetchall()

            print(f"{'statID':<8} {'userID':<8} {'videoID':<8} {'观看次数':<10} {'总时长':<15}")
            print("-" * 60)
            for record in preview_data:
                duration_str = str(record[4]) if record[4] else "00:00:00"
                print(f"{record[0]:<8} {record[1]:<8} {record[2]:<8} {record[3]:<10} {duration_str:<15}")
    except Exception as e:
        print(f"❌ 初始化失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def check_table_status():
    """检查表状态"""
    try:
        connection = pymysql.connect(
            host='47.100.240.111',
            port=3306,
            user='root',
            password='Db123456',
            database='b_friend_rec',
            charset='utf8mb4'
        )

        with connection.cursor() as cursor:
            # 检查表是否存在
            cursor.execute("SHOW TABLES LIKE 'user_video_stats'")
            table_exists = cursor.fetchone()

            if table_exists:
                print("✅ user_video_stats表存在")

                # 检查记录数
                cursor.execute("SELECT COUNT(*) as count FROM user_video_stats")
                count = cursor.fetchone()[0]
                print(f"📊 当前表中有 {count} 条记录")

                # 显示字段类型
                cursor.execute("DESCRIBE user_video_stats")
                columns = cursor.fetchall()
                print("\n📋 字段类型:")
                for column in columns:
                    if column[0] == 'totalWatchDuration':
                        print(f"  ✅ {column[0]}: {column[1]} (TIME类型)")
                    else:
                        print(f"  {column[0]}: {column[1]}")
            else:
                print("❌ user_video_stats表不存在")

    except Exception as e:
        print(f"❌ 检查表状态失败: {e}")
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    while True:
        print("\n" + "=" * 60)
        print("🔄 User Video Stats 表结构更新工具")
        print("=" * 60)
        print("1. 检查表状态")
        print("2. 更新表结构（totalWatchDuration改为TIME类型）")
        print("3. 初始化数据（从user_watch_log表）")
        print("0. 退出")
        print("-" * 60)

        choice = input("请选择操作 (0-3): ").strip()

        if choice == '1':
            check_table_status()

        elif choice == '2':
            confirm = input("⚠️ 确定要更新表结构吗？这将删除现有数据 (y/N): ").strip().lower()
            if confirm == 'y':
                update_user_video_stats_table()
            else:
                print("❌ 操作已取消")

        elif choice == '3':
            confirm = input("⚠️ 确定要从user_watch_log表初始化数据吗？(y/N): ").strip().lower()
            if confirm == 'y':
                initialize_user_video_stats()
            else:
                print("❌ 操作已取消")

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")


if __name__ == "__main__":
    main()