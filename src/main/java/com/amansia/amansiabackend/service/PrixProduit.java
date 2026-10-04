package com.amansia.amansiabackend.service;

import com.amansia.amansiabackend.entity.Produit;
import com.amansia.amansiabackend.entity.VarianteProduit;
import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PrixProduit {
    private PrixProduit() {}

    public static BigDecimal calculer(Produit produit, VarianteProduit variante) {
        BigDecimal prix = produit.getPrix();
        BigDecimal promotion = produit.getPrixPromotion();
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
        return prix.setScale(2, RoundingMode.HALF_UP);
    }
}
