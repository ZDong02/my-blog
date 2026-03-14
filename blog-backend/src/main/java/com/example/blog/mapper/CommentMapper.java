package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 AND c.parent_id IS NULL " +
            "ORDER BY c.created_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "user.id", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.nickname", column = "nickname"),
        @Result(property = "user.avatar", column = "avatar")
    })
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.username, u.nickname, u.avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
            "ORDER BY c.created_at ASC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "user.id", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.nickname", column = "nickname"),
        @Result(property = "user.avatar", column = "avatar")
    })
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM comments WHERE post_id = #{postId} AND status = 1")
    int countCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT * FROM comments WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC LIMIT #{offset}, #{limit}")
    List<Comment> findCommentsByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
}