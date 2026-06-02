package com.brevery.service;

import com.brevery.dto.response.UserAdminResponse;
import com.brevery.entity.User;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.Role;
import com.brevery.exception.AppException;
import com.brevery.repository.OrderRepository;
import com.brevery.repository.RefreshTokenRepository;
import com.brevery.repository.UserRepository;
import com.brevery.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getAllUsersForAdmin(String search, Role role, Boolean isActive, Pageable pageable) {
        Specification<User> spec = UserSpecification.filterUsers(search, role, isActive);
        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(user -> {
            Long totalOrders = orderRepository.countByUserUserId(user.getUserId());
            BigDecimal totalSpent = orderRepository.sumTotalAmountByUserUserId(user.getUserId());
            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }

            return UserAdminResponse.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .isActive(user.getIsActive())
                    .createdAt(user.getCreatedAt())
                    .totalOrders(totalOrders)
                    .totalSpent(totalSpent)
                    .build();
        });
    }

    @Transactional
    public void toggleUserActiveState(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Toggle
        boolean newActiveState = !user.getIsActive();
        user.setIsActive(newActiveState);
        userRepository.save(user);

        log.info("Admin toggled active state of user {} to {}", userId, newActiveState);

        // Nếu khóa tài khoản, thu hồi tất cả refresh tokens để ép buộc logout ngay lập tức
        if (!newActiveState) {
            refreshTokenRepository.revokeAllByUserId(userId);
            log.info("Revoked all refresh tokens of locked user: {}", userId);
        }
    }
}
