 package com.example.finga.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity für ein Produkt.
 *
 * Ein Produkt gehört immer genau zu einem Shop.
 */
@Entity
@Table(
        name = "products",
        indexes = @Index(
                name = "idx_products_shop_id",
                columnList = "shop_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Product {

 /**
  * Eindeutige ID des Produkts.
  */
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 /**
  * Name des Produkts.
  */
 @Column(nullable = false, length = 150)
 private String name;

 /**
  * Beschreibung des Produkts.
  */
 @Column(length = 1000)
 private String description;

 /**
  * Preis des Produkts.
  */
 @Column(nullable = false, precision = 12, scale = 2)
 private BigDecimal price;

 /**
  * Lagerbestand.
  */
 @Column(nullable = false)
 private Integer stock;

 /**
  * Shop, zu dem das Produkt gehört.
  */
 @ManyToOne(fetch = FetchType.LAZY, optional = false)
 @JoinColumn(name = "shop_id", nullable = false)
 private Shop shop;

 /**
  * Relativer Pfad zum Produktfoto.
  *
  * Beispiel:
  * products/25.png
  */
 @Column(name = "photo_path", length = 500)
 private String photoPath;

/**
 * Fotos dieses Produkts.
 *
 * Ein Produkt darf maximal vier Fotos besitzen.
 */
 @OneToMany(
         mappedBy = "product",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private List<ProductPhoto> photos = new ArrayList<>();


 /**
  * Erstellungszeitpunkt.
  */
 @Column(
         name = "created_at",
         nullable = false,
         updatable = false
 )
 private Instant createdAt;

 /**
  * Zeitpunkt der letzten Änderung.
  */
 @Column(name = "updated_at", nullable = false)
 private Instant updatedAt;

 /**
  * Wird automatisch vor dem ersten Speichern ausgeführt.
  */
 @PrePersist
 void prePersist() {

  createdAt = Instant.now();
  updatedAt = createdAt;
 }

 /**
  * Wird automatisch vor jeder Änderung ausgeführt.
  */
 @PreUpdate
 void preUpdate() {

  updatedAt = Instant.now();
 }
}

