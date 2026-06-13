package com.brevery.controller;

import com.brevery.dto.request.CreateContactRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.entity.ContactMessage;
import com.brevery.repository.ContactMessageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Form liên hệ / hỗ trợ khách hàng")
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    @PostMapping
    @Operation(summary = "Gửi tin nhắn liên hệ (public — không cần đăng nhập)")
    public ResponseEntity<ApiResponse<Void>> submitContact(
            @Valid @RequestBody CreateContactRequest request) {
        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .isRead(false)
                .build();
        contactMessageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Gửi tin nhắn thành công. Chúng tôi sẽ phản hồi sớm!"));
    }
}
