# 订单表结构说明

## orders 表

| 字段名 | 类型 | 说明 | 示例值 |
|--------|------|------|--------|
| order_id | VARCHAR(50) | 订单ID，主键 | ORDER_1693654321000 |
| user_id | BIGINT | 用户ID | 12345 |
| product_name | VARCHAR(100) | 产品名称 | 焦糖玛奇朵 |
| sweetness | INT | 甜度：1-无糖，2-微糖，3-半糖，4-少糖，5-标准糖 | 3 |
| ice_level | INT | 冰量：1-热，2-温，3-去冰，4-少冰，5-正常冰 | 5 |
| quantity | INT | 购买数量 | 2 |
| total_price | DECIMAL(10,2) | 订单总金额 | 68.00 |
| status | VARCHAR(20) | 订单状态：pending/completed/cancelled | completed |
| remark | TEXT | 订单备注 | 少加糖 |
| created_at | DATETIME | 创建时间 | 2024-09-01 14:30:00 |
| updated_at | DATETIME | 更新时间 | 2024-09-01 14:35:00 |

## 数据库连接配置

```bash
# 数据库连接参数
DB_HOST=localhost
DB_PORT=3306
DB_NAME=coffee_shop
DB_USER=root
DB_PASSWORD=${DBKEY}
```

## 常用查询SQL

### 查询今日订单
```sql
SELECT * FROM orders WHERE DATE(created_at) = CURDATE();
```

### 按产品统计销量
```sql
SELECT product_name, SUM(quantity) as total_quantity, SUM(total_price) as total_revenue
FROM orders
WHERE status = 'completed'
GROUP BY product_name
ORDER BY total_quantity DESC;
```

### 按时间段统计
```sql
SELECT HOUR(created_at) as hour, COUNT(*) as order_count
FROM orders
WHERE DATE(created_at) = CURDATE()
GROUP BY hour
ORDER BY hour;
```
