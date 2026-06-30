package com.brevery.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // === Auth ===
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_001", "Email đã được sử dụng"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_013", "Số điện thoại đã được sử dụng"),
    NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH_014", "Họ tên này đã được sử dụng"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "Email hoặc mật khẩu không đúng"),
    ACCOUNT_LOCKED(HttpStatus.FORBIDDEN, "AUTH_003", "Tài khoản đã bị khóa"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "AUTH_004", "Tài khoản chưa xác thực email"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "Token không hợp lệ hoặc đã hết hạn"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_006", "Refresh token đã hết hạn"),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "AUTH_007", "Refresh token đã bị thu hồi"),
    LOGIN_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AUTH_008", "Đăng nhập quá nhiều lần, vui lòng thử lại sau 15 phút"),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_009", "Gửi email thất bại"),
    VERIFICATION_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "AUTH_010", "Link xác thực đã hết hạn"),
    VERIFICATION_TOKEN_USED(HttpStatus.BAD_REQUEST, "AUTH_011", "Link xác thực đã được sử dụng"),
    PASSWORD_TOO_WEAK(HttpStatus.BAD_REQUEST, "AUTH_012", "Mật khẩu phải có ít nhất 8 ký tự"),

    // === User ===
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "Không tìm thấy người dùng"),
    USER_ALREADY_ACTIVE(HttpStatus.BAD_REQUEST, "USER_002", "Tài khoản đã được kích hoạt"),

    // === Product ===
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROD_001", "Không tìm thấy sản phẩm"),
    PRODUCT_UNAVAILABLE(HttpStatus.BAD_REQUEST, "PROD_002", "Sản phẩm hiện không khả dụng"),
    VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROD_003", "Không tìm thấy biến thể sản phẩm"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROD_004", "Không tìm thấy danh mục"),
    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROD_005", "Tên danh mục đã tồn tại"),

    // === Cart ===
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "Không tìm thấy sản phẩm trong giỏ hàng"),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "CART_002", "Sản phẩm không đủ số lượng tồn kho"),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_003", "Giỏ hàng trống"),

    // === Order ===
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "Không tìm thấy đơn hàng"),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "ORDER_002", "Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận"),
    ORDER_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "ORDER_003", "Không thể chuyển trạng thái đơn hàng"),
    ORDER_ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "ORDER_004", "Đơn hàng đã được đánh giá"),
    ORDER_NOT_DELIVERED(HttpStatus.BAD_REQUEST, "ORDER_005", "Chỉ có thể đánh giá đơn hàng đã giao"),

    // === Coupon ===
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_001", "Không tìm thấy mã giảm giá"),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON_002", "Mã giảm giá đã hết hạn"),
    COUPON_USAGE_LIMIT(HttpStatus.BAD_REQUEST, "COUPON_003", "Mã giảm giá đã hết lượt sử dụng"),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUPON_004", "Bạn đã sử dụng mã giảm giá này"),
    COUPON_MIN_ORDER(HttpStatus.BAD_REQUEST, "COUPON_005", "Đơn hàng chưa đạt giá trị tối thiểu"),
    COUPON_INACTIVE(HttpStatus.BAD_REQUEST, "COUPON_006", "Mã giảm giá không còn hoạt động"),

    // === File Upload ===
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_001", "File không được vượt quá 5MB"),
    FILE_INVALID_TYPE(HttpStatus.BAD_REQUEST, "FILE_002", "Chỉ chấp nhận file ảnh JPEG, PNG, WebP"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_003", "Upload file thất bại"),

    // === AI ===
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI_001", "Bạn đã gửi quá nhiều tin nhắn, vui lòng thử lại sau"),
    AI_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI_002", "Dịch vụ AI tạm thời không khả dụng"),

    // === General ===
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "GEN_001", "Dữ liệu không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "GEN_002", "Bạn cần đăng nhập để thực hiện thao tác này"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "GEN_003", "Bạn không có quyền thực hiện thao tác này"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GEN_004", "Đã xảy ra lỗi hệ thống");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
