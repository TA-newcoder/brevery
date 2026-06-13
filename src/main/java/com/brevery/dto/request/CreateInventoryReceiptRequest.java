package com.brevery.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateInventoryReceiptRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Variant ID is required")
    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Import price is required")
    @Min(value = 0, message = "Import price must be non-negative")
    private BigDecimal importPrice;

    private String supplier;
    private String note;
}
