import pandas as pd
from datetime import datetime, timedelta

# 读取CSV文件
df = pd.read_csv('smalldata1.csv')

# 1. 处理datetime列：提取日期部分
df['datetime'] = pd.to_datetime(df['datetime']).dt.date

# 2. 处理duration列：将秒数转换为time格式（HH:MM:SS）
def seconds_to_time(seconds):
    # 处理NaN或非数值
    if pd.isna(seconds):
        return None
    # 转换为整数秒数
    seconds = int(float(seconds))
    # 转换为timedelta
    td = timedelta(seconds=seconds)
    # 格式化时间为 HH:MM:SS
    hours, remainder = divmod(td.seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    # 如果有天数，加入小时中
    hours += td.days * 24
    return f"{hours:02d}:{minutes:02d}:{seconds:02d}"

# 应用转换函数
df['duration'] = df['duration'].apply(seconds_to_time)

# 保存到新文件
output_path = 'user_watch_log.csv'
df.to_csv(output_path, index=False)

print(f"处理完成，文件已保存至: {output_path}")
print(f"处理前数据示例:\n{df.head()}")