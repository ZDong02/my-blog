package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体类
 * 对应数据库表：posts
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@TableName("posts")
public class Post {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文章标题
     */
    @TableField("title")
    private String title;

    /**
     * 文章内容
     */
    @TableField("content")
    private String content;

    /**
     * 文章摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 分类 ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 作者 ID
     */
    @TableField("author_id")
    private Long authorId;

    /**
     * 文章状态：0-草稿，1-已发布，2-已下架
     */
    @TableField("status")
    private String status;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞次数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论次数
     */
    @TableField("comment_count")
    private Integer commentCount;

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
     * 发布时间
     */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    // ========== 非数据库字段，用于关联查询 ==========

    /**
     * 关联作者信息
     */
    @TableField(exist = false)
    private User author;

    /**
     * 关联分类信息
     */
    @TableField(exist = false)
    private Category category;

    /**
     * 当前用户是否点赞
     */
    @TableField(exist = false)
    private Boolean isLikedByCurrentUser;

    /**
     * 当前用户是否收藏
     */
    @TableField(exist = false)
    private Boolean isBookmarkedByCurrentUser;

    /**
     * 关联标签列表
     */
    @TableField(exist = false)
    private List<Tag> tags;
}