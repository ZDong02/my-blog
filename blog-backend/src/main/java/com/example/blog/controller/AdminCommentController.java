package com.example.blog.controller;

import com.example.blog.dto.response.ApiResponse;
import com.example.blog.dto.response.PageResult;
import com.example.blog.entity.Comment;
import com.example.blog.security.JwtUserDetails;
import com.example.blog.service.AuditService;
import com.example.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/comments")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<Comment>>> getAllComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long postId) {
        List<Comment> comments = commentService.getAllCommentsAdmin(page, size, status, postId);
        int total = commentService.countAllCommentsAdmin(status, postId);
        return ResponseEntity.ok(ApiResponse.success(PageResult.of(comments, total, page, size)));
    }

    @PutMapping("/{commentId}/status")
    public ResponseEntity<ApiResponse<Void>> updateCommentStatus(
            @PathVariable Long commentId,
            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        commentService.updateCommentStatus(commentId, status);
        auditService.log("COMMENT_STATUS_UPDATE", "COMMENT", commentId,
                "Status changed to " + status);
        return ResponseEntity.ok(ApiResponse.success("评论状态已更新", null));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.adminDeleteComment(commentId);
        auditService.log("COMMENT_DELETE", "COMMENT", commentId, "Admin deleted comment");
        return ResponseEntity.ok(ApiResponse.success("评论已删除", null));
    }

    @PutMapping("/batch/status")
    public ResponseEntity<ApiResponse<Void>> batchUpdateStatus(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.get("commentIds");
        Integer status = (Integer) body.get("status");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("评论ID列表不能为空"));
        }

        List<Long> commentIds = ids.stream().map(Number::longValue).toList();
        commentService.batchUpdateCommentStatus(commentIds, status);
        auditService.log("BATCH_COMMENT_STATUS", "COMMENT", null,
                "Updated " + commentIds.size() + " comments to status " + status);
        return ResponseEntity.ok(ApiResponse.success("批量更新状态成功", null));
    }

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteComments(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.get("commentIds");

        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("评论ID列表不能为空"));
        }

        List<Long> commentIds = ids.stream().map(Number::longValue).toList();
        commentService.batchDeleteComments(commentIds);
        auditService.log("BATCH_COMMENT_DELETE", "COMMENT", null,
                "Batch deleted " + commentIds.size() + " comments");
        return ResponseEntity.ok(ApiResponse.success("批量删除成功", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getCommentStats() {
        Map<String, Integer> stats = commentService.getCommentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
