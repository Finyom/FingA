package com.example.finga.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        List<Long> shopIds
) {}