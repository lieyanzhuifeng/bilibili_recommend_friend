import requests
import json
import time
import csv
import os
import re
from typing import List, Dict, Optional


class BilibiliCommentCrawler:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Referer': 'https://www.bilibili.com/',
        })

    def get_bvid_from_url(self, url: str) -> Optional[str]:
        """从URL中提取BVID"""
        # 使用正则表达式匹配BV号
        bvid_pattern = r'BV[a-zA-Z0-9]{10}'
        match = re.search(bvid_pattern, url)
        if match:
            return match.group()
        return None

    def get_aid_from_bvid(self, bvid: str) -> Optional[int]:
        """将BVID转换为AID"""
        try:
            url = f'https://api.bilibili.com/x/web-interface/view?bvid={bvid}'
            response = self.session.get(url)
            print(f"API请求URL: {url}")
            print(f"响应状态码: {response.status_code}")

            if response.status_code == 200:
                data = response.json()
                print(f"API响应数据: {json.dumps(data, ensure_ascii=False, indent=2)}")

                if data['code'] == 0:
                    aid = data['data']['aid']
                    print(f"成功获取AID: {aid}")
                    return aid
                else:
                    print(f"API返回错误: {data['message']}")
            else:
                print(f"HTTP请求失败: {response.status_code}")
            return None
        except Exception as e:
            print(f"获取AID失败: {e}")
            return None

    def get_comments(self, aid: int, page: int = 1) -> Optional[Dict]:
        """获取评论数据"""
        try:
            url = 'https://api.bilibili.com/x/v2/reply/main'
            params = {
                'jsonp': 'jsonp',
                'next': page,
                'type': 1,  # 1表示视频
                'oid': aid,  # 视频AID
                'mode': 3,  # 排序模式：3按热度，2按时间
                'plat': 1
            }

            print(f"请求评论参数: {params}")
            response = self.session.get(url, params=params, timeout=10)
            print(f"评论API响应状态码: {response.status_code}")

            if response.status_code == 200:
                data = response.json()
                print(f"评论API返回码: {data.get('code')}")
                return data
            return None
        except Exception as e:
            print(f"获取评论失败: {e}")
            return None

    def parse_comments(self, data: Dict, video_bvid: str) -> List[Dict]:
        """解析评论数据"""
        comments = []

        if data['code'] != 0:
            print(f"API返回错误码: {data['code']}, 消息: {data.get('message')}")
            return comments

        replies = data['data'].get('replies', [])
        print(f"找到 {len(replies)} 条一级评论")

        for reply in replies:
            try:
                # 一级评论
                comment_data = {
                    'video_bvid': video_bvid,
                    'comment_id': reply['rpid'],
                    'level': '一级评论',
                    'parent_comment_id': '',
                    'user_name': reply['member']['uname'],
                    'user_id': reply['member']['mid'],
                    'content': reply['content']['message'],
                    'like_count': reply['like'],
                    'ctime': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(reply['ctime'])),
                    'reply_count': reply['rcount']
                }
                comments.append(comment_data)

                # 二级评论（回复）
                if 'replies' in reply and reply['replies']:
                    print(f"  找到 {len(reply['replies'])} 条二级评论")
                    for sub_reply in reply['replies']:
                        sub_comment_data = {
                            'video_bvid': video_bvid,
                            'comment_id': sub_reply['rpid'],
                            'level': '二级评论',
                            'parent_comment_id': reply['rpid'],
                            'user_name': sub_reply['member']['uname'],
                            'user_id': sub_reply['member']['mid'],
                            'content': sub_reply['content']['message'],
                            'like_count': sub_reply['like'],
                            'ctime': time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(sub_reply['ctime'])),
                            'reply_count': 0
                        }
                        comments.append(sub_comment_data)
            except KeyError as e:
                print(f"解析评论时缺少键: {e}")
                continue

        return comments

    def crawl_video_comments(self, video_url: str, max_pages: int = 10) -> List[Dict]:
        """爬取单个视频的评论"""
        print(f"开始爬取视频: {video_url}")

        # 获取BVID
        bvid = self.get_bvid_from_url(video_url)
        if not bvid:
            print(f"无法从URL中提取BVID: {video_url}")
            return []

        print(f"提取到BVID: {bvid}")

        # 获取AID
        aid = self.get_aid_from_bvid(bvid)
        if not aid:
            print(f"无法获取视频AID: {bvid}")
            return []

        print(f"成功获取视频信息 - BVID: {bvid}, AID: {aid}")

        all_comments = []
        page = 1

        while page <= max_pages:
            print(f"正在爬取第 {page} 页评论...")

            data = self.get_comments(aid, page)
            if not data:
                print(f"第 {page} 页评论获取失败")
                break

            comments = self.parse_comments(data, bvid)
            if not comments:
                print(f"第 {page} 页没有更多评论")
                break

            all_comments.extend(comments)
            print(f"第 {page} 页获取到 {len(comments)} 条评论")

            # 检查是否还有更多评论
            cursor = data['data'].get('cursor', {})
            is_end = cursor.get('is_end', True)
            print(f"是否结束: {is_end}")

            if not is_end:
                page += 1
                time.sleep(1)  # 礼貌性延迟
            else:
                print("已到达最后一页")
                break

        print(f"视频 {bvid} 爬取完成，共获取 {len(all_comments)} 条评论")
        return all_comments

    def save_to_csv(self, comments: List[Dict], filename: str):
        """保存评论到CSV文件"""
        if not comments:
            print("没有评论数据可保存")
            return

        fieldnames = ['video_bvid', 'comment_id', 'level', 'parent_comment_id',
                      'user_name', 'user_id', 'content', 'like_count', 'ctime', 'reply_count']

        try:
            with open(filename, 'w', encoding='utf-8-sig', newline='') as f:
                writer = csv.DictWriter(f, fieldnames=fieldnames)
                writer.writeheader()
                writer.writerows(comments)
            print(f"评论已保存到: {filename}")
        except Exception as e:
            print(f"保存文件失败: {e}")

    def save_progress(self, progress: Dict, filename: str = 'progress.json'):
        """保存爬取进度"""
        try:
            with open(filename, 'w', encoding='utf-8') as f:
                json.dump(progress, f, ensure_ascii=False, indent=2)
        except Exception as e:
            print(f"保存进度失败: {e}")

    def load_progress(self, filename: str = 'progress.json') -> Dict:
        """加载爬取进度"""
        try:
            if os.path.exists(filename):
                with open(filename, 'r', encoding='utf-8') as f:
                    return json.load(f)
        except Exception as e:
            print(f"加载进度失败: {e}")
        return {'completed_videos': [], 'current_index': 0}


def test_single_video():
    """测试单个视频"""
    crawler = BilibiliCommentCrawler()

    # 测试视频URL
    test_url = "https://www.bilibili.com/video/BV1ZoCkBxELB"

    print("=== 开始测试 ===")
    comments = crawler.crawl_video_comments(test_url, max_pages=5)

    if comments:
        crawler.save_to_csv(comments, "test_comments.csv")
        print(f"测试完成，共获取 {len(comments)} 条评论")
    else:
        print("测试失败，未获取到评论")


def main():
    """主函数"""
    crawler = BilibiliCommentCrawler()

    # 读取视频列表
    if not os.path.exists('video_list.txt'):
        print("请创建 video_list.txt 文件，每行一个B站视频链接")
        print("示例内容:")
        print("https://www.bilibili.com/video/BV1ZoCkBxELB")
        print("https://www.bilibili.com/video/BV1xx411c7mD")
        return

    with open('video_list.txt', 'r', encoding='utf-8') as f:
        video_urls = [line.strip() for line in f if line.strip()]

    if not video_urls:
        print("video_list.txt 中没有找到有效的视频链接")
        return

    print(f"找到 {len(video_urls)} 个视频链接")

    # 加载进度
    progress = crawler.load_progress()
    completed_videos = progress.get('completed_videos', [])
    current_index = progress.get('current_index', 0)

    # 跳过已完成的视频
    video_urls_to_process = video_urls[current_index:]

    for i, video_url in enumerate(video_urls_to_process, current_index + 1):
        print(f"\n=== 正在处理第 {i}/{len(video_urls)} 个视频 ===")

        try:
            # 爬取评论
            comments = crawler.crawl_video_comments(video_url, max_pages=50)

            if comments:
                # 生成文件名
                bvid = crawler.get_bvid_from_url(video_url)
                filename = f"{bvid}_comments.csv"

                # 保存评论
                crawler.save_to_csv(comments, filename)

                # 更新进度
                completed_videos.append(video_url)
                progress = {
                    'completed_videos': completed_videos,
                    'current_index': i,
                    'last_update': time.strftime('%Y-%m-%d %H:%M:%S')
                }
                crawler.save_progress(progress)

                print(f"进度已保存，已完成 {i}/{len(video_urls)} 个视频")
            else:
                print(f"视频 {video_url} 没有获取到评论")

        except Exception as e:
            print(f"处理视频 {video_url} 时发生错误: {e}")
            # 记录错误
            with open('error_log.txt', 'a', encoding='utf-8') as f:
                f.write(f"{time.strftime('%Y-%m-%d %H:%M:%S')} - {video_url} - {str(e)}\n")

        # 延迟一下，避免请求过于频繁
        time.sleep(2)

    print("\n=== 所有视频处理完成 ===")


if __name__ == "__main__":
    # # 先测试单个视频
    # test_single_video()

    #如果测试成功，再运行完整版本
    main()