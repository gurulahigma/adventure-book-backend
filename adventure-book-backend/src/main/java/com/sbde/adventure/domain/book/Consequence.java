package com.sbde.adventure.domain.book;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consequence {

    @Enumerated(EnumType.STRING)
    @Column(name = "consequence_type")
    private ConsequenceType type;

    @Column(name = "consequence_value")
    private Integer value;

    @Column(name = "consequence_text", length = 2000)
    private String text;

    public Consequence(ConsequenceType type, int value, String text) {
        this.type = type;
        this.value = value;
        this.text = text;
    }

    /** Health after applying this consequence, bounded to [0, maxHealth]. */
    public int applyTo(int health, int maxHealth) {
        int updated = type == ConsequenceType.LOSE_HEALTH ? health - value : health + value;
        return Math.max(0, Math.min(maxHealth, updated));
    }
}
