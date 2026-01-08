import pymysql
import pandas as pd
import sys
from datetime import datetime


class DataImporter:
    def __init__(self):
        self.connection = None

    def connect(self):
        """连接数据库"""
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
            print("✅ 数据库连接成功!")
            return True
        except Exception as e:
            print(f"❌ 数据库连接失败: {e}")
            return False

    def close(self):
        """关闭数据库连接"""
        if self.connection:
            self.connection.close()
            print("✅ 数据库连接已关闭")

    def import_user_favorites(self, csv_file='user_favorites.csv'):
        """导入用户收藏数据"""
        print(f"\n📥 开始导入 {csv_file} 数据到 user_favorites 表...")

        try:
            # 读取CSV文件
            df = pd.read_csv(csv_file)
            print(f"📊 读取到 {len(df)} 条数据")

            # 数据验证
            required_columns = ['userID', 'videoID', 'favorite_time']
            if not all(col in df.columns for col in required_columns):
                print(f"❌ CSV文件缺少必要列，需要的列: {required_columns}")
                return False

            cursor = self.connection.cursor()
            inserted_count = 0
            error_count = 0

            # 准备SQL语句
            sql = """
            INSERT INTO user_favorites (userID, videoID, favorite_time) 
            VALUES (%s, %s, %s)
            """

            # 批量插入
            batch_size = 1000
            for i in range(0, len(df), batch_size):
                batch = df.iloc[i:i + batch_size]
                batch_data = []

                for _, row in batch.iterrows():
                    try:
                        # 处理数据
                        user_id = int(row['userID'])
                        video_id = int(row['videoID'])

                        # 处理时间，确保格式正确
                        favorite_time = row['favorite_time']
                        if pd.isna(favorite_time):
                            favorite_time = None

                        batch_data.append((user_id, video_id, favorite_time))
                    except Exception as e:
                        print(f"❌ 数据处理错误: {e}")
                        error_count += 1

                if batch_data:
                    try:
                        cursor.executemany(sql, batch_data)
                        self.connection.commit()
                        inserted_count += len(batch_data)
                        print(f"✅ 已插入 {inserted_count}/{len(df)} 条数据")
                    except pymysql.Error as e:
                        print(f"❌ 批量插入失败: {e}")
                        self.connection.rollback()
                        error_count += len(batch_data)

            print(f"\n🎉 导入完成!")
            print(f"✅ 成功插入: {inserted_count} 条")
            print(f"❌ 失败: {error_count} 条")
            return True

        except FileNotFoundError:
            print(f"❌ 文件 {csv_file} 不存在")
            return False
        except Exception as e:
            print(f"❌ 导入过程出错: {e}")
            return False

    def import_user_follow_up(self, csv_file='user_follow_up.csv'):
        """导入用户关注UP主数据"""
        print(f"\n📥 开始导入 {csv_file} 数据到 user_follow_up 表...")

        try:
            # 读取CSV文件
            df = pd.read_csv(csv_file)
            print(f"📊 读取到 {len(df)} 条数据")

            # 数据验证
            required_columns = ['userID', 'upID', 'followTime']
            if not all(col in df.columns for col in required_columns):
                print(f"❌ CSV文件缺少必要列，需要的列: {required_columns}")
                return False

            cursor = self.connection.cursor()
            inserted_count = 0
            error_count = 0

            # 准备SQL语句
            sql = """
            INSERT INTO user_follow_up (userID, upID, followTime) 
            VALUES (%s, %s, %s)
            """

            # 批量插入
            batch_size = 1000
            for i in range(0, len(df), batch_size):
                batch = df.iloc[i:i + batch_size]
                batch_data = []

                for _, row in batch.iterrows():
                    try:
                        # 处理数据
                        user_id = int(row['userID'])
                        up_id = int(row['upID'])

                        # 处理时间，确保格式正确
                        follow_time = row['followTime']
                        if pd.isna(follow_time):
                            follow_time = None

                        batch_data.append((user_id, up_id, follow_time))
                    except Exception as e:
                        print(f"❌ 数据处理错误: {e}")
                        error_count += 1

                if batch_data:
                    try:
                        cursor.executemany(sql, batch_data)
                        self.connection.commit()
                        inserted_count += len(batch_data)
                        print(f"✅ 已插入 {inserted_count}/{len(df)} 条数据")
                    except pymysql.Error as e:
                        print(f"❌ 批量插入失败: {e}")
                        self.connection.rollback()
                        error_count += len(batch_data)

            print(f"\n🎉 导入完成!")
            print(f"✅ 成功插入: {inserted_count} 条")
            print(f"❌ 失败: {error_count} 条")
            return True

        except FileNotFoundError:
            print(f"❌ 文件 {csv_file} 不存在")
            return False
        except Exception as e:
            print(f"❌ 导入过程出错: {e}")
            return False

    def import_all_data(self):
        """导入所有数据"""
        print("🚀 开始导入所有数据...")

        if not self.connect():
            return False

        try:
            # 导入user_favorites
            self.import_user_favorites()

            # 导入user_follow_up
            self.import_user_follow_up()

            print("\n🎉 所有数据导入完成!")
            return True

        except Exception as e:
            print(f"❌ 导入过程中出现错误: {e}")
            return False
        finally:
            self.close()


# 使用示例
if __name__ == "__main__":
    importer = DataImporter()

    # 导入所有数据
    importer.import_all_data()