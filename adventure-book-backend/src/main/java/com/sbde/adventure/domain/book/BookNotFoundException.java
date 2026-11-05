package com.sbde.adventure.domain.book;

import com.sbde.adventure.domain.shared.DomainException;

public class BookNotFoundException extends DomainException {

    public BookNotFoundException(String bookId) {
        super("Book '" + bookId + "' not found");
    }
}
