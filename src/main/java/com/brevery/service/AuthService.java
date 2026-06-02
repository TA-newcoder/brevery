package com.brevery.service;

import com.brevery.dto.request.*;
import com.brevery.dto.response.AuthResponse;
import com.brevery.entity.EmailVerification;
import com.brevery.entity.RefreshToken;
import com.brevery.entity.User;
import com.brevery.enums.ErrorCode;
import com.brevery.enums.Role;
import com.brevery.enums.TokenType;
import com.brevery.exception.AppException;
import com.brevery.repository.EmailVerificationRepository;
import com.brevery.repository.RefreshTokenRepository;
import com.brevery.repository.UserRepository;
import com.brevery.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    private static final String LOGIN_ATTEMPT_PREFIX = "login:attempts:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOGIN_LOCK_MINUTES = 15;
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // ==================== REGISTER ====================

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Tạo user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.USER)
                .isActive(true)
                .isEmailVerified(false)
                .build();
        userRepository.save(user);

        // Tạo token xác thực email (async gửi mail)
        String verifyToken = UUID.randomUUID().toString();
        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .token(verifyToken)
                .type(TokenType.VERIFY_EMAIL)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationRepository.save(verification);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), verifyToken);

        // Tạo tokens
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(), user.getRole().name(), user.getIsActive());

        return AuthResponse.of(accessToken, user.getUserId(),
                user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getAvatarUrl());
    }

    // ==================== LOGIN ====================

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest,
                               HttpServletResponse httpResponse) {
        String clientIp = getClientIp(httpRequest);

        // Rate limit kiểm tra
        checkLoginRateLimit(clientIp);

        // Tìm user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    incrementLoginAttempt(clientIp);
                    return new AppException(ErrorCode.INVALID_CREDENTIALS);
                });

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            incrementLoginAttempt(clientIp);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Kiểm tra tài khoản bị khóa
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Xóa bộ đếm login attempt
        clearLoginAttempts(clientIp);

        // Tạo access token
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(), user.getRole().name(), user.getIsActive());

        // Tạo refresh token → lưu DB + set HttpOnly Cookie
        String refreshTokenStr = jwtService.generateRefreshToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        addRefreshTokenCookie(httpResponse, refreshTokenStr);

        return AuthResponse.of(accessToken, user.getUserId(),
                user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getAvatarUrl());
    }

    // ==================== REFRESH ====================

    @Transactional
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenStr = extractRefreshTokenFromCookie(request);
        if (refreshTokenStr == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndIsRevokedFalse(refreshTokenStr)
                .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_REVOKED));

        // Kiểm tra hết hạn
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();
        if (!user.getIsActive()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }

        // Rotate: revoke token cũ, tạo token mới
        refreshToken.setIsRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newRefreshTokenStr = jwtService.generateRefreshToken();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenStr)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        addRefreshTokenCookie(response, newRefreshTokenStr);

        String accessToken = jwtService.generateAccessToken(
                user.getUserId(), user.getRole().name(), user.getIsActive());

        return AuthResponse.of(accessToken, user.getUserId(),
                user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getAvatarUrl());
    }

    // ==================== LOGOUT ====================

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenStr = extractRefreshTokenFromCookie(request);
        if (refreshTokenStr != null) {
            refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenStr)
                    .ifPresent(rt -> {
                        rt.setIsRevoked(true);
                        refreshTokenRepository.save(rt);
                    });
        }
        clearRefreshTokenCookie(response);
    }

    // ==================== VERIFY EMAIL ====================

    @Transactional
    public void verifyEmail(String token) {
        EmailVerification verification = emailVerificationRepository
                .findByTokenAndTypeAndIsUsedFalse(token, TokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setIsEmailVerified(true);
        userRepository.save(user);
    }

    // ==================== FORGOT PASSWORD ====================

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Không tiết lộ email có tồn tại hay không — luôn trả success
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            EmailVerification verification = EmailVerification.builder()
                    .user(user)
                    .token(resetToken)
                    .type(TokenType.RESET_PASSWORD)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            emailVerificationRepository.save(verification);
            emailService.sendResetPasswordEmail(user.getEmail(), user.getFullName(), resetToken);
        });
    }

    // ==================== RESET PASSWORD ====================

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        EmailVerification verification = emailVerificationRepository
                .findByTokenAndTypeAndIsUsedFalse(request.getToken(), TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.VERIFICATION_TOKEN_EXPIRED);
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = verification.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke tất cả refresh tokens — force re-login
        refreshTokenRepository.revokeAllByUserId(user.getUserId());
    }

    // ==================== HELPER: Rate Limiting ====================

    private void checkLoginRateLimit(String clientIp) {
        try {
            String key = LOGIN_ATTEMPT_PREFIX + clientIp;
            String attempts = redisTemplate.opsForValue().get(key);
            if (attempts != null && Integer.parseInt(attempts) >= MAX_LOGIN_ATTEMPTS) {
                throw new AppException(ErrorCode.LOGIN_RATE_LIMITED);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            // Redis không khả dụng (dev mode) → bỏ qua rate limiting
            log.debug("Rate limiting skipped — Redis unavailable");
        }
    }

    private void incrementLoginAttempt(String clientIp) {
        try {
            String key = LOGIN_ATTEMPT_PREFIX + clientIp;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.debug("Login attempt tracking skipped — Redis unavailable");
        }
    }

    private void clearLoginAttempts(String clientIp) {
        try {
            redisTemplate.delete(LOGIN_ATTEMPT_PREFIX + clientIp);
        } catch (Exception e) {
            // Ignore
        }
    }

    // ==================== HELPER: Cookie ====================

    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);   // true trong production
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge((int) (jwtService.getRefreshExpiration() / 1000));  // 7 ngày
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
