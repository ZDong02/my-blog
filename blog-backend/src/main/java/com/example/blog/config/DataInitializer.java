package com.example.blog.config;

import com.example.blog.constant.PostConstants;
import com.example.blog.constant.UserConstants;
import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.PostTag;
import com.example.blog.entity.Tag;
import com.example.blog.entity.User;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.PostMapper;
import com.example.blog.mapper.PostTagMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application Runner to create default admin user and first blog post
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        updateUsersAvatar();
        createAdminUser();
        boolean tagTablesReady = ensureTagTables();
        createSeedContent(tagTablesReady);
    }

    private void updateUsersAvatar() {
        // Update all users with null or empty avatar to default avatar
        userMapper.findAllUsersWithNullAvatar().forEach(user -> {
            user.setAvatar("/images/default-avatar.svg");
            userMapper.updateById(user);
            System.out.println("Updated avatar for user: " + user.getUsername());
        });
    }

    private void createAdminUser() {
        // Check if admin user exists
        User adminUser = userMapper.findByUsername("admin");
        if (adminUser == null) {
            User user = new User();
            user.setUsername("admin");
            user.setEmail("admin@example.com");
            user.setPassword(passwordEncoder.encode("admin123"));
            user.setNickname("Admin");
            user.setAvatar("/images/default-avatar.svg");
            user.setRole(UserConstants.ROLE_ADMIN);
            user.setIsFirstUser(1);
            userMapper.insert(user);
            System.out.println("Admin user created: admin/admin123");
        } else {
            // Upgrade existing admin user to ADMIN role if not already
            if (!UserConstants.ROLE_ADMIN.equals(adminUser.getRole())) {
                adminUser.setRole(UserConstants.ROLE_ADMIN);
                userMapper.updateById(adminUser);
                System.out.println("Admin user upgraded to ADMIN role");
            }
            // Set default avatar if missing
            if (adminUser.getAvatar() == null || adminUser.getAvatar().isEmpty()) {
                adminUser.setAvatar("/images/default-avatar.svg");
                userMapper.updateById(adminUser);
                System.out.println("Admin user avatar updated to default");
            }
        }
    }

    private boolean ensureTagTables() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS tags (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      name VARCHAR(100) NOT NULL,
                      slug VARCHAR(120) NOT NULL,
                      description TEXT NULL,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_tags_name (name),
                      UNIQUE KEY uk_tags_slug (slug)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);

            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS post_tags (
                      id BIGINT NOT NULL AUTO_INCREMENT,
                      post_id BIGINT NOT NULL,
                      tag_id BIGINT NOT NULL,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      PRIMARY KEY (id),
                      UNIQUE KEY uk_post_tags_post_id_tag_id (post_id, tag_id),
                      KEY idx_post_tags_tag_id (tag_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                    """);

            return true;
        } catch (Exception ex) {
            System.err.println("Failed to ensure tag tables, tag seeding will be skipped: " + ex.getMessage());
            return false;
        }
    }

    private void createSeedContent(boolean tagTablesReady) {
        User adminUser = userMapper.findByUsername("admin");
        if (adminUser == null) {
            System.out.println("Admin user not found, skipping seed content creation");
            return;
        }

        Category technology = ensureCategory("Technology", "技术趋势、工程实践与效率工具");
        Category backend = ensureCategory("Backend", "后端架构、API 设计与数据治理");
        Category frontend = ensureCategory("Frontend", "前端体验、性能优化与工程化");
        Category life = ensureCategory("Life", "写作、复盘与个人成长");

        Map<String, Tag> tags = new HashMap<>();
        if (tagTablesReady) {
            tags.put("java", ensureTag("Java", "java", "Java 语言与生态实践"));
            tags.put("spring-boot", ensureTag("Spring Boot", "spring-boot", "Spring Boot 实战与最佳实践"));
            tags.put("frontend", ensureTag("Frontend", "frontend", "前端工程与交互体验"));
            tags.put("astro", ensureTag("Astro", "astro", "Astro 建站与内容系统"));
            tags.put("mysql", ensureTag("MySQL", "mysql", "MySQL 设计、查询与优化"));
            tags.put("tutorial", ensureTag("教程", "tutorial", "可落地的步骤化教程"));
            tags.put("sharing", ensureTag("分享", "sharing", "经验总结与实践复盘"));
            tags.put("life", ensureTag("生活", "life", "成长、习惯与写作"));
        }

        createPostIfMissing(
                adminUser,
                technology,
                "我的第一篇 blog",
                "欢迎来到 Social Factory，这是我开始系统写作的第一篇文章。\n\n" +
                        "## 为什么要开始写作\n\n" +
                        "过去我总是“做了很多事，却很少沉淀”。直到我把项目复盘写成公开文章，才发现写作能把零散经验变成可以复用的方法。\n\n" +
                        "## 这个博客会写什么\n\n" +
                        "- 后端开发中的真实问题与解法\n" +
                        "- 前端与内容系统的工程经验\n" +
                        "- 个人项目从想法到上线的全过程\n" +
                        "- 写作、协作与成长的复盘\n\n" +
                        "## 对读者的承诺\n\n" +
                        "每篇文章尽量提供“可以直接拿去用”的清单、模板和踩坑总结。希望你读完后，能立刻推动一个小改进。\n\n" +
                        "感谢你来到这里，欢迎留言交流。",
                "开始写作不是为了输出观点，而是为了持续沉淀可复用的方法。",
                35,
                tagTablesReady ? List.of(tags.get("sharing"), tags.get("tutorial")) : List.of()
        );

        createPostIfMissing(
                adminUser,
                backend,
                "Spring Boot + MyBatis Plus 实战清单",
                "这篇文章整理了我在 Spring Boot + MyBatis Plus 项目里最常用的一份上线清单。\n\n" +
                        "## 1. 接口层\n\n" +
                        "- 统一响应结构，避免前端适配成本扩大\n" +
                        "- 参数校验放在边界层，错误信息可定位\n" +
                        "- 高风险接口补充权限和审计日志\n\n" +
                        "## 2. 数据层\n\n" +
                        "- 慢 SQL 先看执行计划，再做索引调整\n" +
                        "- 分页查询与 count 查询分离优化\n" +
                        "- 统一软删除与时间字段填充策略\n\n" +
                        "## 3. 运行与可观测\n\n" +
                        "- 对核心链路加缓存，设置合理失效时间\n" +
                        "- 对异常做分层处理，减少 500 噪音\n" +
                        "- 关键业务指标可视化，便于排查回归\n\n" +
                        "你可以把这份清单直接用在自己的新项目初始化阶段。",
                "一份可直接复用的 Spring Boot + MyBatis Plus 项目上线清单。",
                24,
                tagTablesReady ? List.of(tags.get("java"), tags.get("spring-boot"), tags.get("mysql"), tags.get("tutorial")) : List.of()
        );

        createPostIfMissing(
                adminUser,
                frontend,
                "Astro 博客从 0 到 1：信息架构与组件拆分",
                "很多人做博客时先写页面，最后发现结构越做越乱。我更推荐先做信息架构，再做组件拆分。\n\n" +
                        "## 信息架构先行\n\n" +
                        "- 明确核心页面：首页、文章页、标签页、个人中心\n" +
                        "- 统一内容模型：标题、摘要、正文、标签、分类\n" +
                        "- 先画数据流，再写 UI\n\n" +
                        "## 组件拆分原则\n\n" +
                        "- 业务组件只做一件事\n" +
                        "- 公共布局层不耦合业务状态\n" +
                        "- 交互逻辑尽量收敛在独立模块\n\n" +
                        "## 性能与体验\n\n" +
                        "- 优先渲染首屏骨架，异步加载重内容\n" +
                        "- 图片兜底与空状态必须完整\n" +
                        "- 页面文本可读性优先于花哨动画\n\n" +
                        "做好这三步，博客项目会更稳、更易维护。",
                "先做信息架构，再做页面，Astro 博客会更稳定也更好维护。",
                18,
                tagTablesReady ? List.of(tags.get("frontend"), tags.get("astro"), tags.get("tutorial")) : List.of()
        );

        createPostIfMissing(
                adminUser,
                technology,
                "MySQL 索引优化的 5 个常见误区",
                "索引并不是越多越好。以下 5 个误区在中小项目里非常常见。\n\n" +
                        "## 误区 1：只看是否命中索引\n\n" +
                        "命中索引不代表快，还要看扫描行数、回表次数和排序代价。\n\n" +
                        "## 误区 2：为每个字段单独建索引\n\n" +
                        "高频组合查询要优先考虑联合索引，并遵循最左匹配原则。\n\n" +
                        "## 误区 3：忽略写入成本\n\n" +
                        "索引越多，写入越慢。对高写入表要控制索引数量。\n\n" +
                        "## 误区 4：不做慢查询复盘\n\n" +
                        "仅靠直觉优化很容易返工，建议每周固定复盘慢 SQL。\n\n" +
                        "## 误区 5：把数据库问题当成数据库问题\n\n" +
                        "很多瓶颈来自不合理的接口设计和分页策略，先看业务场景再动库。",
                "索引优化不是“加索引”，而是围绕真实查询场景做取舍。",
                12,
                tagTablesReady ? List.of(tags.get("mysql"), tags.get("java"), tags.get("sharing")) : List.of()
        );

        createPostIfMissing(
                adminUser,
                life,
                "为什么我坚持每周写作：从记录到影响",
                "如果你也在做个人项目，强烈建议把“写作”当作长期基础设施。\n\n" +
                        "## 写作带来的三个变化\n\n" +
                        "1. 复盘更客观：问题和改进点会被看见\n" +
                        "2. 协作更顺畅：团队理解成本显著下降\n" +
                        "3. 影响更可持续：经验可以被他人复用\n\n" +
                        "## 我现在的写作节奏\n\n" +
                        "- 每周至少一篇，优先写最近踩坑\n" +
                        "- 每篇都给出可执行清单\n" +
                        "- 用标签管理主题，方便后续串联\n\n" +
                        "## 给刚开始写作的你\n\n" +
                        "先写短文，不求完美；先讲清问题和解法，再谈观点。持续 8 周后，你会看到明显变化。",
                "把写作当作长期基础设施，你的项目和成长都会更稳定。",
                7,
                tagTablesReady ? List.of(tags.get("life"), tags.get("sharing")) : List.of()
        );
    }

    private Category ensureCategory(String name, String description) {
        Category category = categoryMapper.selectByName(name);
        if (category != null) {
            return category;
        }

        category = new Category();
        category.setName(name);
        category.setDescription(description);
        categoryMapper.insert(category);
        System.out.println("Category created: " + name);
        return category;
    }

    private Tag ensureTag(String name, String slug, String description) {
        Tag tag = tagMapper.selectBySlug(slug);
        if (tag != null) {
            return tag;
        }

        tag = tagMapper.selectByName(name);
        if (tag != null) {
            return tag;
        }

        tag = new Tag();
        tag.setName(name);
        tag.setSlug(slug);
        tag.setDescription(description);
        tagMapper.insert(tag);
        System.out.println("Tag created: " + name);
        return tag;
    }

    private void createPostIfMissing(User author,
                                     Category category,
                                     String title,
                                     String content,
                                     String summary,
                                     int daysAgo,
                                     List<Tag> postTags) {
        Post existingPost = postMapper.selectByTitle(title);
        if (existingPost != null) {
            ensureTagsForPost(existingPost.getId(), postTags);
            return;
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setSummary(summary);
        post.setCategoryId(category.getId());
        post.setAuthorId(author.getId());
        post.setStatus(PostConstants.STATUS_PUBLISHED);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setPublishedAt(LocalDateTime.now().minusDays(daysAgo));

        postMapper.insert(post);
        ensureTagsForPost(post.getId(), postTags);
        System.out.println("Seed post created: " + title + " (ID: " + post.getId() + ")");
    }

    private void ensureTagsForPost(Long postId, List<Tag> postTags) {
        if (postTags == null || postTags.isEmpty()) {
            return;
        }

        List<Tag> existingTags = tagMapper.findByPostId(postId);
        if (existingTags != null && !existingTags.isEmpty()) {
            return;
        }

        List<PostTag> mappings = new ArrayList<>();
        for (Tag tag : postTags) {
            if (tag == null || tag.getId() == null) {
                continue;
            }
            PostTag mapping = new PostTag();
            mapping.setPostId(postId);
            mapping.setTagId(tag.getId());
            mappings.add(mapping);
        }

        if (!mappings.isEmpty()) {
            postTagMapper.batchInsert(mappings);
        }
    }
}
