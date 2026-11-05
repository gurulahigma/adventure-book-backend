package com.sbde.adventure.domain.game;

import com.sbde.adventure.domain.book.Section;

/** A game session together with what a player needs to see of its book. */
public record GameView(GameSession session, String bookTitle, Section currentSection, int progressPercent) {
}
