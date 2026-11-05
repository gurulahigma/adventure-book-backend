package com.sbde.jpa.helpers.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filters one or more entity attributes between two values read from fields of the annotated
 * {@code AbstractSearchCriteria} subclass (BETWEEN operator). The type (number, {@code LocalDate} or
 * {@code LocalDateTime}) is inferred from the {@link #from()} and {@link #to()} fields, which must have the same type.
 * A null bound leaves that side open; when both are null the filter is ignored. With several {@link #fields()},
 * an entity matches when any of them matches.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(FilterRanges.class)
public @interface FilterRange {

    /** Entity attribute paths to search in; nested paths use dots, e.g. {@code contract.startDate}. */
    String[] fields();

    /** Name of the criteria class field holding the lower bound. */
    String from();

    /** Name of the criteria class field holding the upper bound. */
    String to();
}
