package com.brevery.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ProductVariants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "product")
@EqualsAndHashCode(exclude = "product")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Column(precision = 12, scale = 0)
    private BigDecimal salePrice; // Giá khuyến mãi

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;
}
