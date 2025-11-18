import pymysql
import random


def insert_user_statistics_data():
    """向user_statistics表插入测试数据"""
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
            # 首先清空表（如果需要）
            cursor.execute("DELETE FROM user_statistics")
            print("🗑️ 已清空user_statistics表")

            # 为userID 1-11插入数据
            inserted_count = 0
            for user_id in range(1, 12):  # 1到11
                # 生成随机数据
                total_watch_hours = round(random.uniform(100, 1000), 2)
                like_rate = round(random.uniform(0, 1), 4)
                coin_rate = round(random.uniform(0, 1), 4)
                favorite_rate = round(random.uniform(0, 1), 4)
                share_rate = round(random.uniform(0, 1), 4)
                active_days = random.randint(50, 365)
                night_watch_minutes = random.randint(5000, 50000)
                night_watch_days = random.randint(10, 200)  # 夜间观看天数
                main_category_id = random.randint(1, 10)
                main_up_id = 11  # 固定为11

                # 插入数据
                sql = """
                      INSERT INTO user_statistics
                      (userID, totalWatchHours, likeRate, coinRate, favoriteRate, shareRate,
                       activeDays, nightWatchMinutes, nightWatchDays, mainCategoryID, mainUPID)
                      VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) \
                      """

                cursor.execute(sql, (
                    user_id, total_watch_hours, like_rate, coin_rate, favorite_rate, share_rate,
                    active_days, night_watch_minutes, night_watch_days, main_category_id, main_up_id
                ))

                inserted_count += 1
                print(
                    f"✅ 插入 userID={user_id}: {total_watch_hours}小时, {active_days}活跃天, 分区{main_category_id}, UP主{main_up_id}")

            # 提交事务
            connection.commit()
            print(f"\n✅ 数据插入完成！成功插入 {inserted_count} 条记录")

            # 显示插入的数据
            print("\n📊 插入的数据预览:")
            cursor.execute("""
                           SELECT userID,
                                  totalWatchHours,
                                  likeRate,
                                  coinRate,
                                  favoriteRate,
                                  shareRate,
                                  activeDays,
                                  nightWatchMinutes,
                                  mainCategoryID,
                                  mainUPID
                           FROM user_statistics
                           ORDER BY userID
                           """)
            records = cursor.fetchall()

            print(
                f"{'userID':<8} {'总时长':<10} {'点赞率':<8} {'投币率':<8} {'收藏率':<8} {'转发率':<8} {'活跃天':<8} {'夜间分钟':<10} {'主分区':<8} {'主UP'}")
            print("-" * 100)
            for record in records:
                print(
                    f"{record[0]:<8} {record[1]:<10} {record[2]:<8.3f} {record[3]:<8.3f} {record[4]:<8.3f} {record[5]:<8.3f} "
                    f"{record[6]:<8} {record[7]:<10} {record[8]:<8} {record[9]:<8}")

    except Exception as e:
        print(f"❌ 插入数据失败: {e}")
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
            cursor.execute("SHOW TABLES LIKE 'user_statistics'")
            table_exists = cursor.fetchone()

            if table_exists:
                print("✅ user_statistics表存在")

                # 检查记录数
                cursor.execute("SELECT COUNT(*) as count FROM user_statistics")
                count = cursor.fetchone()[0]
                print(f"📊 当前表中有 {count} 条记录")

                # 显示userID范围
                cursor.execute("SELECT MIN(userID) as min_id, MAX(userID) as max_id FROM user_statistics")
                ids = cursor.fetchone()
                print(f"👥 userID范围: {ids[0]} - {ids[1]}")
            else:
                print("❌ user_statistics表不存在")

    except Exception as e:
        print(f"❌ 检查表状态失败: {e}")
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    while True:
        print("\n" + "=" * 60)
        print("📊 User Statistics 数据管理")
        print("=" * 60)
        print("1. 检查表状态")
        print("2. 插入测试数据 (userID 1-11)")
        print("0. 退出")
        print("-" * 60)

        choice = input("请选择操作 (0-2): ").strip()

        if choice == '1':
            check_table_status()

        elif choice == '2':
            confirm = input("⚠️ 确定要插入测试数据吗？这将覆盖现有数据 (y/N): ").strip().lower()
            if confirm == 'y':
                insert_user_statistics_data()
            else:
                print("❌ 操作已取消")

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")


if __name__ == "__main__":
    main()