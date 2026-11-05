package com.sbde.sample.search;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.annotations.FilterCriteria;
import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.utils.Operator;
import com.sbde.jpa.helpers.utils.SortDirection;
import com.sbde.sample.domain.Priority;
import com.sbde.sample.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Annotation-only configuration. Extending AbstractSearchCriteria adds page, size, sortBy and direction
 * (bound from the query string) and builds the SearchRequest from the annotations.
 * The FieldType of each filter is inferred from the Java type of the field.
 */
@Getter
@Setter
@NoArgsConstructor
@Sortby(sortBy = "name", direction = SortDirection.ASC)
public class ProductQueryParams extends AbstractSearchCriteria {

    // Long -> FieldType.LONG, EQUAL is the default operator
    @FilterCriteria(fields = "id")
    private Long id;

    // LIKE, ignoring case and accents
    @FilterCriteria(fields = "reference", operator = Operator.LIKE, ignoreCase = true)
    private String reference;

    // EQUAL, ignoring case and accents
    @FilterCriteria(fields = "name", ignoreCase = true)
    private String name;

    // Several fields: a product matches when any of them matches
    @FilterCriteria(fields = {"name", "description"}, operator = Operator.LIKE, ignoreCase = true)
    private String search;

    // Path through an association; LIKE without ignoreCase only ignores the case
    @FilterCriteria(fields = "category.name", operator = Operator.LIKE)
    private String categoryName;

    // IN on a String collection, ignoring case
    @FilterCriteria(fields = "category.code", operator = Operator.IN, ignoreCase = true)
    private List<String> categoryCodes;

    // Path through an embedded object
    @FilterCriteria(fields = "supplier.city", operator = Operator.LIKE, ignoreCase = true)
    private String supplierCity;

    // Enum -> FieldType.ENUM (the attribute uses DescribableEnumConverter)
    @FilterCriteria(fields = "status")
    private ProductStatus status;

    @FilterCriteria(fields = "status", operator = Operator.NOT_EQUAL)
    private ProductStatus excludedStatus;

    // IN on an enum collection (the attribute uses EnumSaveFirstLetterConverter)
    @FilterCriteria(fields = "priority", operator = Operator.IN)
    private List<Priority> priorities;

    // Boolean -> BOOLEAN, Integer -> INTEGER, Double -> DOUBLE, Character -> CHAR
    @FilterCriteria(fields = "active")
    private Boolean active;

    @FilterCriteria(fields = "stock")
    private Integer stock;

    @FilterCriteria(fields = "price")
    private Double price;

    @FilterCriteria(fields = "grade")
    private Character grade;

    // LocalDate -> DATE, LocalDateTime -> DATE_TIME
    @FilterCriteria(fields = "releaseDate")
    private LocalDate releaseDate;

    @FilterCriteria(fields = "lastReviewAt")
    private LocalDateTime lastReviewAt;
}
