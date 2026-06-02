package com.brevery.component;

import com.brevery.entity.Category;
import com.brevery.entity.Product;
import com.brevery.entity.ProductVariant;
import com.brevery.repository.CategoryRepository;
import com.brevery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            log.info("Database already seeded. Skipping DataSeeder.");
            return;
        }
        
        log.info("Starting to seed 50 mock products...");

        String[] categoryNames = {"Bánh kem", "Bánh mì", "Bánh ngọt", "Trà sữa", "Cà phê"};
        List<Category> categories = new ArrayList<>();

        for (String catName : categoryNames) {
            Category cat = Category.builder()
                    .name(catName)
                    .description("Mô tả " + catName)
                    .imageUrl("")
                    .isActive(true)
                    .build();
            categories.add(cat);
        }
        categoryRepository.saveAll(categories);

        Random random = new Random();
        List<Product> productsToSave = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            int catIndex = (i - 1) % 5;
            Category category = categories.get(catIndex);
            
            Product product = Product.builder()
                    .category(category)
                    .name("Sản phẩm " + category.getName() + " " + i)
                    .description("Mô tả cho sản phẩm " + i + ". Rất ngon và chất lượng.")
                    .isAvailable(true)
                    .totalSold(random.nextInt(100))
                    .variants(new ArrayList<>())
                    .build();

            int price = (random.nextInt(131) + 20) * 1000;
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .size("M")
                    .price(new BigDecimal(price))
                    .stock(100)
                    .isAvailable(true)
                    .build();

            product.getVariants().add(variant);
            productsToSave.add(product);
        }

        productRepository.saveAll(productsToSave);
        log.info("Successfully seeded 50 mock products!");
    }
}
