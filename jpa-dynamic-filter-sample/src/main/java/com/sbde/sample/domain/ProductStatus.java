package com.sbde.sample.domain;

import com.sbde.jpa.helpers.DescribableEnum;

public enum ProductStatus implements DescribableEnum {
    DRAFT("Brouillon"),
    PUBLISHED("Publié"),
    ARCHIVED("Archivé");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
