package com.sbde.jpa.helpers.utils;

import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SearchSpecification<T> implements Specification<T> {

    private static final Logger log = LoggerFactory.getLogger(SearchSpecification.class);
    private static final long serialVersionUID = -9153865343320750644L;

    protected final transient SearchRequest request;
    protected final transient List<FetchAttribute> fetchAttributePaths;
    protected final transient List<String> groupByColumns;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.equal(cb.literal(Boolean.TRUE), Boolean.TRUE);
        addFetchAttributes(root, query);
        addGroupBy(root, query);
        addOrderBy(root, cb, query);
        queryModification(cb, query, predicate);
        return addFilter(root, cb, query, predicate);
    }

    public static Pageable getPageable(Integer page, Integer size) {
        return PageRequest.of(Objects.requireNonNullElse(page, 0), Objects.requireNonNullElse(size, 100));
    }

    protected Predicate addFilter(Root<T> root, CriteriaBuilder cb, CriteriaQuery<?> query, Predicate predicate) {
        for (FilterRequest filter : this.request.getFilters()) {
            log.info("Filter: {} {} {}", filter.getKey(), filter.getOperator(), filter.getValue());
            predicate = filter.getOperator().build(root, cb, filter, predicate);
        }
        return predicate;
    }

    public void addFetchAttributes(Root<T> root, CriteriaQuery<?> query) {
        if (!fetchAttributePaths.isEmpty()) {
            fetchAttributePaths.forEach(attributePath -> {
                if (query.getResultType() == Long.class) {
                    SearchHelpers.joinPathFromKey(root, attributePath.getPath());
                } else {
                    SearchHelpers.fecthPathFromKey(root, attributePath.getPath());
                }
            });
            query.distinct(true);
        }
    }

    public void addGroupBy(Root<T> root, CriteriaQuery<?> query) {
        if (!groupByColumns.isEmpty()) {
            List<Expression<?>> columnNamesExpression = groupByColumns.stream()
                    .map(x -> SearchHelpers.getPathFromKey(root, x)).collect(Collectors.toList());
            query.groupBy(columnNamesExpression);
        }
    }

    protected void queryModification(CriteriaBuilder cb, CriteriaQuery<?> query, Predicate predicate) {
    }

    public void addOrderBy(Root<T> root, CriteriaBuilder cb, CriteriaQuery<?> query) {
        List<Order> orders = new ArrayList<>();
        for (SortRequest sort : this.request.getSorts()) {
            if (sort.getDirection() != null)
                orders.add(sort.getDirection().build(root, cb, sort));
        }
        query.orderBy(orders);
    }

    public SearchSpecification(SearchRequest request) {
        this.request = request;
        this.fetchAttributePaths = new ArrayList<>();
        this.groupByColumns = new ArrayList<>();

    }

    public SearchSpecification(SearchRequest request, FetchAttribute... fecthAttributePaths) {
        this.request = request;
        this.fetchAttributePaths = List.of(fecthAttributePaths);
        this.groupByColumns = new ArrayList<>();
    }

    public SearchSpecification(SearchRequest request, String... groupByColumns) {
        this.request = request;
        this.fetchAttributePaths = new ArrayList<>();
        this.groupByColumns = List.of(groupByColumns);
    }

    public SearchSpecification(SearchRequest request, List<String> groupByColumns) {
        this.request = request;
        this.fetchAttributePaths = new ArrayList<>();
        this.groupByColumns = groupByColumns;
    }

    public SearchRequest getRequest() {
        return request;
    }

    public List<FetchAttribute> getFetchAttributePaths() {
        return fetchAttributePaths;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }
}
