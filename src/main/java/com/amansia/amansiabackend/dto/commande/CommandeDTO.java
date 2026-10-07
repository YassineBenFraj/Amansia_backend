package com.amansia.amansiabackend.dto.commande;

import com.amansia.amansiabackend.entity.StatutCommande;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CommandeDTO {

    private Integer id;
    private Integer utilisateurId;
    private String clientNom;
    private String clientEmail;
    private String clientTelephone;
    private Integer adresseLivraisonId;
    private String adresseLivraison;
    private String villeLivraison;
    private String codePostalLivraison;
    private String paysLivraison;
    private String numeroCommande;
    private LocalDateTime dateCommande;
    private StatutCommande statut;
    private BigDecimal sousTotal;
    private BigDecimal sousTotalAvantPromotion;
    private BigDecimal fraisLivraison;
    private BigDecimal remise;
    private BigDecimal remiseProduits;
    private String codePromo;
    private BigDecimal total;
    private String commentaire;
    private List<LigneCommandeDTO> lignes;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    public String getClientNom(){return clientNom;} public void setClientNom(String v){clientNom=v;}
    public String getClientEmail(){return clientEmail;} public void setClientEmail(String v){clientEmail=v;}
    public String getClientTelephone(){return clientTelephone;} public void setClientTelephone(String v){clientTelephone=v;}

    public Integer getAdresseLivraisonId() {
        return adresseLivraisonId;
    }

    public void setAdresseLivraisonId(Integer adresseLivraisonId) {
        this.adresseLivraisonId = adresseLivraisonId;
    }
    public String getAdresseLivraison(){return adresseLivraison;} public void setAdresseLivraison(String v){adresseLivraison=v;}
    public String getVilleLivraison(){return villeLivraison;} public void setVilleLivraison(String v){villeLivraison=v;}
    public String getCodePostalLivraison(){return codePostalLivraison;} public void setCodePostalLivraison(String v){codePostalLivraison=v;}
    public String getPaysLivraison(){return paysLivraison;} public void setPaysLivraison(String v){paysLivraison=v;}

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }

    public LocalDateTime getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDateTime dateCommande) {
        this.dateCommande = dateCommande;
    }

    public StatutCommande getStatut() {
        return statut;
    }

    public void setStatut(StatutCommande statut) {
        this.statut = statut;
    }

    public BigDecimal getSousTotal() {
        return sousTotal;
    }

    public void setSousTotal(BigDecimal sousTotal) {
        this.sousTotal = sousTotal;
    }
    public BigDecimal getSousTotalAvantPromotion() { return sousTotalAvantPromotion; }
    public void setSousTotalAvantPromotion(BigDecimal value) { sousTotalAvantPromotion = value; }

    public BigDecimal getFraisLivraison() {
        return fraisLivraison;
    }

    public void setFraisLivraison(BigDecimal fraisLivraison) {
        this.fraisLivraison = fraisLivraison;
    }

    public BigDecimal getRemise() {
        return remise;
    }

    public void setRemise(BigDecimal remise) {
        this.remise = remise;
    }
    public BigDecimal getRemiseProduits() { return remiseProduits; }
    public void setRemiseProduits(BigDecimal remiseProduits) { this.remiseProduits = remiseProduits; }
    public String getCodePromo() { return codePromo; }
    public void setCodePromo(String codePromo) { this.codePromo = codePromo; }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public List<LigneCommandeDTO> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeDTO> lignes) {
        this.lignes = lignes;
    }
}
