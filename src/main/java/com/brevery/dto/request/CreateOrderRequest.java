package com.brevery.dto.request;

import com.brevery.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    // Thông tin giao hàng
    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String shippingAddress;

    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại nhận hàng không được để trống")
    private String receiverPhone;

    // Guest fields (chỉ bắt buộc nếu đặt hàng không đăng nhập)
    private String guestEmail;

    // Mã coupon áp dụng
    private String couponCode;

    // Phương thức thanh toán
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String note;

    // Danh sách sản phẩm mua hàng (chỉ bắt buộc cho Guest)
    private java.util.List<CartItemRequest> items;

    @Data
    public static class CartItemRequest {
        @NotNull(message = "ID biến thể không được để trống")
        private Long variantId;

        @NotNull(message = "Số lượng không được để trống")
        @jakarta.validation.constraints.Min(value = 1, message = "Số lượng tối thiểu là 1")
        private Integer quantity;
    }
}
