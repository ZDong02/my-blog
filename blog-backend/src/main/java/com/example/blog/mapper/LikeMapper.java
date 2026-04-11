package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.LikeRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 点赞 Mapper 接口
 *
 * @author Blog Team
 * @date 2026-03-18
 */
@Mapper
public interface LikeMapper extends BaseMapper<LikeRecord> {

    /**
     * 统计文章点赞数
     *
     * @param postId 文章 ID
     * @return 点赞数
     */
    @Select("SELECT COUNT(*) FROM like_records WHERE post_id = #{postId}")
    int countLikesByPostId(@Param("postId") Long postId);

    /**
     * 根据用户 ID 和文章 ID 查询点赞记录
     *
     * @param userId 用户 ID
     * @param postId 文章 ID
     * @return 点赞记录
     */
    @Select("SELECT * FROM like_records WHERE user_id = #{userId} AND post_id = #{postId}")
    LikeRecord findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 查询用户的点赞记录
     *
     * @param userId 用户 ID
     * @param offset 偏移量
     * @param limit  限制数
     * @return 点赞记录列表
     */
    @Select("SELECT l.*, p.title as post_title, p.summary as post_summary, " +
            "p.id as p_id, p.like_count as p_like_count, p.comment_count as p_comment_count, p.created_at as p_created_at " +
            "FROM like_records l " +
            "LEFT JOIN posts p ON l.post_id = p.id " +
            "WHERE l.user_id = #{userId} " +
            "ORDER BY l.created_at DESC " +
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
    List<LikeRecord> findLikesByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户点赞总数
     */
    @Select("SELECT COUNT(*) FROM like_records WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 删除点赞记录
     *
     * @param userId 用户 ID
     * @param postId 文章 ID
     * @return 影响行数
     */
    @Delete("DELETE FROM like_records WHERE user_id = #{userId} AND post_id = #{postId}")
    int deleteLike(@Param("userId") Long userId, @Param("postId") Long postId);
}
