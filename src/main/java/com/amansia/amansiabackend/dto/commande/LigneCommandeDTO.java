package com.amansia.amansiabackend.dto.commande;

import java.math.BigDecimal;

public class LigneCommandeDTO {

    private Integer id;
    private Integer produitId;
    private String produitNom;
    private Integer varianteId;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;

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
    public String getProduitNom(){return produitNom;} public void setProduitNom(String v){produitNom=v;}

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

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }
}
