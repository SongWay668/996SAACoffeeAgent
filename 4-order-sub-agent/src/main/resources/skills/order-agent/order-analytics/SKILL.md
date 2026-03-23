---
name: order-analytics
description: This skill should be used when users ask about order statistics, sales data, popular products, peak hours, customer consumption behavior, or any order-related data analysis. It provides comprehensive order analytics including daily/monthly sales, top products, peak time analysis, and customer preference analysis using Python scripts and Shell commands.
---

# 订单统计分析

## 功能说明

本技能提供订单数据的全面统计分析功能，帮助用户了解销售趋势、热门产品、高峰时段、用户消费行为等关键指标。

### 支持的分析类型

- **销售分析**：日销售额、月度销售额、客单价统计
- **产品分析**：热门产品 TOP N、产品销量排名、产品收入分析
- **时段分析**：高峰时段识别、分时段订单统计
- **用户分析**：高价值用户识别、复购率分析、用户消费行为
- **偏好分析**：甜度偏好统计、冰量偏好统计

## 使用方法

当用户询问以下问题时，使用本技能：

### 常见问题示例
- "今天的销售怎么样？"
- "哪些产品最畅销？"
- "什么时候是高峰期？"
- "用户的复购率是多少？"
- "分析一下用户偏好"
- "生成销售报表"

### 数据处理流程

1. **数据导出**（Shell 脚本）
   ```bash
   # 执行数据导出脚本
   bash scripts/analytics-script.sh
   ```
   该脚本会导出以下数据到 `/tmp/order_analytics/latest/` 目录：
   - `orders_today.csv` - 今日订单数据
   - `orders_week.csv` - 本周订单数据
   - `orders_month.csv` - 本月订单数据
   - `product_sales.csv` - 产品销量统计
   - `hour_stats.csv` - 高峰时段统计
   - `user_stats.csv` - 用户消费统计

2. **数据分析**（Python 脚本）
   ```bash
   # 全部分析
   python scripts/analyze-orders.py

   # 特定分析类型
   python scripts/analyze-orders.py --type daily      # 今日销售
   python scripts/analyze-orders.py --type product    # 热门产品
   python scripts/analyze-orders.py --type peak       # 高峰时段
   python scripts/analyze-orders.py --type user       # 用户行为
   python scripts/analyze-orders.py --type pref       # 用户偏好
   ```

3. **结果输出**
   脚本会生成格式化的分析报告，包含：
   - 销售概况（订单数、总金额、客单价）
   - 热门产品排名
   - 高峰时段分布
   - 用户消费行为指标
   - 用户偏好统计

### 环境要求

执行脚本前需要设置数据库密码环境变量：
```bash
export DBKEY=your_mysql_password
```

## 可用资源

### 详细文档
- `references/order-schema.md` - 订单表结构说明和 SQL 查询示例
- `examples/usage-examples.md` - 详细使用示例和输出格式说明

### 脚本文件
- `scripts/analytics-script.sh` - 数据导出 Shell 脚本
- `scripts/analyze-orders.py` - 数据分析 Python 脚本

### 关键统计指标

| 指标 | 说明 | 数据来源 |
|------|------|----------|
| 日销售额 | 当日所有订单总金额 | orders_today.csv |
| 月度销售额 | 当月所有订单总金额 | orders_month.csv |
| 热门产品 TOP 5 | 按销量排序的前5名产品 | product_sales.csv |
| 高峰时段 | 订单最多的时间段 | hour_stats.csv |
| 平均客单价 | 平均每单金额 | 分析脚本计算 |
| 用户复购率 | 复购用户占比 | user_stats.csv |
| 用户偏好 | 甜度、冰量偏好分布 | orders_week.csv |

## 注意事项

1. 数据导出需要 MySQL 数据库连接，确保数据库服务正常运行
2. Shell 脚本使用 `mysql` 命令行工具，确保已安装 MySQL 客户端
3. Python 脚本需要 Python 3.8+ 环境
4. 输出目录默认为 `/tmp/order_analytics/`，可通过环境变量 `OUTPUT_DIR` 修改
5. 分析结果会以文本格式输出，可直接返回给用户
