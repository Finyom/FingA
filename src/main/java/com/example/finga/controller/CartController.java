
package com.example.finga.controller;

import com.example.finga.dto.AddCartItemRequest;
import com.example.finga.dto.CartResponse;
import com.example.finga.dto.UpdateCartItemRequest;
import com.example.finga.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/shops/{shopId}/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Prüft, ob ein Benutzer angemeldet ist.
    private String getUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return authentication.getName();
    }

    // Warenkorb des Benutzers für den ausgewählten Shop abrufen.
    @GetMapping
    public CartResponse getCart(
            Authentication authentication,
            @PathVariable Long shopId
    ) {
        return cartService.getCart(
                getUsername(authentication),
                shopId
        );
    }

    // Produkt zum Warenkorb dieses Shops hinzufügen.
    @PostMapping("/items")
    public CartResponse addItem(
            Authentication authentication,
            @PathVariable Long shopId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartService.addItem(
                getUsername(authentication),
                shopId,
                request.productId(),
                request.quantity()
        );
    }

    // Produktmenge im Warenkorb dieses Shops ändern.
    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(
            Authentication authentication,
            @PathVariable Long shopId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateQuantity(
                getUsername(authentication),
                shopId,
                productId,
                request.quantity()
        );
    }

    // Produkt aus dem Warenkorb dieses Shops entfernen.

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            Authentication authentication,
            @PathVariable Long shopId,
            @PathVariable Long productId,
            @RequestParam Integer quantity
    ) {
        return cartService.removeItem(
                getUsername(authentication),
                shopId,
                productId,
                quantity
        );
    }
}