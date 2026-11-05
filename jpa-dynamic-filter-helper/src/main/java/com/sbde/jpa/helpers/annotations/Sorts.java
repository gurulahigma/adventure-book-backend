package com.sbde.jpa.helpers.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Several default sorts, applied in order: {@code @Sorts({@Sortby(sortBy = "a"), @Sortby(sortBy = "b")})}. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sorts {

    Sortby[] value();
}
