package com.sbde.adventure.application.rest;

import com.sbde.adventure.domain.book.BookNotFoundException;
import com.sbde.adventure.domain.book.BookNotPlayableException;
import com.sbde.adventure.domain.book.DuplicateBookException;
import com.sbde.adventure.domain.book.InvalidBookDataException;
import com.sbde.adventure.domain.game.GameNotFoundException;
import com.sbde.adventure.domain.game.InvalidMoveException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class, GameNotFoundException.class})
    public ProblemDetail notFound(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DuplicateBookException.class, InvalidMoveException.class})
    public ProblemDetail conflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BookNotPlayableException.class)
    public ProblemDetail notPlayable(BookNotPlayableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        problem.setProperty("errors", e.getErrors());
        return problem;
    }

    @ExceptionHandler(InvalidBookDataException.class)
    public ProblemDetail invalidData(InvalidBookDataException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadable() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "The request body is malformed");
    }
}
