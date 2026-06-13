package com.brevery.controller;

import com.brevery.dto.response.ApiResponse;
import com.brevery.entity.Banner;
import com.brevery.repository.BannerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/banners")
@RequiredArgsConstructor
@Tag(name = "Admin - Banner", description = "Quản lý banner trang chủ")
public class AdminBannerController {

    private final BannerRepository bannerRepository;

    @GetMapping
    @Operation(summary = "Lấy tất cả banner")
    public ResponseEntity<ApiResponse<List<Banner>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(bannerRepository.findAllByOrderByPositionAsc()));
    }

    @PostMapping
    @Operation(summary = "Tạo banner mới")
    public ResponseEntity<ApiResponse<Banner>> create(@RequestBody Banner banner) {
        banner.setBannerId(null);
        if (banner.getPosition() == null) banner.setPosition(0);
        if (banner.getIsActive() == null) banner.setIsActive(true);
        bannerRepository.save(banner);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(banner, "Thêm banner thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật banner")
    public ResponseEntity<ApiResponse<Banner>> update(@PathVariable Long id, @RequestBody Banner req) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setTitle(req.getTitle());
        banner.setImageUrl(req.getImageUrl());
        banner.setLink(req.getLink());
        banner.setPosition(req.getPosition() != null ? req.getPosition() : banner.getPosition());
        bannerRepository.save(banner);
        return ResponseEntity.ok(ApiResponse.success(banner, "Cập nhật thành công"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Banner>> toggle(@PathVariable Long id) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setIsActive(!Boolean.TRUE.equals(banner.getIsActive()));
        bannerRepository.save(banner);
        return ResponseEntity.ok(ApiResponse.success(banner));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bannerRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa banner thành công"));
    }
}
