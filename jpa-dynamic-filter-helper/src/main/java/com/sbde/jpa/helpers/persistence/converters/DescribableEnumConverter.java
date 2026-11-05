package com.sbde.jpa.helpers.persistence.converters;

import com.sbde.jpa.helpers.DescribableEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.EnumSet;

@Converter
public class DescribableEnumConverter<E extends Enum<E> & DescribableEnum>
        implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    public DescribableEnumConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String convertToDatabaseColumn(E e) {
        return e == null ? null : e.getDescription();
    }

    @Override
    public E convertToEntityAttribute(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }

        return EnumSet.allOf(enumClass).stream()
                .filter(enumValue -> databaseValue.equals(enumValue.getDescription()))
                .findFirst()
                .orElse(null);
    }
}
