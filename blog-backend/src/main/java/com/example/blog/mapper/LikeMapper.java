package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Like;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LikeMapper extends BaseMapper<Like> {

    @Select("SELECT COUNT(*) FROM likes WHERE post_id = #{postId}")
    int countLikesByPostId(@Param("postId") Long postId);

    @Select("SELECT * FROM likes WHERE user_id = #{userId} AND post_id = #{postId}")
    Like findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Select("SELECT l.*, p.title as post_title, p.summary as post_summary " +
            "FROM likes l " +
            "LEFT JOIN posts p ON l.post_id = p.id " +
            "WHERE l.user_id = #{userId} " +
            "ORDER BY l.created_at DESC " +
            "LIMIT #{offset}, #{limit}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "postId", column = "post_id"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "post.title", column = "post_title"),
        @Result(property = "post.summary", column = "post_summary")
    })
    List<Like> findLikesByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Delete("DELETE FROM likes WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteLike(@Param("userId") Long userId, @Param("postId") Long postId);
}