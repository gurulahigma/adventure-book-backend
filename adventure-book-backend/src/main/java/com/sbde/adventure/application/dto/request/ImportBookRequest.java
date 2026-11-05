package com.sbde.adventure.application.dto.request;

import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.BookFactory;
import com.sbde.adventure.domain.book.Consequence;
import com.sbde.adventure.domain.book.ConsequenceType;
import com.sbde.adventure.domain.book.Difficulty;
import com.sbde.adventure.domain.book.InvalidBookDataException;
import com.sbde.adventure.domain.book.Section;
import com.sbde.adventure.domain.book.SectionOption;
import com.sbde.adventure.domain.book.SectionType;

import java.util.List;

/** Book as described by the JSON book files: id, genres, tags and the like are optional. */
public record ImportBookRequest(String id, String title, String author, Difficulty difficulty, String description,
                                List<String> genres, List<String> tags, String estimatedTime,
                                List<SectionData> sections) {

    public record SectionData(Integer id, String text, SectionType type, List<OptionData> options) {
    }

    public record OptionData(String description, Integer gotoId, ConsequenceData consequence, String hint,
                             String requires) {
    }

    public record ConsequenceData(ConsequenceType type, Integer value, String text) {
    }

    /** @param fallbackId id used when the request has none (the seeder passes the file name); the title otherwise */
    public Book toDomain(String fallbackId) {
        List<Section> domainSections = sections == null ? List.of() : sections.stream().map(this::toSection).toList();
        return BookFactory.create(id != null && !id.isBlank() ? id : fallbackId, title, author, difficulty, description,
                genres, tags, estimatedTime, domainSections);
    }

    private Section toSection(SectionData section) {
        if (section == null || section.id() == null || section.type() == null) {
            throw new InvalidBookDataException("Every section needs an id and a type");
        }
        List<SectionOption> options = section.options() == null ? List.of()
                : section.options().stream().map(this::toOption).toList();
        return new Section(section.id(), section.text(), section.type(), options);
    }

    private SectionOption toOption(OptionData option) {
        if (option == null || option.gotoId() == null) {
            throw new InvalidBookDataException("Every option needs a gotoId");
        }
        ConsequenceData data = option.consequence();
        Consequence consequence = null;
        if (data != null) {
            if (data.type() == null || data.value() == null) {
                throw new InvalidBookDataException("A consequence needs a type and a value");
            }
            consequence = new Consequence(data.type(), data.value(), data.text());
        }
        return new SectionOption(option.description(), option.gotoId(), consequence, option.hint(), option.requires());
    }
}
