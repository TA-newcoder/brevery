package com.brevery.controller;

import com.brevery.dto.request.CreateOrderRequest;
import com.brevery.dto.request.CreateReviewRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.OrderResponse;
import com.brevery.security.CustomUserDetails;
import com.brevery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order - Public & User", description = "Các API thanh toán đơn hàng, lịch sử mua hàng, tra cứu, hủy và đánh giá sản phẩm")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Đặt hàng thanh toán",
               description = "Hỗ trợ cả Thành viên đã đăng nhập (lấy từ giỏ hàng hệ thống) và Khách vãng lai (truyền danh sách items trực tiếp).")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Đặt hàng thành công"));
    }

    @GetMapping("/history")
    @Operation(summary = "Xem lịch sử mua hàng của thành viên đăng nhập")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrderHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> response = orderService.getMyOrders(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/track")
    @Operation(summary = "Tra cứu đơn hàng cho Khách vãng lai và Thành viên")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(
            @RequestParam String code,
            @RequestParam String phone) {

        OrderResponse response = orderService.trackOrder(code, phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Hủy đơn hàng của thành viên (Chỉ được hủy khi trạng thái là PENDING)")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {

        orderService.cancelOrder(orderId, userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn hàng thành công"));
    }

    @PostMapping("/{orderId}/reviews")
    @Operation(summary = "Đánh giá sản phẩm trong đơn hàng của thành viên (Chỉ áp dụng khi đơn hàng ở trạng thái DELIVERED)")
    public ResponseEntity<ApiResponse<Void>> addReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateReviewRequest request) {

        orderService.addReview(orderId, userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đăng đánh giá thành công"));
    }
}
