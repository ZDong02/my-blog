package com.example.blog.service;

import com.example.blog.entity.LikeRecord;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.LikeMapper;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 点赞服务类
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private PostMapper postMapper;

    /**
     * 点赞文章
     *
     * @param postId 文章 ID
     * @param userId 用户 ID
     */
    public void likePost(Long postId, Long userId) {
        // 检查文章是否存在
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("文章不存在");
        }

        // 检查是否已点赞
        LikeRecord existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existingLike != null) {
            throw new BusinessException("已点赞该文章");
        }

        LikeRecord like = new LikeRecord();
        like.setPostId(postId);
        like.setUserId(userId);

        likeMapper.insert(like);

        // 更新文章点赞数
        postMapper.updateLikeCount(postId, 1);
    }

    /**
     * 取消点赞文章
     *
     * @param postId 文章 ID
     * @param userId 用户 ID
     */
    public void unlikePost(Long postId, Long userId) {
        LikeRecord existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existingLike == null) {
            throw new BusinessException("点赞记录不存在");
        }

        likeMapper.deleteLike(userId, postId);

        // 更新文章点赞数
        postMapper.updateLikeCount(postId, -1);
    }

    /**
     * 检查用户是否点赞了文章
     *
     * @param postId 文章 ID
     * @param userId 用户 ID
     * @return 是否点赞
     */
    public boolean isPostLikedByUser(Long postId, Long userId) {
        return likeMapper.findByUserIdAndPostId(userId, postId) != null;
    }

    /**
     * 获取文章点赞数
     *
     * @param postId 文章 ID
     * @return 点赞数
     */
    public int getPostLikeCount(Long postId) {
        return likeMapper.countLikesByPostId(postId);
    }

    /**
     * 获取用户的点赞记录
     *
     * @param userId 用户 ID
     * @param page   页码
     * @param size   每页大小
     * @return 点赞记录列表
     */
    public List<LikeRecord> getUserLikes(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return likeMapper.findLikesByUserId(userId, offset, size);
    }
}