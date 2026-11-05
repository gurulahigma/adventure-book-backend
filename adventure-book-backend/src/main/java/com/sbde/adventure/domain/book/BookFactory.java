package com.sbde.adventure.domain.book;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Builds {@link Book}s from raw input: checks the title, derives the id, fills in defaults and validates the sections. */
public final class BookFactory {

    private static final String SECTION = "Section ";

    private BookFactory() {
    }

    /**
     * @param idHint id of the book, or null to derive it from the title; either way it is turned into a slug
     * @throws InvalidBookDataException when there is no title or no id can be derived
     */
    public static Book create(String idHint, String title, String author, Difficulty difficulty, String description,
                              List<String> genres, List<String> tags, String estimatedTime, List<Section> sections) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookDataException("A book needs a title");
        }
        List<Section> bookSections = sections == null ? List.of() : sections;
        return new Book(slugify(idHint == null || idHint.isBlank() ? title : idHint), title.trim(),
                author == null ? "" : author.trim(), difficulty == null ? Difficulty.MEDIUM : difficulty,
                description, genres == null ? List.of() : genres, tags == null ? List.of() : tags, estimatedTime,
                bookSections, findErrors(bookSections));
    }

    /** Structural rules that make a book playable; an empty result means the book is valid. */
    private static List<String> findErrors(List<Section> sections) {
        List<String> errors = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        sections.stream().filter(s -> !ids.add(s.getSectionId()))
                .forEach(s -> errors.add("Duplicate section id " + s.getSectionId()));

        checkBeginningAndEnding(sections, errors);
        sections.stream().filter(s -> !s.isEnd()).forEach(s -> checkOptions(s, ids, errors));
        return errors;
    }

    private static void checkBeginningAndEnding(List<Section> sections, List<String> errors) {
        long beginnings = sections.stream().filter(s -> s.getType() == SectionType.BEGIN).count();
        if (beginnings == 0) {
            errors.add("Book has no beginning section");
        } else if (beginnings > 1) {
            errors.add("Book has more than one beginning section");
        }
        if (sections.stream().noneMatch(Section::isEnd)) {
            errors.add("Book has no ending section");
        }
    }

    private static void checkOptions(Section section, Set<Integer> ids, List<String> errors) {
        if (section.getOptions().isEmpty()) {
            errors.add(SECTION + section.getSectionId() + " is not an ending and has no options");
            return;
        }
        for (SectionOption option : section.getOptions()) {
            if (!ids.contains(option.getGotoId())) {
                errors.add(SECTION + section.getSectionId() + " points to unknown section " + option.getGotoId());
            }
            if (option.getConsequence() != null && option.getConsequence().getValue() < 0) {
                errors.add(SECTION + section.getSectionId() + " has a negative consequence value");
            }
        }
    }

    private static String slugify(String value) {
        String slug = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            throw new InvalidBookDataException("Cannot derive an id from '" + value + "'");
        }
        return slug;
    }
}
