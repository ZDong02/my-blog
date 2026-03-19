package com.example.blog.config;

import com.example.blog.entity.Category;
import com.example.blog.entity.Post;
import com.example.blog.entity.User;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.PostMapper;
import com.example.blog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        updateUsersAvatar();
        createAdminUser();
        createFirstBlogPost();
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
            user.setRole("ADMIN");
            user.setIsFirstUser(1);
            userMapper.insert(user);
            System.out.println("Admin user created: admin/admin123");
        } else {
            // Upgrade existing admin user to ADMIN role if not already
            if (!"ADMIN".equals(adminUser.getRole())) {
                adminUser.setRole("ADMIN");
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

    private void createFirstBlogPost() {
        // Check if the first blog post exists
        Post existingPost = postMapper.selectById(1L);
        if (existingPost == null || !"我的第一篇 blog".equals(existingPost.getTitle())) {
            // Get admin user
            User adminUser = userMapper.findByUsername("admin");
            if (adminUser == null) {
                System.out.println("Admin user not found, skipping blog post creation");
                return;
            }

            // Get or create default category
            Category category = categoryMapper.selectByName("Technology");
            if (category == null) {
                category = new Category();
                category.setName("Technology");
                category.setDescription("Technology related posts");
                categoryMapper.insert(category);
                System.out.println("Category created: Technology");
            }

            // Check again if post with this title exists
            existingPost = postMapper.selectByTitle("我的第一篇 blog");
            if (existingPost != null) {
                System.out.println("First blog post already exists, skipping");
                return;
            }

            // Create first blog post
            Post post = new Post();
            post.setTitle("我的第一篇 blog");
            post.setContent("欢迎来到我的博客！这是我的第一篇文章。\n\n" +
                    "## 关于这个博客\n\n" +
                    "这个博客是用来分享我的想法、经验和技术文章的地方。我希望通过这种方式，能够记录我的学习和成长历程。\n\n" +
                    "## 未来的计划\n\n" +
                    "在接下来的时间里，我计划分享以下内容：\n\n" +
                    "- 技术教程和笔记\n" +
                    "- 生活点滴和感悟\n" +
                    "- 旅行经历和见闻\n" +
                    "- 美食探索和推荐\n\n" +
                    "## 结语\n\n" +
                    "感谢你的阅读！如果你有任何问题或建议，欢迎在评论区留言。\n\n" +
                    "---\n\n" +
                    "*这是使用 Spring Boot 和 Astro 构建的博客系统*");
            post.setSummary("这是我的第一篇博客文章，记录我开始写博客的时刻。");
            post.setCategoryId(category.getId());
            post.setAuthorId(adminUser.getId());
            post.setStatus("PUBLISHED");
            post.setPublishedAt(LocalDateTime.now());

            postMapper.insert(post);
            System.out.println("First blog post created: 我的第一篇 blog (ID: " + post.getId() + ")");
        }
    }
}
