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

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.orderId = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.userId = :userId")
    Long countByUserUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.userId = :userId AND o.status <> com.brevery.enums.OrderStatus.CANCELLED")
    BigDecimal sumTotalAmountByUserUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status <> com.brevery.enums.OrderStatus.CANCELLED")
    BigDecimal sumTotalAmountBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status <> com.brevery.enums.OrderStatus.CANCELLED")
    Long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
}
