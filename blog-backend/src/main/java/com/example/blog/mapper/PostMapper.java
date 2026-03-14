package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT p.*, u.username as author_username, u.nickname as author_nickname, " +
            "c.name as category_name " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.author_id = u.id " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE p.status = 'PUBLISHED' " +
            "ORDER BY p.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "summary", column = "summary"),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "authorId", column = "author_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "author.username", column = "author_username"),
        @Result(property = "author.nickname", column = "author_nickname"),
        @Result(property = "category.name", column = "category_name")
    })
    List<Post> findPublishedPosts(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM posts WHERE status = 'PUBLISHED'")
    int countPublishedPosts();

    @Select("SELECT * FROM posts WHERE author_id = #{authorId} AND status != 'DELETED' ORDER BY created_at DESC")
    List<Post> findByAuthorId(@Param("authorId") Long authorId);

    @Select("SELECT p.*, u.username as author_username, u.nickname as author_nickname, " +
            "c.name as category_name " +
            "FROM posts p " +
            "LEFT JOIN users u ON p.author_id = u.id " +
            "LEFT JOIN categories c ON p.category_id = c.id " +
            "WHERE p.id = #{postId} AND p.status = 'PUBLISHED'")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "title", column = "title"),
        @Result(property = "content", column = "content"),
        @Result(property = "summary", column = "summary"),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "authorId", column = "author_id"),
        @Result(property = "status", column = "status"),
        @Result(property = "viewCount", column = "view_count"),
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "commentCount", column = "comment_count"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at"),
        @Result(property = "publishedAt", column = "published_at"),
        @Result(property = "author.username", column = "author_username"),
        @Result(property = "author.nickname", column = "author_nickname"),
        @Result(property = "category.name", column = "category_name")
    })
    Post findPublishedPostById(@Param("postId") Long postId);

    @Update("UPDATE posts SET view_count = view_count + 1 WHERE id = #{postId}")
    void incrementViewCount(@Param("postId") Long postId);

    @Update("UPDATE posts SET like_count = like_count + #{increment} WHERE id = #{postId}")
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);

    @Update("UPDATE posts SET comment_count = comment_count + #{increment} WHERE id = #{postId}")
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);
}