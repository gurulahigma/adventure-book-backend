package com.sbde.adventure.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbde.adventure.application.dto.request.ImportBookRequest;
import com.sbde.adventure.domain.book.Book;
import com.sbde.adventure.domain.book.BookService;
import com.sbde.adventure.domain.book.DuplicateBookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/** Loads the books/*.json files of the classpath at startup; unreadable files are skipped, invalid books are kept. */
@Component
public class BookSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BookSeeder.class);

    private final BookService books;
    private final ObjectMapper mapper;

    public BookSeeder(BookService books, ObjectMapper mapper) {
        this.books = books;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        for (Resource file : new PathMatchingResourcePatternResolver().getResources("classpath:books/*.json")) {
            String fileName = file.getFilename();
            String fallbackId = fileName.substring(0, fileName.length() - ".json".length());
            try (InputStream in = file.getInputStream()) {
                Book book = books.importBook(mapper.readValue(in, ImportBookRequest.class).toDomain(fallbackId));
                log.info("Loaded book '{}' ({})", book.getId(),
                        book.isValid() ? "valid" : "invalid: " + book.getValidationErrors());
            } catch (DuplicateBookException e) {
                log.debug("Book file {} already loaded", fileName);
            } catch (IOException | RuntimeException e) {
                log.warn("Skipping book file {}: {}", fileName, e.getMessage());
            }
        }
    }
}
