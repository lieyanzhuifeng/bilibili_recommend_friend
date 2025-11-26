import requests
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8080"
ORIGIN = "http://localhost:3000"  # 使用CORS配置中允许的源

# 定义需要测试的API端点
api_endpoints = [
    # FilterController 接口
    {"url": "/api/filter/same-up", "method": "GET", "params": {"upId": 1}, "name": "Same Up Filter"},
    {"url": "/api/filter/same-tag", "method": "GET", "params": {"tagId": 1}, "name": "Same Tag Filter"},
    {"url": "/api/filter/same-up-video-count", "method": "POST", "json": {"upId": 1}, "name": "Same Up Video Count"},
    {"url": "/api/filter/same-tag-video-count", "method": "GET", "params": {"tagId": 1}, "name": "Same Tag Video Count"},
    {"url": "/api/filter/follow-time", "method": "GET", "params": {"userId": 1}, "name": "Follow Time"},
    {"url": "/api/filter/night-owl", "method": "GET", "params": {"userId": 1}, "name": "Night Owl"},
    {"url": "/api/filter/user-activity", "method": "GET", "params": {"userId": 1}, "name": "User Activity"},
    {"url": "/api/filter/deep-video", "method": "GET", "params": {"videoId": 1, "option": 1}, "name": "Deep Video"},
    {"url": "/api/filter/series", "method": "GET", "params": {"seriesId": 1}, "name": "Series"},
    
    # FriendController 接口
    {"url": "/api/friends/list/1", "method": "GET", "name": "Friend List"},
    {"url": "/api/friends/check", "method": "GET", "params": {"userId": 1, "targetId": 2}, "name": "Check Friend"},
    
    # RecommendationController 接口
    {"url": "/api/recommend/co-comment/1", "method": "GET", "name": "Co-Comment Recommendation"},
    {"url": "/api/recommend/reply/1", "method": "GET", "name": "Reply Recommendation"},
    {"url": "/api/recommend/shared-video/1", "method": "GET", "name": "Shared Video Recommendation"},
    {"url": "/api/recommend/category/1", "method": "GET", "name": "Category Recommendation"},
    {"url": "/api/recommend/theme/1", "method": "GET", "name": "Theme Recommendation"},
    {"url": "/api/recommend/user-behavior/1", "method": "GET", "name": "User Behavior Recommendation"},
    {"url": "/api/recommend/common-up/1", "method": "GET", "name": "Common UP Recommendation"},
    {"url": "/api/recommend/favorite-similarity/1", "method": "GET", "name": "Favorite Similarity Recommendation"},
    {"url": "/api/recommend/comment-friends/1", "method": "GET", "name": "Comment Friends Recommendation"},
    
    # UserController 接口
    {"url": "/api/users/1", "method": "GET", "name": "Get User Info"},
    
    # MessageController 接口 (部分GET接口)
    {"url": "/api/messages/recent-chat", "method": "GET", "params": {"userId": 1}, "name": "Recent Chat"},
    {"url": "/api/messages/unread-count", "method": "GET", "params": {"userId": 1}, "name": "Unread Count"},
    {"url": "/api/messages/chat-partners/1", "method": "GET", "name": "Chat Partners"},
]

def test_api_endpoint(endpoint):
    """测试单个API端点的CORS功能和可用性"""
    url = BASE_URL + endpoint["url"]
    headers = {
        "Origin": ORIGIN,
        "Content-Type": "application/json"
    }
    
    result = {
        "name": endpoint["name"],
        "url": url,
        "success": False,
        "status_code": None,
        "cors_headers": {},
        "error": None
    }
    
    try:
        if endpoint["method"] == "GET":
            response = requests.get(url, headers=headers, params=endpoint.get("params"), timeout=10)
        elif endpoint["method"] == "POST":
            response = requests.post(url, headers=headers, json=endpoint.get("json"), params=endpoint.get("params"), timeout=10)
        elif endpoint["method"] == "PUT":
            response = requests.put(url, headers=headers, json=endpoint.get("json"), params=endpoint.get("params"), timeout=10)
        elif endpoint["method"] == "DELETE":
            response = requests.delete(url, headers=headers, params=endpoint.get("params"), timeout=10)
        else:
            result["error"] = f"Unsupported method: {endpoint['method']}"
            return result
        
        result["status_code"] = response.status_code
        
        # 检查CORS相关的响应头
        cors_headers_to_check = [
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Access-Control-Allow-Methods",
            "Access-Control-Allow-Headers",
            "Access-Control-Max-Age"
        ]
        
        for header in cors_headers_to_check:
            if header in response.headers:
                result["cors_headers"][header] = response.headers[header]
        
        # 如果状态码不是403（CORS错误）或503（服务不可用），则认为请求成功
        if response.status_code != 403 and response.status_code != 503:
            result["success"] = True
        else:
            result["error"] = f"Request failed with status code: {response.status_code}"
            
    except requests.RequestException as e:
        result["error"] = f"Request error: {str(e)}"
    
    return result

def main():
    print("🎯 开始测试API端点的CORS功能和可用性")
    print(f"📡 基础URL: {BASE_URL}")
    print(f"🌐 测试源: {ORIGIN}")
    print(f"📋 总测试接口数: {len(api_endpoints)}")
    print("\n" + "="*80)
    
    start_time = time.time()
    results = []
    
    # 使用线程池并发测试API端点
    with ThreadPoolExecutor(max_workers=10) as executor:
        future_to_endpoint = {executor.submit(test_api_endpoint, endpoint): endpoint for endpoint in api_endpoints}
        
        for i, future in enumerate(as_completed(future_to_endpoint), 1):
            try:
                result = future.result()
                results.append(result)
                
                # 输出单个测试结果
                status = "✅" if result["success"] else "❌"
                print(f"{status} [{i}/{len(api_endpoints)}] {result['name']}")
                print(f"   URL: {result['url']}")
                print(f"   Status Code: {result['status_code']}")
                
                if result['cors_headers']:
                    print("   CORS Headers:")
                    for header, value in result['cors_headers'].items():
                        print(f"     {header}: {value}")
                else:
                    print("   CORS Headers: None")
                
                if result['error']:
                    print(f"   Error: {result['error']}")
                
                print("-" * 80)
                
            except Exception as e:
                print(f"❌ 处理测试结果时发生错误: {str(e)}")
                print("-" * 80)
    
    # 汇总测试结果
    end_time = time.time()
    total_time = end_time - start_time
    
    successful = [r for r in results if r["success"]]
    failed = [r for r in results if not r["success"]]
    
    print("\n📊 测试汇总")
    print(f"✅ 成功: {len(successful)}/{len(results)}")
    print(f"❌ 失败: {len(failed)}/{len(results)}")
    print(f"⏱️  总耗时: {total_time:.2f}秒")
    
    # 输出失败的测试详情
    if failed:
        print("\n❌ 失败的测试详情:")
        for i, result in enumerate(failed, 1):
            print(f"{i}. {result['name']}")
            print(f"   URL: {result['url']}")
            print(f"   Status: {result['status_code']}")
            print(f"   Error: {result.get('error', 'Unknown error')}")
            print(f"   CORS Headers: {result['cors_headers']}")
            print()
    
    # 检查CORS配置
    cors_issues = []
    for result in results:
        if "Access-Control-Allow-Origin" not in result["cors_headers"]:
            cors_issues.append(f"{result['name']} - 缺少 Access-Control-Allow-Origin 头")
        elif result["cors_headers"].get("Access-Control-Allow-Origin") != ORIGIN and result["cors_headers"].get("Access-Control-Allow-Origin") != "*":
            cors_issues.append(f"{result['name']} - Access-Control-Allow-Origin 值不正确")
        
        if "Access-Control-Allow-Credentials" not in result["cors_headers"]:
            cors_issues.append(f"{result['name']} - 缺少 Access-Control-Allow-Credentials 头")
        elif result["cors_headers"].get("Access-Control-Allow-Credentials") != "true":
            cors_issues.append(f"{result['name']} - Access-Control-Allow-Credentials 未设置为 'true'")
    
    print("\n🔍 CORS配置检查")
    if cors_issues:
        print("发现以下CORS配置问题:")
        for issue in cors_issues:
            print(f"- {issue}")
    else:
        print("✅ 所有测试的API端点CORS配置正确")

if __name__ == "__main__":
    main()