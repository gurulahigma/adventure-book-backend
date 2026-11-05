package com.sbde.jpa.helpers.persistence.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.EnumSet;

@Converter
public class EnumSaveFirstLetterConverter<E extends Enum<E>> implements AttributeConverter<E, Character> {

    private final Class<E> enumClass;

    @Override
    public Character convertToDatabaseColumn(E e) {
        return e == null ? null : e.name().charAt(0);
    }

    @Override
    public E convertToEntityAttribute(Character databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        for (E enumValue : EnumSet.allOf(enumClass)) {
            if (databaseValue.charValue() == enumValue.name().charAt(0)) {
                return enumValue;
            }
        }
        return null;
    }

    public EnumSaveFirstLetterConverter(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

}
