
package com.example.finga.service;

import com.example.finga.dto.CartItemResponse;
import com.example.finga.dto.CartResponse;
import com.example.finga.entity.Cart;
import com.example.finga.entity.CartItem;
import com.example.finga.entity.Product;
import com.example.finga.entity.Shop;
import com.example.finga.repository.CartItemRepository;
import com.example.finga.repository.CartRepository;
import com.example.finga.repository.ProductRepository;
import com.example.finga.repository.ShopRepository;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            ShopRepository shopRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
    }

    // Liefert den Warenkorb des Benutzers für den ausgewählten Shop.
    public CartResponse getCart(String username, Long shopId) {
        Cart cart = getOrCreateCart(username, shopId);
        return toResponse(cart);
    }

    // Fügt ein Produkt hinzu oder erhöht die vorhandene Menge.
    public CartResponse addItem(
            String username,
            Long shopId,
            Long productId,
            Integer quantity
    ) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(username, shopId);
        Product product = getProductForShop(productId, shopId);

        CartItem item = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElse(null);

        // Prüfen, ob die gewünschte zusätzliche Menge verfügbar ist.
        checkStock(product, quantity);

        // Lagerbestand um die neu hinzugefügte Menge reduzieren.
        product.setStock(product.getStock() - quantity);

        if (item == null) {
            // Neues Produkt zum Warenkorb hinzufügen.
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);

            cart.getItems().add(item);
        } else {
            // Vorhandene Warenkorbmenge erhöhen.
            item.setQuantity(item.getQuantity() + quantity);
        }

        productRepository.save(product);
        cartRepository.save(cart);

        return toResponse(cart);
    }

    // Ändert die Menge eines Produkts im Warenkorb.
    public CartResponse updateQuantity(
            String username,
            Long shopId,
            Long productId,
            Integer quantity
    ) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(username, shopId);
        getProductForShop(productId, shopId);

        CartItem item = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in cart"
                ));

        Product product = item.getProduct();

        int oldQuantity = item.getQuantity();
        int difference = quantity - oldQuantity;

        // Bei einer Erhöhung nur die zusätzliche Menge prüfen.
        if (difference > 0) {
            checkStock(product, difference);
        }

        // Erhöhung reduziert, Verringerung erhöht den Lagerbestand.
        product.setStock(product.getStock() - difference);
        item.setQuantity(quantity);

        productRepository.save(product);
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    // Entfernt ein Produkt und gibt seinen reservierten Bestand frei.

    public CartResponse removeItem(
            String username,
            Long shopId,
            Long productId,
            Integer quantity
    ) {
        validateQuantity(quantity);

        Cart cart = getOrCreateCart(username, shopId);

        getProductForShop(productId, shopId);

        CartItem item = cartItemRepository
                .findByCart_IdAndProduct_Id(cart.getId(), productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found in cart"
                ));

        // Die zu entfernende Menge darf nicht größer sein
        // als die Menge im Warenkorb.
        if (quantity > item.getQuantity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot remove more items than the cart contains"
            );
        }

        Product product = item.getProduct();

        // Die entfernte Menge wird wieder zum Lagerbestand addiert.
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        // Nur die ausgewählte Menge entfernen.
        if (quantity.equals(item.getQuantity())) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(item.getQuantity() - quantity);
            cartItemRepository.save(item);
        }

        return toResponse(cart);
    }

    // Sucht Benutzer und Shop und lädt oder erstellt deren Warenkorb.
    private Cart getOrCreateCart(String username, Long shopId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Shop not found"
                ));

        return cartRepository
                .findByUser_IdAndShop_Id(user.getId(), shop.getId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setShop(shop);
                    cart.setItems(new ArrayList<>());

                    return cartRepository.save(cart);
                });
    }

    // Prüft, ob das Produkt existiert und zum ausgewählten Shop gehört.
    private Product getProductForShop(Long productId, Long shopId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (product.getShop() == null
                || !product.getShop().getId().equals(shopId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product does not belong to the selected shop"
            );
        }

        return product;
    }

    // Verhindert ungültige Mengen.
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }
    }

    // Prüft, ob genügend Lagerbestand vorhanden ist.
    private void checkStock(Product product, int quantity) {
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Requested quantity exceeds available stock"
            );
        }
    }

    // Wandelt die Entities in eine API-Antwort um.
    private CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();

                    BigDecimal lineTotal = product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    return new CartItemResponse(
                            item.getId(),
                            product.getId(),
                            product.getName(),
                            product.getShop().getId(),
                            product.getShop().getName(),
                            product.getPrice(),
                            item.getQuantity(),
                            lineTotal,
                            product.getPhotoPath()
                    );
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, total);
    }
}