package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.service.UtilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/utils")
@RequiredArgsConstructor
@Tag(name = "Utility", description = "Các API tiện ích: Geolocation, URL Shortener")
public class UtilityController {

    private final UtilityService utilityService;

    @GetMapping("/location")
    @Operation(summary = "Lấy thông tin vị trí dựa trên IP của người dùng")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLocation(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        Map<String, Object> location = utilityService.getLocationByIp(ip);
        return ResponseEntity.ok(ApiResponse.success(location));
    }

    @PostMapping("/shorten-url")
    @Operation(summary = "Rút gọn URL (sử dụng cleanuri.com)")
    public ResponseEntity<ApiResponse<String>> shortenUrl(@RequestBody Map<String, String> payload) {
        String url = payload.get("url");
        String shortened = utilityService.shortenUrl(url);
        return ResponseEntity.ok(ApiResponse.success(shortened));
    }
}
