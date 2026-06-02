package com.brevery.repository;

import com.brevery.entity.ShippingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingDetailRepository extends JpaRepository<ShippingDetail, Long> {
    Optional<ShippingDetail> findByOrderOrderId(Long orderId);
}
