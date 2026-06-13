package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.entity.Banner;
import com.brevery.repository.BannerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banners")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "Lấy danh sách banner (Public)")
public class BannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    @Operation(summary = "Lấy danh sách banner đang hoạt động, sắp xếp theo position")
    public ResponseEntity<ApiResponse<List<Banner>>> getActiveBanners() {
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.findByIsActiveTrueOrderByPositionAsc()));
    }
}
