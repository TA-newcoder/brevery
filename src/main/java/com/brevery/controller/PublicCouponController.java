package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Public - Coupon", description = "Kiểm tra mã khuyến mãi")
public class PublicCouponController {

    private final CouponService couponService;

    @GetMapping("/{code}/validate")
    @Operation(summary = "Kiểm tra mã khuyến mãi")
    public ResponseEntity<ApiResponse<BigDecimal>> validateCoupon(
            @PathVariable String code,
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) Long userId) {

        BigDecimal discount = couponService.validateAndCalculateDiscount(code, orderAmount, userId);
        return ResponseEntity.ok(ApiResponse.success(discount, "Mã hợp lệ"));
    }
}
