package com.example.blog.controller;

import com.example.blog.dto.request.PostCreateRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Post;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文章控制器
 * 处理文章相关请求
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@RestController
@RequestMapping("/posts")
@CrossOrigin
public class PostController {

    @Autowired
    private PostService postService;

    /**
     * 获取文章列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 文章列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Post>>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Post> result = postService.getPublishedPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取文章详情
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> getPost(@PathVariable Long id) {
        Post post = postService.viewPost(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    /**
     * 创建文章
     *
     * @param userDetails 当前用户
     * @param request     创建请求
     * @return 创建的文章
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Post>> createPost(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request) {
        Post post = postService.createPost(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("文章创建成功", post));
    }

    /**
     * 更新文章
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @param request   更新请求
     * @return 更新后的文章
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request) {
        Post post = postService.updatePost(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("文章更新成功", post));
    }

    /**
     * 发布文章
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @return 发布后的文章
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<Post>> publishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Post post = postService.publishPost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("文章已发布", post));
    }

    /**
     * 取消发布文章
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @return 取消发布后的文章
     */
    @PutMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<Post>> unpublishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Post post = postService.unpublishPost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("文章已取消发布", post));
    }

    /**
     * 删除文章（移至回收站）
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        postService.deletePost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("文章已移至回收站", null));
    }

    /**
     * 获取回收站中的文章
     *
     * @return 回收站文章列表
     */
    @GetMapping("/recycle-bin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Post>>> getDeletedPosts() {
        List<Post> posts = postService.getDeletedPosts();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 获取我的回收站文章
     *
     * @param userDetails 当前用户
     * @return 回收站文章列表
     */
    @GetMapping("/my-recycle-bin")
    public ResponseEntity<ApiResponse<List<Post>>> getMyDeletedPosts(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        List<Post> posts = postService.getDeletedPostsByAuthor(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 恢复已删除的文章
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @return 恢复后的文章
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Post>> restorePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Post post = postService.restorePost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("文章已恢复", post));
    }

    /**
     * 永久删除文章
     *
     * @param id        文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        postService.permanentlyDeletePost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("文章已永久删除", null));
    }

    /**
     * 获取我的文章列表
     *
     * @param userDetails 当前用户
     * @return 文章列表
     */
    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<Post>>> getMyPosts(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        List<Post> posts = postService.getPostsByAuthor(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 获取所有文章（管理员用）
     *
     * @return 所有文章列表
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 搜索文章
     *
     * @param keyword    关键词
     * @param categoryId 分类 ID
     * @param page       页码
     * @param size       每页大小
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResult<Post>>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Post> result = postService.searchPosts(keyword, categoryId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取热门文章
     *
     * @param size 数量限制
     * @return 热门文章列表
     */
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<List<Post>>> getHotPosts(
            @RequestParam(defaultValue = "10") int size) {
        List<Post> posts = postService.getHotPosts(size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    /**
     * 获取归档统计
     *
     * @return 归档统计列表
     */
    @GetMapping("/archive")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getArchiveStats() {
        List<Map<String, Object>> stats = postService.getArchiveStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
