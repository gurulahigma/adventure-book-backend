package com.sbde.adventure.application.rest;

import com.sbde.adventure.application.dto.request.ImportBookRequest;
import com.sbde.adventure.application.dto.response.BookResponse;
import com.sbde.adventure.domain.book.BookSearchCriteria;
import com.sbde.adventure.domain.book.BookService;
import com.sbde.jpa.helpers.InternalPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService books;

    public BookController(BookService books) {
        this.books = books;
    }

    /** Query parameters bind to the criteria: search, difficulties, genres, valid, page, size, sortBy, direction. */
    @GetMapping
    public InternalPage<BookResponse> search(BookSearchCriteria criteria) {
        return books.search(criteria).map(BookResponse::from);
    }

    @GetMapping("/genres")
    public List<String> genres() {
        return books.genres();
    }

    @GetMapping("/{id}")
    public BookResponse get(@PathVariable String id) {
        return BookResponse.from(books.get(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@RequestBody ImportBookRequest request) {
        BookResponse created = BookResponse.from(books.importBook(request.toDomain(null)));
        return ResponseEntity.created(URI.create("/api/books/" + created.id())).body(created);
    }
}
