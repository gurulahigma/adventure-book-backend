package com.sbde.jpa.helpers;

import com.sbde.jpa.helpers.annotations.FilterCriteria;
import com.sbde.jpa.helpers.annotations.FilterRange;
import com.sbde.jpa.helpers.annotations.Sortby;
import com.sbde.jpa.helpers.annotations.Sorts;
import com.sbde.jpa.helpers.utils.FieldType;
import com.sbde.jpa.helpers.utils.FilterRequest;
import com.sbde.jpa.helpers.utils.Operator;
import com.sbde.jpa.helpers.utils.SearchRequest;
import com.sbde.jpa.helpers.utils.SortDirection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnnotatedSearchCriteriaTest {

    enum Status {OPEN, CLOSED}

    @Sortby(sortBy = "declarationDate", direction = SortDirection.DESC)
    static class Params extends AbstractSearchCriteria {
        @FilterCriteria(fields = "referenceSinister", operator = Operator.LIKE, ignoreCase = true)
        String reference;
        @FilterCriteria(fields = {"refExtranet", "refVeos"}, operator = Operator.LIKE)
        String search;
        @FilterCriteria(fields = "id")
        Long id;
        @FilterCriteria(fields = "status")
        Status status;
        @FilterCriteria(fields = "contract.product.productCode", operator = Operator.IN)
        List<String> products;
        @FilterCriteria(fields = "occurrenceDate")
        LocalDate occurrenceDate;
    }

    @Test
    void ignoresEmptyValuesAndAppliesDefaultSortAndPagination() {
        Params params = new Params();
        params.search = "  ";
        params.products = List.of();

        SearchRequest request = params.makeSearchRequest();

        assertThat(request.getFilters()).isEmpty();
        assertThat(request.getPage()).isZero();
        assertThat(request.getSize()).isEqualTo(25);
        assertThat(request.getSorts()).singleElement().satisfies(sort -> {
            assertThat(sort.getKey()).isEqualTo("declarationDate");
            assertThat(sort.getDirection()).isEqualTo(SortDirection.DESC);
        });
    }

    @Test
    void infersFieldTypesAndBuildsFilters() {
        Params params = new Params();
        params.reference = "abc";
        params.id = 7L;
        params.status = Status.OPEN;
        params.products = List.of("P1", "P2");
        params.occurrenceDate = LocalDate.of(2024, 1, 2);
        params.setPage(3);
        params.setSize(10);

        SearchRequest request = params.makeSearchRequest();

        assertThat(request.getPage()).isEqualTo(3);
        assertThat(request.getSize()).isEqualTo(10);
        assertThat(request.getFilters()).extracting(FilterRequest::getKey, FilterRequest::getFieldType)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("referenceSinister", FieldType.STRING),
                        org.assertj.core.groups.Tuple.tuple("id", FieldType.LONG),
                        org.assertj.core.groups.Tuple.tuple("status", FieldType.ENUM),
                        org.assertj.core.groups.Tuple.tuple("contract.product.productCode", FieldType.STRING),
                        org.assertj.core.groups.Tuple.tuple("occurrenceDate", FieldType.DATE));
        FilterRequest status = request.getFilters().stream()
                .filter(f -> "status".equals(f.getKey())).findFirst().orElseThrow();
        assertThat(status.getValue()).isEqualTo("OPEN");
        assertThat(status.getClazz()).isEqualTo(Status.class);
        FilterRequest reference = request.getFilters().stream()
                .filter(f -> "referenceSinister".equals(f.getKey())).findFirst().orElseThrow();
        assertThat(reference.isIgnoreCase()).isTrue();
    }

    @Test
    void severalFieldsAreCombinedWithOr() {
        Params params = new Params();
        params.search = "EXT";

        FilterRequest filter = params.makeSearchRequest().getFilters().get(0);

        assertThat(filter.getOperator()).isEqualTo(Operator.ANY_OF);
        assertThat(filter.getAnyOf()).extracting(FilterRequest::getKey).containsExactly("refExtranet", "refVeos");
        assertThat(filter.getAnyOf()).allSatisfy(sub -> assertThat(sub.getValue()).isEqualTo("EXT"));
    }

    @FilterRange(fields = "declarationDate", from = "declaredFrom", to = "declaredTo")
    @FilterRange(fields = {"amountA", "amountB"}, from = "minAmount", to = "maxAmount")
    static class RangeParams extends AbstractSearchCriteria {
        LocalDate declaredFrom;
        LocalDate declaredTo;
        Long minAmount;
        Long maxAmount;
    }

    @Sorts({@Sortby(sortBy = "a", direction = SortDirection.DESC), @Sortby(sortBy = "b")})
    static class MultiSortParams extends AbstractSearchCriteria {
    }

    @Test
    void sortsAppliesAllDefaultSortsInOrderUnlessOverridden() {
        MultiSortParams params = new MultiSortParams();

        assertThat(params.makeSearchRequest().getSorts())
                .extracting(sort -> sort.getKey(), sort -> sort.getDirection())
                .containsExactly(org.assertj.core.groups.Tuple.tuple("a", SortDirection.DESC),
                        org.assertj.core.groups.Tuple.tuple("b", SortDirection.ASC));

        params.setSortBy("c");
        assertThat(params.makeSearchRequest().getSorts()).extracting(sort -> sort.getKey()).containsExactly("c");
    }

    @Test
    void rangeIsIgnoredWhenBothBoundsAreNull() {
        assertThat(new RangeParams().makeSearchRequest().getFilters()).isEmpty();
    }

    @Test
    void rangeBuildsBetweenFiltersFromTheNamedFields() {
        RangeParams params = new RangeParams();
        params.declaredFrom = LocalDate.of(2024, 1, 1);
        params.minAmount = 10L;
        params.maxAmount = 20L;

        List<FilterRequest> filters = params.makeSearchRequest().getFilters();

        assertThat(filters).hasSize(2);
        FilterRequest date = filters.get(0);
        assertThat(date.getOperator()).isEqualTo(Operator.BETWEEN);
        assertThat(date.getKey()).isEqualTo("declarationDate");
        assertThat(date.getFieldType()).isEqualTo(FieldType.DATE);
        assertThat(date.getValue()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(date.getValueTo()).isNull();
        FilterRequest amount = filters.get(1);
        assertThat(amount.getOperator()).isEqualTo(Operator.ANY_OF);
        assertThat(amount.getAnyOf()).extracting(FilterRequest::getKey).containsExactly("amountA", "amountB");
        assertThat(amount.getAnyOf()).allSatisfy(sub -> {
            assertThat(sub.getOperator()).isEqualTo(Operator.BETWEEN);
            assertThat(sub.getValue()).isEqualTo(10L);
            assertThat(sub.getValueTo()).isEqualTo(20L);
        });
    }

    @Test
    void rangeRejectsUnknownBoundField() {
        @FilterRange(fields = "x", from = "nope", to = "nope")
        class Invalid extends AbstractSearchCriteria {
        }

        assertThatThrownBy(() -> new Invalid().makeSearchRequest())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no field named 'nope'");
    }

    @Test
    void rejectsTypesThatCannotBeMappedToAFieldType() {
        class Invalid extends AbstractSearchCriteria {
            @FilterCriteria(fields = "x")
            Object unsupported = "value";
        }

        assertThatThrownBy(() -> new Invalid().makeSearchRequest())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot infer a FieldType");
    }
}
