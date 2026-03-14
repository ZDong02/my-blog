package com.example.blog.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
    private boolean hasNext;
    private boolean hasPrev;

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
}