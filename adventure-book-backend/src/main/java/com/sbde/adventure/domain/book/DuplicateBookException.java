package com.sbde.adventure.domain.book;

import com.sbde.adventure.domain.shared.DomainException;

public class DuplicateBookException extends DomainException {

    public DuplicateBookException(String bookId) {
        super("A book with id '" + bookId + "' already exists");
    }
}
