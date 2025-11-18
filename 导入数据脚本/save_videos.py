import pymysql
import csv
from datetime import datetime


class SimpleVideoExporter:
    def __init__(self):
        self.connection = None
        self.connect()

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

    def export_videos_to_csv(self):
        """直接导出videos表到CSV"""
        try:
            with self.connection.cursor() as cursor:
                # 直接查询videos表的所有数据
                cursor.execute("SELECT * FROM videos")
                videos = cursor.fetchall()
                print(f"📹 获取到 {len(videos)} 条视频记录")

                if not videos:
                    print("❌ 没有找到视频数据")
                    return False

                # 生成CSV文件名
                filename = "videos1.csv"

                # 写入CSV文件
                with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
                    # 获取字段名
                    fieldnames = videos[0].keys()
                    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

                    # 写入表头
                    writer.writeheader()

                    # 写入数据
                    for video in videos:
                        writer.writerow(video)

                print(f"✅ videos表已成功导出到: {filename}")
                print(f"📊 包含字段: {', '.join(fieldnames)}")
                return True

        except Exception as e:
            print(f"❌ 导出失败: {e}")
            return False

    def preview_videos(self, limit=5):
        """预览videos表数据"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SELECT * FROM videos LIMIT {limit}")
                videos = cursor.fetchall()

                print(f"\n📺 videos表预览 (前{len(videos)}条):")
                print("=" * 80)

                for i, video in enumerate(videos, 1):
                    print(f"\n记录 {i}:")
                    for key, value in video.items():
                        print(f"  {key}: {value}")
                print("=" * 80)

        except Exception as e:
            print(f"❌ 预览失败: {e}")


def main():
    """主函数"""
    exporter = SimpleVideoExporter()

    if not exporter.connection:
        return

    while True:
        print("\n" + "=" * 50)
        print("💾 Videos表简单导出工具")
        print("=" * 50)
        print("1. 预览videos表数据")
        print("2. 导出videos表到videos.csv")
        print("0. 退出")
        print("-" * 50)

        choice = input("请选择操作 (0-2): ").strip()

        if choice == '1':
            exporter.preview_videos()

        elif choice == '2':
            exporter.export_videos_to_csv()

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")


if __name__ == "__main__":
    main()