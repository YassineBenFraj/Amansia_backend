package com.amansia.amansiabackend.dto.paiement;

import com.amansia.amansiabackend.entity.MethodePaiement;
import com.amansia.amansiabackend.entity.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaiementDTO {

    private Integer id;
    private Integer commandeId;
    private String numeroCommande;
    private Integer clientId;
    private String clientNom;
    private String clientEmail;
    private BigDecimal montant;
    private MethodePaiement methode;
    private StatutPaiement statut;
    private String reference;
    private LocalDateTime datePaiement;
    private LocalDateTime dateCreation;
    private String clientSecret;
    private String publishableKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Integer commandeId) {
        this.commandeId = commandeId;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public MethodePaiement getMethode() {
        return methode;
    }

    public void setMethode(MethodePaiement methode) {
        this.methode = methode;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getCheckoutUrl() { return null; }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getNumeroCommande(){return numeroCommande;} public void setNumeroCommande(String v){numeroCommande=v;}
    public Integer getClientId(){return clientId;} public void setClientId(Integer v){clientId=v;}
    public String getClientNom(){return clientNom;} public void setClientNom(String v){clientNom=v;}
    public String getClientEmail(){return clientEmail;} public void setClientEmail(String v){clientEmail=v;}

    public String getPublishableKey() { return publishableKey; }
    public void setPublishableKey(String publishableKey) { this.publishableKey = publishableKey; }
}


