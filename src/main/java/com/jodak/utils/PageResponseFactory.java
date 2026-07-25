package com.jodak.utils;

import com.jodak.dtos.common.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Construit une {@link PageResponse} homogène à partir d'une {@link Page} Spring Data.
 */
public final class PageResponseFactory {

    private PageResponseFactory() {
    }

    public static <E, R> PageResponse<R> from(Page<E> page, Function<E, R> mapper) {
        List<R> content = page.getContent().stream().map(mapper).toList();
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}
