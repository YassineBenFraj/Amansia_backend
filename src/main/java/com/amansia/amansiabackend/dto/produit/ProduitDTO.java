package com.amansia.amansiabackend.dto.produit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProduitDTO {

    private Integer id;
    private Integer categorieId;
    private String categorieNom;
    private String nom;
    private String description;
    private BigDecimal prix;
    private BigDecimal prixPromotion;
    private String marque;
    private BigDecimal largeur;
    private BigDecimal hauteur;
    private BigDecimal profondeur;
    private BigDecimal poids;
    private Boolean personnalisable;
    private Boolean actif;
    private Boolean disponible;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(Integer categorieId) {
        this.categorieId = categorieId;
    }

    public String getCategorieNom() {
        return categorieNom;
    }

    public void setCategorieNom(String categorieNom) {
        this.categorieNom = categorieNom;
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

    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

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

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }
}
