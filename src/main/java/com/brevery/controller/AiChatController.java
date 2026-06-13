package com.brevery.controller;

import com.brevery.dto.request.ChatRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ChatResponse;
import com.brevery.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chatbot", description = "Các API tương tác với Trợ lý ảo AI tư vấn sản phẩm")
public class AiChatController {

    private final AiService aiService;

    @PostMapping("/chat")
    @Operation(summary = "Gửi tin nhắn trò chuyện và nhận câu trả lời tư vấn từ Trợ lý ảo AI")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest servletRequest) {

        String clientIp = servletRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = servletRequest.getRemoteAddr();
        }

        String reply = aiService.chat(request.getMessage(), clientIp, request.isAdmin());
        return ResponseEntity.ok(ApiResponse.success(new ChatResponse(reply)));
    }
}
