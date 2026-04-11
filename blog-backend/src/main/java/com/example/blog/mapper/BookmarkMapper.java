package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper extends BaseMapper<Bookmark> {

    @Select("SELECT * FROM bookmarks WHERE user_id = #{userId} AND post_id = #{postId}")
    Bookmark findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT b.*, p.id as p_id, p.title as post_title, p.summary as post_summary, " +
            "p.like_count as p_like_count, p.comment_count as p_comment_count, p.created_at as p_created_at " +
            "FROM bookmarks b " +
            "LEFT JOIN posts p ON b.post_id = p.id " +
            "WHERE b.user_id = #{userId} " +
            "ORDER BY b.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "post.id", column = "p_id"),
        @Result(property = "post.title", column = "post_title"),
        @Result(property = "post.summary", column = "post_summary"),
        @Result(property = "post.likeCount", column = "p_like_count"),
        @Result(property = "post.commentCount", column = "p_comment_count"),
        @Result(property = "post.createdAt", column = "p_created_at")
    })
    List<Bookmark> findBookmarksByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bookmarks WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteBookmark(@Param("userId") Long userId, @Param("postId") Long postId);
}
