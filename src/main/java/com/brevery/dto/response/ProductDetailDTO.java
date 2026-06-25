package com.brevery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO implements Serializable {

    private Long productId;
    private String name;
    private String description;
    private String categoryName;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minSalePrice;
    private Integer totalSold;
    private Double avgRating;
    private Long reviewCount;
    private Boolean isAvailable;
    private String status;
    private LocalDateTime createdAt;
    private Integer totalStock;

    private List<VariantDTO> variants;
    private List<ImageDTO> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantDTO implements Serializable {
        private Long variantId;
        private String size;
        private BigDecimal price;
        private BigDecimal salePrice;
        private Integer stock;
        private Boolean isAvailable;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDTO implements Serializable {
        private Long imageId;
        private String imageUrl;
        private Boolean isPrimary;
        private Integer sortOrder;
    }
}
