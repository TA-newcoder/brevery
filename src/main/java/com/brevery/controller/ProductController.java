package com.brevery.controller;

import com.brevery.dto.request.ProductFilterRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.dto.response.ProductListDTO;
import com.brevery.dto.response.ReviewDTO;
import com.brevery.security.CustomUserDetails;
import com.brevery.service.ProductService;
import com.brevery.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product - Public", description = "Các API tìm kiếm, xem chi tiết sản phẩm, review và gợi ý sản phẩm AI")
public class ProductController {

    private final ProductService productService;
    private final RecommendationService recommendationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách sản phẩm với bộ lọc động", description = "Tìm kiếm theo từ khóa, danh mục, khoảng giá, và sắp xếp linh hoạt.")
    public ResponseEntity<ApiResponse<Page<ProductListDTO>>> getProducts(
            @ModelAttribute ProductFilterRequest filter) {
        Page<ProductListDTO> products = productService.getProducts(filter);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết sản phẩm theo ID")
    public ResponseEntity<ApiResponse<?>> getProductDetail(
            @PathVariable Long id) {
        try {
            ProductDetailDTO product = productService.getProductDetail(id);
            return ResponseEntity.ok(ApiResponse.success(product));
        } catch (com.brevery.exception.AppException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder().status(404).message(e.getMessage()).build());
        }
    }

    @GetMapping("/{id}/reviews")
    @Operation(summary = "Lấy danh sách review được hiển thị công khai (Phân trang cố định 5/trang)")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page) {
        // Fix cứng size = 5 theo spec
        Pageable pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ReviewDTO> reviews = productService.getProductReviews(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/recommendations/frequent/{id}")
    @Operation(summary = "Lấy gợi ý các sản phẩm thường được mua cùng sản phẩm hiện tại")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getFrequentlyBoughtWith(
            @PathVariable Long id,
            @RequestParam(defaultValue = "4") int limit) {
        List<ProductListDTO> recommendations = recommendationService.getFrequentlyBoughtWith(id, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/personal")
    @Operation(summary = "Lấy gợi ý sản phẩm cá nhân hóa dựa trên lịch sử mua sắm của thành viên")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getPersonalizedRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "4") int limit) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        List<ProductListDTO> recommendations = recommendationService.getPersonalizedRecommendations(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/top")
    @Operation(summary = "Lấy danh sách sản phẩm bán chạy nhất")
    public ResponseEntity<ApiResponse<List<ProductListDTO>>> getTopSelling(
            @RequestParam(defaultValue = "4") int limit) {
        List<ProductListDTO> recommendations = recommendationService.getTopSelling(limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }
}
