package com.sbde.adventure.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/** Progression captured by an explicit save, restored on resume. */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checkpoint {

    @Column(name = "checkpoint_section_id")
    private Integer sectionId;

    @Column(name = "checkpoint_health")
    private Integer health;

    @Convert(converter = IntegerListConverter.class)
    @Column(name = "checkpoint_history", length = 10000)
    private List<Integer> history;

    @Column(name = "saved_at")
    private Instant savedAt;

    public Checkpoint(int sectionId, int health, List<Integer> history, Instant savedAt) {
        this.sectionId = sectionId;
        this.health = health;
        this.history = List.copyOf(history);
        this.savedAt = savedAt;
    }
}
