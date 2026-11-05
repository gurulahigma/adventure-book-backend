package com.sbde.adventure.domain.game;

import com.sbde.adventure.domain.shared.DomainException;

public class InvalidMoveException extends DomainException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
