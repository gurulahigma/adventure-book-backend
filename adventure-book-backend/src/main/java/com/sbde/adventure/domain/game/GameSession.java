package com.sbde.adventure.domain.game;

import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.BookNotPlayableException;
import com.sbde.adventure.domain.book.Section;
import com.sbde.adventure.domain.book.SectionOption;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Aggregate root: one player's run through a book. The book is referenced by id only. */
@Entity
@Table(name = "game_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

    public static final int MAX_HEALTH = 10;

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String bookId;

    private int maxHealth;

    private int currentSectionId;

    private int health;

    // Always replaced, never mutated in place, so that Hibernate sees the change.
    @Convert(converter = IntegerListConverter.class)
    @Column(length = 10000)
    private List<Integer> history;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Column(length = 2000)
    private String lastConsequenceText;

    @Embedded
    private Checkpoint checkpoint;

    private Instant createdAt;

    private Instant updatedAt;

    public static GameSession start(Book book) {
        if (!book.isValid()) {
            throw new BookNotPlayableException(book.getId(), book.getValidationErrors());
        }
        int beginId = book.beginSection().orElseThrow().getSectionId();
        GameSession session = new GameSession();
        session.id = UUID.randomUUID().toString();
        session.bookId = book.getId();
        session.maxHealth = MAX_HEALTH;
        session.health = MAX_HEALTH;
        session.currentSectionId = beginId;
        session.history = List.of(beginId);
        session.status = GameStatus.IN_PROGRESS;
        session.createdAt = Instant.now();
        session.updatedAt = session.createdAt;
        return session;
    }

    /** Picks the option at {@code optionIndex} of the current section and applies its consequence. */
    public void choose(Book book, int optionIndex) {
        requireInProgress();
        Section current = book.section(currentSectionId)
                .orElseThrow(() -> new InvalidMoveException("Section " + currentSectionId + " no longer exists"));
        if (optionIndex < 0 || optionIndex >= current.getOptions().size()) {
            throw new InvalidMoveException("Section " + currentSectionId + " has no option " + optionIndex);
        }
        SectionOption option = current.getOptions().get(optionIndex);

        lastConsequenceText = null;
        int updatedHealth = health;
        if (option.getConsequence() != null) {
            updatedHealth = option.getConsequence().applyTo(health, maxHealth);
            lastConsequenceText = option.getConsequence().getText();
        }
        updatedAt = Instant.now();

        if (updatedHealth <= 0) {
            health = 0;
            status = GameStatus.LOST;
            return;
        }
        Section next = book.section(option.getGotoId())
                .orElseThrow(() -> new InvalidMoveException("Section " + option.getGotoId() + " does not exist"));
        health = updatedHealth;
        currentSectionId = next.getSectionId();
        List<Integer> trail = new ArrayList<>(history);
        trail.add(next.getSectionId());
        history = List.copyOf(trail);
        if (next.isEnd()) {
            status = GameStatus.WON;
        }
    }

    public void save() {
        requireInProgress();
        updatedAt = Instant.now();
        checkpoint = new Checkpoint(currentSectionId, health, history, updatedAt);
    }

    /** Goes back to the last saved checkpoint, even after the run was lost. */
    public void resume() {
        if (checkpoint == null) {
            throw new InvalidMoveException("Game '" + id + "' has no saved progression");
        }
        currentSectionId = checkpoint.getSectionId();
        health = checkpoint.getHealth();
        history = List.copyOf(checkpoint.getHistory());
        status = GameStatus.IN_PROGRESS;
        lastConsequenceText = null;
        updatedAt = Instant.now();
    }

    public int visitedSectionCount() {
        return (int) history.stream().distinct().count();
    }

    private void requireInProgress() {
        if (status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Game '" + id + "' is over");
        }
    }
}
