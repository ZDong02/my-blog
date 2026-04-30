# Social Factory Blog

一个简洁优雅的个人博客系统，基于 Astro 前端 + Spring Boot 后端构建，支持 Docker 一键部署。

## Tech Stack

### Frontend
- **Astro** - 现代静态站点生成器
- **Tailwind CSS** - Utility-first CSS 框架
- **GSAP** - 动画库

### Backend
- **Spring Boot 3** - Java 后端框架
- **MyBatis-Plus** - ORM 框架
- **JWT** - 身份认证
- **MySQL** - 主数据库
- **Redis** - 缓存（可选）

### Infrastructure
- **Docker** - 容器化部署
- **Nginx** - 反向代理
- **MinIO** - 对象存储（可选）

## Features

- :memo: **文章管理** - Markdown 写作，支持代码高亮
- :person: **用户系统** - 注册、登录、JWT 认证
- :musical_note: **音乐管理** - 歌单管理（可选功能）
- :framed_picture: **文件上传** - MinIO 对象存储
- :lock: **安全特性** - 登录限流、审计日志
- :whale: **Docker 部署** - 一键部署到生产环境

## Project Structure

```
my-blog/
├── social-factory-blog/     # Astro 前端项目
│   ├── src/
│   │   ├── components/      # Vue/Astro 组件
│   │   ├── layouts/         # 页面布局
│   │   ├── pages/           # 路由页面
│   │   └── content/         # Markdown 内容
│   └── public/              # 静态资源
│
├── blog-backend/            # Spring Boot 后端项目
│   └── src/main/java/com/example/blog/
│       ├── controller/      # REST API 控制器
│       ├── service/         # 业务逻辑层
│       ├── mapper/          # 数据访问层
│       ├── entity/          # 数据实体
│       ├── config/          # 配置类
│       ├── security/        # 安全相关
│       └── exception/       # 异常处理
│
├── deploy/                  # Docker 部署配置
│   ├── docker-compose.yml   # 生产环境编排
│   ├── docker-compose-simple.yml  # 开发环境编排
│   ├── nginx.conf          # Nginx 配置
│   └── db/                 # 数据库初始化脚本
│
└── nginx/                   # Nginx 配置备份
```

## Quick Start

### Prerequisites

- Node.js 18+
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Docker 20.10+ (for deployment)

### Frontend Development

```bash
cd social-factory-blog
npm install
npm run dev
```

访问 `http://localhost:4321`

### Backend Development

```bash
cd blog-backend
# 配置 application.yml 中的数据库连接
./mvnw spring-boot:run
```

API 运行于 `http://localhost:8080`

### Database Setup

```bash
mysql -u root -p < deploy/db/init-tables.sql
```

## Deployment

### One-Click Deploy (Recommended)

```bash
# 生产环境
cd deploy
docker compose -f docker-compose.yml up -d --build

# 或使用简化版本（SQLite）
docker compose -f docker-compose-simple.yml up -d
```

### Manual Deployment

1. 构建前端：`cd social-factory-blog && npm run build`
2. 构建后端：`cd blog-backend && ./mvnw clean package -DskipTests`
3. 配置 Nginx 和 Docker Compose
4. 启动服务：`docker compose up -d`

详细部署指南请参考 [deploy/README.md](deploy/README.md)

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | - | JWT 密钥（必填） |
| `DB_URL` | jdbc:mysql://mysql:3306/blog_db | 数据库地址 |
| `DB_USERNAME` | blog_user | 数据库用户名 |
| `DB_PASSWORD` | - | 数据库密码（必填） |
| `MINIO_ENABLED` | false | 启用 MinIO |
| `MINIO_ENDPOINT` | http://localhost:9000 | MinIO 地址 |
| `MINIO_ACCESS_KEY` | admin | MinIO 用户名 |
| `MINIO_SECRET_KEY` | - | MinIO 密码 |

## API Endpoints

### Auth
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### Posts
- `GET /api/posts` - 获取文章列表
- `GET /api/posts/{id}` - 获取文章详情
- `POST /api/posts` - 创建文章
- `PUT /api/posts/{id}` - 更新文章
- `DELETE /api/posts/{id}` - 删除文章

### Upload
- `POST /api/upload` - 文件上传

## Port Reference

| Port | Service |
|------|---------|
| 80 | Nginx HTTP |
| 443 | Nginx HTTPS |
| 3306 | MySQL |
| 8080 | Spring Boot |
| 9000 | MinIO API |
| 9001 | MinIO Console |

## License

MIT License
