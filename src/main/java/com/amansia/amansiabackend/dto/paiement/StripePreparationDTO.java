package com.amansia.amansiabackend.dto.paiement;
public record StripePreparationDTO(String clientSecret, String publishableKey,
                                   java.math.BigDecimal sousTotal, java.math.BigDecimal remise,
                                   java.math.BigDecimal total, String codePromo) {}
