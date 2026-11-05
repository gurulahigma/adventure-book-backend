package com.sbde.adventure.domain.game;

import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.BookNotFoundException;
import com.sbde.adventure.domain.book.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Application service of the GameSession aggregate. */
@Service
@Transactional
public class GameService {

    private final GameSessionRepository sessions;
    private final BookRepository books;

    public GameService(GameSessionRepository sessions, BookRepository books) {
        this.sessions = sessions;
        this.books = books;
    }

    public GameView start(String bookId) {
        Book book = loadBook(bookId);
        return view(sessions.save(GameSession.start(book)), book);
    }

    @Transactional(readOnly = true)
    public GameView get(String gameId) {
        GameSession session = loadSession(gameId);
        return view(session, loadBook(session.getBookId()));
    }

    public GameView choose(String gameId, int optionIndex) {
        GameSession session = loadSession(gameId);
        Book book = loadBook(session.getBookId());
        session.choose(book, optionIndex);
        return view(sessions.save(session), book);
    }

    public GameView save(String gameId) {
        GameSession session = loadSession(gameId);
        session.save();
        return view(sessions.save(session), loadBook(session.getBookId()));
    }

    public GameView resume(String gameId) {
        GameSession session = loadSession(gameId);
        session.resume();
        return view(sessions.save(session), loadBook(session.getBookId()));
    }

    @Transactional(readOnly = true)
    public List<SavedGame> listSaved() {
        return sessions.findByCheckpointSavedAtIsNotNullOrderByCheckpointSavedAtDesc().stream()
                .flatMap(session -> books.findById(session.getBookId())
                        .map(book -> new SavedGame(session, book.getTitle())).stream())
                .toList();
    }

    private GameView view(GameSession session, Book book) {
        var section = book.section(session.getCurrentSectionId()).orElseThrow();
        int reachable = Math.max(1, book.reachableSectionCount());
        int progress = session.getStatus() == GameStatus.WON ? 100
                : Math.min(100, session.visitedSectionCount() * 100 / reachable);
        return new GameView(session, book.getTitle(), section, progress);
    }

    private Book loadBook(String bookId) {
        return books.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));
    }

    private GameSession loadSession(String gameId) {
        return sessions.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }
}
