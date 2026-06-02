package com.brevery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String message;
}
