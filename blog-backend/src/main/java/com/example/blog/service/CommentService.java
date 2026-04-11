package com.example.blog.service;

import com.example.blog.dto.request.CommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.PostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Transactional
    public Comment addComment(Long postId, Long userId, CommentRequest request) {
        logger.info("Adding comment for post {} by user {}", postId, userId);

        // Validate input parameters
        if (postId == null || postId <= 0) {
            throw new BusinessException("无效的文章ID");
        }

        if (userId == null || userId <= 0) {
            throw new BusinessException("无效的用户ID");
        }

        if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        if (request.getContent().length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }

        // Verify post exists
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("文章不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setParentId(request.getParentId());
        comment.setStatus(1);

        commentMapper.insert(comment);

        // Update post comment count
        postMapper.updateCommentCount(postId, 1);

        logger.info("Comment added successfully: {}", comment.getId());
        return comment;
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        logger.info("Deleting comment {} by user {}", commentId, userId);

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        // Check if comment has replies
        List<Comment> replies = commentMapper.findRepliesByParentId(commentId);
        if (!replies.isEmpty()) {
            throw new BusinessException("该评论有回复，无法删除");
        }

        commentMapper.deleteById(commentId);

        // Update post comment count
        postMapper.updateCommentCount(comment.getPostId(), -1);

        logger.info("Comment deleted successfully: {}", commentId);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        // Get all comments for the post in one query
        List<Comment> allComments = commentMapper.findCommentsByPostId(postId);

        // Organize comments into hierarchical structure
        Map<Long, Comment> commentMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();

        // First pass: create map and initialize replies list
        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
            comment.setReplies(new ArrayList<>());
        }

        // Second pass: build parent-child relationships
        for (Comment comment : allComments) {
            if (comment.getParentId() == null) {
                topLevelComments.add(comment);
            } else {
                Comment parent = commentMap.get(comment.getParentId());
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }

        return topLevelComments;
    }

    public List<Comment> getUserComments(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.findCommentsByUserId(userId, offset, size);
    }

    public int countUserComments(Long userId) {
        return commentMapper.countByUserId(userId);
    }
}
