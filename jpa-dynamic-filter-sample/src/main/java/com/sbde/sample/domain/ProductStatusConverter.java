package com.sbde.sample.domain;

import com.sbde.jpa.helpers.persistence.converters.DescribableEnumConverter;
import jakarta.persistence.Converter;

/** JPA needs a no-arg converter, so the generic library converter is bound to its enum here. */
@Converter
public class ProductStatusConverter extends DescribableEnumConverter<ProductStatus> {

    public ProductStatusConverter() {
        super(ProductStatus.class);
    }
}
