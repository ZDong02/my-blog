package com.example.blog.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 分页工具类
 * 提供编程方式的分页功能，避免 MyBatis-Plus 配置问题
 */
public class PageUtils {

    /**
     * 创建分页对象
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param <T> 实体类型
     * @return 分页对象
     */
    public static <T> Page<T> createPage(int pageNum, int pageSize) {
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 创建分页对象（带排序）
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param orderBy 排序字段
     * @param isAsc 是否升序
     * @param <T> 实体类型
     * @return 分页对象
     */
    public static <T> Page<T> createPage(int pageNum, int pageSize, String orderBy, boolean isAsc) {
        Page<T> page = new Page<>(pageNum, pageSize);

        if (isAsc) {
            page.addOrder(OrderItem.asc(orderBy));
        } else {
            page.addOrder(OrderItem.desc(orderBy));
        }

        return page;
    }

    /**
     * 创建分页对象（多字段排序）
     *
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param orderItems 排序项列表
     * @param <T> 实体类型
     * @return 分页对象
     */
    public static <T> Page<T> createPage(int pageNum, int pageSize, List<OrderItem> orderItems) {
        Page<T> page = new Page<>(pageNum, pageSize);
        page.setOrders(orderItems);
        return page;
    }

    /**
     * 设置默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 1000;

    /**
     * 验证并修正分页参数
     *
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 修正后的分页参数数组 [pageNum, pageSize]
     */
    public static int[] validatePageParams(Integer pageNum, Integer pageSize) {
        int validatedPageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int validatedPageSize = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;

        // 限制最大分页大小
        if (validatedPageSize > MAX_PAGE_SIZE) {
            validatedPageSize = MAX_PAGE_SIZE;
        }

        return new int[]{validatedPageNum, validatedPageSize};
    }
}