#!/bin/bash

# 订单数据导出脚本
# 用于从 MySQL 数据库导出订单数据到 CSV 文件

set -e  # 遇到错误立即退出

# 数据库配置（从环境变量读取）
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-coffee_shop}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DBKEY}"

# 输出文件配置
# Windows: 使用 C:/tmp/order_analytics
# Linux: 使用 /tmp/order_analytics
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    OUTPUT_DIR="${OUTPUT_DIR:-C:/tmp/order_analytics}"
else
    OUTPUT_DIR="${OUTPUT_DIR:-/tmp/order_analytics}"
fi
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 创建输出目录
mkdir -p "$OUTPUT_DIR"

echo "=========================================="
echo "订单数据导出工具"
echo "=========================================="

# 检查数据库连接
echo "检查数据库连接..."
if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -e "USE $DB_NAME;" 2>/dev/null; then
    echo "错误：无法连接到数据库 $DB_NAME"
    echo "请检查数据库配置和网络连接"
    exit 1
fi
echo "数据库连接成功！"

# 导出今日订单
echo ""
echo "导出今日订单数据..."
TODAY_FILE="$OUTPUT_DIR/orders_today_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT order_id, user_id, product_name, sweetness, ice_level, quantity, total_price, status, created_at
        FROM orders
        WHERE DATE(created_at) = CURDATE()
        ORDER BY created_at DESC;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$TODAY_FILE"

if [ -s "$TODAY_FILE" ]; then
    echo "✓ 今日订单导出成功: $TODAY_FILE"
    TODAY_COUNT=$(wc -l < "$TODAY_FILE")
    echo "  记录数: $TODAY_COUNT"
else
    echo "✓ 今日无订单数据"
fi

# 导出本周订单（最近7天）
echo ""
echo "导出本周订单数据..."
WEEK_FILE="$OUTPUT_DIR/orders_week_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT order_id, user_id, product_name, sweetness, ice_level, quantity, total_price, status, created_at
        FROM orders
        WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
        ORDER BY created_at DESC;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$WEEK_FILE"

if [ -s "$WEEK_FILE" ]; then
    echo "✓ 本周订单导出成功: $WEEK_FILE"
    WEEK_COUNT=$(wc -l < "$WEEK_FILE")
    echo "  记录数: $WEEK_COUNT"
else
    echo "✓ 本周无订单数据"
fi

# 导出本月订单
echo ""
echo "导出本月订单数据..."
MONTH_FILE="$OUTPUT_DIR/orders_month_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT order_id, user_id, product_name, sweetness, ice_level, quantity, total_price, status, created_at
        FROM orders
        WHERE YEAR(created_at) = YEAR(CURDATE())
        AND MONTH(created_at) = MONTH(CURDATE())
        ORDER BY created_at DESC;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$MONTH_FILE"

if [ -s "$MONTH_FILE" ]; then
    echo "✓ 本月订单导出成功: $MONTH_FILE"
    MONTH_COUNT=$(wc -l < "$MONTH_FILE")
    echo "  记录数: $MONTH_COUNT"
else
    echo "✓ 本月无订单数据"
fi

# 导出产品销量统计
echo ""
echo "导出产品销量统计..."
PRODUCT_FILE="$OUTPUT_DIR/product_sales_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT product_name, COUNT(*) as order_count, SUM(quantity) as total_quantity, SUM(total_price) as total_revenue, AVG(total_price) as avg_price
        FROM orders
        WHERE status = 'completed'
        GROUP BY product_name
        ORDER BY total_quantity DESC;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$PRODUCT_FILE"

if [ -s "$PRODUCT_FILE" ]; then
    echo "✓ 产品销量统计导出成功: $PRODUCT_FILE"
    PRODUCT_COUNT=$(wc -l < "$PRODUCT_FILE")
    echo "  产品数: $PRODUCT_COUNT"
else
    echo "✓ 无销量数据"
fi

# 导出时间段统计
echo ""
echo "导出高峰时段统计..."
HOUR_FILE="$OUTPUT_DIR/hour_stats_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT HOUR(created_at) as hour, COUNT(*) as order_count, SUM(total_price) as total_revenue
        FROM orders
        WHERE DATE(created_at) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
        GROUP BY hour
        ORDER BY hour;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$HOUR_FILE"

if [ -s "$HOUR_FILE" ]; then
    echo "✓ 高峰时段统计导出成功: $HOUR_FILE"
    HOUR_COUNT=$(wc -l < "$HOUR_FILE")
    echo "  时段数: $HOUR_COUNT"
else
    echo "✓ 无时段数据"
fi

# 导出用户消费统计
echo ""
echo "导出用户消费统计..."
USER_FILE="$OUTPUT_DIR/user_stats_$TIMESTAMP.csv"
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASSWORD" -D"$DB_NAME" \
    -e "SELECT user_id, COUNT(*) as order_count, SUM(total_price) as total_spent, AVG(total_price) as avg_spent, MAX(created_at) as last_order
        FROM orders
        WHERE status = 'completed'
        GROUP BY user_id
        ORDER BY total_spent DESC
        LIMIT 100;" \
    --batch --skip-column-names --default-character-set=utf8mb4 | \
    sed 's/\t/,/g' > "$USER_FILE"

if [ -s "$USER_FILE" ]; then
    echo "✓ 用户消费统计导出成功: $USER_FILE"
    USER_COUNT=$(wc -l < "$USER_FILE")
    echo "  用户数: $USER_COUNT"
else
    echo "✓ 无用户数据"
fi

# 创建最新数据链接（方便后续脚本直接使用）
LATEST_DIR="$OUTPUT_DIR/latest"
mkdir -p "$LATEST_DIR"
ln -sf "$TODAY_FILE" "$LATEST_DIR/orders_today.csv" 2>/dev/null || cp "$TODAY_FILE" "$LATEST_DIR/orders_today.csv" 2>/dev/null || true
ln -sf "$WEEK_FILE" "$LATEST_DIR/orders_week.csv" 2>/dev/null || cp "$WEEK_FILE" "$LATEST_DIR/orders_week.csv" 2>/dev/null || true
ln -sf "$MONTH_FILE" "$LATEST_DIR/orders_month.csv" 2>/dev/null || cp "$MONTH_FILE" "$LATEST_DIR/orders_month.csv" 2>/dev/null || true
ln -sf "$PRODUCT_FILE" "$LATEST_DIR/product_sales.csv" 2>/dev/null || cp "$PRODUCT_FILE" "$LATEST_DIR/product_sales.csv" 2>/dev/null || true
ln -sf "$HOUR_FILE" "$LATEST_DIR/hour_stats.csv" 2>/dev/null || cp "$HOUR_FILE" "$LATEST_DIR/hour_stats.csv" 2>/dev/null || true
ln -sf "$USER_FILE" "$LATEST_DIR/user_stats.csv" 2>/dev/null || cp "$USER_FILE" "$LATEST_DIR/user_stats.csv" 2>/dev/null || true

echo ""
echo "=========================================="
echo "数据导出完成！"
echo "=========================================="
echo "输出目录: $OUTPUT_DIR"
echo "最新数据: $LATEST_DIR"
echo ""
echo "可以使用以下 Python 脚本进行分析："
echo "  python analyze-orders.py --data-dir $LATEST_DIR"
echo ""
