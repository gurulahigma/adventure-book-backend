package com.sbde.jpa.helpers;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class InternalPage<T> {
    private Collection<T> content;
    private int page;
    private int pageSize;
    private long totalElements;
    private long totalPages;
    private long numberOfElements;


    private InternalPage(Collection<T> content, int page, int pageSize, long totalElements, long totalPages,
                         long numberOfElements) {
        this.content = content;
        this.page = page;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.numberOfElements = numberOfElements;
    }

    public static <T> InternalPage<T> emptyPage() {
        return new InternalPage<>(Collections.emptyList());
    }

    public static <T> InternalPage<T> convertToInternalPage(org.springframework.data.domain.Page<T> data) {
        Pageable page = data.getPageable();
        return page.isUnpaged() ? new InternalPage<>(data.getContent()) : new InternalPage<>(data.getContent(), page.getPageNumber(),
                page.getPageSize(), data.getTotalElements(), data.getTotalPages(), data.getNumberOfElements());
    }

    public static <T> InternalPage<T> combineToInternalPage(org.springframework.data.domain.Page<T> data1, org.springframework.data.domain.Page<T> data2) {
        Pageable page1 = data1.getPageable();
        Pageable page2 = data2.getPageable();
        Set<T> content = data1.and(data2).stream()
                .collect(Collectors.toSet());
        int totalPages = (data1.getTotalPages() >= data2.getTotalPages()) ? data1.getTotalPages() : data2.getTotalPages();
        return new InternalPage<>(content, page1.getPageNumber(),
                page1.getPageSize() + page2.getPageSize(), data1.getTotalElements() + data2.getTotalElements(), totalPages, data1.getNumberOfElements() + data2.getNumberOfElements());
    }

    @SuppressWarnings("unchecked")
    public <U> InternalPage<U> map(Function<? super T, ? extends U> converter) {
        Assert.notNull(converter, "Function must not be null!");
        List<U> content = (List<U>) getConvertedContent(converter);

        return new InternalPage<>(content, page,
                pageSize, totalElements, totalPages, numberOfElements);
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    protected <U> List<U> getConvertedContent(Function<? super T, U> converter) {

        Assert.notNull(converter, "Function must not be null!");

        return content.stream().map(converter::apply)
                .collect(Collectors.toList());
    }

    public InternalPage(Collection<T> contents) {
        this.content = contents;
        this.totalElements = contents.size();
        this.numberOfElements = contents.size();
    }
}
