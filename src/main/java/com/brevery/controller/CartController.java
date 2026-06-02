package com.brevery.controller;

import com.brevery.dto.request.AddToCartRequest;
import com.brevery.dto.request.UpdateCartItemRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.CartResponse;
import com.brevery.security.CustomUserDetails;
import com.brevery.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Các API liên quan đến quản lý giỏ hàng của thành viên đã đăng nhập")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng hiện tại")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartResponse cart = cartService.getMyCart(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    @PostMapping("/add")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã thêm vào giỏ hàng thành công"));
    }

    @PutMapping("/{cartItemId}")
    @Operation(summary = "Cập nhật số lượng của một mục sản phẩm trong giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        cartService.updateCartItemQuantity(userDetails.getUserId(), cartItemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.ok("Đã cập nhật số lượng thành công"));
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "Xóa một mục sản phẩm khỏi giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId) {
        cartService.removeCartItem(userDetails.getUserId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa khỏi giỏ hàng thành công"));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Xóa sạch tất cả sản phẩm trong giỏ hàng")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        cartService.clearMyCart(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa sạch giỏ hàng thành công"));
    }
}
