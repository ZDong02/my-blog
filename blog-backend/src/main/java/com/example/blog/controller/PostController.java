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

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Post>>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Post> result = postService.getPublishedPosts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> getPost(@PathVariable Long id) {
        Post post = postService.viewPost(id);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Post>> createPost(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request) {
        Post post = postService.createPost(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Post created successfully", post));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Post>> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody PostCreateRequest request) {
        Post post = postService.updatePost(id, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", post));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Post>> publishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Post post = postService.publishPost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post published successfully", post));
    }

    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Post>> unpublishPost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Post post = postService.unpublishPost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post unpublished successfully", post));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        postService.deletePost(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @GetMapping("/my-posts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Post>>> getMyPosts(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        List<Post> posts = postService.getPostsByAuthor(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(posts));
    }
}