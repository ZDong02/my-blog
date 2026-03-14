package com.example.blog.service;

import com.example.blog.entity.Like;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.LikeMapper;
import com.example.blog.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private PostMapper postMapper;

    public void likePost(Long postId, Long userId) {
        // Check if post exists
        if (postMapper.selectById(postId) == null) {
            throw new BusinessException("Post not found");
        }

        // Check if already liked
        Like existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existingLike != null) {
            throw new BusinessException("Post already liked");
        }

        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);

        likeMapper.insert(like);

        // Update post like count
        postMapper.updateLikeCount(postId, 1);
    }

    public void unlikePost(Long postId, Long userId) {
        Like existingLike = likeMapper.findByUserIdAndPostId(userId, postId);
        if (existingLike == null) {
            throw new BusinessException("Like not found");
        }

        likeMapper.deleteLike(userId, postId);

        // Update post like count
        postMapper.updateLikeCount(postId, -1);
    }

    public boolean isPostLikedByUser(Long postId, Long userId) {
        return likeMapper.findByUserIdAndPostId(userId, postId) != null;
    }

    public int getPostLikeCount(Long postId) {
        return likeMapper.countLikesByPostId(postId);
    }

    public List<Like> getUserLikes(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return likeMapper.findLikesByUserId(userId, offset, size);
    }
}