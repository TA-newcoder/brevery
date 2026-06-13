package com.brevery.controller;

import com.brevery.dto.request.CreateReviewRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ReviewDTO;
import com.brevery.entity.Order;
import com.brevery.entity.Product;
import com.brevery.entity.Review;
import com.brevery.entity.User;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.OrderStatus;
import com.brevery.exception.AppException;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.OrderRepository;
import com.brevery.repository.ProductRepository;
import com.brevery.repository.ReviewRepository;
import com.brevery.repository.UserRepository;
import com.brevery.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Đánh giá sản phẩm")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @PostMapping
    @Operation(summary = "Tạo đánh giá cho sản phẩm (yêu cầu đã mua và đã nhận hàng)")
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        Long userId = userDetails.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // Find a delivered order containing this product by this user
        Order order = orderRepository
                .findFirstByUserUserIdAndStatusAndOrderDetails_Product_ProductId(
                        userId, OrderStatus.DELIVERED, request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_DELIVERED));

        // Check if already reviewed this product in this order
        if (reviewRepository.existsByUserUserIdAndProductProductIdAndOrderOrderId(
                userId, request.getProductId(), order.getOrderId())) {
            throw new AppException(ErrorCode.ORDER_ALREADY_REVIEWED);
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .order(order)
                .rating(request.getRating())
                .comment(request.getComment())
                .status("APPROVED")
                .build();
        reviewRepository.save(review);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productMapper.toReviewDTO(review), "Đánh giá thành công"));
    }
}
