package com.sbde.sample.api;

import com.sbde.sample.domain.Priority;
import com.sbde.sample.domain.Product;
import com.sbde.sample.domain.ProductStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductDto(Long id, String reference, String name, String description, Double price, Integer stock,
                         Boolean active, Character grade, LocalDate releaseDate, LocalDateTime lastReviewAt,
                         ProductStatus status, String statusLabel, Priority priority, String categoryName,
                         String supplierCity, String createdBy, LocalDateTime createdOn) {

    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getReference(), product.getName(), product.getDescription(),
                product.getPrice(), product.getStock(), product.getActive(), product.getGrade(),
                product.getReleaseDate(), product.getLastReviewAt(), product.getStatus(),
                product.getStatus() == null ? null : product.getStatus().getDescription(), product.getPriority(),
                product.getCategory() == null ? null : product.getCategory().getName(),
                product.getSupplier() == null ? null : product.getSupplier().getCity(),
                product.getAudit() == null ? null : product.getAudit().getCreatedBy(),
                product.getAudit() == null ? null : product.getAudit().getCreatedOn());
    }
}
