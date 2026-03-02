# 订单数据为空的解决方案

## 问题
访问 `/orders` 时数据为空。

## 原因
数据库中 `order` 表可能是空的，或者表不存在。

## 解决方案

### 方法1：使用完整测试脚本（推荐）

1. 打开浏览器，访问：`http://localhost:8080/index.html`

2. 按 F12 打开开发者工具，切换到 Console（控制台）

3. 复制以下代码并粘贴到控制台，按回车执行：

```javascript
fetch('/static/test-all.js')
    .then(r => r.text())
    .then(code => eval(code))
```

或者直接打开：`http://localhost:8080/static/test-all.js`

复制文件内容，粘贴到控制台执行。

4. 等待执行完成，刷新页面查看数据。

---

### 方法2：手动创建数据

#### 1. 先创建用户

```javascript
fetch('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        username: 'admin',
        email: 'admin@coffee.com',
        phone: '13800138001',
        role: 'ADMIN'
    })
})
```

#### 2. 创建产品

```javascript
fetch('/api/products', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        name: '拿铁咖啡',
        category: '咖啡',
        price: 28.00,
        stock: 100
    })
})
```

#### 3. 创建订单

```javascript
fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        orderId: 'ORD-001',
        userId: 1,
        productId: 1,
        productName: '拿铁咖啡',
        sweetness: 50,
        iceLevel: 70,
        quantity: 1,
        unitPrice: 28.00,
        totalPrice: 28.00,
        remark: '少糖少冰'
    })
})
```

---

### 方法3：使用调试工具

1. 访问：`http://localhost:8080/debug.html`

2. 依次点击按钮测试接口

3. 如果创建成功，刷新主页面查看数据

---

### 方法4：检查数据库表

如果以上方法都不行，可能是数据库表结构问题。

#### 检查表是否存在

```sql
SHOW TABLES;
```

应该看到：
- users
- products
- orders (或 order)
- feedback

#### 创建 order 表（如果不存在）

```sql
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100),
    sweetness INT,
    ice_level INT,
    quantity INT,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 创建 users 表（如果不存在）

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'CUSTOMER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 创建 products 表（如果不存在）

```sql
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 调试步骤

### 1. 检查后端日志

```bash
mvn spring-boot:run
```

查看是否有数据库连接错误或 SQL 错误。

### 2. 检查浏览器控制台

按 F12 -> Console，查看是否有 JavaScript 错误。

### 3. 检查网络请求

按 F12 -> Network，查看 `/api/orders` 请求：
- 状态码应该是 200
- Response 应该是 `{"code":200,"message":"操作成功","data":[]}`

### 4. 直接测试 API

```bash
# 获取所有订单
curl http://localhost:8080/api/orders

# 创建订单
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"TEST-001","userId":1,"productId":1,"productName":"测试","quantity":1,"unitPrice":28.00,"totalPrice":28.00}'
```

---

## 快速测试命令

在浏览器控制台执行：

```javascript
// 1. 创建测试订单
fetch('/api/orders', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        orderId: 'TEST-' + Date.now(),
        userId: 1,
        productId: 1,
        productName: '测试咖啡',
        sweetness: 50,
        iceLevel: 50,
        quantity: 1,
        unitPrice: 28.00,
        totalPrice: 28.00,
        remark: '测试订单'
    })
}).then(r => r.json()).then(console.log);
```

然后刷新页面查看订单列表。
