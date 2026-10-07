package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.VarianteProduit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public final class PrixProduit {
    private PrixProduit() {}

    public static BigDecimal calculer(Produit produit, VarianteProduit variante) {
        BigDecimal prix = produit.getPrix();
        BigDecimal promotion = promotionApplicable(produit) ? produit.getPrixPromotion() : null;
        if (prix == null || prix.signum() < 0) {
            throw new IllegalArgumentException("Prix produit invalide");
        }
        if (promotion != null && promotion.signum() >= 0 && promotion.compareTo(prix) <= 0) {
            prix = promotion;
        }
        if (variante != null && variante.getPrixSupplementaire() != null) {
            if (variante.getPrixSupplementaire().signum() < 0) {
                throw new IllegalArgumentException("Supplement invalide");
            }
            prix = prix.add(variante.getPrixSupplementaire());
        }
        return prix.setScale(3, RoundingMode.HALF_UP);
    }

    public static boolean promotionApplicable(Produit produit) {
        if (produit == null || !Boolean.TRUE.equals(produit.getPromotionActive())
                || produit.getPrixPromotion() == null || produit.getPrix() == null
                || produit.getPrixPromotion().signum() < 0
                || produit.getPrixPromotion().compareTo(produit.getPrix()) >= 0) return false;
        LocalDateTime now = LocalDateTime.now();
        // Existing promotions have no dates. Null bounds keep those prices intact until scheduled by an admin.
        return (produit.getPromotionDateDebut() == null || !now.isBefore(produit.getPromotionDateDebut()))
                && (produit.getPromotionDateFin() == null || !now.isAfter(produit.getPromotionDateFin()));
    }
}
