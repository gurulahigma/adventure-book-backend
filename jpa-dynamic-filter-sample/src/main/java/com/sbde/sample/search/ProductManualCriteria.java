package com.sbde.sample.search;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.utils.FieldType;
import com.sbde.jpa.helpers.utils.FilterRequest;
import com.sbde.jpa.helpers.utils.Operator;
import com.sbde.jpa.helpers.utils.SearchRequest;
import com.sbde.jpa.helpers.utils.SortDirection;
import com.sbde.jpa.helpers.utils.SortRequest;
import com.sbde.sample.domain.ProductStatus;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

import static com.sbde.jpa.helpers.utils.SearchHelpers.makeFilterRequest;
import static com.sbde.jpa.helpers.utils.SearchHelpers.makeFilterRequestForList;
import static com.sbde.jpa.helpers.utils.SearchHelpers.makeFilterRequestLikeAny;

/** Programmatic configuration: no annotations, the SearchRequest is built by hand with SearchHelpers. */
public class ProductManualCriteria extends AbstractSearchCriteria {

    private final String text;
    private final List<Long> ids;
    private final List<String> categoryCodes;
    private final List<ProductStatus> statuses;
    private final ProductStatus excludedStatus;
    private final Boolean active;
    private final Double minPrice;
    private final Double maxPrice;

    @Builder
    public ProductManualCriteria(String text, List<Long> ids, List<String> categoryCodes,
                                 List<ProductStatus> statuses, ProductStatus excludedStatus, Boolean active,
                                 Double minPrice, Double maxPrice,
                                 int page, int size, String sortBy, SortDirection direction) {
        super(page, size, sortBy, direction);
        this.text = text;
        this.ids = ids;
        this.categoryCodes = categoryCodes;
        this.statuses = statuses;
        this.excludedStatus = excludedStatus;
        this.active = active;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    @Override
    public SearchRequest makeSearchRequest() {
        List<FilterRequest> filters = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            // LIKE on several attributes, any match is enough
            filters.add(makeFilterRequestLikeAny(List.of("name", "reference"), text));
        }
        if (ids != null && !ids.isEmpty()) {
            filters.add(makeFilterRequestForList("id", Operator.IN, FieldType.LONG, ids));
        }
        if (categoryCodes != null && !categoryCodes.isEmpty()) {
            filters.add(makeFilterRequestForList("category.code", Operator.IN, FieldType.STRING, categoryCodes));
        }
        if (statuses != null && !statuses.isEmpty()) {
            filters.add(makeFilterRequestForList("status", Operator.IN, ProductStatus.class, statuses));
        }
        if (excludedStatus != null) {
            filters.add(makeFilterRequest("status", Operator.NOT_EQUAL, excludedStatus));
        }
        if (active != null) {
            filters.add(makeFilterRequest("active", Operator.EQUAL, FieldType.BOOLEAN, active));
        }
        if (minPrice != null || maxPrice != null) {
            filters.add(makeFilterRequest("price", FieldType.DOUBLE, minPrice, maxPrice));
        }

        List<SortRequest> sorts = new ArrayList<>();
        sorts.add(new SortRequest(sortBy, direction));
        return SearchRequest.builder().filters(filters).sorts(sorts).page(page).size(size).build();
    }
}
