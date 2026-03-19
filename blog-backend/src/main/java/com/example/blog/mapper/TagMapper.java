package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 按名称查询标签
     */
    Tag selectByName(@Param("name") String name);

    /**
     * 按 slug 查询标签
     */
    Tag selectBySlug(@Param("slug") String slug);

    /**
     * 查询所有标签及文章数量
     */
    List<Tag> findAllWithPostCount();

    /**
     * 查询标签及文章数量
     */
    Tag selectWithPostCount(@Param("id") Long id);

    /**
     * 查询文章的所有标签
     */
    List<Tag> findByPostId(@Param("postId") Long postId);

    /**
     * 按关键词搜索标签
     */
    List<Tag> searchTags(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计搜索结果
     */
    int countSearchTags(@Param("keyword") String keyword);
}
