package com.amansia.amansiabackend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionDTO(Integer produitId, String produitNom, BigDecimal prixNormal,
                           BigDecimal prixPromotion, LocalDateTime dateDebut,
                           LocalDateTime dateFin, boolean actif, boolean applicable) {}
