# 用户管理功能增强实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为用户管理界面添加三个功能：用户详情查看/编辑、批量操作、用户统计/报表

**Architecture:**
- 后端：扩展 UserManagementController 添加批量操作和统计接口，扩展 UserMapper 添加统计SQL，UserService 添加统计服务方法
- 前端：在现有用户管理页面添加抽屉组件展示详情，表格添加复选框支持批量操作，顶部添加统计卡片和图表

**Tech Stack:** Spring Boot + MyBatis-Plus (后端), Astro + Tailwind + Chart.js (前端)

---

## 文件清单

### 后端
| 文件 | 操作 | 说明 |
|------|------|------|
| `User.java` | 修改 | 添加 lastLoginAt 字段 |
| `UserMapper.java` | 修改 | 添加 batchUpdateStatus, batchDeleteUsers, getUserStats 等方法 |
| `UserMapper.xml` | 修改 | 添加批量操作和统计SQL |
| `UserService.java` | 修改 | 添加批量操作和统计服务方法 |
| `UserManagementController.java` | 修改 | 添加批量操作和统计API端点 |
| `UserStatsResponse.java` | 创建 | 统计响应DTO |

### 前端
| 文件 | 操作 | 说明 |
|------|------|------|
| `api.js` | 修改 | 添加 batchUpdateUserStatus, batchDeleteUsers, getUserStats |
| `index.astro` | 修改 | 添加复选框、批量操作工具栏、统计卡片、详情抽屉 |

---

## Task 1: 后端 - 添加 lastLoginAt 字段到 User 实体

**Files:**
- Modify: `blog-backend/src/main/java/com/example/blog/entity/User.java`

- [ ] **Step 1: 添加 lastLoginAt 字段**

在 `User.java` 的 `updatedAt` 字段后添加:

```java
/**
 * 最后登录时间
 */
@TableField("last_login_at")
private LocalDateTime lastLoginAt;
```

- [ ] **Step 2: 更新 toString 方法**

在 toString 方法中添加 `lastLoginAt` 字段。

- [ ] **Step 3: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/entity/User.java
git commit -m "feat: 添加 User.lastLoginAt 字段

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 2: 后端 - 扩展 UserMapper 接口

**Files:**
- Modify: `blog-backend/src/main/java/com/example/blog/mapper/UserMapper.java`

- [ ] **Step 1: 添加新的 Mapper 方法声明**

在 `UserMapper.java` 中添加以下方法声明:

```java
// 批量更新用户状态
int batchUpdateStatus(@Param("userIds") List<Long> userIds, @Param("status") Integer status);

// 批量删除用户
int batchDeleteUsers(@Param("userIds") List<Long> userIds);

// 统计总用户数
int countTotalUsers();

// 统计今日新增用户数
int countTodayNewUsers();

// 统计本月新增用户数
int countMonthNewUsers();

// 统计活跃用户数（近30天有登录）
int countActiveUsers();

// 统计角色分布
Map<String, Integer> countRoleDistribution();

// 获取用户增长趋势（近30天每日新增）
List<Map<String, Object>> getGrowthTrend(@Param("days") int days);

// 按ID列表查询用户
List<User> selectUsersByIds(@Param("userIds") List<Long> userIds);
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/mapper/UserMapper.java
git commit -m "feat: 扩展 UserMapper 添加批量操作和统计方法

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 3: 后端 - 扩展 UserMapper.xml SQL

**Files:**
- Modify: `blog-backend/src/main/resources/mapper/UserMapper.xml`

- [ ] **Step 1: 在 `</mapper>` 前添加新的 SQL**

```xml
<!-- 批量更新用户状态 -->
<update id="batchUpdateStatus">
    UPDATE users SET status = #{status}, updated_at = NOW()
    WHERE id IN
    <foreach collection="userIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</update>

<!-- 批量删除用户 -->
<delete id="batchDeleteUsers">
    DELETE FROM users WHERE id IN
    <foreach collection="userIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</delete>

<!-- 统计今日新增用户数 -->
<select id="countTodayNewUsers" resultType="int">
    SELECT COUNT(*) FROM users
    WHERE DATE(created_at) = CURDATE()
</select>

<!-- 统计本月新增用户数 -->
<select id="countMonthNewUsers" resultType="int">
    SELECT COUNT(*) FROM users
    WHERE YEAR(created_at) = YEAR(CURDATE())
    AND MONTH(created_at) = MONTH(CURDATE())
</select>

<!-- 统计活跃用户数（近30天有更新 updated_at） -->
<select id="countActiveUsers" resultType="int">
    SELECT COUNT(*) FROM users
    WHERE updated_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
</select>

<!-- 统计角色分布 -->
<select id="countRoleDistribution" resultType="java.util.HashMap">
    SELECT role as roleName, COUNT(*) as count
    FROM users
    GROUP BY role
</select>

<!-- 获取用户增长趋势 -->
<select id="getGrowthTrend" resultType="java.util.HashMap">
    SELECT DATE(created_at) as date, COUNT(*) as count
    FROM users
    WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY)
    GROUP BY DATE(created_at)
    ORDER BY date ASC
</select>

<!-- 按ID列表查询用户 -->
<select id="selectUsersByIds" resultMap="UserResultMap">
    SELECT u.*,
           (SELECT COUNT(*) FROM posts p WHERE p.author_id = u.id AND p.status != 'DELETED') as post_count,
           (SELECT COUNT(*) FROM comments c WHERE c.user_id = u.id) as comment_count
    FROM users u
    WHERE u.id IN
    <foreach collection="userIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/resources/mapper/UserMapper.xml
git commit -m "feat: 扩展 UserMapper.xml 添加批量操作和统计SQL

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 4: 后端 - 创建统计响应 DTO

**Files:**
- Create: `blog-backend/src/main/java/com/example/blog/dto/response/UserStatsResponse.java`

- [ ] **Step 1: 创建 UserStatsResponse DTO**

```java
package com.example.blog.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserStatsResponse {
    private Integer totalUsers;
    private Integer todayNewUsers;
    private Integer monthNewUsers;
    private Integer activeUsers;
    private Map<String, Integer> roleDistribution;
    private List<GrowthData> growthTrend;

    @Data
    public static class GrowthData {
        private String date;
        private Integer count;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/dto/response/UserStatsResponse.java
git commit -m "feat: 创建 UserStatsResponse DTO

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 5: 后端 - 扩展 UserService

**Files:**
- Modify: `blog-backend/src/main/java/com/example/blog/service/UserService.java`

- [ ] **Step 1: 添加新的服务方法**

在 `UserService.java` 中添加:

```java
public void batchUpdateUserStatus(List<Long> userIds, Integer status) {
    if (userIds == null || userIds.isEmpty()) {
        throw new BusinessException("用户ID列表不能为空");
    }
    userMapper.batchUpdateStatus(userIds, status);
}

public void batchDeleteUsers(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        throw new BusinessException("用户ID列表不能为空");
    }
    userMapper.batchDeleteUsers(userIds);
}

public UserStatsResponse getUserStats() {
    UserStatsResponse stats = new UserStatsResponse();
    stats.setTotalUsers(userMapper.countTotalUsers());
    stats.setTodayNewUsers(userMapper.countTodayNewUsers());
    stats.setMonthNewUsers(userMapper.countMonthNewUsers());
    stats.setActiveUsers(userMapper.countActiveUsers());
    stats.setRoleDistribution(userMapper.countRoleDistribution());

    List<Map<String, Object>> rawTrend = userMapper.getGrowthTrend(30);
    List<UserStatsResponse.GrowthData> growthTrend = rawTrend.stream()
        .map(m -> {
            UserStatsResponse.GrowthData data = new UserStatsResponse.GrowthData();
            data.setDate(m.get("date").toString());
            data.setCount(((Number) m.get("count")).intValue());
            return data;
        })
        .collect(Collectors.toList());
    stats.setGrowthTrend(growthTrend);

    return stats;
}

public List<User> getUsersByIds(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        return Collections.emptyList();
    }
    return userMapper.selectUsersByIds(userIds);
}
```

添加必要的 import:
```java
import java.util.Collections;
import java.util.stream.Collectors;
```

- [ ] **Step 2: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/service/UserService.java
git commit -m "feat: 扩展 UserService 添加批量操作和统计方法

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 6: 后端 - 扩展 UserManagementController

**Files:**
- Modify: `blog-backend/src/main/java/com/example/blog/controller/UserManagementController.java`

- [ ] **Step 1: 在 `@RestController` 类中添加新的端点**

在 `deleteUser` 方法后添加:

```java
@PutMapping("/batch/status")
public ResponseEntity<ApiResponse<Void>> batchUpdateStatus(
        @RequestBody BatchStatusUpdateRequest request,
        @AuthenticationPrincipal JwtUserDetails currentAdmin) {

    if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
        throw new BusinessException("用户ID列表不能为空");
    }

    // Prevent self status change through batch
    request.getUserIds().removeIf(id -> currentAdmin.getId().equals(id));

    if (!request.getUserIds().isEmpty()) {
        userService.batchUpdateUserStatus(request.getUserIds(), request.getStatus());
    }

    return ResponseEntity.ok(ApiResponse.success("批量更新状态成功", null));
}

@DeleteMapping("/batch")
public ResponseEntity<ApiResponse<Void>> batchDeleteUsers(
        @RequestBody BatchDeleteRequest request,
        @AuthenticationPrincipal JwtUserDetails currentAdmin) {

    if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
        throw new BusinessException("用户ID列表不能为空");
    }

    // Prevent self deletion through batch
    request.getUserIds().removeIf(id -> currentAdmin.getId().equals(id));

    if (!request.getUserIds().isEmpty()) {
        userService.batchDeleteUsers(request.getUserIds());
    }

    return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
}

@GetMapping("/stats")
public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
    UserStatsResponse stats = userService.getUserStats();
    return ResponseEntity.ok(ApiResponse.success(stats));
}
```

- [ ] **Step 2: 创建请求 DTO**

创建 `BatchStatusUpdateRequest.java`:
```java
package com.example.blog.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchStatusUpdateRequest {
    private List<Long> userIds;
    private Integer status;
}
```

创建 `BatchDeleteRequest.java`:
```java
package com.example.blog.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    private List<Long> userIds;
}
```

- [ ] **Step 3: 添加必要的 import**

```java
import com.example.blog.dto.request.BatchDeleteRequest;
import com.example.blog.dto.request.BatchStatusUpdateRequest;
import com.example.blog.dto.response.UserStatsResponse;
import java.util.List;
```

- [ ] **Step 4: Commit**

```bash
git add blog-backend/src/main/java/com/example/blog/controller/UserManagementController.java
git add blog-backend/src/main/java/com/example/blog/dto/request/BatchStatusUpdateRequest.java
git add blog-backend/src/main/java/com/example/blog/dto/request/BatchDeleteRequest.java
git commit -m "feat: UserManagementController 添加批量操作和统计接口

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 7: 前端 - 扩展 api.js

**Files:**
- Modify: `social-factory-blog/src/lib/api.js`

- [ ] **Step 1: 在 `User management methods (Admin)` 区域后添加新方法**

在 `deleteUser` 方法后添加:

```javascript
// Batch operations (Admin)
async batchUpdateUserStatus(userIds, status) {
  return this.request('/admin/users/batch/status', {
    method: 'PUT',
    body: JSON.stringify({ userIds, status }),
  });
}

async batchDeleteUsers(userIds) {
  return this.request('/admin/users/batch', {
    method: 'DELETE',
    body: JSON.stringify({ userIds }),
  });
}

async getUserStats() {
  return this.request('/admin/users/stats');
}
```

- [ ] **Step 2: Commit**

```bash
git add social-factory-blog/src/lib/api.js
git commit -m "feat: api.js 添加批量操作和统计API方法

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 8: 前端 - 用户管理页面增强

**Files:**
- Modify: `social-factory-blog/src/pages/admin/users/index.astro`

- [ ] **Step 1: 在 `<script>` 标签顶部添加 Chart.js CDN 引入**

```html
<script>
  // Chart.js CDN
  if (!window.Chart) {
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js';
    document.head.appendChild(script);
  }
</script>
```

- [ ] **Step 2: 在 `AdminLayout` 前添加统计卡片 HTML**

在 `<AdminLayout ...>` 前添加:

```html
<!-- Stats Cards -->
<div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8">
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
    <div class="card p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-neutral-500 uppercase tracking-wider">总用户数</p>
          <p id="statTotalUsers" class="text-3xl font-bold text-neutral-900 dark:text-white mt-2">-</p>
        </div>
        <div class="h-12 w-12 rounded-full bg-blue-500/10 flex items-center justify-center">
          <svg class="h-6 w-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/>
          </svg>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-neutral-500 uppercase tracking-wider">今日新增</p>
          <p id="statTodayNew" class="text-3xl font-bold text-neutral-900 dark:text-white mt-2">-</p>
        </div>
        <div class="h-12 w-12 rounded-full bg-green-500/10 flex items-center justify-center">
          <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
          </svg>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-neutral-500 uppercase tracking-wider">本月新增</p>
          <p id="statMonthNew" class="text-3xl font-bold text-neutral-900 dark:text-white mt-2">-</p>
        </div>
        <div class="h-12 w-12 rounded-full bg-purple-500/10 flex items-center justify-center">
          <svg class="h-6 w-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
          </svg>
        </div>
      </div>
    </div>

    <div class="card p-6">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-neutral-500 uppercase tracking-wider">活跃用户</p>
          <p id="statActiveUsers" class="text-3xl font-bold text-neutral-900 dark:text-white mt-2">-</p>
        </div>
        <div class="h-12 w-12 rounded-full bg-orange-500/10 flex items-center justify-center">
          <svg class="h-6 w-6 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/>
          </svg>
        </div>
      </div>
    </div>
  </div>

  <!-- Charts Row -->
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
    <div class="card p-6">
      <h3 class="text-lg font-bold text-neutral-900 dark:text-white mb-4">角色分布</h3>
      <div class="h-64 flex items-center justify-center">
        <canvas id="roleChart"></canvas>
      </div>
    </div>
    <div class="card p-6">
      <h3 class="text-lg font-bold text-neutral-900 dark:text-white mb-4">用户增长趋势（近30天）</h3>
      <div class="h-64">
        <canvas id="growthChart"></canvas>
      </div>
    </div>
  </div>
</div>
```

- [ ] **Step 3: 修改表格，添加复选框列**

将 `<table id="usersTable">` 的 `<thead>` 修改为:

```html
<thead class="bg-neutral-50 dark:bg-neutral-800">
  <tr>
    <th class="px-6 py-4 text-left">
      <input type="checkbox" id="selectAllCheckbox" onchange="toggleSelectAll()" class="w-4 h-4 rounded border-neutral-300 dark:border-neutral-600 text-brand-red focus:ring-brand-red" />
    </th>
    <th class="px-6 py-4 text-left text-xs font-bold text-neutral-500 uppercase tracking-wider">用户</th>
    <th class="px-6 py-4 text-left text-xs font-bold text-neutral-500 uppercase tracking-wider">角色</th>
    <th class="px-6 py-4 text-left text-xs font-bold text-neutral-500 uppercase tracking-wider">状态</th>
    <th class="px-6 py-4 text-left text-xs font-bold text-neutral-500 uppercase tracking-wider">文章数</th>
    <th class="px-6 py-4 text-left text-xs font-bold text-neutral-500 uppercase tracking-wider">注册时间</th>
    <th class="px-6 py-4 text-right text-xs font-bold text-neutral-500 uppercase tracking-wider">操作</th>
  </tr>
</thead>
```

- [ ] **Step 4: 在表格上方添加批量操作工具栏**

在 `<div id="noUsers" class="hidden...` 前添加:

```html
<!-- Batch Actions Toolbar -->
<div id="batchToolbar" class="hidden px-6 py-4 bg-purple-50 dark:bg-purple-900/20 border-b border-neutral-200 dark:border-neutral-700 flex items-center justify-between">
  <div class="flex items-center gap-3">
    <span class="text-sm font-bold text-purple-600 dark:text-purple-400">已选择 <span id="selectedCount">0</span> 项</span>
  </div>
  <div class="flex items-center gap-3">
    <button onclick="batchEnable()" class="btn-secondary px-4 py-2 text-sm">批量启用</button>
    <button onclick="batchDisable()" class="btn-secondary px-4 py-2 text-sm">批量禁用</button>
    <button onclick="batchDelete()" class="btn-danger px-4 py-2 text-sm">批量删除</button>
  </div>
</div>
```

- [ ] **Step 5: 在操作列添加详情按钮**

在操作列的 `deleteUser` 按钮前添加:

```html
<button onclick="openDetailModal(${user.id})" class="text-blue-600 hover:text-blue-900 dark:text-blue-400 dark:hover:text-blue-300 mr-3 font-bold">详情</button>
```

- [ ] **Step 6: 在最后一个 Modal 后添加详情抽屉**

在 `</AdminLayout>` 前添加:

```html
<!-- User Detail Drawer -->
<div id="detailDrawer" class="hidden fixed inset-0 z-50">
  <div class="absolute inset-0 bg-black/50" onclick="closeDetailDrawer()"></div>
  <div id="detailDrawerContent" class="absolute right-0 top-0 h-full w-full max-w-md bg-white dark:bg-neutral-900 shadow-xl transform transition-transform duration-300 translate-x-full">
    <div class="flex items-center justify-between px-6 py-5 border-b border-neutral-200 dark:border-neutral-700">
      <h3 class="text-lg font-bold text-neutral-900 dark:text-white">用户详情</h3>
      <button onclick="closeDetailDrawer()" class="text-neutral-400 hover:text-neutral-600 dark:hover:text-neutral-300">
        <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>
    <div id="detailDrawerBody" class="p-6 overflow-y-auto h-[calc(100%-64px)]">
      <!-- Content loaded dynamically -->
    </div>
  </div>
</div>
```

- [ ] **Step 7: 修改 script 部分**

在 `let allUsers = [];` 后添加:
```javascript
let selectedUserIds = new Set();
let roleChart = null;
let growthChart = null;
```

在 `loadUsers` 函数成功回调中添加统计加载:
```javascript
if (response.success && response.data) {
  allUsers = response.data;
  renderUsers(allUsers);
  loadUserStats();
}
```

在 `checkAdminAccess` 函数中添加 `loadUserStats` 调用:
```javascript
currentAdminId = user.id;
await loadUsers();
loadUserStats();
```

添加新的函数:

```javascript
const loadUserStats = async () => {
  try {
    const response = await apiClient.getUserStats();
    if (response.success && response.data) {
      const stats = response.data;
      document.getElementById('statTotalUsers').textContent = stats.totalUsers || 0;
      document.getElementById('statTodayNew').textContent = stats.todayNewUsers || 0;
      document.getElementById('statMonthNew').textContent = stats.monthNewUsers || 0;
      document.getElementById('statActiveUsers').textContent = stats.activeUsers || 0;

      renderRoleChart(stats.roleDistribution || {});
      renderGrowthChart(stats.growthTrend || []);
    }
  } catch (error) {
    console.error('Failed to load user stats:', error);
  }
};

const renderRoleChart = (distribution) => {
  const ctx = document.getElementById('roleChart')?.getContext('2d');
  if (!ctx) return;

  if (roleChart) roleChart.destroy();

  const labels = Object.keys(distribution).map(k => k === 'ADMIN' ? '管理员' : '普通用户');
  const data = Object.values(distribution);

  roleChart = new Chart(ctx, {
    type: 'pie',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: ['#8b5cf6', '#3b82f6'],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: document.querySelector('html.dark') ? '#fff' : '#374151' }
        }
      }
    }
  });
};

const renderGrowthChart = (trend) => {
  const ctx = document.getElementById('growthChart')?.getContext('2d');
  if (!ctx) return;

  if (growthChart) growthChart.destroy();

  const labels = trend.map(t => t.date);
  const data = trend.map(t => t.count);

  growthChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: '新增用户',
        data,
        borderColor: '#8b5cf6',
        backgroundColor: 'rgba(139, 92, 246, 0.1)',
        fill: true,
        tension: 0.3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true }
      },
      plugins: {
        legend: {
          labels: { color: document.querySelector('html.dark') ? '#fff' : '#374151' }
        }
      }
    }
  });
};

const toggleSelectAll = () => {
  const checkbox = document.getElementById('selectAllCheckbox');
  const checkboxes = document.querySelectorAll('.user-checkbox');

  if (checkbox.checked) {
    checkboxes.forEach(cb => { cb.checked = true; selectedUserIds.add(parseInt(cb.dataset.userId)); });
  } else {
    checkboxes.forEach(cb => { cb.checked = false; });
    selectedUserIds.clear();
  }

  updateBatchToolbar();
};

const toggleUserSelect = (userId) => {
  if (selectedUserIds.has(userId)) {
    selectedUserIds.delete(userId);
  } else {
    selectedUserIds.add(userId);
  }

  updateBatchToolbar();
};

const updateBatchToolbar = () => {
  const toolbar = document.getElementById('batchToolbar');
  const countEl = document.getElementById('selectedCount');

  if (selectedUserIds.size > 0) {
    toolbar.classList.remove('hidden');
    countEl.textContent = selectedUserIds.size;
  } else {
    toolbar.classList.add('hidden');
  }
};

const batchEnable = async () => {
  if (selectedUserIds.size === 0) return;

  try {
    const response = await apiClient.batchUpdateUserStatus(Array.from(selectedUserIds), 1);
    if (response.success) {
      if (window.showToast) window.showToast('批量启用成功', 'success');
      selectedUserIds.clear();
      updateBatchToolbar();
      loadUsers();
    }
  } catch (error) {
    console.error('Batch enable failed:', error);
    if (window.showToast) window.showToast('批量启用失败', 'error');
  }
};

const batchDisable = async () => {
  if (selectedUserIds.size === 0) return;

  try {
    const response = await apiClient.batchUpdateUserStatus(Array.from(selectedUserIds), 0);
    if (response.success) {
      if (window.showToast) window.showToast('批量禁用成功', 'success');
      selectedUserIds.clear();
      updateBatchToolbar();
      loadUsers();
    }
  } catch (error) {
    console.error('Batch disable failed:', error);
    if (window.showToast) window.showToast('批量禁用失败', 'error');
  }
};

const batchDelete = async () => {
  if (selectedUserIds.size === 0) return;

  const selectedNames = allUsers
    .filter(u => selectedUserIds.has(u.id))
    .map(u => u.nickname || u.username)
    .join(', ');

  if (!confirm(`确定要删除以下用户吗？此操作不可恢复。\n${selectedNames}`)) {
    return;
  }

  try {
    const response = await apiClient.batchDeleteUsers(Array.from(selectedUserIds));
    if (response.success) {
      if (window.showToast) window.showToast('批量删除成功', 'success');
      selectedUserIds.clear();
      updateBatchToolbar();
      loadUsers();
    }
  } catch (error) {
    console.error('Batch delete failed:', error);
    if (window.showToast) window.showToast('批量删除失败', 'error');
  }
};

const openDetailModal = async (userId) => {
  const user = allUsers.find(u => u.id === userId);
  if (!user) return;

  const body = document.getElementById('detailDrawerBody');
  const roleClass = user.role === 'ADMIN'
    ? 'bg-purple-500/10 text-purple-600 border-purple-500/20'
    : 'bg-blue-500/10 text-blue-600 border-blue-500/20';
  const roleText = user.role === 'ADMIN' ? '管理员' : '普通用户';
  const statusClass = user.status === 1
    ? 'bg-green-500/10 text-green-600 border-green-500/20'
    : 'bg-red-500/10 text-red-600 border-red-500/20';
  const statusText = user.status === 1 ? '正常' : '禁用';

  body.innerHTML = `
    <div class="flex items-center gap-4 mb-6">
      <div class="h-16 w-16 rounded-full overflow-hidden bg-neutral-200 dark:bg-neutral-700">
        ${user.avatar
          ? `<img src="${user.avatar}" alt="${user.nickname || user.username}" class="w-full h-full object-cover" />`
          : `<div class="w-full h-full flex items-center justify-center text-2xl text-neutral-500 font-bold">${(user.nickname || user.username).charAt(0).toUpperCase()}</div>`
        }
      </div>
      <div>
        <div class="text-xl font-bold text-neutral-900 dark:text-white">${user.nickname || user.username}</div>
        <div class="text-sm text-neutral-500">@${user.username}</div>
      </div>
    </div>

    <div class="space-y-4">
      <div class="flex justify-between items-center py-3 border-b border-neutral-200 dark:border-neutral-700">
        <span class="text-sm text-neutral-500">邮箱</span>
        <span class="text-sm font-medium text-neutral-900 dark:text-white">${user.email}</span>
      </div>
      <div class="flex justify-between items-center py-3 border-b border-neutral-200 dark:border-neutral-700">
        <span class="text-sm text-neutral-500">角色</span>
        <span class="px-3 py-1 text-xs font-bold border ${roleClass}">${roleText}</span>
      </div>
      <div class="flex justify-between items-center py-3 border-b border-neutral-200 dark:border-neutral-700">
        <span class="text-sm text-neutral-500">状态</span>
        <span class="px-3 py-1 text-xs font-bold border ${statusClass}">${statusText}</span>
      </div>
      <div class="flex justify-between items-center py-3 border-b border-neutral-200 dark:border-neutral-700">
        <span class="text-sm text-neutral-500">文章数</span>
        <span class="text-sm font-medium text-neutral-900 dark:text-white">${user.postCount || 0}</span>
      </div>
      <div class="flex justify-between items-center py-3 border-b border-neutral-200 dark:border-neutral-700">
        <span class="text-sm text-neutral-500">注册时间</span>
        <span class="text-sm font-medium text-neutral-900 dark:text-white">${new Date(user.createdAt).toLocaleDateString('zh-CN')}</span>
      </div>
    </div>
  `;

  document.getElementById('detailDrawer').classList.remove('hidden');
  setTimeout(() => {
    document.getElementById('detailDrawerContent').classList.remove('translate-x-full');
  }, 10);
};

const closeDetailDrawer = () => {
  document.getElementById('detailDrawerContent').classList.add('translate-x-full');
  setTimeout(() => {
    document.getElementById('detailDrawer').classList.add('hidden');
  }, 300);
};
```

- [ ] **Step 8: 修改 renderUsers 函数中的表格行，添加复选框**

将 `renderUsers` 中的 `<tr>` 行修改为:

```html
<tr class="hover:bg-neutral-50 dark:hover:bg-neutral-800 transition">
  <td class="px-6 py-4 whitespace-nowrap">
    <input type="checkbox"
           class="user-checkbox w-4 h-4 rounded border-neutral-300 dark:border-neutral-600 text-brand-red focus:ring-brand-red"
           data-user-id="${user.id}"
           onchange="toggleUserSelect(${user.id})"
           ${isCurrentAdmin ? 'disabled' : ''} />
  </td>
  ...
```

- [ ] **Step 9: Commit**

```bash
git add social-factory-blog/src/pages/admin/users/index.astro
git commit -m "feat: 用户管理页面添加详情、批量操作和统计图表

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
```

---

## Task 9: 验证和测试

- [ ] **Step 1: 启动后端服务**

验证后端接口正常:
```bash
# 启动 Spring Boot
cd blog-backend && mvn spring-boot:run
```

- [ ] **Step 2: 测试 API 端点**

使用 curl 或 Postman 测试:
```bash
# 获取用户统计
curl http://localhost:8080/api/admin/users/stats -H "Authorization: Bearer <token>"

# 批量更新状态
curl -X PUT http://localhost:8080/api/admin/users/batch/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userIds":[1,2,3],"status":1}'
```

- [ ] **Step 3: 启动前端并测试**

```bash
cd social-factory-blog && npm run dev
```

访问 http://localhost:4321/admin/users 验证:
- [ ] 统计卡片显示正确
- [ ] 图表渲染正常
- [ ] 用户详情抽屉正常打开
- [ ] 批量选择功能正常
- [ ] 批量操作（启用/禁用/删除）正常

- [ ] **Step 4: Commit 最终版本**

---

## 实施检查清单

| 功能 | 状态 |
|------|------|
| 后端 - lastLoginAt 字段 | ☐ |
| 后端 - UserMapper 批量方法 | ☐ |
| 后端 - UserMapper XML SQL | ☐ |
| 后端 - UserStatsResponse DTO | ☐ |
| 后端 - UserService 统计方法 | ☐ |
| 后端 - Controller 批量接口 | ☐ |
| 前端 - api.js 方法 | ☐ |
| 前端 - 统计卡片 | ☐ |
| 前端 - Chart.js 图表 | ☐ |
| 前端 - 批量操作工具栏 | ☐ |
| 前端 - 详情抽屉 | ☐ |
| 集成测试 | ☐ |
