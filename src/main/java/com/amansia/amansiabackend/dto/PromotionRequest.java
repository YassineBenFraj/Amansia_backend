package com.amansia.amansiabackend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PromotionRequest(@NotNull Integer produitId, @NotNull BigDecimal prixPromotion,
                               @NotNull LocalDateTime dateDebut, @NotNull LocalDateTime dateFin,
                               @NotNull Boolean actif) {}
