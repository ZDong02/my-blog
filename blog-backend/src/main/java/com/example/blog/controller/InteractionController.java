package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Bookmark;
import com.example.blog.entity.LikeRecord;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.BookmarkService;
import com.example.blog.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 互动控制器
 * 处理点赞和收藏相关请求
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@RestController
@RequestMapping("/interactions")
@CrossOrigin
public class InteractionController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private BookmarkService bookmarkService;

    /**
     * 点赞文章
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @PostMapping("/like/{postId}")
    public ResponseEntity<ApiResponse<Void>> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        likeService.likePost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    /**
     * 取消点赞文章
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @DeleteMapping("/like/{postId}")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        likeService.unlikePost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("已取消点赞", null));
    }

    /**
     * 检查点赞状态
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 是否点赞
     */
    @GetMapping("/like/check/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> checkLikeStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        boolean isLiked = likeService.isPostLikedByUser(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }

    /**
     * 获取我的点赞列表
     *
     * @param userDetails 当前用户
     * @param page        页码
     * @param size        每页大小
     * @return 点赞列表
     */
    @GetMapping("/likes/my-likes")
    public ResponseEntity<ApiResponse<PageResult<LikeRecord>>> getMyLikes(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<LikeRecord> likes = likeService.getUserLikes(userDetails.getId(), page, size);
        int total = likeService.countUserLikes(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(PageResult.of(likes, total, page, size)));
    }

    /**
     * 收藏文章
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @PostMapping("/bookmark/{postId}")
    public ResponseEntity<ApiResponse<Void>> bookmarkPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        bookmarkService.bookmarkPost(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("收藏成功", null));
    }

    /**
     * 取消收藏
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 响应
     */
    @DeleteMapping("/bookmark/{postId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        bookmarkService.removeBookmark(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("已取消收藏", null));
    }

    /**
     * 检查收藏状态
     *
     * @param postId      文章 ID
     * @param userDetails 当前用户
     * @return 是否收藏
     */
    @GetMapping("/bookmark/check/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBookmarkStatus(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        boolean isBookmarked = bookmarkService.isPostBookmarkedByUser(postId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(isBookmarked));
    }

    /**
     * 获取我的收藏列表
     *
     * @param userDetails 当前用户
     * @param page        页码
     * @param size        每页大小
     * @return 收藏列表
     */
    @GetMapping("/bookmarks/my-bookmarks")
    public ResponseEntity<ApiResponse<PageResult<Bookmark>>> getMyBookmarks(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), page, size);
        int total = bookmarkService.countUserBookmarks(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(PageResult.of(bookmarks, total, page, size)));
    }
}
