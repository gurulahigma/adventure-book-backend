package com.sbde.sample.api;

import com.sbde.jpa.helpers.AbstractSearchCriteria;
import com.sbde.jpa.helpers.InternalPage;
import com.sbde.jpa.helpers.utils.FetchAttribute;
import com.sbde.jpa.helpers.utils.SearchRequest;
import com.sbde.jpa.helpers.utils.SearchSpecification;
import com.sbde.sample.domain.Category;
import com.sbde.sample.domain.CategoryRepository;
import com.sbde.sample.domain.Product;
import com.sbde.sample.domain.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private final ProductRepository products;
    private final CategoryRepository categories;

    public SearchService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    /** Any criteria works the same way: makeSearchRequest() -> SearchSpecification -> repository. */
    public InternalPage<ProductDto> searchProducts(AbstractSearchCriteria criteria) {
        SearchRequest request = criteria.makeSearchRequest();
        Page<Product> page = products.findAll(new SearchSpecification<>(request),
                SearchSpecification.getPageable(request.getPage(), request.getSize()));
        return InternalPage.convertToInternalPage(page).map(ProductDto::from);
    }

    /** Same search, but the category is fetched in the same query instead of one query per product. */
    public InternalPage<ProductDto> searchProductsWithCategory(AbstractSearchCriteria criteria) {
        SearchRequest request = criteria.makeSearchRequest();
        Page<Product> page = products.findAll(new SearchSpecification<Product>(request, new FetchAttribute("category")),
                SearchSpecification.getPageable(request.getPage(), request.getSize()));
        return InternalPage.convertToInternalPage(page).map(ProductDto::from);
    }

    public InternalPage<CategoryDto> searchCategories(AbstractSearchCriteria criteria) {
        SearchRequest request = criteria.makeSearchRequest();
        Page<Category> page = categories.findAll(new SearchSpecification<>(request),
                SearchSpecification.getPageable(request.getPage(), request.getSize()));
        return InternalPage.convertToInternalPage(page).map(CategoryDto::from);
    }
}
