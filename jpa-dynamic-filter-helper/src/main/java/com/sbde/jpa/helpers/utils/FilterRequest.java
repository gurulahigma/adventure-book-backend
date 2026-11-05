package com.sbde.jpa.helpers.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FilterRequest implements Serializable {

    private static final long serialVersionUID = 6293344849078612450L;

    private String key;

    private Operator operator;

    private FieldType fieldType;

    private transient Object value;

    private transient Object valueTo;

    private transient Collection<?> values;

    @SuppressWarnings("rawtypes")
    private transient Class clazz;

    private boolean ignoreCase;

    private transient List<FilterRequest> anyOf;

}
