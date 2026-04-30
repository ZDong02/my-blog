# Blog 部署指南

## 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 最低 2C2G 服务器

## 快速启动

### 1. 基础部署（使用 SQLite 开发数据库）

```bash
cd deploy
docker compose up -d
```

### 2. 生产部署（使用 MySQL）

```bash
cd deploy
docker compose -f docker-compose.yml up -d --build
```

## 目录结构

```
deploy/
├── Dockerfile.backend    # 后端 Dockerfile
├── docker-compose.yml    # Docker Compose 配置
├── nginx.conf           # Nginx 配置
├── nginx-host.conf      # 主机 Nginx 配置
├── backend.jar          # 后端 JAR 包（可选）
├── db/
│   └── init-tables.sql  # 数据库初始化脚本
└── scripts/
    ├── setup-docker.sh  # Docker 环境安装脚本
    └── setup-server.sh  # 服务器初始化脚本
```

## 配置说明

### 环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `JWT_SECRET` | - | JWT 密钥（**必须设置**） |
| `DB_URL` | jdbc:mysql://mysql:3306/blog_db | 数据库连接地址 |
| `DB_USERNAME` | blog_user | 数据库用户名 |
| `DB_PASSWORD` | - | 数据库密码（**必须设置**） |
| `SPRING_PROFILES_ACTIVE` | prod | Spring 环境 |
| `MINIO_ENABLED` | false | 是否启用 MinIO |
| `MINIO_ENDPOINT` | http://localhost:9000 | MinIO 地址 |
| `MINIO_ACCESS_KEY` | admin | MinIO 用户名 |
| `MINIO_SECRET_KEY` | - | MinIO 密码（**必须设置**） |

### 端口说明

| 端口 | 服务 |
|------|------|
| 80 | Nginx HTTP |
| 443 | Nginx HTTPS |
| 3306 | MySQL |
| 8080 | Spring Boot 后端 |
| 9000 | MinIO API |
| 9001 | MinIO Console |

## 数据持久化

- `mysql_data` - MySQL 数据卷
- `uploads_data` - 上传文件卷
- `minio_data` - MinIO 数据卷

## 常用命令

```bash
# 查看容器状态
docker compose ps

# 查看日志
docker compose logs -f

# 重启服务
docker compose restart

# 停止服务
docker compose down

# 重新构建
docker compose up -d --build
```

## 生产环境检查清单

1. [ ] 修改 `JWT_SECRET` 为强密码
2. [ ] 修改数据库密码
3. [ ] 配置 SSL 证书（443 端口）
4. [ ] 配置防火墙规则
5. [ ] 启用 MinIO 并配置存储

## 故障排除

### 502 Bad Gateway

后端服务未启动，检查：
```bash
docker compose logs backend
```

### 连接数据库失败

检查 MySQL 是否健康：
```bash
docker compose ps mysql
```

### 前端无法访问

检查 Nginx 是否正常：
```bash
docker compose logs nginx
```
