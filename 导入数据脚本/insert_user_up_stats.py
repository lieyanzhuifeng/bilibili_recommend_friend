import pymysql


def initialize_user_up_stats():
    """从user_watch_log表初始化user_up_stats表"""
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
            # 从user_watch_log表统计数据并插入到user_up_stats
            print("📊 从user_watch_log表统计数据...")

            sql = """
                  INSERT INTO user_up_stats (userID, upID, totalWatchDuration, uniqueVideos)
                  SELECT wl.userID, \
                         v.uploaderID               as upID, \
                         SEC_TO_TIME(SUM( \
                                 CASE \
                                     WHEN wl.watchduration IS NULL THEN 0 \
                                     ELSE TIME_TO_SEC(wl.watchduration) \
                                     END \
                                     ))             as totalWatchDuration, \
                         COUNT(DISTINCT wl.videoID) as uniqueVideos
                  FROM user_watch_log wl
                           JOIN videos v ON wl.videoID = v.videoID
                  GROUP BY wl.userID, v.uploaderID \
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
                                  COUNT(DISTINCT upID)                              as unique_ups,
                                  SUM(uniqueVideos)                                 as total_unique_videos,
                                  SEC_TO_TIME(SUM(TIME_TO_SEC(totalWatchDuration))) as total_duration
                           FROM user_up_stats
                           """)
            stats = cursor.fetchall()

            print(f"👥 涉及用户数: {stats[0][0]}")
            print(f"🎬 涉及UP主数: {stats[0][1]}")
            print(f"📺 总唯一视频数: {stats[0][2]}")
            print(f"⏱️  总观看时长: {stats[0][3]}")

            # 显示前10条记录作为预览
            print("\n📋 数据预览 (前10条):")
            cursor.execute("""
                           SELECT upStatID, userID, upID, totalWatchDuration, uniqueVideos
                           FROM user_up_stats
                           ORDER BY TIME_TO_SEC(totalWatchDuration) DESC LIMIT 10
                           """)
            preview_data = cursor.fetchall()

            print(f"{'upStatID':<8} {'userID':<8} {'upID':<8} {'总时长':<15} {'唯一视频数':<12}")
            print("-" * 60)
            for record in preview_data:
                duration_str = str(record[3]) if record[3] else "00:00:00"
                print(f"{record[0]:<8} {record[1]:<8} {record[2]:<8} {duration_str:<15} {record[4]:<12}")

    except Exception as e:
        print(f"❌ 初始化失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def check_user_up_stats_status():
    """检查user_up_stats表状态"""
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
            cursor.execute("SELECT COUNT(*) as count FROM user_up_stats")
            count = cursor.fetchone()[0]

            if count > 0:
                print(f"✅ user_up_stats表已有 {count} 条记录")

                # 显示一些统计信息
                cursor.execute("""
                               SELECT AVG(uniqueVideos)                                 as avg_videos,
                                      MAX(uniqueVideos)                                 as max_videos,
                                      SEC_TO_TIME(AVG(TIME_TO_SEC(totalWatchDuration))) as avg_duration,
                                      SEC_TO_TIME(MAX(TIME_TO_SEC(totalWatchDuration))) as max_duration
                               FROM user_up_stats
                               """)
                stats = cursor.fetchall()
                print(f"📈 平均唯一视频数: {stats[0][0]:.2f}")
                print(f"📈 最大唯一视频数: {stats[0][1]}")
                print(f"⏱️  平均观看时长: {stats[0][2]}")
                print(f"⏱️  最长观看时长: {stats[0][3]}")
            else:
                print("📭 user_up_stats表为空")

    except Exception as e:
        print(f"❌ 检查失败: {e}")
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    while True:
        print("\n" + "=" * 60)
        print("📊 User UP Stats 初始化工具")
        print("=" * 60)
        print("1. 检查表状态")
        print("2. 初始化数据（从user_watch_log表）")
        print("0. 退出")
        print("-" * 60)

        choice = input("请选择操作 (0-2): ").strip()

        if choice == '1':
            check_user_up_stats_status()

        elif choice == '2':
            confirm = input("⚠️ 确定要从user_watch_log表初始化数据吗？(y/N): ").strip().lower()
            if confirm == 'y':
                initialize_user_up_stats()
            else:
                print("❌ 操作已取消")

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")


if __name__ == "__main__":
    main()