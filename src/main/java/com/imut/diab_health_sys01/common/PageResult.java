package com.imut.diab_health_sys01.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应：{ list, total, page, pageSize }
 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;

    private long total;

    private int page;

    private int pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }
}
