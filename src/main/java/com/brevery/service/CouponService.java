package com.brevery.service;

import com.brevery.entity.Coupon;
import com.brevery.enums.DiscountType;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.repository.CouponRepository;
import com.brevery.repository.UserCouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponUsageRepository userCouponUsageRepository;

    /**
     * Xác thực và tính toán giá trị giảm giá của Coupon.
     * Trả về số tiền giảm trừ (BigDecimal).
     */
    @Transactional(readOnly = true)
    public BigDecimal validateAndCalculateDiscount(String code, BigDecimal orderAmount, Long userId) {
        log.info("Validating coupon: {} for order amount: {}", code, orderAmount);

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));

        // 1. Kiểm tra trạng thái hoạt động
        if (!Boolean.TRUE.equals(coupon.getIsActive())) {
            throw new AppException(ErrorCode.COUPON_INACTIVE);
        }

        // 2. Kiểm tra thời gian hết hạn
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.COUPON_EXPIRED);
        }

        // 3. Kiểm tra giá trị đơn hàng tối thiểu
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new AppException(ErrorCode.COUPON_MIN_ORDER);
        }

        // 4. Kiểm tra giới hạn lượt dùng tổng thể
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0 && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new AppException(ErrorCode.COUPON_USAGE_LIMIT);
        }

        // 5. Kiểm tra giới hạn lượt dùng của từng User
        if (userId != null) {
            long usageCount = userCouponUsageRepository.countByUserUserIdAndCouponCouponId(userId, coupon.getCouponId());
            if (usageCount >= 1) { // Mặc định mỗi user chỉ được dùng 1 lần cho mỗi coupon
                throw new AppException(ErrorCode.COUPON_ALREADY_USED);
            }
        }

        // 6. Tính toán giá trị giảm trừ
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getDiscountType() == DiscountType.PERCENT) {
            // Tính phần trăm giảm giá
            BigDecimal percentFactor = coupon.getDiscountValue().divide(BigDecimal.valueOf(100));
            discount = orderAmount.multiply(percentFactor);
            // Giới hạn giảm giá tối đa
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        } else if (coupon.getDiscountType() == DiscountType.FIXED || coupon.getDiscountType() == DiscountType.FREE_SHIP) {
            discount = coupon.getDiscountValue();
        }

        // Không giảm giá vượt quá tổng giá trị đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }
}
