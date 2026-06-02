package com.brevery.repository;

import com.brevery.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrderOrderId(Long orderId);

    @Query("SELECT od2.variant.product.productId " +
            "FROM OrderDetail od1, OrderDetail od2 " +
            "WHERE od1.order.orderId = od2.order.orderId " +
            "  AND od1.variant.product.productId = :productId " +
            "  AND od2.variant.product.productId <> :productId " +
            "  AND od1.order.status <> com.brevery.enums.OrderStatus.CANCELLED " +
            "GROUP BY od2.variant.product.productId " +
            "ORDER BY COUNT(od2.detailId) DESC")
    List<Long> findFrequentlyBoughtWithProductIds(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT od.variant.product.category.categoryId " +
            "FROM OrderDetail od " +
            "WHERE od.order.user.userId = :userId " +
            "  AND od.order.status <> com.brevery.enums.OrderStatus.CANCELLED " +
            "GROUP BY od.variant.product.category.categoryId " +
            "ORDER BY COUNT(od.detailId) DESC")
    List<Long> findMostBoughtCategoryIds(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT od.variant.product.productId " +
            "FROM OrderDetail od " +
            "WHERE od.order.user.userId = :userId " +
            "  AND od.order.status <> com.brevery.enums.OrderStatus.CANCELLED")
    List<Long> findPurchasedProductIds(@Param("userId") Long userId);
}
