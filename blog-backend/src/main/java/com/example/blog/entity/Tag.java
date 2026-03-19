package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库表：tags
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@TableName("tags")
public class Tag {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名称
     */
    @TableField("name")
    private String name;

    /**
     * 标签别名（用于 URL）
     */
    @TableField("slug")
    private String slug;

    /**
     * 标签描述
     */
    @TableField("description")
    private String description;

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

    // ========== 非数据库字段，用于统计信息 ==========

    /**
     * 关联文章数
     */
    @TableField(exist = false)
    private Integer postCount;
}
