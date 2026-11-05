package com.sbde.adventure.domain.book;

import com.sbde.adventure.domain.shared.DomainException;

import java.util.List;

public class BookNotPlayableException extends DomainException {

    private final List<String> errors;

    public BookNotPlayableException(String bookId, List<String> errors) {
        super("Book '" + bookId + "' is invalid and cannot be played");
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
