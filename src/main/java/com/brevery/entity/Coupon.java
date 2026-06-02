package com.brevery.entity;

import com.brevery.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Coupons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "usages")
@EqualsAndHashCode(exclude = "usages")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 12, scale = 0)
    private BigDecimal maxDiscount;       // Giới hạn tối đa cho PERCENT

    @Column(precision = 12, scale = 0)
    private BigDecimal minOrderAmount;    // Giá trị đơn tối thiểu

    @Column(nullable = false)
    @Builder.Default
    private Integer usageLimit = 0;       // 0 = không giới hạn

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "coupon")
    @JsonIgnore
    @Builder.Default
    private List<UserCouponUsage> usages = new ArrayList<>();
}
