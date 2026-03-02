# Reference 表配置说明

## 什么是 Reference 表？

Reference 表是系统配置表，用于存储各种选项配置数据。

## 表结构

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| ref_type | VARCHAR(50) | 配置类型 |
| ref_key | VARCHAR(50) | 配置键（唯一标识） |
| ref_value | VARCHAR(200) | 配置值（显示文本） |
| ref_code | INT | 配置编码（实际使用的值） |
| sort_order | INT | 排序 |
| is_active | INT | 是否启用（1:启用 0:禁用） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

## 已实现的配置类型

### 1. 甜度配置 (sweetness)

| ref_key | ref_value | ref_code | 说明 |
|---------|-----------|----------|------|
| NO_SUGAR | 无糖 | 0 | 无糖 |
| LOW_SUGAR | 少糖 | 30 | 少糖 |
| MEDIUM_SUGAR | 中糖 | 50 | 中糖 |
| HIGH_SUGAR | 多糖 | 70 | 多糖 |
| VERY_HIGH_SUGAR | 超多糖 | 100 | 超多糖 |

### 2. 冰度配置 (ice_level)

| ref_key | ref_value | ref_code | 说明 |
|---------|-----------|----------|------|
| HOT | 热饮 | 0 | 热饮 |
| LOW_ICE | 少冰 | 30 | 少冰 |
| MEDIUM_ICE | 中冰 | 50 | 中冰 |
| HIGH_ICE | 多冰 | 70 | 多冰 |
| VERY_HIGH_ICE | 超多冰 | 100 | 超多冰 |

### 3. 反馈类型 (feedback_type)

| ref_key | ref_value | ref_code | 说明 |
|---------|-----------|----------|------|
| PRODUCT_SUGGESTION | 产品建议 | 1 | 产品建议 |
| SERVICE_FEEDBACK | 服务反馈 | 2 | 服务反馈 |
| COMPLAINT | 投诉 | 3 | 投诉 |
| OTHER | 其他 | 4 | 其他 |

## 使用方式

### 1. 初始化数据

在浏览器控制台执行：

```javascript
fetch('/static/init-reference.js')
    .then(r => r.text())
    .then(code => eval(code))
```

或者打开 `/static/init-reference.js` 文件，复制内容到控制台执行。

### 2. 获取配置

**获取所有配置：**
```javascript
fetch('/api/reference')
    .then(r => r.json())
    .then(data => console.log(data))
```

**按类型获取配置：**
```javascript
// 获取甜度选项
fetch('/api/reference/type/sweetness')
    .then(r => r.json())
    .then(data => console.log(data))

// 获取冰度选项
fetch('/api/reference/type/ice_level')
    .then(r => r.json())
    .then(data => console.log(data))

// 获取反馈类型
fetch('/api/reference/type/feedback_type')
    .then(r => r.json())
    .then(data => console.log(data))
```

### 3. 添加新配置

```javascript
// 添加新的甜度选项
fetch('/api/reference', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        refType: 'sweetness',
        refKey: 'CUSTOM_SWEETNESS',
        refValue: '自定义甜度',
        refCode: 25,
        sortOrder: 6,
        isActive: 1
    })
})
```

## 前端使用

在订单管理中，甜度和冰度现在从 reference 表动态加载：

```javascript
// 获取甜度选项
const sweetnessOptions = await request('/reference/type/sweetness');

// 在下拉框中使用
<el-select v-model="form.sweetness">
    <el-option
        v-for="item in sweetnessOptions"
        :key="item.refCode"
        :label="item.refValue"
        :value="item.refCode"
    />
</el-select>

// 显示时转换
{{ getOptionLabel(sweetnessOptions, order.sweetness) }}
```

## 数据库 SQL

如果需要手动创建表：

```sql
CREATE TABLE IF NOT EXISTS reference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ref_type VARCHAR(50) NOT NULL,
    ref_key VARCHAR(50) NOT NULL,
    ref_value VARCHAR(200) NOT NULL,
    ref_code INT NOT NULL,
    sort_order INT DEFAULT 0,
    is_active INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ref_type (ref_type),
    INDEX idx_ref_key (ref_key),
    UNIQUE KEY uk_type_key (ref_type, ref_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 优势

1. **集中管理**：所有配置集中在一个表
2. **灵活扩展**：可以随时添加新的配置类型
3. **易于维护**：通过 SQL 或 API 即可修改配置
4. **多语言支持**：可以通过 ref_value 存储不同语言
5. **动态加载**：前端实时获取最新配置

## 注意事项

1. `ref_code` 是实际存储在订单表中的值
2. `ref_value` 是前端显示的文本
3. 修改 `ref_code` 可能会影响历史数据
4. 建议使用 `is_active` 来禁用选项，而不是删除
