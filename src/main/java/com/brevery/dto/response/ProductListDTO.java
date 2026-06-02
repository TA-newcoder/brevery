package com.brevery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDTO implements Serializable {

    private Long productId;
    private String name;
    private String primaryImageUrl;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalSold;
    private Double avgRating;
    private Long reviewCount;
    private String categoryName;
    private String status;
}
