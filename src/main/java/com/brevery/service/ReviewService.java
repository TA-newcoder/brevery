package com.brevery.service;

import com.brevery.dto.response.ReviewDTO;
import com.brevery.entity.Review;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getAllReviewsForAdmin(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return reviewRepository.findByStatus(status, pageable)
                    .map(productMapper::toReviewDTO);
        }
        return reviewRepository.findAll(pageable)
                .map(productMapper::toReviewDTO);
    }

    @Transactional
    public ReviewDTO updateReviewStatus(Long reviewId, String status) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Review not found"));
        review.setStatus(status);
        return productMapper.toReviewDTO(reviewRepository.save(review));
    }

    @Transactional
    public ReviewDTO replyToReview(Long reviewId, String adminReply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Review not found"));
        review.setAdminReply(adminReply);
        return productMapper.toReviewDTO(reviewRepository.save(review));
    }
}
