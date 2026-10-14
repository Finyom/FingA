package com.example.finga.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity für ein Produktfoto.
 *
 * Ein Produkt kann mehrere Fotos besitzen.
 */
@Entity
@Table(
        name = "product_photos",
        indexes = @Index(
                name = "idx_product_photos_product_id",
                columnList = "product_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Produkt, zu dem das Foto gehört.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Relativer Pfad zum gespeicherten Foto.
     */
    @Column(name = "photo_path", nullable = false, length = 500)
    private String photoPath;
}

