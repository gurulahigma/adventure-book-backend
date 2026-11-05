package com.sbde.jpa.helpers.utils;

import com.sbde.jpa.helpers.annotations.FilterCriteria;
import com.sbde.jpa.helpers.annotations.FilterRange;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Builds the {@link FilterRequest}s declared with {@link FilterCriteria} on the fields of a criteria object. */
public final class AnnotatedFilterResolver {

    private static final Map<Class<?>, List<FilterField>> CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, List<RangeFilter>> RANGE_CACHE = new ConcurrentHashMap<>();

    private record FilterField(Field field, FilterCriteria criteria, FieldType type, Class<?> elementType,
                               boolean multiValued) {
    }

    private record RangeFilter(FilterRange range, Field from, Field to, FieldType type) {
    }

    private AnnotatedFilterResolver() {
    }

    public static List<FilterRequest> resolve(Object criteria) {
        List<FilterRequest> filters = new ArrayList<>();
        for (FilterField filterField : CACHE.computeIfAbsent(criteria.getClass(), AnnotatedFilterResolver::inspect)) {
            List<Object> values = readValues(filterField, criteria);
            if (values.isEmpty()) {
                continue;
            }
            List<FilterRequest> perField = new ArrayList<>();
            for (String key : filterField.criteria().fields()) {
                perField.add(toFilter(filterField, key, values));
            }
            filters.add(perField.size() == 1 ? perField.get(0) : FilterRequest.builder()
                    .operator(Operator.ANY_OF)
                    .fieldType(filterField.type())
                    .anyOf(perField)
                    .build());
        }
        for (RangeFilter rangeFilter : RANGE_CACHE.computeIfAbsent(criteria.getClass(), AnnotatedFilterResolver::inspectRanges)) {
            Object from = readBound(rangeFilter.from(), criteria);
            Object to = readBound(rangeFilter.to(), criteria);
            if (from == null && to == null) {
                continue;
            }
            List<FilterRequest> perField = new ArrayList<>();
            for (String key : rangeFilter.range().fields()) {
                perField.add(FilterRequest.builder()
                        .key(key)
                        .operator(Operator.BETWEEN)
                        .fieldType(rangeFilter.type())
                        .value(from)
                        .valueTo(to)
                        .build());
            }
            filters.add(perField.size() == 1 ? perField.get(0) : FilterRequest.builder()
                    .operator(Operator.ANY_OF)
                    .fieldType(rangeFilter.type())
                    .anyOf(perField)
                    .build());
        }
        return filters;
    }

    private static List<RangeFilter> inspectRanges(Class<?> type) {
        List<RangeFilter> result = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (FilterRange range : current.getDeclaredAnnotationsByType(FilterRange.class)) {
                result.add(toRangeFilter(type, current, range));
            }
        }
        return List.copyOf(result);
    }

    private static RangeFilter toRangeFilter(Class<?> criteriaType, Class<?> declaringClass, FilterRange range) {
        String where = "@FilterRange on " + declaringClass.getName();
        if (range.fields().length == 0 || java.util.Arrays.stream(range.fields()).anyMatch(String::isBlank)) {
            throw new IllegalStateException(where + ": fields must contain at least one non-blank attribute path");
        }
        Field from = findField(criteriaType, range.from(), where);
        Field to = findField(criteriaType, range.to(), where);
        FieldType type = infer(from.getType(), where);
        if (type != infer(to.getType(), where)) {
            throw new IllegalStateException(where + ": from and to must have the same type");
        }
        if (type != FieldType.LONG && type != FieldType.INTEGER && type != FieldType.DOUBLE
                && type != FieldType.DATE && type != FieldType.DATE_TIME) {
            throw new IllegalStateException(where + ": a range requires a number, LocalDate or LocalDateTime field");
        }
        from.setAccessible(true);
        to.setAccessible(true);
        return new RangeFilter(range, from, to, type);
    }

    private static Field findField(Class<?> type, String name, String where) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep looking in the superclass
            }
        }
        throw new IllegalStateException(where + ": no field named '" + name + "' in " + type.getName());
    }

    private static Object readBound(Field field, Object criteria) {
        try {
            return field.get(criteria);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read " + field, e);
        }
    }

    private static FilterRequest toFilter(FilterField filterField, String key, List<Object> values) {
        FilterCriteria criteria = filterField.criteria();
        FilterRequest.FilterRequestBuilder builder = FilterRequest.builder()
                .key(key)
                .operator(criteria.operator())
                .fieldType(filterField.type())
                .ignoreCase(criteria.ignoreCase());
        if (filterField.type() == FieldType.ENUM) {
            builder.clazz(filterField.elementType());
        }
        return criteria.operator() == Operator.IN ? builder.values(values).build() : builder.value(values.get(0)).build();
    }

    private static List<FilterField> inspect(Class<?> type) {
        List<FilterField> result = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                FilterCriteria criteria = field.getAnnotation(FilterCriteria.class);
                if (criteria != null) {
                    result.add(toFilterField(field, criteria));
                }
            }
        }
        return List.copyOf(result);
    }

    private static FilterField toFilterField(Field field, FilterCriteria criteria) {
        String where = "@FilterCriteria on " + field.getDeclaringClass().getName() + "." + field.getName();
        if (criteria.fields().length == 0 || java.util.Arrays.stream(criteria.fields()).anyMatch(String::isBlank)) {
            throw new IllegalStateException(where + ": fields must contain at least one non-blank attribute path");
        }
        Operator operator = criteria.operator();
        if (operator != Operator.EQUAL && operator != Operator.NOT_EQUAL && operator != Operator.LIKE
                && operator != Operator.IN) {
            throw new IllegalStateException(where + ": unsupported operator " + operator);
        }

        Class<?> raw = field.getType();
        boolean multiValued = raw.isArray() || Collection.class.isAssignableFrom(raw);
        Class<?> elementType = elementTypeOf(field, where);
        if (multiValued && operator != Operator.IN) {
            throw new IllegalStateException(where + ": collection and array fields require operator IN");
        }

        FieldType type = infer(elementType, where);
        if (operator == Operator.LIKE && type != FieldType.STRING) {
            throw new IllegalStateException(where + ": operator LIKE requires a String field");
        }
        field.setAccessible(true);
        return new FilterField(field, criteria, type, elementType, multiValued);
    }

    private static Class<?> elementTypeOf(Field field, String where) {
        Class<?> raw = field.getType();
        if (raw.isArray()) {
            return raw.getComponentType();
        }
        if (Collection.class.isAssignableFrom(raw)) {
            if (field.getGenericType() instanceof ParameterizedType parameterized
                    && parameterized.getActualTypeArguments()[0] instanceof Class<?> element) {
                return element;
            }
            throw new IllegalStateException(where + ": the collection element type must be a concrete class");
        }
        return raw;
    }

    // The FieldType is only guessed from the Java type; it must be one of the FieldType values.
    private static FieldType infer(Class<?> type, String where) {
        if (type == String.class) return FieldType.STRING;
        if (type == Long.class || type == long.class) return FieldType.LONG;
        if (type == Integer.class || type == int.class) return FieldType.INTEGER;
        if (type == Double.class || type == double.class) return FieldType.DOUBLE;
        if (type == Boolean.class || type == boolean.class) return FieldType.BOOLEAN;
        if (type == Character.class || type == char.class) return FieldType.CHAR;
        if (type == LocalDate.class) return FieldType.DATE;
        if (type == LocalDateTime.class) return FieldType.DATE_TIME;
        if (type.isEnum()) return FieldType.ENUM;
        throw new IllegalStateException(where + ": cannot infer a FieldType from " + type.getName());
    }

    private static List<Object> readValues(FilterField filterField, Object criteria) {
        Object raw;
        try {
            raw = filterField.field().get(criteria);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read " + filterField.field(), e);
        }
        List<Object> values = new ArrayList<>();
        if (raw == null) {
            return values;
        }
        if (raw instanceof Collection<?> collection) {
            collection.forEach(item -> addValue(values, item));
        } else if (raw.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(raw); i++) {
                addValue(values, Array.get(raw, i));
            }
        } else {
            addValue(values, raw);
        }
        return values;
    }

    private static void addValue(List<Object> values, Object item) {
        if (Objects.isNull(item) || (item instanceof String str && str.isBlank())) {
            return;
        }
        values.add(item instanceof Enum<?> enumValue ? enumValue.name() : item);
    }
}
