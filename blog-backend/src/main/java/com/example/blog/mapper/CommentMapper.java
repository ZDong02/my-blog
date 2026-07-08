package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.id as user_id, u.username as user_username, u.nickname as user_nickname, u.avatar as user_avatar " +
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
        @Result(property = "user.username", column = "user_username"),
        @Result(property = "user.nickname", column = "user_nickname"),
        @Result(property = "user.avatar", column = "user_avatar")
    })
    List<Comment> findTopLevelCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, u.id as user_id, u.username as user_username, u.nickname as user_nickname, u.avatar as user_avatar " +
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
        @Result(property = "user.username", column = "user_username"),
        @Result(property = "user.nickname", column = "user_nickname"),
        @Result(property = "user.avatar", column = "user_avatar")
    })
    List<Comment> findRepliesByParentId(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM comments WHERE post_id = #{postId} AND status = 1")
    int countCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT c.*, p.id as p_id, p.title as p_title, u.username, u.nickname, u.avatar " +
            "FROM comments c " +
            "LEFT JOIN posts p ON c.post_id = p.id " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND c.status = 1 " +
            "ORDER BY c.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "content", column = "content"),
        @Result(property = "parentId", column = "parent_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "post.id", column = "p_id"),
        @Result(property = "post.title", column = "p_title"),
        @Result(property = "user.id", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.nickname", column = "nickname"),
        @Result(property = "user.avatar", column = "avatar")
    })
    List<Comment> findCommentsByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT c.*, u.id as user_id, u.username as user_username, u.nickname as user_nickname, u.avatar as user_avatar " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.status = 1 " +
            "ORDER BY IF(c.parent_id IS NULL, c.created_at, c.parent_id), c.created_at ASC")
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
        @Result(property = "user.username", column = "user_username"),
        @Result(property = "user.nickname", column = "user_nickname"),
        @Result(property = "user.avatar", column = "user_avatar")
    })
    List<Comment> findCommentsByPostId(@Param("postId") Long postId);

    @Select("SELECT COUNT(*) FROM comments WHERE user_id = #{userId} AND status = 1")
    int countByUserId(@Param("userId") Long userId);

    // ========== 管理后台查询方法 ==========

    @Select("<script>" +
            "SELECT c.*, u.id as user_id, u.username as user_username, u.nickname as user_nickname, u.avatar as user_avatar, " +
            "p.id as p_id, p.title as p_title " +
            "FROM comments c " +
            "LEFT JOIN users u ON c.user_id = u.id " +
            "LEFT JOIN posts p ON c.post_id = p.id " +
            "<where>" +
            "  <if test='status != null'> AND c.status = #{status}</if>" +
            "  <if test='postId != null'> AND c.post_id = #{postId}</if>" +
            "</where>" +
            " ORDER BY c.created_at DESC" +
            " LIMIT #{offset}, #{limit}" +
            "</script>")
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
        @Result(property = "user.username", column = "user_username"),
        @Result(property = "user.nickname", column = "user_nickname"),
        @Result(property = "user.avatar", column = "user_avatar"),
        @Result(property = "post.id", column = "p_id"),
        @Result(property = "post.title", column = "p_title")
    })
    List<Comment> findAllCommentsAdmin(@Param("offset") int offset, @Param("limit") int limit,
                                       @Param("status") Integer status, @Param("postId") Long postId);

    @Select("<script>" +
            "SELECT COUNT(*) FROM comments c" +
            "<where>" +
            "  <if test='status != null'> AND c.status = #{status}</if>" +
            "  <if test='postId != null'> AND c.post_id = #{postId}</if>" +
            "</where>" +
            "</script>")
    int countAllCommentsAdmin(@Param("status") Integer status, @Param("postId") Long postId);
}
