package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Bookmark;
import com.example.blog.entity.Comment;
import com.example.blog.entity.LikeRecord;
import com.example.blog.entity.Post;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.CommentService;
import com.example.blog.service.LikeService;
import com.example.blog.service.PostService;
import com.example.blog.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 * 处理用户仪表盘相关请求
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin
public class DashboardController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private BookmarkService bookmarkService;

    /**
     * 获取仪表盘统计数据
     *
     * @param userDetails 当前用户
     * @return 统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Map<String, Object> stats = new HashMap<>();

        // 获取用户文章数（仅管理员）
        if ("ADMIN".equals(userDetails.getRole())) {
            List<Post> userPosts = postService.getPostsByAuthor(userDetails.getId());
            stats.put("postsCount", userPosts.size());
        }

        // 获取用户点赞数
        List<LikeRecord> userLikes = likeService.getUserLikes(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("likesCount", userLikes.size());

        // 获取用户收藏数
        List<Bookmark> userBookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("bookmarksCount", userBookmarks.size());

        // 获取用户评论数
        List<Comment> userComments = commentService.getUserComments(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("commentsCount", userComments.size());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取最近活动
     *
     * @param userDetails 当前用户
     * @return 活动列表
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivity(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Map<String, Object> activity = new HashMap<>();

        // 最近点赞
        List<LikeRecord> recentLikes = likeService.getUserLikes(userDetails.getId(), 1, 5);
        activity.put("recentLikes", recentLikes);

        // 最近收藏
        List<Bookmark> recentBookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), 1, 5);
        activity.put("recentBookmarks", recentBookmarks);

        // 最近评论
        List<Comment> recentComments = commentService.getUserComments(userDetails.getId(), 1, 5);
        activity.put("recentComments", recentComments);

        // 获取用户的文章（仅管理员）
        if ("ADMIN".equals(userDetails.getRole())) {
            List<Post> userPosts = postService.getPostsByAuthor(userDetails.getId());
            activity.put("myPosts", userPosts);
        }

        return ResponseEntity.ok(ApiResponse.success(activity));
    }
}