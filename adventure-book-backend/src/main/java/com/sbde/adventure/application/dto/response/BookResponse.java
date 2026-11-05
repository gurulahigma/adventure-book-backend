package com.sbde.adventure.application.dto.response;

import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.Difficulty;

import java.util.List;

public record BookResponse(String id, String title, String author, Difficulty difficulty, String description,
                           List<String> genres, List<String> tags, String estimatedTime, int sectionCount,
                           boolean valid, List<String> validationErrors) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getDifficulty(),
                book.getDescription(), book.getGenres(), book.getTags(), book.getEstimatedTime(),
                book.getSectionCount(), book.isValid(), book.getValidationErrors());
    }
}
