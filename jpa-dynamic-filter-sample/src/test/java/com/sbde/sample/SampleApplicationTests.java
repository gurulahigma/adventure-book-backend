package com.sbde.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Each test shows one way of using the library against the seeded H2 data (see DataSeeder). */
@SpringBootTest
@AutoConfigureMockMvc
class SampleApplicationTests {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JdbcTemplate jdbc;

    /** Calls the endpoint with name/value pairs and checks which product references come back. */
    private void assertReferences(String path, String[] params, String... expected) throws Exception {
        search(path, params)
                .andExpect(jsonPath("$.totalElements").value(expected.length))
                .andExpect(jsonPath("$.content[*].reference", containsInAnyOrder(expected)));
    }

    private ResultActions search(String path, String... params) throws Exception {
        MockHttpServletRequestBuilder request = get(path);
        for (int i = 0; i < params.length; i += 2) {
            request.param(params[i], params[i + 1]);
        }
        return mockMvc.perform(request).andExpect(status().isOk());
    }

    private static String[] p(String... params) {
        return params;
    }

    // ---- pagination and sorting (AbstractSearchCriteria + @Sortby) ----

    @Test
    void noFilterReturnsEverythingWithDefaultPaginationAndSort() throws Exception {
        search("/api/products")
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.pageSize").value(25))
                .andExpect(jsonPath("$.content[0].name").value("Bougie parfumée"));
    }

    @Test
    void pageAndSizeComeFromTheQueryString() throws Exception {
        search("/api/products", "page", "1", "size", "3")
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    void sortByAndDirectionOverrideTheSortbyDefault() throws Exception {
        search("/api/products", "sortBy", "price", "direction", "DESC")
                .andExpect(jsonPath("$.content[0].reference").value("REF-007"));
        search("/api/products", "sortBy", "price", "direction", "ASC")
                .andExpect(jsonPath("$.content[0].reference").value("REF-010"));
    }

    @Test
    void emptyValuesAreIgnored() throws Exception {
        search("/api/products", "name", "", "categoryCodes", "").andExpect(jsonPath("$.totalElements").value(10));
    }

    // ---- ignoreCase (case and accents) ----

    @Test
    void equalIgnoringCaseAndAccents() throws Exception {
        assertReferences("/api/products", p("name", "CAFE ARABICA"), "REF-003");
        assertReferences("/api/products", p("name", "eclair au chocolat"), "REF-001");
    }

    @Test
    void likeIgnoringCaseAndAccents() throws Exception {
        assertReferences("/api/products", p("supplierCity", "nimes"), "REF-005", "REF-010");
        assertReferences("/api/products", p("reference", "ref-00"),
                "REF-001", "REF-002", "REF-003", "REF-004", "REF-005", "REF-006", "REF-007", "REF-008", "REF-009");
    }

    @Test
    void likeWithoutIgnoreCaseOnlyIgnoresTheCase() throws Exception {
        assertReferences("/api/products", p("categoryName", "boissons"), "REF-003", "REF-004", "REF-005", "REF-010");
        assertReferences("/api/products", p("categoryName", "epicerie"));
        assertReferences("/api/products", p("categoryName", "épicerie"), "REF-001", "REF-002", "REF-009");
    }

    // ---- several fields ----

    @Test
    void severalFieldsMatchWhenAnyOfThemMatches() throws Exception {
        assertReferences("/api/products", p("search", "creme"), "REF-002");
        assertReferences("/api/products", p("search", "cafe"), "REF-003");
        assertReferences("/api/products", p("search", "vanille"), "REF-006");
        assertReferences("/api/products", p("search", "natur"), "REF-008", "REF-010");
    }

    // ---- IN ----

    @Test
    void inOnStringsIgnoringCase() throws Exception {
        assertReferences("/api/products", p("categoryCodes", "food", "categoryCodes", "home"),
                "REF-001", "REF-002", "REF-009", "REF-006", "REF-007", "REF-008");
    }

    @Test
    void inOnEnums() throws Exception {
        assertReferences("/api/products", p("priorities", "HIGH", "priorities", "LOW"),
                "REF-001", "REF-003", "REF-009", "REF-004", "REF-006", "REF-007", "REF-010");
    }

    // ---- EQUAL / NOT_EQUAL for each inferred type ----

    @Test
    void enumEqualAndNotEqual() throws Exception {
        assertReferences("/api/products", p("status", "PUBLISHED"),
                "REF-001", "REF-002", "REF-003", "REF-006", "REF-009", "REF-010");
        assertReferences("/api/products", p("excludedStatus", "PUBLISHED"),
                "REF-004", "REF-005", "REF-007", "REF-008");
    }

    @Test
    void booleanIntegerDoubleAndCharacter() throws Exception {
        assertReferences("/api/products", p("active", "false"), "REF-004", "REF-008");
        assertReferences("/api/products", p("stock", "0"), "REF-004");
        assertReferences("/api/products", p("price", "3.5"), "REF-001");
        assertReferences("/api/products", p("grade", "C"), "REF-005", "REF-007", "REF-010");
    }

    @Test
    void longDateAndDateTime() throws Exception {
        Long id = jdbc.queryForObject("select id from product where reference = 'REF-006'", Long.class);
        assertReferences("/api/products", p("id", String.valueOf(id)), "REF-006");
        assertReferences("/api/products", p("releaseDate", "2024-01-02"), "REF-010");
        assertReferences("/api/products", p("lastReviewAt", "2024-01-02T07:00:00"), "REF-010");
    }

    @Test
    void filtersAreCombinedWithAnd() throws Exception {
        assertReferences("/api/products", p("status", "PUBLISHED", "categoryCodes", "DRINK"), "REF-003", "REF-010");
    }

    // ---- BETWEEN, inheritance and hybrid configuration ----

    @Test
    void betweenWithBothBoundsOrOnlyOne() throws Exception {
        assertReferences("/api/products/range", p("minPrice", "5", "maxPrice", "13"), "REF-003", "REF-004", "REF-006");
        assertReferences("/api/products/range", p("minPrice", "12"), "REF-006", "REF-007");
        assertReferences("/api/products/range", p("maxPrice", "2.5"), "REF-008", "REF-010");
        assertReferences("/api/products/range", p("releasedFrom", "2024-04-01"), "REF-006", "REF-007", "REF-009");
        assertReferences("/api/products/range", p("releasedTo", "2023-12-31"), "REF-003", "REF-004", "REF-008");
    }

    @Test
    void rangeEndpointStillAppliesTheInheritedAnnotations() throws Exception {
        assertReferences("/api/products/range", p("status", "PUBLISHED", "minPrice", "5"), "REF-003", "REF-006");
    }

    @Test
    void filterRangesOnPriceAndDateAreCombinedWithAnd() throws Exception {
        assertReferences("/api/products/range", p("minPrice", "4", "releasedFrom", "2024-01-01", "releasedTo", "2024-04-01"),
                "REF-002", "REF-006");
    }

    // ---- several default sorts (@Sorts) ----

    @Test
    void sortsAppliesEveryDefaultSortInOrder() throws Exception {
        search("/api/products/range")
                .andExpect(jsonPath("$.content[*].reference", contains("REF-003", "REF-004", "REF-005", "REF-010",
                        "REF-002", "REF-009", "REF-001", "REF-007", "REF-006", "REF-008")));
    }

    @Test
    void sortByOverridesEveryDefaultSort() throws Exception {
        search("/api/products/range", "sortBy", "price", "direction", "ASC")
                .andExpect(jsonPath("$.content[*].reference", contains("REF-010", "REF-008", "REF-005", "REF-001",
                        "REF-009", "REF-002", "REF-004", "REF-003", "REF-006", "REF-007")));
    }

    // ---- programmatic criteria ----

    @Test
    void manualCriteriaBuiltWithSearchHelpers() throws Exception {
        assertReferences("/api/products/manual", p("text", "ref-00", "minPrice", "5", "maxPrice", "13"),
                "REF-003", "REF-004", "REF-006");
        assertReferences("/api/products/manual", p("statuses", "PUBLISHED", "statuses", "DRAFT", "active", "true"),
                "REF-001", "REF-002", "REF-003", "REF-006", "REF-009", "REF-010", "REF-005", "REF-007");
        assertReferences("/api/products/manual", p("excludedStatus", "PUBLISHED", "categoryCodes", "HOME"),
                "REF-007", "REF-008");
        search("/api/products/manual", "sortBy", "price", "direction", "DESC", "size", "1")
                .andExpect(jsonPath("$.content[0].reference").value("REF-007"));
    }

    // ---- fetch attributes ----

    @Test
    void fetchAttributeLoadsTheAssociationInTheSameQuery() throws Exception {
        search("/api/products/with-category", "categoryName", "boissons")
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[*].categoryName", everyItem(is("Boissons"))));
        search("/api/products/with-category", "categoryName", "boissons", "size", "2")
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    // ---- other entity and persistence helpers ----

    @Test
    void anotherEntityWithItsOwnCriteria() throws Exception {
        search("/api/categories")
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].name").value("Boissons"));
        search("/api/categories", "code", "food")
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Épicerie fine"));
        search("/api/categories", "name", "epicerie")
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void convertersAndAuditingFromThePersistencePackage() throws Exception {
        assertThat(jdbc.queryForObject("select status from product where reference = 'REF-001'", String.class))
                .isEqualTo("Publié");
        assertThat(jdbc.queryForObject("select priority from product where reference = 'REF-001'", String.class))
                .isEqualTo("H");
        search("/api/products", "reference", "REF-001")
                .andExpect(jsonPath("$.content[0].statusLabel").value("Publié"))
                .andExpect(jsonPath("$.content[0].createdBy").value("sample-user"))
                .andExpect(jsonPath("$.content[0].createdOn").isNotEmpty());
    }
}
