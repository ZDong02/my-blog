-- =====================================================
-- Social Factory Blog — 唯一数据库结构定义文件
-- =====================================================
-- 本文件是项目内唯一的数据库 schema 来源（Single Source of Truth）。
-- 所有环境（本地开发 / Docker / 生产服务器）均执行本文件。
--
-- 幂等性：全部使用 CREATE TABLE IF NOT EXISTS，可重复安全执行。
--
-- 执行方式：
--   本地 / 生产： mysql -u root -p blog_db < blog-backend/src/main/resources/db/schema.sql
--   Docker：     由 deploy/docker-compose.yml 挂载到 /docker-entrypoint-initdb.d/ 自动执行
--                （仅在 mysql_data 数据卷首次初始化时生效）
--
-- 种子数据（默认管理员、分类、标签、示例文章）不在本文件内，
-- 由后端启动时的 DataInitializer 以幂等方式写入，无需手工导入。
-- =====================================================

-- -------------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密）',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `bio` TEXT DEFAULT NULL COMMENT '个人简介',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户，ADMIN-管理员',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `is_first_user` TINYINT DEFAULT 0 COMMENT '是否为首个注册用户',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 文章表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `posts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
    `content` LONGTEXT NOT NULL COMMENT '文章内容',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，DELETED-已删除',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `like_count` INT DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT DEFAULT 0 COMMENT '评论次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
    `featured_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
    PRIMARY KEY (`id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- -------------------------------------------------------------
-- 3. 分类表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` TEXT DEFAULT NULL COMMENT '分类描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- -------------------------------------------------------------
-- 4. 标签表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `slug` VARCHAR(120) NOT NULL COMMENT '标签别名',
    `description` TEXT DEFAULT NULL COMMENT '标签描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- -------------------------------------------------------------
-- 5. 文章标签关联表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `post_tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_tags_post_id_tag_id` (`post_id`, `tag_id`),
    INDEX `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关联表';

-- -------------------------------------------------------------
-- 6. 点赞记录表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `like_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '文章ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_post_id` (`post_id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- -------------------------------------------------------------
-- 7. 收藏记录表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `bookmarks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `post_id` BIGINT NOT NULL COMMENT '文章ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_post_id` (`post_id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏记录表';

-- -------------------------------------------------------------
-- 8. 评论表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '文章ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（用于回复）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- -------------------------------------------------------------
-- 9. 审计日志表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '操作用户名',
    `action` VARCHAR(100) NOT NULL COMMENT '操作类型',
    `target_type` VARCHAR(50) DEFAULT NULL COMMENT '目标类型',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标ID',
    `details` TEXT DEFAULT NULL COMMENT '详细信息',
    `ip_address` VARCHAR(100) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_target_type` (`target_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- =====================================================
-- 结构校验：确认 9 张表全部创建成功
-- =====================================================
SELECT 'users' AS table_name, COUNT(*) AS created FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'users'
UNION ALL SELECT 'posts',      COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'posts'
UNION ALL SELECT 'categories', COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'categories'
UNION ALL SELECT 'tags',       COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'tags'
UNION ALL SELECT 'post_tags',  COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'post_tags'
UNION ALL SELECT 'like_records', COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'like_records'
UNION ALL SELECT 'bookmarks',  COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'bookmarks'
UNION ALL SELECT 'comments',   COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'comments'
UNION ALL SELECT 'audit_log',  COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'audit_log';
