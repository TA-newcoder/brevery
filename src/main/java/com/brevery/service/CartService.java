package com.brevery.service;

import com.brevery.dto.request.AddToCartRequest;
import com.brevery.dto.response.CartItemResponse;
import com.brevery.dto.response.CartResponse;
import com.brevery.entity.CartItem;
import com.brevery.entity.ProductVariant;
import com.brevery.entity.User;
import com.brevery.enums.ErrorCode;
import com.brevery.exception.AppException;
import com.brevery.mapper.OrderMapper;
import com.brevery.repository.CartItemRepository;
import com.brevery.repository.ProductVariantRepository;
import com.brevery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        log.info("Getting cart for user ID: {}", userId);
        List<CartItem> items = cartItemRepository.findByUserUserId(userId);
        
        List<CartItemResponse> itemResponses = items.stream()
                .map(orderMapper::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(itemResponses)
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void addToCart(Long userId, AddToCartRequest request) {
        log.info("Adding variant {} with quantity {} to user {}'s cart", request.getVariantId(), request.getQuantity(), userId);

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        if (!Boolean.TRUE.equals(variant.getIsAvailable()) || variant.getStock() < request.getQuantity()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Số lượng yêu cầu vượt quá tồn kho khả dụng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        cartItemRepository.findByUserUserIdAndVariantVariantId(userId, request.getVariantId())
                .ifPresentOrElse(
                        existingItem -> {
                            int newQty = existingItem.getQuantity() + request.getQuantity();
                            if (variant.getStock() < newQty) {
                                throw new AppException(ErrorCode.VALIDATION_ERROR, "Tồn kho không đủ để thêm số lượng này");
                            }
                            existingItem.setQuantity(newQty);
                            cartItemRepository.save(existingItem);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .user(user)
                                    .variant(variant)
                                    .quantity(request.getQuantity())
                                    .build();
                            cartItemRepository.save(newItem);
                        }
                );
    }

    @Transactional
    public void updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Mục giỏ hàng không tồn tại"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn không có quyền chỉnh sửa giỏ hàng này");
        }

        ProductVariant variant = item.getVariant();
        if (variant.getStock() < quantity) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Tồn kho không đủ cung cấp số lượng này");
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.VALIDATION_ERROR, "Mục giỏ hàng không tồn tại"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Bạn không có quyền chỉnh sửa giỏ hàng này");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearMyCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);
        cartItemRepository.deleteByUserUserId(userId);
    }
}
