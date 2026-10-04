package com.amansia.amansiabackend.dto.stock;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MouvementStockRequest(@NotNull Integer varianteId, @NotBlank String typeMouvement,
                                    @NotNull @Min(1) Integer quantite, @NotBlank @Size(max=255) String motif) {}
