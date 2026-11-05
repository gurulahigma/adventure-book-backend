package com.sbde.sample.api;

import com.sbde.sample.domain.Category;

public record CategoryDto(Long id, String code, String name) {

    public static CategoryDto from(Category category) {
        return new CategoryDto(category.getId(), category.getCode(), category.getName());
    }
}
