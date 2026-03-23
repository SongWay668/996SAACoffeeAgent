#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
订单数据分析脚本
支持多种分析维度和可视化报告生成
"""

import os
import sys
import argparse
import csv
from datetime import datetime
from collections import defaultdict
from typing import List, Dict, Tuple

# 配置
# Windows: 使用 C:/tmp/order_analytics/latest
# Linux: 使用 /tmp/order_analytics/latest
if os.name == 'nt':  # Windows
    DEFAULT_DATA_DIR = "C:/tmp/order_analytics/latest"
else:
    DEFAULT_DATA_DIR = "/tmp/order_analytics/latest"

# 甜度映射
SWEETNESS_MAP = {
    1: "无糖",
    2: "微糖",
    3: "半糖",
    4: "少糖",
    5: "标准糖"
}

# 冰量映射
ICE_LEVEL_MAP = {
    1: "热",
    2: "温",
    3: "去冰",
    4: "少冰",
    5: "正常冰"
}


def load_csv(file_path: str) -> List[List[str]]:
    """加载 CSV 文件"""
    if not os.path.exists(file_path):
        return []
    with open(file_path, 'r', encoding='utf-8') as f:
        return [row for row in csv.reader(f)]


def analyze_daily_sales(data_dir: str) -> Dict:
    """分析今日销售额"""
    orders_file = os.path.join(data_dir, "orders_today.csv")
    orders = load_csv(orders_file)

    if not orders:
        return {
            "total_orders": 0,
            "total_revenue": 0.0,
            "avg_order_value": 0.0,
            "message": "今日暂无订单数据"
        }

    total_orders = len(orders)
    total_revenue = sum(float(order[6]) for order in orders)  # total_price 在第7列
    avg_order_value = total_revenue / total_orders if total_orders > 0 else 0.0

    return {
        "total_orders": total_orders,
        "total_revenue": round(total_revenue, 2),
        "avg_order_value": round(avg_order_value, 2),
        "message": f"今日共有 {total_orders} 笔订单，总销售额 {total_revenue:.2f} 元，平均客单价 {avg_order_value:.2f} 元"
    }


def analyze_product_sales(data_dir: str, top_n: int = 5) -> Dict:
    """分析产品销量（热门产品 TOP N）"""
    product_file = os.path.join(data_dir, "product_sales.csv")
    products = load_csv(product_file)

    if not products:
        return {
            "top_products": [],
            "message": "暂无产品销量数据"
        }

    # 解析数据并排序
    product_stats = []
    for row in products:
        if len(row) >= 4:
            product_stats.append({
                "name": row[0],
                "order_count": int(row[1]),
                "quantity": int(row[2]),
                "revenue": float(row[3])
            })

    # 按销量排序
    product_stats.sort(key=lambda x: x['quantity'], reverse=True)

    top_products = product_stats[:top_n]

    return {
        "top_products": top_products,
        "total_products": len(product_stats),
        "message": f"热门产品 TOP {len(top_products)} 已分析，共 {len(product_stats)} 种产品"
    }


def analyze_peak_hours(data_dir: str) -> Dict:
    """分析高峰时段"""
    hour_file = os.path.join(data_dir, "hour_stats.csv")
    hours = load_csv(hour_file)

    if not hours:
        return {
            "peak_hours": [],
            "message": "暂无时段数据"
        }

    # 解析数据
    hour_stats = []
    for row in hours:
        if len(row) >= 3:
            hour_stats.append({
                "hour": int(row[0]),
                "order_count": int(row[1]),
                "revenue": float(row[2])
            })

    # 按订单数排序
    hour_stats.sort(key=lambda x: x['order_count'], reverse=True)

    # 取前3个高峰时段
    peak_hours = hour_stats[:3]

    # 计算平均订单数
    avg_orders = sum(h['order_count'] for h in hour_stats) / len(hour_stats) if hour_stats else 0

    return {
        "peak_hours": peak_hours,
        "avg_orders": round(avg_orders, 2),
        "message": f"高峰时段已分析，平均每小时 {avg_orders:.2f} 单"
    }


def analyze_user_behavior(data_dir: str, top_n: int = 10) -> Dict:
    """分析用户消费行为"""
    user_file = os.path.join(data_dir, "user_stats.csv")
    users = load_csv(user_file)

    if not users:
        return {
            "top_users": [],
            "avg_spent": 0.0,
            "repurchase_rate": 0.0,
            "message": "暂无用户数据"
        }

    # 解析数据
    user_stats = []
    for row in users:
        if len(row) >= 5:
            user_stats.append({
                "user_id": row[0],
                "order_count": int(row[1]),
                "total_spent": float(row[2]),
                "avg_spent": float(row[3]),
                "last_order": row[4]
            })

    # 计算统计指标
    total_users = len(user_stats)
    avg_spent = sum(u['avg_spent'] for u in user_stats) / total_users if total_users > 0 else 0.0
    repurchase_users = sum(1 for u in user_stats if u['order_count'] >= 2)
    repurchase_rate = (repurchase_users / total_users * 100) if total_users > 0 else 0.0

    # 按总消费排序
    user_stats.sort(key=lambda x: x['total_spent'], reverse=True)
    top_users = user_stats[:top_n]

    return {
        "top_users": top_users,
        "total_users": total_users,
        "avg_spent": round(avg_spent, 2),
        "repurchase_rate": round(repurchase_rate, 2),
        "repurchase_users": repurchase_users,
        "message": f"共 {total_users} 位用户，复购率 {repurchase_rate:.2f}%"
    }


def analyze_preferences(data_dir: str) -> Dict:
    """分析用户偏好（甜度、冰量）"""
    orders_file = os.path.join(data_dir, "orders_week.csv")
    orders = load_csv(orders_file)

    if not orders:
        return {
            "sweetness_prefs": {},
            "ice_level_prefs": {},
            "message": "暂无订单数据"
        }

    # 统计甜度偏好
    sweetness_counts = defaultdict(int)
    ice_level_counts = defaultdict(int)

    for order in orders:
        if len(order) >= 5:
            try:
                sweetness = int(order[3])  # sweetness 在第4列
                ice_level = int(order[4])   # ice_level 在第5列
                sweetness_counts[sweetness] += 1
                ice_level_counts[ice_level] += 1
            except (ValueError, IndexError):
                continue

    # 排序
    sorted_sweetness = sorted(sweetness_counts.items(), key=lambda x: x[1], reverse=True)
    sorted_ice = sorted(ice_level_counts.items(), key=lambda x: x[1], reverse=True)

    return {
        "sweetness_prefs": [
            {"level": SWEETNESS_MAP.get(k, str(k)), "count": v, "percentage": v / len(orders) * 100}
            for k, v in sorted_sweetness
        ],
        "ice_level_prefs": [
            {"level": ICE_LEVEL_MAP.get(k, str(k)), "count": v, "percentage": v / len(orders) * 100}
            for k, v in sorted_ice
        ],
        "message": "用户偏好分析完成"
    }


def format_output(analysis_result: Dict) -> str:
    """格式化输出结果"""
    output = []

    # 标题
    output.append("=" * 60)
    output.append("订单数据分析报告")
    output.append(f"生成时间: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    output.append("=" * 60)
    output.append("")

    # 日销售额
    daily = analysis_result.get("daily_sales", {})
    if daily:
        output.append("【今日销售】")
        output.append(daily.get("message", "无数据"))
        output.append("")

    # 热门产品
    product = analysis_result.get("product_sales", {})
    if product and product.get("top_products"):
        output.append(f"【热门产品 TOP {len(product['top_products'])}】")
        for idx, p in enumerate(product['top_products'], 1):
            output.append(f"{idx}. {p['name']} - 销量: {p['quantity']} 杯, 金额: {p['revenue']:.2f} 元")
        output.append("")

    # 高峰时段
    peak = analysis_result.get("peak_hours", {})
    if peak and peak.get("peak_hours"):
        output.append("【高峰时段 TOP 3】")
        for idx, h in enumerate(peak['peak_hours'], 1):
            output.append(f"{idx}. {h['hour']:02d}:00 - {h['hour']+1:02d}:00  |  订单数: {h['order_count']}, 营业额: {h['revenue']:.2f} 元")
        output.append("")

    # 用户行为
    user = analysis_result.get("user_behavior", {})
    if user and user.get("top_users"):
        output.append("【高价值用户 TOP 5】")
        for idx, u in enumerate(user['top_users'][:5], 1):
            output.append(f"{idx}. 用户 {u['user_id']} | 订单: {u['order_count']} 单 | 消费: {u['total_spent']:.2f} 元")
        output.append(f"复购率: {user.get('repurchase_rate', 0):.2f}% ({user.get('repurchase_users', 0)}/{user.get('total_users', 0)} 位用户复购)")
        output.append("")

    # 用户偏好
    pref = analysis_result.get("preferences", {})
    if pref and pref.get("sweetness_prefs"):
        output.append("【甜度偏好】")
        for s in pref['sweetness_prefs'][:3]:
            output.append(f"{s['level']}: {s['percentage']:.1f}%")
        output.append("")

        output.append("【冰量偏好】")
        for i in pref['ice_level_prefs'][:3]:
            output.append(f"{i['level']}: {i['percentage']:.1f}%")
        output.append("")

    output.append("=" * 60)

    return "\n".join(output)


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='订单数据分析工具')
    parser.add_argument('--data-dir', type=str, default=DEFAULT_DATA_DIR,
                        help='数据文件目录（默认: /tmp/order_analytics/latest）')
    parser.add_argument('--type', type=str, default='all',
                        choices=['daily', 'product', 'peak', 'user', 'pref', 'all'],
                        help='分析类型：daily-今日销售，product-热门产品，peak-高峰时段，user-用户行为，pref-用户偏好，all-全部分析')
    parser.add_argument('--output', type=str, help='输出文件路径（可选）')

    args = parser.parse_args()

    # 检查数据目录
    if not os.path.exists(args.data_dir):
        print(f"错误：数据目录不存在: {args.data_dir}")
        print("请先运行 analytics-script.sh 导出数据")
        sys.exit(1)

    # 执行分析
    analysis_result = {}

    if args.type in ['daily', 'all']:
        analysis_result['daily_sales'] = analyze_daily_sales(args.data_dir)

    if args.type in ['product', 'all']:
        analysis_result['product_sales'] = analyze_product_sales(args.data_dir)

    if args.type in ['peak', 'all']:
        analysis_result['peak_hours'] = analyze_peak_hours(args.data_dir)

    if args.type in ['user', 'all']:
        analysis_result['user_behavior'] = analyze_user_behavior(args.data_dir)

    if args.type in ['pref', 'all']:
        analysis_result['preferences'] = analyze_preferences(args.data_dir)

    # 格式化输出
    output = format_output(analysis_result)

    # 输出结果
    if args.output:
        with open(args.output, 'w', encoding='utf-8') as f:
            f.write(output)
        print(f"分析报告已保存到: {args.output}")
    else:
        print(output)


if __name__ == '__main__':
    main()
