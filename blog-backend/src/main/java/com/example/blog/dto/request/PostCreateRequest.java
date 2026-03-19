package com.example.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * 文章创建请求 DTO
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
public class PostCreateRequest {

    /**
     * 文章标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在 1 到 200 个字符之间")
    private String title;

    /**
     * 文章内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(min = 1, message = "内容不能为空")
    private String content;

    /**
     * 文章摘要
     */
    @Size(max = 500, message = "摘要长度不能超过 500 个字符")
    private String summary;

    /**
     * 分类 ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /**
     * 标签 ID 列表
     */
    private List<Long> tagIds;
}