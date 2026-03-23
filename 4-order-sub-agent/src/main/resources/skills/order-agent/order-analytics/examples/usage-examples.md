# 订单分析示例

## 1. 导出数据

### 使用 Shell 脚本导出
```bash
# 设置数据库密码（环境变量）
export DBKEY=your_mysql_password

# 运行导出脚本
bash scripts/analytics-script.sh
```

### 输出示例
```
==========================================
订单数据导出工具
==========================================
检查数据库连接...
数据库连接成功！

导出今日订单数据...
✓ 今日订单导出成功: /tmp/order_analytics/orders_today_20240302_143022.csv
  记录数: 45

导出本周订单数据...
✓ 本周订单导出成功: /tmp/order_analytics/orders_week_20240302_143022.csv
  记录数: 312

导出本月订单数据...
✓ 本月订单导出成功: /tmp/order_analytics/orders_month_20240302_143022.csv
  记录数: 1245

导出产品销量统计...
✓ 产品销量统计导出成功: /tmp/order_analytics/product_sales_20240302_143022.csv
  产品数: 15

导出高峰时段统计...
✓ 高峰时段统计导出成功: /tmp/order_analytics/hour_stats_20240302_143022.csv
  时段数: 12

导出用户消费统计...
✓ 用户消费统计导出成功: /tmp/order_analytics/user_stats_20240302_143022.csv
  用户数: 100

==========================================
数据导出完成！
==========================================
输出目录: /tmp/order_analytics
最新数据: /tmp/order_analytics/latest
```

## 2. 分析订单

### 全部分析
```bash
python scripts/analyze-orders.py
```

### 分析类型选择
```bash
# 只分析今日销售
python scripts/analyze-orders.py --type daily

# 只分析热门产品
python scripts/analyze-orders.py --type product

# 只分析高峰时段
python scripts/analyze-orders.py --type peak

# 只分析用户行为
python scripts/analyze-orders.py --type user

# 只分析用户偏好
python scripts/analyze-orders.py --type pref
```

### 指定数据目录
```bash
python scripts/analyze-orders.py --data-dir /path/to/data
```

### 输出到文件
```bash
python scripts/analyze-orders.py --output /tmp/report.txt
```

## 3. 分析结果示例

### 全部分析输出
```
============================================================
订单数据分析报告
生成时间: 2024-03-02 14:30:22
============================================================

【今日销售】
今日共有 45 笔订单，总销售额 2856.50 元，平均客单价 63.48 元

【热门产品 TOP 5】
1. 焦糖玛奇朵 - 销量: 120 杯, 金额: 4560.00 元
2. 香草拿铁 - 销量: 98 杯, 金额: 3920.00 元
3. 冷萃咖啡 - 销量: 85 杯, 金额: 3400.00 元
4. 港式丝袜咖啡 - 销量: 72 杯, 金额: 3600.00 元
5. 摩卡咖啡 - 销量: 65 杯, 金额: 3250.00 元

【高峰时段 TOP 3】
1. 09:00 - 10:00  |  订单数: 45, 营业额: 2850.00 元
2. 14:00 - 15:00  |  订单数: 38, 营业额: 2394.00 元
3. 18:00 - 19:00  |  订单数: 32, 营业额: 2016.00 元

【高价值用户 TOP 5】
1. 用户 12345 | 订单: 15 单 | 消费: 1250.00 元
2. 用户 67890 | 订单: 12 单 | 消费: 980.00 元
3. 用户 11111 | 订单: 10 单 | 消费: 850.00 元
4. 用户 22222 | 订单: 8 单 | 消费: 720.00 元
5. 用户 33333 | 订单: 7 单 | 消费: 630.00 元
复购率: 45.00% (45/100 位用户复购)

【甜度偏好】
标准糖: 35.5%
半糖: 28.3%
少糖: 20.1%

【冰量偏好】
正常冰: 45.2%
去冰: 25.8%
少冰: 18.5%

============================================================
```

## 4. CSV 文件格式

### orders_today.csv / orders_week.csv / orders_month.csv
```csv
ORDER_1693654321000,12345,焦糖玛奇朵,3,5,2,68.00,completed,2024-09-01 14:30:00
ORDER_1693654321001,67890,香草拿铁,4,3,1,40.00,completed,2024-09-01 14:35:00
ORDER_1693654321002,11111,冷萃咖啡,1,5,1,42.00,completed,2024-09-01 14:40:00
...
```
列顺序：order_id, user_id, product_name, sweetness, ice_level, quantity, total_price, status, created_at

### product_sales.csv
```csv
焦糖玛奇朵,120,240,4560.00,38.00
香草拿铁,98,196,3920.00,40.00
冷萃咖啡,85,170,3400.00,40.00
...
```
列顺序：product_name, order_count, total_quantity, total_revenue, avg_price

### hour_stats.csv
```csv
9,45,2850.00
10,32,2016.00
11,25,1575.00
...
```
列顺序：hour, order_count, total_revenue

### user_stats.csv
```csv
12345,15,1250.00,83.33,2024-09-01 14:30:00
67890,12,980.00,81.67,2024-09-01 14:20:00
11111,10,850.00,85.00,2024-09-01 14:15:00
...
```
列顺序：user_id, order_count, total_spent, avg_spent, last_order

## 5. 常见使用场景

### 场景1：每日日报
```bash
# 导出今日数据
bash scripts/analytics-script.sh

# 生成日报
python scripts/analyze-orders.py --type daily --output /tmp/daily_report.txt

# 发送邮件或保存
cat /tmp/daily_report.txt
```

### 场景2：周度总结
```bash
# 导出本周数据
bash scripts/analytics-script.sh

# 生成绩效分析
python scripts/analyze-orders.py --type product --type user --output /tmp/weekly_analysis.txt

# 保存报告
cat /tmp/weekly_analysis.txt
```

### 场景3：产品优化
```bash
# 分析用户偏好
python scripts/analyze-orders.py --type pref --output /tmp/preference_analysis.txt

# 根据偏好调整菜单
cat /tmp/preference_analysis.txt
```

## 6. 集成到 Agent

当用户问："今天的销售怎么样？"、"哪些产品最畅销？"、"什么时候是高峰期？"

Agent 会：
1. 检查是否有最新数据
2. 如果没有，调用 `bash scripts/analytics-script.sh` 导出数据
3. 调用 `python scripts/analyze-orders.py` 分析数据
4. 返回分析结果给用户

示例 Agent 回复：
```
根据今日数据分析：

📊 今日销售概况
- 订单总数：45 笔
- 总销售额：2856.50 元
- 平均客单价：63.48 元

🏆 热门产品 TOP 3
1. 焦糖玛奇朵：销量 120 杯，销售额 4560.00 元
2. 香草拿铁：销量 98 杯，销售额 3920.00 元
3. 冷萃咖啡：销量 85 杯，销售额 3400.00 元

⏰ 高峰时段
- 09:00-10:00：45 单
- 14:00-15:00：38 单
- 18:00-19:00：32 单

是否需要查看更详细的分析报告？
```
