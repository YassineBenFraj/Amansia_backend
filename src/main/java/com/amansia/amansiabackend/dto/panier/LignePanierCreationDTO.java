package com.amansia.amansiabackend.dto.panier;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LignePanierCreationDTO {

    @NotNull(message = "Le produit est obligatoire")
    private Integer produitId;

    private Integer varianteId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à zéro")
    private Integer quantite;

    public Integer getProduitId() {
        return produitId;
    }

    public void setProduitId(Integer produitId) {
        this.produitId = produitId;
    }

    public Integer getVarianteId() {
        return varianteId;
    }

    public void setVarianteId(Integer varianteId) {
        this.varianteId = varianteId;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }
}
