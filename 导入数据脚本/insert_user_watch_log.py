import pymysql
import random
from datetime import datetime, timedelta
import time


class WatchLogGenerator:
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

    def get_video_durations(self):
        """获取所有视频的ID和时长（返回秒数）"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT videoID, duration FROM videos")
                videos = cursor.fetchall()
                # 将timedelta转换为秒数
                video_durations = {}
                for video in videos:
                    if isinstance(video['duration'], timedelta):
                        video_durations[video['videoID']] = int(video['duration'].total_seconds())
                    else:
                        video_durations[video['videoID']] = video['duration']
                print(f"📹 获取到 {len(video_durations)} 个视频的时长信息")
                return video_durations
        except Exception as e:
            print(f"❌ 获取视频时长失败: {e}")
            return {}

    def get_user_ids(self):
        """获取所有用户ID"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SELECT userID FROM users")
                users = cursor.fetchall()
                user_ids = [user['userID'] for user in users]
                print(f"👥 获取到 {len(user_ids)} 个用户")
                return user_ids
        except Exception as e:
            print(f"❌ 获取用户列表失败: {e}")
            return []

    def generate_watch_duration(self, video_duration, watch_behavior_type=None):
        """
        生成合理的观看时长
        watch_behavior_type: 观看行为类型
        - 'complete': 完整观看
        - 'skim': 快速浏览
        - 'partial': 部分观看
        - 'drop': 很快退出
        """
        if watch_behavior_type is None:
            # 随机选择观看行为
            weights = [0.3, 0.4, 0.2, 0.1]  # 完整观看30%，浏览40%，部分20%，退出10%
            behaviors = ['complete', 'skim', 'partial', 'drop']
            watch_behavior_type = random.choices(behaviors, weights=weights)[0]

        if watch_behavior_type == 'complete':
            # 完整观看：时长的80%-100%
            return int(video_duration * random.uniform(0.8, 1.0))
        elif watch_behavior_type == 'skim':
            # 快速浏览：时长的20%-50%
            return int(video_duration * random.uniform(0.2, 0.5))
        elif watch_behavior_type == 'partial':
            # 部分观看：时长的50%-80%
            return int(video_duration * random.uniform(0.5, 0.8))
        elif watch_behavior_type == 'drop':
            # 很快退出：时长的5%-20%
            return int(video_duration * random.uniform(0.05, 0.2))

    def generate_watch_date(self, days_back=365):
        """生成观看日期（过去days_back天内）"""
        end_date = datetime.now()
        start_date = end_date - timedelta(days=days_back)

        random_days = random.randint(0, days_back)
        random_date = start_date + timedelta(days=random_days)

        return random_date.strftime('%Y-%m-%d')

    def insert_watch_log(self, user_id, video_id, watch_date, watch_duration_seconds):
        """插入观看记录（watch_duration_seconds是秒数）"""
        try:
            with self.connection.cursor() as cursor:
                # 将秒数转换为TIME格式 (HH:MM:SS)
                hours = watch_duration_seconds // 3600
                minutes = (watch_duration_seconds % 3600) // 60
                seconds = watch_duration_seconds % 60
                duration_formatted = f"{hours:02d}:{minutes:02d}:{seconds:02d}"

                sql = """
                      INSERT INTO user_watch_log
                          (userID, videoID, watchDate, watchduration)
                      VALUES (%s, %s, %s, %s) \
                      """
                cursor.execute(sql, (user_id, video_id, watch_date, duration_formatted))
            self.connection.commit()
            return True
        except Exception as e:
            print(f"❌ 插入观看记录失败: {e}")
            return False

    def generate_watch_logs(self, num_records=1000):
        """生成观看记录"""
        print(f"🎬 开始生成 {num_records} 条观看记录...")

        # 获取基础数据
        video_durations = self.get_video_durations()
        user_ids = self.get_user_ids()

        if not video_durations or not user_ids:
            print("❌ 无法获取基础数据，请检查数据库")
            return

        successful_records = 0

        for i in range(num_records):
            # 随机选择用户和视频
            user_id = random.choice(user_ids)
            video_id = random.choice(list(video_durations.keys()))
            video_duration = video_durations[video_id]

            # 生成合理的观看时长
            watch_duration = self.generate_watch_duration(video_duration)

            # 确保观看时长不超过视频时长且至少1秒
            watch_duration = max(1, min(watch_duration, video_duration))

            # 生成观看日期
            watch_date = self.generate_watch_date()

            # 插入记录
            if self.insert_watch_log(user_id, video_id, watch_date, watch_duration):
                successful_records += 1

            # 进度显示
            if (i + 1) % 100 == 0:
                print(f"📊 已生成 {i + 1}/{num_records} 条记录")

        print(f"✅ 观看记录生成完成！成功插入 {successful_records} 条记录")

    def generate_realistic_watch_patterns(self):
        """生成更真实的观看模式"""
        print("🎯 生成更真实的观看模式...")

        video_durations = self.get_video_durations()
        user_ids = self.get_user_ids()

        if not video_durations or not user_ids:
            return

        # 为每个用户生成观看记录
        for user_index, user_id in enumerate(user_ids):
            user_records = random.randint(50, 200)  # 每个用户50-200条记录

            # 用户可能重复观看某些视频
            user_videos = random.sample(list(video_durations.keys()),
                                        min(30, len(video_durations)))  # 每个用户看30个不同视频

            for _ in range(user_records):
                video_id = random.choice(user_videos)
                video_duration = video_durations[video_id]

                # 对于重复观看，可能观看更完整
                watch_behavior = random.choices(
                    ['complete', 'skim', 'partial', 'drop'],
                    weights=[0.4, 0.3, 0.2, 0.1]  # 重复观看时更可能完整观看
                )[0]

                watch_duration = self.generate_watch_duration(video_duration, watch_behavior)
                watch_duration = max(1, min(watch_duration, video_duration))
                watch_date = self.generate_watch_date()

                self.insert_watch_log(user_id, video_id, watch_date, watch_duration)

            if (user_index + 1) % 10 == 0:
                print(f"👤 已处理 {user_index + 1}/{len(user_ids)} 个用户")

        print("✅ 真实观看模式生成完成！")


def main():
    """主函数"""
    generator = WatchLogGenerator()

    if not generator.connection:
        return

    while True:
        print("\n" + "=" * 50)
        print("📊 观看记录生成器")
        print("=" * 50)
        print("1. 生成随机观看记录")
        print("2. 生成真实观看模式（推荐）")
        print("3. 查看当前观看记录统计")
        print("0. 退出")
        print("-" * 50)

        choice = input("请选择操作 (0-3): ").strip()

        if choice == '1':
            try:
                num = int(input("请输入要生成的记录数量 (默认1000): ") or "1000")
                generator.generate_watch_logs(num)
            except ValueError:
                print("❌ 请输入有效数字")

        elif choice == '2':
            generator.generate_realistic_watch_patterns()

        elif choice == '3':
            # 查看统计信息
            try:
                with generator.connection.cursor() as cursor:
                    cursor.execute("SELECT COUNT(*) as total FROM user_watch_log")
                    total = cursor.fetchone()['total']
                    print(f"📈 当前观看记录总数: {total}")

                    cursor.execute("""
                                   SELECT COUNT(DISTINCT userID)  as users,
                                          COUNT(DISTINCT videoID) as videos
                                   FROM user_watch_log
                                   """)
                    stats = cursor.fetchone()
                    print(f"👥 涉及用户数: {stats['users']}")
                    print(f"🎬 涉及视频数: {stats['videos']}")
            except Exception as e:
                print(f"❌ 获取统计信息失败: {e}")

        elif choice == '0':
            print("👋 再见！")
            break

        else:
            print("❌ 无效选择")



def test_user_watch_log():
    """测试userID为11的用户的观看记录操作"""

    def connect_db():
        """连接数据库"""
        try:
            connection = pymysql.connect(
                host='47.100.240.111',
                port=3306,
                user='root',
                password='Db123456',
                database='b_friend_rec',
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            return connection
        except Exception as e:
            print(f"❌ 数据库连接失败: {e}")
            return None

    def get_video_durations(connection):
        """获取视频ID和时长"""
        try:
            with connection.cursor() as cursor:
                cursor.execute("SELECT videoID, duration FROM videos")
                videos = cursor.fetchall()
                video_durations = {video['videoID']: video['duration'] for video in videos}
                return video_durations
        except Exception as e:
            print(f"❌ 获取视频时长失败: {e}")
            return {}

    def format_timedelta(td):
        """将timedelta对象格式化为MM:SS字符串"""
        if isinstance(td, timedelta):
            total_seconds = int(td.total_seconds())
            minutes = total_seconds // 60
            seconds = total_seconds % 60
            return f"{minutes:02d}:{seconds:02d}"
        else:
            return str(td)

    def print_table(connection, title):
        """打印user_watch_log表内容"""
        try:
            with connection.cursor() as cursor:
                cursor.execute("""
                               SELECT logID, userID, videoID, watchDate, watchduration
                               FROM user_watch_log
                               ORDER BY logID DESC
                               """)
                records = cursor.fetchall()

                print(f"\n{title}")
                print("=" * 90)
                if records:
                    print(f"{'logID':<8} {'userID':<8} {'videoID':<8} {'watchDate':<20} {'watchduration':<12}")
                    print("-" * 90)
                    for record in records:
                        duration_str = format_timedelta(record['watchduration'])
                        print(
                            f"{record['logID']:<8} {record['userID']:<8} {record['videoID']:<8} {str(record['watchDate']):<20} {duration_str:<12}")
                else:
                    print("📭 表为空，没有记录")
                print("=" * 90)

        except Exception as e:
            print(f"❌ 打印表格失败: {e}")

    def insert_test_data(connection):
        """为userID=11插入3条测试数据"""
        try:
            with connection.cursor() as cursor:
                # 获取视频数据
                video_durations = get_video_durations(connection)
                if not video_durations:
                    print("❌ 没有找到可用的视频数据")
                    return False

                # 随机选择3个视频
                user_videos = random.sample(list(video_durations.keys()), min(3, len(video_durations)))
                print(f"🎬 随机选择的视频ID: {user_videos}")

                # 插入3条测试数据，使用真实的视频时长
                test_records = []
                for i, video_id in enumerate(user_videos):
                    # 生成合理的观看时长（完整观看的80%-100%）
                    video_duration = video_durations[video_id]
                    if isinstance(video_duration, timedelta):
                        total_seconds = int(video_duration.total_seconds())
                        watch_seconds = int(total_seconds * random.uniform(0.8, 1.0))

                        # 转换为TIME格式 (HH:MM:SS)
                        hours = watch_seconds // 3600
                        minutes = (watch_seconds % 3600) // 60
                        seconds = watch_seconds % 60
                        duration_formatted = f"{hours:02d}:{minutes:02d}:{seconds:02d}"
                    else:
                        duration_formatted = "00:05:00"  # 默认5分钟

                    test_records.append((11, video_id, f'2024-01-{15 + i}', duration_formatted))

                sql = """
                      INSERT INTO user_watch_log
                          (userID, videoID, watchDate, watchduration)
                      VALUES (%s, %s, %s, %s) \
                      """

                for record in test_records:
                    cursor.execute(sql, record)
                    original_duration = format_timedelta(video_durations[record[1]])
                    print(
                        f"✅ 插入记录: userID={record[0]}, videoID={record[1]}, date={record[2]}, 观看时长={record[3]} (视频总时长: {original_duration})")

                connection.commit()
                return True

        except Exception as e:
            print(f"❌ 插入测试数据失败: {e}")
            connection.rollback()
            return False

    def delete_test_data(connection):
        """删除userID=11的测试数据"""
        try:
            with connection.cursor() as cursor:
                # 先查看要删除的数据
                cursor.execute("SELECT COUNT(*) as count FROM user_watch_log WHERE userID = 11")
                count_before = cursor.fetchone()['count']

                if count_before == 0:
                    print("ℹ️ 没有找到userID=11的记录，无需删除")
                    return True

                print(f"🗑️ 准备删除 {count_before} 条userID=11的记录")

                # 执行删除
                cursor.execute("DELETE FROM user_watch_log WHERE userID = 11")
                deleted_count = cursor.rowcount
                connection.commit()

                print(f"✅ 成功删除 {deleted_count} 条记录")
                return True

        except Exception as e:
            print(f"❌ 删除测试数据失败: {e}")
            connection.rollback()
            return False

    # 主测试流程
    print("🧪 开始测试 user_watch_log 表操作")
    print("=" * 50)

    # 连接数据库
    connection = connect_db()
    if not connection:
        return

    try:
        # 步骤1: 查看初始状态
        print("\n📋 步骤1: 查看初始表状态")
        print_table(connection, "初始表内容")

        # 步骤2: 插入测试数据
        print("\n📥 步骤2: 插入测试数据")
        if insert_test_data(connection):
            print_table(connection, "插入测试数据后的表内容")

        # 步骤3: 删除测试数据
        print("\n🗑️ 步骤3: 删除测试数据")
        if delete_test_data(connection):
            print_table(connection, "删除测试数据后的表内容")

        print("\n🎉 测试完成！")

    except Exception as e:
        print(f"❌ 测试过程中发生错误: {e}")
    finally:
        if connection and connection.open:
            connection.close()
            print("✅ 数据库连接已关闭")



if __name__ == "__main__":
    main()
    # test_user_watch_log()
