package com.brevery.service;

import com.brevery.dto.request.CreateProductRequest;
import com.brevery.dto.request.ProductFilterRequest;
import com.brevery.dto.request.UpdateProductRequest;
import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.dto.response.ProductListDTO;
import com.brevery.dto.response.ReviewDTO;
import com.brevery.entity.Category;
import com.brevery.entity.Product;
import com.brevery.entity.ProductImage;
import com.brevery.entity.ProductVariant;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.CategoryRepository;
import com.brevery.repository.ProductImageRepository;
import com.brevery.repository.ProductRepository;
import com.brevery.repository.ProductSpecification;
import com.brevery.repository.ProductVariantRepository;
import com.brevery.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;
    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_LIST_PREFIX = "products:list:";
    private static final String CACHE_DETAIL_PREFIX = "products:detail:";

    // ==================== PUBLIC METHODS ====================

    @Transactional(readOnly = true)
    @Cacheable(value = "products_list", key = "#filter.getCacheKey()", unless = "#result == null")
    public Page<ProductListDTO> getProducts(ProductFilterRequest filter) {
        log.info("Fetching products from DB for filter: {}", filter.getCacheKey());

        Sort sort = getSortOrder(filter.getSortBy());
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);

        Specification<Product> spec = ProductSpecification.filterProducts(filter);
        Page<Product> products = productRepository.findAll(spec, pageable);

        return products.map(productMapper::toListDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products_detail", key = "#id", unless = "#result == null")
    public ProductDetailDTO getProductDetail(Long id) {
        log.info("Fetching product detail from DB for ID: {}", id);

        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!Boolean.TRUE.equals(product.getIsAvailable())) {
            throw new AppException(ErrorCode.PRODUCT_UNAVAILABLE);
        }

        return productMapper.toDetailDTO(product);
    }

    public Page<ReviewDTO> getProductReviews(Long id, Pageable pageable) {
        // Chỉ lấy các review được hiển thị công khai (isVisible = true)
        return reviewRepository.findByProductProductIdAndIsVisibleTrue(id, pageable)
                .map(productMapper::toReviewDTO);
    }

    // ==================== ADMIN METHODS ====================

    @Transactional
    public ProductDetailDTO createProduct(CreateProductRequest request, List<MultipartFile> imageFiles) {
        log.info("Admin creating product: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // 1. Tạo và lưu Product
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .isAvailable(request.getStatus() == null || !"PAUSED".equals(request.getStatus()))
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .totalSold(0)
                .build();

        Product savedProduct = productRepository.save(product);

        // 2. Upload & Lưu ảnh qua Cloudinary
        List<ProductImage> productImages = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            boolean isFirst = true;
            int order = 0;
            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) continue;
                String secureUrl = cloudinaryService.uploadImage(file);

                ProductImage img = ProductImage.builder()
                        .product(savedProduct)
                        .imageUrl(secureUrl)
                        .isPrimary(isFirst)
                        .sortOrder(order++)
                        .build();

                productImages.add(productImageRepository.save(img));
                isFirst = false;
            }
        }
        savedProduct.setImages(productImages);

        // 3. Tạo và lưu Variants
        List<ProductVariant> variants = new ArrayList<>();
        for (CreateProductRequest.VariantRequest varReq : request.getVariants()) {
            ProductVariant variant = ProductVariant.builder()
                    .product(savedProduct)
                    .size(varReq.getSize())
                    .price(varReq.getPrice())
                    .stock(varReq.getStock())
                    .isAvailable(varReq.getStock() > 0)
                    .build();

            variants.add(productVariantRepository.save(variant));
        }
        savedProduct.setVariants(variants);

        // Clear cache
        evictProductCache(null);

        return productMapper.toDetailDTO(savedProduct);
    }

    @Transactional
    public ProductDetailDTO updateProduct(Long id, UpdateProductRequest request) {
        log.info("Admin updating product ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getIsAvailable() != null) product.setIsAvailable(request.getIsAvailable());
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
            if ("PAUSED".equals(request.getStatus())) {
                product.setIsAvailable(false);
            } else if ("ACTIVE".equals(request.getStatus())) {
                product.setIsAvailable(true);
            }
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        evictProductCache(id);

        return productMapper.toDetailDTO(saved);
    }

    @Transactional
    public void toggleProductAvailability(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        product.setIsAvailable(!product.getIsAvailable());
        productRepository.save(product);

        evictProductCache(id);
        log.info("Toggled availability of product ID: {} to {}", id, product.getIsAvailable());
    }

    @Transactional
    public void updateVariantStock(Long productId, Long variantId, Integer stock) {
        if (stock < 0) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Tồn kho không được âm");
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Biến thể không thuộc sản phẩm này");
        }

        variant.setStock(stock);
        if (stock > 0) {
            variant.setIsAvailable(true);
            // Nếu tăng tồn kho, tự động cho sản phẩm hiển thị lại nếu trước đó bị tắt (tùy thuộc logic nghiệp vụ)
            Product product = variant.getProduct();
            if (!Boolean.TRUE.equals(product.getIsAvailable())) {
                product.setIsAvailable(true);
                productRepository.save(product);
            }
        } else {
            variant.setIsAvailable(false);
        }

        productVariantRepository.save(variant);
        evictProductCache(productId);
    }

    @Transactional
    public ProductDetailDTO addVariant(Long productId, CreateProductRequest.VariantRequest varReq) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .size(varReq.getSize())
                .price(varReq.getPrice())
                .stock(varReq.getStock())
                .isAvailable(varReq.getStock() > 0)
                .build();

        productVariantRepository.save(variant);
        evictProductCache(productId);

        return productMapper.toDetailDTO(productRepository.findByIdWithDetails(productId).get());
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Admin deleting product ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        productRepository.delete(product);
        evictProductCache(id);
    }

    // ==================== CACHE EVICTION HELPER ====================

    public void evictProductCache(Long id) {
        try {
            // 1. Xóa chi tiết sản phẩm cụ thể
            if (id != null) {
                // Xóa cả cache trên Spring Cache manager
                // Vì ta dùng "products_detail" và "products_list"
                redisTemplate.delete("products_detail::" + id);
                log.info("Evicted detail cache for product: {}", id);
            }

            // 2. Xóa tất cả danh sách sản phẩm (products:list:*)
            Set<String> keys = redisTemplate.keys("products_list::*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Evicted {} product list cache entries", keys.size());
            }
        } catch (Exception e) {
            log.error("Failed to evict Redis cache. This is non-fatal in dev mode.", e);
        }
    }

    // ==================== HELPER METHODS ====================

    private Sort getSortOrder(String sortBy) {
        if (sortBy == null) return Sort.by(Sort.Direction.DESC, "createdAt");

        return switch (sortBy.toLowerCase()) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "variants.price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "variants.price");
            case "best_selling" -> Sort.by(Sort.Direction.DESC, "totalSold");
            case "rating" -> Sort.by(Sort.Direction.DESC, "createdAt"); // Tạm thời sắp xếp theo mới nhất nếu chưa có rating
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
