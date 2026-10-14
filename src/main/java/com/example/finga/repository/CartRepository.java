
package com.example.finga.repository;

import com.example.finga.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser_IdAndShop_Id(Long userId, Long shopId);
}