package com.sbde.adventure.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sbde.adventure.domain.book.Consequence;
import com.sbde.adventure.domain.book.Section;
import com.sbde.adventure.domain.book.SectionOption;
import com.sbde.adventure.domain.book.SectionType;
import com.sbde.adventure.domain.game.Checkpoint;
import com.sbde.adventure.domain.game.GameSession;
import com.sbde.adventure.domain.game.GameStatus;
import com.sbde.adventure.domain.game.GameView;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/** Never exposes the target of an option: the server alone decides where a choice leads. */
public record GameResponse(String id, String bookId, String bookTitle, SectionResponse currentSection,
                           int healthPoints, int maxHealth, List<Integer> history, GameStatus status,
                           @JsonProperty("isOver") boolean isOver, String outcome, String lastConsequenceText,
                           int progressPercent, Instant savedAt) {

    public record ConsequenceResponse(String type, int value, String text) {
    }

    public record OptionResponse(int index, String description, String hint, String requires,
                                 ConsequenceResponse consequence) {
    }

    public record SectionResponse(int id, String text, SectionType type, List<OptionResponse> options) {
    }

    public static GameResponse from(GameView view) {
        GameSession session = view.session();
        Checkpoint checkpoint = session.getCheckpoint();
        String outcome = switch (session.getStatus()) {
            case WON -> "WIN";
            case LOST -> "LOSE";
            case IN_PROGRESS -> null;
        };
        return new GameResponse(session.getId(), session.getBookId(), view.bookTitle(),
                toResponse(view.currentSection()), session.getHealth(), session.getMaxHealth(), session.getHistory(),
                session.getStatus(), session.getStatus() != GameStatus.IN_PROGRESS, outcome,
                session.getLastConsequenceText(), view.progressPercent(),
                checkpoint == null ? null : checkpoint.getSavedAt());
    }

    private static SectionResponse toResponse(Section section) {
        List<SectionOption> options = section.getOptions();
        return new SectionResponse(section.getSectionId(), section.getText(), section.getType(),
                IntStream.range(0, options.size()).mapToObj(i -> toResponse(i, options.get(i))).toList());
    }

    private static OptionResponse toResponse(int index, SectionOption option) {
        Consequence consequence = option.getConsequence();
        return new OptionResponse(index, option.getDescription(), option.getHint(), option.getRequires(),
                consequence == null ? null
                        : new ConsequenceResponse(consequence.getType().name(), consequence.getValue(),
                        consequence.getText()));
    }
}
