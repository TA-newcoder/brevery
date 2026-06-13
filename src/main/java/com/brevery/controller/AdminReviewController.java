package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ReviewDTO;
import com.brevery.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Review - Admin", description = "Quản lý bình luận, đánh giá của người dùng")
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "Lấy danh sách bình luận")
    public ResponseEntity<ApiResponse<Page<ReviewDTO>>> getAllReviews(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAllReviewsForAdmin(status, pageRequest)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái bình luận (APPROVED/HIDDEN)")
    public ResponseEntity<ApiResponse<ReviewDTO>> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.updateReviewStatus(id, status), "Cập nhật trạng thái thành công"));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Phản hồi bình luận")
    public ResponseEntity<ApiResponse<ReviewDTO>> replyToReview(
            @PathVariable Long id,
            @RequestParam String reply) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.replyToReview(id, reply), "Đã phản hồi bình luận"));
    }
}
