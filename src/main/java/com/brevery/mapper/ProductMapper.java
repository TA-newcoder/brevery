package com.brevery.mapper;

import com.brevery.dto.response.ProductDetailDTO;
import com.brevery.dto.response.ProductListDTO;
import com.brevery.dto.response.ReviewDTO;
import com.brevery.entity.Product;
import com.brevery.entity.ProductImage;
import com.brevery.entity.ProductVariant;
import com.brevery.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "primaryImageUrl", source = "images", qualifiedByName = "getPrimaryImageUrl")
    @Mapping(target = "minPrice", source = "variants", qualifiedByName = "getMinPrice")
    @Mapping(target = "maxPrice", source = "variants", qualifiedByName = "getMaxPrice")
    @Mapping(target = "avgRating", source = "reviews", qualifiedByName = "getAvgRating")
    @Mapping(target = "reviewCount", source = "reviews", qualifiedByName = "getReviewCount")
    ProductListDTO toListDTO(Product product);

    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "categoryId", source = "category.categoryId")
    @Mapping(target = "minPrice", source = "variants", qualifiedByName = "getMinPrice")
    @Mapping(target = "maxPrice", source = "variants", qualifiedByName = "getMaxPrice")
    @Mapping(target = "avgRating", source = "reviews", qualifiedByName = "getAvgRating")
    @Mapping(target = "reviewCount", source = "reviews", qualifiedByName = "getReviewCount")
    ProductDetailDTO toDetailDTO(Product product);

    ProductDetailDTO.VariantDTO toVariantDTO(ProductVariant variant);

    ProductDetailDTO.ImageDTO toImageDTO(ProductImage image);

    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    ReviewDTO toReviewDTO(Review review);

    @Named("getPrimaryImageUrl")
    default String getPrimaryImageUrl(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(ProductImage::getIsPrimary)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
    }

    @Named("getMinPrice")
    default BigDecimal getMinPrice(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return BigDecimal.ZERO;
        return variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Named("getMaxPrice")
    default BigDecimal getMaxPrice(List<ProductVariant> variants) {
        if (variants == null || variants.isEmpty()) return BigDecimal.ZERO;
        return variants.stream()
                .map(ProductVariant::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    @Named("getAvgRating")
    default Double getAvgRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .filter(Review::getIsVisible)
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    @Named("getReviewCount")
    default Long getReviewCount(List<Review> reviews) {
        if (reviews == null) return 0L;
        return reviews.stream()
                .filter(Review::getIsVisible)
                .count();
    }
}
