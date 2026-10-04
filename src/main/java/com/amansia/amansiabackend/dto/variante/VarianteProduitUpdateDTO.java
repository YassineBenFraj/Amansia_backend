package com.amansia.amansiabackend.dto.variante;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class VarianteProduitUpdateDTO {


    private Integer materiauId;

    private Integer couleurId;

    @NotBlank
    private String reference;

    @PositiveOrZero
    private BigDecimal prixSupplementaire;

    private Boolean actif;

    public Integer getMateriauId() {
        return materiauId;
    }

    public void setMateriauId(Integer materiauId) {
        this.materiauId = materiauId;
    }

    public Integer getCouleurId() {
        return couleurId;
    }

    public void setCouleurId(Integer couleurId) {
        this.couleurId = couleurId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public BigDecimal getPrixSupplementaire() {
        return prixSupplementaire;
    }

    public void setPrixSupplementaire(BigDecimal prixSupplementaire) {
        this.prixSupplementaire = prixSupplementaire;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}
