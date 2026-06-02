package com.brevery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long cartItemId;
    private Long variantId;
    private String productName;
    private String productSize;
    private String primaryImageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;
}
