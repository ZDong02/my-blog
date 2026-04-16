# 音乐管理功能设计

**日期：** 2026-04-16

## 概述

在后台管理中实现音乐管理功能，允许管理员添加、编辑、删除喜欢的音乐。公开的音乐页面展示音乐列表，点击后跳转到播放页面。

---

## 数据结构

### Music 实体

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 主键，自增 |
| title | String | 歌曲名称（必填） |
| artist | String | 艺术家 |
| youtubeId | String | YouTube 视频 ID（必填） |
| category | String | 分类标签（如 R&B、Pop、Rock） |
| sortOrder | Integer | 排序顺序（越小越靠前） |
| coverImage | String | 封面图 URL（可选） |
| createdAt | LocalDateTime | 创建时间 |
| updatedAt | LocalDateTime | 更新时间 |

---

## API 设计

| 方法 | 端点 | 权限 | 说明 |
|------|------|------|------|
| GET | `/music` | 公开 | 获取所有音乐，按 sortOrder 升序 |
| GET | `/music/{id}` | 公开 | 获取单个音乐详情 |
| POST | `/music` | Admin | 创建音乐 |
| PUT | `/music/{id}` | Admin | 更新音乐 |
| DELETE | `/music/{id}` | Admin | 删除音乐 |

---

## 后台管理页面 (`/admin/music`)

- 使用 AdminLayout 布局
- 音乐列表页，包含：
  - 搜索框（按歌曲名/艺术家搜索）
  - 表格展示：标题、艺术家、分类、排序、封面、操作
  - 添加按钮 → 打开添加弹窗
  - 每行操作：编辑、删除
- 添加/编辑弹窗字段：标题、艺术家、YouTube ID、分类、排序、封面图

---

## 公开页面 (`/music`)

- 现有页面改造
- 按 sortOrder 升序展示双列列表
- 点击音乐项跳转到 `/music?play={youtubeId}` 并内嵌播放该视频
- 维持现有视觉风格

---

## 技术实现

### 后端 (blog-backend)

- `entity/Music.java` — 实体类
- `mapper/MusicMapper.java` — Mapper 接口
- `service/MusicService.java` — 业务逻辑
- `controller/MusicController.java` — REST 控制器

### 前端 (social-factory-blog)

- `src/lib/api.js` — 添加 music 相关 API 方法
- `src/pages/admin/music/index.astro` — 后台管理页面
- `src/pages/music.astro` — 公开页面改造
