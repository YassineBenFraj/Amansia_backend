package com.amansia.amansiabackend.dto.variante;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VarianteProduitDTO {

    private Integer id;
    private Integer produitId;
    private Integer materiauId;
    private Integer couleurId;
    private String reference;
    private BigDecimal prixSupplementaire;
    private Boolean actif;
    private Integer quantiteDisponible;
    private LocalDateTime dateCreation;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProduitId() {
        return produitId;
    }

    public void setProduitId(Integer produitId) {
        this.produitId = produitId;
    }

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

    public Integer getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(Integer quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

}
