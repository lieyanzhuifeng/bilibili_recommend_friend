import pymysql


class DatabaseExplorer:
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

    def get_all_tables(self):
        """获取所有表名"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute("SHOW TABLES")
                tables = [list(table.values())[0] for table in cursor.fetchall()]
                return tables
        except Exception as e:
            print(f"❌ 获取表列表失败: {e}")
            return []

    def get_table_columns(self, table_name):
        """获取表的列信息"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"DESCRIBE {table_name}")
                columns = cursor.fetchall()
                return columns
        except Exception as e:
            print(f"❌ 获取表结构失败: {e}")
            return []

    def get_table_data(self, table_name, limit=10):
        """获取表的数据"""
        try:
            with self.connection.cursor() as cursor:
                cursor.execute(f"SELECT * FROM {table_name} LIMIT {limit}")
                data = cursor.fetchall()
                return data
        except Exception as e:
            print(f"❌ 获取表数据失败: {e}")
            return []

    def print_all_tables_and_columns(self):
        """打印所有表和列名"""
        print("\n" + "=" * 60)
        print("📊 所有表和列结构")
        print("=" * 60)

        tables = self.get_all_tables()
        if not tables:
            print("❌ 没有找到任何表")
            return

        for i, table_name in enumerate(tables, 1):
            print(f"\n{i}. 📋 表名: {table_name}")
            print("-" * 40)

            columns = self.get_table_columns(table_name)
            if columns:
                print("   字段列表:")
                for col in columns:
                    field = col['Field']
                    field_type = col['Type']
                    is_null = "NULL" if col['Null'] == 'YES' else "NOT NULL"
                    key = col['Key']
                    default = col['Default'] or 'NULL'

                    print(f"     ├─ {field:20} {field_type:15} {is_null:10} "
                          f"Key: {key:5} Default: {default}")
            else:
                print("   ⚠ 无法获取字段信息")

        print(f"\n总计: {len(tables)} 个表")

    def print_table_data(self, table_name):
        """打印指定表的数据"""
        print(f"\n" + "=" * 60)
        print(f"📄 表数据: {table_name}")
        print("=" * 60)

        # 先获取列信息
        columns = self.get_table_columns(table_name)
        if not columns:
            print("❌ 无法获取表结构")
            return

        # 获取数据
        data = self.get_table_data(table_name, limit=200)
        if not data:
            print("ℹ 表中没有数据或无法读取")
            return

        # 打印列头
        column_names = [col['Field'] for col in columns]
        print(" | ".join(f"{name:15}" for name in column_names))
        print("-" * (len(column_names) * 18))

        # 打印数据
        for row in data:
            row_values = []
            for col_name in column_names:
                value = row.get(col_name, '')
                # 处理长文本和None值
                if value is None:
                    value = 'NULL'
                else:
                    value = str(value)
                    if len(value) > 23:
                        value = value[:20] + '...'
                row_values.append(f"{value:23}")
            print(" | ".join(row_values))

        print(f"\n总计: {len(data)} 条记录 (最多显示200条)")

    def show_table_list(self):
        """显示表列表供选择"""
        tables = self.get_all_tables()
        if not tables:
            print("❌ 没有找到任何表")
            return None

        print("\n📋 可用表列表:")
        for i, table_name in enumerate(tables, 1):
            print(f"  {i}. {table_name}")

        return tables

    def run(self):
        """运行控制台程序"""
        if not self.connection:
            print("❌ 数据库连接失败，程序退出")
            return

        while True:
            print("\n" + "=" * 50)
            print("🔍 数据库浏览器")
            print("=" * 50)
            print("1. 查看所有表和列结构")
            print("2. 查看指定表的数据")
            print("3. 刷新数据库连接")
            print("0. 退出程序")
            print("-" * 50)

            choice = input("请选择操作 (0-3): ").strip()

            if choice == '1':
                self.print_all_tables_and_columns()

            elif choice == '2':
                tables = self.show_table_list()
                if tables:
                    try:
                        table_choice = input("请输入表名或编号: ").strip()
                        if table_choice.isdigit():
                            index = int(table_choice) - 1
                            if 0 <= index < len(tables):
                                table_name = tables[index]
                            else:
                                print("❌ 编号超出范围")
                                continue
                        else:
                            table_name = table_choice

                        if table_name in tables:
                            self.print_table_data(table_name)
                        else:
                            print("❌ 表不存在，请检查表名")
                    except ValueError:
                        print("❌ 请输入有效的数字")
                    except Exception as e:
                        print(f"❌ 发生错误: {e}")

            elif choice == '3':
                self.connection.close()
                if self.connect():
                    print("✅ 数据库连接已刷新")

            elif choice == '0':
                print("👋 再见！")
                break

            else:
                print("❌ 无效选择，请重新输入")

        # 关闭连接
        if self.connection:
            self.connection.close()
            print("✅ 数据库连接已关闭")


def main():
    """主函数"""
    print("🚀 启动数据库浏览器...")
    explorer = DatabaseExplorer()
    explorer.run()


if __name__ == "__main__":
    main()