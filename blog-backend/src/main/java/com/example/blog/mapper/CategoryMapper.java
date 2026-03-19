package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    // 按名称查询
    Category selectByName(@Param("name") String name);

    // 查询所有分类及文章数量
    List<Category> findAllWithPostCount();

    // 查询分类及文章数量
    Category selectWithPostCount(Long id);
}