package com.brevery.repository;

import com.brevery.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.brevery.enums.OrderStatus;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUserUserId(Long userId, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);
    
    java.util.List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.orderId = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Long countByUserUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.userId = :userId AND o.status <> com.brevery.enums.OrderStatus.CANCELLED")
    BigDecimal sumTotalAmountByUserUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = com.brevery.enums.OrderStatus.COMPLETED")
    BigDecimal sumTotalAmountBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = com.brevery.enums.OrderStatus.COMPLETED")
    Long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query(value = "SELECT CAST(created_at AS DATE) as date, SUM(total_amount) as revenue, COUNT(order_id) as orderCount " +
                   "FROM orders " +
                   "WHERE created_at BETWEEN :startDate AND :endDate " +
                   "AND status = 'COMPLETED' " +
                   "GROUP BY CAST(created_at AS DATE) " +
                   "ORDER BY CAST(created_at AS DATE) ASC", nativeQuery = true)
    java.util.List<Object[]> getRevenueChartData(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o JOIN o.orderDetails od WHERE o.user.userId = :userId AND o.status = :status AND od.product.productId = :productId ORDER BY o.createdAt DESC")
    java.util.List<Order> findByUserAndStatusAndProduct(@Param("userId") Long userId, @Param("status") OrderStatus status, @Param("productId") Long productId);

    default Optional<Order> findFirstByUserUserIdAndStatusAndOrderDetails_Product_ProductId(Long userId, OrderStatus status, Long productId) {
        var list = findByUserAndStatusAndProduct(userId, status, productId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
