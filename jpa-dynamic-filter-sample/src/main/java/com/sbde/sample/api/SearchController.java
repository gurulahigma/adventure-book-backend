package com.sbde.sample.api;

import com.sbde.jpa.helpers.InternalPage;
import com.sbde.jpa.helpers.utils.SortDirection;
import com.sbde.sample.domain.ProductStatus;
import com.sbde.sample.search.CategoryQueryParams;
import com.sbde.sample.search.ProductManualCriteria;
import com.sbde.sample.search.ProductQueryParams;
import com.sbde.sample.search.ProductRangeQueryParams;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {

    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    /** Annotation-based: every query parameter of ProductQueryParams, plus page, size, sortBy and direction. */
    @GetMapping("/products")
    public InternalPage<ProductDto> products(ProductQueryParams params) {
        return service.searchProducts(params);
    }

    /** Annotations plus @FilterRange ranges (minPrice/maxPrice, releasedFrom/releasedTo) and @Sorts. */
    @GetMapping("/products/range")
    public InternalPage<ProductDto> productsInRange(ProductRangeQueryParams params) {
        return service.searchProducts(params);
    }

    /** Same filters as /products, with the category fetched in the same query. */
    @GetMapping("/products/with-category")
    public InternalPage<ProductDto> productsWithCategory(ProductQueryParams params) {
        return service.searchProductsWithCategory(params);
    }

    /** Programmatic criteria built with SearchHelpers, without annotations. */
    @GetMapping("/products/manual")
    public InternalPage<ProductDto> productsManual(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(required = false) List<String> categoryCodes,
            @RequestParam(required = false) List<ProductStatus> statuses,
            @RequestParam(required = false) ProductStatus excludedStatus,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") SortDirection direction) {
        return service.searchProducts(ProductManualCriteria.builder()
                .text(text).ids(ids).categoryCodes(categoryCodes).statuses(statuses)
                .excludedStatus(excludedStatus).active(active).minPrice(minPrice).maxPrice(maxPrice)
                .page(page).size(size).sortBy(sortBy).direction(direction)
                .build());
    }

    @GetMapping("/categories")
    public InternalPage<CategoryDto> categories(CategoryQueryParams params) {
        return service.searchCategories(params);
    }
}
