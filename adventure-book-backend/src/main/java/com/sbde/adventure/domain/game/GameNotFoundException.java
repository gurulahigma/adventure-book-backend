package com.sbde.adventure.domain.game;

import com.sbde.adventure.domain.shared.DomainException;

public class GameNotFoundException extends DomainException {

    public GameNotFoundException(String gameId) {
        super("Game '" + gameId + "' not found");
    }
}
