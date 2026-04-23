package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 音乐实体类
 * 对应数据库表：music
 *
 * @author Blog Team
 * @date 2026-04-16
 */
@Data
@TableName("music")
public class Music {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 歌曲名称
     */
    @TableField("title")
    private String title;

    /**
     * 艺术家
     */
    @TableField("artist")
    private String artist;

    /**
     * YouTube 视频 ID
     */
    @TableField("youtube_id")
    private String youtubeId;

    /**
     * 分类标签（如 R&B、Pop、Rock）
     */
    @TableField("category")
    private String category;

    /**
     * 排序顺序（越小越靠前）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 封面图 URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 音频文件 URL
     */
    @TableField("audio_url")
    private String audioUrl;

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
}
