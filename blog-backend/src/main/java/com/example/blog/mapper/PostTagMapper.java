package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostTagMapper extends BaseMapper<PostTag> {

    /**
     * 批量插入文章标签关联
     */
    int batchInsert(@Param("postTags") List<PostTag> postTags);

    /**
     * 根据文章 ID 删除关联
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 根据标签 ID 删除关联
     */
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 统计标签使用次数
     */
    int countByTagId(@Param("tagId") Long tagId);
}
