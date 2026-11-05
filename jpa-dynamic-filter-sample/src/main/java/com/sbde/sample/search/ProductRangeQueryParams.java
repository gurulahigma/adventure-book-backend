package com.sbde.sample.search;

import com.sbde.jpa.helpers.annotations.FilterRange;
import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.annotations.Sorts;
import com.sbde.jpa.helpers.utils.SortDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * The @FilterCriteria annotations are inherited from ProductQueryParams. @FilterRange adds BETWEEN filters whose
 * bounds are read from the named fields; a missing bound leaves that side of the range open.
 * @Sorts replaces the inherited default sort with several keys, applied in order.
 */
@Getter
@Setter
@NoArgsConstructor
@FilterRange(fields = "price", from = "minPrice", to = "maxPrice")
@FilterRange(fields = "releaseDate", from = "releasedFrom", to = "releasedTo")
@Sorts({@Sortby(sortBy = "category.code"), @Sortby(sortBy = "price", direction = SortDirection.DESC)})
public class ProductRangeQueryParams extends ProductQueryParams {

    private Double minPrice;
    private Double maxPrice;
    private LocalDate releasedFrom;
    private LocalDate releasedTo;
}
