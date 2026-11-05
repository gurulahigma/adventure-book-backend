package com.sbde.adventure.application.dto.response;

import com.sbde.adventure.domain.game.Checkpoint;
import com.sbde.adventure.domain.game.GameSession;
import com.sbde.adventure.domain.game.SavedGame;

import java.time.Instant;

public record SavedGameResponse(String gameId, String bookId, String bookTitle, Instant savedAt, int healthPoints,
                                int sectionId, int visitedSections) {

    public static SavedGameResponse from(SavedGame saved) {
        GameSession session = saved.session();
        Checkpoint checkpoint = session.getCheckpoint();
        return new SavedGameResponse(session.getId(), session.getBookId(), saved.bookTitle(),
                checkpoint.getSavedAt(), checkpoint.getHealth(), checkpoint.getSectionId(),
                (int) checkpoint.getHistory().stream().distinct().count());
    }
}
