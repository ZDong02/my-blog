package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("category_id")
    private Long categoryId;

    @TableField("author_id")
    private Long authorId;

    @TableField("status")
    private String status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    // Additional fields for relationships (not mapped to database)
    @TableField(exist = false)
    private User author;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private Boolean isLikedByCurrentUser;

    @TableField(exist = false)
    private Boolean isBookmarkedByCurrentUser;
}