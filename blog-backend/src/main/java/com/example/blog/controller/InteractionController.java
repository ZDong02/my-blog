package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Bookmark;
import com.example.blog.entity.Like;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.BookmarkService;
import com.example.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin
public class InteractionController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private BookmarkService bookmarkService;

    // Like operations
    @PostMapping("/like/{postId}")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        likeService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", null));
    }

    @DeleteMapping("/like/{postId}")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        likeService.unlikePost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", null));
    }

    @GetMapping("/like/check/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        boolean isLiked = likeService.isPostLikedByUser(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }

    @GetMapping("/likes/my-likes")
    public ResponseEntity<ApiResponse<List<Like>>> getMyLikes(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Like> likes = likeService.getUserLikes(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(likes));
    }

    // Bookmark operations
    @PostMapping("/bookmark/{postId}")
    public ResponseEntity<ApiResponse<Void>> bookmarkPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        bookmarkService.bookmarkPost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Post bookmarked successfully", null));
    }

    @DeleteMapping("/bookmark/{postId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        bookmarkService.removeBookmark(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Bookmark removed successfully", null));
    }

    @GetMapping("/bookmark/check/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBookmarkStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        boolean isBookmarked = bookmarkService.isPostBookmarkedByUser(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(isBookmarked));
    }

    @GetMapping("/bookmarks/my-bookmarks")
    public ResponseEntity<ApiResponse<List<Bookmark>>> getMyBookmarks(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(bookmarks));
    }
}