package com.brevery.repository;

import com.brevery.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserUserId(Long userId);
    Optional<CartItem> findByUserUserIdAndVariantVariantId(Long userId, Long variantId);
    void deleteByUserUserId(Long userId);
}
