package com.sbde.adventure.domain.book;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate root. A book is validated whenever it is built: {@link #isValid()} tells whether it can be played,
 * {@link #getValidationErrors()} why not.
 */
@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @Column(length = 120)
    private String id;

    @Column(nullable = false)
    private String title;

    private String author;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(length = 4000)
    private String description;

    private String estimatedTime;

    private boolean valid;

    private int sectionCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_genre", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "genre")
    private List<String> genres = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_tag", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "book_validation_error", joinColumns = @JoinColumn(name = "book_id"))
    @OrderColumn(name = "position")
    @Column(name = "error", length = 1000)
    private List<String> validationErrors = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "book_id")
    @OrderColumn(name = "position")
    private List<Section> sections = new ArrayList<>();

    /** Use {@link BookFactory}: this constructor expects normalized values and the validation errors of the sections. */
    Book(String id, String title, String author, Difficulty difficulty, String description,
         List<String> genres, List<String> tags, String estimatedTime, List<Section> sections,
         List<String> validationErrors) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.difficulty = difficulty;
        this.description = description;
        this.estimatedTime = estimatedTime;
        this.genres = new ArrayList<>(genres);
        this.tags = new ArrayList<>(tags);
        this.sections = new ArrayList<>(sections);
        this.sectionCount = this.sections.size();
        this.validationErrors = new ArrayList<>(validationErrors);
        this.valid = this.validationErrors.isEmpty();
    }

    public Optional<Section> beginSection() {
        return sections.stream().filter(s -> s.getType() == SectionType.BEGIN).findFirst();
    }

    public Optional<Section> section(int sectionId) {
        return sections.stream().filter(s -> s.getSectionId() == sectionId).findFirst();
    }

    /** Number of sections a player can reach from the beginning; used to compute progression. */
    public int reachableSectionCount() {
        Optional<Section> begin = beginSection();
        if (begin.isEmpty()) {
            return 0;
        }
        Set<Integer> reached = new HashSet<>();
        Deque<Integer> pending = new ArrayDeque<>();
        pending.push(begin.get().getSectionId());
        while (!pending.isEmpty()) {
            int current = pending.pop();
            if (reached.contains(current)) {
                continue;
            }
            Optional<Section> section = section(current);
            if (section.isPresent()) {
                reached.add(current);
                section.get().getOptions().forEach(o -> pending.push(o.getGotoId()));
            }
        }
        return reached.size();
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public List<String> getGenres() {
        return Collections.unmodifiableList(genres);
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public List<String> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }
}
