package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.PageUtils;

import java.util.List;

/**
 * 基础 Service 类
 * 提供通用的分页功能
 */
public abstract class BaseService<M extends BaseMapper<T>, T> {

    protected M mapper;

    public BaseService(M mapper) {
        this.mapper = mapper;
    }

    /**
     * 分页查询
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    public IPage<T> selectPage(Integer pageNum, Integer pageSize) {
        int[] validatedParams = PageUtils.validatePageParams(pageNum, pageSize);
        Page<T> page = PageUtils.createPage(validatedParams[0], validatedParams[1]);
        return mapper.selectPage(page, new QueryWrapper<>());
    }

    /**
     * 分页查询（带条件）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @return 分页结果
     */
    public IPage<T> selectPage(Integer pageNum, Integer pageSize, QueryWrapper<T> queryWrapper) {
        int[] validatedParams = PageUtils.validatePageParams(pageNum, pageSize);
        Page<T> page = PageUtils.createPage(validatedParams[0], validatedParams[1]);
        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 分页查询（带排序）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @return 分页结果
     */
    public IPage<T> selectPage(Integer pageNum, Integer pageSize, String orderBy, boolean isAsc) {
        int[] validatedParams = PageUtils.validatePageParams(pageNum, pageSize);
        Page<T> page = PageUtils.createPage(validatedParams[0], validatedParams[1], orderBy, isAsc);
        return mapper.selectPage(page, new QueryWrapper<>());
    }

    /**
     * 分页查询（带条件和排序）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @return 分页结果
     */
    public IPage<T> selectPage(Integer pageNum, Integer pageSize, QueryWrapper<T> queryWrapper, String orderBy, boolean isAsc) {
        int[] validatedParams = PageUtils.validatePageParams(pageNum, pageSize);
        Page<T> page = PageUtils.createPage(validatedParams[0], validatedParams[1], orderBy, isAsc);
        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 查询所有记录（不分页）
     *
     * @return 所有记录列表
     */
    public List<T> selectAll() {
        return mapper.selectList(new QueryWrapper<>());
    }

    /**
     * 条件查询所有记录
     *
     * @param queryWrapper 查询条件
     * @return 符合条件的记录列表
     */
    public List<T> selectList(QueryWrapper<T> queryWrapper) {
        return mapper.selectList(queryWrapper);
    }
}