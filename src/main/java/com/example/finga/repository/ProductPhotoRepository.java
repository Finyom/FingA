package com.example.finga.repository;

import com.example.finga.dto.ProductPhotoPath;
import com.example.finga.entity.ProductPhoto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductPhotoRepository
        extends JpaRepository<ProductPhoto, Long> {

    List<ProductPhoto> findByProductId(Long productId);

    long countByProductId(Long productId);

    /**
     * Lädt Produkt-ID und Fotopfad für mehrere Produkte,
     * ohne die Product-Entity zu laden.
     */
    @Query("""
        SELECT
            photo.product.id AS productId,
            photo.photoPath AS photoPath
        FROM ProductPhoto photo
        WHERE photo.product.id IN :productIds
        ORDER BY photo.product.id, photo.id
        """)
    List<ProductPhotoPath> findPhotoPathsByProductIds(
            @Param("productIds") Collection<Long> productIds
    );
}

