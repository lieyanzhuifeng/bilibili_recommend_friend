import pymysql
import random
from datetime import datetime, timedelta


def insert_user_follow_up_data():
    """向user_follow_up表插入关注数据"""
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
            cursor.execute("DELETE FROM user_follow_up")
            print("🗑️ 已清空user_follow_up表")

            # 定义关注时间
            follow_times = [
                '2021-11-15',  # 2021年11月
                '2021-11-20',  # 2021年11月
                '2025-01-03',  # 2025年1月3日
                '2025-01-09',  # 2025年1月9日
                '2025-11-15',  # 2025年11月15日
                '2025-11-15'  # 2025年11月15日
            ]

            # 生成4个随机日期（2020-2025年之间）
            for _ in range(4):
                year = random.randint(2020, 2025)
                month = random.randint(1, 12)
                day = random.randint(1, 28)  # 避免2月29日问题
                random_date = f"{year}-{month:02d}-{day:02d}"
                follow_times.append(random_date)

            # 打乱顺序
            random.shuffle(follow_times)

            # 为用户1-10插入关注数据
            for user_id in range(1, 11):
                follow_time = follow_times[user_id - 1]  # 按顺序分配关注时间
                sql = "INSERT INTO user_follow_up (userID, upID, followTime) VALUES (%s, %s, %s)"
                cursor.execute(sql, (user_id, 11, follow_time))
                print(f"✅ 用户 {user_id} 关注了UP主 11，关注时间: {follow_time}")

            # 提交事务
            connection.commit()
            print(f"\n✅ 插入完成！成功插入 10 条关注记录")

            # 显示统计信息
            print("\n📊 关注数据统计:")
            cursor.execute("""
                           SELECT COUNT(DISTINCT userID) as unique_users,
                                  COUNT(DISTINCT upID)   as unique_ups,
                                  MIN(followTime)        as earliest_follow,
                                  MAX(followTime)        as latest_follow
                           FROM user_follow_up
                           """)
            stats = cursor.fetchone()

            print(f"👥 涉及用户数: {stats[0]}")
            print(f"🎬 涉及UP主数: {stats[1]}")
            print(f"📅 最早关注: {stats[2]}")
            print(f"📅 最晚关注: {stats[3]}")

            # 显示所有记录
            print("\n📋 关注数据详情:")
            cursor.execute("""
                           SELECT userID, upID, followTime
                           FROM user_follow_up
                           ORDER BY userID
                           """)
            preview_data = cursor.fetchall()

            print(f"{'用户ID':<8} {'UP主ID':<8} {'关注时间':<12}")
            print("-" * 30)
            for record in preview_data:
                print(f"{record[0]:<8} {record[1]:<8} {record[2]:<12}")

    except Exception as e:
        print(f"❌ 插入数据失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    print("👥 开始插入用户关注数据...")
    insert_user_follow_up_data()
    print("🎉 关注数据插入完成！")


if __name__ == "__main__":
    main()