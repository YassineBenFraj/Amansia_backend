package com.amansia.amansiabackend.dto.produit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ProduitCreationDTO {

    @NotNull
    private Integer categorieId;

    @NotBlank
    @Size(max = 200)
    private String nom;

    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal prix;

    @DecimalMin(value = "0.00")
    private BigDecimal prixPromotion;

    @Size(max = 150)
    private String marque;

    @DecimalMin(value = "0.01")
    private BigDecimal largeur;

    @DecimalMin(value = "0.01")
    private BigDecimal hauteur;

    @DecimalMin(value = "0.01")
    private BigDecimal profondeur;

    @DecimalMin(value = "0.01")
    private BigDecimal poids;

    private Boolean personnalisable;

    public Integer getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Integer categorieId) {
        this.categorieId = categorieId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public BigDecimal getPrixPromotion() {
        return prixPromotion;
    }

    public void setPrixPromotion(BigDecimal prixPromotion) {
        this.prixPromotion = prixPromotion;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public BigDecimal getLargeur() {
        return largeur;
    }

    public void setLargeur(BigDecimal largeur) {
        this.largeur = largeur;
    }

    public BigDecimal getHauteur() {
        return hauteur;
    }

    public void setHauteur(BigDecimal hauteur) {
        this.hauteur = hauteur;
    }

    public BigDecimal getProfondeur() {
        return profondeur;
    }

    public void setProfondeur(BigDecimal profondeur) {
        this.profondeur = profondeur;
    }

    public BigDecimal getPoids() {
        return poids;
    }

    public void setPoids(BigDecimal poids) {
        this.poids = poids;
    }

    public Boolean getPersonnalisable() {
        return personnalisable;
    }

    public void setPersonnalisable(Boolean personnalisable) {
        this.personnalisable = personnalisable;
    }
}
