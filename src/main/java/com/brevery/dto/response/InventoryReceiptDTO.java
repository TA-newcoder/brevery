package com.brevery.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryReceiptDTO {
    private Long receiptId;
    private Long productId;
    private String productName;
    private String categoryName;
    private String variantName;
    private Integer quantity;
    private BigDecimal importPrice;
    private String supplier;
    private String note;
    private LocalDateTime createdAt;
}
