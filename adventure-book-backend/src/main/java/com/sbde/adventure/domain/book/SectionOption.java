package com.sbde.adventure.domain.book;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A choice offered at the end of a section; consequence, hint and requires are optional. */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SectionOption {

    @Column(length = 2000)
    private String description;

    private int gotoId;

    @Embedded
    private Consequence consequence;

    @Column(length = 2000)
    private String hint;

    @Column(length = 2000)
    private String requires;

    public SectionOption(String description, int gotoId, Consequence consequence, String hint, String requires) {
        this.description = description;
        this.gotoId = gotoId;
        this.consequence = consequence;
        this.hint = hint;
        this.requires = requires;
    }
}
