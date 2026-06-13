package com.brevery.repository;

import com.brevery.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByProductProductIdAndStatus(Long productId, String status, Pageable pageable);
    
    Page<Review> findByStatus(String status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Long countActiveReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.product.productId = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    boolean existsByProductProductIdAndUserUserIdAndOrderOrderId(Long productId, Long userId, Long orderId);

    boolean existsByUserUserIdAndProductProductIdAndOrderOrderId(Long userId, Long productId, Long orderId);
}
