package com.sbde.adventure.domain.book;

import com.sbde.adventure.domain.shared.DomainException;

public class InvalidBookDataException extends DomainException {

    public InvalidBookDataException(String message) {
        super(message);
    }
}
