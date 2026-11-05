package com.sbde.jpa.helpers.annotations;

import com.sbde.jpa.helpers.utils.Operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Turns a field of an {@code AbstractSearchCriteria} subclass into a filter on one or more entity attributes.
 * The {@code FieldType} is inferred from the Java type of the field (or of its elements for collections/arrays).
 * With several {@link #fields()}, an entity matches when any of them matches. Null/blank/empty values are ignored.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterCriteria {

    /** Entity attribute paths to search in; nested paths use dots, e.g. {@code contract.numberContract}. */
    String[] fields();

    /** Supported: EQUAL, NOT_EQUAL, LIKE and IN (IN requires a collection or array field). */
    Operator operator() default Operator.EQUAL;

    /**
     * Ignores case and common Latin accents on string fields, using only standard JPA functions (lower, replace).
     * Not applied when IN targets a collection attribute of the entity, which is matched by exact membership.
     */
    boolean ignoreCase() default false;
}
