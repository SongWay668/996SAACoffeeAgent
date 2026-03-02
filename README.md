# 咖啡店多Agent智能系统

基于 Spring AI Alibaba + MCP (Model Context Protocol) + Mem0 构建的咖啡店智能客服多Agent系统(Supervisor模式)，实现了咨询、订单、反馈等业务场景的智能化处理，
持续根据用户行为和喜好推荐并下单产品, 从而实现"越来越懂我", "越用越好用"的用户体验。

主要功能包括:
产品咨询与产品推荐。根据用户习惯和喜好, 为用户推荐咖啡产品并介绍, 同时分析并记录用户习惯和喜好。
点单与订单查询。根据用户需求下订单、修改订单和查询订单, 同时分析并记录用户习惯和喜好。
反馈与投诉处理。处理用户反馈, 对于投诉或差评安抚情绪并出解决方案, 同时分析并记录用户习惯和喜好。

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Supervisor Agent (调度中心)                │
│                  端口: 10008 | LlmRoutingAgent               │
└────────────┬──────────────────────────────────┬─────────────┘
             │                                  │
    ┌────────▼─────────┐        ┌──────────────▼─────────────┐
    │  Consult Agent   │        │       Order Agent          │
    │  咨询助手         │        │       订单助手              │
    │  端口: 8082       │        │       端口: 8083           │
    └──────────────────┘        └──────────────┬─────────────┘
                                            │
                                 ┌──────────▼───────────┐
                                 │   Feedback Agent     │
                                 │    反馈助手          │
                                 │    端口: 8084        │
                                 └──────────────────────┘
             │                                  │
    ┌────────▼─────────┐        ┌──────────────▼─────────────┐
    │ Coffee Shop MCP  │        │    Memory MCP Server       │
    │ Server (8080)    │        │       (8081)               │
    │ 产品/订单查询     │        │    用户偏好记忆             │
    └──────────────────┘        └────────────────────────────┘
```

## 模块说明

| 模块 | 端口 | 功能描述 |
|------|------|----------|
| **9-supervisor-agent** | 10008 | 调度中心，根据用户问题路由到对应的子Agent |
| **1-coffee-shop** | 8080 | MCP Server，提供产品和订单相关工具 |
| **2-memory-mcp-server** | 8081 | MCP Server，提供用户偏好记忆工具（基于OpenSearch） |
| **3-consult-sub-agent** | 8082 | 产品咨询、个性化推荐、智能问答 |
| **4-order-sub-agent** | 8083 | 订单查询、下单、订单状态跟踪 |
| **5-feedback-sub-agent** | 8084 | 用户反馈收集、满意度分析、情绪管理 |


## 技术栈

- **框架**: Spring Boot 3.x
- **AI框架**: Spring AI Alibaba 1.1.1.2(DashScope)
- **模型**: 通义千问 (qwen-plus)
- **向量数据库**: OpenSearch (本地部署)
- **CURD数据库**: MySQL + MyBatis
- **短期对话记忆**:Redis
- **长期记忆**:mem0 (本地部署)
- **协议**: MCP (Model Context Protocol)
- **Agent类型**: LlmRoutingAgent + ReactAgent

## 前置要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 3.0
- OpenSearch 2.x
- 通义千问 API Key
- mem0
- Python 3.8+（用于 mem0）
- Poetry（用于 mem0 依赖管理）

## 快速开始

### 前置
启动Mysql数据库
启动Redis服务
启动Opensearch服务
启动 mem0 服务：
```bash
cd .\mem0\server
uvicorn main:app --host 0.0.0.0 --port 8765
```

### 1. 环境变量配置

创建环境变量文件或设置以下环境变量：

```bash
# 通义千问 API Key
export API-KEY=your-dashscope-api-key

# OpenSearch 密码
export OPENSEARCH_INITIAL_ADMIN_PASSWORD=your-opensearch-password

# MySQL 密码（可选，如果配置文件中已配置）
export DBKEY=your-mysql-password
```

### 2. 数据库初始化

```bash
mysql -u root -p < sql/coffee_shop.sql
```

### 3. 启动 MCP Servers

```bash
# 启动 Coffee Shop MCP Server (端口 8080)
cd 1-coffee-shop
mvn spring-boot:run

# 启动 Memory MCP Server (端口 8081)
cd 2-memory-mcp-server
mvn spring-boot:run
```

### 4. 启动模式选择

#### 模式一：启动 Supervisor（推荐）

所有子Agent由Supervisor统一调度，共用端口10008：

```bash
cd 9-supervisor-agent
mvn spring-boot:run
```

Supervisor 会自动加载并调用子Agent，无需单独启动。

#### 模式二：独立启动各子Agent

各子Agent独立运行，分别占用不同端口：

```bash
# Terminal 1: Consult Agent (端口 8082)
cd 3-consult-sub-agent
mvn spring-boot:run

# Terminal 2: Order Agent (端口 8083)
cd 4-order-sub-agent
mvn spring-boot:run

# Terminal 3: Feedback Agent (端口 8084)
cd 5-feedback-sub-agent
mvn spring-boot:run
```

## API 使用示例

### 调用 Supervisor Agent

```bash
curl -X POST http://localhost:10008/chat \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "12345",
    "message": "来杯美式咖啡"
  }'
```

### 调用子 Agent（独立模式）

```bash
# Consult Agent
curl -X POST http://localhost:8082/chat \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "12345",
    "message": "你们有什么推荐的咖啡？"
  }'

# Order Agent
curl -X POST http://localhost:8083/chat \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "12345",
    "message": "查一下订单 ORDER_12345 的状态"
  }'

# Feedback Agent
curl -X POST http://localhost:8084/chat \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "12345",
    "message": "订单 ORDER_12345 服务很好，五星好评"
  }'
```

## 配置说明

### Supervisor 配置 (application.yml)

```yaml
spring:
  profiles:
    include:
      - consult    # 加载 consult sub-agent
      - order      # 加载 order sub-agent
      - feedback   # 加载 feedback sub-agent
```

### 子Agent配置

每个子Agent有两种配置文件：

- `application-xxx.yml`: 通用配置（supervisor模式和独立模式共用）
- `application.yml`: 独立模式专用（包含端口配置）
- 
### MCP工具过滤

1. 手动过滤
```java
// ConsultAgent 只加载 consult 和 memory 相关的MCP工具
if (toolName.startsWith("consult") || toolName.startsWith("memory")) {
    McpTools.add(toolCallback);
}
```
2. 自动过滤
各子Agent通过 `McpToolFilter` 实现工具过滤，由于子Agent和Supervisor一起启动，自动过滤无法工作。
如部署在Nacos上， 从 `@Component` 改为 `@Configuration`

## 业务流程

### 咨询流程 (Consult Agent)

1. 用户发送咨询消息（可选带 user_id）
2. 如果有 user_id，先查询用户历史偏好（memory_search）
3. 查询产品知识库（consult-search-knowledge）
4. 结合偏好和知识库，提供个性化推荐
5. 识别新偏好，更新用户记忆（memory_store）

### 订单流程 (Order Agent)

1. 用户发送订单相关消息
2. 解析用户意图（查询/下单/取消）
3. 调用对应的MCP工具（order-query、order-create等）
4. 返回订单信息或处理结果

### 反馈流程 (Feedback Agent)

1. 用户发送反馈消息（必须带 user_id）
2. 查询用户历史偏好（memory_search）
3. 识别反馈类型（好评/差评/投诉/建议）
4. 提取评分（1-5星）和订单号（如有）
5. 调用 feedback_create_feedback 记录反馈
6. 更新用户偏好（memory_store）

## 项目结构

```
996SAACoffeeAgent/
├── 1-coffee-shop/              # Coffee Shop MCP Server
├── 2-memory-mcp-server/        # Memory MCP Server (OpenSearch)
├── 3-consult-sub-agent/        # 咨询子Agent
├── 4-order-sub-agent/          # 订单子Agent
├── 5-feedback-sub-agent/       # 反馈子Agent
├── 9-supervisor-agent/         # 调度中心Agent
├── mem0/                       # AI记忆增强框架 (Python)
├── pom.xml                     # Maven 父POM
├── sql/                        # 数据库脚本
└── README.md                   # 本文档
```

## 项目说明

需要在Opeansearch里提前创建index：coffeeshop，并上传.\3-consult-sub-agent\src\main\resources\knowledge里的文件

mem0 配置参考 
https://docs.mem0.ai/open-source/overview

推荐使用Nacos实现A2A 模式
https://github.com/spring-ai-alibaba/spring-ai-alibaba-multi-agent-demo

数据操作相关代码仅供演示，未做完整性保证(如下单并不会减少库存)

## 开发指南

### 添加新的子Agent

1. 创建新的子Agent模块
2. 实现Agent配置类（参考 `ConsultAgent.java`）
3. 创建 instruction 文件（参考 `consult-instruction.txt`）
4. 在 Supervisor 的 `application.yml` 中添加 profile
5. 在 Supervisor 的 `SupervisorAgent.java` 中注册新Agent

### 添加新的MCP工具

1. 在对应的 MCP Server 中添加工具方法（使用 `@McpTool` 注解）
2. 更新子Agent的 `McpToolFilter` 以包含新工具
3. 更新 instruction 文件说明新工具的使用方式

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue。
