# Social Factory Blog

一个简洁优雅的个人博客系统，基于 **Astro** 前端 + **Spring Boot** 后端构建。

> 当前版本面向**本地开发运行**，已移除服务器部署相关配置（Docker、Nginx、CI/CD 等）。

## 功能特性

- 文章管理：Markdown 写作、代码高亮、分类与标签
- 用户系统：注册登录、JWT 认证、角色权限（USER / ADMIN）
- 互动功能：评论（支持回复）、点赞、收藏
- 图形验证码：防机器人注册，内存存储
- 运营支撑：审计日志、登录限流、用户管理后台

## 技术栈

### 前端
| 技术 | 说明 |
|------|------|
| Astro 4 | 静态站点生成器 |
| Tailwind CSS | 原子化 CSS 框架 |
| rehype-pretty-code | Shiki 代码高亮 |

### 后端
| 技术 | 说明 |
|------|------|
| Spring Boot 3.1.5 | Java 后端框架 |
| MyBatis-Plus 3.5.9 | ORM 框架 |
| Spring Security + JWT | 认证与授权 |
| MySQL 8 | 数据库 |
| Caffeine | 本地缓存 |
| MinIO / Nginx / Docker | —— 已移除，本地不使用 |

---

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端构建工具 |
| Node.js | 18+ | 前端构建环境 |
| MySQL | 8.0+ | 数据库 |

## 快速开始

### 1. 初始化数据库

数据库结构统一定义在 `blog-backend/src/main/resources/db/schema.sql`（幂等，可重复执行）：

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 建表（9 张表）
mysql -u root -p blog_db < blog-backend/src/main/resources/db/schema.sql
```

建表后无需手动导入种子数据——后端首次启动会自动创建默认管理员并写入分类、标签与示例文章。

### 2. 安装前端依赖

```bash
cd social-factory-blog
npm install
```

### 3. 启动

**方式一：一键脚本（推荐）**

Windows 双击 `start-dev.bat`，或在 Git Bash / Linux / macOS 执行：

```bash
./start-dev.sh all      # 同时启动前后端
./start-dev.sh backend  # 仅启动后端
./start-dev.sh frontend # 仅启动前端
./start-dev.sh stop     # 停止
```

脚本会自动检查依赖与 MySQL 端口，等待服务就绪后打印访问地址，日志输出到 `.dev-logs/`。

**方式二：手动分两个终端启动**

```bash
# 终端 1 — 后端（http://localhost:8080/api）
cd blog-backend
mvn spring-boot:run

# 终端 2 — 前端（http://localhost:4321）
cd social-factory-blog
SITE_URL="http://localhost:4321" npm run dev
```

### 4. 访问

| 地址 | 说明 |
|------|------|
| http://localhost:4321 | 前端首页 |
| http://localhost:4321/register | 注册页 |
| http://localhost:4321/login | 登录页 |
| http://localhost:4321/admin | 管理后台 |
| http://localhost:8080/api | 后端接口根路径 |

**默认管理员**：`admin` / `admin123`

---

## 配置说明

### 后端：`blog-backend/src/main/resources/application.yml`

所有配置均为本地默认值，按需直接修改即可；也可用环境变量覆盖：

| 配置项 | 默认值 | 环境变量 |
|--------|--------|----------|
| 服务端口 | 8080 | — |
| 数据库地址 | `localhost:3306/blog_db` | `DB_URL` |
| 数据库账号 | root | `DB_USERNAME` |
| 数据库密码 | 123456 | `DB_PASSWORD` |
| 上传目录 | ./uploads | — |
| 单文件上限 | 10MB | — |

调试时如需打印 SQL，将 `mybatis-plus.configuration.log-impl` 改为：

```yaml
log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### 前端：`social-factory-blog/.env` 与 `astro.config.mjs`

| 配置 | 位置 | 说明 |
|------|------|------|
| `SITE_URL` | 环境变量 | 站点地址，本地设 `http://localhost:4321` |
| `/api` 代理 | `astro.config.mjs` | 转发到 `http://127.0.0.1:8080` |

> 注：代理地址使用 `127.0.0.1` 而非 `localhost`，因为 Node 解析 `localhost` 会优先取 IPv6 的 `::1`，而 Spring Boot 默认只监听 IPv4，会导致代理连接被拒。

---

## 目录结构

```
my-blog/
├── blog-backend/                  # Spring Boot 后端
│   ├── pom.xml
│   ├── uploads/                   # 图片上传目录（运行时生成）
│   └── src/main/
│       ├── java/com/example/blog/
│       │   ├── config/            # 配置类（安全、MVC、缓存、MyBatis、种子数据）
│       │   ├── constant/          # 常量定义
│       │   ├── controller/        # REST 控制器
│       │   ├── dto/               # 请求 / 响应对象
│       │   ├── entity/            # 数据库实体
│       │   ├── exception/         # 异常处理
│       │   ├── mapper/            # 数据访问层
│       │   ├── security/          # 认证与授权
│       │   ├── service/           # 业务逻辑
│       │   └── util/              # 工具类
│       └── resources/
│           ├── application.yml    # 应用配置
│           ├── db/schema.sql      # 唯一的数据库结构定义文件
│           └── mapper/*.xml       # MyBatis SQL 映射
│
├── social-factory-blog/           # Astro 前端
│   ├── astro.config.mjs           # 构建与开发配置
│   ├── package.json
│   ├── public/                    # 静态资源
│   └── src/
│       ├── components/            # 组件
│       ├── layouts/               # 布局
│       ├── lib/                   # 工具与 API 配置
│       ├── pages/                 # 页面路由
│       ├── content/               # Markdown 内容
│       └── styles/                # 全局样式
│
├── start-dev.sh                   # 一键启动（类 Unix）
├── start-dev.bat                  # 一键启动（Windows）
├── .gitignore
└── README.md
```

---

## 常见问题

**Q：后端启动报 `Unknown database 'blog_db'`**
A：数据库未创建。按「快速开始」第 1 步执行建库与建表。

**Q：前端报 `ERR_CONNECTION_CLOSED` / 请求域名不是 localhost**
A：检查 `social-factory-blog/.env`，确保 `PUBLIC_API_BASE_URL` 未设置为线上地址。本地开发应留空，走相对路径 `/api` 由 Vite 代理。

**Q：验证码图片不显示**
A：按 F12 查看 Network。确认 `/api/captcha/generate` 返回 200；若 502 说明后端未启动。

**Q：端口被占用**
A：后端改 `application.yml` 的 `server.port`，前端改 `astro.config.mjs` 的 `server.port`。

**Q：上传的图片访问不到**
A：确认 `blog-backend/uploads/` 目录存在且后端从项目根目录启动（相对路径 `./uploads` 依赖启动位置）。

---

## 说明

本版本仅保留本地运行所需内容。若后续需要服务器部署，可从 Git 历史恢复被移除的 `deploy/`、`.github/` 等目录。
