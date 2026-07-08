package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    // 查询已发布的文章（分页）
    List<Post> findPublishedPosts(@Param("offset") int offset, @Param("limit") int limit);

    // 统计已发布文章数量
    int countPublishedPosts();

    // 按作者查询文章
    List<Post> findByAuthorId(@Param("authorId") Long authorId);

    // 查询已发布文章
    Post findPublishedPostById(@Param("postId") Long postId);

    // 增加浏览量
    void incrementViewCount(@Param("postId") Long postId);

    // 更新点赞数
    void updateLikeCount(@Param("postId") Long postId, @Param("increment") int increment);

    // 更新评论数
    void updateCommentCount(@Param("postId") Long postId, @Param("increment") int increment);

    // 按标题查询
    Post selectByTitle(@Param("title") String title);

    // 搜索文章
    List<Post> searchPosts(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    // 统计搜索结果
    int countSearchPosts(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId);

    // 查询热门文章
    List<Post> findHotPosts(@Param("limit") int limit);

    // 获取归档统计
    List<Map<String, Object>> getArchiveStats();

    // 按标签查询文章
    List<Post> findPostsByTag(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);

    // 统计标签下的文章数量
    int countPostsByTag(@Param("tagId") Long tagId);

    // 查询所有未删除的文章（管理员用）
    List<Post> findAllNonDeletedPosts();

    // 查询回收站中的文章（已删除的文章）
    List<Post> findDeletedPosts();

    // 按作者查询回收站中的文章
    List<Post> findDeletedPostsByAuthor(@Param("authorId") Long authorId);

    // 统计回收站中的文章数量
    int countDeletedPosts();
}