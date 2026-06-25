package com.brevery.mapper;

import com.brevery.dto.response.CartItemResponse;
import com.brevery.dto.response.OrderDetailResponse;
import com.brevery.dto.response.OrderResponse;
import com.brevery.entity.CartItem;
import com.brevery.entity.Order;
import com.brevery.entity.OrderDetail;
import com.brevery.entity.ShippingDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "shippingAddress", source = "shippingDetail", qualifiedByName = "getFullAddress")
    @Mapping(target = "receiverName", source = "shippingDetail.recipientName")
    @Mapping(target = "receiverPhone", source = "shippingDetail.phone")
    OrderResponse toResponse(Order order);

    @Mapping(target = "orderDetailId", source = "detailId")
    @Mapping(target = "productId", source = "variant.product.productId")
    @Mapping(target = "variantId", source = "variant.variantId")
    @Mapping(target = "productSize", source = "variantInfo")
    @Mapping(target = "price", source = "unitPrice")
    @Mapping(target = "primaryImageUrl", source = "variant.product.images", qualifiedByName = "getPrimaryImageUrl")
    OrderDetailResponse toDetailResponse(OrderDetail detail);

    @Mapping(target = "variantId", source = "variant.variantId")
    @Mapping(target = "productId", source = "variant.product.productId")
    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "productSize", source = "variant.size")
    @Mapping(target = "price", expression = "java(item.getVariant().getSalePrice() != null ? item.getVariant().getSalePrice() : item.getVariant().getPrice())")
    @Mapping(target = "primaryImageUrl", source = "variant.product.images", qualifiedByName = "getPrimaryImageUrl")
    @Mapping(target = "subTotal", expression = "java((item.getVariant().getSalePrice() != null ? item.getVariant().getSalePrice() : item.getVariant().getPrice()).multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    @Mapping(target = "availableVariants", expression = "java(mapVariants(item.getVariant().getProduct().getVariants()))")
    CartItemResponse toCartItemResponse(CartItem item);

    default java.util.List<com.brevery.dto.response.ProductDetailDTO.VariantDTO> mapVariants(java.util.List<com.brevery.entity.ProductVariant> variants) {
        if (variants == null) return null;
        return variants.stream().map(v -> com.brevery.dto.response.ProductDetailDTO.VariantDTO.builder()
                .variantId(v.getVariantId())
                .size(v.getSize())
                .price(v.getPrice())
                .salePrice(v.getSalePrice())
                .stock(v.getStock())
                .isAvailable(v.getIsAvailable())
                .build()).collect(java.util.stream.Collectors.toList());
    }

    @Named("getFullAddress")
    default String getFullAddress(ShippingDetail shippingDetail) {
        if (shippingDetail == null) return null;
        // Tránh in ra các dấu phẩy thừa nếu tỉnh/huyện trống
        StringBuilder sb = new StringBuilder(shippingDetail.getAddressDetail());
        if (shippingDetail.getWard() != null && !shippingDetail.getWard().isBlank()) {
            sb.append(", ").append(shippingDetail.getWard());
        }
        if (shippingDetail.getDistrict() != null && !shippingDetail.getDistrict().isBlank()) {
            sb.append(", ").append(shippingDetail.getDistrict());
        }
        if (shippingDetail.getProvince() != null && !shippingDetail.getProvince().isBlank()) {
            sb.append(", ").append(shippingDetail.getProvince());
        }
        return sb.toString();
    }

    @Named("getPrimaryImageUrl")
    default String getPrimaryImageUrl(List<com.brevery.entity.ProductImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(com.brevery.entity.ProductImage::getIsPrimary)
                .map(com.brevery.entity.ProductImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
    }
}
