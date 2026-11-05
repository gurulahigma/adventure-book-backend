package com.sbde.adventure.domain.book;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "book_section")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    /** Section id inside its book, as written in the book file. */
    private int sectionId;

    @Column(length = 10000)
    private String text;

    @Enumerated(EnumType.STRING)
    private SectionType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "section_option", joinColumns = @JoinColumn(name = "section_pk"))
    @OrderColumn(name = "position")
    private List<SectionOption> options = new ArrayList<>();

    public Section(int sectionId, String text, SectionType type, List<SectionOption> options) {
        this.sectionId = sectionId;
        this.text = text == null ? "" : text;
        this.type = type;
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
    }

    public boolean isEnd() {
        return type == SectionType.END;
    }

    public List<SectionOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
