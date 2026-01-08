import pymysql
from datetime import datetime


class DatabaseManager:
    def __init__(self):
        self.connection = None
        self.connect()

    def connect(self):
        """建立数据库连接"""
        try:
            self.connection = pymysql.connect(
                host='47.100.240.111',
                port=3306,
                user='root',
                password='Db123456',
                database='b_friend_rec',
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            print("数据库连接成功！")
        except Exception as e:
            print(f"数据库连接失败: {e}")
            raise

    def insert_users_data(self):
        """插入50条用户数据"""
        if not self.connection:
            print("数据库未连接！")
            return

        cursor = None
        try:
            cursor = self.connection.cursor()

            # 插入50条数据
            for user_id in range(1, 51):
                username = f"用户{user_id}"

                sql = """
                INSERT INTO users 
                (userID, username, registerTime, avatarPath, avatar_path, register_time) 
                VALUES (%s, %s, %s, %s, %s, %s)
                """

                # 参数：userID是1到50，username是"用户1"到"用户50"，其他字段为NULL
                params = (user_id, username, None, None, None, None)

                cursor.execute(sql, params)

            # 提交事务
            self.connection.commit()
            print(f"成功插入{50}条用户数据！")

        except Exception as e:
            # 发生错误时回滚
            if self.connection:
                self.connection.rollback()
            print(f"插入数据时发生错误: {e}")

        finally:
            if cursor:
                cursor.close()

    def close(self):
        """关闭数据库连接"""
        if self.connection:
            self.connection.close()
            print("数据库连接已关闭。")

    def verify_data(self):
        """验证数据是否成功插入"""
        if not self.connection:
            print("数据库未连接！")
            return

        cursor = None
        try:
            cursor = self.connection.cursor()
            cursor.execute("SELECT COUNT(*) as count FROM users")
            result = cursor.fetchone()
            print(f"当前users表中共有{result['count']}条记录")

            # 查看前5条记录
            cursor.execute("SELECT * FROM users LIMIT 5")
            records = cursor.fetchall()
            print("\n前5条记录示例:")
            for record in records:
                print(f"userID: {record['userID']}, username: {record['username']}")

        except Exception as e:
            print(f"验证数据时发生错误: {e}")

        finally:
            if cursor:
                cursor.close()


def main():
    db_manager = None

    try:
        # 创建数据库管理器实例
        db_manager = DatabaseManager()

        # 插入50条用户数据
        db_manager.insert_users_data()

        # 验证数据
        db_manager.verify_data()

    except Exception as e:
        print(f"程序执行过程中发生错误: {e}")

    finally:
        # 关闭数据库连接
        if db_manager:
            db_manager.close()


if __name__ == "__main__":
    main()