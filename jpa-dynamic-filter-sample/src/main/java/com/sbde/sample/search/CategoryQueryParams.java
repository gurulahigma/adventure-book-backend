package com.sbde.sample.search;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.annotations.FilterCriteria;
import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.utils.Operator;
import com.sbde.jpa.helpers.utils.SortDirection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Sortby(sortBy = "name", direction = SortDirection.ASC)
public class CategoryQueryParams extends AbstractSearchCriteria {

    @FilterCriteria(fields = "code", ignoreCase = true)
    private String code;

    @FilterCriteria(fields = "name", operator = Operator.LIKE, ignoreCase = true)
    private String name;
}
