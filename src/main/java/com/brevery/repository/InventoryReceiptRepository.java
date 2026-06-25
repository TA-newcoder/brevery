package com.brevery.repository;

import com.brevery.entity.InventoryReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InventoryReceiptRepository extends JpaRepository<InventoryReceipt, Long> {

    @Query("SELECT r FROM InventoryReceipt r WHERE " +
           "(:productId IS NULL OR r.product.productId = :productId) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate)")
    Page<InventoryReceipt> findByFilters(
           @Param("productId") Long productId,
           @Param("startDate") java.time.LocalDateTime startDate,
           @Param("endDate") java.time.LocalDateTime endDate,
           Pageable pageable);
}
