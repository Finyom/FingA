package com.example.finga.repository;

import com.example.finga.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Sucht ein Produkt innerhalb eines bestimmten Warenkorbs.
    Optional<CartItem> findByCart_IdAndProduct_Id(
            Long cartId,
            Long productId
    );
}
