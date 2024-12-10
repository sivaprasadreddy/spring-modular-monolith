package com.sivalabs.bookstore.common.models;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

public record PagedResult<T>(
        List<T> data,
        long totalElements,
        int pageNumber,
        int totalPages,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious) {

    public PagedResult(Page<T> page) {
        this(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious());
    }

    public static <S, T> PagedResult<T> of(PagedResult<S> pagedResult, Function<S, T> mapper) {
        return new PagedResult<>(
                pagedResult.data.stream().map(mapper).toList(),
                pagedResult.totalElements,
                pagedResult.pageNumber,
                pagedResult.totalPages,
                pagedResult.isFirst,
                pagedResult.isLast,
                pagedResult.hasNext,
                pagedResult.hasPrevious);
    }
}
