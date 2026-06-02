package com.brevery.service;

import com.brevery.dto.response.ProductListDTO;
import com.brevery.entity.Product;
import com.brevery.mapper.ProductMapper;
import com.brevery.repository.OrderDetailRepository;
import com.brevery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Lấy các sản phẩm thường mua cùng sản phẩm hiện tại, cache 1 giờ
     */
    @Cacheable(value = "recommendations_frequent", key = "#productId", unless = "#result == null || #result.isEmpty()")
    public List<ProductListDTO> getFrequentlyBoughtWith(Long productId, int limit) {
        log.info("Calculating frequently bought with recommendations for product: {}", productId);
        List<Long> productIds = orderDetailRepository.findFrequentlyBoughtWithProductIds(productId, PageRequest.of(0, limit));
        
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }

        return productRepository.findAllById(productIds).stream()
                .filter(Product::getIsAvailable)
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gợi ý cá nhân hóa dựa trên danh mục hay mua nhất và các sản phẩm chưa mua
     */
    @Cacheable(value = "recommendations_personal", key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<ProductListDTO> getPersonalizedRecommendations(Long userId, int limit) {
        if (userId == null) {
            return getTopSelling(limit);
        }

        log.info("Calculating personalized recommendations for user: {}", userId);

        // 1. Lấy danh mục được mua nhiều nhất của User
        List<Long> categoryIds = orderDetailRepository.findMostBoughtCategoryIds(userId, PageRequest.of(0, 1));
        if (categoryIds.isEmpty()) {
            return getTopSelling(limit);
        }
        Long favoriteCategoryId = categoryIds.get(0);

        // 2. Lấy danh sách sản phẩm User đã từng mua
        List<Long> purchasedProductIds = orderDetailRepository.findPurchasedProductIds(userId);

        // 3. Lọc ra các sản phẩm thuộc danh mục yêu thích nhưng chưa mua
        List<Product> recommendations = productRepository.findByCategoryCategoryIdAndIsAvailableTrue(favoriteCategoryId)
                .stream()
                .filter(p -> !purchasedProductIds.contains(p.getProductId()))
                .limit(limit)
                .collect(Collectors.toList());

        // 4. Nếu số lượng gợi ý chưa đủ, bù thêm bằng các sản phẩm bán chạy nhất thuộc danh mục đó hoặc bán chạy toàn cửa hàng
        if (recommendations.size() < limit) {
            int needed = limit - recommendations.size();
            List<Product> extra = productRepository.findByCategoryCategoryIdAndIsAvailableTrue(favoriteCategoryId)
                    .stream()
                    .filter(p -> purchasedProductIds.contains(p.getProductId())) // Lấy cả những sản phẩm đã mua
                    .limit(needed)
                    .collect(Collectors.toList());
            recommendations.addAll(extra);
        }

        if (recommendations.isEmpty()) {
            return getTopSelling(limit);
        }

        return recommendations.stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm bán chạy nhất, cache 30 phút
     */
    @Cacheable(value = "recommendations_top", key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<ProductListDTO> getTopSelling(int limit) {
        log.info("Fetching top selling products for recommendations");
        return productRepository.findTopSelling(PageRequest.of(0, limit)).stream()
                .map(productMapper::toListDTO)
                .collect(Collectors.toList());
    }
}
