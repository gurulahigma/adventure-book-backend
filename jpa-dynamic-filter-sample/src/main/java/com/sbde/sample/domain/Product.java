package com.sbde.sample.domain;

import com.sbde.jpa.helpers.persistence.AbstractBaseEntity;
import com.sbde.jpa.helpers.persistence.Audit;
import com.sbde.jpa.helpers.persistence.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product extends AbstractBaseEntity<Long> implements Auditable {

    private String reference;
    private String name;
    @Column(length = 1000)
    private String description;
    private Double price;
    private Integer stock;
    private Boolean active;
    private Character grade;
    private LocalDate releaseDate;
    private LocalDateTime lastReviewAt;

    @Convert(converter = ProductStatusConverter.class)
    private ProductStatus status;

    @Convert(converter = PriorityConverter.class)
    private Priority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Embedded
    private Supplier supplier;

    @Embedded
    private Audit audit;
}
