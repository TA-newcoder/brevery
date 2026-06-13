package com.brevery.controller;

import com.brevery.dto.request.CreateCouponRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.CouponResponse;
import com.brevery.entity.Coupon;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.repository.CouponRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "Admin - Coupon", description = "Quản lý mã khuyến mãi")
public class AdminCouponController {

    private final CouponRepository couponRepository;

    @GetMapping
    @Operation(summary = "Lấy tất cả coupon")
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAll() {
        List<CouponResponse> list = couponRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Tạo coupon mới")
    public ResponseEntity<ApiResponse<CouponResponse>> create(
            @Valid @RequestBody CreateCouponRequest request) {
        if (couponRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Mã coupon đã tồn tại");
        }
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderAmount(request.getMinOrderAmount())
                .usageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 0)
                .usedCount(0)
                .expiryDate(request.getExpiryDate())
                .isActive(true)
                .build();
        couponRepository.save(coupon);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(toResponse(coupon), "Tạo coupon thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        // Check duplicate code (exclude self)
        couponRepository.findByCodeIgnoreCase(request.getCode()).ifPresent(existing -> {
            if (!existing.getCouponId().equals(id)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR, "Mã coupon đã tồn tại");
            }
        });
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMaxDiscount(request.getMaxDiscount());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setUsageLimit(request.getUsageLimit() != null ? request.getUsageLimit() : 0);
        coupon.setExpiryDate(request.getExpiryDate());
        couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.success(toResponse(coupon), "Cập nhật coupon thành công"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Bật/Tắt coupon")
    public ResponseEntity<ApiResponse<CouponResponse>> toggle(@PathVariable Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        coupon.setIsActive(!Boolean.TRUE.equals(coupon.getIsActive()));
        couponRepository.save(coupon);
        return ResponseEntity.ok(ApiResponse.success(toResponse(coupon)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa coupon")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COUPON_NOT_FOUND));
        couponRepository.delete(coupon);
        return ResponseEntity.ok(ApiResponse.ok("Xóa coupon thành công"));
    }

    private CouponResponse toResponse(Coupon c) {
        return CouponResponse.builder()
                .couponId(c.getCouponId())
                .code(c.getCode())
                .discountType(c.getDiscountType())
                .discountValue(c.getDiscountValue())
                .maxDiscount(c.getMaxDiscount())
                .minOrderAmount(c.getMinOrderAmount())
                .usageLimit(c.getUsageLimit())
                .usedCount(c.getUsedCount())
                .expiryDate(c.getExpiryDate())
                .isActive(c.getIsActive())
                .build();
    }
}
