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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryReceiptRepository receiptRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
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
        note = "[Nhập cho Size " + variant.getSize() + "] " + note;

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
    public Page<InventoryReceiptDTO> getReceipts(Pageable pageable) {
        return receiptRepository.findAll(pageable).map(this::mapToDTO);
    }

    private InventoryReceiptDTO mapToDTO(InventoryReceipt entity) {
        return InventoryReceiptDTO.builder()
                .receiptId(entity.getReceiptId())
                .productId(entity.getProduct().getProductId())
                .productName(entity.getProduct().getName())
                .quantity(entity.getQuantity())
                .importPrice(entity.getImportPrice())
                .supplier(entity.getSupplier())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
