package com.sbde.sample.config;

import com.sbde.sample.domain.Category;
import com.sbde.sample.domain.CategoryRepository;
import com.sbde.sample.domain.Priority;
import com.sbde.sample.domain.Product;
import com.sbde.sample.domain.ProductRepository;
import com.sbde.sample.domain.ProductStatus;
import com.sbde.sample.domain.Supplier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Accents are deliberate: they show the ignoreCase option. */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(CategoryRepository categories, ProductRepository products) {
        return args -> {
            Category food = categories.save(new Category("FOOD", "Épicerie fine"));
            Category drink = categories.save(new Category("DRINK", "Boissons"));
            Category home = categories.save(new Category("HOME", "Maison"));

            products.save(product("REF-001", "Éclair au chocolat", "Pâtisserie fraîche", 3.50, 120, true, 'A',
                    LocalDate.of(2024, 1, 15), LocalDateTime.of(2024, 2, 1, 9, 30), ProductStatus.PUBLISHED,
                    Priority.HIGH, food, "Boulangerie Martin", "Paris"));
            products.save(product("REF-002", "Crème brûlée", "Dessert traditionnel", 4.90, 60, true, 'A',
                    LocalDate.of(2024, 2, 10), LocalDateTime.of(2024, 3, 1, 10, 0), ProductStatus.PUBLISHED,
                    Priority.MEDIUM, food, "Boulangerie Martin", "Lyon"));
            products.save(product("REF-003", "Café arabica", "Café moulu torréfié", 7.20, 200, true, 'B',
                    LocalDate.of(2023, 11, 5), LocalDateTime.of(2024, 1, 10, 14, 0), ProductStatus.PUBLISHED,
                    Priority.HIGH, drink, "Torréfacteur Dupont", "Marseille"));
            products.save(product("REF-004", "Thé vert", "Thé vert sencha", 5.80, 0, false, 'B',
                    LocalDate.of(2023, 9, 20), LocalDateTime.of(2023, 12, 12, 8, 15), ProductStatus.ARCHIVED,
                    Priority.LOW, drink, "Maison du Thé", "Paris"));
            products.save(product("REF-005", "Jus d'orange", "Pressé à froid", 3.10, 80, true, 'C',
                    LocalDate.of(2024, 3, 5), LocalDateTime.of(2024, 3, 20, 16, 45), ProductStatus.DRAFT,
                    Priority.MEDIUM, drink, "Vergers du Sud", "Nîmes"));
            products.save(product("REF-006", "Bougie parfumée", "Cire de soja, senteur vanille", 12.50, 35, true, 'A',
                    LocalDate.of(2024, 4, 1), LocalDateTime.of(2024, 4, 15, 11, 0), ProductStatus.PUBLISHED,
                    Priority.LOW, home, "Atelier Lumière", "Grasse"));
            products.save(product("REF-007", "Coussin décoratif", "Coton bio", 19.90, 15, true, 'C',
                    LocalDate.of(2024, 4, 20), LocalDateTime.of(2024, 5, 2, 13, 20), ProductStatus.DRAFT,
                    Priority.LOW, home, "Atelier Lumière", "Lyon"));
            products.save(product("REF-008", "Éponge végétale", "Éponge naturelle", 2.40, 300, false, 'B',
                    LocalDate.of(2022, 6, 1), LocalDateTime.of(2023, 1, 5, 9, 0), ProductStatus.ARCHIVED,
                    Priority.MEDIUM, home, "Maison du Thé", "Nantes"));
            products.save(product("REF-009", "Chocolat noir", "Tablette 70%", 4.20, 150, true, 'A',
                    LocalDate.of(2024, 5, 10), LocalDateTime.of(2024, 5, 25, 10, 10), ProductStatus.PUBLISHED,
                    Priority.HIGH, food, "Chocolaterie Léon", "Paris"));
            products.save(product("REF-010", "Eau pétillante", "Source naturelle", 1.20, 500, true, 'C',
                    LocalDate.of(2024, 1, 2), LocalDateTime.of(2024, 1, 2, 7, 0), ProductStatus.PUBLISHED,
                    Priority.LOW, drink, "Vergers du Sud", "Nîmes"));
        };
    }

    private static Product product(String reference, String name, String description, double price, int stock,
                                   boolean active, char grade, LocalDate releaseDate, LocalDateTime lastReviewAt,
                                   ProductStatus status, Priority priority, Category category, String company,
                                   String city) {
        Product product = new Product();
        product.setReference(reference);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setActive(active);
        product.setGrade(grade);
        product.setReleaseDate(releaseDate);
        product.setLastReviewAt(lastReviewAt);
        product.setStatus(status);
        product.setPriority(priority);
        product.setCategory(category);
        product.setSupplier(new Supplier(company, city));
        return product;
    }
}
