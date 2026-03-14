package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Bookmark;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookmarkMapper extends BaseMapper<Bookmark> {

    @Select("SELECT * FROM bookmarks WHERE user_id = #{userId} AND post_id = #{postId}")
    Bookmark findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT b.*, p.title as post_title, p.summary as post_summary " +
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
        @Result(property = "post.title", column = "post_title"),
        @Result(property = "post.summary", column = "post_summary")
    })
    List<Bookmark> findBookmarksByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Delete("DELETE FROM bookmarks WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteBookmark(@Param("userId") Long userId, @Param("postId") Long postId);
}