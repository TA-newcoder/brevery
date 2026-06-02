package com.brevery.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private String status;

    @Valid
    @Size(min = 1, message = "Phải có ít nhất 1 biến thể")
    private List<VariantRequest> variants;

    @Data
    public static class VariantRequest {
        @NotBlank(message = "Size không được để trống")
        private String size;

        @NotNull(message = "Giá không được để trống")
        private BigDecimal price;

        private Integer stock = 0;
    }
}
