package com.example.blog.controller;

import com.example.blog.dto.request.CommentRequest;
import com.example.blog.dto.response.ApiResponse;
import com.example.blog.entity.Comment;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<List<Comment>>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<Comment>> addComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody CommentRequest request) {
        Comment comment = commentService.addComment(postId, userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        commentService.deleteComment(commentId, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    @GetMapping("/my-comments")
    public ResponseEntity<ApiResponse<List<Comment>>> getMyComments(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Comment> comments = commentService.getUserComments(userDetails.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }
}