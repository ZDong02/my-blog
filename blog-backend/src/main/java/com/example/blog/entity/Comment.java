package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 * 对应数据库表：comments
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@TableName("comments")
public class Comment {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章 ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 父评论 ID（用于回复功能）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论状态：0-待审核，1-已通过，2-已拒绝
     */
    @TableField("status")
    private Integer status;

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

    // ========== 非数据库字段，用于关联查询 ==========

    /**
     * 关联用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 子评论列表（回复列表）
     */
    @TableField(exist = false)
    private List<Comment> replies;
}