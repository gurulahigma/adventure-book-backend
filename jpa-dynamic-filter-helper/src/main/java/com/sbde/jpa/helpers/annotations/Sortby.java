package com.sbde.jpa.helpers.annotations;

import com.sbde.jpa.helpers.utils.SortDirection;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Default sort of an {@code AbstractSearchCriteria} subclass; the instance values can still override it.
 * Repeat it (or use {@link Sorts}) to sort on several keys, in declaration order.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Sorts.class)
public @interface Sortby {

    String sortBy();

    SortDirection direction() default SortDirection.ASC;
}
