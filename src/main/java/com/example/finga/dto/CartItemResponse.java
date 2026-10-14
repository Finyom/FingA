package com.example.finga.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        Long shopId,
        String shopName,
        BigDecimal price,
        Integer quantity,
        BigDecimal lineTotal,
        String photoPath
) {
}

