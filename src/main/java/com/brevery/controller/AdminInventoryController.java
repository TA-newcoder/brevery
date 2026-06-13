package com.brevery.controller;

import com.brevery.dto.request.CreateInventoryReceiptRequest;
import com.brevery.dto.response.ApiResponse;
import com.brevery.dto.response.InventoryReceiptDTO;
import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.dto.response.ProductListDTO;
import com.brevery.entity.ProductVariant;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.ProductVariantRepository;
import com.brevery.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Inventory - Admin", description = "Quản lý kho hàng và phiếu nhập (ADMIN)")
public class AdminInventoryController {

    private final InventoryService inventoryService;
    private final ProductVariantRepository variantRepository;
    private final ProductMapper productMapper;

    @GetMapping("/low-stock")
    @Operation(summary = "Lấy danh sách các sản phẩm (theo biến thể) sắp hết hàng")
    public ResponseEntity<ApiResponse<List<Object>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        
        List<ProductVariant> variants = variantRepository.findByStockLessThan(threshold);
        
        // We map to a simple object for the frontend to show low stock
        List<Object> lowStockItems = variants.stream().map(v -> {
            var item = new java.util.HashMap<String, Object>();
            item.put("productId", v.getProduct().getProductId());
            item.put("productName", v.getProduct().getName());
            item.put("variantId", v.getVariantId());
            item.put("size", v.getSize());
            item.put("stock", v.getStock());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(lowStockItems));
    }

    @GetMapping("/receipts")
    @Operation(summary = "Lấy danh sách phiếu nhập kho")
    public ResponseEntity<ApiResponse<Page<InventoryReceiptDTO>>> getReceipts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getReceipts(pageRequest)));
    }

    @PostMapping("/receipts")
    @Operation(summary = "Tạo phiếu nhập kho mới (Cộng thêm tồn kho)")
    public ResponseEntity<ApiResponse<InventoryReceiptDTO>> createReceipt(
            @Valid @RequestBody CreateInventoryReceiptRequest request) {
        InventoryReceiptDTO receipt = inventoryService.createReceipt(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(receipt, "Tạo phiếu nhập kho thành công"));
    }
}
