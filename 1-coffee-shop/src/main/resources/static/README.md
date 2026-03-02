# 前后端合一说明

## 项目改造完成

已将前后端分离的项目改造为前后端合一的 Spring Boot 项目。

## 目录结构

```
1-coffee-shop/
├── src/
│   └── main/
│       ├── java/              # Java 后端代码
│       └── resources/
│           ├── static/        # ⭐ 新增：前端静态资源
│           │   └── index.html # Vue + Element Plus 前端页面
│           ├── application.yml
│           ├── mapper/        # MyBatis XML
│           └── config/        # 配置类
└── frontend/                  # 旧的独立前端项目（已不再使用）
```

## 访问方式

### 旧方式（前后端分离）
- 前端：http://localhost:5173
- 后端：http://localhost:8080/api

### 新方式（前后端合一）
- 访问：http://localhost:8080/index.html
- API：http://localhost:8080/api

## 启动步骤

1. 确保 MySQL 数据库已创建：
   ```sql
   CREATE DATABASE coffee_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 启动 Spring Boot 应用：
   ```bash
   mvn spring-boot:run
   ```

3. 打开浏览器访问：
   ```
   http://localhost:8080/index.html
   ```

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis 3.5.15
- Druid 连接池
- MySQL 8.2.0

### 前端（CDN方式引入）
- Vue 3
- Vue Router 4
- Element Plus
- 原生 Fetch API

## 优势对比

| 特性 | 前后端分离 | 前后端合一 |
|------|----------|-----------|
| 部署 | 需要部署两个服务 | 只需部署一个服务 |
| 开发环境 | 需要Node.js + Spring Boot | 只需 Java 环境 |
| 跨域问题 | 需要配置 CORS | 无跨域问题 |
| 构建 | 需要 npm build | 直接运行 |
| 访问 | 两个端口 | 一个端口 |

## 注意事项

1. **数据库配置**：确保 MySQL 已启动，数据库 `coffee_shop` 已创建

2. **API 前缀**：所有 API 接口以 `/api` 开头

3. **静态资源**：所有静态资源放在 `src/main/resources/static/` 目录下

4. **旧项目**：`frontend/` 目录可以保留或删除，不再使用

## 数据库表

请确保数据库中有以下表（如果没有，需要先创建）：
- users
- products
- orders
- feedback
- user_preferences
- references

## 进一步优化建议

如果需要更高级的前端开发体验，可以考虑：

1. 使用 Vite 构建（构建后放到 static 目录）
2. 添加热重载
3. 使用 TypeScript
4. 添加前端单元测试
