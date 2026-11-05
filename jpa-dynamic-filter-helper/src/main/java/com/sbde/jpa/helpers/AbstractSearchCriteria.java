package com.sbde.jpa.helpers;

import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.utils.AnnotatedFilterResolver;
import com.sbde.jpa.helpers.utils.SearchRequest;
import com.sbde.jpa.helpers.utils.SortDirection;
import com.sbde.jpa.helpers.utils.SortRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Pagination and sorting for search criteria. Subclasses declare their filters with
 * {@link com.sbde.jpa.helpers.annotations.FilterCriteria} and their default sorts with {@link Sortby} / {@link com.sbde.jpa.helpers.annotations.Sorts}, or override
 * {@link #makeSearchRequest()} to build the request by hand.
 */
@Data
public abstract class AbstractSearchCriteria implements SearchCriteria {
    protected int page = 0;
    protected int size = 25;
    protected String sortBy;
    protected SortDirection direction;

    protected AbstractSearchCriteria() {
        Sortby[] defaultSorts = getClass().getAnnotationsByType(Sortby.class);
        if (defaultSorts.length > 0) {
            this.sortBy = defaultSorts[0].sortBy();
            this.direction = defaultSorts[0].direction();
        }
    }

    protected AbstractSearchCriteria(int page, int size, String sortBy, SortDirection direction) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public SearchRequest makeSearchRequest() {
        List<SortRequest> sorts = new ArrayList<>();
        if (sortBy != null) {
            SortDirection effective = direction != null ? direction : SortDirection.ASC;
            sorts.add(new SortRequest(sortBy, effective));
            // The extra default sorts apply only while the first default has not been overridden.
            Sortby[] defaultSorts = getClass().getAnnotationsByType(Sortby.class);
            if (defaultSorts.length > 1 && sortBy.equals(defaultSorts[0].sortBy())
                    && effective == defaultSorts[0].direction()) {
                for (int i = 1; i < defaultSorts.length; i++) {
                    sorts.add(new SortRequest(defaultSorts[i].sortBy(), defaultSorts[i].direction()));
                }
            }
        }
        return SearchRequest.builder()
                .filters(AnnotatedFilterResolver.resolve(this))
                .sorts(sorts)
                .page(page)
                .size(size)
                .build();
    }
}
