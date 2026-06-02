package com.brevery.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @Size(max = 200)
    private String name;

    private String description;

    private Long categoryId;

    private Boolean isAvailable;

    private String status;
}
