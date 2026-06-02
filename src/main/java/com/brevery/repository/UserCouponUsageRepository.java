package com.brevery.repository;

import com.brevery.entity.UserCouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCouponUsageRepository extends JpaRepository<UserCouponUsage, Long> {
    long countByUserUserIdAndCouponCouponId(Long userId, Long couponId);
}
