package com.amansia.amansiabackend.dto.stock;

import java.time.LocalDateTime;

public record MouvementStockDTO(Integer id, Integer varianteId, String produitNom, String referenceVariante,
                                String typeMouvement, Integer quantite, String motif, String reference,
                                LocalDateTime dateMouvement) {}
