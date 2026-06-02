package com.brevery.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDTO {
    private Long categoryId;
    private String name;
    private String description;
    private String imageUrl;
}
