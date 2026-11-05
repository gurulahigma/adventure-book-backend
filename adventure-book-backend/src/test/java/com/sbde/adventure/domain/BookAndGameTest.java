package com.sbde.adventure.domain;

import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.BookFactory;
import com.sbde.adventure.domain.book.BookNotPlayableException;
import com.sbde.adventure.domain.book.Consequence;
import com.sbde.adventure.domain.book.ConsequenceType;
import com.sbde.adventure.domain.book.Difficulty;
import com.sbde.adventure.domain.book.Section;
import com.sbde.adventure.domain.book.SectionOption;
import com.sbde.adventure.domain.book.SectionType;
import com.sbde.adventure.domain.game.GameSession;
import com.sbde.adventure.domain.game.GameStatus;
import com.sbde.adventure.domain.game.InvalidMoveException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookAndGameTest {

    private static SectionOption go(int to) {
        return new SectionOption("go", to, null, null, null);
    }

    private static SectionOption go(int to, ConsequenceType type, int value) {
        return new SectionOption("go", to, new Consequence(type, value, "consequence"), null, null);
    }

    private static Book book(Section... sections) {
        return BookFactory.create(null, "A Book", "Author", Difficulty.EASY, null, null, null, null, List.of(sections));
    }

    private static Section section(int id, SectionType type, SectionOption... options) {
        return new Section(id, "text", type, List.of(options));
    }

    private static Book validBook() {
        return book(
                section(1, SectionType.BEGIN, go(2, ConsequenceType.LOSE_HEALTH, 4), go(3, ConsequenceType.LOSE_HEALTH, 10)),
                section(2, SectionType.NODE, go(4, ConsequenceType.GAIN_HEALTH, 9)),
                section(3, SectionType.NODE, go(4)),
                section(4, SectionType.END));
    }

    @Test
    void derivesTheIdFromTheTitleAndValidatesTheStructure() {
        Book valid = validBook();
        assertThat(valid.getId()).isEqualTo("a-book");
        assertThat(valid.isValid()).isTrue();
        assertThat(valid.getSectionCount()).isEqualTo(4);
        assertThat(valid.reachableSectionCount()).isEqualTo(4);

        assertThat(book(section(1, SectionType.NODE, go(1))).getValidationErrors())
                .anyMatch(e -> e.contains("no beginning")).anyMatch(e -> e.contains("no ending"));
        assertThat(book(section(1, SectionType.BEGIN, go(2)), section(2, SectionType.BEGIN),
                section(3, SectionType.END)).getValidationErrors())
                .anyMatch(e -> e.contains("more than one beginning"))
                .anyMatch(e -> e.contains("Section 2 is not an ending"));
        assertThat(book(section(1, SectionType.BEGIN, go(9)), section(2, SectionType.END)).getValidationErrors())
                .containsExactly("Section 1 points to unknown section 9");
    }

    @Test
    void appliesConsequencesAndCapsHealth() {
        Book book = validBook();
        GameSession game = GameSession.start(book);
        game.choose(book, 0);
        assertThat(game.getHealth()).isEqualTo(6);
        game.choose(book, 0);
        assertThat(game.getHealth()).isEqualTo(GameSession.MAX_HEALTH);
        assertThat(game.getStatus()).isEqualTo(GameStatus.WON);
        assertThat(game.getHistory()).containsExactly(1, 2, 4);
        assertThatThrownBy(() -> game.choose(book, 0)).isInstanceOf(InvalidMoveException.class);
    }

    @Test
    void diesWhenHealthReachesZeroAndCanResumeFromTheCheckpoint() {
        Book book = validBook();
        GameSession game = GameSession.start(book);
        game.save();
        game.choose(book, 1);
        assertThat(game.getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(game.getHealth()).isZero();
        assertThat(game.getCurrentSectionId()).isEqualTo(1);

        game.resume();
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getHealth()).isEqualTo(GameSession.MAX_HEALTH);
    }

    @Test
    void refusesToStartAnInvalidBookOrAnUnknownOption() {
        assertThatThrownBy(() -> GameSession.start(book(section(1, SectionType.BEGIN))))
                .isInstanceOf(BookNotPlayableException.class);
        Book book = validBook();
        GameSession game = GameSession.start(book);
        assertThatThrownBy(() -> game.choose(book, 5)).isInstanceOf(InvalidMoveException.class);
        assertThatThrownBy(game::resume).isInstanceOf(InvalidMoveException.class);
    }
}
