package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.service.PostService;
import com.example.blog.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tags")
@CrossOrigin
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private PostService postService;

    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getAllTags() {
        List<Tag> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> getTagById(@PathVariable Long id) {
        Tag tag = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }

    /**
     * 按 slug 获取标签
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<Tag>> getTagBySlug(@PathVariable String slug) {
        Tag tag = tagService.getTagBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(tag));
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Tag>>> searchTags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int offset = (page - 1) * size;
        List<Tag> tags = tagService.searchTags(keyword, offset, size);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<Tag>>> getHotTags(
            @RequestParam(defaultValue = "20") int limit) {
        List<Tag> tags = tagService.getHotTags(limit);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 创建标签
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Tag>> createTag(
            @Valid @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String slug = request.get("slug");
        String description = request.get("description");

        Tag tag = tagService.createTag(name, slug, description);
        return ResponseEntity.ok(ApiResponse.success("Tag created successfully", tag));
    }

    /**
     * 更新标签
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Tag>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, String> request) {
        String name = request.get("name");
        String slug = request.get("slug");
        String description = request.get("description");

        Tag tag = tagService.updateTag(id, name, slug, description);
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", tag));
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTag(@PathVariable Long id) {
        Map<String, Object> data = new HashMap<>();
        try {
            tagService.deleteTag(id);
            data.put("deleted", true);
            return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", data));
        } catch (BusinessException e) {
            data.put("deleted", false);
            data.put("reason", e.getMessage());
            return ResponseEntity.ok(ApiResponse.errorWithData(e.getMessage(), data));
        }
    }

    /**
     * 获取标签下的文章列表
     */
    @GetMapping("/{id}/posts")
    public ResponseEntity<ApiResponse<PageResult<com.example.blog.entity.Post>>> getPostsByTag(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<com.example.blog.entity.Post> result = postService.getPostsByTag(id, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
