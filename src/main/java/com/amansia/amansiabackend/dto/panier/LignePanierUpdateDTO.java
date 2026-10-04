package com.amansia.amansiabackend.dto.panier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LignePanierUpdateDTO {

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à zéro")
    private Integer quantite;

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}
