package com.example.finga.controller;

import com.example.finga.service.UserShopService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.example.finga.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserShopController {

    private final UserShopService userShopService;


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userShopService.getAllUsers());
    }

    @PostMapping("/{userId}/shops/{shopId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignShop(
            @PathVariable Long userId,
            @PathVariable Long shopId) {

        userShopService.assignShop(userId, shopId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/shops/{shopId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeShop(
            @PathVariable Long userId,
            @PathVariable Long shopId) {

        userShopService.removeShop(userId, shopId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role/user-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> makeUserAdmin(
            @PathVariable Long userId) {

        userShopService.changeRoleToUserAdmin(userId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/role/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> makeUser(
            @PathVariable Long userId) {

        userShopService.changeRoleToUser(userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                userShopService.getCurrentUser(
                        authentication.getName()
                )
        );
    }
}