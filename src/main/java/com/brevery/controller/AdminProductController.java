package com.brevery.controller;

import com.brevery.dto.request.CreateProductRequest;
import com.brevery.dto.request.UpdateProductRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Product - Admin", description = "Quản lý sản phẩm cho Quản trị viên (ADMIN)")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo sản phẩm mới kèm các biến thể và danh sách file hình ảnh upload lên Cloudinary")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> createProduct(
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> imageFiles) {
        ProductDetailDTO product = productService.createProduct(request, imageFiles);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(product, "Tạo sản phẩm thành công"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thông tin cơ bản của sản phẩm")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductDetailDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product, "Cập nhật sản phẩm thành công"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Bật/Tắt trạng thái hiển thị (isAvailable) của sản phẩm")
    public ResponseEntity<ApiResponse<Void>> toggleProduct(@PathVariable Long id) {
        productService.toggleProductAvailability(id);
        return ResponseEntity.ok(ApiResponse.ok("Thay đổi trạng thái hiển thị thành công"));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Cập nhật số lượng tồn kho của biến thể sản phẩm",
               description = "Nếu số lượng > 0, biến thể và sản phẩm tự động ở trạng thái sẵn sàng bán (isAvailable = true)")
    public ResponseEntity<ApiResponse<Void>> updateVariantStock(
            @PathVariable("id") Long productId,
            @RequestParam Long variantId,
            @RequestParam Integer stock) {
        productService.updateVariantStock(productId, variantId, stock);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật tồn kho biến thể thành công"));
    }

    @PostMapping("/{id}/variants")
    @Operation(summary = "Thêm một biến thể (size, price, stock) mới cho sản phẩm có sẵn")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest.VariantRequest variantRequest) {
        ProductDetailDTO product = productService.addVariant(id, variantRequest);
        return ResponseEntity.ok(ApiResponse.success(product, "Thêm biến thể thành công"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa sản phẩm")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm thành công"));
    }
}
