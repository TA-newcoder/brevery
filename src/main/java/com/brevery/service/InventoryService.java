package com.brevery.service;

import com.brevery.dto.request.CreateInventoryReceiptRequest;
import com.brevery.dto.response.InventoryReceiptDTO;
import com.brevery.entity.InventoryReceipt;
import com.brevery.entity.Product;
import com.brevery.entity.ProductVariant;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.repository.InventoryReceiptRepository;
import com.brevery.repository.ProductRepository;
import com.brevery.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.mapper.ProductMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryReceiptRepository receiptRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductMapper productMapper;
    private final ProductService productService; // To reuse cache eviction

    @Transactional
    public InventoryReceiptDTO createReceipt(CreateInventoryReceiptRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!variant.getProduct().getProductId().equals(product.getProductId())) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Variant does not belong to this product");
        }

        // Add size info to note if not already there
        String note = request.getNote() != null ? request.getNote() : "";
        if (!note.startsWith("[Size ")) {
            note = "[Size " + variant.getSize() + "] " + note;
        }

        InventoryReceipt receipt = InventoryReceipt.builder()
                .product(product)
                .quantity(request.getQuantity())
                .importPrice(request.getImportPrice())
                .supplier(request.getSupplier())
                .note(note)
                .build();

        InventoryReceipt savedReceipt = receiptRepository.save(receipt);

        // Update variant stock
        int newStock = variant.getStock() + request.getQuantity();
        variant.setStock(newStock);
        if (newStock > 0) {
            variant.setIsAvailable(true);
            if (!Boolean.TRUE.equals(product.getIsAvailable())) {
                product.setIsAvailable(true);
                productRepository.save(product);
            }
        }
        variantRepository.save(variant);

        // Evict cache
        productService.evictProductCache(product.getProductId());

        return mapToDTO(savedReceipt);
    }

    @Transactional(readOnly = true)
    public Page<InventoryReceiptDTO> getReceipts(Long productId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<InventoryReceipt> receipts;
        
        if (productId != null || startDate != null || endDate != null) {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
            receipts = receiptRepository.findByFilters(productId, start, end, pageable);
        } else {
            receipts = receiptRepository.findAll(pageable);
        }
        
        return receipts.map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductDetailDTO> getInventoryProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toDetailDTO);
    }

    @Transactional
    public void updateStock(Long variantId, Integer stock) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        
        int oldStock = variant.getStock();
        int diff = stock - oldStock;
        
        variant.setStock(stock);
        if (stock > 0) {
            variant.setIsAvailable(true);
            if (!Boolean.TRUE.equals(variant.getProduct().getIsAvailable())) {
                variant.getProduct().setIsAvailable(true);
                productRepository.save(variant.getProduct());
            }
        }
        variantRepository.save(variant);
        
        if (diff != 0) {
            String note = diff > 0 ? "Điều chỉnh tăng tồn kho" : "Điều chỉnh giảm tồn kho";
            note = "[Size " + variant.getSize() + "] " + note + " (Cập nhật trực tiếp)";
            
            InventoryReceipt receipt = InventoryReceipt.builder()
                    .product(variant.getProduct())
                    .quantity(diff)
                    .importPrice(java.math.BigDecimal.ZERO)
                    .note(note)
                    .build();
            receiptRepository.save(receipt);
        }
        productService.evictProductCache(variant.getProduct().getProductId());
    }

    private InventoryReceiptDTO mapToDTO(InventoryReceipt entity) {
        String note = entity.getNote();
        String size = "";
        if (note != null && note.startsWith("[Size ")) {
            int endIdx = note.indexOf("]");
            if (endIdx > -1) {
                size = note.substring(6, endIdx);
                note = note.substring(endIdx + 1).trim();
            }
        } else if (note != null && note.startsWith("[Nhập cho Size ")) {
            int endIdx = note.indexOf("]");
            if (endIdx > -1) {
                size = note.substring(15, endIdx);
                note = note.substring(endIdx + 1).trim();
            }
        }

        return InventoryReceiptDTO.builder()
                .receiptId(entity.getReceiptId())
                .productId(entity.getProduct().getProductId())
                .productName(entity.getProduct().getName())
                .categoryName(entity.getProduct().getCategory() != null ? entity.getProduct().getCategory().getName() : null)
                .variantName(size)
                .quantity(entity.getQuantity())
                .importPrice(entity.getImportPrice())
                .supplier(entity.getSupplier())
                .note(note)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
