package com.amansia.amansiabackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produits")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "prix", nullable = false, precision = 12, scale = 3)
    private BigDecimal prix;

    @Column(name = "prix_promotion", precision = 12, scale = 3)
    private BigDecimal prixPromotion;

    @Column(name = "promotion_active", nullable = false)
    private Boolean promotionActive = false;

    @Column(name = "promotion_date_debut")
    private LocalDateTime promotionDateDebut;

    @Column(name = "promotion_date_fin")
    private LocalDateTime promotionDateFin;

    @Column(name = "marque", length = 150)
    private String marque;

    @Column(name = "largeur", precision = 10, scale = 2)
    private BigDecimal largeur;

    @Column(name = "hauteur", precision = 10, scale = 2)
    private BigDecimal hauteur;

    @Column(name = "profondeur", precision = 10, scale = 2)
    private BigDecimal profondeur;

    @Column(name = "poids", precision = 10, scale = 2)
    private BigDecimal poids;

    @Column(name = "personnalisable", nullable = false)
    private Boolean personnalisable;

    @Column(name = "actif", nullable = false)
    private Boolean actif;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        dateCreation = now;
        dateModification = now;
        if (personnalisable == null) {
            personnalisable = false;
        }
        if (actif == null) {
            actif = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
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

    public Boolean getPromotionActive() { return promotionActive; }
    public void setPromotionActive(Boolean promotionActive) { this.promotionActive = promotionActive; }
    public LocalDateTime getPromotionDateDebut() { return promotionDateDebut; }
    public void setPromotionDateDebut(LocalDateTime promotionDateDebut) { this.promotionDateDebut = promotionDateDebut; }
    public LocalDateTime getPromotionDateFin() { return promotionDateFin; }
    public void setPromotionDateFin(LocalDateTime promotionDateFin) { this.promotionDateFin = promotionDateFin; }

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
