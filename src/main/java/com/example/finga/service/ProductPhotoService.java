package com.example.finga.service;

import com.example.finga.entity.Product;
import com.example.finga.entity.ProductPhoto;
import com.example.finga.entity.Shop;
import com.example.finga.repository.ProductPhotoRepository;
import com.example.finga.repository.ProductRepository;
import com.example.finga.repository.ShopRepository;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ProductPhotoService {

    private static final int MAX_PHOTOS = 4;

    private final ProductRepository productRepository;
    private final ProductPhotoRepository productPhotoRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public ProductPhotoService(
            ProductRepository productRepository,
            ProductPhotoRepository productPhotoRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            @Value("${product.upload-dir:uploads/products}")
            String uploadDir) {

        this.productRepository = productRepository;
        this.productPhotoRepository = productPhotoRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;

        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    /**
     * Lädt ein Produktfoto hoch.
     * Ein Produkt darf maximal vier Fotos besitzen.
     */
    @Transactional
    public void uploadPhoto(
            Long shopId,
            Long productId,
            MultipartFile file,
            Authentication authentication) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte ein Bild auswählen"
            );
        }

        if (authentication == null
                || authentication.getName() == null) {
            throw new AccessDeniedException(
                    "Authentifizierung erforderlich"
            );
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Shop nicht gefunden"
                ));

        Product product = productRepository
                .findByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Produkt nicht gefunden"
                ));

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Benutzer nicht gefunden"
                ));

        checkPermission(user, shop);

        // Dateityp prüfen.
        String contentType = file.getContentType();

        String extension = getExtension(contentType);

        // Maximale Anzahl von vier Fotos prüfen.
        long existingPhotos =
                productPhotoRepository.countByProductId(productId);

        if (existingPhotos >= MAX_PHOTOS) {
            throw new IllegalArgumentException(
                    "Ein Produkt darf maximal vier Fotos besitzen"
            );
        }

        Files.createDirectories(uploadDirectory);

        // Eindeutigen Dateinamen erzeugen.
        String fileName = UUID.randomUUID() + extension;

        Path target = uploadDirectory.resolve(fileName).normalize();

        if (!target.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Ungültiger Dateipfad"
            );
        }

        // Datei speichern.
        Files.copy(file.getInputStream(), target);

        // Datenbankeintrag für das Foto erstellen.
        ProductPhoto photo = new ProductPhoto();
        photo.setProduct(product);
        photo.setPhotoPath("products/" + fileName);

        try {
            productPhotoRepository.save(photo);
        } catch (RuntimeException exception) {
            // Datei entfernen, falls das Speichern fehlschlägt.
            Files.deleteIfExists(target);
            throw exception;
        }
    }

    private void checkPermission(User user, Shop shop) {

        if ("ADMIN".equals(user.getRole())) {
            return;
        }

        if ("USER_ADMIN".equals(user.getRole())) {
            boolean assigned = userRepository.isAssignedToShop(
                    user.getId(),
                    shop.getId()
            );

            if (assigned) {
                return;
            }
        }

        throw new AccessDeniedException(
                "Sie dürfen Fotos dieses Produkts nicht ändern"
        );
    }

    private String getExtension(String contentType) {

        if (contentType == null) {
            throw new IllegalArgumentException(
                    "Der Dateityp konnte nicht erkannt werden"
            );
        }

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Nur JPG, PNG und WebP sind erlaubt"
            );
        };
    }
}

