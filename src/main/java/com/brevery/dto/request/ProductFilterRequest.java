package com.brevery.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductFilterRequest {

    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStockOnly;
    private String search;
    private String sortBy = "newest";   // newest | price_asc | price_desc | best_selling | rating
    private int page = 0;
    private int size = 12;

    /**
     * Giới hạn page size tối đa 50.
     */
    public int getSize() {
        return Math.min(size, 50);
    }

    public String getCacheKey() {
        return String.format("cat_%s_p_%s_%s_stock_%s_q_%s_sort_%s_page_%s_size_%s",
                categoryId == null ? "all" : categoryId,
                minPrice == null ? "0" : minPrice,
                maxPrice == null ? "inf" : maxPrice,
                inStockOnly != null && inStockOnly,
                search == null ? "" : search.trim().toLowerCase(),
                sortBy,
                page,
                getSize());
    }
}
