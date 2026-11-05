package com.sbde.adventure.domain.book;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.InternalPage;
import com.sbde.jpa.helpers.utils.SearchRequest;
import com.sbde.jpa.helpers.utils.SearchSpecification;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Application service of the Book aggregate. */
@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    public InternalPage<Book> search(AbstractSearchCriteria criteria) {
        SearchRequest request = criteria.makeSearchRequest();
        Page<Book> page = books.findAll(new SearchSpecification<>(request),
                SearchSpecification.getPageable(request.getPage(), request.getSize()));
        return InternalPage.convertToInternalPage(page);
    }

    public Book get(String id) {
        return books.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    public List<String> genres() {
        return books.findDistinctGenres();
    }

    /** Stores the book even when it breaks the validity rules: it is then listed as invalid and cannot be played. */
    @Transactional
    public Book importBook(Book book) {
        if (books.existsById(book.getId())) {
            throw new DuplicateBookException(book.getId());
        }
        return books.save(book);
    }
}
