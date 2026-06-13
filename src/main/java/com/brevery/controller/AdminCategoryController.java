package com.brevery.controller;

import com.brevery.dto.request.CreateCategoryRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.CategoryDTO;
import com.brevery.entity.Category;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Category", description = "Quản lý danh mục sản phẩm")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục (bao gồm cả ẩn)")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAll() {
        List<CategoryDTO> list = categoryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @PostMapping
    @Operation(summary = "Tạo danh mục mới")
    public ResponseEntity<ApiResponse<CategoryDTO>> create(
            @Valid @RequestBody CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();
        categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(toDTO(category), "Tạo danh mục thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật danh mục")
    public ResponseEntity<ApiResponse<CategoryDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        // Check duplicate name (exclude self)
        categoryRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getCategoryId().equals(id)) {
                throw new AppException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success(toDTO(category), "Cập nhật danh mục thành công"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Ẩn/Hiện danh mục")
    public ResponseEntity<ApiResponse<CategoryDTO>> toggle(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        category.setIsActive(!Boolean.TRUE.equals(category.getIsActive()));
        categoryRepository.save(category);
        String msg = Boolean.TRUE.equals(category.getIsActive()) ? "Đã hiện danh mục" : "Đã ẩn danh mục";
        return ResponseEntity.ok(ApiResponse.success(toDTO(category), msg));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa danh mục (chỉ khi không có sản phẩm)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (!category.getProducts().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Không thể xóa danh mục đang có sản phẩm");
        }
        categoryRepository.delete(category);
        return ResponseEntity.ok(ApiResponse.ok("Xóa danh mục thành công"));
    }

    private CategoryDTO toDTO(Category c) {
        return CategoryDTO.builder()
                .categoryId(c.getCategoryId())
                .name(c.getName())
                .description(c.getDescription())
                .imageUrl(c.getImageUrl())
                .build();
    }
}
