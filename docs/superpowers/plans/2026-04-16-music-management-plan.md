# 音乐管理功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在后台管理中添加音乐管理功能，允许管理员添加/编辑/删除音乐，公开页面展示音乐列表并支持播放。

**Architecture:** 后端创建 Music 实体 + Mapper + Service + Controller，前端添加 API 方法并改造管理页面和公开页面。

**Tech Stack:** Spring Boot (MyBatis Plus), Astro, MySQL

---

## 文件结构

```
blog-backend/
├── src/main/java/com/example/blog/
│   ├── entity/Music.java                          # 新建
│   ├── mapper/MusicMapper.java                    # 新建
│   ├── service/MusicService.java                  # 新建
│   └── controller/MusicController.java            # 新建
├── src/main/resources/mapper/MusicMapper.xml      # 新建
└── src/main/resources/schema.sql                  # 修改（添加 music 表）

social-factory-blog/
├── src/lib/api.js                                 # 修改（添加 music API 方法）
├── src/pages/admin/music/index.astro              # 新建
└── src/pages/music.astro                          # 修改
```

---

## Task 1: 创建 Music 实体类

**Files:**
- Create: `blog-backend/src/main/java/com/example/blog/entity/Music.java`

- [ ] **Step 1: 创建 Music.java**

```java
package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 音乐实体类
 * 对应数据库表：music
 *
 * @author Blog Team
 * @date 2026-04-16
 */
@Data
@TableName("music")
public class Music {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 歌曲名称
     */
    @TableField("title")
    private String title;

    /**
     * 艺术家
     */
    @TableField("artist")
    private String artist;

    /**
     * YouTube 视频 ID
     */
    @TableField("youtube_id")
    private String youtubeId;

    /**
     * 分类标签（如 R&B、Pop、Rock）
     */
    @TableField("category")
    private String category;

    /**
     * 排序顺序（越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 封面图 URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/entity/Music.java
git commit -m "feat: add Music entity

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 创建 MusicMapper 接口

**Files:**
- Create: `blog-backend/src/main/java/com/example/blog/mapper/MusicMapper.java`

- [ ] **Step 1: 创建 MusicMapper.java**

```java
package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Music;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MusicMapper extends BaseMapper<Music> {
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/mapper/MusicMapper.java
git commit -m "feat: add MusicMapper interface

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 创建 MusicMapper.xml

**Files:**
- Create: `blog-backend/src/main/resources/mapper/MusicMapper.xml`

- [ ] **Step 1: 创建 MusicMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.blog.mapper.MusicMapper">

    <resultMap id="MusicResultMap" type="com.example.blog.entity.Music">
        <id property="id" column="id"/>
        <result property="title" column="title"/>
        <result property="artist" column="artist"/>
        <result property="youtubeId" column="youtube_id"/>
        <result property="category" column="category"/>
        <result property="sortOrder" column="sort_order"/>
        <result property="coverImage" column="cover_image"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>

    <!-- 查询所有音乐，按 sort_order 升序 -->
    <select id="selectAllOrderBySortOrder" resultMap="MusicResultMap">
        SELECT * FROM music ORDER BY sort_order ASC, id DESC
    </select>

</mapper>
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/resources/mapper/MusicMapper.xml
git commit -m "feat: add MusicMapper.xml

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 创建 MusicService

**Files:**
- Create: `blog-backend/src/main/java/com/example/blog/service/MusicService.java`

- [ ] **Step 1: 创建 MusicService.java**

```java
package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blog.entity.Music;
import com.example.blog.mapper.MusicMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicService {

    @Autowired
    private MusicMapper musicMapper;

    /**
     * 获取所有音乐，按 sort_order 升序排列
     */
    public List<Music> getAllMusic() {
        return musicMapper.selectList(new LambdaQueryWrapper<Music>()
                .orderByAsc(Music::getSortOrder)
                .orderByDesc(Music::getId));
    }

    /**
     * 根据 ID 获取音乐
     */
    public Music getMusicById(Long id) {
        return musicMapper.selectById(id);
    }

    /**
     * 创建音乐
     */
    public Music createMusic(Music music) {
        musicMapper.insert(music);
        return music;
    }

    /**
     * 更新音乐
     */
    public Music updateMusic(Long id, Music music) {
        Music existing = musicMapper.selectById(id);
        if (existing == null) {
            return null;
        }
        existing.setTitle(music.getTitle());
        existing.setArtist(music.getArtist());
        existing.setYoutubeId(music.getYoutubeId());
        existing.setCategory(music.getCategory());
        existing.setSortOrder(music.getSortOrder());
        existing.setCoverImage(music.getCoverImage());
        musicMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除音乐
     */
    public boolean deleteMusic(Long id) {
        return musicMapper.deleteById(id) > 0;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/service/MusicService.java
git commit -m "feat: add MusicService

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 创建 MusicController

**Files:**
- Create: `blog-backend/src/main/java/com/example/blog/controller/MusicController.java`

- [ ] **Step 1: 创建 MusicController.java**

```java
package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Music;
import com.example.blog.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/music")
@CrossOrigin
public class MusicController {

    @Autowired
    private MusicService musicService;

    /**
     * 获取所有音乐
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Music>>> getAllMusic() {
        List<Music> musicList = musicService.getAllMusic();
        return ResponseEntity.ok(ApiResponse.success(musicList));
    }

    /**
     * 获取单个音乐
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Music>> getMusicById(@PathVariable Long id) {
        Music music = musicService.getMusicById(id);
        if (music == null) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(music));
    }

    /**
     * 创建音乐（Admin）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Music>> createMusic(@RequestBody Music music) {
        Music created = musicService.createMusic(music);
        return ResponseEntity.ok(ApiResponse.success("Music created successfully", created));
    }

    /**
     * 更新音乐（Admin）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Music>> updateMusic(@PathVariable Long id, @RequestBody Music music) {
        Music updated = musicService.updateMusic(id, music);
        if (updated == null) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Music updated successfully", updated));
    }

    /**
     * 删除音乐（Admin）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMusic(@PathVariable Long id) {
        boolean deleted = musicService.deleteMusic(id);
        if (!deleted) {
            return ResponseEntity.ok(ApiResponse.error("Music not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Music deleted successfully", null));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/controller/MusicController.java
git commit -m "feat: add MusicController

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 添加数据库表结构

**Files:**
- Modify: `blog-backend/src/main/resources/schema.sql`（如果存在）或提供 SQL 迁移脚本

- [ ] **Step 1: 提供 SQL 迁移语句**

```sql
CREATE TABLE IF NOT EXISTS music (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    youtube_id VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    sort_order INT DEFAULT 0,
    cover_image VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/resources/
git commit -m "feat: add music table schema

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 前端 API 方法

**Files:**
- Modify: `social-factory-blog/src/lib/api.js`

- [ ] **Step 1: 添加 music 相关方法**

在 `api.js` 的 `ApiClient` 类中添加以下方法（放在其他方法附近，保持格式一致）：

```javascript
// Music methods
async getMusicList() {
  return this.request('/music');
}

async getMusicById(id) {
  return this.request(`/music/${id}`);
}

async createMusic(musicData) {
  return this.request('/music', {
    method: 'POST',
    body: JSON.stringify(musicData),
  });
}

async updateMusic(id, musicData) {
  return this.request(`/music/${id}`, {
    method: 'PUT',
    body: JSON.stringify(musicData),
  });
}

async deleteMusic(id) {
  return this.request(`/music/${id}`, {
    method: 'DELETE',
  });
}
```

- [ ] **Step 2: Commit**

```bash
cd social-factory-blog && git add src/lib/api.js && git commit -m "feat: add music API methods to apiClient

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 后台管理页面

**Files:**
- Create: `social-factory-blog/src/pages/admin/music/index.astro`

- [ ] **Step 1: 创建 admin/music/index.astro**

参考 `admin/categories/index.astro` 的模式，创建音乐管理页面，包含：
- 页面标题和"添加音乐"按钮
- 搜索框（按歌曲名/艺术家搜索）
- 表格展示：标题、艺术家、分类、排序、封面预览、操作（编辑/删除）
- 添加/编辑弹窗，字段：标题、艺术家、YouTube ID、分类、排序、封面图 URL
- 删除确认逻辑

页面应使用 AdminLayout，保持与现有 admin 页面风格一致。

- [ ] **Step 2: Commit**

```bash
cd social-factory-blog && git add src/pages/admin/music/index.astro && git commit -m "feat: add admin music management page

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 公开音乐页面改造

**Files:**
- Modify: `social-factory-blog/src/pages/music.astro`

- [ ] **Step 1: 改造 music.astro**

改造要点：
1. 页面加载时通过 `apiClient.getMusicList()` 获取音乐列表
2. 按 `sortOrder` 展示双列列表（移除硬编码的歌曲列表）
3. 点击音乐项跳转到 `/music?play={youtubeId}` 并内嵌播放
4. 页面顶部 iframe 播放器根据 URL 参数 `play` 显示对应视频
5. 维持现有视觉风格

- [ ] **Step 2: Commit**

```bash
cd social-factory-blog && git add src/pages/music.astro && git commit -m "feat: refactor music page to use dynamic data

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 10: 最终验证

- [ ] **Step 1: 验证后端编译**

```bash
cd blog-backend && mvn compile -DskipTests
```

- [ ] **Step 2: 验证前端编译**

```bash
cd social-factory-blog && npm run build
```

- [ ] **Step 3: 提交所有更改**

```bash
git add -A && git commit -m "feat: complete music management feature

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

**Spec Coverage Check:**
- [x] Music 实体 — Task 1
- [x] MusicMapper — Task 2, 3
- [x] MusicService — Task 4
- [x] MusicController CRUD — Task 5
- [x] 数据库表结构 — Task 6
- [x] 前端 API — Task 7
- [x] 后台管理页面 `/admin/music` — Task 8
- [x] 公开页面展示和播放 — Task 9

**Plan complete and saved to `docs/superpowers/plans/2026-04-16-music-management-plan.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**
