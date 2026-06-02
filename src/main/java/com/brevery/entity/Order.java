package com.brevery.entity;

import com.brevery.enums.OrderStatus;
import com.brevery.enums.PaymentMethod;
import com.brevery.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "coupon", "orderDetails", "shippingDetail", "reviews"})
@EqualsAndHashCode(exclude = {"user", "coupon", "orderDetails", "shippingDetail", "reviews"})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")  // nullable — cho phép Guest order
    @JsonIgnore
    private User user;

    @Column(nullable = false, unique = true, length = 20)
    private String orderCode;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal subTotal;

    @Column(nullable = false, precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 0)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponId")
    @JsonIgnore
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 500)
    private String note;

    @Column(unique = true, length = 100)
    private String trackingToken;  // UUID — cho Guest tra cứu đơn

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // === Relationships ===

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private ShippingDetail shippingDetail;

    @OneToMany(mappedBy = "order")
    @JsonIgnore
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
