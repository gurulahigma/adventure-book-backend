package com.sbde.jpa.helpers.utils;

import jakarta.persistence.criteria.*;

import java.util.Collection;

public class SearchHelpers {
    /**
     * @param key       - The Attribute name as there in Entity class (you can also put complete path if the attribute is inside object)
     * @param operator  - The Operator that will be applied {@code Operator.EQUAL, Operator.NOT_EQUAL, Operator.LIKE, Operator.IN, Operator.BETWEEN}
     * @param fieldType - The type of the Attribute FieldType {@code FieldType.BOOLEAN, FieldType.CHAR,
     *                  FieldType.DATE, FieldType.DATE_TIME, FieldType.DOUBLE, FieldType.LONG , FieldType.INTEGER, FieldType.STRING, FieldType.ENUM}
     * @param value     - The value to find in the Database.
     *                  {@code
     *                  <pre>
     *                    @Entity
     *                    public class Person{
     *                  		private Long id;
     *                  		private Address address;
     *                        @ElementCollection
     *                        List<Document> documents;
     *                    }
     *
     *                    @Embedded
     *                    public class Address{
     *                  		private String city;
     *                  		private String street;
     *                  		private String zipCode;
     *                    }
     *
     *                    @Embedded
     *                    public class Document{
     *                  		DocumentType type;
     *                  		String number;
     *                    }
     *
     *                  	public enum DocumentType{
     *                  		ID_CARD, PASSPORT, DRIVER_LICENSE
     *                    }
     *                  </pre>}
     *                  Example 1:
     *                  {@code var filterRequest = makeFilterRequest("id", Operator.EQUAL, FieldType.LONG, 1L)}
     *                  <p>
     *                  Example 2:
     *                  {@code var filterRequest = makeFilterRequest("address.city", Operator.LIKE, FieldType.STRING, "Lisbon")}
     *                  <p>
     *                  Example 3:
     *                  {@code var filterRequest = makeFilterRequest("documents.type", Operator.EQUAL, FieldType.ENUM, DocumentType.PASSPORT)}
     *
     */
    public static FilterRequest makeFilterRequest(String key, Operator operator, FieldType fieldType,
                                                  Object value) {
        return FilterRequest.builder()
                .key(key)
                .operator(operator)
                .fieldType(fieldType)
                .value(value)
                .build();
    }

    /**
     * @param key       - The Attribute name as there in Entity class (you can also put complete path if the attribute is inside object)
     * @param fieldType - The type of the Attribute FieldType {@code FieldType.BOOLEAN, FieldType.CHAR,
     *                  FieldType.DATE, FieldType.DATE_TIME, FieldType.DOUBLE, FieldType.LONG , FieldType.INTEGER, FieldType.STRING, FieldType.ENUM}
     * @param value     - The begin of the interval value to find in the Database.
     * @param valueTo   - The end of the interval value to find in the Database.
     *                  <pre>
     *                    @Entity
     *                    public class Person{
     *                  		private Long id;
     *                  		private Address address;
     *                  		private LocalDate birthday;
     *                    }
     *
     *
     *                  </pre>
     *                  Example:
     *                  {@code var filterRequest = makeFilterRequest("birthday", FieldType.DATE, "01-01-1990", "12-31-2004")}
     *
     */
    public static FilterRequest makeFilterRequest(String key, FieldType fieldType,
                                                  Object value, Object valueTo) {
        return FilterRequest.builder()
                .key(key)
                .operator(Operator.BETWEEN)
                .fieldType(fieldType)
                .value(value)
                .valueTo(valueTo)
                .build();
    }


    /**
     * @param key      - The Attribute name as there in Entity class (you can also put complete path if the attribute is inside object)
     * @param operator - The Operator that will be applied {@code Operator.EQUAL, Operator.NOT_EQUAL, Operator.LIKE, Operator.IN, Operator.BETWEEN}
     * @param value    - The value to find in the Database.
     *                 <pre>
     *                    @Entity
     *                    public class Person{
     *                 		private Long id;
     *                 		private Address address;
     *                 		private LocalDate birthday;
     *                        @ElementCollection
     *                        List<Document> documents;
     *                    }
     *
     *                 *	@Embedded
     *                 	public class Document{
     *                 		DocumentType type;
     *                 		String number;
     *                    }
     *
     *                 	public enum DocumentType{
     *                 		ID_CARD, PASSPORT, DRIVER_LICENSE
     *                    }
     *                 </pre>
     *                 Example:
     *                 {@code var filterRequest = makeFilterRequest("documents.type", Operator.EQUAL, DocumentType.PASSPORT)}
     */
    public static FilterRequest makeFilterRequest(String key, Operator operator, Object value) {
        return makeFilterRequest(key, operator, value, value.getClass());
    }

    @SuppressWarnings("rawtypes")
    public static FilterRequest makeFilterRequest(String key, Operator operator, Object value, Class clazz) {
        return FilterRequest.builder()
                .key(key)
                .operator(operator)
                .fieldType(FieldType.ENUM)
                .clazz(clazz)
                .value(value)
                .build();
    }


    /**
     * @param key      - The Attribute name as there in Entity class (you can also put complete path if the attribute is inside object)
     * @param operator - The Operator that will be applied {@code Operator.EQUAL, Operator.NOT_EQUAL, Operator.LIKE, Operator.IN, Operator.BETWEEN}
     * @param value    - The value to find in the Database.
     *                 <pre>
     *                    @Entity
     *                    public class Person{
     *                 		private Long id;
     *                 		private Address address;
     *                 		private LocalDate birthday;
     *                        @ElementCollection
     *                        List<Document> documents;
     *                    }
     *
     *                 *	@Embedded
     *                 	public class Document{
     *                 		DocumentType type;
     *                 		String number;
     *                    }
     *
     *                 	public enum DocumentType{
     *                 		ID_CARD, PASSPORT, DRIVER_LICENSE
     *                    }
     *                 </pre>
     *                 Example:
     *                 {@code var filterRequest = makeFilterRequest("documents.type", Operator.EQUAL, FieldType.ENUM, List.of(DocumentType.PASSPORT, DocumentType.ID_CARD))}
     */
    public static <T> FilterRequest makeFilterRequestForList(String key, Operator operator,
                                                             FieldType fieldType, Collection<T> values) {
        return FilterRequest.builder()
                .key(key)
                .operator(operator)
                .fieldType(fieldType)
                .values(values)
                .build();
    }

    /**
     * Enum variant of {@link #makeFilterRequestForList(String, Operator, FieldType, Collection)}; the enum class lets
     * the values be converted back to constants.
     * Example: {@code makeFilterRequestForList("status", Operator.IN, Status.class, List.of(Status.OPEN))}
     */
    public static <E extends Enum<E>> FilterRequest makeFilterRequestForList(String key, Operator operator,
                                                                           Class<E> enumClass, Collection<E> values) {
        return FilterRequest.builder()
                .key(key)
                .operator(operator)
                .fieldType(FieldType.ENUM)
                .clazz(enumClass)
                .values(values.stream().map(Enum::name).toList())
                .build();
    }

    /**
     * Case-insensitive "contains" search on several attributes, matching if any of them contains the value.
     *
     * @param keys  - The Attribute names as there in Entity class (complete paths are supported)
     * @param value - The text to find in any of the attributes.
     *              Example:
     *              {@code var filterRequest = makeFilterRequestLikeAny(List.of("refExtranet", "refVeos"), "EXT-2024")}
     */
    public static FilterRequest makeFilterRequestLikeAny(Collection<String> keys, Object value) {
        return FilterRequest.builder()
                .operator(Operator.LIKE_ANY)
                .fieldType(FieldType.STRING)
                .value(value)
                .values(keys)
                .build();
    }

    public static <T> Expression<?> getPathFromKey(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] levels = key.split("\\.");
            Path<?> path = root.get(levels[0]);
            for (int i = 1; i <= levels.length - 1; i++) {
                path = path.get(levels[i]);
            }
            return path;
        }
        return root.get(key);
    }

    public static <T> Fetch<T, ?> fecthPathFromKey(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] levels = key.split("\\.");
            Fetch<T, ?> fetch = root.fetch(levels[0]);
            for (int i = 1; i <= levels.length - 1; i++) {
                fetch = fetch.fetch(levels[i]);
            }
            return fetch;
        }
        return root.fetch(key);
    }

    public static <T> Join<T, ?> joinPathFromKey(Root<T> root, String key) {
        if (key.contains(".")) {
            String[] levels = key.split("\\.");
            Join<T, ?> join = root.join(levels[0]);
            for (int i = 1; i <= levels.length - 1; i++) {
                join = join.join(levels[i]);
            }
            return join;
        }
        return root.join(key);
    }


}
