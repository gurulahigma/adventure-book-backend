package com.sbde.adventure.domain.book;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.annotations.FilterCriteria;
import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.utils.Operator;
import com.sbde.jpa.helpers.utils.SortDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/** Annotation-only filter configuration of the library search; see jpa-dynamic-filter-helper. */
@Getter
@Setter
@NoArgsConstructor
@Sortby(sortBy = "title", direction = SortDirection.ASC)
public class BookSearchCriteria extends AbstractSearchCriteria {

    private static final Set<String> SORTABLE = Set.of("title", "author", "difficulty", "sectionCount");
    private static final int MAX_SIZE = 100;

    @FilterCriteria(fields = {"title", "author", "description"}, operator = Operator.LIKE, ignoreCase = true)
    private String search;

    @FilterCriteria(fields = "difficulty", operator = Operator.IN)
    private List<Difficulty> difficulties;

    @FilterCriteria(fields = "genres", operator = Operator.IN)
    private List<String> genres;

    @FilterCriteria(fields = "valid")
    private Boolean valid;

    @Override
    public void setSortBy(String sortBy) {
        this.sortBy = SORTABLE.contains(sortBy) ? sortBy : "title";
    }

    @Override
    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    @Override
    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), MAX_SIZE);
    }
}
