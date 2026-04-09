# Contacts Backend

基于 Spring Boot 3 + Kotlin 构建的联系人管理 REST API 后端，支持多设备实时同步、端对端加密存储与 JWT 身份认证。

## 技术栈

| 层级 | 技术 |
|------|------|
| 语言 / 框架 | Kotlin 1.9.22 + Spring Boot 3.2.3 |
| 运行时 | Java 21 |
| 数据库 | MySQL / PostgreSQL + Spring Data JPA + Flyway |
| 缓存 | Redis 7 |
| 认证 | JWT (JJWT 0.12.3) + Spring Security |
| 实时通信 | WebSocket / STOMP + SockJS |
| 文档 | OpenAPI 3.0 (springdoc 2.3.0) |
| 容器化 | Docker + docker-compose |

## 功能特性

- **JWT 多设备认证** — Access Token（15 分钟）+ Refresh Token（30 天），令牌绑定设备，支持独立吊销
- **联系人端对端加密** — 加密负载 + Nonce/Tag 存储，支持密钥轮换
- **软删除 + 撤销** — 删除后 5 秒内可撤销，后台定时任务确认真正删除
- **增量同步（CRDT 风格）** — 客户端按版本号拉取变更，减少全量传输
- **Outbox 事件模式** — 联系人变更写入 outbox 表，定时任务推送 WebSocket 通知，保证可靠投递
- **乐观锁** — JPA `@Version` 防止并发写入冲突

## 项目结构

```
src/main/kotlin/com/contacts/
├── controller/          # REST 端点
│   ├── AuthController       # 认证
│   ├── ContactController    # 联系人 CRUD & 同步
│   └── DeviceController     # 设备管理
├── service/             # 业务逻辑
├── repository/          # 数据访问层
├── domain/
│   ├── entity/          # JPA 实体
│   └── enums/           # 枚举类型
├── dto/                 # 数据传输对象
├── config/              # 配置（Security / WebSocket / Redis）
├── security/            # JWT 过滤器 & 用户主体
├── websocket/           # WebSocket 认证拦截器 & 消息体
└── exception/           # 全局异常处理
```

## API 概览

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/register` | 注册账号并绑定初始设备 |
| POST | `/login` | 登录，返回 Access / Refresh Token |
| POST | `/refresh` | 使用 Refresh Token 换取新 Access Token |
| POST | `/logout` | 吊销 Refresh Token，设备下线 |

### 联系人 `/api/contacts`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 分页获取联系人列表 |
| POST | `/` | 创建联系人（加密负载） |
| PUT | `/{id}` | 更新联系人 |
| DELETE | `/{id}` | 软删除（5 秒撤销窗口） |
| POST | `/{id}/undo-delete` | 撤销删除 |
| GET | `/sync` | 增量同步（`?sinceVersion=N`） |

### 设备 `/api/devices`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 获取当前用户所有设备 |
| DELETE | `/{deviceId}` | 移除设备 |

### WebSocket `/ws`

- STOMP over SockJS，连接时携带 JWT 进行认证
- 服务端通过 `/topic` / `/queue` 推送联系人变更通知

## 快速启动

### 前置条件

- Docker & docker-compose

### 启动

```bash
cd contacts-backend
docker-compose up -d
```

服务启动后访问：
- API：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui.html`

### 本地开发

```bash
# 仅启动依赖服务
docker-compose up -d db redis

# 运行应用
./gradlew bootRun
```

## 数据库迁移

使用 Flyway 自动管理，脚本位于 `src/main/resources/db/migration/`，应用启动时自动执行。

## 安全说明

> 生产环境部署前，请将 `application.yml` 中的 JWT Secret 替换为环境变量注入，切勿使用默认值。
