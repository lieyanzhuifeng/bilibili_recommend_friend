import pymysql
import pandas as pd
import os


class SimpleCSVImporter:
    def __init__(self):
        self.connection = pymysql.connect(
            host='47.100.240.111',
            port=3306,
            user='root',
            password='Db123456',
            database='b_friend_rec',
            charset='utf8mb4'
        )

    def import_csv_file(self, csv_file_path):
        """简化版CSV导入"""
        try:
            # 1. 使用pandas读取CSV
            print(f"正在读取文件: {csv_file_path}")
            df = pd.read_csv(csv_file_path)
            print(f"读取到 {len(df)} 条记录")

            # 2. 准备数据
            data_tuples = []
            for _, row in df.iterrows():
                data_tuples.append((
                    int(row['logid']),
                    int(row['userID']),
                    int(row['videoID']),
                    str(row['watchDate']),
                    str(row['watchDuration']) if pd.notna(row['watchDuration']) else None
                ))

            print(f"准备导入 {len(data_tuples)} 条数据")

            # 3. 导入数据
            cursor = self.connection.cursor()

            sql = """
            INSERT INTO user_watch_log (logid, userID, videoID, watchDate, watchDuration)
            VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                userID = VALUES(userID),
                videoID = VALUES(videoID),
                watchDate = VALUES(watchDate),
                watchDuration = VALUES(watchDuration)
            """

            # 批量插入
            cursor.executemany(sql, data_tuples)
            self.connection.commit()

            print(f"成功导入 {len(data_tuples)} 条数据")

            cursor.close()
            return True

        except Exception as e:
            print(f"导入失败: {str(e)}")
            if self.connection:
                self.connection.rollback()
            return False

    def close(self):
        if self.connection:
            self.connection.close()


# 使用简化版
if __name__ == "__main__":
    csv_file = "user_watch_log.csv"

    if os.path.exists(csv_file):
        importer = SimpleCSVImporter()
        success = importer.import_csv_file(csv_file)
        importer.close()

        if success:
            print("导入成功!")
        else:
            print("导入失败")
    else:
        print(f"文件不存在: {csv_file}")