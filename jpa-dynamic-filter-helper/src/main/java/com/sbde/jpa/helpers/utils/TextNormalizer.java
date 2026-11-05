package com.sbde.jpa.helpers.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Case and accent folding that only relies on standard JPA Criteria features (lower, replace), so it works with
 * any JPA provider and database. The same table is applied to the column and to the searched value to keep both
 * sides consistent.
 */
final class TextNormalizer {

    private static final Map<String, String> FOLDS = new LinkedHashMap<>();

    static {
        fold("àáâãäå", "a");
        fold("ç", "c");
        fold("èéêë", "e");
        fold("ìíîï", "i");
        fold("ñ", "n");
        fold("òóôõö", "o");
        fold("ùúûü", "u");
        fold("ýÿ", "y");
        FOLDS.put("æ", "ae");
        FOLDS.put("œ", "oe");
        FOLDS.put("ß", "ss");
    }

    private TextNormalizer() {
    }

    private static void fold(String accented, String base) {
        accented.chars().forEach(c -> FOLDS.put(String.valueOf((char) c), base));
    }

    static Expression<String> normalize(CriteriaBuilder cb, Expression<String> expression) {
        Expression<String> result = cb.lower(expression);
        for (Map.Entry<String, String> fold : FOLDS.entrySet()) {
            result = cb.function("replace", String.class, result, cb.literal(fold.getKey()), cb.literal(fold.getValue()));
        }
        return result;
    }

    static String normalize(String value) {
        String result = Normalizer.normalize(value, Normalizer.Form.NFC).toLowerCase(Locale.ROOT);
        for (Map.Entry<String, String> fold : FOLDS.entrySet()) {
            result = result.replace(fold.getKey(), fold.getValue());
        }
        return result;
    }
}
