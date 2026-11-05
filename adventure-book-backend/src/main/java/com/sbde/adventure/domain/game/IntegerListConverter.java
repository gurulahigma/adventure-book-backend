package com.sbde.adventure.domain.game;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {

    @Override
    public String convertToDatabaseColumn(List<Integer> values) {
        return values == null ? "" : String.join(",", values.stream().map(String::valueOf).toList());
    }

    @Override
    public List<Integer> convertToEntityAttribute(String column) {
        if (column == null || column.isBlank()) {
            return List.of();
        }
        return Arrays.stream(column.split(",")).map(Integer::valueOf).toList();
    }
}
