package com.amansia.amansiabackend.dto.panier;

import com.amansia.amansiabackend.dto.produit.ProduitDTO;
import com.amansia.amansiabackend.dto.variante.VarianteProduitDTO;

import java.math.BigDecimal;

public class LignePanierDTO {

    private Integer id;
    private ProduitDTO produit;
    private VarianteProduitDTO variante;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal prixTotal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProduitDTO getProduit() {
        return produit;
    }

    public void setProduit(ProduitDTO produit) {
        this.produit = produit;
    }

    public VarianteProduitDTO getVariante() {
        return variante;
    }

    public void setVariante(VarianteProduitDTO variante) {
        this.variante = variante;
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
