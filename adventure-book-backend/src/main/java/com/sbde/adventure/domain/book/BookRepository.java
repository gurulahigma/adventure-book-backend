package com.sbde.adventure.domain.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** Repository of the Book aggregate; searches go through {@link BookSearchCriteria} specifications. */
public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

    @Query("select distinct g from Book b join b.genres g order by g")
    List<String> findDistinctGenres();
}
