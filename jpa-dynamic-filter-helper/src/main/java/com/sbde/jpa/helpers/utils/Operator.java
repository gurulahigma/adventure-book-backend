package com.sbde.jpa.helpers.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.PluralAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

import static com.sbde.jpa.helpers.utils.SearchHelpers.getPathFromKey;


public enum Operator {
    EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request, request.getValue());
            Expression<?> key = getPathFromKey(root, request.getKey());
            return cb.and(equalTo(cb, key, value, request.isIgnoreCase()), predicate);
        }
    },

    NOT_EQUAL {
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseValue(request, request.getValue());
            Expression<?> key = getPathFromKey(root, request.getKey());
            return cb.and(cb.not(equalTo(cb, key, value, request.isIgnoreCase())), predicate);
        }
    },

    LIKE {
        @SuppressWarnings("unchecked")
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<String> key = (Expression<String>) getPathFromKey(root, request.getKey());
            String value = request.getValue().toString();
            if (request.isIgnoreCase()) {
                return cb.and(cb.like(TextNormalizer.normalize(cb, key), "%" + TextNormalizer.normalize(value) + "%"), predicate);
            }
            return cb.and(cb.like(cb.upper(key), "%" + value.toUpperCase() + "%"), predicate);
        }
    },

    IN {
        @SuppressWarnings("unchecked")
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Expression<?> path = getPathFromKey(root, request.getKey());
            if (path instanceof Path<?> attribute && attribute.getModel() instanceof PluralAttribute) {
                // Membership needs no join, so books are neither duplicated nor loaded with a truncated collection.
                Predicate[] members = request.getValues().stream()
                        .map(value -> cb.isMember(parseValue(request, value), (Expression<Collection<Object>>) path))
                        .toArray(Predicate[]::new);
                return cb.and(cb.or(members), predicate);
            }
            boolean normalize = request.isIgnoreCase() && request.getFieldType() == FieldType.STRING;
            CriteriaBuilder.In<Object> inClause = cb.in(normalize ? TextNormalizer.normalize(cb, (Expression<String>) path) : path);
            for (Object value : request.getValues()) {
                Object parsed = parseValue(request, value);
                inClause.value(normalize ? TextNormalizer.normalize(parsed.toString()) : parsed);
            }
            return cb.and(inClause, predicate);
        }
    },

    BETWEEN {

        public static final int MAX_YEAR = 1000;

        @SuppressWarnings("unchecked")
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Object value = parseBound(request, request.getValue());
            Object valueTo = parseBound(request, request.getValueTo());
            if (request.getFieldType() == FieldType.DATE) {
                LocalDate startDate = value == null ? LocalDate.EPOCH : (LocalDate) value;
                LocalDate endDate = valueTo == null ? LocalDate.now().plusYears(MAX_YEAR) : (LocalDate) valueTo;
                Expression<LocalDate> key = (Expression<LocalDate>) getPathFromKey(root, request.getKey());
                return cb.and(cb.between(key, startDate, endDate), predicate);
            }

            if (request.getFieldType() == FieldType.DATE_TIME) {
                LocalDateTime startDate = value == null ? LocalDateTime.now().minusYears(MAX_YEAR) : (LocalDateTime) value;
                LocalDateTime endDate = valueTo == null ? LocalDateTime.now().plusYears(MAX_YEAR) : (LocalDateTime) valueTo;
                Expression<LocalDateTime> key = (Expression<LocalDateTime>) getPathFromKey(root, request.getKey());
                return cb.and(cb.between(key, startDate, endDate), predicate);
            }

            if (request.getFieldType() != FieldType.CHAR && request.getFieldType() != FieldType.BOOLEAN) {
                Number start = (Number) value;
                Number end = (Number) valueTo;
                Expression<Number> key = (Expression<Number>) getPathFromKey(root, request.getKey());
                Predicate range = predicate;
                if (start != null) {
                    range = cb.and(cb.ge(key, start), range);
                }
                if (end != null) {
                    range = cb.and(cb.le(key, end), range);
                }
                return range;
            }

            log.error("Can not use between for {} field type.", request.getFieldType());
            return predicate;
        }
    },

    LIKE_ANY {
        // The attribute paths to search are carried by request.getValues(); a match on any of them is enough.
        @SuppressWarnings("unchecked")
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            String pattern = "%" + request.getValue().toString().toUpperCase() + "%";
            Predicate[] likes = request.getValues().stream()
                    .map(key -> cb.like(cb.upper((Expression<String>) getPathFromKey(root, key.toString())), pattern))
                    .toArray(Predicate[]::new);
            return cb.and(cb.or(likes), predicate);
        }
    },

    ANY_OF {
        // Each sub-request is built on its own and a match on any of them is enough.
        public <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate) {
            Predicate[] any = request.getAnyOf().stream()
                    .map(sub -> sub.getOperator().build(root, cb, sub, cb.conjunction()))
                    .toArray(Predicate[]::new);
            return cb.and(cb.or(any), predicate);
        }
    };

    // A missing bound leaves that side of the interval open.
    private static Object parseBound(FilterRequest request, Object bound) {
        return Objects.nonNull(bound) ? request.getFieldType().parse(bound.toString()) : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object parseValue(FilterRequest request, Object raw) {
        String str = raw.toString();
        if (request.getFieldType() == FieldType.ENUM && request.getClazz() != null) {
            return Enum.valueOf(request.getClazz(), str);
        }
        return request.getFieldType().parse(str);
    }

    @SuppressWarnings("unchecked")
    private static Predicate equalTo(CriteriaBuilder cb, Expression<?> key, Object value, boolean ignoreCase) {
        if (ignoreCase && value instanceof String str) {
            return cb.equal(TextNormalizer.normalize(cb, (Expression<String>) key), TextNormalizer.normalize(str));
        }
        return cb.equal(key, value);
    }

    private static final Logger log = LoggerFactory.getLogger(Operator.class);


    public abstract <T> Predicate build(Root<T> root, CriteriaBuilder cb, FilterRequest request, Predicate predicate);

}
