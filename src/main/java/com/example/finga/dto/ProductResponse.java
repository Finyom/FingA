package com.example.finga.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO für die Rückgabe eines Produkts an das Frontend.
 */
public record ProductResponse(

        Long id,

        String name,

        String description,

        BigDecimal price,

        Integer stock,

        Long shopId,

        /**
         * Bisheriger Fotopfad für Abwärtskompatibilität.
         */
        String photoPath,

        /**
         * Liste aller Produktfotos.
         */
        List<String> photoPaths,

        Instant createdAt,

        Instant updatedAt

) {
}