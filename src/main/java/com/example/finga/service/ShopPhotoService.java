package com.example.finga.service;

import com.example.finga.entity.Shop;
import com.example.finga.repository.ShopRepository;
import com.example.finga.user.User;
import com.example.finga.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ShopPhotoService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public ShopPhotoService(
            ShopRepository shopRepository,
            UserRepository userRepository,
            @Value("${shop.upload-dir}") String uploadDir) {

        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    public void uploadPhoto(
            Long shopId,
            MultipartFile file,
            Authentication authentication) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Bitte ein Bild auswählen"
            );
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Shop nicht gefunden"
                        ));

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Benutzer nicht gefunden"
                        ));

        checkPermission(user, shop);

        String contentType = file.getContentType();

        if (!"image/jpeg".equals(contentType)
                && !"image/png".equals(contentType)
                && !"image/webp".equals(contentType)) {

            throw new IllegalArgumentException(
                    "Nur JPG, PNG und WebP sind erlaubt"
            );
        }

        Files.createDirectories(uploadDirectory);

        String extension = getExtension(contentType);

        String fileName = shop.getId() + extension;

        Path target = uploadDirectory
                .resolve(fileName)
                .normalize();

        if (!target.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Ungültiger Dateipfad"
            );
        }

        Files.copy(
                file.getInputStream(),
                target,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
        );

        shop.setPhotoPath(
                "shops/" + fileName
        );

        shopRepository.save(shop);
    }

    private void checkPermission(
            User user,
            Shop shop) {

        if ("ADMIN".equals(user.getRole())) {
            return;
        }

        if ("USER_ADMIN".equals(user.getRole())) {

            boolean assigned =
                    userRepository.isAssignedToShop(
                            user.getId(),
                            shop.getId()
                    );

            if (assigned) {
                return;
            }
        }

        throw new AccessDeniedException(
                "Sie dürfen das Foto dieses Shops nicht ändern"
        );
    }

    private String getExtension(String contentType) {

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Nicht unterstütztes Bildformat"
            );
        };
    }
}