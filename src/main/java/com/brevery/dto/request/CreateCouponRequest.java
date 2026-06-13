package com.brevery.dto.request;

import com.brevery.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {

    @NotBlank(message = "Mã coupon không được để trống")
    @Size(max = 30, message = "Mã coupon tối đa 30 ký tự")
    private String code;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;
    private BigDecimal minOrderAmount;

    @Min(value = 0, message = "Giới hạn lượt dùng không được âm")
    private Integer usageLimit;

    @NotNull(message = "Ngày hết hạn không được để trống")
    private LocalDateTime expiryDate;
}
