package com.brevery.repository;

import com.brevery.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p FROM Product p WHERE p.productId = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    List<Product> findByCategoryCategoryIdAndIsAvailableTrue(Long categoryId);

    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.totalSold DESC")
    List<Product> findTopSelling(Pageable pageable);
}
