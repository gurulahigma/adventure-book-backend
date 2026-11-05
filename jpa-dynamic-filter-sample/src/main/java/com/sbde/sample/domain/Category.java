package com.sbde.sample.domain;

import com.sbde.jpa.helpers.persistence.AbstractBaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category extends AbstractBaseEntity<Long> {

    private String code;
    private String name;

    public Category(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
