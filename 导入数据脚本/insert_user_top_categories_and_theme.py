import pymysql
import random


def get_actual_category_ids(connection):
    """获取实际存在的分区ID"""
    with connection.cursor() as cursor:
        cursor.execute("SELECT categoryID FROM video_category")
        return [row[0] for row in cursor.fetchall()]


def get_actual_theme_ids(connection):
    """获取实际存在的主题ID"""
    with connection.cursor() as cursor:
        cursor.execute("SELECT themeID FROM video_theme")
        return [row[0] for row in cursor.fetchall()]


def simulate_user_top_categories():
    """模拟用户最常看的三个分区数据（使用实际存在的分区ID）"""
    try:
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
            # 清空表
            cursor.execute("DELETE FROM user_top_categories")

            # 获取所有用户ID
            cursor.execute("SELECT userID FROM users")
            users = [row[0] for row in cursor.fetchall()]

            # 获取实际存在的分区ID
            categories = get_actual_category_ids(connection)
            print(f"📊 实际分区ID范围: {min(categories)} - {max(categories)}")

            if len(categories) < 3:
                print("❌ 分区数量不足3个，无法为每个用户选择3个不同分区")
                return

            inserted_count = 0

            for user_id in users:
                # 为每个用户随机选择3个不同的实际分区
                top_categories = random.sample(categories, 3)

                # 生成比例（总和在40%-80%之间）
                total_proportion = random.uniform(0.4, 0.8)
                proportions = []

                # 生成3个随机比例，然后按总比例缩放
                for _ in range(3):
                    proportion = random.uniform(0.1, 0.5)
                    proportions.append(proportion)

                # 按目标总比例缩放
                current_total = sum(proportions)
                proportions = [p * total_proportion / current_total for p in proportions]

                # 插入数据
                for i, category_id in enumerate(top_categories):
                    sql = """
                          INSERT INTO user_top_categories (userID, categoryID, proportion)
                          VALUES (%s, %s, %s) \
                          """
                    cursor.execute(sql, (user_id, category_id, round(proportions[i], 3)))
                    inserted_count += 1

            connection.commit()
            print(f"✅ 模拟完成！成功插入 {inserted_count} 条user_top_categories记录")

    except Exception as e:
        print(f"❌ 模拟失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def simulate_user_top_themes():
    """模拟用户最常看的三个主题数据（使用实际存在的主题ID）"""
    try:
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
            # 清空表
            cursor.execute("DELETE FROM user_top_themes")

            # 获取所有用户ID
            cursor.execute("SELECT userID FROM users")
            users = [row[0] for row in cursor.fetchall()]

            # 获取实际存在的主题ID
            themes = get_actual_theme_ids(connection)
            print(f"🎭 实际主题ID范围: {min(themes)} - {max(themes)}")

            if len(themes) < 3:
                print("❌ 主题数量不足3个，无法为每个用户选择3个不同主题")
                return

            inserted_count = 0

            for user_id in users:
                # 为每个用户随机选择3个不同的实际主题
                top_themes = random.sample(themes, 3)

                # 生成比例（总和在40%-80%之间）
                total_proportion = random.uniform(0.4, 0.8)
                proportions = []

                # 生成3个随机比例，然后按总比例缩放
                for _ in range(3):
                    proportion = random.uniform(0.1, 0.5)
                    proportions.append(proportion)

                # 按目标总比例缩放
                current_total = sum(proportions)
                proportions = [p * total_proportion / current_total for p in proportions]

                # 插入数据
                for i, theme_id in enumerate(top_themes):
                    sql = """
                          INSERT INTO user_top_themes (userID, themeID, proportion)
                          VALUES (%s, %s, %s) \
                          """
                    cursor.execute(sql, (user_id, theme_id, round(proportions[i], 3)))
                    inserted_count += 1

            connection.commit()
            print(f"✅ 模拟完成！成功插入 {inserted_count} 条user_top_themes记录")

    except Exception as e:
        print(f"❌ 模拟失败: {e}")
        if 'connection' in locals():
            connection.rollback()
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def check_available_ids():
    """检查可用的分区和主题ID"""
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
            # 检查分区
            cursor.execute("SELECT categoryID, categoryName FROM video_category")
            categories = cursor.fetchall()
            print(f"📊 可用分区 ({len(categories)}个):")
            for cat in categories:
                print(f"  ID: {cat[0]}, 名称: {cat[1]}")

            # 检查主题
            cursor.execute("SELECT themeID, themeName FROM video_theme")
            themes = cursor.fetchall()
            print(f"\n🎭 可用主题 ({len(themes)}个):")
            for theme in themes:
                print(f"  ID: {theme[0]}, 名称: {theme[1]}")

    except Exception as e:
        print(f"❌ 检查失败: {e}")
    finally:
        if 'connection' in locals() and connection.open:
            connection.close()


def main():
    """主函数"""
    while True:
        print("\n" + "=" * 60)
        print("🎯 用户偏好数据模拟工具")
        print("=" * 60)
        print("1. 检查可用分区和主题ID")
        print("2. 模拟用户最常看分区数据")
        print("3. 模拟用户最常看主题数据")
        print("4. 模拟所有偏好数据")
        print("5. 预览模拟数据")
        print("0. 退出")
        print("-" * 60)

        choice = input("请选择操作 (0-5): ").strip()

        if choice == '1':
            check_available_ids()

        elif choice == '2':
            simulate_user_top_categories()

        elif choice == '3':
            simulate_user_top_themes()

        elif choice == '4':
            simulate_user_top_categories()
            simulate_user_top_themes()

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")


if __name__ == "__main__":
    main()