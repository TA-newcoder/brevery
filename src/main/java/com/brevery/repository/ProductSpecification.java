package com.brevery.repository;

import com.brevery.dto.request.ProductFilterRequest;
import com.brevery.entity.Category;
import com.brevery.entity.Product;
import com.brevery.entity.ProductVariant;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(ProductFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn mặc định chỉ lấy sản phẩm có isAvailable = true
            // Trừ khi có cấu hình đặc biệt cho admin nhưng filter này dùng cho public nên isAvailable = true
            predicates.add(cb.equal(root.get("isAvailable"), true));

            // Lọc theo Category
            if (filter.getCategoryId() != null) {
                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(cb.equal(categoryJoin.get("categoryId"), filter.getCategoryId()));
            }

            // Lọc theo khoảng giá (cần join với variants)
            if (filter.getMinPrice() != null || filter.getMaxPrice() != null) {
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                if (filter.getMinPrice() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), filter.getMinPrice()));
                }
                if (filter.getMaxPrice() != null) {
                    predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), filter.getMaxPrice()));
                }
            }

            // Chỉ lấy sản phẩm còn hàng
            if (Boolean.TRUE.equals(filter.getInStockOnly())) {
                Join<Product, ProductVariant> variantJoin = root.join("variants");
                predicates.add(cb.greaterThan(variantJoin.get("stock"), 0));
            }

            // Tìm kiếm theo tên hoặc mô tả
            if (StringUtils.hasText(filter.getSearch())) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            // Đảm bảo không lấy trùng lặp do join variants
            if (query != null) {
                query.distinct(true);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
