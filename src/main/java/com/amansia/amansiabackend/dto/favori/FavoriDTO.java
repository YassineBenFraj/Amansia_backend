package com.amansia.amansiabackend.dto.favori;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FavoriDTO(
        Integer id,
        Integer produitId,
        String nom,
        BigDecimal prix,
        BigDecimal prixPromotion,
        String imageUrl,
        Boolean actif,
        LocalDateTime dateCreation) {
}
