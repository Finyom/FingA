package com.example.finga.dto;
import java.time.Instant;
public record ShopResponse(Long id,
                           String name,
                           String description,
                           String address,
                           String photoPath,
                           Instant createdAt,
                           Instant updatedAt) {}
