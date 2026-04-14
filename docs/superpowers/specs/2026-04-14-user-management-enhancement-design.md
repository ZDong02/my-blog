# 用户管理功能增强设计方案

## 概述

为现有用户管理界面添加三个新功能：用户详情查看/编辑、批量操作、用户统计/报表。

## 1. 用户详情查看/编辑

### 功能描述
- 点击用户行或"详情"按钮打开侧边抽屉
- 显示用户完整信息和可编辑字段

### UI 组件
- **侧边抽屉 (Drawer)**：从右侧滑入，宽度 480px
- 包含：头像、昵称、用户名、邮箱、角色（可改）、状态（可改）、注册时间、最后登录时间、文章数、评论数

### 数据字段
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 昵称 |
| email | String | 邮箱 |
| avatar | String | 头像URL |
| role | String | ADMIN/USER |
| status | Integer | 1正常/0禁用 |
| postCount | Integer | 文章数 |
| commentCount | Integer | 评论数 |
| createdAt | DateTime | 注册时间 |
| lastLoginAt | DateTime | 最后登录 |

### API 端点
- `GET /admin/users/{userId}` - 获取用户详情（需扩展返回 commentCount, lastLoginAt）

## 2. 批量操作

### 功能描述
- 用户列表添加复选框列
- 支持全选/取消全选
- 批量启用、批量禁用、批量删除
- 删除需二次确认

### UI 元素
| 元素 | 位置 | 说明 |
|------|------|------|
| 复选框列 | 表格第一列 | 每行一个 + 表头全选 |
| 批量工具栏 | 表格上方 | 显示选中数量和操作按钮 |
| 确认对话框 | 模态框 | 删除时显示将被删除的用户列表 |

### API 端点
- `PUT /admin/users/batch/status` - 批量更新状态，body: `{ userIds: [], status: 1 }`
- `DELETE /admin/users/batch` - 批量删除，body: `{ userIds: [] }`

## 3. 用户统计/报表

### 功能描述
- 管理员后台添加统计页面或在该页面顶部添加统计卡片
- 关键指标：总用户数、今日新增、本月新增、活跃用户数
- 角色分布饼图
- 用户增长趋势折线图（近30天）

### 统计指标
| 指标 | 说明 |
|------|------|
| totalUsers | 总用户数 |
| todayNewUsers | 今日新增 |
| monthNewUsers | 本月新增 |
| activeUsers | 活跃用户（近30天有登录） |
| roleDistribution | 角色分布 { ADMIN: count, USER: count } |
| growthTrend | 每日新增用户数数组 |

### API 端点
- `GET /admin/users/stats` - 获取统计数据

### 响应格式
```json
{
  "success": true,
  "data": {
    "totalUsers": 1234,
    "todayNewUsers": 23,
    "monthNewUsers": 156,
    "activeUsers": 892,
    "roleDistribution": { "ADMIN": 5, "USER": 1229 },
    "growthTrend": [
      { "date": "2026-04-01", "count": 12 },
      { "date": "2026-04-02", "count": 8 }
    ]
  }
}
```

## 文件修改清单

### 前端 (social-factory-blog)
| 文件 | 操作 | 说明 |
|------|------|------|
| `src/pages/admin/users/index.astro` | 修改 | 添加复选框、批量操作、统计卡片、详情抽屉 |
| `src/lib/api.js` | 修改 | 添加 batchUpdateStatus, batchDeleteUsers, getUserStats |

### 后端 (blog-backend)
| 文件 | 操作 | 说明 |
|------|------|------|
| `UserManagementController.java` | 修改 | 添加 batchUpdateStatus, batchDeleteUsers, getUserStats |
| `UserMapper.java` | 修改 | 添加统计相关方法 |
| `UserMapper.xml` | 修改 | 添加统计SQL |
| `UserService.java` | 修改 | 添加统计服务方法 |

## 依赖
- Chart.js (CDN) - 用于图表渲染
