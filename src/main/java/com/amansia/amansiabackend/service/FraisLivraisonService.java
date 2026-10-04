package com.amansia.amansiabackend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FraisLivraisonService {
    private final BigDecimal montant;

    public FraisLivraisonService(@Value("${app.livraison.frais-fixes}") BigDecimal montant) {
        if (montant.signum() < 0) {
            throw new IllegalArgumentException("Les frais de livraison configures doivent etre positifs ou nuls");
        }
        this.montant = montant.setScale(2, RoundingMode.UNNECESSARY);
    }

    public BigDecimal calculer() {
        return montant;
    }
}
