package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Bookmark;
import com.example.blog.entity.Comment;
import com.example.blog.entity.Like;
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

@RestController
@RequestMapping("/api/dashboard")
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

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Map<String, Object> stats = new HashMap<>();

        // Get user's posts count (for admin users)
        if ("ADMIN".equals(userDetails.getRole())) {
            List<Post> userPosts = postService.getPostsByAuthor(userDetails.getId());
            stats.put("postsCount", userPosts.size());
        }

        // Get user's likes count
        List<Like> userLikes = likeService.getUserLikes(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("likesCount", userLikes.size());

        // Get user's bookmarks count
        List<Bookmark> userBookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("bookmarksCount", userBookmarks.size());

        // Get user's comments count
        List<Comment> userComments = commentService.getUserComments(userDetails.getId(), 1, Integer.MAX_VALUE);
        stats.put("commentsCount", userComments.size());

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecentActivity(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Map<String, Object> activity = new HashMap<>();

        // Get recent likes
        List<Like> recentLikes = likeService.getUserLikes(userDetails.getId(), 1, 5);
        activity.put("recentLikes", recentLikes);

        // Get recent bookmarks
        List<Bookmark> recentBookmarks = bookmarkService.getUserBookmarks(userDetails.getId(), 1, 5);
        activity.put("recentBookmarks", recentBookmarks);

        // Get recent comments
        List<Comment> recentComments = commentService.getUserComments(userDetails.getId(), 1, 5);
        activity.put("recentComments", recentComments);

        // Get user's posts (for admin users)
        if ("ADMIN".equals(userDetails.getRole())) {
            List<Post> userPosts = postService.getPostsByAuthor(userDetails.getId());
            activity.put("myPosts", userPosts);
        }

        return ResponseEntity.ok(ApiResponse.success(activity));
    }
}