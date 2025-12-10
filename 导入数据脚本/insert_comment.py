import pandas as pd
import pymysql
from sqlalchemy import create_engine


def import_csv_to_mysql(csv_file_path):
    """
    将CSV文件数据导入到MySQL数据库的comments表中
    """
    try:
        # 读取CSV文件
        df = pd.read_csv(csv_file_path, encoding='utf-8')

        # 显示数据基本信息
        print(f"CSV文件行数: {len(df)}")
        print("数据列信息:")
        print(df.columns.tolist())
        print("\n前5行数据:")
        print(df.head())

        # 数据清洗和处理
        # 重命名列名以匹配数据库表结构
        df = df.rename(columns={
            'commentID': 'commentId',
            'videoID': 'videoId',
            'userID': 'userId',
            'parentID': 'parentId',
            'content': 'content'
        })

        # 选择需要的列
        df = df[['commentId', 'videoId', 'userId', 'content', 'parentId']]

        # 连接数据库
        connection = pymysql.connect(
            host='47.100.240.111',
            port=3306,
            user='root',
            password='Db123456',
            database='b_friend_rec',
            charset='utf8mb4'
        )

        print("\n数据库连接成功!")

        # 使用SQLAlchemy创建引擎（用于pandas的to_sql方法）
        engine = create_engine('mysql+pymysql://root:Db123456@47.100.240.111:3306/b_friend_rec?charset=utf8mb4')

        # 将数据导入数据库
        # if_exists参数：'fail'=如果表存在则失败, 'replace'=替换表, 'append'=追加数据
        df.to_sql('comments', con=engine, if_exists='append', index=False, method='multi')

        print(f"成功导入 {len(df)} 条数据到comments表!")

    except Exception as e:
        print(f"导入过程中出现错误: {str(e)}")
    finally:
        if 'connection' in locals() and connection:
            connection.close()
            print("数据库连接已关闭")


# 使用示例
if __name__ == "__main__":
    csv_file_path = "BV1ZoCkBxELB_comments.csv"  # 替换为你的CSV文件路径
    import_csv_to_mysql(csv_file_path)