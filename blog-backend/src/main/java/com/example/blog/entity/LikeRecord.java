package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 点赞实体类
 * 对应数据库表：like_records
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@TableName("like_records")
public class LikeRecord {
    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 文章 ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 关联文章信息
     */
    @TableField(exist = false)
    private Post post;
}
