package com.brevery.dto.response;

import com.brevery.enums.OrderStatus;
import com.brevery.enums.PaymentMethod;
import com.brevery.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderCode;
    private Long userId; // Null nếu là Guest
    
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount; // Thực tế phải trả
    
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    
    private String note;
    private String trackingToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String shippingAddress;
    private String receiverName;
    private String receiverPhone;

    private List<OrderDetailResponse> orderDetails;
}
