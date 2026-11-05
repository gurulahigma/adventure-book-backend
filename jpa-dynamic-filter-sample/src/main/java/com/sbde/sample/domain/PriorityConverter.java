package com.sbde.sample.domain;

import com.sbde.jpa.helpers.persistence.converters.EnumSaveFirstLetterConverter;
import jakarta.persistence.Converter;

@Converter
public class PriorityConverter extends EnumSaveFirstLetterConverter<Priority> {

    public PriorityConverter() {
        super(Priority.class);
    }
}
