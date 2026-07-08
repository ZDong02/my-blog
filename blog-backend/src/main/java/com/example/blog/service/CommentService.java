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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    /** 评论发布后可编辑的时间窗口（分钟） */
    private static final int EDIT_WINDOW_MINUTES = 5;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Transactional
    public Comment addComment(Long postId, Long userId, CommentRequest request) {
        logger.info("Adding comment for post {} by user {}", postId, userId);

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
        postMapper.updateCommentCount(postId, 1);

        logger.info("Comment added successfully: {}", comment.getId());
        return comment;
    }

    /**
     * 编辑评论（仅作者本人，且须在发布时间窗口内）
     */
    @Transactional
    public Comment editComment(Long commentId, Long userId, String newContent) {
        logger.info("Editing comment {} by user {}", commentId, userId);

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (newContent.length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权编辑此评论");
        }

        // 检查编辑时间窗口
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceCreation = ChronoUnit.MINUTES.between(comment.getCreatedAt(), now);
        if (minutesSinceCreation > EDIT_WINDOW_MINUTES) {
            throw new BusinessException("评论已超过可编辑时间（" + EDIT_WINDOW_MINUTES + "分钟），无法修改");
        }

        comment.setContent(newContent);
        commentMapper.updateById(comment);

        logger.info("Comment edited successfully: {}", commentId);
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

        // 如果有子回复，级联删除
        List<Comment> replies = commentMapper.findRepliesByParentId(commentId);
        int deletedCount = 1;
        if (!replies.isEmpty()) {
            for (Comment reply : replies) {
                commentMapper.deleteById(reply.getId());
                deletedCount++;
            }
        }

        commentMapper.deleteById(commentId);
        postMapper.updateCommentCount(comment.getPostId(), -deletedCount);

        logger.info("Comment deleted successfully: {} (cascade deleted {} replies)", commentId, replies.size());
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> allComments = commentMapper.findCommentsByPostId(postId);

        Map<Long, Comment> commentMap = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();

        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
            comment.setReplies(new ArrayList<>());
        }

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

    // ========== 管理后台方法 ==========

    /**
     * 管理后台 - 分页获取所有评论
     */
    public List<Comment> getAllCommentsAdmin(int page, int size, Integer status, Long postId) {
        int offset = (page - 1) * size;
        return commentMapper.findAllCommentsAdmin(offset, size, status, postId);
    }

    /**
     * 管理后台 - 统计评论总数（支持按状态/文章筛选）
     */
    public int countAllCommentsAdmin(Integer status, Long postId) {
        return commentMapper.countAllCommentsAdmin(status, postId);
    }

    /**
     * 管理后台 - 更新评论状态
     */
    @Transactional
    public void updateCommentStatus(Long commentId, Integer status) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        if (status < 0 || status > 2) {
            throw new BusinessException("无效的评论状态");
        }

        Integer oldStatus = comment.getStatus();
        comment.setStatus(status);
        commentMapper.updateById(comment);

        // 更新文章评论计数
        if (oldStatus != 1 && status == 1) {
            postMapper.updateCommentCount(comment.getPostId(), 1);
        } else if (oldStatus == 1 && status != 1) {
            postMapper.updateCommentCount(comment.getPostId(), -1);
        }

        logger.info("Comment {} status changed from {} to {}", commentId, oldStatus, status);
    }

    /**
     * 管理后台 - 删除评论（级联删除子评论）
     */
    @Transactional
    public void adminDeleteComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        int deletedCount = 0;
        // 级联删除子评论
        List<Comment> replies = commentMapper.findRepliesByParentId(commentId);
        for (Comment reply : replies) {
            commentMapper.deleteById(reply.getId());
            if (reply.getStatus() == 1) deletedCount++;
        }

        commentMapper.deleteById(commentId);
        if (comment.getStatus() == 1) deletedCount++;

        if (deletedCount > 0) {
            postMapper.updateCommentCount(comment.getPostId(), -deletedCount);
        }

        logger.info("Admin deleted comment {} (cascade {} replies)", commentId, replies.size());
    }

    /**
     * 管理后台 - 批量更新评论状态
     */
    @Transactional
    public void batchUpdateCommentStatus(List<Long> commentIds, Integer status) {
        for (Long commentId : commentIds) {
            try {
                updateCommentStatus(commentId, status);
            } catch (BusinessException e) {
                logger.warn("Batch status update failed for comment {}: {}", commentId, e.getMessage());
            }
        }
    }

    /**
     * 管理后台 - 批量删除评论
     */
    @Transactional
    public void batchDeleteComments(List<Long> commentIds) {
        for (Long commentId : commentIds) {
            try {
                adminDeleteComment(commentId);
            } catch (BusinessException e) {
                logger.warn("Batch delete failed for comment {}: {}", commentId, e.getMessage());
            }
        }
    }

    /**
     * 管理后台 - 评论统计
     */
    public Map<String, Integer> getCommentStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", commentMapper.countAllCommentsAdmin(null, null));
        stats.put("approved", commentMapper.countAllCommentsAdmin(1, null));
        stats.put("pending", commentMapper.countAllCommentsAdmin(0, null));
        stats.put("rejected", commentMapper.countAllCommentsAdmin(2, null));
        return stats;
    }
}
