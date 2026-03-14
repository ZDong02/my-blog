package com.example.blog.service;

import com.example.blog.dto.request.CommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    public Comment addComment(Long postId, Long userId, CommentRequest request) {
        // Verify post exists
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("Post not found");
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

        return comment;
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("Comment not found");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("Unauthorized to delete this comment");
        }

        comment.setStatus(0);
        commentMapper.updateById(comment);

        // Update post comment count
        postMapper.updateCommentCount(comment.getPostId(), -1);
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> topLevelComments = commentMapper.findTopLevelCommentsByPostId(postId);

        // Load replies for each top-level comment
        for (Comment comment : topLevelComments) {
            List<Comment> replies = commentMapper.findRepliesByParentId(comment.getId());
            comment.setReplies(replies);
        }

        return topLevelComments;
    }

    public List<Comment> getUserComments(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return commentMapper.findCommentsByUserId(userId, offset, size);
    }
}