package com.example.finga.dto;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size;
public record ShopRequest(@NotBlank @Size(max=120) String name,@Size(max=500) String description,@NotBlank @Size(max=255) String address) {}
