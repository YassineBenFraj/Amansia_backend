package com.amansia.amansiabackend.dto.stock;

import java.time.LocalDateTime;

public record StockDTO(Integer varianteId, String reference, Integer produitId, String produitNom,
                       String couleur, String materiau, Integer quantite, Integer seuilAlerte,
                       LocalDateTime dateMiseAJour) {}
