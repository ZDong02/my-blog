package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 分页结果 DTO
 *
 * @param <T> 数据类型
 * @author Blog Team
 * @date 2026-03-18
 */
@Data
@Builder
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 是否有上一页
     */
    private boolean hasPrev;

    /**
     * 构建分页结果
     *
     * @param list  数据列表
     * @param total 总记录数
     * @param page  当前页码
     * @param size  每页大小
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        boolean hasNext = (long) page * size < total;
        boolean hasPrev = page > 1;

        return PageResult.<T>builder()
                .list(list)
                .total(total)
                .page(page)
                .size(size)
                .hasNext(hasNext)
                .hasPrev(hasPrev)
                .build();
    }

    /**
     * 前端兼容字段：Spring Data 风格 content
     */
    public List<T> getContent() {
        return list;
    }

    /**
     * 前端兼容字段：Spring Data 风格 number
     */
    public int getNumber() {
        return page;
    }
}
