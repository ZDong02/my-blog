package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@TableName("users")
public class User {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像 URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 角色：USER-普通用户，ADMIN-管理员
     */
    @TableField("role")
    private String role;

    /**
     * 用户状态：0-禁用，1-正常
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否为首个注册用户
     */
    @TableField("is_first_user")
    private Integer isFirstUser;

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

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    // ========== 非数据库字段，用于统计信息 ==========

    /**
     * 文章数量
     */
    @TableField(exist = false)
    private Integer postCount;

    /**
     * 评论数量
     */
    @TableField(exist = false)
    private Integer commentCount;

    /**
     * 重写 toString 方法，排除密码字段
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", avatar='" + avatar + '\'' +
                ", bio='" + bio + '\'' +
                ", role='" + role + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastLoginAt=" + lastLoginAt +
                '}';
    }
}